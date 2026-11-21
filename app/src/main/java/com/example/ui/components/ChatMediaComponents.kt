package com.example.ui.components

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Poll
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.example.data.model.Message
import com.example.data.repository.ChatMediaRepository
import com.example.ui.util.LocalAppStrings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@kotlin.OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatMediaAttachmentSheet(
  onDismiss: () -> Unit,
  onPickPhoto: () -> Unit,
  onPickVideo: () -> Unit,
  onPickFile: () -> Unit,
  onCreatePoll: () -> Unit = {}
) {
  val strings = LocalAppStrings.current
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = MaterialTheme.colorScheme.surface,
    contentColor = MaterialTheme.colorScheme.onSurface
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .navigationBarsPadding()
        .padding(horizontal = 20.dp, vertical = 12.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Text(
        text = strings.attachMedia,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
      )

      AttachmentOptionTile(
        icon = Icons.Default.Image,
        title = strings.attachPhoto,
        subtitle = "PNG / JPG • Original crisp quality",
        accentColor = Color(0xFF4F46E5),
        testTag = "attach_photo_option",
        onClick = {
          onDismiss()
          onPickPhoto()
        }
      )

      AttachmentOptionTile(
        icon = Icons.Default.Videocam,
        title = strings.attachHdVideo,
        subtitle = "MP4 / MOV • Full HD up to 25 MB • Plays inline",
        accentColor = Color(0xFF0EA5E9),
        testTag = "attach_video_option",
        onClick = {
          onDismiss()
          onPickVideo()
        }
      )

      AttachmentOptionTile(
        icon = Icons.Default.Description,
        title = strings.attachFile,
        subtitle = "PDF, ZIP, Audio, Docs • Up to 25 MB",
        accentColor = Color(0xFF10B981),
        testTag = "attach_file_option",
        onClick = {
          onDismiss()
          onPickFile()
        }
      )

      AttachmentOptionTile(
        icon = Icons.Default.Poll,
        title = strings.createPollAction,
        subtitle = "Live vote with instant percentages",
        accentColor = Color(0xFFF59E0B),
        testTag = "attach_poll_option",
        onClick = {
          onDismiss()
          onCreatePoll()
        }
      )

      Spacer(modifier = Modifier.height(10.dp))
    }
  }
}

@Composable
private fun AttachmentOptionTile(
  icon: ImageVector,
  title: String,
  subtitle: String,
  accentColor: Color,
  testTag: String,
  onClick: () -> Unit
) {
  Surface(
    onClick = onClick,
    shape = RoundedCornerShape(16.dp),
    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
    modifier = Modifier
      .fillMaxWidth()
      .defaultMinSize(minHeight = 64.dp)
      .testTag(testTag)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(44.dp)
          .clip(CircleShape)
          .background(accentColor.copy(alpha = 0.16f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = title,
          tint = accentColor,
          modifier = Modifier.size(22.dp)
        )
      }
      Spacer(modifier = Modifier.width(14.dp))
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = title,
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
        Text(
          text = subtitle,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }
    }
  }
}

