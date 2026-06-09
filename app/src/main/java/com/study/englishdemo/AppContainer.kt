package com.study.englishdemo

import android.content.Context
import com.study.englishdemo.data.AppDatabase
import com.study.englishdemo.data.RootReferenceLoader
import com.study.englishdemo.data.StudyRepository
import com.study.englishdemo.data.UserPreferencesRepository
import com.study.englishdemo.data.WordBookImporter
import com.study.englishdemo.reminder.ReviewReminderScheduler
import com.study.englishdemo.speech.WordSpeaker

class AppContainer(context: Context) {
    private val appContext = context.applicationContext
    private val database = AppDatabase.create(appContext)

    val preferencesRepository = UserPreferencesRepository(appContext)
    val rootReferenceLoader = RootReferenceLoader(appContext)
    val repository = StudyRepository(
        wordBookDao = database.wordBookDao(),
        wordEntryDao = database.wordEntryDao(),
        progressDao = database.wordProgressDao(),
        reviewLogDao = database.reviewLogDao(),
        preferencesRepository = preferencesRepository,
        importer = WordBookImporter(appContext),
    )
    val reminderScheduler = ReviewReminderScheduler(appContext)
    val speaker = WordSpeaker(appContext)
}
