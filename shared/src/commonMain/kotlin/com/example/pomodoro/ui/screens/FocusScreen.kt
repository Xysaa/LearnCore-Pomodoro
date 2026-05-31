package com.example.pomodoro.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pomodoro.domain.model.*
import com.example.pomodoro.ui.components.*
import com.example.pomodoro.ui.theme.*
import com.example.pomodoro.viewmodel.PomodoroViewModel
import kotlinx.coroutines.delay

@Composable
fun FocusScreen(
    viewModel: PomodoroViewModel,
    modifier: Modifier = Modifier
) {
    val iotState   by viewModel.iotState.collectAsStateWithLifecycle()
    val airQuality by viewModel.airQuality.collectAsStateWithLifecycle()
    val settings   by viewModel.settings.collectAsStateWithLifecycle()

    val espRunning        = iotState.espRunning
    val espPaused         = iotState.espPaused
    val espPausedBySensor = iotState.espPausedBySensor
    val espPhase          = iotState.espPhase
    val espSecs           = iotState.espSecsLeft
    val espSess           = iotState.espSess
    val espCycles         = iotState.espCycles
    val isConnected       = iotState.isConnected

    // ── Local countdown ticker ────────────────────────────────
    // ESP32 kirim heartbeat setiap ~5 detik. Agar timer tampak smooth,
    // app countdown secara lokal tiap 1 detik. Setiap kali espSecs berubah
    // (dari heartbeat), nilai lokal di-sync ulang.
    var localSecs by remember { mutableIntStateOf(espSecs) }

    // Sync dari ESP32 kapanpun espSecs berubah
    LaunchedEffect(espSecs) {
        localSecs = espSecs
    }

    // Local ticker — hanya jalan saat timer aktif dan tidak paused
    LaunchedEffect(espRunning, espPaused, espPhase) {
        while (espRunning && !espPaused && espPhase != EspPhase.IDLE) {
            delay(1_000L)
            if (localSecs > 0) localSecs--
        }
    }

    val totalSecs = when (espPhase) {
        EspPhase.FOCUS      -> settings.focusDurationMinutes * 60
        EspPhase.SHORT_REST -> settings.shortBreakMinutes * 60
        EspPhase.LONG_REST  -> settings.longBreakMinutes * 60
        EspPhase.IDLE       -> settings.focusDurationMinutes * 60
    }

    val ringPhase = when (espPhase) {
        EspPhase.FOCUS -> TimerPhase.FOCUS
        else           -> TimerPhase.SHORT_BREAK
    }

    // Pilih nilai untuk display:
    // - IDLE: tampilkan durasi setting (bukan 00:00)
    // - Running/Paused: tampilkan localSecs (smooth countdown)
    val displaySecs = when {
        !espRunning && !espPaused && espPhase == EspPhase.IDLE -> settings.focusDurationMinutes * 60
        else -> localSecs
    }
    val displayProgress = when {
        !espRunning && !espPaused && espPhase == EspPhase.IDLE -> 0f
        totalSecs > 0 -> 1f - (displaySecs.toFloat() / totalSecs.toFloat())
        else -> 0f
    }
    val displayTime = "%02d:%02d".format(displaySecs / 60, displaySecs % 60)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .verticalScroll(rememberScrollState())
    ) {
        // ── Top Bar ──────────────────────────────────────────────
        FocusSenseTopBar(
            title       = "FocusSense",
            isConnected = isConnected,
            actions     = {
                Icon(
                    imageVector        = AppIcons.Notification,
                    contentDescription = "Notifikasi",
                    tint               = TextSecondary,
                    modifier           = Modifier.size(20.dp)
                )
            }
        )

        // ── Koneksi BLE banner (jika belum terhubung) ────────────
        AnimatedVisibility(
            visible = !isConnected,
            enter   = fadeIn() + expandVertically(),
            exit    = fadeOut() + shrinkVertically()
        ) {
            BleConnectBanner(
                bleStatus = iotState.bleStatus,
                onConnect = { viewModel.startBleConnection() },
                modifier  = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 8.dp)
            )
        }

        // ── Phase & session label ─────────────────────────────────
        Text(
            text     = "${iotState.espPhaseLabel}  ·  Sesi $espSess dari $espCycles",
            style    = MaterialTheme.typography.bodyMedium,
            color    = TextSecondary,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(Modifier.height(24.dp))

        // ── Progress Ring ─────────────────────────────────────────
        ProgressRing(
            progress      = displayProgress,
            timerText     = displayTime,
            phase         = ringPhase,
            reducedMotion = settings.reducedMotion,
            ringSize      = 240.dp
        )

        Spacer(Modifier.height(8.dp))

        // ── Status timer ──────────────────────────────────────────
        val statusLabel = when {
            !isConnected              -> "Hubungkan ESP32"
            espPausedBySensor         -> "Dijeda sensor — kembalilah ke depan sensor"
            espPaused                 -> "Dijeda"
            espRunning                -> "Berjalan"
            espPhase == EspPhase.IDLE -> "Siap"
            else                      -> ""
        }
        val statusColor = when {
            !isConnected      -> TextSecondary
            espPausedBySensor -> AccentWarning
            espPaused         -> AccentWarning
            espRunning        -> AccentFocus
            else              -> TextSecondary
        }
        if (statusLabel.isNotEmpty()) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier              = Modifier.padding(horizontal = 24.dp)
            ) {
                val statusIcon: ImageVector? = when {
                    espPausedBySensor -> AppIcons.Sensor
                    espPaused         -> AppIcons.Pause
                    espRunning        -> AppIcons.Play
                    !isConnected      -> AppIcons.BleDisconnected
                    else              -> null
                }
                statusIcon?.let {
                    Icon(
                        imageVector        = it,
                        contentDescription = null,
                        tint               = statusColor,
                        modifier           = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                }
                Text(
                    text  = statusLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = statusColor
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── IoT Presence Badge ────────────────────────────────────
        IoTStatusBadge(
            iotState      = iotState,
            reducedMotion = settings.reducedMotion
        )

        Spacer(Modifier.height(20.dp))

        // ── Timer Controls ────────────────────────────────────────
        TimerControls(
            isConnected       = isConnected,
            espRunning        = espRunning,
            espPaused         = espPaused,
            espPausedBySensor = espPausedBySensor,
            espPhase          = espPhase,
            onConnect         = { viewModel.startBleConnection() },
            onStart           = { viewModel.sendStart() },
            onPause           = { viewModel.sendPause() },
            onResume          = { viewModel.sendResume() },
            onReset           = { viewModel.sendReset() },
            modifier          = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(Modifier.height(24.dp))

        // ── Sensor Summary Cards ──────────────────────────────────
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            // HC-SR04
            val presColor = when (iotState.presenceStatus) {
                PresenceStatus.PRESENT -> AccentFocus
                PresenceStatus.ABSENT  -> TextSecondary
                PresenceStatus.UNKNOWN -> BorderGreen
            }
            val presIcon = when (iotState.presenceStatus) {
                PresenceStatus.PRESENT -> AppIcons.PersonPresent
                PresenceStatus.ABSENT  -> AppIcons.PersonAbsent
                PresenceStatus.UNKNOWN -> AppIcons.Sensor
            }
            SensorSummaryCard(
                icon        = presIcon,
                iconTint    = presColor,
                title       = "HC-SR04",
                primary     = if (iotState.distanceCm > 0) "${iotState.distanceCm.toInt()} cm" else "— cm",
                secondary   = when (iotState.presenceStatus) {
                    PresenceStatus.PRESENT -> "Terdeteksi"
                    PresenceStatus.ABSENT  -> "Tidak Ada"
                    PresenceStatus.UNKNOWN -> "Menunggu..."
                },
                accentColor = presColor,
                modifier    = Modifier.weight(1f)
            )

            // MQ-135
            SensorSummaryCard(
                icon        = AppIcons.AirQuality,
                iconTint    = aqiColor(airQuality.level),
                title       = "MQ-135",
                primary     = "ADC ${airQuality.adc}",
                secondary   = airQuality.levelLabel,
                accentColor = aqiColor(airQuality.level),
                modifier    = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(20.dp))

        // ── Session Dots ──────────────────────────────────────────
        SessionDots(
            completedToday = (espSess - 1).coerceAtLeast(0),
            totalTarget    = espCycles,
            modifier       = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        )

        Spacer(Modifier.height(16.dp))
    }
}

// ── BLE Connect Banner ────────────────────────────────────
@Composable
private fun BleConnectBanner(
    bleStatus: BleConnectionStatus,
    onConnect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isScanning   = bleStatus == BleConnectionStatus.SCANNING ||
                       bleStatus == BleConnectionStatus.CONNECTING
    val bannerColor  = if (isScanning) AccentWarning else AccentFocus
    val bannerIcon   = when (bleStatus) {
        BleConnectionStatus.SCANNING   -> AppIcons.BleScanning
        BleConnectionStatus.CONNECTING -> AppIcons.BleConnected
        BleConnectionStatus.ERROR      -> AppIcons.Error
        else                           -> AppIcons.BleDisconnected
    }
    val bannerText   = when (bleStatus) {
        BleConnectionStatus.SCANNING   -> "Mencari LEARNCORE-POMO..."
        BleConnectionStatus.CONNECTING -> "Menghubungkan..."
        BleConnectionStatus.ERROR      -> "Gagal terhubung — coba lagi"
        else                           -> "Hubungkan ke ESP32 untuk memulai"
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(bannerColor.copy(alpha = 0.10f))
            .border(1.dp, bannerColor.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Icon(
            imageVector        = bannerIcon,
            contentDescription = null,
            tint               = bannerColor,
            modifier           = Modifier.size(22.dp)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text     = bannerText,
            style    = MaterialTheme.typography.bodyMedium,
            color    = bannerColor,
            modifier = Modifier.weight(1f)
        )
        if (!isScanning) {
            Spacer(Modifier.width(10.dp))
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(bannerColor)
                    .clickable(onClick = onConnect)
                    .padding(horizontal = 12.dp, vertical = 7.dp)
            ) {
                Text(
                    text       = "Connect",
                    color      = BackgroundDark,
                    fontSize   = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ── Timer Controls ────────────────────────────────────────
@Composable
private fun TimerControls(
    isConnected: Boolean,
    espRunning: Boolean,
    espPaused: Boolean,
    espPausedBySensor: Boolean,
    espPhase: EspPhase,
    onConnect: () -> Unit,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
            verticalAlignment     = Alignment.CenterVertically,
            modifier              = Modifier.fillMaxWidth()
        ) {
            when {
                !isConnected -> {
                    PrimaryButton(
                        text     = "Hubungkan IoT",
                        onClick  = onConnect,
                        modifier = Modifier.weight(1f)
                    )
                }
                // Dijeda oleh sensor — hanya bisa resume otomatis, tampilkan info
                espPaused && espPausedBySensor -> {
                    SecondaryButton(
                        text     = "Menunggu sensor...",
                        onClick  = { },
                        enabled  = false,
                        modifier = Modifier.weight(1f)
                    )
                }
                // IDLE — belum start, tombol Mulai selalu aktif saat connected
                !espRunning && !espPaused -> {
                    PrimaryButton(
                        text     = "Mulai",
                        onClick  = onStart,
                        modifier = Modifier.weight(1f)
                    )
                }
                // Running — bisa jeda
                espRunning && !espPaused -> {
                    SecondaryButton(
                        text     = "Jeda",
                        onClick  = onPause,
                        modifier = Modifier.weight(1f)
                    )
                }
                // Paused manual — bisa lanjut
                else -> {
                    PrimaryButton(
                        text     = "Lanjutkan",
                        onClick  = onResume,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Reset
        AnimatedVisibility(
            visible = isConnected && (espRunning || espPaused),
            enter   = fadeIn() + expandVertically(),
            exit    = fadeOut() + shrinkVertically()
        ) {
            Column {
                Spacer(Modifier.height(8.dp))
                DangerButton(text = "Reset", onClick = onReset)
            }
        }
    }
}

// ── Sensor Card kecil ─────────────────────────────────────
@Composable
private fun SensorSummaryCard(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    primary: String,
    secondary: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceDark)
            .border(1.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
            .padding(vertical = 14.dp, horizontal = 10.dp)
    ) {
        Icon(
            imageVector        = icon,
            contentDescription = null,
            tint               = iconTint,
            modifier           = Modifier.size(24.dp)
        )
        Spacer(Modifier.height(4.dp))
        Text(text = title,    style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        Spacer(Modifier.height(6.dp))
        Text(
            text       = primary,
            style      = MaterialTheme.typography.headlineSmall,
            color      = accentColor,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text  = secondary,
            style = MaterialTheme.typography.labelSmall,
            color = accentColor.copy(alpha = 0.8f)
        )
    }
}
