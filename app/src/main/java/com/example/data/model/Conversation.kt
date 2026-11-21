package com.example.data.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp

@Keep
@IgnoreExtraProperties
data class Conversation(
  @DocumentId val id: String = "",
  val type: String = "direct", // "direct" or "group"
  val title: String = "",
  @get:PropertyName("isVerified") @PropertyName("isVerified")
  val isVerified: Boolean = false,
  val verifiedTier: String = "BLUE",
  val participantIds: List<String> = emptyList(),
  val lastMessageText: String = "",
  val lastMessageSenderId: String = "",
  val lastMessageTimestamp: Timestamp? = null,
  val pinnedBy: List<String> = emptyList(),
  val archivedBy: List<String> = emptyList(),
  val mutedBy: List<String> = emptyList(),
  val pinnedMessageId: String? = null,
  val pinnedMessageText: String? = null,
  val pinnedMessageSender: String? = null,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null
) {
  fun toMap(): Map<String, Any?> = mapOf(
    "type" to type,
    "title" to title,
    "isVerified" to isVerified,
    "verifiedTier" to verifiedTier,
    "participantIds" to participantIds,
    "lastMessageText" to lastMessageText,
    "lastMessageSenderId" to lastMessageSenderId,
    "lastMessageTimestamp" to lastMessageTimestamp,
    "pinnedBy" to pinnedBy,
    "archivedBy" to archivedBy,
    "mutedBy" to mutedBy,
    "pinnedMessageId" to pinnedMessageId,
    "pinnedMessageText" to pinnedMessageText,
    "pinnedMessageSender" to pinnedMessageSender,
    "createdAt" to createdAt,
    "updatedAt" to updatedAt
  ).filterValues { it != null }
}
