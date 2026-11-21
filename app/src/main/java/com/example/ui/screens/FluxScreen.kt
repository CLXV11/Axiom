package com.example.ui.screens

import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.outlined.VolumeOff
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.AddReaction
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.data.model.FluxComment
import com.example.data.model.FluxVideo
import com.example.data.model.UserProfile
import com.example.data.repository.ChatMediaRepository
import com.example.data.repository.FluxRepository
import com.example.data.repository.UserRepository
import com.example.ui.components.AvatarView
import com.example.ui.components.GlossyAppleEmoji
import com.example.ui.components.IosFloatingReactionBar
import com.example.ui.components.ReactionChipsRow
import com.example.ui.components.StyledDisplayName
import com.example.ui.components.VerifiedBadge
import com.example.ui.components.WeirdEmojiPickerBottomSheet
import com.example.ui.theme.KoraTheme
import com.example.ui.util.LocalAppStrings
import com.example.ui.util.formatHandleForDisplay
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private enum class FluxFeedFilter {
  ALL,
  SAVED
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FluxScreen(
  fluxRepository: FluxRepository,
  userRepository: UserRepository,
  onLanguageToggle: () -> Unit,
  initialMuted: Boolean = false,
  autoLoadVideos: Boolean = true,
  onInspectUser: (UserProfile) -> Unit = {}
) {
  val context = LocalContext.current
  val strings = LocalAppStrings.current
  val colors = MaterialTheme.colorScheme
  val coroutineScope = rememberCoroutineScope()
  val currentUid = userRepository.requireUserId()

  val videos by fluxRepository.observeVideos().collectAsState(initial = emptyList())
  val directoryUsers by userRepository.observeDirectoryUsers()
    .collectAsState(initial = userRepository.getDirectoryUsers())
  val currentUserProfile by userRepository.observeUserProfile(currentUid)
    .collectAsState(initial = userRepository.getCachedProfile(currentUid))
  var extraUsers by remember { mutableStateOf<Map<String, UserProfile>>(emptyMap()) }

  LaunchedEffect(Unit) {
    userRepository.fetchDirectoryUsers()
  }

  val usersById = remember(directoryUsers, currentUserProfile, currentUid, extraUsers) {
    val map = extraUsers.toMutableMap()
    directoryUsers.forEach { map[it.id] = it }
    currentUserProfile?.let { map[currentUid] = it }
    map
  }

  LaunchedEffect(videos, directoryUsers) {
    val missingAuthorIds = videos.map { it.authorId }
      .distinct()
      .filter { it != currentUid && directoryUsers.none { u -> u.id == it } && extraUsers[it] == null }
    if (missingAuthorIds.isNotEmpty()) {
      val fetched = mutableMapOf<String, UserProfile>()
      for (aid in missingAuthorIds) {
        val loaded = runCatching { userRepository.getUserProfile(aid) }.getOrNull()
        if (loaded != null) fetched[aid] = loaded
      }
      if (fetched.isNotEmpty()) {
        extraUsers = extraUsers + fetched
      }
    }
  }

  var selectedFilter by remember { mutableStateOf(FluxFeedFilter.ALL) }
  val filteredVideos = remember(videos, selectedFilter, currentUid) {
    if (selectedFilter == FluxFeedFilter.ALL) videos
    else videos.filter { it.savedBy.contains(currentUid) }
  }

  val pagerState = rememberPagerState(pageCount = { filteredVideos.size })
  LaunchedEffect(filteredVideos.size) {
    if (filteredVideos.isNotEmpty() && pagerState.currentPage >= filteredVideos.size) {
      pagerState.scrollToPage(filteredVideos.size - 1)
    }
  }

  var showCreateSheet by remember { mutableStateOf(false) }
  var activeCommentsFlux by remember { mutableStateOf<FluxVideo?>(null) }
  var reactionTargetFlux by remember { mutableStateOf<FluxVideo?>(null) }
  var editingFlux by remember { mutableStateOf<FluxVideo?>(null) }
  var deletingFlux by remember { mutableStateOf<FluxVideo?>(null) }
  var isAudioMuted by remember(initialMuted) { mutableStateOf(initialMuted) }
  var isRefreshingFeed by remember { mutableStateOf(false) }

  val isDarkBackdrop = colors.background.red < 0.2f
  val immersiveBackdrop = if (isDarkBackdrop) colors.background else Color(0xFF1E1F22)
  val overlayPillColor = if (isDarkBackdrop) colors.surface else Color(0xFF1E1F22)

  LaunchedEffect(Unit) {
    fluxRepository.refreshVideosFromServer()
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(immersiveBackdrop)
  ) {
    if (filteredVideos.isEmpty()) {
      // Calm Discord / AMOLED Empty State
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
      ) {
        Box(
          modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(if (isDarkBackdrop) colors.surface else Color(0xFF313338))
            .border(1.dp, colors.outlineVariant.copy(alpha = 0.7f), CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = if (selectedFilter == FluxFeedFilter.SAVED) Icons.Outlined.BookmarkBorder else Icons.Outlined.Videocam,
            contentDescription = null,
            tint = colors.primary,
            modifier = Modifier.size(34.dp)
          )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
          text = if (selectedFilter == FluxFeedFilter.SAVED) strings.noSavedFluxYet else strings.noFluxYet,
          style = KoraTheme.typography.titleLarge,
          color = Color(0xFFF2F3F5),
          fontWeight = FontWeight.Bold,
          textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = if (selectedFilter == FluxFeedFilter.SAVED) strings.noSavedFluxDesc else strings.beFirstToPublishFlux,
          style = KoraTheme.typography.bodyMedium,
          color = Color(0xFFB5BAC1),
          textAlign = TextAlign.Center,
          modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(22.dp))
        if (selectedFilter == FluxFeedFilter.SAVED) {
          Button(
            onClick = { selectedFilter = FluxFeedFilter.ALL },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = colors.primary,
              contentColor = colors.onPrimary
            )
          ) {
            Text(strings.fluxFilterAll, fontWeight = FontWeight.Bold)
          }
        } else {
          Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(
              onClick = {
                if (!isRefreshingFeed) {
                  isRefreshingFeed = true
                  coroutineScope.launch {
                    fluxRepository.refreshVideosFromServer()
                    isRefreshingFeed = false
                  }
                }
              },
              shape = RoundedCornerShape(12.dp),
              border = BorderStroke(1.dp, Color(0xFF3F4147))
            ) {
              if (isRefreshingFeed) {
                CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
              } else {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
              }
              Spacer(modifier = Modifier.width(6.dp))
              Text(strings.refreshFluxFeed, color = Color(0xFFF2F3F5))
            }
            Button(
              onClick = { showCreateSheet = true },
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.buttonColors(
                containerColor = colors.primary,
                contentColor = colors.onPrimary
              )
            ) {
              Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(strings.newFlux, fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    } else {
      // Fullscreen Vertical Pager
      VerticalPager(
        state = pagerState,
        key = { idx -> filteredVideos[idx].id },
        modifier = Modifier.fillMaxSize()
      ) { pageIndex ->
        val rawVideo = filteredVideos[pageIndex]
        val authorProfile = usersById[rawVideo.authorId]
        val liveVideo = rawVideo.copy(
          isAuthorVerified = authorProfile?.isVerified ?: rawVideo.isAuthorVerified,
          authorVerifiedTier = authorProfile?.verifiedTier ?: rawVideo.authorVerifiedTier
        )

        FluxItemPage(
          video = liveVideo.copy(
            authorName = authorProfile?.displayName?.takeIf { it.isNotBlank() } ?: liveVideo.authorName,
            authorHandle = authorProfile?.handle?.takeIf { it.isNotBlank() } ?: liveVideo.authorHandle
          ),
          authorAvatarUrl = authorProfile?.avatarUrl,
          authorNameFontId = authorProfile?.nameFontId ?: "default",
          authorNameThemeId = authorProfile?.nameThemeId ?: "default",
          fluxRepository = fluxRepository,
          isCurrentPage = pagerState.currentPage == pageIndex,
          isMuted = isAudioMuted,
          autoLoadVideos = autoLoadVideos,
          currentUserId = currentUid,
          onToggleLike = {
            coroutineScope.launch {
              val res = fluxRepository.toggleLike(liveVideo.id, currentUid)
              if (res.isFailure) {
                Toast.makeText(
                  context,
                  res.exceptionOrNull()?.localizedMessage ?: strings.errorOccurred,
                  Toast.LENGTH_SHORT
                ).show()
              }
            }
          },
          onToggleReaction = { emoji ->
            coroutineScope.launch {
              val res = fluxRepository.toggleFluxReaction(liveVideo.id, emoji)
              if (res.isFailure) {
                Toast.makeText(
                  context,
                  res.exceptionOrNull()?.localizedMessage ?: strings.errorOccurred,
                  Toast.LENGTH_SHORT
                ).show()
              }
            }
          },
          onOpenReactionSheet = { reactionTargetFlux = liveVideo },
          onEditVideo = { editingFlux = liveVideo },
          onDeleteVideo = { deletingFlux = liveVideo },
          onToggleSave = {
            coroutineScope.launch {
              val res = fluxRepository.toggleSave(liveVideo.id, currentUid)
              if (res.isSuccess) {
                val nowSaved = res.getOrDefault(false)
                Toast.makeText(
                  context,
                  if (nowSaved) strings.videoSavedToast else strings.videoUnsavedToast,
                  Toast.LENGTH_SHORT
                ).show()
              } else {
                Toast.makeText(
                  context,
                  res.exceptionOrNull()?.localizedMessage ?: strings.errorOccurred,
                  Toast.LENGTH_SHORT
                ).show()
              }
            }
          },
          onSaveToGallery = {
            coroutineScope.launch {
              val res = fluxRepository.saveVideoToGallery(context, liveVideo)
              Toast.makeText(
                context,
                if (res.isSuccess) strings.savedToGalleryToast
                else res.exceptionOrNull()?.localizedMessage ?: strings.gallerySaveFailedToast,
                Toast.LENGTH_SHORT
              ).show()
            }
          },
          onOpenComments = { activeCommentsFlux = liveVideo },
          onShare = {
            coroutineScope.launch {
              fluxRepository.incrementShare(liveVideo.id)
            }
            Toast.makeText(context, strings.fluxLinkCopied, Toast.LENGTH_SHORT).show()
          },
          onInspectAuthor = {
            val target = authorProfile ?: UserProfile(
              id = liveVideo.authorId,
              displayName = liveVideo.authorName,
              handle = liveVideo.authorHandle,
              isVerified = liveVideo.isAuthorVerified,
              verifiedTier = liveVideo.authorVerifiedTier
            )
            onInspectUser(target)
          }
        )
      }
    }

    // Clean Single-Row Top Overlay Bar (non-intrusive to video)
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .statusBarsPadding()
        .padding(horizontal = 14.dp, vertical = 10.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      val savedCount = videos.count { it.savedBy.contains(currentUid) }
      Surface(
        shape = RoundedCornerShape(18.dp),
        color = overlayPillColor.copy(alpha = 0.78f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
      ) {
        Row(
          modifier = Modifier.padding(3.dp),
          horizontalArrangement = Arrangement.spacedBy(2.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          FluxOverlayFilterChip(
            label = strings.fluxFilterAll,
            selected = selectedFilter == FluxFeedFilter.ALL,
            onClick = { selectedFilter = FluxFeedFilter.ALL },
            testTag = "flux_filter_all"
          )
          FluxOverlayFilterChip(
            label = if (savedCount > 0) "${strings.fluxFilterSaved} ($savedCount)" else strings.fluxFilterSaved,
            selected = selectedFilter == FluxFeedFilter.SAVED,
            onClick = { selectedFilter = FluxFeedFilter.SAVED },
            testTag = "flux_filter_saved"
          )
        }
      }

      Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(
          onClick = {
            if (!isRefreshingFeed) {
              isRefreshingFeed = true
              coroutineScope.launch {
                fluxRepository.refreshVideosFromServer()
                isRefreshingFeed = false
              }
            }
          },
          modifier = Modifier
            .size(48.dp)
            .testTag("flux_refresh_button")
        ) {
          Box(
            modifier = Modifier
              .size(38.dp)
              .clip(CircleShape)
              .background(overlayPillColor.copy(alpha = 0.78f))
              .border(1.dp, Color.White.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center
          ) {
            if (isRefreshingFeed) {
              CircularProgressIndicator(
                strokeWidth = 2.dp,
                color = colors.primary,
                modifier = Modifier.size(16.dp)
              )
            } else {
              Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = strings.refreshFluxFeed,
                tint = Color(0xFFF2F3F5),
                modifier = Modifier.size(18.dp)
              )
            }
          }
        }

        IconButton(
          onClick = { isAudioMuted = !isAudioMuted },
          modifier = Modifier.size(48.dp)
        ) {
          Box(
            modifier = Modifier
              .size(38.dp)
              .clip(CircleShape)
              .background(overlayPillColor.copy(alpha = 0.78f))
              .border(1.dp, Color.White.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = if (isAudioMuted) Icons.AutoMirrored.Outlined.VolumeOff else Icons.AutoMirrored.Outlined.VolumeUp,
              contentDescription = "Mute",
              tint = Color(0xFFF2F3F5),
              modifier = Modifier.size(19.dp)
            )
          }
        }

        Surface(
          onClick = { showCreateSheet = true },
          shape = RoundedCornerShape(18.dp),
          color = colors.primary,
          contentColor = colors.onPrimary,
          modifier = Modifier.defaultMinSize(minHeight = 38.dp)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.Add,
              contentDescription = strings.newFlux,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = strings.newFlux,
              style = KoraTheme.typography.labelMedium,
              fontWeight = FontWeight.SemiBold,
              maxLines = 1,
              softWrap = false
            )
          }
        }
      }
    }

    // Create Flux Sheet
    if (showCreateSheet) {
      CreateFluxSheet(
        userRepository = userRepository,
        fluxRepository = fluxRepository,
        onDismiss = { showCreateSheet = false },
        onPublished = {
          showCreateSheet = false
          selectedFilter = FluxFeedFilter.ALL
          coroutineScope.launch {
            fluxRepository.refreshVideosFromServer()
            if (pagerState.pageCount > 0) {
              pagerState.animateScrollToPage(0)
            }
          }
          Toast.makeText(context, strings.fluxPublished, Toast.LENGTH_SHORT).show()
        }
      )
    }

    // Comments Bottom Sheet
    if (activeCommentsFlux != null) {
      FluxCommentsSheet(
        video = activeCommentsFlux!!,
        fluxRepository = fluxRepository,
        userRepository = userRepository,
        usersById = usersById,
        onInspectUser = onInspectUser,
        onDismiss = { activeCommentsFlux = null }
      )
    }

    // Full Glossy Emoji Picker Sheet for Flux
    val liveReactionTarget = reactionTargetFlux?.let { target ->
      videos.firstOrNull { it.id == target.id } ?: target
    }
    if (liveReactionTarget != null) {
      val selectedEmojis = remember(liveReactionTarget.reactions, currentUid) {
        liveReactionTarget.reactions.filterValues { it.contains(currentUid) }.keys.toSet()
      }
      WeirdEmojiPickerBottomSheet(
        selectedEmojis = selectedEmojis,
        onEmojiSelected = { emoji ->
          reactionTargetFlux = null
          coroutineScope.launch {
            val res = fluxRepository.toggleFluxReaction(liveReactionTarget.id, emoji)
            if (res.isFailure) {
              Toast.makeText(
                context,
                res.exceptionOrNull()?.localizedMessage ?: strings.errorOccurred,
                Toast.LENGTH_SHORT
              ).show()
            }
          }
        },
        onDismiss = { reactionTargetFlux = null }
      )
    }

    // Author Edit Flux Caption / Audio Dialog
    editingFlux?.let { target ->
      var captionDraft by remember(target.id) { mutableStateOf(target.caption) }
      var audioDraft by remember(target.id) { mutableStateOf(target.audioTitle) }
      var isSavingEdit by remember(target.id) { mutableStateOf(false) }

      AlertDialog(
        onDismissRequest = { if (!isSavingEdit) editingFlux = null },
        title = { Text(strings.editFluxTitle, fontWeight = FontWeight.Bold) },
        text = {
          Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
              value = captionDraft,
              onValueChange = { captionDraft = it },
              label = { Text(strings.fluxCaptionPlaceholder) },
              maxLines = 4,
              modifier = Modifier
                .fillMaxWidth()
                .testTag("edit_flux_caption_input")
            )
            OutlinedTextField(
              value = audioDraft,
              onValueChange = { audioDraft = it },
              label = { Text(strings.editFluxAudioLabel) },
              singleLine = true,
              modifier = Modifier
                .fillMaxWidth()
                .testTag("edit_flux_audio_input")
            )
          }
        },
        confirmButton = {
          Button(
            enabled = captionDraft.isNotBlank() && !isSavingEdit,
            onClick = {
              isSavingEdit = true
              coroutineScope.launch {
                val res = fluxRepository.updateFluxVideo(
                  fluxId = target.id,
                  caption = captionDraft,
                  audioTitle = audioDraft
                )
                isSavingEdit = false
                if (res.isSuccess) {
                  editingFlux = null
                  Toast.makeText(context, strings.contentUpdatedToast, Toast.LENGTH_SHORT).show()
                } else {
                  Toast.makeText(
                    context,
                    res.exceptionOrNull()?.localizedMessage ?: strings.errorOccurred,
                    Toast.LENGTH_SHORT
                  ).show()
                }
              }
            }
          ) {
            Text(strings.confirm)
          }
        },
        dismissButton = {
          TextButton(
            enabled = !isSavingEdit,
            onClick = { editingFlux = null }
          ) {
            Text(strings.cancel)
          }
        }
      )
    }

    // Author Delete Flux Confirmation Dialog
    deletingFlux?.let { target ->
      var isDeleting by remember(target.id) { mutableStateOf(false) }
      AlertDialog(
        onDismissRequest = { if (!isDeleting) deletingFlux = null },
        title = { Text(strings.deleteContentTitle, fontWeight = FontWeight.Bold) },
        text = { Text(strings.deleteContentConfirm) },
        confirmButton = {
          Button(
            enabled = !isDeleting,
            colors = ButtonDefaults.buttonColors(
              containerColor = colors.error,
              contentColor = colors.onError
            ),
            onClick = {
              isDeleting = true
              coroutineScope.launch {
                val res = fluxRepository.deleteFluxVideo(target.id)
                isDeleting = false
                if (res.isSuccess) {
                  deletingFlux = null
                  Toast.makeText(context, strings.contentDeletedToast, Toast.LENGTH_SHORT).show()
                } else {
                  Toast.makeText(
                    context,
                    res.exceptionOrNull()?.localizedMessage ?: strings.errorOccurred,
                    Toast.LENGTH_SHORT
                  ).show()
                }
              }
            }
          ) {
            Text(strings.delete)
          }
        },
        dismissButton = {
          TextButton(
            enabled = !isDeleting,
            onClick = { deletingFlux = null }
          ) {
            Text(strings.cancel)
          }
        }
      )
    }
  }
}

