package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.KoraTheme
import com.example.ui.util.LocalAppStrings

/**
 * Three animated wave dots for typing indicators.
 * Staggered using keyframe transitions for an organic, refined rhythm.
 */
@Composable
fun TypingDotsIndicator(
  modifier: Modifier = Modifier,
  dotSize: Dp = 6.dp,
  dotColor: Color = KoraTheme.colors.primary,
  travelDistance: Dp = 4.dp
) {
  val infiniteTransition = rememberInfiniteTransition(label = "typing_dots_transition")

  val dot1OffsetY by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 0f,
    animationSpec = infiniteRepeatable(
      animation = keyframes {
        durationMillis = 1100
        0f at 0 using FastOutSlowInEasing
        -1f at 220 using FastOutSlowInEasing
        0f at 440 using FastOutSlowInEasing
        0f at 1100
      },
      repeatMode = RepeatMode.Restart
    ),
    label = "dot1_anim"
  )

  val dot2OffsetY by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 0f,
    animationSpec = infiniteRepeatable(
      animation = keyframes {
        durationMillis = 1100
        0f at 160 using FastOutSlowInEasing
        -1f at 380 using FastOutSlowInEasing
        0f at 600 using FastOutSlowInEasing
        0f at 1100
      },
      repeatMode = RepeatMode.Restart
    ),
    label = "dot2_anim"
  )

  val dot3OffsetY by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 0f,
    animationSpec = infiniteRepeatable(
      animation = keyframes {
        durationMillis = 1100
        0f at 320 using FastOutSlowInEasing
        -1f at 540 using FastOutSlowInEasing
        0f at 760 using FastOutSlowInEasing
        0f at 1100
      },
      repeatMode = RepeatMode.Restart
    ),
    label = "dot3_anim"
  )

  Row(
    modifier = modifier,
    horizontalArrangement = Arrangement.spacedBy(4.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(
      modifier = Modifier
        .size(dotSize)
        .offset(y = travelDistance * dot1OffsetY)
        .clip(CircleShape)
        .background(dotColor)
    )
    Box(
      modifier = Modifier
        .size(dotSize)
        .offset(y = travelDistance * dot2OffsetY)
        .clip(CircleShape)
        .background(dotColor)
    )
    Box(
      modifier = Modifier
        .size(dotSize)
        .offset(y = travelDistance * dot3OffsetY)
        .clip(CircleShape)
        .background(dotColor)
    )
  }
}

/**
 * Message bubble typing indicator displayed inside the message stream.
 * Mimics an incoming message bubble with avatar, animated dots, and user name.
 */
@Composable
fun TypingBubble(
  typingUserNames: List<String>,
  modifier: Modifier = Modifier
) {
  val strings = LocalAppStrings.current
  if (typingUserNames.isEmpty()) return

  val displayText = when {
    typingUserNames.size == 1 -> String.format(strings.typingSingle, typingUserNames.first())
    typingUserNames.size == 2 -> "${typingUserNames[0]} & ${typingUserNames[1]} ${strings.isTyping}"
    else -> strings.typingMultiple
  }

  Row(
    modifier = modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp)
      .testTag("typing_indicator_bubble"),
    verticalAlignment = Alignment.Bottom,
    horizontalArrangement = Arrangement.Start
  ) {
    AvatarView(
      name = typingUserNames.firstOrNull() ?: "User",
      size = 28.dp
    )

    Spacer(modifier = Modifier.width(8.dp))

    Surface(
      shape = RoundedCornerShape(
        topStart = 20.dp,
        topEnd = 20.dp,
        bottomEnd = 20.dp,
        bottomStart = 6.dp
      ),
      color = KoraTheme.colors.surfaceVariant.copy(alpha = 0.65f),
      tonalElevation = 1.dp,
      modifier = Modifier
        .border(
          width = 1.dp,
          color = KoraTheme.colors.outlineVariant.copy(alpha = 0.35f),
          shape = RoundedCornerShape(
            topStart = 20.dp,
            topEnd = 20.dp,
            bottomEnd = 20.dp,
            bottomStart = 6.dp
          )
        )
    ) {
      Row(
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        TypingDotsIndicator(
          dotSize = 5.dp,
          dotColor = KoraTheme.colors.primary,
          travelDistance = 3.dp
        )

        Text(
          text = displayText,
          style = KoraTheme.typography.bodySmall.copy(fontSize = 12.sp),
          color = KoraTheme.colors.onSurfaceVariant,
          fontWeight = FontWeight.Medium
        )
      }
    }
  }
}

/**
 * Modern floating typing strip positioned directly above the chat composer input.
 * Provides instant feedback even if the chat list is scrolled upwards.
 */
@Composable
fun TypingStatusBanner(
  typingUserNames: List<String>,
  modifier: Modifier = Modifier
) {
  val strings = LocalAppStrings.current

  AnimatedVisibility(
    visible = typingUserNames.isNotEmpty(),
    enter = fadeIn() + slideInVertically { it / 2 },
    exit = fadeOut() + slideOutVertically { it / 2 },
    modifier = modifier
  ) {
    val displayText = when {
      typingUserNames.size == 1 -> String.format(strings.typingSingle, typingUserNames.first())
      typingUserNames.size == 2 -> "${typingUserNames[0]} & ${typingUserNames[1]} ${strings.isTyping}"
      else -> strings.typingMultiple
    }

    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 4.dp)
        .testTag("typing_status_banner"),
      verticalAlignment = Alignment.CenterVertically
    ) {
      TypingDotsIndicator(
        dotSize = 5.dp,
        dotColor = KoraTheme.colors.primary,
        travelDistance = 3.dp
      )

      Spacer(modifier = Modifier.width(8.dp))

      Text(
        text = displayText,
        style = KoraTheme.typography.labelSmall.copy(fontSize = 12.sp),
        color = KoraTheme.colors.primary,
        fontWeight = FontWeight.Medium,
        maxLines = 1
      )
    }
  }
}
