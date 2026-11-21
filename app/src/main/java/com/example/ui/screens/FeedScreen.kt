package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DynamicFeed
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.outlined.AddReaction
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.data.model.Post
import com.example.data.model.PostComment
import com.example.data.model.UserProfile
import com.example.data.repository.PostRepository
import com.example.data.repository.UserRepository
import com.example.ui.components.AvatarView
import com.example.ui.components.EmptyStateView
import com.example.ui.components.IosFloatingReactionBar
import com.example.ui.components.KoraTopBar
import com.example.ui.components.ReactionChipsRow
import com.example.ui.components.StyledDisplayName
import com.example.ui.components.VerifiedBadge
import com.example.ui.components.WeirdEmojiPickerBottomSheet
import com.example.ui.theme.SemanticError
import com.example.ui.util.LocalAppStrings
import com.example.ui.util.formatHandleForDisplay
import kotlinx.coroutines.launch

@Composable
fun FeedScreen(
  postRepository: PostRepository,
  userRepository: UserRepository,
  onLanguageToggle: () -> Unit,
  onInspectUser: (UserProfile) -> Unit = {}
) {
  val context = LocalContext.current
  val strings = LocalAppStrings.current
  val coroutineScope = rememberCoroutineScope()
  val currentUid = userRepository.requireUserId()

  var selectedFilter by remember { mutableStateOf("all") } // "all" or "circles"
  val posts by postRepository.observePosts(selectedFilter).collectAsState(initial = emptyList())
  val directoryUsers by userRepository.observeDirectoryUsers()
    .collectAsState(initial = userRepository.getDirectoryUsers())
  val currentUserProfile by userRepository.observeUserProfile(currentUid)
    .collectAsState(initial = userRepository.getCachedProfile(currentUid))
  val usersById = remember(directoryUsers, currentUserProfile, currentUid) {
    val map = directoryUsers.associateBy { it.id }.toMutableMap()
    currentUserProfile?.let { map[currentUid] = it }
    map
  }

  var newPostContent by remember { mutableStateOf("") }
  var postAudience by remember { mutableStateOf("public") } // "public" or "circles"
  var isSubmitting by remember { mutableStateOf(false) }
  var reactionTargetPostId by remember { mutableStateOf<String?>(null) }
  var editingPost by remember { mutableStateOf<Post?>(null) }
  var deletingPost by remember { mutableStateOf<Post?>(null) }

  LaunchedEffect(Unit) {
    userRepository.fetchDirectoryUsers()
  }

  fun publishPost() {
    val content = newPostContent.trim()
    if (content.isBlank()) return

    isSubmitting = true
    coroutineScope.launch {
      val userProfile = userRepository.getUserProfile(currentUid)
      val authorName = userProfile?.displayName ?: "User"
      val authorHandle = userProfile?.handle ?: "@user"
      val isVerified = userProfile?.isVerified ?: false
      val verifiedTier = userProfile?.verifiedTier ?: "BLUE"

      val result = postRepository.createPost(
        content = content,
        audience = postAudience,
        authorName = authorName,
        authorHandle = authorHandle,
        isAuthorVerified = isVerified,
        authorVerifiedTier = verifiedTier
      )
      isSubmitting = false
      if (result.isSuccess) {
        newPostContent = ""
        Toast.makeText(context, strings.publishedSuccess, Toast.LENGTH_SHORT).show()
      } else {
        Toast.makeText(context, strings.errorOccurred, Toast.LENGTH_SHORT).show()
      }
    }
  }

  Scaffold(
    contentWindowInsets = WindowInsets(0, 0, 0, 0),
    topBar = {
      KoraTopBar(
        title = strings.feedTitle,
        subtitle = strings.tagline,
        onLanguageToggle = onLanguageToggle
      )
    }
  ) { paddingValues ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .background(MaterialTheme.colorScheme.background),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // Post Creator
      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
          elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
          modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 760.dp)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            OutlinedTextField(
              value = newPostContent,
              onValueChange = { newPostContent = it },
              placeholder = { Text(strings.whatsOnYourMind) },
              shape = RoundedCornerShape(14.dp),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("new_post_input"),
              maxLines = 6
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically
            ) {
              // Audience Selector
              Row(
                modifier = Modifier
                  .weight(1f)
                  .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                FilterChip(
                  selected = postAudience == "public",
                  onClick = { postAudience = "public" },
                  label = { Text(strings.audiencePublic, maxLines = 1, softWrap = false) },
                  leadingIcon = {
                    Icon(
                      imageVector = Icons.Default.Public,
                      contentDescription = null,
                      modifier = Modifier.size(16.dp)
                    )
                  }
                )
                FilterChip(
                  selected = postAudience == "circles",
                  onClick = { postAudience = "circles" },
                  label = { Text(strings.audienceCircles, maxLines = 1, softWrap = false) },
                  leadingIcon = {
                    Icon(
                      imageVector = Icons.Default.Group,
                      contentDescription = null,
                      modifier = Modifier.size(16.dp)
                    )
                  }
                )
              }

              Spacer(modifier = Modifier.width(12.dp))

              Button(
                onClick = { publishPost() },
                enabled = newPostContent.isNotBlank() && !isSubmitting,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("publish_post_button")
              ) {
                Text(strings.publish, fontWeight = FontWeight.SemiBold, maxLines = 1, softWrap = false)
              }
            }
          }
        }
      }

      // Feed Filter Row
      item {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 760.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          FilterChip(
            selected = selectedFilter == "all",
            onClick = { selectedFilter = "all" },
            label = { Text(strings.filterAll) }
          )
          FilterChip(
            selected = selectedFilter == "circles",
            onClick = { selectedFilter = "circles" },
            label = { Text(strings.filterCircles) }
          )
        }
      }

      if (posts.isEmpty()) {
        item {
          EmptyStateView(
            icon = Icons.Default.DynamicFeed,
            title = strings.noPostsYet,
            description = strings.beTheFirstToShare
          )
        }
      } else {
        items(posts, key = { it.id }) { post ->
          PostCard(
            post = post,
            authorAvatarUrl = usersById[post.authorId]?.avatarUrl,
            usersById = usersById,
            currentUid = currentUid,
            postRepository = postRepository,
            userRepository = userRepository,
            onOpenReactionSheet = { reactionTargetPostId = post.id },
            onEditPost = { editingPost = post },
            onDeletePost = { deletingPost = post },
            onInspectUser = onInspectUser
          )
        }
      }
    }
  }

  val activeReactionPost = reactionTargetPostId?.let { targetId ->
    posts.firstOrNull { it.id == targetId }
  }
  if (activeReactionPost != null) {
    val selectedEmojis = remember(activeReactionPost.reactions, currentUid) {
      activeReactionPost.reactions
        .filterValues { it.contains(currentUid) }
        .keys
        .toSet()
    }
    WeirdEmojiPickerBottomSheet(
      selectedEmojis = selectedEmojis,
      onEmojiSelected = { emoji ->
        reactionTargetPostId = null
        coroutineScope.launch {
          val res = postRepository.togglePostReaction(activeReactionPost.id, emoji)
          if (res.isFailure) {
            Toast.makeText(
              context,
              res.exceptionOrNull()?.localizedMessage ?: strings.errorOccurred,
              Toast.LENGTH_SHORT
            ).show()
          }
        }
      },
      onDismiss = { reactionTargetPostId = null }
    )
  }

  editingPost?.let { target ->
    var contentDraft by remember(target.id) { mutableStateOf(target.content) }
    var isSavingEdit by remember(target.id) { mutableStateOf(false) }

    AlertDialog(
      onDismissRequest = { if (!isSavingEdit) editingPost = null },
      title = { Text(strings.editPostTitle, fontWeight = FontWeight.Bold) },
      text = {
        OutlinedTextField(
          value = contentDraft,
          onValueChange = { contentDraft = it },
          placeholder = { Text(strings.whatsOnYourMind) },
          maxLines = 6,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("edit_post_content_input")
        )
      },
      confirmButton = {
        Button(
          enabled = contentDraft.isNotBlank() && !isSavingEdit,
          onClick = {
            isSavingEdit = true
            coroutineScope.launch {
              val res = postRepository.updatePost(target.id, contentDraft, target.audience)
              isSavingEdit = false
              if (res.isSuccess) {
                editingPost = null
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
          onClick = { editingPost = null }
        ) {
          Text(strings.cancel)
        }
      }
    )
  }

  deletingPost?.let { target ->
    var isDeleting by remember(target.id) { mutableStateOf(false) }
    AlertDialog(
      onDismissRequest = { if (!isDeleting) deletingPost = null },
      title = { Text(strings.deleteContentTitle, fontWeight = FontWeight.Bold) },
      text = { Text(strings.deleteContentConfirm) },
      confirmButton = {
        Button(
          enabled = !isDeleting,
          colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error,
            contentColor = MaterialTheme.colorScheme.onError
          ),
          onClick = {
            isDeleting = true
            coroutineScope.launch {
              val res = postRepository.deletePost(target.id)
              isDeleting = false
              if (res.isSuccess) {
                deletingPost = null
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
          onClick = { deletingPost = null }
        ) {
          Text(strings.cancel)
        }
      }
    )
  }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PostCard(
  post: Post,
  authorAvatarUrl: String?,
  usersById: Map<String, UserProfile>,
  currentUid: String,
  postRepository: PostRepository,
  userRepository: UserRepository,
  onOpenReactionSheet: () -> Unit,
  onEditPost: () -> Unit,
  onDeletePost: () -> Unit,
  onInspectUser: (UserProfile) -> Unit
) {
  val context = LocalContext.current
  val strings = LocalAppStrings.current
  val coroutineScope = rememberCoroutineScope()
  var showThreads by remember { mutableStateOf(false) }
  var showInlineReactionBar by remember { mutableStateOf(false) }
  var showAuthorMenu by remember { mutableStateOf(false) }
  var commentInput by remember { mutableStateOf("") }

  val isLiked = post.likedBy.contains(currentUid)
  val comments by postRepository.observeComments(post.id).collectAsState(initial = emptyList())

  fun submitComment() {
    val text = commentInput.trim()
    if (text.isBlank()) return

    coroutineScope.launch {
      val userProfile = userRepository.getUserProfile(currentUid)
      val authorName = userProfile?.displayName ?: "User"
      val authorHandle = userProfile?.handle ?: "@user"
      val isVerified = userProfile?.isVerified ?: false

      postRepository.addComment(post.id, text, authorName, authorHandle, isAuthorVerified = isVerified)
      commentInput = ""
    }
  }

  val authorProfile = usersById[post.authorId]
  val liveAuthorName = authorProfile?.displayName?.takeIf { it.isNotBlank() } ?: post.authorName
  val liveAvatarUrl = authorProfile?.avatarUrl ?: authorAvatarUrl
  val liveIsVerified = authorProfile?.isVerified ?: post.isAuthorVerified
  val liveVerifiedTier = authorProfile?.verifiedTier?.takeIf { it.isNotBlank() } ?: post.authorVerifiedTier

  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    modifier = Modifier
      .fillMaxWidth()
      .widthIn(max = 760.dp)
      .clip(RoundedCornerShape(16.dp))
      .combinedClickable(
        onClick = {},
        onLongClick = { showInlineReactionBar = !showInlineReactionBar }
      )
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      // Author Header (tappable to inspect profile)
      Row(verticalAlignment = Alignment.CenterVertically) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(10.dp))
            .clickable {
              val target = authorProfile ?: UserProfile(
                id = post.authorId,
                displayName = liveAuthorName,
                handle = post.authorHandle,
                avatarUrl = liveAvatarUrl.orEmpty(),
                isVerified = liveIsVerified,
                verifiedTier = liveVerifiedTier
              )
              onInspectUser(target)
            }
            .padding(vertical = 2.dp)
            .testTag("post_author_header_${post.id}")
        ) {
          AvatarView(
            name = liveAuthorName,
            avatarUrl = liveAvatarUrl,
            size = 40.dp,
            frameStyleKey = authorProfile?.equippedFrame
          )
          Spacer(modifier = Modifier.width(12.dp))
          Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              StyledDisplayName(
                name = liveAuthorName,
                nameFontId = authorProfile?.nameFontId ?: "default",
                nameThemeId = authorProfile?.nameThemeId ?: "default",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                fallbackColor = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
              )
              if (liveIsVerified) {
                Spacer(modifier = Modifier.width(6.dp))
                VerifiedBadge(size = 15.dp, tierKey = liveVerifiedTier, showDialogOnClick = true)
              }
            }
            Text(
              text = formatHandleForDisplay(post.authorHandle),
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
          // Audience Badge
          Row(
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(MaterialTheme.colorScheme.surfaceVariant)
              .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = if (post.audience == "circles") Icons.Default.Lock else Icons.Default.Public,
              contentDescription = null,
              modifier = Modifier.size(12.dp),
              tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = if (post.audience == "circles") strings.audienceCircles else strings.audiencePublic,
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          if (post.authorId == currentUid) {
            Box {
              IconButton(
                onClick = { showAuthorMenu = true },
                modifier = Modifier
                  .size(36.dp)
                  .testTag("post_author_menu_${post.id}")
              ) {
                Icon(
                  imageVector = Icons.Default.MoreVert,
                  contentDescription = strings.edit,
                  tint = MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.size(18.dp)
                )
              }
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
                    onEditPost()
                  }
                )
                DropdownMenuItem(
                  text = { Text(strings.delete, color = MaterialTheme.colorScheme.error) },
                  leadingIcon = {
                    Icon(
                      Icons.Default.DeleteOutline,
                      contentDescription = null,
                      tint = MaterialTheme.colorScheme.error,
                      modifier = Modifier.size(18.dp)
                    )
                  },
                  onClick = {
                    showAuthorMenu = false
                    onDeletePost()
                  }
                )
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Content
      Text(
        text = post.content,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurface
      )

      // Multi-Emoji Reaction Chips
      if (post.reactions.isNotEmpty()) {
        Spacer(modifier = Modifier.height(10.dp))
        ReactionChipsRow(
          reactions = post.reactions,
          currentUid = currentUid,
          onToggleReaction = { emoji ->
            coroutineScope.launch {
              val res = postRepository.togglePostReaction(post.id, emoji)
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

      // iPhone-style Floating Reaction Pill Bar (toggled on React button or Long-Press)
      AnimatedVisibility(visible = showInlineReactionBar) {
        val mySelected = remember(post.reactions, currentUid) {
          post.reactions.filterValues { it.contains(currentUid) }.keys.toSet()
        }
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp),
          contentAlignment = Alignment.CenterStart
        ) {
          IosFloatingReactionBar(
            selectedEmojis = mySelected,
            onEmojiSelected = { emoji ->
              showInlineReactionBar = false
              coroutineScope.launch {
                postRepository.togglePostReaction(post.id, emoji)
              }
            },
            onMoreClick = {
              showInlineReactionBar = false
              onOpenReactionSheet()
            }
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))
      HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
      Spacer(modifier = Modifier.height(8.dp))

      // Actions (Like, Weird React Bar, Thread)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
              onClick = {
                coroutineScope.launch {
                  postRepository.toggleLike(post.id, isLiked)
                }
              },
              modifier = Modifier.defaultMinSize(minWidth = 44.dp, minHeight = 44.dp)
            ) {
              Icon(
                imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = strings.likes,
                tint = if (isLiked) SemanticError else MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
            Text(
              text = "${post.likesCount}",
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          Surface(
            onClick = { showInlineReactionBar = !showInlineReactionBar },
            shape = RoundedCornerShape(999.dp),
            color = if (showInlineReactionBar) {
              MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
            } else {
              MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
            },
            modifier = Modifier
              .defaultMinSize(minHeight = 36.dp)
              .testTag("post_react_button_${post.id}")
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
              Icon(
                imageVector = Icons.Outlined.AddReaction,
                contentDescription = strings.reactAction,
                tint = if (showInlineReactionBar) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
              )
              Text(
                text = strings.reactAction,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = if (showInlineReactionBar) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                softWrap = false
              )
            }
          }
        }

        Row(
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { showThreads = !showThreads }
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag("post_comments_toggle_${post.id}"),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.ChatBubbleOutline,
            contentDescription = strings.contextThreads,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "${strings.contextThreads} (${post.commentsCount})",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            softWrap = false
          )
        }
      }

      // Context Threads Dropdown
      AnimatedVisibility(visible = showThreads) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
        ) {
          HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
          Spacer(modifier = Modifier.height(8.dp))

          Text(
            text = "${strings.postCommentsTitle} (${post.commentsCount})",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
          )

          if (comments.isEmpty()) {
            Text(
              text = strings.noPostCommentsYet,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.78f),
              modifier = Modifier.padding(vertical = 8.dp)
            )
          } else {
            comments.forEach { comment ->
              val commentAuthor = usersById[comment.authorId]
              ContextThreadCommentItem(
                comment = comment,
                avatarUrl = commentAuthor?.avatarUrl,
                authorNameFontId = commentAuthor?.nameFontId ?: "default",
                authorNameThemeId = commentAuthor?.nameThemeId ?: "default",
                liveVerified = commentAuthor?.isVerified ?: comment.isAuthorVerified,
                liveVerifiedTier = commentAuthor?.verifiedTier ?: "BLUE",
                canDelete = comment.authorId == currentUid,
                onInspectAuthor = {
                  val target = commentAuthor ?: UserProfile(
                    id = comment.authorId,
                    displayName = comment.authorName,
                    handle = comment.authorHandle,
                    isVerified = comment.isAuthorVerified
                  )
                  onInspectUser(target)
                },
                onDelete = {
                  coroutineScope.launch {
                    val res = postRepository.deleteComment(post.id, comment.id)
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
              Spacer(modifier = Modifier.height(8.dp))
            }
          }

          // Thread reply input
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            OutlinedTextField(
              value = commentInput,
              onValueChange = { commentInput = it },
              placeholder = { Text(strings.writeAComment) },
              keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
              keyboardActions = KeyboardActions(onSend = { submitComment() }),
              modifier = Modifier
                .weight(1f)
                .testTag("post_comment_input_${post.id}"),
              shape = RoundedCornerShape(16.dp),
              maxLines = 2
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
              onClick = { submitComment() },
              enabled = commentInput.isNotBlank(),
              modifier = Modifier.testTag("post_comment_send_${post.id}")
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = strings.send,
                tint = if (commentInput.isNotBlank()) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun ContextThreadCommentItem(
  comment: PostComment,
  avatarUrl: String? = null,
  authorNameFontId: String = "default",
  authorNameThemeId: String = "default",
  liveVerified: Boolean = comment.isAuthorVerified,
  liveVerifiedTier: String = "BLUE",
  canDelete: Boolean = false,
  onInspectAuthor: () -> Unit = {},
  onDelete: () -> Unit = {}
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(10.dp))
      .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
      .clickable(onClick = onInspectAuthor)
      .padding(8.dp),
    verticalAlignment = Alignment.Top
  ) {
    AvatarView(name = comment.authorName, avatarUrl = avatarUrl, size = 32.dp)
    Spacer(modifier = Modifier.width(8.dp))
    Column(modifier = Modifier.weight(1f)) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        StyledDisplayName(
          name = comment.authorName,
          nameFontId = authorNameFontId,
          nameThemeId = authorNameThemeId,
          style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
          fallbackColor = MaterialTheme.colorScheme.onSurface,
          maxLines = 1,
          overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
          modifier = Modifier.weight(1f, fill = false)
        )
        if (liveVerified) {
          Spacer(modifier = Modifier.width(4.dp))
          VerifiedBadge(size = 13.dp, tierKey = liveVerifiedTier, showDialogOnClick = true)
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = formatHandleForDisplay(comment.authorHandle),
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = comment.content,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface
      )
    }
    if (canDelete) {
      IconButton(
        onClick = onDelete,
        modifier = Modifier.size(32.dp)
      ) {
        Icon(
          imageVector = Icons.Default.DeleteOutline,
          contentDescription = "Delete comment",
          tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
          modifier = Modifier.size(16.dp)
        )
      }
    }
  }
}
