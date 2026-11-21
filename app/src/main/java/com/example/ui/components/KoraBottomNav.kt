package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.DynamicFeed
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.KoraTheme
import com.example.ui.util.LocalAppStrings

enum class NavigationTab {
  CHATS,
  FLUX,
  FEED,
  MEMORY,
  PROFILE,
  SETTINGS
}

@Composable
fun KoraBottomNav(
  currentTab: NavigationTab,
  onTabSelected: (NavigationTab) -> Unit,
  modifier: Modifier = Modifier
) {
  val strings = LocalAppStrings.current
  val colors = KoraTheme.colors

  Column(
    modifier = modifier
      .fillMaxWidth()
      .background(colors.surfaceContainerLowest)
      .windowInsetsPadding(WindowInsets.navigationBars)
  ) {
    HorizontalDivider(
      thickness = 1.dp,
      color = colors.outlineVariant.copy(alpha = 0.6f)
    )

    Row(
      modifier = Modifier
        .fillMaxWidth()
        .defaultMinSize(minHeight = 64.dp)
        .padding(horizontal = 6.dp, vertical = 6.dp),
      horizontalArrangement = Arrangement.SpaceEvenly,
      verticalAlignment = Alignment.CenterVertically
    ) {
      NavItem(
        label = strings.navChats,
        icon = Icons.Outlined.ChatBubbleOutline,
        isSelected = currentTab == NavigationTab.CHATS,
        onClick = { onTabSelected(NavigationTab.CHATS) }
      )

      NavItem(
        label = strings.navFlux,
        icon = Icons.Outlined.PlayCircle,
        isSelected = currentTab == NavigationTab.FLUX,
        onClick = { onTabSelected(NavigationTab.FLUX) }
      )

      NavItem(
        label = strings.navFeed,
        icon = Icons.Outlined.DynamicFeed,
        isSelected = currentTab == NavigationTab.FEED,
        onClick = { onTabSelected(NavigationTab.FEED) }
      )

      NavItem(
        label = strings.navMemory,
        icon = Icons.Outlined.BookmarkBorder,
        isSelected = currentTab == NavigationTab.MEMORY,
        onClick = { onTabSelected(NavigationTab.MEMORY) }
      )

      NavItem(
        label = strings.navProfile,
        icon = Icons.Outlined.PersonOutline,
        isSelected = currentTab == NavigationTab.PROFILE || currentTab == NavigationTab.SETTINGS,
        onClick = { onTabSelected(NavigationTab.PROFILE) }
      )
    }
  }
}

@Composable
private fun RowScope.NavItem(
  label: String,
  icon: ImageVector,
  isSelected: Boolean,
  onClick: () -> Unit
) {
  val interactionSource = remember { MutableInteractionSource() }
  val colors = KoraTheme.colors

  Column(
    modifier = Modifier
      .weight(1f)
      .defaultMinSize(minHeight = 52.dp)
      .clip(RoundedCornerShape(12.dp))
      .clickable(
        interactionSource = interactionSource,
        indication = ripple(bounded = true, radius = 32.dp),
        onClick = onClick
      )
      .padding(horizontal = 2.dp, vertical = 4.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Box(
      modifier = Modifier
        .size(width = 48.dp, height = 28.dp)
        .clip(RoundedCornerShape(10.dp))
        .background(
          if (isSelected) colors.primary.copy(alpha = 0.16f)
          else Color.Transparent
        ),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = label,
        modifier = Modifier.size(21.dp),
        tint = if (isSelected) colors.primary else colors.onSurfaceVariant
      )
    }

    Spacer(modifier = Modifier.height(3.dp))

    Text(
      text = label,
      style = KoraTheme.typography.labelSmall.copy(fontSize = 11.sp),
      color = if (isSelected) colors.primary else colors.onSurfaceVariant,
      fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
      maxLines = 1,
      softWrap = false,
      overflow = TextOverflow.Ellipsis
    )
  }
}
