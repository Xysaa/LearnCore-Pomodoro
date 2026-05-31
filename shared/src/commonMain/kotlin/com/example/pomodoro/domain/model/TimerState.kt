package com.example.pomodoro.domain.model

enum class TimerPhase {
    FOCUS,
    SHORT_BREAK,
    LONG_BREAK
}

enum class TimerStatus {
    IDLE,
    RUNNING,
    PAUSED,
    FINISHED
}

data class TimerState(
    val phase: TimerPhase = TimerPhase.FOCUS,
    val status: TimerStatus = TimerStatus.IDLE,
    val remainingSeconds: Int = 25 * 60,
    val totalSeconds: Int = 25 * 60,
    val currentSession: Int = 1,
    val totalSessions: Int = 4,
    val completedSessionsToday: Int = 0
) {
    val progress: Float
        get() = if (totalSeconds == 0) 0f
                else 1f - (remainingSeconds.toFloat() / totalSeconds.toFloat())

    val formattedTime: String
        get() {
            val minutes = remainingSeconds / 60
            val seconds = remainingSeconds % 60
            return "%02d:%02d".format(minutes, seconds)
        }
}
