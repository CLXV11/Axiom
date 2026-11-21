package com.example.ui.components

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
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
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import java.text.BreakIterator
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Splits a string containing 1..N grapheme clusters (such as jumbo emojis with ZWJ/skin modifiers)
 * cleanly into visual emoji tokens.
 */
fun splitGraphemeClusters(text: String): List<String> {
  val trimmed = text.trim()
  if (trimmed.isEmpty()) return emptyList()
  val iterator = BreakIterator.getCharacterInstance()
  iterator.setText(trimmed)
  val out = mutableListOf<String>()
  var start = iterator.first()
  var end = iterator.next()
  while (end != BreakIterator.DONE) {
    val cluster = trimmed.substring(start, end)
    if (cluster.isNotBlank()) out.add(cluster)
    start = end
    end = iterator.next()
  }
  return out
}

/**
 * Renders 100% custom, iPhone-inspired 3D glossy emojis directly via Jetpack Compose Canvas,
 * completely independent from Android OEM system emoji fonts.
 */
@Composable
fun GlossyAppleEmoji(
  emoji: String,
  size: Dp = 24.dp,
  modifier: Modifier = Modifier
) {
  val normalized = emoji.trim().replace("\uFE0F", "")
  Canvas(modifier = modifier.size(size)) {
    drawGlossyEmoji(normalized)
  }
}

