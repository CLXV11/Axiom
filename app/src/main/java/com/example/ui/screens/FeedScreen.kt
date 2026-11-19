package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.DynamicFeed
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import com.example.data.model.Post
import com.example.data.model.PostComment
import com.example.data.repository.PostRepository
import com.example.data.repository.UserRepository
import com.example.ui.components.AvatarView
import com.example.ui.components.EmptyStateView
import com.example.ui.components.KoraTopBar
import com.example.ui.components.VerifiedBadge
import com.example.ui.theme.SemanticError
import com.example.ui.util.LocalAppStrings
import com.example.ui.util.formatHandleForDisplay
import kotlinx.coroutines.launch

@Composable
fun FeedScreen(
  postRepository: PostRepository,
  userRepository: UserRepository,
  onLanguageToggle: () -> Unit
) {
  val context = LocalContext.current
  val strings = LocalAppStrings.current
  val coroutineScope = rememberCoroutineScope()
  val currentUid = userRepository.requireUserId()

  var selectedFilter by remember { mutableStateOf("all") } // "all" or "circles"
  val posts by postRepository.observePosts(selectedFilter).collectAsState(initial = emptyList())
  val directoryUsers by userRepository.observeDirectoryUsers()
    .collectAsState(initial = userRepository.getDirectoryUsers())
  val usersById = remember(directoryUsers) { directoryUsers.associateBy { it.id } }

  var newPostContent by remember { mutableStateOf("") }
  var postAudience by remember { mutableStateOf("public") } // "public" or "circles"
  var isSubmitting by remember { mutableStateOf(false) }

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
            userRepository = userRepository
          )
        }
      }
    }
  }
}

@Composable
private fun PostCard(
  post: Post,
  authorAvatarUrl: String?,
  usersById: Map<String, com.example.data.model.UserProfile>,
  currentUid: String,
  postRepository: PostRepository,
  userRepository: UserRepository
) {
  val strings = LocalAppStrings.current
  val coroutineScope = rememberCoroutineScope()
  var showThreads by remember { mutableStateOf(false) }
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
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      // Author Header
      Row(verticalAlignment = Alignment.CenterVertically) {
        AvatarView(name = liveAuthorName, avatarUrl = liveAvatarUrl, size = 40.dp)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = liveAuthorName,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.SemiBold
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
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Content
      Text(
        text = post.content,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurface
      )

      Spacer(modifier = Modifier.height(14.dp))
      HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
      Spacer(modifier = Modifier.height(8.dp))

      // Actions (Like, Thread)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          IconButton(onClick = {
            coroutineScope.launch {
              postRepository.toggleLike(post.id, isLiked)
            }
          }) {
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

        Row(
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { showThreads = !showThreads }
            .padding(horizontal = 10.dp, vertical = 6.dp),
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
            fontWeight = FontWeight.SemiBold
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

          comments.forEach { comment ->
            val commentAuthor = usersById[comment.authorId]
            ContextThreadCommentItem(
              comment = comment,
              avatarUrl = commentAuthor?.avatarUrl,
              liveVerified = commentAuthor?.isVerified ?: comment.isAuthorVerified,
              liveVerifiedTier = commentAuthor?.verifiedTier ?: "BLUE"
            )
            Spacer(modifier = Modifier.height(8.dp))
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
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(16.dp),
              maxLines = 2
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
              onClick = { submitComment() },
              enabled = commentInput.isNotBlank()
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
  liveVerified: Boolean = comment.isAuthorVerified,
  liveVerifiedTier: String = "BLUE"
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(8.dp))
      .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
      .padding(8.dp)
  ) {
    AvatarView(name = comment.authorName, avatarUrl = avatarUrl, size = 32.dp)
    Spacer(modifier = Modifier.width(8.dp))
    Column {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
          text = comment.authorName,
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Bold
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
  }
}
