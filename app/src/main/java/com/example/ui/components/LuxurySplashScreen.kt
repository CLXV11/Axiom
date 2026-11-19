package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.util.LocalAppStrings
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Editorial luxury cold-start reveal for Kora.
 * Runs non-blocking in ~1.15s alongside Firebase session initialization,
 * and allows instant tap-to-enter so launch never stalls.
 */
@Composable
fun LuxurySplashScreen(
  visible: Boolean,
  onFinished: () -> Unit
) {
  val strings = LocalAppStrings.current
  val primaryAccent = MaterialTheme.colorScheme.primary

  val emblemScale = remember { Animatable(0.88f) }
  val emblemAlpha = remember { Animatable(0f) }
  val hairlineProgress = remember { Animatable(0f) }
  val backdropScale = remember { Animatable(1.06f) }
  val arcSweep = remember { Animatable(0f) }

  LaunchedEffect(visible) {
    if (!visible) return@LaunchedEffect
    launch {
      emblemAlpha.animateTo(1f, animationSpec = tween(420, easing = FastOutSlowInEasing))
    }
    launch {
      emblemScale.animateTo(1f, animationSpec = tween(560, easing = FastOutSlowInEasing))
    }
    launch {
      backdropScale.animateTo(1f, animationSpec = tween(1100, easing = FastOutSlowInEasing))
    }
    launch {
      delay(140L)
      hairlineProgress.animateTo(1f, animationSpec = tween(520, easing = FastOutSlowInEasing))
    }
    launch {
      delay(80L)
      arcSweep.animateTo(360f, animationSpec = tween(820, easing = FastOutSlowInEasing))
    }
    delay(1150L)
    onFinished()
  }

  AnimatedVisibility(
    visible = visible,
    enter = fadeIn(tween(150)),
    exit = fadeOut(tween(340, easing = FastOutSlowInEasing))
  ) {
    val obsidianAnchor = Color(0xFF050811)
    val champagneGold = Color(0xFFE7C88B)

    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(obsidianAnchor)
        .clickable(
          interactionSource = remember { MutableInteractionSource() },
          indication = null,
          onClick = onFinished
        )
        .testTag("luxury_splash_root")
    ) {
      // Sculpted matte dark architectural backdrop
      Image(
        painter = painterResource(id = R.drawable.img_luxury_intro_bg),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier
          .fillMaxSize()
          .scale(backdropScale.value)
          .alpha(0.68f)
      )

      // Vertical obsidian scrim for high contrast editorial typography
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(
            Brush.verticalGradient(
              colors = listOf(
                obsidianAnchor.copy(alpha = 0.72f),
                obsidianAnchor.copy(alpha = 0.36f),
                obsidianAnchor.copy(alpha = 0.88f)
              )
            )
          )
      )

      // Top subtle version / edition marker
      Row(
        modifier = Modifier
          .align(Alignment.TopCenter)
          .statusBarsPadding()
          .padding(top = 24.dp)
          .alpha(emblemAlpha.value * 0.75f),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Box(
          modifier = Modifier
            .size(6.dp)
            .clip(CircleShape)
            .background(champagneGold)
        )
        Text(
          text = "KORA EDITION",
          style = MaterialTheme.typography.labelSmall.copy(
            letterSpacing = 3.sp,
            fontSize = 10.sp
          ),
          color = Color(0xFFE2E8F0)
        )
      }

      // Centered emblem & editorial wordmark
      Column(
        modifier = Modifier
          .align(Alignment.Center)
          .padding(horizontal = 32.dp)
          .alpha(emblemAlpha.value)
          .scale(emblemScale.value),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Box(
          modifier = Modifier.size(124.dp),
          contentAlignment = Alignment.Center
        ) {
          // Architectural hairline orbital ring
          Canvas(modifier = Modifier.fillMaxSize()) {
            val strokePx = 1.4.dp.toPx()
            drawCircle(
              color = Color.White.copy(alpha = 0.10f),
              style = Stroke(width = strokePx)
            )
            drawArc(
              brush = Brush.sweepGradient(
                listOf(
                  champagneGold.copy(alpha = 0.85f),
                  primaryAccent.copy(alpha = 0.90f),
                  Color.Transparent
                )
              ),
              startAngle = -90f,
              sweepAngle = arcSweep.value,
              useCenter = false,
              style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
            )
          }

          Surface(
            modifier = Modifier.size(86.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF0E1424),
            border = androidx.compose.foundation.BorderStroke(
              1.dp,
              Brush.linearGradient(
                listOf(
                  champagneGold.copy(alpha = 0.55f),
                  primaryAccent.copy(alpha = 0.45f)
                )
              )
            ),
            shadowElevation = 12.dp
          ) {
            Image(
              painter = painterResource(id = R.drawable.kora_icon),
              contentDescription = strings.appName,
              contentScale = ContentScale.Crop,
              modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(24.dp))
            )
          }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
          text = "KORA",
          style = MaterialTheme.typography.headlineMedium.copy(
            letterSpacing = 8.sp,
            fontWeight = FontWeight.Bold
          ),
          color = Color.White
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Expanding architectural hairline rule
        Box(
          modifier = Modifier
            .width((56f * hairlineProgress.value).dp)
            .height(1.5.dp)
            .background(
              Brush.horizontalGradient(
                listOf(Color.Transparent, champagneGold, Color.Transparent)
              )
            )
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
          text = strings.splashEditorialLine,
          style = MaterialTheme.typography.bodyMedium.copy(
            letterSpacing = 0.4.sp
          ),
          color = Color(0xFFCBD5E1),
          textAlign = TextAlign.Center
        )
      }

      // Subtle bottom tap-to-enter hint
      Surface(
        modifier = Modifier
          .align(Alignment.BottomCenter)
          .navigationBarsPadding()
          .padding(bottom = 32.dp)
          .alpha(emblemAlpha.value * 0.85f)
          .testTag("luxury_splash_skip"),
        shape = RoundedCornerShape(999.dp),
        color = Color.White.copy(alpha = 0.08f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.14f))
      ) {
        Text(
          text = strings.splashTapToSkip,
          style = MaterialTheme.typography.labelMedium,
          color = Color(0xFFE2E8F0),
          modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp)
        )
      }
    }
  }
}
