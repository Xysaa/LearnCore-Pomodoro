package com.example.pomodoro.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pomodoro.ui.theme.*

/** Toggle row untuk settings */
@Composable
fun SettingsToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text     = label,
            color    = TextPrimary,
            fontSize = 14.sp,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked         = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor       = BackgroundDark,
                checkedTrackColor       = AccentFocus,
                uncheckedThumbColor     = TextSecondary,
                uncheckedTrackColor     = BorderGreen
            )
        )
    }
}

/** Stepper row untuk angka (misal durasi menit) */
@Composable
fun SettingsStepperRow(
    label: String,
    value: Int,
    unit: String = "m",
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text     = label,
            color    = TextPrimary,
            fontSize = 14.sp,
            modifier = Modifier.weight(1f)
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StepButton("-", onDecrement)
            Text(
                text  = "$value$unit",
                color = AccentFocus,
                fontSize = 14.sp,
                modifier = Modifier.widthIn(min = 40.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            StepButton("+", onIncrement)
        }
    }
}

@Composable
private fun StepButton(label: String, onClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(30.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, AccentFocus.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
    ) {
        Text(text = label, color = AccentFocus, fontSize = 16.sp)
    }
}

/** Slider row untuk threshold */
@Composable
fun SettingsSliderRow(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    unit: String = "",
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = label, color = TextPrimary, fontSize = 14.sp, modifier = Modifier.weight(1f))
            Text(
                text  = "${value.toInt()}$unit",
                color = AccentFocus,
                fontSize = 13.sp
            )
        }
        androidx.compose.material3.Slider(
            value         = value,
            onValueChange = onValueChange,
            valueRange    = valueRange,
            colors = androidx.compose.material3.SliderDefaults.colors(
                thumbColor       = AccentFocus,
                activeTrackColor = AccentFocus,
                inactiveTrackColor = BorderGreen
            )
        )
    }
}
