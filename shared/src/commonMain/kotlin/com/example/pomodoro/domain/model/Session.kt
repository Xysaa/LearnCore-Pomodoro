package com.example.pomodoro.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Session(
    val id: String,
    val phase: String,           // "FOCUS" | "SHORT_BREAK" | "LONG_BREAK"
    val startTime: Long,
    val endTime: Long,
    val durationSeconds: Int,
    val completed: Boolean,
    val autoStarted: Boolean = false   // true jika dimulai oleh IoT
) {
    val durationMinutes: Int get() = durationSeconds / 60

    val formattedStartTime: String
        get() {
            val totalMinutes = (startTime / 60000) % (24 * 60)
            val hours = totalMinutes / 60
            val minutes = totalMinutes % 60
            return "%02d:%02d".format(hours, minutes)
        }

    val formattedEndTime: String
        get() {
            val totalMinutes = (endTime / 60000) % (24 * 60)
            val hours = totalMinutes / 60
            val minutes = totalMinutes % 60
            return "%02d:%02d".format(hours, minutes)
        }
}

@Serializable
data class DailyStats(
    val dateEpochDay: Long,
    val totalFocusMinutes: Int,
    val totalSessions: Int,
    val completedSessions: Int
)