private fun DrawScope.drawGlossyEmoji(emoji: String) {
  val w = size.width
  val h = size.height
  val cx = w / 2f
  val cy = h / 2f
  val r = minOf(w, h) * 0.44f

  // Soft ambient ground shadow
  drawOval(
    color = Color.Black.copy(alpha = 0.24f),
    topLeft = Offset(cx - r * 0.72f, cy + r * 0.78f),
    size = Size(r * 1.44f, r * 0.26f)
  )

  when (emoji) {
    "❤️", "❤", "🩷", "🫶" -> {
      val topColor = if (emoji == "🩷") Color(0xFFFF9CE3) else Color(0xFFFF6B8B)
      val midColor = if (emoji == "🩷") Color(0xFFF43F9E) else Color(0xFFE11D48)
      val deepColor = if (emoji == "🩷") Color(0xFF9D174D) else Color(0xFF881337)
      drawGlossyHeart(cx, cy, r, topColor, midColor, deepColor)
    }

    "😂" -> {
      drawGlossySunSphere(cx, cy, r)
      drawAppleLaughEyes(cx, cy, r)
      drawAppleHappyTears(cx, cy, r)
      drawAppleOpenLaughMouth(cx, cy, r)
    }

    "🥹" -> {
      drawGlossySunSphere(cx, cy, r)
      drawApplePleadingEyes(cx, cy, r)
      drawOval(
        color = Color(0xFFFF6B81).copy(alpha = 0.45f),
        topLeft = Offset(cx - r * 0.62f, cy + r * 0.04f),
        size = Size(r * 0.30f, r * 0.16f)
      )
      drawOval(
        color = Color(0xFFFF6B81).copy(alpha = 0.45f),
        topLeft = Offset(cx + r * 0.32f, cy + r * 0.04f),
        size = Size(r * 0.30f, r * 0.16f)
      )
      val smile = Path().apply {
        moveTo(cx - r * 0.25f, cy + r * 0.28f)
        quadraticTo(cx, cy + r * 0.44f, cx + r * 0.25f, cy + r * 0.28f)
      }
      drawPath(smile, Color(0xFF5D2906), style = Stroke(width = r * 0.10f, cap = StrokeCap.Round))
    }

    "😍", "🤩" -> {
      drawGlossySunSphere(cx, cy, r)
      if (emoji == "😍") {
        drawGlossyHeart(cx - r * 0.36f, cy - r * 0.18f, r * 0.25f, Color(0xFFFF7A96), Color(0xFFE11D48), Color(0xFF881337))
        drawGlossyHeart(cx + r * 0.36f, cy - r * 0.18f, r * 0.25f, Color(0xFFFF7A96), Color(0xFFE11D48), Color(0xFF881337))
      } else {
        drawStarShape(cx - r * 0.36f, cy - r * 0.18f, r * 0.24f, Color(0xFFFF3D7F), Color(0xFFFFD166))
        drawStarShape(cx + r * 0.36f, cy - r * 0.18f, r * 0.24f, Color(0xFFFF3D7F), Color(0xFFFFD166))
      }
      drawAppleOpenLaughMouth(cx, cy, r)
    }

    "😮" -> {
      drawGlossySunSphere(cx, cy, r)
      drawOval(Color(0xFF4A2506), Offset(cx - r * 0.40f, cy - r * 0.30f), Size(r * 0.22f, r * 0.28f))
      drawOval(Color(0xFF4A2506), Offset(cx + r * 0.18f, cy - r * 0.30f), Size(r * 0.22f, r * 0.28f))
      drawCircle(Color.White.copy(alpha = 0.85f), radius = r * 0.055f, center = Offset(cx - r * 0.32f, cy - r * 0.22f))
      drawCircle(Color.White.copy(alpha = 0.85f), radius = r * 0.055f, center = Offset(cx + r * 0.26f, cy - r * 0.22f))
      drawOval(
        brush = Brush.verticalGradient(listOf(Color(0xFF5A1E08), Color(0xFF280902))),
        topLeft = Offset(cx - r * 0.20f, cy + r * 0.12f),
        size = Size(r * 0.40f, r * 0.44f)
      )
      drawOval(
        color = Color(0xFFFF7A96).copy(alpha = 0.65f),
        topLeft = Offset(cx - r * 0.12f, cy + r * 0.38f),
        size = Size(r * 0.24f, r * 0.14f)
      )
    }

    "😢" -> {
      drawGlossySunSphere(cx, cy, r)
      drawCircle(Color(0xFF4A2506), r * 0.11f, Offset(cx - r * 0.32f, cy - r * 0.14f))
      drawCircle(Color(0xFF4A2506), r * 0.11f, Offset(cx + r * 0.32f, cy - r * 0.14f))
      drawCircle(Color.White.copy(alpha = 0.85f), r * 0.04f, Offset(cx - r * 0.35f, cy - r * 0.18f))
      drawCircle(Color.White.copy(alpha = 0.85f), r * 0.04f, Offset(cx + r * 0.29f, cy - r * 0.18f))
      drawGlossyTeardrop(cx + r * 0.44f, cy + r * 0.18f, r * 0.22f)
      val frown = Path().apply {
        moveTo(cx - r * 0.26f, cy + r * 0.38f)
        quadraticTo(cx, cy + r * 0.18f, cx + r * 0.26f, cy + r * 0.38f)
      }
      drawPath(frown, Color(0xFF5D2906), style = Stroke(width = r * 0.10f, cap = StrokeCap.Round))
    }

    "😡", "🥵" -> {
      val c1 = if (emoji == "😡") Color(0xFFFF7A59) else Color(0xFFFF9F43)
      val c2 = if (emoji == "😡") Color(0xFFEF233C) else Color(0xFFEE5253)
      val c3 = Color(0xFF8E0E24)
      drawGlossyCustomSphere(cx, cy, r, c1, c2, c3)
      drawLine(Color(0xFF3E0A0A), Offset(cx - r * 0.52f, cy - r * 0.36f), Offset(cx - r * 0.12f, cy - r * 0.18f), strokeWidth = r * 0.11f, cap = StrokeCap.Round)
      drawLine(Color(0xFF3E0A0A), Offset(cx + r * 0.52f, cy - r * 0.36f), Offset(cx + r * 0.12f, cy - r * 0.18f), strokeWidth = r * 0.11f, cap = StrokeCap.Round)
      drawCircle(Color(0xFF3E0A0A), r * 0.10f, Offset(cx - r * 0.30f, cy - r * 0.08f))
      drawCircle(Color(0xFF3E0A0A), r * 0.10f, Offset(cx + r * 0.30f, cy - r * 0.08f))
      if (emoji == "🥵") {
        drawGlossyTeardrop(cx - r * 0.62f, cy - r * 0.06f, r * 0.18f)
        drawOval(Color(0xFFFF6B81), Offset(cx - r * 0.18f, cy + r * 0.20f), Size(r * 0.36f, r * 0.36f))
      } else {
        val grimace = Path().apply {
          moveTo(cx - r * 0.30f, cy + r * 0.36f)
          quadraticTo(cx, cy + r * 0.18f, cx + r * 0.30f, cy + r * 0.36f)
        }
        drawPath(grimace, Color(0xFF3E0A0A), style = Stroke(width = r * 0.11f, cap = StrokeCap.Round))
      }
    }

    "🥶" -> {
      drawGlossyCustomSphere(cx, cy, r, Color(0xFFE0F7FA), Color(0xFF38BDF8), Color(0xFF0369A1))
      drawCircle(Color(0xFF0C4A6E), r * 0.10f, Offset(cx - r * 0.32f, cy - r * 0.14f))
      drawCircle(Color(0xFF0C4A6E), r * 0.10f, Offset(cx + r * 0.32f, cy - r * 0.14f))
      drawRoundRect(
        color = Color.White,
        topLeft = Offset(cx - r * 0.38f, cy + r * 0.16f),
        size = Size(r * 0.76f, r * 0.24f),
        cornerRadius = CornerRadius(r * 0.12f, r * 0.12f)
      )
      drawLine(Color(0xFF0369A1), Offset(cx - r * 0.38f, cy + r * 0.28f), Offset(cx + r * 0.38f, cy + r * 0.28f), strokeWidth = r * 0.05f)
      for (dx in listOf(-0.18f, 0f, 0.18f)) {
        drawLine(Color(0xFF0369A1), Offset(cx + r * dx, cy + r * 0.16f), Offset(cx + r * dx, cy + r * 0.40f), strokeWidth = r * 0.04f)
      }
    }

    "🫠" -> {
      // Melting glossy face
      val meltPath = Path().apply {
        addOval(androidx.compose.ui.geometry.Rect(cx - r * 0.92f, cy - r * 0.92f, cx + r * 0.92f, cy + r * 0.68f))
      }
      drawPath(
        meltPath,
        Brush.radialGradient(
          colors = listOf(Color(0xFFFFF176), Color(0xFFFFB300), Color(0xFFE67E00)),
          center = Offset(cx - r * 0.25f, cy - r * 0.30f),
          radius = r * 1.4f
        )
      )
      // Melting puddle & drips
      drawOval(
        brush = Brush.verticalGradient(listOf(Color(0xFFFFCA28), Color(0xFFE67E00))),
        topLeft = Offset(cx - r * 0.98f, cy + r * 0.50f),
        size = Size(r * 1.96f, r * 0.44f)
      )
      drawOval(Color(0xFFFFB300), Offset(cx - r * 0.46f, cy + r * 0.38f), Size(r * 0.24f, r * 0.34f))
      drawOval(Color(0xFFFFB300), Offset(cx + r * 0.24f, cy + r * 0.42f), Size(r * 0.20f, r * 0.28f))
      drawSphereHighlight(cx, cy - r * 0.08f, r * 0.85f)
      // Droopy eyes & crooked smile
      drawCircle(Color(0xFF4E2600), r * 0.11f, Offset(cx - r * 0.30f, cy - r * 0.16f))
      drawCircle(Color(0xFF4E2600), r * 0.11f, Offset(cx + r * 0.30f, cy - r * 0.04f))
      val crookedSmile = Path().apply {
        moveTo(cx - r * 0.30f, cy + r * 0.24f)
        quadraticTo(cx, cy + r * 0.38f, cx + r * 0.32f, cy + r * 0.16f)
      }
      drawPath(crookedSmile, Color(0xFF4E2600), style = Stroke(width = r * 0.10f, cap = StrokeCap.Round))
    }

    "🤯" -> {
      // Mind blown glossy cloud explosion
      drawGlossySunSphere(cx, cy + r * 0.14f, r * 0.86f)
      val cloudColors = listOf(Color(0xFFFF8A65), Color(0xFFFF4081), Color(0xFFFFEB3B))
      drawCircle(cloudColors[1], r * 0.34f, Offset(cx - r * 0.36f, cy - r * 0.56f))
      drawCircle(cloudColors[0], r * 0.38f, Offset(cx + r * 0.36f, cy - r * 0.54f))
      drawCircle(cloudColors[2], r * 0.42f, Offset(cx, cy - r * 0.70f))
      drawCircle(Color.White.copy(alpha = 0.55f), r * 0.16f, Offset(cx - r * 0.10f, cy - r * 0.78f))
      drawCircle(Color(0xFF4A2506), r * 0.11f, Offset(cx - r * 0.30f, cy + r * 0.06f))
      drawCircle(Color(0xFF4A2506), r * 0.11f, Offset(cx + r * 0.30f, cy + r * 0.06f))
      drawOval(Color(0xFF4A2506), Offset(cx - r * 0.16f, cy + r * 0.30f), Size(r * 0.32f, r * 0.34f))
    }

    "🥳" -> {
      drawGlossySunSphere(cx, cy, r)
      // Party hat
      val hat = Path().apply {
        moveTo(cx - r * 0.48f, cy - r * 0.52f)
        lineTo(cx - r * 0.66f, cy - r * 1.12f)
        lineTo(cx - r * 0.02f, cy - r * 0.78f)
        close()
      }
      drawPath(hat, Brush.linearGradient(listOf(Color(0xFFEC4899), Color(0xFF8B5CF6))))
      drawAppleLaughEyes(cx, cy, r)
      drawCircle(Color(0xFF38BDF8), r * 0.09f, Offset(cx + r * 0.65f, cy - r * 0.68f))
      drawCircle(Color(0xFFF43F5E), r * 0.09f, Offset(cx + r * 0.78f, cy - r * 0.24f))
      drawAppleOpenLaughMouth(cx, cy, r)
    }

    "😇" -> {
      drawGlossySunSphere(cx, cy + r * 0.06f, r * 0.94f)
      drawOval(
        brush = Brush.linearGradient(listOf(Color(0xFF93C5FD), Color(0xFF38BDF8), Color(0xFFE0F2FE))),
        topLeft = Offset(cx - r * 0.64f, cy - r * 1.02f),
        size = Size(r * 1.28f, r * 0.28f),
        style = Stroke(width = r * 0.12f)
      )
      drawAppleLaughEyes(cx, cy + r * 0.06f, r * 0.94f)
      val haloSmile = Path().apply {
        moveTo(cx - r * 0.32f, cy + r * 0.32f)
        quadraticTo(cx, cy + r * 0.56f, cx + r * 0.32f, cy + r * 0.32f)
      }
      drawPath(haloSmile, Color(0xFF5D2906), style = Stroke(width = r * 0.10f, cap = StrokeCap.Round))
    }

    "🤔" -> {
      drawGlossySunSphere(cx, cy, r)
      drawCircle(Color(0xFF4A2506), r * 0.11f, Offset(cx - r * 0.32f, cy - r * 0.12f))
      drawCircle(Color(0xFF4A2506), r * 0.11f, Offset(cx + r * 0.32f, cy - r * 0.12f))
      val browL = Path().apply {
        moveTo(cx - r * 0.48f, cy - r * 0.34f)
        quadraticTo(cx - r * 0.30f, cy - r * 0.46f, cx - r * 0.14f, cy - r * 0.32f)
      }
      drawPath(browL, Color(0xFF5D2906), style = Stroke(width = r * 0.08f, cap = StrokeCap.Round))
      drawLine(Color(0xFF5D2906), Offset(cx + r * 0.14f, cy - r * 0.30f), Offset(cx + r * 0.48f, cy - r * 0.30f), strokeWidth = r * 0.08f, cap = StrokeCap.Round)
      drawLine(Color(0xFF5D2906), Offset(cx - r * 0.24f, cy + r * 0.28f), Offset(cx + r * 0.26f, cy + r * 0.22f), strokeWidth = r * 0.10f, cap = StrokeCap.Round)
    }

    "🤫" -> {
      drawGlossySunSphere(cx, cy, r)
      drawCircle(Color(0xFF4A2506), r * 0.11f, Offset(cx - r * 0.32f, cy - r * 0.14f))
      drawCircle(Color(0xFF4A2506), r * 0.11f, Offset(cx + r * 0.32f, cy - r * 0.14f))
      drawCircle(Color(0xFF4A2506), r * 0.12f, Offset(cx, cy + r * 0.24f))
      // Shushing finger
      drawRoundRect(
        brush = Brush.verticalGradient(listOf(Color(0xFFFFDF7E), Color(0xFFF59E0B))),
        topLeft = Offset(cx - r * 0.11f, cy + r * 0.12f),
        size = Size(r * 0.22f, r * 0.72f),
        cornerRadius = CornerRadius(r * 0.11f, r * 0.11f)
      )
    }

    "🫡", "🫥", "😴", "🤡" -> {
      val baseTop = if (emoji == "🤡") Color(0xFFFFFFFF) else if (emoji == "🫥") Color(0xFFE2E8F0) else Color(0xFFFFF176)
      val baseMid = if (emoji == "🤡") Color(0xFFE2E8F0) else if (emoji == "🫥") Color(0xFF94A3B8) else Color(0xFFFFB300)
      val baseBot = if (emoji == "🤡") Color(0xFF94A3B8) else if (emoji == "🫥") Color(0xFF475569) else Color(0xFFE67E00)
      drawGlossyCustomSphere(cx, cy, r, baseTop, baseMid, baseBot)
      when (emoji) {
        "🤡" -> {
          drawCircle(Color(0xFF38BDF8), r * 0.12f, Offset(cx - r * 0.32f, cy - r * 0.18f))
          drawCircle(Color(0xFF38BDF8), r * 0.12f, Offset(cx + r * 0.32f, cy - r * 0.18f))
          drawCircle(Color(0xFFE11D48), r * 0.22f, Offset(cx, cy + r * 0.04f))
          val smile = Path().apply {
            moveTo(cx - r * 0.40f, cy + r * 0.32f)
            quadraticTo(cx, cy + r * 0.62f, cx + r * 0.40f, cy + r * 0.32f)
          }
          drawPath(smile, Color(0xFFE11D48), style = Stroke(width = r * 0.12f, cap = StrokeCap.Round))
        }
        "😴" -> {
          drawLine(Color(0xFF4E2600), Offset(cx - r * 0.44f, cy - r * 0.12f), Offset(cx - r * 0.18f, cy - r * 0.12f), strokeWidth = r * 0.09f, cap = StrokeCap.Round)
          drawLine(Color(0xFF4E2600), Offset(cx + r * 0.18f, cy - r * 0.12f), Offset(cx + r * 0.44f, cy - r * 0.12f), strokeWidth = r * 0.09f, cap = StrokeCap.Round)
          drawCircle(Color(0xFF4E2600), r * 0.15f, Offset(cx, cy + r * 0.28f))
          drawEmojiTextFallback("Z", cx + r * 0.62f, cy - r * 0.56f, r * 0.58f)
        }
        else -> {
          drawCircle(Color(0xFF334155), r * 0.10f, Offset(cx - r * 0.30f, cy - r * 0.14f))
          drawCircle(Color(0xFF334155), r * 0.10f, Offset(cx + r * 0.30f, cy - r * 0.14f))
          drawLine(Color(0xFF334155), Offset(cx - r * 0.24f, cy + r * 0.26f), Offset(cx + r * 0.24f, cy + r * 0.26f), strokeWidth = r * 0.09f, cap = StrokeCap.Round)
        }
      }
    }

    "🔥" -> {
      val outerFlame = Path().apply {
        moveTo(cx, cy - r * 1.02f)
        cubicTo(cx + r * 0.72f, cy - r * 0.45f, cx + r * 0.88f, cy + r * 0.32f, cx + r * 0.52f, cy + r * 0.72f)
        cubicTo(cx + r * 0.24f, cy + r * 0.98f, cx - r * 0.24f, cy + r * 0.98f, cx - r * 0.52f, cy + r * 0.72f)
        cubicTo(cx - r * 0.90f, cy + r * 0.32f, cx - r * 0.62f, cy - r * 0.36f, cx, cy - r * 1.02f)
        close()
      }
      drawPath(
        outerFlame,
        Brush.verticalGradient(listOf(Color(0xFFFF5E3A), Color(0xFFFF2D55), Color(0xFFD90429)))
      )
      val innerFlame = Path().apply {
        moveTo(cx, cy - r * 0.46f)
        cubicTo(cx + r * 0.42f, cy - r * 0.06f, cx + r * 0.48f, cy + r * 0.46f, cx, cy + r * 0.76f)
        cubicTo(cx - r * 0.48f, cy + r * 0.46f, cx - r * 0.42f, cy - r * 0.06f, cx, cy - r * 0.46f)
        close()
      }
      drawPath(
        innerFlame,
        Brush.verticalGradient(listOf(Color(0xFFFFF59D), Color(0xFFFFC107), Color(0xFFFF6D00)))
      )
    }

    "✨" -> {
      drawSparkle4Point(cx - r * 0.14f, cy + r * 0.06f, r * 0.76f, Color(0xFFFFF9C4), Color(0xFFF59E0B))
      drawSparkle4Point(cx + r * 0.54f, cy - r * 0.48f, r * 0.36f, Color(0xFFFFFFFF), Color(0xFFFBBF24))
      drawSparkle4Point(cx - r * 0.58f, cy - r * 0.56f, r * 0.26f, Color(0xFFFFFFFF), Color(0xFFFDE68A))
    }

    "⚡" -> {
      val bolt = Path().apply {
        moveTo(cx + r * 0.18f, cy - r * 0.98f)
        lineTo(cx - r * 0.56f, cy + r * 0.08f)
        lineTo(cx + r * 0.02f, cy + r * 0.08f)
        lineTo(cx - r * 0.24f, cy + r * 0.98f)
        lineTo(cx + r * 0.58f, cy - r * 0.12f)
        lineTo(cx + r * 0.02f, cy - r * 0.12f)
        close()
      }
      drawPath(
        bolt,
        Brush.linearGradient(listOf(Color(0xFFFFF59D), Color(0xFFFACC15), Color(0xFFEA580C)))
      )
      drawPath(bolt, Color.White.copy(alpha = 0.45f), style = Stroke(width = r * 0.06f, join = StrokeJoin.Round))
    }

    "💎" -> {
      val diamond = Path().apply {
        moveTo(cx - r * 0.58f, cy - r * 0.54f)
        lineTo(cx + r * 0.58f, cy - r * 0.54f)
        lineTo(cx + r * 0.92f, cy - r * 0.06f)
        lineTo(cx, cy + r * 0.88f)
        lineTo(cx - r * 0.92f, cy - r * 0.06f)
        close()
      }
      drawPath(
        diamond,
        Brush.linearGradient(listOf(Color(0xFFE0F2FE), Color(0xFF38BDF8), Color(0xFF0284C7)))
      )
      // Facet specular triangle
      val facet = Path().apply {
        moveTo(cx - r * 0.36f, cy - r * 0.54f)
        lineTo(cx + r * 0.36f, cy - r * 0.54f)
        lineTo(cx, cy - r * 0.06f)
        close()
      }
      drawPath(facet, Color.White.copy(alpha = 0.45f))
      drawPath(diamond, Color.White.copy(alpha = 0.55f), style = Stroke(width = r * 0.06f, join = StrokeJoin.Round))
    }

    "🔮", "🧿", "🪩", "🫧" -> {
      val (c1, c2, c3) = when (emoji) {
        "🔮" -> Triple(Color(0xFFE9D5FF), Color(0xFF9333EA), Color(0xFF4C1D95))
        "🧿" -> Triple(Color(0xFF93C5FD), Color(0xFF1D4ED8), Color(0xFF1E3A8A))
        "🫧" -> Triple(Color(0xFFFFFFFF), Color(0xFF67E8F9), Color(0xFFA855F7))
        else -> Triple(Color(0xFFF1F5F9), Color(0xFF94A3B8), Color(0xFF334155))
      }
      drawGlossyCustomSphere(cx, cy, r, c1, c2, c3)
      if (emoji == "🧿") {
        drawCircle(Color.White, r * 0.52f, Offset(cx, cy))
        drawCircle(Color(0xFF38BDF8), r * 0.34f, Offset(cx, cy))
        drawCircle(Color(0xFF0F172A), r * 0.18f, Offset(cx, cy))
        drawSphereHighlight(cx, cy, r * 0.85f)
      } else if (emoji == "🔮") {
        drawSparkle4Point(cx + r * 0.20f, cy - r * 0.16f, r * 0.30f, Color.White, Color(0xFFF0ABFC))
      }
    }

    "💀" -> {
      drawGlossyCustomSphere(cx, cy - r * 0.08f, r * 0.86f, Color(0xFFFFFFFF), Color(0xFFE2E8F0), Color(0xFF94A3B8))
      drawRoundRect(
        brush = Brush.verticalGradient(listOf(Color(0xFFE2E8F0), Color(0xFF94A3B8))),
        topLeft = Offset(cx - r * 0.42f, cy + r * 0.34f),
        size = Size(r * 0.84f, r * 0.50f),
        cornerRadius = CornerRadius(r * 0.16f, r * 0.16f)
      )
      drawOval(Color(0xFF1E293B), Offset(cx - r * 0.46f, cy - r * 0.22f), Size(r * 0.32f, r * 0.36f))
      drawOval(Color(0xFF1E293B), Offset(cx + r * 0.14f, cy - r * 0.22f), Size(r * 0.32f, r * 0.36f))
      drawCircle(Color(0xFF1E293B), r * 0.10f, Offset(cx, cy + r * 0.20f))
    }

    "👾" -> {
      drawRoundRect(
        brush = Brush.verticalGradient(listOf(Color(0xFFC084FC), Color(0xFF7E22CE))),
        topLeft = Offset(cx - r * 0.82f, cy - r * 0.56f),
        size = Size(r * 1.64f, r * 1.06f),
        cornerRadius = CornerRadius(r * 0.26f, r * 0.26f)
      )
      drawRect(Color(0xFF0F172A), Offset(cx - r * 0.44f, cy - r * 0.20f), Size(r * 0.24f, r * 0.28f))
      drawRect(Color(0xFF0F172A), Offset(cx + r * 0.20f, cy - r * 0.20f), Size(r * 0.24f, r * 0.28f))
      drawSphereHighlight(cx, cy - r * 0.04f, r * 0.78f)
    }

    "👁" -> {
      drawOval(
        brush = Brush.verticalGradient(listOf(Color.White, Color(0xFFE2E8F0))),
        topLeft = Offset(cx - r * 0.94f, cy - r * 0.52f),
        size = Size(r * 1.88f, r * 1.04f)
      )
      drawCircle(
        brush = Brush.radialGradient(listOf(Color(0xFF38BDF8), Color(0xFF1D4ED8))),
        radius = r * 0.42f,
        center = Offset(cx, cy)
      )
      drawCircle(Color(0xFF0F172A), r * 0.20f, Offset(cx, cy))
      drawCircle(Color.White, r * 0.09f, Offset(cx - r * 0.14f, cy - r * 0.14f))
    }

    "💯" -> {
      drawRoundRect(
        brush = Brush.linearGradient(listOf(Color(0xFFFF6B8B), Color(0xFFE11D48), Color(0xFF9F1239))),
        topLeft = Offset(cx - r * 0.92f, cy - r * 0.78f),
        size = Size(r * 1.84f, r * 1.56f),
        cornerRadius = CornerRadius(r * 0.34f, r * 0.34f)
      )
      drawSphereHighlight(cx, cy, r * 0.85f)
      drawEmojiTextFallback("100", cx, cy + r * 0.12f, r * 0.76f, android.graphics.Color.WHITE)
    }

    "🚀" -> {
      rotate(degrees = -40f, pivot = Offset(cx, cy)) {
        val body = Path().apply {
          moveTo(cx, cy - r * 0.96f)
          cubicTo(cx + r * 0.54f, cy - r * 0.38f, cx + r * 0.44f, cy + r * 0.52f, cx + r * 0.24f, cy + r * 0.66f)
          lineTo(cx - r * 0.24f, cy + r * 0.66f)
          cubicTo(cx - r * 0.44f, cy + r * 0.52f, cx - r * 0.54f, cy - r * 0.38f, cx, cy - r * 0.96f)
          close()
        }
        drawPath(body, Brush.verticalGradient(listOf(Color(0xFFF8FAFC), Color(0xFFCBD5E1))))
        drawCircle(Color(0xFF38BDF8), r * 0.20f, Offset(cx, cy - r * 0.16f))
        drawOval(Color(0xFFF97316), Offset(cx - r * 0.18f, cy + r * 0.66f), Size(r * 0.36f, r * 0.38f))
      }
    }

    "👑" -> {
      val crown = Path().apply {
        moveTo(cx - r * 0.86f, cy + r * 0.56f)
        lineTo(cx - r * 0.96f, cy - r * 0.42f)
        lineTo(cx - r * 0.42f, cy + r * 0.04f)
        lineTo(cx, cy - r * 0.68f)
        lineTo(cx + r * 0.42f, cy + r * 0.04f)
        lineTo(cx + r * 0.96f, cy - r * 0.42f)
        lineTo(cx + r * 0.86f, cy + r * 0.56f)
        close()
      }
      drawPath(crown, Brush.verticalGradient(listOf(Color(0xFFFEF08A), Color(0xFFF59E0B), Color(0xFFB45309))))
      drawCircle(Color(0xFFE11D48), r * 0.13f, Offset(cx, cy + r * 0.20f))
      drawCircle(Color(0xFF38BDF8), r * 0.10f, Offset(cx - r * 0.48f, cy + r * 0.24f))
      drawCircle(Color(0xFF38BDF8), r * 0.10f, Offset(cx + r * 0.48f, cy + r * 0.24f))
    }

    "🗿" -> {
      val moai = Path().apply {
        moveTo(cx - r * 0.52f, cy - r * 0.90f)
        lineTo(cx + r * 0.46f, cy - r * 0.84f)
        lineTo(cx + r * 0.64f, cy + r * 0.84f)
        lineTo(cx - r * 0.58f, cy + r * 0.84f)
        close()
      }
      drawPath(moai, Brush.linearGradient(listOf(Color(0xFFCBD5E1), Color(0xFF64748B), Color(0xFF334155))))
      // Strong brow & nose bridge
      drawRoundRect(Color(0xFF475569), Offset(cx - r * 0.46f, cy - r * 0.48f), Size(r * 0.92f, r * 0.22f), CornerRadius(6f, 6f))
      drawRoundRect(Color(0xFF94A3B8), Offset(cx - r * 0.16f, cy - r * 0.34f), Size(r * 0.26f, r * 0.62f), CornerRadius(8f, 8f))
      drawLine(Color(0xFF1E293B), Offset(cx - r * 0.26f, cy + r * 0.46f), Offset(cx + r * 0.24f, cy + r * 0.46f), strokeWidth = r * 0.08f, cap = StrokeCap.Round)
    }

    else -> {
      // Juicy 3D Apple-style Frosted Glass Orb hosting crisp glyph inside so ALL remaining symbols look glossy & unified!
      val (orbTop, orbMid, orbBot) = when {
        emoji in listOf("🦄", "🪩", "🎭", "🌈", "🦋") ->
          Triple(Color(0xFFF5D0FE), Color(0xFFC084FC), Color(0xFF7E22CE))
        emoji in listOf("🛸", "🤖", "🦾", "🌙", "☄") ->
          Triple(Color(0xFFCFFAFE), Color(0xFF22D3EE), Color(0xFF0E7490))
        emoji in listOf("🦖", "🐸", "🐉") ->
          Triple(Color(0xFFDCFCE7), Color(0xFF34D399), Color(0xFF047857))
        emoji in listOf("🍓", "🌶", "🫀") ->
          Triple(Color(0xFFFFE4E6), Color(0xFFFB7185), Color(0xFFBE123C))
        else ->
          Triple(Color(0xFF38BDF8).copy(alpha = 0.45f), Color(0xFF1E293B), Color(0xFF0F172A))
      }
      drawGlossyCustomSphere(cx, cy, r * 1.02f, orbTop, orbMid, orbBot)
      drawCircle(
        color = Color.White.copy(alpha = 0.28f),
        radius = r * 1.02f,
        center = Offset(cx, cy),
        style = Stroke(width = r * 0.06f)
      )
      drawEmojiTextFallback(emoji, cx, cy + r * 0.36f, r * 1.06f)
    }
  }
}

