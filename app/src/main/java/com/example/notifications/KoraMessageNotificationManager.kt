package com.example.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.data.model.Conversation
import com.example.ui.util.KoraStrings

data class IncomingMessageAlert(
  val conversation: Conversation,
  val senderName: String,
  val senderAvatarUrl: String?,
  val messagePreview: String
)

object KoraMessageNotificationManager {
  const val CHANNEL_ID = "kora_messages"
  const val EXTRA_CONVERSATION_ID = "kora_extra_conversation_id"

  fun hasNotificationPermission(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.POST_NOTIFICATIONS
      ) == PackageManager.PERMISSION_GRANTED
    } else {
      true
    }
  }

  fun ensureNotificationChannel(context: Context, strings: KoraStrings) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
      val existing = manager.getNotificationChannel(CHANNEL_ID)
      if (existing != null) {
        if (existing.name != strings.newMessageChannelName) {
          existing.name = strings.newMessageChannelName
          existing.description = strings.newMessageChannelDesc
          manager.createNotificationChannel(existing)
        }
        return
      }
      val channel = NotificationChannel(
        CHANNEL_ID,
        strings.newMessageChannelName,
        NotificationManager.IMPORTANCE_HIGH
      ).apply {
        description = strings.newMessageChannelDesc
        enableVibration(true)
        enableLights(true)
        setShowBadge(true)
        lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
      }
      manager.createNotificationChannel(channel)
    }
  }

  fun postNewMessageNotification(
    context: Context,
    conversationId: String,
    senderName: String,
    messagePreview: String,
    strings: KoraStrings
  ) {
    if (!hasNotificationPermission(context)) return
    ensureNotificationChannel(context, strings)

    val notificationId = (conversationId.hashCode() and 0x7FFFFFFF).coerceAtLeast(1001)
    val launchIntent = Intent(context, MainActivity::class.java).apply {
      flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
      putExtra(EXTRA_CONVERSATION_ID, conversationId)
    }
    val pendingIntent = PendingIntent.getActivity(
      context,
      notificationId,
      launchIntent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val title = if (senderName.isNotBlank()) {
      String.format(strings.newMessageFromTitle, senderName)
    } else {
      strings.newMessageNotificationTitle
    }
    val body = messagePreview.ifBlank { strings.newMessageNotificationTitle }

    val notification = NotificationCompat.Builder(context, CHANNEL_ID)
      .setSmallIcon(R.mipmap.ic_launcher)
      .setContentTitle(title)
      .setContentText(body)
      .setStyle(NotificationCompat.BigTextStyle().bigText(body))
      .setPriority(NotificationCompat.PRIORITY_HIGH)
      .setCategory(NotificationCompat.CATEGORY_MESSAGE)
      .setDefaults(NotificationCompat.DEFAULT_ALL)
      .setAutoCancel(true)
      .setContentIntent(pendingIntent)
      .build()

    runCatching {
      NotificationManagerCompat.from(context).notify(notificationId, notification)
    }
  }

  fun cancelNotificationForConversation(context: Context, conversationId: String) {
    val notificationId = (conversationId.hashCode() and 0x7FFFFFFF).coerceAtLeast(1001)
    runCatching {
      NotificationManagerCompat.from(context).cancel(notificationId)
    }
  }

  /**
   * Compares incoming conversation snapshots against a baseline map so the user only gets
   * notified when a real new peer message arrives while they are not viewing that conversation.
   */
  fun detectNewIncomingMessages(
    currentUid: String,
    activeConversationId: String?,
    baselineInitialized: Boolean,
    lastSeenTimestamps: MutableMap<String, Long>,
    lastSeenSignatures: MutableMap<String, String>,
    conversations: List<Conversation>,
    aiConversationId: String
  ): List<Conversation> {
    if (!baselineInitialized) {
      conversations.forEach { convo ->
        val ts = resolveTimestampMs(convo)
        lastSeenTimestamps[convo.id] = ts
        lastSeenSignatures[convo.id] = buildSignature(convo)
      }
      return emptyList()
    }

    val newlyArrived = mutableListOf<Conversation>()
    for (convo in conversations) {
      val msgTs = resolveTimestampMs(convo)
      val signature = buildSignature(convo)
      val previousTs = lastSeenTimestamps[convo.id]
      val previousSig = lastSeenSignatures[convo.id]

      lastSeenTimestamps[convo.id] = maxOf(previousTs ?: 0L, msgTs)
      lastSeenSignatures[convo.id] = signature

      val isNewEvent = when {
        previousTs == null -> convo.lastMessageText.isNotBlank()
        msgTs > previousTs -> true
        msgTs == previousTs && signature != previousSig -> true
        else -> false
      }

      if (
        isNewEvent &&
        convo.id != aiConversationId &&
        convo.id != activeConversationId &&
        convo.lastMessageSenderId.isNotBlank() &&
        convo.lastMessageSenderId != currentUid &&
        convo.lastMessageText.isNotBlank() &&
        !convo.mutedBy.contains(currentUid)
      ) {
        newlyArrived.add(convo)
      }
    }
    return newlyArrived
  }

  private fun resolveTimestampMs(convo: Conversation): Long {
    return convo.lastMessageTimestamp?.toDate()?.time
      ?: convo.updatedAt?.toDate()?.time
      ?: convo.createdAt?.toDate()?.time
      ?: 0L
  }

  private fun buildSignature(convo: Conversation): String {
    return "${convo.lastMessageSenderId}|${convo.lastMessageText}"
  }
}
