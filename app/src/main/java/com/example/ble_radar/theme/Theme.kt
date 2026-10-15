package com.example.ble_radar.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme =
  darkColorScheme(
    primary = RadarPrimary,
    onPrimary = RadarOnPrimary,
    primaryContainer = RadarPrimaryContainer,
    onPrimaryContainer = RadarOnPrimaryContainer,
    secondary = RadarSecondary,
    onSecondary = RadarOnSecondary,
    secondaryContainer = RadarSecondaryContainer,
    onSecondaryContainer = RadarOnSecondaryContainer,
    tertiary = RadarTertiaryFixedDim,
    onTertiary = RadarOnTertiary,
    tertiaryContainer = RadarTertiaryContainer,
    onTertiaryContainer = RadarOnTertiaryContainer,
    background = RadarBackground,
    onBackground = RadarOnBackground,
    surface = RadarSurface,
    onSurface = RadarOnSurface,
    surfaceVariant = RadarSurfaceVariant,
    onSurfaceVariant = RadarOnSurfaceVariant,
    surfaceContainerLowest = RadarSurfaceContainerLowest,
    surfaceContainerLow = RadarSurfaceContainerLow,
    surfaceContainer = RadarSurfaceContainer,
    surfaceContainerHigh = RadarSurfaceContainerHigh,
    surfaceContainerHighest = RadarSurfaceContainerHighest,
    outline = RadarOutline,
    outlineVariant = RadarOutlineVariant,
    error = RadarError,
    onError = RadarOnError,
    errorContainer = RadarErrorContainer,
    onErrorContainer = RadarOnErrorContainer,
  )

private val LightColorScheme = DarkColorScheme // Cyber radar is inherently dark-themed

@Composable
fun BleRadarTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = DarkColorScheme,
    typography = Typography,
    content = content,
  )
}
