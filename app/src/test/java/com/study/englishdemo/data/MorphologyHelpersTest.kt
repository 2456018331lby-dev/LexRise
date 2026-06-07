package com.study.englishdemo.data

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.LocalDate

class MorphologyHelpersTest {
    @Test
    fun decomposeWord_splitsPrefixRootSuffix() {
        val segs = decomposeWord("prescribe", "scrib")
        assertThat(segs.map { it.text }).containsExactly("pre", "scrib", "e").inOrder()
        assertThat(segs.map { it.kind }).containsExactly(
            Morpheme.PREFIX, Morpheme.ROOT, Morpheme.SUFFIX,
        ).inOrder()
    }

    @Test
    fun decomposeWord_handlesRootAtStart() {
        val segs = decomposeWord("statement", "stat")
        assertThat(segs.map { it.text }).containsExactly("stat", "ement").inOrder()
        assertThat(segs[0].kind).isEqualTo(Morpheme.ROOT)
        assertThat(segs[1].kind).isEqualTo(Morpheme.SUFFIX)
    }

    @Test
    fun decomposeWord_missingRootReturnsPlain() {
        val segs = decomposeWord("floor", "")
        assertThat(segs).hasSize(1)
        assertThat(segs.first().kind).isEqualTo(Morpheme.PLAIN)
    }

    @Test
    fun decomposeWord_unmatchedRootReturnsPlain() {
        val segs = decomposeWord("running", "scrib")
        assertThat(segs).hasSize(1)
        assertThat(segs.first().text).isEqualTo("running")
    }

    @Test
    fun computePaceRecommendation_autoDisabledReturnsBaseline() {
        val pace = computePaceRecommendation(
            remainingWords = 500,
            examDate = LocalDate.of(2026, 6, 1),
            today = LocalDate.of(2026, 5, 9),
            baseline = 20,
            autoEnabled = false,
        )
        assertThat(pace.isAuto).isFalse()
        assertThat(pace.target).isEqualTo(20)
        assertThat(pace.remainingDays).isEqualTo(23)
    }

    @Test
    fun computePaceRecommendation_noExamDateClampsBaseline() {
        val pace = computePaceRecommendation(
            remainingWords = 500,
            examDate = null,
            today = LocalDate.of(2026, 5, 9),
            baseline = 999,
            autoEnabled = true,
        )
        assertThat(pace.target).isEqualTo(40)
        assertThat(pace.examDate).isNull()
        assertThat(pace.remainingDays).isNull()
    }

    @Test
    fun computePaceRecommendation_autoComputesTargetWithBuffer() {
        // 600 words / 30 days = 20/day raw, 24/day with 20% buffer
        val pace = computePaceRecommendation(
            remainingWords = 600,
            examDate = LocalDate.of(2026, 6, 8),
            today = LocalDate.of(2026, 5, 9),
            baseline = 10,
            autoEnabled = true,
        )
        assertThat(pace.isAuto).isTrue()
        assertThat(pace.remainingDays).isEqualTo(30)
        assertThat(pace.target).isEqualTo(24)
    }

    @Test
    fun computePaceRecommendation_pastExamDateFallsBackToBaseline() {
        val pace = computePaceRecommendation(
            remainingWords = 200,
            examDate = LocalDate.of(2026, 5, 1),
            today = LocalDate.of(2026, 5, 9),
            baseline = 20,
            autoEnabled = true,
        )
        assertThat(pace.isAuto).isFalse()
        assertThat(pace.target).isEqualTo(20)
        assertThat(pace.remainingDays).isLessThan(0)
    }

    @Test
    fun buildStudyFocusCue_prioritizesDueReview() {
        val cue = buildStudyFocusCue(
            session = fakeSession(reviewDueCount = 18, studiedToday = 6, completionRatio = 0.3f),
            rootSnapshot = BookRootSnapshot(totalRoots = 100, touchedRoots = 5, totalClustered = 300, learnedClustered = 20),
            pace = fakePace(target = 30, isAuto = true),
        )

        assertThat(cue.kind).isEqualTo(StudyFocusKind.REVIEW)
        assertThat(cue.primaryValue).isEqualTo("18")
        assertThat(cue.actionLabel).isEqualTo("去复习")
        assertThat(cue.progress).isEqualTo(0.3f)
    }

