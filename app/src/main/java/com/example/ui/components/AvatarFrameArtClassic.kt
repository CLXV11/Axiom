package com.example.ui.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

internal fun DrawScope.drawHauntedSpirits(center: Offset, r: Float, pulse: Float, rotation: Float) {
  // Violet spectral aura behind archway
  drawCircle(
    brush = Brush.radialGradient(
      listOf(Color(0x997C3AED), Color(0x444C1D95), Color.Transparent),
      center = center,
      radius = r * 1.48f
    ),
    radius = r * 1.48f,
    center = center
  )

  // Stone Gothic Archway Ring (beveled obsidian/slate)
  drawCircle(
    brush = Brush.sweepGradient(
      listOf(Color(0xFF475569), Color(0xFF1E293B), Color(0xFF64748B), Color(0xFF0F172A), Color(0xFF475569))
    ),
    radius = r * 1.04f,
    center = center,
    style = Stroke(width = r * 0.16f)
  )
  drawCircle(
    color = Color(0xFF94A3B8).copy(alpha = 0.55f),
    radius = r * 0.96f,
    center = center,
    style = Stroke(width = 1.2.dp.toPx())
  )
  drawCircle(
    color = Color(0xFFA855F7).copy(alpha = 0.45f),
    radius = r * 1.12f,
    center = center,
    style = Stroke(width = 1.dp.toPx())
  )

  // Twinkling stars in upper arch
  for (i in 0 until 5) {
    val ang = Math.toRadians(-145.0 + i * 27.5)
    val sx = center.x + r * 1.26f * cos(ang).toFloat()
    val sy = center.y + r * 1.26f * sin(ang).toFloat()
    drawFourPointStar(Offset(sx, sy), r * (if (i % 2 == 0) 0.08f else 0.055f) * pulse, Color(0xFFFDE68A))
  }

  // Cute Glowing Ghosts peeking left & right
  drawCuteGhost(Offset(center.x - r * 1.02f, center.y - r * 0.42f), r * 0.25f, -14f, pulse)
  drawCuteGhost(Offset(center.x + r * 1.02f, center.y - r * 0.36f), r * 0.23f, 16f, pulse)

  // Tombstone silhouettes at bottom corners
  drawTombstone(Offset(center.x - r * 0.78f, center.y + r * 0.85f), r * 0.24f)
  drawTombstone(Offset(center.x + r * 0.80f, center.y + r * 0.82f), r * 0.26f)

  // Carved Glowing Jack-o'-Lantern bottom-left
  drawJackOLantern(Offset(center.x - r * 0.56f, center.y + r * 0.96f), r * 0.26f, pulse)

  // Subtle floating spectral orbs
  val orbAngle = Math.toRadians((rotation * 0.7).toDouble())
  drawCircle(
    color = Color(0xFFC084FC).copy(alpha = 0.8f),
    radius = r * 0.045f,
    center = Offset(
      center.x + r * 1.16f * cos(orbAngle).toFloat(),
      center.y + r * 1.16f * sin(orbAngle).toFloat()
    )
  )
}

