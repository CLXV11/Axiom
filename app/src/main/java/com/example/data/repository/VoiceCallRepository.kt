package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.CallIceCandidate
import com.example.data.model.VoiceCallSession
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

class VoiceCallRepository(
  private val db: FirebaseFirestore?,
  private val auth: FirebaseAuth? = null
) {
  constructor(context: Context) : this(
    resolveFirestore(context),
    resolveAuth()
  )

  private companion object {
    const val INCOMING_CALL_FRESHNESS_MS = 90_000L

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

  private fun isSignedIn(): Boolean = auth?.currentUser != null && db != null

  fun observeIncomingRingingCall(currentUid: String): Flow<VoiceCallSession?> {
    if (!isSignedIn() || currentUid.isBlank()) return emptyFlow()
    return db!!.collection("calls")
      .whereArrayContains("participantIds", currentUid)
      .snapshots()
      .map { snapshot ->
        val now = System.currentTimeMillis()
        snapshot.toObjects(
          VoiceCallSession::class.java,
          DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
        ).filter { session ->
          val createdMs = session.createdAt?.toDate()?.time ?: now
          session.calleeId == currentUid &&
            session.status == "ringing" &&
            (now - createdMs) <= INCOMING_CALL_FRESHNESS_MS
        }.maxByOrNull { it.createdAt?.toDate()?.time ?: 0L }
      }
      .catch { e ->
        handleFirestoreError(e, OperationType.LIST, "calls", auth)
        emit(null)
      }
  }

  fun observeCallSession(callId: String): Flow<VoiceCallSession?> {
    if (!isSignedIn() || callId.isBlank()) return emptyFlow()
    return db!!.collection("calls")
      .document(callId)
      .snapshots()
      .map { snap ->
        if (!snap.exists()) null
        else snap.toObject(VoiceCallSession::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
      }
      .catch { e ->
        handleFirestoreError(e, OperationType.GET, "calls/$callId", auth)
        emit(null)
      }
  }

  fun observeRemoteCandidates(callId: String, currentUid: String): Flow<List<CallIceCandidate>> {
    if (!isSignedIn() || callId.isBlank()) return emptyFlow()
    return db!!.collection("calls")
      .document(callId)
      .collection("candidates")
      .orderBy("createdAt", Query.Direction.ASCENDING)
      .snapshots()
      .map { snap ->
        snap.toObjects(CallIceCandidate::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
          .filter { it.senderId != currentUid }
      }
      .catch { e ->
        handleFirestoreError(e, OperationType.LIST, "calls/$callId/candidates", auth)
        emit(emptyList())
      }
  }

  suspend fun createCallSession(
    conversationId: String,
    callerId: String,
    callerName: String,
    callerAvatarUrl: String?,
    calleeId: String,
    calleeName: String,
    calleeAvatarUrl: String?,
    offerSdp: String? = null
  ): Result<VoiceCallSession> {
    if (!isSignedIn()) {
      return Result.failure(IllegalStateException("Must be signed in to place voice calls"))
    }
    val uid = auth!!.currentUser!!.uid
    val callId = "call_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(8)}"
    val callRef = db!!.collection("calls").document(callId)

    val payload = buildMap<String, Any> {
      put("conversationId", conversationId)
      put("callerId", uid)
      put("callerName", callerName.ifBlank { "Member" }.take(100))
      if (!callerAvatarUrl.isNullOrBlank()) put("callerAvatarUrl", callerAvatarUrl.take(2000))
      put("calleeId", calleeId)
      put("calleeName", calleeName.ifBlank { "Member" }.take(100))
      if (!calleeAvatarUrl.isNullOrBlank()) put("calleeAvatarUrl", calleeAvatarUrl.take(2000))
      put("participantIds", listOf(uid, calleeId).distinct())
      put("status", "ringing")
      if (!offerSdp.isNullOrBlank()) put("offerSdp", offerSdp.take(40000))
      put("createdAt", FieldValue.serverTimestamp())
      put("updatedAt", FieldValue.serverTimestamp())
    }

    return try {
      callRef.set(payload).await()
      Result.success(
        VoiceCallSession(
          id = callId,
          conversationId = conversationId,
          callerId = uid,
          callerName = callerName,
          callerAvatarUrl = callerAvatarUrl,
          calleeId = calleeId,
          calleeName = calleeName,
          calleeAvatarUrl = calleeAvatarUrl,
          participantIds = listOf(uid, calleeId).distinct(),
          status = "ringing",
          offerSdp = offerSdp
        )
      )
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, callRef.path, auth)
      Result.failure(e)
    }
  }

  suspend fun submitOffer(callId: String, offerSdp: String): Result<Unit> {
    if (!isSignedIn()) return Result.success(Unit)
    val callRef = db!!.collection("calls").document(callId)
    return try {
      callRef.update(
        mapOf(
          "offerSdp" to offerSdp.take(40000),
          "updatedAt" to FieldValue.serverTimestamp()
        )
      ).await()
      Result.success(Unit)
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, callRef.path, auth)
      Result.failure(e)
    }
  }

  suspend fun submitAnswer(callId: String, answerSdp: String): Result<Unit> {
    if (!isSignedIn()) return Result.success(Unit)
    val callRef = db!!.collection("calls").document(callId)
    return try {
      callRef.update(
        mapOf(
          "answerSdp" to answerSdp.take(40000),
          "status" to "connecting",
          "updatedAt" to FieldValue.serverTimestamp()
        )
      ).await()
      Result.success(Unit)
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, callRef.path, auth)
      Result.failure(e)
    }
  }

  suspend fun updateCallStatus(callId: String, status: String): Result<Unit> {
    if (!isSignedIn()) return Result.success(Unit)
    val callRef = db!!.collection("calls").document(callId)
    val updates = buildMap<String, Any> {
      put("status", status)
      put("updatedAt", FieldValue.serverTimestamp())
      if (status in listOf("ended", "declined", "missed")) {
        put("endedAt", FieldValue.serverTimestamp())
      }
    }
    return try {
      callRef.update(updates).await()
      Result.success(Unit)
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, callRef.path, auth)
      Result.failure(e)
    }
  }

  suspend fun sendIceCandidate(
    callId: String,
    sdpMid: String,
    sdpMLineIndex: Int,
    candidate: String
  ): Result<Unit> {
    if (!isSignedIn()) return Result.success(Unit)
    val uid = auth!!.currentUser!!.uid
    val candId = "ice_${UUID.randomUUID().toString().take(12)}"
    val candRef = db!!.collection("calls").document(callId).collection("candidates").document(candId)
    return try {
      candRef.set(
        mapOf(
          "senderId" to uid,
          "sdpMid" to sdpMid.take(64),
          "sdpMLineIndex" to sdpMLineIndex.coerceAtLeast(0),
          "candidate" to candidate.take(4096),
          "createdAt" to FieldValue.serverTimestamp()
        )
      ).await()
      Result.success(Unit)
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, candRef.path, auth)
      Result.failure(e)
    }
  }
}
