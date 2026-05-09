package com.study.englishdemo.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.LocalDate

@Entity(tableName = "word_books")
data class WordBookEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val source: String,
    val examTag: String,
    val totalWords: Int,
    val createdAt: Instant,
)

@Entity(
    tableName = "word_entries",
    foreignKeys = [
        ForeignKey(
            entity = WordBookEntity::class,
            parentColumns = ["id"],
            childColumns = ["bookId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("bookId"), Index(value = ["bookId", "term"], unique = true), Index("rootKey")],
)
data class WordEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bookId: Long,
    val term: String,
    val phonetic: String,
    val definition: String,
    val translation: String,
    val example: String,
    val tags: String,
    val rootKey: String = "",
    val derivatives: String = "",
    val synonyms: String = "",
    val antonyms: String = "",
    val mnemonic: String = "",
    val frq: Int = 0,
    val pos: String = "",
    val positionInBook: Int = 0,
)

@Entity(
    tableName = "word_progress",
    foreignKeys = [
        ForeignKey(
            entity = WordEntryEntity::class,
            parentColumns = ["id"],
            childColumns = ["wordId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["wordId"], unique = true), Index("nextReviewAt")],
)
data class WordProgressEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val wordId: Long,
    val phase: StudyPhase,
    val familiarity: Int,
    val streak: Int,
    val lapses: Int,
    val easeFactor: Double,
    val intervalDays: Int,
    val lastReviewedAt: Instant?,
    val nextReviewAt: Instant,
    val createdAt: Instant,
)

@Entity(
    tableName = "review_logs",
    foreignKeys = [
        ForeignKey(
            entity = WordEntryEntity::class,
            parentColumns = ["id"],
            childColumns = ["wordId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("wordId"), Index("reviewedAt")],
)
data class ReviewLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val wordId: Long,
    val rating: ReviewRating,
    val reviewedAt: Instant,
    val nextReviewAt: Instant,
)

enum class StudyPhase {
    NEW,
    LEARNING,
    REVIEW,
    MASTERED,
}

enum class ReviewRating {
    AGAIN,
    HARD,
    GOOD,
    EASY,
}

data class WordEntry(
    val id: Long,
    val term: String,
    val phonetic: String,
    val definition: String,
    val translation: String,
    val example: String,
    val tags: List<String>,
    val rootKey: String = "",
    val derivatives: List<String> = emptyList(),
    val synonyms: List<String> = emptyList(),
    val antonyms: List<String> = emptyList(),
    val mnemonic: String = "",
    val pos: String = "",
    val frq: Int = 0,
    val progress: WordProgress?,
)

data class RootReference(
    val key: String,
    val meanings: List<String>,
    val examples: List<String>,
)

data class WordProgress(
    val phase: StudyPhase,
    val familiarity: Int,
    val streak: Int,
    val lapses: Int,
    val easeFactor: Double,
    val intervalDays: Int,
    val lastReviewedAt: Instant?,
    val nextReviewAt: Instant,
)

data class WordBook(
    val id: Long,
    val title: String,
    val description: String,
    val source: String,
    val examTag: String,
    val totalWords: Int,
)

data class DailyOverview(
    val today: LocalDate,
    val newWordTarget: Int,
    val newWordsRemaining: Int,
    val reviewDueCount: Int,
    val streakDays: Int,
    val studiedToday: Int,
    val completionRatio: Float,
)

data class LearningSession(
    val overview: DailyOverview,
    val dueReviewWords: List<WordEntry>,
    val recommendedNewWords: List<WordEntry>,
)

data class ImportedBookResult(
    val bookId: Long,
    val preview: ImportPreview,
)

data class ImportPreview(
    val title: String,
    val description: String,
    val source: String,
    val examTag: String,
    val words: List<ImportedWord>,
)

data class ImportedWord(
    val term: String,
    val phonetic: String,
    val definition: String,
    val translation: String,
    val example: String,
    val tags: List<String>,
    val rootKey: String = "",
    val derivatives: List<String> = emptyList(),
    val synonyms: List<String> = emptyList(),
    val antonyms: List<String> = emptyList(),
    val mnemonic: String = "",
    val pos: String = "",
    val frq: Int = 0,
)

data class SettingsState(
    val dailyNewWordTarget: Int = 20,
    val reviewReminderEnabled: Boolean = true,
    val reminderHour: Int = 20,
    val reminderMinute: Int = 30,
    val examDate: LocalDate? = null,
    val autoPaceEnabled: Boolean = false,
)

data class DailyReviewCount(
    val date: LocalDate,
    val count: Int,
)

data class RootGroup(
    val rootKey: String,
    val meanings: List<String>,
    val totalWords: Int,
    val learnedWords: Int,
    val members: List<WordEntry>,
)

data class BookRootSnapshot(
    val totalRoots: Int,
    val touchedRoots: Int,
    val totalClustered: Int,
    val learnedClustered: Int,
)

data class PaceRecommendation(
    val target: Int,
    val remainingWords: Int,
    val remainingDays: Int?,
    val examDate: LocalDate?,
    val isAuto: Boolean,
)

enum class Morpheme { PREFIX, ROOT, SUFFIX, PLAIN }

data class MorphemeSegment(
    val text: String,
    val kind: Morpheme,
)
