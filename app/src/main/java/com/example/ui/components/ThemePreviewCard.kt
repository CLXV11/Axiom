package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentColorChoice
import com.example.ui.theme.AppThemePreset
import com.example.ui.theme.buildKoraColorScheme
import com.example.ui.util.LocalAppStrings

/**
 * Live interactive preview card reflecting the selected theme preset, AMOLED mode, and accent choice.
 */
@Composable
fun ThemePreviewCard(
  preset: AppThemePreset,
  amoledPureBlack: Boolean,
  accentChoice: AccentColorChoice,
  modifier: Modifier = Modifier
) {
  val strings = LocalAppStrings.current
  val (colorScheme, _) = buildKoraColorScheme(
    preset = preset,
    isSystemDark = preset.isDark,
    amoledPureBlack = amoledPureBlack,
    accentChoice = accentChoice
  )

  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = colorScheme.background),
    border = androidx.compose.foundation.BorderStroke(1.dp, colorScheme.outline),
    modifier = modifier.fillMaxWidth()
  ) {
    Column(modifier = Modifier.fillMaxWidth()) {
      // Mock Discord Channel Top Bar
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(colorScheme.surface)
          .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          imageVector = Icons.Default.Numbers,
          contentDescription = null,
          tint = colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
          modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = strings.livePreviewChannel,
          color = colorScheme.onSurface,
          fontWeight = FontWeight.Bold,
          fontSize = 13.sp
        )
        Spacer(modifier = Modifier.weight(1f))
        Box(
          modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(Color(0xFF23A55A))
        )
      }

      HorizontalDivider(color = colorScheme.outline.copy(alpha = 0.4f), thickness = 1.dp)

      // Mock Discord Message Stream Item
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(14.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.Top
        ) {
          // Mock Avatar
          Box(
            modifier = Modifier
              .size(34.dp)
              .clip(CircleShape)
              .background(colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "K",
              color = colorScheme.onPrimaryContainer,
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp
            )
          }

          Spacer(modifier = Modifier.width(10.dp))

          Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = "Alex Kora",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = colorScheme.primary
              )
              Spacer(modifier = Modifier.width(4.dp))
              Icon(
                imageVector = Icons.Default.Verified,
                contentDescription = null,
                tint = colorScheme.primary,
                modifier = Modifier.size(12.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "Today at 4:20 PM",
                fontSize = 10.sp,
                color = colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
              )
            }
            Spacer(modifier = Modifier.height(3.dp))
            Text(
              text = strings.livePreviewSampleMessage,
              fontSize = 12.sp,
              color = colorScheme.onSurfaceVariant,
              lineHeight = 16.sp
            )

            // Reaction pill
            Spacer(modifier = Modifier.height(6.dp))
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(colorScheme.surfaceVariant)
                .border(1.dp, colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
              Text(text = "🔥", fontSize = 11.sp)
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "12",
                fontSize = 11.sp,
                color = colorScheme.primary,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Mock Discord Composer Capsule
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(colorScheme.surfaceVariant)
            .padding(horizontal = 10.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(24.dp)
              .clip(CircleShape)
              .background(Color(0xFF4E5058)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Add,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(14.dp)
            )
          }

          Spacer(modifier = Modifier.width(8.dp))

          Text(
            text = "Message #${strings.livePreviewChannel}",
            fontSize = 11.sp,
            color = colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.weight(1f)
          )

          Box(
            modifier = Modifier
              .size(26.dp)
              .clip(CircleShape)
              .background(colorScheme.primary),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.Send,
              contentDescription = null,
              tint = colorScheme.onPrimary,
              modifier = Modifier.size(13.dp)
            )
          }
        }
      }
    }
  }
}
