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

enum class WordMemoryAnchorKind { ROOT_FAMILY, WORD_FORMS, CONTEXT, SOLO }

data class WordMemoryAnchor(
    val kind: WordMemoryAnchorKind,
    val badgeLabel: String,
    val title: String,
    val message: String,
    val primaryLabel: String,
    val primaryValue: String,
    val secondaryLabel: String,
    val secondaryValue: String,
    val actionLabel: String,
    val focusTerms: List<String>,
)

enum class RootWordGuideKind { ROOT_TRACE, WORD_FORMS, CONTEXT, QUICK_REVIEW }

data class RootWordGuide(
    val kind: RootWordGuideKind,
    val badgeLabel: String,
    val title: String,
    val message: String,
    val primaryLabel: String,
    val primaryValue: String,
    val secondaryLabel: String,
    val secondaryValue: String,
    val actionLabel: String,
    val intensity: Float,
    val focusTerms: List<String>,
)

enum class WordBatchBriefKind { ROOTS, WORD_FORMS, CONTEXT, MIXED, EMPTY }

data class WordBatchBrief(
    val kind: WordBatchBriefKind,
    val title: String,
    val message: String,
    val primaryLabel: String,
    val primaryValue: String,
    val secondaryLabel: String,
    val secondaryValue: String,
    val actionLabel: String,
    val intensity: Float,
    val focusTerms: List<String>,
)

enum class MnemonicBatchBriefKind { READY, SEED_GAP, ROOT_BRIDGE, QUICK_START, EMPTY }

