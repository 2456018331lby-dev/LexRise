package com.study.englishdemo.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.study.englishdemo.AppContainer
import com.study.englishdemo.data.BookRootSnapshot
import com.study.englishdemo.data.ClozeQuestion
import com.study.englishdemo.data.DailyReviewCount
import com.study.englishdemo.data.LearningSession
import com.study.englishdemo.data.PaceRecommendation
import com.study.englishdemo.data.PracticeMode
import com.study.englishdemo.data.PracticeSessionStats
import com.study.englishdemo.data.QuizQuestion
import com.study.englishdemo.data.ReviewRating
import com.study.englishdemo.data.RootGroup
import com.study.englishdemo.data.RootReference
import com.study.englishdemo.data.SettingsState
import com.study.englishdemo.data.StudyPhase
import com.study.englishdemo.data.ToughWord
import com.study.englishdemo.data.WordBook
import com.study.englishdemo.data.WordEntry
import com.study.englishdemo.data.normalizedEditDistance
import com.study.englishdemo.data.recordPracticeAttempt
import com.study.englishdemo.data.ratingFromEditDistance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

data class AppUiState(
    val loading: Boolean = true,
    val books: List<WordBook> = emptyList(),
    val selectedBookId: Long? = null,
    val selectedBookTitle: String = "",
    val session: LearningSession? = null,
    val settings: SettingsState = SettingsState(),
    val message: String? = null,
    val isImporting: Boolean = false,
    val recentReviewCounts: List<DailyReviewCount> = emptyList(),
    val vocabularyQuery: String = "",
    val vocabularyPhaseFilter: StudyPhase? = null,
    val vocabularyResults: List<WordEntry> = emptyList(),
    val vocabularyLoading: Boolean = false,
    val rootRefByKey: Map<String, RootReference> = emptyMap(),
    val siblingsByWordId: Map<Long, List<WordEntry>> = emptyMap(),
    val anchorWordIds: Set<Long> = emptySet(),
    val rootSnapshot: BookRootSnapshot = BookRootSnapshot(0, 0, 0, 0),
    val pace: PaceRecommendation = PaceRecommendation(
        target = 20, remainingWords = 0, remainingDays = null,
        examDate = null, isAuto = false,
    ),
    val rootsQuery: String = "",
    val rootGroups: List<RootGroup> = emptyList(),
    val rootsLoading: Boolean = false,
    val practiceMode: PracticeMode = PracticeMode.FLIP,
    val practiceStats: PracticeSessionStats = PracticeSessionStats(),
    val toughWords: List<ToughWord> = emptyList(),
    val toughLoading: Boolean = false,
)

class AppViewModel(private val container: AppContainer) : ViewModel() {
    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    private var vocabularySearchJob: Job? = null
    private var rootsSearchJob: Job? = null

    init {
        refresh()
    }

