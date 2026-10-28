package com.example.watchdiary.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val AppDarkColors = darkColorScheme(
    primary = BluePrimary,
    onPrimary = NightBackground,
    primaryContainer = BluePrimaryContainer,
    onPrimaryContainer = TextPrimary,

    secondary = CyanSecondary,
    tertiary = PinkAccent,

    background = NightBackground,
    onBackground = TextPrimary,

    surface = NightSurface,
    onSurface = TextPrimary,

    surfaceVariant = NightSurfaceSoft,
    onSurfaceVariant = TextSecondary,

    outline = StrokeColor,
    error = ErrorRed
)

@Composable
fun WatchDiaryTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AppDarkColors,
        typography = Typography,
        content = content
    )
}