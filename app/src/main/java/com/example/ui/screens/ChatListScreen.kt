package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Contacts
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Conversation
import com.example.data.model.UserProfile
import com.example.data.repository.ChatRepository
import com.example.data.repository.UserRepository
import com.example.ui.components.AvatarView
import com.example.ui.components.ContactDirectorySheet
import com.example.ui.components.EmptyStateView
import com.example.ui.components.KoraTopBar
import com.example.ui.components.TypingDotsIndicator
import com.example.ui.components.VerifiedBadge
import com.example.ui.util.LocalAppStrings
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
  chatRepository: ChatRepository,
  userRepository: UserRepository,
  onOpenConversation: (Conversation) -> Unit,
  onNewChatClicked: () -> Unit,
  onLanguageToggle: () -> Unit,
  onOpenStore: () -> Unit = {},
  onOpenSettings: (() -> Unit)? = null,
  onInspectUser: (UserProfile) -> Unit = {},
  compactMode: Boolean = false
) {
  val context = LocalContext.current
  val strings = LocalAppStrings.current
  val colors = MaterialTheme.colorScheme
  val coroutineScope = rememberCoroutineScope()
  val currentUid = userRepository.requireUserId()
  val conversations by chatRepository.observeConversations().collectAsState(initial = emptyList())
  val directoryUsers by userRepository.observeDirectoryUsers()
    .collectAsState(initial = userRepository.getDirectoryUsers())
  val usersById = remember(directoryUsers) { directoryUsers.associateBy { it.id } }
  var extraPeers by remember { mutableStateOf<Map<String, UserProfile>>(emptyMap()) }

  LaunchedEffect(Unit) {
    userRepository.fetchDirectoryUsers()
  }

  LaunchedEffect(conversations, usersById) {
    val missingPeerIds = conversations
      .filter { !chatRepository.isAiChatbotConversation(it.id) && it.type == "direct" }
      .mapNotNull { convo -> convo.participantIds.firstOrNull { it != currentUid } }
      .distinct()
      .filter { usersById[it] == null && extraPeers[it] == null }
    if (missingPeerIds.isNotEmpty()) {
      val fetched = mutableMapOf<String, UserProfile>()
      for (peerId in missingPeerIds) {
        val loaded = runCatching { userRepository.getUserProfile(peerId) }.getOrNull()
        if (loaded != null) {
          fetched[peerId] = loaded
        }
      }
      if (fetched.isNotEmpty()) {
        extraPeers = extraPeers + fetched
      }
    }
  }

  var searchQuery by remember { mutableStateOf("") }
  var selectedFilter by remember { mutableStateOf("all") }
  var showContactDirectorySheet by remember { mutableStateOf(false) }

  fun resolvePeer(convo: Conversation): UserProfile? {
    if (chatRepository.isAiChatbotConversation(convo.id) || convo.type != "direct") return null
    val peerId = convo.participantIds.firstOrNull { it != currentUid } ?: return null
    return usersById[peerId] ?: extraPeers[peerId]
  }

  val archivedCount = remember(conversations, currentUid) {
    conversations.count { it.archivedBy.contains(currentUid) }
  }
  val activeCount = remember(conversations, archivedCount) {
    (conversations.size - archivedCount).coerceAtLeast(0)
  }

  LaunchedEffect(archivedCount, selectedFilter) {
    if (selectedFilter == "archived" && archivedCount == 0) {
      selectedFilter = "all"
    }
  }

  val filteredConversations = conversations.filter { convo ->
    val isAi = convo.id == ChatRepository.AI_CHATBOT_CONVO_ID
    val isArchived = !isAi && convo.archivedBy.contains(currentUid)
    val peer = resolvePeer(convo)
    val displayTitle = when {
      isAi -> strings.aiAssistantName
      peer != null -> peer.displayName
      else -> convo.title
    }
    val matchesFilter = when (selectedFilter) {
      "direct" -> !isArchived && convo.type == "direct"
      "group" -> !isArchived && convo.type == "group"
      "archived" -> isArchived
      else -> !isArchived
    }
    val matchesSearch = if (searchQuery.isBlank()) true else {
      displayTitle.contains(searchQuery, ignoreCase = true) ||
        convo.title.contains(searchQuery, ignoreCase = true) ||
        (peer?.handle?.contains(searchQuery, ignoreCase = true) == true) ||
        (peer?.profession?.contains(searchQuery, ignoreCase = true) == true) ||
        convo.lastMessageText.contains(searchQuery, ignoreCase = true)
    }
    matchesFilter && matchesSearch
  }

  Scaffold(
    containerColor = colors.background,
    contentWindowInsets = WindowInsets(0, 0, 0, 0),
    topBar = {
      KoraTopBar(
        title = strings.conversations,
        subtitle = if (activeCount <= 1) strings.aiAssistantSubtitle else String.format(strings.activeChatsCount, activeCount),
        onLanguageToggle = onLanguageToggle,
        actions = {
          IconButton(
            onClick = { showContactDirectorySheet = true },
            modifier = Modifier.testTag("open_contact_directory_button")
          ) {
            Icon(
              imageVector = Icons.Outlined.Contacts,
              contentDescription = strings.contactDirectoryTitle,
              tint = colors.onSurfaceVariant
            )
          }
          if (onOpenSettings != null) {
            IconButton(onClick = onOpenSettings) {
              Icon(
                imageVector = Icons.Outlined.Settings,
                contentDescription = strings.settingsTitle,
                tint = colors.onSurfaceVariant
              )
            }
          } else {
            IconButton(onClick = onOpenStore) {
              Icon(
                imageVector = Icons.Outlined.AutoAwesome,
                contentDescription = strings.customDecoration,
                tint = colors.onSurfaceVariant
              )
            }
          }
        }
      )
    },
    floatingActionButton = {
      FloatingActionButton(
        onClick = onNewChatClicked,
        containerColor = colors.primary,
        contentColor = colors.onPrimary,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.testTag("new_chat_fab")
      ) {
        Icon(imageVector = Icons.Default.Add, contentDescription = strings.newChat)
      }
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
          .fillMaxSize()
          .widthIn(max = 840.dp)
      ) {
        // Recessed Discord-style Search Bar
        OutlinedTextField(
          value = searchQuery,
          onValueChange = { searchQuery = it },
          placeholder = {
            Text(
              text = strings.searchChats,
              color = colors.onSurfaceVariant,
              style = MaterialTheme.typography.bodyMedium,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          },
          leadingIcon = {
            Icon(
              imageVector = Icons.Outlined.Search,
              contentDescription = null,
              tint = colors.onSurfaceVariant,
              modifier = Modifier.size(20.dp)
            )
          },
          singleLine = true,
          shape = RoundedCornerShape(12.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = colors.surfaceContainerLowest,
            unfocusedContainerColor = colors.surfaceContainerLowest,
            focusedBorderColor = colors.primary.copy(alpha = 0.7f),
            unfocusedBorderColor = colors.outlineVariant.copy(alpha = 0.55f),
            focusedTextColor = colors.onSurface,
            unfocusedTextColor = colors.onSurface
          ),
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .testTag("search_conversations_input")
        )

        // Uniform Filter Pill Strip
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 2.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          DiscordFilterPill(
            label = strings.filterAll,
            selected = selectedFilter == "all",
            onClick = { selectedFilter = "all" }
          )
          DiscordFilterPill(
            label = strings.directChats,
            selected = selectedFilter == "direct",
            onClick = { selectedFilter = "direct" }
          )
          DiscordFilterPill(
            label = strings.groupChats,
            selected = selectedFilter == "group",
            onClick = { selectedFilter = "group" }
          )
          if (archivedCount > 0) {
            DiscordFilterPill(
              label = "${strings.filterArchived} ($archivedCount)",
              selected = selectedFilter == "archived",
              onClick = { selectedFilter = "archived" }
            )
          }
        }

        Spacer(modifier = Modifier.height(6.dp))

        if (filteredConversations.isEmpty()) {
          EmptyStateView(
            icon = Icons.Outlined.ChatBubbleOutline,
            title = strings.noChatsYet,
            description = strings.startFirstConversation,
            actionLabel = strings.newChat,
            onAction = onNewChatClicked
          )
        } else {
          LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
              horizontal = 14.dp,
              vertical = if (compactMode) 4.dp else 8.dp
            ),
            verticalArrangement = Arrangement.spacedBy(if (compactMode) 6.dp else 8.dp)
          ) {
            items(filteredConversations, key = { it.id }) { convo ->
              val typingUsers by chatRepository.observeTyping(convo.id).collectAsState(initial = emptyList())
              val peerUser = resolvePeer(convo)
              val isAiConvo = chatRepository.isAiChatbotConversation(convo.id)
              val peerId = if (!isAiConvo && convo.type == "direct") {
                convo.participantIds.firstOrNull { it != currentUid }
              } else null
              ConversationRow(
                conversation = convo,
                peerUser = peerUser,
                isAiChatbot = isAiConvo,
                currentUserId = currentUid,
                typingUsers = typingUsers,
                compactMode = compactMode,
                onClick = { onOpenConversation(convo) },
                onInspectPeer = if (peerId != null) {
                  {
                    onInspectUser(
                      peerUser ?: UserProfile(
                        id = peerId,
                        displayName = convo.title,
                        isVerified = convo.isVerified,
                        verifiedTier = convo.verifiedTier
                      )
                    )
                  }
                } else null,
                onTogglePin = { pin ->
                  coroutineScope.launch { chatRepository.togglePin(convo.id, pin) }
                },
                onToggleArchive = { archive ->
                  coroutineScope.launch { chatRepository.toggleArchive(convo.id, archive) }
                },
                onToggleMute = { mute ->
                  coroutineScope.launch { chatRepository.toggleMute(convo.id, mute) }
                }
              )
            }
          }
        }
      }
    }
  }

  // Contact Directory Sheet
  if (showContactDirectorySheet) {
    ContactDirectorySheet(
      userRepository = userRepository,
      onDismiss = { showContactDirectorySheet = false },
      onSelectUserToMessage = { user ->
        showContactDirectorySheet = false
        coroutineScope.launch {
          val res = chatRepository.findOrCreateDirectConversation(
            targetUserId = user.id,
            title = user.displayName,
            isVerified = user.isVerified,
            verifiedTier = user.verifiedTier
          )
          if (res.isSuccess) {
            val convoId = res.getOrThrow()
            val convo = conversations.find { it.id == convoId }
              ?: Conversation(
                id = convoId,
                title = user.displayName,
                type = "direct",
                isVerified = user.isVerified,
                verifiedTier = user.verifiedTier,
                participantIds = listOf(currentUid, user.id)
              )
            onOpenConversation(convo)
          } else {
            Toast.makeText(
              context,
              res.exceptionOrNull()?.localizedMessage ?: strings.errorOccurred,
              Toast.LENGTH_SHORT
            ).show()
          }
        }
      },
      onInspectUser = { user ->
        showContactDirectorySheet = false
        onInspectUser(user)
      }
    )
  }
}

