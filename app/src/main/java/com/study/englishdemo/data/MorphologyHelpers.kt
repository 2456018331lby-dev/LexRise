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

fun buildDailyStudyRoute(
    session: LearningSession,
    rootSnapshot: BookRootSnapshot,
    pace: PaceRecommendation,
    toughWordCount: Int,
    maxSteps: Int = 3,
): DailyStudyRoute {
    val overview = session.overview
    val cappedSteps = maxSteps.coerceAtLeast(1)
    val rootProgress = if (rootSnapshot.totalRoots == 0) {
        1f
    } else {
        (rootSnapshot.touchedRoots.toFloat() / rootSnapshot.totalRoots.toFloat()).coerceIn(0f, 1f)
    }
    val routeSteps = mutableListOf<DailyStudyRouteStep>()
    if (overview.reviewDueCount > 0) {
        routeSteps += DailyStudyRouteStep(
            target = DailyStudyRouteTarget.REVIEW,
            title = "清复习债",
            reason = "先把到期旧词拉回长期记忆，再进入新内容。",
            metricLabel = "到期",
            metricValue = overview.reviewDueCount.toString(),
            actionLabel = "复习",
            weight = (overview.reviewDueCount.toFloat() / 24f).coerceIn(0.18f, 1f),
        )
    }
    if (toughWordCount > 0) {
        routeSteps += DailyStudyRouteStep(
            target = DailyStudyRouteTarget.TOUGH,
            title = "修错题",
            reason = "集中处理反复 AGAIN 的词，避免同一批词持续漏水。",
            metricLabel = "难词",
            metricValue = toughWordCount.toString(),
            actionLabel = "难词",
            weight = (toughWordCount.toFloat() / 12f).coerceIn(0.18f, 1f),
        )
    }
    if (rootSnapshot.totalRoots > 0 && rootProgress < 0.5f) {
        routeSteps += DailyStudyRouteStep(
            target = DailyStudyRouteTarget.ROOTS,
            title = "补词根网",
            reason = "先补低覆盖词根，用构词线索减少孤立记忆。",
            metricLabel = "触达",
            metricValue = "${rootSnapshot.touchedRoots}/${rootSnapshot.totalRoots}",
            actionLabel = "词根",
            weight = (1f - rootProgress).coerceIn(0.18f, 1f),
        )
    }
    if (overview.newWordsRemaining > 0) {
        val target = if (
            pace.isAuto &&
            pace.remainingWords > 0 &&
            pace.target > overview.newWordTarget
        ) {
            pace.target
        } else {
            overview.newWordTarget
        }
        val title = if (target > overview.newWordTarget) "按配速推进" else "补新词地基"
        val reason = if (target > overview.newWordTarget && pace.remainingDays != null) {
            "考试倒计时 ${pace.remainingDays} 天，按推荐节奏消化新词。"
        } else {
            "复习压力可控时，推进一小组新词保持词书滚动。"
        }
        routeSteps += DailyStudyRouteStep(
            target = DailyStudyRouteTarget.LEARN,
            title = title,
            reason = reason,
            metricLabel = "目标",
            metricValue = target.coerceAtMost(overview.newWordsRemaining).toString(),
            actionLabel = "新词",
            weight = (target.toFloat() / 40f).coerceIn(0.18f, 1f),
        )
    }
    if (routeSteps.isEmpty()) {
        routeSteps += DailyStudyRouteStep(
            target = DailyStudyRouteTarget.REVIEW,
            title = "稳态复盘",
            reason = "今天核心负担已清，做一轮轻复习保持手感。",
            metricLabel = "连续",
            metricValue = overview.streakDays.toString(),
            actionLabel = "巩固",
            weight = overview.completionRatio.coerceIn(0.18f, 1f),
        )
    }
    val selected = routeSteps.take(cappedSteps)
    val headline = when (selected.first().target) {
        DailyStudyRouteTarget.REVIEW -> "先稳记忆，再开新局"
        DailyStudyRouteTarget.TOUGH -> "先修漏点，再补地基"
        DailyStudyRouteTarget.ROOTS -> "先铺词根网，再滚新词"
        DailyStudyRouteTarget.LEARN -> "压力可控，推进新词"
    }
    return DailyStudyRoute(
        headline = headline,
        summary = selected.joinToString(" → ") { it.title },
        steps = selected,
    )
}

fun buildStudyRhythmBrief(
    counts: List<DailyReviewCount>,
    overview: DailyOverview,
    windowDays: Int = 7,
): StudyRhythmBrief {
    val window = windowDays.coerceAtLeast(1)
    val countsByDate = counts.associateBy { it.date }
    val recentCounts = (window - 1 downTo 0).map { offset ->
        val date = overview.today.minusDays(offset.toLong())
        val storedCount = countsByDate[date]?.count ?: 0
        if (date == overview.today) maxOf(storedCount, overview.studiedToday) else storedCount
    }
    val total = recentCounts.sum()
    val activeDays = recentCounts.count { it > 0 }
    val peak = recentCounts.maxOrNull() ?: 0
    val todayCount = recentCounts.lastOrNull() ?: 0
    val dailyLoad = (overview.newWordTarget + overview.reviewDueCount).coerceAtLeast(1)
    val todayProgress = (todayCount.toFloat() / dailyLoad.toFloat()).coerceIn(0f, 1f)
    val consistency = activeDays.toFloat() / window.toFloat()
    val baseMomentum = (overview.completionRatio.coerceIn(0f, 1f) * 0.45f) +
        (todayProgress * 0.35f) +
        (consistency * 0.20f)

    return when {
        total == 0 -> StudyRhythmBrief(
            kind = StudyRhythmBriefKind.QUIET,
            title = "七日节奏还没启动",
            message = "最近一周没有复习记录，先做一轮低压回温，让热力图重新亮起来。",
            primaryLabel = "7日复习",
            primaryValue = "0",
            secondaryLabel = "今日负载",
            secondaryValue = dailyLoad.toString(),
            actionLabel = "先点亮今天",
            momentum = 0f,
            recentCounts = recentCounts,
        )
        overview.reviewDueCount > 0 && activeDays <= 2 -> StudyRhythmBrief(
            kind = StudyRhythmBriefKind.RECOVERY,
            title = "先恢复复习连续性",
            message = "过去 $window 天只有 $activeDays 天有复习，到期词还有 ${overview.reviewDueCount} 个；今天先清旧词，不急着加新负载。",
            primaryLabel = "活跃天",
            primaryValue = "$activeDays/$window",
            secondaryLabel = "待复习",
            secondaryValue = overview.reviewDueCount.toString(),
            actionLabel = "恢复节奏",
            momentum = baseMomentum.coerceIn(0.18f, 0.62f),
            recentCounts = recentCounts,
        )
        todayCount >= dailyLoad && activeDays >= 3 -> StudyRhythmBrief(
            kind = StudyRhythmBriefKind.SURGE,
            title = "今天已经拉起冲刺",
            message = "今日完成 $todayCount 词，已经覆盖当前负载；后半程适合收口复盘，别再盲目叠加新词。",
            primaryLabel = "今日",
            primaryValue = todayCount.toString(),
            secondaryLabel = "7日总量",
            secondaryValue = total.toString(),
            actionLabel = "收口复盘",
            momentum = maxOf(baseMomentum, 0.68f).coerceIn(0f, 1f),
            recentCounts = recentCounts,
        )
        activeDays >= 5 -> StudyRhythmBrief(
            kind = StudyRhythmBriefKind.STEADY,
            title = "七日节奏稳定",
            message = "最近 $window 天里 $activeDays 天都有复习，节奏已经成形；保持小步清债，再推进新词最稳。",
            primaryLabel = "活跃天",
            primaryValue = "$activeDays/$window",
            secondaryLabel = "峰值",
            secondaryValue = peak.toString(),
            actionLabel = "保持节奏",
            momentum = baseMomentum.coerceIn(0.48f, 1f),
            recentCounts = recentCounts,
        )
        else -> StudyRhythmBrief(
            kind = StudyRhythmBriefKind.BALANCE,
            title = "节奏正在回稳",
            message = "七日内累计复习 $total 词，已经有 $activeDays 天动起来；今天按路线做完一组，把间隔补齐。",
            primaryLabel = "7日总量",
            primaryValue = total.toString(),
            secondaryLabel = "活跃天",
            secondaryValue = "$activeDays/$window",
            actionLabel = "稳步推进",
            momentum = baseMomentum.coerceIn(0.24f, 0.78f),
            recentCounts = recentCounts,
        )
    }
}

