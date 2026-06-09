package com.study.englishdemo.ui

import com.study.englishdemo.data.DailyOverview
import com.study.englishdemo.data.DailyReviewCount
import com.study.englishdemo.data.LearningSession
import com.study.englishdemo.data.SettingsState
import com.study.englishdemo.data.StudyPhase
import com.study.englishdemo.data.WordBook
import com.study.englishdemo.data.WordEntry
import com.study.englishdemo.data.WordProgress
import java.time.Instant
import java.time.LocalDate

fun previewUiState(): AppUiState {
    val word = WordEntry(
        id = 1,
        term = "clarify",
        phonetic = "/ˈklærəfaɪ/",
        definition = "to make easier to understand",
        translation = "澄清",
        example = "A note can clarify the difference between similar words.",
        tags = listOf("cet4", "clar"),
        rootKey = "clar",
        derivatives = listOf("clarified", "clarifies", "clarifying"),
        synonyms = listOf("explain"),
        antonyms = listOf("confuse"),
        mnemonic = "clar = clear，让事情 clear 起来",
        pos = "vt./vi.",
        frq = 1234,
        progress = WordProgress(
            phase = StudyPhase.REVIEW,
            familiarity = 6,
            streak = 4,
            lapses = 1,
            easeFactor = 2.5,
            intervalDays = 5,
            lastReviewedAt = Instant.now(),
            nextReviewAt = Instant.now(),
        ),
    )
    val today = LocalDate.now()
    return AppUiState(
        loading = false,
        books = listOf(
            WordBook(1, "四六级核心高频词", "离线内置词书，首版默认导入。", "builtin", "cet4", 20),
        ),
        selectedBookId = 1,
        selectedBookTitle = "四六级核心高频词",
        session = LearningSession(
            overview = DailyOverview(
                today = today,
                newWordTarget = 20,
                newWordsRemaining = 8,
                reviewDueCount = 12,
                streakDays = 5,
                studiedToday = 14,
                completionRatio = 0.58f,
            ),
            dueReviewWords = listOf(word),
            recommendedNewWords = listOf(word.copy(id = 2, term = "enhance")),
        ),
        settings = SettingsState(),
        recentReviewCounts = (0 until 7).map {
            DailyReviewCount(today.minusDays((6 - it).toLong()), (it * 3) % 11)
        },
        vocabularyResults = listOf(word, word.copy(id = 3, term = "maintain")),
    )
}