private fun DrawScope.drawGlossyHeart(
  cx: Float,
  cy: Float,
  r: Float,
  topColor: Color,
  midColor: Color,
  deepColor: Color
) {
  val heartPath = Path().apply {
    moveTo(cx, cy + r * 0.86f)
    cubicTo(cx - r * 1.18f, cy + r * 0.16f, cx - r * 1.12f, cy - r * 0.90f, cx, cy - r * 0.32f)
    cubicTo(cx + r * 1.12f, cy - r * 0.90f, cx + r * 1.18f, cy + r * 0.16f, cx, cy + r * 0.86f)
    close()
  }
  drawPath(
    path = heartPath,
    brush = Brush.radialGradient(
      colors = listOf(topColor, midColor, deepColor),
      center = Offset(cx - r * 0.30f, cy - r * 0.34f),
      radius = r * 1.45f
    )
  )
  // Specular iPhone-style top-left gloss lobe
  drawOval(
    brush = Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.70f), Color.Transparent)),
    topLeft = Offset(cx - r * 0.72f, cy - r * 0.56f),
    size = Size(r * 0.46f, r * 0.28f)
  )
  drawOval(
    brush = Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.45f), Color.Transparent)),
    topLeft = Offset(cx + r * 0.26f, cy - r * 0.56f),
    size = Size(r * 0.38f, r * 0.22f)
  )
}