@Composable
private fun FluxOverlayFilterChip(
  label: String,
  selected: Boolean,
  onClick: () -> Unit,
  testTag: String
) {
  val colors = MaterialTheme.colorScheme
  Box(
    modifier = Modifier
      .defaultMinSize(minHeight = 30.dp)
      .clip(RoundedCornerShape(14.dp))
      .background(if (selected) colors.primary else Color.Transparent)
      .clickable(onClick = onClick)
      .padding(horizontal = 12.dp, vertical = 5.dp)
      .testTag(testTag),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = label,
      style = KoraTheme.typography.labelMedium,
      color = if (selected) colors.onPrimary else Color(0xFFDBDEE1),
      fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
      maxLines = 1,
      softWrap = false
    )
  }
}

@Composable
private fun FluxItemPage(
  video: FluxVideo,
  authorAvatarUrl: String?,
  authorNameFontId: String = "default",
  authorNameThemeId: String = "default",
  fluxRepository: FluxRepository,
  isCurrentPage: Boolean,
  isMuted: Boolean,
  autoLoadVideos: Boolean,
  currentUserId: String,
  onToggleLike: () -> Unit,
  onToggleReaction: (String) -> Unit,
  onOpenReactionSheet: () -> Unit,
  onEditVideo: () -> Unit,
  onDeleteVideo: () -> Unit,
  onToggleSave: () -> Unit,
  onSaveToGallery: () -> Unit,
  onOpenComments: () -> Unit,
  onShare: () -> Unit,
  onInspectAuthor: () -> Unit
) {
  val context = LocalContext.current
  val strings = LocalAppStrings.current
  val colors = MaterialTheme.colorScheme
  val isDarkBackdrop = colors.background.red < 0.2f
  val overlaySurface = if (isDarkBackdrop) colors.surface else Color(0xFF1E1F22)
  val scrimColor = if (isDarkBackdrop) colors.background else Color(0xFF1E1F22)

  var isPlaying by remember { mutableStateOf(true) }
  var showHeartBurst by remember { mutableStateOf(false) }
  var showPauseIcon by remember { mutableStateOf(false) }
  var showReactionBar by remember(video.id) { mutableStateOf(false) }
  var showAuthorMenu by remember(video.id) { mutableStateOf(false) }

  val isLiked = video.likedBy.contains(currentUserId)
  val isSaved = video.savedBy.contains(currentUserId)
  val isAuthor = video.authorId == currentUserId

  var resolvedVideoFile by remember(video.id, video.mediaId, video.videoUri) {
    mutableStateOf(fluxRepository.peekCachedFluxFile(video))
  }
  var isBufferingVideo by remember(video.id) { mutableStateOf(false) }
  var downloadProgress by remember(video.id) { mutableFloatStateOf(0f) }
  var videoLoadError by remember(video.id) { mutableStateOf<String?>(null) }
  var manualLoadRequested by remember(video.id) { mutableStateOf(false) }
  var loadRetryCount by remember(video.id) { mutableIntStateOf(0) }

  val directPlayableUri = remember(video.videoUri, context) {
    val raw = video.videoUri
    if (raw.isNullOrBlank()) null
    else {
      val parsed = Uri.parse(raw)
      when (parsed.scheme) {
        "http", "https" -> parsed
        "file" -> parsed.path?.let { java.io.File(it) }?.takeIf { it.exists() && it.canRead() }?.let { parsed }
        "content" -> runCatching {
          context.contentResolver.openFileDescriptor(parsed, "r")?.use { parsed }
        }.getOrNull()
        else -> null
      }
    }
  }

  LaunchedEffect(video.id, video.mediaId, video.mediaChunkCount, resolvedVideoFile, autoLoadVideos, isCurrentPage, manualLoadRequested, loadRetryCount) {
    val shouldLoad = (autoLoadVideos && isCurrentPage) || manualLoadRequested
    if (shouldLoad && resolvedVideoFile == null && !video.mediaId.isNullOrBlank() && video.mediaChunkCount > 0) {
      isBufferingVideo = true
      videoLoadError = null
      val res = fluxRepository.resolvePlayableFluxFile(video) { progress ->
        downloadProgress = progress
      }
      isBufferingVideo = false
      res.onSuccess { file ->
        resolvedVideoFile = file
      }.onFailure { err ->
        videoLoadError = err.localizedMessage ?: strings.fluxVideoLoadFailed
      }
    }
  }

  val playableUri: Uri? = remember(resolvedVideoFile, directPlayableUri) {
    resolvedVideoFile?.let { Uri.fromFile(it) } ?: directPlayableUri
  }

  val infiniteTransition = rememberInfiniteTransition(label = "disc_spin")
  val discRotation by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 360f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 4000, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "disc"
  )

  Box(
    modifier = Modifier
      .fillMaxSize()
      .pointerInput(Unit) {
        detectTapGestures(
          onTap = {
            isPlaying = !isPlaying
            showPauseIcon = true
          },
          onDoubleTap = {
            if (!isLiked) {
              onToggleLike()
            }
            showHeartBurst = true
          }
        )
      }
  ) {
    // Media Playback Layer
    if (playableUri != null) {
      VideoPlayerSurface(
        videoUri = playableUri,
        isPlaying = isPlaying && isCurrentPage,
        isMuted = isMuted
      )
    } else {
      // Dynamic Visual Canvas for Flux
      FluxAmbientVisualCanvas(
        theme = video.gradientTheme,
        isPlaying = isPlaying && isCurrentPage
      )
    }

    if (isBufferingVideo) {
      Surface(
        shape = RoundedCornerShape(999.dp),
        color = overlaySurface.copy(alpha = 0.84f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.14f)),
        modifier = Modifier.align(Alignment.Center)
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          CircularProgressIndicator(
            color = colors.primary,
            strokeWidth = 2.5.dp,
            modifier = Modifier.size(18.dp)
          )
          Text(
            text = "${(downloadProgress * 100).toInt().coerceIn(1, 99)}%",
            style = KoraTheme.typography.labelMedium,
            color = Color(0xFFF2F3F5),
            fontWeight = FontWeight.Bold
          )
        }
      }
    } else if (playableUri == null && !video.mediaId.isNullOrBlank() && video.mediaChunkCount > 0) {
      Surface(
        onClick = {
          manualLoadRequested = true
          loadRetryCount++
        },
        shape = RoundedCornerShape(16.dp),
        color = overlaySurface.copy(alpha = 0.88f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.16f)),
        modifier = Modifier
          .align(Alignment.Center)
          .testTag("flux_retry_load_${video.id}")
      ) {
        Column(
          modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Icon(
            imageVector = if (videoLoadError != null) Icons.Default.Refresh else Icons.Default.PlayArrow,
            contentDescription = strings.fluxTapToLoadVideo,
            tint = colors.primary,
            modifier = Modifier.size(28.dp)
          )
          Text(
            text = if (videoLoadError != null) strings.fluxVideoLoadFailed else strings.fluxTapToLoadVideo,
            style = KoraTheme.typography.labelMedium,
            color = Color(0xFFF2F3F5),
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
          )
        }
      }
    }

    // Soft AMOLED / Charcoal gradient scrims for crisp legibility
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(
          Brush.verticalGradient(
            colors = listOf(
              scrimColor.copy(alpha = 0.48f),
              Color.Transparent,
              Color.Transparent,
              scrimColor.copy(alpha = 0.90f)
            )
          )
        )
    )

    // Center pause/play indicator on tap
    LaunchedEffect(showPauseIcon) {
      if (showPauseIcon) {
        delay(700)
        showPauseIcon = false
      }
    }
    AnimatedVisibility(
      visible = showPauseIcon,
      enter = scaleIn() + fadeIn(),
      exit = scaleOut() + fadeOut(),
      modifier = Modifier.align(Alignment.Center)
    ) {
      Box(
        modifier = Modifier
          .size(64.dp)
          .clip(CircleShape)
          .background(overlaySurface.copy(alpha = 0.80f))
          .border(1.dp, Color.White.copy(alpha = 0.14f), CircleShape),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = if (isPlaying) Icons.Default.PlayArrow else Icons.Default.Pause,
          contentDescription = null,
          tint = Color(0xFFF2F3F5),
          modifier = Modifier.size(32.dp)
        )
      }
    }

    // Double-tap Glossy Heart Burst
    LaunchedEffect(showHeartBurst) {
      if (showHeartBurst) {
        delay(800)
        showHeartBurst = false
      }
    }
    AnimatedVisibility(
      visible = showHeartBurst,
      enter = scaleIn(animationSpec = tween(300, easing = FastOutSlowInEasing)) + fadeIn(),
      exit = scaleOut(animationSpec = tween(300)) + fadeOut(),
      modifier = Modifier.align(Alignment.Center)
    ) {
      GlossyAppleEmoji(
        emoji = "❤️",
        size = 104.dp
      )
    }

    // Bottom Start Creator + Caption + Reactions + Audio Metadata Stack
    Column(
      modifier = Modifier
        .align(Alignment.BottomStart)
        .navigationBarsPadding()
        .fillMaxWidth(0.76f)
        .padding(start = 16.dp, end = 8.dp, bottom = 20.dp),
      verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
      AnimatedVisibility(visible = showReactionBar) {
        val mySelected = remember(video.reactions, currentUserId) {
          video.reactions.filterValues { it.contains(currentUserId) }.keys.toSet()
        }
        IosFloatingReactionBar(
          selectedEmojis = mySelected,
          onEmojiSelected = { emoji ->
            showReactionBar = false
            onToggleReaction(emoji)
          },
          onMoreClick = {
            showReactionBar = false
            onOpenReactionSheet()
          },
          modifier = Modifier.padding(bottom = 4.dp)
        )
      }

      Column(
        modifier = Modifier
          .clip(RoundedCornerShape(10.dp))
          .clickable(onClick = onInspectAuthor)
          .padding(vertical = 2.dp, horizontal = 2.dp)
          .testTag("flux_author_header_${video.id}")
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          StyledDisplayName(
            name = video.authorName,
            nameFontId = authorNameFontId,
            nameThemeId = authorNameThemeId,
            style = KoraTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            fallbackColor = Color(0xFFF2F3F5),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false)
          )
          if (video.isAuthorVerified) {
            VerifiedBadge(
              size = 15.dp,
              tierKey = video.authorVerifiedTier,
              showDialogOnClick = true
            )
          }
        }
        Text(
          text = formatHandleForDisplay(video.authorHandle),
          style = KoraTheme.typography.labelMedium,
          color = Color(0xFFB5BAC1),
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }

      Text(
        text = video.caption,
        style = KoraTheme.typography.bodyMedium,
        color = Color(0xFFDBDEE1),
        maxLines = 3,
        overflow = TextOverflow.Ellipsis,
        lineHeight = 20.sp
      )

      if (video.reactions.isNotEmpty()) {
        ReactionChipsRow(
          reactions = video.reactions,
          currentUid = currentUserId,
          onToggleReaction = onToggleReaction
        )
      }

      Surface(
        shape = RoundedCornerShape(12.dp),
        color = overlaySurface.copy(alpha = 0.80f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.10f))
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
          Icon(
            imageVector = Icons.Outlined.MusicNote,
            contentDescription = null,
            tint = colors.primary,
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = video.audioTitle,
            style = KoraTheme.typography.labelSmall,
            color = Color(0xFFF2F3F5),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }
      }
    }

    // Uniform Right Action Rail
    Column(
      modifier = Modifier
        .align(Alignment.BottomEnd)
        .navigationBarsPadding()
        .padding(end = 12.dp, bottom = 20.dp)
        .width(56.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(9.dp)
    ) {
      AvatarView(
        name = video.authorName,
        avatarUrl = authorAvatarUrl,
        size = 46.dp,
        modifier = Modifier
          .clip(CircleShape)
          .clickable(onClick = onInspectAuthor)
          .testTag("flux_author_avatar_${video.id}")
      )

      FluxActionButton(
        icon = if (isLiked) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
        countText = formatCount(video.likesCount),
        tint = if (isLiked) Color(0xFFF43F5E) else Color(0xFFF2F3F5),
        onClick = onToggleLike
      )

      FluxActionButton(
        icon = Icons.Outlined.AddReaction,
        countText = strings.reactAction,
        tint = if (showReactionBar) colors.primary else Color(0xFFF2F3F5),
        onClick = { showReactionBar = !showReactionBar },
        testTag = "flux_react_button_${video.id}"
      )

      FluxActionButton(
        icon = Icons.Outlined.ChatBubbleOutline,
        countText = formatCount(video.commentsCount),
        tint = Color(0xFFF2F3F5),
        onClick = onOpenComments
      )

      FluxActionButton(
        icon = if (isSaved) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
        countText = formatCount(video.savedBy.size),
        tint = if (isSaved) Color(0xFFF0B232) else Color(0xFFF2F3F5),
        onClick = onToggleSave,
        testTag = "flux_bookmark_button_${video.id}"
      )

      if (video.hasVideoContent()) {
        FluxActionButton(
          icon = Icons.Outlined.Download,
          countText = strings.fluxSaveShort,
          tint = Color(0xFFF2F3F5),
          onClick = onSaveToGallery,
          testTag = "flux_gallery_save_button_${video.id}"
        )
      }

      FluxActionButton(
        icon = Icons.Outlined.Share,
        countText = formatCount(video.sharesCount),
        tint = Color(0xFFF2F3F5),
        onClick = onShare
      )

      if (isAuthor) {
        Box {
          FluxActionButton(
            icon = Icons.Default.MoreVert,
            countText = strings.edit,
            tint = Color(0xFFF2F3F5),
            onClick = { showAuthorMenu = true },
            testTag = "flux_author_menu_button_${video.id}"
          )
          DropdownMenu(
            expanded = showAuthorMenu,
            onDismissRequest = { showAuthorMenu = false }
          ) {
            DropdownMenuItem(
              text = { Text(strings.edit) },
              leadingIcon = {
                Icon(Icons.Outlined.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
              },
              onClick = {
                showAuthorMenu = false
                onEditVideo()
              }
            )
            DropdownMenuItem(
              text = { Text(strings.delete, color = colors.error) },
              leadingIcon = {
                Icon(
                  Icons.Outlined.DeleteOutline,
                  contentDescription = null,
                  tint = colors.error,
                  modifier = Modifier.size(18.dp)
                )
              },
              onClick = {
                showAuthorMenu = false
                onDeleteVideo()
              }
            )
          }
        }
      }

      Box(
        modifier = Modifier
          .size(36.dp)
          .rotate(discRotation)
          .clip(CircleShape)
          .background(overlaySurface)
          .border(1.5.dp, Color.White.copy(alpha = 0.25f), CircleShape),
        contentAlignment = Alignment.Center
      ) {
        Box(
          modifier = Modifier
            .size(13.dp)
            .clip(CircleShape)
            .background(colors.primary)
        )
      }
    }
  }
}

