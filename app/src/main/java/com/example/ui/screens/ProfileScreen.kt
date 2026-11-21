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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FontDownload
import androidx.compose.material.icons.outlined.MonetizationOn
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.PostAdd
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FluxVideo
import com.example.data.model.Post
import com.example.data.model.UserProfile
import com.example.data.repository.FluxRepository
import com.example.data.repository.PostRepository
import com.example.data.repository.StoreRepository
import com.example.data.repository.UserRepository
import com.example.ui.components.AchievementWallView
import com.example.ui.components.AvatarPickerSheet
import com.example.ui.components.AvatarView
import com.example.ui.components.KoraTopBar
import com.example.ui.components.NameStyleCustomizerSheet
import com.example.ui.components.RoleNameplateView
import com.example.ui.components.StyledDisplayName
import com.example.ui.components.VerificationTier
import com.example.ui.components.VerifiedBadge
import com.example.ui.theme.StatusQuiet
import com.example.ui.util.AppLanguage
import com.example.ui.util.LocalAppLanguage
import com.example.ui.util.LocalAppStrings
import com.example.ui.util.formatHandleForDisplay
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
  userRepository: UserRepository,
  storeRepository: StoreRepository,
  postRepository: PostRepository? = null,
  fluxRepository: FluxRepository? = null,
  onOpenStore: () -> Unit,
  onLanguageToggle: () -> Unit,
  onOpenSettings: (() -> Unit)? = null,
  onOpenThemeStudio: (() -> Unit)? = null,
  onSignOut: (() -> Unit)? = null
) {
  val context = LocalContext.current
  val strings = LocalAppStrings.current
  val colors = MaterialTheme.colorScheme
  val currentLang = LocalAppLanguage.current
  val isArabic = currentLang == AppLanguage.AR
  val coroutineScope = rememberCoroutineScope()
  val uid = userRepository.requireUserId()

  val cachedInitialProfile = remember(uid) { userRepository.getCachedProfile(uid) }
  val profile by userRepository.observeUserProfile(uid).collectAsState(initial = cachedInitialProfile)
  val storeFrame by storeRepository.equippedFrame.collectAsState()
  val storeNameplate by storeRepository.equippedNameplate.collectAsState()
  val storeWall by storeRepository.equippedWall.collectAsState()
  val equippedFrame = storeFrame ?: profile?.equippedFrame
  val equippedNameplate = storeNameplate ?: profile?.equippedNameplate
  val equippedWall = storeWall ?: profile?.equippedWall
  val coins by storeRepository.coinBalance.collectAsState()

  val allFluxVideos by remember(fluxRepository) {
    fluxRepository?.observeVideos() ?: flowOf(emptyList())
  }.collectAsState(initial = emptyList())
  val allPosts by remember(postRepository) {
    postRepository?.observePosts("all") ?: flowOf(emptyList())
  }.collectAsState(initial = emptyList())

  val myFluxVideos = remember(allFluxVideos, uid) { allFluxVideos.filter { it.authorId == uid } }
  val myPosts = remember(allPosts, uid) { allPosts.filter { it.authorId == uid } }

  var showEditDialog by remember { mutableStateOf(false) }
  var showAvatarPickerSheet by remember { mutableStateOf(false) }
  var showNameStyleSheet by remember { mutableStateOf(false) }
  var myContentTabIndex by remember { mutableIntStateOf(0) }
  var editingFlux by remember { mutableStateOf<FluxVideo?>(null) }
  var editFluxCaption by remember { mutableStateOf("") }
  var editFluxAudio by remember { mutableStateOf("") }
  var deletingFlux by remember { mutableStateOf<FluxVideo?>(null) }
  var editingPost by remember { mutableStateOf<Post?>(null) }
  var editPostContent by remember { mutableStateOf("") }
  var editPostAudience by remember { mutableStateOf("public") }
  var deletingPost by remember { mutableStateOf<Post?>(null) }

  var editDisplayName by remember { mutableStateOf("") }
  var editHandle by remember { mutableStateOf("") }
  var editBio by remember { mutableStateOf("") }
  var editProfession by remember { mutableStateOf("") }
  var editOpenToNetworking by remember { mutableStateOf(true) }

  fun openEditDialog() {
    editDisplayName = profile?.displayName ?: ""
    editHandle = profile?.handle ?: ""
    editBio = profile?.bio ?: ""
    editProfession = profile?.profession ?: ""
    editOpenToNetworking = profile?.openToNetworking ?: true
    showEditDialog = true
  }

  fun saveProfileEdits() {
    val cleanHandle = editHandle.trim().ifBlank { profile?.handle ?: "@user" }
    val updated = (profile ?: UserProfile(id = uid)).copy(
      displayName = editDisplayName.trim().ifBlank { profile?.displayName ?: "Kora Member" },
      handle = cleanHandle,
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

  val cardShape = RoundedCornerShape(14.dp)
  val cardBorder = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.65f))

  Scaffold(
    containerColor = colors.background,
    contentWindowInsets = WindowInsets(0, 0, 0, 0),
    topBar = {
      KoraTopBar(
        title = strings.profileTitle,
        onLanguageToggle = onLanguageToggle,
        actions = {
          if (onOpenSettings != null) {
            IconButton(
              onClick = onOpenSettings,
              modifier = Modifier.testTag("profile_open_settings_icon")
            ) {
              Icon(
                imageVector = Icons.Outlined.Settings,
                contentDescription = strings.settingsTitle,
                tint = colors.onSurfaceVariant
              )
            }
          }
        }
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
        // 1. Clean Discord User Identity Card (single balanced badge, structured hierarchy, zero rainbow clutter)
        Card(
          shape = cardShape,
          colors = CardDefaults.cardColors(containerColor = colors.surface),
          border = cardBorder,
          elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.fillMaxWidth()) {
            // Subtle Discord Profile Header Banner (or equipped Achievement Wall) + Overlapping Avatar
            Box(modifier = Modifier.fillMaxWidth()) {
              if (!equippedWall.isNullOrBlank()) {
                AchievementWallView(
                  styleKey = equippedWall,
                  scrimAlpha = 0.22f,
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(92.dp)
                )
              } else {
                Box(
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(74.dp)
                    .background(
                      Brush.horizontalGradient(
                        listOf(
                          colors.surfaceContainerLowest,
                          colors.primary.copy(alpha = 0.24f)
                        )
                      )
                    )
                )
              }

              Box(
                modifier = Modifier
                  .padding(top = if (!equippedWall.isNullOrBlank()) 42.dp else 26.dp)
                  .align(Alignment.BottomCenter)
                  .clickable { showAvatarPickerSheet = true }
                  .testTag("profile_avatar_clickable_container"),
                contentAlignment = Alignment.Center
              ) {
                Box(
                  modifier = Modifier
                    .clip(CircleShape)
                    .background(colors.surface)
                    .padding(4.dp)
                ) {
                  AvatarView(
                    name = profile?.displayName ?: strings.appName,
                    avatarUrl = profile?.avatarUrl,
                    size = 84.dp,
                    frameStyleKey = equippedFrame,
                    showPresence = true,
                    isOnline = true,
                    isQuiet = profile?.quietMode == true
                  )
                }

                Box(
                  modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(colors.primary)
                    .border(2.dp, colors.surface, CircleShape),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Outlined.CameraAlt,
                    contentDescription = strings.changeAvatarTitle,
                    tint = colors.onPrimary,
                    modifier = Modifier.size(15.dp)
                  )
                }
              }
            }

            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              // Name (with Custom Display Name Font & Theme ONLY) + Single Balanced Verification Badge
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
              ) {
                StyledDisplayName(
                  name = profile?.displayName?.takeIf { it.isNotBlank() } ?: strings.loading,
                  nameFontId = profile?.nameFontId,
                  nameThemeId = profile?.nameThemeId,
                  style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                  fallbackColor = colors.onSurface,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                  modifier = Modifier.weight(1f, fill = false)
                )
                if (profile?.isVerified == true) {
                  Spacer(modifier = Modifier.width(6.dp))
                  VerifiedBadge(
                    size = 18.dp,
                    tierKey = profile?.verifiedTier,
                    showDialogOnClick = true
                  )
                }
              }

              Spacer(modifier = Modifier.height(3.dp))

              // Handle
              Text(
                text = formatHandleForDisplay(profile?.handle),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant,
                fontWeight = FontWeight.Medium
              )

              Spacer(modifier = Modifier.height(10.dp))

              // Followers & Following Metrics Row
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                Surface(
                  shape = RoundedCornerShape(10.dp),
                  color = colors.background,
                  border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.5f)),
                  modifier = Modifier
                    .weight(1f)
                    .testTag("profile_followers_stat")
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Text(
                      text = (profile?.followersCount ?: 0L).toString(),
                      style = MaterialTheme.typography.titleSmall,
                      fontWeight = FontWeight.ExtraBold,
                      color = colors.onSurface
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                      text = strings.followersLabel,
                      style = MaterialTheme.typography.labelMedium,
                      color = colors.onSurfaceVariant,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis
                    )
                  }
                }
                Surface(
                  shape = RoundedCornerShape(10.dp),
                  color = colors.background,
                  border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.5f)),
                  modifier = Modifier
                    .weight(1f)
                    .testTag("profile_following_stat")
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Text(
                      text = (profile?.followingCount ?: 0L).toString(),
                      style = MaterialTheme.typography.titleSmall,
                      fontWeight = FontWeight.ExtraBold,
                      color = colors.onSurface
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                      text = strings.followingLabel,
                      style = MaterialTheme.typography.labelMedium,
                      color = colors.onSurfaceVariant,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis
                    )
                  }
                }
              }

              // Single Optional Role/Specialty Pill (never repeats displayName)
              val roleLabel = profile?.profession?.takeIf { it.isNotBlank() }
              if (equippedNameplate != null) {
                Spacer(modifier = Modifier.height(8.dp))
                RoleNameplateView(
                  styleKey = equippedNameplate,
                  title = roleLabel ?: if (isArabic) "عضو كورا" else "Kora Member"
                )
              } else if (roleLabel != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = colors.surfaceVariant,
                  border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.6f))
                ) {
                  Text(
                    text = roleLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                  )
                }
              }

              // Discord-style "About Me" Inset Section
              if (!profile?.bio.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                  shape = RoundedCornerShape(10.dp),
                  color = colors.background,
                  border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.45f)),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Column(
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(horizontal = 12.dp, vertical = 10.dp)
                  ) {
                    Text(
                      text = strings.settingsAboutMeLabel,
                      style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                      fontWeight = FontWeight.Bold,
                      color = colors.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                      text = profile!!.bio,
                      style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                      color = colors.onSurface
                    )
                  }
                }
              }

              Spacer(modifier = Modifier.height(14.dp))

              // Balanced 2-Button Identity Action Row
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                OutlinedButton(
                  onClick = { openEditDialog() },
                  shape = RoundedCornerShape(10.dp),
                  border = BorderStroke(1.dp, colors.outlineVariant),
                  colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = colors.surfaceVariant,
                    contentColor = colors.onSurface
                  ),
                  contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                  modifier = Modifier
                    .weight(1f)
                    .defaultMinSize(minHeight = 44.dp)
                    .testTag("edit_profile_button")
                ) {
                  Icon(Icons.Outlined.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = strings.editProfile,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                }

                OutlinedButton(
                  onClick = { showAvatarPickerSheet = true },
                  shape = RoundedCornerShape(10.dp),
                  border = BorderStroke(1.dp, colors.outlineVariant),
                  colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = colors.surfaceVariant,
                    contentColor = colors.onSurface
                  ),
                  contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                  modifier = Modifier
                    .weight(1f)
                    .defaultMinSize(minHeight = 44.dp)
                    .testTag("change_avatar_text_button")
                ) {
                  Icon(Icons.Outlined.PhotoCamera, contentDescription = null, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = strings.changeAvatarTitle,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                }
              }

              Spacer(modifier = Modifier.height(10.dp))

              // Display Name Font & Theme Customizer Button (affects display name only)
              OutlinedButton(
                onClick = { showNameStyleSheet = true },
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.45f)),
                colors = ButtonDefaults.outlinedButtonColors(
                  containerColor = colors.primary.copy(alpha = 0.12f),
                  contentColor = colors.primary
                ),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                modifier = Modifier
                  .fillMaxWidth()
                  .defaultMinSize(minHeight = 46.dp)
                  .testTag("customize_name_style_button")
              ) {
                Icon(Icons.Outlined.FontDownload, contentDescription = null, modifier = Modifier.size(17.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = strings.customizeNameStyleBtn,
                  style = MaterialTheme.typography.labelLarge,
                  fontWeight = FontWeight.Bold,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
              }
            }
          }
        }

        // 1b. Author's Published Flux Clips & Feed Posts Management Card
        if (fluxRepository != null || postRepository != null) {
          Card(
            shape = cardShape,
            colors = CardDefaults.cardColors(containerColor = colors.surface),
            border = cardBorder,
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("profile_my_content_card")
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
              verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = strings.myContentTitle,
                  style = MaterialTheme.typography.titleSmall.copy(fontSize = 15.sp),
                  fontWeight = FontWeight.Bold,
                  color = colors.onSurface
                )
                Text(
                  text = "${myFluxVideos.size + myPosts.size}",
                  style = MaterialTheme.typography.labelMedium,
                  color = colors.primary,
                  fontWeight = FontWeight.Bold
                )
              }

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                FilterChip(
                  selected = myContentTabIndex == 0,
                  onClick = { myContentTabIndex = 0 },
                  leadingIcon = {
                    Icon(
                      imageVector = Icons.Outlined.Movie,
                      contentDescription = null,
                      modifier = Modifier.size(16.dp)
                    )
                  },
                  label = {
                    Text(
                      text = "${strings.myContentFluxTab} (${myFluxVideos.size})",
                      fontWeight = if (myContentTabIndex == 0) FontWeight.Bold else FontWeight.Medium
                    )
                  },
                  colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = colors.primary.copy(alpha = 0.16f),
                    selectedLabelColor = colors.primary,
                    selectedLeadingIconColor = colors.primary
                  ),
                  modifier = Modifier.testTag("profile_my_flux_tab")
                )
                FilterChip(
                  selected = myContentTabIndex == 1,
                  onClick = { myContentTabIndex = 1 },
                  leadingIcon = {
                    Icon(
                      imageVector = Icons.Outlined.PostAdd,
                      contentDescription = null,
                      modifier = Modifier.size(16.dp)
                    )
                  },
                  label = {
                    Text(
                      text = "${strings.myContentPostsTab} (${myPosts.size})",
                      fontWeight = if (myContentTabIndex == 1) FontWeight.Bold else FontWeight.Medium
                    )
                  },
                  colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = colors.primary.copy(alpha = 0.16f),
                    selectedLabelColor = colors.primary,
                    selectedLeadingIconColor = colors.primary
                  ),
                  modifier = Modifier.testTag("profile_my_posts_tab")
                )
              }

              if (myContentTabIndex == 0) {
                if (myFluxVideos.isEmpty()) {
                  Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = colors.background,
                    border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                  ) {
                    Text(
                      text = strings.noMyFluxYet,
                      style = MaterialTheme.typography.bodySmall,
                      color = colors.onSurfaceVariant,
                      textAlign = TextAlign.Center,
                      modifier = Modifier.padding(16.dp)
                    )
                  }
                } else {
                  myFluxVideos.forEach { clip ->
                    Surface(
                      shape = RoundedCornerShape(12.dp),
                      color = colors.background,
                      border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.5f)),
                      modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_flux_item_${clip.id}")
                    ) {
                      Row(
                        modifier = Modifier
                          .fillMaxWidth()
                          .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                      ) {
                        Box(
                          modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.primary.copy(alpha = 0.14f)),
                          contentAlignment = Alignment.Center
                        ) {
                          Icon(
                            imageVector = Icons.Outlined.Movie,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(20.dp)
                          )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                          Text(
                            text = clip.caption.ifBlank { "#flux" },
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                          )
                          Text(
                            text = "${clip.audioTitle} • ❤️ ${clip.likesCount} • 💬 ${clip.commentsCount}",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                          )
                        }
                        IconButton(
                          onClick = {
                            editFluxCaption = clip.caption
                            editFluxAudio = clip.audioTitle
                            editingFlux = clip
                          },
                          modifier = Modifier
                            .size(40.dp)
                            .testTag("profile_edit_flux_${clip.id}")
                        ) {
                          Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = strings.edit,
                            tint = colors.primary,
                            modifier = Modifier.size(18.dp)
                          )
                        }
                        IconButton(
                          onClick = { deletingFlux = clip },
                          modifier = Modifier
                            .size(40.dp)
                            .testTag("profile_delete_flux_${clip.id}")
                        ) {
                          Icon(
                            imageVector = Icons.Outlined.DeleteOutline,
                            contentDescription = strings.delete,
                            tint = colors.error,
                            modifier = Modifier.size(18.dp)
                          )
                        }
                      }
                    }
                  }
                }
              } else {
                if (myPosts.isEmpty()) {
                  Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = colors.background,
                    border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                  ) {
                    Text(
                      text = strings.noMyPostsYet,
                      style = MaterialTheme.typography.bodySmall,
                      color = colors.onSurfaceVariant,
                      textAlign = TextAlign.Center,
                      modifier = Modifier.padding(16.dp)
                    )
                  }
                } else {
                  myPosts.forEach { post ->
                    Surface(
                      shape = RoundedCornerShape(12.dp),
                      color = colors.background,
                      border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.5f)),
                      modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_post_item_${post.id}")
                    ) {
                      Row(
                        modifier = Modifier
                          .fillMaxWidth()
                          .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                      ) {
                        Column(modifier = Modifier.weight(1f)) {
                          Text(
                            text = post.content,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = colors.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                          )
                          Spacer(modifier = Modifier.height(2.dp))
                          val audLabel = if (post.audience == "circles") strings.audienceCircles else strings.audiencePublic
                          Text(
                            text = "$audLabel • ❤️ ${post.likesCount} • 💬 ${post.commentsCount}",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                          )
                        }
                        IconButton(
                          onClick = {
                            editPostContent = post.content
                            editPostAudience = post.audience
                            editingPost = post
                          },
                          modifier = Modifier
                            .size(40.dp)
                            .testTag("profile_edit_post_${post.id}")
                        ) {
                          Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = strings.edit,
                            tint = colors.primary,
                            modifier = Modifier.size(18.dp)
                          )
                        }
                        IconButton(
                          onClick = { deletingPost = post },
                          modifier = Modifier
                            .size(40.dp)
                            .testTag("profile_delete_post_${post.id}")
                        ) {
                          Icon(
                            imageVector = Icons.Outlined.DeleteOutline,
                            contentDescription = strings.delete,
                            tint = colors.error,
                            modifier = Modifier.size(18.dp)
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

        // 2. Custom Decoration Store Card (aligned rows & columns, calm single accent)
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
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            Box(
              modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(colors.primary.copy(alpha = 0.16f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Outlined.AutoAwesome,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(21.dp)
              )
            }

            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = strings.customDecoration,
                style = MaterialTheme.typography.titleSmall.copy(fontSize = 15.sp),
                fontWeight = FontWeight.SemiBold,
                color = colors.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
              Spacer(modifier = Modifier.height(2.dp))
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Outlined.MonetizationOn,
                  contentDescription = null,
                  tint = Color(0xFFF0B232),
                  modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "$coins ${strings.coinsUnit}",
                  style = MaterialTheme.typography.labelMedium,
                  color = colors.onSurfaceVariant,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
              }
            }

            Button(
              onClick = onOpenStore,
              colors = ButtonDefaults.buttonColors(
                containerColor = colors.primary,
                contentColor = colors.onPrimary
              ),
              shape = RoundedCornerShape(10.dp),
              contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
              modifier = Modifier.defaultMinSize(minWidth = 88.dp, minHeight = 42.dp)
            ) {
              Text(
                text = strings.preview,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                softWrap = false
              )
            }
          }
        }

        // 3. Balanced 2-Column Utility Row (Themes + Comprehensive Settings)
        if (onOpenThemeStudio != null || onOpenSettings != null) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            if (onOpenThemeStudio != null) {
              ProfileQuickGridCard(
                icon = Icons.Outlined.Palette,
                title = strings.themeSwitcherTitle,
                subtitle = strings.quickThemeSwitcher,
                onClick = onOpenThemeStudio,
                modifier = Modifier.weight(1f)
              )
            }
            if (onOpenSettings != null) {
              ProfileQuickGridCard(
                icon = Icons.Outlined.Tune,
                title = strings.settingsFullSettingsCardTitle,
                subtitle = strings.settingsOpenButton,
                onClick = onOpenSettings,
                modifier = Modifier.weight(1f),
                testTag = "profile_open_settings_card"
              )
            }
          }
        }

        // 4. Calm Verification Badge Card
        Card(
          shape = cardShape,
          colors = CardDefaults.cardColors(containerColor = colors.surface),
          border = cardBorder,
          elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("verification_badge_card")
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            val activeTier = remember(profile?.verifiedTier) {
              VerificationTier.fromId(profile?.verifiedTier)
            }

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(12.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(42.dp)
                  .clip(RoundedCornerShape(12.dp))
                  .background(colors.primary.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
              ) {
                if (profile?.isVerified == true) {
                  VerifiedBadge(size = 24.dp, tierKey = profile?.verifiedTier, showDialogOnClick = true)
                } else {
                  Icon(
                    imageVector = Icons.Outlined.Verified,
                    contentDescription = null,
                    tint = colors.onSurfaceVariant,
                    modifier = Modifier.size(21.dp)
                  )
                }
              }

              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = strings.verificationBadge,
                  style = MaterialTheme.typography.titleSmall.copy(fontSize = 15.sp),
                  fontWeight = FontWeight.SemiBold,
                  color = colors.onSurface
                )
                Text(
                  text = if (profile?.isVerified == true) {
                    if (isArabic) activeTier.titleAr else activeTier.titleEn
                  } else {
                    strings.verifiedBadgeSubtitle
                  },
                  style = MaterialTheme.typography.bodySmall,
                  color = colors.onSurfaceVariant,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
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

            Spacer(modifier = Modifier.height(10.dp))

            Row(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(colors.background)
                .border(1.dp, colors.outlineVariant.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = if (profile?.isVerified == true) Icons.Outlined.CheckCircle else Icons.Outlined.Bedtime,
                contentDescription = null,
                tint = if (profile?.isVerified == true) colors.primary else colors.onSurfaceVariant,
                modifier = Modifier.size(14.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = if (profile?.isVerified == true)
                  "${strings.verifiedStatusActive} • ${if (isArabic) activeTier.titleAr else activeTier.titleEn}"
                else strings.verifiedStatusInactive,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = if (profile?.isVerified == true) colors.onSurface else colors.onSurfaceVariant
              )
            }

            // Tidy 2-Column Badge Tier Grid
            if (profile?.isVerified == true) {
              Spacer(modifier = Modifier.height(12.dp))
              HorizontalDivider(color = colors.outlineVariant.copy(alpha = 0.45f))
              Spacer(modifier = Modifier.height(12.dp))

              Text(
                text = if (isArabic) "نمط الشارة" else "Badge Style",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = colors.onSurfaceVariant
              )
              Spacer(modifier = Modifier.height(8.dp))

              val currentTierId = profile?.verifiedTier ?: "BLUE"
              val rows = remember { VerificationTier.values().toList().chunked(2) }
              Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                rows.forEach { pair ->
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                  ) {
                    pair.forEach { tier ->
                      val isSelected = currentTierId.equals(tier.id, ignoreCase = true)
                      Surface(
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
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) colors.primary.copy(alpha = 0.14f) else colors.background,
                        border = BorderStroke(
                          width = if (isSelected) 1.5.dp else 1.dp,
                          color = if (isSelected) colors.primary else colors.outlineVariant.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier
                          .weight(1f)
                          .defaultMinSize(minHeight = 46.dp)
                          .testTag("tier_card_${tier.id.lowercase()}")
                      ) {
                        Row(
                          modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
                          verticalAlignment = Alignment.CenterVertically,
                          horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                          VerifiedBadge(size = 20.dp, tier = tier, showDialogOnClick = false)
                          Text(
                            text = if (isArabic) tier.titleAr else tier.titleEn,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) colors.primary else colors.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                          )
                        }
                      }
                    }
                    if (pair.size == 1) {
                      Spacer(modifier = Modifier.weight(1f))
                    }
                  }
                }
              }
            }
          }
        }

        // 5. Quiet Presence & Read Receipts Card
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
              style = MaterialTheme.typography.titleSmall.copy(fontSize = 15.sp),
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

            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Outlined.Bedtime,
                contentDescription = null,
                tint = if (profile?.quietMode == true) StatusQuiet else colors.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(12.dp))
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = strings.quietMode,
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.SemiBold,
                  color = colors.onSurface
                )
                Text(
                  text = if (profile?.quietMode == true) strings.quietFocusActive else strings.standardAlerts,
                  style = MaterialTheme.typography.labelSmall,
                  color = colors.onSurfaceVariant
                )
              }
              Spacer(modifier = Modifier.width(12.dp))
              Switch(
                checked = profile?.quietMode == true,
                onCheckedChange = { checked ->
                  coroutineScope.launch {
                    val res = userRepository.setQuietMode(checked)
                    if (res.isFailure) {
                      Toast.makeText(
                        context,
                        res.exceptionOrNull()?.localizedMessage ?: strings.errorOccurred,
                        Toast.LENGTH_SHORT
                      ).show()
                    }
                  }
                },
                modifier = Modifier.testTag("quiet_mode_switch")
              )
            }

            HorizontalDivider(color = colors.outlineVariant.copy(alpha = 0.35f))

            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Outlined.Visibility,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(12.dp))
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = strings.readReceipts,
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.SemiBold,
                  color = colors.onSurface
                )
                Text(
                  text = strings.readReceiptsDesc,
                  style = MaterialTheme.typography.labelSmall,
                  color = colors.onSurfaceVariant
                )
              }
              Spacer(modifier = Modifier.width(12.dp))
              Switch(
                checked = profile?.readReceiptsEnabled != false,
                onCheckedChange = { checked ->
                  coroutineScope.launch {
                    val updated = (profile ?: UserProfile(id = uid)).copy(readReceiptsEnabled = checked)
                    val res = userRepository.createOrUpdateProfile(updated)
                    if (res.isFailure) {
                      Toast.makeText(
                        context,
                        res.exceptionOrNull()?.localizedMessage ?: strings.errorOccurred,
                        Toast.LENGTH_SHORT
                      ).show()
                    }
                  }
                }
              )
            }
          }
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
              .testTag("sign_out_button")
          ) {
            Icon(Icons.AutoMirrored.Outlined.ExitToApp, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(strings.signOut, fontWeight = FontWeight.SemiBold)
          }
        } else {
          Button(
            onClick = onOpenStore,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
              .testTag("open_store_primary_button")
          ) {
            Icon(Icons.Outlined.Storefront, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              if (isArabic) "استكشاف متجر الزينة" else "Explore Custom Decorations",
              fontWeight = FontWeight.SemiBold
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))
      }
    }
  }

  // Edit Profile Dialog
  if (showEditDialog) {
    AlertDialog(
      onDismissRequest = { showEditDialog = false },
      containerColor = colors.surface,
      title = { Text(if (isArabic) "تعديل الهوية والملف" else "Edit Identity") },
      text = {
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
          OutlinedTextField(
            value = editDisplayName,
            onValueChange = { editDisplayName = it },
            label = { Text(strings.enterDisplayName) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("edit_display_name_input")
          )
          Spacer(modifier = Modifier.height(10.dp))
          OutlinedTextField(
            value = editHandle,
            onValueChange = { editHandle = it },
            label = { Text(strings.enterHandle) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("edit_handle_input")
          )
          Spacer(modifier = Modifier.height(10.dp))
          OutlinedTextField(
            value = editProfession,
            onValueChange = { editProfession = it },
            label = { Text(strings.enterProfession) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("edit_profession_input")
          )
          Spacer(modifier = Modifier.height(10.dp))
          OutlinedTextField(
            value = editBio,
            onValueChange = { editBio = it },
            label = { Text(strings.enterBio) },
            maxLines = 3,
            modifier = Modifier.fillMaxWidth().testTag("edit_bio_input")
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

  // Display Name Style & Font Sheet (applies strictly to display name)
  if (showNameStyleSheet) {
    NameStyleCustomizerSheet(
      displayName = profile?.displayName ?: strings.appName,
      handle = formatHandleForDisplay(profile?.handle),
      initialFontId = profile?.nameFontId ?: "default",
      initialThemeId = profile?.nameThemeId ?: "default",
      onDismiss = { showNameStyleSheet = false },
      onSave = { fontId, themeId ->
        coroutineScope.launch {
          val res = userRepository.updateDisplayNameStyle(fontId, themeId)
          if (res.isSuccess) {
            showNameStyleSheet = false
            Toast.makeText(context, strings.profileUpdated, Toast.LENGTH_SHORT).show()
          } else {
            Toast.makeText(context, strings.errorOccurred, Toast.LENGTH_SHORT).show()
          }
        }
      }
    )
  }

  // Edit Flux Clip Dialog
  editingFlux?.let { clip ->
    AlertDialog(
      onDismissRequest = { editingFlux = null },
      containerColor = colors.surface,
      title = { Text(strings.editFluxTitle) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          OutlinedTextField(
            value = editFluxCaption,
            onValueChange = { editFluxCaption = it },
            label = { Text(strings.fluxCaptionPlaceholder) },
            maxLines = 4,
            modifier = Modifier.fillMaxWidth().testTag("edit_flux_caption_input")
          )
          OutlinedTextField(
            value = editFluxAudio,
            onValueChange = { editFluxAudio = it },
            label = { Text(strings.editFluxAudioLabel) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("edit_flux_audio_input")
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            val repo = fluxRepository ?: return@Button
            coroutineScope.launch {
              val res = repo.updateFluxVideo(clip.id, editFluxCaption, editFluxAudio)
              if (res.isSuccess) {
                editingFlux = null
                Toast.makeText(context, strings.contentUpdatedToast, Toast.LENGTH_SHORT).show()
              } else {
                Toast.makeText(context, strings.errorOccurred, Toast.LENGTH_SHORT).show()
              }
            }
          },
          modifier = Modifier.testTag("save_edit_flux_button")
        ) {
          Text(strings.confirm)
        }
      },
      dismissButton = {
        TextButton(onClick = { editingFlux = null }) {
          Text(strings.cancel)
        }
      }
    )
  }

  // Delete Flux Confirmation Dialog
  deletingFlux?.let { clip ->
    AlertDialog(
      onDismissRequest = { deletingFlux = null },
      containerColor = colors.surface,
      title = { Text(strings.deleteContentTitle) },
      text = { Text(strings.deleteContentConfirm) },
      confirmButton = {
        Button(
          onClick = {
            val repo = fluxRepository ?: return@Button
            coroutineScope.launch {
              val res = repo.deleteFluxVideo(clip.id)
              if (res.isSuccess) {
                deletingFlux = null
                Toast.makeText(context, strings.contentDeletedToast, Toast.LENGTH_SHORT).show()
              } else {
                Toast.makeText(context, strings.errorOccurred, Toast.LENGTH_SHORT).show()
              }
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = colors.error),
          modifier = Modifier.testTag("confirm_delete_flux_button")
        ) {
          Text(strings.delete)
        }
      },
      dismissButton = {
        TextButton(onClick = { deletingFlux = null }) {
          Text(strings.cancel)
        }
      }
    )
  }

  // Edit Post Dialog
  editingPost?.let { post ->
    AlertDialog(
      onDismissRequest = { editingPost = null },
      containerColor = colors.surface,
      title = { Text(strings.editPostTitle) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          OutlinedTextField(
            value = editPostContent,
            onValueChange = { editPostContent = it },
            label = { Text(strings.whatsOnYourMind) },
            maxLines = 5,
            modifier = Modifier.fillMaxWidth().testTag("edit_post_content_input")
          )
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
              selected = editPostAudience == "public",
              onClick = { editPostAudience = "public" },
              label = { Text(strings.audiencePublic) }
            )
            FilterChip(
              selected = editPostAudience == "circles",
              onClick = { editPostAudience = "circles" },
              label = { Text(strings.audienceCircles) }
            )
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            val repo = postRepository ?: return@Button
            coroutineScope.launch {
              val res = repo.updatePost(post.id, editPostContent, editPostAudience)
              if (res.isSuccess) {
                editingPost = null
                Toast.makeText(context, strings.contentUpdatedToast, Toast.LENGTH_SHORT).show()
              } else {
                Toast.makeText(context, strings.errorOccurred, Toast.LENGTH_SHORT).show()
              }
            }
          },
          modifier = Modifier.testTag("save_edit_post_button")
        ) {
          Text(strings.confirm)
        }
      },
      dismissButton = {
        TextButton(onClick = { editingPost = null }) {
          Text(strings.cancel)
        }
      }
    )
  }

  // Delete Post Confirmation Dialog
  deletingPost?.let { post ->
    AlertDialog(
      onDismissRequest = { deletingPost = null },
      containerColor = colors.surface,
      title = { Text(strings.deleteContentTitle) },
      text = { Text(strings.deleteContentConfirm) },
      confirmButton = {
        Button(
          onClick = {
            val repo = postRepository ?: return@Button
            coroutineScope.launch {
              val res = repo.deletePost(post.id)
              if (res.isSuccess) {
                deletingPost = null
                Toast.makeText(context, strings.contentDeletedToast, Toast.LENGTH_SHORT).show()
              } else {
                Toast.makeText(context, strings.errorOccurred, Toast.LENGTH_SHORT).show()
              }
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = colors.error),
          modifier = Modifier.testTag("confirm_delete_post_button")
        ) {
          Text(strings.delete)
        }
      },
      dismissButton = {
        TextButton(onClick = { deletingPost = null }) {
          Text(strings.cancel)
        }
      }
    )
  }
}

@Composable
private fun ProfileQuickGridCard(
  icon: ImageVector,
  title: String,
  subtitle: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  testTag: String? = null
) {
  val colors = MaterialTheme.colorScheme
  Card(
    onClick = onClick,
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = colors.surface),
    border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.65f)),
    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    modifier = modifier
      .defaultMinSize(minHeight = 82.dp)
      .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 14.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      Box(
        modifier = Modifier
          .size(38.dp)
          .clip(RoundedCornerShape(10.dp))
          .background(colors.primary.copy(alpha = 0.14f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = title,
          tint = colors.primary,
          modifier = Modifier.size(20.dp)
        )
      }
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = title,
          style = MaterialTheme.typography.titleSmall.copy(fontSize = 14.sp),
          fontWeight = FontWeight.SemiBold,
          color = colors.onSurface,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
        Text(
          text = subtitle,
          style = MaterialTheme.typography.labelSmall,
          color = colors.primary,
          fontWeight = FontWeight.Medium,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }
    }
  }
}
