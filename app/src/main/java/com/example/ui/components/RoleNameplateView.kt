package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

/**
 * Illustrated animated Role Nameplate banner matching rich collector banners
 * (Peachy Bubble, Pear Bubbles, Midnight Rose, Midnight Haunt, Aurora Galaxy, Prismatic Tide,
 * Golden Glow, Cracked Stone, Deep Space Whisper, Raven Engraving, Moonlit, Bone Noir).
 */
@Composable
fun RoleNameplateView(
  styleKey: String,
  title: String,
  modifier: Modifier = Modifier,
  badgeIcon: @Composable (() -> Unit)? = null
) {
  val infiniteTransition = rememberInfiniteTransition(label = "np_fx")
  val drift by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(4200, easing = LinearEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "drift"
  )

  val borderColor = when (styleKey) {
    "np_peachy_bubble" -> Color(0xFFF9A8D4)
    "np_pear_bubbles" -> Color(0xFFBEF264)
    "np_midnight_rose" -> Color(0xFFEF4444)
    "np_midnight_haunt" -> Color(0xFFA855F7)
    "np_aurora" -> Color(0xFF5EEAD4)
    "np_prismatic" -> Color(0xFF67E8F9)
    "np_golden" -> Color(0xFFFDE047)
    "np_cracked_stone" -> Color(0xFF94A3B8)
    "np_deep_space" -> Color(0xFF38BDF8)
    "np_raven_engraving" -> Color(0xFF34D399)
    "np_bone_noir" -> Color(0xFFA1A1AA)
    else -> Color(0xFF818CF8)
  }

  val shape = RoundedCornerShape(12.dp)
  Box(
    modifier = modifier
      .defaultMinSize(minWidth = 148.dp)
      .height(42.dp)
      .clip(shape)
      .border(1.2.dp, borderColor.copy(alpha = 0.72f), shape),
    contentAlignment = Alignment.CenterStart
  ) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      when (styleKey) {
        "np_peachy_bubble" -> drawPeachyBubbleBanner(drift)
        "np_pear_bubbles" -> drawPearBubbleBanner(drift)
        "np_midnight_rose" -> drawMidnightRoseBanner(drift)
        "np_midnight_haunt" -> drawMidnightHauntBanner(drift)
        "np_aurora" -> drawAuroraBanner(drift)
        "np_prismatic" -> drawPrismaticTideBanner(drift)
        "np_golden" -> drawGoldenFlareBanner(drift)
        "np_cracked_stone" -> drawCrackedStoneBanner()
        "np_deep_space" -> drawDeepSpaceBanner(drift)
        "np_raven_engraving" -> drawRavenEngravingBanner()
        "np_moonlit" -> drawMoonlitBanner(drift)
        "np_bone_noir" -> drawBoneNoirBanner()
        else -> drawMidnightBanner(drift)
      }
      // Subtle readable text scrim on leading half so Arabic/English text is crystal clear
      drawRect(
        brush = Brush.horizontalGradient(
          colors = listOf(Color(0x7A090B12), Color(0x44090B12), Color.Transparent)
        )
      )
    }

    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp)
    ) {
      if (badgeIcon != null) {
        badgeIcon()
        Spacer(modifier = Modifier.width(6.dp))
      } else {
        Icon(
          imageVector = Icons.Default.Star,
          contentDescription = null,
          tint = borderColor,
          modifier = Modifier.size(13.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
      }
      Text(
        text = title,
        style = TextStyle(
          color = Color.White,
          fontWeight = FontWeight.Bold,
          fontSize = 12.sp,
          letterSpacing = 0.3.sp,
          shadow = Shadow(
            color = Color(0xDD000000),
            offset = Offset(0f, 2f),
            blurRadius = 6f
          )
        ),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }
  }
}

private fun DrawScope.drawPeachyBubbleBanner(drift: Float) {
  drawRect(
    brush = Brush.horizontalGradient(
      listOf(Color(0xFF9D174D), Color(0xFFDB2777), Color(0xFFF472B6), Color(0xFFFBCFE8))
    )
  )
  // Soft cherry blossoms drifting across left-middle
  for (i in 0..3) {
    val bx = size.width * (0.14f + i * 0.14f)
    val by = size.height * (if (i % 2 == 0) 0.30f else 0.72f)
    drawFlowerBlossom(
      center = Offset(bx, by),
      petalRadius = size.height * 0.16f,
      petalColor = Color.White.copy(alpha = 0.45f),
      coreColor = Color(0xFFFCE7F3),
      petals = 5
    )
  }
  // Cute Pink Mochi Character peeking on right
  val mc = Offset(size.width * 0.84f, size.height * (0.64f - drift * 0.04f))
  val mr = size.height * 0.56f
  drawCircle(
    brush = Brush.radialGradient(listOf(Color(0xFFFCE7F3), Color(0xFFF472B6)), center = Offset(mc.x - mr * 0.2f, mc.y - mr * 0.3f), radius = mr * 1.4f),
    radius = mr,
    center = mc
  )
  drawCircle(color = Color.White.copy(alpha = 0.65f), radius = mr * 0.15f, center = Offset(mc.x - mr * 0.35f, mc.y - mr * 0.45f))
  // Cute eyes & coral blush cheeks
  drawCircle(color = Color(0xFF3B0764), radius = size.height * 0.055f, center = Offset(mc.x - mr * 0.28f, mc.y - mr * 0.06f))
  drawCircle(color = Color(0xFF3B0764), radius = size.height * 0.055f, center = Offset(mc.x + mr * 0.28f, mc.y - mr * 0.06f))
  drawCircle(color = Color(0xFFFB7185), radius = size.height * 0.075f, center = Offset(mc.x - mr * 0.46f, mc.y + mr * 0.12f))
  drawCircle(color = Color(0xFFFB7185), radius = size.height * 0.075f, center = Offset(mc.x + mr * 0.46f, mc.y + mr * 0.12f))
}

private fun DrawScope.drawPearBubbleBanner(drift: Float) {
  drawRect(
    brush = Brush.horizontalGradient(
      listOf(Color(0xFF4D7C0F), Color(0xFF84CC16), Color(0xFFBEF264), Color(0xFFD9F99D))
    )
  )
  // Translucent white citrus flowers & floating bubbles
  for (i in 0..3) {
    val bx = size.width * (0.16f + i * 0.13f)
    val by = size.height * (if (i % 2 == 0) 0.68f else 0.28f)
    drawFlowerBlossom(
      center = Offset(bx, by),
      petalRadius = size.height * 0.14f,
      petalColor = Color.White.copy(alpha = 0.45f),
      coreColor = Color(0xFFFEF08A),
      petals = 5
    )
  }
  // Cute Pear-Green Mochi Character peeking on right
  val mc = Offset(size.width * 0.84f, size.height * (0.64f - drift * 0.04f))
  val mr = size.height * 0.56f
  drawCircle(
    brush = Brush.radialGradient(listOf(Color(0xFFECFCCB), Color(0xFFA3E635)), center = Offset(mc.x - mr * 0.2f, mc.y - mr * 0.3f), radius = mr * 1.4f),
    radius = mr,
    center = mc
  )
  drawCircle(color = Color(0xFF1A2E05), radius = size.height * 0.055f, center = Offset(mc.x - mr * 0.26f, mc.y - mr * 0.06f))
  drawCircle(color = Color(0xFF1A2E05), radius = size.height * 0.055f, center = Offset(mc.x + mr * 0.26f, mc.y - mr * 0.06f))
  drawCircle(color = Color(0xFFFB923C), radius = size.height * 0.075f, center = Offset(mc.x - mr * 0.44f, mc.y + mr * 0.10f))
  drawCircle(color = Color(0xFFFB923C), radius = size.height * 0.075f, center = Offset(mc.x + mr * 0.44f, mc.y + mr * 0.10f))
}

private fun DrawScope.drawMidnightRoseBanner(drift: Float) {
  drawRect(
    brush = Brush.horizontalGradient(
      listOf(Color(0xFF28050D), Color(0xFF6B0F1A), Color(0xFF991B1B), Color(0xFF3F0D12))
    )
  )
  // Faint vintage music staff lines
  for (line in 0..3) {
    val y = size.height * (0.28f + line * 0.14f)
    drawLine(Color.White.copy(alpha = 0.12f), Offset(0f, y), Offset(size.width * 0.7f, y), strokeWidth = 0.9.dp.toPx())
  }
  // Flowing crimson satin ribbon
  val ribbon = Path().apply {
    moveTo(0f, size.height * 0.85f)
    cubicTo(size.width * 0.3f, size.height * 0.15f, size.width * 0.55f, size.height * 0.95f, size.width, size.height * 0.25f)
  }
  drawPath(ribbon, color = Color(0xFFDC2626).copy(alpha = 0.55f), style = Stroke(width = 6.dp.toPx()))
  // Antique Pocket Watch Face right-center
  val wc = Offset(size.width * 0.72f, size.height * 0.52f)
  val wr = size.height * 0.36f
  drawCircle(color = Color(0xFFFDE68A), radius = wr, center = wc)
  drawCircle(color = Color(0xFFFEF9C3), radius = wr * 0.82f, center = wc)
  drawLine(Color(0xFF1E293B), wc, Offset(wc.x, wc.y - wr * 0.55f), strokeWidth = 1.4.dp.toPx(), cap = StrokeCap.Round)
  drawLine(Color(0xFF1E293B), wc, Offset(wc.x + wr * 0.45f, wc.y + wr * 0.15f), strokeWidth = 1.2.dp.toPx(), cap = StrokeCap.Round)
  // Lush Crimson Roses on far right
  drawLayeredRose(Offset(size.width * 0.90f, size.height * 0.35f), size.height * 0.28f, Color(0xFF7F1D1D), Color(0xFFDC2626), Color(0xFFFCA5A5))
  drawLayeredRose(Offset(size.width * 0.88f, size.height * 0.80f), size.height * 0.22f, Color(0xFF7F1D1D), Color(0xFFEF4444), Color(0xFFFECACA))
  drawFourPointStar(Offset(size.width * (0.60f + drift * 0.04f), size.height * 0.22f), 4.dp.toPx(), Color(0xFFFDE68A))
}

private fun DrawScope.drawMidnightHauntBanner(drift: Float) {
  drawRect(
    brush = Brush.verticalGradient(
      listOf(Color(0xFF090D16), Color(0xFF1E1B4B), Color(0xFF312E81))
    )
  )
  // Glowing Full Moon
  val moon = Offset(size.width * 0.78f, size.height * 0.34f)
  drawCircle(color = Color(0x44FDE68A), radius = size.height * 0.48f, center = moon)
  drawCircle(color = Color(0xFFFEF3C7), radius = size.height * 0.22f, center = moon)
  // Haunted Manor Silhouette
  val manor = Path().apply {
    moveTo(size.width * 0.46f, size.height)
    lineTo(size.width * 0.46f, size.height * 0.48f)
    lineTo(size.width * 0.54f, size.height * 0.22f)
    lineTo(size.width * 0.62f, size.height * 0.48f)
    lineTo(size.width * 0.68f, size.height * 0.32f)
    lineTo(size.width * 0.75f, size.height * 0.52f)
    lineTo(size.width * 0.75f, size.height)
    close()
  }
  drawPath(manor, color = Color(0xFF030712))
  // Glowing amber windows & mini jack-o'-lanterns along path
  drawJackOLantern(Offset(size.width * 0.64f, size.height * 0.76f), size.height * 0.16f, 0.96f + drift * 0.08f)
  drawJackOLantern(Offset(size.width * 0.88f, size.height * 0.74f), size.height * 0.18f, 0.98f + drift * 0.06f)
}

private fun DrawScope.drawAuroraBanner(drift: Float) {
  drawRect(
    brush = Brush.horizontalGradient(
      listOf(Color(0xFF022C22), Color(0xFF065F46), Color(0xFF1E1B4B), Color(0xFF3B0764))
    )
  )
  // Flowing Emerald, Cyan & Violet Aurora Waves
  val wave1 = Path().apply {
    moveTo(0f, size.height * 0.75f)
    cubicTo(size.width * 0.28f, size.height * (0.12f + drift * 0.12f), size.width * 0.62f, size.height * 0.85f, size.width, size.height * 0.20f)
    lineTo(size.width, size.height)
    lineTo(0f, size.height)
    close()
  }
  drawPath(
    wave1,
    brush = Brush.horizontalGradient(
      listOf(Color(0x8810B981), Color(0xAA06B6D4), Color(0xAA8B5CF6))
    )
  )
  val wave2 = Path().apply {
    moveTo(0f, size.height * 0.30f)
    cubicTo(size.width * 0.35f, size.height * 0.90f, size.width * 0.68f, size.height * (0.10f + drift * 0.1f), size.width, size.height * 0.62f)
  }
  drawPath(
    wave2,
    brush = Brush.horizontalGradient(listOf(Color(0xFF6EE7B7), Color(0xFF38BDF8), Color(0xFFC084FC))),
    style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
  )
  // Distant Galaxy Stars
  drawFourPointStar(Offset(size.width * 0.74f, size.height * 0.30f), 4.5.dp.toPx(), Color(0xFFFDE68A))
  drawFourPointStar(Offset(size.width * 0.89f, size.height * 0.62f), 3.5.dp.toPx(), Color.White)
  drawCircle(color = Color.White.copy(alpha = 0.85f), radius = 1.5.dp.toPx(), center = Offset(size.width * 0.62f, size.height * 0.22f))
}

private fun DrawScope.drawPrismaticTideBanner(drift: Float) {
  drawRect(
    brush = Brush.horizontalGradient(
      listOf(Color(0xFF0D9488), Color(0xFF2DD4BF), Color(0xFFF472B6), Color(0xFFA855F7))
    )
  )
  // Iridescent Swirl Curl
  val swirl = Path().apply {
    moveTo(size.width * 0.35f, size.height * 0.85f)
    cubicTo(size.width * 0.55f, size.height * 0.05f, size.width * 0.72f, size.height * 0.95f, size.width * 0.94f, size.height * 0.35f)
  }
  drawPath(swirl, color = Color.White.copy(alpha = 0.42f), style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round))
  drawFourPointStar(Offset(size.width * (0.64f + drift * 0.03f), size.height * 0.34f), 5.dp.toPx(), Color.White)
  drawFourPointStar(Offset(size.width * 0.82f, size.height * 0.66f), 3.5.dp.toPx(), Color(0xFFFEF08A))
}

