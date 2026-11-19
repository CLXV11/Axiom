package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.KoraTheme
import com.example.ui.theme.StatusOnline

@Composable
fun KoraTopBar(
  title: String,
  subtitle: String? = null,
  isVerified: Boolean = false,
  verifiedTier: String? = null,
  prefixType: String? = null, // "channel", "dm", or null
  isOnline: Boolean? = null,
  onBackClick: (() -> Unit)? = null,
  onLanguageToggle: (() -> Unit)? = null,
  actions: @Composable () -> Unit = {}
) {
  val spacing = KoraTheme.spacing
  val colors = KoraTheme.colors

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .background(colors.surface)
      .statusBarsPadding()
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .defaultMinSize(minHeight = 60.dp)
        .padding(horizontal = spacing.small, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      if (onBackClick != null) {
        IconButton(onClick = onBackClick) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = colors.onSurface
          )
        }
      } else {
        Spacer(modifier = Modifier.width(spacing.small))
      }

      if (prefixType == "channel") {
        Icon(
          imageVector = Icons.Default.Numbers,
          contentDescription = "Channel",
          tint = colors.onSurfaceVariant,
          modifier = Modifier
            .padding(end = 6.dp)
            .size(20.dp)
        )
      } else if (prefixType == "dm") {
        Text(
          text = "@",
          color = colors.onSurfaceVariant,
          fontWeight = FontWeight.Bold,
          fontSize = 19.sp,
          modifier = Modifier.padding(end = 4.dp)
        )
      }

      Column(modifier = Modifier.weight(1f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = title,
            style = KoraTheme.typography.titleMedium,
            color = colors.onSurface,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
          if (isVerified) {
            Spacer(modifier = Modifier.width(6.dp))
            VerifiedBadge(size = 16.dp, tierKey = verifiedTier, showDialogOnClick = true)
          }
        }
        if (!subtitle.isNullOrBlank()) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 1.dp)
          ) {
            if (isOnline == true) {
              Box(
                modifier = Modifier
                  .size(8.dp)
                  .clip(CircleShape)
                  .background(StatusOnline)
              )
              Spacer(modifier = Modifier.width(5.dp))
            }
            Text(
              text = subtitle,
              style = KoraTheme.typography.labelSmall,
              color = colors.onSurfaceVariant,
              maxLines = 2,
              overflow = TextOverflow.Ellipsis
            )
          }
        }
      }

      if (onLanguageToggle != null) {
        IconButton(onClick = onLanguageToggle) {
          Icon(
            imageVector = Icons.Default.Translate,
            contentDescription = "Switch Language",
            tint = colors.onSurfaceVariant
          )
        }
      }

      actions()
    }

    HorizontalDivider(
      thickness = 1.dp,
      color = colors.outlineVariant.copy(alpha = 0.45f)
    )
  }
}
