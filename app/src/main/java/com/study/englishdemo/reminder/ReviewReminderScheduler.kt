package com.study.englishdemo.reminder

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.study.englishdemo.data.SettingsState
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit

class ReviewReminderScheduler(
    private val context: Context,
    private val clock: Clock = Clock.systemDefaultZone(),
) {
    fun sync(settings: SettingsState) {
        val workManager = WorkManager.getInstance(context)
        if (!settings.reviewReminderEnabled) {
            workManager.cancelUniqueWork(ReviewReminderWorker.WORK_NAME)
            return
        }

        val request = PeriodicWorkRequestBuilder<ReviewReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(
                computeReminderInitialDelay(
                    clock.instant(),
                    clock.zone,
                    settings.reminderHour,
                    settings.reminderMinute,
                ).toMillis(),
                TimeUnit.MILLISECONDS,
            )
            .build()

        workManager.enqueueUniquePeriodicWork(
            ReviewReminderWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request,
        )
    }
}

internal fun computeReminderInitialDelay(
    nowInstant: Instant,
    zone: ZoneId,
    reminderHour: Int,
    reminderMinute: Int,
): Duration {
    val now = LocalDateTime.ofInstant(nowInstant, zone)
    var nextRun = now.withHour(reminderHour).withMinute(reminderMinute).withSecond(0).withNano(0)
    if (!nextRun.isAfter(now)) {
        nextRun = nextRun.plusDays(1)
    }
    return Duration.between(now.atZone(zone), nextRun.atZone(zone))
}
