package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class UserProfile(
  @DocumentId val id: String = "",
  val displayName: String = "",
  val handle: String = "",
  val bio: String = "",
  val profession: String = "",
  val isProfessionalVerified: Boolean = false,
  val professionalOrg: String = "",
  val openToNetworking: Boolean = true,
  val avatarUrl: String = "",
  val isVerified: Boolean = false,
  val verifiedTier: String = "GOLD",
  val quietMode: Boolean = false,
  val readReceiptsEnabled: Boolean = true,
  val lastSeen: Timestamp? = null,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null
) {
  fun toMap(): Map<String, Any?> = mapOf(
    "displayName" to displayName,
    "handle" to handle,
    "bio" to bio,
    "profession" to profession,
    "isProfessionalVerified" to isProfessionalVerified,
    "professionalOrg" to professionalOrg,
    "openToNetworking" to openToNetworking,
    "avatarUrl" to avatarUrl,
    "isVerified" to isVerified,
    "verifiedTier" to verifiedTier,
    "quietMode" to quietMode,
    "readReceiptsEnabled" to readReceiptsEnabled,
    "lastSeen" to lastSeen,
    "createdAt" to createdAt,
    "updatedAt" to updatedAt
  ).filterValues { it != null }
}
