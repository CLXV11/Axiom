package com.example.ui.components

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

internal fun DrawScope.drawVioletButterfly(center: Offset, r: Float, pulse: Float) {
  val roseDeep = Color(0xFF581C87)
  val roseMid = Color(0xFFA855F7)
  val lotusPink = Color(0xFFF472B6)
  val lotusLight = Color(0xFFFCE7F3)

  drawCircle(
    brush = Brush.radialGradient(listOf(Color(0x55EC4899), Color.Transparent), center = center, radius = r * 1.46f),
    radius = r * 1.46f,
    center = center
  )

  // Glowing pink-violet garland ring
  drawCircle(
    brush = Brush.sweepGradient(listOf(roseDeep, lotusPink, lotusLight, roseMid, roseDeep)),
    radius = r * 1.03f,
    center = center,
    style = Stroke(width = r * 0.14f)
  )

  // Lush Violet Roses top & left
  drawLayeredRose(Offset(center.x - r * 0.35f, center.y - r * 1.06f), r * 0.22f, roseDeep, roseMid, lotusLight)
  drawLayeredRose(Offset(center.x + r * 0.12f, center.y - r * 1.12f), r * 0.24f, roseDeep, roseMid, lotusLight)
  drawLayeredRose(Offset(center.x - r * 1.05f, center.y - r * 0.18f), r * 0.22f, roseDeep, roseMid, lotusLight)
  drawLayeredRose(Offset(center.x - r * 0.85f, center.y + r * 0.65f), r * 0.19f, roseDeep, roseMid, lotusLight)

  // Luminous Pink Lotus Blossoms bottom-right
  drawFlowerBlossom(
    center = Offset(center.x + r * 0.60f, center.y + r * 0.92f),
    petalRadius = r * 0.22f,
    petalColor = lotusPink,
    coreColor = Color(0xFFFDE047),
    petals = 7
  )
  drawFlowerBlossom(
    center = Offset(center.x - r * 0.15f, center.y + r * 1.08f),
    petalRadius = r * 0.15f,
    petalColor = lotusLight,
    coreColor = lotusPink,
    petals = 5
  )

  // Two Delicate Butterflies perched around the wreath
  drawButterfly(Offset(center.x + r * 0.95f, center.y - r * 0.48f), r * 0.25f, 22f, lotusLight, lotusPink, pulse)
  drawButterfly(Offset(center.x - r * 0.86f, center.y - r * 0.72f), r * 0.20f, -26f, lotusLight, roseMid, pulse)
  drawButterfly(Offset(center.x + r * 1.02f, center.y + r * 0.30f), r * 0.18f, 45f, lotusLight, lotusPink, pulse)
}

internal fun DrawScope.drawSteampunkEngine(center: Offset, r: Float, rotation: Float, pulse: Float) {
  val brassBright = Color(0xFFF59E0B)
  val copperMid = Color(0xFFB45309)
  val ironDark = Color(0xFF451A03)
  val cyanEnergy = Color(0xFF2DD4BF)

  // Heavy Copper Pipe Ring + Rivets
  drawCircle(
    brush = Brush.sweepGradient(listOf(copperMid, brassBright, ironDark, brassBright, copperMid)),
    radius = r * 1.04f,
    center = center,
    style = Stroke(width = r * 0.20f)
  )
  drawCircle(
    color = Color(0xFFFDE68A).copy(alpha = 0.65f),
    radius = r * 0.94f,
    center = center,
    style = Stroke(width = 1.4.dp.toPx())
  )

  // Spinning Brass Cogwheels
  drawCogwheel(Offset(center.x + r * 0.98f, center.y - r * 0.62f), r * 0.26f, rotation, brassBright, ironDark)
  drawCogwheel(Offset(center.x + r * 0.66f, center.y - r * 0.92f), r * 0.18f, -rotation * 1.4f, copperMid, ironDark)
  drawCogwheel(Offset(center.x - r * 0.95f, center.y + r * 0.70f), r * 0.22f, -rotation, brassBright, ironDark)

  // Analog Steam Pressure Gauges
  drawPressureGauge(Offset(center.x - r * 1.02f, center.y - r * 0.15f), r * 0.20f, pulse)
  drawPressureGauge(Offset(center.x + r * 0.35f, center.y + r * 1.06f), r * 0.17f, pulse * 1.1f)

  // Glowing Cyan Steam Energy Vials
  drawEnergyVial(Offset(center.x + r * 1.06f, center.y + r * 0.16f), r * 0.18f, 75f, cyanEnergy)
  drawEnergyVial(Offset(center.x - r * 0.25f, center.y + r * 1.08f), r * 0.18f, 15f, cyanEnergy)
  drawEnergyVial(Offset(center.x - r * 0.58f, center.y - r * 0.96f), r * 0.17f, -35f, cyanEnergy)
}

