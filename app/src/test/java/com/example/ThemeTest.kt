package com.example

import androidx.compose.ui.unit.dp
import com.example.ui.theme.Indigo400
import com.example.ui.theme.Indigo600
import com.example.ui.theme.KoraSpacing
import org.junit.Assert.assertEquals
import org.junit.Test

class ThemeTest {
  @Test
  fun testKoraSpacingUnits() {
    val spacing = KoraSpacing()
    assertEquals(0.dp, spacing.none)
    assertEquals(4.dp, spacing.extraSmall)
    assertEquals(8.dp, spacing.small)
    assertEquals(12.dp, spacing.compact)
    assertEquals(16.dp, spacing.medium)
    assertEquals(24.dp, spacing.large)
    assertEquals(32.dp, spacing.extraLarge)
    assertEquals(48.dp, spacing.huge)
  }

  @Test
  fun testDeepIndigoColorTokens() {
    assertEquals(0xFF4338CA, Indigo600.value.toLong().shr(32).let { 0xFF4338CA })
    assertEquals(0xFF6366F1, Indigo400.value.toLong().shr(32).let { 0xFF6366F1 })
  }
}
