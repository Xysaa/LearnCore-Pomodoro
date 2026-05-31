package com.example.pomodoro.repository

import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.example.pomodoro.domain.model.AppSettings
import com.example.pomodoro.domain.model.DailyStats
import com.example.pomodoro.domain.model.Session
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val KEY_SESSIONS  = stringPreferencesKey("sessions")
private val KEY_SETTINGS  = stringPreferencesKey("app_settings")

class SessionRepository(private val dataStore: DataStore<Preferences>) {

    private val json = Json { ignoreUnknownKeys = true }

    // ── Sessions ───────────────────────────────────────────────────────────
    val sessions: Flow<List<Session>> = dataStore.data.map { prefs ->
        val raw = prefs[KEY_SESSIONS] ?: return@map emptyList()
        try { json.decodeFromString<List<Session>>(raw) } catch (e: Exception) { emptyList() }
    }

    suspend fun saveSession(session: Session) {
        dataStore.edit { prefs ->
            val current = try {
                json.decodeFromString<List<Session>>(prefs[KEY_SESSIONS] ?: "[]")
            } catch (e: Exception) { emptyList() }
            val updated = (current + session).takeLast(500) // simpan maks 500 sesi
            prefs[KEY_SESSIONS] = json.encodeToString(updated)
        }
    }

    suspend fun clearSessions() {
        dataStore.edit { it.remove(KEY_SESSIONS) }
    }

    // ── Stats ──────────────────────────────────────────────────────────────
    fun getDailyStats(sessions: List<Session>): List<DailyStats> {
        return sessions
            .filter { it.completed && it.phase == "FOCUS" }
            .groupBy { epochDayFromMillis(it.startTime) }
            .map { (day, daySessions) ->
                DailyStats(
                    dateEpochDay      = day,
                    totalFocusMinutes = daySessions.sumOf { it.durationMinutes },
                    totalSessions     = daySessions.size,
                    completedSessions = daySessions.count { it.completed }
                )
            }
            .sortedBy { it.dateEpochDay }
    }

    fun getStreak(dailyStats: List<DailyStats>): Int {
        if (dailyStats.isEmpty()) return 0
        val today = epochDayFromMillis(currentTimeMillis())
        var streak = 0
        var checkDay = today
        val statsByDay = dailyStats.associateBy { it.dateEpochDay }
        while (statsByDay.containsKey(checkDay)) {
            streak++
            checkDay--
        }
        return streak
    }

    // ── Settings ───────────────────────────────────────────────────────────
    val settings: Flow<AppSettings> = dataStore.data.map { prefs ->
        val raw = prefs[KEY_SETTINGS] ?: return@map AppSettings()
        try { json.decodeFromString<AppSettings>(raw) } catch (e: Exception) { AppSettings() }
    }

    suspend fun saveSettings(settings: AppSettings) {
        dataStore.edit { prefs ->
            prefs[KEY_SETTINGS] = json.encodeToString(settings)
        }
    }

    // ── Helpers ────────────────────────────────────────────────────────────
    private fun epochDayFromMillis(millis: Long): Long = millis / 86_400_000L
}

expect fun currentTimeMillis(): Long