fun buildWordMemoryAnchor(
    word: WordEntry,
    rootRef: RootReference?,
    siblingCount: Int,
): WordMemoryAnchor {
    val cleanDerivatives = word.derivatives
        .map { it.trim() }
        .filter { it.isNotBlank() && !it.equals(word.term, ignoreCase = true) }
        .distinctBy { it.lowercase() }
    val rootKey = word.rootKey.ifBlank { rootRef?.key.orEmpty() }
    val phaseLabel = studyPhaseLabel(word.progress?.phase)
    return when {
        rootKey.isNotBlank() || rootRef != null -> {
            val meanings = rootRef?.meanings.orEmpty().take(2).joinToString(" / ").ifBlank { "同根线索" }
            val familyTerms = (rootRef?.examples.orEmpty() + cleanDerivatives)
                .map { it.trim() }
                .filter { it.isNotBlank() && !it.equals(word.term, ignoreCase = true) }
                .distinctBy { it.lowercase() }
                .take(4)
            WordMemoryAnchor(
                kind = WordMemoryAnchorKind.ROOT_FAMILY,
                badgeLabel = "词根锚",
                title = "先抓 $rootKey 这条线",
                message = "把 ${word.term} 放进 $rootKey 家族，先记“$meanings”，再看释义细节。",
                primaryLabel = "词根",
                primaryValue = rootKey,
                secondaryLabel = "同根邻居",
                secondaryValue = siblingCount.coerceAtLeast(0).toString(),
                actionLabel = "先看构词",
                focusTerms = familyTerms,
            )
        }
        cleanDerivatives.isNotEmpty() -> WordMemoryAnchor(
            kind = WordMemoryAnchorKind.WORD_FORMS,
            badgeLabel = "词形锚",
            title = "从词形家族入手",
            message = "先把原词和常见变化绑在一起，完形、拼写和听写都会更稳。",
            primaryLabel = "派生词",
            primaryValue = cleanDerivatives.size.toString(),
            secondaryLabel = "阶段",
            secondaryValue = phaseLabel,
            actionLabel = "看词形变化",
            focusTerms = cleanDerivatives.take(4),
        )
        word.example.isNotBlank() -> WordMemoryAnchor(
            kind = WordMemoryAnchorKind.CONTEXT,
            badgeLabel = "语境锚",
            title = "把词放回句子里",
            message = "先读例句，再遮住 ${word.term} 回想；能在语境里提取，才不是孤立背中文。",
            primaryLabel = "例句",
            primaryValue = "可用",
            secondaryLabel = "词性",
            secondaryValue = word.pos.ifBlank { "未标" },
            actionLabel = "读例句自测",
            focusTerms = emptyList(),
        )
        else -> WordMemoryAnchor(
            kind = WordMemoryAnchorKind.SOLO,
            badgeLabel = "基础锚",
            title = "先建最小记忆",
            message = "这个词线索较少，先用发音、中文释义和一次主动回忆建立第一层记忆。",
            primaryLabel = "阶段",
            primaryValue = phaseLabel,
            secondaryLabel = "频率",
            secondaryValue = if (word.frq > 0) word.frq.toString() else "未标",
            actionLabel = "翻卡自测",
            focusTerms = emptyList(),
        )
    }
}

fun buildRootWordGuide(
    word: WordEntry,
    rootMeanings: List<String>,
    familyTerms: List<String>,
    focusLimit: Int = 4,
): RootWordGuide {
    val cappedFocus = focusLimit.coerceAtLeast(0)
    val cleanDerivatives = word.derivatives
        .map { it.trim() }
        .filter { it.isNotBlank() && !it.equals(word.term, ignoreCase = true) }
        .distinctBy { it.lowercase() }
    val cleanFamilyTerms = familyTerms
        .map { it.trim() }
        .filter { it.isNotBlank() && !it.equals(word.term, ignoreCase = true) }
        .distinctBy { it.lowercase() }
    val rootKey = word.rootKey.trim()
    val phaseLabel = studyPhaseLabel(word.progress?.phase)
    return when {
        rootKey.isNotBlank() && (rootMeanings.isNotEmpty() || cleanFamilyTerms.isNotEmpty()) -> {
            val meaning = rootMeanings
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .take(2)
                .joinToString(" / ")
                .ifBlank { "同根线索" }
            val focusTerms = (cleanFamilyTerms + cleanDerivatives)
                .distinctBy { it.lowercase() }
                .take(cappedFocus)
            val familyCount = cleanFamilyTerms.size
            RootWordGuide(
                kind = RootWordGuideKind.ROOT_TRACE,
                badgeLabel = "根族导读",
                title = "先把 ${word.term} 放回 $rootKey 这条根线",
                message = "用“$rootKey = $meaning”定位核心义，再和同根词对照差异，能比单看释义更快稳住记忆。",
                primaryLabel = "词根",
                primaryValue = rootKey,
                secondaryLabel = "同根可比",
                secondaryValue = familyCount.toString(),
                actionLabel = "先比同根",
                intensity = ((familyCount + 1).toFloat() / 6f).coerceIn(0.24f, 1f),
                focusTerms = focusTerms,
            )
        }
        cleanDerivatives.isNotEmpty() -> RootWordGuide(
            kind = RootWordGuideKind.WORD_FORMS,
            badgeLabel = "词形导读",
            title = "先看 ${word.term} 的词形变化",
            message = "这张详情没有强词根线索，先把原词和派生形绑起来，再回到释义和例句。",
            primaryLabel = "派生词",
            primaryValue = cleanDerivatives.size.toString(),
            secondaryLabel = "阶段",
            secondaryValue = phaseLabel,
            actionLabel = "先扫词形",
            intensity = (cleanDerivatives.size.toFloat() / 4f).coerceIn(0.24f, 1f),
            focusTerms = cleanDerivatives.take(cappedFocus),
        )
        word.example.isNotBlank() -> RootWordGuide(
            kind = RootWordGuideKind.CONTEXT,
            badgeLabel = "语境导读",
            title = "把 ${word.term} 放回句子里确认",
            message = "先读例句，再回看中文释义；如果能在句子里复述它，就比只认中文更可靠。",
            primaryLabel = "例句",
            primaryValue = "可用",
            secondaryLabel = "词性",
            secondaryValue = word.pos.ifBlank { "未标" },
            actionLabel = "先读例句",
            intensity = 0.58f,
            focusTerms = emptyList(),
        )
        else -> RootWordGuide(
            kind = RootWordGuideKind.QUICK_REVIEW,
            badgeLabel = "速览导读",
            title = "先做一次最小回看",
            message = "这个词的外部线索较少，先听发音、看释义，再用一次主动回忆建立基本印象。",
            primaryLabel = "阶段",
            primaryValue = phaseLabel,
            secondaryLabel = "频率",
            secondaryValue = if (word.frq > 0) word.frq.toString() else "未标",
            actionLabel = "快速回看",
            intensity = 0.32f,
            focusTerms = emptyList(),
        )
    }
}

