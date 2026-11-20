package com.bengkelin.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = BengkelinOrange,
    onPrimary = Color.White,
    primaryContainer = BengkelinOrangeLight,
    onPrimaryContainer = BengkelinNavy,
    secondary = BengkelinNavy,
    onSecondary = Color.White,
    background = Color(0xFFFAFAFA),
    onBackground = Slate900,
    surface = Color.White,
    onSurface = Slate900,
    surfaceVariant = Slate100,
    onSurfaceVariant = Slate500,
    error = ErrorRed,
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = Color(0xFF991B1B)
)

private val DarkColorScheme = darkColorScheme(
    primary = BengkelinOrange,
    onPrimary = Color.White,
    secondary = BengkelinOrangeLight,
    background = Color(0xFF0F172A),
    onBackground = Slate100,
    surface = Color(0xFF1E293B),
    onSurface = Slate100,
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = Color(0xFF94A3B8),
    error = ErrorRed
)

@Composable
fun BengkelinTheme(
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
