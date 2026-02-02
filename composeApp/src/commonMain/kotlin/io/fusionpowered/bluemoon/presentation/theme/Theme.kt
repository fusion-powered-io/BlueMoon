package io.fusionpowered.bluemoon.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val lightScheme = lightColorScheme(
    primary = BluetoothBlue,
    onPrimary = Color.White,
    surface = CardSurface,
    onSurface = TextPrimary,
    surfaceVariant = SecondarySurface,
    onSurfaceVariant = TextSecondary,
    background = AppBackground,
    outline = DividerColor,
    tertiary = ConnectionGreen
)

private val darkScheme = darkColorScheme(
    primary = BluetoothBlue,
    onPrimary = Color.White,
    surface = CardSurface,
    onSurface = TextPrimary,
    surfaceVariant = SecondarySurface,
    onSurfaceVariant = TextSecondary,
    background = AppBackground,
    outline = DividerColor,
    tertiary = ConnectionGreen
)

@Composable
fun BlueMoonTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) darkScheme else lightScheme,
        typography = AppTypography,
        content = content
    )
}