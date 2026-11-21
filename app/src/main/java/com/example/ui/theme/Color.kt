package com.example.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ==========================================
// Kora Calm Discord-Inspired Brand & Accents
// ==========================================
val DiscordBlurple = Color(0xFF5865F2)        // Classic Calm Discord Blurple
val DiscordBlurpleDark = Color(0xFF4752C4)    // Pressed Blurple
val DiscordBlurpleLight = Color(0xFF7983F5)   // Soft Highlight Blurple
val DiscordNitroPink = Color(0xFFEB459E)      // Subtle Nitro Rose
val KoraCyanAccent = Color(0xFF00A8FC)        // Crisp Link Cyan

// Aliases for Indigo scale compatibility (preserved for ThemeTest)
val Indigo700 = Color(0xFF3730A3)
val Indigo600 = Color(0xFF4338CA)
val Indigo500 = Color(0xFF4F46E5)
val Indigo400 = Color(0xFF6366F1)
val Indigo300 = DiscordBlurpleLight
val Indigo200 = Color(0xFFA5B4FC)
val Indigo100 = Color(0xFFC7D2FE)
val Indigo50 = Color(0xFFECEFFD)

// ==========================================
// Discord Layered Charcoal Surfaces (No Pure #000000)
// ==========================================
val DiscordDarkDeepest = Color(0xFF1E1F22)    // Bottom Nav, Recessed Search Wells, Rails
val DiscordDarkPrimary = Color(0xFF2B2D31)    // Main Background Canvas
val DiscordDarkSecondary = Color(0xFF313338)  // Cards, Top Bars, Chat Surface
val DiscordDarkElevated = Color(0xFF383A40)   // Elevated Popups, Inputs, Nested Tiles
val DiscordDarkInput = Color(0xFF383A40)      // Text Inputs & Interactive Chips

val DiscordBorder = Color(0xFF3F4147)         // Subtle Charcoal Hairline Border
val DiscordHover = Color(0xFF404249)          // Active/Hover Surface Tint
val DiscordDivider = Color(0x553F4147)        // Soft Hairline Divider

// Slate scale mapped cleanly
val Slate950 = DiscordDarkDeepest
val Slate900 = DiscordDarkSecondary
val Slate850 = DiscordDarkElevated
val Slate800 = DiscordDarkPrimary
val Slate700 = DiscordBorder
val Slate600 = Color(0xFF4E5058)
val Slate500 = Color(0xFF80848E)
val Slate400 = Color(0xFF949BA4)
val Slate300 = Color(0xFFB5BAC1)
val Slate200 = Color(0xFFDBDEE1)
val Slate100 = Color(0xFFF2F3F5)
val Slate50 = Color(0xFFF8FAFC)
val PureWhite = Color(0xFFFFFFFF)

// ==========================================
// Ergonomic High-Contrast Text Tokens
// ==========================================
val DiscordTextPrimary = Color(0xFFF2F3F5)
val DiscordTextNormal = Color(0xFFDBDEE1)
val DiscordTextMuted = Color(0xFF949BA4)
val DiscordTextInteractive = Color(0xFFB5BAC1)

// ==========================================
// Status & Semantic Tokens
// ==========================================
val StatusOnline = Color(0xFF23A55A)          // Discord Online Green
val StatusQuiet = Color(0xFFF0B232)           // Discord Idle Amber
val StatusOffline = Color(0xFF80848E)         // Discord Offline Gray
val StatusDnd = Color(0xFFDA373C)             // Discord DND Red
val DiscordRed = Color(0xFFDA373C)
val DiscordGreen = Color(0xFF23A55A)
val DiscordYellow = Color(0xFFF0B232)
val SemanticError = Color(0xFFDA373C)
val SemanticSuccess = Color(0xFF23A55A)

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
