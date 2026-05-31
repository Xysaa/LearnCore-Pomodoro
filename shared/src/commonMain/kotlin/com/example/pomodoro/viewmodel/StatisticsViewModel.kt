package com.example.pomodoro.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pomodoro.domain.model.DailyStats
import com.example.pomodoro.domain.model.Session
import com.example.pomodoro.repository.SessionRepository
import com.example.pomodoro.repository.currentTimeMillis
import kotlinx.coroutines.flow.*

data class StatisticsUiState(
    val sessions: List<Session> = emptyList(),
    val dailyStats: List<DailyStats> = emptyList(),
    val weeklyStats: List<DailyStats> = emptyList(),
    val totalFocusMinutesWeek: Int = 0,
    val totalSessionsWeek: Int = 0,
    val streak: Int = 0,
    val todaySessions: List<Session> = emptyList()
)

class StatisticsViewModel(
    private val repository: SessionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    init {
        repository.sessions
            .onEach { sessions -> computeStats(sessions) }
            .launchIn(viewModelScope)
    }

    private fun computeStats(sessions: List<Session>) {
        val allDaily   = repository.getDailyStats(sessions)
        val streak     = repository.getStreak(allDaily)
        val todayEpoch = currentTimeMillis() / 86_400_000L
        val weekAgo    = todayEpoch - 6

        val weekly = allDaily.filter { it.dateEpochDay in weekAgo..todayEpoch }

        // Pad missing days in last 7 days with zeros
        val paddedWeekly = (0..6).map { offset ->
            val day = weekAgo + offset
            weekly.find { it.dateEpochDay == day }
                ?: DailyStats(
                    dateEpochDay      = day,
                    totalFocusMinutes = 0,
                    totalSessions     = 0,
                    completedSessions = 0
                )
        }

        val todaySessions = sessions.filter { s ->
            s.startTime / 86_400_000L == todayEpoch && s.phase == "FOCUS"
        }.sortedByDescending { it.startTime }

        _uiState.value = StatisticsUiState(
            sessions               = sessions,
            dailyStats             = allDaily,
            weeklyStats            = paddedWeekly,
            totalFocusMinutesWeek  = paddedWeekly.sumOf { it.totalFocusMinutes },
            totalSessionsWeek      = paddedWeekly.sumOf { it.completedSessions },
            streak                 = streak,
            todaySessions          = todaySessions
        )
    }
}
