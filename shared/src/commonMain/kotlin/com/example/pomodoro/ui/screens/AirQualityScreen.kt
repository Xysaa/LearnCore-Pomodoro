package com.example.pomodoro.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pomodoro.domain.model.AirQualityLevel
import com.example.pomodoro.ui.components.*
import com.example.pomodoro.ui.theme.*
import com.example.pomodoro.viewmodel.AirQualityViewModel

@Composable
fun AirQualityScreen(
    viewModel: AirQualityViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val current = uiState.current

    val infiniteTransition = rememberInfiniteTransition(label = "live_pulse")
    val liveScale by infiniteTransition.animateFloat(
        initialValue  = 1f,
        targetValue   = 1.5f,
        animationSpec = infiniteRepeatable(
            animation  = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "live_scale"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .verticalScroll(rememberScrollState())
    ) {
        // ── Top Bar ──────────────────────────────────────────────
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            Icon(
                imageVector        = AppIcons.AirQuality,
                contentDescription = null,
                tint               = AccentFocus,
                modifier           = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text       = "Kualitas Udara",
                style      = MaterialTheme.typography.headlineMedium,
                color      = TextPrimary,
                fontWeight = FontWeight.Bold,
                modifier   = Modifier.weight(1f)
            )
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    text     = "Live",
                    color    = if (uiState.isLive) AccentFocus else TextSecondary,
                    fontSize = 13.sp
                )
                Box(modifier = Modifier.size(8.dp).scale(if (uiState.isLive) liveScale else 1f)) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(color = if (uiState.isLive) AqiGood else StatusAbsent)
                    }
                }
            }
        }

        // ── AQI Big Display ───────────────────────────────────────
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp)
        ) {
            AQIBigDisplay(airQuality = current)
        }

        // ── Sensor MQ-135 Info ────────────────────────────────────
        FocusSenseCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector        = AppIcons.Sensor,
                    contentDescription = null,
                    tint               = AccentFocus,
                    modifier           = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text       = "Sensor MQ-135",
                    style      = MaterialTheme.typography.headlineSmall,
                    color      = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(Modifier.height(14.dp))

            // ADC value row
            SensorInfoRow(
                icon       = AppIcons.AirQuality,
                iconTint   = aqiColor(current.level),
                label      = "Udara (MQ-135)",
                value      = "ADC: ${current.adc}",
                valueColor = aqiColor(current.level)
            )
            Spacer(Modifier.height(10.dp))

            // Progress bar ADC
            Text(
                text  = "Tingkat polutan  (< 2500 = Baik)",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
            Spacer(Modifier.height(4.dp))
            LinearProgressIndicator(
                progress   = { current.adcProgress },
                modifier   = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color      = aqiColor(current.level),
                trackColor = BorderGreen,
                strokeCap  = StrokeCap.Round
            )
            Spacer(Modifier.height(4.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("0",    color = TextSecondary, fontSize = 10.sp)
                Text("4095", color = TextSecondary, fontSize = 10.sp)
            }

            Spacer(Modifier.height(10.dp))

            SensorInfoRow(
                icon       = AppIcons.Chart,
                iconTint   = TextSecondary,
                label      = "Status",
                value      = current.levelLabel,
                valueColor = aqiColor(current.level)
            )
        }

        Spacer(Modifier.height(20.dp))

        // ── Tren ADC Chart ────────────────────────────────────────
        FocusSenseCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector        = AppIcons.Chart,
                    contentDescription = null,
                    tint               = AccentBreak,
                    modifier           = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text       = "Tren ADC",
                    style      = MaterialTheme.typography.headlineSmall,
                    color      = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(Modifier.height(12.dp))
            AirQualityLineChart(
                history  = uiState.history,
                modifier = Modifier.fillMaxWidth()
            )
            if (uiState.history.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Awal",     color = TextSecondary, fontSize = 10.sp)
                    Text("Sekarang", color = TextSecondary, fontSize = 10.sp)
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // ── Rekomendasi ───────────────────────────────────────────
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Text(
                text       = "Rekomendasi",
                style      = MaterialTheme.typography.headlineSmall,
                color      = TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(8.dp))
            RecommendationCard(
                text   = current.recommendation,
                isGood = current.level == AirQualityLevel.GOOD
            )
        }

        Spacer(Modifier.height(20.dp))

        // ── Panduan Level ADC ─────────────────────────────────────
        FocusSenseCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector        = AppIcons.Info,
                    contentDescription = null,
                    tint               = AccentBreak,
                    modifier           = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text       = "Panduan ADC",
                    style      = MaterialTheme.typography.headlineSmall,
                    color      = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(Modifier.height(12.dp))
            AqiLegendRow("< 2500",  "Baik",  AqiGood)
            AqiLegendRow(">= 2500", "Buruk", AqiHazardous)
            Spacer(Modifier.height(6.dp))
            Text(
                text  = "Nilai ADC langsung dari sensor MQ-135 (0–4095)",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }

        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun SensorInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: androidx.compose.ui.graphics.Color,
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(BackgroundDark)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Icon(
            imageVector        = icon,
            contentDescription = null,
            tint               = iconTint,
            modifier           = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text     = label,
            style    = MaterialTheme.typography.bodyMedium,
            color    = TextSecondary,
            modifier = Modifier.weight(1f)
        )
        Text(
            text       = value,
            style      = MaterialTheme.typography.bodyMedium,
            color      = valueColor,
            fontWeight = FontWeight.SemiBold
        )
    }
}
