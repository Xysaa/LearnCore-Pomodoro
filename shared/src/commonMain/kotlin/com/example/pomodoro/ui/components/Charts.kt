package com.example.pomodoro.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pomodoro.domain.model.AirQualityData
import com.example.pomodoro.domain.model.DailyStats
import com.example.pomodoro.ui.theme.*

private val DAY_LABELS = listOf("M", "S", "S", "R", "K", "J", "S")

/**
 * Bar chart 7 hari untuk StatisticsScreen.
 * stats: list 7 DailyStats berurutan dari lama ke baru
 */
@Composable
fun WeeklyBarChart(
    stats: List<DailyStats>,
    modifier: Modifier = Modifier
) {
    val maxMinutes = stats.maxOfOrNull { it.totalFocusMinutes }?.coerceAtLeast(1) ?: 1

    Column(modifier = modifier) {
        // Bars
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
        ) {
            stats.forEachIndexed { index, stat ->
                val heightFraction = stat.totalFocusMinutes.toFloat() / maxMinutes.toFloat()
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .fillMaxHeight(heightFraction.coerceAtLeast(0.03f))
                            .background(
                                color = if (index == stats.lastIndex) AccentFocus
                                        else AccentFocus.copy(alpha = 0.5f),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(
                                    topStart = 4.dp, topEnd = 4.dp
                                )
                            )
                    )
                }
            }
        }
        Spacer(Modifier.height(4.dp))
        // Day labels
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            val todayIdx = 6
            DAY_LABELS.forEachIndexed { index, label ->
                Text(
                    text     = label,
                    fontSize = 11.sp,
                    color    = if (index == todayIdx) AccentFocus else TextSecondary,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

/**
 * Line chart tren kualitas udara.
 * history: list AirQualityData terbaru (maks 24 titik)
 */
@Composable
fun AirQualityLineChart(
    history: List<AirQualityData>,
    modifier: Modifier = Modifier
) {
    if (history.size < 2) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(SurfaceDark)
        ) {
            Text(
                text  = "Menunggu data sensor...",
                color = TextSecondary,
                fontSize = 12.sp
            )
        }
        return
    }

    // Gunakan adc sebagai nilai Y
    val maxAdc = history.maxOf { it.adc }.coerceAtLeast(1).toFloat()
    val lineColor = AccentBreak

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp)
    ) {
        val step = size.width / (history.size - 1).coerceAtLeast(1)
        val path = Path()

        history.forEachIndexed { index, data ->
            val x = index * step
            val y = size.height - (data.adc.toFloat() / maxAdc) * size.height * 0.9f
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }

        // Fill under line
        val fillPath = Path().apply {
            addPath(path)
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }
        drawPath(path = fillPath, color = lineColor.copy(alpha = 0.15f))

        // Line stroke
        drawPath(path = path, color = lineColor, style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round))

        // Dots
        history.forEachIndexed { index, data ->
            val x = index * step
            val y = size.height - (data.adc.toFloat() / maxAdc) * size.height * 0.9f
            drawCircle(color = lineColor, radius = 3.dp.toPx(), center = Offset(x, y))
        }
    }
}