@Composable
fun MediaMessageBubbleContent(
  conversationId: String,
  message: Message,
  chatMediaRepository: ChatMediaRepository,
  isInlineVideoPlaying: Boolean,
  onPlayInlineVideo: () -> Unit,
  onStopInlineVideo: () -> Unit,
  onOpenFullscreenVideo: (File) -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val strings = LocalAppStrings.current
  val scope = rememberCoroutineScope()

  var resolvedFile by remember(message.id, message.mediaId, message.localMediaUri) {
    mutableStateOf(chatMediaRepository.peekCachedMediaFile(message))
  }
  var isLoading by remember(message.id) { mutableStateOf(false) }
  var progress by remember(message.id) { mutableFloatStateOf(0f) }
  var loadError by remember(message.id) { mutableStateOf<String?>(null) }
  var retryCounter by remember(message.id) { mutableIntStateOf(0) }
  var showImageLightbox by remember { mutableStateOf(false) }

  LaunchedEffect(message.id, message.mediaId, retryCounter) {
    if (resolvedFile == null && !message.mediaId.isNullOrBlank()) {
      isLoading = true
      loadError = null
      val res = chatMediaRepository.resolveMediaFile(
        conversationId = conversationId,
        message = message,
        onProgress = { progress = it }
      )
      isLoading = false
      res.onSuccess { file ->
        resolvedFile = file
      }.onFailure { err ->
        loadError = err.localizedMessage ?: strings.mediaDownloadFailed
      }
    }
  }

  Column(modifier = modifier.fillMaxWidth()) {
    when (message.mediaType) {
      "image" -> {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 140.dp, max = 260.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.45f))
            .clickable(enabled = resolvedFile != null) { showImageLightbox = true }
            .testTag("chat_media_image_${message.id}"),
          contentAlignment = Alignment.Center
        ) {
          if (resolvedFile != null) {
            AsyncImage(
              model = resolvedFile,
              contentDescription = message.mediaFileName ?: strings.attachPhoto,
              contentScale = ContentScale.Crop,
              modifier = Modifier.fillMaxSize()
            )
            // Quick save button pill
            Surface(
              onClick = {
                scope.launch {
                  val res = chatMediaRepository.saveToDeviceGalleryOrDownloads(
                    file = resolvedFile!!,
                    mediaType = "image",
                    mimeType = message.mediaMimeType,
                    fileName = message.mediaFileName
                  )
                  Toast.makeText(
                    context,
                    if (res.isSuccess) strings.savedImageToast else strings.gallerySaveFailedToast,
                    Toast.LENGTH_SHORT
                  ).show()
                }
              },
              shape = CircleShape,
              color = Color.Black.copy(alpha = 0.55f),
              modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(8.dp)
                .size(38.dp)
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(
                  imageVector = Icons.Default.Download,
                  contentDescription = strings.saveFileAction,
                  tint = Color.White,
                  modifier = Modifier.size(18.dp)
                )
              }
            }
          } else {
            MediaDownloadPlaceholder(
              isLoading = isLoading,
              progress = progress,
              errorText = loadError,
              fallbackLabel = strings.attachPhoto,
              onRetry = { retryCounter++ }
            )
          }
        }
      }

      "video" -> {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 10f)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF0A0D18))
            .testTag("chat_media_video_${message.id}"),
          contentAlignment = Alignment.Center
        ) {
          val file = resolvedFile
          if (isInlineVideoPlaying && file != null) {
            // Live hardware-accelerated ExoPlayer embedded directly inside the chat bubble!
            EmbeddedChatExoPlayer(
              file = file,
              onStopClick = onStopInlineVideo,
              onFullscreenClick = { onOpenFullscreenVideo(file) },
              onSaveClick = {
                scope.launch {
                  val res = chatMediaRepository.saveToDeviceGalleryOrDownloads(
                    file = file,
                    mediaType = "video",
                    mimeType = message.mediaMimeType,
                    fileName = message.mediaFileName
                  )
                  Toast.makeText(
                    context,
                    if (res.isSuccess) strings.savedToGalleryToast else strings.gallerySaveFailedToast,
                    Toast.LENGTH_SHORT
                  ).show()
                }
              }
            )
          } else {
            val thumbnailBitmap by produceState<Bitmap?>(initialValue = null, key1 = file?.absolutePath) {
              value = file?.let { extractVideoFrameBitmap(it) }
            }

            if (thumbnailBitmap != null) {
              Image(
                bitmap = thumbnailBitmap!!.asImageBitmap(),
                contentDescription = message.mediaFileName ?: strings.attachHdVideo,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
              )
            } else {
              Box(
                modifier = Modifier
                  .fillMaxSize()
                  .background(
                    Brush.linearGradient(
                      listOf(Color(0xFF141B30), Color(0xFF0E1324))
                    )
                  )
              )
            }

            // Subtle dark scrim for legibility
            Box(
              modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.25f))
                .clickable(enabled = file != null || loadError != null) {
                  if (file != null) {
                    onPlayInlineVideo()
                  } else if (loadError != null) {
                    retryCounter++
                  }
                }
            )

            // Top badges: HD + File size
            Row(
              modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp),
              horizontalArrangement = Arrangement.spacedBy(6.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color.Black.copy(alpha = 0.65f)
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Icon(
                    imageVector = Icons.Default.HighQuality,
                    contentDescription = null,
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(13.dp)
                  )
                  Spacer(modifier = Modifier.width(3.dp))
                  Text(
                    text = strings.hdBadge,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                  )
                }
              }
              if (message.mediaSizeBytes > 0L) {
                Surface(
                  shape = RoundedCornerShape(6.dp),
                  color = Color.Black.copy(alpha = 0.55f)
                ) {
                  Text(
                    text = ChatMediaRepository.formatBytes(message.mediaSizeBytes),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                  )
                }
              }
            }

            // Center Play button or Loading/Retry
            if (file != null) {
              Surface(
                onClick = onPlayInlineVideo,
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.92f),
                border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.8f)),
                modifier = Modifier
                  .size(56.dp)
                  .testTag("play_inline_video_${message.id}")
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = strings.tapToPlayInline,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                  )
                }
              }

              // Bottom bar: Duration + Quick Fullscreen & Save
              Row(
                modifier = Modifier
                  .align(Alignment.BottomCenter)
                  .fillMaxWidth()
                  .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = Color.Black.copy(alpha = 0.65f)
                ) {
                  Text(
                    text = ChatMediaRepository.formatDuration(message.mediaDurationMs),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                  )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                  Surface(
                    onClick = {
                      scope.launch {
                        val res = chatMediaRepository.saveToDeviceGalleryOrDownloads(
                          file = file,
                          mediaType = "video",
                          mimeType = message.mediaMimeType,
                          fileName = message.mediaFileName
                        )
                        Toast.makeText(
                          context,
                          if (res.isSuccess) strings.savedToGalleryToast else strings.gallerySaveFailedToast,
                          Toast.LENGTH_SHORT
                        ).show()
                      }
                    },
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.65f),
                    modifier = Modifier.size(34.dp)
                  ) {
                    Box(contentAlignment = Alignment.Center) {
                      Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = strings.saveToGallery,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                      )
                    }
                  }
                  Surface(
                    onClick = { onOpenFullscreenVideo(file) },
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.65f),
                    modifier = Modifier.size(34.dp)
                  ) {
                    Box(contentAlignment = Alignment.Center) {
                      Icon(
                        imageVector = Icons.Default.Fullscreen,
                        contentDescription = strings.fullscreenVideo,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                      )
                    }
                  }
                }
              }
            } else {
              MediaDownloadPlaceholder(
                isLoading = isLoading,
                progress = progress,
                errorText = loadError,
                fallbackLabel = strings.attachHdVideo,
                onRetry = { retryCounter++ }
              )
            }
          }
        }
      }

      "audio" -> {
        VoiceNoteBubblePlayer(
          messageId = message.id,
          file = resolvedFile,
          durationHintMs = message.mediaDurationMs,
          isLoading = isLoading,
          progress = progress,
          loadError = loadError,
          onRetry = { retryCounter++ }
        )
      }

      "file" -> {
        val ext = (message.mediaFileName?.substringAfterLast('.', "") ?: "FILE")
          .uppercase()
          .take(5)
          .ifBlank { "FILE" }

        Surface(
          shape = RoundedCornerShape(14.dp),
          color = MaterialTheme.colorScheme.surface.copy(alpha = 0.68f),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("chat_media_file_${message.id}")
        ) {
          Column(modifier = Modifier.padding(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                modifier = Modifier.size(42.dp)
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                      imageVector = Icons.Default.InsertDriveFile,
                      contentDescription = null,
                      tint = MaterialTheme.colorScheme.primary,
                      modifier = Modifier.size(16.dp)
                    )
                    Text(
                      text = ext,
                      style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                      fontWeight = FontWeight.ExtraBold,
                      color = MaterialTheme.colorScheme.primary,
                      maxLines = 1
                    )
                  }
                }
              }
              Spacer(modifier = Modifier.width(10.dp))
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = message.mediaFileName ?: "Attachment",
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.SemiBold,
                  color = MaterialTheme.colorScheme.onSurface,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
                Text(
                  text = ChatMediaRepository.formatBytes(message.mediaSizeBytes),
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }

            if (isLoading) {
              Spacer(modifier = Modifier.height(8.dp))
              LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier
                  .fillMaxWidth()
                  .height(4.dp)
                  .clip(RoundedCornerShape(2.dp))
              )
            } else if (loadError != null) {
              Spacer(modifier = Modifier.height(8.dp))
              OutlinedButton(
                onClick = { retryCounter++ },
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                modifier = Modifier.fillMaxWidth()
              ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(strings.retry, style = MaterialTheme.typography.labelMedium)
              }
            } else if (resolvedFile != null) {
              Spacer(modifier = Modifier.height(8.dp))
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                OutlinedButton(
                  onClick = {
                    val res = chatMediaRepository.openFileWithSystem(resolvedFile!!, message.mediaMimeType)
                    if (res.isFailure) {
                      Toast.makeText(context, strings.noAppToOpenFile, Toast.LENGTH_SHORT).show()
                    }
                  },
                  contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                  modifier = Modifier
                    .weight(1f)
                    .defaultMinSize(minHeight = 40.dp)
                ) {
                  Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(15.dp))
                  Spacer(modifier = Modifier.width(5.dp))
                  Text(strings.openFileAction, style = MaterialTheme.typography.labelMedium)
                }

                Button(
                  onClick = {
                    scope.launch {
                      val res = chatMediaRepository.saveToDeviceGalleryOrDownloads(
                        file = resolvedFile!!,
                        mediaType = "file",
                        mimeType = message.mediaMimeType,
                        fileName = message.mediaFileName
                      )
                      Toast.makeText(
                        context,
                        if (res.isSuccess) strings.savedFileToast else strings.gallerySaveFailedToast,
                        Toast.LENGTH_SHORT
                      ).show()
                    }
                  },
                  contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                  modifier = Modifier
                    .weight(1f)
                    .defaultMinSize(minHeight = 40.dp)
                ) {
                  Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(15.dp))
                  Spacer(modifier = Modifier.width(5.dp))
                  Text(strings.saveFileAction, style = MaterialTheme.typography.labelMedium)
                }
              }
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(6.dp))
  }

  if (showImageLightbox && resolvedFile != null) {
    ImageLightboxDialog(
      file = resolvedFile!!,
      fileName = message.mediaFileName,
      mimeType = message.mediaMimeType,
      chatMediaRepository = chatMediaRepository,
      onDismiss = { showImageLightbox = false }
    )
  }
}

