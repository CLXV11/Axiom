package com.example.media

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioDeviceInfo
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import org.webrtc.AudioSource
import org.webrtc.AudioTrack
import org.webrtc.DataChannel
import org.webrtc.IceCandidate
import org.webrtc.MediaConstraints
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.RtpReceiver
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription
import org.webrtc.audio.JavaAudioDeviceModule

/**
 * High-clarity 48kHz Opus WebRTC voice call audio engine with hardware AEC, NS, AGC,
 * Android 12+ AudioManager.setCommunicationDevice speakerphone routing, and ring/ringback tones.
 */
class VoiceCallEngine(
  context: Context,
  private val onLocalIceCandidate: (sdpMid: String, sdpMLineIndex: Int, candidate: String) -> Unit,
  private val onIceConnected: () -> Unit,
  private val onIceDisconnectedOrFailed: () -> Unit
) {
  companion object {
    private const val TAG = "VoiceCallEngine"

    @Volatile
    private var isFactoryInitialized = false

    private fun ensureFactoryInitialized(context: Context) {
      if (isFactoryInitialized) return
      synchronized(this) {
        if (!isFactoryInitialized) {
          PeerConnectionFactory.initialize(
            PeerConnectionFactory.InitializationOptions.builder(context.applicationContext)
              .setEnableInternalTracer(false)
              .createInitializationOptions()
          )
          isFactoryInitialized = true
        }
      }
    }

    /**
     * Tune Opus codec parameters in SDP for strong, crystal-clear 48kHz voice with FEC.
     */
    fun enhanceOpusSdp(sdp: String): String {
      val lines = sdp.split("\r\n").toMutableList()
      var opusPayload: String? = null
      for (line in lines) {
        if (line.startsWith("a=rtpmap:") && line.contains("opus/48000", ignoreCase = true)) {
          opusPayload = line.substringAfter("a=rtpmap:").substringBefore(" ").trim()
          break
        }
      }
      if (opusPayload == null) return sdp

      val fmtpPrefix = "a=fmtp:$opusPayload "
      val existingIdx = lines.indexOfFirst { it.startsWith(fmtpPrefix) }
      val params = "minptime=10;useinbandfec=1;usedtx=0;maxaveragebitrate=64000"
      if (existingIdx >= 0) {
        val current = lines[existingIdx]
        val updated = buildString {
          append(current)
          if (!current.contains("useinbandfec")) append(";useinbandfec=1")
          if (!current.contains("maxaveragebitrate")) append(";maxaveragebitrate=64000")
          if (!current.contains("usedtx")) append(";usedtx=0")
        }
        lines[existingIdx] = updated
      } else {
        val rtpIdx = lines.indexOfFirst { it.startsWith("a=rtpmap:$opusPayload ") }
        if (rtpIdx >= 0) {
          lines.add(rtpIdx + 1, "$fmtpPrefix$params")
        }
      }
      return lines.joinToString("\r\n")
    }
  }

  private val appContext = context.applicationContext
  private val audioManager = appContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager

  private var audioDeviceModule: JavaAudioDeviceModule? = null
  private var peerConnectionFactory: PeerConnectionFactory? = null
  private var peerConnection: PeerConnection? = null
  private var localAudioSource: AudioSource? = null
  private var localAudioTrack: AudioTrack? = null

  private var previousAudioMode: Int = AudioManager.MODE_NORMAL
  private var audioFocusRequest: AudioFocusRequest? = null
  private var ringbackTone: ToneGenerator? = null
  private var vibrator: Vibrator? = null
  private var isRemoteDescriptionSet = false
  private val pendingRemoteCandidates = mutableListOf<IceCandidate>()
  private var isReleased = false

  private val iceServers = listOf(
    PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer(),
    PeerConnection.IceServer.builder("stun:stun1.l.google.com:19302").createIceServer(),
    PeerConnection.IceServer.builder("stun:stun2.l.google.com:19302").createIceServer(),
    PeerConnection.IceServer.builder("stun:stun3.l.google.com:19302").createIceServer(),
    PeerConnection.IceServer.builder("stun:stun4.l.google.com:19302").createIceServer()
  )

  fun startPeerSession(initialSpeakerOn: Boolean = true) {
    if (isReleased) return
    ensureFactoryInitialized(appContext)
    configureCommunicationAudio(speakerOn = initialSpeakerOn)

    val adm = JavaAudioDeviceModule.builder(appContext)
      .setUseHardwareAcousticEchoCanceler(JavaAudioDeviceModule.isBuiltInAcousticEchoCancelerSupported())
      .setUseHardwareNoiseSuppressor(JavaAudioDeviceModule.isBuiltInNoiseSuppressorSupported())
      .setSampleRate(48000)
      .setAudioRecordErrorCallback(object : JavaAudioDeviceModule.AudioRecordErrorCallback {
        override fun onWebRtcAudioRecordInitError(p0: String?) {
          Log.w(TAG, "AudioRecord init: $p0")
        }

        override fun onWebRtcAudioRecordStartError(
          p0: JavaAudioDeviceModule.AudioRecordStartErrorCode?,
          p1: String?
        ) {
          Log.w(TAG, "AudioRecord start: $p0 $p1")
        }

        override fun onWebRtcAudioRecordError(p0: String?) {
          Log.w(TAG, "AudioRecord error: $p0")
        }
      })
      .createAudioDeviceModule()
    audioDeviceModule = adm

    val factory = PeerConnectionFactory.builder()
      .setAudioDeviceModule(adm)
      .createPeerConnectionFactory()
    peerConnectionFactory = factory

    val rtcConfig = PeerConnection.RTCConfiguration(iceServers).apply {
      sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
      continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY
      iceTransportsType = PeerConnection.IceTransportsType.ALL
    }

    val pc = factory.createPeerConnection(
      rtcConfig,
      object : PeerConnection.Observer {
        override fun onSignalingChange(state: PeerConnection.SignalingState?) {}

        override fun onIceConnectionChange(state: PeerConnection.IceConnectionState?) {
          when (state) {
            PeerConnection.IceConnectionState.CONNECTED,
            PeerConnection.IceConnectionState.COMPLETED -> {
              stopTonesAndVibration()
              onIceConnected()
            }
            PeerConnection.IceConnectionState.FAILED,
            PeerConnection.IceConnectionState.DISCONNECTED,
            PeerConnection.IceConnectionState.CLOSED -> {
              onIceDisconnectedOrFailed()
            }
            else -> Unit
          }
        }

        override fun onIceConnectionReceivingChange(receiving: Boolean) {}

        override fun onIceGatheringChange(state: PeerConnection.IceGatheringState?) {}

        override fun onIceCandidate(candidate: IceCandidate?) {
          candidate ?: return
          onLocalIceCandidate(
            candidate.sdpMid ?: "0",
            candidate.sdpMLineIndex,
            candidate.sdp
          )
        }

        override fun onIceCandidatesRemoved(candidates: Array<out IceCandidate>?) {}

        override fun onAddStream(stream: MediaStream?) {
          stream?.audioTracks?.forEach { track ->
            track.setEnabled(true)
            track.setVolume(1.5)
          }
        }

        override fun onRemoveStream(stream: MediaStream?) {}

        override fun onDataChannel(channel: DataChannel?) {}

        override fun onRenegotiationNeeded() {}

        override fun onAddTrack(receiver: RtpReceiver?, streams: Array<out MediaStream>?) {
          (receiver?.track() as? AudioTrack)?.apply {
            setEnabled(true)
            setVolume(1.5)
          }
        }
      }
    )
    peerConnection = pc

    val audioConstraints = MediaConstraints().apply {
      mandatory.add(MediaConstraints.KeyValuePair("googEchoCancellation", "true"))
      mandatory.add(MediaConstraints.KeyValuePair("googAutoGainControl", "true"))
      mandatory.add(MediaConstraints.KeyValuePair("googNoiseSuppression", "true"))
      mandatory.add(MediaConstraints.KeyValuePair("googHighpassFilter", "true"))
      mandatory.add(MediaConstraints.KeyValuePair("googTypingNoiseDetection", "true"))
    }
    val source = factory.createAudioSource(audioConstraints)
    localAudioSource = source
    val track = factory.createAudioTrack("kora_voice_track", source).apply {
      setEnabled(true)
      setVolume(1.2)
    }
    localAudioTrack = track
    pc?.addTrack(track, listOf("kora_voice_stream"))
  }

  fun createOffer(onOfferReady: (String) -> Unit, onError: (String) -> Unit) {
    val pc = peerConnection ?: run {
      onError("Voice connection not ready")
      return
    }
    val constraints = MediaConstraints().apply {
      mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
      mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "false"))
    }
    pc.createOffer(object : SdpObserver {
      override fun onCreateSuccess(sdp: SessionDescription?) {
        val raw = sdp ?: return
        val tuned = SessionDescription(raw.type, enhanceOpusSdp(raw.description))
        pc.setLocalDescription(object : SdpObserver {
          override fun onCreateSuccess(p0: SessionDescription?) {}
          override fun onSetSuccess() {
            onOfferReady(tuned.description)
          }
          override fun onCreateFailure(err: String?) {}
          override fun onSetFailure(err: String?) {
            onError(err ?: "Failed to set local offer")
          }
        }, tuned)
      }
      override fun onSetSuccess() {}
      override fun onCreateFailure(err: String?) {
        onError(err ?: "Failed to create call offer")
      }
      override fun onSetFailure(err: String?) {}
    }, constraints)
  }

  fun handleRemoteOfferAndCreateAnswer(
    offerSdp: String,
    onAnswerReady: (String) -> Unit,
    onError: (String) -> Unit
  ) {
    val pc = peerConnection ?: run {
      onError("Voice connection not ready")
      return
    }
    val remoteDesc = SessionDescription(SessionDescription.Type.OFFER, offerSdp)
    pc.setRemoteDescription(object : SdpObserver {
      override fun onCreateSuccess(p0: SessionDescription?) {}
      override fun onSetSuccess() {
        isRemoteDescriptionSet = true
        flushPendingCandidates()
        val constraints = MediaConstraints().apply {
          mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
          mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "false"))
        }
        pc.createAnswer(object : SdpObserver {
          override fun onCreateSuccess(answer: SessionDescription?) {
            val raw = answer ?: return
            val tuned = SessionDescription(raw.type, enhanceOpusSdp(raw.description))
            pc.setLocalDescription(object : SdpObserver {
              override fun onCreateSuccess(p0: SessionDescription?) {}
              override fun onSetSuccess() {
                onAnswerReady(tuned.description)
              }
              override fun onCreateFailure(err: String?) {}
              override fun onSetFailure(err: String?) {
                onError(err ?: "Failed to set local answer")
              }
            }, tuned)
          }
          override fun onSetSuccess() {}
          override fun onCreateFailure(err: String?) {
            onError(err ?: "Failed to create call answer")
          }
          override fun onSetFailure(err: String?) {}
        }, constraints)
      }
      override fun onCreateFailure(err: String?) {}
      override fun onSetFailure(err: String?) {
        onError(err ?: "Failed to apply remote offer")
      }
    }, remoteDesc)
  }

  fun setRemoteAnswer(answerSdp: String) {
    val pc = peerConnection ?: return
    if (isRemoteDescriptionSet) return
    val remoteDesc = SessionDescription(SessionDescription.Type.ANSWER, answerSdp)
    pc.setRemoteDescription(object : SdpObserver {
      override fun onCreateSuccess(p0: SessionDescription?) {}
      override fun onSetSuccess() {
        isRemoteDescriptionSet = true
        flushPendingCandidates()
      }
      override fun onCreateFailure(err: String?) {}
      override fun onSetFailure(err: String?) {
        Log.w(TAG, "setRemoteAnswer failed: $err")
      }
    }, remoteDesc)
  }

  fun addRemoteIceCandidate(sdpMid: String, sdpMLineIndex: Int, candidate: String) {
    val ice = IceCandidate(sdpMid, sdpMLineIndex, candidate)
    if (isRemoteDescriptionSet && peerConnection != null) {
      peerConnection?.addIceCandidate(ice)
    } else {
      synchronized(pendingRemoteCandidates) {
        pendingRemoteCandidates.add(ice)
      }
    }
  }

  private fun flushPendingCandidates() {
    val pc = peerConnection ?: return
    synchronized(pendingRemoteCandidates) {
      pendingRemoteCandidates.forEach { pc.addIceCandidate(it) }
      pendingRemoteCandidates.clear()
    }
  }

  fun setMicrophoneMuted(muted: Boolean) {
    localAudioTrack?.setEnabled(!muted)
    runCatching { audioDeviceModule?.setMicrophoneMute(muted) }
  }

  fun setSpeakerphoneEnabled(enabled: Boolean) {
    runCatching {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val targetType = if (enabled) {
          AudioDeviceInfo.TYPE_BUILTIN_SPEAKER
        } else {
          AudioDeviceInfo.TYPE_BUILTIN_EARPIECE
        }
        val available = audioManager.availableCommunicationDevices
        val chosen = available.firstOrNull { it.type == targetType }
          ?: if (!enabled) available.firstOrNull { it.type == AudioDeviceInfo.TYPE_WIRED_HEADSET || it.type == AudioDeviceInfo.TYPE_BLUETOOTH_SCO || it.type == AudioDeviceInfo.TYPE_BLE_HEADSET } else null
        if (chosen != null) {
          audioManager.setCommunicationDevice(chosen)
        } else if (!enabled) {
          audioManager.clearCommunicationDevice()
        }
      } else {
        @Suppress("DEPRECATION")
        audioManager.isSpeakerphoneOn = enabled
      }
    }
  }

  private fun configureCommunicationAudio(speakerOn: Boolean) {
    runCatching {
      previousAudioMode = audioManager.mode
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val attrs = AudioAttributes.Builder()
          .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
          .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
          .build()
        val req = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
          .setAudioAttributes(attrs)
          .setAcceptsDelayedFocusGain(false)
          .setWillPauseWhenDucked(false)
          .build()
        audioFocusRequest = req
        audioManager.requestAudioFocus(req)
      } else {
        @Suppress("DEPRECATION")
        audioManager.requestAudioFocus(null, AudioManager.STREAM_VOICE_CALL, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
      }
      audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
      setSpeakerphoneEnabled(speakerOn)
    }
  }

  fun startRingbackTone() {
    stopTonesAndVibration()
    runCatching {
      ringbackTone = ToneGenerator(AudioManager.STREAM_VOICE_CALL, 65).apply {
        startTone(ToneGenerator.TONE_SUP_RINGTONE)
      }
    }
  }

  fun startIncomingRingFeedback() {
    stopTonesAndVibration()
    runCatching {
      ringbackTone = ToneGenerator(AudioManager.STREAM_RING, 75).apply {
        startTone(ToneGenerator.TONE_PROP_BEEP2)
      }
    }
    runCatching {
      val vib = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        (appContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
      } else {
        @Suppress("DEPRECATION")
        appContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
      }
      vibrator = vib
      val pattern = longArrayOf(0L, 450L, 650L)
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vib?.vibrate(VibrationEffect.createWaveform(pattern, 0))
      } else {
        @Suppress("DEPRECATION")
        vib?.vibrate(pattern, 0)
      }
    }
  }

  fun stopTonesAndVibration() {
    runCatching {
      ringbackTone?.stopTone()
      ringbackTone?.release()
    }
    ringbackTone = null
    runCatching { vibrator?.cancel() }
    vibrator = null
  }

  fun release() {
    if (isReleased) return
    isReleased = true
    stopTonesAndVibration()

    runCatching { localAudioTrack?.setEnabled(false) }
    runCatching { localAudioTrack?.dispose() }
    localAudioTrack = null
    runCatching { localAudioSource?.dispose() }
    localAudioSource = null
    runCatching { peerConnection?.close() }
    runCatching { peerConnection?.dispose() }
    peerConnection = null
    runCatching { peerConnectionFactory?.dispose() }
    peerConnectionFactory = null
    runCatching { audioDeviceModule?.release() }
    audioDeviceModule = null

    runCatching {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        audioManager.clearCommunicationDevice()
      } else {
        @Suppress("DEPRECATION")
        audioManager.isSpeakerphoneOn = false
      }
      audioManager.mode = previousAudioMode
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        audioFocusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
      } else {
        @Suppress("DEPRECATION")
        audioManager.abandonAudioFocus(null)
      }
    }
  }
}
