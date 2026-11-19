package com.example.ui.screens

import android.net.Uri
import android.widget.MediaController
import android.widget.Toast
import android.widget.VideoView
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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.model.FluxComment
import com.example.data.model.FluxVideo
import com.example.data.repository.FluxRepository
import com.example.data.repository.UserRepository
import com.example.ui.components.AvatarView
import com.example.ui.components.VerifiedBadge
import com.example.ui.theme.DiscordBlurple
import com.example.ui.theme.DiscordBlurpleLight
import com.example.ui.theme.DiscordBorder
import com.example.ui.theme.DiscordDarkDeepest
import com.example.ui.theme.DiscordDarkInput
import com.example.ui.theme.DiscordDarkPrimary
import com.example.ui.theme.DiscordDarkSecondary
import com.example.ui.theme.DiscordNitroPink
import com.example.ui.theme.DiscordRed
import com.example.ui.theme.DiscordTextMuted
import com.example.ui.theme.DiscordTextNormal
import com.example.ui.theme.DiscordTextPrimary
import com.example.ui.theme.KoraTheme
import com.example.ui.util.LocalAppStrings
import com.example.ui.util.formatHandleForDisplay
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FluxScreen(
  fluxRepository: FluxRepository,
  userRepository: UserRepository,
  onLanguageToggle: () -> Unit
) {
  val context = LocalContext.current
  val strings = LocalAppStrings.current
  val coroutineScope = rememberCoroutineScope()
  val currentUid = userRepository.requireUserId()

  val videos by fluxRepository.observeVideos().collectAsState(initial = emptyList())
  val pagerState = rememberPagerState(pageCount = { videos.size })

  var showCreateSheet by remember { mutableStateOf(false) }
  var activeCommentsFlux by remember { mutableStateOf<FluxVideo?>(null) }
  var isAudioMuted by remember { mutableStateOf(false) }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Color.Black)
  ) {
    if (videos.isEmpty()) {
      // Empty state
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
      ) {
        Icon(
          imageVector = Icons.Default.Videocam,
          contentDescription = null,
          tint = DiscordBlurple,
          modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
          text = strings.noFluxYet,
          style = KoraTheme.typography.titleLarge,
          color = DiscordTextPrimary,
          fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = strings.beFirstToPublishFlux,
          style = KoraTheme.typography.bodyMedium,
          color = DiscordTextMuted,
          modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
          onClick = { showCreateSheet = true },
          colors = ButtonDefaults.buttonColors(containerColor = DiscordBlurple)
        ) {
          Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text(strings.newFlux, color = Color.White)
        }
      }
    } else {
      // Fullscreen Vertical Pager
      VerticalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize()
      ) { pageIndex ->
        val video = videos[pageIndex]
        FluxItemPage(
          video = video,
          isCurrentPage = pagerState.currentPage == pageIndex,
          isMuted = isAudioMuted,
          currentUserId = currentUid,
          onToggleLike = { fluxRepository.toggleLike(video.id, currentUid) },
          onToggleSave = { fluxRepository.toggleSave(video.id, currentUid) },
          onOpenComments = { activeCommentsFlux = video },
          onShare = {
            fluxRepository.incrementShare(video.id)
            Toast.makeText(context, "Flux link copied to clipboard!", Toast.LENGTH_SHORT).show()
          }
        )
      }
    }

    // Top Overlay Bar
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .statusBarsPadding()
        .padding(horizontal = 16.dp, vertical = 12.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
          text = "FLUX",
          style = KoraTheme.typography.titleLarge.copy(
            fontSize = 22.sp,
            letterSpacing = 2.sp
          ),
          color = Color.White,
          fontWeight = FontWeight.Black
        )
        Spacer(modifier = Modifier.width(6.dp))
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(DiscordBlurple)
            .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
          Text(
            text = "LIVE",
            style = KoraTheme.typography.labelSmall.copy(fontSize = 9.sp),
            color = Color.White,
            fontWeight = FontWeight.Bold
          )
        }
      }

      Row(verticalAlignment = Alignment.CenterVertically) {
        // Mute / Unmute
        IconButton(
          onClick = { isAudioMuted = !isAudioMuted },
          modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.5f))
        ) {
          Icon(
            imageVector = if (isAudioMuted) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
            contentDescription = "Mute",
            tint = Color.White,
            modifier = Modifier.size(20.dp)
          )
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Create Flux Button
        Row(
          modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(DiscordBlurple)
            .clickable { showCreateSheet = true }
            .padding(horizontal = 14.dp, vertical = 8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.Add,
            contentDescription = strings.newFlux,
            tint = Color.White,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = strings.newFlux,
            style = KoraTheme.typography.labelMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold
          )
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
        onDismiss = { activeCommentsFlux = null }
      )
    }
  }
}

