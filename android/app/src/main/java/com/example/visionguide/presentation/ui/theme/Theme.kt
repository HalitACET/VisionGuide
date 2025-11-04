package com.example.visionguide.presentation.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = AppYellow,
    background = AppBlack,
    surface = CardGray,
    onPrimary = AppBlack,
    onBackground = TextWhite,
    onSurface = TextWhite
)

@Composable
fun VisionGuideTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