@Composable
private fun MediaDownloadPlaceholder(
  isLoading: Boolean,
  progress: Float,
  errorText: String?,
  fallbackLabel: String,
  onRetry: () -> Unit
) {
  val strings = LocalAppStrings.current
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
    modifier = Modifier.padding(16.dp)
  ) {
    if (errorText != null) {
      Text(
        text = errorText,
        style = MaterialTheme.typography.labelSmall,
        color = Color.White,
        maxLines = 2
      )
      Spacer(modifier = Modifier.height(8.dp))
      Button(
        onClick = onRetry,
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
      ) {
        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(strings.retry)
      }
    } else if (isLoading) {
      CircularProgressIndicator(
        progress = { progress.coerceIn(0.05f, 1f) },
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(34.dp)
      )
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = strings.downloadingMedia,
        style = MaterialTheme.typography.labelSmall,
        color = Color.White.copy(alpha = 0.85f)
      )
    } else {
      Text(
        text = fallbackLabel,
        style = MaterialTheme.typography.labelMedium,
        color = Color.White.copy(alpha = 0.8f)
      )
    }
  }
}

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
private fun EmbeddedChatExoPlayer(
  file: File,
  onStopClick: () -> Unit,
  onFullscreenClick: () -> Unit,
  onSaveClick: () -> Unit
) {
  val context = LocalContext.current
  val strings = LocalAppStrings.current
  var isMuted by remember { mutableStateOf(false) }

  val exoPlayer = remember(file.absolutePath) {
    ExoPlayer.Builder(context).build().apply {
      setMediaItem(MediaItem.fromUri(Uri.fromFile(file)))
      repeatMode = Player.REPEAT_MODE_OFF
      prepare()
      playWhenReady = true
    }
  }

  LaunchedEffect(isMuted) {
    exoPlayer.volume = if (isMuted) 0f else 1f
  }

  DisposableEffect(exoPlayer) {
    onDispose {
      exoPlayer.release()
    }
  }

  Box(modifier = Modifier.fillMaxSize()) {
    AndroidView(
      factory = { ctx ->
        PlayerView(ctx).apply {
          player = exoPlayer
          useController = true
          controllerAutoShow = true
          resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
        }
      },
      modifier = Modifier
        .fillMaxSize()
        .testTag("embedded_exo_player_view")
    )

    // Floating top control pills inside the chat video bubble
    Row(
      modifier = Modifier
        .align(Alignment.TopEnd)
        .padding(8.dp),
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      Surface(
        onClick = { isMuted = !isMuted },
        shape = CircleShape,
        color = Color.Black.copy(alpha = 0.62f),
        modifier = Modifier.size(32.dp)
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(
            imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(16.dp)
          )
        }
      }

      Surface(
        onClick = onSaveClick,
        shape = CircleShape,
        color = Color.Black.copy(alpha = 0.62f),
        modifier = Modifier.size(32.dp)
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(
            imageVector = Icons.Default.Download,
            contentDescription = strings.saveToGallery,
            tint = Color.White,
            modifier = Modifier.size(16.dp)
          )
        }
      }

      Surface(
        onClick = onFullscreenClick,
        shape = CircleShape,
        color = Color.Black.copy(alpha = 0.62f),
        modifier = Modifier.size(32.dp)
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(
            imageVector = Icons.Default.Fullscreen,
            contentDescription = strings.fullscreenVideo,
            tint = Color.White,
            modifier = Modifier.size(18.dp)
          )
        }
      }

      Surface(
        onClick = onStopClick,
        shape = CircleShape,
        color = Color.Black.copy(alpha = 0.62f),
        modifier = Modifier.size(32.dp)
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = strings.cancel,
            tint = Color.White,
            modifier = Modifier.size(16.dp)
          )
        }
      }
    }
  }
}

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun FullscreenVideoPlayerDialog(
  file: File,
  fileName: String?,
  mimeType: String?,
  chatMediaRepository: ChatMediaRepository,
  onDismiss: () -> Unit
) {
  val context = LocalContext.current
  val strings = LocalAppStrings.current
  val scope = rememberCoroutineScope()

  val exoPlayer = remember(file.absolutePath) {
    ExoPlayer.Builder(context).build().apply {
      setMediaItem(MediaItem.fromUri(Uri.fromFile(file)))
      prepare()
      playWhenReady = true
    }
  }

  DisposableEffect(exoPlayer) {
    onDispose {
      exoPlayer.release()
    }
  }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(Color.Black)
    ) {
      AndroidView(
        factory = { ctx ->
          PlayerView(ctx).apply {
            player = exoPlayer
            useController = true
            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
          }
        },
        modifier = Modifier.fillMaxSize()
      )

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .statusBarsPadding()
          .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(
          onClick = onDismiss,
          modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.55f))
        ) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = strings.cancel,
            tint = Color.White
          )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
          text = fileName ?: strings.attachHdVideo,
          style = MaterialTheme.typography.titleSmall,
          color = Color.White,
          fontWeight = FontWeight.SemiBold,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(
          onClick = {
            scope.launch {
              val res = chatMediaRepository.saveToDeviceGalleryOrDownloads(
                file = file,
                mediaType = "video",
                mimeType = mimeType,
                fileName = fileName
              )
              Toast.makeText(
                context,
                if (res.isSuccess) strings.savedToGalleryToast else strings.gallerySaveFailedToast,
                Toast.LENGTH_SHORT
              ).show()
            }
          },
          modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.55f))
        ) {
          Icon(
            imageVector = Icons.Default.Download,
            contentDescription = strings.saveToGallery,
            tint = Color.White
          )
        }
      }
    }
  }
}

