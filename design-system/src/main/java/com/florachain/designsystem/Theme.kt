package com.florachain.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = BotanicalEmerald,
    onPrimary = DeepSlate,
    primaryContainer = BotanicalDark,
    onPrimaryContainer = BotanicalLight,
    secondary = MintAccent,
    onSecondary = DeepSlate,
    background = DeepSlate,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceDarkElevated,
    onSurfaceVariant = TextSecondary,
    outline = GlassBorder,
    error = ErrorRose,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = BotanicalEmerald,
    onPrimary = Color.White,
    primaryContainer = BotanicalLight,
    onPrimaryContainer = BotanicalDark,
    secondary = BotanicalDark,
    onSecondary = Color.White,
    background = Color(0xFFF0FDF4),
    onBackground = Color(0xFF0F172A),
    surface = Color.White,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFE2E8F0),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFCBD5E1),
    error = ErrorRose,
    onError = Color.White
)

@Composable
fun FloraChainTheme(
    darkTheme: Boolean = true, // Default to sleek dark glassmorphism
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
