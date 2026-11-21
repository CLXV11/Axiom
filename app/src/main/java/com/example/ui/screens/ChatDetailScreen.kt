package com.example.ui.screens

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Forward
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Poll
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.AddReaction
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Search
import androidx.core.content.ContextCompat
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Conversation
import com.example.data.model.Message
import com.example.data.model.UserProfile
import com.example.data.repository.AiConversationState
import com.example.data.repository.ChatMediaRepository
import com.example.data.repository.ChatRepository
import com.example.data.repository.PostRepository
import com.example.data.repository.UserRepository
import com.example.ui.components.AvatarView
import com.example.ui.components.ChatMediaAttachmentSheet
import com.example.ui.components.FullscreenVideoPlayerDialog
import com.example.ui.components.IosFloatingReactionBar
import com.example.ui.components.MediaMessageBubbleContent
import com.example.ui.components.PollMessageCard
import com.example.ui.components.ReactionChipsRow
import com.example.ui.components.TypingBubble
import com.example.ui.components.TypingDotsIndicator
import com.example.ui.components.VerifiedBadge
import com.example.ui.components.VoiceRecorderController
import com.example.ui.components.WeirdEmojiPickerBottomSheet
import com.example.ui.theme.StatusOnline
import com.example.ui.util.LocalAppStrings
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

private typealias UnitCallback = () -> Unit

