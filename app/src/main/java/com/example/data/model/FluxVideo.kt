package com.example.data.model

import androidx.annotation.Keep
import com.google.firebase.firestore.PropertyName

@Keep
data class FluxVideo(
  val id: String = "",
  val authorId: String = "",
  val authorName: String = "",
  val authorHandle: String = "",
  val authorVerifiedTier: String = "BLUE",
  @get:PropertyName("isAuthorVerified") @PropertyName("isAuthorVerified")
  val isAuthorVerified: Boolean = false,
  val caption: String = "",
  val videoUri: String? = null,
  val mediaId: String? = null,
  val mediaMimeType: String? = null,
  val mediaSizeBytes: Long = 0L,
  val mediaChunkCount: Int = 0,
  val audioTitle: String = "Original Sound",
  val likesCount: Int = 0,
  val likedBy: List<String> = emptyList(),
  val commentsCount: Int = 0,
  val sharesCount: Int = 0,
  val savedBy: List<String> = emptyList(),
  val reactions: Map<String, List<String>> = emptyMap(),
  val tags: List<String> = emptyList(),
  val gradientTheme: String = "BLURPLE", // "BLURPLE", "NITRO", "EMERALD", "SOLAR", "CYBER"
  val createdAt: Long = System.currentTimeMillis()
) {
  fun hasVideoContent(): Boolean =
    (!mediaId.isNullOrBlank() && mediaChunkCount > 0) || !videoUri.isNullOrBlank()
}

@Keep
data class FluxComment(
  val id: String = "",
  val fluxId: String = "",
  val authorId: String = "",
  val authorName: String = "",
  val authorHandle: String = "",
  val authorVerifiedTier: String = "BLUE",
  @get:PropertyName("isAuthorVerified") @PropertyName("isAuthorVerified")
  val isAuthorVerified: Boolean = false,
  val content: String = "",
  val timestamp: Long = System.currentTimeMillis()
)
