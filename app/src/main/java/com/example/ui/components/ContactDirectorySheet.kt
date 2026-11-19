package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.material.icons.filled.WorkOutline
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfile
import com.example.data.repository.UserRepository
import com.example.ui.util.LocalAppStrings
import com.example.ui.util.formatHandleForDisplay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactDirectorySheet(
  userRepository: UserRepository,
  onDismiss: () -> Unit,
  onSelectUserToMessage: (UserProfile) -> Unit,
  sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
  val strings = LocalAppStrings.current
  val coroutineScope = rememberCoroutineScope()
  val currentUid = userRepository.requireUserId()

  var searchQuery by remember { mutableStateOf("") }
  var selectedProfessionId by remember { mutableStateOf("all") }
  var contacts by remember { mutableStateOf<List<UserProfile>>(emptyList()) }
  var allDirectoryCount by remember { mutableStateOf(0) }
  var isLoading by remember { mutableStateOf(false) }

  fun filterContacts(query: String, profId: String) {
    searchQuery = query
    selectedProfessionId = profId
    isLoading = true
    coroutineScope.launch {
      val all = userRepository.fetchDirectoryUsers()
      allDirectoryCount = all.size
      val categoryKeyword = if (profId == "all") null else {
        DefaultProfessionCategories.find { it.id == profId }?.filterKeyword ?: profId
      }
      contacts = userRepository.filterDirectoryUsers(
        users = all,
        query = query,
        professionCategory = categoryKeyword
      ).filter { it.id != currentUid }
      isLoading = false
    }
  }

  LaunchedEffect(Unit) {
    filterContacts("", "all")
  }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = MaterialTheme.colorScheme.surface,
    dragHandle = null,
    modifier = Modifier.testTag("contact_directory_sheet")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .widthIn(max = 680.dp)
        .fillMaxHeight(0.85f)
        .heightIn(min = 320.dp, max = 680.dp)
        .align(Alignment.CenterHorizontally)
    ) {
      // Top Navigation / Header Title
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
          Box(
            modifier = Modifier
              .size(38.dp)
              .clip(CircleShape)
              .background(
                Brush.linearGradient(
                  listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                )
              ),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Contacts,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onPrimary,
              modifier = Modifier.size(20.dp)
            )
          }
          Spacer(modifier = Modifier.width(12.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = strings.contactDirectoryTitle,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = strings.contactDirectorySubtitle,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        IconButton(
          onClick = onDismiss,
          modifier = Modifier.testTag("close_contact_directory_button")
        ) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = strings.cancel,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

      // Searchable Contact Directory Header Component
      ContactDirectoryHeader(
        searchQuery = searchQuery,
        onSearchQueryChanged = { query -> filterContacts(query, selectedProfessionId) },
        selectedProfessionId = selectedProfessionId,
        onProfessionSelected = { profId -> filterContacts(searchQuery, profId) },
        filteredCount = contacts.size,
        totalCount = allDirectoryCount,
        onClearFilters = { filterContacts("", "all") },
        modifier = Modifier.testTag("embedded_contact_directory_header")
      )

      HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))

      // Contacts List Stream
      if (contacts.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .padding(24.dp),
          contentAlignment = Alignment.Center
        ) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
          ) {
            Icon(
              imageVector = Icons.Default.PersonOff,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
              modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
              text = strings.noContactsFound,
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = strings.noContactsFoundDesc,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
              onClick = { filterContacts("", "all") },
              colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
              ),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.testTag("reset_contact_filters_button")
            ) {
              Text(strings.clearSearch, fontWeight = FontWeight.Bold)
            }
          }
        }
      } else {
        LazyColumn(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("contact_directory_list")
        ) {
          items(contacts, key = { it.id }) { user ->
            Card(
              onClick = { onSelectUserToMessage(user) },
              shape = RoundedCornerShape(14.dp),
              colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
              ),
              border = androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
              ),
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .testTag("contact_card_${user.id}")
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                AvatarView(
                  name = user.displayName,
                  avatarUrl = user.avatarUrl,
                  size = 46.dp,
                  showPresence = true,
                  isOnline = true
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                      text = user.displayName,
                      style = MaterialTheme.typography.bodyMedium,
                      fontWeight = FontWeight.Bold,
                      color = MaterialTheme.colorScheme.onSurface
                    )
                    if (user.isVerified) {
                      Spacer(modifier = Modifier.width(4.dp))
                      VerifiedBadge(size = 15.dp, tierKey = user.verifiedTier, showDialogOnClick = false)
                    }
                    if (user.isProfessionalVerified) {
                      Spacer(modifier = Modifier.width(5.dp))
                      VerifiedProfessionalBadge(
                        user = user,
                        size = 15.dp,
                        showDialogOnClick = true,
                        onMessageClick = { onSelectUserToMessage(user) }
                      )
                    }
                  }

                  // Profession Badge Pill
                  if (user.profession.isNotBlank()) {
                    Spacer(modifier = Modifier.height(3.dp))
                    if (user.isProfessionalVerified) {
                      VerifiedProfessionalPill(
                        user = user,
                        showDialogOnClick = true,
                        onMessageClick = { onSelectUserToMessage(user) }
                      )
                    } else {
                      Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                          .clip(RoundedCornerShape(6.dp))
                          .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f))
                          .padding(horizontal = 6.dp, vertical = 2.dp)
                      ) {
                        Icon(
                          imageVector = Icons.Default.WorkOutline,
                          contentDescription = null,
                          tint = MaterialTheme.colorScheme.primary,
                          modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                          text = user.profession,
                          style = MaterialTheme.typography.labelSmall,
                          color = MaterialTheme.colorScheme.primary,
                          fontWeight = FontWeight.SemiBold,
                          fontSize = 11.sp
                        )
                      }
                    }
                  }

                  Spacer(modifier = Modifier.height(2.dp))

                  val subtitleText = buildString {
                    if (user.professionalOrg.isNotBlank()) {
                      append(user.professionalOrg)
                      append(" • ")
                    }
                    append(formatHandleForDisplay(user.handle))
                    if (user.bio.isNotBlank()) {
                      append(" • ")
                      append(user.bio)
                    }
                  }

                  Text(
                    text = subtitleText,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    fontSize = 11.5.sp
                  )
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                  onClick = { onSelectUserToMessage(user) },
                  modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f))
                    .testTag("message_contact_button_${user.id}")
                ) {
                  Icon(
                    imageVector = Icons.Default.Chat,
                    contentDescription = strings.newChat,
                    tint = MaterialTheme.colorScheme.primary,
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
