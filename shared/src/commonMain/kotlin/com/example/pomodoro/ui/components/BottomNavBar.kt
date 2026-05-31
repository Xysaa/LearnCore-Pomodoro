package com.example.pomodoro.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pomodoro.ui.theme.*

enum class NavTab(
    val iconOutlined: ImageVector,
    val iconFilled: ImageVector,
    val label: String
) {
    FOCUS(AppIcons.Focus, AppIcons.FocusFilled, "Fokus"),
    STATISTICS(AppIcons.Statistics, AppIcons.StatsFilled, "Statistik"),
    AIR_QUALITY(AppIcons.AirQuality, AppIcons.AirFilled, "Udara"),
    SETTINGS(AppIcons.Settings, AppIcons.SettingsFilled, "Setelan")
}

@Composable
fun BottomNavBar(
    selectedTab: NavTab,
    onTabSelected: (NavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .background(SurfaceDark)
            .padding(vertical = 8.dp)
    ) {
        NavTab.entries.forEach { tab ->
            NavBarItem(
                tab        = tab,
                isSelected = tab == selectedTab,
                onClick    = { onTabSelected(tab) }
            )
        }
    }
}

@Composable
private fun NavBarItem(
    tab: NavTab,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val iconColor by animateColorAsState(
        targetValue   = if (isSelected) AccentFocus else TextSecondary,
        animationSpec = tween(200),
        label         = "navIconColor"
    )
    val labelColor by animateColorAsState(
        targetValue   = if (isSelected) AccentFocus else TextSecondary,
        animationSpec = tween(200),
        label         = "navLabelColor"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Icon(
            imageVector = if (isSelected) tab.iconFilled else tab.iconOutlined,
            contentDescription = tab.label,
            tint     = iconColor,
            modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text       = tab.label,
            fontSize   = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color      = labelColor
        )
        Spacer(Modifier.height(2.dp))
        Box(
            modifier = Modifier
                .size(if (isSelected) 4.dp else 0.dp)
                .clip(RoundedCornerShape(50))
                .background(AccentFocus)
        )
    }
}
