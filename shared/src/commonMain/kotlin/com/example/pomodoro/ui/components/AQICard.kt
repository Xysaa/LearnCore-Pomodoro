package com.example.pomodoro.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pomodoro.domain.model.AirQualityData
import com.example.pomodoro.domain.model.AirQualityLevel
import com.example.pomodoro.ui.theme.*

fun aqiColor(level: AirQualityLevel): Color = when (level) {
    AirQualityLevel.GOOD      -> AqiGood
    AirQualityLevel.HAZARDOUS -> AqiHazardous
}

/** Big display — lingkaran besar dengan angka ADC & label, icon vektor */
@Composable
fun AQIBigDisplay(
    airQuality: AirQualityData,
    modifier: Modifier = Modifier
) {
    val color = aqiColor(airQuality.level)
    val icon  = if (airQuality.level == AirQualityLevel.GOOD) AppIcons.AirGood else AppIcons.Warning

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(180.dp)
                .clip(RoundedCornerShape(90.dp))
                .background(color.copy(alpha = 0.12f))
                .border(3.dp, color, RoundedCornerShape(90.dp))
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector        = icon,
                    contentDescription = airQuality.levelLabel,
                    tint               = color,
                    modifier           = Modifier.size(28.dp)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text       = "${airQuality.adc}",
                    style      = MaterialTheme.typography.displayMedium,
                    color      = color,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text  = "ADC",
                    style = MaterialTheme.typography.labelMedium,
                    color = color.copy(alpha = 0.7f)
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector        = icon,
                contentDescription = null,
                tint               = color,
                modifier           = Modifier.size(16.dp)
            )
            Text(
                text       = airQuality.levelLabel,
                style      = MaterialTheme.typography.labelLarge,
                color      = color,
                fontWeight = FontWeight.SemiBold
            )
        }
        Text(
            text  = "MQ-135 Sensor",
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )
    }
}

/** Legend row untuk panduan level */
@Composable
fun AqiLegendRow(
    range: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(color)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text     = range,
            color    = TextSecondary,
            fontSize = 13.sp,
            modifier = Modifier.width(100.dp)
        )
        Text(
            text       = label,
            color      = color,
            fontSize   = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
