package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DecorationItem
import com.example.data.model.DecorationType
import com.example.data.model.ItemRarity
import com.example.data.model.UserProfile
import com.example.data.repository.StoreRepository
import com.example.data.repository.UserRepository
import com.example.ui.components.AvatarView
import com.example.ui.components.RoleNameplateView
import com.example.ui.util.AppLanguage
import com.example.ui.util.LocalAppLanguage
import com.example.ui.util.LocalAppStrings
import kotlinx.coroutines.launch

private enum class StoreTab {
  BUY_COINS,
  EARN_COINS,
  CUSTOM_DECORATION
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DecorationStoreScreen(
  storeRepository: StoreRepository,
  userRepository: UserRepository,
  onBack: () -> Unit
) {
  val strings = LocalAppStrings.current
  val currentLang = LocalAppLanguage.current
  val isArabic = currentLang == AppLanguage.AR
  val colors = MaterialTheme.colorScheme

  val coins by storeRepository.coinBalance.collectAsState()
  val equippedFrame by storeRepository.equippedFrame.collectAsState()
  val equippedNameplate by storeRepository.equippedNameplate.collectAsState()

  val currentUid = remember {
    runCatching { userRepository.requireUserId() }.getOrNull()
  }
  val cachedUser = remember(currentUid) {
    currentUid?.let { userRepository.getCachedProfile(it) }
  }
  val currentUser by if (currentUid != null) {
    userRepository.observeUserProfile(currentUid).collectAsState(initial = cachedUser)
  } else {
    remember { mutableStateOf<UserProfile?>(null) }
  }

  var selectedTab by remember { mutableStateOf(StoreTab.CUSTOM_DECORATION) }
  var selectedCategory by remember { mutableStateOf<DecorationType?>(null) }
  var previewItem by remember { mutableStateOf<DecorationItem?>(null) }
  var dailyClaimed by remember { mutableStateOf(false) }

  val coroutineScope = rememberCoroutineScope()
  val snackbarHostState = remember { SnackbarHostState() }

  val filteredCatalog = remember(selectedCategory) {
    if (selectedCategory == null) {
      storeRepository.catalog
    } else {
      storeRepository.catalog.filter { it.type == selectedCategory }
    }
  }

  Scaffold(
    containerColor = colors.background,
    snackbarHost = {
      SnackbarHost(hostState = snackbarHostState) { data ->
        Snackbar(
          snackbarData = data,
          containerColor = colors.surfaceVariant,
          contentColor = colors.onSurface,
          shape = RoundedCornerShape(14.dp)
        )
      }
    },
    topBar = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .background(colors.surface)
          .statusBarsPadding()
      ) {
        // 1. Top Header Row: Back, Title (weighted), Coin Balance Pill
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .padding(horizontal = 8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
          ) {
            IconButton(onClick = onBack) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = strings.cancel,
                tint = colors.onSurface
              )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = strings.storeTitle,
              style = MaterialTheme.typography.titleMedium,
              color = colors.onSurface,
              fontWeight = FontWeight.Bold,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }

          Spacer(modifier = Modifier.width(10.dp))

          // Coin Balance Pill
          Row(
            modifier = Modifier
              .clip(RoundedCornerShape(20.dp))
              .background(colors.surfaceVariant)
              .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.45f), RoundedCornerShape(20.dp))
              .clickable { selectedTab = StoreTab.BUY_COINS }
              .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.MonetizationOn,
              contentDescription = null,
              tint = Color(0xFFF59E0B),
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
              text = "$coins",
              color = colors.onSurface,
              fontWeight = FontWeight.Bold,
              style = MaterialTheme.typography.labelLarge,
              maxLines = 1,
              softWrap = false
            )
            Spacer(modifier = Modifier.width(6.dp))
            Box(
              modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(Color(0xFFF59E0B)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Add,
                contentDescription = strings.buyCoins,
                tint = Color(0xFF0B0F19),
                modifier = Modifier.size(11.dp)
              )
            }
          }
        }

        // 2. Top Tabs: Buy Coins | Earn Coins | Custom Decoration
        TabRow(
          selectedTabIndex = selectedTab.ordinal,
          containerColor = colors.surface,
          contentColor = colors.primary,
          divider = {
            HorizontalDivider(color = colors.outlineVariant.copy(alpha = 0.45f))
          },
          indicator = { tabPositions ->
            TabRowDefaults.SecondaryIndicator(
              modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab.ordinal]),
              color = colors.primary,
              height = 3.dp
            )
          }
        ) {
          Tab(
            selected = selectedTab == StoreTab.BUY_COINS,
            onClick = { selectedTab = StoreTab.BUY_COINS },
            text = {
              Text(
                text = strings.buyCoins,
                fontWeight = if (selectedTab == StoreTab.BUY_COINS) FontWeight.Bold else FontWeight.Medium,
                color = if (selectedTab == StoreTab.BUY_COINS) colors.onSurface else colors.onSurfaceVariant,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
              )
            }
          )

          Tab(
            selected = selectedTab == StoreTab.EARN_COINS,
            onClick = { selectedTab = StoreTab.EARN_COINS },
            text = {
              Text(
                text = strings.earnCoins,
                fontWeight = if (selectedTab == StoreTab.EARN_COINS) FontWeight.Bold else FontWeight.Medium,
                color = if (selectedTab == StoreTab.EARN_COINS) colors.onSurface else colors.onSurfaceVariant,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
              )
            }
          )

          Tab(
            selected = selectedTab == StoreTab.CUSTOM_DECORATION,
            onClick = { selectedTab = StoreTab.CUSTOM_DECORATION },
            text = {
              Text(
                text = strings.customDecoration,
                fontWeight = if (selectedTab == StoreTab.CUSTOM_DECORATION) FontWeight.Bold else FontWeight.Medium,
                color = if (selectedTab == StoreTab.CUSTOM_DECORATION) colors.onSurface else colors.onSurfaceVariant,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
              )
            }
          )
        }
      }
    }
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .navigationBarsPadding()
    ) {
      when (selectedTab) {
        StoreTab.CUSTOM_DECORATION -> {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .horizontalScroll(rememberScrollState())
              .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            StoreFilterChip(
              label = strings.allCategories,
              selected = selectedCategory == null,
              onClick = { selectedCategory = null }
            )
            StoreFilterChip(
              label = strings.avatarFrames,
              selected = selectedCategory == DecorationType.AVATAR_FRAME,
              onClick = { selectedCategory = DecorationType.AVATAR_FRAME }
            )
            StoreFilterChip(
              label = strings.roleNameplates,
              selected = selectedCategory == DecorationType.ROLE_NAMEPLATE,
              onClick = { selectedCategory = DecorationType.ROLE_NAMEPLATE }
            )
          }

          Text(
            text = strings.shopBannerText,
            color = colors.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
          )

          LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
          ) {
            items(filteredCatalog, key = { it.id }) { item ->
              val isOwned = storeRepository.isOwned(item.id)
              val isEquipped = (item.type == DecorationType.AVATAR_FRAME && equippedFrame == item.id) ||
                (item.type == DecorationType.ROLE_NAMEPLATE && equippedNameplate == item.id)

              DecorationItemCard(
                item = item,
                isOwned = isOwned,
                isEquipped = isEquipped,
                isArabic = isArabic,
                onPreviewClick = { previewItem = item }
              )
            }
          }
        }

        StoreTab.BUY_COINS -> {
          BuyCoinsTabContent(
            onBuyPack = { amount ->
              storeRepository.addCoins(amount)
              coroutineScope.launch {
                snackbarHostState.showSnackbar("+$amount ${strings.coinsUnit}")
              }
            }
          )
        }

        StoreTab.EARN_COINS -> {
          EarnCoinsTabContent(
            dailyClaimed = dailyClaimed,
            onClaimDaily = {
              storeRepository.claimDailyReward()
              dailyClaimed = true
              coroutineScope.launch {
                snackbarHostState.showSnackbar(strings.coinsClaimed)
              }
            }
          )
        }
      }
    }

    // Live Preview BottomSheet
    if (previewItem != null) {
      val item = previewItem!!
      val isOwned = storeRepository.isOwned(item.id)
      val isEquipped = (item.type == DecorationType.AVATAR_FRAME && equippedFrame == item.id) ||
        (item.type == DecorationType.ROLE_NAMEPLATE && equippedNameplate == item.id)

      ModalBottomSheet(
        onDismissRequest = { previewItem = null },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = colors.surface,
        contentColor = colors.onSurface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .navigationBarsPadding(),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Box(
            modifier = Modifier
              .size(144.dp)
              .clip(RoundedCornerShape(24.dp))
              .background(
                Brush.radialGradient(
                  colors = listOf(colors.surfaceVariant, colors.background)
                )
              )
              .border(1.dp, colors.outlineVariant.copy(alpha = 0.6f), RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
          ) {
            val userDisplayName = currentUser?.displayName?.takeIf { it.isNotBlank() } ?: strings.appName
            val userAvatarUrl = currentUser?.avatarUrl
            if (item.type == DecorationType.AVATAR_FRAME) {
              AvatarView(
                name = userDisplayName,
                avatarUrl = userAvatarUrl,
                size = 96.dp,
                frameStyleKey = item.styleKey
              )
            } else {
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                AvatarView(
                  name = userDisplayName,
                  avatarUrl = userAvatarUrl,
                  size = 54.dp,
                  frameStyleKey = equippedFrame
                )
                Spacer(modifier = Modifier.height(10.dp))
                RoleNameplateView(
                  styleKey = item.styleKey,
                  title = if (isArabic) item.nameAr else item.nameEn
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Diamond,
              contentDescription = null,
              tint = when (item.rarity) {
                ItemRarity.LEGENDARY -> Color(0xFFF59E0B)
                ItemRarity.EPIC -> Color(0xFFA855F7)
                ItemRarity.RARE -> Color(0xFF38BDF8)
                ItemRarity.COMMON -> colors.onSurfaceVariant
              },
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = if (isArabic) item.nameAr else item.nameEn,
              color = colors.onSurface,
              fontWeight = FontWeight.Bold,
              style = MaterialTheme.typography.titleLarge
            )
          }

          Spacer(modifier = Modifier.height(6.dp))

          Text(
            text = if (isArabic) item.descriptionAr else item.descriptionEn,
            color = colors.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 12.dp)
          )

          Spacer(modifier = Modifier.height(16.dp))

          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .clip(RoundedCornerShape(12.dp))
              .background(colors.surfaceVariant)
              .padding(horizontal = 16.dp, vertical = 8.dp)
          ) {
            Icon(
              imageVector = Icons.Default.MonetizationOn,
              contentDescription = null,
              tint = Color(0xFFF59E0B),
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "${item.price} ${strings.coinsUnit}",
              color = colors.onSurface,
              fontWeight = FontWeight.Bold,
              style = MaterialTheme.typography.titleSmall
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
              text = if (isArabic) item.rarity.labelAr else item.rarity.labelEn,
              color = Color(0xFFF59E0B),
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.SemiBold
            )
          }

          Spacer(modifier = Modifier.height(22.dp))

          when {
            isEquipped -> {
              OutlinedButton(
                onClick = {
                  if (item.type == DecorationType.AVATAR_FRAME) {
                    storeRepository.unequipFrame()
                  } else {
                    storeRepository.unequipNameplate()
                  }
                  previewItem = null
                },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.error),
                border = BorderStroke(1.dp, colors.error.copy(alpha = 0.6f))
              ) {
                Text(strings.unequip, fontWeight = FontWeight.Bold)
              }
            }

            isOwned -> {
              Button(
                onClick = {
                  storeRepository.equipItem(item)
                  previewItem = null
                  coroutineScope.launch {
                    snackbarHostState.showSnackbar(strings.purchasedSuccess)
                  }
                },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(
                  containerColor = colors.primary,
                  contentColor = colors.onPrimary
                )
              ) {
                Text(strings.equip, fontWeight = FontWeight.Bold)
              }
            }

            else -> {
              val canAfford = coins >= item.price
              Button(
                onClick = {
                  if (canAfford) {
                    val ok = storeRepository.buyItem(item)
                    if (ok) {
                      previewItem = null
                      coroutineScope.launch {
                        snackbarHostState.showSnackbar(strings.purchasedSuccess)
                      }
                    }
                  } else {
                    coroutineScope.launch {
                      snackbarHostState.showSnackbar(strings.notEnoughCoins)
                    }
                    selectedTab = StoreTab.EARN_COINS
                    previewItem = null
                  }
                },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(
                  containerColor = if (canAfford) colors.primary else colors.surfaceVariant,
                  contentColor = if (canAfford) colors.onPrimary else colors.onSurfaceVariant
                )
              ) {
                Text(
                  text = "${strings.buyFor} ${item.price}",
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(16.dp))
        }
      }
    }
  }
}

