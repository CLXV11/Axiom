package com.example.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ==========================================
// Discord Signature Brand & Accent Colors
// ==========================================
val DiscordBlurple = Color(0xFF5865F2)        // Iconic Discord Blurple (Vibrant, high-contrast)
val DiscordBlurpleDark = Color(0xFF4752C4)    // Active / Pressed Blurple
val DiscordBlurpleLight = Color(0xFF7983F5)   // Subtle highlight Blurple
val DiscordNitroPink = Color(0xFFEB459E)      // Discord Nitro Accent

// Aliases for Indigo scale compatibility
val Indigo700 = Color(0xFF3C45A5)
val Indigo600 = DiscordBlurpleDark
val Indigo500 = DiscordBlurple
val Indigo400 = DiscordBlurple
val Indigo300 = DiscordBlurpleLight
val Indigo200 = Color(0xFFA5B4FC)
val Indigo100 = Color(0xFFC7D2FE)
val Indigo50 = Color(0xFFECEFFD)

// ==========================================
// Discord Dark Theme Surfaces & Neutral Scale
// ==========================================
val DiscordDarkPrimary = Color(0xFF313338)   // Signature Discord Chat Background (Canvas)
val DiscordDarkSecondary = Color(0xFF2B2D31) // Discord Sidebar, Headers, Channel Cards
val DiscordDarkElevated = Color(0xFF232428)  // Menus, Popups, Moderation cards
val DiscordDarkInput = Color(0xFF383A40)     // Discord Message Input capsule, embed boxes
val DiscordDarkDeepest = Color(0xFF1E1F22)   // Bottom Navigation bar, active search container

val DiscordBorder = Color(0xFF3F4147)        // Discord crisp border / outline
val DiscordHover = Color(0xFF35373C)         // Discord active / hovered channel row
val DiscordDivider = Color(0x264E5058)       // Subtle channel / item divider

// Backward-compatible slate mapping to rich Discord dark tones
val Slate950 = DiscordDarkDeepest            // #1E1F22
val Slate900 = DiscordDarkSecondary          // #2B2D31
val Slate850 = DiscordDarkElevated           // #232428
val Slate800 = DiscordDarkPrimary            // #313338
val Slate700 = DiscordBorder                 // #3F4147
val Slate600 = Color(0xFF4E5058)
val Slate500 = Color(0xFF80848E)
val Slate400 = Color(0xFF949BA4)             // Discord muted text
val Slate300 = Color(0xFFB5BAC1)             // Discord interactive text
val Slate200 = Color(0xFFDBDEE1)             // Discord normal text
val Slate100 = Color(0xFFE3E5E8)
val Slate50 = Color(0xFFF2F3F5)              // Discord crisp header text
val PureWhite = Color(0xFFFFFFFF)

// ==========================================
// Discord Typography Foregrounds
// ==========================================
val DiscordTextPrimary = Color(0xFFF2F3F5)   // Crisp near-white headers (never faded)
val DiscordTextNormal = Color(0xFFDBDEE1)    // High-contrast clean body text
val DiscordTextMuted = Color(0xFF949BA4)     // Timestamp, channel prefix, placeholder
val DiscordTextInteractive = Color(0xFFB5BAC1) // Hover & button icons

// ==========================================
// Discord Status Indicators
// ==========================================
val StatusOnline = Color(0xFF23A55A)         // Discord Green (Online)
val StatusQuiet = Color(0xFFF0B232)          // Discord Yellow (Idle / Away)
val StatusOffline = Color(0xFF80848E)        // Discord Gray (Invisible / Offline)
val StatusDnd = Color(0xFFF23F43)            // Discord Red (Do Not Disturb)
val DiscordRed = Color(0xFFF23F43)           // Official Discord Red
val DiscordGreen = Color(0xFF23A55A)         // Official Discord Green
val DiscordYellow = Color(0xFFF0B232)        // Official Discord Yellow
val SemanticError = Color(0xFFF23F43)        // Discord Red
val SemanticSuccess = Color(0xFF23A55A)      // Discord Green

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
