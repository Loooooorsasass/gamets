package com.looooorsasass.gamets.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// Bảng màu Dark hiện đại chuẩn Material 3
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF38BDF8),        // Sky-400
    onPrimary = Color(0xFF082F49),      // Sky-950
    primaryContainer = Color(0xFF0369A1),// Sky-700
    onPrimaryContainer = Color(0xFFE0F2FE),
    secondary = Color(0xFF818CF8),      // Indigo-400
    onSecondary = Color(0xFF1E1B4B),
    secondaryContainer = Color(0xFF3730A3),
    onSecondaryContainer = Color(0xFFEEF2FF),
    tertiary = Color(0xFF34D399),       // Emerald-400
    onTertiary = Color(0xFF022C22),
    tertiaryContainer = Color(0xFF065F46),
    onTertiaryContainer = Color(0xFFD1FAE5),
    error = Color(0xFFF87171),          // Red-400
    onError = Color(0xFF450A0A),
    errorContainer = Color(0xFF7F1D1D),
    onErrorContainer = Color(0xFFFEE2E2),
    background = Color(0xFF0B1329),     // Deep Slate
    onBackground = Color(0xFFF8FAFC),
    surface = Color(0xFF131D38),        // Elevated Slate
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF1E293B),
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = Color(0xFF475569)
)

// Bảng màu Light chuẩn Material 3
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF0284C7),        // Sky-600
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFBAE6FD),
    onPrimaryContainer = Color(0xFF082F49),
    secondary = Color(0xFF4F46E5),      // Indigo-600
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE0E7FF),
    onSecondaryContainer = Color(0xFF1E1B4B),
    tertiary = Color(0xFF059669),       // Emerald-600
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFA7F3D0),
    onTertiaryContainer = Color(0xFF022C22),
    error = Color(0xFFDC2626),          // Red-600
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = Color(0xFF7F1D1D),
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFE2E8F0),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFF94A3B8)
)

/**
 * GametsTheme: Theme giao diện hiện đại tuân thủ Material Design 3.
 * Tự động hỗ trợ Dynamic Color trên Android 12+ (API 31+) và tương thích ngược hoàn hảo.
 */
@Composable
fun GametsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme: ColorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