private fun DrawScope.drawGoldenFlareBanner(drift: Float) {
  drawRect(
    brush = Brush.horizontalGradient(
      listOf(Color(0xFF92400E), Color(0xFFD97706), Color(0xFFF59E0B), Color(0xFFFDE047))
    )
  )
  // Crystalline Facet Polygons
  for (i in 0..4) {
    val x0 = size.width * (0.35f + i * 0.14f)
    val facet = Path().apply {
      moveTo(x0, 0f)
      lineTo(x0 + size.width * 0.14f, size.height * 0.45f)
      lineTo(x0 + size.width * 0.05f, size.height)
      lineTo(x0 - size.width * 0.06f, size.height * 0.55f)
      close()
    }
    drawPath(facet, color = Color.White.copy(alpha = if (i % 2 == 0) 0.16f else 0.08f))
  }
  drawFourPointStar(Offset(size.width * (0.76f + drift * 0.04f), size.height * 0.42f), 5.5.dp.toPx(), Color.White)
}

private fun DrawScope.drawCrackedStoneBanner() {
  drawRect(
    brush = Brush.horizontalGradient(
      listOf(Color(0xFF18181B), Color(0xFF27272A), Color(0xFF3F3F46), Color(0xFF1C1917))
    )
  )
  // Jagged Basalt Cracks
  val crack = Path().apply {
    moveTo(size.width * 0.42f, 0f)
    lineTo(size.width * 0.48f, size.height * 0.38f)
    lineTo(size.width * 0.44f, size.height * 0.64f)
    lineTo(size.width * 0.54f, size.height)
    moveTo(size.width * 0.48f, size.height * 0.38f)
    lineTo(size.width * 0.65f, size.height * 0.52f)
    lineTo(size.width * 0.78f, size.height * 0.32f)
  }
  drawPath(crack, color = Color(0xFF09090B), style = Stroke(width = 2.dp.toPx()))
  drawPath(crack, color = Color(0xFF94A3B8).copy(alpha = 0.25f), style = Stroke(width = 0.8.dp.toPx()))
}

