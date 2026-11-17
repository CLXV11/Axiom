package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class KoraSpacing(
  val none: Dp = 0.dp,
  val extraSmall: Dp = 4.dp,
  val small: Dp = 8.dp,
  val compact: Dp = 12.dp,
  val medium: Dp = 16.dp,
  val large: Dp = 24.dp,
  val extraLarge: Dp = 32.dp,
  val huge: Dp = 48.dp
)

val LocalKoraSpacing = staticCompositionLocalOf { KoraSpacing() }

val MaterialTheme.spacing: KoraSpacing
  @Composable
  @ReadOnlyComposable
  get() = LocalKoraSpacing.current
