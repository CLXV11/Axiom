package com.example.ui.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.SpeakerPhone
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.VoiceCallSession
import com.example.data.repository.VoiceCallRepository
import com.example.media.VoiceCallEngine
import com.example.ui.util.LocalAppStrings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class VoiceCallUiPhase {
  IDLE,
  OUTGOING_RINGING,
  INCOMING_RINGING,
  CONNECTING,
  ACTIVE
}

data class VoiceCallUiState(
  val phase: VoiceCallUiPhase = VoiceCallUiPhase.IDLE,
  val session: VoiceCallSession? = null,
  val peerName: String = "",
  val peerAvatarUrl: String? = null,
  val peerVerified: Boolean = false,
  val peerVerifiedTier: String? = null,
  val isMuted: Boolean = false,
  val isSpeakerOn: Boolean = true,
  val isMinimized: Boolean = false,
  val durationSeconds: Int = 0
)

class VoiceCallController(
  private val context: Context,
  private val currentUid: String,
  private val currentUserNameProvider: () -> String,
  private val currentAvatarUrlProvider: () -> String?,
  private val repository: VoiceCallRepository,
  private val scope: CoroutineScope,
  private val defaultSpeakerProvider: () -> Boolean = { true }
) {
  private val _uiState = MutableStateFlow(VoiceCallUiState())
  val uiState: StateFlow<VoiceCallUiState> = _uiState.asStateFlow()

  private var callEngine: VoiceCallEngine? = null
  private var incomingObserverJob: Job? = null
  private var sessionObserverJob: Job? = null
  private var candidatesObserverJob: Job? = null
  private var timerJob: Job? = null
  private var timeoutJob: Job? = null

  private val processedCandidateIds = mutableSetOf<String>()
  private var answerApplied = false
  private var offerHandled = false

  init {
    startIncomingCallObserver()
  }

  private fun startIncomingCallObserver() {
    incomingObserverJob?.cancel()
    incomingObserverJob = scope.launch {
      repository.observeIncomingRingingCall(currentUid).collect { incoming ->
        val currentPhase = _uiState.value.phase
        if (incoming != null && currentPhase == VoiceCallUiPhase.IDLE) {
          _uiState.value = VoiceCallUiState(
            phase = VoiceCallUiPhase.INCOMING_RINGING,
            session = incoming,
            peerName = incoming.callerName,
            peerAvatarUrl = incoming.callerAvatarUrl,
            isMuted = false,
            isSpeakerOn = defaultSpeakerProvider(),
            isMinimized = false,
            durationSeconds = 0
          )
          // Start gentle ring & vibration for incoming call
          createOrResetEngine()
          callEngine?.startIncomingRingFeedback()
          observeSessionLifecycle(incoming.id, isCaller = false)
        } else if (incoming == null && currentPhase == VoiceCallUiPhase.INCOMING_RINGING) {
          cleanupToIdle()
        }
      }
    }
  }

  fun startOutgoingCall(
    conversationId: String,
    peerId: String,
    peerName: String,
    peerAvatarUrl: String?,
    peerVerified: Boolean = false,
    peerVerifiedTier: String? = null
  ) {
    if (_uiState.value.phase != VoiceCallUiPhase.IDLE) return
    val initialSpeaker = defaultSpeakerProvider()

    scope.launch {
      _uiState.value = VoiceCallUiState(
        phase = VoiceCallUiPhase.OUTGOING_RINGING,
        peerName = peerName,
        peerAvatarUrl = peerAvatarUrl,
        peerVerified = peerVerified,
        peerVerifiedTier = peerVerifiedTier,
        isMuted = false,
        isSpeakerOn = initialSpeaker,
        isMinimized = false,
        durationSeconds = 0
      )

      val created = repository.createCallSession(
        conversationId = conversationId,
        callerId = currentUid,
        callerName = currentUserNameProvider(),
        callerAvatarUrl = currentAvatarUrlProvider(),
        calleeId = peerId,
        calleeName = peerName,
        calleeAvatarUrl = peerAvatarUrl
      )

      val session = created.getOrNull()
      if (session == null) {
        cleanupToIdle()
        return@launch
      }

      _uiState.value = _uiState.value.copy(session = session)
      val engine = createOrResetEngine(activeCallId = session.id)
      engine.startPeerSession(initialSpeakerOn = initialSpeaker)
      engine.startRingbackTone()

      engine.createOffer(
        onOfferReady = { offerSdp ->
          scope.launch {
            repository.submitOffer(session.id, offerSdp)
          }
        },
        onError = {
          scope.launch { endCall("ended") }
        }
      )

      observeSessionLifecycle(session.id, isCaller = true)
      observeCandidates(session.id)

      // Auto-timeout after 45s if callee does not answer
      timeoutJob?.cancel()
      timeoutJob = scope.launch {
        delay(45_000L)
        if (_uiState.value.phase == VoiceCallUiPhase.OUTGOING_RINGING) {
          endCall("missed")
        }
      }
    }
  }

  fun acceptIncomingCall() {
    val session = _uiState.value.session ?: return
    if (_uiState.value.phase != VoiceCallUiPhase.INCOMING_RINGING) return

    scope.launch {
      _uiState.value = _uiState.value.copy(
        phase = VoiceCallUiPhase.CONNECTING,
        isMinimized = false
      )

      val engine = createOrResetEngine(activeCallId = session.id)
      engine.stopTonesAndVibration()
      engine.startPeerSession(initialSpeakerOn = _uiState.value.isSpeakerOn)

      observeCandidates(session.id)

      val offer = session.offerSdp
      if (!offer.isNullOrBlank()) {
        processIncomingOffer(session.id, offer)
      }
    }
  }

  private fun processIncomingOffer(callId: String, offerSdp: String) {
    if (offerHandled) return
    offerHandled = true
    val engine = callEngine ?: return
    engine.handleRemoteOfferAndCreateAnswer(
      offerSdp = offerSdp,
      onAnswerReady = { answerSdp ->
        scope.launch {
          repository.submitAnswer(callId, answerSdp)
        }
      },
      onError = {
        scope.launch { endCall("ended") }
      }
    )
  }

  fun declineIncomingCall() {
    val callId = _uiState.value.session?.id
    scope.launch {
      if (!callId.isNullOrBlank()) {
        repository.updateCallStatus(callId, "declined")
      }
      cleanupToIdle()
    }
  }

  fun endCall(finalStatus: String = "ended") {
    val callId = _uiState.value.session?.id
    scope.launch {
      if (!callId.isNullOrBlank()) {
        repository.updateCallStatus(callId, finalStatus)
      }
      cleanupToIdle()
    }
  }

  fun toggleMute() {
    val nextMuted = !_uiState.value.isMuted
    callEngine?.setMicrophoneMuted(nextMuted)
    _uiState.value = _uiState.value.copy(isMuted = nextMuted)
  }

  fun toggleSpeaker() {
    val nextSpeaker = !_uiState.value.isSpeakerOn
    callEngine?.setSpeakerphoneEnabled(nextSpeaker)
    _uiState.value = _uiState.value.copy(isSpeakerOn = nextSpeaker)
  }

  fun setMinimized(minimized: Boolean) {
    _uiState.value = _uiState.value.copy(isMinimized = minimized)
  }

  private fun observeSessionLifecycle(callId: String, isCaller: Boolean) {
    sessionObserverJob?.cancel()
    sessionObserverJob = scope.launch {
      repository.observeCallSession(callId).collect { session ->
        if (session == null) {
          cleanupToIdle()
          return@collect
        }

        _uiState.value = _uiState.value.copy(session = session)

        if (session.status in listOf("ended", "declined", "missed")) {
          cleanupToIdle()
          return@collect
        }

        if (isCaller && !answerApplied && !session.answerSdp.isNullOrBlank()) {
          answerApplied = true
          timeoutJob?.cancel()
          callEngine?.stopTonesAndVibration()
          _uiState.value = _uiState.value.copy(phase = VoiceCallUiPhase.CONNECTING)
          callEngine?.setRemoteAnswer(session.answerSdp)
        } else if (!isCaller && !offerHandled &&
          _uiState.value.phase == VoiceCallUiPhase.CONNECTING &&
          !session.offerSdp.isNullOrBlank()
        ) {
          processIncomingOffer(callId, session.offerSdp)
        }

        if (session.status == "connected" && _uiState.value.phase != VoiceCallUiPhase.ACTIVE) {
          transitionToActive(callId)
        }
      }
    }
  }

  private fun observeCandidates(callId: String) {
    candidatesObserverJob?.cancel()
    candidatesObserverJob = scope.launch {
      repository.observeRemoteCandidates(callId, currentUid).collect { candidates ->
        candidates.forEach { cand ->
          if (cand.id.isNotBlank() && processedCandidateIds.add(cand.id)) {
            callEngine?.addRemoteIceCandidate(
              sdpMid = cand.sdpMid,
              sdpMLineIndex = cand.sdpMLineIndex,
              candidate = cand.candidate
            )
          }
        }
      }
    }
  }

  private fun transitionToActive(callId: String) {
    if (_uiState.value.phase == VoiceCallUiPhase.ACTIVE) return
    timeoutJob?.cancel()
    callEngine?.stopTonesAndVibration()
    _uiState.value = _uiState.value.copy(phase = VoiceCallUiPhase.ACTIVE)
    startCallDurationTimer()
    scope.launch {
      repository.updateCallStatus(callId, "connected")
    }
  }

  private fun startCallDurationTimer() {
    timerJob?.cancel()
    timerJob = scope.launch {
      while (true) {
        delay(1000L)
        _uiState.value = _uiState.value.copy(
          durationSeconds = _uiState.value.durationSeconds + 1
        )
      }
    }
  }

  private fun createOrResetEngine(activeCallId: String? = null): VoiceCallEngine {
    callEngine?.release()
    answerApplied = false
    offerHandled = false
    processedCandidateIds.clear()

    val engine = VoiceCallEngine(
      context = context,
      onLocalIceCandidate = { mid, mLine, cand ->
        val callId = activeCallId ?: _uiState.value.session?.id ?: return@VoiceCallEngine
        scope.launch {
          repository.sendIceCandidate(callId, mid, mLine, cand)
        }
      },
      onIceConnected = {
        val callId = activeCallId ?: _uiState.value.session?.id
        scope.launch {
          if (callId != null) {
            transitionToActive(callId)
          }
        }
      },
      onIceDisconnectedOrFailed = {
        scope.launch {
          if (_uiState.value.phase == VoiceCallUiPhase.ACTIVE) {
            endCall("ended")
          }
        }
      }
    )
    callEngine = engine
    return engine
  }

  private fun cleanupToIdle() {
    sessionObserverJob?.cancel()
    candidatesObserverJob?.cancel()
    timerJob?.cancel()
    timeoutJob?.cancel()
    callEngine?.release()
    callEngine = null
    answerApplied = false
    offerHandled = false
    processedCandidateIds.clear()
    _uiState.value = VoiceCallUiState()
  }

  fun release() {
    incomingObserverJob?.cancel()
    cleanupToIdle()
  }
}

