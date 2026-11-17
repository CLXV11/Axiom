package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DecorationItem
import com.example.data.model.DecorationType
import com.example.data.model.ItemRarity
import com.example.data.repository.StoreRepository
import com.example.data.repository.UserRepository
import com.example.ui.components.AvatarView
import com.example.ui.components.RoleNameplateView
import com.example.ui.theme.KoraTheme
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

  val coins by storeRepository.coinBalance.collectAsState()
  val equippedFrame by storeRepository.equippedFrame.collectAsState()
  val equippedNameplate by storeRepository.equippedNameplate.collectAsState()

  val currentUid = remember {
    try { userRepository.requireUserId() } catch (e: Exception) { null }
  }
  val currentUser by if (currentUid != null) {
    userRepository.observeUserProfile(currentUid).collectAsState(initial = null)
  } else {
    remember { mutableStateOf<com.example.data.model.UserProfile?>(null) }
  }

  var selectedTab by remember { mutableStateOf(StoreTab.CUSTOM_DECORATION) }
  var selectedCategory by remember { mutableStateOf<DecorationType?>(null) } // null = All
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

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Color(0xFF090D16)) // Dark gaming aesthetic from reference screenshots
      .statusBarsPadding()
      .navigationBarsPadding()
  ) {
    Column(modifier = Modifier.fillMaxSize()) {
      // 1. Top Header Row: Back, Title, Coin Balance pill
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          IconButton(onClick = onBack) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back",
              tint = Color.White
            )
          }
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = strings.storeTitle,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
          )
        }

        // Coin Balance Pill
        Row(
          modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF1E293B))
            .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.5f), RoundedCornerShape(20.dp))
            .clickable { selectedTab = StoreTab.BUY_COINS }
            .padding(horizontal = 10.dp, vertical = 5.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.MonetizationOn,
            contentDescription = null,
            tint = Color(0xFFFBBF24),
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(5.dp))
          Text(
            text = "$coins",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
          )
          Spacer(modifier = Modifier.width(4.dp))
          Box(
            modifier = Modifier
              .size(16.dp)
              .clip(CircleShape)
              .background(Color(0xFFF59E0B)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Add,
              contentDescription = "Add",
              tint = Color.Black,
              modifier = Modifier.size(12.dp)
            )
          }
        }
      }

      // 2. Top Tabs: Buy Coins | Earn Coins | Custom Decoration (NEW)
      TabRow(
        selectedTabIndex = selectedTab.ordinal,
        containerColor = Color.Transparent,
        contentColor = Color.White,
        divider = {
          HorizontalDivider(color = Color(0xFF1E293B))
        },
        indicator = { tabPositions ->
          TabRowDefaults.SecondaryIndicator(
            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab.ordinal]),
            color = Color(0xFFEC4899), // Neon magenta underline from reference
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
              fontWeight = if (selectedTab == StoreTab.BUY_COINS) FontWeight.Bold else FontWeight.Normal,
              color = if (selectedTab == StoreTab.BUY_COINS) Color.White else Color(0xFF94A3B8),
              fontSize = 14.sp
            )
          }
        )

        Tab(
          selected = selectedTab == StoreTab.EARN_COINS,
          onClick = { selectedTab = StoreTab.EARN_COINS },
          text = {
            Text(
              text = strings.earnCoins,
              fontWeight = if (selectedTab == StoreTab.EARN_COINS) FontWeight.Bold else FontWeight.Normal,
              color = if (selectedTab == StoreTab.EARN_COINS) Color.White else Color(0xFF94A3B8),
              fontSize = 14.sp
            )
          }
        )

        Tab(
          selected = selectedTab == StoreTab.CUSTOM_DECORATION,
          onClick = { selectedTab = StoreTab.CUSTOM_DECORATION },
          text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = strings.customDecoration,
                fontWeight = if (selectedTab == StoreTab.CUSTOM_DECORATION) FontWeight.Bold else FontWeight.Normal,
                color = if (selectedTab == StoreTab.CUSTOM_DECORATION) Color.White else Color(0xFF94A3B8),
                fontSize = 14.sp
              )
              Spacer(modifier = Modifier.width(4.dp))
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(4.dp))
                  .background(Color(0xFFEF4444))
                  .padding(horizontal = 4.dp, vertical = 1.dp)
              ) {
                Text(
                  text = "NEW",
                  color = Color.White,
                  fontSize = 9.sp,
                  fontWeight = FontWeight.Black
                )
              }
            }
          }
        )
      }

      // Tab Content View
      when (selectedTab) {
        StoreTab.CUSTOM_DECORATION -> {
          // Subcategory Filter Chips: All, Avatar Frames, Role Nameplates
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 14.dp, vertical = 8.dp),
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

          // Banner Slogan
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 14.dp, vertical = 4.dp)
          ) {
            Text(
              text = strings.shopBannerText,
              color = Color(0xFFE2E8F0),
              fontSize = 13.sp,
              fontWeight = FontWeight.Medium,
              style = androidx.compose.ui.text.TextStyle(
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
              )
            )
          }

          // 2-Column Store Grid
          LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
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
                snackbarHostState.showSnackbar("+$amount ${strings.buyCoins}!")
              }
            }
          )
        }

        StoreTab.EARN_COINS -> {
          EarnCoinsTabContent(
            dailyClaimed = dailyClaimed,
            onClaimDaily = {
              val reward = storeRepository.claimDailyReward()
              dailyClaimed = true
              coroutineScope.launch {
                snackbarHostState.showSnackbar(strings.purchasedSuccess)
              }
            }
          )
        }
      }
    }

    // Live Preview BottomSheet / Modal
    if (previewItem != null) {
      val item = previewItem!!
      val isOwned = storeRepository.isOwned(item.id)
      val isEquipped = (item.type == DecorationType.AVATAR_FRAME && equippedFrame == item.id) ||
        (item.type == DecorationType.ROLE_NAMEPLATE && equippedNameplate == item.id)

      ModalBottomSheet(
        onDismissRequest = { previewItem = null },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color(0xFF0F172A),
        contentColor = Color.White
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          // Preview Stage
          Box(
            modifier = Modifier
              .size(140.dp)
              .clip(RoundedCornerShape(24.dp))
              .background(
                Brush.radialGradient(
                  colors = listOf(Color(0xFF1E293B), Color(0xFF090D16))
                )
              )
              .border(1.dp, Color(0xFF334155), RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
          ) {
            val userDisplayName = currentUser?.displayName ?: "User"
            val userAvatarUrl = currentUser?.avatarUrl
            if (item.type == DecorationType.AVATAR_FRAME) {
              AvatarView(
                name = userDisplayName,
                avatarUrl = userAvatarUrl,
                size = 100.dp,
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

          // Rarity & Title
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Diamond,
              contentDescription = null,
              tint = when (item.rarity) {
                ItemRarity.LEGENDARY -> Color(0xFFF59E0B)
                ItemRarity.EPIC -> Color(0xFFA855F7)
                ItemRarity.RARE -> Color(0xFF38BDF8)
                ItemRarity.COMMON -> Color(0xFF94A3B8)
              },
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = if (isArabic) item.nameAr else item.nameEn,
              color = Color.White,
              fontWeight = FontWeight.Bold,
              fontSize = 20.sp
            )
          }

          Spacer(modifier = Modifier.height(6.dp))

          Text(
            text = if (isArabic) item.descriptionAr else item.descriptionEn,
            color = Color(0xFF94A3B8),
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
          )

          Spacer(modifier = Modifier.height(16.dp))

          // Price Tag
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .clip(RoundedCornerShape(12.dp))
              .background(Color(0xFF1E293B))
              .padding(horizontal = 16.dp, vertical = 8.dp)
          ) {
            Icon(
              imageVector = Icons.Default.MonetizationOn,
              contentDescription = null,
              tint = Color(0xFFFBBF24),
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "${item.price}",
              color = Color.White,
              fontWeight = FontWeight.Bold,
              fontSize = 16.sp
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
              text = if (isArabic) item.rarity.labelAr else item.rarity.labelEn,
              color = Color(0xFFFBBF24),
              fontSize = 12.sp,
              fontWeight = FontWeight.SemiBold
            )
          }

          Spacer(modifier = Modifier.height(24.dp))

          // Action Buttons
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
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444))
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
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5))
              ) {
                Text(strings.equip, fontWeight = FontWeight.Bold, color = Color.White)
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
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(
                  containerColor = if (canAfford) Color(0xFFD97706) else Color(0xFF475569)
                )
              ) {
                Text(
                  text = "${strings.buyFor} ${item.price}",
                  fontWeight = FontWeight.Bold,
                  color = Color.White
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(16.dp))
        }
      }
    }

    SnackbarHost(
      hostState = snackbarHostState,
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .padding(16.dp)
    )
  }
}