    @Test
    fun buildStudyFocusCue_usesAutoPaceWhenReviewIsClear() {
        val cue = buildStudyFocusCue(
            session = fakeSession(reviewDueCount = 0, newWordTarget = 20, newWordsRemaining = 400),
            rootSnapshot = BookRootSnapshot(totalRoots = 100, touchedRoots = 50, totalClustered = 300, learnedClustered = 160),
            pace = fakePace(target = 28, remainingWords = 400, remainingDays = 21, isAuto = true),
        )

        assertThat(cue.kind).isEqualTo(StudyFocusKind.PACE)
        assertThat(cue.primaryValue).isEqualTo("28")
        assertThat(cue.secondaryValue).isEqualTo("400")
        assertThat(cue.actionLabel).isEqualTo("按配速学新词")
    }

    @Test
    fun buildStudyFocusCue_surfacesLowRootCoverage() {
        val cue = buildStudyFocusCue(
            session = fakeSession(reviewDueCount = 0, newWordTarget = 20, newWordsRemaining = 120),
            rootSnapshot = BookRootSnapshot(totalRoots = 80, touchedRoots = 12, totalClustered = 300, learnedClustered = 40),
            pace = fakePace(target = 20, isAuto = false),
        )

        assertThat(cue.kind).isEqualTo(StudyFocusKind.ROOTS)
        assertThat(cue.primaryValue).isEqualTo("12/80")
        assertThat(cue.actionLabel).isEqualTo("看词根图谱")
        assertThat(cue.progress).isWithin(0.001f).of(0.15f)
    }

    @Test
    fun buildStudyFocusCue_pointsToNewWordsWhenPaceAndRootsAreFine() {
        val cue = buildStudyFocusCue(
            session = fakeSession(reviewDueCount = 0, newWordTarget = 20, newWordsRemaining = 70),
            rootSnapshot = BookRootSnapshot(totalRoots = 80, touchedRoots = 40, totalClustered = 300, learnedClustered = 140),
            pace = fakePace(target = 20, remainingWords = 70, isAuto = false),
        )

        assertThat(cue.kind).isEqualTo(StudyFocusKind.NEW_WORDS)
        assertThat(cue.primaryValue).isEqualTo("20")
        assertThat(cue.actionLabel).isEqualTo("去学新词")
    }

    @Test
    fun buildStudyFocusCue_celebratesMomentumWhenWorkIsClear() {
        val cue = buildStudyFocusCue(
            session = fakeSession(
                reviewDueCount = 0,
                newWordsRemaining = 0,
                studiedToday = 22,
                streakDays = 9,
                completionRatio = 1f,
            ),
            rootSnapshot = BookRootSnapshot(totalRoots = 80, touchedRoots = 60, totalClustered = 300, learnedClustered = 260),
            pace = fakePace(target = 20, remainingWords = 0, isAuto = false),
        )

        assertThat(cue.kind).isEqualTo(StudyFocusKind.MOMENTUM)
        assertThat(cue.primaryValue).isEqualTo("9")
        assertThat(cue.actionLabel).isEqualTo("巩固一轮")
        assertThat(cue.progress).isEqualTo(1f)
    }

    @Test
    fun buildQuizOptions_alwaysIncludesCorrectAndFourTotal() {
        val pool = listOf(
            fakeEntry("state", "stat"),
            fakeEntry("statement", "stat"),
            fakeEntry("status", "stat"),
            fakeEntry("include", "clud"),
            fakeEntry("floor", ""),
        )
        val opts = buildQuizOptions("exclude", "clud", pool, seed = 42L)
        assertThat(opts).contains("exclude")
        assertThat(opts).hasSize(4)
        assertThat(opts.toSet()).hasSize(4)
    }

    @Test
    fun buildQuizOptions_prefersSameRootDistractors() {
        val pool = listOf(
            fakeEntry("statement", "stat"),
            fakeEntry("status", "stat"),
            fakeEntry("include", "clud"),
            fakeEntry("banana", ""),
            fakeEntry("cherry", ""),
        )
        // With 2 same-root and 3 others, same-root should be picked first
        val opts = buildQuizOptions("state", "stat", pool, seed = 123L).toSet()
        assertThat(opts).containsAtLeast("statement", "status")
    }

