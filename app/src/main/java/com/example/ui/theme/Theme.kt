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
import androidx.compose.ui.graphics.luminance

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

  val onPrimaryColor = if (basePrimary.luminance() > 0.52f) {
    Color(0xFF0B0F19)
  } else {
    PureWhite
  }

  val baseBg = if (isDark) {
    if (amoledPureBlack || preset == AppThemePreset.OLED_MIDNIGHT) Color(0xFF000000)
    else preset.backgroundColor
  } else {
    Color(0xFFF6F8FC)
  }

  val baseSurface = if (isDark) {
    if (amoledPureBlack || preset == AppThemePreset.OLED_MIDNIGHT) Color(0xFF0D1117)
    else preset.surfaceColor
  } else {
    PureWhite
  }

  val baseInput = if (isDark) {
    if (amoledPureBlack || preset == AppThemePreset.OLED_MIDNIGHT) Color(0xFF161B24)
    else when (preset) {
      AppThemePreset.NITRO_MAGENTA -> Color(0xFF281C34)
      AppThemePreset.CYBER_EMERALD -> Color(0xFF182E28)
      AppThemePreset.CRIMSON_RUBY -> Color(0xFF301A20)
      AppThemePreset.SOLAR_AMBER -> Color(0xFF2E2618)
      else -> DiscordDarkInput
    }
  } else {
    Color(0xFFEDF1F7)
  }

  val baseBorder = if (isDark) {
    if (amoledPureBlack || preset == AppThemePreset.OLED_MIDNIGHT) Color(0xFF212836)
    else when (preset) {
      AppThemePreset.NITRO_MAGENTA -> Color(0xFF3B294D)
      AppThemePreset.CYBER_EMERALD -> Color(0xFF224238)
      AppThemePreset.CRIMSON_RUBY -> Color(0xFF45242C)
      AppThemePreset.SOLAR_AMBER -> Color(0xFF423622)
      else -> DiscordBorder
    }
  } else {
    Color(0xFFDCE3EE)
  }

  val colorScheme = if (isDark) {
    darkColorScheme(
      primary = basePrimary,
      onPrimary = onPrimaryColor,
      primaryContainer = basePrimary.copy(alpha = 0.20f),
      onPrimaryContainer = DiscordTextPrimary,
      secondary = KoraCyanAccent,
      onSecondary = Color(0xFF0B0F19),
      secondaryContainer = baseInput,
      onSecondaryContainer = DiscordTextPrimary,
      tertiary = basePrimary,
      onTertiary = onPrimaryColor,
      background = baseBg,
      onBackground = DiscordTextPrimary,
      surface = baseSurface,
      onSurface = DiscordTextPrimary,
      surfaceVariant = baseInput,
      onSurfaceVariant = DiscordTextMuted,
      surfaceContainerLow = baseBg,
      surfaceContainer = baseSurface,
      surfaceContainerHigh = baseInput,
      inverseSurface = Color(0xFF1E293B),
      inverseOnSurface = DiscordTextPrimary,
      inversePrimary = basePrimary,
      outline = baseBorder,
      outlineVariant = baseBorder.copy(alpha = 0.7f),
      error = SemanticError,
      onError = PureWhite
    )
  } else {
    lightColorScheme(
      primary = basePrimary,
      onPrimary = onPrimaryColor,
      primaryContainer = basePrimary.copy(alpha = 0.14f),
      onPrimaryContainer = basePrimary,
      secondary = Color(0xFF0891B2),
      onSecondary = PureWhite,
      secondaryContainer = Color(0xFFEFF3FA),
      onSecondaryContainer = Color(0xFF0F172A),
      tertiary = basePrimary,
      onTertiary = onPrimaryColor,
      background = baseBg,
      onBackground = Color(0xFF0F172A),
      surface = baseSurface,
      onSurface = Color(0xFF0F172A),
      surfaceVariant = baseInput,
      onSurfaceVariant = Color(0xFF475569),
      surfaceContainerLow = Color(0xFFF1F5F9),
      surfaceContainer = PureWhite,
      surfaceContainerHigh = baseInput,
      inverseSurface = Color(0xFF0F172A),
      inverseOnSurface = PureWhite,
      inversePrimary = basePrimary,
      outline = baseBorder,
      outlineVariant = Color(0xFFE2E8F0),
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
