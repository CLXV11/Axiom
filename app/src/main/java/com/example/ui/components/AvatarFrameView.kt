package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

/**
 * High-fidelity, vector-rendered Avatar Frame overlay.
 * Renders bespoke ornaments, wings, crowns, mecha parts, skulls, and glowing rings.
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
      animation = tween(12000, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "rotation"
  )
  val pulse by infiniteTransition.animateFloat(
    initialValue = 0.85f,
    targetValue = 1.15f,
    animationSpec = infiniteRepeatable(
      animation = tween(2200, easing = LinearEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pulse"
  )

  Canvas(modifier = modifier.fillMaxSize()) {
    val center = Offset(size.width / 2f, size.height / 2f)
    val radius = size.minDimension / 2f * 0.72f

    when (styleKey) {
      "celestial_wings" -> drawCelestialWings(center, radius, pulse)
      "imperial_gold" -> drawImperialGold(center, radius, rotation)
      "future_mech" -> drawFutureMech(center, radius, rotation)
      "skull_eye" -> drawSkullEye(center, radius)
      "octopus_crown" -> drawOctopusCrown(center, radius, pulse)
      "sakura_tide" -> drawSakuraTide(center, radius, rotation)
      "dragon_flame" -> drawDragonFlame(center, radius, pulse)
      "cat_bell" -> drawCatBell(center, radius)
      "violet_night" -> drawVioletNight(center, radius)
      "cosmic_swirl" -> drawCosmicSwirl(center, radius, rotation)
      else -> {
        // Subtle refined metallic ring
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

private fun DrawScope.drawCelestialWings(center: Offset, radius: Radius, pulse: Float) {
  val wingColor = Color(0xFFF8FAFC)
  val goldAccent = Color(0xFFFFD700)
  val haloBrush = Brush.radialGradient(
    colors = listOf(Color(0x99A5B4FC), Color(0x33818CF8), Color.Transparent),
    center = center,
    radius = radius * 1.5f
  )
  drawCircle(brush = haloBrush, radius = radius * 1.35f, center = center)

  // Glowing base ring
  drawCircle(
    brush = Brush.sweepGradient(
      listOf(Color(0xFFE0E7FF), Color(0xFF818CF8), Color(0xFFC7D2FE), Color(0xFFE0E7FF))
    ),
    radius = radius,
    center = center,
    style = Stroke(width = 3.5.dp.toPx())
  )

  // Archangel Wings Left & Right
  val leftWing = Path().apply {
    moveTo(center.x - radius * 0.7f, center.y)
    cubicTo(
      center.x - radius * 1.45f, center.y - radius * 0.9f,
      center.x - radius * 1.35f, center.y + radius * 0.5f,
      center.x - radius * 0.95f, center.y + radius * 0.95f
    )
    cubicTo(
      center.x - radius * 1.15f, center.y + radius * 0.35f,
      center.x - radius * 1.05f, center.y - radius * 0.45f,
      center.x - radius * 0.7f, center.y
    )
    close()
  }
  drawPath(leftWing, color = wingColor.copy(alpha = 0.92f))
  drawPath(leftWing, color = goldAccent.copy(alpha = 0.6f), style = Stroke(width = 1.5.dp.toPx()))

  val rightWing = Path().apply {
    moveTo(center.x + radius * 0.7f, center.y)
    cubicTo(
      center.x + radius * 1.45f, center.y - radius * 0.9f,
      center.x + radius * 1.35f, center.y + radius * 0.5f,
      center.x + radius * 0.95f, center.y + radius * 0.95f
    )
    cubicTo(
      center.x + radius * 1.15f, center.y + radius * 0.35f,
      center.x + radius * 1.05f, center.y - radius * 0.45f,
      center.x + radius * 0.7f, center.y
    )
    close()
  }
  drawPath(rightWing, color = wingColor.copy(alpha = 0.92f))
  drawPath(rightWing, color = goldAccent.copy(alpha = 0.6f), style = Stroke(width = 1.5.dp.toPx()))

  // Top Radiant Celestial Star
  val starTop = Offset(center.x, center.y - radius * 1.08f)
  drawCircle(color = goldAccent, radius = 4.dp.toPx() * pulse, center = starTop)
  drawCircle(color = Color.White, radius = 2.dp.toPx(), center = starTop)
}

private fun DrawScope.drawImperialGold(center: Offset, radius: Float, rotation: Float) {
  val goldDeep = Color(0xFFD97706)
  val goldBright = Color(0xFFFDE68A)
  val ruby = Color(0xFFDC2626)

  // 16-point Mandala sunburst filigree
  val petals = 16
  for (i in 0 until petals) {
    val angleDeg = (i * (360f / petals)) + (rotation * 0.25f)
    val rad = Math.toRadians(angleDeg.toDouble())
    val outerR = radius * 1.25f
    val tipX = (center.x + outerR * cos(rad)).toFloat()
    val tipY = (center.y + outerR * sin(rad)).toFloat()

    val path = Path().apply {
      moveTo(tipX, tipY)
      val leftAngle = Math.toRadians((angleDeg - 7.0))
      val rightAngle = Math.toRadians((angleDeg + 7.0))
      lineTo(
        (center.x + radius * 0.98f * cos(leftAngle)).toFloat(),
        (center.y + radius * 0.98f * sin(leftAngle)).toFloat()
      )
      lineTo(
        (center.x + radius * 0.98f * cos(rightAngle)).toFloat(),
        (center.y + radius * 0.98f * sin(rightAngle)).toFloat()
      )
      close()
    }
    drawPath(path, brush = Brush.linearGradient(listOf(goldBright, goldDeep)))

    if (i % 2 == 0) {
      drawCircle(color = ruby, radius = 2.2.dp.toPx(), center = Offset(tipX, tipY))
    }
  }

  // Inner Double Gold Rings
  drawCircle(
    brush = Brush.sweepGradient(listOf(goldBright, goldDeep, goldBright)),
    radius = radius,
    center = center,
    style = Stroke(width = 3.5.dp.toPx())
  )
  drawCircle(
    color = Color(0x66B45309),
    radius = radius * 0.92f,
    center = center,
    style = Stroke(width = 1.dp.toPx())
  )
}

private fun DrawScope.drawFutureMech(center: Offset, radius: Float, rotation: Float) {
  val cyanNeon = Color(0xFF06B6D4)
  val tealGlow = Color(0xFF14B8A6)
  val mechPlate = Color(0xFF1E293B)
  val orangeAccent = Color(0xFFF97316)

  // Cybernetic rotating segmented arcs
  rotate(rotation, center) {
    drawArc(
      brush = Brush.sweepGradient(listOf(cyanNeon, tealGlow, Color.White, cyanNeon)),
      startAngle = 15f,
      sweepAngle = 70f,
      useCenter = false,
      topLeft = Offset(center.x - radius * 1.05f, center.y - radius * 1.05f),
      size = Size(radius * 2.1f, radius * 2.1f),
      style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
    )
    drawArc(
      brush = Brush.sweepGradient(listOf(cyanNeon, tealGlow, Color.White, cyanNeon)),
      startAngle = 195f,
      sweepAngle = 70f,
      useCenter = false,
      topLeft = Offset(center.x - radius * 1.05f, center.y - radius * 1.05f),
      size = Size(radius * 2.1f, radius * 2.1f),
      style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
    )
  }

  // Heavy Mecha armor corners (Top and Bottom brackets)
  val topBracket = Path().apply {
    moveTo(center.x - radius * 0.7f, center.y - radius * 0.95f)
    lineTo(center.x - radius * 0.45f, center.y - radius * 1.25f)
    lineTo(center.x + radius * 0.45f, center.y - radius * 1.25f)
    lineTo(center.x + radius * 0.7f, center.y - radius * 0.95f)
  }
  drawPath(topBracket, color = mechPlate, style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Square))
  drawPath(topBracket, color = cyanNeon, style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Square))

  val bottomBracket = Path().apply {
    moveTo(center.x - radius * 0.6f, center.y + radius * 0.95f)
    lineTo(center.x - radius * 0.35f, center.y + radius * 1.22f)
    lineTo(center.x + radius * 0.35f, center.y + radius * 1.22f)
    lineTo(center.x + radius * 0.6f, center.y + radius * 0.95f)
  }
  drawPath(bottomBracket, color = mechPlate, style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Square))
  drawPath(bottomBracket, color = orangeAccent, style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Square))

  // Neon sensor dots
  drawCircle(color = cyanNeon, radius = 3.dp.toPx(), center = Offset(center.x - radius * 0.85f, center.y))
  drawCircle(color = cyanNeon, radius = 3.dp.toPx(), center = Offset(center.x + radius * 0.85f, center.y))
}

private fun DrawScope.drawSkullEye(center: Offset, radius: Float) {
  val silverBright = Color(0xFFE2E8F0)
  val silverDark = Color(0xFF475569)
  val eyeRed = Color(0xFFEF4444)

  // Gothic thorned silver ring
  drawCircle(
    brush = Brush.sweepGradient(listOf(silverBright, silverDark, silverBright, silverDark, silverBright)),
    radius = radius,
    center = center,
    style = Stroke(width = 3.5.dp.toPx())
  )

  // Gothic spikes around perimeter
  for (i in 0 until 12) {
    val angle = Math.toRadians((i * 30.0))
    val tipX = (center.x + radius * 1.18f * cos(angle)).toFloat()
    val tipY = (center.y + radius * 1.18f * sin(angle)).toFloat()
    val basePath = Path().apply {
      moveTo(tipX, tipY)
      val p1 = Math.toRadians(i * 30.0 - 5.0)
      val p2 = Math.toRadians(i * 30.0 + 5.0)
      lineTo((center.x + radius * cos(p1)).toFloat(), (center.y + radius * sin(p1)).toFloat())
      lineTo((center.x + radius * cos(p2)).toFloat(), (center.y + radius * sin(p2)).toFloat())
      close()
    }
    drawPath(basePath, color = silverBright)
  }

  // Top Mystic Eye
  val eyeCenter = Offset(center.x, center.y - radius * 1.15f)
  val eyePath = Path().apply {
    moveTo(eyeCenter.x - 9.dp.toPx(), eyeCenter.y)
    cubicTo(
      eyeCenter.x - 4.dp.toPx(), eyeCenter.y - 6.dp.toPx(),
      eyeCenter.x + 4.dp.toPx(), eyeCenter.y - 6.dp.toPx(),
      eyeCenter.x + 9.dp.toPx(), eyeCenter.y
    )
    cubicTo(
      eyeCenter.x + 4.dp.toPx(), eyeCenter.y + 6.dp.toPx(),
      eyeCenter.x - 4.dp.toPx(), eyeCenter.y + 6.dp.toPx(),
      eyeCenter.x - 9.dp.toPx(), eyeCenter.y
    )
    close()
  }
  drawPath(eyePath, color = Color(0xFF0F172A))
  drawPath(eyePath, color = silverBright, style = Stroke(width = 1.5.dp.toPx()))
  drawCircle(color = eyeRed, radius = 2.5.dp.toPx(), center = eyeCenter)

  // Bottom Gothic Skull Icon
  val skullCenter = Offset(center.x, center.y + radius * 1.12f)
  drawCircle(color = silverBright, radius = 6.dp.toPx(), center = skullCenter)
  drawCircle(color = Color(0xFF0F172A), radius = 1.5.dp.toPx(), center = Offset(skullCenter.x - 2.5.dp.toPx(), skullCenter.y - 1.dp.toPx()))
  drawCircle(color = Color(0xFF0F172A), radius = 1.5.dp.toPx(), center = Offset(skullCenter.x + 2.5.dp.toPx(), skullCenter.y - 1.dp.toPx()))
}

private fun DrawScope.drawOctopusCrown(center: Offset, radius: Float, pulse: Float) {
  val tentacleViolet = Color(0xFF9333EA)
  val tentaclePink = Color(0xFFD946EF)
  val crownGold = Color(0xFFFBBF24)

  // Luminescent violet ring
  drawCircle(
    brush = Brush.sweepGradient(listOf(tentacleViolet, tentaclePink, Color(0xFFC084FC), tentacleViolet)),
    radius = radius,
    center = center,
    style = Stroke(width = 3.5.dp.toPx())
  )

  // Curled tentacles on bottom and sides
  val tentaclePath = Path().apply {
    moveTo(center.x - radius * 0.9f, center.y + radius * 0.3f)
    cubicTo(
      center.x - radius * 1.35f, center.y + radius * 0.8f,
      center.x - radius * 0.8f, center.y + radius * 1.35f,
      center.x - radius * 0.3f, center.y + radius * 1.05f
    )
    cubicTo(
      center.x - radius * 0.6f, center.y + radius * 1.15f,
      center.x - radius * 1.1f, center.y + radius * 0.75f,
      center.x - radius * 0.85f, center.y + radius * 0.45f
    )
    close()
  }
  drawPath(tentaclePath, brush = Brush.linearGradient(listOf(tentaclePink, tentacleViolet)))

  val rightTentacle = Path().apply {
    moveTo(center.x + radius * 0.9f, center.y + radius * 0.3f)
    cubicTo(
      center.x + radius * 1.35f, center.y + radius * 0.8f,
      center.x + radius * 0.8f, center.y + radius * 1.35f,
      center.x + radius * 0.3f, center.y + radius * 1.05f
    )
    cubicTo(
      center.x + radius * 0.6f, center.y + radius * 1.15f,
      center.x + radius * 1.1f, center.y + radius * 0.75f,
      center.x + radius * 0.85f, center.y + radius * 0.45f
    )
    close()
  }
  drawPath(rightTentacle, brush = Brush.linearGradient(listOf(tentaclePink, tentacleViolet)))

  // Imperial Crown on top
  val crownTop = Offset(center.x, center.y - radius * 1.1f)
  val crownPath = Path().apply {
    moveTo(crownTop.x - 12.dp.toPx(), crownTop.y + 4.dp.toPx())
    lineTo(crownTop.x - 8.dp.toPx(), crownTop.y - 6.dp.toPx())
    lineTo(crownTop.x - 3.dp.toPx(), crownTop.y)
    lineTo(crownTop.x, crownTop.y - 9.dp.toPx())
    lineTo(crownTop.x + 3.dp.toPx(), crownTop.y)
    lineTo(crownTop.x + 8.dp.toPx(), crownTop.y - 6.dp.toPx())
    lineTo(crownTop.x + 12.dp.toPx(), crownTop.y + 4.dp.toPx())
    close()
  }
  drawPath(crownPath, color = crownGold)
  drawCircle(color = tentaclePink, radius = 2.dp.toPx(), center = Offset(crownTop.x, crownTop.y - 9.dp.toPx()))
}

private fun DrawScope.drawSakuraTide(center: Offset, radius: Float, rotation: Float) {
  val petalPink = Color(0xFFF472B6)
  val petalSoft = Color(0xFFFCE7F3)
  val waveAqua = Color(0xFF38BDF8)

  // Pastel Aqua/Pink Ring
  drawCircle(
    brush = Brush.sweepGradient(listOf(waveAqua, petalPink, petalSoft, waveAqua)),
    radius = radius,
    center = center,
    style = Stroke(width = 3.dp.toPx())
  )

  // Floating sakura blossoms
  rotate(rotation * 0.4f, center) {
    for (i in 0 until 5) {
      val angle = Math.toRadians((i * 72.0))
      val bX = (center.x + radius * 1.08f * cos(angle)).toFloat()
      val bY = (center.y + radius * 1.08f * sin(angle)).toFloat()

      for (p in 0 until 5) {
        val pAngle = Math.toRadians((p * 72.0))
        val pX = bX + (3.5.dp.toPx() * cos(pAngle)).toFloat()
        val pY = bY + (3.5.dp.toPx() * sin(pAngle)).toFloat()
        drawCircle(color = petalPink, radius = 2.2.dp.toPx(), center = Offset(pX, pY))
      }
      drawCircle(color = Color(0xFFFDE047), radius = 1.2.dp.toPx(), center = Offset(bX, bY))
    }
  }
}

private fun DrawScope.drawDragonFlame(center: Offset, radius: Float, pulse: Float) {
  val flameOrange = Color(0xFFEA580C)
  val flameYellow = Color(0xFFFACC15)
  val flameRed = Color(0xFFDC2626)

  // Fiery corona
  val flames = 12
  for (i in 0 until flames) {
    val angle = Math.toRadians((i * 30.0))
    val flameLength = (if (i % 2 == 0) 1.28f else 1.14f) * pulse
    val tipX = (center.x + radius * flameLength * cos(angle)).toFloat()
    val tipY = (center.y + radius * flameLength * sin(angle)).toFloat()

    val path = Path().apply {
      moveTo(tipX, tipY)
      val left = Math.toRadians(i * 30.0 - 9.0)
      val right = Math.toRadians(i * 30.0 + 9.0)
      lineTo((center.x + radius * cos(left)).toFloat(), (center.y + radius * sin(left)).toFloat())
      lineTo((center.x + radius * cos(right)).toFloat(), (center.y + radius * sin(right)).toFloat())
      close()
    }
    drawPath(path, brush = Brush.radialGradient(listOf(flameYellow, flameOrange, flameRed), center = center))
  }

  // Inner Molten Ring
  drawCircle(
    brush = Brush.sweepGradient(listOf(flameRed, flameOrange, flameYellow, flameRed)),
    radius = radius,
    center = center,
    style = Stroke(width = 3.5.dp.toPx())
  )
}

private fun DrawScope.drawCatBell(center: Offset, radius: Float) {
  val earWhite = Color(0xFFF8FAFC)
  val earPink = Color(0xFFF472B6)
  val ribbonPurple = Color(0xFF8B5CF6)
  val bellGold = Color(0xFFFACC15)

  // Base Soft Ring
  drawCircle(
    brush = Brush.sweepGradient(listOf(ribbonPurple, Color(0xFFC084FC), Color(0xFFFBCFE8), ribbonPurple)),
    radius = radius,
    center = center,
    style = Stroke(width = 3.dp.toPx())
  )

  // Cat Ear Left
  val leftEar = Path().apply {
    moveTo(center.x - radius * 0.75f, center.y - radius * 0.7f)
    lineTo(center.x - radius * 0.65f, center.y - radius * 1.35f)
    lineTo(center.x - radius * 0.2f, center.y - radius * 0.98f)
    close()
  }
  drawPath(leftEar, color = earWhite)
  val leftEarInner = Path().apply {
    moveTo(center.x - radius * 0.68f, center.y - radius * 0.75f)
    lineTo(center.x - radius * 0.62f, center.y - radius * 1.25f)
    lineTo(center.x - radius * 0.28f, center.y - radius * 0.96f)
    close()
  }
  drawPath(leftEarInner, color = earPink)

  // Cat Ear Right
  val rightEar = Path().apply {
    moveTo(center.x + radius * 0.75f, center.y - radius * 0.7f)
    lineTo(center.x + radius * 0.65f, center.y - radius * 1.35f)
    lineTo(center.x + radius * 0.2f, center.y - radius * 0.98f)
    close()
  }
  drawPath(rightEar, color = earWhite)
  val rightEarInner = Path().apply {
    moveTo(center.x + radius * 0.68f, center.y - radius * 0.75f)
    lineTo(center.x + radius * 0.62f, center.y - radius * 1.25f)
    lineTo(center.x + radius * 0.28f, center.y - radius * 0.96f)
    close()
  }
  drawPath(rightEarInner, color = earPink)

  // Bottom Golden Bell and Bow
  val bellCenter = Offset(center.x, center.y + radius * 1.08f)
  drawCircle(color = bellGold, radius = 5.dp.toPx(), center = bellCenter)
  drawCircle(color = Color(0xFF78350F), radius = 1.2.dp.toPx(), center = Offset(bellCenter.x, bellCenter.y + 1.5.dp.toPx()))

  // Bow Wings
  drawCircle(color = ribbonPurple, radius = 3.5.dp.toPx(), center = Offset(bellCenter.x - 6.dp.toPx(), bellCenter.y - 1.dp.toPx()))
  drawCircle(color = ribbonPurple, radius = 3.5.dp.toPx(), center = Offset(bellCenter.x + 6.dp.toPx(), bellCenter.y - 1.dp.toPx()))
}

private fun DrawScope.drawVioletNight(center: Offset, radius: Float) {
  val darkViolet = Color(0xFF581C87)
  val lightViolet = Color(0xFFA855F7)
  val silver = Color(0xFFCBD5E1)

  // Ornate Baroque Filigree Ring
  drawCircle(
    brush = Brush.sweepGradient(listOf(darkViolet, lightViolet, silver, darkViolet)),
    radius = radius,
    center = center,
    style = Stroke(width = 3.5.dp.toPx())
  )

  // 8 Baroque Floral Points
  for (i in 0 until 8) {
    val angle = Math.toRadians((i * 45.0))
    val tipX = (center.x + radius * 1.22f * cos(angle)).toFloat()
    val tipY = (center.y + radius * 1.22f * sin(angle)).toFloat()

    val path = Path().apply {
      moveTo(tipX, tipY)
      val a1 = Math.toRadians(i * 45.0 - 8.0)
      val a2 = Math.toRadians(i * 45.0 + 8.0)
      lineTo((center.x + radius * cos(a1)).toFloat(), (center.y + radius * sin(a1)).toFloat())
      lineTo((center.x + radius * cos(a2)).toFloat(), (center.y + radius * sin(a2)).toFloat())
      close()
    }
    drawPath(path, brush = Brush.linearGradient(listOf(lightViolet, darkViolet)))
    drawCircle(color = silver, radius = 1.8.dp.toPx(), center = Offset(tipX, tipY))
  }
}

private fun DrawScope.drawCosmicSwirl(center: Offset, radius: Float, rotation: Float) {
  val colors = listOf(
    Color(0xFF6366F1),
    Color(0xFFA855F7),
    Color(0xFFEC4899),
    Color(0xFF06B6D4),
    Color(0xFF6366F1)
  )
  rotate(rotation, center) {
    drawCircle(
      brush = Brush.sweepGradient(colors),
      radius = radius,
      center = center,
      style = Stroke(width = 4.dp.toPx())
    )
    drawCircle(
      brush = Brush.sweepGradient(colors.reversed()),
      radius = radius * 1.12f,
      center = center,
      style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
    )
  }

  // Glittering Starlight dots
  for (i in 0 until 6) {
    val angle = Math.toRadians((i * 60.0 + rotation * 0.5))
    val starX = (center.x + radius * 1.06f * cos(angle)).toFloat()
    val starY = (center.y + radius * 1.06f * sin(angle)).toFloat()
    drawCircle(color = Color.White, radius = 2.dp.toPx(), center = Offset(starX, starY))
  }
}

private typealias Radius = Float

/**
 * Role Nameplate view for rendering decorated name banners under or behind user headers.
 */
