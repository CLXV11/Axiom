package com.example.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.R
import com.example.data.model.Conversation
import com.example.data.model.Message
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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import java.util.UUID

data class AiConversationState(
  val isGenerating: Boolean = false,
  val errorMessage: String? = null,
  val lastFailedPrompt: String? = null
)

class ChatRepository(
  private val db: FirebaseFirestore,
  private val prefsManager: PreferencesManager? = null,
  private val aiChatService: AiChatService = AiChatService(),
  private val auth: FirebaseAuth = Firebase.auth
) {
  constructor(context: Context) : this(
    FirebaseFirestore.getInstance(
      context.applicationContext.getString(R.string.firestore_database_id)
    ),
    PreferencesManager(context.applicationContext),
    AiChatService(),
    Firebase.auth
  )

  companion object {
    const val AI_CHATBOT_CONVO_ID = "convo_kora_ai"
    const val AI_CHATBOT_SENDER_ID = "kora_ai_bot"
    const val AI_CHATBOT_NAME = "Kora AI"
    private const val TAG = "ChatRepository"
    val VALID_TIERS = listOf("BLUE", "GOLD", "EMERALD", "ROSE_GOLD", "AMETHYST", "ONYX", "PLATINUM")

    fun canonicalDirectConversationId(uidA: String, uidB: String): String {
      val sorted = listOf(uidA.trim(), uidB.trim()).sorted()
      val raw = "dm_${sorted[0]}_${sorted[1]}"
      val sanitized = raw.replace(Regex("[^a-zA-Z0-9_-]"), "_")
      return if (sanitized.length <= 128) {
        sanitized
      } else {
        val digest = java.security.MessageDigest.getInstance("SHA-256")
          .digest(raw.toByteArray())
          .joinToString("") { "%02x".format(it) }
        "dm_${digest.take(40)}"
      }
    }

    fun deduplicateDirectConversations(
      list: List<Conversation>,
      currentUid: String
    ): List<Conversation> {
      val nonAi = list.filterNot { it.id == AI_CHATBOT_CONVO_ID }
      val groups = nonAi.filter { it.type != "direct" }
      val directs = nonAi.filter { it.type == "direct" }

      val dedupedDirects = directs
        .groupBy { convo ->
          convo.participantIds.firstOrNull { it != currentUid }
            ?: convo.participantIds.firstOrNull()
            ?: convo.id
        }
        .map { (peerId, candidates) ->
          val canonicalId = canonicalDirectConversationId(currentUid, peerId)
          candidates.maxWith(
            Comparator { a, b ->
              val aHasMsg = if (a.lastMessageText.isNotBlank()) 1 else 0
              val bHasMsg = if (b.lastMessageText.isNotBlank()) 1 else 0
              if (aHasMsg != bHasMsg) return@Comparator aHasMsg.compareTo(bHasMsg)

              val aTime = (a.lastMessageTimestamp ?: a.createdAt)?.seconds ?: 0L
              val bTime = (b.lastMessageTimestamp ?: b.createdAt)?.seconds ?: 0L
              if (aTime != bTime) return@Comparator aTime.compareTo(bTime)

              val aCanonical = if (a.id == canonicalId) 1 else 0
              val bCanonical = if (b.id == canonicalId) 1 else 0
              if (aCanonical != bCanonical) return@Comparator aCanonical.compareTo(bCanonical)

              val aActive = if (!a.archivedBy.contains(currentUid)) 1 else 0
              val bActive = if (!b.archivedBy.contains(currentUid)) 1 else 0
              if (aActive != bActive) return@Comparator aActive.compareTo(bActive)

              a.id.compareTo(b.id)
            }
          )
        }

      return (dedupedDirects + groups).sortedWith(
        compareByDescending<Conversation> { it.pinnedBy.contains(currentUid) }
          .thenByDescending { (it.lastMessageTimestamp ?: it.createdAt)?.seconds ?: 0L }
          .thenByDescending { (it.lastMessageTimestamp ?: it.createdAt)?.nanoseconds ?: 0 }
      )
    }
  }

  private val repositoryScope = CoroutineScope(Dispatchers.Main.immediate)

  private val localConversations = MutableStateFlow<List<Conversation>>(emptyList())
  private var cachedFirestoreConversations: List<Conversation> = emptyList()
  private val localMessages = mutableMapOf<String, MutableStateFlow<List<Message>>>()
  private val localTypingUsers = mutableMapOf<String, MutableStateFlow<List<String>>>()
  private val aiStates = mutableMapOf<String, MutableStateFlow<AiConversationState>>()
  private var activeAiJob: Job? = null

  init {
    initSingleAiChatbot()
  }

  private fun getEffectiveUserId(): String {
    return auth.currentUser?.uid ?: prefsManager?.getOrCreateGuestUserId() ?: "kora_lead_user"
  }

  private fun createAiChatbotConversation(uid: String): Conversation {
    val now = Timestamp.now()
    return Conversation(
      id = AI_CHATBOT_CONVO_ID,
      type = "direct",
      title = AI_CHATBOT_NAME,
      isVerified = true,
      verifiedTier = "AMETHYST",
      participantIds = listOf(uid, AI_CHATBOT_SENDER_ID),
      lastMessageText = "",
      lastMessageSenderId = AI_CHATBOT_SENDER_ID,
      pinnedBy = listOf(uid),
      lastMessageTimestamp = now,
      createdAt = now
    )
  }

  private fun initSingleAiChatbot() {
    val uid = getEffectiveUserId()
    localConversations.value = listOf(createAiChatbotConversation(uid))
    localMessages[AI_CHATBOT_CONVO_ID] = MutableStateFlow(emptyList())
  }

  fun isAiChatbotConversation(conversationId: String): Boolean {
    return conversationId == AI_CHATBOT_CONVO_ID
  }

  fun observeAiState(conversationId: String): Flow<AiConversationState> {
    return aiStates.getOrPut(conversationId) { MutableStateFlow(AiConversationState()) }.asStateFlow()
  }

  fun clearAiError(conversationId: String) {
    val flow = aiStates.getOrPut(conversationId) { MutableStateFlow(AiConversationState()) }
    flow.value = flow.value.copy(errorMessage = null)
  }

  fun clearAiConversation() {
    activeAiJob?.cancel()
    setTyping(AI_CHATBOT_CONVO_ID, AI_CHATBOT_NAME, false)
    localMessages.getOrPut(AI_CHATBOT_CONVO_ID) { MutableStateFlow(emptyList()) }.value = emptyList()
    aiStates.getOrPut(AI_CHATBOT_CONVO_ID) { MutableStateFlow(AiConversationState()) }.value = AiConversationState()
    localConversations.value = localConversations.value.map { convo ->
      if (convo.id == AI_CHATBOT_CONVO_ID) {
        convo.copy(lastMessageText = "", lastMessageTimestamp = Timestamp.now())
      } else convo
    }
  }

  fun observeConversations(): Flow<List<Conversation>> {
    val currentUser = auth.currentUser
    if (currentUser != null) {
      val uid = currentUser.uid
      val query = db.collection("conversations")
        .whereArrayContains("participantIds", uid)

      return query.snapshots()
        .map { snapshot ->
          val firestoreList = snapshot.toObjects(
            Conversation::class.java,
            DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
          )
          val dedupedOthers = deduplicateDirectConversations(firestoreList, uid)
          cachedFirestoreConversations = dedupedOthers
          val aiConvo = localConversations.value.find { it.id == AI_CHATBOT_CONVO_ID }
            ?.copy(participantIds = listOf(uid, AI_CHATBOT_SENDER_ID), pinnedBy = listOf(uid))
            ?: createAiChatbotConversation(uid)
          listOf(aiConvo) + dedupedOthers
        }
        .catch { e ->
          handleFirestoreError(e, OperationType.LIST, "conversations", auth)
          val aiConvo = localConversations.value.find { it.id == AI_CHATBOT_CONVO_ID }
            ?.copy(participantIds = listOf(uid, AI_CHATBOT_SENDER_ID), pinnedBy = listOf(uid))
            ?: createAiChatbotConversation(uid)
          val others = deduplicateDirectConversations(cachedFirestoreConversations, uid)
          emit(listOf(aiConvo) + others)
        }
    } else {
      return localConversations.asStateFlow()
    }
  }

  suspend fun findOrCreateDirectConversation(
    targetUserId: String,
    title: String? = null,
    isVerified: Boolean = false,
    verifiedTier: String = "BLUE"
  ): Result<String> {
    val uid = getEffectiveUserId()
    val canonicalId = canonicalDirectConversationId(uid, targetUserId)

    if (auth.currentUser != null) {
      try {
        val existingSnap = db.collection("conversations")
          .whereArrayContains("participantIds", uid)
          .get()
          .await()
        val existingList = existingSnap.toObjects(
          Conversation::class.java,
          DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
        )
        val matchingDirects = existingList.filter { convo ->
          convo.id == canonicalId || (
            convo.type == "direct" &&
              convo.participantIds.contains(targetUserId) &&
              convo.participantIds.size == 2
            )
        }
        if (matchingDirects.isNotEmpty()) {
          val winner = deduplicateDirectConversations(matchingDirects, uid).firstOrNull()
            ?: matchingDirects.first()
          if (winner.archivedBy.contains(uid)) {
            runCatching { toggleArchiveConversation(winner.id, true) }
          }
          return Result.success(winner.id)
        }
      } catch (e: Exception) {
        handleFirestoreError(e, OperationType.LIST, "conversations", auth)
        return Result.failure(e)
      }

      val createResult = createConversationWithId(
        convoId = canonicalId,
        type = "direct",
        targetUserIds = listOf(targetUserId),
        title = title,
        isVerified = isVerified,
        verifiedTier = verifiedTier
      )
      if (createResult.isSuccess) return createResult

      // Recover cleanly if peer created canonicalId concurrently
      val retrySnap = runCatching {
        db.collection("conversations")
          .whereArrayContains("participantIds", uid)
          .get()
          .await()
      }.getOrNull()
      val concurrent = retrySnap?.toObjects(
        Conversation::class.java,
        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
      )?.firstOrNull { convo ->
        convo.id == canonicalId ||
          (convo.type == "direct" && convo.participantIds.contains(targetUserId) && convo.participantIds.size == 2)
      }
      return if (concurrent != null) Result.success(concurrent.id) else createResult
    } else {
      val existing = localConversations.value.firstOrNull { convo ->
        convo.type == "direct" &&
          convo.participantIds.contains(targetUserId) &&
          convo.participantIds.contains(uid)
      }
      if (existing != null) return Result.success(existing.id)
      return createConversationWithId(
        convoId = canonicalId,
        type = "direct",
        targetUserIds = listOf(targetUserId),
        title = title,
        isVerified = isVerified,
        verifiedTier = verifiedTier
      )
    }
  }

  suspend fun createConversation(
    type: String,
    targetUserIds: List<String>,
    title: String? = null,
    isVerified: Boolean = false,
    verifiedTier: String = "BLUE"
  ): Result<String> {
    val uid = getEffectiveUserId()
    val convoId = if (type == "direct" && targetUserIds.size == 1) {
      canonicalDirectConversationId(uid, targetUserIds.first())
    } else {
      UUID.randomUUID().toString()
    }
    return createConversationWithId(
      convoId = convoId,
      type = type,
      targetUserIds = targetUserIds,
      title = title,
      isVerified = isVerified,
      verifiedTier = verifiedTier
    )
  }

  private suspend fun createConversationWithId(
    convoId: String,
    type: String,
    targetUserIds: List<String>,
    title: String? = null,
    isVerified: Boolean = false,
    verifiedTier: String = "BLUE"
  ): Result<String> {
    val uid = getEffectiveUserId()
    val allParticipants = (targetUserIds + uid).distinct()
    val validTier = if (verifiedTier in VALID_TIERS) verifiedTier else "BLUE"

    if (auth.currentUser != null) {
      val convoRef = db.collection("conversations").document(convoId)
      val payload = mapOf(
        "type" to type,
        "title" to (title ?: "").take(100),
        "isVerified" to isVerified,
        "verifiedTier" to validTier,
        "participantIds" to allParticipants,
        "lastMessageText" to "",
        "lastMessageSenderId" to "",
        "pinnedBy" to emptyList<String>(),
        "archivedBy" to emptyList<String>(),
        "mutedBy" to emptyList<String>(),
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp()
      )

      return try {
        convoRef.set(payload).await()
        Result.success(convoId)
      } catch (e: Exception) {
        handleFirestoreError(e, OperationType.CREATE, convoRef.path, auth)
        Result.failure(e)
      }
    }

    val now = Timestamp.now()
    val newConvo = Conversation(
      id = convoId,
      type = type,
      title = title ?: if (type == "group") "Group Chat" else "Direct Chat",
      isVerified = isVerified,
      verifiedTier = validTier,
      participantIds = allParticipants,
      lastMessageText = "",
      lastMessageSenderId = uid,
      lastMessageTimestamp = now,
      createdAt = now
    )
    localConversations.value = localConversations.value + newConvo
    localMessages[convoId] = MutableStateFlow(emptyList())
    return Result.success(convoId)
  }

  fun observeMessages(conversationId: String): Flow<List<Message>> {
    val localFlow = localMessages.getOrPut(conversationId) { MutableStateFlow(emptyList()) }

    if (auth.currentUser != null && !isAiChatbotConversation(conversationId)) {
      val query = db.collection("conversations")
        .document(conversationId)
        .collection("messages")
        .orderBy("createdAt", Query.Direction.ASCENDING)

      return query.snapshots()
        .map { snapshot ->
          snapshot.toObjects(
            Message::class.java,
            DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
          ).also { msgs ->
            localFlow.value = msgs
          }
        }
        .catch { e ->
          handleFirestoreError(e, OperationType.LIST, "conversations/$conversationId/messages", auth)
          emit(localFlow.value)
        }
    } else {
      return localFlow.asStateFlow()
    }
  }

  suspend fun sendMessage(
    conversationId: String,
    content: String,
    senderName: String,
    replyTo: Message? = null,
    isSenderVerified: Boolean = false,
    senderVerifiedTier: String = "BLUE",
    isSpoiler: Boolean = false,
    forwardedFromName: String? = null
  ): Result<Unit> {
    val uid = getEffectiveUserId()
    val messageId = UUID.randomUUID().toString()
    val validTier = if (senderVerifiedTier in VALID_TIERS) senderVerifiedTier else "BLUE"
    val cleanForwardedFrom = forwardedFromName?.trim()?.take(100)?.takeIf { it.isNotBlank() }
    val previewSummary = (if (isSpoiler) "🙈 $content" else content).take(150)

    if (isAiChatbotConversation(conversationId)) {
      val now = Timestamp.now()
      val newMsg = Message(
        id = messageId,
        conversationId = conversationId,
        senderId = uid,
        senderName = senderName,
        isSenderVerified = isSenderVerified,
        senderVerifiedTier = validTier,
        content = content,
        isSpoiler = isSpoiler,
        forwardedFromName = cleanForwardedFrom,
        replyToId = replyTo?.id,
        replyToSender = replyTo?.senderName,
        replyToText = replyTo?.content?.take(100),
        status = "read",
        readBy = listOf(uid),
        isEdited = false,
        isDeleted = false,
        createdAt = now
      )
      val msgFlow = localMessages.getOrPut(conversationId) { MutableStateFlow(emptyList()) }
      val priorHistory = msgFlow.value
      msgFlow.value = priorHistory + newMsg

      localConversations.value = localConversations.value.map { convo ->
        if (convo.id == conversationId) {
          convo.copy(
            lastMessageText = previewSummary,
            lastMessageSenderId = uid,
            lastMessageTimestamp = now
          )
        } else convo
      }

      val effectivePrompt = if (replyTo != null && replyTo.content.isNotBlank()) {
        "[Replying to ${replyTo.senderName}: \"${replyTo.content.take(200)}\"]\n$content"
      } else {
        content
      }
      launchAiReply(priorHistory = priorHistory, userPrompt = effectivePrompt)
      return Result.success(Unit)
    }

    if (auth.currentUser != null) {
      val msgRef = db.collection("conversations")
        .document(conversationId)
        .collection("messages")
        .document(messageId)
      val convoRef = db.collection("conversations").document(conversationId)

      val payload = buildMap<String, Any> {
        put("conversationId", conversationId)
        put("senderId", uid)
        put("senderName", senderName.ifBlank { "Member" }.take(100))
        put("isSenderVerified", isSenderVerified)
        put("senderVerifiedTier", validTier)
        put("content", content.take(4000))
        put("mediaType", "text")
        put("isSpoiler", isSpoiler)
        if (cleanForwardedFrom != null) {
          put("forwardedFromName", cleanForwardedFrom)
        }
        if (replyTo != null) {
          put("replyToId", replyTo.id)
          put("replyToSender", replyTo.senderName.take(100))
          put("replyToText", replyTo.content.take(500))
        }
        put("status", "sent")
        put("readBy", listOf(uid))
        put("isEdited", false)
        put("isDeleted", false)
        put("createdAt", FieldValue.serverTimestamp())
        put("updatedAt", FieldValue.serverTimestamp())
      }

      return try {
        val batch = db.batch()
        batch.set(msgRef, payload)
        batch.update(
          convoRef,
          mapOf(
            "lastMessageText" to previewSummary,
            "lastMessageSenderId" to uid,
            "lastMessageTimestamp" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
          )
        )
        batch.commit().await()
        Result.success(Unit)
      } catch (e: Exception) {
        handleFirestoreError(e, OperationType.CREATE, msgRef.path, auth)
        Result.failure(e)
      }
    }

    val now = Timestamp.now()
    val newMsg = Message(
      id = messageId,
      conversationId = conversationId,
      senderId = uid,
      senderName = senderName,
      isSenderVerified = isSenderVerified,
      senderVerifiedTier = validTier,
      content = content,
      isSpoiler = isSpoiler,
      forwardedFromName = cleanForwardedFrom,
      replyToId = replyTo?.id,
      replyToSender = replyTo?.senderName,
      replyToText = replyTo?.content?.take(100),
      status = "read",
      readBy = listOf(uid),
      isEdited = false,
      isDeleted = false,
      createdAt = now
    )
    val msgFlow = localMessages.getOrPut(conversationId) { MutableStateFlow(emptyList()) }
    msgFlow.value = msgFlow.value + newMsg
    localConversations.value = localConversations.value.map { convo ->
      if (convo.id == conversationId) {
        convo.copy(
          lastMessageText = previewSummary,
          lastMessageSenderId = uid,
          lastMessageTimestamp = now
        )
      } else convo
    }
    return Result.success(Unit)
  }

  suspend fun sendMediaMessage(
    conversationId: String,
    media: PreparedMediaMetadata,
    caption: String,
    senderName: String,
    replyTo: Message? = null,
    isSenderVerified: Boolean = false,
    senderVerifiedTier: String = "BLUE",
    isSpoiler: Boolean = false,
    forwardedFromName: String? = null
  ): Result<Unit> {
    val uid = getEffectiveUserId()
    val messageId = UUID.randomUUID().toString()
    val validTier = if (senderVerifiedTier in VALID_TIERS) senderVerifiedTier else "BLUE"
    val cleanForwardedFrom = forwardedFromName?.trim()?.take(100)?.takeIf { it.isNotBlank() }
    val baseLabel = when (media.mediaType) {
      "image" -> "📷 ${caption.ifBlank { media.fileName }}"
      "video" -> "🎬 ${caption.ifBlank { media.fileName }}"
      "audio" -> "🎤 ${caption.ifBlank { ChatMediaRepository.formatDuration(media.durationMs) }}"
      else -> "📎 ${media.fileName}"
    }
    val previewLabel = if (isSpoiler) "🙈 $baseLabel" else baseLabel
    val messageContent = caption.trim().ifBlank { baseLabel }

    if (auth.currentUser != null && !isAiChatbotConversation(conversationId)) {
      val msgRef = db.collection("conversations")
        .document(conversationId)
        .collection("messages")
        .document(messageId)
      val convoRef = db.collection("conversations").document(conversationId)

      val payload = buildMap<String, Any> {
        put("conversationId", conversationId)
        put("senderId", uid)
        put("senderName", senderName.ifBlank { "Member" }.take(100))
        put("isSenderVerified", isSenderVerified)
        put("senderVerifiedTier", validTier)
        put("content", messageContent.take(4000))
        put("mediaType", media.mediaType)
        put("mediaId", media.mediaId)
        put("mediaFileName", media.fileName.take(255))
        put("mediaMimeType", media.mimeType.take(128))
        put("mediaSizeBytes", media.sizeBytes)
        put("mediaChunkCount", media.chunkCount)
        put("mediaDurationMs", media.durationMs)
        put("isSpoiler", isSpoiler)
        if (cleanForwardedFrom != null) {
          put("forwardedFromName", cleanForwardedFrom)
        }
        if (replyTo != null) {
          put("replyToId", replyTo.id)
          put("replyToSender", replyTo.senderName.take(100))
          put("replyToText", replyTo.content.take(500))
        }
        put("status", "sent")
        put("readBy", listOf(uid))
        put("isEdited", false)
        put("isDeleted", false)
        put("createdAt", FieldValue.serverTimestamp())
        put("updatedAt", FieldValue.serverTimestamp())
      }

      return try {
        val batch = db.batch()
        batch.set(msgRef, payload)
        batch.update(
          convoRef,
          mapOf(
            "lastMessageText" to previewLabel.take(150),
            "lastMessageSenderId" to uid,
            "lastMessageTimestamp" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
          )
        )
        batch.commit().await()
        Result.success(Unit)
      } catch (e: Exception) {
        handleFirestoreError(e, OperationType.CREATE, msgRef.path, auth)
        Result.failure(e)
      }
    }

    val now = Timestamp.now()
    val newMsg = Message(
      id = messageId,
      conversationId = conversationId,
      senderId = uid,
      senderName = senderName,
      isSenderVerified = isSenderVerified,
      senderVerifiedTier = validTier,
      content = messageContent,
      mediaType = media.mediaType,
      mediaId = media.mediaId,
      mediaFileName = media.fileName,
      mediaMimeType = media.mimeType,
      mediaSizeBytes = media.sizeBytes,
      mediaChunkCount = media.chunkCount,
      mediaDurationMs = media.durationMs,
      localMediaUri = Uri.fromFile(media.localFile).toString(),
      isSpoiler = isSpoiler,
      forwardedFromName = cleanForwardedFrom,
      replyToId = replyTo?.id,
      replyToSender = replyTo?.senderName,
      replyToText = replyTo?.content?.take(100),
      status = "read",
      readBy = listOf(uid),
      isEdited = false,
      isDeleted = false,
      createdAt = now
    )
    val msgFlow = localMessages.getOrPut(conversationId) { MutableStateFlow(emptyList()) }
    msgFlow.value = msgFlow.value + newMsg
    localConversations.value = localConversations.value.map { convo ->
      if (convo.id == conversationId) {
        convo.copy(
          lastMessageText = previewLabel.take(150),
          lastMessageSenderId = uid,
          lastMessageTimestamp = now
        )
      } else convo
    }
    return Result.success(Unit)
  }

  suspend fun sendPollMessage(
    conversationId: String,
    question: String,
    options: List<String>,
    senderName: String,
    isSenderVerified: Boolean = false,
    senderVerifiedTier: String = "BLUE",
    forwardedFromName: String? = null
  ): Result<Unit> {
    val cleanQuestion = question.trim().take(240)
    val cleanOptions = options.map { it.trim().take(120) }.filter { it.isNotBlank() }.distinct().take(6)
    if (cleanQuestion.isBlank() || cleanOptions.size < 2) {
      return Result.failure(IllegalArgumentException("Poll requires a question and at least 2 options"))
    }
    val uid = getEffectiveUserId()
    val messageId = UUID.randomUUID().toString()
    val validTier = if (senderVerifiedTier in VALID_TIERS) senderVerifiedTier else "BLUE"
    val cleanForwardedFrom = forwardedFromName?.trim()?.take(100)?.takeIf { it.isNotBlank() }
    val previewLabel = "📊 $cleanQuestion".take(150)

    if (auth.currentUser != null && !isAiChatbotConversation(conversationId)) {
      val msgRef = db.collection("conversations")
        .document(conversationId)
        .collection("messages")
        .document(messageId)
      val convoRef = db.collection("conversations").document(conversationId)

      val payload = buildMap<String, Any> {
        put("conversationId", conversationId)
        put("senderId", uid)
        put("senderName", senderName.ifBlank { "Member" }.take(100))
        put("isSenderVerified", isSenderVerified)
        put("senderVerifiedTier", validTier)
        put("content", cleanQuestion)
        put("mediaType", "poll")
        put("pollQuestion", cleanQuestion)
        put("pollOptions", cleanOptions)
        put("pollVotes", emptyMap<String, List<String>>())
        if (cleanForwardedFrom != null) {
          put("forwardedFromName", cleanForwardedFrom)
        }
        put("status", "sent")
        put("readBy", listOf(uid))
        put("isEdited", false)
        put("isDeleted", false)
        put("createdAt", FieldValue.serverTimestamp())
        put("updatedAt", FieldValue.serverTimestamp())
      }

      return try {
        val batch = db.batch()
        batch.set(msgRef, payload)
        batch.update(
          convoRef,
          mapOf(
            "lastMessageText" to previewLabel,
            "lastMessageSenderId" to uid,
            "lastMessageTimestamp" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
          )
        )
        batch.commit().await()
        Result.success(Unit)
      } catch (e: Exception) {
        handleFirestoreError(e, OperationType.CREATE, msgRef.path, auth)
        Result.failure(e)
      }
    }

    val now = Timestamp.now()
    val newMsg = Message(
      id = messageId,
      conversationId = conversationId,
      senderId = uid,
      senderName = senderName,
      isSenderVerified = isSenderVerified,
      senderVerifiedTier = validTier,
      content = cleanQuestion,
      mediaType = "poll",
      pollQuestion = cleanQuestion,
      pollOptions = cleanOptions,
      pollVotes = emptyMap(),
      forwardedFromName = cleanForwardedFrom,
      status = "read",
      readBy = listOf(uid),
      isEdited = false,
      isDeleted = false,
      createdAt = now
    )
    val msgFlow = localMessages.getOrPut(conversationId) { MutableStateFlow(emptyList()) }
    msgFlow.value = msgFlow.value + newMsg
    localConversations.value = localConversations.value.map { convo ->
      if (convo.id == conversationId) {
        convo.copy(
          lastMessageText = previewLabel,
          lastMessageSenderId = uid,
          lastMessageTimestamp = now
        )
      } else convo
    }
    return Result.success(Unit)
  }

  suspend fun votePollOption(
    conversationId: String,
    messageId: String,
    optionIndex: Int
  ): Result<Unit> {
    if (optionIndex < 0 || optionIndex >= 6) return Result.success(Unit)
    val uid = getEffectiveUserId()
    val optionKey = optionIndex.toString()

    if (auth.currentUser != null && !isAiChatbotConversation(conversationId)) {
      val msgRef = db.collection("conversations")
        .document(conversationId)
        .collection("messages")
        .document(messageId)
      return try {
        db.runTransaction { transaction ->
          val snap = transaction.get(msgRef)
          val msg = snap.toObject(Message::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            ?: throw IllegalStateException("Poll message not found")
          if (msg.isDeleted || !msg.isPollMessage() || optionIndex >= msg.pollOptions.size) {
            return@runTransaction Unit
          }
          val updated = msg.pollVotes.mapValues { it.value.toMutableList() }.toMutableMap()
          val alreadyVotedThisOption = updated[optionKey]?.contains(uid) == true
          // Single-choice poll: remove voter from all options first, then toggle target option
          updated.values.forEach { it.remove(uid) }
          if (!alreadyVotedThisOption) {
            updated.getOrPut(optionKey) { mutableListOf() }.add(uid)
          }
          val cleanMap = updated
            .filterValues { it.isNotEmpty() }
            .mapValues { it.value.distinct().take(200) }
          transaction.update(
            msgRef,
            mapOf(
              "pollVotes" to cleanMap,
              "updatedAt" to FieldValue.serverTimestamp()
            )
          )
          Unit
        }.await()
        Result.success(Unit)
      } catch (e: Exception) {
        handleFirestoreError(e, OperationType.UPDATE, msgRef.path, auth)
        Result.failure(e)
      }
    }

    val msgFlow = localMessages[conversationId]
    if (msgFlow != null) {
      msgFlow.value = msgFlow.value.map { msg ->
        if (msg.id == messageId && !msg.isDeleted && msg.isPollMessage() && optionIndex < msg.pollOptions.size) {
          val updated = msg.pollVotes.mapValues { it.value.toMutableList() }.toMutableMap()
          val alreadyVoted = updated[optionKey]?.contains(uid) == true
          updated.values.forEach { it.remove(uid) }
          if (!alreadyVoted) {
            updated.getOrPut(optionKey) { mutableListOf() }.add(uid)
          }
          msg.copy(pollVotes = updated.filterValues { it.isNotEmpty() }.mapValues { it.value.distinct() })
        } else msg
      }
    }
    return Result.success(Unit)
  }

  suspend fun pinMessageInConversation(
    conversationId: String,
    message: Message?
  ): Result<Unit> {
    if (auth.currentUser != null && !isAiChatbotConversation(conversationId)) {
      val convoRef = db.collection("conversations").document(conversationId)
      val payload = hashMapOf<String, Any?>(
        "pinnedMessageId" to message?.id?.take(128),
        "pinnedMessageText" to message?.content?.take(500),
        "pinnedMessageSender" to message?.senderName?.take(100),
        "updatedAt" to FieldValue.serverTimestamp()
      )
      return try {
        convoRef.update(payload).await()
        Result.success(Unit)
      } catch (e: Exception) {
        handleFirestoreError(e, OperationType.UPDATE, convoRef.path, auth)
        Result.failure(e)
      }
    }

    localConversations.value = localConversations.value.map { convo ->
      if (convo.id == conversationId) {
        convo.copy(
          pinnedMessageId = message?.id,
          pinnedMessageText = message?.content?.take(500),
          pinnedMessageSender = message?.senderName?.take(100)
        )
      } else convo
    }
    return Result.success(Unit)
  }

  suspend fun forwardMessage(
    targetConversationId: String,
    sourceMessage: Message,
    senderName: String,
    isSenderVerified: Boolean = false,
    senderVerifiedTier: String = "BLUE",
    chatMediaRepository: ChatMediaRepository? = null
  ): Result<Unit> {
    if (sourceMessage.isDeleted) {
      return Result.failure(IllegalStateException("Cannot forward a deleted message"))
    }
    val forwardedOrigin = sourceMessage.forwardedFromName?.takeIf { it.isNotBlank() }
      ?: sourceMessage.senderName.ifBlank { "Member" }

    if (sourceMessage.isPollMessage()) {
      return sendPollMessage(
        conversationId = targetConversationId,
        question = sourceMessage.pollQuestion ?: sourceMessage.content,
        options = sourceMessage.pollOptions,
        senderName = senderName,
        isSenderVerified = isSenderVerified,
        senderVerifiedTier = senderVerifiedTier,
        forwardedFromName = forwardedOrigin
      )
    }

    if (sourceMessage.hasMediaAttachment()) {
      val repo = chatMediaRepository
        ?: return Result.failure(IllegalStateException("Media repository required to forward attachments"))
      val fileRes = repo.resolveMediaFile(
        conversationId = sourceMessage.conversationId,
        message = sourceMessage
      )
      if (fileRes.isFailure) {
        return Result.failure(fileRes.exceptionOrNull() ?: IllegalStateException("Could not load media to forward"))
      }
      val resolvedFile = fileRes.getOrThrow()
      val uploadRes = repo.prepareAndUploadMedia(
        conversationId = targetConversationId,
        sourceUri = Uri.fromFile(resolvedFile),
        mediaType = sourceMessage.mediaType,
        isLocalOnlyChat = isAiChatbotConversation(targetConversationId)
      )
      if (uploadRes.isFailure) {
        return Result.failure(uploadRes.exceptionOrNull() ?: IllegalStateException("Could not re-upload forwarded media"))
      }
      val prepared = uploadRes.getOrThrow()
      val isSynthetic = sourceMessage.content.startsWith("📷 ") ||
        sourceMessage.content.startsWith("🎬 ") ||
        sourceMessage.content.startsWith("🎤 ") ||
        sourceMessage.content.startsWith("📎 ")
      val caption = if (isSynthetic) "" else sourceMessage.content
      return sendMediaMessage(
        conversationId = targetConversationId,
        media = prepared,
        caption = caption,
        senderName = senderName,
        isSenderVerified = isSenderVerified,
        senderVerifiedTier = senderVerifiedTier,
        isSpoiler = sourceMessage.isSpoiler,
        forwardedFromName = forwardedOrigin
      )
    }

    return sendMessage(
      conversationId = targetConversationId,
      content = sourceMessage.content,
      senderName = senderName,
      isSenderVerified = isSenderVerified,
      senderVerifiedTier = senderVerifiedTier,
      isSpoiler = sourceMessage.isSpoiler,
      forwardedFromName = forwardedOrigin
    )
  }

  fun retryLastAiPrompt() {
    val stateFlow = aiStates.getOrPut(AI_CHATBOT_CONVO_ID) { MutableStateFlow(AiConversationState()) }
    val failedPrompt = stateFlow.value.lastFailedPrompt ?: return
    val currentMessages = localMessages.getOrPut(AI_CHATBOT_CONVO_ID) { MutableStateFlow(emptyList()) }.value
    val historyBeforeLastUser = if (currentMessages.lastOrNull()?.senderId != AI_CHATBOT_SENDER_ID) {
      currentMessages.dropLast(1)
    } else {
      currentMessages
    }
    launchAiReply(priorHistory = historyBeforeLastUser, userPrompt = failedPrompt)
  }

  private fun launchAiReply(priorHistory: List<Message>, userPrompt: String) {
    activeAiJob?.cancel()

    val aiStateFlow = aiStates.getOrPut(AI_CHATBOT_CONVO_ID) { MutableStateFlow(AiConversationState()) }
    val msgFlow = localMessages.getOrPut(AI_CHATBOT_CONVO_ID) { MutableStateFlow(emptyList()) }

    aiStateFlow.value = AiConversationState(isGenerating = true, errorMessage = null, lastFailedPrompt = null)
    setTyping(AI_CHATBOT_CONVO_ID, AI_CHATBOT_NAME, true)

    val replyId = UUID.randomUUID().toString()
    var replyInserted = false

    activeAiJob = repositoryScope.launch {
      try {
        aiChatService.streamReply(
          priorMessages = priorHistory,
          userPrompt = userPrompt,
          aiSenderId = AI_CHATBOT_SENDER_ID
        ).collect { accumulatedText ->
          setTyping(AI_CHATBOT_CONVO_ID, AI_CHATBOT_NAME, false)
          val now = Timestamp.now()

          if (!replyInserted) {
            replyInserted = true
            val aiMsg = Message(
              id = replyId,
              conversationId = AI_CHATBOT_CONVO_ID,
              senderId = AI_CHATBOT_SENDER_ID,
              senderName = AI_CHATBOT_NAME,
              isSenderVerified = true,
              senderVerifiedTier = "AMETHYST",
              content = accumulatedText,
              status = "read",
              readBy = listOf(AI_CHATBOT_SENDER_ID),
              isEdited = false,
              isDeleted = false,
              createdAt = now
            )
            msgFlow.value = msgFlow.value + aiMsg
          } else {
            msgFlow.value = msgFlow.value.map { msg ->
              if (msg.id == replyId) msg.copy(content = accumulatedText) else msg
            }
          }

          localConversations.value = localConversations.value.map { convo ->
            if (convo.id == AI_CHATBOT_CONVO_ID) {
              convo.copy(
                lastMessageText = accumulatedText.take(150),
                lastMessageSenderId = AI_CHATBOT_SENDER_ID,
                lastMessageTimestamp = now
              )
            } else convo
          }
        }

        aiStateFlow.value = AiConversationState(isGenerating = false, errorMessage = null, lastFailedPrompt = null)
      } catch (e: CancellationException) {
        setTyping(AI_CHATBOT_CONVO_ID, AI_CHATBOT_NAME, false)
        throw e
      } catch (e: Exception) {
        Log.e(TAG, "AI Chatbot failed to generate reply", e)
        setTyping(AI_CHATBOT_CONVO_ID, AI_CHATBOT_NAME, false)
        aiStateFlow.value = AiConversationState(
          isGenerating = false,
          errorMessage = e.localizedMessage ?: "Failed to reach Kora AI.",
          lastFailedPrompt = userPrompt
        )
      }
    }
  }

  fun observeTyping(conversationId: String): Flow<List<String>> {
    val localFlow = localTypingUsers.getOrPut(conversationId) { MutableStateFlow(emptyList()) }.asStateFlow()
    val currentUser = auth.currentUser
    if (currentUser != null && !isAiChatbotConversation(conversationId)) {
      val uid = currentUser.uid
      val remoteFlow = db.collection("conversations")
        .document(conversationId)
        .collection("typing")
        .snapshots()
        .map { snapshot ->
          val nowMs = System.currentTimeMillis()
          snapshot.documents.mapNotNull { doc ->
            if (doc.id == uid) return@mapNotNull null
            val active = doc.getBoolean("isTyping") == true
            if (!active) return@mapNotNull null
            val ts = doc.getTimestamp("updatedAt", DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            val ageMs = ts?.toDate()?.time?.let { nowMs - it } ?: 0L
            if (ageMs > 15_000L) return@mapNotNull null
            doc.getString("displayName")?.takeIf { it.isNotBlank() }
          }.distinct()
        }
        .catch { e ->
          handleFirestoreError(e, OperationType.LIST, "conversations/$conversationId/typing", auth)
          emit(emptyList())
        }
      return combine(remoteFlow, localFlow) { remote, local ->
        (remote + local).distinct()
      }
    }
    return localFlow
  }

  fun setTyping(conversationId: String, userName: String, isTyping: Boolean) {
    val currentUser = auth.currentUser
    if (currentUser != null && !isAiChatbotConversation(conversationId)) {
      val uid = currentUser.uid
      val cleanName = userName.trim().ifBlank { "Member" }.take(100)
      val typingRef = db.collection("conversations")
        .document(conversationId)
        .collection("typing")
        .document(uid)
      repositoryScope.launch(Dispatchers.IO) {
        runCatching {
          typingRef.set(
            mapOf(
              "displayName" to cleanName,
              "isTyping" to isTyping,
              "updatedAt" to FieldValue.serverTimestamp()
            )
          ).await()
        }.onFailure { err ->
          Log.w(TAG, "Typing indicator update failed: ${err.message}")
        }
      }
      return
    }

    val typingFlow = localTypingUsers.getOrPut(conversationId) { MutableStateFlow(emptyList()) }
    val current = typingFlow.value
    if (isTyping) {
      if (!current.contains(userName)) {
        typingFlow.value = current + userName
      }
    } else {
      if (current.contains(userName)) {
        typingFlow.value = current - userName
      }
    }
  }

  suspend fun editMessage(
    conversationId: String,
    messageId: String,
    newContent: String
  ): Result<Unit> {
    if (auth.currentUser != null && !isAiChatbotConversation(conversationId)) {
      val msgRef = db.collection("conversations")
        .document(conversationId)
        .collection("messages")
        .document(messageId)

      return try {
        msgRef.update(
          mapOf(
            "content" to newContent.take(4000),
            "isEdited" to true,
            "updatedAt" to FieldValue.serverTimestamp()
          )
        ).await()
        Result.success(Unit)
      } catch (e: Exception) {
        handleFirestoreError(e, OperationType.UPDATE, msgRef.path, auth)
        Result.failure(e)
      }
    }

    val msgFlow = localMessages[conversationId]
    if (msgFlow != null) {
      msgFlow.value = msgFlow.value.map { msg ->
        if (msg.id == messageId) msg.copy(content = newContent, isEdited = true) else msg
      }
    }
    return Result.success(Unit)
  }

  suspend fun deleteMessage(
    conversationId: String,
    messageId: String,
    deletedPlaceholder: String = "تم حذف هذه الرسالة"
  ): Result<Unit> {
    if (auth.currentUser != null && !isAiChatbotConversation(conversationId)) {
      val msgRef = db.collection("conversations")
        .document(conversationId)
        .collection("messages")
        .document(messageId)

      return try {
        msgRef.update(
          mapOf(
            "content" to deletedPlaceholder,
            "isDeleted" to true,
            "updatedAt" to FieldValue.serverTimestamp()
          )
        ).await()
        Result.success(Unit)
      } catch (e: Exception) {
        handleFirestoreError(e, OperationType.UPDATE, msgRef.path, auth)
        Result.failure(e)
      }
    }

    val msgFlow = localMessages[conversationId]
    if (msgFlow != null) {
      msgFlow.value = msgFlow.value.filterNot { it.id == messageId }
    }
    return Result.success(Unit)
  }

  suspend fun markAsRead(conversationId: String): Result<Unit> {
    val uid = getEffectiveUserId()
    if (auth.currentUser != null && !isAiChatbotConversation(conversationId)) {
      val messagesRef = db.collection("conversations")
        .document(conversationId)
        .collection("messages")
      return try {
        val snapshot = messagesRef.orderBy("createdAt", Query.Direction.DESCENDING).limit(25).get().await()
        val toMark = snapshot.toObjects(Message::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
          .filter { !it.readBy.contains(uid) && it.senderId != uid }
        if (toMark.isNotEmpty()) {
          val batch = db.batch()
          toMark.forEach { msg ->
            batch.update(
              messagesRef.document(msg.id),
              mapOf(
                "status" to "read",
                "readBy" to FieldValue.arrayUnion(uid),
                "updatedAt" to FieldValue.serverTimestamp()
              )
            )
          }
          batch.commit().await()
        }
        Result.success(Unit)
      } catch (e: Exception) {
        handleFirestoreError(e, OperationType.UPDATE, messagesRef.path, auth)
        Result.failure(e)
      }
    }

    val msgFlow = localMessages[conversationId]
    if (msgFlow != null) {
      msgFlow.value = msgFlow.value.map { msg ->
        if (!msg.readBy.contains(uid)) msg.copy(readBy = msg.readBy + uid, status = "read") else msg
      }
    }
    return Result.success(Unit)
  }

  suspend fun togglePinConversation(conversationId: String, currentPinned: Boolean): Result<Unit> {
    val uid = getEffectiveUserId()
    if (auth.currentUser != null && !isAiChatbotConversation(conversationId)) {
      val convoRef = db.collection("conversations").document(conversationId)
      return try {
        convoRef.update(
          mapOf(
            "pinnedBy" to if (currentPinned) FieldValue.arrayRemove(uid) else FieldValue.arrayUnion(uid),
            "updatedAt" to FieldValue.serverTimestamp()
          )
        ).await()
        Result.success(Unit)
      } catch (e: Exception) {
        handleFirestoreError(e, OperationType.UPDATE, convoRef.path, auth)
        Result.failure(e)
      }
    }

    localConversations.value = localConversations.value.map { convo ->
      if (convo.id == conversationId) {
        val newPinned = if (currentPinned) convo.pinnedBy - uid else convo.pinnedBy + uid
        convo.copy(pinnedBy = newPinned)
      } else convo
    }
    return Result.success(Unit)
  }

  suspend fun toggleArchiveConversation(conversationId: String, currentArchived: Boolean): Result<Unit> {
    val uid = getEffectiveUserId()
    if (auth.currentUser != null && !isAiChatbotConversation(conversationId)) {
      val convoRef = db.collection("conversations").document(conversationId)
      return try {
        convoRef.update(
          mapOf(
            "archivedBy" to if (currentArchived) FieldValue.arrayRemove(uid) else FieldValue.arrayUnion(uid),
            "updatedAt" to FieldValue.serverTimestamp()
          )
        ).await()
        Result.success(Unit)
      } catch (e: Exception) {
        handleFirestoreError(e, OperationType.UPDATE, convoRef.path, auth)
        Result.failure(e)
      }
    }

    localConversations.value = localConversations.value.map { convo ->
      if (convo.id == conversationId) {
        val newArchived = if (currentArchived) convo.archivedBy - uid else convo.archivedBy + uid
        convo.copy(archivedBy = newArchived)
      } else convo
    }
    return Result.success(Unit)
  }

  suspend fun toggleMuteConversation(conversationId: String, currentMuted: Boolean): Result<Unit> {
    val uid = getEffectiveUserId()
    if (auth.currentUser != null && !isAiChatbotConversation(conversationId)) {
      val convoRef = db.collection("conversations").document(conversationId)
      return try {
        convoRef.update(
          mapOf(
            "mutedBy" to if (currentMuted) FieldValue.arrayRemove(uid) else FieldValue.arrayUnion(uid),
            "updatedAt" to FieldValue.serverTimestamp()
          )
        ).await()
        Result.success(Unit)
      } catch (e: Exception) {
        handleFirestoreError(e, OperationType.UPDATE, convoRef.path, auth)
        Result.failure(e)
      }
    }

    localConversations.value = localConversations.value.map { convo ->
      if (convo.id == conversationId) {
        val newMuted = if (currentMuted) convo.mutedBy - uid else convo.mutedBy + uid
        convo.copy(mutedBy = newMuted)
      } else convo
    }
    return Result.success(Unit)
  }

  suspend fun toggleMessageReaction(
    conversationId: String,
    messageId: String,
    emoji: String
  ): Result<Unit> {
    val cleanEmoji = emoji.trim().take(16)
    if (cleanEmoji.isBlank()) return Result.success(Unit)
    val uid = getEffectiveUserId()

    if (auth.currentUser != null && !isAiChatbotConversation(conversationId)) {
      val msgRef = db.collection("conversations")
        .document(conversationId)
        .collection("messages")
        .document(messageId)
      return try {
        db.runTransaction { transaction ->
          val snap = transaction.get(msgRef)
          val currentMsg = snap.toObject(Message::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            ?: throw IllegalStateException("Message not found")
          if (currentMsg.isDeleted) return@runTransaction Unit

          val updated = currentMsg.reactions.mapValues { it.value.toMutableList() }.toMutableMap()
          val list = updated.getOrPut(cleanEmoji) { mutableListOf() }
          if (list.contains(uid)) {
            list.remove(uid)
            if (list.isEmpty()) updated.remove(cleanEmoji)
          } else {
            if (updated.size < 40 || updated.containsKey(cleanEmoji)) {
              list.add(uid)
            }
          }
          transaction.update(
            msgRef,
            mapOf(
              "reactions" to updated.mapValues { it.value.distinct().take(200) },
              "updatedAt" to FieldValue.serverTimestamp()
            )
          )
          Unit
        }.await()
        Result.success(Unit)
      } catch (e: Exception) {
        handleFirestoreError(e, OperationType.UPDATE, msgRef.path, auth)
        Result.failure(e)
      }
    }

    val msgFlow = localMessages[conversationId]
    if (msgFlow != null) {
      msgFlow.value = msgFlow.value.map { msg ->
        if (msg.id == messageId && !msg.isDeleted) {
          val updated = msg.reactions.mapValues { it.value.toMutableList() }.toMutableMap()
          val list = updated.getOrPut(cleanEmoji) { mutableListOf() }
          if (list.contains(uid)) {
            list.remove(uid)
            if (list.isEmpty()) updated.remove(cleanEmoji)
          } else {
            list.add(uid)
          }
          msg.copy(reactions = updated.mapValues { it.value.distinct() })
        } else msg
      }
    }
    return Result.success(Unit)
  }

  suspend fun togglePin(conversationId: String, currentPinned: Boolean): Result<Unit> =
    togglePinConversation(conversationId, currentPinned)

  suspend fun toggleArchive(conversationId: String, currentArchived: Boolean): Result<Unit> =
    toggleArchiveConversation(conversationId, currentArchived)

  suspend fun toggleMute(conversationId: String, currentMuted: Boolean): Result<Unit> =
    toggleMuteConversation(conversationId, currentMuted)
}
