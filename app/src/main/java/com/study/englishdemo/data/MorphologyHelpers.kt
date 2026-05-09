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
