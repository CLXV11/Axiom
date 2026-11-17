package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import com.example.data.repository.ChatRepository
import com.example.data.repository.PostRepository
import com.example.data.repository.UserRepository
import com.example.ui.components.AvatarView
import com.example.ui.components.TypingBubble
import com.example.ui.components.TypingDotsIndicator
import com.example.ui.components.VerifiedBadge
import com.example.ui.theme.KoraTheme
import com.example.ui.theme.StatusOnline
import com.example.ui.util.LocalAppStrings
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

private typealias UnitCallback = () -> Unit

@Composable
fun ChatDetailScreen(
  conversation: Conversation,
  chatRepository: ChatRepository,
  postRepository: PostRepository,
  userRepository: UserRepository,
  onBack: UnitCallback
) {
  BackHandler { onBack() }

  val context = LocalContext.current
  val strings = LocalAppStrings.current
  val coroutineScope = rememberCoroutineScope()
  val listState = rememberLazyListState()

  val isAiChat = remember(conversation.id) {
    chatRepository.isAiChatbotConversation(conversation.id)
  }

  val currentUid = userRepository.requireUserId()
  var currentUserName by remember { mutableStateOf("Me") }

  LaunchedEffect(currentUid) {
    val profile = userRepository.getUserProfile(currentUid)
    if (profile?.displayName?.isNotBlank() == true) {
      currentUserName = profile.displayName
    }
  }

  val messages by chatRepository.observeMessages(conversation.id).collectAsState(initial = emptyList())
  val typingUsers by chatRepository.observeTyping(conversation.id).collectAsState(initial = emptyList())
  val aiState by chatRepository.observeAiState(conversation.id).collectAsState(initial = AiConversationState())
  val directoryUsers by userRepository.observeDirectoryUsers()
    .collectAsState(initial = userRepository.getDirectoryUsers())

  val peerId = remember(conversation.participantIds, currentUid, isAiChat) {
    if (!isAiChat && conversation.type == "direct") {
      conversation.participantIds.firstOrNull { it != currentUid }
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

  LaunchedEffect(conversation.id, messages.size) {
    if (!isAiChat && messages.isNotEmpty()) {
      chatRepository.markAsRead(conversation.id)
    }
  }

  val peerTypingUsers = remember(typingUsers, currentUserName) {
    typingUsers.filter { it != currentUserName }
  }

  var messageInput by remember { mutableStateOf("") }
  var replyingToMessage by remember { mutableStateOf<Message?>(null) }
  var editingMessage by remember { mutableStateOf<Message?>(null) }

  // Debounced user typing state for multi-user chats
  LaunchedEffect(messageInput) {
    if (!isAiChat && messageInput.isNotBlank()) {
      chatRepository.setTyping(conversation.id, currentUserName, true)
      delay(2200)
      chatRepository.setTyping(conversation.id, currentUserName, false)
    } else if (!isAiChat) {
      chatRepository.setTyping(conversation.id, currentUserName, false)
    }
  }

  DisposableEffect(conversation.id) {
    onDispose {
      chatRepository.setTyping(conversation.id, currentUserName, false)
    }
  }

  // Save to Memory Dialog state
  var memoryTargetMessage by remember { mutableStateOf<Message?>(null) }
  var privateNoteInput by remember { mutableStateOf("") }

  // Auto-scroll smoothly as messages or streaming chunks arrive
  val lastContentLength = messages.lastOrNull()?.content?.length ?: 0
  LaunchedEffect(messages.size, lastContentLength, peerTypingUsers.size, aiState.errorMessage) {
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

    chatRepository.setTyping(conversation.id, currentUserName, false)

    coroutineScope.launch {
      val userProfile = runCatching { userRepository.getUserProfile(currentUid) }.getOrNull()
      val senderName = userProfile?.displayName ?: currentUserName
      val isVerified = userProfile?.isVerified ?: false
      val verifiedTier = userProfile?.verifiedTier ?: "GOLD"

      if (editingMessage != null) {
        val res = chatRepository.editMessage(conversation.id, editingMessage!!.id, text)
        if (res.isSuccess) {
          editingMessage = null
          messageInput = ""
        } else {
          Toast.makeText(context, strings.errorOccurred, Toast.LENGTH_SHORT).show()
        }
      } else {
        val pendingReply = replyingToMessage
        messageInput = ""
        replyingToMessage = null
        val res = chatRepository.sendMessage(
          conversationId = conversation.id,
          content = text,
          senderName = senderName,
          replyTo = pendingReply,
          isSenderVerified = isVerified,
          senderVerifiedTier = verifiedTier
        )
        if (res.isFailure) {
          messageInput = text
          replyingToMessage = pendingReply
          Toast.makeText(context, strings.errorOccurred, Toast.LENGTH_SHORT).show()
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

  val isGroup = conversation.type == "group"
  val convoTitle = when {
    isAiChat -> strings.aiAssistantName
    peerUser != null && peerUser.displayName.isNotBlank() -> peerUser.displayName
    conversation.title.isNotBlank() -> conversation.title
    isGroup -> strings.groupChats
    else -> strings.directChats
  }

  val headerSubtitle = remember(isAiChat, peerTypingUsers, aiState.isGenerating, conversation, peerUser, strings) {
    when {
      isAiChat && aiState.isGenerating -> strings.aiThinking
      peerTypingUsers.isNotEmpty() -> when {
        peerTypingUsers.size == 1 -> String.format(strings.typingSingle, peerTypingUsers.first())
        peerTypingUsers.size == 2 -> "${peerTypingUsers[0]} & ${peerTypingUsers[1]} ${strings.isTyping}"
        else -> strings.typingMultiple
      }
      isAiChat -> strings.aiAssistantSubtitle
      isGroup -> "${conversation.participantIds.size} members"
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
        isAiChat = isAiChat,
        isVerified = peerUser?.isVerified ?: conversation.isVerified,
        verifiedTier = peerUser?.verifiedTier ?: conversation.verifiedTier,
        isThinkingOrTyping = aiState.isGenerating || peerTypingUsers.isNotEmpty(),
        canClearAiChat = isAiChat && messages.isNotEmpty(),
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
        .imePadding()
    ) {
      // Message Stream or Calm AI Welcome Canvas
      Box(
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth()
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
              val senderAvatarUrl = directoryUsers.find { it.id == msg.senderId }?.avatarUrl
              PolishedMessageBubble(
                message = msg,
                isMe = isMe,
                isAiMessage = isBotMsg,
                senderAvatarUrl = senderAvatarUrl,
                onReply = {
                  replyingToMessage = msg
                  editingMessage = null
                },
                onCopy = { copyToClipboard(msg.content) },
                onEdit = {
                  editingMessage = msg
                  replyingToMessage = null
                  messageInput = msg.content
                },
                onDelete = {
                  coroutineScope.launch {
                    val res = chatRepository.deleteMessage(
                      conversationId = conversation.id,
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
                  onDismiss = { chatRepository.clearAiError(conversation.id) }
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

      // Floating Soft Pill Composer
      PolishedMessageComposer(
        value = messageInput,
        onValueChange = { messageInput = it },
        placeholder = strings.typeMessage,
        isSendEnabled = messageInput.isNotBlank() && !(isAiChat && aiState.isGenerating),
        onSend = { sendText(messageInput) }
      )
    }
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
}

@Composable
private fun PolishedChatTopBar(
  title: String,
  subtitle: String,
  avatarUrl: String?,
  isAiChat: Boolean,
  isVerified: Boolean,
  verifiedTier: String?,
  isThinkingOrTyping: Boolean,
  canClearAiChat: Boolean,
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
          .height(64.dp)
          .padding(horizontal = 8.dp),
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

        Spacer(modifier = Modifier.width(4.dp))

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
            showPresence = true,
            isOnline = true
          )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = title,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            if (isVerified) {
              Spacer(modifier = Modifier.width(6.dp))
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
                maxLines = 1
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
                maxLines = 1
              )
            }
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
  message: Message,
  isMe: Boolean,
  isAiMessage: Boolean,
  senderAvatarUrl: String?,
  onReply: () -> Unit,
  onCopy: () -> Unit,
  onEdit: () -> Unit,
  onDelete: () -> Unit,
  onSaveMemory: () -> Unit
) {
  val strings = LocalAppStrings.current
  var showMenu by remember { mutableStateOf(false) }

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

  val bubbleContainerColor = if (isMe) {
    MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
  } else {
    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
  }

  val bubbleBorderColor = if (isMe) {
    MaterialTheme.colorScheme.primary.copy(alpha = 0.28f)
  } else {
    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
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
        AvatarView(
          name = message.senderName,
          avatarUrl = senderAvatarUrl,
          size = 30.dp
        )
      }
      Spacer(modifier = Modifier.width(8.dp))
    }

    Box {
      Surface(
        shape = bubbleShape,
        color = bubbleContainerColor,
        border = BorderStroke(1.dp, bubbleBorderColor),
        modifier = Modifier
          .widthIn(max = 310.dp)
          .clip(bubbleShape)
          .combinedClickable(
            onClick = {},
            onLongClick = { showMenu = true }
          )
      ) {
        Column(
          modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
          // Incoming sender badge row
          if (!isMe) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(bottom = 4.dp)
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

          // Message Text
          Text(
            text = message.content,
            style = MaterialTheme.typography.bodyMedium.copy(
              fontSize = 15.sp,
              lineHeight = 23.sp
            ),
            color = MaterialTheme.colorScheme.onSurface
          )

          Spacer(modifier = Modifier.height(4.dp))

          // Subtle timestamp & status row
          Row(
            modifier = Modifier.align(Alignment.End),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
          ) {
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
  onSend: () -> Unit
) {
  val strings = LocalAppStrings.current
  val sendBtnBg by animateColorAsState(
    targetValue = if (isSendEnabled) {
      MaterialTheme.colorScheme.primary
    } else {
      MaterialTheme.colorScheme.surfaceVariant
    },
    label = "send_btn_bg"
  )
  val sendIconTint by animateColorAsState(
    targetValue = if (isSendEnabled) {
      MaterialTheme.colorScheme.onPrimary
    } else {
      MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
    },
    label = "send_icon_tint"
  )

  Box(
    modifier = Modifier
      .fillMaxWidth()
      .navigationBarsPadding()
      .padding(horizontal = 14.dp, vertical = 10.dp)
  ) {
    Surface(
      shape = RoundedCornerShape(28.dp),
      color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f),
      border = BorderStroke(
        width = 1.dp,
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
      ),
      tonalElevation = 2.dp,
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(start = 18.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
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
                text = placeholder,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
              )
            }
            innerTextField()
          }
        )

        Spacer(modifier = Modifier.width(8.dp))

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
