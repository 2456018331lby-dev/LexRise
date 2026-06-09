package com.study.englishdemo.data

import java.time.Clock
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.math.max

class SpacedRepetitionScheduler(private val clock: Clock = Clock.systemDefaultZone()) {
    fun initialProgress(wordId: Long): WordProgressEntity {
        val now = clock.instant()
        return WordProgressEntity(
            wordId = wordId,
            phase = StudyPhase.NEW,
            familiarity = 0,
            streak = 0,
            lapses = 0,
            easeFactor = 2.3,
            intervalDays = 0,
            lastReviewedAt = null,
            nextReviewAt = now,
            createdAt = now,
        )
    }

    fun review(current: WordProgressEntity, rating: ReviewRating): WordProgressEntity {
        val now = clock.instant()
        val nextEase = when (rating) {
            ReviewRating.AGAIN -> max(1.3, current.easeFactor - 0.2)
            ReviewRating.HARD -> max(1.3, current.easeFactor - 0.12)
            ReviewRating.GOOD -> current.easeFactor
            ReviewRating.EASY -> current.easeFactor + 0.15
        }

        val baseInterval = max(1, current.intervalDays)
        val nextInterval = when (rating) {
            ReviewRating.AGAIN -> 1
            ReviewRating.HARD -> max(baseInterval + 1, (baseInterval * 1.2).toInt())
            ReviewRating.GOOD -> max(baseInterval + 1, (baseInterval * nextEase).toInt())
            ReviewRating.EASY -> max(baseInterval + 2, (baseInterval * (nextEase + 0.35)).toInt())
        }

        val nextPhase = when {
            rating == ReviewRating.AGAIN -> StudyPhase.LEARNING
            nextInterval >= 21 -> StudyPhase.MASTERED
            nextInterval >= 3 -> StudyPhase.REVIEW
            else -> StudyPhase.LEARNING
        }

        return current.copy(
            phase = nextPhase,
            familiarity = current.familiarity + when (rating) {
                ReviewRating.AGAIN -> 0
                ReviewRating.HARD -> 1
                ReviewRating.GOOD -> 2
                ReviewRating.EASY -> 3
            },
            streak = if (rating == ReviewRating.AGAIN) 0 else current.streak + 1,
            lapses = if (rating == ReviewRating.AGAIN) current.lapses + 1 else current.lapses,
            easeFactor = nextEase,
            intervalDays = nextInterval,
            lastReviewedAt = now,
            nextReviewAt = now.plus(nextInterval.toLong(), ChronoUnit.DAYS),
        )
    }

    fun isDue(progress: WordProgressEntity, now: Instant = clock.instant()): Boolean =
        progress.nextReviewAt <= now
}
