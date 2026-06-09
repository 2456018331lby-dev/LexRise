package com.study.englishdemo.data

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class SpacedRepetitionSchedulerTest {
    private val clock = Clock.fixed(Instant.parse("2026-05-09T10:00:00Z"), ZoneOffset.UTC)
    private val scheduler = SpacedRepetitionScheduler(clock)

    @Test
    fun again_resetsToShortInterval() {
        val current = scheduler.initialProgress(1).copy(
            phase = StudyPhase.REVIEW,
            intervalDays = 5,
            easeFactor = 2.5,
            streak = 3,
        )

        val updated = scheduler.review(current, ReviewRating.AGAIN)

        assertThat(updated.intervalDays).isEqualTo(1)
        assertThat(updated.phase).isEqualTo(StudyPhase.LEARNING)
        assertThat(updated.streak).isEqualTo(0)
        assertThat(updated.lapses).isEqualTo(1)
    }

    @Test
    fun easy_promotesToLongerInterval() {
        val current = scheduler.initialProgress(2).copy(
            phase = StudyPhase.REVIEW,
            intervalDays = 8,
            easeFactor = 2.3,
            streak = 5,
        )

        val updated = scheduler.review(current, ReviewRating.EASY)

        assertThat(updated.intervalDays).isGreaterThan(8)
        assertThat(updated.easeFactor).isGreaterThan(2.3)
        assertThat(updated.phase).isAnyOf(StudyPhase.REVIEW, StudyPhase.MASTERED)
    }

    @Test
    fun hard_alwaysAdvancesBeyondCurrentInterval() {
        val current = scheduler.initialProgress(3).copy(
            phase = StudyPhase.LEARNING,
            intervalDays = 1,
            easeFactor = 2.3,
        )

        val updated = scheduler.review(current, ReviewRating.HARD)

        assertThat(updated.intervalDays).isGreaterThan(1)
    }

    @Test
    fun good_onFreshCardPromotesInterval() {
        val current = scheduler.initialProgress(4)

        val updated = scheduler.review(current, ReviewRating.GOOD)

        assertThat(updated.intervalDays).isAtLeast(2)
        assertThat(updated.nextReviewAt).isGreaterThan(clock.instant())
    }
}