    @Test
    fun blankTermInExample_replacesWholeTermIgnoringCase() {
        val prompt = blankTermInExample("Please Clarify the final goal.", "clarify")

        assertThat(prompt).isEqualTo("Please ____ the final goal.")
    }

    @Test
    fun blankTermInExample_doesNotReplaceInsideLongerWords() {
        val prompt = blankTermInExample("The statement was clear.", "state")

        assertThat(prompt).isNull()
    }

    @Test
    fun fuzzyTermMatchDistance_acceptsLikelyTypos() {
        val distance = fuzzyTermMatchDistance("clarfy", "clarify")

        assertThat(distance).isNotNull()
        assertThat(distance!!).isLessThan(0.2)
    }

    @Test
    fun fuzzyTermMatchDistance_rejectsShortOrDistantQueries() {
        assertThat(fuzzyTermMatchDistance("sta", "state")).isNull()
        assertThat(fuzzyTermMatchDistance("moon", "maintain")).isNull()
    }

    @Test
    fun recordPracticeAttempt_countsStableAndNeedsPracticeRatings() {
        val stats = listOf(
            ReviewRating.GOOD,
            ReviewRating.EASY,
            ReviewRating.HARD,
            ReviewRating.AGAIN,
        ).fold(PracticeSessionStats()) { acc, rating ->
            recordPracticeAttempt(acc, rating)
        }

        assertThat(stats.answered).isEqualTo(4)
        assertThat(stats.stable).isEqualTo(2)
        assertThat(stats.needsPractice).isEqualTo(2)
        assertThat(stats.stabilityPercent).isEqualTo(50)
    }

    @Test
    fun normalizedEditDistance_exactMatchIsZero() {
        assertThat(normalizedEditDistance("focus", "focus")).isEqualTo(0.0)
        assertThat(normalizedEditDistance("Focus", "focus")).isEqualTo(0.0)
    }

    @Test
    fun normalizedEditDistance_onetypo() {
        // "focu" vs "focus" = 1 edit over 5 chars = 0.2
        val d = normalizedEditDistance("focu", "focus")
        assertThat(d).isWithin(0.001).of(0.2)
    }

    @Test
    fun ratingFromEditDistance_mapping() {
        assertThat(ratingFromEditDistance(0.0)).isEqualTo(ReviewRating.EASY)
        assertThat(ratingFromEditDistance(0.1)).isEqualTo(ReviewRating.GOOD)
        assertThat(ratingFromEditDistance(0.25)).isEqualTo(ReviewRating.HARD)
        assertThat(ratingFromEditDistance(0.6)).isEqualTo(ReviewRating.AGAIN)
    }

    private fun fakeEntry(term: String, rootKey: String): WordEntry = WordEntry(
        id = term.hashCode().toLong(),
        term = term,
        phonetic = "",
        definition = "",
        translation = "",
        example = "",
        tags = emptyList(),
        rootKey = rootKey,
        progress = null,
    )

    private fun fakeSession(
        reviewDueCount: Int = 0,
        newWordTarget: Int = 20,
        newWordsRemaining: Int = 100,
        studiedToday: Int = 0,
        streakDays: Int = 0,
        completionRatio: Float = 0f,
    ): LearningSession = LearningSession(
        overview = DailyOverview(
            today = LocalDate.of(2026, 6, 7),
            newWordTarget = newWordTarget,
            newWordsRemaining = newWordsRemaining,
            reviewDueCount = reviewDueCount,
            streakDays = streakDays,
            studiedToday = studiedToday,
            completionRatio = completionRatio,
        ),
        dueReviewWords = emptyList(),
        recommendedNewWords = emptyList(),
    )

    private fun fakePace(
        target: Int,
        remainingWords: Int = 100,
        remainingDays: Int? = null,
        isAuto: Boolean = false,
    ): PaceRecommendation = PaceRecommendation(
        target = target,
        remainingWords = remainingWords,
        remainingDays = remainingDays,
        examDate = remainingDays?.let { LocalDate.of(2026, 6, 7).plusDays(it.toLong()) },
        isAuto = isAuto,
    )
}