data class MnemonicBatchBrief(
    val kind: MnemonicBatchBriefKind,
    val title: String,
    val message: String,
    val primaryLabel: String,
    val primaryValue: String,
    val secondaryLabel: String,
    val secondaryValue: String,
    val actionLabel: String,
    val coverage: Float,
    val focusTerms: List<String>,
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

enum class DailyLoadBriefKind { REVIEW_DEBT, TOUGH_REPAIR, ROOT_GAP, PACE_PUSH, BALANCED, CLEAR }

data class DailyLoadLane(
    val label: String,
    val value: String,
    val weight: Float,
)

data class DailyLoadBrief(
    val kind: DailyLoadBriefKind,
    val title: String,
    val message: String,
    val primaryLabel: String,
    val primaryValue: String,
    val secondaryLabel: String,
    val secondaryValue: String,
    val actionLabel: String,
    val intensity: Float,
    val lanes: List<DailyLoadLane>,
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

enum class StudyRhythmBriefKind { QUIET, RECOVERY, BALANCE, STEADY, SURGE }

data class StudyRhythmBrief(
    val kind: StudyRhythmBriefKind,
    val title: String,
    val message: String,
    val primaryLabel: String,
    val primaryValue: String,
    val secondaryLabel: String,
    val secondaryValue: String,
    val actionLabel: String,
    val momentum: Float,
    val recentCounts: List<Int>,
)

data class RootGroup(
    val rootKey: String,
    val meanings: List<String>,
    val totalWords: Int,
    val learnedWords: Int,
    val members: List<WordEntry>,
)

enum class RootGroupStage { SEED, BUILDING, CONSOLIDATING, MASTERED }

data class RootGroupInsight(
    val stage: RootGroupStage,
    val badgeLabel: String,
    val title: String,
    val message: String,
    val primaryLabel: String,
    val primaryValue: String,
    val secondaryLabel: String,
    val secondaryValue: String,
    val actionLabel: String,
    val progress: Float,
    val focusTerms: List<String>,
)

enum class RootAtlasBriefKind { EMPTY, SEED, EXPAND, CONSOLIDATE, MASTERED }

data class RootAtlasBrief(
    val kind: RootAtlasBriefKind,
    val title: String,
    val message: String,
    val primaryLabel: String,
    val primaryValue: String,
    val secondaryLabel: String,
    val secondaryValue: String,
    val actionLabel: String,
    val progress: Float,
    val focusRoots: List<String>,
)

enum class RootMnemonicBriefKind { EMPTY, ROOT_SEED, PATCH_GAPS, READY, SATURATED }

data class RootMnemonicBrief(
    val kind: RootMnemonicBriefKind,
    val title: String,
    val message: String,
    val primaryLabel: String,
    val primaryValue: String,
    val secondaryLabel: String,
    val secondaryValue: String,
    val actionLabel: String,
    val progress: Float,
    val focusRoots: List<String>,
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

enum class StudyFocusKind { REVIEW, PACE, ROOTS, NEW_WORDS, MOMENTUM }

data class StudyFocusCue(
    val kind: StudyFocusKind,
    val title: String,
    val message: String,
    val primaryLabel: String,
    val primaryValue: String,
    val secondaryLabel: String,
    val secondaryValue: String,
    val actionLabel: String,
    val progress: Float,
)

enum class DailyStudyRouteTarget { REVIEW, TOUGH, ROOTS, LEARN }

data class DailyStudyRouteStep(
    val target: DailyStudyRouteTarget,
    val title: String,
    val reason: String,
    val metricLabel: String,
    val metricValue: String,
    val actionLabel: String,
    val weight: Float,
)

data class DailyStudyRoute(
    val headline: String,
    val summary: String,
    val steps: List<DailyStudyRouteStep>,
)

enum class Morpheme { PREFIX, ROOT, SUFFIX, PLAIN }

data class MorphemeSegment(
    val text: String,
    val kind: Morpheme,
)

enum class PracticeMode { FLIP, CHOICE, CLOZE, SPELL, DICTATION }

data class PracticeSessionStats(
    val answered: Int = 0,
    val stable: Int = 0,
    val needsPractice: Int = 0,
) {
    val hasAttempts: Boolean get() = answered > 0
    val stabilityPercent: Int get() = if (answered == 0) 0 else (stable * 100) / answered
}

enum class PracticeSessionCoachKind { WARMUP, RECOVER, STABILIZE, ADVANCE }

data class PracticeSessionCoach(
    val kind: PracticeSessionCoachKind,
    val title: String,
    val message: String,
    val primaryLabel: String,
    val primaryValue: String,
    val secondaryLabel: String,
    val secondaryValue: String,
    val actionLabel: String,
    val progress: Float,
)

enum class ReviewQueueBriefKind { EMPTY, WARMUP, ROOT_TRACE, CONTEXT, ACTIVE_RECALL, MIXED }

data class ReviewQueueBrief(
    val kind: ReviewQueueBriefKind,
    val title: String,
    val message: String,
    val primaryLabel: String,
    val primaryValue: String,
    val secondaryLabel: String,
    val secondaryValue: String,
    val actionLabel: String,
    val intensity: Float,
    val focusTerms: List<String>,
)

enum class ReviewExitBriefKind { START, CONTINUE, REPAIR, LEVEL_UP, WRAP_UP, CLEAR }

data class ReviewExitBrief(
    val kind: ReviewExitBriefKind,
    val title: String,
    val message: String,
    val primaryLabel: String,
    val primaryValue: String,
    val secondaryLabel: String,
    val secondaryValue: String,
    val actionLabel: String,
    val progress: Float,
)

data class QuizQuestion(
    val word: WordEntry,
    /** Options are term strings; the correct one is guaranteed to be included. */
    val options: List<String>,
    val correct: String,
)

data class ClozeQuestion(
    val word: WordEntry,
    val prompt: String,
    /** Options are term strings; the correct one is guaranteed to be included. */
    val options: List<String>,
    val correct: String,
)

data class ClozeBlank(
    val prompt: String,
    val answer: String,
)

enum class ClozeContextGuideKind { ROOT_TRACE, WORD_FORM, MEANING, QUICK_SCAN }

data class ClozeContextGuide(
    val kind: ClozeContextGuideKind,
    val badgeLabel: String,
    val title: String,
    val message: String,
    val primaryLabel: String,
    val primaryValue: String,
    val secondaryLabel: String,
    val secondaryValue: String,
    val actionLabel: String,
    val confidence: Float,
    val focusTerms: List<String>,
)

data class ToughWord(
    val word: WordEntry,
    val againCount: Int,
    val lapses: Int,
    val lastReviewedAt: Instant?,
)

enum class ToughWordPrescriptionKind { REBUILD, ROOT_TRACE, CONTEXT, STABILIZE }

data class ToughWordPrescription(
    val kind: ToughWordPrescriptionKind,
    val badgeLabel: String,
    val title: String,
    val message: String,
    val primaryLabel: String,
    val primaryValue: String,
    val secondaryLabel: String,
    val secondaryValue: String,
    val actionLabel: String,
    val intensity: Float,
)

data class ToughWordsBrief(
    val title: String,
    val message: String,
    val dominantKind: ToughWordPrescriptionKind,
    val dominantLabel: String,
    val totalCount: Int,
    val highRiskCount: Int,
    val peakAgainCount: Int,
    val actionLabel: String,
    val intensity: Float,
)

enum class VocabularySearchInsightKind { READY, EMPTY_RESULTS, TERM_MATCH, WORD_FORM_MATCH, ROOT_CLUSTER, MEANING_MATCH }

data class VocabularySearchInsight(
    val kind: VocabularySearchInsightKind,
    val title: String,
    val message: String,
    val primaryLabel: String,
    val primaryValue: String,
    val secondaryLabel: String,
    val secondaryValue: String,
    val actionLabel: String,
    val confidence: Float,
    val focusTerms: List<String>,
)
