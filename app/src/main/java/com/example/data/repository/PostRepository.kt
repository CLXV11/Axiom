package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.Post
import com.example.data.model.PostComment
import com.example.data.model.SavedMemory
import com.example.data.util.OperationType
import com.example.data.util.handleFirestoreError
import com.example.ui.util.PreferencesManager
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

class PostRepository(
  private val db: FirebaseFirestore,
  private val prefsManager: PreferencesManager? = null,
  private val auth: FirebaseAuth = Firebase.auth
) {
  constructor(context: Context) : this(
    FirebaseFirestore.getInstance(
      context.applicationContext.getString(R.string.firestore_database_id)
    ),
    PreferencesManager(context.applicationContext),
    Firebase.auth
  )

  private val localPosts = MutableStateFlow<List<Post>>(emptyList())
  private val localComments = mutableMapOf<String, MutableStateFlow<List<PostComment>>>()
  private val localMemories = MutableStateFlow<List<SavedMemory>>(emptyList())

  private fun getEffectiveUserId(): String {
    return auth.currentUser?.uid ?: prefsManager?.getOrCreateGuestUserId() ?: "kora_lead_user"
  }

  fun observePosts(audienceFilter: String? = null): Flow<List<Post>> {
    if (auth.currentUser != null) {
      val query: Query = if (audienceFilter != null && audienceFilter != "all") {
        db.collection("posts")
          .whereEqualTo("audience", audienceFilter)
          .limit(50)
      } else {
        db.collection("posts")
          .orderBy("createdAt", Query.Direction.DESCENDING)
          .limit(50)
      }

      return query.snapshots()
        .map { snapshot ->
          snapshot.toObjects(Post::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            .sortedByDescending { it.createdAt }
        }
        .catch { e ->
          handleFirestoreError(e, OperationType.LIST, "posts", auth)
          emit(emptyList())
        }
    } else {
      return localPosts.asStateFlow()
    }
  }

  suspend fun createPost(
    content: String,
    audience: String,
    authorName: String,
    authorHandle: String,
    isAuthorVerified: Boolean = false,
    authorVerifiedTier: String = "GOLD"
  ): Result<Unit> {
    val uid = getEffectiveUserId()
    val postId = UUID.randomUUID().toString()
    val validTier = if (authorVerifiedTier in listOf("BLUE", "GOLD", "EMERALD", "ROSE_GOLD", "AMETHYST", "ONYX")) {
      authorVerifiedTier
    } else {
      "GOLD"
    }
    val validAudience = if (audience == "circles") "circles" else "public"

    if (auth.currentUser != null) {
      val postRef = db.collection("posts").document(postId)
      val payload = mapOf(
        "authorId" to uid,
        "authorName" to authorName.ifBlank { "Member" }.take(100),
        "authorHandle" to authorHandle.ifBlank { "@member" }.take(50),
        "isAuthorVerified" to isAuthorVerified,
        "authorVerifiedTier" to validTier,
        "content" to content.trim().take(5000),
        "audience" to validAudience,
        "likedBy" to emptyList<String>(),
        "likesCount" to 0L,
        "commentsCount" to 0L,
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp()
      )

      return try {
        postRef.set(payload).await()
        Result.success(Unit)
      } catch (e: Exception) {
        handleFirestoreError(e, OperationType.CREATE, postRef.path, auth)
        Result.failure(e)
      }
    }

    val now = Timestamp.now()
    val newPost = Post(
      id = postId,
      authorId = uid,
      authorName = authorName,
      authorHandle = authorHandle,
      isAuthorVerified = isAuthorVerified,
      authorVerifiedTier = validTier,
      content = content,
      audience = validAudience,
      likedBy = emptyList(),
      likesCount = 0L,
      commentsCount = 0L,
      createdAt = now
    )
    localPosts.value = listOf(newPost) + localPosts.value
    localComments[postId] = MutableStateFlow(emptyList())
    return Result.success(Unit)
  }

  suspend fun toggleLikePost(postId: String, currentLikes: List<String>): Result<Unit> {
    val uid = getEffectiveUserId()
    val isLiked = currentLikes.contains(uid)
    val updatedList = if (isLiked) currentLikes - uid else currentLikes + uid

    if (auth.currentUser != null) {
      val postRef = db.collection("posts").document(postId)
      return try {
        postRef.update(
          mapOf(
            "likedBy" to updatedList,
            "likesCount" to updatedList.size.toLong()
          )
        ).await()
        Result.success(Unit)
      } catch (e: Exception) {
        handleFirestoreError(e, OperationType.UPDATE, postRef.path, auth)
        Result.failure(e)
      }
    }

    localPosts.value = localPosts.value.map { post ->
      if (post.id == postId) {
        post.copy(likedBy = updatedList, likesCount = updatedList.size.toLong())
      } else post
    }
    return Result.success(Unit)
  }

  fun observePostComments(postId: String): Flow<List<PostComment>> {
    val localFlow = localComments.getOrPut(postId) { MutableStateFlow(emptyList()) }

    if (auth.currentUser != null) {
      val query = db.collection("posts")
        .document(postId)
        .collection("comments")
        .orderBy("createdAt", Query.Direction.ASCENDING)

      return query.snapshots()
        .map { snapshot ->
          snapshot.toObjects(PostComment::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
        }
        .catch { e ->
          handleFirestoreError(e, OperationType.LIST, "posts/$postId/comments", auth)
          emit(emptyList())
        }
    } else {
      return localFlow.asStateFlow()
    }
  }

  suspend fun addComment(
    postId: String,
    content: String,
    authorName: String,
    authorHandle: String,
    isAuthorVerified: Boolean = false
  ): Result<Unit> {
    val uid = getEffectiveUserId()
    val commentId = UUID.randomUUID().toString()

    if (auth.currentUser != null) {
      val commentRef = db.collection("posts")
        .document(postId)
        .collection("comments")
        .document(commentId)
      val postRef = db.collection("posts").document(postId)

      val payload = mapOf(
        "postId" to postId,
        "authorId" to uid,
        "authorName" to authorName.ifBlank { "Member" }.take(100),
        "authorHandle" to authorHandle.ifBlank { "@member" }.take(50),
        "isAuthorVerified" to isAuthorVerified,
        "content" to content.trim().take(2000),
        "createdAt" to FieldValue.serverTimestamp()
      )

      return try {
        val batch = db.batch()
        batch.set(commentRef, payload)
        batch.update(postRef, "commentsCount", FieldValue.increment(1L))
        batch.commit().await()
        Result.success(Unit)
      } catch (e: Exception) {
        handleFirestoreError(e, OperationType.CREATE, commentRef.path, auth)
        Result.failure(e)
      }
    }

    val now = Timestamp.now()
    val newComment = PostComment(
      id = commentId,
      postId = postId,
      authorId = uid,
      authorName = authorName,
      authorHandle = authorHandle,
      isAuthorVerified = isAuthorVerified,
      content = content,
      createdAt = now
    )
    val commentFlow = localComments.getOrPut(postId) { MutableStateFlow(emptyList()) }
    commentFlow.value = commentFlow.value + newComment
    localPosts.value = localPosts.value.map { post ->
      if (post.id == postId) post.copy(commentsCount = post.commentsCount + 1L) else post
    }
    return Result.success(Unit)
  }

  suspend fun saveToMemory(
    messageId: String,
    snippet: String,
    senderName: String,
    note: String? = null,
    conversationId: String? = null
  ): Result<Unit> {
    val uid = getEffectiveUserId()
    val memoryId = UUID.randomUUID().toString()

    if (auth.currentUser != null) {
      val memRef = db.collection("users")
        .document(uid)
        .collection("saved_memories")
        .document(memoryId)

      val payload = mapOf(
        "userId" to uid,
        "originalMessageId" to messageId.ifBlank { "note" }.take(128),
        "conversationId" to (conversationId ?: "").take(128),
        "snippet" to snippet.trim().take(1000),
        "senderName" to senderName.take(100),
        "privateNote" to (note ?: "").take(2000),
        "createdAt" to FieldValue.serverTimestamp()
      )

      return try {
        memRef.set(payload).await()
        Result.success(Unit)
      } catch (e: Exception) {
        handleFirestoreError(e, OperationType.CREATE, memRef.path, auth)
        Result.failure(e)
      }
    }

    val now = Timestamp.now()
    val newMemory = SavedMemory(
      id = memoryId,
      userId = uid,
      originalMessageId = messageId,
      conversationId = conversationId ?: "",
      snippet = snippet,
      senderName = senderName,
      privateNote = note ?: "",
      createdAt = now
    )
    localMemories.value = listOf(newMemory) + localMemories.value
    return Result.success(Unit)
  }

  fun observeSavedMemories(): Flow<List<SavedMemory>> {
    if (auth.currentUser != null) {
      val uid = auth.currentUser!!.uid
      val query = db.collection("users")
        .document(uid)
        .collection("saved_memories")
        .orderBy("createdAt", Query.Direction.DESCENDING)

      return query.snapshots()
        .map { snapshot ->
          snapshot.toObjects(SavedMemory::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
        }
        .catch { e ->
          handleFirestoreError(e, OperationType.LIST, "users/$uid/saved_memories", auth)
          emit(emptyList())
        }
    } else {
      return localMemories.asStateFlow()
    }
  }

  suspend fun deleteMemory(memoryId: String): Result<Unit> {
    if (auth.currentUser != null) {
      val uid = auth.currentUser!!.uid
      val memRef = db.collection("users")
        .document(uid)
        .collection("saved_memories")
        .document(memoryId)

      return try {
        memRef.delete().await()
        Result.success(Unit)
      } catch (e: Exception) {
        handleFirestoreError(e, OperationType.DELETE, memRef.path, auth)
        Result.failure(e)
      }
    }

    localMemories.value = localMemories.value.filter { it.id != memoryId }
    return Result.success(Unit)
  }

  fun observeComments(postId: String): Flow<List<PostComment>> = observePostComments(postId)

  suspend fun toggleLike(postId: String, isLiked: Boolean): Result<Unit> {
    val uid = getEffectiveUserId()
    if (auth.currentUser != null) {
      val postRef = db.collection("posts").document(postId)
      return try {
        val snapshot = postRef.get().await()
        val existing = snapshot.toObject(Post::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
        val currentLikes = existing?.likedBy ?: emptyList()
        val updated = if (currentLikes.contains(uid)) currentLikes - uid else currentLikes + uid
        postRef.update(
          mapOf(
            "likedBy" to updated,
            "likesCount" to updated.size.toLong()
          )
        ).await()
        Result.success(Unit)
      } catch (e: Exception) {
        handleFirestoreError(e, OperationType.UPDATE, postRef.path, auth)
        Result.failure(e)
      }
    }
    val post = localPosts.value.find { it.id == postId }
    return toggleLikePost(postId, post?.likedBy ?: emptyList())
  }

  suspend fun saveMemory(
    originalMessageId: String,
    conversationId: String,
    snippet: String,
    senderName: String,
    privateNote: String = ""
  ): Result<Unit> = saveToMemory(
    messageId = originalMessageId,
    snippet = snippet,
    senderName = senderName,
    note = privateNote,
    conversationId = conversationId
  )

  suspend fun deleteSavedMemory(memoryId: String): Result<Unit> = deleteMemory(memoryId)
}
