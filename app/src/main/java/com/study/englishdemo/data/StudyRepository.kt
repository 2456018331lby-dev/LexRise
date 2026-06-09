package com.study.englishdemo.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

internal fun WordEntryEntity.toDomain(progress: WordProgress?): WordEntry = WordEntry(
    id = id,
    term = term,
    phonetic = phonetic,
    definition = definition,
    translation = translation,
    example = example,
    tags = tags.split("|").filter { it.isNotBlank() },
    rootKey = rootKey,
    derivatives = derivatives.split("|").filter { it.isNotBlank() },
    synonyms = synonyms.split("|").filter { it.isNotBlank() },
    antonyms = antonyms.split("|").filter { it.isNotBlank() },
    mnemonic = mnemonic,
    pos = pos,
    frq = frq,
    progress = progress,
)

class StudyRepository(
    private val wordBookDao: WordBookDao,
    private val wordEntryDao: WordEntryDao,
    private val progressDao: WordProgressDao,
    private val reviewLogDao: ReviewLogDao,
    private val preferencesRepository: UserPreferencesProvider,
    private val importer: BookImporter,
    private val scheduler: SpacedRepetitionScheduler = SpacedRepetitionScheduler(),
    private val clock: Clock = Clock.systemDefaultZone(),
) {
    private val importMutex = Mutex()

    fun observeBooks(): Flow<List<WordBook>> = wordBookDao.observeBooks().map { list ->
        list.map {
            WordBook(
                id = it.id,
                title = it.title,
                description = it.description,
                source = it.source,
                examTag = it.examTag,
                totalWords = it.totalWords,
            )
        }
    }

    suspend fun ensureBundledBookImported(): Long = importMutex.withLock {
        val cet4Id = wordBookDao.findByTitle("四级高频 · 词根序")?.id
            ?: run {
                val preview = importer.importBundledBook("books/cet4_core.csv").copy(
                    title = "四级高频 · 词根序",
                    description = "CET4 大纲约 3800 词，按词根聚簇 + 频率排序，同根词连着记。",
                    source = "builtin",
                    examTag = "cet4",
                )
                importPreviewUnlocked(preview)
            }
        if (wordBookDao.findByTitle("六级进阶 · 词根序") == null) {
            val preview = importer.importBundledBook("books/cet6_core.csv").copy(
                title = "六级进阶 · 词根序",
                description = "CET6 大纲约 5400 词，包含学术用词，词根优先。",
                source = "builtin",
                examTag = "cet6",
            )
            importPreviewUnlocked(preview)
        }
        if (wordBookDao.findByTitle("考研英语 · 大纲词") == null) {
            val preview = importer.importBundledBook("books/ky_core.csv").copy(
                title = "考研英语 · 大纲词",
                description = "考研英语一大纲约 4800 词，按词根聚簇 + 频率排序。",
                source = "builtin",
                examTag = "ky",
            )
            importPreviewUnlocked(preview)
        }
        cet4Id
    }

    suspend fun importPreview(preview: ImportPreview): Long = importMutex.withLock {
        importPreviewUnlocked(preview)
    }

    private suspend fun importPreviewUnlocked(preview: ImportPreview): Long {
        val bookId = wordBookDao.insert(
            WordBookEntity(
                title = preview.title,
                description = preview.description,
                source = preview.source,
                examTag = preview.examTag,
                totalWords = preview.words.size,
                createdAt = clock.instant(),
            ),
        )

        val entryIds = wordEntryDao.insertAll(
            preview.words.mapIndexed { idx, it ->
                WordEntryEntity(
                    bookId = bookId,
                    term = it.term,
                    phonetic = it.phonetic,
                    definition = it.definition,
                    translation = it.translation,
                    example = it.example,
                    tags = it.tags.joinToString("|"),
                    rootKey = it.rootKey,
                    derivatives = it.derivatives.joinToString("|"),
                    synonyms = it.synonyms.joinToString("|"),
                    antonyms = it.antonyms.joinToString("|"),
                    mnemonic = it.mnemonic,
                    frq = it.frq,
                    pos = it.pos,
                    positionInBook = idx,
                )
            },
        )

        if (entryIds.isNotEmpty()) {
            progressDao.insertAll(entryIds.map { wordId -> scheduler.initialProgress(wordId) })
        }

        return bookId
    }

    suspend fun importBook(filename: String, stream: java.io.InputStream): ImportedBookResult {
        val preview = importer.import(stream, filename, filename.substringBeforeLast("."))
        val bookId = importMutex.withLock {
            val existing = wordBookDao.findByTitle(preview.title)
            if (existing != null && existing.source != "builtin") {
                mergeIntoBook(existing, preview)
            } else {
                importPreviewUnlocked(preview)
            }
        }
        return ImportedBookResult(bookId = bookId, preview = preview)
    }

    private suspend fun mergeIntoBook(existing: WordBookEntity, preview: ImportPreview): Long {
        val existingTerms = wordEntryDao.getTermsForBook(existing.id).map { it.lowercase() }.toHashSet()
        val newWords = preview.words.filter { it.term.lowercase() !in existingTerms }
        if (newWords.isEmpty()) return existing.id

        val baseIdx = wordEntryDao.countForBook(existing.id)
        val entryIds = wordEntryDao.insertAll(
            newWords.mapIndexed { idx, it ->
                WordEntryEntity(
                    bookId = existing.id,
                    term = it.term,
                    phonetic = it.phonetic,
                    definition = it.definition,
                    translation = it.translation,
                    example = it.example,
                    tags = it.tags.joinToString("|"),
                    rootKey = it.rootKey,
                    derivatives = it.derivatives.joinToString("|"),
                    synonyms = it.synonyms.joinToString("|"),
                    antonyms = it.antonyms.joinToString("|"),
                    mnemonic = it.mnemonic,
                    frq = it.frq,
                    pos = it.pos,
                    positionInBook = baseIdx + idx,
                )
            },
        )
        if (entryIds.isNotEmpty()) {
            progressDao.insertAll(entryIds.map { wordId -> scheduler.initialProgress(wordId) })
        }
        wordEntryDao.countForBook(existing.id).let { total ->
            wordBookDao.updateTotalWords(existing.id, total)
        }
        return existing.id
    }

    suspend fun renameBook(bookId: Long, newTitle: String, newDescription: String? = null) {
        val book = wordBookDao.getById(bookId) ?: return
        require(book.source != "builtin") { "内置词书不可重命名" }
        val title = newTitle.trim()
        require(title.isNotEmpty()) { "词书名称不能为空" }
        val desc = newDescription?.trim()?.ifEmpty { book.description } ?: book.description
        wordBookDao.renameBook(bookId, title, desc)
    }

    suspend fun updateMnemonic(wordId: Long, text: String) {
        wordEntryDao.updateMnemonic(wordId, text.trim())
    }

    suspend fun getLearningSession(bookId: Long): LearningSession {
        val settings = preferencesRepository.settings.first()
        val now = clock.instant()
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(clock)
        val start = today.atStartOfDay(zone).toInstant()
        val end = today.plusDays(1).atStartOfDay(zone).toInstant()
        val studiedToday = reviewLogDao.countReviewedBetween(start, end)
        val reviewDueCount = progressDao.countDueForBook(bookId, now)
        val newCount = progressDao.countNewForBook(bookId)
        val paceTarget = computePaceRecommendation(
            remainingWords = newCount,
            examDate = settings.examDate,
            today = today,
            baseline = settings.dailyNewWordTarget,
            autoEnabled = settings.autoPaceEnabled,
        ).target
        val overview = DailyOverview(
            today = today,
            newWordTarget = paceTarget,
            newWordsRemaining = newCount.coerceAtMost(paceTarget),
            reviewDueCount = reviewDueCount,
            streakDays = computeStreakDays(),
            studiedToday = studiedToday,
            completionRatio = if (paceTarget + reviewDueCount == 0) {
                0f
            } else {
                studiedToday.toFloat() / (paceTarget + reviewDueCount).toFloat()
            }.coerceIn(0f, 1f),
        )

        val dueWords = hydrate(progressDao.getDueForBook(bookId, now, 12))
        val newWords = hydrate(
            progressDao.getFreshForBook(bookId, paceTarget.coerceAtMost(12)),
        )

        return LearningSession(
            overview = overview,
            dueReviewWords = dueWords,
            recommendedNewWords = newWords,
        )
    }

    suspend fun getPaceRecommendation(bookId: Long): PaceRecommendation {
        val settings = preferencesRepository.settings.first()
        val remaining = progressDao.countNewForBook(bookId)
        return computePaceRecommendation(
            remainingWords = remaining,
            examDate = settings.examDate,
            today = LocalDate.now(clock),
            baseline = settings.dailyNewWordTarget,
            autoEnabled = settings.autoPaceEnabled,
        )
    }

    suspend fun updateExamPlan(examDate: LocalDate?, autoPaceEnabled: Boolean) {
        preferencesRepository.updateExamPlan(examDate, autoPaceEnabled)
    }

    suspend fun reviewWord(wordId: Long, rating: ReviewRating) {
        val current = progressDao.getByWordId(wordId) ?: return
        val updated = scheduler.review(current, rating)
        progressDao.update(updated)
        reviewLogDao.insert(
            ReviewLogEntity(
                wordId = wordId,
                rating = rating,
                reviewedAt = clock.instant(),
                nextReviewAt = updated.nextReviewAt,
            ),
        )
    }

    suspend fun getSettings(): SettingsState = preferencesRepository.settings.first()

    suspend fun updateDailyTarget(value: Int) {
        preferencesRepository.updateDailyTarget(value)
    }

    suspend fun updateReminderSettings(enabled: Boolean, hour: Int, minute: Int) {
        preferencesRepository.updateReminderSettings(enabled, hour, minute)
    }

    suspend fun getDueCount(): Int = progressDao.countDue(clock.instant())

    suspend fun searchWords(bookId: Long, keyword: String, limit: Int = 200): List<WordEntry> {
        val trimmed = keyword.trim()
        val directEntries = wordEntryDao.searchInBook(bookId, trimmed, limit, 0)
        val entries = if (trimmed.length >= 4 && directEntries.size < limit) {
            val directIds = directEntries.map { it.id }.toSet()
            val fuzzyEntries = wordEntryDao.getEntriesForBook(bookId)
                .asSequence()
                .filter { it.id !in directIds }
                .mapNotNull { entry ->
                    fuzzyWordFormMatchDistance(
                        query = trimmed,
                        term = entry.term,
                        variants = entry.derivatives.split("|").filter { it.isNotBlank() },
                    )?.let { distance -> entry to distance }
                }
                .sortedWith(
                    compareBy<Pair<WordEntryEntity, Double>> { it.second }
                        .thenBy { it.first.frq.takeIf { value -> value > 0 } ?: Int.MAX_VALUE }
                        .thenBy { it.first.term.lowercase() },
                )
                .take(limit - directEntries.size)
                .map { it.first }
                .toList()
            directEntries + fuzzyEntries
        } else {
            directEntries
        }
        if (entries.isEmpty()) return emptyList()
        return entries.map { entry ->
            val p = progressDao.getByWordId(entry.id)
            val progress = p?.let {
                WordProgress(
                    phase = it.phase,
                    familiarity = it.familiarity,
                    streak = it.streak,
                    lapses = it.lapses,
                    easeFactor = it.easeFactor,
                    intervalDays = it.intervalDays,
                    lastReviewedAt = it.lastReviewedAt,
                    nextReviewAt = it.nextReviewAt,
                )
            }
            entry.toDomain(progress)
        }
    }

    suspend fun getSemanticNeighbors(wordId: Long, limit: Int = 6): List<WordEntry> {
        val entry = wordEntryDao.getEntriesByIds(listOf(wordId)).firstOrNull() ?: return emptyList()
        if (entry.rootKey.isBlank()) return emptyList()
        val siblings = wordEntryDao.getBookRootSiblings(entry.bookId, entry.rootKey, entry.id, limit)
        return siblings.map { sibling ->
            val progress = progressDao.getByWordId(sibling.id)?.let {
                WordProgress(
                    phase = it.phase,
                    familiarity = it.familiarity,
                    streak = it.streak,
                    lapses = it.lapses,
                    easeFactor = it.easeFactor,
                    intervalDays = it.intervalDays,
                    lastReviewedAt = it.lastReviewedAt,
                    nextReviewAt = it.nextReviewAt,
                )
            }
            sibling.toDomain(progress)
        }
    }

    suspend fun getRootGroups(
        bookId: Long,
        loader: suspend (String) -> RootReference?,
        query: String = "",
        limit: Int = 200,
    ): List<RootGroup> {
        val clustered = wordEntryDao.getClusteredEntries(bookId)
        if (clustered.isEmpty()) return emptyList()
        val byRoot = clustered.groupBy { it.rootKey }
        val progressMap = progressDao
            .getByWordIds(clustered.map { it.id })
            .associateBy { it.wordId }
        val normalizedQuery = query.trim().lowercase()
        val result = mutableListOf<RootGroup>()
        for ((rootKey, entries) in byRoot) {
            val ref = loader(rootKey)
            if (normalizedQuery.isNotEmpty()) {
                val hit = rootKey.lowercase().contains(normalizedQuery) ||
                    (ref?.meanings?.any { it.lowercase().contains(normalizedQuery) } == true) ||
                    entries.any { it.term.lowercase().contains(normalizedQuery) }
                if (!hit) continue
            }
            val members = entries.map { entry ->
                val p = progressMap[entry.id]?.let {
                    WordProgress(
                        phase = it.phase,
                        familiarity = it.familiarity,
                        streak = it.streak,
                        lapses = it.lapses,
                        easeFactor = it.easeFactor,
                        intervalDays = it.intervalDays,
                        lastReviewedAt = it.lastReviewedAt,
                        nextReviewAt = it.nextReviewAt,
                    )
                }
                entry.toDomain(p)
            }
            val learned = members.count {
                val phase = it.progress?.phase
                phase != null && phase != StudyPhase.NEW
            }
            result += RootGroup(
                rootKey = rootKey,
                meanings = ref?.meanings.orEmpty(),
                totalWords = members.size,
                learnedWords = learned,
                members = members,
            )
        }
        return result
            .sortedWith(
                compareByDescending<RootGroup> { it.totalWords }
                    .thenBy { it.rootKey },
            )
            .take(limit)
    }

    suspend fun getBookRootSnapshot(bookId: Long): BookRootSnapshot {
        val clustered = wordEntryDao.getClusteredEntries(bookId)
        if (clustered.isEmpty()) {
            return BookRootSnapshot(0, 0, 0, 0)
        }
        val progressMap = progressDao
            .getByWordIds(clustered.map { it.id })
            .associateBy { it.wordId }
        val rootsAll = clustered.map { it.rootKey }.toSet()
        val rootsTouched = clustered
            .filter { (progressMap[it.id]?.phase ?: StudyPhase.NEW) != StudyPhase.NEW }
            .map { it.rootKey }
            .toSet()
        val learnedClustered = clustered.count {
            (progressMap[it.id]?.phase ?: StudyPhase.NEW) != StudyPhase.NEW
        }
        return BookRootSnapshot(
            totalRoots = rootsAll.size,
            touchedRoots = rootsTouched.size,
            totalClustered = clustered.size,
            learnedClustered = learnedClustered,
        )
    }

    /**
     * Returns the set of word ids that are the "anchor" (lowest-frq clustered
     * member) inside their rootKey bucket for the given book. These are
     * surfaced in UI as 地基词 badges.
     */
    suspend fun getAnchorWordIds(bookId: Long): Set<Long> {
        val clustered = wordEntryDao.getClusteredEntries(bookId)
        if (clustered.isEmpty()) return emptySet()
        return clustered
            .groupBy { it.rootKey }
            .values
            .mapNotNull { group ->
                group.minByOrNull { it.frq.takeIf { v -> v > 0 } ?: Int.MAX_VALUE }?.id
            }
            .toSet()
    }

    suspend fun buildQuizQuestion(wordId: Long, bookId: Long): QuizQuestion? {
        val entry = wordEntryDao.getEntriesByIds(listOf(wordId)).firstOrNull() ?: return null
        val pool = wordEntryDao.getDistractorPool(
            bookId = bookId,
            excludeId = entry.id,
            rootKey = entry.rootKey,
            limit = 24,
        ).map { it.toDomain(null) }
        if (pool.size < 3) return null
        val options = buildQuizOptions(
            correctTerm = entry.term,
            correctRootKey = entry.rootKey,
            pool = pool,
        )
        val progress = progressDao.getByWordId(entry.id)?.let {
            WordProgress(
                phase = it.phase,
                familiarity = it.familiarity,
                streak = it.streak,
                lapses = it.lapses,
                easeFactor = it.easeFactor,
                intervalDays = it.intervalDays,
                lastReviewedAt = it.lastReviewedAt,
                nextReviewAt = it.nextReviewAt,
            )
        }
        return QuizQuestion(
            word = entry.toDomain(progress),
            options = options,
            correct = entry.term,
        )
    }

    suspend fun buildClozeQuestion(wordId: Long, bookId: Long): ClozeQuestion? {
        val entry = wordEntryDao.getEntriesByIds(listOf(wordId)).firstOrNull() ?: return null
        val blank = buildClozeBlank(
            example = entry.example,
            term = entry.term,
            variants = entry.derivatives.split("|"),
        ) ?: return null
        val pool = wordEntryDao.getDistractorPool(
            bookId = bookId,
            excludeId = entry.id,
            rootKey = entry.rootKey,
            limit = 24,
        ).map { it.toDomain(null) }
        if (pool.size < 3) return null
        val options = buildQuizOptions(
            correctTerm = blank.answer,
            correctRootKey = entry.rootKey,
            pool = pool,
        )
        val progress = progressDao.getByWordId(entry.id)?.let {
            WordProgress(
                phase = it.phase,
                familiarity = it.familiarity,
                streak = it.streak,
                lapses = it.lapses,
                easeFactor = it.easeFactor,
                intervalDays = it.intervalDays,
                lastReviewedAt = it.lastReviewedAt,
                nextReviewAt = it.nextReviewAt,
            )
        }
        return ClozeQuestion(
            word = entry.toDomain(progress),
            prompt = blank.prompt,
            options = options,
            correct = blank.answer,
        )
    }

    suspend fun getToughWords(bookId: Long, limit: Int = 30): List<ToughWord> {
        val rows = reviewLogDao.getToughWordsForBook(bookId, limit)
        if (rows.isEmpty()) return emptyList()
        val entries = wordEntryDao
            .getEntriesByIds(rows.map { it.wordId })
            .associateBy { it.id }
        val progresses = progressDao
            .getByWordIds(rows.map { it.wordId })
            .associateBy { it.wordId }
        return rows.mapNotNull { row ->
            val entry = entries[row.wordId] ?: return@mapNotNull null
            val p = progresses[row.wordId]
            val progress = p?.let {
                WordProgress(
                    phase = it.phase,
                    familiarity = it.familiarity,
                    streak = it.streak,
                    lapses = it.lapses,
                    easeFactor = it.easeFactor,
                    intervalDays = it.intervalDays,
                    lastReviewedAt = it.lastReviewedAt,
                    nextReviewAt = it.nextReviewAt,
                )
            }
            ToughWord(
                word = entry.toDomain(progress),
                againCount = row.againCount,
                lapses = p?.lapses ?: 0,
                lastReviewedAt = row.lastReviewedAt,
            )
        }
    }

    suspend fun deleteBook(bookId: Long) {
        val book = wordBookDao.getById(bookId) ?: return
        require(book.source != "builtin") { "内置词书不可删除" }
        wordBookDao.deleteById(bookId)
    }

    suspend fun recentReviewCounts(days: Int): List<DailyReviewCount> {
        val zone = clock.zone
        val today = LocalDate.now(clock)
        val start = today.minusDays((days - 1).toLong()).atStartOfDay(zone).toInstant()
        val logs = reviewLogDao.getReviewTimesSince(start)
        val buckets = (0 until days).associate { offset ->
            today.minusDays((days - 1 - offset).toLong()) to 0
        }.toMutableMap()
        logs.forEach { instant ->
            val day = instant.atZone(zone).toLocalDate()
            buckets[day]?.let { buckets[day] = it + 1 }
        }
        return buckets.entries.sortedBy { it.key }.map { DailyReviewCount(it.key, it.value) }
    }

    private suspend fun hydrate(progressList: List<WordProgressEntity>): List<WordEntry> {
        if (progressList.isEmpty()) return emptyList()
        val progressByWordId = progressList.associateBy { it.wordId }
        val entries = wordEntryDao.getEntriesByIds(progressByWordId.keys.toList())
        return entries.map { entry ->
            val progress = requireNotNull(progressByWordId[entry.id])
            entry.toDomain(
                WordProgress(
                    phase = progress.phase,
                    familiarity = progress.familiarity,
                    streak = progress.streak,
                    lapses = progress.lapses,
                    easeFactor = progress.easeFactor,
                    intervalDays = progress.intervalDays,
                    lastReviewedAt = progress.lastReviewedAt,
                    nextReviewAt = progress.nextReviewAt,
                ),
            )
        }
    }

    private suspend fun computeStreakDays(): Int {
        val times = reviewLogDao.getAllReviewTimesDesc()
        if (times.isEmpty()) return 0
        val zone = ZoneId.systemDefault()
        val distinctDays = times.map { it.atZone(zone).toLocalDate() }.distinct()
        val today = LocalDate.now(clock)
        val mostRecent = distinctDays.first()
        var cursor = when (mostRecent) {
            today -> today
            today.minusDays(1) -> today.minusDays(1)
            else -> return 0
        }
        var streak = 0
        for (day in distinctDays) {
            if (day == cursor) {
                streak += 1
                cursor = cursor.minusDays(1)
            } else if (day.isBefore(cursor)) {
                break
            }
        }
        return streak
    }
}
