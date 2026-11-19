package com.example.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ==========================================
// Kora Luminous Brand & Accent Colors
// ==========================================
val DiscordBlurple = Color(0xFF6366F1)        // Luminous Sapphire Indigo
val DiscordBlurpleDark = Color(0xFF4338CA)    // Deep Pressed Indigo
val DiscordBlurpleLight = Color(0xFF818CF8)   // Soft Highlight Indigo
val DiscordNitroPink = Color(0xFFEC4899)      // Rose Quartz Accent
val KoraCyanAccent = Color(0xFF22D3EE)        // Fresh Cyan Highlight

// Aliases for Indigo scale compatibility
val Indigo700 = Color(0xFF3730A3)
val Indigo600 = DiscordBlurpleDark
val Indigo500 = DiscordBlurple
val Indigo400 = DiscordBlurple
val Indigo300 = DiscordBlurpleLight
val Indigo200 = Color(0xFFA5B4FC)
val Indigo100 = Color(0xFFC7D2FE)
val Indigo50 = Color(0xFFECEFFD)

// ==========================================
// Kora Midnight Obsidian Surfaces & Neutrals
// ==========================================
val DiscordDarkPrimary = Color(0xFF0B0F19)   // Rich Midnight Canvas
val DiscordDarkSecondary = Color(0xFF131A29) // Elevated Silk Surface Cards & Bars
val DiscordDarkElevated = Color(0xFF192235)  // Popups, Sheets, Highlight Cards
val DiscordDarkInput = Color(0xFF1C263B)     // Search Bars & Inputs
val DiscordDarkDeepest = Color(0xFF070A12)   // Bottom Navigation & Deepest Anchor

val DiscordBorder = Color(0xFF27344D)        // Crisp Hairline Border
val DiscordHover = Color(0xFF1E2940)         // Subtle Interactive Layer
val DiscordDivider = Color(0x33334155)       // Clean Hairline Divider

// Slate scale mapped cleanly
val Slate950 = DiscordDarkDeepest
val Slate900 = DiscordDarkSecondary
val Slate850 = DiscordDarkElevated
val Slate800 = DiscordDarkPrimary
val Slate700 = DiscordBorder
val Slate600 = Color(0xFF475569)
val Slate500 = Color(0xFF64748B)
val Slate400 = Color(0xFF94A3B8)
val Slate300 = Color(0xFFCBD5E1)
val Slate200 = Color(0xFFE2E8F0)
val Slate100 = Color(0xFFF1F5F9)
val Slate50 = Color(0xFFF8FAFC)
val PureWhite = Color(0xFFFFFFFF)

// ==========================================
// Crisp High-Contrast Foregrounds
// ==========================================
val DiscordTextPrimary = Color(0xFFF8FAFC)
val DiscordTextNormal = Color(0xFFDCE4F0)
val DiscordTextMuted = Color(0xFF94A3B8)
val DiscordTextInteractive = Color(0xFFCBD5E1)

// ==========================================
// Status & Semantic Tokens
// ==========================================
val StatusOnline = Color(0xFF10B981)         // Emerald Green
val StatusQuiet = Color(0xFFF59E0B)          // Warm Amber
val StatusOffline = Color(0xFF64748B)        // Neutral Slate
val StatusDnd = Color(0xFFEF4444)            // Coral Red
val DiscordRed = Color(0xFFEF4444)
val DiscordGreen = Color(0xFF10B981)
val DiscordYellow = Color(0xFFF59E0B)
val SemanticError = Color(0xFFEF4444)
val SemanticSuccess = Color(0xFF10B981)

@Immutable
data class KoraExtendedColors(
  val deepIndigoAccent: Color,
  val statusOnline: Color,
  val statusQuiet: Color,
  val statusOffline: Color,
  val subtleBorder: Color,
  val cardBackground: Color
)

val LocalKoraExtendedColors = staticCompositionLocalOf {
  KoraExtendedColors(
    deepIndigoAccent = DiscordBlurple,
    statusOnline = StatusOnline,
    statusQuiet = StatusQuiet,
    statusOffline = StatusOffline,
    subtleBorder = DiscordBorder,
    cardBackground = DiscordDarkSecondary
  )
}
