package com.study.englishdemo.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

interface UserPreferencesProvider {
    val settings: Flow<SettingsState>
    suspend fun updateDailyTarget(value: Int)
    suspend fun updateReminderSettings(enabled: Boolean, hour: Int, minute: Int)
    suspend fun updateExamPlan(examDate: LocalDate?, autoPaceEnabled: Boolean)
}

private val Context.preferencesDataStore by preferencesDataStore(name = "lexrise_settings")

class UserPreferencesRepository(private val context: Context) : UserPreferencesProvider {
    private val dailyNewWordTargetKey = intPreferencesKey("daily_new_word_target")
    private val reminderEnabledKey = booleanPreferencesKey("review_reminder_enabled")
    private val reminderHourKey = intPreferencesKey("review_reminder_hour")
    private val reminderMinuteKey = intPreferencesKey("review_reminder_minute")
    private val examDateKey = stringPreferencesKey("exam_date_iso")
    private val autoPaceKey = booleanPreferencesKey("auto_pace_enabled")

    override val settings: Flow<SettingsState> = context.preferencesDataStore.data.map { prefs ->
        SettingsState(
            dailyNewWordTarget = prefs[dailyNewWordTargetKey] ?: 20,
            reviewReminderEnabled = prefs[reminderEnabledKey] ?: true,
            reminderHour = prefs[reminderHourKey] ?: 20,
            reminderMinute = prefs[reminderMinuteKey] ?: 30,
            examDate = prefs[examDateKey]?.let { raw ->
                runCatching { LocalDate.parse(raw) }.getOrNull()
            },
            autoPaceEnabled = prefs[autoPaceKey] ?: false,
        )
    }

    override suspend fun updateDailyTarget(value: Int) {
        context.preferencesDataStore.edit { prefs ->
            prefs[dailyNewWordTargetKey] = value
        }
    }

    override suspend fun updateReminderSettings(enabled: Boolean, hour: Int, minute: Int) {
        context.preferencesDataStore.edit { prefs ->
            prefs[reminderEnabledKey] = enabled
            prefs[reminderHourKey] = hour.coerceIn(0, 23)
            prefs[reminderMinuteKey] = minute.coerceIn(0, 59)
        }
    }

    override suspend fun updateExamPlan(examDate: LocalDate?, autoPaceEnabled: Boolean) {
        context.preferencesDataStore.edit { prefs ->
            if (examDate == null) {
                prefs.remove(examDateKey)
            } else {
                prefs[examDateKey] = examDate.toString()
            }
            prefs[autoPaceKey] = autoPaceEnabled
        }
    }
}
