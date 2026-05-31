package com.example.pomodoro.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pomodoro.ui.theme.*

@Composable
fun FocusSenseTopBar(
    title: String,
    isConnected: Boolean,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        Text(
            text       = title,
            style      = MaterialTheme.typography.headlineMedium,
            color      = TextPrimary,
            fontWeight = FontWeight.Bold,
            modifier   = Modifier.weight(1f)
        )
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            actions()
            // BLE connection icon (bolt = connected, no-connection = disconnected)
            Icon(
                imageVector        = if (isConnected) AppIcons.Bolt else AppIcons.NoConnection,
                contentDescription = if (isConnected) "Terhubung" else "Tidak Terhubung",
                tint               = if (isConnected) AccentFocus else AccentDanger,
                modifier           = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun SimpleTopBar(
    title: String,
    modifier: Modifier = Modifier,
    trailing: String = ""
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        Text(
            text       = title,
            style      = MaterialTheme.typography.headlineMedium,
            color      = TextPrimary,
            fontWeight = FontWeight.Bold,
            modifier   = Modifier.weight(1f)
        )
        if (trailing.isNotEmpty()) {
            Text(text = trailing, color = TextSecondary, fontSize = 13.sp)
        }
    }
}
