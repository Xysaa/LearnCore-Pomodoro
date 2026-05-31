package com.example.pomodoro

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.pomodoro.ui.components.BottomNavBar
import com.example.pomodoro.ui.components.NavTab
import com.example.pomodoro.ui.screens.*
import com.example.pomodoro.ui.theme.*
import com.example.pomodoro.viewmodel.SnackbarEvent
import com.example.pomodoro.viewmodel.ViewModelContainer

@Composable
fun App() {
    FocusSenseTheme {
        var selectedTab by remember { mutableStateOf(NavTab.FOCUS) }

        val pomodoroVm   = remember { ViewModelContainer.pomodoroViewModel }
        val statisticsVm = remember { ViewModelContainer.statisticsViewModel }
        val airQualityVm = remember { ViewModelContainer.airQualityViewModel }
        val settingsVm   = remember { ViewModelContainer.settingsViewModel }

        // ── Scan BLE diinisiasi dari MainActivity setelah permission granted.
        //    TIDAK ada LaunchedEffect scan di sini agar tidak race condition
        //    dengan permission dialog Android.

        // ── Snackbar host ─────────────────────────────────────────
        val snackbarHostState = remember { SnackbarHostState() }

        LaunchedEffect(pomodoroVm) {
            pomodoroVm.snackbarMessage.collect { event ->
                val message = when (event) {
                    is SnackbarEvent.Info    -> event.message
                    is SnackbarEvent.Success -> event.message
                    is SnackbarEvent.Warning -> event.message
                    is SnackbarEvent.Error   -> event.message
                }
                snackbarHostState.showSnackbar(message)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundDark)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.systemBars)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    when (selectedTab) {
                        NavTab.FOCUS       -> FocusScreen(
                            viewModel = pomodoroVm,
                            modifier  = Modifier.fillMaxSize()
                        )
                        NavTab.STATISTICS  -> StatisticsScreen(
                            viewModel = statisticsVm,
                            modifier  = Modifier.fillMaxSize()
                        )
                        NavTab.AIR_QUALITY -> AirQualityScreen(
                            viewModel = airQualityVm,
                            modifier  = Modifier.fillMaxSize()
                        )
                        NavTab.SETTINGS    -> SettingsScreen(
                            viewModel = settingsVm,
                            modifier  = Modifier.fillMaxSize()
                        )
                    }
                }

                BottomNavBar(
                    selectedTab   = selectedTab,
                    onTabSelected = { selectedTab = it }
                )
            }

            // Snackbar muncul dari bawah, di atas bottom nav
            SnackbarHost(
                hostState = snackbarHostState,
                modifier  = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 72.dp, start = 16.dp, end = 16.dp)
            ) { data ->
                Snackbar(
                    snackbarData   = data,
                    shape          = RoundedCornerShape(12.dp),
                    containerColor = SurfaceDark,
                    contentColor   = TextPrimary,
                    actionColor    = AccentFocus
                )
            }
        }
    }
}