private fun DrawScope.drawDeepSpaceBanner(drift: Float) {
  drawRect(
    brush = Brush.horizontalGradient(
      listOf(Color(0xFF020617), Color(0xFF0C4A6E), Color(0xFF164E63), Color(0xFF090D16))
    )
  )
  drawCircle(
    brush = Brush.radialGradient(listOf(Color(0x8838BDF8), Color.Transparent), center = Offset(size.width * 0.82f, size.height * 0.72f), radius = size.height * 0.95f),
    radius = size.height * 0.95f,
    center = Offset(size.width * 0.82f, size.height * 0.72f)
  )
  drawFourPointStar(Offset(size.width * (0.72f + drift * 0.03f), size.height * 0.26f), 4.dp.toPx(), Color(0xFFBAE6FD))
}

private fun DrawScope.drawRavenEngravingBanner() {
  drawRect(
    brush = Brush.horizontalGradient(
      listOf(Color(0xFF071312), Color(0xFF0F2922), Color(0xFF134E4A), Color(0xFF0B1F1C))
    )
  )
  for (i in 0..4) {
    val cx = size.width * (0.50f + i * 0.11f)
    drawArc(
      color = Color(0xFF34D399).copy(alpha = 0.38f),
      startAngle = 190f,
      sweepAngle = 150f,
      useCenter = false,
      topLeft = Offset(cx, size.height * 0.15f),
      size = Size(size.height * 0.7f, size.height * 0.7f),
      style = Stroke(width = 1.2.dp.toPx())
    )
  }
}