@Composable
private fun FluxActionButton(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  countText: String,
  tint: Color,
  onClick: () -> Unit,
  testTag: String? = null
) {
  val colors = MaterialTheme.colorScheme
  val overlayBg = if (colors.background.red < 0.2f) colors.surface else Color(0xFF1E1F22)
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier
      .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
      .clickable(onClick = onClick)
      .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
  ) {
    Box(
      modifier = Modifier
        .size(40.dp)
        .clip(CircleShape)
        .background(overlayBg.copy(alpha = 0.76f))
        .border(1.dp, Color.White.copy(alpha = 0.12f), CircleShape),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = countText,
        tint = tint,
        modifier = Modifier.size(20.dp)
      )
    }
    Spacer(modifier = Modifier.height(2.dp))
    Text(
      text = countText,
      style = KoraTheme.typography.labelSmall.copy(fontSize = 11.sp),
      color = Color(0xFFF2F3F5),
      fontWeight = FontWeight.SemiBold,
      maxLines = 1,
      softWrap = false,
      overflow = TextOverflow.Ellipsis
    )
  }
}

private fun formatCount(num: Int): String {
  return when {
    num >= 1_000_000 -> String.format(Locale.getDefault(), "%.1fM", num / 1_000_000f)
    num >= 1_000 -> String.format(Locale.getDefault(), "%.1fK", num / 1_000f)
    else -> num.toString()
  }
}

