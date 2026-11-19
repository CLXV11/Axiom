package com.example.data.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@Keep
@IgnoreExtraProperties
data class VoiceCallSession(
  @DocumentId val id: String = "",
  val conversationId: String = "",
  val callerId: String = "",
  val callerName: String = "",
  val callerAvatarUrl: String? = null,
  val calleeId: String = "",
  val calleeName: String = "",
  val calleeAvatarUrl: String? = null,
  val participantIds: List<String> = emptyList(),
  val status: String = "ringing", // "ringing", "connecting", "connected", "ended", "declined", "missed"
  val offerSdp: String? = null,
  val answerSdp: String? = null,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
  val endedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class CallIceCandidate(
  @DocumentId val id: String = "",
  val senderId: String = "",
  val sdpMid: String = "",
  val sdpMLineIndex: Int = 0,
  val candidate: String = "",
  @ServerTimestamp val createdAt: Timestamp? = null
)
