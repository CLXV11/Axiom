package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.graphics.Brush
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
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
  chatRepository: ChatRepository,
  userRepository: UserRepository,
  onOpenConversation: (Conversation) -> Unit,
  onNewChatClicked: () -> Unit,
  onLanguageToggle: () -> Unit,
  onOpenStore: () -> Unit = {}
) {
  val context = LocalContext.current
  val strings = LocalAppStrings.current
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

  val filteredConversations = conversations.filter { convo ->
    val isAi = convo.id == ChatRepository.AI_CHATBOT_CONVO_ID
    val peer = resolvePeer(convo)
    val displayTitle = when {
      isAi -> strings.aiAssistantName
      peer != null -> peer.displayName
      else -> convo.title
    }
    val matchesFilter = when (selectedFilter) {
      "direct" -> convo.type == "direct"
      "group" -> convo.type == "group"
      else -> true
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
    containerColor = MaterialTheme.colorScheme.background,
    topBar = {
      KoraTopBar(
        title = strings.conversations,
        subtitle = if (conversations.size <= 1) strings.aiAssistantSubtitle else String.format(strings.activeChatsCount, conversations.size),
        onLanguageToggle = onLanguageToggle,
        actions = {
          IconButton(
            onClick = { showContactDirectorySheet = true },
            modifier = Modifier.testTag("open_contact_directory_button")
          ) {
            Icon(
              imageVector = Icons.Default.Contacts,
              contentDescription = strings.contactDirectoryTitle,
              tint = MaterialTheme.colorScheme.primary
            )
          }
          IconButton(onClick = onOpenStore) {
            Icon(
              imageVector = Icons.Default.AutoAwesome,
              contentDescription = strings.customDecoration,
              tint = MaterialTheme.colorScheme.primary
            )
          }
        }
      )
    },
    floatingActionButton = {
      FloatingActionButton(
        onClick = onNewChatClicked,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        shape = CircleShape,
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
        .background(MaterialTheme.colorScheme.background),
      contentAlignment = Alignment.TopCenter
    ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .widthIn(max = 840.dp)
    ) {
      // Soft Search Bar Capsule
      OutlinedTextField(
        value = searchQuery,
        onValueChange = { searchQuery = it },
        placeholder = {
          Text(
            text = strings.searchChats,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium
          )
        },
        leadingIcon = {
          Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
          )
        },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
          unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f),
          focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.65f),
          unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
          focusedTextColor = MaterialTheme.colorScheme.onSurface,
          unfocusedTextColor = MaterialTheme.colorScheme.onSurface
        ),
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 10.dp)
          .testTag("search_conversations_input")
      )

      // Calm Filter Chips
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        val chipColors = FilterChipDefaults.filterChipColors(
          containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
          labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
          selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
          selectedLabelColor = MaterialTheme.colorScheme.primary
        )
        val chipBorder = FilterChipDefaults.filterChipBorder(
          enabled = true,
          selected = false,
          borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
        )

        FilterChip(
          selected = selectedFilter == "all",
          onClick = { selectedFilter = "all" },
          label = { Text(strings.filterAll, fontWeight = FontWeight.SemiBold) },
          colors = chipColors,
          border = chipBorder,
          shape = RoundedCornerShape(12.dp)
        )
        FilterChip(
          selected = selectedFilter == "direct",
          onClick = { selectedFilter = "direct" },
          label = { Text(strings.directChats, fontWeight = FontWeight.SemiBold) },
          colors = chipColors,
          border = chipBorder,
          shape = RoundedCornerShape(12.dp)
        )
        FilterChip(
          selected = selectedFilter == "group",
          onClick = { selectedFilter = "group" },
          label = { Text(strings.groupChats, fontWeight = FontWeight.SemiBold) },
          colors = chipColors,
          border = chipBorder,
          shape = RoundedCornerShape(12.dp)
        )
      }

      if (filteredConversations.isEmpty()) {
        EmptyStateView(
          icon = Icons.Default.ChatBubbleOutline,
          title = strings.noChatsYet,
          description = strings.startFirstConversation,
          actionLabel = strings.newChat,
          onAction = onNewChatClicked
        )
      } else {
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          items(filteredConversations, key = { it.id }) { convo ->
            val typingUsers by chatRepository.observeTyping(convo.id).collectAsState(initial = emptyList())
            val peerUser = resolvePeer(convo)
            ConversationRow(
              conversation = convo,
              peerUser = peerUser,
              isAiChatbot = chatRepository.isAiChatbotConversation(convo.id),
              currentUserId = currentUid,
              typingUsers = typingUsers,
              onClick = { onOpenConversation(convo) },
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
      }
    )
  }
}

