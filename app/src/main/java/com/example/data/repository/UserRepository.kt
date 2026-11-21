package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.UserProfile
import com.example.data.util.OperationType
import com.example.data.util.handleFirestoreError
import com.example.ui.util.PreferencesManager
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.tasks.await

class UserRepository(
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

  private val localUserProfileFlow = MutableStateFlow<UserProfile?>(null)
  private var cachedFirestoreDirectoryUsers: List<UserProfile> = emptyList()

  init {
    initLocalProfile()
  }

  private fun initLocalProfile() {
    val uid = getEffectiveUserId()
    val name = prefsManager?.guestDisplayName ?: "Kora Member"
    val handle = prefsManager?.guestHandle ?: "@kora_member"
    val bio = prefsManager?.guestBio ?: ""
    val quiet = prefsManager?.guestQuietMode ?: false
    val readReceipts = prefsManager?.guestReadReceipts ?: true
    val isVerified = prefsManager?.guestIsVerified ?: false
    val verifiedTier = prefsManager?.guestVerifiedTier ?: "BLUE"
    val avatar = prefsManager?.guestAvatarUrl ?: ""

    localUserProfileFlow.value = UserProfile(
      id = uid,
      displayName = name,
      handle = handle,
      bio = bio,
      avatarUrl = avatar,
      isVerified = isVerified,
      verifiedTier = verifiedTier,
      quietMode = quiet,
      readReceiptsEnabled = readReceipts
    )
  }

  companion object {
    val VALID_TIERS = listOf("BLUE", "GOLD", "EMERALD", "ROSE_GOLD", "AMETHYST", "ONYX", "PLATINUM")
  }

  fun isSignedIn(): Boolean = auth.currentUser != null

  fun getEffectiveUserId(): String {
    return auth.currentUser?.uid ?: prefsManager?.getOrCreateGuestUserId() ?: "kora_lead_user"
  }

  fun requireUserId(): String {
    return getEffectiveUserId()
  }

  fun getCachedProfile(userId: String): UserProfile? = prefsManager?.getCachedProfile(userId)

  fun observeUserProfile(userId: String): Flow<UserProfile?> {
    if (auth.currentUser != null) {
      val isCurrentUser = userId == auth.currentUser?.uid
      val docRef = db.collection("users").document(userId)
      return docRef.snapshots()
        .map { snapshot ->
          if (snapshot.exists()) {
            snapshot.toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)?.also {
              if (isCurrentUser) {
                localUserProfileFlow.value = it
                prefsManager?.saveCachedProfile(it)
              }
            }
          } else {
            null
          }
        }
        .onStart {
          prefsManager?.getCachedProfile(userId)?.let { emit(it) }
        }
        .catch { e ->
          handleFirestoreError(e, OperationType.GET, docRef.path, auth)
          emit(prefsManager?.getCachedProfile(userId) ?: if (isCurrentUser) localUserProfileFlow.value else null)
        }
    } else {
      return if (userId == getEffectiveUserId()) localUserProfileFlow.asStateFlow() else flowOf(null)
    }
  }

  suspend fun getUserProfile(userId: String): UserProfile? {
    if (auth.currentUser != null) {
      val isCurrentUser = userId == auth.currentUser?.uid
      val docRef = db.collection("users").document(userId)
      return try {
        val snapshot = docRef.get().await()
        if (snapshot.exists()) {
          snapshot.toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)?.also {
            if (isCurrentUser) {
              localUserProfileFlow.value = it
              prefsManager?.saveCachedProfile(it)
            }
          }
        } else {
          null
        }
      } catch (e: Exception) {
        handleFirestoreError(e, OperationType.GET, docRef.path, auth)
        throw e
      }
    }
    return if (userId == getEffectiveUserId()) localUserProfileFlow.value else null
  }

  suspend fun createOrUpdateProfile(profile: UserProfile): Result<Unit> {
    val uid = requireUserId()
    val cleanHandle = profile.handle.trim().ifBlank { "user_${uid.take(6)}" }
      .let { if (it.startsWith("@")) it else "@$it" }
      .take(40)
    val cleanName = profile.displayName.trim().ifBlank { "Kora Member" }.take(60)
    val validTier = if (profile.verifiedTier in VALID_TIERS) {
      profile.verifiedTier
    } else {
      "BLUE"
    }

    val normalizedProfile = profile.copy(
      id = uid,
      displayName = cleanName,
      handle = cleanHandle,
      bio = profile.bio.trim().take(300),
      profession = profile.profession.trim().take(80),
      professionalOrg = profile.professionalOrg.trim().take(100),
      verifiedTier = validTier
    )

    if (auth.currentUser != null) {
      val docRef = db.collection("users").document(uid)
      return try {
        val existing = docRef.get().await()
        if (!existing.exists()) {
          val payload = hashMapOf<String, Any?>(
            "displayName" to normalizedProfile.displayName,
            "handle" to normalizedProfile.handle,
            "bio" to normalizedProfile.bio,
            "profession" to normalizedProfile.profession,
            "isProfessionalVerified" to false,
            "professionalOrg" to normalizedProfile.professionalOrg,
            "openToNetworking" to normalizedProfile.openToNetworking,
            "avatarUrl" to normalizedProfile.avatarUrl,
            "isVerified" to normalizedProfile.isVerified,
            "verifiedTier" to normalizedProfile.verifiedTier,
            "equippedFrame" to normalizedProfile.equippedFrame,
            "equippedNameplate" to normalizedProfile.equippedNameplate,
            "equippedWall" to normalizedProfile.equippedWall,
            "quietMode" to normalizedProfile.quietMode,
            "readReceiptsEnabled" to normalizedProfile.readReceiptsEnabled,
            "lastSeen" to FieldValue.serverTimestamp(),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
          )
          docRef.set(payload).await()
        } else {
          val payload = hashMapOf<String, Any?>(
            "displayName" to normalizedProfile.displayName,
            "handle" to normalizedProfile.handle,
            "bio" to normalizedProfile.bio,
            "profession" to normalizedProfile.profession,
            "professionalOrg" to normalizedProfile.professionalOrg,
            "openToNetworking" to normalizedProfile.openToNetworking,
            "avatarUrl" to normalizedProfile.avatarUrl,
            "isVerified" to normalizedProfile.isVerified,
            "verifiedTier" to normalizedProfile.verifiedTier,
            "equippedFrame" to normalizedProfile.equippedFrame,
            "equippedNameplate" to normalizedProfile.equippedNameplate,
            "equippedWall" to normalizedProfile.equippedWall,
            "quietMode" to normalizedProfile.quietMode,
            "readReceiptsEnabled" to normalizedProfile.readReceiptsEnabled,
            "lastSeen" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
          )
          docRef.update(payload).await()
        }
        localUserProfileFlow.value = normalizedProfile
        prefsManager?.saveCachedProfile(normalizedProfile)
        Result.success(Unit)
      } catch (e: Exception) {
        handleFirestoreError(e, OperationType.WRITE, docRef.path, auth)
        Result.failure(e)
      }
    }

    localUserProfileFlow.value = normalizedProfile
    prefsManager?.apply {
      guestDisplayName = normalizedProfile.displayName
      guestHandle = normalizedProfile.handle
      guestBio = normalizedProfile.bio
      guestQuietMode = normalizedProfile.quietMode
      guestReadReceipts = normalizedProfile.readReceiptsEnabled
      guestIsVerified = normalizedProfile.isVerified
      guestVerifiedTier = normalizedProfile.verifiedTier
      guestAvatarUrl = normalizedProfile.avatarUrl
    }
    return Result.success(Unit)
  }

  private suspend fun loadCurrentOrFallbackProfile(uid: String): UserProfile {
    return if (auth.currentUser != null) {
      runCatching { getUserProfile(uid) }.getOrNull()
        ?: prefsManager?.getCachedProfile(uid)
        ?: localUserProfileFlow.value
        ?: UserProfile(id = uid)
    } else {
      localUserProfileFlow.value ?: UserProfile(id = uid)
    }
  }

  suspend fun updateAvatarUrl(avatarUrl: String): Result<Unit> {
    val uid = requireUserId()
    val current = loadCurrentOrFallbackProfile(uid)
    return createOrUpdateProfile(current.copy(avatarUrl = avatarUrl))
  }

  suspend fun setVerificationStatus(enabled: Boolean): Result<Boolean> {
    val uid = requireUserId()
    val current = loadCurrentOrFallbackProfile(uid)
    val resolvedTier = if (current.verifiedTier in VALID_TIERS) current.verifiedTier else "BLUE"
    val updated = current.copy(isVerified = enabled, verifiedTier = resolvedTier)
    val res = createOrUpdateProfile(updated)
    return if (res.isSuccess) Result.success(enabled) else Result.failure(res.exceptionOrNull()!!)
  }

  suspend fun toggleVerificationStatus(): Result<Boolean> {
    val uid = requireUserId()
    val current = loadCurrentOrFallbackProfile(uid)
    return setVerificationStatus(!current.isVerified)
  }

  suspend fun setVerificationTier(tier: String): Result<Unit> {
    val uid = requireUserId()
    val current = loadCurrentOrFallbackProfile(uid)
    val validTier = if (tier in VALID_TIERS) tier else "BLUE"
    val updated = current.copy(verifiedTier = validTier, isVerified = true)
    return createOrUpdateProfile(updated)
  }

  suspend fun setQuietMode(quiet: Boolean): Result<Unit> {
    val uid = requireUserId()
    val current = loadCurrentOrFallbackProfile(uid)
    return createOrUpdateProfile(current.copy(quietMode = quiet))
  }

  suspend fun updateLastSeen() {
    val uid = auth.currentUser?.uid ?: return
    val docRef = db.collection("users").document(uid)
    try {
      docRef.update("lastSeen", FieldValue.serverTimestamp()).await()
    } catch (_: Exception) {
      // Quiet fail when profile doc does not exist yet
    }
  }

  fun getDirectoryUsers(): List<UserProfile> {
    return cachedFirestoreDirectoryUsers
  }

  fun observeDirectoryUsers(): Flow<List<UserProfile>> {
    if (auth.currentUser == null) return flowOf(emptyList())
    return db.collection("users")
      .limit(200)
      .snapshots()
      .map { snapshot ->
        val list = snapshot.toObjects(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
        cachedFirestoreDirectoryUsers = list
        list
      }
      .catch { e ->
        handleFirestoreError(e, OperationType.LIST, "users", auth)
        emit(cachedFirestoreDirectoryUsers)
      }
  }

  suspend fun fetchDirectoryUsers(): List<UserProfile> {
    if (auth.currentUser != null) {
      try {
        val snapshot = db.collection("users")
          .limit(200)
          .get()
          .await()
        cachedFirestoreDirectoryUsers = snapshot.toObjects(
          UserProfile::class.java,
          DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
        )
      } catch (e: Exception) {
        handleFirestoreError(e, OperationType.LIST, "users", auth)
      }
    }
    return cachedFirestoreDirectoryUsers
  }

  fun filterDirectoryUsers(
    users: List<UserProfile>,
    query: String = "",
    professionCategory: String? = null,
    verifiedProOnly: Boolean = false
  ): List<UserProfile> {
    val clean = query.trim().lowercase().removePrefix("@")
    val cat = professionCategory?.trim()?.lowercase()

    return users.filter { user ->
      val matchesProOnly = if (verifiedProOnly) user.isProfessionalVerified else true

      val matchesCategory = when {
        cat.isNullOrBlank() || cat == "all" || cat == "الكل" -> true
        cat == "verified_pro" -> user.isProfessionalVerified
        else -> user.profession.lowercase().contains(cat) || user.bio.lowercase().contains(cat)
      }

      val matchesQuery = if (clean.isBlank()) {
        true
      } else {
        user.displayName.lowercase().contains(clean) ||
          user.handle.lowercase().contains(clean) ||
          user.profession.lowercase().contains(clean) ||
          user.professionalOrg.lowercase().contains(clean) ||
          user.bio.lowercase().contains(clean)
      }

      matchesProOnly && matchesCategory && matchesQuery
    }
  }

  suspend fun searchDirectoryUsers(
    query: String = "",
    professionCategory: String? = null,
    verifiedProOnly: Boolean = false
  ): List<UserProfile> {
    val all = fetchDirectoryUsers()
    return filterDirectoryUsers(
      users = all,
      query = query,
      professionCategory = professionCategory,
      verifiedProOnly = verifiedProOnly
    )
  }

  suspend fun searchUsers(query: String): List<UserProfile> {
    return searchDirectoryUsers(query = query)
  }
}