internal fun DrawScope.drawCatBell(center: Offset, r: Float, pulse: Float) {
  val furLight = Color(0xFFFFFBFB)
  val furShade = Color(0xFFE9D5FF)
  val earPink = Color(0xFFF472B6)
  val ribbonViolet = Color(0xFF9333EA)
  val ribbonLight = Color(0xFFD8B4FE)
  val goldBright = Color(0xFFFDE047)
  val goldDeep = Color(0xFFD97706)

  // Soft pastel aura
  drawCircle(
    brush = Brush.radialGradient(
      listOf(Color(0x55F472B6), Color(0x33A855F7), Color.Transparent),
      center = center,
      radius = r * 1.45f
    ),
    radius = r * 1.45f,
    center = center
  )

  // Cat Ears Left & Right with 3D shading
  fun drawEar(sign: Float) {
    val outer = Path().apply {
      moveTo(center.x + sign * r * 0.82f, center.y - r * 0.52f)
      quadraticTo(
        center.x + sign * r * 0.88f, center.y - r * 1.24f,
        center.x + sign * r * 0.62f, center.y - r * 1.36f
      )
      quadraticTo(
        center.x + sign * r * 0.30f, center.y - r * 1.18f,
        center.x + sign * r * 0.14f, center.y - r * 0.96f
      )
      close()
    }
    drawPath(outer, brush = Brush.linearGradient(listOf(furLight, furShade)))
    drawPath(outer, color = Color(0xFFD8B4FE), style = Stroke(width = 1.4.dp.toPx()))

    val inner = Path().apply {
      moveTo(center.x + sign * r * 0.72f, center.y - r * 0.62f)
      quadraticTo(
        center.x + sign * r * 0.74f, center.y - r * 1.10f,
        center.x + sign * r * 0.58f, center.y - r * 1.20f
      )
      quadraticTo(
        center.x + sign * r * 0.36f, center.y - r * 1.06f,
        center.x + sign * r * 0.25f, center.y - r * 0.92f
      )
      close()
    }
    drawPath(inner, brush = Brush.linearGradient(listOf(Color(0xFFFBCFE8), earPink)))
  }
  drawEar(-1f)
  drawEar(1f)

  // Golden Crown/Laurel Sprigs between ears
  for (s in listOf(-1f, 1f)) {
    for (k in 0 until 3) {
      val a = Math.toRadians((-90.0 + s * (16 + k * 14)))
      val lx = center.x + r * 1.08f * cos(a).toFloat()
      val ly = center.y + r * 1.08f * sin(a).toFloat()
      rotate(a.toFloat() * 57.3f + 90f, Offset(lx, ly)) {
        drawOval(
          brush = Brush.linearGradient(listOf(goldBright, goldDeep)),
          topLeft = Offset(lx - r * 0.07f, ly - r * 0.12f),
          size = Size(r * 0.14f, r * 0.24f)
        )
      }
    }
  }

  // Braided Plush Pearl Ring
  drawCircle(
    brush = Brush.sweepGradient(listOf(furLight, Color(0xFFF3E8FF), Color(0xFFFBCFE8), furLight)),
    radius = r * 1.03f,
    center = center,
    style = Stroke(width = r * 0.16f)
  )
  drawCircle(
    color = Color.White.copy(alpha = 0.85f),
    radius = r * 0.96f,
    center = center,
    style = Stroke(width = 1.dp.toPx())
  )

  // Left & Right Silk Bows + Paws
  for (s in listOf(-1f, 1f)) {
    val bowCenter = Offset(center.x + s * r * 0.98f, center.y + r * 0.26f)
    drawSilkBow(bowCenter, r * 0.22f, ribbonLight, ribbonViolet)
    // Tiny Golden Bell on side bow
    drawGoldBell(Offset(bowCenter.x, bowCenter.y + r * 0.16f), r * 0.11f, goldBright, goldDeep)
    // Cute Paw Pad lower-side
    val pawPos = Offset(center.x + s * r * 0.78f, center.y + r * 0.68f)
    drawCircle(color = furLight, radius = r * 0.14f, center = pawPos)
    drawCircle(color = earPink, radius = r * 0.055f, center = Offset(pawPos.x, pawPos.y + r * 0.02f))
  }

  // Bottom Big Purple Silk Bow + Glowing Golden Bell
  val bottomCenter = Offset(center.x, center.y + r * 1.04f)
  drawSilkBow(bottomCenter, r * 0.30f, ribbonLight, ribbonViolet)
  drawGoldBell(Offset(bottomCenter.x, bottomCenter.y + r * 0.14f * pulse.coerceIn(0.96f, 1.04f)), r * 0.16f, goldBright, goldDeep)
}

