package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class Post(
  @DocumentId val id: String = "",
  val authorId: String = "",
  val authorName: String = "",
  val authorHandle: String = "",
  val isAuthorVerified: Boolean = false,
  val authorVerifiedTier: String = "BLUE",
  val content: String = "",
  val audience: String = "public", // "public" or "circles"
  val likedBy: List<String> = emptyList(),
  val likesCount: Long = 0L,
  val commentsCount: Long = 0L,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null
) {
  fun toMap(): Map<String, Any?> = mapOf(
    "authorId" to authorId,
    "authorName" to authorName,
    "authorHandle" to authorHandle,
    "isAuthorVerified" to isAuthorVerified,
    "authorVerifiedTier" to authorVerifiedTier,
    "content" to content,
    "audience" to audience,
    "likedBy" to likedBy,
    "likesCount" to likesCount,
    "commentsCount" to commentsCount,
    "createdAt" to createdAt,
    "updatedAt" to updatedAt
  ).filterValues { it != null }
}
