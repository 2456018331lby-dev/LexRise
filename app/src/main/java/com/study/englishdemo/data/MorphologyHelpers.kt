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

fun buildRootGroupInsight(group: RootGroup, focusLimit: Int = 3): RootGroupInsight {
    val total = group.totalWords.coerceAtLeast(0)
    val learned = group.learnedWords.coerceIn(0, total)
    val remaining = (total - learned).coerceAtLeast(0)
    val progress = if (total == 0) 0f else (learned.toFloat() / total.toFloat()).coerceIn(0f, 1f)
    val members = group.members.distinctBy { it.term.lowercase() }
    val untouchedTerms = members
        .filter {
            val phase = it.progress?.phase
            phase == null || phase == StudyPhase.NEW
        }
        .map { it.term }
    val learningTerms = members
        .filter { it.progress?.phase == StudyPhase.LEARNING }
        .map { it.term }
    val reviewTerms = members
        .filter { it.progress?.phase == StudyPhase.REVIEW }
        .map { it.term }
    val masteredTerms = members
        .filter { it.progress?.phase == StudyPhase.MASTERED }
        .map { it.term }
    val focusTerms = when {
        total == 0 -> emptyList()
        learned == 0 -> untouchedTerms
        learned == total -> reviewTerms + masteredTerms + learningTerms + untouchedTerms
        progress < 0.5f -> untouchedTerms + learningTerms + reviewTerms
        else -> learningTerms + reviewTerms + untouchedTerms + masteredTerms
    }
        .distinct()
        .take(focusLimit.coerceAtLeast(0))

    val stage = when {
        total == 0 || learned == 0 -> RootGroupStage.SEED
        learned == total -> RootGroupStage.MASTERED
        progress < 0.5f -> RootGroupStage.BUILDING
        else -> RootGroupStage.CONSOLIDATING
    }

    val title: String
    val badge: String
    val message: String
    val action: String
    when (stage) {
        RootGroupStage.SEED -> {
            title = "先建根族地基"
            badge = "起步"
            val firstTerm = focusTerms.firstOrNull()
            message = if (firstTerm == null) {
                "这组词根暂时没有可展示成员，先换一个词根继续浏览。"
            } else {
                "这组还没触达，先把 ${group.rootKey} 和 $firstTerm 绑定起来，再顺手扫同根词。"
            }
            action = "展开首攻词"
        }
        RootGroupStage.BUILDING -> {
            title = "沿同根继续扩展"
            badge = "推进"
            message = "已经触达 $learned 个，还有 $remaining 个待补；继续按同根扩展，记忆负担会比孤立背词更低。"
            action = "展开下一批"
        }
        RootGroupStage.CONSOLIDATING -> {
            title = "收束近义和形近差异"
            badge = "收尾"
            message = "大半词族已经入轨，现在适合回看学习中/复习词，把同根词之间的细微差异讲清楚。"
            action = "展开收尾词"
        }
        RootGroupStage.MASTERED -> {
            title = "整族已触达"
            badge = "稳固"
            message = "这一组已全部进入学习轨道，后续靠 Review 维持；混淆时回看成员 chip 复盘差异。"
            action = "回看整族"
        }
    }

    return RootGroupInsight(
        stage = stage,
        badgeLabel = badge,
        title = title,
        message = message,
        primaryLabel = "已触达",
        primaryValue = "$learned/$total",
        secondaryLabel = "剩余",
        secondaryValue = remaining.toString(),
        actionLabel = action,
        progress = progress,
        focusTerms = focusTerms,
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

private data class ClozeCandidate(
    val text: String,
    val isLemma: Boolean,
    val order: Int,
)

private data class ClozeCandidateMatch(
    val candidate: ClozeCandidate,
    val match: MatchResult,
)

fun buildClozeBlank(example: String, term: String, variants: List<String> = emptyList()): ClozeBlank? {
    val cleanExample = example.trim()
    val candidates = (
        listOf(ClozeCandidate(term, isLemma = true, order = 0)) +
            variants.mapIndexed { index, variant ->
                ClozeCandidate(variant, isLemma = false, order = index + 1)
            }
        )
        .map { it.copy(text = it.text.trim()) }
        .filter { it.text.isNotEmpty() }
        .distinctBy { it.text.lowercase() }
    if (cleanExample.isEmpty() || candidates.isEmpty()) return null

    val bestMatch = candidates
        .asSequence()
        .mapNotNull { candidate ->
            val pattern = Regex(
                "(?i)(?<![A-Za-z])${Regex.escape(candidate.text)}(?![A-Za-z])",
            )
            pattern.find(cleanExample)?.let { match -> ClozeCandidateMatch(candidate, match) }
        }
        .minWithOrNull(
            compareBy<ClozeCandidateMatch> { if (it.candidate.isLemma) 0 else 1 }
                .thenBy { it.match.range.first }
                .thenByDescending { it.candidate.text.length }
                .thenBy { it.candidate.order },
        ) ?: return null

    return ClozeBlank(
        prompt = cleanExample.replaceRange(bestMatch.match.range, "____"),
        answer = bestMatch.match.value,
    )
}

/**
 * Replaces the target term in an example sentence with a visible blank for
 * cloze practice. Matching is case-insensitive and avoids partial matches
 * inside longer alphabetic words.
 */
fun blankTermInExample(example: String, term: String): String? = buildClozeBlank(
    example = example,
    term = term,
)?.prompt

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

fun fuzzyWordFormMatchDistance(query: String, term: String, variants: List<String>): Double? {
    return (listOf(term) + variants)
        .asSequence()
        .mapNotNull { fuzzyTermMatchDistance(query, it) }
        .minOrNull()
}

fun matchingWordForms(query: String, term: String, variants: List<String>, limit: Int = 4): List<String> {
    val q = query.trim()
    if (q.length < 2 || limit <= 0) return emptyList()
    val forms = (listOf(term) + variants)
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .distinctBy { it.lowercase() }
    val exactMatches = forms.filter { it.contains(q, ignoreCase = true) }
    val matches = exactMatches.ifEmpty {
        forms
            .mapNotNull { form -> fuzzyTermMatchDistance(q, form)?.let { distance -> form to distance } }
            .sortedWith(compareBy<Pair<String, Double>> { it.second }.thenBy { it.first.length })
            .map { it.first }
    }
    return matches.take(limit)
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

fun buildPracticeSessionCoach(stats: PracticeSessionStats, mode: PracticeMode): PracticeSessionCoach {
    val answered = stats.answered.coerceAtLeast(0)
    val stable = stats.stable.coerceIn(0, answered)
    val needsPractice = stats.needsPractice.coerceIn(0, answered)
    val stabilityPercent = if (answered == 0) 0 else (stable * 100) / answered
    val progress = (stabilityPercent / 100f).coerceIn(0f, 1f)
    val modeLabel = practiceModeLabel(mode)
    return when {
        answered == 0 -> PracticeSessionCoach(
            kind = PracticeSessionCoachKind.WARMUP,
            title = "先校准手感",
            message = "从${modeLabel}开始，先完成 5 题建立本轮基线；别急着追速度。",
            primaryLabel = "热身目标",
            primaryValue = "5题",
            secondaryLabel = "当前完成",
            secondaryValue = "0",
            actionLabel = "开始第一题",
            progress = 0f,
        )
        needsPractice > stable || stabilityPercent < 50 -> PracticeSessionCoach(
            kind = PracticeSessionCoachKind.RECOVER,
            title = "先降速修复",
            message = "再练数已经压过稳答数，留在${modeLabel}逐题复盘；每题先说出词根或例句线索再评分。",
            primaryLabel = "再练",
            primaryValue = needsPractice.toString(),
            secondaryLabel = "稳答",
            secondaryValue = stable.toString(),
            actionLabel = "慢速复盘",
            progress = progress,
        )
        answered >= 10 && stabilityPercent >= 85 -> PracticeSessionCoach(
            kind = PracticeSessionCoachKind.ADVANCE,
            title = "可以提高难度",
            message = "${modeLabel}稳定率已经够高，下一组切到${practiceModeUpgradeLabel(mode)}，把识别提升到主动提取。",
            primaryLabel = "稳定率",
            primaryValue = "$stabilityPercent%",
            secondaryLabel = "完成",
            secondaryValue = answered.toString(),
            actionLabel = "加难一档",
            progress = progress,
        )
        else -> PracticeSessionCoach(
            kind = PracticeSessionCoachKind.STABILIZE,
            title = "继续稳住节奏",
            message = "当前稳定率可用，再做 ${maxOf(0, 10 - answered)} 题形成一组样本；若再练上升就降速复盘。",
            primaryLabel = "稳定率",
            primaryValue = "$stabilityPercent%",
            secondaryLabel = "再练",
            secondaryValue = needsPractice.toString(),
            actionLabel = "再做一组",
            progress = progress,
        )
    }
}

private fun practiceModeLabel(mode: PracticeMode): String = when (mode) {
    PracticeMode.FLIP -> "翻卡"
    PracticeMode.CHOICE -> "选择"
    PracticeMode.CLOZE -> "完形"
    PracticeMode.SPELL -> "拼写"
    PracticeMode.DICTATION -> "听写"
}

private fun practiceModeUpgradeLabel(mode: PracticeMode): String = when (mode) {
    PracticeMode.FLIP, PracticeMode.CHOICE -> "完形或拼写"
    PracticeMode.CLOZE -> "拼写"
    PracticeMode.SPELL -> "听写"
    PracticeMode.DICTATION -> "错词回放"
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