internal fun DrawScope.drawVioletNight(center: Offset, r: Float, pulse: Float) {
  val darkPlum = Color(0xFF3B0764)
  val midViolet = Color(0xFF9333EA)
  val silverLilac = Color(0xFFE9D5FF)
  val silverEdge = Color(0xFFF8FAFC)

  // Deep aura
  drawCircle(
    brush = Brush.radialGradient(
      listOf(Color(0x669333EA), Color.Transparent),
      center = center,
      radius = r * 1.45f
    ),
    radius = r * 1.45f,
    center = center
  )

  // Ornate octagonal/square baroque outer frame border behind circular ring
  val half = r * 1.08f
  val rectPath = Path().apply {
    moveTo(center.x - half, center.y - half * 0.82f)
    quadraticTo(center.x - half, center.y - half, center.x - half * 0.82f, center.y - half)
    lineTo(center.x + half * 0.82f, center.y - half)
    quadraticTo(center.x + half, center.y - half, center.x + half, center.y - half * 0.82f)
    lineTo(center.x + half, center.y + half * 0.82f)
    quadraticTo(center.x + half, center.y + half, center.x + half * 0.82f, center.y + half)
    lineTo(center.x - half * 0.82f, center.y + half)
    quadraticTo(center.x - half, center.y + half, center.x - half, center.y + half * 0.82f)
    close()
  }
  drawPath(
    rectPath,
    brush = Brush.linearGradient(listOf(silverLilac, midViolet, darkPlum, silverLilac)),
    style = Stroke(width = r * 0.16f, join = StrokeJoin.Round)
  )

  // Inner carved metallic ring
  drawCircle(
    brush = Brush.sweepGradient(listOf(darkPlum, silverLilac, midViolet, silverEdge, darkPlum)),
    radius = r * 0.99f,
    center = center,
    style = Stroke(width = r * 0.13f)
  )

  // 8 Sculpted Baroque Acanthus Leaves + Violet Blossoms
  for (i in 0 until 8) {
    val deg = i * 45f
    val rad = Math.toRadians(deg.toDouble())
    val px = center.x + r * 1.15f * cos(rad).toFloat()
    val py = center.y + r * 1.15f * sin(rad).toFloat()
    drawBaroqueLeaf(Offset(px, py), r * 0.24f, deg, silverLilac, darkPlum)
    if (i % 2 == 1) {
      drawFlowerBlossom(
        center = Offset(px, py),
        petalRadius = r * 0.11f,
        petalColor = silverLilac,
        coreColor = Color(0xFFFDE047),
        petals = 5
      )
    }
  }
  drawFourPointStar(Offset(center.x, center.y - r * 1.16f), r * 0.10f * pulse, Color.White)
}

