package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.CleaningServices
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.SpeakerPhone
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material.icons.outlined.ViewAgenda
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VolumeMute
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfile
import com.example.data.repository.UserRepository
import com.example.ui.components.AvatarPickerSheet
import com.example.ui.components.AvatarView
import com.example.ui.components.KoraTopBar
import com.example.ui.components.ThemePreviewCard
import com.example.ui.components.ThemeSwitcherSheet
import com.example.ui.theme.AccentColorChoice
import com.example.ui.theme.AppThemePreset
import com.example.ui.theme.KoraTheme
import com.example.ui.util.AppLanguage
import com.example.ui.util.LocalAppLanguage
import com.example.ui.util.LocalAppStrings
import com.example.ui.util.PreferencesManager
import com.example.ui.util.formatHandleForDisplay
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
  currentThemeMode: String = "dark",
  currentPreset: AppThemePreset = AppThemePreset.DISCORD_DARK,
  currentAmoled: Boolean = false,
  currentAccent: AccentColorChoice = AccentColorChoice.DEFAULT,
  onThemeChanged: (AppThemePreset, Boolean, AccentColorChoice) -> Unit = { _, _, _ -> },
  onThemeModeChanged: (String) -> Unit = {},
  onLanguageChanged: (AppLanguage) -> Unit,
  userRepository: UserRepository? = null,
  compactMode: Boolean = false,
  onCompactModeChanged: (Boolean) -> Unit = {},
  inAppBannersEnabled: Boolean = true,
  onInAppBannersChanged: (Boolean) -> Unit = {},
  notificationSoundEnabled: Boolean = true,
  onNotificationSoundChanged: (Boolean) -> Unit = {},
  fluxStartMuted: Boolean = false,
  onFluxStartMutedChanged: (Boolean) -> Unit = {},
  fluxAutoDownload: Boolean = true,
  onFluxAutoDownloadChanged: (Boolean) -> Unit = {},
  callSpeakerDefault: Boolean = false,
  onCallSpeakerDefaultChanged: (Boolean) -> Unit = {},
  onBackClick: (() -> Unit)? = null,
  onSignOut: (() -> Unit)? = null
) {
  val context = LocalContext.current
  val strings = LocalAppStrings.current
  val colors = MaterialTheme.colorScheme
  val currentLanguage = LocalAppLanguage.current
  val isArabic = currentLanguage == AppLanguage.AR
  val coroutineScope = rememberCoroutineScope()
  val prefs = remember(context) { PreferencesManager(context) }

  val uid = remember(userRepository) { userRepository?.requireUserId().orEmpty() }
  val cachedProfile = remember(uid, userRepository) {
    if (uid.isNotBlank()) userRepository?.getCachedProfile(uid) else null
  }
  val profile by (if (userRepository != null && uid.isNotBlank()) {
    userRepository.observeUserProfile(uid).collectAsState(initial = cachedProfile)
  } else {
    remember { mutableStateOf<UserProfile?>(null) }
  })
  val signedInEmail = remember {
    runCatching { Firebase.auth.currentUser?.email }.getOrNull()
  }

  var showThemeSheet by remember { mutableStateOf(false) }
  var showEditProfileDialog by remember { mutableStateOf(false) }
  var showAvatarPicker by remember { mutableStateOf(false) }
  var cacheBytes by remember { mutableLongStateOf(prefs.calculateMediaCacheBytes(context)) }

  var editDisplayName by remember { mutableStateOf("") }
  var editHandle by remember { mutableStateOf("") }
  var editProfession by remember { mutableStateOf("") }
  var editBio by remember { mutableStateOf("") }
  var editOpenToNetworking by remember { mutableStateOf(true) }

  fun openEditProfile() {
    editDisplayName = profile?.displayName ?: ""
    editHandle = profile?.handle ?: ""
    editProfession = profile?.profession ?: ""
    editBio = profile?.bio ?: ""
    editOpenToNetworking = profile?.openToNetworking ?: true
    showEditProfileDialog = true
  }

  Scaffold(
    containerColor = colors.background,
    contentWindowInsets = WindowInsets(0, 0, 0, 0),
    topBar = {
      KoraTopBar(
        title = strings.settingsTitle,
        onBackClick = onBackClick
      )
    }
  ) { paddingValues ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .background(colors.background),
      contentAlignment = Alignment.TopCenter
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .widthIn(max = 760.dp)
          .verticalScroll(rememberScrollState())
          .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        // 1. Account & Identity Section (Edit everything about your profile)
        if (userRepository != null) {
          SettingsSectionHeader(title = strings.settingsAccountSection)
          SettingsSectionCard {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              AvatarView(
                name = profile?.displayName ?: strings.appName,
                avatarUrl = profile?.avatarUrl,
                size = 50.dp,
                frameStyleKey = profile?.equippedFrame
              )
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = profile?.displayName?.takeIf { it.isNotBlank() } ?: strings.loading,
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = colors.onSurface,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
                Text(
                  text = formatHandleForDisplay(profile?.handle),
                  style = MaterialTheme.typography.labelMedium,
                  color = colors.onSurfaceVariant
                )
                if (!signedInEmail.isNullOrBlank()) {
                  Text(
                    text = signedInEmail,
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                }
              }
            }

            HorizontalDivider(color = colors.outlineVariant.copy(alpha = 0.45f))

            SettingsActionRow(
              icon = Icons.Outlined.Edit,
              title = strings.settingsEditIdentity,
              subtitle = strings.settingsEditIdentityDesc,
              onClick = { openEditProfile() },
              testTag = "settings_edit_identity_row"
            )

            HorizontalDivider(color = colors.outlineVariant.copy(alpha = 0.35f))

            SettingsActionRow(
              icon = Icons.Outlined.PhotoCamera,
              title = strings.changeAvatarTitle,
              subtitle = strings.changeAvatarSubtitle,
              onClick = { showAvatarPicker = true }
            )

            HorizontalDivider(color = colors.outlineVariant.copy(alpha = 0.35f))

            SettingsSwitchRow(
              icon = Icons.Outlined.Verified,
              iconTint = if (profile?.isVerified == true) colors.primary else colors.onSurfaceVariant,
              title = strings.verificationBadge,
              subtitle = if (profile?.isVerified == true) strings.verifiedStatusActive else strings.verifiedBadgeSubtitle,
              checked = profile?.isVerified == true,
              onCheckedChange = { checked ->
                coroutineScope.launch {
                  val res = userRepository.setVerificationStatus(checked)
                  if (res.isSuccess) {
                    Toast.makeText(
                      context,
                      if (res.getOrDefault(checked)) strings.verificationEnableSuccess else strings.verificationDisableSuccess,
                      Toast.LENGTH_SHORT
                    ).show()
                  } else {
                    Toast.makeText(
                      context,
                      res.exceptionOrNull()?.localizedMessage ?: strings.verificationError,
                      Toast.LENGTH_SHORT
                    ).show()
                  }
                }
              }
            )
          }

          // 2. Privacy & Presence Section
          SettingsSectionHeader(title = strings.settingsPrivacySection)
          SettingsSectionCard {
            SettingsSwitchRow(
              icon = Icons.Outlined.Bedtime,
              title = strings.quietMode,
              subtitle = strings.quietModeDesc,
              checked = profile?.quietMode == true,
              onCheckedChange = { checked ->
                coroutineScope.launch {
                  val res = userRepository.setQuietMode(checked)
                  if (res.isFailure) {
                    Toast.makeText(context, res.exceptionOrNull()?.localizedMessage ?: strings.errorOccurred, Toast.LENGTH_SHORT).show()
                  }
                }
              }
            )

            HorizontalDivider(color = colors.outlineVariant.copy(alpha = 0.35f))

            SettingsSwitchRow(
              icon = Icons.Outlined.Visibility,
              title = strings.readReceipts,
              subtitle = strings.readReceiptsDesc,
              checked = profile?.readReceiptsEnabled != false,
              onCheckedChange = { checked ->
                coroutineScope.launch {
                  val updated = (profile ?: UserProfile(id = uid)).copy(readReceiptsEnabled = checked)
                  val res = userRepository.createOrUpdateProfile(updated)
                  if (res.isFailure) {
                    Toast.makeText(context, res.exceptionOrNull()?.localizedMessage ?: strings.errorOccurred, Toast.LENGTH_SHORT).show()
                  }
                }
              }
            )

            HorizontalDivider(color = colors.outlineVariant.copy(alpha = 0.35f))

            SettingsSwitchRow(
              icon = Icons.Outlined.Public,
              title = strings.openToNetworkingToggle,
              subtitle = strings.contactDirectorySubtitle,
              checked = profile?.openToNetworking != false,
              onCheckedChange = { checked ->
                coroutineScope.launch {
                  val updated = (profile ?: UserProfile(id = uid)).copy(openToNetworking = checked)
                  val res = userRepository.createOrUpdateProfile(updated)
                  if (res.isFailure) {
                    Toast.makeText(context, res.exceptionOrNull()?.localizedMessage ?: strings.errorOccurred, Toast.LENGTH_SHORT).show()
                  }
                }
              }
            )
          }
        }

        // 3. Appearance & Theme Studio
        SettingsSectionHeader(title = strings.appearance)
        SettingsSectionCard {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            Box(
              modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(colors.primaryContainer),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Outlined.Palette,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(20.dp)
              )
            }
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = strings.themeSwitcherTitle,
                style = MaterialTheme.typography.titleSmall.copy(fontSize = 15.sp),
                fontWeight = FontWeight.SemiBold,
                color = colors.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
              Text(
                text = if (isArabic) currentPreset.nameAr else currentPreset.nameEn,
                style = MaterialTheme.typography.labelSmall,
                color = colors.primary,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }
            Button(
              onClick = { showThemeSheet = true },
              colors = ButtonDefaults.buttonColors(
                containerColor = colors.primary,
                contentColor = colors.onPrimary
              ),
              shape = RoundedCornerShape(10.dp),
              contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
            ) {
              Text(
                text = strings.quickThemeSwitcher,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                softWrap = false
              )
            }
          }

          Spacer(modifier = Modifier.height(12.dp))
          ThemePreviewCard(
            preset = currentPreset,
            amoledPureBlack = currentAmoled,
            accentChoice = currentAccent
          )

          Spacer(modifier = Modifier.height(14.dp))
          Text(
            text = strings.themePresets,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = colors.onSurface
          )
          Spacer(modifier = Modifier.height(8.dp))

          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            AppThemePreset.entries.forEach { preset ->
              val isSelected = currentPreset == preset
              Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                  containerColor = if (isSelected) colors.primary.copy(alpha = 0.14f)
                  else colors.background
                ),
                border = BorderStroke(
                  width = if (isSelected) 1.5.dp else 1.dp,
                  color = if (isSelected) colors.primary else colors.outlineVariant.copy(alpha = 0.5f)
                ),
                modifier = Modifier
                  .fillMaxWidth()
                  .clickable {
                    onThemeChanged(preset, currentAmoled, currentAccent)
                    onThemeModeChanged(if (!preset.isDark) "light" else if (preset == AppThemePreset.SYSTEM) "system" else "dark")
                  }
                  .testTag("settings_theme_${preset.id}")
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Box(
                    modifier = Modifier
                      .size(32.dp)
                      .clip(CircleShape)
                      .background(preset.backgroundColor)
                      .border(1.5.dp, colors.outline.copy(alpha = 0.45f), CircleShape),
                    contentAlignment = Alignment.Center
                  ) {
                    Box(
                      modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(preset.primaryColor)
                    )
                  }
                  Spacer(modifier = Modifier.width(12.dp))
                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = if (isArabic) preset.nameAr else preset.nameEn,
                      style = MaterialTheme.typography.bodyMedium,
                      fontWeight = FontWeight.SemiBold,
                      color = if (isSelected) colors.primary else colors.onSurface
                    )
                    Text(
                      text = if (isArabic) preset.subtitleAr else preset.subtitleEn,
                      style = KoraTheme.typography.labelSmall,
                      color = colors.onSurfaceVariant
                    )
                  }
                  if (isSelected) {
                    Box(
                      modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(colors.primary),
                      contentAlignment = Alignment.Center
                    ) {
                      Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Active",
                        tint = colors.onPrimary,
                        modifier = Modifier.size(14.dp)
                      )
                    }
                  }
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(14.dp))
          Text(
            text = strings.customAccentColor,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = colors.onSurface
          )
          Spacer(modifier = Modifier.height(8.dp))

          LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 2.dp)
          ) {
            items(AccentColorChoice.entries) { choice ->
              val isSelected = currentAccent == choice
              val chipColor = if (choice == AccentColorChoice.DEFAULT) currentPreset.primaryColor else choice.color
              val checkTint = if (chipColor.luminance() > 0.52f) Color(0xFF1E1F22) else Color.White

              Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                  .clickable { onThemeChanged(currentPreset, currentAmoled, choice) }
                  .padding(4.dp)
              ) {
                Box(
                  modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(chipColor)
                    .border(
                      width = if (isSelected) 2.dp else 1.dp,
                      color = if (isSelected) colors.onSurface else colors.outline.copy(alpha = 0.5f),
                      shape = CircleShape
                    ),
                  contentAlignment = Alignment.Center
                ) {
                  if (isSelected) {
                    Icon(
                      imageVector = Icons.Default.Check,
                      contentDescription = "Selected",
                      tint = checkTint,
                      modifier = Modifier.size(16.dp)
                    )
                  }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = if (isArabic) choice.nameAr else choice.nameEn,
                  style = KoraTheme.typography.labelSmall.copy(fontSize = 10.sp),
                  color = if (isSelected) colors.onSurface else colors.onSurfaceVariant,
                  maxLines = 1,
                  softWrap = false
                )
              }
            }
          }

          if (currentPreset.isDark) {
            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = colors.outlineVariant.copy(alpha = 0.35f))
            SettingsSwitchRow(
              icon = Icons.Outlined.DarkMode,
              title = strings.amoledMode,
              subtitle = strings.amoledModeDesc,
              checked = currentAmoled,
              onCheckedChange = { checked ->
                onThemeChanged(currentPreset, checked, currentAccent)
              }
            )
          }

          HorizontalDivider(color = colors.outlineVariant.copy(alpha = 0.35f))
          SettingsSwitchRow(
            icon = Icons.Outlined.ViewAgenda,
            title = strings.settingsCompactLayout,
            subtitle = strings.settingsCompactLayoutDesc,
            checked = compactMode,
            onCheckedChange = onCompactModeChanged
          )
        }

        // 4. Notifications & Calls Section
        SettingsSectionHeader(title = strings.settingsNotificationsSection)
        SettingsSectionCard {
          SettingsSwitchRow(
            icon = Icons.Outlined.NotificationsActive,
            title = strings.settingsInAppBanners,
            subtitle = strings.settingsInAppBannersDesc,
            checked = inAppBannersEnabled,
            onCheckedChange = onInAppBannersChanged
          )
          HorizontalDivider(color = colors.outlineVariant.copy(alpha = 0.35f))
          SettingsSwitchRow(
            icon = Icons.Outlined.VolumeUp,
            title = strings.settingsSoundAlerts,
            subtitle = strings.settingsSoundAlertsDesc,
            checked = notificationSoundEnabled,
            onCheckedChange = onNotificationSoundChanged
          )
          HorizontalDivider(color = colors.outlineVariant.copy(alpha = 0.35f))
          SettingsSwitchRow(
            icon = Icons.Outlined.SpeakerPhone,
            title = strings.settingsCallSpeakerDefault,
            subtitle = strings.settingsCallSpeakerDefaultDesc,
            checked = callSpeakerDefault,
            onCheckedChange = onCallSpeakerDefaultChanged
          )
        }

        // 5. Flux & Media Storage Section
        SettingsSectionHeader(title = strings.settingsMediaFluxSection)
        SettingsSectionCard {
          SettingsSwitchRow(
            icon = Icons.Outlined.VolumeMute,
            title = strings.settingsFluxStartMuted,
            subtitle = strings.settingsFluxStartMutedDesc,
            checked = fluxStartMuted,
            onCheckedChange = onFluxStartMutedChanged
          )
          HorizontalDivider(color = colors.outlineVariant.copy(alpha = 0.35f))
          SettingsSwitchRow(
            icon = Icons.Outlined.Download,
            title = strings.settingsFluxAutoDownload,
            subtitle = strings.settingsFluxAutoDownloadDesc,
            checked = fluxAutoDownload,
            onCheckedChange = onFluxAutoDownloadChanged
          )
          HorizontalDivider(color = colors.outlineVariant.copy(alpha = 0.35f))

          val usedMb = cacheBytes / (1024f * 1024f)
          SettingsActionRow(
            icon = Icons.Outlined.CleaningServices,
            title = strings.settingsClearCache,
            subtitle = "${strings.settingsClearCacheDesc} (${String.format(strings.settingsCacheSizeFormat, usedMb)})",
            onClick = {
              val freedBytes = prefs.clearMediaCache(context)
              cacheBytes = prefs.calculateMediaCacheBytes(context)
              val freedMb = freedBytes / (1024f * 1024f)
              Toast.makeText(
                context,
                String.format(strings.settingsCacheClearedToast, freedMb),
                Toast.LENGTH_SHORT
              ).show()
            },
            testTag = "settings_clear_cache_row"
          )
        }

        // 6. Language Card
        SettingsSectionHeader(title = strings.language)
        SettingsSectionCard {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Outlined.Translate,
              contentDescription = null,
              tint = colors.primary,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = strings.language,
              style = MaterialTheme.typography.titleSmall.copy(fontSize = 15.sp),
              fontWeight = FontWeight.SemiBold,
              color = colors.onSurface
            )
          }

          Spacer(modifier = Modifier.height(10.dp))
          HorizontalDivider(color = colors.outlineVariant.copy(alpha = 0.45f))

          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { onLanguageChanged(AppLanguage.EN) }
              .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(
              text = strings.english,
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = if (currentLanguage == AppLanguage.EN) FontWeight.SemiBold else FontWeight.Normal,
              color = colors.onSurface
            )
            RadioButton(
              selected = currentLanguage == AppLanguage.EN,
              onClick = { onLanguageChanged(AppLanguage.EN) },
              modifier = Modifier.testTag("lang_en_radio")
            )
          }

          HorizontalDivider(color = colors.outlineVariant.copy(alpha = 0.35f))

          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { onLanguageChanged(AppLanguage.AR) }
              .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(
              text = strings.arabic,
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = if (currentLanguage == AppLanguage.AR) FontWeight.SemiBold else FontWeight.Normal,
              color = colors.onSurface
            )
            RadioButton(
              selected = currentLanguage == AppLanguage.AR,
              onClick = { onLanguageChanged(AppLanguage.AR) },
              modifier = Modifier.testTag("lang_ar_radio")
            )
          }
        }

        // 7. About Kora
        SettingsSectionCard {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Outlined.Info,
              contentDescription = null,
              tint = colors.primary,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = strings.aboutKoraTitle,
              style = MaterialTheme.typography.titleSmall.copy(fontSize = 15.sp),
              fontWeight = FontWeight.SemiBold,
              color = colors.onSurface
            )
          }
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = strings.aboutKoraDesc,
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurfaceVariant
          )
        }

        if (onSignOut != null) {
          OutlinedButton(
            onClick = onSignOut,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, colors.error.copy(alpha = 0.45f)),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.error),
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
              .testTag("settings_sign_out_button")
          ) {
            Icon(Icons.AutoMirrored.Outlined.ExitToApp, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(strings.signOut, fontWeight = FontWeight.SemiBold)
          }
        }

        Spacer(modifier = Modifier.height(16.dp))
      }
    }

    if (showThemeSheet) {
      ThemeSwitcherSheet(
        currentPreset = currentPreset,
        currentAmoled = currentAmoled,
        currentAccent = currentAccent,
        onThemeChanged = { preset, amoled, accent ->
          onThemeChanged(preset, amoled, accent)
          onThemeModeChanged(if (!preset.isDark) "light" else if (preset == AppThemePreset.SYSTEM) "system" else "dark")
        },
        onDismiss = { showThemeSheet = false }
      )
    }

    if (showEditProfileDialog && userRepository != null) {
      AlertDialog(
        onDismissRequest = { showEditProfileDialog = false },
        containerColor = colors.surface,
        title = { Text(strings.settingsEditIdentity) },
        text = {
          Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            OutlinedTextField(
              value = editDisplayName,
              onValueChange = { editDisplayName = it },
              label = { Text(strings.enterDisplayName) },
              singleLine = true,
              modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
              value = editHandle,
              onValueChange = { editHandle = it },
              label = { Text(strings.enterHandle) },
              singleLine = true,
              modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
              value = editProfession,
              onValueChange = { editProfession = it },
              label = { Text(strings.enterProfession) },
              singleLine = true,
              modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
              value = editBio,
              onValueChange = { editBio = it },
              label = { Text(strings.enterBio) },
              maxLines = 3,
              modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(10.dp))
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
            onClick = {
              val updated = (profile ?: UserProfile(id = uid)).copy(
                displayName = editDisplayName.trim().ifBlank { profile?.displayName ?: "Kora Member" },
                handle = editHandle.trim().ifBlank { profile?.handle ?: "@user" },
                profession = editProfession.trim(),
                bio = editBio.trim(),
                openToNetworking = editOpenToNetworking
              )
              coroutineScope.launch {
                val res = userRepository.createOrUpdateProfile(updated)
                if (res.isSuccess) {
                  showEditProfileDialog = false
                  Toast.makeText(context, strings.profileUpdated, Toast.LENGTH_SHORT).show()
                } else {
                  Toast.makeText(context, strings.errorOccurred, Toast.LENGTH_SHORT).show()
                }
              }
            }
          ) {
            Text(strings.confirm)
          }
        },
        dismissButton = {
          TextButton(onClick = { showEditProfileDialog = false }) {
            Text(strings.cancel)
          }
        }
      )
    }

    if (showAvatarPicker && userRepository != null) {
      AvatarPickerSheet(
        currentAvatarUrl = profile?.avatarUrl,
        displayName = profile?.displayName ?: "User",
        equippedFrame = profile?.equippedFrame,
        onDismiss = { showAvatarPicker = false },
        onSaveAvatar = { newUrl ->
          coroutineScope.launch {
            val res = userRepository.updateAvatarUrl(newUrl)
            if (res.isSuccess) {
              showAvatarPicker = false
              Toast.makeText(context, strings.avatarUpdatedSuccess, Toast.LENGTH_SHORT).show()
            } else {
              Toast.makeText(context, strings.errorOccurred, Toast.LENGTH_SHORT).show()
            }
          }
        }
      )
    }
  }
}

