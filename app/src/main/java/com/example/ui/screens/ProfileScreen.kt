package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Visibility
import com.example.ui.components.AvatarPickerSheet
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.example.data.repository.StoreRepository
import com.example.ui.components.RoleNameplateView
import com.example.ui.components.VerificationTier
import com.example.ui.components.VerifiedBadge
import com.example.ui.util.AppLanguage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.UserProfile
import com.example.data.repository.UserRepository
import com.example.ui.components.AvatarView
import com.example.ui.components.KoraTopBar
import com.example.ui.theme.StatusQuiet
import com.example.ui.util.LocalAppLanguage
import com.example.ui.util.LocalAppStrings
import com.example.ui.util.formatHandleForDisplay
import kotlinx.coroutines.launch

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
  userRepository: UserRepository,
  storeRepository: StoreRepository,
  onOpenStore: () -> Unit,
  onLanguageToggle: () -> Unit,
  onOpenSettings: (() -> Unit)? = null,
  onOpenThemeStudio: (() -> Unit)? = null,
  onSignOut: (() -> Unit)? = null
) {
  val context = LocalContext.current
  val strings = LocalAppStrings.current
  val currentLang = LocalAppLanguage.current
  val isArabic = currentLang == AppLanguage.AR
  val coroutineScope = rememberCoroutineScope()
  val uid = userRepository.requireUserId()

  val cachedInitialProfile = remember(uid) { userRepository.getCachedProfile(uid) }
  val profile by userRepository.observeUserProfile(uid).collectAsState(initial = cachedInitialProfile)
  val storeFrame by storeRepository.equippedFrame.collectAsState()
  val storeNameplate by storeRepository.equippedNameplate.collectAsState()
  val equippedFrame = storeFrame ?: profile?.equippedFrame
  val equippedNameplate = storeNameplate ?: profile?.equippedNameplate
  val coins by storeRepository.coinBalance.collectAsState()

  var showEditDialog by remember { mutableStateOf(false) }
  var showAvatarPickerSheet by remember { mutableStateOf(false) }
  var editDisplayName by remember { mutableStateOf("") }
  var editBio by remember { mutableStateOf("") }
  var editProfession by remember { mutableStateOf("") }
  var editOpenToNetworking by remember { mutableStateOf(true) }

  fun openEditDialog() {
    editDisplayName = profile?.displayName ?: ""
    editBio = profile?.bio ?: ""
    editProfession = profile?.profession ?: ""
    editOpenToNetworking = profile?.openToNetworking ?: true
    showEditDialog = true
  }

  fun saveProfileEdits() {
    val updated = (profile ?: UserProfile(id = uid)).copy(
      displayName = editDisplayName.trim(),
      bio = editBio.trim(),
      profession = editProfession.trim(),
      openToNetworking = editOpenToNetworking
    )
    coroutineScope.launch {
      val res = userRepository.createOrUpdateProfile(updated)
      if (res.isSuccess) {
        showEditDialog = false
        Toast.makeText(context, strings.profileUpdated, Toast.LENGTH_SHORT).show()
      } else {
        Toast.makeText(context, strings.errorOccurred, Toast.LENGTH_SHORT).show()
      }
    }
  }

  Scaffold(
    topBar = {
      KoraTopBar(
        title = strings.profileTitle,
        onLanguageToggle = onLanguageToggle,
        actions = {
          if (onOpenSettings != null) {
            IconButton(onClick = onOpenSettings) {
              Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = strings.settingsTitle,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
      )
    }
  ) { paddingValues ->
    val scrollState = rememberScrollState()

    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .background(MaterialTheme.colorScheme.background),
      contentAlignment = Alignment.TopCenter
    ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .widthIn(max = 760.dp)
        .verticalScroll(scrollState)
        .padding(16.dp)
    ) {
      val colors = MaterialTheme.colorScheme
      val cardShape = RoundedCornerShape(16.dp)
      val cardBorder = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.45f))

      // Profile Info Card
      Card(
        shape = cardShape,
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        border = cardBorder,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Box(
            modifier = Modifier
              .clickable { showAvatarPickerSheet = true }
              .testTag("profile_avatar_clickable_container"),
            contentAlignment = Alignment.Center
          ) {
            AvatarView(
              name = profile?.displayName ?: strings.appName,
              avatarUrl = profile?.avatarUrl,
              size = 90.dp,
              frameStyleKey = equippedFrame,
              showPresence = true,
              isOnline = true,
              isQuiet = profile?.quietMode == true
            )

            Box(
              modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(30.dp)
                .clip(CircleShape)
                .background(colors.primary)
                .border(2.dp, colors.surface, CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = strings.changeAvatarTitle,
                tint = colors.onPrimary,
                modifier = Modifier.size(16.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(6.dp))

          TextButton(
            onClick = { showAvatarPickerSheet = true },
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            modifier = Modifier.testTag("change_avatar_text_button")
          ) {
            Icon(
              imageVector = Icons.Default.PhotoCamera,
              contentDescription = null,
              modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = strings.changeAvatarTitle,
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.SemiBold
            )
          }

          Spacer(modifier = Modifier.height(6.dp))

          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
          ) {
            Text(
              text = profile?.displayName?.takeIf { it.isNotBlank() } ?: strings.loading,
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
              color = colors.onSurface,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            if (profile?.isVerified == true) {
              Spacer(modifier = Modifier.width(6.dp))
              VerifiedBadge(size = 20.dp, tierKey = profile?.verifiedTier, showDialogOnClick = true)
            }
          }

          if (equippedNameplate != null) {
            Spacer(modifier = Modifier.height(8.dp))
            RoleNameplateView(
              styleKey = equippedNameplate!!,
              title = profile?.profession?.takeIf { it.isNotBlank() }
                ?: if (isArabic) "عضو كورا المميز" else "Kora Member"
            )
          }

          Spacer(modifier = Modifier.height(6.dp))

          Text(
            text = formatHandleForDisplay(profile?.handle),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.primary,
            fontWeight = FontWeight.SemiBold
          )

          if (!profile?.profession.isNullOrBlank() && equippedNameplate == null) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = profile!!.profession,
              style = MaterialTheme.typography.labelLarge,
              color = colors.secondary,
              fontWeight = FontWeight.SemiBold
            )
          }

          if (!profile?.bio.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = profile!!.bio,
              style = MaterialTheme.typography.bodyMedium,
              color = colors.onSurfaceVariant
            )
          }

          Spacer(modifier = Modifier.height(16.dp))

          OutlinedButton(
            onClick = { openEditDialog() },
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, colors.outlineVariant),
            modifier = Modifier
              .fillMaxWidth()
              .defaultMinSize(minHeight = 48.dp)
              .testTag("edit_profile_button")
          ) {
            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(strings.editProfile, fontWeight = FontWeight.SemiBold)
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Custom Decoration Store Card
      Card(
        shape = cardShape,
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        border = cardBorder,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
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
                .size(44.dp)
                .clip(CircleShape)
                .background(
                  Brush.linearGradient(
                    listOf(Color(0xFF6366F1), Color(0xFF22D3EE))
                  )
                ),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Diamond,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(22.dp)
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = strings.customDecoration,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.MonetizationOn,
                  contentDescription = null,
                  tint = Color(0xFFF59E0B),
                  modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "$coins ${strings.coinsUnit}",
                  style = MaterialTheme.typography.labelSmall,
                  color = colors.onSurfaceVariant,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
              }
            }
          }

          Spacer(modifier = Modifier.width(12.dp))

          Button(
            onClick = onOpenStore,
            colors = ButtonDefaults.buttonColors(
              containerColor = colors.primary,
              contentColor = colors.onPrimary
            ),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
            modifier = Modifier.defaultMinSize(minHeight = 44.dp)
          ) {
            Text(
              text = strings.preview,
              fontWeight = FontWeight.Bold,
              maxLines = 1,
              softWrap = false
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Theme Studio Card
      if (onOpenThemeStudio != null) {
        Card(
          shape = cardShape,
          colors = CardDefaults.cardColors(containerColor = colors.surface),
          border = cardBorder,
          elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
          modifier = Modifier.fillMaxWidth()
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
                  .size(44.dp)
                  .clip(CircleShape)
                  .background(
                    Brush.linearGradient(
                      listOf(colors.primary, Color(0xFFEC4899))
                    )
                  ),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Palette,
                  contentDescription = null,
                  tint = Color.White,
                  modifier = Modifier.size(22.dp)
                )
              }
              Spacer(modifier = Modifier.width(12.dp))
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = strings.themeSwitcherTitle,
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = colors.onSurface,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
                Text(
                  text = strings.themePresets,
                  style = MaterialTheme.typography.labelSmall,
                  color = colors.primary,
                  fontWeight = FontWeight.SemiBold,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
              }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Button(
              onClick = onOpenThemeStudio,
              colors = ButtonDefaults.buttonColors(
                containerColor = colors.primary,
                contentColor = colors.onPrimary
              ),
              shape = RoundedCornerShape(12.dp),
              contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
              modifier = Modifier.defaultMinSize(minHeight = 44.dp)
            ) {
              Text(
                text = strings.quickThemeSwitcher,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                softWrap = false
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))
      }

      // Verification Badge Management Card
      Card(
        shape = cardShape,
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        border = cardBorder,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth().testTag("verification_badge_card")
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          val activeTier = remember(profile?.verifiedTier) {
            VerificationTier.fromId(profile?.verifiedTier)
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.weight(1f)
            ) {
              VerifiedBadge(size = 36.dp, tierKey = profile?.verifiedTier, showDialogOnClick = true)
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(
                    text = strings.verificationBadge,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                  )
                  if (profile?.isVerified == true) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                      text = "• ${if (isArabic) activeTier.titleAr else activeTier.titleEn}",
                      style = MaterialTheme.typography.labelMedium,
                      color = activeTier.accentColor,
                      fontWeight = FontWeight.Bold
                    )
                  }
                }
                Text(
                  text = if (profile?.isVerified == true)
                    (if (isArabic) activeTier.subtitleAr else activeTier.subtitleEn)
                  else strings.verifiedBadgeSubtitle,
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }

            Switch(
              checked = profile?.isVerified == true,
              onCheckedChange = { desired ->
                coroutineScope.launch {
                  val res = userRepository.setVerificationStatus(desired)
                  if (res.isSuccess) {
                    val newStatus = res.getOrDefault(desired)
                    val msg = if (newStatus) strings.verificationEnableSuccess else strings.verificationDisableSuccess
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                  } else {
                    Toast.makeText(
                      context,
                      res.exceptionOrNull()?.localizedMessage ?: strings.verificationError,
                      Toast.LENGTH_SHORT
                    ).show()
                  }
                }
              },
              modifier = Modifier.testTag("verification_toggle_switch")
            )
          }

          Spacer(modifier = Modifier.height(12.dp))
          HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
          Spacer(modifier = Modifier.height(10.dp))

          Text(
            text = strings.verificationDetails,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Spacer(modifier = Modifier.height(10.dp))

          Row(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(
                if (profile?.isVerified == true) {
                  activeTier.accentColor.copy(alpha = 0.12f)
                } else {
                  MaterialTheme.colorScheme.surfaceVariant
                }
              )
              .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = if (profile?.isVerified == true) Icons.Default.CheckCircle else Icons.Default.Bedtime,
              contentDescription = null,
              tint = if (profile?.isVerified == true) activeTier.accentColor else MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = if (profile?.isVerified == true)
                "${strings.verifiedStatusActive} (${if (isArabic) activeTier.titleAr else activeTier.titleEn})"
              else strings.verifiedStatusInactive,
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Medium,
              color = if (profile?.isVerified == true) activeTier.accentColor else MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          // Luxury Tiers Selection Gallery
          if (profile?.isVerified == true) {
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))

            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
              modifier = Modifier.fillMaxWidth()
            ) {
              Text(
                text = if (isArabic) "فئات وشارات التوثيق الفاخرة" else "Luxury Verification Tiers & Gradients",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = "${VerificationTier.values().size} ${if (isArabic) "فئات" else "Tiers"}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
              )
            }

            Spacer(modifier = Modifier.height(10.dp))

            val currentTierId = profile?.verifiedTier ?: "GOLD"
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
              VerificationTier.values().forEach { tier ->
                val isSelected = currentTierId.equals(tier.id, ignoreCase = true)
                Card(
                  onClick = {
                    coroutineScope.launch {
                      val res = userRepository.setVerificationTier(tier.id)
                      if (res.isSuccess) {
                        val name = if (isArabic) tier.titleAr else tier.titleEn
                        Toast.makeText(context, "${if (isArabic) "تم تفعيل" else "Equipped"}: $name", Toast.LENGTH_SHORT).show()
                      } else {
                        Toast.makeText(
                          context,
                          res.exceptionOrNull()?.localizedMessage ?: strings.verificationError,
                          Toast.LENGTH_SHORT
                        ).show()
                      }
                    }
                  },
                  shape = RoundedCornerShape(12.dp),
                  colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) tier.accentColor.copy(alpha = 0.12f)
                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                  ),
                  border = if (isSelected)
                    androidx.compose.foundation.BorderStroke(1.5.dp, tier.accentColor)
                  else
                    androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                  modifier = Modifier.fillMaxWidth().testTag("tier_card_${tier.id.lowercase()}")
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                  ) {
                    Row(
                      verticalAlignment = Alignment.CenterVertically,
                      modifier = Modifier.weight(1f)
                    ) {
                      VerifiedBadge(
                        size = 28.dp,
                        tier = tier,
                        showDialogOnClick = false
                      )
                      Spacer(modifier = Modifier.width(12.dp))
                      Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                          Text(
                            text = if (isArabic) tier.titleAr else tier.titleEn,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                          )
                          Spacer(modifier = Modifier.width(6.dp))
                          Box(
                            modifier = Modifier
                              .size(width = 24.dp, height = 7.dp)
                              .clip(RoundedCornerShape(4.dp))
                              .background(Brush.linearGradient(tier.gradientColors))
                          )
                        }
                        Text(
                          text = if (isArabic) tier.subtitleAr else tier.subtitleEn,
                          style = MaterialTheme.typography.labelSmall,
                          color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                      }
                    }

                    if (isSelected) {
                      val chipFg = if (tier.accentColor.luminance() > 0.52f) Color(0xFF0B0F19) else Color.White
                      Box(
                        modifier = Modifier
                          .clip(RoundedCornerShape(8.dp))
                          .background(tier.accentColor)
                          .padding(horizontal = 8.dp, vertical = 4.dp)
                      ) {
                        Text(
                          text = if (isArabic) "مفعّل" else "Active",
                          color = chipFg,
                          style = MaterialTheme.typography.labelSmall,
                          fontWeight = FontWeight.Bold,
                          maxLines = 1,
                          softWrap = false
                        )
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Quiet Presence Settings Card
      Card(
        shape = cardShape,
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        border = cardBorder,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = strings.quietPresence,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = colors.onSurface
          )
          Text(
            text = strings.quietModeDesc,
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurfaceVariant,
            modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
          )

          HorizontalDivider(color = colors.outlineVariant.copy(alpha = 0.45f))

          // Quiet Mode Toggle
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.weight(1f)
            ) {
              Icon(
                imageVector = Icons.Default.Bedtime,
                contentDescription = null,
                tint = if (profile?.quietMode == true) StatusQuiet else colors.onSurfaceVariant
              )
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Text(
                  text = strings.quietMode,
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.SemiBold
                )
                Text(
                  text = if (profile?.quietMode == true) strings.quietFocusActive else strings.standardAlerts,
                  style = MaterialTheme.typography.labelSmall,
                  color = colors.onSurfaceVariant
                )
              }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Switch(
              checked = profile?.quietMode == true,
              onCheckedChange = { checked ->
                coroutineScope.launch { userRepository.setQuietMode(checked) }
              },
              modifier = Modifier.testTag("quiet_mode_switch")
            )
          }

          HorizontalDivider(color = colors.outlineVariant.copy(alpha = 0.35f))

          // Read Receipts Toggle
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.weight(1f)
            ) {
              Icon(
                imageVector = Icons.Default.Visibility,
                contentDescription = null,
                tint = colors.primary
              )
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Text(
                  text = strings.readReceipts,
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.SemiBold
                )
                Text(
                  text = strings.readReceiptsDesc,
                  style = MaterialTheme.typography.labelSmall,
                  color = colors.onSurfaceVariant
                )
              }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Switch(
              checked = profile?.readReceiptsEnabled != false,
              onCheckedChange = { checked ->
                coroutineScope.launch {
                  val updated = (profile ?: UserProfile(id = uid)).copy(readReceiptsEnabled = checked)
                  userRepository.createOrUpdateProfile(updated)
                }
              }
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      if (onSignOut != null) {
        OutlinedButton(
          onClick = onSignOut,
          shape = RoundedCornerShape(14.dp),
          border = BorderStroke(1.dp, colors.error.copy(alpha = 0.45f)),
          colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.error),
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag("sign_out_button")
        ) {
          Icon(Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text(strings.signOut, fontWeight = FontWeight.SemiBold)
        }
      } else {
        Button(
          onClick = onOpenStore,
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag("open_store_primary_button")
        ) {
          Icon(Icons.Default.Storefront, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text(if (LocalAppLanguage.current.code == "ar") "استكشاف متجر الإطارات والشارات" else "Explore Custom Frames & Decorations", fontWeight = FontWeight.SemiBold)
        }
      }

      Spacer(modifier = Modifier.height(24.dp))
    }
    }
  }

  // Edit Profile Dialog
  if (showEditDialog) {
    AlertDialog(
      onDismissRequest = { showEditDialog = false },
      title = { Text(if (isArabic) "تعديل الهوية والملف" else "Edit Identity") },
      text = {
        Column {
          OutlinedTextField(
            value = editDisplayName,
            onValueChange = { editDisplayName = it },
            label = { Text(strings.enterDisplayName) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("edit_display_name_input")
          )
          Spacer(modifier = Modifier.height(12.dp))
          OutlinedTextField(
            value = editProfession,
            onValueChange = { editProfession = it },
            label = { Text(strings.enterProfession) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("edit_profession_input")
          )
          Spacer(modifier = Modifier.height(12.dp))
          OutlinedTextField(
            value = editBio,
            onValueChange = { editBio = it },
            label = { Text(strings.enterBio) },
            maxLines = 3,
            modifier = Modifier.fillMaxWidth().testTag("edit_bio_input")
          )
          Spacer(modifier = Modifier.height(12.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = strings.openToNetworkingToggle,
              style = MaterialTheme.typography.bodyMedium,
              modifier = Modifier.weight(1f)
            )
            Switch(
              checked = editOpenToNetworking,
              onCheckedChange = { editOpenToNetworking = it }
            )
          }
        }
      },
      confirmButton = {
        Button(
          onClick = { saveProfileEdits() },
          modifier = Modifier.testTag("save_edit_profile_button")
        ) {
          Text(strings.confirm)
        }
      },
      dismissButton = {
        TextButton(onClick = { showEditDialog = false }) {
          Text(strings.cancel)
        }
      }
    )
  }

  // Change Profile Picture Sheet
  if (showAvatarPickerSheet) {
    AvatarPickerSheet(
      currentAvatarUrl = profile?.avatarUrl,
      displayName = profile?.displayName ?: "User",
      equippedFrame = equippedFrame,
      onDismiss = { showAvatarPickerSheet = false },
      onSaveAvatar = { newUrl ->
        coroutineScope.launch {
          val res = userRepository.updateAvatarUrl(newUrl)
          if (res.isSuccess) {
            showAvatarPickerSheet = false
            Toast.makeText(context, strings.avatarUpdatedSuccess, Toast.LENGTH_SHORT).show()
          } else {
            Toast.makeText(context, strings.errorOccurred, Toast.LENGTH_SHORT).show()
          }
        }
      }
    )
  }
}
