package com.example.pomodoro.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DeepForestNightColorScheme = darkColorScheme(
    primary          = AccentFocus,
    onPrimary        = BackgroundDark,
    primaryContainer = SurfaceDark,
    onPrimaryContainer = TextPrimary,

    secondary        = AccentBreak,
    onSecondary      = BackgroundDark,
    secondaryContainer = BorderGreen,
    onSecondaryContainer = TextSecondary,

    tertiary         = AccentWarning,
    onTertiary       = BackgroundDark,

    background       = BackgroundDark,
    onBackground     = TextPrimary,

    surface          = SurfaceDark,
    onSurface        = TextPrimary,
    surfaceVariant   = BorderGreen,
    onSurfaceVariant = TextSecondary,

    outline          = BorderGreen,
    outlineVariant   = Color(0xFF2A4A38),

    error            = AccentDanger,
    onError          = BackgroundDark,
    errorContainer   = Color(0xFF3D1515),
    onErrorContainer = AccentDanger,

    inverseSurface   = TextPrimary,
    inverseOnSurface = BackgroundDark,
    inversePrimary   = Color(0xFF0D5C32),

    scrim            = Color(0x80000000)
)

@Composable
fun FocusSenseTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DeepForestNightColorScheme,
        typography  = AppTypography,
        content     = content
    )
}