fun buildWordBatchBrief(words: List<WordEntry>, focusLimit: Int = 4): WordBatchBrief {
    val cleanWords = words.distinctBy { it.id }
    val total = cleanWords.size
    val cappedFocus = focusLimit.coerceAtLeast(0)
    if (total == 0) {
        return WordBatchBrief(
            kind = WordBatchBriefKind.EMPTY,
            title = "新词队列已清空",
            message = "当前没有待处理新词，可以去 Review 或难词专攻保持手感。",
            primaryLabel = "待学",
            primaryValue = "0",
            secondaryLabel = "建议",
            secondaryValue = "复习",
            actionLabel = "去巩固",
            intensity = 0f,
            focusTerms = emptyList(),
        )
    }

    val rootedWords = cleanWords.filter { it.rootKey.isNotBlank() }
    val derivativeWords = cleanWords.filter { word ->
        word.derivatives.any { it.isNotBlank() && !it.equals(word.term, ignoreCase = true) }
    }
    val contextWords = cleanWords.filter { it.example.isNotBlank() }
    val strongestRoot = rootedWords
        .groupingBy { it.rootKey }
        .eachCount()
        .maxWithOrNull(
            compareBy<Map.Entry<String, Int>> { it.value }
                .thenBy { it.key.length },
        )
    val rootShare = strongestRoot?.value?.toFloat()?.div(total.toFloat()) ?: 0f
    val derivativeShare = derivativeWords.size.toFloat() / total.toFloat()
    val contextShare = contextWords.size.toFloat() / total.toFloat()
    val focusTerms = when {
        strongestRoot != null && strongestRoot.value >= 2 -> cleanWords
            .filter { it.rootKey == strongestRoot.key }
            .map { it.term }
        derivativeWords.size >= 2 -> derivativeWords.map { it.term }
        contextWords.size >= 2 -> contextWords.map { it.term }
        else -> cleanWords.map { it.term }
    }
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .distinctBy { it.lowercase() }
        .take(cappedFocus)

    return when {
        strongestRoot != null && strongestRoot.value >= 2 -> WordBatchBrief(
            kind = WordBatchBriefKind.ROOTS,
            title = "这批先按 ${strongestRoot.key} 词根成组学",
            message = "当前新词里有 ${strongestRoot.value} 个同根词，先抓同一条构词线，再逐张翻卡补释义。",
            primaryLabel = "同根词",
            primaryValue = strongestRoot.value.toString(),
            secondaryLabel = "批次",
            secondaryValue = total.toString(),
            actionLabel = "先看词根",
            intensity = rootShare.coerceIn(0.18f, 1f),
            focusTerms = focusTerms,
        )
        derivativeWords.size >= 2 -> WordBatchBrief(
            kind = WordBatchBriefKind.WORD_FORMS,
            title = "先把词形变化绑成一组",
            message = "这批新词里派生形较多，先扫原词、过去式、复数或 -ing 形，后面的完形和拼写会更稳。",
            primaryLabel = "词形词",
            primaryValue = derivativeWords.size.toString(),
            secondaryLabel = "批次",
            secondaryValue = total.toString(),
            actionLabel = "先看词形",
            intensity = derivativeShare.coerceIn(0.18f, 1f),
            focusTerms = focusTerms,
        )
        contextWords.size >= 2 -> WordBatchBrief(
            kind = WordBatchBriefKind.CONTEXT,
            title = "这批适合放回例句里学",
            message = "多数词有可用例句，先读句子再翻卡；能在语境里提取，比只背中文释义更可靠。",
            primaryLabel = "有例句",
            primaryValue = contextWords.size.toString(),
            secondaryLabel = "批次",
            secondaryValue = total.toString(),
            actionLabel = "先读例句",
            intensity = contextShare.coerceIn(0.18f, 1f),
            focusTerms = focusTerms,
        )
        else -> WordBatchBrief(
            kind = WordBatchBriefKind.MIXED,
            title = "小批混合，逐张建立第一印象",
            message = "这批线索分散，先按卡片里的记忆锚走：有词根看词根，有词形看词形，其他用发音和释义建立最小记忆。",
            primaryLabel = "待学",
            primaryValue = total.toString(),
            secondaryLabel = "线索",
            secondaryValue = "混合",
            actionLabel = "逐张建立",
            intensity = (total.toFloat() / 10f).coerceIn(0.18f, 1f),
            focusTerms = focusTerms,
        )
    }
}