private fun DrawScope.drawMoonlitBanner(drift: Float) {
  drawRect(brush = Brush.horizontalGradient(listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF334155))))
  val moon = Offset(size.width * 0.82f, size.height * 0.36f)
  drawCircle(color = Color(0x55E2E8F0), radius = size.height * 0.42f, center = moon)
  drawCircle(color = Color(0xFFF8FAFC), radius = size.height * 0.20f, center = moon)
  drawFourPointStar(Offset(size.width * (0.64f + drift * 0.04f), size.height * 0.28f), 3.5.dp.toPx(), Color.White)
}

private fun DrawScope.drawBoneNoirBanner() {
  drawRect(brush = Brush.horizontalGradient(listOf(Color(0xFF09090B), Color(0xFF18181B), Color(0xFF27272A))))
  val filigree = Path().apply {
    moveTo(size.width * 0.65f, size.height * 0.5f)
    cubicTo(size.width * 0.75f, size.height * 0.1f, size.width * 0.85f, size.height * 0.9f, size.width * 0.95f, size.height * 0.5f)
  }
  drawPath(filigree, color = Color(0xFFD4D4D8).copy(alpha = 0.45f), style = Stroke(width = 2.dp.toPx()))
}

private fun DrawScope.drawMidnightBanner(drift: Float) {
  drawRect(brush = Brush.horizontalGradient(listOf(Color(0xFF0F172A), Color(0xFF1E1B4B), Color(0xFF312E81), Color(0xFF1E3A8A))))
  for (i in 0..3) {
    val rad = Math.toRadians((i * 40.0))
    val sx = size.width * (0.56f + i * 0.11f)
    val sy = size.height * (0.28f + 0.35f * sin(rad).toFloat())
    drawFourPointStar(Offset(sx + drift * 4f, sy), 3.5.dp.toPx(), Color(0xFFBAE6FD))
  }
}
