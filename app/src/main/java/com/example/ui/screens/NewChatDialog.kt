package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.WorkOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfile
import com.example.data.repository.ChatRepository
import com.example.data.repository.UserRepository
import com.example.ui.components.AvatarView
import com.example.ui.components.ContactDirectoryHeader
import com.example.ui.components.DefaultProfessionCategories
import com.example.ui.components.VerifiedBadge
import com.example.ui.components.VerifiedProfessionalBadge
import com.example.ui.util.LocalAppStrings
import com.example.ui.util.formatHandleForDisplay
import kotlinx.coroutines.launch

@Composable
fun NewChatDialog(
  userRepository: UserRepository,
  chatRepository: ChatRepository,
  onDismiss: () -> Unit,
  onConversationCreated: (com.example.data.model.Conversation) -> Unit
) {
  val strings = LocalAppStrings.current
  val coroutineScope = rememberCoroutineScope()
  val currentUid = userRepository.requireUserId()

  var chatType by remember { mutableStateOf("direct") } // "direct" or "group"
  var groupTitleInput by remember { mutableStateOf("") }
  var searchQuery by remember { mutableStateOf("") }
  var selectedProfessionId by remember { mutableStateOf("all") }
  var searchResults by remember { mutableStateOf<List<UserProfile>>(emptyList()) }
  var totalDirectoryCount by remember { mutableStateOf(0) }
  var isSearching by remember { mutableStateOf(false) }
  var selectedUsers by remember { mutableStateOf<List<UserProfile>>(emptyList()) }
  var isCreating by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  fun executeSearch(query: String, profId: String) {
    searchQuery = query
    selectedProfessionId = profId
    isSearching = true
    coroutineScope.launch {
      val all = userRepository.fetchDirectoryUsers()
      totalDirectoryCount = all.size
      val categoryKeyword = if (profId == "all") null else {
        DefaultProfessionCategories.find { it.id == profId }?.filterKeyword ?: profId
      }
      searchResults = userRepository.filterDirectoryUsers(
        users = all,
        query = query,
        professionCategory = categoryKeyword
      ).filter { it.id != currentUid }
      isSearching = false
    }
  }

  LaunchedEffect(Unit) {
    executeSearch("", "all")
  }

  fun createChat() {
    if (isCreating) return
    if (chatType == "direct" && selectedUsers.isEmpty()) return
    if (chatType == "group" && groupTitleInput.isBlank()) return

    isCreating = true
    errorMessage = null
    coroutineScope.launch {
      val participantIds = selectedUsers.map { it.id }
      val title = if (chatType == "group") groupTitleInput.trim() else selectedUsers.firstOrNull()?.displayName
      val isVerified = if (chatType == "direct") selectedUsers.firstOrNull()?.isVerified == true else false
      val verifiedTier = if (chatType == "direct") selectedUsers.firstOrNull()?.verifiedTier ?: "BLUE" else "BLUE"
      val res = if (chatType == "direct") {
        chatRepository.findOrCreateDirectConversation(
          targetUserId = participantIds.first(),
          title = title,
          isVerified = isVerified,
          verifiedTier = verifiedTier
        )
      } else {
        chatRepository.createConversation(
          type = "group",
          targetUserIds = participantIds,
          title = title,
          isVerified = isVerified,
          verifiedTier = verifiedTier
        )
      }
      isCreating = false
      if (res.isSuccess) {
        val convoId = res.getOrThrow()
        onConversationCreated(
          com.example.data.model.Conversation(
            id = convoId,
            type = chatType,
            title = title ?: "",
            isVerified = isVerified,
            verifiedTier = verifiedTier,
            participantIds = (participantIds + currentUid).distinct()
          )
        )
      } else {
        errorMessage = res.exceptionOrNull()?.localizedMessage ?: strings.errorOccurred
      }
    }
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = if (chatType == "direct") strings.newChat else strings.newGroup,
        style = MaterialTheme.typography.titleLarge
      )
    },
    text = {
      Column(modifier = Modifier.fillMaxWidth()) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          FilterChip(
            selected = chatType == "direct",
            onClick = { chatType = "direct" },
            label = { Text(strings.directChats) },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp)) }
          )
          FilterChip(
            selected = chatType == "group",
            onClick = { chatType = "group" },
            label = { Text(strings.groupChats) },
            leadingIcon = { Icon(Icons.Default.Group, contentDescription = null, modifier = Modifier.size(16.dp)) }
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (chatType == "group") {
          OutlinedTextField(
            value = groupTitleInput,
            onValueChange = { groupTitleInput = it },
            label = { Text(strings.groupNameLabel) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().testTag("group_title_input")
          )
          Spacer(modifier = Modifier.height(10.dp))
        }

        // Contact Directory Search & Profession Header
        ContactDirectoryHeader(
          searchQuery = searchQuery,
          onSearchQueryChanged = { query -> executeSearch(query, selectedProfessionId) },
          selectedProfessionId = selectedProfessionId,
          onProfessionSelected = { profId -> executeSearch(searchQuery, profId) },
          filteredCount = searchResults.size,
          totalCount = totalDirectoryCount,
          onClearFilters = { executeSearch("", "all") },
          modifier = Modifier.testTag("dialog_contact_directory_header")
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (isSearching) {
          CircularProgressIndicator(modifier = Modifier.size(24.dp).align(Alignment.CenterHorizontally))
        } else {
          LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(min = 140.dp, max = 260.dp)) {
            items(searchResults, key = { it.id }) { user ->
              val isSelected = selectedUsers.any { it.id == user.id }
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(8.dp))
                  .clickable {
                    selectedUsers = if (chatType == "direct") {
                      listOf(user)
                    } else {
                      if (isSelected) selectedUsers.filterNot { it.id == user.id }
                      else selectedUsers + user
                    }
                  }
                  .padding(vertical = 8.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                AvatarView(name = user.displayName, avatarUrl = user.avatarUrl, size = 38.dp)
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                      text = user.displayName,
                      style = MaterialTheme.typography.bodyMedium,
                      fontWeight = FontWeight.SemiBold
                    )
                    if (user.isVerified) {
                      Spacer(modifier = Modifier.width(4.dp))
                      VerifiedBadge(size = 14.dp, tierKey = user.verifiedTier, showDialogOnClick = true)
                    }
                    if (user.isProfessionalVerified) {
                      Spacer(modifier = Modifier.width(4.dp))
                      VerifiedProfessionalBadge(user = user, size = 14.dp, showDialogOnClick = false)
                    }
                  }

                  if (user.profession.isNotBlank()) {
                    Row(
                      verticalAlignment = Alignment.CenterVertically,
                      modifier = Modifier.padding(top = 1.dp)
                    ) {
                      Icon(
                        imageVector = Icons.Default.WorkOutline,
                        contentDescription = null,
                        tint = if (user.isProfessionalVerified) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(10.dp)
                      )
                      Spacer(modifier = Modifier.width(3.dp))
                      Text(
                        text = user.profession,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (user.isProfessionalVerified) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                        fontWeight = if (user.isProfessionalVerified) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 10.5.sp
                      )
                      if (user.isProfessionalVerified) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                          text = "• PRO",
                          style = MaterialTheme.typography.labelSmall,
                          color = MaterialTheme.colorScheme.secondary,
                          fontWeight = FontWeight.ExtraBold,
                          fontSize = 9.sp
                        )
                      }
                    }
                  }

                  Text(
                    text = formatHandleForDisplay(user.handle),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                  )
                }
                if (isSelected) {
                  Text(
                    text = strings.selectedLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                  )
                }
              }
              HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
            }
          }
        }
        if (errorMessage != null) {
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = errorMessage!!,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = { createChat() },
        enabled = !isCreating && (
          (chatType == "direct" && selectedUsers.isNotEmpty()) ||
            (chatType == "group" && groupTitleInput.isNotBlank())
          ),
        modifier = Modifier.testTag("confirm_create_chat_button")
      ) {
        if (isCreating) {
          CircularProgressIndicator(modifier = Modifier.size(18.dp), color = MaterialTheme.colorScheme.onPrimary)
        } else {
          Text(strings.confirm)
        }
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text(strings.cancel)
      }
    }
  )
}