private fun DrawScope.drawGlossySunSphere(cx: Float, cy: Float, r: Float) {
  drawGlossyCustomSphere(
    cx = cx,
    cy = cy,
    r = r,
    topColor = Color(0xFFFFF59D),
    midColor = Color(0xFFFFC107),
    deepColor = Color(0xFFE67E00)
  )
}

private fun DrawScope.drawGlossyCustomSphere(
  cx: Float,
  cy: Float,
  r: Float,
  topColor: Color,
  midColor: Color,
  deepColor: Color
) {
  drawCircle(
    brush = Brush.radialGradient(
      colors = listOf(topColor, midColor, deepColor),
      center = Offset(cx - r * 0.28f, cy - r * 0.34f),
      radius = r * 1.48f
    ),
    radius = r,
    center = Offset(cx, cy)
  )
  drawSphereHighlight(cx, cy, r)
}

private fun DrawScope.drawSphereHighlight(cx: Float, cy: Float, r: Float) {
  drawOval(
    brush = Brush.verticalGradient(
      listOf(Color.White.copy(alpha = 0.62f), Color.White.copy(alpha = 0.05f), Color.Transparent)
    ),
    topLeft = Offset(cx - r * 0.60f, cy - r * 0.84f),
    size = Size(r * 1.20f, r * 0.54f)
  )
}