    fun refresh(preferredBookId: Long? = _uiState.value.selectedBookId) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true)
            val defaultBookId = container.repository.ensureBundledBookImported()
            val books = container.repository.observeBooks().first()
            val selectedBook = books.firstOrNull { it.id == preferredBookId }
                ?: books.firstOrNull { it.id == defaultBookId }
                ?: books.firstOrNull()
            val selectedBookId = requireNotNull(selectedBook?.id) { "没有可用词书" }
            val session = container.repository.getLearningSession(selectedBookId)
            val settings = container.repository.getSettings()
            val recent = container.repository.recentReviewCounts(30)
            val snapshot = container.repository.getBookRootSnapshot(selectedBookId)
            val anchors = container.repository.getAnchorWordIds(selectedBookId)
            val pace = container.repository.getPaceRecommendation(selectedBookId)
            val (rootMap, siblingsMap) = resolveRootsAndSiblings(session)
            val keepPracticeStats = selectedBookId == _uiState.value.selectedBookId
            _uiState.value = AppUiState(
                loading = false,
                books = books,
                selectedBookId = selectedBookId,
                selectedBookTitle = selectedBook.title,
                session = session,
                settings = settings,
                message = _uiState.value.message,
                isImporting = false,
                recentReviewCounts = recent,
                vocabularyQuery = _uiState.value.vocabularyQuery,
                vocabularyPhaseFilter = _uiState.value.vocabularyPhaseFilter,
                anchorWordIds = anchors,
                rootSnapshot = snapshot,
                pace = pace,
                rootsQuery = _uiState.value.rootsQuery,
                rootRefByKey = rootMap,
                siblingsByWordId = siblingsMap,
                practiceMode = _uiState.value.practiceMode,
                practiceStats = if (keepPracticeStats) _uiState.value.practiceStats else PracticeSessionStats(),
            )
            loadVocabulary()
            loadRootGroups()
            loadToughWords()
        }
    }

    private suspend fun resolveRootsAndSiblings(
        session: LearningSession,
    ): Pair<Map<String, RootReference>, Map<Long, List<WordEntry>>> {
        val allWords = session.recommendedNewWords + session.dueReviewWords
        val rootKeys = allWords.mapNotNull { w -> w.rootKey.takeIf { it.isNotBlank() } }.toSet()
        val rootMap = rootKeys.mapNotNull { key ->
            container.rootReferenceLoader.lookup(key)?.let { key to it }
        }.toMap()
        val siblings = allWords.filter { it.rootKey.isNotBlank() }.associate { w ->
            w.id to container.repository.getSemanticNeighbors(w.id, 4)
        }
        return rootMap to siblings
    }

    fun selectBook(bookId: Long) {
        refresh(bookId)
    }

    fun reviewWord(wordId: Long, rating: ReviewRating) {
        reviewWordInternal(wordId, rating, countPracticeAttempt = false)
    }

    fun reviewPracticeWord(wordId: Long, rating: ReviewRating) {
        reviewWordInternal(wordId, rating, countPracticeAttempt = true)
    }

    private fun reviewWordInternal(wordId: Long, rating: ReviewRating, countPracticeAttempt: Boolean) {
        viewModelScope.launch {
            container.repository.reviewWord(wordId, rating)
            _uiState.value = _uiState.value.copy(
                message = "已记录 ${rating.name.lowercase()} 反馈",
                practiceStats = if (countPracticeAttempt) {
                    recordPracticeAttempt(_uiState.value.practiceStats, rating)
                } else {
                    _uiState.value.practiceStats
                },
            )
            val selectedBookId = _uiState.value.selectedBookId ?: return@launch
            val session = container.repository.getLearningSession(selectedBookId)
            val snapshot = container.repository.getBookRootSnapshot(selectedBookId)
            val pace = container.repository.getPaceRecommendation(selectedBookId)
            val (rootMap, siblingsMap) = resolveRootsAndSiblings(session)
            _uiState.value = _uiState.value.copy(
                session = session,
                settings = container.repository.getSettings(),
                recentReviewCounts = container.repository.recentReviewCounts(30),
                rootSnapshot = snapshot,
                pace = pace,
                rootRefByKey = rootMap,
                siblingsByWordId = siblingsMap,
            )
            loadVocabulary()
            loadRootGroups()
            loadToughWords()
        }
    }

    fun updateMnemonic(wordId: Long, text: String) {
        viewModelScope.launch {
            container.repository.updateMnemonic(wordId, text)
            val selectedBookId = _uiState.value.selectedBookId ?: return@launch
            val session = container.repository.getLearningSession(selectedBookId)
            _uiState.value = _uiState.value.copy(
                session = session,
                message = if (text.isBlank()) "已清除巧记" else "已保存巧记",
            )
        }
    }

    fun speakWord(term: String) {
        container.speaker.speak(term)
    }

    fun updateDailyTarget(target: Int) {
        viewModelScope.launch {
            container.repository.updateDailyTarget(target)
            val selectedBookId = _uiState.value.selectedBookId ?: return@launch
            _uiState.value = _uiState.value.copy(
                session = container.repository.getLearningSession(selectedBookId),
                settings = container.repository.getSettings(),
                pace = container.repository.getPaceRecommendation(selectedBookId),
            )
        }
    }

    fun updateReminderSettings(enabled: Boolean, hour: Int, minute: Int) {
        viewModelScope.launch {
            container.repository.updateReminderSettings(enabled, hour, minute)
            val settings = container.repository.getSettings()
            container.reminderScheduler.sync(settings)
            _uiState.value = _uiState.value.copy(settings = settings, message = "复习提醒已更新")
        }
    }

    fun updateExamPlan(examDate: LocalDate?, autoPaceEnabled: Boolean) {
        viewModelScope.launch {
            container.repository.updateExamPlan(examDate, autoPaceEnabled)
            val selectedBookId = _uiState.value.selectedBookId ?: return@launch
            val session = container.repository.getLearningSession(selectedBookId)
            _uiState.value = _uiState.value.copy(
                settings = container.repository.getSettings(),
                session = session,
                pace = container.repository.getPaceRecommendation(selectedBookId),
                message = if (examDate == null) "已清除考试日期" else "学习计划已更新",
            )
        }
    }

    fun deleteBook(bookId: Long) {
        viewModelScope.launch {
            runCatching { container.repository.deleteBook(bookId) }
                .onSuccess {
                    _uiState.value = _uiState.value.copy(message = "已删除词书")
                    val fallback = _uiState.value.books.firstOrNull { it.id != bookId }?.id
                    refresh(fallback)
                }
                .onFailure { throwable ->
                    _uiState.value = _uiState.value.copy(
                        message = throwable.message ?: "删除失败",
                    )
                }
        }
    }

    fun renameBook(bookId: Long, newTitle: String) {
        viewModelScope.launch {
            runCatching { container.repository.renameBook(bookId, newTitle) }
                .onSuccess {
                    _uiState.value = _uiState.value.copy(message = "已重命名")
                    refresh()
                }
                .onFailure { throwable ->
                    _uiState.value = _uiState.value.copy(
                        message = throwable.message ?: "重命名失败",
                    )
                }
        }
    }

    fun updateVocabularyQuery(query: String) {
        _uiState.value = _uiState.value.copy(vocabularyQuery = query)
        loadVocabulary()
    }

    fun updateVocabularyPhase(phase: StudyPhase?) {
        _uiState.value = _uiState.value.copy(vocabularyPhaseFilter = phase)
        loadVocabulary()
    }

    fun updateRootsQuery(query: String) {
        _uiState.value = _uiState.value.copy(rootsQuery = query)
        loadRootGroups()
    }

    fun setPracticeMode(mode: PracticeMode) {
        _uiState.value = _uiState.value.copy(practiceMode = mode)
    }

    fun resetPracticeSessionStats() {
        _uiState.value = _uiState.value.copy(practiceStats = PracticeSessionStats())
    }

    /**
     * For CHOICE/SPELL/DICTATION modes: user picks/types an answer. Repository
     * records the rating against SRS exactly like a flip-card review. Caller
     * handles advancing to the next word from `session` state.
     */
    fun submitAnswer(wordId: Long, rating: ReviewRating) {
        reviewPracticeWord(wordId, rating)
    }

    suspend fun buildQuiz(wordId: Long): QuizQuestion? {
        val bookId = _uiState.value.selectedBookId ?: return null
        return container.repository.buildQuizQuestion(wordId, bookId)
    }

    suspend fun buildCloze(wordId: Long): ClozeQuestion? {
        val bookId = _uiState.value.selectedBookId ?: return null
        return container.repository.buildClozeQuestion(wordId, bookId)
    }

    fun gradeSpelling(answer: String, correct: String): ReviewRating {
        val d = normalizedEditDistance(answer, correct)
        return ratingFromEditDistance(d)
    }

    fun loadToughWords() {
        val bookId = _uiState.value.selectedBookId ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(toughLoading = true)
            val list = container.repository.getToughWords(bookId, limit = 40)
            _uiState.value = _uiState.value.copy(
                toughWords = list,
                toughLoading = false,
            )
        }
    }

    private fun loadVocabulary() {
        val bookId = _uiState.value.selectedBookId ?: return
        vocabularySearchJob?.cancel()
        vocabularySearchJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(vocabularyLoading = true)
            val words = container.repository.searchWords(bookId, _uiState.value.vocabularyQuery)
            val phase = _uiState.value.vocabularyPhaseFilter
            val filtered = if (phase == null) words else words.filter { it.progress?.phase == phase }
            _uiState.value = _uiState.value.copy(
                vocabularyResults = filtered,
                vocabularyLoading = false,
            )
        }
    }

    private fun loadRootGroups() {
        val bookId = _uiState.value.selectedBookId ?: return
        rootsSearchJob?.cancel()
        rootsSearchJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(rootsLoading = true)
            val groups = container.repository.getRootGroups(
                bookId = bookId,
                loader = { container.rootReferenceLoader.lookup(it) },
                query = _uiState.value.rootsQuery,
                limit = 120,
            )
            _uiState.value = _uiState.value.copy(
                rootGroups = groups,
                rootsLoading = false,
            )
        }
    }

    fun importBook(context: android.content.Context, uri: android.net.Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isImporting = true, message = null)
            val fileName = (uri.lastPathSegment ?: "imported_words.csv").substringAfterLast('/')
            runCatching {
                val imported = withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        container.repository.importBook(fileName, stream)
                    }
                } ?: error("无法读取所选文件")
                _uiState.value = _uiState.value.copy(
                    message = "已导入 ${imported.preview.title}，共 ${imported.preview.words.size} 个词条",
                )
                refresh(imported.bookId)
            }.onFailure { throwable ->
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    isImporting = false,
                    message = "导入失败：${throwable.message ?: "请检查文件格式"}",
                )
            }
        }
    }
}

class AppViewModelFactory(private val container: AppContainer) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = AppViewModel(container) as T
}