/**
 * Media3 ExoPlayer surface for smooth full-bleed looping Flux playback.
 */
@androidx.annotation.OptIn(UnstableApi::class)
@Composable
private fun VideoPlayerSurface(
  videoUri: Uri,
  isPlaying: Boolean,
  isMuted: Boolean
) {
  val context = LocalContext.current
  val exoPlayer = remember(videoUri) {
    ExoPlayer.Builder(context).build().apply {
      setMediaItem(MediaItem.fromUri(videoUri))
      repeatMode = Player.REPEAT_MODE_ONE
      prepare()
    }
  }

  LaunchedEffect(exoPlayer, isPlaying, isMuted) {
    exoPlayer.volume = if (isMuted) 0f else 1f
    exoPlayer.playWhenReady = isPlaying
  }

  DisposableEffect(exoPlayer) {
    onDispose {
      exoPlayer.release()
    }
  }

  AndroidView(
    modifier = Modifier.fillMaxSize(),
    factory = { ctx ->
      PlayerView(ctx).apply {
        player = exoPlayer
        useController = false
        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
      }
    },
    update = { playerView ->
      if (playerView.player !== exoPlayer) {
        playerView.player = exoPlayer
      }
    }
  )
}

/**
 * Ambient live visual canvas for Flux
 */
