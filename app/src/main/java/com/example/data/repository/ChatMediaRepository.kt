package com.example.data.repository

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Base64
import androidx.core.content.FileProvider
import com.example.R
import com.example.data.model.Message
import com.example.data.util.OperationType
import com.example.data.util.handleFirestoreError
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

import kotlinx.coroutines.delay

data class PreparedMediaMetadata(
  val mediaId: String,
  val mediaType: String, // "image", "video", "file", "audio"
  val fileName: String,
  val mimeType: String,
  val sizeBytes: Long,
  val chunkCount: Int,
  val durationMs: Long,
  val localFile: File
)

class ChatMediaRepository(
  private val context: Context,
  private val db: FirebaseFirestore? = resolveFirestore(context),
  private val auth: FirebaseAuth? = resolveAuth()
) {
  companion object {
    const val CHUNK_RAW_BYTES = 450 * 1024 // 450 KB -> ~600 KB Base64 (< 750 KB rule cap)
    const val MAX_MEDIA_BYTES = 25L * 1024L * 1024L // 25 MB high-quality media cap
    private const val MAX_IMAGE_DIM_PX = 2048

    private fun resolveFirestore(context: Context): FirebaseFirestore? = try {
      FirebaseFirestore.getInstance(
        context.applicationContext.getString(R.string.firestore_database_id)
      )
    } catch (_: Exception) {
      null
    }

    private fun resolveAuth(): FirebaseAuth? = try {
      Firebase.auth
    } catch (_: Exception) {
      null
    }

    fun formatBytes(bytes: Long): String {
      return when {
        bytes >= 1024L * 1024L -> String.format(java.util.Locale.US, "%.1f MB", bytes / (1024.0 * 1024.0))
        bytes >= 1024L -> String.format(java.util.Locale.US, "%.0f KB", bytes / 1024.0)
        else -> "$bytes B"
      }
    }

    fun formatDuration(durationMs: Long): String {
      if (durationMs <= 0L) return "0:00"
      val totalSeconds = (durationMs / 1000L).toInt()
      val mins = totalSeconds / 60
      val secs = totalSeconds % 60
      return String.format(java.util.Locale.US, "%d:%02d", mins, secs)
    }
  }

  private val appContext = context.applicationContext
  private val mediaDir: File
    get() = File(appContext.filesDir, "chat_media").apply { mkdirs() }

  private val downloadMutex = Mutex()
  private val activeDownloads = ConcurrentHashMap<String, Deferred<Result<File>>>()

  private fun sanitizeFileName(raw: String): String {
    val clean = raw.replace(Regex("[^a-zA-Z0-9._\\-\\u0600-\\u06FF ]"), "_").trim()
    return clean.ifBlank { "attachment" }.take(120)
  }

  private fun cacheFileFor(mediaId: String, fileName: String?): File {
    val safeName = sanitizeFileName(fileName ?: "media")
    return File(mediaDir, "${mediaId}_$safeName")
  }

  fun peekCachedMediaFile(message: Message): File? {
    val localPath = message.localMediaUri?.let { Uri.parse(it).path }
    if (!localPath.isNullOrBlank()) {
      val local = File(localPath)
      if (local.exists() && local.length() > 0L) return local
    }
    val mediaId = message.mediaId ?: return null
    val cached = cacheFileFor(mediaId, message.mediaFileName)
    if (cached.exists() && cached.length() > 0L) {
      if (message.mediaSizeBytes <= 0L || cached.length() == message.mediaSizeBytes) {
        return cached
      }
    }
    return null
  }

  private fun queryFileDisplayName(uri: Uri, fallbackExt: String): String {
    if (uri.scheme == "file") {
      val name = uri.lastPathSegment
      if (!name.isNullOrBlank()) return sanitizeFileName(name)
    } else {
      appContext.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
          val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
          if (idx >= 0) {
            val name = cursor.getString(idx)
            if (!name.isNullOrBlank()) return sanitizeFileName(name)
          }
        }
      }
    }
    return "kora_${System.currentTimeMillis()}.$fallbackExt"
  }

  private fun extractMediaDurationMs(file: File): Long {
    val retriever = MediaMetadataRetriever()
    return try {
      retriever.setDataSource(file.absolutePath)
      retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
    } catch (_: Exception) {
      0L
    } finally {
      runCatching { retriever.release() }
    }
  }

  private fun copyOrOptimizeImageToFile(uri: Uri, destFile: File): String {
    val resolver = appContext.contentResolver
    val tempRaw = File(appContext.cacheDir, "img_raw_${UUID.randomUUID()}")
    try {
      resolver.openInputStream(uri)?.use { input ->
        FileOutputStream(tempRaw).use { out -> input.copyTo(out) }
      } ?: throw IllegalStateException("Could not open selected image")

      // Keep crisp <= 1.8 MB original photos untouched; scale down ultra-huge camera captures cleanly
      if (tempRaw.length() <= 1_800_000L) {
        tempRaw.copyTo(destFile, overwrite = true)
        return resolver.getType(uri) ?: "image/jpeg"
      }

      val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
      BitmapFactory.decodeFile(tempRaw.absolutePath, bounds)
      var sampleSize = 1
      val maxDim = maxOf(bounds.outWidth, bounds.outHeight)
      while (maxDim / sampleSize > MAX_IMAGE_DIM_PX * 2) {
        sampleSize *= 2
      }
      val decodeOpts = BitmapFactory.Options().apply { inSampleSize = sampleSize }
      val decoded = BitmapFactory.decodeFile(tempRaw.absolutePath, decodeOpts)
        ?: run {
          tempRaw.copyTo(destFile, overwrite = true)
          return resolver.getType(uri) ?: "image/jpeg"
        }

      val scaled = if (maxOf(decoded.width, decoded.height) > MAX_IMAGE_DIM_PX) {
        val ratio = MAX_IMAGE_DIM_PX.toFloat() / maxOf(decoded.width, decoded.height).toFloat()
        Bitmap.createScaledBitmap(
          decoded,
          (decoded.width * ratio).toInt().coerceAtLeast(1),
          (decoded.height * ratio).toInt().coerceAtLeast(1),
          true
        )
      } else decoded

      FileOutputStream(destFile).use { out ->
        val baos = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, 90, baos)
        out.write(baos.toByteArray())
      }
      if (scaled !== decoded) scaled.recycle()
      decoded.recycle()
      return "image/jpeg"
    } finally {
      tempRaw.delete()
    }
  }

  suspend fun prepareAndUploadMedia(
    conversationId: String,
    sourceUri: Uri,
    mediaType: String,
    isLocalOnlyChat: Boolean,
    onProgress: (Float) -> Unit = {}
  ): Result<PreparedMediaMetadata> = withContext(Dispatchers.IO) {
    try {
      val mediaId = "m_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(8)}"
      val defaultExt = when (mediaType) {
        "image" -> "jpg"
        "video" -> "mp4"
        "audio" -> "m4a"
        else -> "bin"
      }
      val resolvedName = queryFileDisplayName(sourceUri, defaultExt)
      val destFile = cacheFileFor(mediaId, resolvedName)

      onProgress(0.05f)

      val resolver = appContext.contentResolver
      var mimeType = resolver.getType(sourceUri) ?: when (mediaType) {
        "image" -> "image/jpeg"
        "video" -> "video/mp4"
        "audio" -> "audio/mp4"
        else -> "application/octet-stream"
      }

      if (mediaType == "image") {
        mimeType = copyOrOptimizeImageToFile(sourceUri, destFile)
      } else {
        val inputStream = if (sourceUri.scheme == "file" && !sourceUri.path.isNullOrBlank()) {
          FileInputStream(File(sourceUri.path!!))
        } else {
          resolver.openInputStream(sourceUri)
        }
        inputStream?.use { input ->
          FileOutputStream(destFile).use { output ->
            val buf = ByteArray(64 * 1024)
            var total = 0L
            while (true) {
              val read = input.read(buf)
              if (read < 0) break
              total += read
              if (total > MAX_MEDIA_BYTES) {
                destFile.delete()
                throw IllegalArgumentException("File exceeds 25 MB limit")
              }
              output.write(buf, 0, read)
            }
          }
        } ?: throw IllegalStateException("Could not read selected file")
      }

      val sizeBytes = destFile.length()
      if (sizeBytes <= 0L) {
        destFile.delete()
        throw IllegalStateException("Selected file is empty")
      }
      if (sizeBytes > MAX_MEDIA_BYTES) {
        destFile.delete()
        throw IllegalArgumentException("File exceeds 25 MB limit")
      }

      val durationMs = if (mediaType == "video" || mediaType == "audio") extractMediaDurationMs(destFile) else 0L
      val totalChunks = ((sizeBytes + CHUNK_RAW_BYTES - 1L) / CHUNK_RAW_BYTES).toInt().coerceAtLeast(1)

      if (!isLocalOnlyChat && auth?.currentUser != null && db != null) {
        val uid = auth.currentUser!!.uid
        val chunksCollection = db.collection("conversations")
          .document(conversationId)
          .collection("media")
          .document(mediaId)
          .collection("chunks")

        val semaphore = Semaphore(4)
        var completedChunks = 0
        val countLock = Mutex()

        // Stream chunks sequentially from disk and upload up to 4 chunks concurrently without loading full file into RAM
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
                  val chunkDoc = chunksCollection.document("chunk_$index")
                  chunkDoc.set(
                    mapOf(
                      "mediaId" to mediaId,
                      "senderId" to uid,
                      "index" to index,
                      "data" to encoded,
                      "createdAt" to FieldValue.serverTimestamp()
                    )
                  ).await()
                  countLock.withLock {
                    completedChunks++
                    val progress = 0.1f + (completedChunks.toFloat() / totalChunks.toFloat()) * 0.85f
                    onProgress(progress.coerceIn(0.1f, 0.95f))
                  }
                }
              }
              uploadJobs.add(job)
            }
          }
          uploadJobs.awaitAll()
        }
      }

      onProgress(1f)
      Result.success(
        PreparedMediaMetadata(
          mediaId = mediaId,
          mediaType = mediaType,
          fileName = resolvedName,
          mimeType = mimeType,
          sizeBytes = sizeBytes,
          chunkCount = totalChunks,
          durationMs = durationMs,
          localFile = destFile
        )
      )
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, "conversations/$conversationId/media", auth)
      Result.failure(e)
    }
  }

  suspend fun resolveMediaFile(
    conversationId: String,
    message: Message,
    onProgress: (Float) -> Unit = {}
  ): Result<File> {
    peekCachedMediaFile(message)?.let {
      onProgress(1f)
      return Result.success(it)
    }

    val mediaId = message.mediaId
      ?: return Result.failure(IllegalStateException("Missing mediaId"))

    val deferred = downloadMutex.withLock {
      activeDownloads[mediaId] ?: kotlinx.coroutines.CoroutineScope(Dispatchers.IO).async {
        downloadAndAssemble(conversationId, message, onProgress)
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

  private suspend fun downloadAndAssemble(
    conversationId: String,
    message: Message,
    onProgress: (Float) -> Unit
  ): Result<File> = withContext(Dispatchers.IO) {
    val firestore = db ?: return@withContext Result.failure(IllegalStateException("Firestore unavailable"))
    val mediaId = message.mediaId ?: return@withContext Result.failure(IllegalStateException("Missing mediaId"))
    val finalFile = cacheFileFor(mediaId, message.mediaFileName)
    val tmpFile = File(mediaDir, "${mediaId}_${System.currentTimeMillis()}.tmp")

    try {
      onProgress(0.08f)
      val chunksCollection = firestore.collection("conversations")
        .document(conversationId)
        .collection("media")
        .document(mediaId)
        .collection("chunks")

      val expectedChunks = message.mediaChunkCount
      val orderedBytes: List<ByteArray> = if (expectedChunks > 0) {
        val semaphore = Semaphore(4)
        var fetched = 0
        val countLock = Mutex()
        coroutineScope {
          val jobs = (0 until expectedChunks).map { idx ->
            async {
              semaphore.withPermit {
                var lastErr: Exception? = null
                var bytes: ByteArray? = null
                for (attempt in 0..1) {
                  try {
                    val doc = chunksCollection.document("chunk_$idx").get().await()
                    val b64 = doc.getString("data")
                      ?: throw IllegalStateException("Media chunk $idx missing")
                    bytes = Base64.decode(b64, Base64.DEFAULT)
                    break
                  } catch (e: Exception) {
                    lastErr = e
                    if (attempt == 0) delay(300L)
                  }
                }
                val resolved = bytes ?: throw (lastErr ?: IllegalStateException("Failed chunk $idx"))
                countLock.withLock {
                  fetched++
                  onProgress(0.10f + (fetched.toFloat() / expectedChunks.toFloat()) * 0.80f)
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
          return@withContext Result.failure(IllegalStateException("Media chunks not found"))
        }
        snap.documents.mapIndexed { idx, doc ->
          val b64 = doc.getString("data") ?: throw IllegalStateException("Corrupt media chunk $idx")
          Base64.decode(b64, Base64.DEFAULT)
        }
      }

      if (orderedBytes.isEmpty()) {
        return@withContext Result.failure(IllegalStateException("Media chunks not found"))
      }

      var writtenBytes = 0L
      FileOutputStream(tmpFile).use { fos ->
        orderedBytes.forEach { decoded ->
          fos.write(decoded)
          writtenBytes += decoded.size
        }
      }

      if (writtenBytes <= 0L) {
        tmpFile.delete()
        return@withContext Result.failure(IllegalStateException("Downloaded file is empty"))
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
      handleFirestoreError(e, OperationType.GET, "conversations/$conversationId/media/$mediaId", auth)
      Result.failure(e)
    }
  }

  suspend fun saveToDeviceGalleryOrDownloads(
    file: File,
    mediaType: String,
    mimeType: String?,
    fileName: String?
  ): Result<Unit> = withContext(Dispatchers.IO) {
    if (!file.exists()) {
      return@withContext Result.failure(IllegalStateException("File does not exist locally"))
    }
    val resolver = appContext.contentResolver
    val displayName = sanitizeFileName(fileName ?: file.name)
    val effectiveMime = mimeType ?: when (mediaType) {
      "image" -> "image/jpeg"
      "video" -> "video/mp4"
      else -> "application/octet-stream"
    }

    return@withContext try {
      val (collectionUri, values) = when (mediaType) {
        "image" -> {
          val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
          } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
          }
          val cv = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
            put(MediaStore.Images.Media.MIME_TYPE, effectiveMime)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
              put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/Kora")
              put(MediaStore.Images.Media.IS_PENDING, 1)
            }
          }
          uri to cv
        }
        "video" -> {
          val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
          } else {
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
          }
          val cv = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, displayName)
            put(MediaStore.Video.Media.MIME_TYPE, effectiveMime)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
              put(MediaStore.Video.Media.RELATIVE_PATH, "${Environment.DIRECTORY_MOVIES}/Kora")
              put(MediaStore.Video.Media.IS_PENDING, 1)
            }
          }
          uri to cv
        }
        else -> {
          if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            return@withContext openFileWithSystem(file, effectiveMime)
          }
          val uri = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
          val cv = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, displayName)
            put(MediaStore.Downloads.MIME_TYPE, effectiveMime)
            put(MediaStore.Downloads.RELATIVE_PATH, "${Environment.DIRECTORY_DOWNLOADS}/Kora")
            put(MediaStore.Downloads.IS_PENDING, 1)
          }
          uri to cv
        }
      }

      val targetUri = resolver.insert(collectionUri, values)
        ?: return@withContext Result.failure(IllegalStateException("Could not create MediaStore entry"))

      try {
        FileInputStream(file).use { input ->
          resolver.openOutputStream(targetUri)?.use { output ->
            input.copyTo(output)
          } ?: throw IllegalStateException("Could not open MediaStore output stream")
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
          val done = ContentValues().apply { put(MediaStore.MediaColumns.IS_PENDING, 0) }
          resolver.update(targetUri, done, null, null)
        }
        Result.success(Unit)
      } catch (e: Exception) {
        runCatching { resolver.delete(targetUri, null, null) }
        Result.failure(e)
      }
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  fun openFileWithSystem(file: File, mimeType: String?): Result<Unit> {
    return try {
      val contentUri = FileProvider.getUriForFile(
        appContext,
        "${appContext.packageName}.fileprovider",
        file
      )
      val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(contentUri, mimeType ?: "*/*")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      appContext.startActivity(intent)
      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }
}