@Composable
private fun SettingsSectionHeader(title: String) {
  Text(
    text = title,
    style = MaterialTheme.typography.labelLarge,
    fontWeight = FontWeight.Bold,
    color = MaterialTheme.colorScheme.onSurfaceVariant,
    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
  )
}

@Composable
private fun SettingsSectionCard(content: @Composable ColumnScope.() -> Unit) {
  val colors = MaterialTheme.colorScheme
  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = colors.surface),
    border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.65f)),
    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      content = content
    )
  }
}

@Composable
private fun SettingsSwitchRow(
  icon: ImageVector,
  title: String,
  subtitle: String,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit,
  iconTint: Color = MaterialTheme.colorScheme.primary
) {
  val colors = MaterialTheme.colorScheme
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .defaultMinSize(minHeight = 48.dp)
      .padding(vertical = 8.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = iconTint,
      modifier = Modifier.size(20.dp)
    )
    Spacer(modifier = Modifier.width(12.dp))
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = title,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.SemiBold,
        color = colors.onSurface
      )
      Text(
        text = subtitle,
        style = MaterialTheme.typography.labelSmall,
        color = colors.onSurfaceVariant
      )
    }
    Spacer(modifier = Modifier.width(12.dp))
    Switch(
      checked = checked,
      onCheckedChange = onCheckedChange,
      colors = SwitchDefaults.colors(
        checkedThumbColor = colors.onPrimary,
        checkedTrackColor = colors.primary
      )
    )
  }
}

@Composable
private fun SettingsActionRow(
  icon: ImageVector,
  title: String,
  subtitle: String,
  onClick: () -> Unit,
  testTag: String? = null
) {
  val colors = MaterialTheme.colorScheme
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .defaultMinSize(minHeight = 48.dp)
      .clip(RoundedCornerShape(10.dp))
      .clickable(onClick = onClick)
      .padding(vertical = 10.dp)
      .then(if (testTag != null) Modifier.testTag(testTag) else Modifier),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = colors.primary,
      modifier = Modifier.size(20.dp)
    )
    Spacer(modifier = Modifier.width(12.dp))
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = title,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.SemiBold,
        color = colors.onSurface
      )
      Text(
        text = subtitle,
        style = MaterialTheme.typography.labelSmall,
        color = colors.onSurfaceVariant
      )
    }
    Icon(
      imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
      contentDescription = null,
      tint = colors.onSurfaceVariant,
      modifier = Modifier.size(20.dp)
    )
  }
}
