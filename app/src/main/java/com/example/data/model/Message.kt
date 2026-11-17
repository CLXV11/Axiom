package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class Message(
  @DocumentId val id: String = "",
  val conversationId: String = "",
  val senderId: String = "",
  val senderName: String = "",
  val isSenderVerified: Boolean = false,
  val senderVerifiedTier: String = "BLUE",
  val content: String = "",
  val replyToId: String? = null,
  val replyToSender: String? = null,
  val replyToText: String? = null,
  val status: String = "sent", // "sent", "delivered", "read"
  val readBy: List<String> = emptyList(),
  val isEdited: Boolean = false,
  val isDeleted: Boolean = false,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null
) {
  fun toMap(): Map<String, Any?> = mapOf(
    "conversationId" to conversationId,
    "senderId" to senderId,
    "senderName" to senderName,
    "isSenderVerified" to isSenderVerified,
    "senderVerifiedTier" to senderVerifiedTier,
    "content" to content,
    "replyToId" to replyToId,
    "replyToSender" to replyToSender,
    "replyToText" to replyToText,
    "status" to status,
    "readBy" to readBy,
    "isEdited" to isEdited,
    "isDeleted" to isDeleted,
    "createdAt" to createdAt,
    "updatedAt" to updatedAt
  ).filterValues { it != null }
}