@Composable
private fun FluxAmbientVisualCanvas(
  theme: String,
  isPlaying: Boolean
) {
  val colors = MaterialTheme.colorScheme
  val infiniteTransition = rememberInfiniteTransition(label = "ambient")
  val pulse by infiniteTransition.animateFloat(
    initialValue = 0.8f,
    targetValue = 1.25f,
    animationSpec = infiniteRepeatable(
      animation = tween(2800, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pulse"
  )

  val gradientColors = when (theme) {
    "NITRO" -> listOf(Color(0xFF2B2D31), Color(0xFFFF73FA).copy(alpha = 0.75f), Color(0xFF1E1F22))
    "CYBER" -> listOf(Color(0xFF111214), Color(0xFF00E5FF).copy(alpha = 0.65f), colors.primary.copy(alpha = 0.8f))
    "EMERALD" -> listOf(Color(0xFF1E1F22), Color(0xFF23A55A).copy(alpha = 0.65f), Color(0xFF0F172A))
    "SOLAR" -> listOf(Color(0xFF232428), Color(0xFFF0B232).copy(alpha = 0.7f), Color(0xFFE11D48).copy(alpha = 0.5f))
    else -> listOf(Color(0xFF111214), colors.primary.copy(alpha = 0.7f), Color(0xFF1F2330))
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Brush.radialGradient(gradientColors, radius = 900f * pulse)),
    contentAlignment = Alignment.Center
  ) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      val canvasWidth = size.width
      val canvasHeight = size.height
      val barCount = 28
      val barWidth = canvasWidth / (barCount * 1.8f)

      for (i in 0 until barCount) {
        val barX = i * (barWidth * 1.8f) + barWidth
        val factor = kotlin.math.sin(i.toDouble() * 0.45 + (pulse * 3.0)).toFloat()
        val barHeight = (canvasHeight * 0.18f * (0.35f + kotlin.math.abs(factor))).coerceAtLeast(10f)

        drawRoundRect(
          color = Color.White.copy(alpha = 0.25f + (0.2f * kotlin.math.abs(factor))),
          topLeft = Offset(barX, (canvasHeight / 2f) - (barHeight / 2f)),
          size = androidx.compose.ui.geometry.Size(barWidth, barHeight),
          cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
        )
      }
    }
  }
}