@Composable
fun ChatDetailScreen(
  conversation: Conversation,
  chatRepository: ChatRepository,
  chatMediaRepository: ChatMediaRepository,
  postRepository: PostRepository,
  userRepository: UserRepository,
  conversations: List<Conversation> = emptyList(),
  onSwitchToConversation: (Conversation) -> Unit = {},
  onInspectUser: (UserProfile) -> Unit = {},
  onStartVoiceCall: (
    peerId: String,
    peerName: String,
    peerAvatarUrl: String?,
    peerVerified: Boolean,
    peerVerifiedTier: String?
  ) -> Unit = { _, _, _, _, _ -> },
  onBack: UnitCallback
) {
  BackHandler { onBack() }

  val context = LocalContext.current
  val strings = LocalAppStrings.current
  val coroutineScope = rememberCoroutineScope()
  val listState = rememberLazyListState()

  val liveConversation = remember(conversations, conversation) {
    conversations.find { it.id == conversation.id } ?: conversation
  }

  val isAiChat = remember(liveConversation.id) {
    chatRepository.isAiChatbotConversation(liveConversation.id)
  }

  val currentUid = userRepository.requireUserId()
  val cachedCurrentProfile = remember(currentUid) { userRepository.getCachedProfile(currentUid) }
  val currentUserProfile by userRepository.observeUserProfile(currentUid)
    .collectAsState(initial = cachedCurrentProfile)
  val currentUserName = currentUserProfile?.displayName?.takeIf { it.isNotBlank() } ?: "Me"

  val messages by chatRepository.observeMessages(liveConversation.id).collectAsState(initial = emptyList())
  val typingUsers by chatRepository.observeTyping(liveConversation.id).collectAsState(initial = emptyList())
  val aiState by chatRepository.observeAiState(liveConversation.id).collectAsState(initial = AiConversationState())
  val directoryUsers by userRepository.observeDirectoryUsers()
    .collectAsState(initial = userRepository.getDirectoryUsers())

  val peerId = remember(liveConversation.participantIds, currentUid, isAiChat) {
    if (!isAiChat && liveConversation.type == "direct") {
      liveConversation.participantIds.firstOrNull { it != currentUid }
    } else null
  }
  var fallbackPeer by remember(peerId) { mutableStateOf<UserProfile?>(null) }

  LaunchedEffect(peerId) {
    if (peerId != null && directoryUsers.none { it.id == peerId }) {
      fallbackPeer = runCatching { userRepository.getUserProfile(peerId) }.getOrNull()
    }
  }

  val peerUser = remember(peerId, directoryUsers, fallbackPeer) {
    peerId?.let { id -> directoryUsers.find { it.id == id } ?: fallbackPeer }
  }

  LaunchedEffect(liveConversation.id, messages.size) {
    if (!isAiChat && messages.isNotEmpty()) {
      chatRepository.markAsRead(liveConversation.id)
    }
  }

  val peerTypingUsers = remember(typingUsers, currentUserName) {
    typingUsers.filter { it != currentUserName }
  }

  var messageInput by remember { mutableStateOf("") }
  var replyingToMessage by remember { mutableStateOf<Message?>(null) }
  var editingMessage by remember { mutableStateOf<Message?>(null) }
  var spoilerMode by remember { mutableStateOf(false) }

  // Debounced user typing state for multi-user chats
  LaunchedEffect(messageInput, liveConversation.id) {
    if (!isAiChat && messageInput.isNotBlank()) {
      chatRepository.setTyping(liveConversation.id, currentUserName, true)
      delay(2200)
      chatRepository.setTyping(liveConversation.id, currentUserName, false)
    } else if (!isAiChat) {
      chatRepository.setTyping(liveConversation.id, currentUserName, false)
    }
  }

  DisposableEffect(liveConversation.id) {
    onDispose {
      chatRepository.setTyping(liveConversation.id, currentUserName, false)
    }
  }

  // Save to Memory, Reaction, Poll, Forward, Search & Chat Info state
  var memoryTargetMessage by remember { mutableStateOf<Message?>(null) }
  var privateNoteInput by remember { mutableStateOf("") }
  var reactionTargetMessageId by remember { mutableStateOf<String?>(null) }
  var showCreatePollDialog by remember { mutableStateOf(false) }
  var forwardTargetMessage by remember { mutableStateOf<Message?>(null) }
  var showChatInfoSheet by remember { mutableStateOf(false) }
  var showSearchBar by remember { mutableStateOf(false) }
  var searchQuery by remember { mutableStateOf("") }
  var searchMatchIndex by remember { mutableIntStateOf(0) }

  // Voice Note recorder
  val voiceRecorder = remember(liveConversation.id, coroutineScope) {
    VoiceRecorderController(context, coroutineScope)
  }
  val isRecordingVoice by voiceRecorder.isRecording.collectAsState()
  val recordingElapsedMs by voiceRecorder.elapsedMs.collectAsState()
  DisposableEffect(voiceRecorder) {
    onDispose { voiceRecorder.release() }
  }

  // Rich media attachment, upload progress, and inline/fullscreen video playback state
  var showMediaPickerSheet by remember { mutableStateOf(false) }
  var isUploadingMedia by remember { mutableStateOf(false) }
  var uploadProgress by remember { mutableFloatStateOf(0f) }
  var activeInlineVideoMsgId by remember { mutableStateOf<String?>(null) }
  var fullscreenVideoItem by remember { mutableStateOf<Triple<File, String?, String?>?>(null) }

  fun inspectParticipant(
    targetUid: String,
    fallbackName: String,
    fallbackVerified: Boolean,
    fallbackTier: String?
  ) {
    if (targetUid == ChatRepository.AI_CHATBOT_SENDER_ID || targetUid.isBlank()) return
    val profile = directoryUsers.find { it.id == targetUid }
      ?: (if (targetUid == peerId) peerUser else null)
      ?: UserProfile(
        id = targetUid,
        displayName = fallbackName.ifBlank { "Member" },
        isVerified = fallbackVerified,
        verifiedTier = fallbackTier ?: "BLUE"
      )
    onInspectUser(profile)
  }

  fun uploadAndSendMedia(uri: Uri, mediaType: String) {
    if (isUploadingMedia) return
    val pendingReply = replyingToMessage
    val caption = messageInput.trim()
    coroutineScope.launch {
      isUploadingMedia = true
      uploadProgress = 0.05f
      val uploadRes = chatMediaRepository.prepareAndUploadMedia(
        conversationId = conversation.id,
        sourceUri = uri,
        mediaType = mediaType,
        isLocalOnlyChat = isAiChat,
        onProgress = { uploadProgress = it }
      )
      if (uploadRes.isSuccess) {
        val prepared = uploadRes.getOrThrow()
        val userProfile = currentUserProfile ?: runCatching { userRepository.getUserProfile(currentUid) }.getOrNull()
        val senderName = userProfile?.displayName?.takeIf { it.isNotBlank() } ?: currentUserName
        val isVerified = userProfile?.isVerified ?: false
        val verifiedTier = userProfile?.verifiedTier ?: "BLUE"
        val pendingSpoiler = spoilerMode && mediaType != "audio"
        val sendRes = chatRepository.sendMediaMessage(
          conversationId = liveConversation.id,
          media = prepared,
          caption = caption,
          senderName = senderName,
          replyTo = pendingReply,
          isSenderVerified = isVerified,
          senderVerifiedTier = verifiedTier,
          isSpoiler = pendingSpoiler
        )
        if (sendRes.isSuccess) {
          messageInput = ""
          replyingToMessage = null
          if (pendingSpoiler) spoilerMode = false
        } else {
          Toast.makeText(
            context,
            sendRes.exceptionOrNull()?.localizedMessage ?: strings.chatDeliveryError,
            Toast.LENGTH_SHORT
          ).show()
        }
      } else {
        val errMsg = uploadRes.exceptionOrNull()?.message.orEmpty()
        val displayMsg = if (errMsg.contains("25 MB", ignoreCase = true)) {
          strings.fileTooLargeLimit
        } else {
          uploadRes.exceptionOrNull()?.localizedMessage ?: strings.mediaUploadFailed
        }
        Toast.makeText(context, displayMsg, Toast.LENGTH_LONG).show()
      }
      isUploadingMedia = false
      uploadProgress = 0f
    }
  }

  val photoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
  ) { uri ->
    if (uri != null) uploadAndSendMedia(uri, "image")
  }

  val videoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
  ) { uri ->
    if (uri != null) uploadAndSendMedia(uri, "video")
  }

  val filePickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.OpenDocument()
  ) { uri ->
    if (uri != null) uploadAndSendMedia(uri, "file")
  }

  var pendingMicAction by remember { mutableStateOf<(() -> Unit)?>(null) }
  val micPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { granted ->
    if (granted) {
      pendingMicAction?.invoke()
    } else {
      Toast.makeText(context, strings.micPermissionRequired, Toast.LENGTH_LONG).show()
    }
    pendingMicAction = null
  }

  fun runWithMicPermission(action: () -> Unit) {
    val hasMic = ContextCompat.checkSelfPermission(
      context,
      Manifest.permission.RECORD_AUDIO
    ) == PackageManager.PERMISSION_GRANTED
    if (hasMic) {
      action()
    } else {
      pendingMicAction = action
      micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }
  }

  val matchingMessageIds = remember(messages, searchQuery) {
    val q = searchQuery.trim()
    if (q.isBlank()) emptyList()
    else messages.filter { !it.isDeleted && it.content.contains(q, ignoreCase = true) }.map { it.id }
  }
  val activeMatchedMsgId = matchingMessageIds.getOrNull(
    searchMatchIndex.coerceIn(0, (matchingMessageIds.size - 1).coerceAtLeast(0))
  )

  LaunchedEffect(searchQuery, matchingMessageIds) {
    searchMatchIndex = if (matchingMessageIds.isNotEmpty()) matchingMessageIds.lastIndex else 0
  }

  LaunchedEffect(activeMatchedMsgId) {
    val targetId = activeMatchedMsgId ?: return@LaunchedEffect
    val idx = messages.indexOfFirst { it.id == targetId }
    if (idx >= 0) {
      listState.animateScrollToItem(idx)
    }
  }

  // Auto-scroll smoothly as messages or streaming chunks arrive
  val lastContentLength = messages.lastOrNull()?.content?.length ?: 0
  LaunchedEffect(
    messages.size,
    lastContentLength,
    peerTypingUsers.size,
    aiState.errorMessage,
    showSearchBar,
    searchQuery
  ) {
    if (showSearchBar && searchQuery.isNotBlank()) return@LaunchedEffect
    val extraItems = (if (peerTypingUsers.isNotEmpty()) 1 else 0) +
      (if (aiState.errorMessage != null) 1 else 0)
    val totalCount = messages.size + extraItems
    if (totalCount > 0) {
      listState.animateScrollToItem(totalCount - 1)
    }
  }

  fun sendText(rawText: String) {
    val text = rawText.trim()
    if (text.isBlank()) return
    if (isAiChat && aiState.isGenerating) return

    chatRepository.setTyping(liveConversation.id, currentUserName, false)

    coroutineScope.launch {
      val userProfile = currentUserProfile ?: runCatching { userRepository.getUserProfile(currentUid) }.getOrNull()
      val senderName = userProfile?.displayName?.takeIf { it.isNotBlank() } ?: currentUserName
      val isVerified = userProfile?.isVerified ?: false
      val verifiedTier = userProfile?.verifiedTier ?: "BLUE"

      if (editingMessage != null) {
        val res = chatRepository.editMessage(liveConversation.id, editingMessage!!.id, text)
        if (res.isSuccess) {
          editingMessage = null
          messageInput = ""
        } else {
          Toast.makeText(
            context,
            res.exceptionOrNull()?.localizedMessage ?: strings.chatDeliveryError,
            Toast.LENGTH_SHORT
          ).show()
        }
      } else {
        val pendingReply = replyingToMessage
        val pendingSpoiler = spoilerMode && !isAiChat
        messageInput = ""
        replyingToMessage = null
        spoilerMode = false
        val res = chatRepository.sendMessage(
          conversationId = liveConversation.id,
          content = text,
          senderName = senderName,
          replyTo = pendingReply,
          isSenderVerified = isVerified,
          senderVerifiedTier = verifiedTier,
          isSpoiler = pendingSpoiler
        )
        if (res.isFailure) {
          messageInput = text
          replyingToMessage = pendingReply
          spoilerMode = pendingSpoiler
          Toast.makeText(
            context,
            res.exceptionOrNull()?.localizedMessage ?: strings.chatDeliveryError,
            Toast.LENGTH_SHORT
          ).show()
        }
      }
    }
  }

  fun copyToClipboard(text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Kora Message", text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, strings.messageCopied, Toast.LENGTH_SHORT).show()
  }

  fun confirmSaveToMemory() {
    val target = memoryTargetMessage ?: return
    coroutineScope.launch {
      val res = postRepository.saveMemory(
        originalMessageId = target.id,
        conversationId = conversation.id,
        snippet = target.content,
        senderName = target.senderName,
        privateNote = privateNoteInput.trim()
      )
      memoryTargetMessage = null
      privateNoteInput = ""
      Toast.makeText(
        context,
        if (res.isSuccess) strings.memorySaved else strings.errorOccurred,
        Toast.LENGTH_SHORT
      ).show()
    }
  }

  val isGroup = liveConversation.type == "group"
  val convoTitle = when {
    isAiChat -> strings.aiAssistantName
    peerUser != null && peerUser.displayName.isNotBlank() -> peerUser.displayName
    liveConversation.title.isNotBlank() -> liveConversation.title
    isGroup -> strings.groupChats
    else -> strings.directChats
  }

  val headerSubtitle = remember(isAiChat, peerTypingUsers, aiState.isGenerating, liveConversation, peerUser, strings) {
    when {
      isAiChat && aiState.isGenerating -> strings.aiThinking
      peerTypingUsers.isNotEmpty() -> when {
        peerTypingUsers.size == 1 -> String.format(strings.typingSingle, peerTypingUsers.first())
        peerTypingUsers.size == 2 -> "${peerTypingUsers[0]} & ${peerTypingUsers[1]} ${strings.isTyping}"
        else -> strings.typingMultiple
      }
      isAiChat -> strings.aiAssistantSubtitle
      isGroup -> String.format(strings.membersCountFormat, liveConversation.participantIds.size)
      !peerUser?.profession.isNullOrBlank() -> peerUser!!.profession
      !peerUser?.handle.isNullOrBlank() -> peerUser!!.handle
      else -> strings.online
    }
  }

  val calmBackdrop = Brush.verticalGradient(
    colors = listOf(
      MaterialTheme.colorScheme.background,
      MaterialTheme.colorScheme.surface.copy(alpha = 0.65f),
      MaterialTheme.colorScheme.background
    )
  )

  Scaffold(
    containerColor = MaterialTheme.colorScheme.background,
    topBar = {
      PolishedChatTopBar(
        title = convoTitle,
        subtitle = headerSubtitle,
        avatarUrl = peerUser?.avatarUrl,
        frameStyleKey = peerUser?.equippedFrame,
        isAiChat = isAiChat,
        isVerified = peerUser?.isVerified ?: liveConversation.isVerified,
        verifiedTier = peerUser?.verifiedTier ?: liveConversation.verifiedTier,
        isThinkingOrTyping = aiState.isGenerating || peerTypingUsers.isNotEmpty(),
        canClearAiChat = isAiChat && messages.isNotEmpty(),
        canStartVoiceCall = !isAiChat && !isGroup && !peerId.isNullOrBlank(),
        canInspectOrViewInfo = !isAiChat,
        isSearchOpen = showSearchBar,
        onHeaderClick = {
          if (!isAiChat) {
            if (!isGroup && peerId != null) {
              inspectParticipant(
                targetUid = peerId,
                fallbackName = convoTitle,
                fallbackVerified = liveConversation.isVerified,
                fallbackTier = liveConversation.verifiedTier
              )
            } else {
              showChatInfoSheet = true
            }
          }
        },
        onToggleSearch = {
          showSearchBar = !showSearchBar
          if (!showSearchBar) searchQuery = ""
        },
        onOpenChatInfo = {
          if (!isAiChat) showChatInfoSheet = true
        },
        onStartVoiceCall = {
          val targetId = peerId ?: return@PolishedChatTopBar
          runWithMicPermission {
            onStartVoiceCall(
              targetId,
              convoTitle,
              peerUser?.avatarUrl,
              peerUser?.isVerified ?: liveConversation.isVerified,
              peerUser?.verifiedTier ?: liveConversation.verifiedTier
            )
          }
        },
        onClearAiChat = { chatRepository.clearAiConversation() },
        onBackClick = onBack
      )
    }
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .background(calmBackdrop)
        .imePadding(),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // Expandable In-Chat Search Bar
      AnimatedVisibility(
        visible = showSearchBar,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
      ) {
        ChatSearchBarStrip(
          query = searchQuery,
          onQueryChange = { searchQuery = it },
          matchCount = matchingMessageIds.size,
          currentMatchIndex = searchMatchIndex,
          onPrevMatch = {
            if (matchingMessageIds.isNotEmpty()) {
              searchMatchIndex = (searchMatchIndex - 1 + matchingMessageIds.size) % matchingMessageIds.size
            }
          },
          onNextMatch = {
            if (matchingMessageIds.isNotEmpty()) {
              searchMatchIndex = (searchMatchIndex + 1) % matchingMessageIds.size
            }
          },
          onClose = {
            showSearchBar = false
            searchQuery = ""
          }
        )
      }

      // Sticky Pinned Message Banner
      val pinnedId = liveConversation.pinnedMessageId
      AnimatedVisibility(
        visible = !pinnedId.isNullOrBlank() && !isAiChat,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
      ) {
        if (!pinnedId.isNullOrBlank()) {
          PinnedMessageBanner(
            senderName = liveConversation.pinnedMessageSender.orEmpty(),
            previewText = liveConversation.pinnedMessageText.orEmpty(),
            onJumpToPinned = {
              val idx = messages.indexOfFirst { it.id == pinnedId }
              if (idx >= 0) {
                coroutineScope.launch { listState.animateScrollToItem(idx) }
              }
            },
            onUnpin = {
              coroutineScope.launch {
                val res = chatRepository.pinMessageInConversation(liveConversation.id, null)
                if (res.isSuccess) {
                  Toast.makeText(context, strings.messageUnpinnedToast, Toast.LENGTH_SHORT).show()
                }
              }
            }
          )
        }
      }

      // Message Stream or Calm AI Welcome Canvas
      Box(
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth()
          .widthIn(max = 840.dp)
      ) {
        if (isAiChat && messages.isEmpty() && aiState.errorMessage == null) {
          AiWelcomeHero(
            onPromptClick = { prompt -> sendText(prompt) },
            modifier = Modifier
              .fillMaxSize()
              .padding(horizontal = 20.dp, vertical = 16.dp)
          )
        } else {
          LazyColumn(
            state = listState,
            modifier = Modifier
              .fillMaxSize()
              .testTag("chat_messages_list"),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            items(messages, key = { it.id }) { msg ->
              val isMe = msg.senderId == currentUid
              val isBotMsg = msg.senderId == ChatRepository.AI_CHATBOT_SENDER_ID
              val senderProfile = directoryUsers.find { it.id == msg.senderId }
                ?: if (msg.senderId == peerId) peerUser else null
              val liveVerified = when {
                isBotMsg -> msg.isSenderVerified
                isMe -> currentUserProfile?.isVerified ?: msg.isSenderVerified
                else -> senderProfile?.isVerified ?: msg.isSenderVerified
              }
              val liveVerifiedTier = when {
                isBotMsg -> msg.senderVerifiedTier
                isMe -> currentUserProfile?.verifiedTier ?: msg.senderVerifiedTier
                else -> senderProfile?.verifiedTier ?: msg.senderVerifiedTier
              }
              val canPin = !isAiChat && !msg.isDeleted
              val isPinnedMsg = liveConversation.pinnedMessageId == msg.id
              val isSearchMatched = activeMatchedMsgId == msg.id

              PolishedMessageBubble(
                conversationId = liveConversation.id,
                currentUid = currentUid,
                message = msg.copy(
                  isSenderVerified = liveVerified,
                  senderVerifiedTier = liveVerifiedTier
                ),
                isMe = isMe,
                isAiMessage = isBotMsg,
                isPinned = isPinnedMsg,
                isSearchHighlighted = isSearchMatched,
                senderAvatarUrl = senderProfile?.avatarUrl,
                senderFrameKey = senderProfile?.equippedFrame,
                chatMediaRepository = chatMediaRepository,
                isInlineVideoPlaying = activeInlineVideoMsgId == msg.id,
                onInspectSender = {
                  inspectParticipant(
                    targetUid = msg.senderId,
                    fallbackName = msg.senderName,
                    fallbackVerified = liveVerified,
                    fallbackTier = liveVerifiedTier
                  )
                },
                onVotePoll = { optionIdx ->
                  coroutineScope.launch {
                    val res = chatRepository.votePollOption(
                      conversationId = liveConversation.id,
                      messageId = msg.id,
                      optionIndex = optionIdx
                    )
                    if (res.isFailure) {
                      Toast.makeText(
                        context,
                        res.exceptionOrNull()?.localizedMessage ?: strings.errorOccurred,
                        Toast.LENGTH_SHORT
                      ).show()
                    }
                  }
                },
                onPlayInlineVideo = { activeInlineVideoMsgId = msg.id },
                onStopInlineVideo = {
                  if (activeInlineVideoMsgId == msg.id) {
                    activeInlineVideoMsgId = null
                  }
                },
                onOpenFullscreenVideo = { file ->
                  activeInlineVideoMsgId = null
                  fullscreenVideoItem = Triple(file, msg.mediaFileName, msg.mediaMimeType)
                },
                onToggleReaction = { emoji ->
                  coroutineScope.launch {
                    val res = chatRepository.toggleMessageReaction(liveConversation.id, msg.id, emoji)
                    if (res.isFailure) {
                      Toast.makeText(
                        context,
                        res.exceptionOrNull()?.localizedMessage ?: strings.errorOccurred,
                        Toast.LENGTH_SHORT
                      ).show()
                    }
                  }
                },
                onOpenReactionSheet = {
                  reactionTargetMessageId = msg.id
                },
                onReply = {
                  replyingToMessage = msg
                  editingMessage = null
                },
                onCopy = { copyToClipboard(msg.content) },
                onForward = if (!msg.isDeleted) {
                  { forwardTargetMessage = msg }
                } else null,
                onTogglePin = if (canPin) {
                  {
                    coroutineScope.launch {
                      val targetPin = if (isPinnedMsg) null else msg
                      val res = chatRepository.pinMessageInConversation(liveConversation.id, targetPin)
                      if (res.isSuccess) {
                        Toast.makeText(
                          context,
                          if (targetPin != null) strings.messagePinnedToast else strings.messageUnpinnedToast,
                          Toast.LENGTH_SHORT
                        ).show()
                      } else {
                        Toast.makeText(context, strings.errorOccurred, Toast.LENGTH_SHORT).show()
                      }
                    }
                  }
                } else null,
                onEdit = {
                  editingMessage = msg
                  replyingToMessage = null
                  messageInput = msg.content
                },
                onDelete = {
                  coroutineScope.launch {
                    val res = chatRepository.deleteMessage(
                      conversationId = liveConversation.id,
                      messageId = msg.id,
                      deletedPlaceholder = strings.messageDeleted
                    )
                    if (res.isFailure) {
                      Toast.makeText(context, strings.errorOccurred, Toast.LENGTH_SHORT).show()
                    }
                  }
                },
                onSaveMemory = {
                  memoryTargetMessage = msg
                  privateNoteInput = ""
                }
              )
            }

            if (peerTypingUsers.isNotEmpty()) {
              item(key = "typing_bubble_indicator") {
                TypingBubble(
                  typingUserNames = peerTypingUsers,
                  modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                )
              }
            }

            if (aiState.errorMessage != null) {
              item(key = "ai_error_banner") {
                AiErrorBanner(
                  message = aiState.errorMessage!!,
                  onRetry = { chatRepository.retryLastAiPrompt() },
                  onDismiss = { chatRepository.clearAiError(liveConversation.id) }
                )
              }
            }
          }
        }
      }

      // Reply Preview Bar
      AnimatedVisibility(
        visible = replyingToMessage != null,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
      ) {
        replyingToMessage?.let { target ->
          ComposerContextStrip(
            label = "${strings.replyingTo} ${target.senderName}",
            previewText = target.content,
            onDismiss = { replyingToMessage = null }
          )
        }
      }

      // Edit Preview Bar
      AnimatedVisibility(
        visible = editingMessage != null,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
      ) {
        editingMessage?.let { target ->
          ComposerContextStrip(
            label = strings.edit,
            previewText = target.content,
            onDismiss = {
              editingMessage = null
              messageInput = ""
            }
          )
        }
      }

      // Media upload progress bar
      AnimatedVisibility(
        visible = isUploadingMedia,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
      ) {
        Surface(
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f),
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 840.dp)
            .padding(horizontal = 14.dp, vertical = 4.dp)
        ) {
          Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
            Text(
              text = String.format(strings.uploadingMediaProgress, (uploadProgress * 100).toInt().coerceIn(1, 99)),
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
              progress = { uploadProgress.coerceIn(0.05f, 1f) },
              modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
            )
          }
        }
      }

      // Floating Soft Pill Composer (or Voice Note Recording Bar)
      Box(modifier = Modifier.fillMaxWidth().widthIn(max = 840.dp)) {
        PolishedMessageComposer(
          value = messageInput,
          onValueChange = { messageInput = it },
          placeholder = strings.typeMessage,
          isSendEnabled = messageInput.isNotBlank() && !(isAiChat && aiState.isGenerating) && !isUploadingMedia,
          showAttachButton = !isAiChat && editingMessage == null,
          isAttachEnabled = !isUploadingMedia,
          canRecordVoiceNote = !isAiChat && editingMessage == null && !isUploadingMedia,
          isRecordingVoice = isRecordingVoice,
          recordingElapsedMs = recordingElapsedMs,
          isSpoilerMode = spoilerMode,
          onToggleSpoiler = { spoilerMode = !spoilerMode },
          onStartRecording = {
            runWithMicPermission {
              val res = voiceRecorder.startRecording()
              if (res.isFailure) {
                Toast.makeText(
                  context,
                  res.exceptionOrNull()?.localizedMessage ?: strings.errorOccurred,
                  Toast.LENGTH_SHORT
                ).show()
              }
            }
          },
          onCancelRecording = { voiceRecorder.cancelRecording() },
          onSendRecording = {
            val file = voiceRecorder.stopRecording()
            if (file != null) {
              uploadAndSendMedia(Uri.fromFile(file), "audio")
            } else {
              Toast.makeText(context, strings.errorOccurred, Toast.LENGTH_SHORT).show()
            }
          },
          onAttachClick = { showMediaPickerSheet = true },
          onSend = { sendText(messageInput) }
        )
      }
    }
  }

  if (showMediaPickerSheet) {
    ChatMediaAttachmentSheet(
      onDismiss = { showMediaPickerSheet = false },
      onPickPhoto = {
        photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
      },
      onPickVideo = {
        videoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
      },
      onPickFile = {
        filePickerLauncher.launch(arrayOf("*/*"))
      },
      onCreatePoll = {
        showCreatePollDialog = true
      }
    )
  }

  if (showCreatePollDialog) {
    CreatePollDialog(
      onDismiss = { showCreatePollDialog = false },
      onConfirm = { question, options ->
        showCreatePollDialog = false
        coroutineScope.launch {
          val userProfile = currentUserProfile ?: runCatching { userRepository.getUserProfile(currentUid) }.getOrNull()
          val res = chatRepository.sendPollMessage(
            conversationId = liveConversation.id,
            question = question,
            options = options,
            senderName = userProfile?.displayName?.takeIf { it.isNotBlank() } ?: currentUserName,
            isSenderVerified = userProfile?.isVerified ?: false,
            senderVerifiedTier = userProfile?.verifiedTier ?: "BLUE"
          )
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

  forwardTargetMessage?.let { msgToForward ->
    val availableTargets = remember(conversations, liveConversation.id) {
      conversations.filter { !chatRepository.isAiChatbotConversation(it.id) }
    }
    ForwardMessageSheet(
      conversations = availableTargets,
      directoryUsers = directoryUsers,
      currentUid = currentUid,
      onDismiss = { forwardTargetMessage = null },
      onSelectConversation = { targetConvo ->
        forwardTargetMessage = null
        coroutineScope.launch {
          val userProfile = currentUserProfile ?: runCatching { userRepository.getUserProfile(currentUid) }.getOrNull()
          val res = chatRepository.forwardMessage(
            targetConversationId = targetConvo.id,
            sourceMessage = msgToForward,
            senderName = userProfile?.displayName?.takeIf { it.isNotBlank() } ?: currentUserName,
            isSenderVerified = userProfile?.isVerified ?: false,
            senderVerifiedTier = userProfile?.verifiedTier ?: "BLUE",
            chatMediaRepository = chatMediaRepository
          )
          if (res.isSuccess) {
            Toast.makeText(context, strings.messageForwardedToast, Toast.LENGTH_SHORT).show()
            if (targetConvo.id != liveConversation.id) {
              onSwitchToConversation(targetConvo)
            }
          } else {
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

  if (showChatInfoSheet) {
    val participants = remember(liveConversation.participantIds, directoryUsers, currentUserProfile, peerUser, currentUid) {
      liveConversation.participantIds.map { uid ->
        when {
          uid == currentUid && currentUserProfile != null -> currentUserProfile!!
          uid == peerId && peerUser != null -> peerUser
          else -> directoryUsers.find { it.id == uid } ?: UserProfile(id = uid, displayName = "Member")
        }
      }
    }
    ChatInfoAndMediaSheet(
      conversationTitle = convoTitle,
      conversationType = liveConversation.type,
      participants = participants,
      messages = messages,
      onDismiss = { showChatInfoSheet = false },
      onInspectParticipant = { profile ->
        showChatInfoSheet = false
        onInspectUser(profile)
      },
      onJumpToMessage = { msgId ->
        showChatInfoSheet = false
        val idx = messages.indexOfFirst { it.id == msgId }
        if (idx >= 0) {
          coroutineScope.launch { listState.animateScrollToItem(idx) }
        }
      }
    )
  }

  fullscreenVideoItem?.let { (file, name, mime) ->
    FullscreenVideoPlayerDialog(
      file = file,
      fileName = name,
      mimeType = mime,
      chatMediaRepository = chatMediaRepository,
      onDismiss = { fullscreenVideoItem = null }
    )
  }

  // Save to Memory Dialog
  if (memoryTargetMessage != null) {
    AlertDialog(
      containerColor = MaterialTheme.colorScheme.surface,
      titleContentColor = MaterialTheme.colorScheme.onSurface,
      textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
      onDismissRequest = { memoryTargetMessage = null },
      title = { Text(strings.saveToMemory, fontWeight = FontWeight.Bold) },
      text = {
        Column {
          Text(
            text = "\"${memoryTargetMessage!!.content.take(150)}\"",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.height(12.dp))
          OutlinedTextField(
            value = privateNoteInput,
            onValueChange = { privateNoteInput = it },
            label = { Text(strings.addPrivateNote) },
            placeholder = { Text(strings.notePlaceholder) },
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = MaterialTheme.colorScheme.primary,
              unfocusedBorderColor = MaterialTheme.colorScheme.outline
            ),
            maxLines = 3,
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        Button(onClick = { confirmSaveToMemory() }) {
          Text(strings.saveNote)
        }
      },
      dismissButton = {
        TextButton(onClick = { memoryTargetMessage = null }) {
          Text(strings.cancel)
        }
      }
    )
  }

  // Full Weird & Classic Emoji Picker Sheet for Messages
  val activeReactionMsg = reactionTargetMessageId?.let { targetId ->
    messages.firstOrNull { it.id == targetId }
  }
  if (activeReactionMsg != null) {
    val mySelected = remember(activeReactionMsg.reactions, currentUid) {
      activeReactionMsg.reactions
        .filterValues { it.contains(currentUid) }
        .keys
        .toSet()
    }
    WeirdEmojiPickerBottomSheet(
      selectedEmojis = mySelected,
      onEmojiSelected = { emoji ->
        reactionTargetMessageId = null
        coroutineScope.launch {
          val res = chatRepository.toggleMessageReaction(
            conversationId = conversation.id,
            messageId = activeReactionMsg.id,
            emoji = emoji
          )
          if (res.isFailure) {
            Toast.makeText(
              context,
              res.exceptionOrNull()?.localizedMessage ?: strings.errorOccurred,
              Toast.LENGTH_SHORT
            ).show()
          }
        }
      },
      onDismiss = { reactionTargetMessageId = null }
    )
  }
}

@Composable
private fun PolishedChatTopBar(
  title: String,
  subtitle: String,
  avatarUrl: String?,
  frameStyleKey: String? = null,
  isAiChat: Boolean,
  isVerified: Boolean,
  verifiedTier: String?,
  isThinkingOrTyping: Boolean,
  canClearAiChat: Boolean,
  canStartVoiceCall: Boolean,
  canInspectOrViewInfo: Boolean = true,
  isSearchOpen: Boolean = false,
  onHeaderClick: () -> Unit = {},
  onToggleSearch: () -> Unit = {},
  onOpenChatInfo: () -> Unit = {},
  onStartVoiceCall: () -> Unit,
  onClearAiChat: () -> Unit,
  onBackClick: () -> Unit
) {
  val strings = LocalAppStrings.current

  Surface(
    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
    tonalElevation = 2.dp,
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(modifier = Modifier.statusBarsPadding()) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .defaultMinSize(minHeight = 64.dp)
          .padding(horizontal = 6.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(
          onClick = onBackClick,
          modifier = Modifier.size(48.dp)
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = MaterialTheme.colorScheme.onSurface
          )
        }

        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = canInspectOrViewInfo, onClick = onHeaderClick)
            .padding(horizontal = 4.dp, vertical = 4.dp)
            .testTag("chat_header_profile_button")
        ) {
          if (isAiChat) {
            Box(
              modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(
                  Brush.linearGradient(
                    colors = listOf(
                      MaterialTheme.colorScheme.primary,
                      MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                    )
                  )
                ),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = title,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
              )
            }
          } else {
            AvatarView(
              name = title,
              avatarUrl = avatarUrl,
              size = 40.dp,
              frameStyleKey = frameStyleKey,
              showPresence = true,
              isOnline = true
            )
          }

          Spacer(modifier = Modifier.width(10.dp))

          Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
              )
              if (isVerified) {
                Spacer(modifier = Modifier.width(5.dp))
                VerifiedBadge(
                  size = 15.dp,
                  tierKey = verifiedTier,
                  showDialogOnClick = true
                )
              }
              if (isAiChat) {
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                  shape = RoundedCornerShape(6.dp),
                  color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                ) {
                  Text(
                    text = strings.aiBotBadge,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                  )
                }
              }
            }

            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(top = 2.dp)
            ) {
              if (isThinkingOrTyping) {
                TypingDotsIndicator(
                  dotSize = 4.dp,
                  travelDistance = 2.dp,
                  dotColor = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = subtitle,
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.primary,
                  fontWeight = FontWeight.Medium,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
              } else {
                Box(
                  modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(StatusOnline)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = subtitle,
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
              }
            }
          }
        }

        IconButton(
          onClick = onToggleSearch,
          modifier = Modifier
            .size(48.dp)
            .testTag("chat_search_button")
        ) {
          Icon(
            imageVector = Icons.Outlined.Search,
            contentDescription = strings.searchMessagesPlaceholder,
            tint = if (isSearchOpen) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        if (canStartVoiceCall) {
          IconButton(
            onClick = onStartVoiceCall,
            modifier = Modifier
              .size(48.dp)
              .testTag("start_voice_call_button")
          ) {
            Icon(
              imageVector = Icons.Default.Call,
              contentDescription = strings.startVoiceCall,
              tint = MaterialTheme.colorScheme.primary
            )
          }
        }

        if (canInspectOrViewInfo) {
          IconButton(
            onClick = onOpenChatInfo,
            modifier = Modifier
              .size(48.dp)
              .testTag("chat_info_button")
          ) {
            Icon(
              imageVector = Icons.Outlined.Info,
              contentDescription = strings.chatInfoTitle,
              tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        if (canClearAiChat) {
          IconButton(
            onClick = onClearAiChat,
            modifier = Modifier
              .size(48.dp)
              .testTag("clear_ai_chat_button")
          ) {
            Icon(
              imageVector = Icons.Default.Refresh,
              contentDescription = strings.aiClearChat,
              tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }

      HorizontalDivider(
        thickness = 0.8.dp,
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
      )
    }
  }
}

@Composable
private fun AiWelcomeHero(
  onPromptClick: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  val strings = LocalAppStrings.current
  val prompts = remember(strings) {
    listOf(strings.aiPromptOne, strings.aiPromptTwo, strings.aiPromptThree)
  }

  Column(
    modifier = modifier,
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Box(
      modifier = Modifier
        .size(68.dp)
        .clip(CircleShape)
        .background(
          Brush.linearGradient(
            listOf(
              MaterialTheme.colorScheme.primary.copy(alpha = 0.22f),
              MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
            )
          )
        )
        .border(
          width = 1.dp,
          color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
          shape = CircleShape
        ),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = Icons.Default.AutoAwesome,
        contentDescription = strings.aiAssistantName,
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(30.dp)
      )
    }

    Spacer(modifier = Modifier.height(18.dp))

    Text(
      text = strings.aiWelcomeTitle,
      style = MaterialTheme.typography.titleLarge,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onSurface,
      textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
      text = strings.aiWelcomeDesc,
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      textAlign = TextAlign.Center,
      lineHeight = 22.sp,
      modifier = Modifier.widthIn(max = 320.dp)
    )

    Spacer(modifier = Modifier.height(24.dp))

    Column(
      modifier = Modifier.widthIn(max = 360.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      prompts.forEachIndexed { idx, promptText ->
        Surface(
          onClick = { onPromptClick(promptText) },
          shape = RoundedCornerShape(16.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
          border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
          ),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("ai_starter_prompt_$idx")
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.AutoAwesome,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
              text = promptText,
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurface,
              fontWeight = FontWeight.Medium,
              modifier = Modifier.weight(1f)
            )
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PolishedMessageBubble(
  conversationId: String,
  currentUid: String,
  message: Message,
  isMe: Boolean,
  isAiMessage: Boolean,
  isPinned: Boolean = false,
  isSearchHighlighted: Boolean = false,
  senderAvatarUrl: String?,
  senderFrameKey: String? = null,
  chatMediaRepository: ChatMediaRepository,
  isInlineVideoPlaying: Boolean,
  onInspectSender: () -> Unit = {},
  onVotePoll: (Int) -> Unit = {},
  onPlayInlineVideo: () -> Unit,
  onStopInlineVideo: () -> Unit,
  onOpenFullscreenVideo: (File) -> Unit,
  onToggleReaction: (String) -> Unit,
  onOpenReactionSheet: () -> Unit,
  onReply: () -> Unit,
  onCopy: () -> Unit,
  onForward: (() -> Unit)? = null,
  onTogglePin: (() -> Unit)? = null,
  onEdit: () -> Unit,
  onDelete: () -> Unit,
  onSaveMemory: () -> Unit
) {
  val strings = LocalAppStrings.current
  var showMenu by remember { mutableStateOf(false) }
  var isSpoilerRevealed by remember(message.id) { mutableStateOf(false) }

  val formattedTime = remember(message.createdAt) {
    val date = message.createdAt?.toDate()
    if (date != null) {
      try {
        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(date)
      } catch (_: Exception) {
        ""
      }
    } else ""
  }

  val bubbleShape = if (isMe) {
    RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 6.dp)
  } else {
    RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomEnd = 20.dp, bottomStart = 6.dp)
  }

  val bubbleContainerColor = when {
    isSearchHighlighted -> MaterialTheme.colorScheme.primary.copy(alpha = 0.24f)
    isMe -> MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
  }

  val bubbleBorderColor = when {
    isSearchHighlighted -> MaterialTheme.colorScheme.primary
    isPinned -> Color(0xFFF59E0B).copy(alpha = 0.55f)
    isMe -> MaterialTheme.colorScheme.primary.copy(alpha = 0.28f)
    else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
  }

  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start,
    verticalAlignment = Alignment.Bottom
  ) {
    if (!isMe) {
      if (isAiMessage) {
        Box(
          modifier = Modifier
            .size(30.dp)
            .clip(CircleShape)
            .background(
              Brush.linearGradient(
                listOf(
                  MaterialTheme.colorScheme.primary,
                  MaterialTheme.colorScheme.primary.copy(alpha = 0.72f)
                )
              )
            ),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = message.senderName,
            tint = Color.White,
            modifier = Modifier.size(15.dp)
          )
        }
      } else {
        Box(
          modifier = Modifier
            .clip(CircleShape)
            .clickable(onClick = onInspectSender)
            .testTag("message_avatar_inspect_${message.id}")
        ) {
          AvatarView(
            name = message.senderName,
            avatarUrl = senderAvatarUrl,
            size = 30.dp,
            frameStyleKey = senderFrameKey
          )
        }
      }
      Spacer(modifier = Modifier.width(8.dp))
    }

    Column(
      horizontalAlignment = if (isMe) Alignment.End else Alignment.Start,
      modifier = Modifier.widthIn(max = 316.dp)
    ) {
      Box {
      Surface(
        shape = bubbleShape,
        color = bubbleContainerColor,
        border = BorderStroke(if (isSearchHighlighted) 1.5.dp else 1.dp, bubbleBorderColor),
        modifier = Modifier
          .widthIn(max = 310.dp)
          .clip(bubbleShape)
          .combinedClickable(
            onClick = {
              if (message.isSpoiler && !isSpoilerRevealed && !message.isDeleted) {
                isSpoilerRevealed = true
              }
            },
            onLongClick = { showMenu = true }
          )
      ) {
        Column(
          modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
          // Incoming sender badge row (clickable to inspect user profile)
          if (!isMe) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier
                .padding(bottom = 4.dp)
                .clip(RoundedCornerShape(6.dp))
                .clickable(enabled = !isAiMessage, onClick = onInspectSender)
            ) {
              Text(
                text = if (isAiMessage) strings.aiAssistantName else message.senderName,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
              )
              if (message.isSenderVerified) {
                Spacer(modifier = Modifier.width(4.dp))
                VerifiedBadge(
                  size = 13.dp,
                  tierKey = message.senderVerifiedTier,
                  showDialogOnClick = true
                )
              }
            }
          }

          // Forwarded provenance pill
          if (!message.forwardedFromName.isNullOrBlank() && !message.isDeleted) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(bottom = 4.dp)
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.Forward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(13.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = String.format(strings.forwardedFromLabel, message.forwardedFromName),
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
              )
            }
          }

          // Clean Quoted Reply Container
          if (!message.replyToText.isNullOrBlank()) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.55f))
                .padding(horizontal = 8.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .width(3.dp)
                  .height(26.dp)
                  .clip(RoundedCornerShape(2.dp))
                  .background(MaterialTheme.colorScheme.primary)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = message.replyToSender.orEmpty(),
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.primary,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
                Text(
                  text = message.replyToText,
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
              }
            }
          }

          // Spoiler Gate OR Message Content
          if (message.isSpoiler && !isSpoilerRevealed && !message.isDeleted) {
            Surface(
              onClick = { isSpoilerRevealed = true },
              shape = RoundedCornerShape(12.dp),
              color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
              border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("spoiler_cover_${message.id}")
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = Icons.Default.VisibilityOff,
                  contentDescription = strings.spoilerHiddenTitle,
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = strings.spoilerHiddenTitle,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                  )
                  Text(
                    text = strings.spoilerTapToReveal,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }
            }
          } else {
            // Interactive Poll Card
            if (message.isPollMessage() && !message.isDeleted) {
              PollMessageCard(
                message = message,
                currentUid = currentUid,
                onVoteOption = onVotePoll
              )
            }

            // Embedded Media Content (Photo / Inline ExoPlayer Video / Voice Note Audio / File Card)
            if (message.hasMediaAttachment() && !message.isDeleted) {
              MediaMessageBubbleContent(
                conversationId = conversationId,
                message = message,
                chatMediaRepository = chatMediaRepository,
                isInlineVideoPlaying = isInlineVideoPlaying,
                onPlayInlineVideo = onPlayInlineVideo,
                onStopInlineVideo = onStopInlineVideo,
                onOpenFullscreenVideo = onOpenFullscreenVideo
              )
            }

            val isSyntheticMediaPlaceholder = message.hasMediaAttachment() &&
              (message.content.startsWith("📷 ") ||
                message.content.startsWith("🎬 ") ||
                message.content.startsWith("🎤 ") ||
                message.content.startsWith("📎 "))

            // Message Text / Optional Caption (suppressed for polls & pure synthetic media prefixes)
            if (!message.isPollMessage() && !isSyntheticMediaPlaceholder && message.content.isNotBlank()) {
              Text(
                text = message.content,
                style = MaterialTheme.typography.bodyMedium.copy(
                  fontSize = 15.sp,
                  lineHeight = 23.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
              )
            }
          }

          Spacer(modifier = Modifier.height(4.dp))

          // Subtle timestamp, pinned indicator & status row
          Row(
            modifier = Modifier.align(Alignment.End),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
          ) {
            if (isPinned) {
              Icon(
                imageVector = Icons.Default.PushPin,
                contentDescription = strings.pinnedMessageBannerTitle,
                modifier = Modifier.size(12.dp),
                tint = Color(0xFFF59E0B)
              )
            }
            if (message.isEdited) {
              Text(
                text = strings.edited,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f)
              )
            }
            if (formattedTime.isNotBlank()) {
              Text(
                text = formattedTime,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
              )
            }
            if (isMe) {
              Icon(
                imageVector = Icons.Default.DoneAll,
                contentDescription = "Delivered",
                modifier = Modifier.size(13.dp),
                tint = MaterialTheme.colorScheme.primary
              )
            }
          }
        }
      }

      DropdownMenu(
        expanded = showMenu,
        onDismissRequest = { showMenu = false },
        modifier = Modifier.background(MaterialTheme.colorScheme.surface)
      ) {
        if (!message.isDeleted) {
          val mySelectedEmojis = remember(message.reactions, currentUid) {
            message.reactions.filterValues { it.contains(currentUid) }.keys.toSet()
          }
          Box(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
            IosFloatingReactionBar(
              selectedEmojis = mySelectedEmojis,
              onEmojiSelected = { emoji ->
                showMenu = false
                onToggleReaction(emoji)
              },
              onMoreClick = {
                showMenu = false
                onOpenReactionSheet()
              }
            )
          }
          HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
          DropdownMenuItem(
            text = { Text(strings.reactAction, color = MaterialTheme.colorScheme.onSurface) },
            leadingIcon = {
              Icon(Icons.Outlined.AddReaction, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            },
            onClick = {
              showMenu = false
              onOpenReactionSheet()
            }
          )
        }
        DropdownMenuItem(
          text = { Text(strings.copy, color = MaterialTheme.colorScheme.onSurface) },
          leadingIcon = {
            Icon(Icons.Default.ContentCopy, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
          },
          onClick = {
            showMenu = false
            onCopy()
          }
        )
        DropdownMenuItem(
          text = { Text(strings.reply, color = MaterialTheme.colorScheme.onSurface) },
          leadingIcon = {
            Icon(Icons.AutoMirrored.Filled.Reply, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
          },
          onClick = {
            showMenu = false
            onReply()
          }
        )
        if (onForward != null) {
          DropdownMenuItem(
            text = { Text(strings.forwardAction, color = MaterialTheme.colorScheme.onSurface) },
            leadingIcon = {
              Icon(Icons.AutoMirrored.Filled.Forward, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            },
            onClick = {
              showMenu = false
              onForward()
            }
          )
        }
        if (onTogglePin != null) {
          DropdownMenuItem(
            text = {
              Text(
                text = if (isPinned) strings.unpinMessageAction else strings.pinMessageAction,
                color = MaterialTheme.colorScheme.onSurface
              )
            },
            leadingIcon = {
              Icon(
                imageVector = if (isPinned) Icons.Default.PushPin else Icons.Outlined.PushPin,
                contentDescription = null,
                tint = Color(0xFFF59E0B)
              )
            },
            onClick = {
              showMenu = false
              onTogglePin()
            }
          )
        }
        if (!isMe && !isAiMessage) {
          DropdownMenuItem(
            text = { Text(strings.inspectProfileAction, color = MaterialTheme.colorScheme.onSurface) },
            leadingIcon = {
              Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            },
            onClick = {
              showMenu = false
              onInspectSender()
            }
          )
        }
        DropdownMenuItem(
          text = { Text(strings.saveToMemory, color = MaterialTheme.colorScheme.onSurface) },
          leadingIcon = {
            Icon(Icons.Default.Bookmark, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
          },
          onClick = {
            showMenu = false
            onSaveMemory()
          }
        )
        if (isMe) {
          DropdownMenuItem(
            text = { Text(strings.edit, color = MaterialTheme.colorScheme.onSurface) },
            leadingIcon = {
              Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            },
            onClick = {
              showMenu = false
              onEdit()
            }
          )
          DropdownMenuItem(
            text = { Text(strings.delete, color = MaterialTheme.colorScheme.error) },
            leadingIcon = {
              Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
            },
            onClick = {
              showMenu = false
              onDelete()
            }
          )
        }
      }
      }

      if (message.reactions.isNotEmpty() && !message.isDeleted) {
        ReactionChipsRow(
          reactions = message.reactions,
          currentUid = currentUid,
          onToggleReaction = onToggleReaction,
          modifier = Modifier.padding(top = 4.dp)
        )
      }
    }
  }
}

@Composable
private fun AiErrorBanner(
  message: String,
  onRetry: () -> Unit,
  onDismiss: () -> Unit
) {
  val strings = LocalAppStrings.current

  Surface(
    shape = RoundedCornerShape(16.dp),
    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.35f)),
    modifier = Modifier
      .fillMaxWidth()
      .testTag("ai_error_banner")
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.ErrorOutline,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.onErrorContainer,
          modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = strings.aiErrorBannerTitle,
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onErrorContainer,
          modifier = Modifier.weight(1f)
        )
        IconButton(
          onClick = onDismiss,
          modifier = Modifier.size(32.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = strings.cancel,
            tint = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.size(16.dp)
          )
        }
      }
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = message,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.88f)
      )
      Spacer(modifier = Modifier.height(10.dp))
      Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
        Button(
          onClick = onRetry,
          colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error,
            contentColor = MaterialTheme.colorScheme.onError
          ),
          shape = RoundedCornerShape(10.dp),
          contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
          modifier = Modifier.testTag("ai_retry_button")
        ) {
          Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(15.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text(strings.retry, fontWeight = FontWeight.SemiBold)
        }
      }
    }
  }
}

