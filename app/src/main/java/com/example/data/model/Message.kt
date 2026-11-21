package com.example.data.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp

@Keep
@IgnoreExtraProperties
data class Message(
  @DocumentId val id: String = "",
  val conversationId: String = "",
  val senderId: String = "",
  val senderName: String = "",
  @get:PropertyName("isSenderVerified") @PropertyName("isSenderVerified")
  val isSenderVerified: Boolean = false,
  val senderVerifiedTier: String = "BLUE",
  val content: String = "",
  val mediaType: String = "text", // "text", "image", "video", "file"
  val mediaId: String? = null,
  val mediaFileName: String? = null,
  val mediaMimeType: String? = null,
  val mediaSizeBytes: Long = 0L,
  val mediaChunkCount: Int = 0,
  val mediaDurationMs: Long = 0L,
  val localMediaUri: String? = null,
  val replyToId: String? = null,
  val replyToSender: String? = null,
  val replyToText: String? = null,
  val status: String = "sent", // "sent", "delivered", "read"
  val readBy: List<String> = emptyList(),
  val reactions: Map<String, List<String>> = emptyMap(),
  @get:PropertyName("isEdited") @PropertyName("isEdited")
  val isEdited: Boolean = false,
  @get:PropertyName("isDeleted") @PropertyName("isDeleted")
  val isDeleted: Boolean = false,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null
) {
  fun hasMediaAttachment(): Boolean =
    mediaType in listOf("image", "video", "file") && !mediaId.isNullOrBlank()

  fun toMap(): Map<String, Any?> = mapOf(
    "conversationId" to conversationId,
    "senderId" to senderId,
    "senderName" to senderName,
    "isSenderVerified" to isSenderVerified,
    "senderVerifiedTier" to senderVerifiedTier,
    "content" to content,
    "mediaType" to mediaType,
    "mediaId" to mediaId,
    "mediaFileName" to mediaFileName,
    "mediaMimeType" to mediaMimeType,
    "mediaSizeBytes" to mediaSizeBytes,
    "mediaChunkCount" to mediaChunkCount,
    "mediaDurationMs" to mediaDurationMs,
    "replyToId" to replyToId,
    "replyToSender" to replyToSender,
    "replyToText" to replyToText,
    "status" to status,
    "readBy" to readBy,
    "reactions" to reactions,
    "isEdited" to isEdited,
    "isDeleted" to isDeleted,
    "createdAt" to createdAt,
    "updatedAt" to updatedAt
  ).filterValues { it != null }
}