/**
 * Modal BottomSheet for publishing a new Flux video
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateFluxSheet(
  userRepository: UserRepository,
  fluxRepository: FluxRepository,
  onDismiss: () -> Unit,
  onPublished: () -> Unit
) {
  val context = LocalContext.current
  val strings = LocalAppStrings.current
  val colors = MaterialTheme.colorScheme
  val coroutineScope = rememberCoroutineScope()
  var isPublishing by remember { mutableStateOf(false) }
  val sheetState = rememberModalBottomSheetState(
    skipPartiallyExpanded = true,
    confirmValueChange = { !isPublishing }
  )
  val currentUid = userRepository.requireUserId()

  var selectedVideoUri by remember { mutableStateOf<Uri?>(null) }
  var selectedVideoError by remember { mutableStateOf<String?>(null) }
  var captionText by remember { mutableStateOf("") }
  var audioText by remember { mutableStateOf("") }
  var selectedTheme by remember { mutableStateOf("BLURPLE") }
  var uploadProgress by remember { mutableFloatStateOf(0f) }

  // Zero-permission Android Photo/Video Picker with immediate 25 MB validation
  val videoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
  ) { uri: Uri? ->
    if (uri != null) {
      val fileSize = runCatching {
        context.contentResolver.query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)?.use { c ->
          val col = c.getColumnIndex(OpenableColumns.SIZE)
          if (c.moveToFirst() && col >= 0) c.getLong(col) else null
        } ?: context.contentResolver.openFileDescriptor(uri, "r")?.use { it.statSize }
      }.getOrNull() ?: 0L
      if (fileSize > ChatMediaRepository.MAX_MEDIA_BYTES) {
        selectedVideoUri = null
        selectedVideoError = strings.fluxVideoTooLargeInline
      } else {
        selectedVideoError = null
        selectedVideoUri = uri
      }
    }
  }

  ModalBottomSheet(
    onDismissRequest = { if (!isPublishing) onDismiss() },
    sheetState = sheetState,
    containerColor = colors.surface,
    contentColor = colors.onSurface
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .widthIn(max = 600.dp)
        .align(Alignment.CenterHorizontally)
        .padding(horizontal = 20.dp, vertical = 12.dp)
        .navigationBarsPadding()
        .imePadding()
    ) {
      // Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = strings.publishFlux,
          style = KoraTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
          color = colors.onSurface
        )
        IconButton(onClick = { if (!isPublishing) onDismiss() }, enabled = !isPublishing) {
          Icon(Icons.Default.Close, contentDescription = strings.cancel, tint = colors.onSurfaceVariant)
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Video Source Selection Area
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceVariant.copy(alpha = 0.55f)),
        border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier
          .fillMaxWidth()
          .clickable {
            videoPickerLauncher.launch(
              PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
            )
          }
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(48.dp)
              .clip(CircleShape)
              .background(colors.primary.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Outlined.Videocam,
              contentDescription = null,
              tint = colors.primary,
              modifier = Modifier.size(24.dp)
            )
          }
          Spacer(modifier = Modifier.width(14.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = if (selectedVideoUri != null) strings.videoSelectedDevice else strings.selectVideo,
              style = KoraTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = if (selectedVideoUri != null) Color(0xFF23A55A) else colors.onSurface
            )
            Text(
              text = if (selectedVideoUri != null) selectedVideoUri!!.lastPathSegment ?: "video.mp4" else strings.tapToChooseVideo,
              style = KoraTheme.typography.bodySmall,
              color = colors.onSurfaceVariant,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }
        }
      }

      if (selectedVideoError != null) {
        Spacer(modifier = Modifier.height(8.dp))
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = colors.errorContainer.copy(alpha = 0.85f),
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            text = selectedVideoError!!,
            style = KoraTheme.typography.bodySmall,
            color = colors.onErrorContainer,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Caption Field
      OutlinedTextField(
        value = captionText,
        onValueChange = { captionText = it },
        placeholder = { Text(strings.fluxCaptionPlaceholder, color = colors.onSurfaceVariant) },
        colors = OutlinedTextFieldDefaults.colors(
          focusedContainerColor = colors.surfaceVariant.copy(alpha = 0.4f),
          unfocusedContainerColor = colors.surfaceVariant.copy(alpha = 0.3f),
          focusedBorderColor = colors.primary,
          unfocusedBorderColor = colors.outlineVariant,
          focusedTextColor = colors.onSurface,
          unfocusedTextColor = colors.onSurface
        ),
        shape = RoundedCornerShape(12.dp),
        maxLines = 3,
        modifier = Modifier.fillMaxWidth()
      )

      // Hashtag Suggestion Chips
      Spacer(modifier = Modifier.height(8.dp))
      LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        val suggestedTags = listOf("#flux", "#kora", "#creative", "#lofi", "#tech", "#highlights")
        items(suggestedTags) { tag ->
          Text(
            text = tag,
            style = KoraTheme.typography.labelSmall,
            color = colors.primary,
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(colors.primary.copy(alpha = 0.12f))
              .clickable {
                if (!captionText.contains(tag)) {
                  captionText = if (captionText.isBlank()) tag else "$captionText $tag"
                }
              }
              .padding(horizontal = 8.dp, vertical = 4.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Audio Title Field
      OutlinedTextField(
        value = audioText,
        onValueChange = { audioText = it },
        placeholder = { Text(strings.fluxAudioPlaceholder, color = colors.onSurfaceVariant) },
        leadingIcon = {
          Icon(Icons.Outlined.GraphicEq, contentDescription = null, tint = colors.onSurfaceVariant)
        },
        colors = OutlinedTextFieldDefaults.colors(
          focusedContainerColor = colors.surfaceVariant.copy(alpha = 0.4f),
          unfocusedContainerColor = colors.surfaceVariant.copy(alpha = 0.3f),
          focusedBorderColor = colors.primary,
          unfocusedBorderColor = colors.outlineVariant,
          focusedTextColor = colors.onSurface,
          unfocusedTextColor = colors.onSurface
        ),
        shape = RoundedCornerShape(12.dp),
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
      )

      Spacer(modifier = Modifier.height(14.dp))

      // Visual Theme Selector
      Text(
        text = strings.fluxAmbientStyle,
        style = KoraTheme.typography.labelMedium,
        color = colors.onSurfaceVariant,
        fontWeight = FontWeight.SemiBold
      )
      Spacer(modifier = Modifier.height(6.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        val themes = listOf("BLURPLE" to "Indigo", "NITRO" to "Magenta", "CYBER" to "Cyber", "EMERALD" to "Emerald")
        themes.forEach { (key, label) ->
          val isSelected = selectedTheme == key
          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(8.dp))
              .background(if (isSelected) colors.primary else colors.surfaceVariant.copy(alpha = 0.5f))
              .border(
                1.dp,
                if (isSelected) colors.primary else colors.outlineVariant.copy(alpha = 0.5f),
                RoundedCornerShape(8.dp)
              )
              .clickable { selectedTheme = key }
              .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = label,
              style = KoraTheme.typography.labelSmall,
              color = if (isSelected) colors.onPrimary else colors.onSurface,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
              maxLines = 1
            )
          }
        }
      }

      AnimatedVisibility(visible = isPublishing && selectedVideoUri != null) {
        Column(modifier = Modifier.padding(top = 14.dp)) {
          Text(
            text = String.format(
              strings.uploadingFluxVideoProgress,
              (uploadProgress * 100).toInt().coerceIn(1, 99)
            ),
            style = KoraTheme.typography.labelMedium,
            color = colors.primary,
            fontWeight = FontWeight.SemiBold
          )
          Spacer(modifier = Modifier.height(6.dp))
          LinearProgressIndicator(
            progress = { uploadProgress.coerceIn(0.05f, 1f) },
            modifier = Modifier
              .fillMaxWidth()
              .height(5.dp)
              .clip(RoundedCornerShape(3.dp))
          )
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Publish Action Button
      Button(
        onClick = {
          if (captionText.isBlank() && selectedVideoUri == null) {
            captionText = "New Flux clip #flux"
          }
          isPublishing = true
          uploadProgress = 0.05f
          coroutineScope.launch {
            try {
              val userProfile = userRepository.getCachedProfile(currentUid)
                ?: runCatching { userRepository.getUserProfile(currentUid) }.getOrNull()
              fluxRepository.publishFlux(
                authorId = currentUid,
                authorName = userProfile?.displayName ?: "Creator",
                authorHandle = userProfile?.handle ?: "@creator",
                isAuthorVerified = userProfile?.isVerified ?: false,
                authorVerifiedTier = userProfile?.verifiedTier ?: "BLUE",
                caption = captionText.trim(),
                videoUri = selectedVideoUri?.toString(),
                audioTitle = audioText.trim(),
                gradientTheme = selectedTheme,
                onUploadProgress = { uploadProgress = it }
              )
              isPublishing = false
              uploadProgress = 0f
              onPublished()
            } catch (e: Exception) {
              isPublishing = false
              uploadProgress = 0f
              val msg = e.message.orEmpty()
              val display = if (msg.contains("25 MB", ignoreCase = true)) {
                strings.fileTooLargeLimit
              } else {
                e.localizedMessage ?: strings.errorOccurred
              }
              Toast.makeText(
                context,
                display,
                Toast.LENGTH_LONG
              ).show()
            }
          }
        },
        enabled = !isPublishing,
        colors = ButtonDefaults.buttonColors(
          containerColor = colors.primary,
          contentColor = colors.onPrimary
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
          .fillMaxWidth()
          .defaultMinSize(minHeight = 50.dp)
          .testTag("publish_flux_button")
      ) {
        if (isPublishing) {
          CircularProgressIndicator(color = colors.onPrimary, modifier = Modifier.size(20.dp))
        } else {
          Text(strings.publishFlux, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
      }
    }
  }
}

/**
 * Comments Bottom Sheet for Flux
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FluxCommentsSheet(
  video: FluxVideo,
  fluxRepository: FluxRepository,
  userRepository: UserRepository,
  usersById: Map<String, UserProfile>,
  onInspectUser: (UserProfile) -> Unit = {},
  onDismiss: () -> Unit
) {
  val context = LocalContext.current
  val strings = LocalAppStrings.current
  val colors = MaterialTheme.colorScheme
  val coroutineScope = rememberCoroutineScope()
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
  val currentUid = userRepository.requireUserId()

  val comments by fluxRepository.observeComments(video.id).collectAsState(initial = emptyList())
  var newCommentText by remember { mutableStateOf("") }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = colors.surface,
    contentColor = colors.onSurface
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .widthIn(max = 600.dp)
        .align(Alignment.CenterHorizontally)
        .heightIn(min = 360.dp, max = 540.dp)
        .padding(horizontal = 16.dp, vertical = 8.dp)
        .navigationBarsPadding()
        .imePadding()
    ) {
      // Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "${strings.fluxCommentsTitle} (${comments.size})",
          style = KoraTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = colors.onSurface
        )
        IconButton(onClick = onDismiss) {
          Icon(Icons.Default.Close, contentDescription = strings.cancel, tint = colors.onSurfaceVariant)
        }
      }

      HorizontalDivider(color = colors.outlineVariant.copy(alpha = 0.5f), thickness = 1.dp, modifier = Modifier.padding(vertical = 6.dp))

      // Comments List
      LazyColumn(
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
      ) {
        if (comments.isEmpty()) {
          item {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = strings.noCommentsYet,
                style = KoraTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant
              )
            }
          }
        } else {
          items(comments, key = { it.id }) { comment ->
            val authorProfile = usersById[comment.authorId]
              FluxCommentItem(
                comment = comment.copy(
                  isAuthorVerified = authorProfile?.isVerified ?: comment.isAuthorVerified,
                  authorVerifiedTier = authorProfile?.verifiedTier ?: comment.authorVerifiedTier
                ),
                authorAvatarUrl = authorProfile?.avatarUrl,
                authorNameFontId = authorProfile?.nameFontId ?: "default",
                authorNameThemeId = authorProfile?.nameThemeId ?: "default",
                onInspectAuthor = {
                val target = authorProfile ?: UserProfile(
                  id = comment.authorId,
                  displayName = comment.authorName,
                  handle = comment.authorHandle,
                  isVerified = comment.isAuthorVerified,
                  verifiedTier = comment.authorVerifiedTier
                )
                onDismiss()
                onInspectUser(target)
              }
            )
          }
        }
      }

      // Input Bar
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        OutlinedTextField(
          value = newCommentText,
          onValueChange = { newCommentText = it },
          placeholder = { Text(strings.addFluxComment, color = colors.onSurfaceVariant) },
          colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = colors.surfaceVariant.copy(alpha = 0.4f),
            unfocusedContainerColor = colors.surfaceVariant.copy(alpha = 0.3f),
            focusedBorderColor = colors.primary,
            unfocusedBorderColor = colors.outlineVariant,
            focusedTextColor = colors.onSurface,
            unfocusedTextColor = colors.onSurface
          ),
          shape = RoundedCornerShape(20.dp),
          maxLines = 3,
          modifier = Modifier
            .weight(1f)
            .testTag("flux_comment_input")
        )

        Spacer(modifier = Modifier.width(8.dp))

        IconButton(
          onClick = {
            val text = newCommentText.trim()
            if (text.isNotBlank()) {
              coroutineScope.launch {
                try {
                  val profile = userRepository.getCachedProfile(currentUid)
                    ?: runCatching { userRepository.getUserProfile(currentUid) }.getOrNull()
                  fluxRepository.addComment(
                    fluxId = video.id,
                    authorId = currentUid,
                    authorName = profile?.displayName ?: "User",
                    authorHandle = profile?.handle ?: "@user",
                    isAuthorVerified = profile?.isVerified ?: false,
                    authorVerifiedTier = profile?.verifiedTier ?: "BLUE",
                    content = text
                  )
                  newCommentText = ""
                } catch (e: Exception) {
                  Toast.makeText(
                    context,
                    e.localizedMessage ?: strings.errorOccurred,
                    Toast.LENGTH_SHORT
                  ).show()
                }
              }
            }
          },
          enabled = newCommentText.isNotBlank(),
          modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(
              if (newCommentText.isNotBlank()) colors.primary
              else colors.surfaceVariant
            )
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.Send,
            contentDescription = strings.send,
            tint = if (newCommentText.isNotBlank()) colors.onPrimary else colors.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
          )
        }
      }
    }
  }
}

@Composable
private fun FluxCommentItem(
  comment: FluxComment,
  authorAvatarUrl: String? = null,
  authorNameFontId: String = "default",
  authorNameThemeId: String = "default",
  onInspectAuthor: () -> Unit = {}
) {
  val colors = MaterialTheme.colorScheme
  val formattedTime = remember(comment.timestamp) {
    try {
      SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(comment.timestamp))
    } catch (_: Exception) {
      ""
    }
  }

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(10.dp))
      .clickable(onClick = onInspectAuthor)
      .padding(4.dp),
    verticalAlignment = Alignment.Top
  ) {
    AvatarView(name = comment.authorName, avatarUrl = authorAvatarUrl, size = 36.dp)
    Spacer(modifier = Modifier.width(10.dp))
    Column(modifier = Modifier.weight(1f)) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        StyledDisplayName(
          name = comment.authorName,
          nameFontId = authorNameFontId,
          nameThemeId = authorNameThemeId,
          style = KoraTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
          fallbackColor = colors.onSurface,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier.weight(1f, fill = false)
        )
        if (comment.isAuthorVerified) {
          Spacer(modifier = Modifier.width(4.dp))
          VerifiedBadge(size = 12.dp, tierKey = comment.authorVerifiedTier)
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = formattedTime,
          style = KoraTheme.typography.labelSmall,
          color = colors.onSurfaceVariant
        )
      }
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = comment.content,
        style = KoraTheme.typography.bodyMedium,
        color = colors.onSurfaceVariant
      )
    }
  }
}

