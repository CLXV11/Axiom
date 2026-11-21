package com.example.ui.components

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

class VoiceRecorderController(
  private val context: Context,
  private val scope: CoroutineScope
) {
  companion object {
    private const val TAG = "VoiceRecorderController"
    private const val MAX_VOICE_NOTE_MS = 120_000L // 2 minutes max keeps well below 25MB
  }

  private val _isRecording = MutableStateFlow(false)
  val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

  private val _elapsedMs = MutableStateFlow(0L)
  val elapsedMs: StateFlow<Long> = _elapsedMs.asStateFlow()

  private var recorder: MediaRecorder? = null
  private var outputFile: File? = null
  private var timerJob: Job? = null
  private var startedAtMs: Long = 0L

  fun startRecording(): Result<Unit> {
    if (_isRecording.value) return Result.success(Unit)
    return try {
      val audioDir = File(context.cacheDir, "voice_notes").apply { mkdirs() }
      val file = File(audioDir, "voice_${UUID.randomUUID()}.m4a")
      val mr = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        MediaRecorder(context.applicationContext)
      } else {
        @Suppress("DEPRECATION")
        MediaRecorder()
      }
      mr.apply {
        setAudioSource(MediaRecorder.AudioSource.MIC)
        setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        setAudioEncodingBitRate(96_000)
        setAudioSamplingRate(44_100)
        setOutputFile(file.absolutePath)
        prepare()
        start()
      }

      recorder = mr
      outputFile = file
      startedAtMs = System.currentTimeMillis()
      _elapsedMs.value = 0L
      _isRecording.value = true

      timerJob?.cancel()
      timerJob = scope.launch(Dispatchers.Main.immediate) {
        while (isActive && _isRecording.value) {
          val elapsed = System.currentTimeMillis() - startedAtMs
          _elapsedMs.value = elapsed
          if (elapsed >= MAX_VOICE_NOTE_MS) {
            break
          }
          delay(200L)
        }
      }
      Result.success(Unit)
    } catch (e: Exception) {
      Log.e(TAG, "Failed to start voice recording", e)
      cleanupInternal(deleteFile = true)
      Result.failure(e)
    }
  }

  fun stopRecording(): File? {
    if (!_isRecording.value) return null
    timerJob?.cancel()
    timerJob = null
    _isRecording.value = false

    val file = outputFile
    val activeRecorder = recorder
    recorder = null
    outputFile = null

    try {
      activeRecorder?.stop()
    } catch (e: Exception) {
      Log.w(TAG, "MediaRecorder stop warning: ${e.message}")
      runCatching { file?.delete() }
      runCatching { activeRecorder?.release() }
      _elapsedMs.value = 0L
      return null
    }
    runCatching { activeRecorder?.release() }
    _elapsedMs.value = 0L

    return if (file != null && file.exists() && file.length() > 256L) {
      file
    } else {
      runCatching { file?.delete() }
      null
    }
  }

  fun cancelRecording() {
    cleanupInternal(deleteFile = true)
  }

  fun release() {
    cleanupInternal(deleteFile = true)
  }

  private fun cleanupInternal(deleteFile: Boolean) {
    timerJob?.cancel()
    timerJob = null
    _isRecording.value = false
    _elapsedMs.value = 0L

    val activeRecorder = recorder
    val file = outputFile
    recorder = null
    outputFile = null

    if (activeRecorder != null) {
      runCatching { activeRecorder.stop() }
      runCatching { activeRecorder.release() }
    }
    if (deleteFile && file != null) {
      runCatching { file.delete() }
    }
  }
}
