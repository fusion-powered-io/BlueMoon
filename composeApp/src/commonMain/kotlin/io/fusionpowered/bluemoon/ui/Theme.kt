package io.fusionpowered.bluemoon.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val lightColors = lightColorScheme(
    primary = BluePrimary,
    secondary = BlueSecondary,
    background = BlueBackground,
    surface = BlueSurface,
    onPrimary = OnPrimary,
    onBackground = OnBackground,
)

private val darkColors = darkColorScheme(
    primary = BlueSecondary,
    secondary = BluePrimary,
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    onPrimary = Color.Black,
    onBackground = Color.White,
)

@Composable
fun BluemoonTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) darkColors else lightColors

    MaterialTheme (
        colorScheme = colors,
        typography = appTypography,
        content = content
    )
}
