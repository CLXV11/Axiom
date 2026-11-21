package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DecorationItem
import com.example.data.model.DecorationType
import com.example.data.model.ItemRarity
import com.example.ui.util.LocalAppStrings

@Composable
fun StoreFilterChip(
  label: String,
  selected: Boolean,
  onClick: () -> Unit
) {
  val colors = MaterialTheme.colorScheme
  Box(
    modifier = Modifier
      .defaultMinSize(minHeight = 36.dp)
      .clip(RoundedCornerShape(14.dp))
      .background(
        if (selected) Brush.horizontalGradient(listOf(Color(0xFF4F46E5), Color(0xFF2563EB)))
        else Brush.horizontalGradient(listOf(Color(0xFF171926), Color(0xFF1E2235)))
      )
      .border(
        1.dp,
        if (selected) Color(0xFF60A5FA) else colors.outlineVariant.copy(alpha = 0.45f),
        RoundedCornerShape(14.dp)
      )
      .clickable(onClick = onClick)
      .padding(horizontal = 14.dp, vertical = 8.dp),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = label,
      color = if (selected) Color.White else colors.onSurfaceVariant,
      style = MaterialTheme.typography.labelMedium,
      fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
      maxLines = 1,
      softWrap = false
    )
  }
}

@Composable
fun CollectorDecorationCard(
  item: DecorationItem,
  isOwned: Boolean,
  isEquipped: Boolean,
  isArabic: Boolean,
  onPreviewClick: () -> Unit
) {
  val strings = LocalAppStrings.current
  val gemColor = when (item.rarity) {
    ItemRarity.LEGENDARY -> Color(0xFFF59E0B)
    ItemRarity.EPIC -> Color(0xFFA855F7)
    ItemRarity.RARE -> Color(0xFF38BDF8)
    ItemRarity.COMMON -> Color(0xFF94A3B8)
  }
  val cardBorderColor = when {
    isEquipped -> Color(0xFF10B981)
    else -> gemColor.copy(alpha = 0.72f)
  }
  val buttonGradient = when {
    isEquipped -> listOf(Color(0xFF047857), Color(0xFF10B981), Color(0xFF047857))
    item.rarity == ItemRarity.LEGENDARY -> listOf(Color(0xFF92400E), Color(0xFFF59E0B), Color(0xFFD97706))
    item.rarity == ItemRarity.EPIC -> listOf(Color(0xFF5B21B6), Color(0xFFA855F7), Color(0xFF7C3AED))
    else -> listOf(Color(0xFF1E3A8A), Color(0xFF2563EB), Color(0xFF38BDF8))
  }

  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    border = BorderStroke(1.3.dp, cardBorderColor),
    colors = CardDefaults.cardColors(containerColor = Color(0xFF0D0F18)),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .background(
          Brush.verticalGradient(
            listOf(Color(0xFF0B0D16), Color(0xFF121521), Color(0xFF171A26))
          )
        )
        .padding(11.dp)
    ) {
      // Art Stage with Radial Rarity Spotlight & Faceted Geometry
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(118.dp)
          .clip(RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
      ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
          drawRect(
            brush = Brush.radialGradient(
              colors = listOf(gemColor.copy(alpha = 0.22f), Color(0xFF0A0C14)),
              center = Offset(size.width / 2f, size.height * 0.48f),
              radius = size.maxDimension * 0.72f
            )
          )
          // Subtle faceted polygon wing accents matching screenshots
          val leftFacet = Path().apply {
            moveTo(0f, size.height * 0.55f)
            lineTo(size.width * 0.25f, size.height * 0.42f)
            lineTo(size.width * 0.32f, size.height * 0.72f)
            lineTo(0f, size.height * 0.88f)
            close()
          }
          val rightFacet = Path().apply {
            moveTo(size.width, size.height * 0.55f)
            lineTo(size.width * 0.75f, size.height * 0.42f)
            lineTo(size.width * 0.68f, size.height * 0.72f)
            lineTo(size.width, size.height * 0.88f)
            close()
          }
          drawPath(leftFacet, color = gemColor.copy(alpha = 0.08f))
          drawPath(rightFacet, color = gemColor.copy(alpha = 0.08f))
        }

        when (item.type) {
          DecorationType.AVATAR_FRAME -> {
            AvatarView(
              name = "K",
              size = 86.dp,
              frameStyleKey = item.styleKey
            )
          }

          DecorationType.ROLE_NAMEPLATE -> {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp),
              contentAlignment = Alignment.CenterStart
            ) {
              RoleNameplateView(
                styleKey = item.styleKey,
                title = if (isArabic) item.nameAr else item.nameEn,
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(top = 12.dp)
              )
              Box(
                modifier = Modifier
                  .offset(x = 8.dp, y = (-8).dp)
                  .clip(CircleShape)
                  .background(Color(0xFF0A0C14))
                  .border(1.2.dp, Color.White.copy(alpha = 0.75f), CircleShape)
                  .padding(2.dp)
              ) {
                AvatarView(name = "K", size = 30.dp)
              }
            }
          }

          DecorationType.ACHIEVEMENT_WALL -> {
            AchievementWallView(
              styleKey = item.styleKey,
              scrimAlpha = 0.06f,
              modifier = Modifier
                .size(width = 112.dp, height = 96.dp)
                .clip(RoundedCornerShape(14.dp))
                .border(1.dp, Color.White.copy(alpha = 0.22f), RoundedCornerShape(14.dp))
            )
          }

          DecorationType.CHAT_BUBBLE -> {
            RoleNameplateView(
              styleKey = item.styleKey,
              title = if (isArabic) item.nameAr else item.nameEn
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Price + Optional Seasonal Tag
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.MonetizationOn,
            contentDescription = null,
            tint = Color(0xFFF59E0B),
            modifier = Modifier.size(15.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "${item.price}",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleSmall
          )
        }

        val tagText = if (isArabic) item.badgeTagAr else item.badgeTagEn
        if (!tagText.isNullOrBlank()) {
          Text(
            text = tagText,
            color = Color(0xFFC084FC),
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
          )
        }
      }

      Spacer(modifier = Modifier.height(4.dp))

      // Rarity Gem + Item Title
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.Diamond,
          contentDescription = null,
          tint = gemColor,
          modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
          text = if (isArabic) item.nameAr else item.nameEn,
          color = Color.White,
          fontWeight = FontWeight.Bold,
          style = MaterialTheme.typography.bodyMedium,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }

      Spacer(modifier = Modifier.height(3.dp))

      Text(
        text = if (isArabic) item.descriptionAr else item.descriptionEn,
        color = Color(0xFF94A3B8),
        style = MaterialTheme.typography.labelSmall,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        minLines = 2
      )

      Spacer(modifier = Modifier.height(10.dp))

      // Sparkling Radiant Preview / Equip / Equipped CTA Button matching Screenshots
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(38.dp)
          .clip(RoundedCornerShape(10.dp))
          .background(Brush.horizontalGradient(buttonGradient))
          .border(1.dp, Color.White.copy(alpha = 0.42f), RoundedCornerShape(10.dp))
          .clickable(onClick = onPreviewClick),
        contentAlignment = Alignment.Center
      ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
          // Delicate left & right 4-point sparkles
          drawFourPointStar(Offset(size.width * 0.14f, size.height * 0.5f), 5.dp.toPx(), Color.White.copy(alpha = 0.78f))
          drawFourPointStar(Offset(size.width * 0.22f, size.height * 0.34f), 2.5.dp.toPx(), Color.White.copy(alpha = 0.55f))
          drawFourPointStar(Offset(size.width * 0.86f, size.height * 0.5f), 5.dp.toPx(), Color.White.copy(alpha = 0.78f))
          drawFourPointStar(Offset(size.width * 0.78f, size.height * 0.66f), 2.5.dp.toPx(), Color.White.copy(alpha = 0.55f))
        }

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center,
          modifier = Modifier.padding(horizontal = 10.dp)
        ) {
          if (isEquipped) {
            Icon(
              imageVector = Icons.Default.Check,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
          }
          Text(
            text = when {
              isEquipped -> strings.equipped
              isOwned -> strings.equip
              else -> strings.preview
            },
            color = Color.White,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelLarge,
            maxLines = 1,
            softWrap = false
          )
        }
      }
    }
  }
}