private fun DrawScope.drawAppleLaughEyes(cx: Float, cy: Float, r: Float) {
  val stroke = Stroke(width = r * 0.11f, cap = StrokeCap.Round, join = StrokeJoin.Round)
  val left = Path().apply {
    moveTo(cx - r * 0.56f, cy - r * 0.08f)
    lineTo(cx - r * 0.32f, cy - r * 0.28f)
    lineTo(cx - r * 0.12f, cy - r * 0.08f)
  }
  val right = Path().apply {
    moveTo(cx + r * 0.12f, cy - r * 0.08f)
    lineTo(cx + r * 0.32f, cy - r * 0.28f)
    lineTo(cx + r * 0.56f, cy - r * 0.08f)
  }
  drawPath(left, Color(0xFF4A2506), style = stroke)
  drawPath(right, Color(0xFF4A2506), style = stroke)
}

private fun DrawScope.drawAppleHappyTears(cx: Float, cy: Float, r: Float) {
  rotate(degrees = -28f, pivot = Offset(cx - r * 0.68f, cy + r * 0.10f)) {
    drawGlossyTeardrop(cx - r * 0.72f, cy + r * 0.12f, r * 0.24f)
  }
  rotate(degrees = 28f, pivot = Offset(cx + r * 0.68f, cy + r * 0.10f)) {
    drawGlossyTeardrop(cx + r * 0.72f, cy + r * 0.12f, r * 0.24f)
  }
}