internal fun DrawScope.drawVioletWreath(center: Offset, r: Float, rotation: Float, pulse: Float) {
  val deepViolet = Color(0xFF4C1D95)
  val vividViolet = Color(0xFFA855F7)
  val softLilac = Color(0xFFE9D5FF)

  drawCircle(
    brush = Brush.radialGradient(
      listOf(Color(0x66A855F7), Color.Transparent),
      center = center,
      radius = r * 1.45f
    ),
    radius = r * 1.45f,
    center = center
  )

  // Intertwined silver-violet vine rings
  drawCircle(
    brush = Brush.sweepGradient(listOf(deepViolet, vividViolet, softLilac, deepViolet)),
    radius = r * 1.03f,
    center = center,
    style = Stroke(width = r * 0.14f)
  )
  drawCircle(
    color = softLilac.copy(alpha = 0.7f),
    radius = r * 0.97f,
    center = center,
    style = Stroke(width = 1.3.dp.toPx())
  )

  // Climbing vine leaves and lush 5-petal violet roses around the wreath
  for (i in 0 until 12) {
    val ang = i * 30f
    val rad = Math.toRadians(ang.toDouble())
    val pos = Offset(center.x + r * 1.08f * cos(rad).toFloat(), center.y + r * 1.08f * sin(rad).toFloat())
    drawBaroqueLeaf(pos, r * 0.18f, ang + 35f, vividViolet, deepViolet)
  }

  // Prominent corner flower clusters matching Screenshot 3
  val blossomAngles = listOf(-95f, -35f, 32f, 125f, 155f, 215f)
  blossomAngles.forEachIndexed { idx, deg ->
    val rad = Math.toRadians(deg.toDouble())
    val bPos = Offset(center.x + r * 1.09f * cos(rad).toFloat(), center.y + r * 1.09f * sin(rad).toFloat())
    drawLayeredRose(bPos, r * (if (idx % 2 == 0) 0.22f else 0.17f), deepViolet, vividViolet, softLilac)
  }

  // Orbiting light pollen particles
  rotate(rotation * 0.35f, center) {
    for (i in 0 until 6) {
      val rad = Math.toRadians((i * 60).toDouble())
      val sp = Offset(center.x + r * 1.24f * cos(rad).toFloat(), center.y + r * 1.24f * sin(rad).toFloat())
      drawCircle(color = softLilac.copy(alpha = 0.85f), radius = r * 0.03f * pulse, center = sp)
    }
  }
}

internal fun DrawScope.drawSakuraTide(center: Offset, r: Float, rotation: Float) {
  val indigoWave = Color(0xFF312E81)
  val lavenderWave = Color(0xFF818CF8)
  val foamWhite = Color(0xFFFCE7F3)
  val sakuraPink = Color(0xFFF472B6)
  val sakuraDeep = Color(0xFFDB2777)

  // Swirling wave outer ring
  rotate(rotation * 0.2f, center) {
    drawCircle(
      brush = Brush.sweepGradient(listOf(indigoWave, lavenderWave, sakuraPink, foamWhite, indigoWave)),
      radius = r * 1.05f,
      center = center,
      style = Stroke(width = r * 0.18f)
    )
  }

  // Frothy curved ocean crests hugging left and bottom
  for (i in 0 until 6) {
    val startA = i * 60f
    drawArc(
      brush = Brush.linearGradient(listOf(foamWhite, lavenderWave)),
      startAngle = startA,
      sweepAngle = 42f,
      useCenter = false,
      topLeft = Offset(center.x - r * 1.12f, center.y - r * 1.12f),
      size = Size(r * 2.24f, r * 2.24f),
      style = Stroke(width = r * 0.08f, cap = StrokeCap.Round)
    )
  }

  // Layered Sakura Blossoms clustered around rim
  val spots = listOf(-120f, -80f, -30f, 20f, 80f, 145f, 195f)
  spots.forEachIndexed { idx, deg ->
    val rad = Math.toRadians(deg.toDouble())
    val pos = Offset(center.x + r * 1.08f * cos(rad).toFloat(), center.y + r * 1.08f * sin(rad).toFloat())
    drawFlowerBlossom(
      center = pos,
      petalRadius = r * (if (idx % 2 == 0) 0.16f else 0.12f),
      petalColor = if (idx % 2 == 0) sakuraPink else foamWhite,
      coreColor = sakuraDeep,
      petals = 5
    )
  }
}

