package com.study.englishdemo.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF2D5B52),
    onPrimary = Color(0xFFF7F2EA),
    primaryContainer = Color(0xFFD6E6DD),
    secondary = Color(0xFFC98A3D),
    background = Color(0xFFF6F1E8),
    surface = Color(0xFFFFFBF5),
    surfaceVariant = Color(0xFFEDE4D5),
    onSurface = Color(0xFF1F1B16),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFA8D0C0),
    secondary = Color(0xFFF0B670),
    background = Color(0xFF151515),
    surface = Color(0xFF211F1B),
)

@Composable
fun LexRiseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}