@Composable
private fun ImageLightboxDialog(
  file: File,
  fileName: String?,
  mimeType: String?,
  chatMediaRepository: ChatMediaRepository,
  onDismiss: () -> Unit
) {
  val context = LocalContext.current
  val strings = LocalAppStrings.current
  val scope = rememberCoroutineScope()

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(Color.Black)
    ) {
      AsyncImage(
        model = file,
        contentDescription = fileName ?: strings.attachPhoto,
        contentScale = ContentScale.Fit,
        modifier = Modifier.fillMaxSize()
      )

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .statusBarsPadding()
          .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(
          onClick = onDismiss,
          modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.55f))
        ) {
          Icon(Icons.Default.Close, contentDescription = strings.cancel, tint = Color.White)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
          text = fileName ?: strings.attachPhoto,
          style = MaterialTheme.typography.titleSmall,
          color = Color.White,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(
          onClick = {
            scope.launch {
              val res = chatMediaRepository.saveToDeviceGalleryOrDownloads(
                file = file,
                mediaType = "image",
                mimeType = mimeType,
                fileName = fileName
              )
              Toast.makeText(
                context,
                if (res.isSuccess) strings.savedImageToast else strings.gallerySaveFailedToast,
                Toast.LENGTH_SHORT
              ).show()
            }
          },
          modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.55f))
        ) {
          Icon(Icons.Default.Download, contentDescription = strings.saveFileAction, tint = Color.White)
        }
      }
    }
  }
}

