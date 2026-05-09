package com.study.englishdemo.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface UserPreferencesProvider {
    val settings: Flow<SettingsState>
    suspend fun updateDailyTarget(value: Int)
    suspend fun updateReminderSettings(enabled: Boolean, hour: Int, minute: Int)
}

private val Context.preferencesDataStore by preferencesDataStore(name = "lexrise_settings")

class UserPreferencesRepository(private val context: Context) : UserPreferencesProvider {
    private val dailyNewWordTargetKey = intPreferencesKey("daily_new_word_target")
    private val reminderEnabledKey = booleanPreferencesKey("review_reminder_enabled")
    private val reminderHourKey = intPreferencesKey("review_reminder_hour")
    private val reminderMinuteKey = intPreferencesKey("review_reminder_minute")

    override val settings: Flow<SettingsState> = context.preferencesDataStore.data.map { prefs ->
        SettingsState(
            dailyNewWordTarget = prefs[dailyNewWordTargetKey] ?: 20,
            reviewReminderEnabled = prefs[reminderEnabledKey] ?: true,
            reminderHour = prefs[reminderHourKey] ?: 20,
            reminderMinute = prefs[reminderMinuteKey] ?: 30,
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
}
