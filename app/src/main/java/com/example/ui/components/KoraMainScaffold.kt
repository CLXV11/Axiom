package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.DynamicFeed
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.ui.theme.KoraTheme
import com.example.ui.util.LocalAppStrings

/**
 * Main layout skeleton for the Kora professional networking & messaging interface.
 * Adapts automatically between a bottom navigation bar on compact screens and a
 * side NavigationRail on foldables/tablets (>= 600.dp).
 */
@Composable
fun KoraMainScaffold(
  currentTab: NavigationTab,
  onTabSelected: (NavigationTab) -> Unit,
  modifier: Modifier = Modifier,
  title: String? = null,
  subtitle: String? = null,
  onLanguageToggle: (() -> Unit)? = null,
  onBackClick: (() -> Unit)? = null,
  topBarActions: @Composable () -> Unit = {},
  customTopBar: (@Composable () -> Unit)? = null,
  floatingActionButton: @Composable () -> Unit = {},
  snackbarHostState: SnackbarHostState? = null,
  showBottomNav: Boolean = true,
  content: @Composable (PaddingValues) -> Unit
) {
  val strings = LocalAppStrings.current
  val colors = KoraTheme.colors

  BoxWithConstraints(
    modifier = modifier
      .fillMaxSize()
      .background(colors.background)
  ) {
    val useNavigationRail = showBottomNav && maxWidth >= 600.dp

    Row(modifier = Modifier.fillMaxSize()) {
      if (useNavigationRail) {
        NavigationRail(
          containerColor = colors.surfaceContainerLowest,
          contentColor = colors.onSurface,
          modifier = Modifier
            .fillMaxHeight()
            .windowInsetsPadding(WindowInsets.statusBars.union(WindowInsets.navigationBars))
        ) {
          val railItems = listOf(
            Triple(NavigationTab.CHATS, strings.navChats, Icons.Outlined.ChatBubbleOutline),
            Triple(NavigationTab.FLUX, strings.navFlux, Icons.Outlined.PlayCircle),
            Triple(NavigationTab.FEED, strings.navFeed, Icons.Outlined.DynamicFeed),
            Triple(NavigationTab.MEMORY, strings.navMemory, Icons.Outlined.BookmarkBorder),
            Triple(NavigationTab.PROFILE, strings.navProfile, Icons.Outlined.PersonOutline)
          )
          railItems.forEach { (tab, label, icon) ->
            val selected = currentTab == tab || (tab == NavigationTab.PROFILE && currentTab == NavigationTab.SETTINGS)
            NavigationRailItem(
              selected = selected,
              onClick = { onTabSelected(tab) },
              icon = {
                Icon(
                  imageVector = icon,
                  contentDescription = label
                )
              },
              label = {
                Text(
                  text = label,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
              },
              colors = NavigationRailItemDefaults.colors(
                selectedIconColor = colors.primary,
                indicatorColor = colors.primary.copy(alpha = 0.16f),
                selectedTextColor = colors.primary,
                unselectedIconColor = colors.onSurfaceVariant,
                unselectedTextColor = colors.onSurfaceVariant
              )
            )
          }
        }
        VerticalDivider(
          modifier = Modifier
            .fillMaxHeight()
            .width(1.dp),
          color = colors.outlineVariant.copy(alpha = 0.45f)
        )
      }

      Scaffold(
        modifier = Modifier
          .weight(1f)
          .fillMaxHeight(),
        topBar = {
          if (customTopBar != null) {
            customTopBar()
          } else if (title != null) {
            KoraTopBar(
              title = title,
              subtitle = subtitle,
              onBackClick = onBackClick,
              onLanguageToggle = onLanguageToggle,
              actions = topBarActions
            )
          }
        },
        bottomBar = {
          if (showBottomNav && !useNavigationRail) {
            KoraBottomNav(
              currentTab = currentTab,
              onTabSelected = onTabSelected
            )
          }
        },
        floatingActionButton = floatingActionButton,
        floatingActionButtonPosition = FabPosition.End,
        snackbarHost = {
          if (snackbarHostState != null) {
            SnackbarHost(
              hostState = snackbarHostState,
              modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            )
          }
        },
        containerColor = colors.background,
        contentColor = colors.onBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
      ) { paddingValues ->
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
        ) {
          content(paddingValues)
        }
      }
    }
  }
}