@Composable
private fun ConversationRow(
  conversation: Conversation,
  peerUser: UserProfile? = null,
  isAiChatbot: Boolean,
  currentUserId: String,
  typingUsers: List<String> = emptyList(),
  onClick: () -> Unit,
  onTogglePin: (Boolean) -> Unit,
  onToggleArchive: (Boolean) -> Unit,
  onToggleMute: (Boolean) -> Unit
) {
  val strings = LocalAppStrings.current
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

  val containerColor = if (isAiChatbot) {
    MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
  } else {
    MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
  }

  val borderColor = if (isAiChatbot) {
    MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
  } else {
    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.28f)
  }

  Surface(
    shape = RoundedCornerShape(18.dp),
    color = containerColor,
    border = BorderStroke(1.dp, borderColor),
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(18.dp))
      .clickable(onClick = onClick)
      .testTag(if (isAiChatbot) "ai_chatbot_conversation_card" else "conversation_card_${conversation.id}")
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      if (isAiChatbot) {
        Box(
          modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(
              Brush.linearGradient(
                listOf(
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
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(22.dp)
          )
        }
      } else {
        AvatarView(
          name = title,
          avatarUrl = peerUser?.avatarUrl,
          size = 48.dp,
          showPresence = conversation.type == "direct",
          isOnline = true
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
            modifier = Modifier.weight(1f, fill = false)
          ) {
            if (isPinned && !isAiChatbot) {
              Icon(
                imageVector = Icons.Default.PushPin,
                contentDescription = "Pinned",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(14.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
              text = title,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            if (isRowVerified) {
              Spacer(modifier = Modifier.width(6.dp))
              VerifiedBadge(
                size = 15.dp,
                tierKey = rowVerifiedTier,
                showDialogOnClick = true
              )
            }
            if (isAiChatbot) {
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

          if (isMuted) {
            Icon(
              imageVector = Icons.Default.NotificationsOff,
              contentDescription = "Muted",
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(16.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(4.dp))

        if (typingUsers.isNotEmpty()) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 2.dp)
          ) {
            TypingDotsIndicator(
              dotSize = 4.dp,
              travelDistance = 2.dp,
              dotColor = MaterialTheme.colorScheme.primary
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
              color = MaterialTheme.colorScheme.primary,
              fontWeight = FontWeight.SemiBold,
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
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }
      }

      if (!isAiChatbot) {
        Box {
          IconButton(onClick = { showMenu = true }) {
            Icon(
              imageVector = Icons.Default.MoreVert,
              contentDescription = "Actions",
              tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
          ) {
            DropdownMenuItem(
              text = { Text(if (isPinned) strings.unpinChat else strings.pinChat, color = MaterialTheme.colorScheme.onSurface) },
              onClick = {
                showMenu = false
                onTogglePin(!isPinned)
              }
            )
            DropdownMenuItem(
              text = { Text(if (isArchived) strings.unarchiveChat else strings.archiveChat, color = MaterialTheme.colorScheme.onSurface) },
              onClick = {
                showMenu = false
                onToggleArchive(!isArchived)
              }
            )
            DropdownMenuItem(
              text = { Text(if (isMuted) strings.unmuteChat else strings.muteChat, color = MaterialTheme.colorScheme.onSurface) },
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