private suspend fun extractVideoFrameBitmap(file: File): Bitmap? = withContext(Dispatchers.IO) {
  val retriever = MediaMetadataRetriever()
  try {
    retriever.setDataSource(file.absolutePath)
    retriever.getFrameAtTime(0L, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
  } catch (_: Exception) {
    null
  } finally {
    runCatching { retriever.release() }
  }
}

@Composable
private fun VoiceNoteBubblePlayer(
  messageId: String,
  file: File?,
  durationHintMs: Long,
  isLoading: Boolean,
  progress: Float,
  loadError: String?,
  onRetry: () -> Unit
) {
  val context = LocalContext.current
  val strings = LocalAppStrings.current
  var isPlaying by remember(messageId) { mutableStateOf(false) }
  var speedIndex by remember(messageId) { mutableIntStateOf(0) }
  val speeds = remember { listOf(1.0f, 1.5f, 2.0f) }
  val speedLabels = remember { listOf("1x", "1.5x", "2x") }
  var currentPositionMs by remember(messageId) { mutableLongStateOf(0L) }
  var totalDurationMs by remember(messageId, durationHintMs) { mutableLongStateOf(durationHintMs.coerceAtLeast(0L)) }

  val exoPlayer = remember(file?.absolutePath) {
    file?.let { audioFile ->
      ExoPlayer.Builder(context).build().apply {
        setMediaItem(MediaItem.fromUri(Uri.fromFile(audioFile)))
        prepare()
      }
    }
  }

  DisposableEffect(exoPlayer) {
    val listener = object : Player.Listener {
      override fun onPlaybackStateChanged(playbackState: Int) {
        if (exoPlayer != null && playbackState == Player.STATE_READY && exoPlayer.duration > 0L) {
          totalDurationMs = exoPlayer.duration
        }
        if (playbackState == Player.STATE_ENDED) {
          isPlaying = false
          currentPositionMs = 0L
          exoPlayer?.pause()
          exoPlayer?.seekTo(0L)
        }
      }

      override fun onIsPlayingChanged(playing: Boolean) {
        isPlaying = playing
      }
    }
    exoPlayer?.addListener(listener)
    onDispose {
      exoPlayer?.removeListener(listener)
      exoPlayer?.release()
    }
  }

  LaunchedEffect(exoPlayer, speedIndex) {
    exoPlayer?.playbackParameters = PlaybackParameters(speeds[speedIndex])
  }

  LaunchedEffect(exoPlayer, isPlaying) {
    while (exoPlayer != null && isPlaying) {
      currentPositionMs = exoPlayer.currentPosition.coerceAtLeast(0L)
      if (exoPlayer.duration > 0L) {
        totalDurationMs = exoPlayer.duration
      }
      delay(120L)
    }
  }

  Surface(
    shape = RoundedCornerShape(14.dp),
    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.65f),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
    modifier = Modifier
      .fillMaxWidth()
      .testTag("chat_media_audio_$messageId")
  ) {
    Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
      ) {
        if (file != null && exoPlayer != null) {
          IconButton(
            onClick = {
              if (isPlaying) {
                exoPlayer.pause()
                isPlaying = false
              } else {
                exoPlayer.play()
                isPlaying = true
              }
            },
            modifier = Modifier
              .size(44.dp)
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.primary)
              .testTag("voice_note_play_toggle_$messageId")
          ) {
            Icon(
              imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
              contentDescription = strings.voiceNoteLabel,
              tint = MaterialTheme.colorScheme.onPrimary,
              modifier = Modifier.size(22.dp)
            )
          }
        } else if (loadError != null) {
          IconButton(
            onClick = onRetry,
            modifier = Modifier
              .size(44.dp)
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.error.copy(alpha = 0.16f))
          ) {
            Icon(
              imageVector = Icons.Default.Refresh,
              contentDescription = strings.retry,
              tint = MaterialTheme.colorScheme.error
            )
          }
        } else {
          Box(
            modifier = Modifier.size(44.dp),
            contentAlignment = Alignment.Center
          ) {
            if (isLoading) {
              CircularProgressIndicator(
                progress = { progress.coerceIn(0.08f, 1f) },
                strokeWidth = 2.5.dp,
                modifier = Modifier.size(24.dp)
              )
            } else {
              Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = strings.voiceNoteLabel,
                tint = MaterialTheme.colorScheme.primary
              )
            }
          }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
          val maxDur = totalDurationMs.coerceAtLeast(1000L).toFloat()
          Slider(
            value = currentPositionMs.toFloat().coerceIn(0f, maxDur),
            onValueChange = { target ->
              currentPositionMs = target.toLong()
              exoPlayer?.seekTo(target.toLong())
            },
            valueRange = 0f..maxDur,
            enabled = exoPlayer != null,
            colors = SliderDefaults.colors(
              thumbColor = MaterialTheme.colorScheme.primary,
              activeTrackColor = MaterialTheme.colorScheme.primary,
              inactiveTrackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)
            ),
            modifier = Modifier
              .fillMaxWidth()
              .height(26.dp)
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            val timeText = if (isPlaying || currentPositionMs > 0L) {
              "${ChatMediaRepository.formatDuration(currentPositionMs)} / ${ChatMediaRepository.formatDuration(totalDurationMs)}"
            } else {
              ChatMediaRepository.formatDuration(totalDurationMs)
            }
            Text(
              text = timeText,
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              fontWeight = FontWeight.Medium
            )

            Surface(
              onClick = { speedIndex = (speedIndex + 1) % speeds.size },
              shape = RoundedCornerShape(999.dp),
              color = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
              modifier = Modifier.testTag("voice_note_speed_$messageId")
            ) {
              Text(
                text = speedLabels[speedIndex],
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
              )
            }
          }
        }
      }
    }
  }
}

