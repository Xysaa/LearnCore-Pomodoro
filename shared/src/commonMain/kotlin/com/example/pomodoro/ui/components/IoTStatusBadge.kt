package com.example.pomodoro.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pomodoro.domain.model.BleConnectionStatus
import com.example.pomodoro.domain.model.IoTState
import com.example.pomodoro.domain.model.PresenceStatus
import com.example.pomodoro.ui.theme.*

private data class BadgeConfig(
    val dotColor: Color,
    val icon: ImageVector,
    val iconTint: Color,
    val label: String
)

private fun badgeConfig(iotState: IoTState): BadgeConfig = when (iotState.bleStatus) {
    BleConnectionStatus.CONNECTED -> when (iotState.presenceStatus) {
        PresenceStatus.PRESENT -> BadgeConfig(
            StatusPresent, AppIcons.PersonPresent, StatusPresent, "Kamu Terdeteksi"
        )
        PresenceStatus.ABSENT -> BadgeConfig(
            StatusAbsent, AppIcons.PersonAbsent, StatusAbsent, "Tidak Ada Aktivitas"
        )
        PresenceStatus.UNKNOWN -> BadgeConfig(
            AccentBreak, AppIcons.BleConnected, AccentBreak, "Terhubung"
        )
    }
    BleConnectionStatus.SCANNING -> BadgeConfig(
        AccentWarning, AppIcons.BleScanning, AccentWarning, "Mencari Perangkat..."
    )
    BleConnectionStatus.CONNECTING -> BadgeConfig(
        AccentBreak, AppIcons.BleConnected, AccentBreak, "Menghubungkan..."
    )
    BleConnectionStatus.ERROR -> BadgeConfig(
        StatusError, AppIcons.Error, StatusError, "Koneksi Error"
    )
    BleConnectionStatus.DISCONNECTED -> BadgeConfig(
        StatusError, AppIcons.BleDisconnected, StatusError, "IoT Tidak Terhubung"
    )
}

@Composable
fun IoTStatusBadge(
    iotState: IoTState,
    modifier: Modifier = Modifier,
    reducedMotion: Boolean = false
) {
    val cfg = badgeConfig(iotState)

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue  = if (!reducedMotion && iotState.presenceStatus == PresenceStatus.PRESENT) 1.3f else 1f,
        animationSpec = infiniteRepeatable(
            animation  = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(SurfaceDark)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        // Pulsing dot
        Box(
            modifier = Modifier
                .size(10.dp)
                .scale(pulseScale)
                .clip(RoundedCornerShape(50))
                .background(cfg.dotColor)
        )
        Spacer(Modifier.width(8.dp))
        Icon(
            imageVector        = cfg.icon,
            contentDescription = null,
            tint               = cfg.iconTint,
            modifier           = Modifier.size(15.dp)
        )
        Spacer(Modifier.width(5.dp))
        Text(
            text     = cfg.label,
            color    = TextPrimary,
            fontSize = 13.sp
        )
    }
}

@Composable
fun BleConnectionIndicator(
    isConnected: Boolean,
    modifier: Modifier = Modifier
) {
    val color = if (isConnected) StatusPresent else StatusError
    Box(
        modifier = modifier
            .size(10.dp)
            .clip(RoundedCornerShape(50))
            .background(color)
    )
}
