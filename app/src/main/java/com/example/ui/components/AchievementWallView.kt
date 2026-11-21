package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.R
import kotlin.math.sin

/**
 * High-impact Achievement & Profile Wall Background view.
 * Combines 4 bespoke generated fantasy/cyberpunk artworks with procedural animated walls
 * for Moonlight Holo and Digital Matrix.
 */
@Composable
fun AchievementWallView(
  styleKey: String,
  modifier: Modifier = Modifier,
  scrimAlpha: Float = 0.25f
) {
  val resId = when (styleKey) {
    "wall_crimson_forest" -> R.drawable.img_wall_crimson_forest
    "wall_dusk_castle" -> R.drawable.img_wall_dusk_castle
    "wall_neon_nexus" -> R.drawable.img_wall_neon_nexus
    "wall_dragon_throne" -> R.drawable.img_wall_dragon_throne
    else -> null
  }

  Box(modifier = modifier) {
    if (resId != null) {
      Image(
        painter = painterResource(id = resId),
        contentDescription = styleKey,
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize()
      )
    } else {
      val infiniteTransition = rememberInfiniteTransition(label = "wall_fx")
      val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
          animation = tween(5000, easing = LinearEasing),
          repeatMode = RepeatMode.Restart
        ),
        label = "phase"
      )

      Canvas(modifier = Modifier.fillMaxSize()) {
        if (styleKey == "wall_digital_matrix") {
          drawMatrixWall(phase)
        } else {
          drawMoonlightHoloWall(phase)
        }
      }
    }

    if (scrimAlpha > 0f) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(
            Brush.verticalGradient(
              listOf(
                Color.Black.copy(alpha = scrimAlpha * 0.6f),
                Color.Black.copy(alpha = scrimAlpha)
              )
            )
          )
      )
    }
  }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawDigitalRainColumn(
  x: Float,
  colIdx: Int,
  phase: Float
) {
  val steps = 11
  val offset = ((phase + colIdx * 0.17f) % 1f)
  for (s in 0 until steps) {
    val normY = ((s.toFloat() / steps) + offset) % 1f
    val y = normY * size.height
    val isHead = s == steps - 1
    val alpha = if (isHead) 0.95f else (0.18f + 0.55f * (s.toFloat() / steps))
    val glyphW = (size.width / 26f).coerceAtLeast(3f)
    val glyphH = (size.height / 16f).coerceAtLeast(4f)
    drawRect(
      color = if (isHead) Color(0xFFDCFCE7) else Color(0xFF22C55E).copy(alpha = alpha),
      topLeft = Offset(x, y),
      size = androidx.compose.ui.geometry.Size(glyphW * 0.7f, glyphH * 0.65f)
    )
  }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawMatrixWall(phase: Float) {
  drawRect(
    brush = Brush.radialGradient(
      listOf(Color(0xFF064E3B), Color(0xFF022C22), Color(0xFF010A06)),
      center = Offset(size.width * 0.5f, size.height * 0.45f),
      radius = size.maxDimension * 0.85f
    )
  )
  val cols = 16
  for (c in 0 until cols) {
    val x = (c + 0.35f) * (size.width / cols)
    drawDigitalRainColumn(x, c, phase)
  }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawMoonlightHoloWall(phase: Float) {
  drawRect(
    brush = Brush.verticalGradient(
      listOf(Color(0xFF090D16), Color(0xFF1E1B4B), Color(0xFF312E81), Color(0xFF1C1917))
    )
  )
  // Full Harvest Moon
  val moon = Offset(size.width * 0.5f, size.height * 0.30f)
  drawCircle(
    brush = Brush.radialGradient(listOf(Color(0xBBFEF08A), Color(0x33F59E0B), Color.Transparent), center = moon, radius = size.minDimension * 0.45f),
    radius = size.minDimension * 0.45f,
    center = moon
  )
  drawCircle(color = Color(0xFFFEF3C7), radius = size.minDimension * 0.16f, center = moon)

  // Rolling mist hills
  val hills = Path().apply {
    moveTo(0f, size.height * 0.72f)
    quadraticTo(size.width * 0.3f, size.height * 0.56f, size.width * 0.6f, size.height * 0.70f)
    quadraticTo(size.width * 0.85f, size.height * 0.80f, size.width, size.height * 0.64f)
    lineTo(size.width, size.height)
    lineTo(0f, size.height)
    close()
  }
  drawPath(hills, color = Color(0xFF0F172A))

  // Glowing Halloween pumpkins along bottom ridge
  val pulse = 0.94f + 0.08f * sin(phase * 6.28f)
  drawJackOLantern(Offset(size.width * 0.22f, size.height * 0.82f), size.minDimension * 0.10f, pulse)
  drawJackOLantern(Offset(size.width * 0.50f, size.height * 0.84f), size.minDimension * 0.12f, pulse)
  drawJackOLantern(Offset(size.width * 0.78f, size.height * 0.80f), size.minDimension * 0.10f, pulse)
}