internal fun DrawScope.drawFeatherTotem(center: Offset, r: Float, pulse: Float) {
  val turquoise = Color(0xFF0D9488)
  val turquoiseLight = Color(0xFF5EEAD4)
  val amber = Color(0xFFF59E0B)
  val goldLight = Color(0xFFFDE68A)
  val bronze = Color(0xFF78350F)

  // 22 overlapping Tribal Feathers fanning radially
  val featherCount = 22
  for (i in 0 until featherCount) {
    val deg = i * (360f / featherCount)
    val rad = Math.toRadians(deg.toDouble())
    val fPos = Offset(center.x + r * 1.15f * cos(rad).toFloat(), center.y + r * 1.15f * sin(rad).toFloat())
    val primary = if (i % 3 == 0) amber else turquoise
    val tip = if (i % 3 == 0) goldLight else turquoiseLight
    drawFeather(fPos, r * 0.28f, deg + 90f, primary, tip, bronze)
  }

  // Braided Tribal Gold Crest Ring
  drawCircle(
    brush = Brush.sweepGradient(listOf(bronze, goldLight, amber, bronze, goldLight, bronze)),
    radius = r * 1.00f,
    center = center,
    style = Stroke(width = r * 0.15f)
  )

  // Central Top Crown Plume
  drawFeather(Offset(center.x, center.y - r * 1.24f), r * 0.34f, 0f, amber, goldLight, bronze)
  drawCircle(
    color = turquoiseLight.copy(alpha = (0.8f * pulse).coerceIn(0.6f, 1f)),
    radius = r * 0.055f,
    center = Offset(center.x, center.y - r * 1.00f)
  )
}

internal fun DrawScope.drawSunlitGarden(center: Offset, r: Float, pulse: Float) {
  val cream = Color(0xFFFEF3C7)
  val warmGold = Color(0xFFFBBF24)
  val petalYellow = Color(0xFFFACC15)
  val petalOrange = Color(0xFFEA580C)
  val leafGreen = Color(0xFF22C55E)
  val leafDark = Color(0xFF15803D)

  // Soft sunlit glow
  drawCircle(
    brush = Brush.radialGradient(listOf(Color(0x55FACC15), Color.Transparent), center = center, radius = r * 1.44f),
    radius = r * 1.44f,
    center = center
  )

  // Warm scalloped Cream-Gold Ring with golden pearl dots
  drawCircle(
    brush = Brush.sweepGradient(listOf(cream, warmGold, cream, warmGold, cream)),
    radius = r * 1.03f,
    center = center,
    style = Stroke(width = r * 0.17f)
  )
  for (i in 0 until 16) {
    val rad = Math.toRadians((i * 22.5))
    drawCircle(
      color = petalOrange,
      radius = r * 0.028f,
      center = Offset(center.x + r * 1.03f * cos(rad).toFloat(), center.y + r * 1.03f * sin(rad).toFloat())
    )
  }

  // Fresh Spring Leaves
  for (deg in listOf(-120f, -60f, 15f, 45f, 130f, 165f)) {
    val rad = Math.toRadians(deg.toDouble())
    drawBaroqueLeaf(
      Offset(center.x + r * 1.12f * cos(rad).toFloat(), center.y + r * 1.12f * sin(rad).toFloat()),
      r * 0.22f,
      deg + 40f,
      leafGreen,
      leafDark
    )
  }

  // Bright Sunflowers top, left-bottom, right-bottom
  drawSunflower(Offset(center.x, center.y - r * 1.10f), r * 0.25f * pulse.coerceIn(0.97f, 1.03f), petalYellow, petalOrange)
  drawSunflower(Offset(center.x - r * 0.88f, center.y + r * 0.65f), r * 0.20f, petalYellow, petalOrange)
  drawSunflower(Offset(center.x + r * 0.86f, center.y + r * 0.65f), r * 0.19f, petalYellow, petalOrange)
}