@Composable
private fun ComposerContextStrip(
  label: String,
  previewText: String,
  onDismiss: () -> Unit
) {
  Surface(
    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
    shape = RoundedCornerShape(14.dp),
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 14.dp, vertical = 4.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .width(3.dp)
          .height(28.dp)
          .clip(RoundedCornerShape(2.dp))
          .background(MaterialTheme.colorScheme.primary)
      )
      Spacer(modifier = Modifier.width(10.dp))
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = label,
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary
        )
        Text(
          text = previewText,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }
      IconButton(
        onClick = onDismiss,
        modifier = Modifier.size(36.dp)
      ) {
        Icon(
          imageVector = Icons.Default.Close,
          contentDescription = "Cancel",
          tint = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.size(18.dp)
        )
      }
    }
  }
}

@Composable
private fun PolishedMessageComposer(
  value: String,
  onValueChange: (String) -> Unit,
  placeholder: String,
  isSendEnabled: Boolean,
  showAttachButton: Boolean,
  isAttachEnabled: Boolean,
  canRecordVoiceNote: Boolean = false,
  isRecordingVoice: Boolean = false,
  recordingElapsedMs: Long = 0L,
  isSpoilerMode: Boolean = false,
  onToggleSpoiler: () -> Unit = {},
  onStartRecording: () -> Unit = {},
  onCancelRecording: () -> Unit = {},
  onSendRecording: () -> Unit = {},
  onAttachClick: () -> Unit,
  onSend: () -> Unit
) {
  val strings = LocalAppStrings.current
  val useMicAction = canRecordVoiceNote && value.isBlank()
  val sendBtnBg by animateColorAsState(
    targetValue = when {
      useMicAction -> MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
      isSendEnabled -> MaterialTheme.colorScheme.primary
      else -> MaterialTheme.colorScheme.surfaceVariant
    },
    label = "send_btn_bg"
  )
  val sendIconTint by animateColorAsState(
    targetValue = when {
      useMicAction -> MaterialTheme.colorScheme.primary
      isSendEnabled -> MaterialTheme.colorScheme.onPrimary
      else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
    },
    label = "send_icon_tint"
  )

  Box(
    modifier = Modifier
      .fillMaxWidth()
      .navigationBarsPadding()
      .padding(horizontal = 14.dp, vertical = 10.dp)
  ) {
    if (isRecordingVoice) {
      Surface(
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.55f)),
        tonalElevation = 2.dp,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("voice_recording_bar")
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          IconButton(
            onClick = onCancelRecording,
            modifier = Modifier
              .size(48.dp)
              .testTag("cancel_voice_recording_button")
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = strings.cancelRecording,
              tint = MaterialTheme.colorScheme.error
            )
          }

          Box(
            modifier = Modifier
              .size(10.dp)
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.error)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "${strings.recordingVoiceNote} • ${ChatMediaRepository.formatDuration(recordingElapsedMs)}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )

          IconButton(
            onClick = onSendRecording,
            modifier = Modifier
              .size(48.dp)
              .testTag("send_voice_recording_button")
          ) {
            Box(
              modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = strings.sendVoiceNote,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(18.dp)
              )
            }
          }
        }
      }
    } else {
      Surface(
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f),
        border = BorderStroke(
          width = 1.dp,
          color = if (isSpoilerMode) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.65f)
          } else {
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
          }
        ),
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(
              start = if (showAttachButton) 4.dp else 18.dp,
              end = 6.dp,
              top = 6.dp,
              bottom = 6.dp
            ),
          verticalAlignment = Alignment.CenterVertically
        ) {
          if (showAttachButton) {
            IconButton(
              onClick = onAttachClick,
              enabled = isAttachEnabled,
              modifier = Modifier
                .size(48.dp)
                .testTag("chat_attach_button")
            ) {
              Icon(
                imageVector = Icons.Default.AttachFile,
                contentDescription = strings.attachMedia,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
              )
            }

            IconButton(
              onClick = onToggleSpoiler,
              enabled = isAttachEnabled,
              modifier = Modifier
                .size(48.dp)
                .testTag("chat_spoiler_toggle")
            ) {
              Icon(
                imageVector = if (isSpoilerMode) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                contentDescription = strings.spoilerToggleLabel,
                tint = if (isSpoilerMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
              )
            }
          }

          BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = MaterialTheme.typography.bodyMedium.copy(
              fontSize = 15.sp,
              color = MaterialTheme.colorScheme.onSurface
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            maxLines = 5,
            modifier = Modifier
              .weight(1f)
              .padding(vertical = 8.dp)
              .testTag("chat_message_input"),
            decorationBox = { innerTextField ->
              if (value.isEmpty()) {
                Text(
                  text = if (isSpoilerMode) "${strings.spoilerToggleLabel}: $placeholder" else placeholder,
                  style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp),
                  color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
              }
              innerTextField()
            }
          )

          Spacer(modifier = Modifier.width(6.dp))

          if (useMicAction) {
            IconButton(
              onClick = onStartRecording,
              modifier = Modifier
                .size(48.dp)
                .testTag("chat_record_voice_button")
            ) {
              Box(
                modifier = Modifier
                  .size(38.dp)
                  .clip(CircleShape)
                  .background(sendBtnBg),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Mic,
                  contentDescription = strings.voiceNoteLabel,
                  tint = sendIconTint,
                  modifier = Modifier.size(20.dp)
                )
              }
            }
          } else {
            IconButton(
              onClick = onSend,
              enabled = isSendEnabled,
              modifier = Modifier
                .size(48.dp)
                .testTag("chat_send_button")
            ) {
              Box(
                modifier = Modifier
                  .size(38.dp)
                  .clip(CircleShape)
                  .background(sendBtnBg),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.AutoMirrored.Filled.Send,
                  contentDescription = strings.send,
                  tint = sendIconTint,
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

@Composable
private fun PinnedMessageBanner(
  senderName: String,
  previewText: String,
  onJumpToPinned: () -> Unit,
  onUnpin: () -> Unit
) {
  val strings = LocalAppStrings.current
  Surface(
    onClick = onJumpToPinned,
    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f),
    modifier = Modifier
      .fillMaxWidth()
      .widthIn(max = 840.dp)
      .testTag("pinned_message_banner")
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 6.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        imageVector = Icons.Default.PushPin,
        contentDescription = strings.pinnedMessageBannerTitle,
        tint = Color(0xFFF59E0B),
        modifier = Modifier.size(18.dp)
      )
      Spacer(modifier = Modifier.width(10.dp))
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = if (senderName.isNotBlank()) "${strings.pinnedMessageBannerTitle} • $senderName" else strings.pinnedMessageBannerTitle,
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.Bold,
          color = Color(0xFFF59E0B),
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
        Text(
          text = previewText,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurface,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }
      IconButton(
        onClick = onUnpin,
        modifier = Modifier
          .size(48.dp)
          .testTag("unpin_message_banner_button")
      ) {
        Icon(
          imageVector = Icons.Default.Close,
          contentDescription = strings.unpinMessageAction,
          tint = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.size(18.dp)
        )
      }
    }
  }
}

@Composable
private fun ChatSearchBarStrip(
  query: String,
  onQueryChange: (String) -> Unit,
  matchCount: Int,
  currentMatchIndex: Int,
  onPrevMatch: () -> Unit,
  onNextMatch: () -> Unit,
  onClose: () -> Unit
) {
  val strings = LocalAppStrings.current
  Surface(
    color = MaterialTheme.colorScheme.surface,
    tonalElevation = 1.dp,
    modifier = Modifier
      .fillMaxWidth()
      .widthIn(max = 840.dp)
      .testTag("chat_search_bar")
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 10.dp, vertical = 6.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text(strings.searchMessagesPlaceholder, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
          .weight(1f)
          .testTag("chat_search_input")
      )
      if (query.isNotBlank()) {
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = if (matchCount > 0) {
            String.format(strings.searchResultsCountFormat, currentMatchIndex + 1, matchCount)
          } else {
            "0 / 0"
          },
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          fontWeight = FontWeight.SemiBold
        )
        IconButton(
          onClick = onPrevMatch,
          enabled = matchCount > 0,
          modifier = Modifier.size(48.dp)
        ) {
          Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Previous")
        }
        IconButton(
          onClick = onNextMatch,
          enabled = matchCount > 0,
          modifier = Modifier.size(48.dp)
        ) {
          Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Next")
        }
      }
      IconButton(
        onClick = onClose,
        modifier = Modifier.size(48.dp)
      ) {
        Icon(Icons.Default.Close, contentDescription = strings.cancel)
      }
    }
  }
}