fun formatCallDuration(seconds: Int): String {
  val mins = seconds / 60
  val secs = seconds % 60
  return String.format(java.util.Locale.US, "%02d:%02d", mins, secs)
}

@Composable
fun VoiceCallOverlay(controller: VoiceCallController) {
  val context = LocalContext.current
  val strings = LocalAppStrings.current
  val state by controller.uiState.collectAsState()

  var pendingAcceptCall by remember { mutableStateOf(false) }

  val micPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { granted ->
    if (granted && pendingAcceptCall) {
      pendingAcceptCall = false
      controller.acceptIncomingCall()
    } else if (!granted) {
      pendingAcceptCall = false
      Toast.makeText(context, strings.micPermissionRequired, Toast.LENGTH_LONG).show()
      controller.declineIncomingCall()
    }
  }

  // Incoming Call Floating Banner
  AnimatedVisibility(
    visible = state.phase == VoiceCallUiPhase.INCOMING_RINGING,
    enter = slideInVertically { -it } + fadeIn(),
    exit = slideOutVertically { -it } + fadeOut()
  ) {
    IncomingCallBanner(
      peerName = state.peerName,
      peerAvatarUrl = state.peerAvatarUrl,
      onAccept = {
        val hasMic = ContextCompat.checkSelfPermission(
          context,
          Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        if (hasMic) {
          controller.acceptIncomingCall()
        } else {
          pendingAcceptCall = true
          micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
      },
      onDecline = { controller.declineIncomingCall() }
    )
  }

  // Minimized Active/Ringing Call Pill
  AnimatedVisibility(
    visible = state.phase in listOf(
      VoiceCallUiPhase.OUTGOING_RINGING,
      VoiceCallUiPhase.CONNECTING,
      VoiceCallUiPhase.ACTIVE
    ) && state.isMinimized,
    enter = slideInVertically { -it } + fadeIn(),
    exit = slideOutVertically { -it } + fadeOut()
  ) {
    MinimizedCallPill(
      state = state,
      onExpand = { controller.setMinimized(false) },
      onToggleMute = { controller.toggleMute() },
      onEndCall = { controller.endCall() }
    )
  }

  // Fullscreen Voice Call Experience
  AnimatedVisibility(
    visible = state.phase in listOf(
      VoiceCallUiPhase.OUTGOING_RINGING,
      VoiceCallUiPhase.CONNECTING,
      VoiceCallUiPhase.ACTIVE
    ) && !state.isMinimized,
    enter = fadeIn(),
    exit = fadeOut()
  ) {
    FullscreenVoiceCallScreen(
      state = state,
      onMinimize = { controller.setMinimized(true) },
      onToggleMute = { controller.toggleMute() },
      onToggleSpeaker = { controller.toggleSpeaker() },
      onEndCall = { controller.endCall() }
    )
  }
}

@Composable
private fun IncomingCallBanner(
  peerName: String,
  peerAvatarUrl: String?,
  onAccept: () -> Unit,
  onDecline: () -> Unit
) {
  val strings = LocalAppStrings.current

  Box(
    modifier = Modifier
      .fillMaxWidth()
      .statusBarsPadding()
      .padding(horizontal = 14.dp, vertical = 10.dp),
    contentAlignment = Alignment.TopCenter
  ) {
    Surface(
      shape = RoundedCornerShape(22.dp),
      color = Color(0xFF10172A),
      border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.4f)),
      tonalElevation = 8.dp,
      modifier = Modifier
        .fillMaxWidth()
        .widthIn(max = 520.dp)
        .testTag("incoming_voice_call_banner")
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        AvatarView(
          name = peerName.ifBlank { "Member" },
          avatarUrl = peerAvatarUrl,
          size = 48.dp
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = peerName.ifBlank { strings.voiceCallLabel },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.PhoneInTalk,
              contentDescription = null,
              tint = Color(0xFF34D399),
              modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
              text = strings.callIncoming,
              style = MaterialTheme.typography.bodySmall,
              color = Color.White.copy(alpha = 0.8f)
            )
          }
        }
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(
          onClick = onDecline,
          modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(Color(0xFFEF4444))
            .testTag("decline_call_button")
        ) {
          Icon(
            imageVector = Icons.Default.CallEnd,
            contentDescription = strings.declineCall,
            tint = Color.White
          )
        }
        Spacer(modifier = Modifier.width(10.dp))
        IconButton(
          onClick = onAccept,
          modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(Color(0xFF10B981))
            .testTag("accept_call_button")
        ) {
          Icon(
            imageVector = Icons.Default.Call,
            contentDescription = strings.acceptCall,
            tint = Color.White
          )
        }
      }
    }
  }
}

