package com.study.englishdemo.reminder

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.study.englishdemo.EnglishDemoApplication

class ReviewReminderWorker(
    context: Context,
    workerParameters: WorkerParameters,
) : CoroutineWorker(context, workerParameters) {
    override suspend fun doWork(): Result {
        val app = applicationContext as EnglishDemoApplication
        val dueCount = app.container.repository.getDueCount()
        if (dueCount <= 0) return Result.success()

        if (ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS,
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return Result.success()
        }

        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "review_reminders"
        if (manager.getNotificationChannel(channelId) == null) {
            manager.createNotificationChannel(
                NotificationChannel(
                    channelId,
                    "复习提醒",
                    NotificationManager.IMPORTANCE_DEFAULT,
                ),
            )
        }

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("LexRise 复习提醒")
            .setContentText("现在有 $dueCount 个单词等待复习。")
            .setAutoCancel(true)
            .build()
        manager.notify(1001, notification)
        return Result.success()
    }

    companion object {
        const val WORK_NAME = "review_reminder_worker"
    }
}