@Composable
private fun StoreFilterChip(
  label: String,
  selected: Boolean,
  onClick: () -> Unit
) {
  val colors = MaterialTheme.colorScheme
  Box(
    modifier = Modifier
      .defaultMinSize(minHeight = 36.dp)
      .clip(RoundedCornerShape(12.dp))
      .background(if (selected) colors.primary else colors.surfaceVariant.copy(alpha = 0.6f))
      .border(
        1.dp,
        if (selected) colors.primary else colors.outlineVariant.copy(alpha = 0.5f),
        RoundedCornerShape(12.dp)
      )
      .clickable(onClick = onClick)
      .padding(horizontal = 14.dp, vertical = 8.dp),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = label,
      color = if (selected) colors.onPrimary else colors.onSurfaceVariant,
      style = MaterialTheme.typography.labelMedium,
      fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
      maxLines = 1,
      softWrap = false
    )
  }
}

@Composable
private fun DecorationItemCard(
  item: DecorationItem,
  isOwned: Boolean,
  isEquipped: Boolean,
  isArabic: Boolean,
  onPreviewClick: () -> Unit
) {
  val strings = LocalAppStrings.current
  val colors = MaterialTheme.colorScheme
  val gemColor = when (item.rarity) {
    ItemRarity.LEGENDARY -> Color(0xFFF59E0B)
    ItemRarity.EPIC -> Color(0xFFA855F7)
    ItemRarity.RARE -> Color(0xFF38BDF8)
    ItemRarity.COMMON -> colors.onSurfaceVariant
  }

  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    border = BorderStroke(
      width = 1.dp,
      color = if (isEquipped) Color(0xFF10B981).copy(alpha = 0.65f) else colors.outlineVariant.copy(alpha = 0.45f)
    ),
    colors = CardDefaults.cardColors(containerColor = colors.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(112.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(
            Brush.verticalGradient(
              listOf(colors.surfaceVariant, colors.background)
            )
          ),
        contentAlignment = Alignment.Center
      ) {
        if (item.type == DecorationType.AVATAR_FRAME) {
          AvatarView(
            name = "K",
            size = 76.dp,
            frameStyleKey = item.styleKey
          )
        } else {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
          ) {
            AvatarView(name = "K", size = 44.dp)
            Spacer(modifier = Modifier.height(6.dp))
            RoleNameplateView(
              styleKey = item.styleKey,
              title = if (isArabic) item.nameAr else item.nameEn
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.MonetizationOn,
          contentDescription = null,
          tint = Color(0xFFF59E0B),
          modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = "${item.price} ${strings.coinsUnit}",
          color = colors.onSurface,
          fontWeight = FontWeight.Bold,
          style = MaterialTheme.typography.labelLarge
        )
      }

      Spacer(modifier = Modifier.height(4.dp))

      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.Diamond,
          contentDescription = null,
          tint = gemColor,
          modifier = Modifier.size(13.dp)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
          text = if (isArabic) item.nameAr else item.nameEn,
          color = colors.onSurface,
          fontWeight = FontWeight.Bold,
          style = MaterialTheme.typography.bodyMedium,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }

      Spacer(modifier = Modifier.height(3.dp))

      Text(
        text = if (isArabic) item.descriptionAr else item.descriptionEn,
        color = colors.onSurfaceVariant,
        style = MaterialTheme.typography.labelSmall,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        minLines = 2
      )

      Spacer(modifier = Modifier.height(10.dp))

      Button(
        onClick = onPreviewClick,
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = if (isEquipped) Color(0xFF059669)
          else if (isOwned) colors.primaryContainer
          else colors.primary,
          contentColor = if (isEquipped) Color.White
          else if (isOwned) colors.onPrimaryContainer
          else colors.onPrimary
        ),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        modifier = Modifier
          .fillMaxWidth()
          .height(38.dp)
      ) {
        if (isEquipped) {
          Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = strings.equipped,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
            softWrap = false
          )
        } else {
          Text(
            text = if (isOwned) strings.equip else strings.preview,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
            softWrap = false
          )
        }
      }
    }
  }
}

