package com.weaper.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val WeaperDarkColors = darkColorScheme(
    primary = WeaperBlue,
    onPrimary = TextPrimary,
    primaryContainer = DarkSurfaceVariant,
    onPrimaryContainer = TextPrimary,
    secondary = WeaperGreen,
    onSecondary = TextPrimary,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    error = WeaperRed,
    onError = TextPrimary
)

/**
 * Dark-mode-only theme optimized for live stage use.
 * High contrast, large touch targets, minimal eye strain.
 */
@Composable
fun WeaperTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = WeaperDarkColors,
        typography = WeaperTypography,
        content = content
    )
}
