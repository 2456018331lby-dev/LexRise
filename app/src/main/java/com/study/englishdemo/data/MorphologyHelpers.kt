package com.study.englishdemo.data

/**
 * Splits a term into [prefix, root, suffix] segments for visual morpheme display.
 * Never throws; returns a single PLAIN segment when the rootKey can't be located
 * inside the term (e.g. rootKey == "" or rootKey is a lemma variant not present
 * in the surface form).
 *
 * The split is case-insensitive but preserves the original casing of the term.
 */
fun decomposeWord(term: String, rootKey: String): List<MorphemeSegment> {
    val cleaned = term.trim()
    if (cleaned.isEmpty()) return emptyList()
    val key = rootKey.trim()
    if (key.isEmpty() || key.length > cleaned.length) {
        return listOf(MorphemeSegment(cleaned, Morpheme.PLAIN))
    }
    val idx = cleaned.lowercase().indexOf(key.lowercase())
    if (idx < 0) return listOf(MorphemeSegment(cleaned, Morpheme.PLAIN))

    val prefix = cleaned.substring(0, idx)
    val root = cleaned.substring(idx, idx + key.length)
    val suffix = cleaned.substring(idx + key.length)
    val segments = mutableListOf<MorphemeSegment>()
    if (prefix.isNotEmpty()) segments += MorphemeSegment(prefix, Morpheme.PREFIX)
    segments += MorphemeSegment(root, Morpheme.ROOT)
    if (suffix.isNotEmpty()) segments += MorphemeSegment(suffix, Morpheme.SUFFIX)
    return segments
}

/**
 * Recommends a daily new-word target based on remaining unseen words and
 * how many days are left until the exam. Adds 20% buffer (so the user can
 * still finish after missing a few days), clamped to [5, 40] to match the
 * settings slider range.
 *
 * When examDate is null or already past, falls back to the user-set baseline.
 */
fun computePaceRecommendation(
    remainingWords: Int,
    examDate: java.time.LocalDate?,
    today: java.time.LocalDate,
    baseline: Int,
    autoEnabled: Boolean,
): PaceRecommendation {
    if (!autoEnabled || examDate == null || remainingWords <= 0) {
        return PaceRecommendation(
            target = baseline.coerceIn(5, 40),
            remainingWords = remainingWords,
            remainingDays = examDate?.let { java.time.temporal.ChronoUnit.DAYS.between(today, it).toInt() },
            examDate = examDate,
            isAuto = false,
        )
    }
    val daysLeft = java.time.temporal.ChronoUnit.DAYS.between(today, examDate).toInt()
    if (daysLeft <= 0) {
        return PaceRecommendation(
            target = baseline.coerceIn(5, 40),
            remainingWords = remainingWords,
            remainingDays = daysLeft,
            examDate = examDate,
            isAuto = false,
        )
    }
    val rawPerDay = kotlin.math.ceil(remainingWords.toDouble() / daysLeft.toDouble()).toInt()
    val withBuffer = kotlin.math.ceil(rawPerDay * 1.2).toInt()
    return PaceRecommendation(
        target = withBuffer.coerceIn(5, 40),
        remainingWords = remainingWords,
        remainingDays = daysLeft,
        examDate = examDate,
        isAuto = true,
    )
}

fun buildStudyFocusCue(
    session: LearningSession,
    rootSnapshot: BookRootSnapshot,
    pace: PaceRecommendation,
): StudyFocusCue {
    val overview = session.overview
    val dailyProgress = overview.completionRatio.coerceIn(0f, 1f)
    val rootProgress = if (rootSnapshot.totalRoots == 0) {
        0f
    } else {
        (rootSnapshot.touchedRoots.toFloat() / rootSnapshot.totalRoots.toFloat()).coerceIn(0f, 1f)
    }
    return when {
        overview.reviewDueCount > 0 -> StudyFocusCue(
            kind = StudyFocusKind.REVIEW,
            title = "先清复习债",
            message = "到期词最容易滑出长期记忆，先过 ${overview.reviewDueCount} 个旧词，再开新词更稳。",
            primaryLabel = "待复习",
            primaryValue = overview.reviewDueCount.toString(),
            secondaryLabel = "今日已学",
            secondaryValue = overview.studiedToday.toString(),
            actionLabel = "去复习",
            progress = dailyProgress,
        )
        pace.isAuto && pace.remainingDays != null && pace.remainingWords > 0 &&
            pace.target > overview.newWordTarget -> StudyFocusCue(
                kind = StudyFocusKind.PACE,
                title = "按考试节奏加速",
                message = "距考试还有 ${pace.remainingDays} 天，今天建议按 ${pace.target} 词推进，避免最后冲刺堆积。",
                primaryLabel = "推荐/天",
                primaryValue = pace.target.toString(),
                secondaryLabel = "剩余词",
                secondaryValue = pace.remainingWords.toString(),
                actionLabel = "按配速学新词",
                progress = dailyProgress,
            )
        rootSnapshot.totalRoots > 0 && rootProgress < 0.35f -> StudyFocusCue(
            kind = StudyFocusKind.ROOTS,
            title = "补词根覆盖",
            message = "词根覆盖还在起步期，优先学带 rootKey 的新词，用一个根串起一组同源词。",
            primaryLabel = "已触达词根",
            primaryValue = "${rootSnapshot.touchedRoots}/${rootSnapshot.totalRoots}",
            secondaryLabel = "聚簇词",
            secondaryValue = "${rootSnapshot.learnedClustered}/${rootSnapshot.totalClustered}",
            actionLabel = "看词根图谱",
            progress = rootProgress,
        )
        overview.newWordsRemaining > 0 -> StudyFocusCue(
            kind = StudyFocusKind.NEW_WORDS,
            title = "推进新词地基",
            message = "复习债已轻，适合补 ${overview.newWordTarget.coerceAtMost(overview.newWordsRemaining)} 个新词，保持词书向前滚动。",
            primaryLabel = "今日目标",
            primaryValue = overview.newWordTarget.coerceAtMost(overview.newWordsRemaining).toString(),
            secondaryLabel = "剩余新词",
            secondaryValue = overview.newWordsRemaining.toString(),
            actionLabel = "去学新词",
            progress = dailyProgress,
        )
        else -> StudyFocusCue(
            kind = StudyFocusKind.MOMENTUM,
            title = "保持复习手感",
            message = "今天的核心负担已经清掉，可以用 Review 或难词专攻巩固手感。",
            primaryLabel = "连续天数",
            primaryValue = overview.streakDays.toString(),
            secondaryLabel = "今日已学",
            secondaryValue = overview.studiedToday.toString(),
            actionLabel = "巩固一轮",
            progress = dailyProgress,
        )
    }
}

