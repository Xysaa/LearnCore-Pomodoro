package com.example.pomodoro.domain.model

enum class AirQualityLevel {
    GOOD,       // ADC < 2500
    HAZARDOUS   // ADC >= 2500
}

/**
 * Data kualitas udara dari MQ-135.
 * ESP32 mengirim nilai ADC raw (0–4095).
 *   ADC < 2500  → Baik
 *   ADC >= 2500 → Buruk
 */
data class AirQualityData(
    val adc: Int = 0,
    val timestamp: Long = 0L
) {
    val level: AirQualityLevel
        get() = if (adc < 2500) AirQualityLevel.GOOD else AirQualityLevel.HAZARDOUS

    val levelLabel: String
        get() = when (level) {
            AirQualityLevel.GOOD      -> "Baik"
            AirQualityLevel.HAZARDOUS -> "Buruk"
        }

    val levelEmoji: String
        get() = when (level) {
            AirQualityLevel.GOOD      -> "✅"
            AirQualityLevel.HAZARDOUS -> "🔴"
        }

    val recommendation: String
        get() = when (level) {
            AirQualityLevel.GOOD      -> "Kondisi ruangan ideal untuk fokus."
            AirQualityLevel.HAZARDOUS -> "Kualitas udara buruk. Buka jendela atau ventilasi ruangan."
        }

    /** Progress bar 0f..1f untuk visualisasi ADC (max 4095) */
    val adcProgress: Float
        get() = (adc.toFloat() / 4095f).coerceIn(0f, 1f)
}
