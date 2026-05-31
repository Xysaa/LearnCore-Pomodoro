package com.example.pomodoro.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pomodoro.domain.model.BleConnectionStatus
import com.example.pomodoro.ui.components.*
import com.example.pomodoro.ui.theme.*
import com.example.pomodoro.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val settings  by viewModel.settings.collectAsStateWithLifecycle()
    val bleStatus by viewModel.bleConnectionStatus.collectAsStateWithLifecycle()

    var deviceNameInput by remember(settings.bleDeviceName) {
        mutableStateOf(settings.bleDeviceName)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .verticalScroll(rememberScrollState())
    ) {
        SimpleTopBar(title = "Setelan")

        Column(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // ── Timer Pomodoro ──────────────────────────────────────
            SettingsSectionCard(title = "Timer Pomodoro", icon = AppIcons.Timer) {
                SettingsStepperRow(
                    label       = "Durasi Fokus",
                    value       = settings.focusDurationMinutes,
                    onDecrement = { viewModel.updateFocusDuration(settings.focusDurationMinutes - 5) },
                    onIncrement = { viewModel.updateFocusDuration(settings.focusDurationMinutes + 5) }
                )
                HorizontalDivider(color = BorderGreen, modifier = Modifier.padding(vertical = 6.dp))
                SettingsStepperRow(
                    label       = "Istirahat Pendek",
                    value       = settings.shortBreakMinutes,
                    onDecrement = { viewModel.updateShortBreak(settings.shortBreakMinutes - 1) },
                    onIncrement = { viewModel.updateShortBreak(settings.shortBreakMinutes + 1) }
                )
                HorizontalDivider(color = BorderGreen, modifier = Modifier.padding(vertical = 6.dp))
                SettingsStepperRow(
                    label       = "Istirahat Panjang",
                    value       = settings.longBreakMinutes,
                    onDecrement = { viewModel.updateLongBreak(settings.longBreakMinutes - 5) },
                    onIncrement = { viewModel.updateLongBreak(settings.longBreakMinutes + 5) }
                )
                HorizontalDivider(color = BorderGreen, modifier = Modifier.padding(vertical = 6.dp))
                SettingsStepperRow(
                    label       = "Sesi sebelum istirahat panjang",
                    value       = settings.sessionsBeforeLongBreak,
                    unit        = "x",
                    onDecrement = { viewModel.updateSessionsBeforeLong(settings.sessionsBeforeLongBreak - 1) },
                    onIncrement = { viewModel.updateSessionsBeforeLong(settings.sessionsBeforeLongBreak + 1) }
                )
                HorizontalDivider(color = BorderGreen, modifier = Modifier.padding(vertical = 6.dp))
                SettingsStepperRow(
                    label       = "Volume Audio ESP32",
                    value       = settings.volume,
                    unit        = "",
                    onDecrement = { viewModel.updateVolume(settings.volume - 1) },
                    onIncrement = { viewModel.updateVolume(settings.volume + 1) }
                )
                Spacer(Modifier.height(8.dp))
                SecondaryButton(
                    text    = "Kirim Config ke ESP32",
                    onClick = { viewModel.pushConfigToEsp() }
                )
                Text(
                    text  = "Perubahan dikirim ke ESP32 saat tombol ditekan atau saat mulai timer",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // ── Koneksi BLE ─────────────────────────────────────────
            SettingsSectionCard(title = "Koneksi BLE", icon = AppIcons.BleConnected) {
                // Nama device
                Column {
                    Text(text = "Nama Perangkat BLE", color = TextSecondary, fontSize = 12.sp)
                    Spacer(Modifier.height(4.dp))
                    BasicTextField(
                        value         = deviceNameInput,
                        onValueChange = { deviceNameInput = it },
                        textStyle     = TextStyle(color = TextPrimary, fontSize = 14.sp),
                        singleLine    = true,
                        decorationBox = { inner ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(BackgroundDark, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                            ) {
                                if (deviceNameInput.isEmpty()) {
                                    Text("LEARNCORE-POMO", color = TextSecondary, fontSize = 14.sp)
                                }
                                inner()
                            }
                        }
                    )
                    Spacer(Modifier.height(8.dp))
                    SecondaryButton(
                        text    = "Simpan Nama",
                        onClick = { viewModel.updateDeviceName(deviceNameInput) }
                    )
                }

                HorizontalDivider(color = BorderGreen, modifier = Modifier.padding(vertical = 10.dp))

                // Status koneksi dengan icon vektor
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val (icon, label, color) = when (bleStatus) {
                        BleConnectionStatus.CONNECTED    ->
                            Triple(AppIcons.BleConnected,    "Terhubung",        AccentFocus)
                        BleConnectionStatus.CONNECTING   ->
                            Triple(AppIcons.BleConnected,    "Menghubungkan...", AccentBreak)
                        BleConnectionStatus.SCANNING     ->
                            Triple(AppIcons.BleScanning,     "Mencari...",       AccentWarning)
                        BleConnectionStatus.ERROR        ->
                            Triple(AppIcons.Error,           "Error",            AccentDanger)
                        BleConnectionStatus.DISCONNECTED ->
                            Triple(AppIcons.BleDisconnected, "Tidak Terhubung",  TextSecondary)
                    }
                    Icon(
                        imageVector        = icon,
                        contentDescription = null,
                        tint               = color,
                        modifier           = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text     = "Status: $label",
                        color    = color,
                        fontSize = 13.sp,
                        modifier = Modifier.weight(1f)
                    )
                    // Pulsing dot indicator
                    BleConnectionIndicator(
                        isConnected = bleStatus == BleConnectionStatus.CONNECTED
                    )
                }
                Spacer(Modifier.height(8.dp))
                if (bleStatus == BleConnectionStatus.DISCONNECTED ||
                    bleStatus == BleConnectionStatus.ERROR) {
                    PrimaryButton(
                        text     = "Hubungkan",
                        onClick  = { viewModel.connectBle() },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else if (bleStatus == BleConnectionStatus.CONNECTED) {
                    SecondaryButton(
                        text     = "Putuskan",
                        onClick  = { viewModel.disconnectBle() },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    // Scanning / Connecting
                    SecondaryButton(
                        text     = "Mencari perangkat...",
                        onClick  = { },
                        enabled  = false,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text  = "Device: LEARNCORE-POMO",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }

            // ── Notifikasi ──────────────────────────────────────────
            SettingsSectionCard(title = "Notifikasi", icon = AppIcons.Notification) {
                SettingsToggleRow(
                    label           = "Sesi Selesai",
                    checked         = settings.notifySessionDone,
                    onCheckedChange = { viewModel.toggleNotifySession(it) }
                )
                HorizontalDivider(color = BorderGreen, modifier = Modifier.padding(vertical = 4.dp))
                SettingsToggleRow(
                    label           = "Kualitas Udara Buruk",
                    checked         = settings.notifyAirQuality,
                    onCheckedChange = { viewModel.toggleNotifyAir(it) }
                )
                HorizontalDivider(color = BorderGreen, modifier = Modifier.padding(vertical = 4.dp))
                SettingsToggleRow(
                    label           = "Auto-detect Kehadiran",
                    checked         = settings.notifyAutoDetect,
                    onCheckedChange = { viewModel.toggleNotifyAutoDetect(it) }
                )
                HorizontalDivider(color = BorderGreen, modifier = Modifier.padding(vertical = 4.dp))
                SettingsToggleRow(
                    label           = "Suara Notifikasi",
                    checked         = settings.soundEnabled,
                    onCheckedChange = { viewModel.toggleSound(it) }
                )
            }

            // ── Aksesibilitas ───────────────────────────────────────
            SettingsSectionCard(title = "Aksesibilitas", icon = AppIcons.Accessibility) {
                SettingsToggleRow(
                    label           = "Kurangi Animasi",
                    checked         = settings.reducedMotion,
                    onCheckedChange = { viewModel.toggleReducedMotion(it) }
                )
                HorizontalDivider(color = BorderGreen, modifier = Modifier.padding(vertical = 4.dp))
                SettingsToggleRow(
                    label           = "Mode Color Blind",
                    checked         = settings.colorBlindMode,
                    onCheckedChange = { viewModel.toggleColorBlind(it) }
                )
            }

            // ── App Info ────────────────────────────────────────────
            FocusSenseCard {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier            = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector        = AppIcons.TimerFilled,
                        contentDescription = null,
                        tint               = AccentFocus,
                        modifier           = Modifier.size(36.dp)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text       = "FocusSense",
                        style      = MaterialTheme.typography.headlineSmall,
                        color      = AccentFocus,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text  = "v2.0 · LearnCore Pomodoro",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text  = "HC-SR04 · MQ-135 · DFPlayer Mini · BLE",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}
