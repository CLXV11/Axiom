package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

/**
 * High-detail, multi-layered Avatar Frame overlay with animated aura & vector embellishments.
 */
@Composable
fun AvatarFrameView(
  styleKey: String,
  modifier: Modifier = Modifier,
  isAnimated: Boolean = true
) {
  val infiniteTransition = rememberInfiniteTransition(label = "frame_fx")
  val rotation by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 360f,
    animationSpec = infiniteRepeatable(
      animation = tween(14000, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "rotation"
  )
  val pulse by infiniteTransition.animateFloat(
    initialValue = 0.90f,
    targetValue = 1.10f,
    animationSpec = infiniteRepeatable(
      animation = tween(2200, easing = LinearEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pulse"
  )
  val rVal = if (isAnimated) rotation else 0f
  val pVal = if (isAnimated) pulse else 1f

  Canvas(modifier = modifier.fillMaxSize()) {
    val center = Offset(size.width / 2f, size.height / 2f)
    val radius = size.minDimension / 2f * 0.65f

    when (styleKey) {
      "haunted_spirits" -> drawHauntedSpirits(center, radius, pVal, rVal)
      "cat_bell" -> drawCatBell(center, radius, pVal)
      "violet_night" -> drawVioletNight(center, radius, pVal)
      "violet_wreath" -> drawVioletWreath(center, radius, rVal, pVal)
      "sakura_tide" -> drawSakuraTide(center, radius, rVal)
      "sunlit_pattern" -> drawSunlitPattern(center, radius, rVal)
      "crimson_silver" -> drawCrimsonSilver(center, radius, pVal)
      "skull_eye" -> drawSkullEye(center, radius, pVal)
      "octopus_crown" -> drawOctopusCrown(center, radius, pVal)
      "violet_butterfly" -> drawVioletButterfly(center, radius, pVal)
      "steampunk_engine" -> drawSteampunkEngine(center, radius, rVal, pVal)
      "feather_totem" -> drawFeatherTotem(center, radius, pVal)
      "sunlit_garden" -> drawSunlitGarden(center, radius, pVal)
      "future_mech" -> drawFutureMech(center, radius, rVal)
      "celestial_wings" -> drawCelestialWings(center, radius, pVal)
      "imperial_gold" -> drawImperialGold(center, radius, rVal)
      "dragon_flame" -> drawDragonFlame(center, radius, pVal)
      "cosmic_swirl" -> drawCosmicSwirl(center, radius, rVal, pVal)
      else -> {
        drawCircle(
          brush = Brush.sweepGradient(listOf(Color(0xFFE2E8F0), Color(0xFF6366F1), Color(0xFFE2E8F0))),
          radius = radius,
          center = center,
          style = Stroke(width = 3.dp.toPx())
        )
      }
    }
  }
}

// =========================================================================
// Shared Ornate Vector Motif Primitives (Petals, Roses, Butterflies, Ghosts, Cogs)
// =========================================================================

internal fun DrawScope.drawFourPointStar(center: Offset, radius: Float, color: Color) {
  val p = Path().apply {
    moveTo(center.x, center.y - radius)
    quadraticTo(center.x + radius * 0.16f, center.y - radius * 0.16f, center.x + radius, center.y)
    quadraticTo(center.x + radius * 0.16f, center.y + radius * 0.16f, center.x, center.y + radius)
    quadraticTo(center.x - radius * 0.16f, center.y + radius * 0.16f, center.x - radius, center.y)
    quadraticTo(center.x - radius * 0.16f, center.y - radius * 0.16f, center.x, center.y - radius)
    close()
  }
  drawPath(p, color = color)
}

internal fun DrawScope.drawBaroqueLeaf(
  pos: Offset,
  size: Float,
  angleDeg: Float,
  lightColor: Color,
  darkColor: Color
) {
  rotate(angleDeg, pos) {
    val leaf = Path().apply {
      moveTo(pos.x, pos.y - size)
      quadraticTo(pos.x + size * 0.55f, pos.y - size * 0.3f, pos.x, pos.y + size * 0.45f)
      quadraticTo(pos.x - size * 0.55f, pos.y - size * 0.3f, pos.x, pos.y - size)
      close()
    }
    drawPath(leaf, brush = Brush.linearGradient(listOf(lightColor, darkColor)))
    drawLine(
      color = Color.White.copy(alpha = 0.45f),
      start = Offset(pos.x, pos.y - size * 0.8f),
      end = Offset(pos.x, pos.y + size * 0.35f),
      strokeWidth = 1.dp.toPx()
    )
  }
}

internal fun DrawScope.drawFlowerBlossom(
  center: Offset,
  petalRadius: Float,
  petalColor: Color,
  coreColor: Color,
  petals: Int = 5
) {
  for (i in 0 until petals) {
    val rad = Math.toRadians(i * (360.0 / petals))
    val px = center.x + petalRadius * 0.72f * cos(rad).toFloat()
    val py = center.y + petalRadius * 0.72f * sin(rad).toFloat()
    drawCircle(
      brush = Brush.radialGradient(listOf(Color.White.copy(alpha = 0.9f), petalColor), center = Offset(px, py), radius = petalRadius * 0.75f),
      radius = petalRadius * 0.62f,
      center = Offset(px, py)
    )
  }
  drawCircle(color = coreColor, radius = petalRadius * 0.40f, center = center)
}

internal fun DrawScope.drawLayeredRose(
  center: Offset,
  radius: Float,
  deepColor: Color,
  midColor: Color,
  highlightColor: Color
) {
  drawCircle(brush = Brush.radialGradient(listOf(midColor, deepColor), center = center, radius = radius), radius = radius, center = center)
  for (i in 0 until 5) {
    val rad = Math.toRadians(i * 72.0)
    val p = Offset(center.x + radius * 0.42f * cos(rad).toFloat(), center.y + radius * 0.42f * sin(rad).toFloat())
    drawCircle(color = highlightColor.copy(alpha = 0.75f), radius = radius * 0.46f, center = p, style = Stroke(width = 1.4.dp.toPx()))
  }
  drawCircle(color = highlightColor, radius = radius * 0.30f, center = center)
  drawCircle(color = deepColor, radius = radius * 0.14f, center = center)
}

internal fun DrawScope.drawButterfly(
  center: Offset,
  size: Float,
  tiltDeg: Float,
  lightWing: Color,
  accentWing: Color,
  pulse: Float
) {
  rotate(tiltDeg, center) {
    val flap = pulse.coerceIn(0.88f, 1.08f)
    for (s in listOf(-1f, 1f)) {
      val upper = Path().apply {
        moveTo(center.x, center.y)
        cubicTo(
          center.x + s * size * 0.95f * flap, center.y - size * 0.95f,
          center.x + s * size * 1.10f * flap, center.y - size * 0.10f,
          center.x, center.y + size * 0.12f
        )
        close()
      }
      val lower = Path().apply {
        moveTo(center.x, center.y + size * 0.08f)
        cubicTo(
          center.x + s * size * 0.85f * flap, center.y + size * 0.18f,
          center.x + s * size * 0.65f * flap, center.y + size * 0.90f,
          center.x, center.y + size * 0.40f
        )
        close()
      }
      drawPath(upper, brush = Brush.linearGradient(listOf(lightWing, accentWing)))
      drawPath(lower, brush = Brush.linearGradient(listOf(accentWing, lightWing)))
      drawPath(upper, color = Color.White.copy(alpha = 0.6f), style = Stroke(width = 0.8.dp.toPx()))
    }
    drawLine(
      color = Color(0xFF2E1065),
      start = Offset(center.x, center.y - size * 0.35f),
      end = Offset(center.x, center.y + size * 0.45f),
      strokeWidth = 1.8.dp.toPx(),
      cap = StrokeCap.Round
    )
  }
}

internal fun DrawScope.drawSunflower(center: Offset, r: Float, yellow: Color, orange: Color) {
  for (i in 0 until 14) {
    val rad = Math.toRadians(i * (360.0 / 14.0))
    val tip = Offset(center.x + r * 1.05f * cos(rad).toFloat(), center.y + r * 1.05f * sin(rad).toFloat())
    drawCircle(brush = Brush.radialGradient(listOf(yellow, orange), center = tip, radius = r * 0.4f), radius = r * 0.28f, center = tip)
  }
  drawCircle(color = Color(0xFF451A03), radius = r * 0.56f, center = center)
  drawCircle(color = Color(0xFF92400E), radius = r * 0.36f, center = center)
}

internal fun DrawScope.drawFeather(
  pos: Offset,
  length: Float,
  angleDeg: Float,
  primary: Color,
  tip: Color,
  quill: Color
) {
  rotate(angleDeg, pos) {
    val path = Path().apply {
      moveTo(pos.x, pos.y - length)
      quadraticTo(pos.x + length * 0.34f, pos.y - length * 0.35f, pos.x, pos.y + length * 0.2f)
      quadraticTo(pos.x - length * 0.34f, pos.y - length * 0.35f, pos.x, pos.y - length)
      close()
    }
    drawPath(path, brush = Brush.linearGradient(listOf(tip, primary)))
    drawPath(path, color = quill.copy(alpha = 0.7f), style = Stroke(width = 0.8.dp.toPx()))
    drawLine(
      color = Color.White.copy(alpha = 0.55f),
      start = Offset(pos.x, pos.y - length * 0.85f),
      end = Offset(pos.x, pos.y + length * 0.15f),
      strokeWidth = 0.9.dp.toPx()
    )
  }
}

internal fun DrawScope.drawSilkBow(center: Offset, w: Float, light: Color, dark: Color) {
  for (s in listOf(-1f, 1f)) {
    val wing = Path().apply {
      moveTo(center.x, center.y)
      cubicTo(center.x + s * w * 0.85f, center.y - w * 0.65f, center.x + s * w * 1.05f, center.y + w * 0.45f, center.x, center.y + w * 0.18f)
      close()
    }
    drawPath(wing, brush = Brush.linearGradient(listOf(light, dark)))
    drawPath(wing, color = Color.White.copy(alpha = 0.5f), style = Stroke(width = 0.8.dp.toPx()))
  }
  drawCircle(color = dark, radius = w * 0.22f, center = center)
}

internal fun DrawScope.drawGoldBell(center: Offset, r: Float, bright: Color, deep: Color) {
  drawCircle(brush = Brush.radialGradient(listOf(Color.White, bright, deep), center = Offset(center.x - r * 0.25f, center.y - r * 0.25f), radius = r * 1.4f), radius = r, center = center)
  drawCircle(color = Color(0xFF78350F), radius = r * 0.24f, center = Offset(center.x, center.y + r * 0.36f))
}

internal fun DrawScope.drawCuteGhost(center: Offset, size: Float, tiltDeg: Float, pulse: Float) {
  rotate(tiltDeg, center) {
    val ghost = Path().apply {
      moveTo(center.x - size * 0.65f, center.y + size * 0.65f)
      lineTo(center.x - size * 0.60f, center.y - size * 0.20f)
      cubicTo(center.x - size * 0.55f, center.y - size * 0.95f, center.x + size * 0.55f, center.y - size * 0.95f, center.x + size * 0.60f, center.y - size * 0.20f)
      lineTo(center.x + size * 0.65f, center.y + size * 0.65f)
      lineTo(center.x + size * 0.22f, center.y + size * 0.42f)
      lineTo(center.x, center.y + size * 0.68f)
      lineTo(center.x - size * 0.24f, center.y + size * 0.42f)
      close()
    }
    drawPath(
      ghost,
      brush = Brush.verticalGradient(listOf(Color(0xFFE0E7FF).copy(alpha = 0.92f), Color(0xFF94A3B8).copy(alpha = 0.72f)))
    )
    drawCircle(color = Color(0xFF0F172A), radius = size * 0.12f, center = Offset(center.x - size * 0.20f, center.y - size * 0.22f))
    drawCircle(color = Color(0xFF0F172A), radius = size * 0.12f, center = Offset(center.x + size * 0.20f, center.y - size * 0.22f))
    drawCircle(color = Color(0xFFA855F7).copy(alpha = (0.4f * pulse).coerceIn(0.25f, 0.55f)), radius = size * 1.15f, center = center, style = Stroke(width = 1.dp.toPx()))
  }
}

internal fun DrawScope.drawTombstone(center: Offset, size: Float) {
  val stone = Path().apply {
    moveTo(center.x - size * 0.55f, center.y + size * 0.7f)
    lineTo(center.x - size * 0.50f, center.y - size * 0.25f)
    quadraticTo(center.x, center.y - size * 0.85f, center.x + size * 0.50f, center.y - size * 0.25f)
    lineTo(center.x + size * 0.55f, center.y + size * 0.7f)
    close()
  }
  drawPath(stone, brush = Brush.verticalGradient(listOf(Color(0xFF475569), Color(0xFF1E293B))))
  drawPath(stone, color = Color(0xFF94A3B8).copy(alpha = 0.45f), style = Stroke(width = 1.dp.toPx()))
}

internal fun DrawScope.drawJackOLantern(center: Offset, r: Float, pulse: Float) {
  drawCircle(color = Color(0x55F97316), radius = r * 1.35f * pulse, center = center)
  // Stem
  drawRoundRect(
    color = Color(0xFF365314),
    topLeft = Offset(center.x - r * 0.15f, center.y - r * 1.15f),
    size = Size(r * 0.30f, r * 0.40f),
    cornerRadius = CornerRadius(r * 0.1f, r * 0.1f)
  )
  // Pumpkin body
  drawOval(
    brush = Brush.radialGradient(listOf(Color(0xFFFACC15), Color(0xFFF97316), Color(0xFF9A3412)), center = center, radius = r),
    topLeft = Offset(center.x - r * 0.98f, center.y - r * 0.80f),
    size = Size(r * 1.96f, r * 1.60f)
  )
  // Glowing carved eyes & smile
  val eyeGlow = Color(0xFFFEF08A)
  drawCircle(color = eyeGlow, radius = r * 0.17f, center = Offset(center.x - r * 0.34f, center.y - r * 0.16f))
  drawCircle(color = eyeGlow, radius = r * 0.17f, center = Offset(center.x + r * 0.34f, center.y - r * 0.16f))
  drawArc(
    color = eyeGlow,
    startAngle = 15f,
    sweepAngle = 150f,
    useCenter = true,
    topLeft = Offset(center.x - r * 0.45f, center.y + r * 0.02f),
    size = Size(r * 0.90f, r * 0.48f)
  )
}

internal fun DrawScope.drawCogwheel(center: Offset, r: Float, rotation: Float, brass: Color, dark: Color) {
  rotate(rotation, center) {
    for (i in 0 until 8) {
      val rad = Math.toRadians(i * 45.0)
      drawCircle(
        color = brass,
        radius = r * 0.24f,
        center = Offset(center.x + r * 0.85f * cos(rad).toFloat(), center.y + r * 0.85f * sin(rad).toFloat())
      )
    }
    drawCircle(brush = Brush.radialGradient(listOf(brass, dark)), radius = r * 0.78f, center = center)
    drawCircle(color = dark, radius = r * 0.30f, center = center)
  }
}

internal fun DrawScope.drawPressureGauge(center: Offset, r: Float, pulse: Float) {
  drawCircle(color = Color(0xFFB45309), radius = r, center = center)
  drawCircle(color = Color(0xFFFEF3C7), radius = r * 0.78f, center = center)
  val ang = Math.toRadians((-120f + 160f * (pulse - 0.90f) * 5f).toDouble())
  drawLine(
    color = Color(0xFFDC2626),
    start = center,
    end = Offset(center.x + r * 0.62f * cos(ang).toFloat(), center.y + r * 0.62f * sin(ang).toFloat()),
    strokeWidth = 1.6.dp.toPx(),
    cap = StrokeCap.Round
  )
}

internal fun DrawScope.drawEnergyVial(center: Offset, len: Float, tiltDeg: Float, glow: Color) {
  rotate(tiltDeg, center) {
    drawRoundRect(
      color = Color(0xFF0F172A),
      topLeft = Offset(center.x - len * 0.26f, center.y - len * 0.55f),
      size = Size(len * 0.52f, len * 1.1f),
      cornerRadius = CornerRadius(len * 0.2f, len * 0.2f)
    )
    drawRoundRect(
      brush = Brush.verticalGradient(listOf(Color.White, glow, Color(0xFF0F766E))),
      topLeft = Offset(center.x - len * 0.18f, center.y - len * 0.42f),
      size = Size(len * 0.36f, len * 0.84f),
      cornerRadius = CornerRadius(len * 0.14f, len * 0.14f)
    )
  }
}