@Composable
private fun MinimizedCallPill(
  state: VoiceCallUiState,
  onExpand: () -> Unit,
  onToggleMute: () -> Unit,
  onEndCall: () -> Unit
) {
  val strings = LocalAppStrings.current
  val statusText = when (state.phase) {
    VoiceCallUiPhase.ACTIVE -> formatCallDuration(state.durationSeconds)
    VoiceCallUiPhase.CONNECTING -> strings.callConnecting
    else -> strings.callRinging
  }

  Box(
    modifier = Modifier
      .fillMaxWidth()
      .statusBarsPadding()
      .padding(horizontal = 16.dp, vertical = 8.dp),
    contentAlignment = Alignment.TopCenter
  ) {
    Surface(
      onClick = onExpand,
      shape = RoundedCornerShape(30.dp),
      color = Color(0xFF0F2922),
      border = BorderStroke(1.dp, Color(0xFF34D399).copy(alpha = 0.55f)),
      tonalElevation = 6.dp,
      modifier = Modifier
        .fillMaxWidth()
        .widthIn(max = 460.dp)
        .defaultMinSize(minHeight = 52.dp)
        .testTag("minimized_call_pill")
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          imageVector = Icons.Default.GraphicEq,
          contentDescription = null,
          tint = Color(0xFF34D399),
          modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = state.peerName.ifBlank { strings.voiceCallLabel },
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
          Text(
            text = "$statusText • ${strings.returnToCall}",
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF6EE7B7),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }
        IconButton(
          onClick = onToggleMute,
          modifier = Modifier.size(40.dp)
        ) {
          Icon(
            imageVector = if (state.isMuted) Icons.Default.MicOff else Icons.Default.Mic,
            contentDescription = if (state.isMuted) strings.unmuteMic else strings.muteMic,
            tint = if (state.isMuted) Color(0xFFFBBF24) else Color.White,
            modifier = Modifier.size(18.dp)
          )
        }
        Spacer(modifier = Modifier.width(4.dp))
        IconButton(
          onClick = onEndCall,
          modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(Color(0xFFEF4444))
        ) {
          Icon(
            imageVector = Icons.Default.CallEnd,
            contentDescription = strings.endCall,
            tint = Color.White,
            modifier = Modifier.size(18.dp)
          )
        }
      }
    }
  }
}

