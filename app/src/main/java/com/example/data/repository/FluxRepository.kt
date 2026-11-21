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
import android.util.Base64
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

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
    const val CHUNK_RAW_BYTES = 450 * 1024
    const val MAX_VIDEO_BYTES = 25L * 1024L * 1024L
    const val RECENT_FLUX_GRACE_MS = 30_000L
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

  private val downloadMutex = Mutex()
  private val activeDownloads = ConcurrentHashMap<String, Deferred<Result<File>>>()
  private val recentlyPublishedFlux = ConcurrentHashMap<String, Pair<FluxVideo, Long>>()

  private val _videos = MutableStateFlow<List<FluxVideo>>(emptyList())
  val videos: Flow<List<FluxVideo>> = _videos.asStateFlow()

  private val _comments = MutableStateFlow<Map<String, List<FluxComment>>>(emptyMap())

  init {
    loadPersistedVideos()
  }

  private fun isSignedIn(): Boolean = auth?.currentUser != null && db != null

  private fun mergeRecentFlux(serverList: List<FluxVideo>): List<FluxVideo> {
    val now = System.currentTimeMillis()
    val serverIds = serverList.mapTo(HashSet()) { it.id }
    val pendingRecent = mutableListOf<FluxVideo>()
    val iterator = recentlyPublishedFlux.entries.iterator()
    while (iterator.hasNext()) {
      val entry = iterator.next()
      val (video, publishedAtMs) = entry.value
      if (serverIds.contains(video.id) || (now - publishedAtMs) > RECENT_FLUX_GRACE_MS) {
        iterator.remove()
      } else {
        pendingRecent.add(video)
      }
    }
    return (pendingRecent + serverList)
      .distinctBy { it.id }
      .sortedByDescending { it.createdAt }
  }

  private fun DocumentSnapshot.toFluxVideoOrNull(): FluxVideo? {
    if (!exists()) return null
    val authorId = getString("authorId") ?: return null
    @Suppress("UNCHECKED_CAST")
    val likedBy = (get("likedBy") as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
    @Suppress("UNCHECKED_CAST")
    val savedBy = (get("savedBy") as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
    @Suppress("UNCHECKED_CAST")
    val tags = (get("tags") as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
    val reactionsRaw = get("reactions") as? Map<*, *>
    val reactionsMap: Map<String, List<String>> = reactionsRaw?.entries
      ?.mapNotNull { (k, v) ->
        val emoji = k as? String ?: return@mapNotNull null
        val uids = (v as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
        if (uids.isEmpty()) null else emoji to uids
      }
      ?.toMap() ?: emptyMap()
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
      mediaId = getString("mediaId"),
      mediaMimeType = getString("mediaMimeType"),
      mediaSizeBytes = getLong("mediaSizeBytes") ?: 0L,
      mediaChunkCount = (getLong("mediaChunkCount") ?: 0L).toInt(),
      audioTitle = getString("audioTitle") ?: "Original Sound",
      likesCount = (getLong("likesCount") ?: 0L).toInt(),
      likedBy = likedBy,
      commentsCount = (getLong("commentsCount") ?: 0L).toInt(),
      sharesCount = (getLong("sharesCount") ?: 0L).toInt(),
      savedBy = savedBy,
      reactions = reactionsMap,
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
          val serverList = snapshot.documents.mapNotNull { it.toFluxVideoOrNull() }
          mergeRecentFlux(serverList).also { merged ->
            _videos.value = merged
            saveVideosToPrefs(merged)
          }
        }
        .onStart {
          if (_videos.value.isNotEmpty()) {
            emit(_videos.value)
          }
        }
        .retryWhen { cause, attempt ->
          val isTransient = attempt < 2 && cause !is CancellationException
          if (isTransient) {
            delay(700L * (attempt + 1))
          }
          isTransient
        }
        .catch { e ->
          handleFirestoreError(e, OperationType.LIST, "flux_videos", auth)
          emit(_videos.value)
        }
    }
    return videos
  }

  suspend fun refreshVideosFromServer(): Result<List<FluxVideo>> {
    if (!isSignedIn()) return Result.success(_videos.value)
    val query = db!!.collection("flux_videos")
      .orderBy("createdAt", Query.Direction.DESCENDING)
      .limit(50)
    return try {
      val snapshot = try {
        query.get(Source.SERVER).await()
      } catch (_: Exception) {
        query.get(Source.DEFAULT).await()
      }
      val serverList = snapshot.documents.mapNotNull { it.toFluxVideoOrNull() }
      val merged = mergeRecentFlux(serverList)
      _videos.value = merged
      saveVideosToPrefs(merged)
      Result.success(merged)
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.LIST, "flux_videos", auth)
      Result.failure(e)
    }
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

  private fun getMediaDir(): File? {
    val ctx = appContext ?: return null
    return File(ctx.filesDir, "flux_media").apply { mkdirs() }
  }

  private fun cachedFileForMediaId(mediaId: String): File? {
    val dir = getMediaDir() ?: return null
    return File(dir, "$mediaId.mp4")
  }

  fun peekCachedFluxFile(video: FluxVideo): File? {
    val mId = video.mediaId
    if (!mId.isNullOrBlank()) {
      val cached = cachedFileForMediaId(mId)
      if (cached != null && cached.exists() && cached.length() > 0L) {
        if (video.mediaSizeBytes <= 0L || cached.length() == video.mediaSizeBytes) {
          return cached
        }
      }
    }
    val rawUri = video.videoUri
    if (!rawUri.isNullOrBlank() && rawUri.startsWith("file://")) {
      val path = Uri.parse(rawUri).path
      if (!path.isNullOrBlank()) {
        val file = File(path)
        if (file.exists() && file.canRead() && file.length() > 0L) return file
      }
    }
    return null
  }

  suspend fun resolvePlayableFluxFile(
    video: FluxVideo,
    onProgress: (Float) -> Unit = {}
  ): Result<File> {
    peekCachedFluxFile(video)?.let {
      onProgress(1f)
      return Result.success(it)
    }
    val mediaId = video.mediaId?.takeIf { it.isNotBlank() && video.mediaChunkCount > 0 }
      ?: return Result.failure(IllegalStateException("No downloadable video chunks"))

    val deferred = downloadMutex.withLock {
      activeDownloads[mediaId] ?: kotlinx.coroutines.CoroutineScope(Dispatchers.IO).async {
        downloadFluxChunksToCache(
          mediaId = mediaId,
          expectedChunkCount = video.mediaChunkCount,
          expectedSizeBytes = video.mediaSizeBytes,
          onProgress = onProgress
        )
      }.also { activeDownloads[mediaId] = it }
    }

    return try {
      deferred.await()
    } finally {
      downloadMutex.withLock {
        if (activeDownloads[mediaId] === deferred) {
          activeDownloads.remove(mediaId)
        }
      }
    }
  }

  private suspend fun downloadFluxChunksToCache(
    mediaId: String,
    expectedChunkCount: Int,
    expectedSizeBytes: Long,
    onProgress: (Float) -> Unit
  ): Result<File> = withContext(Dispatchers.IO) {
    val firestore = db ?: return@withContext Result.failure(IllegalStateException("Firestore unavailable"))
    val finalFile = cachedFileForMediaId(mediaId)
      ?: return@withContext Result.failure(IllegalStateException("Cache directory unavailable"))
    val dir = finalFile.parentFile ?: return@withContext Result.failure(IllegalStateException("Cache dir missing"))
    val tmpFile = File(dir, "${mediaId}_${System.currentTimeMillis()}.tmp")

    try {
      onProgress(0.06f)
      val chunksCollection = firestore.collection("flux_media")
        .document(mediaId)
        .collection("chunks")

      val orderedChunkBytes: List<ByteArray> = if (expectedChunkCount > 0) {
        val semaphore = Semaphore(4)
        var fetchedCount = 0
        val countLock = Mutex()
        coroutineScope {
          val jobs = (0 until expectedChunkCount).map { idx ->
            async {
              semaphore.withPermit {
                var lastErr: Exception? = null
                var bytes: ByteArray? = null
                for (attempt in 0..1) {
                  try {
                    val doc = chunksCollection.document("chunk_$idx").get().await()
                    val b64 = doc.getString("data")
                      ?: throw IllegalStateException("Flux chunk $idx missing")
                    bytes = Base64.decode(b64, Base64.DEFAULT)
                    break
                  } catch (e: Exception) {
                    lastErr = e
                    if (attempt == 0) delay(300L)
                  }
                }
                val resolved = bytes ?: throw (lastErr ?: IllegalStateException("Failed chunk $idx"))
                countLock.withLock {
                  fetchedCount++
                  onProgress(0.08f + (fetchedCount.toFloat() / expectedChunkCount.toFloat()) * 0.80f)
                }
                idx to resolved
              }
            }
          }
          jobs.awaitAll().sortedBy { it.first }.map { it.second }
        }
      } else {
        val snap = chunksCollection.orderBy("index", Query.Direction.ASCENDING).get().await()
        if (snap.isEmpty) {
          return@withContext Result.failure(IllegalStateException("Flux video chunks not found"))
        }
        snap.documents.mapIndexed { idx, doc ->
          val b64 = doc.getString("data") ?: throw IllegalStateException("Corrupt flux chunk $idx")
          Base64.decode(b64, Base64.DEFAULT)
        }
      }

      if (orderedChunkBytes.isEmpty()) {
        return@withContext Result.failure(IllegalStateException("Flux video chunks not found"))
      }

      var writtenBytes = 0L
      FileOutputStream(tmpFile).use { fos ->
        orderedChunkBytes.forEach { decoded ->
          fos.write(decoded)
          writtenBytes += decoded.size
        }
      }

      if (writtenBytes <= 0L || (expectedSizeBytes > 0L && writtenBytes != expectedSizeBytes)) {
        tmpFile.delete()
        return@withContext Result.failure(
          IllegalStateException("Downloaded video size mismatch ($writtenBytes vs $expectedSizeBytes)")
        )
      }

      if (finalFile.exists()) finalFile.delete()
      if (!tmpFile.renameTo(finalFile)) {
        tmpFile.copyTo(finalFile, overwrite = true)
        tmpFile.delete()
      }
      onProgress(1f)
      Result.success(finalFile)
    } catch (e: Exception) {
      tmpFile.delete()
      handleFirestoreError(e, OperationType.GET, "flux_media/$mediaId/chunks", auth)
      Result.failure(e)
    }
  }

  private data class PreparedFluxUpload(
    val mediaId: String,
    val mimeType: String,
    val sizeBytes: Long,
    val chunkCount: Int,
    val localFile: File
  )

  private suspend fun prepareAndUploadFluxVideo(
    sourceUriString: String?,
    onProgress: (Float) -> Unit
  ): PreparedFluxUpload? = withContext(Dispatchers.IO) {
    if (sourceUriString.isNullOrBlank()) return@withContext null
    val context = appContext ?: return@withContext null
    if (sourceUriString.startsWith("http://") || sourceUriString.startsWith("https://")) {
      return@withContext null
    }
    val parsed = Uri.parse(sourceUriString)
    val mediaId = "fm_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(8)}"
    val destFile = cachedFileForMediaId(mediaId) ?: return@withContext null
    val resolver = context.contentResolver
    val mimeType = runCatching { resolver.getType(parsed) }.getOrNull()?.take(128) ?: "video/mp4"

    onProgress(0.04f)
    val inputStream = try {
      resolver.openInputStream(parsed)
    } catch (e: Exception) {
      if (!isSignedIn()) return@withContext null
      throw e
    }
    if (inputStream == null) {
      if (!isSignedIn()) return@withContext null
      throw IllegalStateException("Could not open selected video")
    }
    inputStream.use { input ->
      FileOutputStream(destFile).use { output ->
        val buf = ByteArray(64 * 1024)
        var total = 0L
        while (true) {
          val read = input.read(buf)
          if (read < 0) break
          total += read
          if (total > MAX_VIDEO_BYTES) {
            destFile.delete()
            throw IllegalArgumentException("Video exceeds 25 MB limit")
          }
          output.write(buf, 0, read)
        }
      }
    }

    val sizeBytes = destFile.length()
    if (sizeBytes <= 0L) {
      destFile.delete()
      throw IllegalStateException("Selected video is empty")
    }

    val totalChunks = ((sizeBytes + CHUNK_RAW_BYTES - 1L) / CHUNK_RAW_BYTES).toInt().coerceAtLeast(1)

    if (isSignedIn()) {
      val uid = auth!!.currentUser!!.uid
      val chunksCollection = db!!.collection("flux_media")
        .document(mediaId)
        .collection("chunks")

      val semaphore = Semaphore(4)
      var completedChunks = 0
      val countLock = Mutex()

      coroutineScope {
        val uploadJobs = mutableListOf<Deferred<Unit>>()
        FileInputStream(destFile).use { fis ->
          for (index in 0 until totalChunks) {
            val buffer = ByteArray(CHUNK_RAW_BYTES)
            var offset = 0
            while (offset < buffer.size) {
              val read = fis.read(buffer, offset, buffer.size - offset)
              if (read < 0) break
              offset += read
            }
            if (offset <= 0) break
            val chunkBytes = if (offset == buffer.size) buffer else buffer.copyOf(offset)
            val encoded = Base64.encodeToString(chunkBytes, Base64.NO_WRAP)

            val job = async {
              semaphore.withPermit {
                var lastErr: Exception? = null
                for (attempt in 0..2) {
                  try {
                    chunksCollection.document("chunk_$index").set(
                      mapOf(
                        "mediaId" to mediaId,
                        "senderId" to uid,
                        "index" to index,
                        "data" to encoded,
                        "createdAt" to FieldValue.serverTimestamp()
                      )
                    ).await()
                    lastErr = null
                    break
                  } catch (e: Exception) {
                    lastErr = e
                    if (attempt < 2) delay(350L * (attempt + 1))
                  }
                }
                if (lastErr != null) throw lastErr
                countLock.withLock {
                  completedChunks++
                  onProgress(0.08f + (completedChunks.toFloat() / totalChunks.toFloat()) * 0.85f)
                }
              }
            }
            uploadJobs.add(job)
          }
        }
        uploadJobs.awaitAll()
      }
    }

    onProgress(0.96f)
    PreparedFluxUpload(
      mediaId = mediaId,
      mimeType = mimeType,
      sizeBytes = sizeBytes,
      chunkCount = totalChunks,
      localFile = destFile
    )
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
    gradientTheme: String = "BLURPLE",
    onUploadProgress: (Float) -> Unit = {}
  ): FluxVideo {
    val fluxId = "flux_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}"
    val preparedUpload = prepareAndUploadFluxVideo(videoUri, onUploadProgress)
    val remoteVideoUrl = if (videoUri?.startsWith("http") == true) videoUri else null
    val localPlayableUri = preparedUpload?.let { Uri.fromFile(it.localFile).toString() }
      ?: remoteVideoUrl
      ?: videoUri?.takeIf { !isSignedIn() }

    val extractedTags = Regex("#[\\p{L}\\p{N}_]+").findAll(caption)
      .map { it.value.lowercase(java.util.Locale.ROOT) }
      .distinct()
      .take(15)
      .toList()
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
      videoUri = localPlayableUri,
      mediaId = preparedUpload?.mediaId,
      mediaMimeType = preparedUpload?.mimeType,
      mediaSizeBytes = preparedUpload?.sizeBytes ?: 0L,
      mediaChunkCount = preparedUpload?.chunkCount ?: 0,
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
    recentlyPublishedFlux[fluxId] = newVideo to System.currentTimeMillis()
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
        if (!remoteVideoUrl.isNullOrBlank()) {
          put("videoUri", remoteVideoUrl.take(1000))
        }
        if (preparedUpload != null) {
          put("mediaId", preparedUpload.mediaId)
          put("mediaMimeType", preparedUpload.mimeType)
          put("mediaSizeBytes", preparedUpload.sizeBytes)
          put("mediaChunkCount", preparedUpload.chunkCount)
        }
        put("audioTitle", cleanAudio)
        put("likesCount", 0)
        put("likedBy", emptyList<String>())
        put("commentsCount", 0)
        put("sharesCount", 0)
        put("savedBy", emptyList<String>())
        put("reactions", emptyMap<String, List<String>>())
        put("tags", tags)
        put("gradientTheme", validTheme)
        put("createdAt", FieldValue.serverTimestamp())
      }
      try {
        fluxRef.set(payload).await()
      } catch (e: Exception) {
        recentlyPublishedFlux.remove(fluxId)
        _videos.value = previousList
        saveVideosToPrefs(previousList)
        handleFirestoreError(e, OperationType.CREATE, fluxRef.path, auth)
        throw e
      }
    }

    onUploadProgress(1f)
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
    updated.find { it.id == fluxId }?.let { v ->
      if (recentlyPublishedFlux.containsKey(fluxId)) {
        recentlyPublishedFlux[fluxId] = v to System.currentTimeMillis()
      }
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
    updated.find { it.id == fluxId }?.let { v ->
      if (recentlyPublishedFlux.containsKey(fluxId)) {
        recentlyPublishedFlux[fluxId] = v to System.currentTimeMillis()
      }
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

  suspend fun toggleFluxReaction(fluxId: String, emoji: String, userId: String? = null): Result<Unit> {
    val cleanEmoji = emoji.trim().take(16)
    if (cleanEmoji.isBlank()) return Result.success(Unit)
    val effectiveUid = auth?.currentUser?.uid ?: userId ?: "kora_lead_user"
    val previousList = _videos.value

    val updated = previousList.map { video ->
      if (video.id == fluxId) {
        val mutable = video.reactions.mapValues { it.value.toMutableList() }.toMutableMap()
        val list = mutable.getOrPut(cleanEmoji) { mutableListOf() }
        if (list.contains(effectiveUid)) {
          list.remove(effectiveUid)
          if (list.isEmpty()) mutable.remove(cleanEmoji)
        } else {
          if (mutable.size < 40 || mutable.containsKey(cleanEmoji)) {
            list.add(effectiveUid)
          }
        }
        video.copy(reactions = mutable.mapValues { it.value.distinct().take(200) })
      } else video
    }
    updated.find { it.id == fluxId }?.let { v ->
      if (recentlyPublishedFlux.containsKey(fluxId)) {
        recentlyPublishedFlux[fluxId] = v to System.currentTimeMillis()
      }
    }
    _videos.value = updated
    saveVideosToPrefs(updated)

    if (isSignedIn()) {
      val uid = auth!!.currentUser!!.uid
      val fluxRef = db!!.collection("flux_videos").document(fluxId)
      return try {
        db.runTransaction { transaction ->
          val snap = transaction.get(fluxRef)
          val existing = snap.toFluxVideoOrNull()
            ?: throw IllegalStateException("Flux video not found")
          val mutable = existing.reactions.mapValues { it.value.toMutableList() }.toMutableMap()
          val list = mutable.getOrPut(cleanEmoji) { mutableListOf() }
          if (list.contains(uid)) {
            list.remove(uid)
            if (list.isEmpty()) mutable.remove(cleanEmoji)
          } else {
            if (mutable.size < 40 || mutable.containsKey(cleanEmoji)) {
              list.add(uid)
            }
          }
          transaction.update(
            fluxRef,
            mapOf("reactions" to mutable.mapValues { it.value.distinct().take(200) })
          )
          Unit
        }.await()
        Result.success(Unit)
      } catch (e: Exception) {
        _videos.value = previousList
        saveVideosToPrefs(previousList)
        handleFirestoreError(e, OperationType.UPDATE, fluxRef.path, auth)
        Result.failure(e)
      }
    }

    return Result.success(Unit)
  }

  suspend fun updateFluxVideo(fluxId: String, caption: String, audioTitle: String): Result<Unit> {
    val cleanCaption = caption.trim().ifBlank { "#flux" }.take(2000)
    val cleanAudio = audioTitle.trim().ifBlank { "Original Sound" }.take(120)
    val extractedTags = Regex("#[\\p{L}\\p{N}_]+").findAll(cleanCaption)
      .map { it.value.lowercase(java.util.Locale.ROOT) }
      .distinct()
      .take(15)
      .toList()
    val tags = extractedTags.ifEmpty { listOf("#flux") }

    val previousList = _videos.value
    val updated = previousList.map { video ->
      if (video.id == fluxId) {
        video.copy(caption = cleanCaption, audioTitle = cleanAudio, tags = tags)
      } else video
    }
    updated.find { it.id == fluxId }?.let { v ->
      if (recentlyPublishedFlux.containsKey(fluxId)) {
        recentlyPublishedFlux[fluxId] = v to System.currentTimeMillis()
      }
    }
    _videos.value = updated
    saveVideosToPrefs(updated)

    if (isSignedIn()) {
      val fluxRef = db!!.collection("flux_videos").document(fluxId)
      return try {
        fluxRef.update(
          mapOf(
            "caption" to cleanCaption,
            "audioTitle" to cleanAudio,
            "tags" to tags
          )
        ).await()
        Result.success(Unit)
      } catch (e: Exception) {
        _videos.value = previousList
        saveVideosToPrefs(previousList)
        handleFirestoreError(e, OperationType.UPDATE, fluxRef.path, auth)
        Result.failure(e)
      }
    }

    return Result.success(Unit)
  }

  suspend fun deleteFluxVideo(fluxId: String): Result<Unit> {
    val previousList = _videos.value
    val target = previousList.find { it.id == fluxId }
    recentlyPublishedFlux.remove(fluxId)
    val updated = previousList.filterNot { it.id == fluxId }
    _videos.value = updated
    saveVideosToPrefs(updated)

    if (isSignedIn()) {
      val fluxRef = db!!.collection("flux_videos").document(fluxId)
      try {
        fluxRef.delete().await()
      } catch (e: Exception) {
        _videos.value = previousList
        saveVideosToPrefs(previousList)
        handleFirestoreError(e, OperationType.DELETE, fluxRef.path, auth)
        return Result.failure(e)
      }
    }

    target?.mediaId?.takeIf { it.isNotBlank() }?.let { mId ->
      runCatching { cachedFileForMediaId(mId)?.delete() }
    }
    return Result.success(Unit)
  }

  suspend fun saveVideoToGallery(context: Context, video: FluxVideo): Result<Unit> = withContext(Dispatchers.IO) {
    val resolvedCachedFile = peekCachedFluxFile(video)
      ?: if (!video.mediaId.isNullOrBlank() && video.mediaChunkCount > 0) {
        resolvePlayableFluxFile(video).getOrNull()
      } else null

    val resolver = context.applicationContext.contentResolver
    val sourceStream: java.io.InputStream? = if (resolvedCachedFile != null && resolvedCachedFile.exists()) {
      FileInputStream(resolvedCachedFile)
    } else {
      val rawUri = video.videoUri
      if (!rawUri.isNullOrBlank()) {
        val parsed = Uri.parse(rawUri)
        when (parsed.scheme) {
          "file" -> parsed.path?.let { File(it) }?.takeIf { it.exists() && it.canRead() }?.let { FileInputStream(it) }
          "content" -> resolver.openInputStream(parsed)
          else -> null
        }
      } else null
    }

    if (sourceStream == null) {
      return@withContext Result.failure(IllegalStateException("Video file unavailable on this device"))
    }

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
          val reactionsObj = obj.optJSONObject("reactions")
          val reactionsMap = mutableMapOf<String, List<String>>()
          if (reactionsObj != null) {
            val keys = reactionsObj.keys()
            while (keys.hasNext()) {
              val emoji = keys.next()
              val uidsArray = reactionsObj.optJSONArray(emoji)
              if (uidsArray != null) {
                val uids = mutableListOf<String>()
                for (k in 0 until uidsArray.length()) {
                  val uid = uidsArray.optString(k)
                  if (!uid.isNullOrBlank()) uids.add(uid)
                }
                if (uids.isNotEmpty()) reactionsMap[emoji] = uids
              }
            }
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
              mediaId = if (obj.has("mediaId") && !obj.isNull("mediaId")) obj.getString("mediaId") else null,
              mediaMimeType = if (obj.has("mediaMimeType") && !obj.isNull("mediaMimeType")) obj.getString("mediaMimeType") else null,
              mediaSizeBytes = obj.optLong("mediaSizeBytes", 0L),
              mediaChunkCount = obj.optInt("mediaChunkCount", 0),
              audioTitle = obj.optString("audioTitle", "Original Sound"),
              likesCount = obj.optInt("likesCount", 0),
              likedBy = likedByList,
              commentsCount = obj.optInt("commentsCount", 0),
              sharesCount = obj.optInt("sharesCount", 0),
              savedBy = savedByList,
              reactions = reactionsMap,
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
        obj.put("mediaId", v.mediaId)
        obj.put("mediaMimeType", v.mediaMimeType)
        obj.put("mediaSizeBytes", v.mediaSizeBytes)
        obj.put("mediaChunkCount", v.mediaChunkCount)
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

        val reactionsObj = JSONObject()
        v.reactions.forEach { (emoji, uids) ->
          val uidsArray = JSONArray()
          uids.forEach { uidsArray.put(it) }
          reactionsObj.put(emoji, uidsArray)
        }
        obj.put("reactions", reactionsObj)

        array.put(obj)
      }
      prefs?.edit()?.putString("saved_flux_videos", array.toString())?.apply()
    } catch (_: Exception) {
      // ignore
    }
  }
}
