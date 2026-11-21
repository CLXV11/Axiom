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

  val baseDeepest = if (isDark) {
    if (amoledPureBlack || preset == AppThemePreset.OLED_MIDNIGHT) Color(0xFF000000)
    else when (preset) {
      AppThemePreset.NITRO_MAGENTA -> Color(0xFF1A1420)
      AppThemePreset.CYBER_EMERALD -> Color(0xFF17201E)
      AppThemePreset.CRIMSON_RUBY -> Color(0xFF1F171A)
      AppThemePreset.SOLAR_AMBER -> Color(0xFF1E1B15)
      else -> DiscordDarkDeepest
    }
  } else {
    Color(0xFFE3E5E8)
  }

  val baseInput = if (isDark) {
    if (amoledPureBlack || preset == AppThemePreset.OLED_MIDNIGHT) Color(0xFF161B24)
    else when (preset) {
      AppThemePreset.NITRO_MAGENTA -> Color(0xFF372E40)
      AppThemePreset.CYBER_EMERALD -> Color(0xFF30403B)
      AppThemePreset.CRIMSON_RUBY -> Color(0xFF3D2E33)
      AppThemePreset.SOLAR_AMBER -> Color(0xFF3C362B)
      else -> DiscordDarkInput
    }
  } else {
    Color(0xFFE3E5E8)
  }

  val baseBorder = if (isDark) {
    if (amoledPureBlack || preset == AppThemePreset.OLED_MIDNIGHT) Color(0xFF212836)
    else when (preset) {
      AppThemePreset.NITRO_MAGENTA -> Color(0xFF42374D)
      AppThemePreset.CYBER_EMERALD -> Color(0xFF3A4C46)
      AppThemePreset.CRIMSON_RUBY -> Color(0xFF4A383E)
      AppThemePreset.SOLAR_AMBER -> Color(0xFF484134)
      else -> DiscordBorder
    }
  } else {
    Color(0xFFD4D7DC)
  }

  val colorScheme = if (isDark) {
    darkColorScheme(
      primary = basePrimary,
      onPrimary = onPrimaryColor,
      primaryContainer = basePrimary.copy(alpha = 0.18f),
      onPrimaryContainer = DiscordTextPrimary,
      secondary = KoraCyanAccent,
      onSecondary = Color(0xFF1E1F22),
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
      surfaceContainerLowest = baseDeepest,
      surfaceContainerLow = baseDeepest,
      surfaceContainer = baseSurface,
      surfaceContainerHigh = baseInput,
      inverseSurface = Color(0xFF232428),
      inverseOnSurface = DiscordTextPrimary,
      inversePrimary = basePrimary,
      outline = baseBorder,
      outlineVariant = baseBorder,
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
  amoledPureBlack: Boolean = true,
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