/**
 * Builds a 4-option multiple-choice question. Distractors come from the
 * provided pool, prioritizing words sharing the same rootKey (higher
 * difficulty + learning value). Falls back to random pool members when
 * same-root candidates are insufficient.
 *
 * Pure function: caller supplies the seed for deterministic testing.
 * Guarantees: options.size <= 4, correct term is always included,
 * distractors never equal the correct term (case-insensitive).
 */
fun buildQuizOptions(
    correctTerm: String,
    correctRootKey: String,
    pool: List<WordEntry>,
    seed: Long = System.nanoTime(),
): List<String> {
    val rng = kotlin.random.Random(seed)
    val correctLower = correctTerm.lowercase()
    val filtered = pool.asSequence()
        .map { it.term to it.rootKey }
        .filter { it.first.lowercase() != correctLower }
        .distinctBy { it.first.lowercase() }
        .toList()
    val sameRoot = filtered.filter { correctRootKey.isNotBlank() && it.second == correctRootKey }
    val rest = filtered - sameRoot.toSet()
    val ordered = sameRoot.shuffled(rng) + rest.shuffled(rng)
    val distractors = ordered.take(3).map { it.first }
    return (distractors + correctTerm).shuffled(rng)
}

/**
 * Replaces the target term in an example sentence with a visible blank for
 * cloze practice. Matching is case-insensitive and avoids partial matches
 * inside longer alphabetic words.
 */
fun blankTermInExample(example: String, term: String): String? {
    val cleanExample = example.trim()
    val cleanTerm = term.trim()
    if (cleanExample.isEmpty() || cleanTerm.isEmpty()) return null
    val pattern = Regex(
        "(?i)(?<![A-Za-z])${Regex.escape(cleanTerm)}(?![A-Za-z])",
    )
    if (!pattern.containsMatchIn(cleanExample)) return null
    return pattern.replaceFirst(cleanExample, "____")
}

/**
 * Returns a fuzzy-search distance when [query] is close enough to [term] to be
 * useful as a vocabulary-search fallback. Short queries are intentionally
 * ignored to avoid noisy results across 5k-word books.
 */
fun fuzzyTermMatchDistance(query: String, term: String): Double? {
    val q = query.trim().lowercase()
    val t = term.trim().lowercase()
    if (q.length < 4 || t.isEmpty()) return null
    if (t.contains(q)) return 0.0
    val distance = normalizedEditDistance(q, t)
    val maxDistance = when {
        q.length <= 5 -> 0.22
        q.length <= 8 -> 0.28
        else -> 0.32
    }
    return distance.takeIf { it <= maxDistance }
}

fun recordPracticeAttempt(
    stats: PracticeSessionStats,
    rating: ReviewRating,
): PracticeSessionStats {
    val stable = rating == ReviewRating.GOOD || rating == ReviewRating.EASY
    return stats.copy(
        answered = stats.answered + 1,
        stable = stats.stable + if (stable) 1 else 0,
        needsPractice = stats.needsPractice + if (stable) 0 else 1,
    )
}

/**
 * Levenshtein edit distance normalized to [0, 1] over the longer string,
 * case-insensitive. 0 = identical, 1 = totally different.
 */
fun normalizedEditDistance(a: String, b: String): Double {
    val s = a.trim().lowercase()
    val t = b.trim().lowercase()
    if (s == t) return 0.0
    if (s.isEmpty() || t.isEmpty()) return 1.0
    val n = s.length
    val m = t.length
    val prev = IntArray(m + 1) { it }
    val curr = IntArray(m + 1)
    for (i in 1..n) {
        curr[0] = i
        for (j in 1..m) {
            val cost = if (s[i - 1] == t[j - 1]) 0 else 1
            curr[j] = minOf(
                prev[j] + 1,
                curr[j - 1] + 1,
                prev[j - 1] + cost,
            )
        }
        System.arraycopy(curr, 0, prev, 0, m + 1)
    }
    val d = prev[m]
    return d.toDouble() / maxOf(n, m).toDouble()
}

/**
 * Maps a spelling/dictation attempt's edit distance to an SRS rating.
 *   0.00 exact            → EASY
 *   <= 0.15 one-or-two    → GOOD
 *   <= 0.35 bad spelling  → HARD
 *   else                  → AGAIN
 */
fun ratingFromEditDistance(distance: Double): ReviewRating = when {
    distance == 0.0 -> ReviewRating.EASY
    distance <= 0.15 -> ReviewRating.GOOD
    distance <= 0.35 -> ReviewRating.HARD
    else -> ReviewRating.AGAIN
}