private fun DrawScope.drawGlossyTeardrop(cx: Float, cy: Float, r: Float) {
  val drop = Path().apply {
    moveTo(cx, cy - r)
    cubicTo(cx + r * 0.88f, cy, cx + r * 0.88f, cy + r, cx, cy + r)
    cubicTo(cx - r * 0.88f, cy + r, cx - r * 0.88f, cy, cx, cy - r)
    close()
  }
  drawPath(
    drop,
    Brush.verticalGradient(listOf(Color(0xFFBAE6FD), Color(0xFF38BDF8), Color(0xFF0284C7)))
  )
  drawCircle(Color.White.copy(alpha = 0.85f), r * 0.18f, Offset(cx - r * 0.16f, cy + r * 0.16f))
}

private fun DrawScope.drawApplePleadingEyes(cx: Float, cy: Float, r: Float) {
  for (side in listOf(-1f, 1f)) {
    val ex = cx + side * r * 0.34f
    val ey = cy - r * 0.16f
    drawCircle(Color(0xFF3B1D0A), r * 0.19f, Offset(ex, ey))
    drawCircle(Color.White, r * 0.085f, Offset(ex - r * 0.05f, ey - r * 0.07f))
    drawCircle(Color.White.copy(alpha = 0.8f), r * 0.045f, Offset(ex + r * 0.06f, ey + r * 0.06f))
  }
}

