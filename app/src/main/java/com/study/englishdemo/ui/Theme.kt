package com.study.englishdemo.ui

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val FallbackLight = lightColorScheme(
    primary = Color(0xFF2D5B52),
    onPrimary = Color(0xFFF7F2EA),
    primaryContainer = Color(0xFFD6E6DD),
    onPrimaryContainer = Color(0xFF082A22),
    secondary = Color(0xFFC98A3D),
    onSecondary = Color(0xFFFFFBF5),
    secondaryContainer = Color(0xFFF8E3C0),
    onSecondaryContainer = Color(0xFF3A2409),
    tertiary = Color(0xFF7A5EA3),
    onTertiary = Color(0xFFFFFBFF),
    tertiaryContainer = Color(0xFFEADDFF),
    background = Color(0xFFF6F1E8),
    onBackground = Color(0xFF1F1B16),
    surface = Color(0xFFFFFBF5),
    onSurface = Color(0xFF1F1B16),
    surfaceVariant = Color(0xFFEDE4D5),
    onSurfaceVariant = Color(0xFF4B4638),
    outline = Color(0xFF7D776B),
)

private val FallbackDark = darkColorScheme(
    primary = Color(0xFFA8D0C0),
    onPrimary = Color(0xFF0B2E27),
    primaryContainer = Color(0xFF21433B),
    onPrimaryContainer = Color(0xFFC4E6D7),
    secondary = Color(0xFFF0B670),
    onSecondary = Color(0xFF2B1800),
    secondaryContainer = Color(0xFF4A3115),
    onSecondaryContainer = Color(0xFFFFDCB0),
    tertiary = Color(0xFFCDB7F5),
    onTertiary = Color(0xFF2B1755),
    tertiaryContainer = Color(0xFF412B6C),
    background = Color(0xFF121212),
    onBackground = Color(0xFFE8E2D6),
    surface = Color(0xFF1B1A17),
    onSurface = Color(0xFFE8E2D6),
    surfaceVariant = Color(0xFF2A2721),
    onSurfaceVariant = Color(0xFFBFB9AB),
    outline = Color(0xFF6B665C),
)

@Composable
fun LexRiseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val colors = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> FallbackDark
        else -> FallbackLight
    }
    MaterialTheme(
        colorScheme = colors,
        content = content,
    )
}
