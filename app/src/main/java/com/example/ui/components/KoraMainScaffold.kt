package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.ui.theme.KoraTheme

/**
 * Main layout skeleton for the Kora professional networking & messaging interface.
 * Built with Scaffold and centralized styling from KoraTheme.
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
  Scaffold(
    modifier = modifier
      .fillMaxSize()
      .background(KoraTheme.colors.background),
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
      if (showBottomNav) {
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
    containerColor = KoraTheme.colors.background,
    contentColor = KoraTheme.colors.onBackground
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
