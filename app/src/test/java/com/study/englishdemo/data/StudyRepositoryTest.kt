package com.study.englishdemo.data

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.io.ByteArrayInputStream
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class StudyRepositoryTest {
    private val clock = Clock.fixed(Instant.parse("2026-05-09T10:00:00Z"), ZoneOffset.UTC)

    @Test
    fun recentReviewCounts_returnsSevenDaysIncludingZeros() = runBlocking {
        val wordBookDao = FakeWordBookDao()
        val wordEntryDao = FakeWordEntryDao()
        val progressDao = FakeWordProgressDao()
        val reviewLogDao = FakeReviewLogDao()
        reviewLogDao.seed(
            listOf(
                Instant.parse("2026-05-09T08:00:00Z"),
                Instant.parse("2026-05-09T09:00:00Z"),
                Instant.parse("2026-05-08T10:00:00Z"),
                Instant.parse("2026-05-06T22:00:00Z"),
            ),
        )

        val repo = StudyRepository(
            wordBookDao = wordBookDao,
            wordEntryDao = wordEntryDao,
            progressDao = progressDao,
            reviewLogDao = reviewLogDao,
            preferencesRepository = FakePreferences(),
            importer = FakeImporter(emptyPreview()),
            scheduler = SpacedRepetitionScheduler(clock),
            clock = clock,
        )

        val counts = repo.recentReviewCounts(7)

        assertThat(counts).hasSize(7)
        assertThat(counts.map { it.count }).containsExactly(0, 0, 0, 1, 0, 1, 2).inOrder()
    }

    @Test
    fun importBook_mergesDuplicateTermsWithoutDuplication() = runBlocking {
        val wordBookDao = FakeWordBookDao()
        val wordEntryDao = FakeWordEntryDao()
        val progressDao = FakeWordProgressDao()
        val reviewLogDao = FakeReviewLogDao()
        val preview = ImportPreview(
            title = "user_list",
            description = "",
            source = "imported",
            examTag = "cet4",
            words = listOf(
                ImportedWord("clarify", "", "", "澄清", "", emptyList()),
                ImportedWord("focus", "", "", "专注", "", emptyList()),
            ),
        )

        val repo = StudyRepository(
            wordBookDao = wordBookDao,
            wordEntryDao = wordEntryDao,
            progressDao = progressDao,
            reviewLogDao = reviewLogDao,
            preferencesRepository = FakePreferences(),
            importer = FakeImporter(preview),
            scheduler = SpacedRepetitionScheduler(clock),
            clock = clock,
        )

        val first = repo.importBook("user_list.csv", ByteArrayInputStream("".toByteArray()))
        val firstCount = wordEntryDao.countForBook(first.bookId)

        val second = repo.importBook("user_list.csv", ByteArrayInputStream("".toByteArray()))
        val totalCount = wordEntryDao.countForBook(second.bookId)

        assertThat(second.bookId).isEqualTo(first.bookId)
        assertThat(firstCount).isEqualTo(2)
        assertThat(totalCount).isEqualTo(2)
    }

    private fun emptyPreview() = ImportPreview(
        title = "",
        description = "",
        source = "imported",
        examTag = "cet4",
        words = emptyList(),
    )

    @Test
    fun getSemanticNeighbors_returnsSameRootSiblingsOrdered() = runBlocking {
        val wordBookDao = FakeWordBookDao()
        val wordEntryDao = FakeWordEntryDao()
        val progressDao = FakeWordProgressDao()
        val reviewLogDao = FakeReviewLogDao()
        val preview = ImportPreview(
            title = "mini",
            description = "",
            source = "imported",
            examTag = "cet4",
            words = listOf(
                ImportedWord(term = "state", phonetic = "", definition = "", translation = "陈述", example = "", tags = listOf("cet4"), rootKey = "stat", frq = 100),
                ImportedWord(term = "statement", phonetic = "", definition = "", translation = "声明", example = "", tags = listOf("cet4"), rootKey = "stat", frq = 500),
                ImportedWord(term = "include", phonetic = "", definition = "", translation = "包括", example = "", tags = listOf("cet4"), rootKey = "clud", frq = 200),
            ),
        )
        val repo = StudyRepository(
            wordBookDao = wordBookDao,
            wordEntryDao = wordEntryDao,
            progressDao = progressDao,
            reviewLogDao = reviewLogDao,
            preferencesRepository = FakePreferences(),
            importer = FakeImporter(preview),
            scheduler = SpacedRepetitionScheduler(clock),
            clock = clock,
        )
        val result = repo.importBook("mini.csv", ByteArrayInputStream("".toByteArray()))
        val state = wordEntryDao.getEntriesForBook(result.bookId).first { it.term == "state" }

        val siblings = repo.getSemanticNeighbors(state.id, limit = 5)

        assertThat(siblings).hasSize(1)
        assertThat(siblings.first().term).isEqualTo("statement")
    }
}

private class FakeWordBookDao : WordBookDao {
    private val items = mutableListOf<WordBookEntity>()
    private val flow = MutableStateFlow<List<WordBookEntity>>(emptyList())
    private var nextId = 1L