fun buildMnemonicBatchBrief(words: List<WordEntry>, focusLimit: Int = 4): MnemonicBatchBrief {
    val cleanWords = words.distinctBy { it.id }
    val total = cleanWords.size
    val cappedFocus = focusLimit.coerceAtLeast(0)
    if (total == 0) {
        return MnemonicBatchBrief(
            kind = MnemonicBatchBriefKind.EMPTY,
            title = "巧记队列暂时清空",
            message = "当前没有待学新词，先去 Review 或难词专攻保持主动回忆。",
            primaryLabel = "待学",
            primaryValue = "0",
            secondaryLabel = "巧记",
            secondaryValue = "0",
            actionLabel = "去巩固",
            coverage = 0f,
            focusTerms = emptyList(),
        )
    }

    val mnemonicWords = cleanWords.filter { it.mnemonic.isNotBlank() }
    val blankMnemonicWords = cleanWords.filter { it.mnemonic.isBlank() }
    val rootedWords = cleanWords.filter { it.rootKey.isNotBlank() }
    val derivativeWords = cleanWords.filter { word ->
        word.derivatives.any { it.isNotBlank() && !it.equals(word.term, ignoreCase = true) }
    }
    val coverage = mnemonicWords.size.toFloat() / total.toFloat()
    val rootShare = rootedWords.size.toFloat() / total.toFloat()
    val formShare = derivativeWords.size.toFloat() / total.toFloat()
    val coverageLabel = "${(coverage * 100f).toInt()}%"

    fun focusFrom(source: List<WordEntry>): List<String> = source
        .map { it.term.trim() }
        .filter { it.isNotEmpty() }
        .distinctBy { it.lowercase() }
        .take(cappedFocus)

    return when {
        mnemonicWords.isNotEmpty() && coverage >= 0.5f -> MnemonicBatchBrief(
            kind = MnemonicBatchBriefKind.READY,
            title = "先用现成巧记开局",
            message = "这批有 ${mnemonicWords.size} 个词已经带巧记，先读线索再翻卡，能把第一印象压得更稳。",
            primaryLabel = "有巧记",
            primaryValue = mnemonicWords.size.toString(),
            secondaryLabel = "覆盖",
            secondaryValue = coverageLabel,
            actionLabel = "先读巧记",
            coverage = coverage.coerceIn(0f, 1f),
            focusTerms = focusFrom(mnemonicWords),
        )
        rootedWords.size >= 2 && mnemonicWords.isEmpty() -> MnemonicBatchBrief(
            kind = MnemonicBatchBriefKind.ROOT_BRIDGE,
            title = "先用词根桥接空白巧记",
            message = "这批暂时没有巧记，但有 ${rootedWords.size} 个词带 rootKey；先借词根成组，再给难记词补个人口诀。",
            primaryLabel = "可桥接",
            primaryValue = rootedWords.size.toString(),
            secondaryLabel = "覆盖",
            secondaryValue = "${(rootShare * 100f).toInt()}%",
            actionLabel = "先借词根",
            coverage = rootShare.coerceIn(0.12f, 1f),
            focusTerms = focusFrom(rootedWords),
        )
        mnemonicWords.isNotEmpty() && blankMnemonicWords.size >= 2 -> MnemonicBatchBrief(
            kind = MnemonicBatchBriefKind.SEED_GAP,
            title = "这批适合补个人巧记",
            message = "已有巧记覆盖 $coverageLabel，还有 ${blankMnemonicWords.size} 个词是空白；遇到卡顿词时补一句自己的线索，不要一次性硬编全批。",
            primaryLabel = "待补",
            primaryValue = blankMnemonicWords.size.toString(),
            secondaryLabel = "已有",
            secondaryValue = mnemonicWords.size.toString(),
            actionLabel = "边学边补",
            coverage = coverage.coerceIn(0f, 1f),
            focusTerms = focusFrom(blankMnemonicWords),
        )
        else -> MnemonicBatchBrief(
            kind = MnemonicBatchBriefKind.QUICK_START,
            title = "先做最小速记起步",
            message = "这批巧记和词根线索都不密集，先靠发音、释义和词形建立第一印象；真正卡住的词再补巧记。",
            primaryLabel = "待学",
            primaryValue = total.toString(),
            secondaryLabel = "词形线索",
            secondaryValue = derivativeWords.size.toString(),
            actionLabel = "先翻卡",
            coverage = maxOf(coverage, formShare * 0.6f).coerceIn(0.10f, 0.55f),
            focusTerms = focusFrom(cleanWords),
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

fun buildRootAtlasBrief(
    groups: List<RootGroup>,
    snapshot: BookRootSnapshot,
    focusLimit: Int = 4,
): RootAtlasBrief {
    val visibleGroups = groups
        .filter { it.rootKey.isNotBlank() && it.totalWords > 0 }
        .distinctBy { it.rootKey.lowercase() }
    val totalRoots = if (snapshot.totalRoots > 0) snapshot.totalRoots else visibleGroups.size
    val totalClustered = if (snapshot.totalClustered > 0) {
        snapshot.totalClustered
    } else {
        visibleGroups.sumOf { it.totalWords.coerceAtLeast(0) }
    }
    if (totalRoots <= 0 || totalClustered <= 0) {
        return RootAtlasBrief(
            kind = RootAtlasBriefKind.EMPTY,
            title = "词根图谱待生成",
            message = "当前词书暂时没有可聚合的词根线索，先回到新词或词汇页建立基础词条。",
            primaryLabel = "词根",
            primaryValue = "0",
            secondaryLabel = "词条",
            secondaryValue = "0",
            actionLabel = "先学新词",
            progress = 0f,
            focusRoots = emptyList(),
        )
    }

    val touchedRoots = if (snapshot.totalRoots > 0) {
        snapshot.touchedRoots.coerceIn(0, totalRoots)
    } else {
        visibleGroups.count { it.learnedWords.coerceAtLeast(0) > 0 }
    }
    val learnedClustered = if (snapshot.totalClustered > 0) {
        snapshot.learnedClustered.coerceIn(0, totalClustered)
    } else {
        visibleGroups.sumOf { group -> group.learnedWords.coerceIn(0, group.totalWords.coerceAtLeast(0)) }
    }
    val coverage = (touchedRoots.toFloat() / totalRoots.toFloat()).coerceIn(0f, 1f)
    val clusteredProgress = (learnedClustered.toFloat() / totalClustered.toFloat()).coerceIn(0f, 1f)
    val partialGroups = visibleGroups.filter {
        val total = it.totalWords.coerceAtLeast(0)
        val learned = it.learnedWords.coerceIn(0, total)
        learned in 1 until total
    }

    val kind = when {
        touchedRoots == 0 || learnedClustered == 0 -> RootAtlasBriefKind.SEED
        coverage < 0.35f -> RootAtlasBriefKind.EXPAND
        coverage >= 0.90f && clusteredProgress >= 0.88f -> RootAtlasBriefKind.MASTERED
        else -> RootAtlasBriefKind.CONSOLIDATE
    }
    val focusLimitSafe = focusLimit.coerceAtLeast(0)
    fun RootGroup.progressRatio(): Float {
        val total = totalWords.coerceAtLeast(0)
        val learned = learnedWords.coerceIn(0, total)
        return if (total == 0) 0f else learned.toFloat() / total.toFloat()
    }
    val focusSource = when (kind) {
        RootAtlasBriefKind.EMPTY -> emptyList()
        RootAtlasBriefKind.SEED -> visibleGroups
            .filter { it.learnedWords.coerceAtMost(it.totalWords) <= 0 }
            .sortedWith(compareByDescending<RootGroup> { it.totalWords }.thenBy { it.rootKey })
        RootAtlasBriefKind.EXPAND -> visibleGroups
            .filter { it.learnedWords < it.totalWords }
            .sortedWith(
                compareBy<RootGroup> { if (it.learnedWords > 0) 1 else 0 }
                    .thenByDescending { it.totalWords }
                    .thenBy { it.rootKey },
            )
        RootAtlasBriefKind.CONSOLIDATE -> partialGroups
            .sortedWith(
                compareByDescending<RootGroup> { it.progressRatio() }
                    .thenByDescending { it.totalWords }
                    .thenBy { it.rootKey },
            )
            .ifEmpty {
                visibleGroups.sortedWith(
                    compareByDescending<RootGroup> { it.progressRatio() }
                        .thenByDescending { it.totalWords }
                        .thenBy { it.rootKey },
                )
            }
        RootAtlasBriefKind.MASTERED -> visibleGroups
            .filter { it.learnedWords >= it.totalWords }
            .sortedWith(compareByDescending<RootGroup> { it.totalWords }.thenBy { it.rootKey })
            .ifEmpty {
                visibleGroups.sortedWith(
                    compareByDescending<RootGroup> { it.progressRatio() }
                        .thenByDescending { it.totalWords }
                        .thenBy { it.rootKey },
                )
            }
    }
    val focusRoots = focusSource
        .map { it.rootKey }
        .distinct()
        .take(focusLimitSafe)

    val title: String
    val message: String
    val action: String
    when (kind) {
        RootAtlasBriefKind.EMPTY -> {
            title = "词根图谱待生成"
            message = "当前词书暂时没有可聚合的词根线索，先回到新词或词汇页建立基础词条。"
            action = "先学新词"
        }
        RootAtlasBriefKind.SEED -> {
            title = "先给词根图谱打地基"
            message = "整本书还没有触达词根，先从成员多的根族开局，把 rootKey 和第一批高频词绑在一起。"
            action = "先开地基根"
        }
        RootAtlasBriefKind.EXPAND -> {
            title = "词根覆盖偏低，先铺开图谱"
            message = "已触达 $touchedRoots/$totalRoots 个词根，优先打开未触达的大根族，让后续新词不再孤立出现。"
            action = "扩展图谱"
        }
        RootAtlasBriefKind.CONSOLIDATE -> {
            title = "图谱已铺开，收束半熟根族"
            message = "已有 $touchedRoots 个词根入轨，下一步适合补齐学习中根族，把相近词义和词形差异讲清楚。"
            action = "收束半熟根"
        }
        RootAtlasBriefKind.MASTERED -> {
            title = "词根图谱基本铺开"
            message = "多数聚簇词已经进入学习轨道，后续靠 Review 稳住；混淆时回看强根族做横向复盘。"
            action = "回看强根族"
        }
    }

    return RootAtlasBrief(
        kind = kind,
        title = title,
        message = message,
        primaryLabel = "词根覆盖",
        primaryValue = "$touchedRoots/$totalRoots",
        secondaryLabel = "词条入轨",
        secondaryValue = "$learnedClustered/$totalClustered",
        actionLabel = action,
        progress = coverage,
        focusRoots = focusRoots,
    )
}

fun buildRootMnemonicBrief(groups: List<RootGroup>, focusLimit: Int = 4): RootMnemonicBrief {
    data class RootMnemonicStats(
        val rootKey: String,
        val totalWords: Int,
        val mnemonicWords: Int,
        val blankWords: Int,
    )

    val stats = groups
        .filter { it.rootKey.isNotBlank() && it.totalWords > 0 }
        .distinctBy { it.rootKey.lowercase() }
        .map { group ->
            val members = group.members.distinctBy { it.id }
            val total = if (members.isNotEmpty()) {
                members.size
            } else {
                group.totalWords
            }.coerceAtLeast(0)
            val mnemonicCount = members
                .count { it.mnemonic.isNotBlank() }
                .coerceIn(0, total)
            RootMnemonicStats(
                rootKey = group.rootKey,
                totalWords = total,
                mnemonicWords = mnemonicCount,
                blankWords = (total - mnemonicCount).coerceAtLeast(0),
            )
        }
        .filter { it.totalWords > 0 }

    val totalRoots = stats.size
    val totalWords = stats.sumOf { it.totalWords }
    val mnemonicWords = stats.sumOf { it.mnemonicWords }
    val seededRoots = stats.count { it.mnemonicWords > 0 }
    val blankWords = (totalWords - mnemonicWords).coerceAtLeast(0)
    val coverage = if (totalWords == 0) 0f else (mnemonicWords.toFloat() / totalWords.toFloat()).coerceIn(0f, 1f)
    val rootCoverage = if (totalRoots == 0) 0f else (seededRoots.toFloat() / totalRoots.toFloat()).coerceIn(0f, 1f)
    val focusLimitSafe = focusLimit.coerceAtLeast(0)

    if (totalRoots == 0 || totalWords == 0) {
        return RootMnemonicBrief(
            kind = RootMnemonicBriefKind.EMPTY,
            title = "根族巧记暂时无图谱",
            message = "当前词书没有可聚合的词根成员，先回到新词页建立基础词条，再回来补巧记网络。",
            primaryLabel = "巧记词",
            primaryValue = "0",
            secondaryLabel = "根族",
            secondaryValue = "0",
            actionLabel = "先学新词",
            progress = 0f,
            focusRoots = emptyList(),
        )
    }

    val kind = when {
        mnemonicWords == 0 -> RootMnemonicBriefKind.ROOT_SEED
        coverage >= 0.85f && rootCoverage >= 0.70f -> RootMnemonicBriefKind.SATURATED
        coverage >= 0.40f || (coverage >= 0.30f && rootCoverage >= 0.70f) -> RootMnemonicBriefKind.READY
        else -> RootMnemonicBriefKind.PATCH_GAPS
    }

    val focusSource = when (kind) {
        RootMnemonicBriefKind.EMPTY -> emptyList()
        RootMnemonicBriefKind.ROOT_SEED -> stats.sortedWith(
            compareByDescending<RootMnemonicStats> { it.totalWords }
                .thenBy { it.rootKey },
        )
        RootMnemonicBriefKind.PATCH_GAPS -> stats
            .filter { it.mnemonicWords > 0 && it.blankWords > 0 }
            .ifEmpty { stats.filter { it.blankWords > 0 } }
            .sortedWith(
                compareByDescending<RootMnemonicStats> { it.blankWords }
                    .thenByDescending { it.mnemonicWords }
                    .thenBy { it.rootKey },
            )
        RootMnemonicBriefKind.READY -> stats
            .filter { it.mnemonicWords > 0 }
            .sortedWith(
                compareByDescending<RootMnemonicStats> { it.mnemonicWords }
                    .thenByDescending { it.totalWords }
                    .thenBy { it.rootKey },
            )
        RootMnemonicBriefKind.SATURATED -> stats
            .filter { it.mnemonicWords > 0 }
            .sortedWith(
                compareByDescending<RootMnemonicStats> { it.totalWords }
                    .thenBy { it.rootKey },
            )
    }
    val focusRoots = focusSource
        .map { it.rootKey }
        .distinct()
        .take(focusLimitSafe)

    val coverageLabel = "${(coverage * 100f).toInt()}%"
    val title: String
    val message: String
    val action: String
    when (kind) {
        RootMnemonicBriefKind.EMPTY -> {
            title = "根族巧记暂时无图谱"
            message = "当前词书没有可聚合的词根成员，先回到新词页建立基础词条，再回来补巧记网络。"
            action = "先学新词"
        }
        RootMnemonicBriefKind.ROOT_SEED -> {
            title = "先给大根族补第一条巧记"
            message = "当前可见根族还没有巧记 seed；优先挑成员多的根族，各补 1 个地基词线索，比零散补词更能形成网络。"
            action = "补地基词"
        }
        RootMnemonicBriefKind.PATCH_GAPS -> {
            title = "已有巧记种子，先补根族缺口"
            message = "已有 $mnemonicWords 个巧记词，但还有 $blankWords 个同根成员空白；沿已有 seed 补相邻词，能复用同一条记忆线。"
            action = "补根族缺口"
        }
        RootMnemonicBriefKind.READY -> {
            title = "巧记根族已经能带路"
            message = "当前巧记覆盖 $coverageLabel，已有 $seededRoots 个根族带 seed；先回看这些根族，再把最容易混淆的空白词补成短线索。"
            action = "回看有种子根"
        }
        RootMnemonicBriefKind.SATURATED -> {
            title = "巧记网络基本成型"
            message = "大部分根族已经有可用巧记，后续只在 Review 卡顿时补缺；避免为了凑覆盖率硬写低质量口诀。"
            action = "抽查强根族"
        }
    }

    return RootMnemonicBrief(
        kind = kind,
        title = title,
        message = message,
        primaryLabel = "巧记词",
        primaryValue = "$mnemonicWords/$totalWords",
        secondaryLabel = "已播根族",
        secondaryValue = "$seededRoots/$totalRoots",
        actionLabel = action,
        progress = coverage,
        focusRoots = focusRoots,
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

fun buildClozeContextGuide(question: ClozeQuestion, focusLimit: Int = 4): ClozeContextGuide {
    val word = question.word
    val cappedFocus = focusLimit.coerceAtLeast(0)
    val promptWords = clozePromptFocusTerms(question.prompt, cappedFocus)
    val sentenceLength = Regex("[A-Za-z]+").findAll(question.prompt.replace("____", " ")).count()
    val optionCount = question.options.distinctBy { it.lowercase() }.size
    val answerIsDerivative = !question.correct.equals(word.term, ignoreCase = true)
    return when {
        answerIsDerivative -> ClozeContextGuide(
            kind = ClozeContextGuideKind.WORD_FORM,
            badgeLabel = "词形导读",
            title = "先判断空格需要哪种词形",
            message = "这题挖掉的是原词的词形变化；先看空格前后的时态、语法位置和候选词形，再决定答案。",
            primaryLabel = "答案类型",
            primaryValue = "派生形",
            secondaryLabel = "候选",
            secondaryValue = optionCount.toString(),
            actionLabel = "先判词形",
            confidence = 0.74f,
            focusTerms = promptWords,
        )
        word.rootKey.isNotBlank() -> ClozeContextGuide(
            kind = ClozeContextGuideKind.ROOT_TRACE,
            badgeLabel = "词根导读",
            title = "先用 ${word.rootKey} 锚住空格",
            message = "空格附近的语境先给方向，再用词根 ${word.rootKey} 约束含义，避免只靠中文释义猜选项。",
            primaryLabel = "词根",
            primaryValue = word.rootKey,
            secondaryLabel = "句长",
            secondaryValue = sentenceLength.coerceAtLeast(1).toString(),
            actionLabel = "先抓词根",
            confidence = if (promptWords.isEmpty()) 0.52f else 0.68f,
            focusTerms = promptWords,
        )
        word.translation.isNotBlank() || word.definition.isNotBlank() -> ClozeContextGuide(
            kind = ClozeContextGuideKind.MEANING,
            badgeLabel = "语义导读",
            title = "先读完整语境再看候选",
            message = "这题没有强词根或词形线索，先把空格所在句读顺，再用中文释义和词性缩小选项。",
            primaryLabel = "释义",
            primaryValue = "可参考",
            secondaryLabel = "词性",
            secondaryValue = word.pos.ifBlank { "未标" },
            actionLabel = "先读语境",
            confidence = 0.56f,
            focusTerms = promptWords,
        )
        else -> ClozeContextGuide(
            kind = ClozeContextGuideKind.QUICK_SCAN,
            badgeLabel = "速扫导读",
            title = "先做一次最小判断",
            message = "外部线索较少，先扫空格左右两侧，再在候选里找最自然的搭配。",
            primaryLabel = "候选",
            primaryValue = optionCount.toString(),
            secondaryLabel = "句长",
            secondaryValue = sentenceLength.coerceAtLeast(1).toString(),
            actionLabel = "快速判断",
            confidence = 0.34f,
            focusTerms = promptWords,
        )
    }
}

private fun clozePromptFocusTerms(prompt: String, focusLimit: Int): List<String> {
    if (focusLimit <= 0) return emptyList()
    return Regex("[A-Za-z]{3,}")
        .findAll(prompt.replace("____", " "))
        .map { it.value.trim('\'', '"', '.', ',', ';', ':', '!', '?') }
        .filter { it.length >= 3 }
        .distinctBy { it.lowercase() }
        .take(focusLimit)
        .toList()
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

fun buildVocabularySearchInsight(
    query: String,
    results: List<WordEntry>,
    focusLimit: Int = 4,
): VocabularySearchInsight {
    val trimmed = query.trim()
    val cappedFocus = focusLimit.coerceAtLeast(0)
    if (trimmed.isEmpty()) {
        return VocabularySearchInsight(
            kind = VocabularySearchInsightKind.READY,
            title = "输入一个线索开始定位",
            message = "可以搜原词、派生词、中文释义或英文解释；英文拼写有轻微错误也会尝试找回。",
            primaryLabel = "检索范围",
            primaryValue = "全词书",
            secondaryLabel = "支持",
            secondaryValue = "词形容错",
            actionLabel = "先搜一个词",
            confidence = 0f,
            focusTerms = emptyList(),
        )
    }
    if (results.isEmpty()) {
        return VocabularySearchInsight(
            kind = VocabularySearchInsightKind.EMPTY_RESULTS,
            title = "这条线索暂时没命中",
            message = "换成原形、常见派生词或中文释义再试；很短的英文 typo 会被刻意忽略，避免噪声过多。",
            primaryLabel = "结果",
            primaryValue = "0",
            secondaryLabel = "建议",
            secondaryValue = "换词形",
            actionLabel = "改关键词",
            confidence = 0f,
            focusTerms = emptyList(),
        )
    }

    val directTermHits = results
        .filter { it.term.contains(trimmed, ignoreCase = true) }
        .distinctBy { it.term.lowercase() }
    val formHits = results
        .map { word ->
            word to matchingWordForms(
                query = trimmed,
                term = word.term,
                variants = word.derivatives,
                limit = 2,
            )
        }
        .filter { (_, forms) -> forms.isNotEmpty() }
    val formHitCount = formHits.size
    val meaningHits = results.count { word ->
        word.translation.contains(trimmed, ignoreCase = true) ||
            word.definition.contains(trimmed, ignoreCase = true)
    }
    val rootCluster = results
        .mapNotNull { word -> word.rootKey.takeIf { it.isNotBlank() } }
        .groupingBy { it }
        .eachCount()
        .filterValues { it >= 2 }
        .maxWithOrNull(
            compareBy<Map.Entry<String, Int>> { it.value }
                .thenBy { it.key.length },
        )
    val focusTerms = when {
        directTermHits.isNotEmpty() -> directTermHits.map { it.term }
        formHits.isNotEmpty() -> formHits.flatMap { (word, forms) ->
            forms.map { form -> if (form.equals(word.term, ignoreCase = true)) word.term else form }
        }
        rootCluster != null -> results.filter { it.rootKey == rootCluster.key }.map { it.term }
        else -> results.map { it.term }
    }
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .distinctBy { it.lowercase() }
        .take(cappedFocus)

    return when {
        directTermHits.isNotEmpty() -> VocabularySearchInsight(
            kind = VocabularySearchInsightKind.TERM_MATCH,
            title = "已直接命中原词",
            message = "结果里有 ${directTermHits.size} 个词条包含“$trimmed”，优先看这些原词，再顺手扫同根和派生词。",
            primaryLabel = "原词命中",
            primaryValue = directTermHits.size.toString(),
            secondaryLabel = "总结果",
            secondaryValue = results.size.toString(),
            actionLabel = "先看原词",
            confidence = (directTermHits.size.toFloat() / results.size.toFloat()).coerceIn(0.2f, 1f),
            focusTerms = focusTerms,
        )
        formHitCount > 0 -> VocabularySearchInsight(
            kind = VocabularySearchInsightKind.WORD_FORM_MATCH,
            title = "这是词形命中",
            message = "“$trimmed” 更像派生词或轻微 typo，先看词形雷达里的命中形，再回到原词建立记忆。",
            primaryLabel = "词形命中",
            primaryValue = formHitCount.toString(),
            secondaryLabel = "总结果",
            secondaryValue = results.size.toString(),
            actionLabel = "看词形雷达",
            confidence = (formHitCount.toFloat() / results.size.toFloat()).coerceIn(0.24f, 1f),
            focusTerms = focusTerms,
        )
        rootCluster != null -> VocabularySearchInsight(
            kind = VocabularySearchInsightKind.ROOT_CLUSTER,
            title = "结果集中在 ${rootCluster.key} 词根",
            message = "这次命中形成了同根小簇，适合把这些词放在一起比较，而不是逐个孤立背。",
            primaryLabel = "同根结果",
            primaryValue = rootCluster.value.toString(),
            secondaryLabel = "词根",
            secondaryValue = rootCluster.key,
            actionLabel = "按词根看",
            confidence = (rootCluster.value.toFloat() / results.size.toFloat()).coerceIn(0.25f, 1f),
            focusTerms = focusTerms,
        )
        else -> VocabularySearchInsight(
            kind = VocabularySearchInsightKind.MEANING_MATCH,
            title = "主要是释义线索命中",
            message = "这次更像中文释义或英文解释命中；先看前几条，再用词根和例句判断是不是你要找的词。",
            primaryLabel = "释义命中",
            primaryValue = meaningHits.toString(),
            secondaryLabel = "总结果",
            secondaryValue = results.size.toString(),
            actionLabel = if (trimmed.length >= 4) "对照释义" else "继续缩小",
            confidence = (meaningHits.toFloat() / results.size.toFloat()).coerceIn(0.12f, 1f),
            focusTerms = focusTerms,
        )
    }
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

fun buildReviewQueueBrief(
    words: List<WordEntry>,
    mode: PracticeMode,
    focusLimit: Int = 4,
): ReviewQueueBrief {
    val cleanWords = words.distinctBy { it.id }
    val total = cleanWords.size
    val cappedFocus = focusLimit.coerceAtLeast(0)
    if (total == 0) {
        return ReviewQueueBrief(
            kind = ReviewQueueBriefKind.EMPTY,
            title = "复习队列已清空",
            message = "当前没有到期词，适合去新词页推进一小组，或到难词专攻做轻量巩固。",
            primaryLabel = "到期",
            primaryValue = "0",
            secondaryLabel = "模式",
            secondaryValue = practiceModeLabel(mode),
            actionLabel = "去新词",
            intensity = 0f,
            focusTerms = emptyList(),
        )
    }

    val rootedWords = cleanWords.filter { it.rootKey.isNotBlank() }
    val contextWords = cleanWords.filter { it.example.isNotBlank() }
    val derivativeWords = cleanWords.filter { word ->
        word.derivatives.any { it.isNotBlank() && !it.equals(word.term, ignoreCase = true) }
    }
    val strongestRoot = rootedWords
        .groupingBy { it.rootKey }
        .eachCount()
        .filterValues { it >= 2 }
        .maxWithOrNull(
            compareBy<Map.Entry<String, Int>> { it.value }
                .thenBy { it.key.length },
        )
    val rootShare = strongestRoot?.value?.toFloat()?.div(total.toFloat()) ?: 0f
    val contextShare = contextWords.size.toFloat() / total.toFloat()
    val derivativeShare = derivativeWords.size.toFloat() / total.toFloat()
    val focusTerms = when {
        strongestRoot != null -> cleanWords.filter { it.rootKey == strongestRoot.key }.map { it.term }
        mode == PracticeMode.CLOZE && contextWords.isNotEmpty() -> contextWords.map { it.term }
        mode == PracticeMode.SPELL || mode == PracticeMode.DICTATION -> derivativeWords.ifEmpty { cleanWords }.map { it.term }
        contextWords.size >= 2 -> contextWords.map { it.term }
        derivativeWords.size >= 2 -> derivativeWords.map { it.term }
        else -> cleanWords.map { it.term }
    }
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .distinctBy { it.lowercase() }
        .take(cappedFocus)

    fun activeRecallBrief() = ReviewQueueBrief(
        kind = ReviewQueueBriefKind.ACTIVE_RECALL,
        title = "进入主动提取模式",
        message = if (derivativeWords.isEmpty()) {
            "这轮适合把识别推进到拼写或听写；先听音或看中文，在心里完整拼出再提交答案。"
        } else {
            "这轮适合把识别推进到拼写或听写；先扫词形变化，再提交答案。"
        },
        primaryLabel = if (derivativeWords.isEmpty()) "到期" else "词形词",
        primaryValue = if (derivativeWords.isEmpty()) total.toString() else derivativeWords.size.toString(),
        secondaryLabel = "模式",
        secondaryValue = practiceModeLabel(mode),
        actionLabel = "主动提取",
        intensity = maxOf(derivativeShare, total.toFloat() / 12f).coerceIn(0.18f, 1f),
        focusTerms = focusTerms,
    )

    return when {
        strongestRoot != null -> ReviewQueueBrief(
            kind = ReviewQueueBriefKind.ROOT_TRACE,
            title = "这轮先按 ${strongestRoot.key} 词根修",
            message = "到期词里同根聚集，先把这组词的共同线索说出来，再进入 ${practiceModeLabel(mode)}。",
            primaryLabel = "同根",
            primaryValue = strongestRoot.value.toString(),
            secondaryLabel = "到期",
            secondaryValue = total.toString(),
            actionLabel = "先按词根",
            intensity = rootShare.coerceIn(0.18f, 1f),
            focusTerms = focusTerms,
        )
        mode == PracticeMode.SPELL || mode == PracticeMode.DICTATION -> activeRecallBrief()
        total <= 3 && mode == PracticeMode.FLIP -> ReviewQueueBrief(
            kind = ReviewQueueBriefKind.WARMUP,
            title = "先做轻量回温",
            message = "到期词不多，用翻卡快速确认记忆状态；翻出前先在心里说出词义或构词线索。",
            primaryLabel = "到期",
            primaryValue = total.toString(),
            secondaryLabel = "模式",
            secondaryValue = practiceModeLabel(mode),
            actionLabel = "快翻确认",
            intensity = (total.toFloat() / 6f).coerceIn(0.18f, 1f),
            focusTerms = focusTerms,
        )
        (mode == PracticeMode.CLOZE && contextWords.isNotEmpty()) || contextShare >= 0.5f -> ReviewQueueBrief(
            kind = ReviewQueueBriefKind.CONTEXT,
            title = "用语境把旧词拉回来",
            message = "这轮有 ${contextWords.size} 个词带例句，先读上下文再作答，避免只靠中文释义回忆。",
            primaryLabel = "有例句",
            primaryValue = contextWords.size.toString(),
            secondaryLabel = "到期",
            secondaryValue = total.toString(),
            actionLabel = "先看语境",
            intensity = contextShare.coerceIn(0.18f, 1f),
            focusTerms = focusTerms,
        )
        derivativeShare >= 0.45f -> activeRecallBrief()
        else -> ReviewQueueBrief(
            kind = ReviewQueueBriefKind.MIXED,
            title = "按当前模式稳步清债",
            message = "这轮到期词线索分散，先保持 ${practiceModeLabel(mode)} 节奏；错的词会自动回流到难词专攻。",
            primaryLabel = "到期",
            primaryValue = total.toString(),
            secondaryLabel = "模式",
            secondaryValue = practiceModeLabel(mode),
            actionLabel = "开始清债",
            intensity = (total.toFloat() / 20f).coerceIn(0.18f, 1f),
            focusTerms = focusTerms,
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

fun buildToughWordPrescription(tough: ToughWord): ToughWordPrescription {
    val again = tough.againCount.coerceAtLeast(0)
    val lapses = tough.lapses.coerceAtLeast(0)
    val word = tough.word
    val phaseLabel = studyPhaseLabel(word.progress?.phase)
    val intensity = ((again + lapses).toFloat() / 8f).coerceIn(0f, 1f)
    return when {
        lapses >= 3 || again >= 5 -> ToughWordPrescription(
            kind = ToughWordPrescriptionKind.REBUILD,
            badgeLabel = "重建",
            title = "先拆开重建",
            message = "这个词已经多次回落，先暂停快刷：读词根、中文和例句，再只给 HARD/GOOD，别急着点会。",
            primaryLabel = "重来",
            primaryValue = again.toString(),
            secondaryLabel = "回落",
            secondaryValue = lapses.toString(),
            actionLabel = "慢速重建",
            intensity = intensity,
        )
        word.rootKey.isNotBlank() && (again >= 2 || word.progress?.phase == StudyPhase.LEARNING) ->
            ToughWordPrescription(
                kind = ToughWordPrescriptionKind.ROOT_TRACE,
                badgeLabel = "词根",
                title = "回到同根线索",
                message = "先把 ${word.rootKey} 和词义重新绑住，再对照同根词；这类错题通常不是不会背，是线索没接牢。",
                primaryLabel = "词根",
                primaryValue = word.rootKey,
                secondaryLabel = "阶段",
                secondaryValue = phaseLabel,
                actionLabel = "看词根",
                intensity = intensity,
            )
        word.example.isNotBlank() && again >= 2 -> ToughWordPrescription(
            kind = ToughWordPrescriptionKind.CONTEXT,
            badgeLabel = "语境",
            title = "放回例句里修复",
            message = "不要只盯中文释义，先读例句再遮住目标词；能在上下文里想起，才算真的可提取。",
            primaryLabel = "重来",
            primaryValue = again.toString(),
            secondaryLabel = "阶段",
            secondaryValue = phaseLabel,
            actionLabel = "读例句",
            intensity = intensity,
        )
        else -> ToughWordPrescription(
            kind = ToughWordPrescriptionKind.STABILIZE,
            badgeLabel = "巩固",
            title = "短频快扫",
            message = "这还是轻量错题，快速听发音、说中文、再评分；连续两次 GOOD 后它会自然退出错题压力区。",
            primaryLabel = "重来",
            primaryValue = again.toString(),
            secondaryLabel = "阶段",
            secondaryValue = phaseLabel,
            actionLabel = "快扫确认",
            intensity = intensity,
        )
    }
}

fun buildToughWordsBrief(toughWords: List<ToughWord>): ToughWordsBrief {
    if (toughWords.isEmpty()) {
        return ToughWordsBrief(
            title = "错题池很干净",
            message = "目前没有需要专攻的 AGAIN 记录，可以把时间放在新词或常规复习上。",
            dominantKind = ToughWordPrescriptionKind.STABILIZE,
            dominantLabel = "清空",
            totalCount = 0,
            highRiskCount = 0,
            peakAgainCount = 0,
            actionLabel = "保持节奏",
            intensity = 0f,
        )
    }
    val prescriptions = toughWords.map { buildToughWordPrescription(it) }
    val dominantKind = prescriptions
        .groupingBy { it.kind }
        .eachCount()
        .maxWithOrNull(
            compareBy<Map.Entry<ToughWordPrescriptionKind, Int>> { it.value }
                .thenBy { toughKindPriority(it.key) },
        )?.key ?: ToughWordPrescriptionKind.STABILIZE
    val highRiskCount = prescriptions.count { it.intensity >= 0.75f }
    val peakAgain = toughWords.maxOfOrNull { it.againCount.coerceAtLeast(0) } ?: 0
    val averageIntensity = prescriptions.map { it.intensity }.average().takeIf { !it.isNaN() }?.toFloat() ?: 0f
    val total = toughWords.size
    return when (dominantKind) {
        ToughWordPrescriptionKind.REBUILD -> ToughWordsBrief(
            title = "先处理高风险断点",
            message = "错题池里重建型最多，先慢速拆词、读例句，再评分；不要连续快点“会了”。",
            dominantKind = dominantKind,
            dominantLabel = "重建",
            totalCount = total,
            highRiskCount = highRiskCount,
            peakAgainCount = peakAgain,
            actionLabel = "先重建",
            intensity = maxOf(averageIntensity, highRiskCount.toFloat() / total.toFloat()).coerceIn(0f, 1f),
        )
        ToughWordPrescriptionKind.ROOT_TRACE -> ToughWordsBrief(
            title = "错因集中在词根线索",
            message = "多数难词需要回到 rootKey，同根对照比单词孤刷更有效。",
            dominantKind = dominantKind,
            dominantLabel = "词根",
            totalCount = total,
            highRiskCount = highRiskCount,
            peakAgainCount = peakAgain,
            actionLabel = "按词根修",
            intensity = averageIntensity.coerceIn(0f, 1f),
        )
        ToughWordPrescriptionKind.CONTEXT -> ToughWordsBrief(
            title = "先用语境修复提取",
            message = "错题更适合放回例句，先读句子再遮词回想，避免只记中文释义。",
            dominantKind = dominantKind,
            dominantLabel = "语境",
            totalCount = total,
            highRiskCount = highRiskCount,
            peakAgainCount = peakAgain,
            actionLabel = "读例句",
            intensity = averageIntensity.coerceIn(0f, 1f),
        )
        ToughWordPrescriptionKind.STABILIZE -> ToughWordsBrief(
            title = "错题压力可控",
            message = "当前多是轻量错题，短频快扫即可；连续 GOOD 后它们会自然退出压力区。",
            dominantKind = dominantKind,
            dominantLabel = "巩固",
            totalCount = total,
            highRiskCount = highRiskCount,
            peakAgainCount = peakAgain,
            actionLabel = "快扫一轮",
            intensity = averageIntensity.coerceIn(0f, 1f),
        )
    }
}

private fun toughKindPriority(kind: ToughWordPrescriptionKind): Int = when (kind) {
    ToughWordPrescriptionKind.REBUILD -> 4
    ToughWordPrescriptionKind.ROOT_TRACE -> 3
    ToughWordPrescriptionKind.CONTEXT -> 2
    ToughWordPrescriptionKind.STABILIZE -> 1
}

private fun studyPhaseLabel(phase: StudyPhase?): String = when (phase) {
    StudyPhase.NEW -> "新词"
    StudyPhase.LEARNING -> "学习中"
    StudyPhase.REVIEW -> "复习"
    StudyPhase.MASTERED -> "掌握"
    null -> "未建档"
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
