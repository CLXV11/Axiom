package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.ui.theme.StatusOnline
import com.example.ui.theme.StatusQuiet

@Composable
fun AvatarView(
  name: String,
  modifier: Modifier = Modifier,
  avatarUrl: String? = null,
  size: Dp = 44.dp,
  frameStyleKey: String? = null,
  showPresence: Boolean = false,
  isOnline: Boolean = false,
  isQuiet: Boolean = false
) {
  val initial = name.trim().take(1).uppercase().ifEmpty { "K" }
  val innerCircleSize = if (frameStyleKey != null) size * 0.64f else size

  Box(
    modifier = modifier.size(size),
    contentAlignment = Alignment.Center
  ) {
    // Inner Avatar circle
    Box(
      modifier = Modifier
        .size(innerCircleSize)
        .clip(CircleShape)
        .background(MaterialTheme.colorScheme.primaryContainer),
      contentAlignment = Alignment.Center
    ) {
      val preset = if (avatarUrl?.startsWith("preset:") == true) {
        PresetAvatars.findById(avatarUrl)
      } else null

      when {
        preset != null -> {
          Box(
            modifier = Modifier
              .fillMaxSize()
              .background(Brush.linearGradient(preset.gradient)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = preset.icon,
              contentDescription = preset.nameEn,
              tint = Color.White,
              modifier = Modifier.size(innerCircleSize * 0.55f)
            )
          }
        }
        !avatarUrl.isNullOrBlank() -> {
          SubcomposeAsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
              .data(avatarUrl)
              .crossfade(true)
              .build(),
            contentDescription = name,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            loading = {
              AvatarInitial(initial = initial, circleSize = innerCircleSize)
            },
            error = {
              AvatarInitial(initial = initial, circleSize = innerCircleSize)
            }
          )
        }
        else -> {
          AvatarInitial(initial = initial, circleSize = innerCircleSize)
        }
      }
    }

    // Custom Decorative Avatar Frame Overlay
    if (frameStyleKey != null) {
      AvatarFrameView(
        styleKey = frameStyleKey,
        modifier = Modifier.size(size)
      )
    }

    // Status indicator
    if (showPresence) {
      val statusColor = when {
        isQuiet -> StatusQuiet
        isOnline -> StatusOnline
        else -> Color.Transparent
      }

      if (statusColor != Color.Transparent) {
        val indicatorSize = (innerCircleSize.value * 0.28f).coerceAtLeast(10f).dp
        Box(
          modifier = Modifier
            .size(indicatorSize)
            .align(Alignment.BottomEnd)
            .clip(CircleShape)
            .background(statusColor)
            .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
        )
      }
    }
  }
}

@Composable
private fun AvatarInitial(initial: String, circleSize: Dp) {
  Box(
    modifier = Modifier.fillMaxSize(),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = initial,
      style = MaterialTheme.typography.titleMedium.copy(
        color = MaterialTheme.colorScheme.onPrimaryContainer,
        fontWeight = FontWeight.Bold,
        fontSize = (circleSize.value * 0.44f).sp
      )
    )
  }
}