@Composable
private fun CreatePollDialog(
  onDismiss: () -> Unit,
  onConfirm: (question: String, options: List<String>) -> Unit
) {
  val strings = LocalAppStrings.current
  var question by remember { mutableStateOf("") }
  val options = remember { mutableStateListOf("", "") }
  val validOptions = options.map { it.trim() }.filter { it.isNotBlank() }.distinct()
  val canSubmit = question.trim().isNotBlank() && validOptions.size >= 2

  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = MaterialTheme.colorScheme.surface,
    titleContentColor = MaterialTheme.colorScheme.onSurface,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Poll, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(8.dp))
        Text(strings.createPollAction, fontWeight = FontWeight.Bold)
      }
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
          value = question,
          onValueChange = { if (it.length <= 240) question = it },
          label = { Text(strings.pollQuestionLabel) },
          placeholder = { Text(strings.pollQuestionPlaceholder) },
          maxLines = 2,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("poll_question_input")
        )
        options.forEachIndexed { idx, value ->
          OutlinedTextField(
            value = value,
            onValueChange = { updated -> if (updated.length <= 120) options[idx] = updated },
            label = { Text(String.format(strings.pollOptionLabel, idx + 1)) },
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("poll_option_input_$idx")
          )
        }
        if (options.size < 5) {
          TextButton(
            onClick = { options.add("") },
            modifier = Modifier.align(Alignment.Start)
          ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(strings.addPollOption)
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = { onConfirm(question, validOptions) },
        enabled = canSubmit,
        modifier = Modifier.testTag("poll_submit_button")
      ) {
        Text(strings.sendPoll)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text(strings.cancel)
      }
    }
  )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ForwardMessageSheet(
  conversations: List<Conversation>,
  directoryUsers: List<UserProfile>,
  currentUid: String,
  onDismiss: () -> Unit,
  onSelectConversation: (Conversation) -> Unit
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
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      Text(
        text = strings.forwardToTitle,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
      )
      LazyColumn(
        modifier = Modifier
          .fillMaxWidth()
          .heightIn(max = 360.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        items(conversations, key = { it.id }) { convo ->
          val peerId = if (convo.type == "direct") convo.participantIds.firstOrNull { it != currentUid } else null
          val peer = peerId?.let { id -> directoryUsers.find { it.id == id } }
          val title = peer?.displayName?.takeIf { it.isNotBlank() }
            ?: convo.title.ifBlank { strings.directChats }

          Surface(
            onClick = { onSelectConversation(convo) },
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
            modifier = Modifier
              .fillMaxWidth()
              .defaultMinSize(minHeight = 56.dp)
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              AvatarView(
                name = title,
                avatarUrl = peer?.avatarUrl,
                size = 38.dp,
                frameStyleKey = peer?.equippedFrame
              )
              Spacer(modifier = Modifier.width(12.dp))
              Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
              Icon(
                imageVector = Icons.AutoMirrored.Filled.Forward,
                contentDescription = strings.forwardAction,
                tint = MaterialTheme.colorScheme.primary
              )
            }
          }
        }
      }
      Spacer(modifier = Modifier.height(8.dp))
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatInfoAndMediaSheet(
  conversationTitle: String,
  conversationType: String,
  participants: List<UserProfile>,
  messages: List<Message>,
  onDismiss: () -> Unit,
  onInspectParticipant: (UserProfile) -> Unit,
  onJumpToMessage: (String) -> Unit
) {
  val strings = LocalAppStrings.current
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  var selectedFilter by remember { mutableStateOf("image") }

  val filteredItems = remember(messages, selectedFilter) {
    messages.filter { msg ->
      !msg.isDeleted && when (selectedFilter) {
        "poll" -> msg.isPollMessage()
        else -> msg.hasMediaAttachment() && msg.mediaType == selectedFilter
      }
    }.asReversed()
  }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = MaterialTheme.colorScheme.surface,
    contentColor = MaterialTheme.colorScheme.onSurface,
    modifier = Modifier.testTag("chat_info_sheet")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .navigationBarsPadding()
        .padding(horizontal = 20.dp, vertical = 12.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = strings.chatInfoTitle,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = conversationTitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }
        IconButton(onClick = onDismiss, modifier = Modifier.size(48.dp)) {
          Icon(Icons.Default.Close, contentDescription = strings.cancel)
        }
      }

      if (participants.isNotEmpty()) {
        Text(
          text = if (conversationType == "group") {
            String.format(strings.membersCountFormat, participants.size)
          } else {
            strings.inspectProfileMemberTitle
          },
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary
        )
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          participants.forEach { member ->
            Surface(
              onClick = { onInspectParticipant(member) },
              shape = RoundedCornerShape(12.dp),
              color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
              border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
              modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 52.dp)
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                AvatarView(
                  name = member.displayName.ifBlank { "Member" },
                  avatarUrl = member.avatarUrl,
                  size = 34.dp,
                  frameStyleKey = member.equippedFrame
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                  text = member.displayName.ifBlank { "Member" },
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.SemiBold,
                  modifier = Modifier.weight(1f),
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
                if (member.isVerified) {
                  VerifiedBadge(size = 14.dp, tierKey = member.verifiedTier)
                  Spacer(modifier = Modifier.width(6.dp))
                }
                Text(
                  text = strings.inspectProfileAction,
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.primary,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }
        }
      }

      val filters = remember(strings) {
        listOf(
          "image" to strings.sharedMediaTabPhotos,
          "video" to strings.sharedMediaTabVideos,
          "audio" to strings.sharedMediaTabAudio,
          "file" to strings.sharedMediaTabFiles,
          "poll" to strings.sharedMediaTabPolls
        )
      }

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        filters.forEach { (key, label) ->
          FilterChip(
            selected = selectedFilter == key,
            onClick = { selectedFilter = key },
            label = { Text(label, maxLines = 1, softWrap = false) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
              selectedLabelColor = MaterialTheme.colorScheme.primary
            )
          )
        }
      }

      if (filteredItems.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = strings.noSharedMediaYet,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      } else {
        LazyColumn(
          modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 260.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          items(filteredItems, key = { it.id }) { msg ->
            Surface(
              onClick = { onJumpToMessage(msg.id) },
              shape = RoundedCornerShape(12.dp),
              color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
              border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                val icon = when (selectedFilter) {
                  "image" -> Icons.Default.Image
                  "video" -> Icons.Default.Videocam
                  "audio" -> Icons.Default.Mic
                  "poll" -> Icons.Default.Poll
                  else -> Icons.Default.Description
                }
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = msg.mediaFileName?.takeIf { it.isNotBlank() } ?: msg.content,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                  Text(
                    text = msg.senderName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(6.dp))
    }
  }
}
