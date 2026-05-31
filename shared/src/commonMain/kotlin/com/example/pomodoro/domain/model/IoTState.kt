package com.example.pomodoro.domain.model

enum class BleConnectionStatus {
    DISCONNECTED,
    SCANNING,
    CONNECTING,
    CONNECTED,
    ERROR
}

enum class PresenceStatus {
    PRESENT,   // user terdeteksi oleh HC-SR04
    ABSENT,    // user tidak terdeteksi
    UNKNOWN    // belum ada data dari ESP32
}

/**
 * State IoT yang disinkronkan dari ESP32 via BLE.
 *
 * Berisi:
 *  - Status koneksi BLE
 *  - Data sensor HC-SR04 (distanceCm → PresenceStatus)
 *  - Data sensor MQ-135 (adc → AirQualityLevel)
 *  - State timer Pomodoro yang dijalankan di ESP32
 *  - pausedBySensor: true jika timer dijeda otomatis oleh sensor HC-SR04
 *  - lastMsg: pesan teks terakhir dari ESP32 (type:"msg")
 */
data class IoTState(
    // ── Koneksi BLE ──────────────────────────────────────────
    val bleStatus: BleConnectionStatus = BleConnectionStatus.DISCONNECTED,
    val deviceName: String = "",
    val deviceAddress: String = "",

    // ── HC-SR04 ───────────────────────────────────────────────
    val distanceCm: Float = -1f,
    val presenceStatus: PresenceStatus = PresenceStatus.UNKNOWN,

    // ── MQ-135 ────────────────────────────────────────────────
    val adc: Int = 0,                  // ADC raw (0–4095)
    val airBad: Boolean = false,

    // ── Timer state dari ESP32 ────────────────────────────────
    val espRunning: Boolean = false,
    val espPaused: Boolean = false,
    val espPausedBySensor: Boolean = false,   // dijeda otomatis oleh HC-SR04
    val espPhase: EspPhase = EspPhase.IDLE,
    val espSecsLeft: Int = 0,
    val espSess: Int = 1,
    val espCycles: Int = 4,
    val espVolume: Int = 20,
    val dfOk: Boolean = true,   // default true — jika audio sudah terbukti jalan

    // ── Pesan teks dari ESP32 (type:"msg") ────────────────────
    val lastMsg: String = "",

    // ── Metadata ──────────────────────────────────────────────
    val lastUpdated: Long = 0L,
    val rawJson: String = "",

    // ── Debug log (hanya untuk development) ───────────────────
    val debugLog: String = "Belum scan"
) {
    val isConnected: Boolean
        get() = bleStatus == BleConnectionStatus.CONNECTED

    // Label badge koneksi
    val connectionLabel: String
        get() = when (bleStatus) {
            BleConnectionStatus.DISCONNECTED -> "IoT Tidak Terhubung"
            BleConnectionStatus.SCANNING     -> "Mencari Perangkat..."
            BleConnectionStatus.CONNECTING   -> "Menghubungkan..."
            BleConnectionStatus.CONNECTED    -> when (presenceStatus) {
                PresenceStatus.PRESENT -> "Kamu Terdeteksi"
                PresenceStatus.ABSENT  -> "Tidak Ada Aktivitas"
                PresenceStatus.UNKNOWN -> "Terhubung"
            }
            BleConnectionStatus.ERROR -> "Koneksi Error"
        }

    // Label kehadiran
    val presenceLabel: String
        get() = when (presenceStatus) {
            PresenceStatus.PRESENT -> "Kamu Terdeteksi"
            PresenceStatus.ABSENT  -> "Tidak Ada Aktivitas"
            PresenceStatus.UNKNOWN -> "Menunggu Data..."
        }

    // Label fase ESP32
    val espPhaseLabel: String
        get() = when (espPhase) {
            EspPhase.FOCUS      -> "Sesi Fokus"
            EspPhase.SHORT_REST -> "Istirahat Pendek"
            EspPhase.LONG_REST  -> "Istirahat Panjang"
            EspPhase.IDLE       -> "Siap"
        }

    // Waktu tersisa terformat MM:SS
    val espFormattedTime: String
        get() {
            val m = espSecsLeft / 60
            val s = espSecsLeft % 60
            return "%02d:%02d".format(m, s)
        }

    // Progress ring 0f..1f berdasarkan secsLeft vs total fase
    fun espProgress(totalSecs: Int): Float {
        if (totalSecs <= 0) return 0f
        return 1f - (espSecsLeft.toFloat() / totalSecs.toFloat())
    }
}
