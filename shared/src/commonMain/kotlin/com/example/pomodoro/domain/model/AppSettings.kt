package com.example.pomodoro.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class AppSettings(
    // ── Timer (dikirim ke ESP32 via BLE) ──────────────────────
    val focusDurationMinutes: Int = 25,
    val shortBreakMinutes: Int = 5,
    val longBreakMinutes: Int = 15,
    val sessionsBeforeLongBreak: Int = 4,
    val volume: Int = 20,

    // ── BLE ───────────────────────────────────────────────────
    // Nama device sesuai BLEDevice::init("LEARNCORE-POMO") di firmware
    val bleDeviceName: String = "LEARNCORE-POMO",

    // ── Notifikasi ────────────────────────────────────────────
    val notifySessionDone: Boolean = true,
    val notifyAirQuality: Boolean = true,
    val notifyAutoDetect: Boolean = true,
    val soundEnabled: Boolean = true,

    // ── Aksesibilitas ─────────────────────────────────────────
    val reducedMotion: Boolean = false,
    val colorBlindMode: Boolean = false
)
