package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = MazeAmber,
    onPrimary = Color(0xFF080D1A),
    primaryContainer = Color(0xFF78350F),
    onPrimaryContainer = Color(0xFFFEF3C7),
    secondary = MazePlayer,
    onSecondary = Color(0xFF080D1A),
    secondaryContainer = Color(0xFF0E3A5A),
    onSecondaryContainer = Color(0xFFE0F2FE),
    tertiary = MazeAmberGlow,
    onTertiary = Color(0xFF451A03),
    background = MazeBgDark,
    onBackground = MazeTextH1,
    surface = MazeSurface1,
    onSurface = MazeTextH1,
    surfaceVariant = MazeSurface2,
    onSurfaceVariant = MazeTextBody,
    outline = MazeEdgeHighlight,
    outlineVariant = MazeEdgeHighlightBright,
    error = MazeDanger,
    onError = Color.White
)

private val LightColorScheme = darkColorScheme(
    primary = MazeAmber,
    onPrimary = Color(0xFF080D1A),
    primaryContainer = Color(0xFF78350F),
    onPrimaryContainer = Color(0xFFFEF3C7),
    secondary = MazePlayer,
    onSecondary = Color(0xFF080D1A),
    secondaryContainer = Color(0xFF0E3A5A),
    onSecondaryContainer = Color(0xFFE0F2FE),
    tertiary = MazeAmberGlow,
    onTertiary = Color(0xFF451A03),
    background = MazeBgLight,
    onBackground = MazeTextLight,
    surface = MazeSurfaceLight,
    onSurface = MazeTextLight,
    surfaceVariant = MazeSurface2Light,
    onSurfaceVariant = MazeTextBody,
    outline = MazeEdgeHighlight,
    outlineVariant = MazeEdgeHighlightBright,
    error = MazeDanger,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
