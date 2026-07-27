package com.silverymusic.app.theme

import androidx.compose.ui.graphics.Color

// Surfaces
val SilveryBackground = Color(0xFF121212)
val SilverySurface = Color(0xFF262626)
val SilverySurfaceAlt = Color(0xFF1E1E1E)
val SilveryMiniPlayerSurface = Color(0xFF1F1F1F)
val SilveryArtPlaceholder = Color(0xFF404040)
val SilveryBorder = Color(0xFF4D4D4D)

// Text
val SilveryTextPrimary = Color(0xFFFFFFFF)
val SilveryTextSecondary = Color(0xFF999999)
val SilveryTextTertiary = Color(0xFF8C8C8C)
val SilveryTextMuted = Color(0xFF808080)
val SilveryIconInactive = Color(0xFF737373)

// Nav bar inactive state, per Figma rgba(255,255,255,0.35)
val SilveryNavInactive = Color(0x59FFFFFF)

// Accent — the silver/white orb motif from onboarding and selection states
val SilveryAccent = Color(0xFFE6E6E6)
val SilveryAccentDim = Color(0xFFB8BCC4)

// Semantic
val SilveryLiveDot = Color(0xFF4CD964)
val SilveryLiked = Color(0xFFE0526C)

/**
 * Muted avatar tints for profiles. Desaturated on purpose — they need to read as
 * distinct at a glance without breaking the Visual Serenity principle.
 */
val ProfileAccents = listOf(
    Color(0xFF4A6B7C),
    Color(0xFF6B5B7C),
    Color(0xFF7C6B4A),
    Color(0xFF4A7C5E),
    Color(0xFF7C4A5E),
    Color(0xFF5A5F7C),
)