internal fun DrawScope.drawSunlitPattern(center: Offset, r: Float, rotation: Float) {
  val turquoise = Color(0xFF0D9488)
  val turquoiseBright = Color(0xFF2DD4BF)
  val terracotta = Color(0xFFB91C1C)
  val gold = Color(0xFFF59E0B)
  val goldLight = Color(0xFFFDE68A)

  // 16 ornate sunlit mandala petals alternating turquoise & crimson with gold borders
  val count = 16
  for (i in 0 until count) {
    val deg = i * (360f / count) + rotation * 0.12f
    val rad = Math.toRadians(deg.toDouble())
    val tip = Offset(center.x + r * 1.28f * cos(rad).toFloat(), center.y + r * 1.28f * sin(rad).toFloat())
    val a1 = Math.toRadians((deg - 10f).toDouble())
    val a2 = Math.toRadians((deg + 10f).toDouble())
    val p1 = Offset(center.x + r * 0.96f * cos(a1).toFloat(), center.y + r * 0.96f * sin(a1).toFloat())
    val p2 = Offset(center.x + r * 0.96f * cos(a2).toFloat(), center.y + r * 0.96f * sin(a2).toFloat())

    val petal = Path().apply {
      moveTo(p1.x, p1.y)
      quadraticTo(
        center.x + r * 1.18f * cos(a1).toFloat(),
        center.y + r * 1.18f * sin(a1).toFloat(),
        tip.x, tip.y
      )
      quadraticTo(
        center.x + r * 1.18f * cos(a2).toFloat(),
        center.y + r * 1.18f * sin(a2).toFloat(),
        p2.x, p2.y
      )
      close()
    }
    drawPath(
      petal,
      brush = Brush.linearGradient(
        if (i % 2 == 0) listOf(turquoiseBright, turquoise) else listOf(Color(0xFFEF4444), terracotta)
      )
    )
    drawPath(petal, color = goldLight, style = Stroke(width = 1.3.dp.toPx()))
    drawCircle(color = goldLight, radius = r * 0.032f, center = tip)
  }

  // Beveled Gold Sunburst Ring
  drawCircle(
    brush = Brush.sweepGradient(listOf(goldLight, gold, Color(0xFF92400E), goldLight)),
    radius = r * 0.99f,
    center = center,
    style = Stroke(width = r * 0.14f)
  )
}

internal fun DrawScope.drawCrimsonSilver(center: Offset, r: Float, pulse: Float) {
  val silverWhite = Color(0xFFF8FAFC)
  val silverMid = Color(0xFF94A3B8)
  val silverDark = Color(0xFF334155)
  val rubyBright = Color(0xFFEF4444)
  val rubyDark = Color(0xFF7F1D1D)

  // Radiant Crimson Inner Channel
  drawCircle(
    brush = Brush.sweepGradient(listOf(rubyDark, rubyBright, Color(0xFFFCA5A5), rubyBright, rubyDark)),
    radius = r * 1.02f,
    center = center,
    style = Stroke(width = r * 0.18f)
  )

  // Sculpted Armored Silver Feather-Blades wrapping left & right
  for (s in listOf(-1f, 1f)) {
    for (k in 0 until 6) {
      val baseAngle = if (s < 0) 130f + k * 20f else -50f + k * 20f
      val rad = Math.toRadians(baseAngle.toDouble())
      val pos = Offset(center.x + r * 1.10f * cos(rad).toFloat(), center.y + r * 1.10f * sin(rad).toFloat())
      drawBaroqueLeaf(pos, r * 0.26f, baseAngle + 70f * s, silverWhite, silverDark)
    }
  }

  // Outer Metallic Bevel Ring
  drawCircle(
    brush = Brush.sweepGradient(listOf(silverWhite, silverMid, silverDark, silverWhite)),
    radius = r * 0.93f,
    center = center,
    style = Stroke(width = r * 0.07f)
  )

  // Top & Bottom Obsidian-Silver Diamond Crests
  for (dir in listOf(-1f, 1f)) {
    val c = Offset(center.x, center.y + dir * r * 1.10f)
    val diamond = Path().apply {
      moveTo(c.x, c.y - r * 0.16f)
      lineTo(c.x + r * 0.18f, c.y)
      lineTo(c.x, c.y + r * 0.16f)
      lineTo(c.x - r * 0.18f, c.y)
      close()
    }
    drawPath(diamond, brush = Brush.linearGradient(listOf(silverWhite, silverMid)))
    drawPath(diamond, color = Color(0xFF0F172A), style = Stroke(width = 1.5.dp.toPx()))
    drawCircle(color = rubyBright.copy(alpha = (0.85f * pulse).coerceIn(0.6f, 1f)), radius = r * 0.055f, center = c)
  }
}

