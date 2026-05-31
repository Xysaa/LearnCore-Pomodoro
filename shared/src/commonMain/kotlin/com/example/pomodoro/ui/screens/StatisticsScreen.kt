package com.example.pomodoro.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pomodoro.ui.components.*
import com.example.pomodoro.ui.theme.*
import com.example.pomodoro.viewmodel.StatisticsViewModel

@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val totalHours   = uiState.totalFocusMinutesWeek / 60
    val totalMinutes = uiState.totalFocusMinutesWeek % 60
    val totalTimeStr = "${totalHours}j ${totalMinutes}m"

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .verticalScroll(rememberScrollState())
    ) {
        SimpleTopBar(title = "Statistik", trailing = "Minggu Ini")

        Spacer(Modifier.height(8.dp))

        // ── Summary cards ──────────────────────────────────────────────────
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            StatCard(
                label       = "Total Fokus",
                value       = totalTimeStr,
                accentColor = AccentFocus,
                modifier    = Modifier.weight(1f)
            )
            StatCard(
                label       = "Total Sesi",
                value       = "${uiState.totalSessionsWeek}",
                accentColor = AccentBreak,
                modifier    = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(20.dp))

        // ── Bar chart ──────────────────────────────────────────────────────
        FocusSenseCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Text(
                text       = "Grafik Harian",
                style      = MaterialTheme.typography.headlineSmall,
                color      = TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(16.dp))
            WeeklyBarChart(
                stats    = uiState.weeklyStats,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(20.dp))

        // ── Streak ─────────────────────────────────────────────────────────
        FocusSenseCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector        = AppIcons.Flame,
                    contentDescription = "Streak",
                    tint               = AccentWarning,
                    modifier           = Modifier.size(28.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text       = "Streak: ${uiState.streak} hari",
                        style      = MaterialTheme.typography.headlineSmall,
                        color      = AccentWarning,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text  = if (uiState.streak == 0) "Mulai hari ini!"
                                else "Pertahankan streakmu!",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // ── Today's Session History ────────────────────────────────────────
        FocusSenseCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector        = AppIcons.History,
                    contentDescription = "Riwayat",
                    tint               = AccentFocus,
                    modifier           = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text       = "Riwayat Sesi Hari Ini",
                    style      = MaterialTheme.typography.headlineSmall,
                    color      = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(Modifier.height(8.dp))

            if (uiState.todaySessions.isEmpty()) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector        = AppIcons.TimerFilled,
                            contentDescription = null,
                            tint               = TextSecondary,
                            modifier           = Modifier.size(32.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text      = "Belum ada sesi hari ini.\nMulai fokus sekarang!",
                            style     = MaterialTheme.typography.bodyMedium,
                            color     = TextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                uiState.todaySessions.forEachIndexed { index, session ->
                    SessionHistoryItem(
                        startTime   = session.formattedStartTime,
                        endTime     = session.formattedEndTime,
                        durationMin = session.durationMinutes,
                        completed   = session.completed
                    )
                    if (index < uiState.todaySessions.lastIndex) {
                        HorizontalDivider(
                            color    = BorderGreen,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))
    }
}
