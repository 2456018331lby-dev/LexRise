package com.study.englishdemo.data

import com.google.common.truth.Truth.assertThat
import com.study.englishdemo.reminder.computeReminderInitialDelay
import org.junit.Test
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset

class ReviewReminderSchedulerTest {
    @Test
    fun computeInitialDelay_rollsToNextDayWhenReminderPassed() {
        val delay = computeReminderInitialDelay(
            Instant.parse("2026-05-09T14:30:00Z"),
            ZoneOffset.UTC,
            9,
            0,
        )

        assertThat(delay).isEqualTo(Duration.ofHours(18).plusMinutes(30))
    }

    @Test
    fun computeInitialDelay_targetsSameDayFutureReminder() {
        val delay = computeReminderInitialDelay(
            Instant.parse("2026-05-09T14:30:00Z"),
            ZoneOffset.UTC,
            20,
            15,
        )

        assertThat(delay).isEqualTo(Duration.ofHours(5).plusMinutes(45))
    }
}