internal fun DrawScope.drawFutureMech(center: Offset, r: Float, rotation: Float) {
  val cyanNeon = Color(0xFF06B6D4)
  val cyanBright = Color(0xFF67E8F9)
  val mechSlate = Color(0xFF1E293B)
  val orangeCore = Color(0xFFF97316)

  // Rotating Plasma Energy Arcs
  rotate(rotation, center) {
    for (startA in listOf(10f, 130f, 250f)) {
      drawArc(
        brush = Brush.sweepGradient(listOf(cyanNeon, cyanBright, Color.White, cyanNeon)),
        startAngle = startA,
        sweepAngle = 80f,
        useCenter = false,
        topLeft = Offset(center.x - r * 1.06f, center.y - r * 1.06f),
        size = Size(r * 2.12f, r * 2.12f),
        style = Stroke(width = r * 0.11f, cap = StrokeCap.Round)
      )
    }
  }

  // Heavy Mecha Armored Visor Brackets Top & Bottom
  val topBracket = Path().apply {
    moveTo(center.x - r * 0.72f, center.y - r * 0.94f)
    lineTo(center.x - r * 0.45f, center.y - r * 1.24f)
    lineTo(center.x + r * 0.45f, center.y - r * 1.24f)
    lineTo(center.x + r * 0.72f, center.y - r * 0.94f)
  }
  drawPath(topBracket, color = mechSlate, style = Stroke(width = r * 0.16f, cap = StrokeCap.Square))
  drawPath(topBracket, color = cyanBright, style = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Square))

  val bottomBracket = Path().apply {
    moveTo(center.x - r * 0.65f, center.y + r * 0.94f)
    lineTo(center.x - r * 0.38f, center.y + r * 1.22f)
    lineTo(center.x + r * 0.38f, center.y + r * 1.22f)
    lineTo(center.x + r * 0.65f, center.y + r * 0.94f)
  }
  drawPath(bottomBracket, color = mechSlate, style = Stroke(width = r * 0.15f, cap = StrokeCap.Square))
  drawPath(bottomBracket, color = orangeCore, style = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Square))

  // Side Thruster Nodes
  for (s in listOf(-1f, 1f)) {
    drawCircle(color = mechSlate, radius = r * 0.13f, center = Offset(center.x + s * r * 1.02f, center.y))
    drawCircle(color = cyanBright, radius = r * 0.065f, center = Offset(center.x + s * r * 1.02f, center.y))
  }
}

internal fun DrawScope.drawCelestialWings(center: Offset, r: Float, pulse: Float) {
  val featherWhite = Color(0xFFF8FAFC)
  val featherBlue = Color(0xFFC7D2FE)
  val goldAccent = Color(0xFFFACC15)

  drawCircle(
    brush = Brush.radialGradient(listOf(Color(0x88A5B4FC), Color(0x336366F1), Color.Transparent), center = center, radius = r * 1.50f),
    radius = r * 1.50f,
    center = center
  )

  // Glowing Astral Halo Ring
  drawCircle(
    brush = Brush.sweepGradient(listOf(featherWhite, Color(0xFF818CF8), goldAccent, featherWhite)),
    radius = r * 1.02f,
    center = center,
    style = Stroke(width = r * 0.13f)
  )

  // Tiered Archangel Wings Left & Right
  for (s in listOf(-1f, 1f)) {
    for (tier in 0..2) {
      val spread = 1.0f + tier * 0.15f
      val wing = Path().apply {
        moveTo(center.x + s * r * 0.80f, center.y + tier * r * 0.14f)
        cubicTo(
          center.x + s * r * (1.42f * spread), center.y - r * (0.95f - tier * 0.28f),
          center.x + s * r * (1.38f * spread), center.y + r * (0.28f + tier * 0.18f),
          center.x + s * r * 0.92f, center.y + r * (0.60f + tier * 0.15f)
        )
        close()
      }
      drawPath(wing, brush = Brush.linearGradient(listOf(featherWhite, featherBlue)))
      drawPath(wing, color = goldAccent.copy(alpha = 0.7f), style = Stroke(width = 1.1.dp.toPx()))
    }
  }

  // Radiant Top Celestial Star
  val starCenter = Offset(center.x, center.y - r * 1.12f)
  drawFourPointStar(starCenter, r * 0.22f * pulse.coerceIn(0.95f, 1.08f), goldAccent)
  drawCircle(color = Color.White, radius = r * 0.055f, center = starCenter)
}

