package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color

/**
 * Builds dynamic color schemes based on AppThemePreset, AMOLED black mode, and custom accent choice.
 */
fun buildKoraColorScheme(
  preset: AppThemePreset,
  isSystemDark: Boolean,
  amoledPureBlack: Boolean,
  accentChoice: AccentColorChoice
): Pair<ColorScheme, KoraExtendedColors> {
  val isDark = when (preset) {
    AppThemePreset.SYSTEM -> isSystemDark
    else -> preset.isDark
  }

  val basePrimary = if (accentChoice != AccentColorChoice.DEFAULT) {
    accentChoice.color
  } else {
    preset.primaryColor
  }

  val baseBg = if (isDark) {
    if (amoledPureBlack || preset == AppThemePreset.OLED_MIDNIGHT) Color(0xFF000000)
    else preset.backgroundColor
  } else {
    PureWhite
  }

  val baseSurface = if (isDark) {
    if (amoledPureBlack || preset == AppThemePreset.OLED_MIDNIGHT) Color(0xFF0C0E12)
    else preset.surfaceColor
  } else {
    Color(0xFFF2F3F5)
  }

  val baseInput = if (isDark) {
    if (amoledPureBlack || preset == AppThemePreset.OLED_MIDNIGHT) Color(0xFF16181D)
    else when (preset) {
      AppThemePreset.NITRO_MAGENTA -> Color(0xFF2C1E3C)
      AppThemePreset.CYBER_EMERALD -> Color(0xFF1B3027)
      AppThemePreset.CRIMSON_RUBY -> Color(0xFF341B21)
      AppThemePreset.SOLAR_AMBER -> Color(0xFF34281A)
      else -> DiscordDarkInput
    }
  } else {
    Color(0xFFE3E5E8)
  }

  val baseBorder = if (isDark) {
    if (amoledPureBlack || preset == AppThemePreset.OLED_MIDNIGHT) Color(0xFF22262E)
    else when (preset) {
      AppThemePreset.NITRO_MAGENTA -> Color(0xFF452D5C)
      AppThemePreset.CYBER_EMERALD -> Color(0xFF28483B)
      AppThemePreset.CRIMSON_RUBY -> Color(0xFF4A252E)
      AppThemePreset.SOLAR_AMBER -> Color(0xFF4A3824)
      else -> DiscordBorder
    }
  } else {
    Color(0xFFDCDDDE)
  }

  val colorScheme = if (isDark) {
    darkColorScheme(
      primary = basePrimary,
      onPrimary = PureWhite,
      primaryContainer = basePrimary.copy(alpha = 0.35f),
      onPrimaryContainer = DiscordTextPrimary,
      secondary = DiscordTextInteractive,
      onSecondary = DiscordDarkDeepest,
      secondaryContainer = baseInput,
      onSecondaryContainer = DiscordTextPrimary,
      tertiary = basePrimary,
      onTertiary = PureWhite,
      background = baseBg,
      onBackground = DiscordTextPrimary,
      surface = baseSurface,
      onSurface = DiscordTextPrimary,
      surfaceVariant = baseInput,
      onSurfaceVariant = DiscordTextNormal,
      outline = baseBorder,
      outlineVariant = if (amoledPureBlack) Color(0xFF14161B) else DiscordDarkElevated,
      error = SemanticError,
      onError = PureWhite
    )
  } else {
    lightColorScheme(
      primary = basePrimary,
      onPrimary = PureWhite,
      primaryContainer = basePrimary.copy(alpha = 0.15f),
      onPrimaryContainer = basePrimary,
      secondary = Slate600,
      onSecondary = PureWhite,
      secondaryContainer = Slate100,
      onSecondaryContainer = DiscordDarkDeepest,
      tertiary = basePrimary,
      onTertiary = PureWhite,
      background = PureWhite,
      onBackground = Color(0xFF060607),
      surface = Color(0xFFF2F3F5),
      onSurface = Color(0xFF060607),
      surfaceVariant = Color(0xFFE3E5E8),
      onSurfaceVariant = Color(0xFF2E3338),
      outline = Color(0xFFDCDDDE),
      outlineVariant = Color(0xFFE3E5E8),
      error = SemanticError,
      onError = PureWhite
    )
  }

  val extendedColors = KoraExtendedColors(
    deepIndigoAccent = basePrimary,
    statusOnline = StatusOnline,
    statusQuiet = StatusQuiet,
    statusOffline = StatusOffline,
    subtleBorder = baseBorder,
    cardBackground = baseSurface
  )

  return colorScheme to extendedColors
}

/**
 * Centralized design system accessor for Kora.
 * Provides colors, typography, shapes, consistent spacing units, and extended semantic tokens.
 */
object KoraTheme {
  val colors: ColorScheme
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme

  val typography: Typography
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.typography

  val shapes: Shapes
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.shapes

  val spacing: KoraSpacing
    @Composable
    @ReadOnlyComposable
    get() = LocalKoraSpacing.current

  val extendedColors: KoraExtendedColors
    @Composable
    @ReadOnlyComposable
    get() = LocalKoraExtendedColors.current
}

@Composable
fun KoraTheme(
  themePreset: AppThemePreset = AppThemePreset.DISCORD_DARK,
  amoledPureBlack: Boolean = false,
  accentChoice: AccentColorChoice = AccentColorChoice.DEFAULT,
  darkTheme: Boolean = isSystemInDarkTheme(),
  spacing: KoraSpacing = KoraSpacing(),
  content: @Composable () -> Unit,
) {
  val (colorScheme, extendedColors) = remember(themePreset, amoledPureBlack, accentChoice, darkTheme) {
    buildKoraColorScheme(
      preset = themePreset,
      isSystemDark = darkTheme,
      amoledPureBlack = amoledPureBlack,
      accentChoice = accentChoice
    )
  }

  CompositionLocalProvider(
    LocalKoraSpacing provides spacing,
    LocalKoraExtendedColors provides extendedColors
  ) {
    MaterialTheme(
      colorScheme = colorScheme,
      typography = Typography,
      shapes = Shapes,
      content = content
    )
  }
}