@Composable
private fun StoreFilterChip(
  label: String,
  selected: Boolean,
  onClick: () -> Unit
) {
  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(16.dp))
      .background(if (selected) Color(0xFF4338CA) else Color(0xFF1E293B))
      .border(
        1.dp,
        if (selected) Color(0xFF818CF8) else Color(0xFF334155),
        RoundedCornerShape(16.dp)
      )
      .clickable(onClick = onClick)
      .padding(horizontal = 14.dp, vertical = 6.dp)
  ) {
    Text(
      text = label,
      color = if (selected) Color.White else Color(0xFF94A3B8),
      fontSize = 12.sp,
      fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
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
  val gemColor = when (item.rarity) {
    ItemRarity.LEGENDARY -> Color(0xFFF59E0B)
    ItemRarity.EPIC -> Color(0xFFA855F7)
    ItemRarity.RARE -> Color(0xFF38BDF8)
    ItemRarity.COMMON -> Color(0xFF94A3B8)
  }

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(14.dp))
      .border(
        width = 1.dp,
        brush = Brush.verticalGradient(
          listOf(
            Color(0xFFF59E0B).copy(alpha = 0.5f),
            Color(0xFF312E81).copy(alpha = 0.3f),
            Color(0xFF1E293B)
          )
        ),
        shape = RoundedCornerShape(14.dp)
      ),
    colors = CardDefaults.cardColors(containerColor = Color(0xFF121926))
  ) {
    Column(modifier = Modifier.padding(10.dp)) {
      // 1. Showcase Pedestal Preview
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(115.dp)
          .clip(RoundedCornerShape(10.dp))
          .background(
            Brush.verticalGradient(
              listOf(Color(0xFF1E293B), Color(0xFF0B0F19))
            )
          ),
        contentAlignment = Alignment.Center
      ) {
        if (item.type == DecorationType.AVATAR_FRAME) {
          AvatarView(
            name = "K",
            size = 80.dp,
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

      Spacer(modifier = Modifier.height(8.dp))

      // 2. Price Row
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.MonetizationOn,
          contentDescription = null,
          tint = Color(0xFFFBBF24),
          modifier = Modifier.size(15.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = "${item.price}",
          color = Color.White,
          fontWeight = FontWeight.Bold,
          fontSize = 14.sp
        )
      }

      Spacer(modifier = Modifier.height(4.dp))

      // 3. Gem Icon + Name
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
          color = Color.White,
          fontWeight = FontWeight.Bold,
          fontSize = 13.sp,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }

      Spacer(modifier = Modifier.height(3.dp))

      // 4. Description
      Text(
        text = if (isArabic) item.descriptionAr else item.descriptionEn,
        color = Color(0xFF94A3B8),
        fontSize = 11.sp,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        lineHeight = 14.sp
      )

      Spacer(modifier = Modifier.height(10.dp))

      // 5. Ornate Golden Sparkle "Preview" Button (Identical to reference screenshot)
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(34.dp)
          .clip(RoundedCornerShape(6.dp))
          .background(
            if (isEquipped) {
              Brush.horizontalGradient(listOf(Color(0xFF059669), Color(0xFF10B981)))
            } else {
              Brush.horizontalGradient(
                listOf(
                  Color(0xFFD97706),
                  Color(0xFFF59E0B),
                  Color(0xFFFDE68A),
                  Color(0xFFF59E0B),
                  Color(0xFFD97706)
                )
              )
            }
          )
          .clickable(onClick = onPreviewClick),
        contentAlignment = Alignment.Center
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          if (isEquipped) {
            Icon(
              imageVector = Icons.Default.Check,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = strings.equipped,
              color = Color.White,
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp
            )
          } else {
            Text(
              text = "✦  ${strings.preview}  ✦",
              color = Color(0xFF78350F),
              fontWeight = FontWeight.ExtraBold,
              fontSize = 12.sp,
              letterSpacing = 0.5.sp
            )
          }
        }
      }
    }
  }
}