internal fun DrawScope.drawSkullEye(center: Offset, r: Float, pulse: Float) {
  val boneLight = Color(0xFFE7E5E4)
  val boneMid = Color(0xFF78716C)
  val boneDark = Color(0xFF292524)
  val mysticGlow = Color(0xFFDC2626)

  // Weathered Carved Bone Ring
  drawCircle(
    brush = Brush.sweepGradient(listOf(boneLight, boneMid, boneDark, boneMid, boneLight)),
    radius = r * 1.05f,
    center = center,
    style = Stroke(width = r * 0.22f)
  )

  // Sculpted Gothic Thorns/Ribs around circumference
  for (i in 0 until 12) {
    val ang = i * 30.0
    val rad = Math.toRadians(ang)
    val tip = Offset(center.x + r * 1.30f * cos(rad).toFloat(), center.y + r * 1.30f * sin(rad).toFloat())
    val a1 = Math.toRadians(ang - 7.0)
    val a2 = Math.toRadians(ang + 7.0)
    val spike = Path().apply {
      moveTo(tip.x, tip.y)
      lineTo(center.x + r * 1.02f * cos(a1).toFloat(), center.y + r * 1.02f * sin(a1).toFloat())
      lineTo(center.x + r * 1.02f * cos(a2).toFloat(), center.y + r * 1.02f * sin(a2).toFloat())
      close()
    }
    drawPath(spike, brush = Brush.linearGradient(listOf(boneLight, boneDark)))
  }

  // Top Mystic All-Seeing Eye Crest
  val eyeCenter = Offset(center.x, center.y - r * 1.15f)
  val eyeShape = Path().apply {
    moveTo(eyeCenter.x - r * 0.28f, eyeCenter.y)
    quadraticTo(eyeCenter.x, eyeCenter.y - r * 0.22f, eyeCenter.x + r * 0.28f, eyeCenter.y)
    quadraticTo(eyeCenter.x, eyeCenter.y + r * 0.22f, eyeCenter.x - r * 0.28f, eyeCenter.y)
    close()
  }
  drawPath(eyeShape, color = Color(0xFF0C0A09))
  drawPath(eyeShape, color = boneLight, style = Stroke(width = 1.8.dp.toPx()))
  drawCircle(color = mysticGlow, radius = r * 0.08f * pulse.coerceIn(0.9f, 1.1f), center = eyeCenter)
  drawCircle(color = Color(0xFFFDE047), radius = r * 0.032f, center = eyeCenter)

  // Bottom Carved Gothic Skull Crest
  val sc = Offset(center.x, center.y + r * 1.13f)
  drawCircle(brush = Brush.radialGradient(listOf(boneLight, boneMid)), radius = r * 0.22f, center = sc)
  // Jawbone
  drawRoundRect(
    color = boneMid,
    topLeft = Offset(sc.x - r * 0.12f, sc.y + r * 0.10f),
    size = Size(r * 0.24f, r * 0.13f),
    cornerRadius = androidx.compose.ui.geometry.CornerRadius(r * 0.04f, r * 0.04f)
  )
  // Hollow eye sockets & nasal cavity
  drawCircle(color = Color(0xFF0C0A09), radius = r * 0.055f, center = Offset(sc.x - r * 0.08f, sc.y - r * 0.02f))
  drawCircle(color = Color(0xFF0C0A09), radius = r * 0.055f, center = Offset(sc.x + r * 0.08f, sc.y - r * 0.02f))
  drawCircle(color = mysticGlow, radius = r * 0.022f, center = Offset(sc.x - r * 0.08f, sc.y - r * 0.02f))
  drawCircle(color = mysticGlow, radius = r * 0.022f, center = Offset(sc.x + r * 0.08f, sc.y - r * 0.02f))
}

