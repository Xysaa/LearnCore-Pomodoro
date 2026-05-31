package com.example.pomodoro.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.pomodoro.ui.theme.*

/**
 * Dot indicator sesi seperti di wireframe: [●●●○]
 * completedToday = sesi yang sudah selesai hari ini
 * totalTarget    = target sesi harian (misal 4)
 * currentSession = sesi yang sedang aktif (bisa partial)
 */
@Composable
fun SessionDots(
    completedToday: Int,
    totalTarget: Int,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text  = "Target hari ini: $totalTarget sesi",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
        Spacer(Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(totalTarget) { index ->
                val filled = index < completedToday
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(RoundedCornerShape(50))
                        .background(if (filled) AccentFocus else SurfaceDark)
                        .border(1.5.dp, if (filled) AccentFocus else BorderGreen, RoundedCornerShape(50))
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text  = "$completedToday/$totalTarget selesai",
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )
    }
}