@Composable
private fun BuyCoinsTabContent(onBuyPack: (Int) -> Unit) {
  val strings = LocalAppStrings.current
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    Text(
      text = strings.buyCoinsDesc,
      color = Color(0xFF94A3B8),
      fontSize = 13.sp
    )

    CoinPackCard(
      title = strings.packStarter,
      coins = 500,
      badge = "Starter",
      gradient = listOf(Color(0xFF3B82F6), Color(0xFF1E3A8A)),
      onBuy = { onBuyPack(500) }
    )

    CoinPackCard(
      title = strings.packPopular,
      coins = 2000,
      badge = "Best Value",
      gradient = listOf(Color(0xFF8B5CF6), Color(0xFF4C1D95)),
      onBuy = { onBuyPack(2000) }
    )

    CoinPackCard(
      title = strings.packElite,
      coins = 5000,
      badge = "Elite",
      gradient = listOf(Color(0xFFF59E0B), Color(0xFF78350F)),
      onBuy = { onBuyPack(5000) }
    )
  }
}

@Composable
private fun CoinPackCard(
  title: String,
  coins: Int,
  badge: String,
  gradient: List<Color>,
  onBuy: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(14.dp))
      .border(1.dp, gradient.first().copy(alpha = 0.5f), RoundedCornerShape(14.dp)),
    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
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
            modifier = Modifier.size(26.dp)
          )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column {
          Text(text = title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
          Text(text = "$coins Coins", color = Color(0xFFFBBF24), fontSize = 13.sp)
        }
      }

      Button(
        onClick = onBuy,
        colors = ButtonDefaults.buttonColors(containerColor = gradient.first()),
        shape = RoundedCornerShape(10.dp)
      ) {
        Text("Get", fontWeight = FontWeight.Bold, color = Color.White)
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
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(14.dp))
        .border(1.dp, Color(0xFF10B981).copy(alpha = 0.5f), RoundedCornerShape(14.dp)),
      colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = null,
            tint = Color(0xFF34D399),
            modifier = Modifier.size(24.dp)
          )
          Spacer(modifier = Modifier.width(10.dp))
          Text(
            text = strings.dailyCheckIn,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
          )
        }

        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = strings.dailyCheckInDesc,
          color = Color(0xFF94A3B8),
          fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        Button(
          onClick = onClaimDaily,
          enabled = !dailyClaimed,
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
          modifier = Modifier.fillMaxWidth().height(42.dp),
          shape = RoundedCornerShape(10.dp)
        ) {
          Text(
            text = if (dailyClaimed) strings.coinsClaimed else strings.claimCoins,
            fontWeight = FontWeight.Bold,
            color = Color.White
          )
        }
      }
    }
  }
}
