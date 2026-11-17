package com.example

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.test.core.app.ApplicationProvider
import com.example.ui.theme.AccentColorChoice
import com.example.ui.theme.AppThemePreset
import com.example.ui.theme.buildKoraColorScheme
import com.example.ui.util.PreferencesManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ThemeSwitcherTest {

  private lateinit var prefs: PreferencesManager

  @Before
  fun setup() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    prefs = PreferencesManager(context)
  }

  @Test
  fun testThemePreset_fromId_fallback() {
    assertEquals(AppThemePreset.DISCORD_DARK, AppThemePreset.fromId("discord_dark"))
    assertEquals(AppThemePreset.OLED_MIDNIGHT, AppThemePreset.fromId("oled_midnight"))
    assertEquals(AppThemePreset.NITRO_MAGENTA, AppThemePreset.fromId("nitro_magenta"))
    assertEquals(AppThemePreset.CYBER_EMERALD, AppThemePreset.fromId("cyber_emerald"))
    assertEquals(AppThemePreset.CRIMSON_RUBY, AppThemePreset.fromId("crimson_ruby"))
    assertEquals(AppThemePreset.SOLAR_AMBER, AppThemePreset.fromId("solar_amber"))
    assertEquals(AppThemePreset.DISCORD_LIGHT, AppThemePreset.fromId("discord_light"))
    // Fallback on unknown
    assertEquals(AppThemePreset.DISCORD_DARK, AppThemePreset.fromId("unknown_preset_123"))
    assertEquals(AppThemePreset.DISCORD_DARK, AppThemePreset.fromId(null))
  }

  @Test
  fun testAccentColorChoice_fromId_fallback() {
    assertEquals(AccentColorChoice.BLURPLE, AccentColorChoice.fromId("blurple"))
    assertEquals(AccentColorChoice.NITRO_PINK, AccentColorChoice.fromId("nitro_pink"))
    assertEquals(AccentColorChoice.CYAN, AccentColorChoice.fromId("cyan"))
    assertEquals(AccentColorChoice.EMERALD, AccentColorChoice.fromId("emerald"))
    assertEquals(AccentColorChoice.GOLD, AccentColorChoice.fromId("gold"))
    assertEquals(AccentColorChoice.VIOLET, AccentColorChoice.fromId("violet"))
    assertEquals(AccentColorChoice.RED, AccentColorChoice.fromId("red"))
    assertEquals(AccentColorChoice.DEFAULT, AccentColorChoice.fromId("unknown_color"))
  }

  @Test
  fun testPreferencesManager_persistsThemeProperties() {
    prefs.themePreset = AppThemePreset.NITRO_MAGENTA
    assertEquals(AppThemePreset.NITRO_MAGENTA, prefs.themePreset)

    prefs.amoledPureBlack = true
    assertTrue(prefs.amoledPureBlack)

    prefs.accentChoice = AccentColorChoice.CYAN
    assertEquals(AccentColorChoice.CYAN, prefs.accentChoice)

    // Reset
    prefs.themePreset = AppThemePreset.DISCORD_DARK
    prefs.amoledPureBlack = false
    prefs.accentChoice = AccentColorChoice.DEFAULT
    assertEquals(AppThemePreset.DISCORD_DARK, prefs.themePreset)
    assertFalse(prefs.amoledPureBlack)
    assertEquals(AccentColorChoice.DEFAULT, prefs.accentChoice)
  }

  @Test
  fun testBuildKoraColorScheme_amoledModeProducesTrueBlack() {
    val (schemeDark, _) = buildKoraColorScheme(
      preset = AppThemePreset.DISCORD_DARK,
      isSystemDark = true,
      amoledPureBlack = true,
      accentChoice = AccentColorChoice.DEFAULT
    )
    assertEquals(Color(0xFF000000), schemeDark.background)

    val (schemeOled, _) = buildKoraColorScheme(
      preset = AppThemePreset.OLED_MIDNIGHT,
      isSystemDark = true,
      amoledPureBlack = false,
      accentChoice = AccentColorChoice.DEFAULT
    )
    assertEquals(Color(0xFF000000), schemeOled.background)
  }

  @Test
  fun testBuildKoraColorScheme_customAccentApplied() {
    val (scheme, extended) = buildKoraColorScheme(
      preset = AppThemePreset.DISCORD_DARK,
      isSystemDark = true,
      amoledPureBlack = false,
      accentChoice = AccentColorChoice.NITRO_PINK
    )
    assertEquals(AccentColorChoice.NITRO_PINK.color, scheme.primary)
    assertEquals(AccentColorChoice.NITRO_PINK.color, extended.deepIndigoAccent)
  }
}
