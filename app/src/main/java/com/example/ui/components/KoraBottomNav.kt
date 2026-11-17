package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.DynamicFeed
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.DynamicFeed
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Settings
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
import androidx.compose.ui.unit.dp
import com.example.ui.theme.DiscordBlurple
import com.example.ui.theme.DiscordDarkDeepest
import com.example.ui.theme.DiscordTextMuted
import com.example.ui.theme.DiscordTextPrimary
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
  val spacing = KoraTheme.spacing

  Column(
    modifier = modifier
      .fillMaxWidth()
      .background(DiscordDarkDeepest)
      .windowInsetsPadding(WindowInsets.navigationBars)
  ) {
    HorizontalDivider(
      thickness = 1.dp,
      color = Color(0x33000000)
    )

    Row(
      modifier = Modifier
        .fillMaxWidth()
        .height(64.dp)
        .padding(horizontal = spacing.compact),
      horizontalArrangement = Arrangement.SpaceAround,
      verticalAlignment = Alignment.CenterVertically
    ) {
      NavItem(
        label = strings.navChats,
        selectedIcon = Icons.Filled.ChatBubble,
        unselectedIcon = Icons.Outlined.ChatBubbleOutline,
        isSelected = currentTab == NavigationTab.CHATS,
        onClick = { onTabSelected(NavigationTab.CHATS) }
      )

      NavItem(
        label = strings.navFlux,
        selectedIcon = Icons.Filled.PlayArrow,
        unselectedIcon = Icons.Outlined.PlayArrow,
        isSelected = currentTab == NavigationTab.FLUX,
        onClick = { onTabSelected(NavigationTab.FLUX) }
      )

      NavItem(
        label = strings.navFeed,
        selectedIcon = Icons.Filled.DynamicFeed,
        unselectedIcon = Icons.Outlined.DynamicFeed,
        isSelected = currentTab == NavigationTab.FEED,
        onClick = { onTabSelected(NavigationTab.FEED) }
      )

      NavItem(
        label = strings.navMemory,
        selectedIcon = Icons.Filled.Bookmark,
        unselectedIcon = Icons.Outlined.BookmarkBorder,
        isSelected = currentTab == NavigationTab.MEMORY,
        onClick = { onTabSelected(NavigationTab.MEMORY) }
      )

      NavItem(
        label = strings.navProfile,
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.PersonOutline,
        isSelected = currentTab == NavigationTab.PROFILE,
        onClick = { onTabSelected(NavigationTab.PROFILE) }
      )
    }
  }
}

@Composable
private fun NavItem(
  label: String,
  selectedIcon: ImageVector,
  unselectedIcon: ImageVector,
  isSelected: Boolean,
  onClick: () -> Unit
) {
  val interactionSource = remember { MutableInteractionSource() }
  val spacing = KoraTheme.spacing

  Column(
    modifier = Modifier
      .clip(RoundedCornerShape(16.dp))
      .clickable(
        interactionSource = interactionSource,
        indication = ripple(bounded = true, radius = 28.dp),
        onClick = onClick
      )
      .padding(horizontal = spacing.compact, vertical = 4.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Box(
      modifier = Modifier
        .size(width = 48.dp, height = 28.dp)
        .clip(RoundedCornerShape(14.dp))
        .background(
          if (isSelected) DiscordBlurple
          else Color.Transparent
        ),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = if (isSelected) selectedIcon else unselectedIcon,
        contentDescription = label,
        modifier = Modifier.size(20.dp),
        tint = if (isSelected) Color.White else DiscordTextMuted
      )
    }

    Text(
      text = label,
      style = KoraTheme.typography.labelSmall,
      color = if (isSelected) DiscordTextPrimary else DiscordTextMuted,
      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
    )
  }
}