@Composable
fun PollMessageCard(
  message: Message,
  currentUid: String,
  onVoteOption: (Int) -> Unit,
  modifier: Modifier = Modifier
) {
  val strings = LocalAppStrings.current
  val colors = MaterialTheme.colorScheme
  val question = message.pollQuestion?.takeIf { it.isNotBlank() } ?: message.content
  val totalVotes = remember(message.pollVotes) {
    message.pollVotes.values.sumOf { it.size }
  }

  Surface(
    shape = RoundedCornerShape(14.dp),
    color = colors.surface.copy(alpha = 0.72f),
    border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.45f)),
    modifier = modifier
      .fillMaxWidth()
      .testTag("poll_card_${message.id}")
  ) {
    Column(
      modifier = Modifier.padding(12.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.Poll,
          contentDescription = null,
          tint = colors.primary,
          modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = question,
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold,
          color = colors.onSurface
        )
      }

      message.pollOptions.forEachIndexed { index, optionText ->
        val voters = message.pollVotes[index.toString()].orEmpty()
        val isVotedByMe = voters.contains(currentUid)
        val fraction = if (totalVotes > 0) voters.size.toFloat() / totalVotes.toFloat() else 0f
        val percent = (fraction * 100f).toInt()

        Surface(
          onClick = { onVoteOption(index) },
          shape = RoundedCornerShape(10.dp),
          color = colors.surfaceVariant.copy(alpha = 0.55f),
          border = BorderStroke(
            width = if (isVotedByMe) 1.5.dp else 1.dp,
            color = if (isVotedByMe) colors.primary else colors.outlineVariant.copy(alpha = 0.45f)
          ),
          modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 46.dp)
            .testTag("poll_option_${message.id}_$index")
        ) {
          Box(modifier = Modifier.fillMaxWidth()) {
            if (fraction > 0f) {
              Box(
                modifier = Modifier
                  .matchParentSize()
                  .fillMaxWidth(fraction)
              ) {
                Box(
                  modifier = Modifier
                    .fillMaxSize()
                    .background(
                      if (isVotedByMe) colors.primary.copy(alpha = 0.22f)
                      else colors.onSurface.copy(alpha = 0.08f)
                    )
                )
              }
            }
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 10.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
              ) {
                if (isVotedByMe) {
                  Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(16.dp)
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                }
                Text(
                  text = optionText,
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = if (isVotedByMe) FontWeight.Bold else FontWeight.Medium,
                  color = colors.onSurface,
                  maxLines = 2,
                  overflow = TextOverflow.Ellipsis
                )
              }
              if (totalVotes > 0) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "$percent% (${voters.size})",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.SemiBold,
                  color = if (isVotedByMe) colors.primary else colors.onSurfaceVariant
                )
              }
            }
          }
        }
      }

      Text(
        text = String.format(strings.pollVotesCountFormat, totalVotes),
        style = MaterialTheme.typography.labelSmall,
        color = colors.onSurfaceVariant
      )
    }
  }
}
