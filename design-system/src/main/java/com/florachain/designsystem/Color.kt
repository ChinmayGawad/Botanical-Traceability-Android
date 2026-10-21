package com.florachain.designsystem

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ==========================================
// 🌿 Primary Botanical & Neon Accents
// ==========================================
val BotanicalEmerald = Color(0xFF10B981)
val BotanicalDark = Color(0xFF064E3B)
val BotanicalLight = Color(0xFFD1FAE5)
val MintAccent = Color(0xFF34D399)
val ForestGreen = Color(0xFF047857)
val HarvestGold = Color(0xFFD97706)
val HarvestGoldLight = Color(0xFFFEF3C7)

// ==========================================
// 🌙 Cyber-Slate Dark Mode Tokens
// ==========================================
val DarkBackground = Color(0xFF090E17)
val DeepSlate = Color(0xFF0F172A)
val SurfaceDark = Color(0xFF131D31)
val SurfaceDarkElevated = Color(0xFF1E2D4A)
val GlassBorderDark = Color(0x4010B981) // 25% emerald
val GlassSurfaceDark = Color(0xBF131D31) // Translucent obsidian

// ==========================================
// ☀️ Fresh Biophilic Light Mode Tokens
// ==========================================
val LightBackground = Color(0xFFF3FAF5)
val SurfaceLight = Color(0xFFFFFFFF)
val SurfaceLightElevated = Color(0xFFE9F7EF)
val GlassBorderLight = Color(0xFFBCECD2) // Soft fresh emerald border
val GlassSurfaceLight = Color(0xF2FFFFFF) // Frosted white tint
val LightTextPrimary = Color(0xFF0E2A1E)
val LightTextSecondary = Color(0xFF355245)
val LightTextMuted = Color(0xFF68877A)

// ==========================================
// 🛡️ Status & Trust Indicators (Both Modes)
// ==========================================
val TrustGreen = Color(0xFF059669)
val TrustGreenLight = Color(0xFFD1FAE5)
val WarningAmber = Color(0xFFF59E0B)
val WarningAmberLight = Color(0xFFFEF3C7)
val ErrorRose = Color(0xFFF43F5E)
val ErrorRoseLight = Color(0xFFFFE4E6)
val InfoBlue = Color(0xFF38BDF8)
val InfoBlueLight = Color(0xFFE0F2FE)

// ==========================================
// 📝 Legacy Neutral Compatibility
// ==========================================
val GlassBorder = GlassBorderDark
val GlassSurface = GlassSurfaceDark
val TextPrimary = Color(0xFFF8FAFC)
val TextSecondary = Color(0xFF94A3B8)
val TextMuted = Color(0xFF64748B)

// ==========================================
// 🎨 Dynamic Gradients & Glow Brushes
// ==========================================
val EmeraldGradient = Brush.linearGradient(
    colors = listOf(BotanicalEmerald, MintAccent)
)

val HeroGlowDark = Brush.radialGradient(
    colors = listOf(Color(0x3310B981), Color(0x00090E17)),
    radius = 600f
)

val HeroGlowLight = Brush.radialGradient(
    colors = listOf(Color(0x3310B981), Color(0x00F3FAF5)),
    radius = 600f
)

val LaserSweepGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0x0010B981),
        Color(0x4010B981),
        BotanicalEmerald,
        MintAccent,
        Color(0x4010B981),
        Color(0x0010B981)
    )
)
