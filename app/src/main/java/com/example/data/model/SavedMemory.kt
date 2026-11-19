package com.example.data.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@Keep
@IgnoreExtraProperties
data class SavedMemory(
  @DocumentId val id: String = "",
  val userId: String = "",
  val originalMessageId: String = "",
  val conversationId: String = "",
  val snippet: String = "",
  val senderName: String = "",
  val privateNote: String = "",
  @ServerTimestamp val createdAt: Timestamp? = null
) {
  fun toMap(): Map<String, Any?> = mapOf(
    "userId" to userId,
    "originalMessageId" to originalMessageId,
    "conversationId" to conversationId,
    "snippet" to snippet,
    "senderName" to senderName,
    "privateNote" to privateNote,
    "createdAt" to createdAt
  ).filterValues { it != null }
}