@Composable
fun RoleNameplateView(
  styleKey: String,
  title: String,
  modifier: Modifier = Modifier,
  badgeIcon: @Composable (() -> Unit)? = null
) {
  val brush = when (styleKey) {
    "np_aurora" -> Brush.horizontalGradient(
      listOf(Color(0xFF064E3B), Color(0xFF047857), Color(0xFF06B6D4), Color(0xFF8B5CF6))
    )
    "np_prismatic" -> Brush.horizontalGradient(
      listOf(Color(0xFF0E7490), Color(0xFF2DD4BF), Color(0xFFF472B6), Color(0xFFA78BFA))
    )
    "np_golden" -> Brush.horizontalGradient(
      listOf(Color(0xFFB45309), Color(0xFFF59E0B), Color(0xFFFDE68A), Color(0xFFD97706))
    )
    "np_midnight" -> Brush.horizontalGradient(
      listOf(Color(0xFF0F172A), Color(0xFF1E1B4B), Color(0xFF312E81), Color(0xFF1E293B))
    )
    "np_moonlit" -> Brush.horizontalGradient(
      listOf(Color(0xFF1E293B), Color(0xFF334155), Color(0xFF64748B), Color(0xFF0F172A))
    )
    "np_bone_noir" -> Brush.horizontalGradient(
      listOf(Color(0xFF18181B), Color(0xFF27272A), Color(0xFF3F3F46), Color(0xFF09090B))
    )
    else -> Brush.horizontalGradient(
      listOf(Color(0xFF312E81), Color(0xFF4338CA), Color(0xFF6366F1))
    )
  }

  val borderColor = when (styleKey) {
    "np_golden" -> Color(0xFFFCD34D)
    "np_prismatic" -> Color(0xFF5EEAD4)
    "np_aurora" -> Color(0xFF6EE7B7)
    "np_bone_noir" -> Color(0xFF71717A)
    else -> Color(0xFF818CF8)
  }

  Box(
    modifier = modifier
      .clip(RoundedCornerShape(8.dp))
      .background(brush)
      .border(1.dp, borderColor.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
      .padding(horizontal = 10.dp, vertical = 4.dp),
    contentAlignment = Alignment.CenterStart
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      if (badgeIcon != null) {
        badgeIcon()
        Spacer(modifier = Modifier.width(6.dp))
      } else {
        Icon(
          imageVector = Icons.Default.Star,
          contentDescription = null,
          tint = Color.White.copy(alpha = 0.9f),
          modifier = Modifier.size(12.dp)
        )
        Spacer(modifier = Modifier.width(5.dp))
      }
      Text(
        text = title,
        color = Color.White,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        letterSpacing = 0.4.sp,
        maxLines = 1
      )
    }
  }
}