@Composable
private fun FluxItemPage(
  video: FluxVideo,
  isCurrentPage: Boolean,
  isMuted: Boolean,
  currentUserId: String,
  onToggleLike: () -> Unit,
  onToggleSave: () -> Unit,
  onOpenComments: () -> Unit,
  onShare: () -> Unit
) {
  var isPlaying by remember { mutableStateOf(true) }
  var showHeartBurst by remember { mutableStateOf(false) }
  var showPauseIcon by remember { mutableStateOf(false) }

  val isLiked = video.likedBy.contains(currentUserId)
  val isSaved = video.savedBy.contains(currentUserId)

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
    if (!video.videoUri.isNullOrBlank()) {
      VideoPlayerSurface(
        videoUri = Uri.parse(video.videoUri),
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

    // Subtle dark gradient overlays for maximum contrast
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(
          Brush.verticalGradient(
            colors = listOf(
              Color.Black.copy(alpha = 0.6f),
              Color.Transparent,
              Color.Transparent,
              Color.Black.copy(alpha = 0.85f)
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
          .size(68.dp)
          .clip(CircleShape)
          .background(Color.Black.copy(alpha = 0.6f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = if (isPlaying) Icons.Default.PlayArrow else Icons.Default.Pause,
          contentDescription = null,
          tint = Color.White,
          modifier = Modifier.size(36.dp)
        )
      }
    }

    // Double-tap Heart Burst
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
      Icon(
        imageVector = Icons.Default.Favorite,
        contentDescription = "Heart",
        tint = DiscordRed,
        modifier = Modifier.size(110.dp)
      )
    }

    // Bottom Left Details & Creator info
    Column(
      modifier = Modifier
        .align(Alignment.BottomStart)
        .fillMaxWidth(0.78f)
        .padding(horizontal = 16.dp, vertical = 24.dp)
    ) {
      // Author handle & badge
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 6.dp)
      ) {
        Text(
          text = video.authorName,
          style = KoraTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = Color.White
        )
        if (video.isAuthorVerified) {
          Spacer(modifier = Modifier.width(6.dp))
          VerifiedBadge(
            size = 16.dp,
            tierKey = video.authorVerifiedTier,
            showDialogOnClick = true
          )
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = formatHandleForDisplay(video.authorHandle),
          style = KoraTheme.typography.bodySmall,
          color = DiscordTextMuted
        )
      }

      // Caption
      Text(
        text = video.caption,
        style = KoraTheme.typography.bodyMedium,
        color = Color.White,
        maxLines = 3,
        overflow = TextOverflow.Ellipsis,
        lineHeight = 20.sp,
        modifier = Modifier.padding(bottom = 10.dp)
      )

      // Audio marquee track pill
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
          .clip(RoundedCornerShape(14.dp))
          .background(Color.Black.copy(alpha = 0.5f))
          .padding(horizontal = 10.dp, vertical = 5.dp)
      ) {
        Icon(
          imageVector = Icons.Default.MusicNote,
          contentDescription = null,
          tint = DiscordBlurpleLight,
          modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = video.audioTitle,
          style = KoraTheme.typography.labelSmall,
          color = Color.White,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }
    }

    // Right Action Bar
    Column(
      modifier = Modifier
        .align(Alignment.BottomEnd)
        .padding(end = 12.dp, bottom = 28.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // Author Avatar with mini follow indicator
      Box(contentAlignment = Alignment.BottomCenter) {
        AvatarView(
          name = video.authorName,
          size = 46.dp
        )
      }

      // Like Button
      FluxActionButton(
        icon = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
        count = video.likesCount,
        tint = if (isLiked) DiscordRed else Color.White,
        onClick = onToggleLike
      )

      // Comments Button
      FluxActionButton(
        icon = Icons.Default.ChatBubble,
        count = video.commentsCount,
        tint = Color.White,
        onClick = onOpenComments
      )

      // Save / Bookmark Button
      FluxActionButton(
        icon = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
        count = video.savedBy.size,
        tint = if (isSaved) Color(0xFFF0B232) else Color.White,
        onClick = onToggleSave
      )

      // Share Button
      FluxActionButton(
        icon = Icons.Default.Share,
        count = video.sharesCount,
        tint = Color.White,
        onClick = onShare
      )

      // Rotating Audio Vinyl Disc
      Box(
        modifier = Modifier
          .size(42.dp)
          .rotate(discRotation)
          .clip(CircleShape)
          .background(DiscordDarkDeepest)
          .border(2.dp, DiscordBorder, CircleShape),
        contentAlignment = Alignment.Center
      ) {
        Box(
          modifier = Modifier
            .size(16.dp)
            .clip(CircleShape)
            .background(DiscordBlurple)
        )
      }
    }
  }
}

@Composable
private fun FluxActionButton(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  count: Int,
  tint: Color,
  onClick: () -> Unit
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier.clickable(onClick = onClick)
  ) {
    Box(
      modifier = Modifier
        .size(44.dp)
        .clip(CircleShape)
        .background(Color.Black.copy(alpha = 0.45f)),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = tint,
        modifier = Modifier.size(24.dp)
      )
    }
    Spacer(modifier = Modifier.height(4.dp))
    Text(
      text = formatCount(count),
      style = KoraTheme.typography.labelSmall,
      color = Color.White,
      fontWeight = FontWeight.Bold
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
 * Android VideoView integration for playing device-selected or URI videos
 */
@Composable
private fun VideoPlayerSurface(
  videoUri: Uri,
  isPlaying: Boolean,
  isMuted: Boolean
) {
  AndroidView(
    modifier = Modifier.fillMaxSize(),
    factory = { ctx ->
      VideoView(ctx).apply {
        setVideoURI(videoUri)
        setOnPreparedListener { mp ->
          mp.isLooping = true
          if (isMuted) mp.setVolume(0f, 0f) else mp.setVolume(1f, 1f)
          if (isPlaying) mp.start()
        }
      }
    },
    update = { videoView ->
      if (isPlaying) {
        if (!videoView.isPlaying) videoView.start()
      } else {
        if (videoView.isPlaying) videoView.pause()
      }
    }
  )
}

/**
 * Discord aesthetic live ambient visual canvas for Flux
 */
@Composable
private fun FluxAmbientVisualCanvas(
  theme: String,
  isPlaying: Boolean
) {
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
    "NITRO" -> listOf(Color(0xFF2B2D31), DiscordNitroPink.copy(alpha = 0.75f), Color(0xFF1E1F22))
    "CYBER" -> listOf(Color(0xFF111214), Color(0xFF00E5FF).copy(alpha = 0.65f), DiscordBlurple.copy(alpha = 0.8f))
    "EMERALD" -> listOf(Color(0xFF1E1F22), Color(0xFF23A55A).copy(alpha = 0.65f), Color(0xFF0F172A))
    "SOLAR" -> listOf(Color(0xFF232428), Color(0xFFF0B232).copy(alpha = 0.7f), Color(0xFFE11D48).copy(alpha = 0.5f))
    else -> listOf(DiscordDarkDeepest, DiscordBlurple.copy(alpha = 0.7f), DiscordDarkPrimary)
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Brush.radialGradient(gradientColors, radius = 900f * pulse)),
    contentAlignment = Alignment.Center
  ) {
    // Stylized Animated Waveform Spectrum
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
  val strings = LocalAppStrings.current
  val coroutineScope = rememberCoroutineScope()
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  val currentUid = userRepository.requireUserId()

  var selectedVideoUri by remember { mutableStateOf<Uri?>(null) }
  var captionText by remember { mutableStateOf("") }
  var audioText by remember { mutableStateOf("") }
  var selectedTheme by remember { mutableStateOf("BLURPLE") }
  var isPublishing by remember { mutableStateOf(false) }

  // Zero-permission Android Photo/Video Picker!
  val videoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
  ) { uri: Uri? ->
    if (uri != null) {
      selectedVideoUri = uri
    }
  }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = DiscordDarkSecondary,
    contentColor = DiscordTextPrimary
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
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
          color = DiscordTextPrimary
        )
        IconButton(onClick = onDismiss) {
          Icon(Icons.Default.Close, contentDescription = "Close", tint = DiscordTextMuted)
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Video Source Selection Area
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DiscordDarkInput),
        border = androidx.compose.foundation.BorderStroke(1.dp, DiscordBorder),
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
              .background(DiscordBlurple.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Videocam,
              contentDescription = null,
              tint = DiscordBlurple,
              modifier = Modifier.size(24.dp)
            )
          }
          Spacer(modifier = Modifier.width(14.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = if (selectedVideoUri != null) "Video selected from device ✓" else strings.selectVideo,
              style = KoraTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = if (selectedVideoUri != null) Color(0xFF23A55A) else DiscordTextPrimary
            )
            Text(
              text = if (selectedVideoUri != null) selectedVideoUri!!.lastPathSegment ?: "video.mp4" else "Tap to choose a short video from gallery",
              style = KoraTheme.typography.bodySmall,
              color = DiscordTextMuted,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Caption Field
      OutlinedTextField(
        value = captionText,
        onValueChange = { captionText = it },
        placeholder = { Text(strings.fluxCaptionPlaceholder, color = DiscordTextMuted) },
        colors = OutlinedTextFieldDefaults.colors(
          focusedContainerColor = DiscordDarkInput,
          unfocusedContainerColor = DiscordDarkInput,
          focusedBorderColor = DiscordBlurple,
          unfocusedBorderColor = DiscordBorder,
          focusedTextColor = DiscordTextPrimary,
          unfocusedTextColor = DiscordTextNormal
        ),
        shape = RoundedCornerShape(12.dp),
        maxLines = 3,
        modifier = Modifier.fillMaxWidth()
      )

      // Hashtag Suggestion Chips
      Spacer(modifier = Modifier.height(8.dp))
      LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        val suggestedTags = listOf("#flux", "#gaming", "#discord", "#lofi", "#tech", "#clutch")
        items(suggestedTags) { tag ->
          Text(
            text = tag,
            style = KoraTheme.typography.labelSmall,
            color = DiscordBlurpleLight,
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(DiscordDarkDeepest)
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
        placeholder = { Text(strings.fluxAudioPlaceholder, color = DiscordTextMuted) },
        leadingIcon = {
          Icon(Icons.Default.GraphicEq, contentDescription = null, tint = DiscordTextMuted)
        },
        colors = OutlinedTextFieldDefaults.colors(
          focusedContainerColor = DiscordDarkInput,
          unfocusedContainerColor = DiscordDarkInput,
          focusedBorderColor = DiscordBlurple,
          unfocusedBorderColor = DiscordBorder,
          focusedTextColor = DiscordTextPrimary,
          unfocusedTextColor = DiscordTextNormal
        ),
        shape = RoundedCornerShape(12.dp),
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
      )

      Spacer(modifier = Modifier.height(14.dp))

      // Visual Theme Selector (if no video selected or ambient backdrop)
      Text(
        text = "Ambient Visual Style",
        style = KoraTheme.typography.labelMedium,
        color = DiscordTextMuted,
        fontWeight = FontWeight.SemiBold
      )
      Spacer(modifier = Modifier.height(6.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        val themes = listOf("BLURPLE" to "Blurple", "NITRO" to "Nitro", "CYBER" to "Cyber", "EMERALD" to "Emerald")
        themes.forEach { (key, label) ->
          val isSelected = selectedTheme == key
          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(8.dp))
              .background(if (isSelected) DiscordBlurple else DiscordDarkInput)
              .border(
                1.dp,
                if (isSelected) DiscordBlurpleLight else DiscordBorder,
                RoundedCornerShape(8.dp)
              )
              .clickable { selectedTheme = key }
              .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = label,
              style = KoraTheme.typography.labelSmall,
              color = if (isSelected) Color.White else DiscordTextNormal,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
          }
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
          coroutineScope.launch {
            val userProfile = userRepository.getUserProfile(currentUid)
            fluxRepository.publishFlux(
              authorId = currentUid,
              authorName = userProfile?.displayName ?: "Creator",
              authorHandle = userProfile?.handle ?: "@creator",
              isAuthorVerified = userProfile?.isVerified ?: false,
              authorVerifiedTier = userProfile?.verifiedTier ?: "GOLD",
              caption = captionText.trim(),
              videoUri = selectedVideoUri?.toString(),
              audioTitle = audioText.trim(),
              gradientTheme = selectedTheme
            )
            isPublishing = false
            onPublished()
          }
        },
        enabled = !isPublishing,
        colors = ButtonDefaults.buttonColors(containerColor = DiscordBlurple),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
          .fillMaxWidth()
          .height(50.dp)
          .testTag("publish_flux_button")
      ) {
        if (isPublishing) {
          CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
        } else {
          Text(strings.publishFlux, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
  onDismiss: () -> Unit
) {
  val strings = LocalAppStrings.current
  val coroutineScope = rememberCoroutineScope()
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
  val currentUid = userRepository.requireUserId()

  val comments by fluxRepository.observeComments(video.id).collectAsState(initial = emptyList())
  var newCommentText by remember { mutableStateOf("") }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = DiscordDarkSecondary,
    contentColor = DiscordTextPrimary
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
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
          color = DiscordTextPrimary
        )
        IconButton(onClick = onDismiss) {
          Icon(Icons.Default.Close, contentDescription = "Close", tint = DiscordTextMuted)
        }
      }

      HorizontalDivider(color = DiscordBorder, thickness = 1.dp, modifier = Modifier.padding(vertical = 6.dp))

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
                text = "No comments yet. Say something friendly!",
                style = KoraTheme.typography.bodyMedium,
                color = DiscordTextMuted
              )
            }
          }
        } else {
          items(comments, key = { it.id }) { comment ->
            FluxCommentItem(comment = comment)
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
          placeholder = { Text(strings.addFluxComment, color = DiscordTextMuted) },
          colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = DiscordDarkInput,
            unfocusedContainerColor = DiscordDarkInput,
            focusedBorderColor = DiscordBlurple,
            unfocusedBorderColor = DiscordBorder,
            focusedTextColor = DiscordTextPrimary,
            unfocusedTextColor = DiscordTextNormal
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
                val profile = userRepository.getUserProfile(currentUid)
                fluxRepository.addComment(
                  fluxId = video.id,
                  authorId = currentUid,
                  authorName = profile?.displayName ?: "User",
                  authorHandle = profile?.handle ?: "@user",
                  isAuthorVerified = profile?.isVerified ?: false,
                  authorVerifiedTier = profile?.verifiedTier ?: "GOLD",
                  content = text
                )
                newCommentText = ""
              }
            }
          },
          enabled = newCommentText.isNotBlank(),
          modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(
              if (newCommentText.isNotBlank()) DiscordBlurple
              else Color(0xFF4E5058).copy(alpha = 0.5f)
            )
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.Send,
            contentDescription = "Send",
            tint = if (newCommentText.isNotBlank()) Color.White else DiscordTextMuted,
            modifier = Modifier.size(18.dp)
          )
        }
      }
    }
  }
}

@Composable
private fun FluxCommentItem(comment: FluxComment) {
  val formattedTime = remember(comment.timestamp) {
    try {
      SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(comment.timestamp))
    } catch (_: Exception) {
      ""
    }
  }

  Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.Top
  ) {
    AvatarView(name = comment.authorName, size = 36.dp)
    Spacer(modifier = Modifier.width(10.dp))
    Column(modifier = Modifier.weight(1f)) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
          text = comment.authorName,
          style = KoraTheme.typography.labelMedium,
          fontWeight = FontWeight.Bold,
          color = DiscordTextPrimary
        )
        if (comment.isAuthorVerified) {
          Spacer(modifier = Modifier.width(4.dp))
          VerifiedBadge(size = 12.dp, tierKey = comment.authorVerifiedTier)
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = formattedTime,
          style = KoraTheme.typography.labelSmall,
          color = DiscordTextMuted
        )
      }
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = comment.content,
        style = KoraTheme.typography.bodyMedium,
        color = DiscordTextNormal
      )
    }
  }
}
