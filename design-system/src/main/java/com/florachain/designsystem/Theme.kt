package com.florachain.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Immutable
data class FloraChainColors(
    val background: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val glassSurface: Color,
    val glassBorder: Color,
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val secondary: Color,
    val accent: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val trustGreen: Color,
    val warningAmber: Color,
    val errorRose: Color,
    val infoBlue: Color,
    val cardBorderBrush: Brush,
    val isDark: Boolean
)

val DarkFloraChainColors = FloraChainColors(
    background = DarkBackground,
    surface = SurfaceDark,
    surfaceElevated = SurfaceDarkElevated,
    glassSurface = GlassSurfaceDark,
    glassBorder = GlassBorderDark,
    primary = BotanicalEmerald,
    onPrimary = DarkBackground,
    primaryContainer = BotanicalDark,
    onPrimaryContainer = BotanicalLight,
    secondary = MintAccent,
    accent = MintAccent,
    textPrimary = DarkTextPrimary,
    textSecondary = DarkTextSecondary,
    textMuted = DarkTextMuted,
    trustGreen = TrustGreen,
    warningAmber = WarningAmber,
    errorRose = ErrorRose,
    infoBlue = InfoBlue,
    cardBorderBrush = Brush.linearGradient(
        listOf(
            Color(0x8010B981),
            Color(0x2634D399),
            Color(0x4010B981)
        )
    ),
    isDark = true
)

val LightFloraChainColors = FloraChainColors(
    background = LightBackground,
    surface = SurfaceLight,
    surfaceElevated = SurfaceLightElevated,
    glassSurface = GlassSurfaceLight,
    glassBorder = GlassBorderLight,
    primary = ForestGreen,
    onPrimary = Color.White,
    primaryContainer = BotanicalLight,
    onPrimaryContainer = BotanicalDark,
    secondary = BotanicalEmerald,
    accent = HarvestGold,
    textPrimary = LightTextPrimary,
    textSecondary = LightTextSecondary,
    textMuted = LightTextMuted,
    trustGreen = TrustGreen,
    warningAmber = WarningAmber,
    errorRose = ErrorRose,
    infoBlue = InfoBlue,
    cardBorderBrush = Brush.linearGradient(
        listOf(
            Color(0xFF6EE7B7),
            Color(0xFFA7F3D0),
            Color(0xFF6EE7B7)
        )
    ),
    isDark = false
)

val LocalFloraChainColors = staticCompositionLocalOf { DarkFloraChainColors }

data class ThemeController(
    val isDark: Boolean,
    val toggleTheme: () -> Unit
)

val LocalThemeController = compositionLocalOf {
    ThemeController(isDark = true, toggleTheme = {})
}

private val DarkColorScheme = darkColorScheme(
    primary = BotanicalEmerald,
    onPrimary = DarkBackground,
    primaryContainer = BotanicalDark,
    onPrimaryContainer = BotanicalLight,
    secondary = MintAccent,
    onSecondary = DarkBackground,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = SurfaceDark,
    onSurface = DarkTextPrimary,
    surfaceVariant = SurfaceDarkElevated,
    onSurfaceVariant = DarkTextSecondary,
    outline = GlassBorderDark,
    error = ErrorRose,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = ForestGreen,
    onPrimary = Color.White,
    primaryContainer = BotanicalLight,
    onPrimaryContainer = BotanicalDark,
    secondary = BotanicalEmerald,
    onSecondary = Color.White,
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = SurfaceLight,
    onSurface = LightTextPrimary,
    surfaceVariant = SurfaceLightElevated,
    onSurfaceVariant = LightTextSecondary,
    outline = GlassBorderLight,
    error = ErrorRose,
    onError = Color.White
)

object FloraChainTheme {
    val colors: FloraChainColors
        @Composable
        get() = LocalFloraChainColors.current

    val typography: Typography
        @Composable
        get() = MaterialTheme.typography
}

@Composable
fun FloraChainTheme(
    darkTheme: Boolean = true,
    onToggleTheme: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val floraColors = if (darkTheme) DarkFloraChainColors else LightFloraChainColors
    val materialScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val themeController = remember(darkTheme, onToggleTheme) {
        ThemeController(
            isDark = darkTheme,
            toggleTheme = onToggleTheme ?: {}
        )
    }

    CompositionLocalProvider(
        LocalFloraChainColors provides floraColors,
        LocalThemeController provides themeController
    ) {
        MaterialTheme(
            colorScheme = materialScheme,
            typography = Typography,
            content = content
        )
    }
}

@Composable
fun floraChainTextFieldColors() = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
    focusedTextColor = FloraChainTheme.colors.textPrimary,
    unfocusedTextColor = FloraChainTheme.colors.textPrimary,
    focusedBorderColor = FloraChainTheme.colors.primary,
    unfocusedBorderColor = FloraChainTheme.colors.glassBorder,
    focusedContainerColor = FloraChainTheme.colors.surface,
    unfocusedContainerColor = FloraChainTheme.colors.surface,
    focusedLabelColor = FloraChainTheme.colors.primary,
    unfocusedLabelColor = FloraChainTheme.colors.textMuted,
    cursorColor = FloraChainTheme.colors.primary
)
