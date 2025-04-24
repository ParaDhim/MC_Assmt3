// theme/Theme.kt
package com.example.myapplication.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF006C51),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB3F1D8),
    onPrimaryContainer = Color(0xFF00201A),
    secondary = Color(0xFF4D6356),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD0E8D8),
    onSecondaryContainer = Color(0xFF0A2015),
    tertiary = Color(0xFF406377),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFC3E8FF),
    onTertiaryContainer = Color(0xFF001E2B),
    error = Color(0xFFBA1A1A),
    background = Color(0xFFFBFDF9),
    onBackground = Color(0xFF191C1A),
    surface = Color(0xFFFBFDF9),
    onSurface = Color(0xFF191C1A),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF95D4BC),
    onPrimary = Color(0xFF00382C),
    primaryContainer = Color(0xFF00513E),
    onPrimaryContainer = Color(0xFFB3F1D8),
    secondary = Color(0xFFB4CCBC),
    onSecondary = Color(0xFF1F352A),
    secondaryContainer = Color(0xFF354B3F),
    onSecondaryContainer = Color(0xFFD0E8D8),
    tertiary = Color(0xFFA7CCE3),
    onTertiary = Color(0xFF0B3445),
    tertiaryContainer = Color(0xFF274B5D),
    onTertiaryContainer = Color(0xFFC3E8FF),
    error = Color(0xFFFFB4AB),
    background = Color(0xFF191C1A),
    onBackground = Color(0xFFE1E3DF),
    surface = Color(0xFF191C1A),
    onSurface = Color(0xFFE1E3DF),
)

@Composable
fun WifiSignalMapperTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}