internal fun DrawScope.drawOctopusCrown(center: Offset, r: Float, pulse: Float) {
  val deepAmethyst = Color(0xFF581C87)
  val vividMagenta = Color(0xFFD946EF)
  val neonLilac = Color(0xFFE9D5FF)

  drawCircle(
    brush = Brush.radialGradient(listOf(Color(0x77A855F7), Color.Transparent), center = center, radius = r * 1.48f),
    radius = r * 1.48f,
    center = center
  )

  // Ornate Purple Ring
  drawCircle(
    brush = Brush.sweepGradient(listOf(deepAmethyst, vividMagenta, neonLilac, deepAmethyst)),
    radius = r * 1.03f,
    center = center,
    style = Stroke(width = r * 0.17f)
  )

  // Coiled Abyssal Tentacles on Lower Left & Lower Right with glowing suckers
  for (s in listOf(-1f, 1f)) {
    val t1 = Path().apply {
      moveTo(center.x + s * r * 0.98f, center.y + r * 0.10f)
      cubicTo(
        center.x + s * r * 1.48f, center.y + r * 0.65f,
        center.x + s * r * 1.05f, center.y + r * 1.38f,
        center.x + s * r * 0.42f, center.y + r * 1.24f
      )
      cubicTo(
        center.x + s * r * 0.85f, center.y + r * 1.15f,
        center.x + s * r * 1.15f, center.y + r * 0.68f,
        center.x + s * r * 0.88f, center.y + r * 0.30f
      )
      close()
    }
    drawPath(t1, brush = Brush.linearGradient(listOf(vividMagenta, deepAmethyst)))
    drawPath(t1, color = neonLilac.copy(alpha = 0.6f), style = Stroke(width = 1.dp.toPx()))

    // Upper curling tentacles
    val t2 = Path().apply {
      moveTo(center.x + s * r * 0.96f, center.y - r * 0.25f)
      cubicTo(
        center.x + s * r * 1.35f, center.y - r * 0.80f,
        center.x + s * r * 0.85f, center.y - r * 1.25f,
        center.x + s * r * 0.55f, center.y - r * 1.05f
      )
      cubicTo(
        center.x + s * r * 0.85f, center.y - r * 1.05f,
        center.x + s * r * 1.10f, center.y - r * 0.65f,
        center.x + s * r * 0.88f, center.y - r * 0.35f
      )
      close()
    }
    drawPath(t2, brush = Brush.linearGradient(listOf(neonLilac, deepAmethyst)))

    // Suction cup pearls along lower tentacle
    for (k in 0..3) {
      val sx = center.x + s * r * (1.16f - k * 0.16f)
      val sy = center.y + r * (0.62f + k * 0.18f)
      drawCircle(color = neonLilac, radius = r * 0.042f, center = Offset(sx, sy))
    }
  }

  // Sculpted Royal Amethyst Crown Crest at top
  val top = Offset(center.x, center.y - r * 1.15f)
  val crown = Path().apply {
    moveTo(top.x - r * 0.34f, top.y + r * 0.12f)
    lineTo(top.x - r * 0.25f, top.y - r * 0.16f)
    lineTo(top.x - r * 0.09f, top.y + r * 0.02f)
    lineTo(top.x, top.y - r * 0.25f)
    lineTo(top.x + r * 0.09f, top.y + r * 0.02f)
    lineTo(top.x + r * 0.25f, top.y - r * 0.16f)
    lineTo(top.x + r * 0.34f, top.y + r * 0.12f)
    close()
  }
  drawPath(crown, brush = Brush.linearGradient(listOf(neonLilac, vividMagenta, deepAmethyst)))
  drawCircle(color = Color(0xFFFDE047), radius = r * 0.06f * pulse.coerceIn(0.9f, 1.1f), center = Offset(top.x, top.y - r * 0.25f))
}