private fun DrawScope.drawAppleOpenLaughMouth(cx: Float, cy: Float, r: Float) {
  val mouthPath = Path().apply {
    moveTo(cx - r * 0.56f, cy + r * 0.10f)
    lineTo(cx + r * 0.56f, cy + r * 0.10f)
    cubicTo(cx + r * 0.54f, cy + r * 0.72f, cx - r * 0.54f, cy + r * 0.72f, cx - r * 0.56f, cy + r * 0.10f)
    close()
  }
  drawPath(
    mouthPath,
    Brush.verticalGradient(listOf(Color(0xFF6B240C), Color(0xFF340C02)))
  )
  // Upper teeth arc
  drawRoundRect(
    color = Color.White,
    topLeft = Offset(cx - r * 0.44f, cy + r * 0.10f),
    size = Size(r * 0.88f, r * 0.15f),
    cornerRadius = CornerRadius(r * 0.06f, r * 0.06f)
  )
  // Glossy tongue
  drawOval(
    brush = Brush.verticalGradient(listOf(Color(0xFFFF8FA3), Color(0xFFE11D48))),
    topLeft = Offset(cx - r * 0.24f, cy + r * 0.38f),
    size = Size(r * 0.48f, r * 0.20f)
  )
}

private fun DrawScope.drawStarShape(
  cx: Float,
  cy: Float,
  r: Float,
  fillTop: Color,
  fillBot: Color
) {
  val path = Path()
  for (i in 0 until 10) {
    val rad = if (i % 2 == 0) r else r * 0.44f
    val angle = (-PI / 2.0) + i * (PI / 5.0)
    val x = cx + (rad * cos(angle)).toFloat()
    val y = cy + (rad * sin(angle)).toFloat()
    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
  }
  path.close()
  drawPath(path, Brush.verticalGradient(listOf(fillTop, fillBot)))
}

private fun DrawScope.drawSparkle4Point(
  cx: Float,
  cy: Float,
  r: Float,
  topColor: Color,
  botColor: Color
) {
  val path = Path().apply {
    moveTo(cx, cy - r)
    quadraticTo(cx + r * 0.18f, cy - r * 0.18f, cx + r, cy)
    quadraticTo(cx + r * 0.18f, cy + r * 0.18f, cx, cy + r)
    quadraticTo(cx - r * 0.18f, cy + r * 0.18f, cx - r, cy)
    quadraticTo(cx - r * 0.18f, cy - r * 0.18f, cx, cy - r)
    close()
  }
  drawPath(path, Brush.linearGradient(listOf(topColor, botColor)))
}

private fun DrawScope.drawEmojiTextFallback(
  text: String,
  x: Float,
  y: Float,
  textSizePx: Float,
  colorInt: Int = android.graphics.Color.WHITE
) {
  drawContext.canvas.nativeCanvas.drawText(
    text,
    x,
    y,
    Paint(Paint.ANTI_ALIAS_FLAG).apply {
      textAlign = Paint.Align.CENTER
      textSize = textSizePx
      color = colorInt
      typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
  )
}