    override fun observeBooks(): Flow<List<WordBookEntity>> = flow
    override suspend fun insert(book: WordBookEntity): Long {
        val saved = book.copy(id = nextId++)
        items += saved
        flow.value = items.toList()
        return saved.id
    }
    override suspend fun deleteById(bookId: Long) {
        items.removeAll { it.id == bookId }
        flow.value = items.toList()
    }
    override suspend fun getById(bookId: Long) = items.firstOrNull { it.id == bookId }
    override suspend fun findByTitle(title: String) = items.firstOrNull { it.title == title }
    override suspend fun renameBook(bookId: Long, title: String, description: String) {
        val idx = items.indexOfFirst { it.id == bookId }
        if (idx >= 0) items[idx] = items[idx].copy(title = title, description = description)
        flow.value = items.toList()
    }
    override suspend fun updateTotalWords(bookId: Long, count: Int) {
        val idx = items.indexOfFirst { it.id == bookId }
        if (idx >= 0) items[idx] = items[idx].copy(totalWords = count)
        flow.value = items.toList()
    }
}

private class FakeWordEntryDao : WordEntryDao {
    private val items = mutableListOf<WordEntryEntity>()
    private var nextId = 1L

    override suspend fun insertAll(entries: List<WordEntryEntity>): List<Long> =
        entries.map { entry ->
            val saved = entry.copy(id = nextId++)
            items += saved
            saved.id
        }
    override suspend fun getEntriesForBook(bookId: Long) = items.filter { it.bookId == bookId }
    override suspend fun getTermsForBook(bookId: Long) = items.filter { it.bookId == bookId }.map { it.term }
    override suspend fun countForBook(bookId: Long) = items.count { it.bookId == bookId }
    override suspend fun getEntriesByIds(ids: List<Long>) = items.filter { it.id in ids }
    override suspend fun updateMnemonic(wordId: Long, text: String) {
        val idx = items.indexOfFirst { it.id == wordId }
        if (idx >= 0) items[idx] = items[idx].copy(mnemonic = text)
    }
    override suspend fun searchInBook(bookId: Long, keyword: String, limit: Int, offset: Int) =
        items.filter { it.bookId == bookId }
    override suspend fun getBookRootSiblings(bookId: Long, rootKey: String, excludeId: Long, limit: Int) =
        items.filter { it.bookId == bookId && it.rootKey == rootKey && it.id != excludeId }.take(limit)
}

private class FakeWordProgressDao : WordProgressDao {
    private val items = mutableListOf<WordProgressEntity>()
    private var nextId = 1L

    override suspend fun insert(progress: WordProgressEntity): Long {
        val saved = progress.copy(id = nextId++)
        items += saved
        return saved.id
    }
    override suspend fun update(progress: WordProgressEntity) {
        val idx = items.indexOfFirst { it.id == progress.id }
        if (idx >= 0) items[idx] = progress
    }
    override suspend fun getByWordId(wordId: Long) = items.firstOrNull { it.wordId == wordId }
    override suspend fun getDue(now: Instant, limit: Int) = emptyList<WordProgressEntity>()
    override suspend fun getDueForBook(bookId: Long, now: Instant, limit: Int) = emptyList<WordProgressEntity>()
    override suspend fun getFresh(limit: Int) = emptyList<WordProgressEntity>()
    override suspend fun getFreshForBook(bookId: Long, limit: Int) = emptyList<WordProgressEntity>()
    override suspend fun countDue(now: Instant) = 0
    override suspend fun countDueForBook(bookId: Long, now: Instant) = 0
    override suspend fun countNew() = items.count { it.phase == StudyPhase.NEW }
    override suspend fun countNewForBook(bookId: Long) = items.count { it.phase == StudyPhase.NEW }
}

private class FakeReviewLogDao : ReviewLogDao {
    private val times = mutableListOf<Instant>()
    fun seed(values: List<Instant>) { times.addAll(values) }
    override suspend fun insert(log: ReviewLogEntity) { times.add(log.reviewedAt) }
    override suspend fun countReviewedBetween(start: Instant, end: Instant) =
        times.count { !it.isBefore(start) && it.isBefore(end) }
    override suspend fun getAllReviewTimesDesc() = times.sortedDescending()
    override suspend fun getReviewTimesSince(since: Instant) =
        times.filter { !it.isBefore(since) }.sorted()
}

private class FakePreferences : UserPreferencesProvider {
    private val state = MutableStateFlow(SettingsState())
    override val settings: Flow<SettingsState> = state.map { it }
    override suspend fun updateDailyTarget(value: Int) { state.value = state.value.copy(dailyNewWordTarget = value) }
    override suspend fun updateReminderSettings(enabled: Boolean, hour: Int, minute: Int) {
        state.value = state.value.copy(
            reviewReminderEnabled = enabled,
            reminderHour = hour,
            reminderMinute = minute,
        )
    }
}

private class FakeImporter(private val preview: ImportPreview) : BookImporter {
    override suspend fun importBundledBook(assetPath: String): ImportPreview = preview
    override suspend fun import(stream: java.io.InputStream, filename: String, fallbackTitle: String): ImportPreview = preview
}

