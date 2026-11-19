package com.example.data.repository

import android.content.ContentValues
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.example.R
import com.example.data.model.FluxComment
import com.example.data.model.FluxVideo
import com.example.data.util.OperationType
import com.example.data.util.handleFirestoreError
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.UUID

class FluxRepository(
  private val db: FirebaseFirestore?,
  private val prefs: SharedPreferences?,
  private val auth: FirebaseAuth? = null,
  private val appContext: Context? = null
) {
  constructor(context: Context) : this(
    resolveFirestore(context),
    context.applicationContext.getSharedPreferences("flux_storage_prefs", Context.MODE_PRIVATE),
    resolveAuth(),
    context.applicationContext
  )

  private companion object {
    val VALID_TIERS = listOf("BLUE", "GOLD", "EMERALD", "ROSE_GOLD", "AMETHYST", "ONYX", "PLATINUM")

    fun resolveFirestore(context: Context): FirebaseFirestore? = try {
      FirebaseFirestore.getInstance(
        context.applicationContext.getString(R.string.firestore_database_id)
      )
    } catch (_: Exception) {
      null
    }

    fun resolveAuth(): FirebaseAuth? = try {
      Firebase.auth
    } catch (_: Exception) {
      null
    }
  }

  private val _videos = MutableStateFlow<List<FluxVideo>>(emptyList())
  val videos: Flow<List<FluxVideo>> = _videos.asStateFlow()

  private val _comments = MutableStateFlow<Map<String, List<FluxComment>>>(emptyMap())

  init {
    loadPersistedVideos()
  }

  private fun isSignedIn(): Boolean = auth?.currentUser != null && db != null

  private fun DocumentSnapshot.toFluxVideoOrNull(): FluxVideo? {
    if (!exists()) return null
    val authorId = getString("authorId") ?: return null
    @Suppress("UNCHECKED_CAST")
    val likedBy = (get("likedBy") as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
    @Suppress("UNCHECKED_CAST")
    val savedBy = (get("savedBy") as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
    @Suppress("UNCHECKED_CAST")
    val tags = (get("tags") as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
    val ts = getTimestamp("createdAt", DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)?.toDate()?.time
      ?: System.currentTimeMillis()
    return FluxVideo(
      id = id,
      authorId = authorId,
      authorName = getString("authorName") ?: "Creator",
      authorHandle = getString("authorHandle") ?: "@creator",
      authorVerifiedTier = getString("authorVerifiedTier") ?: "BLUE",
      isAuthorVerified = getBoolean("isAuthorVerified") ?: false,
      caption = getString("caption") ?: "",
      videoUri = getString("videoUri"),
      audioTitle = getString("audioTitle") ?: "Original Sound",
      likesCount = (getLong("likesCount") ?: 0L).toInt(),
      likedBy = likedBy,
      commentsCount = (getLong("commentsCount") ?: 0L).toInt(),
      sharesCount = (getLong("sharesCount") ?: 0L).toInt(),
      savedBy = savedBy,
      tags = tags,
      gradientTheme = getString("gradientTheme") ?: "BLURPLE",
      createdAt = ts
    )
  }

  private fun DocumentSnapshot.toFluxCommentOrNull(): FluxComment? {
    if (!exists()) return null
    val authorId = getString("authorId") ?: return null
    val ts = getTimestamp("createdAt", DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)?.toDate()?.time
      ?: System.currentTimeMillis()
    return FluxComment(
      id = id,
      fluxId = getString("fluxId") ?: "",
      authorId = authorId,
      authorName = getString("authorName") ?: "User",
      authorHandle = getString("authorHandle") ?: "@user",
      authorVerifiedTier = getString("authorVerifiedTier") ?: "BLUE",
      isAuthorVerified = getBoolean("isAuthorVerified") ?: false,
      content = getString("content") ?: "",
      timestamp = ts
    )
  }

  fun observeVideos(): Flow<List<FluxVideo>> {
    if (isSignedIn()) {
      return db!!.collection("flux_videos")
        .orderBy("createdAt", Query.Direction.DESCENDING)
        .limit(50)
        .snapshots()
        .map { snapshot ->
          snapshot.documents.mapNotNull { it.toFluxVideoOrNull() }.also { serverList ->
            _videos.value = serverList
            saveVideosToPrefs(serverList)
          }
        }
        .catch { e ->
          handleFirestoreError(e, OperationType.LIST, "flux_videos", auth)
          emit(_videos.value)
        }
    }
    return videos
  }

  fun observeComments(fluxId: String): Flow<List<FluxComment>> {
    if (isSignedIn()) {
      return db!!.collection("flux_videos")
        .document(fluxId)
        .collection("comments")
        .orderBy("createdAt", Query.Direction.ASCENDING)
        .snapshots()
        .map { snapshot ->
          snapshot.documents.mapNotNull { it.toFluxCommentOrNull() }.also { list ->
            _comments.value = _comments.value + (fluxId to list)
          }
        }
        .catch { e ->
          handleFirestoreError(e, OperationType.LIST, "flux_videos/$fluxId/comments", auth)
          emit(_comments.value[fluxId] ?: emptyList())
        }
    }
    return kotlinx.coroutines.flow.flow {
      _comments.collect { map ->
        emit(map[fluxId] ?: emptyList())
      }
    }
  }

  private suspend fun persistPickedVideoLocally(fluxId: String, videoUri: String?): String? {
    if (videoUri.isNullOrBlank()) return null
    val context = appContext ?: return videoUri
    if (!videoUri.startsWith("content://")) return videoUri
    return withContext(Dispatchers.IO) {
      try {
        val parsed = Uri.parse(videoUri)
        val inputStream = context.contentResolver.openInputStream(parsed) ?: return@withContext videoUri
        val dir = File(context.filesDir, "flux_media").apply { mkdirs() }
        val destFile = File(dir, "$fluxId.mp4")
        var bytesWritten = 0L
        inputStream.use { input ->
          FileOutputStream(destFile).use { output ->
            bytesWritten = input.copyTo(output)
          }
        }
        if (bytesWritten > 0L) Uri.fromFile(destFile).toString() else {
          destFile.delete()
          videoUri
        }
      } catch (_: Exception) {
        videoUri
      }
    }
  }

  suspend fun publishFlux(
    authorId: String,
    authorName: String,
    authorHandle: String,
    isAuthorVerified: Boolean,
    authorVerifiedTier: String,
    caption: String,
    videoUri: String?,
    audioTitle: String,
    gradientTheme: String = "BLURPLE"
  ): FluxVideo {
    val fluxId = "flux_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}"
    val persistedUri = persistPickedVideoLocally(fluxId, videoUri)
    val extractedTags = Regex("#\\w+").findAll(caption).map { it.value }.take(15).toList()
    val tags = extractedTags.ifEmpty { listOf("#flux") }
    val cleanAudio = audioTitle.ifBlank { "Original Audio - $authorName" }.take(120)
    val validTier = if (authorVerifiedTier in VALID_TIERS) authorVerifiedTier else "BLUE"
    val validTheme = if (gradientTheme in listOf("BLURPLE", "NITRO", "EMERALD", "SOLAR", "CYBER")) {
      gradientTheme
    } else "BLURPLE"

    val newVideo = FluxVideo(
      id = fluxId,
      authorId = authorId,
      authorName = authorName,
      authorHandle = authorHandle,
      authorVerifiedTier = validTier,
      isAuthorVerified = isAuthorVerified,
      caption = caption.trim().ifBlank { "#flux" },
      videoUri = persistedUri,
      audioTitle = cleanAudio,
      likesCount = 0,
      likedBy = emptyList(),
      commentsCount = 0,
      sharesCount = 0,
      savedBy = emptyList(),
      tags = tags,
      gradientTheme = validTheme,
      createdAt = System.currentTimeMillis()
    )

    val previousList = _videos.value
    val updatedList = listOf(newVideo) + previousList
    _videos.value = updatedList
    saveVideosToPrefs(updatedList)

    if (isSignedIn()) {
      val currentUid = auth!!.currentUser!!.uid
      val fluxRef = db!!.collection("flux_videos").document(fluxId)
      val payload = buildMap<String, Any> {
        put("authorId", currentUid)
        put("authorName", authorName.ifBlank { "Creator" }.take(100))
        put("authorHandle", authorHandle.ifBlank { "@creator" }.take(50))
        put("isAuthorVerified", isAuthorVerified)
        put("authorVerifiedTier", validTier)
        put("caption", newVideo.caption.take(2000))
        if (!persistedUri.isNullOrBlank()) {
          put("videoUri", persistedUri.take(1000))
        }
        put("audioTitle", cleanAudio)
        put("likesCount", 0)
        put("likedBy", emptyList<String>())
        put("commentsCount", 0)
        put("sharesCount", 0)
        put("savedBy", emptyList<String>())
        put("tags", tags)
        put("gradientTheme", validTheme)
        put("createdAt", FieldValue.serverTimestamp())
      }
      try {
        fluxRef.set(payload).await()
      } catch (e: Exception) {
        _videos.value = previousList
        saveVideosToPrefs(previousList)
        handleFirestoreError(e, OperationType.CREATE, fluxRef.path, auth)
        throw e
      }
    }

    return newVideo
  }

  suspend fun toggleLike(fluxId: String, userId: String): Result<Boolean> {
    val previousList = _videos.value
    val target = previousList.find { it.id == fluxId }
    val optimisticNowLiked = target?.let { !it.likedBy.contains(userId) } ?: true

    val updated = previousList.map { video ->
      if (video.id == fluxId) {
        val alreadyLiked = video.likedBy.contains(userId)
        val newLikedBy = if (alreadyLiked) video.likedBy - userId else video.likedBy + userId
        val newCount = (video.likesCount + if (alreadyLiked) -1 else 1).coerceAtLeast(0)
        video.copy(likedBy = newLikedBy, likesCount = newCount)
      } else video
    }
    _videos.value = updated
    saveVideosToPrefs(updated)

    if (isSignedIn()) {
      val uid = auth!!.currentUser!!.uid
      val fluxRef = db!!.collection("flux_videos").document(fluxId)
      return try {
        val serverNowLiked = db.runTransaction { transaction ->
          val snap = transaction.get(fluxRef)
          val existing = snap.toFluxVideoOrNull()
            ?: throw IllegalStateException("Flux video not found")
          val alreadyLiked = existing.likedBy.contains(uid)
          val newLikedBy = if (alreadyLiked) existing.likedBy - uid else existing.likedBy + uid
          transaction.update(
            fluxRef,
            mapOf(
              "likedBy" to newLikedBy,
              "likesCount" to newLikedBy.size
            )
          )
          !alreadyLiked
        }.await()
        Result.success(serverNowLiked)
      } catch (e: Exception) {
        _videos.value = previousList
        saveVideosToPrefs(previousList)
        handleFirestoreError(e, OperationType.UPDATE, fluxRef.path, auth)
        Result.failure(e)
      }
    }

    return Result.success(optimisticNowLiked)
  }

  suspend fun toggleSave(fluxId: String, userId: String): Result<Boolean> {
    val previousList = _videos.value
    val target = previousList.find { it.id == fluxId }
    val optimisticNowSaved = target?.let { !it.savedBy.contains(userId) } ?: true

    val updated = previousList.map { video ->
      if (video.id == fluxId) {
        val alreadySaved = video.savedBy.contains(userId)
        val newSavedBy = if (alreadySaved) video.savedBy - userId else video.savedBy + userId
        video.copy(savedBy = newSavedBy)
      } else video
    }
    _videos.value = updated
    saveVideosToPrefs(updated)

    if (isSignedIn()) {
      val uid = auth!!.currentUser!!.uid
      val fluxRef = db!!.collection("flux_videos").document(fluxId)
      return try {
        val serverNowSaved = db.runTransaction { transaction ->
          val snap = transaction.get(fluxRef)
          val existing = snap.toFluxVideoOrNull()
            ?: throw IllegalStateException("Flux video not found")
          val alreadySaved = existing.savedBy.contains(uid)
          val newSavedBy = if (alreadySaved) existing.savedBy - uid else existing.savedBy + uid
          transaction.update(fluxRef, "savedBy", newSavedBy)
          !alreadySaved
        }.await()
        Result.success(serverNowSaved)
      } catch (e: Exception) {
        _videos.value = previousList
        saveVideosToPrefs(previousList)
        handleFirestoreError(e, OperationType.UPDATE, fluxRef.path, auth)
        Result.failure(e)
      }
    }

    return Result.success(optimisticNowSaved)
  }

  suspend fun saveVideoToGallery(context: Context, video: FluxVideo): Result<Unit> = withContext(Dispatchers.IO) {
    val rawUri = video.videoUri
    if (rawUri.isNullOrBlank()) {
      return@withContext Result.failure(IllegalStateException("No downloadable video file attached"))
    }
    val parsed = Uri.parse(rawUri)
    val resolver = context.applicationContext.contentResolver

    fun openSourceStream(): java.io.InputStream? {
      return when (parsed.scheme) {
        "file" -> {
          val filePath = parsed.path ?: return null
          val file = File(filePath)
          if (file.exists() && file.canRead()) FileInputStream(file) else null
        }
        else -> resolver.openInputStream(parsed)
      }
    }

    val sourceStream = openSourceStream()
      ?: return@withContext Result.failure(IllegalStateException("Video file unavailable on this device"))

    val fileName = "Kora_Flux_${video.id}.mp4"
    return@withContext try {
      val values = ContentValues().apply {
        put(MediaStore.Video.Media.DISPLAY_NAME, fileName)
        put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
          put(MediaStore.Video.Media.RELATIVE_PATH, "${Environment.DIRECTORY_MOVIES}/Kora")
          put(MediaStore.Video.Media.IS_PENDING, 1)
        }
      }
      val collectionUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
      } else {
        MediaStore.Video.Media.EXTERNAL_CONTENT_URI
      }
      val itemUri = resolver.insert(collectionUri, values)
        ?: return@withContext Result.failure(IllegalStateException("Could not create MediaStore video entry"))

      try {
        sourceStream.use { input ->
          resolver.openOutputStream(itemUri)?.use { output ->
            input.copyTo(output)
          } ?: throw IllegalStateException("Could not open MediaStore output stream")
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
          val doneValues = ContentValues().apply {
            put(MediaStore.Video.Media.IS_PENDING, 0)
          }
          resolver.update(itemUri, doneValues, null, null)
        }
        Result.success(Unit)
      } catch (e: Exception) {
        runCatching { resolver.delete(itemUri, null, null) }
        Result.failure(e)
      }
    } catch (e: Exception) {
      runCatching { sourceStream.close() }
      Result.failure(e)
    }
  }

  suspend fun incrementShare(fluxId: String): Result<Unit> {
    val current = _videos.value
    val updated = current.map { video ->
      if (video.id == fluxId) video.copy(sharesCount = video.sharesCount + 1) else video
    }
    _videos.value = updated
    saveVideosToPrefs(updated)

    if (isSignedIn()) {
      val fluxRef = db!!.collection("flux_videos").document(fluxId)
      return try {
        fluxRef.update("sharesCount", FieldValue.increment(1L)).await()
        Result.success(Unit)
      } catch (e: Exception) {
        _videos.value = current
        saveVideosToPrefs(current)
        handleFirestoreError(e, OperationType.UPDATE, fluxRef.path, auth)
        Result.failure(e)
      }
    }
    return Result.success(Unit)
  }

  suspend fun addComment(
    fluxId: String,
    authorId: String,
    authorName: String,
    authorHandle: String,
    isAuthorVerified: Boolean,
    authorVerifiedTier: String,
    content: String
  ): FluxComment {
    val commentId = UUID.randomUUID().toString()
    val validTier = if (authorVerifiedTier in VALID_TIERS) authorVerifiedTier else "BLUE"

    val newComment = FluxComment(
      id = commentId,
      fluxId = fluxId,
      authorId = authorId,
      authorName = authorName,
      authorHandle = authorHandle,
      authorVerifiedTier = validTier,
      isAuthorVerified = isAuthorVerified,
      content = content.trim(),
      timestamp = System.currentTimeMillis()
    )

    val previousCommentsMap = _comments.value
    val currentComments = previousCommentsMap[fluxId] ?: emptyList()
    _comments.value = previousCommentsMap + (fluxId to (currentComments + newComment))

    val previousVideos = _videos.value
    val updatedVideos = previousVideos.map { v ->
      if (v.id == fluxId) v.copy(commentsCount = v.commentsCount + 1) else v
    }
    _videos.value = updatedVideos
    saveVideosToPrefs(updatedVideos)

    if (isSignedIn()) {
      val uid = auth!!.currentUser!!.uid
      val fluxRef = db!!.collection("flux_videos").document(fluxId)
      val commentRef = fluxRef.collection("comments").document(commentId)
      val payload = mapOf(
        "fluxId" to fluxId,
        "authorId" to uid,
        "authorName" to authorName.ifBlank { "Member" }.take(100),
        "authorHandle" to authorHandle.ifBlank { "@member" }.take(50),
        "isAuthorVerified" to isAuthorVerified,
        "authorVerifiedTier" to validTier,
        "content" to content.trim().take(2000),
        "createdAt" to FieldValue.serverTimestamp()
      )
      try {
        val batch = db.batch()
        batch.set(commentRef, payload)
        batch.update(fluxRef, "commentsCount", FieldValue.increment(1L))
        batch.commit().await()
      } catch (e: Exception) {
        _comments.value = previousCommentsMap
        _videos.value = previousVideos
        saveVideosToPrefs(previousVideos)
        handleFirestoreError(e, OperationType.CREATE, commentRef.path, auth)
        throw e
      }
    }

    return newComment
  }

  private fun loadPersistedVideos() {
    val rawJson = prefs?.getString("saved_flux_videos", null)
    if (!rawJson.isNullOrBlank()) {
      try {
        val array = JSONArray(rawJson)
        val list = mutableListOf<FluxVideo>()
        for (i in 0 until array.length()) {
          val obj = array.getJSONObject(i)
          val id = obj.getString("id")
          if (id.startsWith("flux_seed_")) continue

          val likedByArray = obj.optJSONArray("likedBy")
          val likedByList = mutableListOf<String>()
          if (likedByArray != null) {
            for (j in 0 until likedByArray.length()) likedByList.add(likedByArray.getString(j))
          }
          val savedByArray = obj.optJSONArray("savedBy")
          val savedByList = mutableListOf<String>()
          if (savedByArray != null) {
            for (j in 0 until savedByArray.length()) savedByList.add(savedByArray.getString(j))
          }
          val tagsArray = obj.optJSONArray("tags")
          val tagsList = mutableListOf<String>()
          if (tagsArray != null) {
            for (j in 0 until tagsArray.length()) tagsList.add(tagsArray.getString(j))
          }

          list.add(
            FluxVideo(
              id = id,
              authorId = obj.optString("authorId", "user_1"),
              authorName = obj.optString("authorName", "Creator"),
              authorHandle = obj.optString("authorHandle", "@creator"),
              authorVerifiedTier = obj.optString("authorVerifiedTier", "BLUE"),
              isAuthorVerified = obj.optBoolean("isAuthorVerified", false),
              caption = obj.optString("caption", ""),
              videoUri = if (obj.has("videoUri") && !obj.isNull("videoUri")) obj.getString("videoUri") else null,
              audioTitle = obj.optString("audioTitle", "Original Sound"),
              likesCount = obj.optInt("likesCount", 0),
              likedBy = likedByList,
              commentsCount = obj.optInt("commentsCount", 0),
              sharesCount = obj.optInt("sharesCount", 0),
              savedBy = savedByList,
              tags = tagsList,
              gradientTheme = obj.optString("gradientTheme", "BLURPLE"),
              createdAt = obj.optLong("createdAt", System.currentTimeMillis())
            )
          )
        }
        _videos.value = list
      } catch (_: Exception) {
        _videos.value = emptyList()
      }
    } else {
      _videos.value = emptyList()
    }
  }

  private fun saveVideosToPrefs(list: List<FluxVideo>) {
    try {
      val array = JSONArray()
      for (v in list) {
        val obj = JSONObject()
        obj.put("id", v.id)
        obj.put("authorId", v.authorId)
        obj.put("authorName", v.authorName)
        obj.put("authorHandle", v.authorHandle)
        obj.put("authorVerifiedTier", v.authorVerifiedTier)
        obj.put("isAuthorVerified", v.isAuthorVerified)
        obj.put("caption", v.caption)
        obj.put("videoUri", v.videoUri)
        obj.put("audioTitle", v.audioTitle)
        obj.put("likesCount", v.likesCount)
        obj.put("commentsCount", v.commentsCount)
        obj.put("sharesCount", v.sharesCount)
        obj.put("gradientTheme", v.gradientTheme)
        obj.put("createdAt", v.createdAt)

        val likedArray = JSONArray()
        for (l in v.likedBy) likedArray.put(l)
        obj.put("likedBy", likedArray)

        val savedArray = JSONArray()
        for (s in v.savedBy) savedArray.put(s)
        obj.put("savedBy", savedArray)

        val tagsArray = JSONArray()
        for (t in v.tags) tagsArray.put(t)
        obj.put("tags", tagsArray)

        array.put(obj)
      }
      prefs?.edit()?.putString("saved_flux_videos", array.toString())?.apply()
    } catch (_: Exception) {
      // ignore
    }
  }
}