internal fun DrawScope.drawImperialGold(center: Offset, r: Float, rotation: Float) {
  val goldBright = Color(0xFFFDE68A)
  val goldMid = Color(0xFFF59E0B)
  val goldDark = Color(0xFF92400E)
  val ruby = Color(0xFFDC2626)

  for (i in 0 until 16) {
    val deg = i * 22.5f + rotation * 0.15f
    val rad = Math.toRadians(deg.toDouble())
    val tip = Offset(center.x + r * 1.28f * cos(rad).toFloat(), center.y + r * 1.28f * sin(rad).toFloat())
    val a1 = Math.toRadians((deg - 7f).toDouble())
    val a2 = Math.toRadians((deg + 7f).toDouble())
    val ray = Path().apply {
      moveTo(tip.x, tip.y)
      lineTo(center.x + r * 0.96f * cos(a1).toFloat(), center.y + r * 0.96f * sin(a1).toFloat())
      lineTo(center.x + r * 0.96f * cos(a2).toFloat(), center.y + r * 0.96f * sin(a2).toFloat())
      close()
    }
    drawPath(ray, brush = Brush.linearGradient(listOf(goldBright, goldMid, goldDark)))
    if (i % 2 == 0) {
      drawCircle(color = ruby, radius = r * 0.045f, center = tip)
    }
  }

  drawCircle(
    brush = Brush.sweepGradient(listOf(goldBright, goldMid, goldDark, goldBright)),
    radius = r * 1.00f,
    center = center,
    style = Stroke(width = r * 0.16f)
  )
}

internal fun DrawScope.drawDragonFlame(center: Offset, r: Float, pulse: Float) {
  val yellow = Color(0xFFFEF08A)
  val orange = Color(0xFFF97316)
  val crimson = Color(0xFFDC2626)

  drawCircle(
    brush = Brush.radialGradient(listOf(Color(0x66EA580C), Color.Transparent), center = center, radius = r * 1.45f),
    radius = r * 1.45f,
    center = center
  )

  for (i in 0 until 14) {
    val deg = i * (360.0 / 14.0)
    val rad = Math.toRadians(deg)
    val len = (if (i % 2 == 0) 1.32f else 1.18f) * pulse.coerceIn(0.95f, 1.07f)
    val tip = Offset(center.x + r * len * cos(rad).toFloat(), center.y + r * len * sin(rad).toFloat())
    val a1 = Math.toRadians(deg - 9.0)
    val a2 = Math.toRadians(deg + 9.0)
    val flame = Path().apply {
      moveTo(tip.x, tip.y)
      quadraticTo(
        center.x + r * 1.12f * cos(a1).toFloat(),
        center.y + r * 1.12f * sin(a1).toFloat(),
        center.x + r * 0.95f * cos(a1).toFloat(),
        center.y + r * 0.95f * sin(a1).toFloat()
      )
      lineTo(center.x + r * 0.95f * cos(a2).toFloat(), center.y + r * 0.95f * sin(a2).toFloat())
      close()
    }
    drawPath(flame, brush = Brush.radialGradient(listOf(yellow, orange, crimson), center = center))
  }

  drawCircle(
    brush = Brush.sweepGradient(listOf(crimson, orange, yellow, crimson)),
    radius = r * 1.00f,
    center = center,
    style = Stroke(width = r * 0.15f)
  )
}

internal fun DrawScope.drawCosmicSwirl(center: Offset, r: Float, rotation: Float, pulse: Float) {
  val palette = listOf(Color(0xFF6366F1), Color(0xFFA855F7), Color(0xFFEC4899), Color(0xFF06B6D4), Color(0xFF6366F1))
  rotate(rotation, center) {
    drawCircle(
      brush = Brush.sweepGradient(palette),
      radius = r * 1.02f,
      center = center,
      style = Stroke(width = r * 0.15f)
    )
    drawCircle(
      brush = Brush.sweepGradient(palette.reversed()),
      radius = r * 1.15f,
      center = center,
      style = Stroke(width = r * 0.06f)
    )
  }
  for (i in 0 until 6) {
    val rad = Math.toRadians(i * 60.0 + rotation * 0.45)
    drawFourPointStar(
      Offset(center.x + r * 1.15f * cos(rad).toFloat(), center.y + r * 1.15f * sin(rad).toFloat()),
      r * 0.085f * pulse.coerceIn(0.9f, 1.1f),
      Color.White
    )
  }
}
