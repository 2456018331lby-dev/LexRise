package com.study.englishdemo.data

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
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
    fun searchWords_addsFuzzyTermMatchesForTypos() = runBlocking {
        val wordBookDao = FakeWordBookDao()
        val wordEntryDao = FakeWordEntryDao()
        val progressDao = FakeWordProgressDao()
        val reviewLogDao = FakeReviewLogDao()
        val preview = ImportPreview(
            title = "search",
            description = "",
            source = "imported",
            examTag = "cet4",
            words = listOf(
                ImportedWord(term = "clarify", phonetic = "", definition = "make clear", translation = "澄清", example = "", tags = listOf("cet4"), frq = 100),
                ImportedWord(term = "classic", phonetic = "", definition = "typical", translation = "经典", example = "", tags = listOf("cet4"), frq = 200),
                ImportedWord(term = "focus", phonetic = "", definition = "concentrate", translation = "专注", example = "", tags = listOf("cet4"), frq = 50),
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
        val book = repo.importBook("search.csv", ByteArrayInputStream("".toByteArray()))

        val results = repo.searchWords(book.bookId, "clarfy", limit = 10)

        assertThat(results.map { it.term }).contains("clarify")
        assertThat(results.first().term).isEqualTo("clarify")
    }

    @Test
    fun searchWords_matchesDerivativeForms() = runBlocking {
        val repo = repoForDerivativeSearch()
        val book = repo.importBook("derivative-search.csv", ByteArrayInputStream("".toByteArray()))

        val results = repo.searchWords(book.bookId, "clarified", limit = 10)

        assertThat(results.map { it.term }).contains("clarify")
        assertThat(results.first { it.term == "clarify" }.derivatives).contains("clarified")
    }

    @Test
    fun searchWords_addsFuzzyDerivativeMatchesForTypos() = runBlocking {
        val repo = repoForDerivativeSearch()
        val book = repo.importBook("derivative-search.csv", ByteArrayInputStream("".toByteArray()))

        val results = repo.searchWords(book.bookId, "clarifed", limit = 10)

        assertThat(results.map { it.term }).contains("clarify")
        assertThat(results.first().term).isEqualTo("clarify")
    }

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

    @Test
    fun getRootGroups_groupsBookEntriesByRootAndCountsLearned() = runBlocking {
        val wordBookDao = FakeWordBookDao()
        val wordEntryDao = FakeWordEntryDao()
        val progressDao = FakeWordProgressDao()
        val reviewLogDao = FakeReviewLogDao()
        val preview = ImportPreview(
            title = "roots_demo",
            description = "",
            source = "imported",
            examTag = "cet4",
            words = listOf(
                ImportedWord(term = "state", phonetic = "", definition = "", translation = "陈述", example = "", tags = listOf("cet4"), rootKey = "stat", frq = 100),
                ImportedWord(term = "statement", phonetic = "", definition = "", translation = "声明", example = "", tags = listOf("cet4"), rootKey = "stat", frq = 500),
                ImportedWord(term = "include", phonetic = "", definition = "", translation = "包括", example = "", tags = listOf("cet4"), rootKey = "clud", frq = 200),
                ImportedWord(term = "floor", phonetic = "", definition = "", translation = "地板", example = "", tags = listOf("cet4"), rootKey = "", frq = 50),
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
        val book = repo.importBook("roots_demo.csv", ByteArrayInputStream("".toByteArray()))
        val state = wordEntryDao.getEntriesForBook(book.bookId).first { it.term == "state" }
        // mark 'state' as learned
        progressDao.mutate(state.id) { it.copy(phase = StudyPhase.LEARNING) }

        val groups = repo.getRootGroups(book.bookId, loader = {
            if (it == "stat") RootReference("stat", listOf("stand, stay"), listOf("status")) else null
        })

        val stat = groups.first { it.rootKey == "stat" }
        assertThat(stat.totalWords).isEqualTo(2)
        assertThat(stat.learnedWords).isEqualTo(1)
        assertThat(stat.meanings).containsExactly("stand, stay")
        assertThat(groups.map { it.rootKey }).containsExactly("stat", "clud").inOrder()
    }

    @Test
    fun getAnchorWordIds_picksLowestFrqPerRoot() = runBlocking {
        val wordBookDao = FakeWordBookDao()
        val wordEntryDao = FakeWordEntryDao()
        val progressDao = FakeWordProgressDao()
        val reviewLogDao = FakeReviewLogDao()
        val preview = ImportPreview(
            title = "anchors",
            description = "",
            source = "imported",
            examTag = "cet4",
            words = listOf(
                ImportedWord(term = "state", phonetic = "", definition = "", translation = "陈述", example = "", tags = listOf("cet4"), rootKey = "stat", frq = 100),
                ImportedWord(term = "statement", phonetic = "", definition = "", translation = "声明", example = "", tags = listOf("cet4"), rootKey = "stat", frq = 500),
                ImportedWord(term = "include", phonetic = "", definition = "", translation = "包括", example = "", tags = listOf("cet4"), rootKey = "clud", frq = 200),
                ImportedWord(term = "exclude", phonetic = "", definition = "", translation = "排除", example = "", tags = listOf("cet4"), rootKey = "clud", frq = 700),
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
        val book = repo.importBook("anchors.csv", ByteArrayInputStream("".toByteArray()))
        val state = wordEntryDao.getEntriesForBook(book.bookId).first { it.term == "state" }
        val include = wordEntryDao.getEntriesForBook(book.bookId).first { it.term == "include" }

        val anchors = repo.getAnchorWordIds(book.bookId)

        assertThat(anchors).isEqualTo(setOf(state.id, include.id))
    }

    @Test
    fun getPaceRecommendation_honorsAutoPaceWithBuffer() = runBlocking {
        val wordBookDao = FakeWordBookDao()
        val wordEntryDao = FakeWordEntryDao()
        val progressDao = FakeWordProgressDao()
        val reviewLogDao = FakeReviewLogDao()
        val preferences = FakePreferences()
        val preview = ImportPreview(
            title = "pace",
            description = "",
            source = "imported",
            examTag = "cet4",
            words = (1..100).map { i ->
                ImportedWord(term = "w$i", phonetic = "", definition = "", translation = "t$i", example = "", tags = emptyList())
            },
        )
        val repo = StudyRepository(
            wordBookDao = wordBookDao,
            wordEntryDao = wordEntryDao,
            progressDao = progressDao,
            reviewLogDao = reviewLogDao,
            preferencesRepository = preferences,
            importer = FakeImporter(preview),
            scheduler = SpacedRepetitionScheduler(clock),
            clock = clock,
        )
        val book = repo.importBook("pace.csv", ByteArrayInputStream("".toByteArray()))
        // 100 new words, 20 days to exam (2026-05-29 from 2026-05-09) -> 5/day raw, 6/day with buffer
        preferences.updateExamPlan(java.time.LocalDate.parse("2026-05-29"), autoPaceEnabled = true)

        val pace = repo.getPaceRecommendation(book.bookId)

        assertThat(pace.isAuto).isTrue()
        assertThat(pace.remainingDays).isEqualTo(20)
        assertThat(pace.target).isEqualTo(6)
    }

    @Test
    fun buildQuizQuestion_includesCorrectTermAndFourOptions() = runBlocking {
        val repo = miniRepoForQuiz()
        val book = repo.importBook("quiz.csv", ByteArrayInputStream("".toByteArray()))
        val state = (repo as StudyRepository).let {
            // expose via DAO through repo-specific helpers: search to find state
            it.searchWords(book.bookId, "state").first()
        }

        val question = repo.buildQuizQuestion(state.id, book.bookId)

        assertThat(question).isNotNull()
        assertThat(question!!.options).hasSize(4)
        assertThat(question.correct).isEqualTo("state")
        assertThat(question.options).contains("state")
        assertThat(question.options.toSet()).hasSize(4)
    }

    @Test
    fun buildClozeQuestion_blanksExampleAndIncludesCorrectTerm() = runBlocking {
        val repo = miniRepoForQuiz()
        val book = repo.importBook("cloze.csv", ByteArrayInputStream("".toByteArray()))
        val state = repo.searchWords(book.bookId, "state").first { it.term == "state" }

        val question = repo.buildClozeQuestion(state.id, book.bookId)

        assertThat(question).isNotNull()
        assertThat(question!!.prompt).isEqualTo("The ____ of the project is stable.")
        assertThat(question.correct).isEqualTo("state")
        assertThat(question.options).contains("state")
        assertThat(question.options).hasSize(4)
    }

    @Test
    fun buildClozeQuestion_usesDerivativeWhenExampleDoesNotContainLemma() = runBlocking {
        val repo = miniRepoForDerivativeCloze()
        val book = repo.importBook("derivative-cloze.csv", ByteArrayInputStream("".toByteArray()))
        val clarify = repo.searchWords(book.bookId, "clarify").first { it.term == "clarify" }

        val question = repo.buildClozeQuestion(clarify.id, book.bookId)

        assertThat(question).isNotNull()
        assertThat(question!!.prompt).isEqualTo("The note ____ the final goal.")
        assertThat(question.correct).isEqualTo("clarified")
        assertThat(question.options).contains("clarified")
        assertThat(question.word.term).isEqualTo("clarify")
    }

    @Test
    fun buildClozeQuestion_usesBestSentenceFromMultiSentenceExample() = runBlocking {
        val repo = miniRepoForMultiSentenceCloze()
        val book = repo.importBook("multi-cloze.csv", ByteArrayInputStream("".toByteArray()))
        val clarify = repo.searchWords(book.bookId, "clarify").first { it.term == "clarify" }

        val question = repo.buildClozeQuestion(clarify.id, book.bookId)

        assertThat(question).isNotNull()
        assertThat(question!!.prompt).isEqualTo("The team should ____ the final goal before launch.")
        assertThat(question.correct).isEqualTo("clarify")
        assertThat(question.options).contains("clarify")
    }

    @Test
    fun ensureBundledBookImported_isIdempotent() = runBlocking {
        val wordBookDao = FakeWordBookDao()
        val wordEntryDao = FakeWordEntryDao()
        val progressDao = FakeWordProgressDao()
        val reviewLogDao = FakeReviewLogDao()
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

        val firstId = repo.ensureBundledBookImported()
        val secondId = repo.ensureBundledBookImported()
        val books = wordBookDao.observeBooks().first()

        assertThat(secondId).isEqualTo(firstId)
        assertThat(books.map { it.title }).containsExactly(
            "四级高频 · 词根序",
            "六级进阶 · 词根序",
            "考研英语 · 大纲词",
        )
        Unit
    }

    @Test
    fun getToughWords_ordersByAgainCountDesc() = runBlocking {
        val wordBookDao = FakeWordBookDao()
        val wordEntryDao = FakeWordEntryDao()
        val progressDao = FakeWordProgressDao()
        val reviewLogDao = FakeReviewLogDao()
        val preview = ImportPreview(
            title = "tough",
            description = "",
            source = "imported",
            examTag = "cet4",
            words = listOf(
                ImportedWord(term = "alpha", phonetic = "", definition = "", translation = "一", example = "", tags = listOf("cet4")),
                ImportedWord(term = "bravo", phonetic = "", definition = "", translation = "二", example = "", tags = listOf("cet4")),
                ImportedWord(term = "charlie", phonetic = "", definition = "", translation = "三", example = "", tags = listOf("cet4")),
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
        val book = repo.importBook("tough.csv", ByteArrayInputStream("".toByteArray()))
        val entries = wordEntryDao.getEntriesForBook(book.bookId)
        val alpha = entries.first { it.term == "alpha" }
        val bravo = entries.first { it.term == "bravo" }
        // bravo: 3 AGAIN; alpha: 1 AGAIN; charlie: none
        repo.reviewWord(bravo.id, ReviewRating.AGAIN)
        repo.reviewWord(bravo.id, ReviewRating.AGAIN)
        repo.reviewWord(bravo.id, ReviewRating.AGAIN)
        repo.reviewWord(alpha.id, ReviewRating.AGAIN)

        val tough = repo.getToughWords(book.bookId)

        assertThat(tough.map { it.word.term }).containsExactly("bravo", "alpha").inOrder()
        assertThat(tough.first().againCount).isEqualTo(3)
    }

    private fun miniRepoForQuiz(): StudyRepository {
        val wordBookDao = FakeWordBookDao()
        val wordEntryDao = FakeWordEntryDao()
        val progressDao = FakeWordProgressDao()
        val reviewLogDao = FakeReviewLogDao()
        val preview = ImportPreview(
            title = "quiz",
            description = "",
            source = "imported",
            examTag = "cet4",
            words = listOf(
                ImportedWord(term = "state", phonetic = "", definition = "condition", translation = "状态", example = "The state of the project is stable.", tags = listOf("cet4"), rootKey = "stat"),
                ImportedWord(term = "statement", phonetic = "", definition = "", translation = "声明", example = "The statement was clear.", tags = listOf("cet4"), rootKey = "stat"),
                ImportedWord(term = "status", phonetic = "", definition = "", translation = "状态", example = "Status matters.", tags = listOf("cet4"), rootKey = "stat"),
                ImportedWord(term = "include", phonetic = "", definition = "", translation = "包括", example = "Include the final answer.", tags = listOf("cet4"), rootKey = "clud"),
                ImportedWord(term = "floor", phonetic = "", definition = "", translation = "地板", example = "The floor is clean.", tags = listOf("cet4")),
            ),
        )
        return StudyRepository(
            wordBookDao = wordBookDao,
            wordEntryDao = wordEntryDao,
            progressDao = progressDao,
            reviewLogDao = reviewLogDao,
            preferencesRepository = FakePreferences(),
            importer = FakeImporter(preview),
            scheduler = SpacedRepetitionScheduler(clock),
            clock = clock,
        )
    }

    private fun miniRepoForDerivativeCloze(): StudyRepository {
        val wordBookDao = FakeWordBookDao()
        val wordEntryDao = FakeWordEntryDao()
        val progressDao = FakeWordProgressDao()
        val reviewLogDao = FakeReviewLogDao()
        val preview = ImportPreview(
            title = "derivative cloze",
            description = "",
            source = "imported",
            examTag = "cet4",
            words = listOf(
                ImportedWord(
                    term = "clarify",
                    phonetic = "",
                    definition = "make clear",
                    translation = "澄清",
                    example = "The note clarified the final goal.",
                    tags = listOf("cet4"),
                    rootKey = "clar",
                    derivatives = listOf("clarified", "clarifies"),
                ),
                ImportedWord(term = "clear", phonetic = "", definition = "", translation = "清楚", example = "The sky is clear.", tags = listOf("cet4"), rootKey = "clar"),
                ImportedWord(term = "include", phonetic = "", definition = "", translation = "包括", example = "Include the answer.", tags = listOf("cet4"), rootKey = "clud"),
                ImportedWord(term = "status", phonetic = "", definition = "", translation = "状态", example = "Status matters.", tags = listOf("cet4"), rootKey = "stat"),
                ImportedWord(term = "floor", phonetic = "", definition = "", translation = "地板", example = "The floor is clean.", tags = listOf("cet4")),
            ),
        )
        return StudyRepository(
            wordBookDao = wordBookDao,
            wordEntryDao = wordEntryDao,
            progressDao = progressDao,
            reviewLogDao = reviewLogDao,
            preferencesRepository = FakePreferences(),
            importer = FakeImporter(preview),
            scheduler = SpacedRepetitionScheduler(clock),
            clock = clock,
        )
    }

    private fun miniRepoForMultiSentenceCloze(): StudyRepository {
        val wordBookDao = FakeWordBookDao()
        val wordEntryDao = FakeWordEntryDao()
        val progressDao = FakeWordProgressDao()
        val reviewLogDao = FakeReviewLogDao()
        val preview = ImportPreview(
            title = "multi cloze",
            description = "",
            source = "imported",
            examTag = "cet4",
            words = listOf(
                ImportedWord(
                    term = "clarify",
                    phonetic = "",
                    definition = "make clear",
                    translation = "澄清",
                    example = "Clarify. The team should clarify the final goal before launch.",
                    tags = listOf("cet4"),
                    rootKey = "clar",
                    derivatives = listOf("clarified", "clarifies"),
                ),
                ImportedWord(term = "clear", phonetic = "", definition = "", translation = "清楚", example = "The sky is clear.", tags = listOf("cet4"), rootKey = "clar"),
                ImportedWord(term = "include", phonetic = "", definition = "", translation = "包括", example = "Include the answer.", tags = listOf("cet4"), rootKey = "clud"),
                ImportedWord(term = "status", phonetic = "", definition = "", translation = "状态", example = "Status matters.", tags = listOf("cet4"), rootKey = "stat"),
                ImportedWord(term = "floor", phonetic = "", definition = "", translation = "地板", example = "The floor is clean.", tags = listOf("cet4")),
            ),
        )
        return StudyRepository(
            wordBookDao = wordBookDao,
            wordEntryDao = wordEntryDao,
            progressDao = progressDao,
            reviewLogDao = reviewLogDao,
            preferencesRepository = FakePreferences(),
            importer = FakeImporter(preview),
            scheduler = SpacedRepetitionScheduler(clock),
            clock = clock,
        )
    }

    private fun repoForDerivativeSearch(): StudyRepository {
        val wordBookDao = FakeWordBookDao()
        val wordEntryDao = FakeWordEntryDao()
        val progressDao = FakeWordProgressDao()
        val reviewLogDao = FakeReviewLogDao()
        val preview = ImportPreview(
            title = "derivative search",
            description = "",
            source = "imported",
            examTag = "cet4",
            words = listOf(
                ImportedWord(
                    term = "clarify",
                    phonetic = "",
                    definition = "make clear",
                    translation = "澄清",
                    example = "",
                    tags = listOf("cet4"),
                    rootKey = "clar",
                    derivatives = listOf("clarified", "clarifies"),
                    frq = 100,
                ),
                ImportedWord(
                    term = "classic",
                    phonetic = "",
                    definition = "typical",
                    translation = "经典",
                    example = "",
                    tags = listOf("cet4"),
                    frq = 200,
                ),
                ImportedWord(
                    term = "focus",
                    phonetic = "",
                    definition = "concentrate",
                    translation = "专注",
                    example = "",
                    tags = listOf("cet4"),
                    frq = 50,
                ),
            ),
        )
        return StudyRepository(
            wordBookDao = wordBookDao,
            wordEntryDao = wordEntryDao,
            progressDao = progressDao,
            reviewLogDao = reviewLogDao,
            preferencesRepository = FakePreferences(),
            importer = FakeImporter(preview),
            scheduler = SpacedRepetitionScheduler(clock),
            clock = clock,
        )
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
    override suspend fun searchInBook(bookId: Long, keyword: String, limit: Int, offset: Int): List<WordEntryEntity> {
        val normalized = keyword.trim().lowercase()
        return items
            .filter { entry ->
                entry.bookId == bookId &&
                    (
                        normalized.isEmpty() ||
                            entry.term.lowercase().contains(normalized) ||
                            entry.derivatives.lowercase().contains(normalized) ||
                            entry.translation.lowercase().contains(normalized) ||
                            entry.definition.lowercase().contains(normalized)
                        )
            }
            .sortedBy { it.term.lowercase() }
            .drop(offset)
            .take(limit)
    }
    override suspend fun getBookRootSiblings(bookId: Long, rootKey: String, excludeId: Long, limit: Int) =
        items.filter { it.bookId == bookId && it.rootKey == rootKey && it.id != excludeId }.take(limit)
    override suspend fun getClusteredEntries(bookId: Long) =
        items.filter { it.bookId == bookId && it.rootKey.isNotBlank() }
            .sortedWith(compareBy({ it.rootKey }, { it.frq }, { it.positionInBook }))
    override suspend fun countClusteredForBook(bookId: Long) =
        items.count { it.bookId == bookId && it.rootKey.isNotBlank() }
    override suspend fun getDistractorPool(bookId: Long, excludeId: Long, rootKey: String, limit: Int) =
        items.filter { it.bookId == bookId && it.id != excludeId }
            .sortedByDescending { it.rootKey.isNotBlank() && it.rootKey == rootKey }
            .take(limit)
}

private class FakeWordProgressDao : WordProgressDao {
    private val items = mutableListOf<WordProgressEntity>()
    private var nextId = 1L

    override suspend fun insert(progress: WordProgressEntity): Long {
        val saved = progress.copy(id = nextId++)
        items += saved
        return saved.id
    }
    override suspend fun insertAll(progress: List<WordProgressEntity>): List<Long> =
        progress.map { item ->
            val saved = item.copy(id = nextId++)
            items += saved
            saved.id
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
    override suspend fun getByWordIds(ids: List<Long>) = items.filter { it.wordId in ids }
    override suspend fun countLearnedForBook(bookId: Long) = items.count { it.phase != StudyPhase.NEW }
    fun mutate(wordId: Long, transform: (WordProgressEntity) -> WordProgressEntity) {
        val idx = items.indexOfFirst { it.wordId == wordId }
        if (idx >= 0) items[idx] = transform(items[idx])
    }
}

private class FakeReviewLogDao : ReviewLogDao {
    private val logs = mutableListOf<ReviewLogEntity>()
    fun seed(values: List<Instant>) {
        values.forEach { logs += ReviewLogEntity(wordId = 0, rating = ReviewRating.GOOD, reviewedAt = it, nextReviewAt = it) }
    }
    override suspend fun insert(log: ReviewLogEntity) { logs.add(log.copy(id = (logs.size + 1).toLong())) }
    override suspend fun countReviewedBetween(start: Instant, end: Instant) =
        logs.count { !it.reviewedAt.isBefore(start) && it.reviewedAt.isBefore(end) }
    override suspend fun getAllReviewTimesDesc() = logs.map { it.reviewedAt }.sortedDescending()
    override suspend fun getReviewTimesSince(since: Instant) =
        logs.map { it.reviewedAt }.filter { !it.isBefore(since) }.sorted()
    override suspend fun getToughWordsForBook(bookId: Long, limit: Int): List<ToughWordRow> {
        return logs
            .filter { it.rating == ReviewRating.AGAIN }
            .groupBy { it.wordId }
            .map { (wordId, group) ->
                ToughWordRow(
                    wordId = wordId,
                    againCount = group.size,
                    lastReviewedAt = group.maxOf { it.reviewedAt },
                )
            }
            .sortedWith(compareByDescending<ToughWordRow> { it.againCount }.thenByDescending { it.lastReviewedAt })
            .take(limit)
    }
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
    override suspend fun updateExamPlan(examDate: java.time.LocalDate?, autoPaceEnabled: Boolean) {
        state.value = state.value.copy(
            examDate = examDate,
            autoPaceEnabled = autoPaceEnabled,
        )
    }
}

private class FakeImporter(private val preview: ImportPreview) : BookImporter {
    override suspend fun importBundledBook(assetPath: String): ImportPreview = preview
    override suspend fun import(stream: java.io.InputStream, filename: String, fallbackTitle: String): ImportPreview = preview
}
