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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pomodoro.ui.theme.*

/** Generic surface card */
@Composable
fun FocusSenseCard(
    modifier: Modifier = Modifier,
    borderColor: Color = BorderGreen,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceDark)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .padding(16.dp),
        content = content
    )
}

/** Stat card kecil: label + nilai besar */
@Composable
fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    accentColor: Color = AccentFocus
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceDark)
            .border(1.dp, accentColor.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(
            text       = value,
            style      = MaterialTheme.typography.headlineLarge,
            color      = accentColor,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text  = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
    }
}

/** Rekomendasi card di AirQualityScreen — icon vektor, bukan emoji */
@Composable
fun RecommendationCard(
    text: String,
    isGood: Boolean,
    modifier: Modifier = Modifier
) {
    val color = if (isGood) AccentFocus else AccentWarning
    val icon  = if (isGood) AppIcons.CheckCircle else AppIcons.Warning
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.1f))
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Icon(
            imageVector        = icon,
            contentDescription = null,
            tint               = color,
            modifier           = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text  = text,
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary
        )
    }
}

/** Session history list item — icon vektor, bukan emoji */
@Composable
fun SessionHistoryItem(
    startTime: String,
    endTime: String,
    durationMin: Int,
    completed: Boolean,
    modifier: Modifier = Modifier
) {
    val icon  = if (completed) AppIcons.CheckCircle else AppIcons.Pause
    val color = if (completed) AccentFocus else TextSecondary
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Icon(
            imageVector        = icon,
            contentDescription = null,
            tint               = color,
            modifier           = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text     = "$startTime – $endTime",
            style    = MaterialTheme.typography.bodyMedium,
            color    = TextPrimary,
            modifier = Modifier.weight(1f)
        )
        Text(
            text  = "${durationMin}m",
            style = MaterialTheme.typography.labelMedium,
            color = color
        )
    }
}

/** Settings section card with title — icon vektor, bukan emoji */
@Composable
fun SettingsSectionCard(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector        = icon,
                contentDescription = null,
                tint               = AccentFocus,
                modifier           = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text  = title,
                style = MaterialTheme.typography.headlineSmall,
                color = TextPrimary
            )
        }
        Spacer(Modifier.height(8.dp))
        FocusSenseCard {
            content()
        }
    }
}

/** Overload dengan String icon (untuk backward compat sementara) */
@Composable
fun SettingsSectionCard(
    title: String,
    icon: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    // Fallback ke icon default jika dipanggil dengan String
    SettingsSectionCard(
        title    = title,
        icon     = AppIcons.Settings,
        modifier = modifier,
        content  = content
    )
}
