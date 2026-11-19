package com.example.data.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp

@Keep
@IgnoreExtraProperties
data class PostComment(
  @DocumentId val id: String = "",
  val postId: String = "",
  val authorId: String = "",
  val authorName: String = "",
  val authorHandle: String = "",
  @get:PropertyName("isAuthorVerified") @PropertyName("isAuthorVerified")
  val isAuthorVerified: Boolean = false,
  val content: String = "",
  @ServerTimestamp val createdAt: Timestamp? = null
) {
  fun toMap(): Map<String, Any?> = mapOf(
    "postId" to postId,
    "authorId" to authorId,
    "authorName" to authorName,
    "authorHandle" to authorHandle,
    "isAuthorVerified" to isAuthorVerified,
    "content" to content,
    "createdAt" to createdAt
  ).filterValues { it != null }
}