@Composable
private fun BuyCoinsTabContent(onBuyPack: (Int) -> Unit) {
  val strings = LocalAppStrings.current
  val colors = MaterialTheme.colorScheme
  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    Text(
      text = strings.buyCoinsDesc,
      color = colors.onSurfaceVariant,
      style = MaterialTheme.typography.bodyMedium
    )

    CoinPackCard(
      title = strings.packStarter,
      coins = 500,
      gradient = listOf(Color(0xFF3B82F6), Color(0xFF1D4ED8)),
      onBuy = { onBuyPack(500) }
    )

    CoinPackCard(
      title = strings.packPopular,
      coins = 2000,
      gradient = listOf(Color(0xFF8B5CF6), Color(0xFF5B21B6)),
      onBuy = { onBuyPack(2000) }
    )

    CoinPackCard(
      title = strings.packElite,
      coins = 5000,
      gradient = listOf(Color(0xFFF59E0B), Color(0xFFB45309)),
      onBuy = { onBuyPack(5000) }
    )

    Spacer(modifier = Modifier.height(16.dp))
  }
}

@Composable
private fun CoinPackCard(
  title: String,
  coins: Int,
  gradient: List<Color>,
  onBuy: () -> Unit
) {
  val strings = LocalAppStrings.current
  val colors = MaterialTheme.colorScheme
  val buttonColor = gradient.first()
  val buttonFg = if (buttonColor.luminance() > 0.52f) Color(0xFF0B0F19) else Color.White

  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.45f)),
    colors = CardDefaults.cardColors(containerColor = colors.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.weight(1f)
      ) {
        Box(
          modifier = Modifier
            .size(46.dp)
            .clip(CircleShape)
            .background(Brush.linearGradient(gradient)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.MonetizationOn,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
          )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = title,
            color = colors.onSurface,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = "$coins ${strings.coinsUnit}",
            color = Color(0xFFF59E0B),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
          )
        }
      }

      Spacer(modifier = Modifier.width(12.dp))

      Button(
        onClick = onBuy,
        colors = ButtonDefaults.buttonColors(
          containerColor = buttonColor,
          contentColor = buttonFg
        ),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
        modifier = Modifier.defaultMinSize(minWidth = 72.dp, minHeight = 42.dp)
      ) {
        Text(
          text = strings.getPack,
          fontWeight = FontWeight.Bold,
          maxLines = 1,
          softWrap = false
        )
      }
    }
  }
}

@Composable
private fun EarnCoinsTabContent(
  dailyClaimed: Boolean,
  onClaimDaily: () -> Unit
) {
  val strings = LocalAppStrings.current
  val colors = MaterialTheme.colorScheme
  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(16.dp),
      border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.45f)),
      colors = CardDefaults.cardColors(containerColor = colors.surface),
      elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = null,
            tint = Color(0xFF10B981),
            modifier = Modifier.size(24.dp)
          )
          Spacer(modifier = Modifier.width(10.dp))
          Text(
            text = strings.dailyCheckIn,
            color = colors.onSurface,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium
          )
        }

        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = strings.dailyCheckInDesc,
          color = colors.onSurfaceVariant,
          style = MaterialTheme.typography.bodySmall
        )

        Spacer(modifier = Modifier.height(14.dp))

        Button(
          onClick = onClaimDaily,
          enabled = !dailyClaimed,
          colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF059669),
            contentColor = Color.White
          ),
          modifier = Modifier.fillMaxWidth().height(46.dp),
          shape = RoundedCornerShape(12.dp)
        ) {
          Text(
            text = if (dailyClaimed) strings.coinsClaimed else strings.claimCoins,
            fontWeight = FontWeight.Bold
          )
        }
      }
    }
  }
}
