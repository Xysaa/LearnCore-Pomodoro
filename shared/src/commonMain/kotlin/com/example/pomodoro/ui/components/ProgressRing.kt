package com.example.pomodoro.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.pomodoro.domain.model.TimerPhase
import com.example.pomodoro.ui.theme.*

@Composable
fun ProgressRing(
    progress: Float,          // 0f..1f
    timerText: String,
    phase: TimerPhase,
    modifier: Modifier = Modifier,
    ringSize: Dp = 240.dp,
    strokeWidth: Dp = 14.dp,
    reducedMotion: Boolean = false
) {
    val accentColor = if (phase == TimerPhase.FOCUS) AccentFocus else AccentBreak

    // Animate progress smoothly each tick
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = if (reducedMotion) snap()
        else tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "ring_progress"
    )

    // Pulsing glow animation for the ring tip
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(ringSize)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokePx = strokeWidth.toPx()
            val inset    = strokePx / 2f
            val arcSize  = Size(size.width - strokePx, size.height - strokePx)
            val topLeft  = Offset(inset, inset)

            // Background ring track
            drawArc(
                color       = RingBackground,
                startAngle  = -90f,
                sweepAngle  = 360f,
                useCenter   = false,
                topLeft     = topLeft,
                size        = arcSize,
                style       = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            // Progress arc with gradient brush
            val gradientBrush = Brush.sweepGradient(
                colors = listOf(accentColor.copy(alpha = 0.6f), accentColor),
                center = Offset(size.width / 2f, size.height / 2f)
            )
            drawArc(
                brush       = gradientBrush,
                startAngle  = -90f,
                sweepAngle  = 360f * animatedProgress,
                useCenter   = false,
                topLeft     = topLeft,
                size        = arcSize,
                style       = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            // Glowing tip at the leading edge
            if (!reducedMotion && animatedProgress > 0.01f) {
                val sweepRad = Math.toRadians((-90f + 360f * animatedProgress).toDouble())
                val radius   = (size.width - strokePx) / 2f
                val cx       = size.width / 2f + radius * kotlin.math.cos(sweepRad).toFloat()
                val cy       = size.height / 2f + radius * kotlin.math.sin(sweepRad).toFloat()
                drawCircle(
                    color  = accentColor.copy(alpha = glowAlpha),
                    radius = strokePx * 1.2f,
                    center = Offset(cx, cy)
                )
            }
        }

        // Timer text in center
        Text(
            text  = timerText,
            style = MaterialTheme.typography.displayMedium,
            color = TextPrimary
        )
    }
}
