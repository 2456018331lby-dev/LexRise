package com.study.englishdemo.data

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.Instant
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
    fun buildDailyStudyRoute_prioritizesReviewToughAndRoots() {
        val route = buildDailyStudyRoute(
            session = fakeSession(reviewDueCount = 18, newWordsRemaining = 120),
            rootSnapshot = BookRootSnapshot(totalRoots = 80, touchedRoots = 12, totalClustered = 300, learnedClustered = 40),
            pace = fakePace(target = 20, isAuto = false),
            toughWordCount = 7,
        )

        assertThat(route.headline).isEqualTo("先稳记忆，再开新局")
        assertThat(route.summary).isEqualTo("清复习债 → 修错题 → 补词根网")
        assertThat(route.steps.map { it.target }).containsExactly(
            DailyStudyRouteTarget.REVIEW,
            DailyStudyRouteTarget.TOUGH,
            DailyStudyRouteTarget.ROOTS,
        ).inOrder()
        assertThat(route.steps[1].metricValue).isEqualTo("7")
    }

    @Test
    fun buildDailyStudyRoute_usesAutoPaceForLearnStep() {
        val route = buildDailyStudyRoute(
            session = fakeSession(reviewDueCount = 0, newWordTarget = 20, newWordsRemaining = 400),
            rootSnapshot = BookRootSnapshot(totalRoots = 80, touchedRoots = 50, totalClustered = 300, learnedClustered = 180),
            pace = fakePace(target = 32, remainingWords = 400, remainingDays = 18, isAuto = true),
            toughWordCount = 0,
        )

        assertThat(route.headline).isEqualTo("压力可控，推进新词")
        assertThat(route.steps).hasSize(1)
        assertThat(route.steps.first().target).isEqualTo(DailyStudyRouteTarget.LEARN)
        assertThat(route.steps.first().title).isEqualTo("按配速推进")
        assertThat(route.steps.first().metricValue).isEqualTo("32")
    }

    @Test
    fun buildDailyStudyRoute_capsStepCountButKeepsPriority() {
        val route = buildDailyStudyRoute(
            session = fakeSession(reviewDueCount = 12, newWordsRemaining = 90),
            rootSnapshot = BookRootSnapshot(totalRoots = 80, touchedRoots = 10, totalClustered = 300, learnedClustered = 20),
            pace = fakePace(target = 28, remainingWords = 90, remainingDays = 8, isAuto = true),
            toughWordCount = 6,
            maxSteps = 2,
        )

        assertThat(route.steps.map { it.target }).containsExactly(
            DailyStudyRouteTarget.REVIEW,
            DailyStudyRouteTarget.TOUGH,
        ).inOrder()
        assertThat(route.summary).isEqualTo("清复习债 → 修错题")
    }

    @Test
    fun buildDailyStudyRoute_fallsBackToStabilizeWhenWorkIsClear() {
        val route = buildDailyStudyRoute(
            session = fakeSession(
                reviewDueCount = 0,
                newWordsRemaining = 0,
                streakDays = 9,
                completionRatio = 1f,
            ),
            rootSnapshot = BookRootSnapshot(totalRoots = 80, touchedRoots = 70, totalClustered = 300, learnedClustered = 260),
            pace = fakePace(target = 20, remainingWords = 0, isAuto = false),
            toughWordCount = 0,
        )

        assertThat(route.steps).hasSize(1)
        assertThat(route.steps.first().target).isEqualTo(DailyStudyRouteTarget.REVIEW)
        assertThat(route.steps.first().title).isEqualTo("稳态复盘")
        assertThat(route.steps.first().metricValue).isEqualTo("9")
    }

    @Test
    fun buildStudyRhythmBrief_handlesQuietWeeks() {
        val brief = buildStudyRhythmBrief(
            counts = emptyList(),
            overview = fakeSession().overview,
        )

        assertThat(brief.kind).isEqualTo(StudyRhythmBriefKind.QUIET)
        assertThat(brief.primaryValue).isEqualTo("0")
        assertThat(brief.secondaryValue).isEqualTo("20")
        assertThat(brief.actionLabel).isEqualTo("先点亮今天")
        assertThat(brief.recentCounts).containsExactly(0, 0, 0, 0, 0, 0, 0).inOrder()
    }

    @Test
    fun buildStudyRhythmBrief_surfacesRecoveryWhenReviewDebtIsActive() {
        val today = LocalDate.of(2026, 6, 7)
        val brief = buildStudyRhythmBrief(
            counts = listOf(
                DailyReviewCount(today.minusDays(6), 4),
                DailyReviewCount(today.minusDays(1), 3),
            ),
            overview = fakeSession(reviewDueCount = 18, studiedToday = 0).overview,
        )

        assertThat(brief.kind).isEqualTo(StudyRhythmBriefKind.RECOVERY)
        assertThat(brief.primaryValue).isEqualTo("2/7")
        assertThat(brief.secondaryValue).isEqualTo("18")
        assertThat(brief.actionLabel).isEqualTo("恢复节奏")
    }

    @Test
    fun buildStudyRhythmBrief_usesTodayOverviewForSurge() {
        val today = LocalDate.of(2026, 6, 7)
        val brief = buildStudyRhythmBrief(
            counts = listOf(
                DailyReviewCount(today.minusDays(4), 6),
                DailyReviewCount(today.minusDays(2), 8),
                DailyReviewCount(today.minusDays(1), 4),
                DailyReviewCount(today, 3),
            ),
            overview = fakeSession(
                reviewDueCount = 5,
                newWordTarget = 20,
                studiedToday = 27,
                completionRatio = 1f,
            ).overview,
        )

        assertThat(brief.kind).isEqualTo(StudyRhythmBriefKind.SURGE)
        assertThat(brief.primaryValue).isEqualTo("27")
        assertThat(brief.secondaryValue).isEqualTo("45")
        assertThat(brief.actionLabel).isEqualTo("收口复盘")
        assertThat(brief.recentCounts.last()).isEqualTo(27)
    }

    @Test
    fun buildStudyRhythmBrief_marksSteadyFiveDayWeeks() {
        val today = LocalDate.of(2026, 6, 7)
        val brief = buildStudyRhythmBrief(
            counts = (0..4).map { offset ->
                DailyReviewCount(today.minusDays(offset.toLong()), 5 + offset)
            },
            overview = fakeSession(studiedToday = 5, completionRatio = 0.25f).overview,
        )

        assertThat(brief.kind).isEqualTo(StudyRhythmBriefKind.STEADY)
        assertThat(brief.primaryValue).isEqualTo("5/7")
        assertThat(brief.secondaryValue).isEqualTo("9")
        assertThat(brief.actionLabel).isEqualTo("保持节奏")
    }

    @Test
    fun buildStudyRhythmBrief_balancesPartialWeeks() {
        val today = LocalDate.of(2026, 6, 7)
        val brief = buildStudyRhythmBrief(
            counts = listOf(
                DailyReviewCount(today.minusDays(3), 6),
                DailyReviewCount(today.minusDays(1), 5),
                DailyReviewCount(today, 4),
            ),
            overview = fakeSession(studiedToday = 4, completionRatio = 0.2f).overview,
        )

        assertThat(brief.kind).isEqualTo(StudyRhythmBriefKind.BALANCE)
        assertThat(brief.primaryValue).isEqualTo("15")
        assertThat(brief.secondaryValue).isEqualTo("3/7")
        assertThat(brief.actionLabel).isEqualTo("稳步推进")
    }

    @Test
    fun buildWordMemoryAnchor_prefersRootFamilyClues() {
        val anchor = buildWordMemoryAnchor(
            word = fakeEntry(
                term = "inspect",
                rootKey = "spec",
                phase = StudyPhase.NEW,
                derivatives = listOf("inspected"),
            ),
            rootRef = RootReference("spec", listOf("看"), listOf("inspect", "respect", "spectator")),
            siblingCount = 3,
        )

        assertThat(anchor.kind).isEqualTo(WordMemoryAnchorKind.ROOT_FAMILY)
        assertThat(anchor.badgeLabel).isEqualTo("词根锚")
        assertThat(anchor.primaryValue).isEqualTo("spec")
        assertThat(anchor.secondaryValue).isEqualTo("3")
        assertThat(anchor.focusTerms).containsExactly("respect", "spectator", "inspected").inOrder()
    }

    @Test
    fun buildWordMemoryAnchor_usesWordFormsWhenRootIsMissing() {
        val anchor = buildWordMemoryAnchor(
            word = fakeEntry(
                term = "clarify",
                rootKey = "",
                phase = StudyPhase.LEARNING,
                derivatives = listOf("clarified", "clarifies", "clarify"),
            ),
            rootRef = null,
            siblingCount = 0,
        )

        assertThat(anchor.kind).isEqualTo(WordMemoryAnchorKind.WORD_FORMS)
        assertThat(anchor.primaryValue).isEqualTo("2")
        assertThat(anchor.secondaryValue).isEqualTo("学习中")
        assertThat(anchor.focusTerms).containsExactly("clarified", "clarifies").inOrder()
    }

    @Test
    fun buildWordMemoryAnchor_usesContextWhenOnlyExampleExists() {
        val anchor = buildWordMemoryAnchor(
            word = fakeEntry(
                term = "abandon",
                rootKey = "",
                example = "Never abandon your plan.",
                pos = "vt.",
            ),
            rootRef = null,
            siblingCount = 0,
        )

        assertThat(anchor.kind).isEqualTo(WordMemoryAnchorKind.CONTEXT)
        assertThat(anchor.badgeLabel).isEqualTo("语境锚")
        assertThat(anchor.secondaryValue).isEqualTo("vt.")
        assertThat(anchor.focusTerms).isEmpty()
    }

    @Test
    fun buildWordMemoryAnchor_fallsBackToSoloAnchor() {
        val anchor = buildWordMemoryAnchor(
            word = fakeEntry(
                term = "plain",
                rootKey = "",
                phase = null,
                frq = 1200,
            ),
            rootRef = null,
            siblingCount = 0,
        )

        assertThat(anchor.kind).isEqualTo(WordMemoryAnchorKind.SOLO)
        assertThat(anchor.primaryValue).isEqualTo("未建档")
        assertThat(anchor.secondaryValue).isEqualTo("1200")
        assertThat(anchor.actionLabel).isEqualTo("翻卡自测")
    }

    @Test
    fun buildRootWordGuide_prefersRootTraceWithFamilyTerms() {
        val guide = buildRootWordGuide(
            word = fakeEntry(
                term = "inspect",
                rootKey = "spec",
                phase = StudyPhase.REVIEW,
                derivatives = listOf("inspected"),
            ),
            rootMeanings = listOf("看", "观察"),
            familyTerms = listOf("inspect", "respect", "spectator"),
        )

        assertThat(guide.kind).isEqualTo(RootWordGuideKind.ROOT_TRACE)
        assertThat(guide.badgeLabel).isEqualTo("根族导读")
        assertThat(guide.primaryValue).isEqualTo("spec")
        assertThat(guide.secondaryValue).isEqualTo("2")
        assertThat(guide.focusTerms).containsExactly("respect", "spectator", "inspected").inOrder()
        assertThat(guide.actionLabel).isEqualTo("先比同根")
    }

    @Test
    fun buildRootWordGuide_usesWordFormsWithoutRootTrace() {
        val guide = buildRootWordGuide(
            word = fakeEntry(
                term = "clarify",
                rootKey = "",
                phase = StudyPhase.LEARNING,
                derivatives = listOf("clarified", "clarifies", "clarify"),
            ),
            rootMeanings = emptyList(),
            familyTerms = emptyList(),
        )

        assertThat(guide.kind).isEqualTo(RootWordGuideKind.WORD_FORMS)
        assertThat(guide.primaryValue).isEqualTo("2")
        assertThat(guide.secondaryValue).isEqualTo("学习中")
        assertThat(guide.focusTerms).containsExactly("clarified", "clarifies").inOrder()
        assertThat(guide.actionLabel).isEqualTo("先扫词形")
    }

    @Test
    fun buildRootWordGuide_usesContextWhenOnlyExampleExists() {
        val guide = buildRootWordGuide(
            word = fakeEntry(
                term = "abandon",
                rootKey = "",
                example = "Never abandon your plan.",
                pos = "vt.",
            ),
            rootMeanings = emptyList(),
            familyTerms = emptyList(),
        )

        assertThat(guide.kind).isEqualTo(RootWordGuideKind.CONTEXT)
        assertThat(guide.primaryValue).isEqualTo("可用")
        assertThat(guide.secondaryValue).isEqualTo("vt.")
        assertThat(guide.intensity).isWithin(0.001f).of(0.58f)
        assertThat(guide.focusTerms).isEmpty()
    }

    @Test
    fun buildRootWordGuide_fallsBackToQuickReview() {
        val guide = buildRootWordGuide(
            word = fakeEntry(
                term = "plain",
                rootKey = "",
                phase = null,
                frq = 1200,
            ),
            rootMeanings = emptyList(),
            familyTerms = emptyList(),
        )

        assertThat(guide.kind).isEqualTo(RootWordGuideKind.QUICK_REVIEW)
        assertThat(guide.primaryValue).isEqualTo("未建档")
        assertThat(guide.secondaryValue).isEqualTo("1200")
        assertThat(guide.actionLabel).isEqualTo("快速回看")
    }

    @Test
    fun buildWordBatchBrief_handlesEmptyBatch() {
        val brief = buildWordBatchBrief(emptyList())

        assertThat(brief.kind).isEqualTo(WordBatchBriefKind.EMPTY)
        assertThat(brief.primaryValue).isEqualTo("0")
        assertThat(brief.actionLabel).isEqualTo("去巩固")
        assertThat(brief.focusTerms).isEmpty()
    }

    @Test
    fun buildWordBatchBrief_prefersRootClusters() {
        val brief = buildWordBatchBrief(
            listOf(
                fakeEntry("inspect", "spec", derivatives = listOf("inspected")),
                fakeEntry("respect", "spec"),
                fakeEntry("clarify", "", derivatives = listOf("clarified")),
            ),
        )

        assertThat(brief.kind).isEqualTo(WordBatchBriefKind.ROOTS)
        assertThat(brief.primaryValue).isEqualTo("2")
        assertThat(brief.secondaryValue).isEqualTo("3")
        assertThat(brief.focusTerms).containsExactly("inspect", "respect").inOrder()
        assertThat(brief.actionLabel).isEqualTo("先看词根")
    }

    @Test
    fun buildWordBatchBrief_usesWordFormsWhenRootsAreSparse() {
        val brief = buildWordBatchBrief(
            listOf(
                fakeEntry("clarify", "", derivatives = listOf("clarified", "clarifies")),
                fakeEntry("review", "", derivatives = listOf("reviewed")),
                fakeEntry("plain", ""),
            ),
        )

        assertThat(brief.kind).isEqualTo(WordBatchBriefKind.WORD_FORMS)
        assertThat(brief.primaryValue).isEqualTo("2")
        assertThat(brief.focusTerms).containsExactly("clarify", "review").inOrder()
        assertThat(brief.actionLabel).isEqualTo("先看词形")
    }

    @Test
    fun buildWordBatchBrief_usesContextWhenExamplesDominate() {
        val brief = buildWordBatchBrief(
            listOf(
                fakeEntry("abandon", "", example = "Never abandon the plan."),
                fakeEntry("benefit", "", example = "Daily practice brings benefit."),
                fakeEntry("plain", ""),
            ),
        )

        assertThat(brief.kind).isEqualTo(WordBatchBriefKind.CONTEXT)
        assertThat(brief.primaryValue).isEqualTo("2")
        assertThat(brief.focusTerms).containsExactly("abandon", "benefit").inOrder()
        assertThat(brief.actionLabel).isEqualTo("先读例句")
    }

    @Test
    fun buildWordBatchBrief_fallsBackToMixedSmallBatches() {
        val brief = buildWordBatchBrief(
            listOf(
                fakeEntry("plain", ""),
                fakeEntry("single", "sing"),
            ),
        )

        assertThat(brief.kind).isEqualTo(WordBatchBriefKind.MIXED)
        assertThat(brief.primaryValue).isEqualTo("2")
        assertThat(brief.secondaryValue).isEqualTo("混合")
        assertThat(brief.focusTerms).containsExactly("plain", "single").inOrder()
    }

    @Test
    fun buildMnemonicBatchBrief_handlesEmptyBatch() {
        val brief = buildMnemonicBatchBrief(emptyList())

        assertThat(brief.kind).isEqualTo(MnemonicBatchBriefKind.EMPTY)
        assertThat(brief.primaryValue).isEqualTo("0")
        assertThat(brief.coverage).isEqualTo(0f)
        assertThat(brief.actionLabel).isEqualTo("去巩固")
    }

    @Test
    fun buildMnemonicBatchBrief_usesReadyPlanWhenMnemonicCoverageIsHigh() {
        val brief = buildMnemonicBatchBrief(
            listOf(
                fakeEntry("state", "stat", mnemonic = "stat 站住就是状态"),
                fakeEntry("statement", "stat", mnemonic = "state + ment 是陈述"),
                fakeEntry("plain", ""),
            ),
        )

        assertThat(brief.kind).isEqualTo(MnemonicBatchBriefKind.READY)
        assertThat(brief.primaryValue).isEqualTo("2")
        assertThat(brief.secondaryValue).isEqualTo("66%")
        assertThat(brief.focusTerms).containsExactly("state", "statement").inOrder()
        assertThat(brief.actionLabel).isEqualTo("先读巧记")
    }

    @Test
    fun buildMnemonicBatchBrief_usesSeedGapWhenCoverageIsPartial() {
        val brief = buildMnemonicBatchBrief(
            listOf(
                fakeEntry("state", "stat", mnemonic = "stat 站住就是状态"),
                fakeEntry("plain", ""),
                fakeEntry("single", ""),
                fakeEntry("solo", ""),
            ),
        )

        assertThat(brief.kind).isEqualTo(MnemonicBatchBriefKind.SEED_GAP)
        assertThat(brief.primaryValue).isEqualTo("3")
        assertThat(brief.secondaryValue).isEqualTo("1")
        assertThat(brief.focusTerms).containsExactly("plain", "single", "solo").inOrder()
        assertThat(brief.actionLabel).isEqualTo("边学边补")
    }

    @Test
    fun buildMnemonicBatchBrief_bridgesWithRootsWhenMnemonicIsMissing() {
        val brief = buildMnemonicBatchBrief(
            listOf(
                fakeEntry("inspect", "spec"),
                fakeEntry("respect", "spec"),
                fakeEntry("plain", ""),
            ),
        )

        assertThat(brief.kind).isEqualTo(MnemonicBatchBriefKind.ROOT_BRIDGE)
        assertThat(brief.primaryValue).isEqualTo("2")
        assertThat(brief.focusTerms).containsExactly("inspect", "respect").inOrder()
        assertThat(brief.actionLabel).isEqualTo("先借词根")
    }

    @Test
    fun buildMnemonicBatchBrief_fallsBackToQuickStart() {
        val brief = buildMnemonicBatchBrief(
            listOf(
                fakeEntry("plain", ""),
                fakeEntry("form", "", derivatives = listOf("formed")),
            ),
        )

        assertThat(brief.kind).isEqualTo(MnemonicBatchBriefKind.QUICK_START)
        assertThat(brief.primaryValue).isEqualTo("2")
        assertThat(brief.secondaryValue).isEqualTo("1")
        assertThat(brief.focusTerms).containsExactly("plain", "form").inOrder()
        assertThat(brief.actionLabel).isEqualTo("先翻卡")
    }

    @Test
    fun buildRootGroupInsight_seedsUntouchedRootFamilies() {
        val insight = buildRootGroupInsight(
            RootGroup(
                rootKey = "spec",
                meanings = listOf("看"),
                totalWords = 4,
                learnedWords = 0,
                members = listOf(
                    fakeEntry("inspect", "spec", StudyPhase.NEW),
                    fakeEntry("respect", "spec", StudyPhase.NEW),
                    fakeEntry("prospect", "spec", StudyPhase.NEW),
                    fakeEntry("spectator", "spec", StudyPhase.NEW),
                ),
            ),
        )

        assertThat(insight.stage).isEqualTo(RootGroupStage.SEED)
        assertThat(insight.badgeLabel).isEqualTo("起步")
        assertThat(insight.primaryValue).isEqualTo("0/4")
        assertThat(insight.secondaryValue).isEqualTo("4")
        assertThat(insight.focusTerms).containsExactly("inspect", "respect", "prospect").inOrder()
    }

    @Test
    fun buildRootGroupInsight_buildingPrioritizesUntouchedThenLearningTerms() {
        val insight = buildRootGroupInsight(
            RootGroup(
                rootKey = "stat",
                meanings = listOf("站立"),
                totalWords = 5,
                learnedWords = 2,
                members = listOf(
                    fakeEntry("state", "stat", StudyPhase.REVIEW),
                    fakeEntry("status", "stat", StudyPhase.LEARNING),
                    fakeEntry("statement", "stat", StudyPhase.NEW),
                    fakeEntry("static", "stat", null),
                    fakeEntry("statue", "stat", StudyPhase.NEW),
                ),
            ),
        )

        assertThat(insight.stage).isEqualTo(RootGroupStage.BUILDING)
        assertThat(insight.badgeLabel).isEqualTo("推进")
        assertThat(insight.focusTerms).containsExactly("statement", "static", "statue").inOrder()
        assertThat(insight.progress).isWithin(0.001f).of(0.4f)
    }

    @Test
    fun buildRootGroupInsight_consolidatingPrioritizesLearningAndReviewTerms() {
        val insight = buildRootGroupInsight(
            RootGroup(
                rootKey = "scrib",
                meanings = listOf("写"),
                totalWords = 4,
                learnedWords = 3,
                members = listOf(
                    fakeEntry("scribe", "scrib", StudyPhase.REVIEW),
                    fakeEntry("describe", "scrib", StudyPhase.LEARNING),
                    fakeEntry("prescribe", "scrib", StudyPhase.MASTERED),
                    fakeEntry("inscription", "scrib", StudyPhase.NEW),
                ),
            ),
        )

        assertThat(insight.stage).isEqualTo(RootGroupStage.CONSOLIDATING)
        assertThat(insight.badgeLabel).isEqualTo("收尾")
        assertThat(insight.focusTerms).containsExactly("describe", "scribe", "inscription").inOrder()
        assertThat(insight.secondaryValue).isEqualTo("1")
    }

    @Test
    fun buildRootGroupInsight_marksFullyTouchedFamiliesStable() {
        val insight = buildRootGroupInsight(
            RootGroup(
                rootKey = "clar",
                meanings = listOf("清楚"),
                totalWords = 2,
                learnedWords = 2,
                members = listOf(
                    fakeEntry("clarify", "clar", StudyPhase.REVIEW),
                    fakeEntry("clear", "clar", StudyPhase.MASTERED),
                ),
            ),
        )

        assertThat(insight.stage).isEqualTo(RootGroupStage.MASTERED)
        assertThat(insight.badgeLabel).isEqualTo("稳固")
        assertThat(insight.primaryValue).isEqualTo("2/2")
        assertThat(insight.progress).isEqualTo(1f)
        assertThat(insight.actionLabel).isEqualTo("回看整族")
    }

    @Test
    fun buildRootAtlasBrief_handlesEmptyRootBooks() {
        val brief = buildRootAtlasBrief(
            groups = emptyList(),
            snapshot = BookRootSnapshot(totalRoots = 0, touchedRoots = 0, totalClustered = 0, learnedClustered = 0),
        )

        assertThat(brief.kind).isEqualTo(RootAtlasBriefKind.EMPTY)
        assertThat(brief.primaryValue).isEqualTo("0")
        assertThat(brief.secondaryValue).isEqualTo("0")
        assertThat(brief.progress).isEqualTo(0f)
        assertThat(brief.focusRoots).isEmpty()
    }

    @Test
    fun buildRootAtlasBrief_seedsUntouchedAtlas() {
        val brief = buildRootAtlasBrief(
            groups = listOf(
                fakeRootGroup("spec", total = 4, learned = 0),
                fakeRootGroup("stat", total = 2, learned = 0),
            ),
            snapshot = BookRootSnapshot(totalRoots = 10, touchedRoots = 0, totalClustered = 30, learnedClustered = 0),
        )

        assertThat(brief.kind).isEqualTo(RootAtlasBriefKind.SEED)
        assertThat(brief.primaryValue).isEqualTo("0/10")
        assertThat(brief.secondaryValue).isEqualTo("0/30")
        assertThat(brief.focusRoots).containsExactly("spec", "stat").inOrder()
        assertThat(brief.actionLabel).isEqualTo("先开地基根")
    }

    @Test
    fun buildRootAtlasBrief_expandsLowCoverageWithUntouchedLargeRoots() {
        val brief = buildRootAtlasBrief(
            groups = listOf(
                fakeRootGroup("stat", total = 5, learned = 2),
                fakeRootGroup("spect", total = 4, learned = 0),
                fakeRootGroup("grad", total = 2, learned = 0),
            ),
            snapshot = BookRootSnapshot(totalRoots = 20, touchedRoots = 4, totalClustered = 100, learnedClustered = 20),
        )

        assertThat(brief.kind).isEqualTo(RootAtlasBriefKind.EXPAND)
        assertThat(brief.primaryValue).isEqualTo("4/20")
        assertThat(brief.focusRoots).containsExactly("spect", "grad", "stat").inOrder()
        assertThat(brief.progress).isWithin(0.001f).of(0.2f)
    }

    @Test
    fun buildRootAtlasBrief_consolidatesPartialRootFamilies() {
        val brief = buildRootAtlasBrief(
            groups = listOf(
                fakeRootGroup("scrib", total = 4, learned = 3),
                fakeRootGroup("tract", total = 5, learned = 2),
                fakeRootGroup("port", total = 2, learned = 2),
            ),
            snapshot = BookRootSnapshot(totalRoots = 10, touchedRoots = 6, totalClustered = 40, learnedClustered = 20),
        )

        assertThat(brief.kind).isEqualTo(RootAtlasBriefKind.CONSOLIDATE)
        assertThat(brief.primaryValue).isEqualTo("6/10")
        assertThat(brief.secondaryValue).isEqualTo("20/40")
        assertThat(brief.focusRoots).containsExactly("scrib", "tract").inOrder()
        assertThat(brief.actionLabel).isEqualTo("收束半熟根")
    }

    @Test
    fun buildRootAtlasBrief_marksBroadlyTouchedAtlasAsMastered() {
        val brief = buildRootAtlasBrief(
            groups = listOf(
                fakeRootGroup("clar", total = 2, learned = 2),
                fakeRootGroup("spec", total = 4, learned = 4),
                fakeRootGroup("stat", total = 5, learned = 4),
            ),
            snapshot = BookRootSnapshot(totalRoots = 10, touchedRoots = 10, totalClustered = 40, learnedClustered = 36),
        )

        assertThat(brief.kind).isEqualTo(RootAtlasBriefKind.MASTERED)
        assertThat(brief.primaryValue).isEqualTo("10/10")
        assertThat(brief.focusRoots).containsExactly("spec", "clar").inOrder()
        assertThat(brief.progress).isEqualTo(1f)
    }

    @Test
    fun buildRootMnemonicBrief_handlesEmptyRootBooks() {
        val brief = buildRootMnemonicBrief(emptyList())

        assertThat(brief.kind).isEqualTo(RootMnemonicBriefKind.EMPTY)
        assertThat(brief.primaryValue).isEqualTo("0")
        assertThat(brief.secondaryValue).isEqualTo("0")
        assertThat(brief.progress).isEqualTo(0f)
        assertThat(brief.focusRoots).isEmpty()
    }

    @Test
    fun buildRootMnemonicBrief_seedsLargeRootsWhenNoMnemonicExists() {
        val brief = buildRootMnemonicBrief(
            listOf(
                fakeRootGroup("spec", total = 5, learned = 0),
                fakeRootGroup("stat", total = 3, learned = 0),
                fakeRootGroup("clar", total = 1, learned = 0),
            ),
        )

        assertThat(brief.kind).isEqualTo(RootMnemonicBriefKind.ROOT_SEED)
        assertThat(brief.primaryValue).isEqualTo("0/9")
        assertThat(brief.secondaryValue).isEqualTo("0/3")
        assertThat(brief.focusRoots).containsExactly("spec", "stat", "clar").inOrder()
        assertThat(brief.actionLabel).isEqualTo("补地基词")
    }

    @Test
    fun buildRootMnemonicBrief_patchesSeededRootsWithLargeGaps() {
        val brief = buildRootMnemonicBrief(
            listOf(
                fakeRootGroup("spec", total = 5, learned = 1, mnemonicIndices = setOf(1)),
                fakeRootGroup("stat", total = 4, learned = 1, mnemonicIndices = setOf(1)),
                fakeRootGroup("clar", total = 2, learned = 0),
            ),
        )

        assertThat(brief.kind).isEqualTo(RootMnemonicBriefKind.PATCH_GAPS)
        assertThat(brief.primaryValue).isEqualTo("2/11")
        assertThat(brief.secondaryValue).isEqualTo("2/3")
        assertThat(brief.focusRoots).containsExactly("spec", "stat").inOrder()
        assertThat(brief.actionLabel).isEqualTo("补根族缺口")
    }

    @Test
    fun buildRootMnemonicBrief_marksUsableRootMnemonicCoverageReady() {
        val brief = buildRootMnemonicBrief(
            listOf(
                fakeRootGroup("scrib", total = 4, learned = 2, mnemonicIndices = setOf(1, 2)),
                fakeRootGroup("spec", total = 3, learned = 1, mnemonicIndices = setOf(1, 2)),
                fakeRootGroup("port", total = 3, learned = 1),
            ),
        )

        assertThat(brief.kind).isEqualTo(RootMnemonicBriefKind.READY)
        assertThat(brief.primaryValue).isEqualTo("4/10")
        assertThat(brief.secondaryValue).isEqualTo("2/3")
        assertThat(brief.focusRoots).containsExactly("scrib", "spec").inOrder()
        assertThat(brief.actionLabel).isEqualTo("回看有种子根")
    }

    @Test
    fun buildRootMnemonicBrief_marksDenseMnemonicNetworkSaturated() {
        val brief = buildRootMnemonicBrief(
            listOf(
                fakeRootGroup("scrib", total = 4, learned = 4, mnemonicIndices = setOf(1, 2, 3, 4)),
                fakeRootGroup("spec", total = 3, learned = 3, mnemonicIndices = setOf(1, 2, 3)),
                fakeRootGroup("port", total = 3, learned = 3, mnemonicIndices = setOf(1, 2)),
            ),
        )

        assertThat(brief.kind).isEqualTo(RootMnemonicBriefKind.SATURATED)
        assertThat(brief.primaryValue).isEqualTo("9/10")
        assertThat(brief.secondaryValue).isEqualTo("3/3")
        assertThat(brief.focusRoots).containsExactly("scrib", "port", "spec").inOrder()
        assertThat(brief.progress).isWithin(0.001f).of(0.9f)
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
    fun buildClozeBlank_acceptsDerivativeAndPreservesMatchedAnswer() {
        val blank = buildClozeBlank(
            example = "The note clarified the final goal.",
            term = "clarify",
            variants = listOf("clarified", "clarifies"),
        )

        assertThat(blank).isNotNull()
        assertThat(blank!!.prompt).isEqualTo("The note ____ the final goal.")
        assertThat(blank.answer).isEqualTo("clarified")
    }

    @Test
    fun buildClozeBlank_prefersLemmaWhenLemmaAndDerivativeBothAppear() {
        val blank = buildClozeBlank(
            example = "Clarified notes should clarify the result.",
            term = "clarify",
            variants = listOf("clarified"),
        )

        assertThat(blank).isNotNull()
        assertThat(blank!!.prompt).isEqualTo("Clarified notes should ____ the result.")
        assertThat(blank.answer).isEqualTo("clarify")
    }

    @Test
    fun buildClozeBlank_prefersEarliestDerivativeWhenLemmaIsMissing() {
        val blank = buildClozeBlank(
            example = "Clarifies first; then clarified again.",
            term = "clarify",
            variants = listOf("clarified", "clarifies"),
        )

        assertThat(blank).isNotNull()
        assertThat(blank!!.prompt).isEqualTo("____ first; then clarified again.")
        assertThat(blank.answer).isEqualTo("Clarifies")
    }

    @Test
    fun blankTermInExample_doesNotReplaceInsideLongerWords() {
        val prompt = blankTermInExample("The statement was clear.", "state")

        assertThat(prompt).isNull()
    }

    @Test
    fun buildClozeContextGuide_prioritizesWordFormWithoutLeakingAnswer() {
        val guide = buildClozeContextGuide(
            ClozeQuestion(
                word = fakeEntry(
                    term = "clarify",
                    rootKey = "",
                    translation = "澄清",
                    derivatives = listOf("clarified", "clarifies"),
                ),
                prompt = "The note ____ the final goal.",
                options = listOf("clarified", "state", "include", "remain"),
                correct = "clarified",
            ),
        )

        assertThat(guide.kind).isEqualTo(ClozeContextGuideKind.WORD_FORM)
        assertThat(guide.primaryValue).isEqualTo("派生形")
        assertThat(guide.secondaryValue).isEqualTo("4")
        assertThat(guide.actionLabel).isEqualTo("先判词形")
        assertThat(guide.title).doesNotContain("clarified")
        assertThat(guide.message).doesNotContain("clarified")
        assertThat(guide.focusTerms).containsExactly("The", "note", "final", "goal").inOrder()
    }

    @Test
    fun buildClozeContextGuide_usesRootTraceForLemmaQuestions() {
        val guide = buildClozeContextGuide(
            ClozeQuestion(
                word = fakeEntry(term = "state", rootKey = "stat", translation = "状态"),
                prompt = "The ____ of the project is stable.",
                options = listOf("state", "include", "floor", "banana"),
                correct = "state",
            ),
        )

        assertThat(guide.kind).isEqualTo(ClozeContextGuideKind.ROOT_TRACE)
        assertThat(guide.primaryValue).isEqualTo("stat")
        assertThat(guide.secondaryValue).isEqualTo("6")
        assertThat(guide.actionLabel).isEqualTo("先抓词根")
        assertThat(guide.focusTerms).containsExactly("The", "project", "stable").inOrder()
    }

    @Test
    fun buildClozeContextGuide_usesMeaningWhenNoRootOrDerivativeSignal() {
        val guide = buildClozeContextGuide(
            ClozeQuestion(
                word = fakeEntry(term = "abandon", rootKey = "", translation = "放弃", pos = "vt."),
                prompt = "Never ____ your plan.",
                options = listOf("abandon", "inspect", "include", "remain"),
                correct = "abandon",
            ),
        )

        assertThat(guide.kind).isEqualTo(ClozeContextGuideKind.MEANING)
        assertThat(guide.primaryValue).isEqualTo("可参考")
        assertThat(guide.secondaryValue).isEqualTo("vt.")
        assertThat(guide.actionLabel).isEqualTo("先读语境")
        assertThat(guide.confidence).isWithin(0.001f).of(0.56f)
    }

    @Test
    fun buildClozeContextGuide_fallsBackToQuickScan() {
        val guide = buildClozeContextGuide(
            ClozeQuestion(
                word = fakeEntry(term = "plain", rootKey = ""),
                prompt = "Choose ____ fast.",
                options = listOf("plain", "state", "include", "remain"),
                correct = "plain",
            ),
        )

        assertThat(guide.kind).isEqualTo(ClozeContextGuideKind.QUICK_SCAN)
        assertThat(guide.primaryValue).isEqualTo("4")
        assertThat(guide.secondaryValue).isEqualTo("2")
        assertThat(guide.actionLabel).isEqualTo("快速判断")
        assertThat(guide.focusTerms).containsExactly("Choose", "fast").inOrder()
    }

    @Test
    fun fuzzyTermMatchDistance_acceptsLikelyTypos() {
        val distance = fuzzyTermMatchDistance("clarfy", "clarify")

        assertThat(distance).isNotNull()
        assertThat(distance!!).isLessThan(0.2)
    }

    @Test
    fun fuzzyWordFormMatchDistance_acceptsDerivativeTypos() {
        val distance = fuzzyWordFormMatchDistance(
            query = "clarifed",
            term = "clarify",
            variants = listOf("clarified", "clarifies"),
        )

        assertThat(distance).isNotNull()
        assertThat(distance!!).isLessThan(0.2)
    }

    @Test
    fun matchingWordForms_returnsDerivativeExactMatches() {
        val matches = matchingWordForms(
            query = "clarified",
            term = "clarify",
            variants = listOf("clarified", "clarifies"),
        )

        assertThat(matches).containsExactly("clarified")
    }

    @Test
    fun matchingWordForms_returnsNearestFormForTypos() {
        val matches = matchingWordForms(
            query = "clarifed",
            term = "clarify",
            variants = listOf("clarified", "clarifies"),
        )

        assertThat(matches.first()).isEqualTo("clarified")
    }

    @Test
    fun matchingWordForms_ignoresVeryShortQueriesAndEmptyLimit() {
        assertThat(
            matchingWordForms(
                query = "cl",
                term = "clarify",
                variants = listOf("clarified"),
                limit = 0,
            ),
        ).isEmpty()
        assertThat(
            matchingWordForms(
                query = "c",
                term = "clarify",
                variants = listOf("clarified"),
            ),
        ).isEmpty()
    }

    @Test
    fun fuzzyTermMatchDistance_rejectsShortOrDistantQueries() {
        assertThat(fuzzyTermMatchDistance("sta", "state")).isNull()
        assertThat(fuzzyTermMatchDistance("moon", "maintain")).isNull()
    }

    @Test
    fun buildVocabularySearchInsight_guidesBlankQueries() {
        val insight = buildVocabularySearchInsight(
            query = " ",
            results = listOf(fakeEntry("clarify", "clar")),
        )

        assertThat(insight.kind).isEqualTo(VocabularySearchInsightKind.READY)
        assertThat(insight.primaryValue).isEqualTo("全词书")
        assertThat(insight.focusTerms).isEmpty()
        assertThat(insight.confidence).isEqualTo(0f)
    }

    @Test
    fun buildVocabularySearchInsight_handlesEmptyResults() {
        val insight = buildVocabularySearchInsight(
            query = "clarifiy",
            results = emptyList(),
        )

        assertThat(insight.kind).isEqualTo(VocabularySearchInsightKind.EMPTY_RESULTS)
        assertThat(insight.primaryValue).isEqualTo("0")
        assertThat(insight.actionLabel).isEqualTo("改关键词")
    }

    @Test
    fun buildVocabularySearchInsight_prioritizesDirectTermMatches() {
        val insight = buildVocabularySearchInsight(
            query = "stat",
            results = listOf(
                fakeEntry("state", "stat"),
                fakeEntry("statement", "stat"),
                fakeEntry("stable", "sta"),
            ),
        )

        assertThat(insight.kind).isEqualTo(VocabularySearchInsightKind.TERM_MATCH)
        assertThat(insight.primaryValue).isEqualTo("2")
        assertThat(insight.focusTerms).containsExactly("state", "statement").inOrder()
    }

    @Test
    fun buildVocabularySearchInsight_explainsDerivativeOrTypoHits() {
        val insight = buildVocabularySearchInsight(
            query = "clarifed",
            results = listOf(
                fakeEntry(
                    term = "clarify",
                    rootKey = "clar",
                    derivatives = listOf("clarified", "clarifies"),
                ),
            ),
        )

        assertThat(insight.kind).isEqualTo(VocabularySearchInsightKind.WORD_FORM_MATCH)
        assertThat(insight.primaryValue).isEqualTo("1")
        assertThat(insight.focusTerms).containsExactly("clarified", "clarifies").inOrder()
        assertThat(insight.actionLabel).isEqualTo("看词形雷达")
    }

    @Test
    fun buildVocabularySearchInsight_surfacesRootClustersAfterMeaningMatches() {
        val insight = buildVocabularySearchInsight(
            query = "说",
            results = listOf(
                fakeEntry(term = "describe", rootKey = "scrib", translation = "描述，说清楚"),
                fakeEntry(term = "prescribe", rootKey = "scrib", translation = "规定，开处方"),
                fakeEntry(term = "announce", rootKey = "", translation = "宣布，说出"),
            ),
        )

        assertThat(insight.kind).isEqualTo(VocabularySearchInsightKind.ROOT_CLUSTER)
        assertThat(insight.primaryValue).isEqualTo("2")
        assertThat(insight.secondaryValue).isEqualTo("scrib")
        assertThat(insight.focusTerms).containsExactly("describe", "prescribe").inOrder()
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
    fun buildPracticeSessionCoach_warmsUpBeforeAttempts() {
        val coach = buildPracticeSessionCoach(PracticeSessionStats(), PracticeMode.FLIP)

        assertThat(coach.kind).isEqualTo(PracticeSessionCoachKind.WARMUP)
        assertThat(coach.title).isEqualTo("先校准手感")
        assertThat(coach.primaryValue).isEqualTo("5题")
        assertThat(coach.secondaryValue).isEqualTo("0")
        assertThat(coach.progress).isEqualTo(0f)
    }

    @Test
    fun buildPracticeSessionCoach_recoversWhenNeedsPracticeDominates() {
        val coach = buildPracticeSessionCoach(
            PracticeSessionStats(answered = 6, stable = 2, needsPractice = 4),
            PracticeMode.CLOZE,
        )

        assertThat(coach.kind).isEqualTo(PracticeSessionCoachKind.RECOVER)
        assertThat(coach.primaryLabel).isEqualTo("再练")
        assertThat(coach.primaryValue).isEqualTo("4")
        assertThat(coach.secondaryValue).isEqualTo("2")
        assertThat(coach.actionLabel).isEqualTo("慢速复盘")
    }

    @Test
    fun buildPracticeSessionCoach_stabilizesMidSessionProgress() {
        val coach = buildPracticeSessionCoach(
            PracticeSessionStats(answered = 7, stable = 5, needsPractice = 2),
            PracticeMode.SPELL,
        )

        assertThat(coach.kind).isEqualTo(PracticeSessionCoachKind.STABILIZE)
        assertThat(coach.primaryValue).isEqualTo("71%")
        assertThat(coach.actionLabel).isEqualTo("再做一组")
        assertThat(coach.progress).isWithin(0.001f).of(0.71f)
    }

    @Test
    fun buildPracticeSessionCoach_advancesAfterStableTenQuestionSample() {
        val coach = buildPracticeSessionCoach(
            PracticeSessionStats(answered = 12, stable = 11, needsPractice = 1),
            PracticeMode.CHOICE,
        )

        assertThat(coach.kind).isEqualTo(PracticeSessionCoachKind.ADVANCE)
        assertThat(coach.primaryValue).isEqualTo("91%")
        assertThat(coach.secondaryValue).isEqualTo("12")
        assertThat(coach.message).contains("完形或拼写")
    }

    @Test
    fun buildReviewQueueBrief_handlesEmptyQueue() {
        val brief = buildReviewQueueBrief(emptyList(), PracticeMode.FLIP)

        assertThat(brief.kind).isEqualTo(ReviewQueueBriefKind.EMPTY)
        assertThat(brief.primaryValue).isEqualTo("0")
        assertThat(brief.actionLabel).isEqualTo("去新词")
        assertThat(brief.focusTerms).isEmpty()
    }

    @Test
    fun buildReviewQueueBrief_warmsUpSmallFlipQueues() {
        val brief = buildReviewQueueBrief(
            words = listOf(
                fakeEntry("plain", ""),
                fakeEntry("focus", ""),
            ),
            mode = PracticeMode.FLIP,
        )

        assertThat(brief.kind).isEqualTo(ReviewQueueBriefKind.WARMUP)
        assertThat(brief.primaryValue).isEqualTo("2")
        assertThat(brief.secondaryValue).isEqualTo("翻卡")
        assertThat(brief.actionLabel).isEqualTo("快翻确认")
    }

    @Test
    fun buildReviewQueueBrief_prioritizesRootClusters() {
        val brief = buildReviewQueueBrief(
            words = listOf(
                fakeEntry("inspect", "spec"),
                fakeEntry("respect", "spec"),
                fakeEntry("benefit", "", example = "Daily practice brings benefit."),
            ),
            mode = PracticeMode.CHOICE,
        )

        assertThat(brief.kind).isEqualTo(ReviewQueueBriefKind.ROOT_TRACE)
        assertThat(brief.primaryValue).isEqualTo("2")
        assertThat(brief.secondaryValue).isEqualTo("3")
        assertThat(brief.focusTerms).containsExactly("inspect", "respect").inOrder()
    }

    @Test
    fun buildReviewQueueBrief_usesContextForClozeQueues() {
        val brief = buildReviewQueueBrief(
            words = listOf(
                fakeEntry("abandon", "", example = "Never abandon the plan."),
                fakeEntry("benefit", "", example = "Daily practice brings benefit."),
                fakeEntry("plain", ""),
            ),
            mode = PracticeMode.CLOZE,
        )

        assertThat(brief.kind).isEqualTo(ReviewQueueBriefKind.CONTEXT)
        assertThat(brief.primaryValue).isEqualTo("2")
        assertThat(brief.actionLabel).isEqualTo("先看语境")
        assertThat(brief.focusTerms).containsExactly("abandon", "benefit").inOrder()
    }

    @Test
    fun buildReviewQueueBrief_usesActiveRecallForSpellQueues() {
        val brief = buildReviewQueueBrief(
            words = listOf(
                fakeEntry("clarify", "", derivatives = listOf("clarified", "clarifies")),
                fakeEntry("review", "", derivatives = listOf("reviewed")),
                fakeEntry("plain", ""),
            ),
            mode = PracticeMode.SPELL,
        )

        assertThat(brief.kind).isEqualTo(ReviewQueueBriefKind.ACTIVE_RECALL)
        assertThat(brief.primaryValue).isEqualTo("2")
        assertThat(brief.secondaryValue).isEqualTo("拼写")
        assertThat(brief.focusTerms).containsExactly("clarify", "review").inOrder()
    }

    @Test
    fun buildReviewQueueBrief_prioritizesSpellModeBeforeContextHints() {
        val brief = buildReviewQueueBrief(
            words = listOf(
                fakeEntry("abandon", "", example = "Never abandon the plan."),
                fakeEntry("benefit", "", example = "Daily practice brings benefit."),
                fakeEntry("focus", "", example = "Focus on the next answer."),
            ),
            mode = PracticeMode.SPELL,
        )

        assertThat(brief.kind).isEqualTo(ReviewQueueBriefKind.ACTIVE_RECALL)
        assertThat(brief.primaryLabel).isEqualTo("到期")
        assertThat(brief.primaryValue).isEqualTo("3")
        assertThat(brief.secondaryValue).isEqualTo("拼写")
        assertThat(brief.focusTerms).containsExactly("abandon", "benefit", "focus").inOrder()
    }

    @Test
    fun buildReviewQueueBrief_fallsBackToMixedQueues() {
        val brief = buildReviewQueueBrief(
            words = listOf(
                fakeEntry("plain", ""),
                fakeEntry("single", "sing"),
                fakeEntry("focus", ""),
                fakeEntry("benefit", ""),
            ),
            mode = PracticeMode.CHOICE,
        )

        assertThat(brief.kind).isEqualTo(ReviewQueueBriefKind.MIXED)
        assertThat(brief.primaryValue).isEqualTo("4")
        assertThat(brief.secondaryValue).isEqualTo("选择")
        assertThat(brief.actionLabel).isEqualTo("开始清债")
    }

    @Test
    fun buildToughWordPrescription_rebuildsHighLapseWords() {
        val prescription = buildToughWordPrescription(
            ToughWord(
                word = fakeEntry("clarify", "clar", StudyPhase.LEARNING, example = "Please clarify the goal."),
                againCount = 5,
                lapses = 3,
                lastReviewedAt = Instant.EPOCH,
            ),
        )

        assertThat(prescription.kind).isEqualTo(ToughWordPrescriptionKind.REBUILD)
        assertThat(prescription.badgeLabel).isEqualTo("重建")
        assertThat(prescription.primaryValue).isEqualTo("5")
        assertThat(prescription.secondaryValue).isEqualTo("3")
        assertThat(prescription.intensity).isEqualTo(1f)
    }

    @Test
    fun buildToughWordPrescription_prefersRootTraceForRootedWords() {
        val prescription = buildToughWordPrescription(
            ToughWord(
                word = fakeEntry("statement", "stat", StudyPhase.LEARNING),
                againCount = 2,
                lapses = 0,
                lastReviewedAt = null,
            ),
        )

        assertThat(prescription.kind).isEqualTo(ToughWordPrescriptionKind.ROOT_TRACE)
        assertThat(prescription.primaryLabel).isEqualTo("词根")
        assertThat(prescription.primaryValue).isEqualTo("stat")
        assertThat(prescription.secondaryValue).isEqualTo("学习中")
    }

    @Test
    fun buildToughWordPrescription_usesContextForUnrootedExampleWords() {
        val prescription = buildToughWordPrescription(
            ToughWord(
                word = fakeEntry(
                    term = "benefit",
                    rootKey = "",
                    phase = StudyPhase.REVIEW,
                    example = "Daily practice brings benefit.",
                ),
                againCount = 2,
                lapses = 0,
                lastReviewedAt = null,
            ),
        )

        assertThat(prescription.kind).isEqualTo(ToughWordPrescriptionKind.CONTEXT)
        assertThat(prescription.badgeLabel).isEqualTo("语境")
        assertThat(prescription.actionLabel).isEqualTo("读例句")
    }

    @Test
    fun buildToughWordPrescription_stabilizesLightMistakes() {
        val prescription = buildToughWordPrescription(
            ToughWord(
                word = fakeEntry("focus", "", StudyPhase.REVIEW),
                againCount = 1,
                lapses = 0,
                lastReviewedAt = null,
            ),
        )

        assertThat(prescription.kind).isEqualTo(ToughWordPrescriptionKind.STABILIZE)
        assertThat(prescription.badgeLabel).isEqualTo("巩固")
        assertThat(prescription.actionLabel).isEqualTo("快扫确认")
        assertThat(prescription.intensity).isWithin(0.001f).of(0.125f)
    }

    @Test
    fun buildToughWordsBrief_handlesEmptyPool() {
        val brief = buildToughWordsBrief(emptyList())

        assertThat(brief.dominantKind).isEqualTo(ToughWordPrescriptionKind.STABILIZE)
        assertThat(brief.totalCount).isEqualTo(0)
        assertThat(brief.highRiskCount).isEqualTo(0)
        assertThat(brief.actionLabel).isEqualTo("保持节奏")
        assertThat(brief.intensity).isEqualTo(0f)
    }

    @Test
    fun buildToughWordsBrief_surfacesRebuildPressure() {
        val brief = buildToughWordsBrief(
            listOf(
                ToughWord(
                    word = fakeEntry("clarify", "clar", StudyPhase.LEARNING),
                    againCount = 5,
                    lapses = 3,
                    lastReviewedAt = null,
                ),
                ToughWord(
                    word = fakeEntry("collapse", "", StudyPhase.REVIEW),
                    againCount = 6,
                    lapses = 0,
                    lastReviewedAt = null,
                ),
                ToughWord(
                    word = fakeEntry("statement", "stat", StudyPhase.LEARNING),
                    againCount = 2,
                    lapses = 0,
                    lastReviewedAt = null,
                ),
            ),
        )

        assertThat(brief.dominantKind).isEqualTo(ToughWordPrescriptionKind.REBUILD)
        assertThat(brief.dominantLabel).isEqualTo("重建")
        assertThat(brief.totalCount).isEqualTo(3)
        assertThat(brief.highRiskCount).isEqualTo(2)
        assertThat(brief.peakAgainCount).isEqualTo(6)
        assertThat(brief.actionLabel).isEqualTo("先重建")
    }

    @Test
    fun buildToughWordsBrief_breaksTiesTowardRootTrace() {
        val brief = buildToughWordsBrief(
            listOf(
                ToughWord(
                    word = fakeEntry("statement", "stat", StudyPhase.LEARNING),
                    againCount = 2,
                    lapses = 0,
                    lastReviewedAt = null,
                ),
                ToughWord(
                    word = fakeEntry(
                        term = "benefit",
                        rootKey = "",
                        phase = StudyPhase.REVIEW,
                        example = "Daily practice brings benefit.",
                    ),
                    againCount = 2,
                    lapses = 0,
                    lastReviewedAt = null,
                ),
            ),
        )

        assertThat(brief.dominantKind).isEqualTo(ToughWordPrescriptionKind.ROOT_TRACE)
        assertThat(brief.dominantLabel).isEqualTo("词根")
        assertThat(brief.actionLabel).isEqualTo("按词根修")
    }

    @Test
    fun buildToughWordsBrief_stabilizesLightMistakes() {
        val brief = buildToughWordsBrief(
            listOf(
                ToughWord(
                    word = fakeEntry("focus", "", StudyPhase.REVIEW),
                    againCount = 1,
                    lapses = 0,
                    lastReviewedAt = null,
                ),
                ToughWord(
                    word = fakeEntry("plain", "", StudyPhase.NEW),
                    againCount = 1,
                    lapses = 0,
                    lastReviewedAt = null,
                ),
            ),
        )

        assertThat(brief.dominantKind).isEqualTo(ToughWordPrescriptionKind.STABILIZE)
        assertThat(brief.totalCount).isEqualTo(2)
        assertThat(brief.highRiskCount).isEqualTo(0)
        assertThat(brief.actionLabel).isEqualTo("快扫一轮")
        assertThat(brief.intensity).isWithin(0.001f).of(0.125f)
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

    private fun fakeEntry(
        term: String,
        rootKey: String,
        phase: StudyPhase? = null,
        example: String = "",
        derivatives: List<String> = emptyList(),
        translation: String = "",
        mnemonic: String = "",
        pos: String = "",
        frq: Int = 0,
    ): WordEntry = WordEntry(
        id = term.hashCode().toLong(),
        term = term,
        phonetic = "",
        definition = "",
        translation = translation,
        example = example,
        tags = emptyList(),
        rootKey = rootKey,
        derivatives = derivatives,
        mnemonic = mnemonic,
        pos = pos,
        frq = frq,
        progress = phase?.let {
            WordProgress(
                phase = it,
                familiarity = 0,
                streak = 0,
                lapses = 0,
                easeFactor = 2.5,
                intervalDays = 0,
                lastReviewedAt = null,
                nextReviewAt = Instant.EPOCH,
            )
        },
    )

    private fun fakeRootGroup(
        rootKey: String,
        total: Int,
        learned: Int,
        mnemonicIndices: Set<Int> = emptySet(),
    ): RootGroup {
        val safeTotal = total.coerceAtLeast(0)
        val safeLearned = learned.coerceIn(0, safeTotal)
        val members = (1..safeTotal).map { index ->
            fakeEntry(
                term = "$rootKey$index",
                rootKey = rootKey,
                phase = if (index <= safeLearned) StudyPhase.REVIEW else StudyPhase.NEW,
                mnemonic = if (index in mnemonicIndices) "$rootKey$index 巧记 seed" else "",
            )
        }
        return RootGroup(
            rootKey = rootKey,
            meanings = emptyList(),
            totalWords = safeTotal,
            learnedWords = safeLearned,
            members = members,
        )
    }

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
