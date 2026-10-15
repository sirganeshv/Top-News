package com.topten.news

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Navy ink with temple-gold rank numerals.
private val LightColors = lightColorScheme(
    primary = Color(0xFF1B2430),
    onPrimary = Color.White,
    secondary = Color(0xFFB07D0B),
    background = Color(0xFFF4F6F8),
    onBackground = Color(0xFF1B2430),
    surface = Color(0xFFF4F6F8),
    onSurface = Color(0xFF1B2430),
    surfaceVariant = Color(0xFFE3E8ED),
    onSurfaceVariant = Color(0xFF586574),
    outlineVariant = Color(0xFFD5DCE3),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFE6EBF0),
    onPrimary = Color(0xFF12161C),
    secondary = Color(0xFFE3B341),
    background = Color(0xFF12161C),
    onBackground = Color(0xFFE6EBF0),
    surface = Color(0xFF12161C),
    onSurface = Color(0xFFE6EBF0),
    surfaceVariant = Color(0xFF232A33),
    onSurfaceVariant = Color(0xFF9AA6B2),
    outlineVariant = Color(0xFF2C343E),
)

@Composable
fun TopTenTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        content = content,
    )
}