@Composable
private fun DiscordFilterPill(
  label: String,
  selected: Boolean,
  onClick: () -> Unit
) {
  val colors = MaterialTheme.colorScheme
  Surface(
    onClick = onClick,
    shape = RoundedCornerShape(10.dp),
    color = if (selected) colors.primary.copy(alpha = 0.18f) else colors.surface,
    border = BorderStroke(
      width = 1.dp,
      color = if (selected) colors.primary.copy(alpha = 0.6f) else colors.outlineVariant.copy(alpha = 0.65f)
    ),
    modifier = Modifier.defaultMinSize(minHeight = 34.dp)
  ) {
    Box(
      modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        color = if (selected) colors.primary else colors.onSurfaceVariant,
        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
        maxLines = 1,
        softWrap = false
      )
    }
  }
}

@Composable
private fun ConversationRow(
  conversation: Conversation,
  peerUser: UserProfile? = null,
  isAiChatbot: Boolean,
  currentUserId: String,
  typingUsers: List<String> = emptyList(),
  compactMode: Boolean = false,
  onClick: () -> Unit,
  onInspectPeer: (() -> Unit)? = null,
  onTogglePin: (Boolean) -> Unit,
  onToggleArchive: (Boolean) -> Unit,
  onToggleMute: (Boolean) -> Unit
) {
  val strings = LocalAppStrings.current
  val colors = MaterialTheme.colorScheme
  var showMenu by remember { mutableStateOf(false) }

  val isPinned = conversation.pinnedBy.contains(currentUserId)
  val isArchived = conversation.archivedBy.contains(currentUserId)
  val isMuted = conversation.mutedBy.contains(currentUserId)

  val title = when {
    isAiChatbot -> strings.aiAssistantName
    peerUser != null && peerUser.displayName.isNotBlank() -> peerUser.displayName
    conversation.title.isNotBlank() -> conversation.title
    conversation.type == "group" -> strings.groupChats
    else -> strings.directChats
  }
  val isRowVerified = peerUser?.isVerified ?: conversation.isVerified
  val rowVerifiedTier = peerUser?.verifiedTier ?: conversation.verifiedTier
  val formattedTime = remember(conversation.lastMessageTimestamp, conversation.createdAt) {
    formatConversationTimestamp(conversation.lastMessageTimestamp ?: conversation.createdAt)
  }

  Surface(
    shape = RoundedCornerShape(14.dp),
    color = colors.surface,
    border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.6f)),
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(14.dp))
      .clickable(onClick = onClick)
      .testTag(if (isAiChatbot) "ai_chatbot_conversation_card" else "conversation_card_${conversation.id}")
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(
          horizontal = 14.dp,
          vertical = if (compactMode) 9.dp else 12.dp
        ),
      verticalAlignment = Alignment.CenterVertically
    ) {
      if (isAiChatbot) {
        Box(
          modifier = Modifier
            .size(if (compactMode) 42.dp else 46.dp)
            .clip(CircleShape)
            .background(colors.primary.copy(alpha = 0.16f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Outlined.AutoAwesome,
            contentDescription = title,
            tint = colors.primary,
            modifier = Modifier.size(22.dp)
          )
        }
      } else {
        AvatarView(
          name = title,
          avatarUrl = peerUser?.avatarUrl,
          size = if (compactMode) 42.dp else 46.dp,
          frameStyleKey = peerUser?.equippedFrame,
          showPresence = conversation.type == "direct",
          isOnline = !(peerUser?.quietMode ?: false),
          isQuiet = peerUser?.quietMode == true,
          modifier = Modifier
            .clip(CircleShape)
            .then(
              if (onInspectPeer != null) {
                Modifier
                  .clickable(onClick = onInspectPeer)
                  .testTag("inspect_peer_avatar_${conversation.id}")
              } else Modifier
            )
        )
      }

      Spacer(modifier = Modifier.width(12.dp))

      Column(modifier = Modifier.weight(1f)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
          ) {
            if (isPinned && !isAiChatbot) {
              Icon(
                imageVector = Icons.Outlined.PushPin,
                contentDescription = "Pinned",
                tint = colors.primary,
                modifier = Modifier.size(14.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
              text = title,
              style = MaterialTheme.typography.titleSmall.copy(fontSize = 15.sp),
              fontWeight = FontWeight.SemiBold,
              color = colors.onSurface,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
              modifier = Modifier.weight(1f, fill = false)
            )
            if (isRowVerified) {
              Spacer(modifier = Modifier.width(5.dp))
              VerifiedBadge(
                size = 14.dp,
                tierKey = rowVerifiedTier,
                showDialogOnClick = true
              )
            }
            if (isAiChatbot) {
              Spacer(modifier = Modifier.width(6.dp))
              Surface(
                shape = RoundedCornerShape(6.dp),
                color = colors.primary.copy(alpha = 0.16f)
              ) {
                Text(
                  text = strings.aiBotBadge,
                  style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                  color = colors.primary,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
              }
            }
          }

          if (formattedTime.isNotBlank()) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = formattedTime,
              style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
              color = colors.onSurfaceVariant,
              maxLines = 1,
              softWrap = false
            )
          }
        }

        Spacer(modifier = Modifier.height(3.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          if (typingUsers.isNotEmpty()) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.weight(1f)
            ) {
              TypingDotsIndicator(
                dotSize = 4.dp,
                travelDistance = 2.dp,
                dotColor = colors.primary
              )
              Spacer(modifier = Modifier.width(6.dp))
              val typingText = if (isAiChatbot) {
                strings.aiThinking
              } else when {
                typingUsers.size == 1 -> String.format(strings.typingSingle, typingUsers.first())
                typingUsers.size == 2 -> "${typingUsers[0]} & ${typingUsers[1]} ${strings.isTyping}"
                else -> strings.typingMultiple
              }
              Text(
                text = typingText,
                style = MaterialTheme.typography.bodySmall,
                color = colors.primary,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }
          } else {
            val previewLine = when {
              conversation.lastMessageText.isNotBlank() -> conversation.lastMessageText
              isAiChatbot -> strings.aiWelcomeTitle
              else -> strings.startFirstConversation
            }
            Text(
              text = previewLine,
              style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
              color = colors.onSurfaceVariant,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
              modifier = Modifier.weight(1f)
            )
          }

          if (isMuted) {
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
              imageVector = Icons.Outlined.NotificationsOff,
              contentDescription = "Muted",
              tint = colors.onSurfaceVariant,
              modifier = Modifier.size(14.dp)
            )
          }
        }
      }

      if (!isAiChatbot) {
        Box {
          IconButton(
            onClick = { showMenu = true },
            modifier = Modifier.size(48.dp)
          ) {
            Icon(
              imageVector = Icons.Default.MoreVert,
              contentDescription = "Actions",
              tint = colors.onSurfaceVariant,
              modifier = Modifier.size(19.dp)
            )
          }

          DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
            modifier = Modifier.background(colors.surfaceContainerHigh)
          ) {
            if (onInspectPeer != null) {
              DropdownMenuItem(
                text = { Text(strings.inspectProfileAction, color = colors.onSurface) },
                onClick = {
                  showMenu = false
                  onInspectPeer()
                }
              )
            }
            DropdownMenuItem(
              text = { Text(if (isPinned) strings.unpinChat else strings.pinChat, color = colors.onSurface) },
              onClick = {
                showMenu = false
                onTogglePin(!isPinned)
              }
            )
            DropdownMenuItem(
              text = { Text(if (isArchived) strings.unarchiveChat else strings.archiveChat, color = colors.onSurface) },
              onClick = {
                showMenu = false
                onToggleArchive(!isArchived)
              }
            )
            DropdownMenuItem(
              text = { Text(if (isMuted) strings.unmuteChat else strings.muteChat, color = colors.onSurface) },
              onClick = {
                showMenu = false
                onToggleMute(!isMuted)
              }
            )
          }
        }
      }
    }
  }
}

private fun formatConversationTimestamp(ts: Timestamp?): String {
  val date = ts?.toDate() ?: return ""
  val now = Calendar.getInstance()
  val msgCal = Calendar.getInstance().apply { time = date }
  return if (
    now.get(Calendar.YEAR) == msgCal.get(Calendar.YEAR) &&
    now.get(Calendar.DAY_OF_YEAR) == msgCal.get(Calendar.DAY_OF_YEAR)
  ) {
    SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
  } else {
    SimpleDateFormat("dd/MM", Locale.getDefault()).format(date)
  }
}
