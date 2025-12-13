package com.example.visionguide.presentation.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.isSystemInDarkTheme

private val DarkColorScheme = darkColorScheme(
    primary = NeonGreen,
    secondary = NeonBlue,
    tertiary = Pink80,
    background = VisionBlack,
    surface = VisionDarkGray,
    onPrimary = VisionBlack,
    onSecondary = HighContrastWhite,
    onTertiary = VisionBlack,
    onBackground = HighContrastWhite,
    onSurface = HighContrastWhite,
)

private val LightColorScheme = lightColorScheme(
    primary = NeonGreen,
    secondary = NeonBlue,
    tertiary = Pink40,
    background = HighContrastWhite,
    surface = Color(0xFFF5F5F5),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = VisionBlack,
    onSurface = VisionBlack,
)

@Composable
fun VisionGuideTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