@Composable
private fun FullscreenVoiceCallScreen(
  state: VoiceCallUiState,
  onMinimize: () -> Unit,
  onToggleMute: () -> Unit,
  onToggleSpeaker: () -> Unit,
  onEndCall: () -> Unit
) {
  val strings = LocalAppStrings.current
  val infiniteTransition = rememberInfiniteTransition(label = "voice_pulse")
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 1.0f,
    targetValue = 1.18f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "avatar_halo_scale"
  )

  val statusText = when (state.phase) {
    VoiceCallUiPhase.OUTGOING_RINGING -> strings.callRinging
    VoiceCallUiPhase.CONNECTING -> strings.callConnecting
    VoiceCallUiPhase.ACTIVE -> "${strings.callActive} • ${formatCallDuration(state.durationSeconds)}"
    else -> ""
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(
        Brush.verticalGradient(
          colors = listOf(
            Color(0xFF0A0F1F),
            Color(0xFF101935),
            Color(0xFF070A14)
          )
        )
      )
      .statusBarsPadding()
      .navigationBarsPadding()
      .testTag("fullscreen_voice_call_overlay")
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 24.dp, vertical = 18.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      // Top Bar
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(
          onClick = onMinimize,
          modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.08f))
            .testTag("minimize_call_button")
        ) {
          Icon(
            imageVector = Icons.Default.KeyboardArrowDown,
            contentDescription = strings.minimizeCall,
            tint = Color.White
          )
        }

        Surface(
          shape = RoundedCornerShape(50),
          color = Color(0xFF38BDF8).copy(alpha = 0.14f),
          border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.4f))
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.GraphicEq,
              contentDescription = null,
              tint = Color(0xFF38BDF8),
              modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = strings.callQualityHdBadge,
              style = MaterialTheme.typography.labelSmall,
              color = Color(0xFFBAE6FD),
              fontWeight = FontWeight.SemiBold
            )
          }
        }

        Spacer(modifier = Modifier.size(48.dp))
      }

      // Center Peer Identity & Pulsing Audio Aura
      Column(
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Box(contentAlignment = Alignment.Center) {
          Box(
            modifier = Modifier
              .size(154.dp)
              .scale(if (state.phase == VoiceCallUiPhase.ACTIVE) pulseScale else 1.05f)
              .clip(CircleShape)
              .background(
                Brush.radialGradient(
                  listOf(
                    (if (state.phase == VoiceCallUiPhase.ACTIVE) Color(0xFF10B981) else Color(0xFF6366F1))
                      .copy(alpha = 0.28f),
                    Color.Transparent
                  )
                )
              )
          )
          Box(
            modifier = Modifier
              .size(120.dp)
              .clip(CircleShape)
              .border(
                width = 2.dp,
                color = if (state.phase == VoiceCallUiPhase.ACTIVE) {
                  Color(0xFF34D399).copy(alpha = 0.8f)
                } else {
                  Color(0xFF818CF8).copy(alpha = 0.65f)
                },
                shape = CircleShape
              ),
            contentAlignment = Alignment.Center
          ) {
            AvatarView(
              name = state.peerName.ifBlank { "Member" },
              avatarUrl = state.peerAvatarUrl,
              size = 112.dp
            )
          }
        }

        Spacer(modifier = Modifier.height(26.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = state.peerName.ifBlank { strings.voiceCallLabel },
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
          if (state.peerVerified) {
            Spacer(modifier = Modifier.width(8.dp))
            VerifiedBadge(
              size = 20.dp,
              tierKey = state.peerVerifiedTier,
              showDialogOnClick = false
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
          text = statusText,
          style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp),
          color = if (state.phase == VoiceCallUiPhase.ACTIVE) Color(0xFF6EE7B7) else Color.White.copy(alpha = 0.78f),
          fontWeight = FontWeight.Medium
        )
      }

      // Bottom Audio Control Dock
      Surface(
        shape = RoundedCornerShape(28.dp),
        color = Color.White.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.14f)),
        modifier = Modifier
          .fillMaxWidth()
          .widthIn(max = 420.dp)
          .padding(bottom = 12.dp)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 18.dp),
          horizontalArrangement = Arrangement.SpaceEvenly,
          verticalAlignment = Alignment.CenterVertically
        ) {
          CallActionButton(
            icon = if (state.isMuted) Icons.Default.MicOff else Icons.Default.Mic,
            label = if (state.isMuted) strings.unmuteMic else strings.muteMic,
            isActive = state.isMuted,
            activeBg = Color(0xFFF59E0B),
            testTag = "call_mute_button",
            onClick = onToggleMute
          )

          CallActionButton(
            icon = if (state.isSpeakerOn) Icons.AutoMirrored.Filled.VolumeUp else Icons.Default.SpeakerPhone,
            label = if (state.isSpeakerOn) strings.speakerOn else strings.speakerOff,
            isActive = state.isSpeakerOn,
            activeBg = Color(0xFF3B82F6),
            testTag = "call_speaker_button",
            onClick = onToggleSpeaker
          )

          CallActionButton(
            icon = Icons.Default.CallEnd,
            label = strings.endCall,
            isActive = true,
            activeBg = Color(0xFFEF4444),
            testTag = "call_end_button",
            onClick = onEndCall
          )
        }
      }
    }
  }
}

@Composable
private fun CallActionButton(
  icon: ImageVector,
  label: String,
  isActive: Boolean,
  activeBg: Color,
  testTag: String,
  onClick: () -> Unit
) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    IconButton(
      onClick = onClick,
      modifier = Modifier
        .size(60.dp)
        .clip(CircleShape)
        .background(if (isActive) activeBg else Color.White.copy(alpha = 0.14f))
        .testTag(testTag)
    ) {
      Icon(
        imageVector = icon,
        contentDescription = label,
        tint = Color.White,
        modifier = Modifier.size(26.dp)
      )
    }
    Spacer(modifier = Modifier.height(8.dp))
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall,
      color = Color.White.copy(alpha = 0.85f),
      maxLines = 1
    )
  }
}
