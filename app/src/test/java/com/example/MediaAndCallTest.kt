package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.Conversation
import com.example.data.model.Message
import com.example.data.repository.ChatMediaRepository
import com.example.data.repository.ChatRepository
import com.example.data.repository.PreparedMediaMetadata
import com.example.media.VoiceCallEngine
import com.example.notifications.KoraMessageNotificationManager
import com.example.ui.components.formatCallDuration
import com.example.ui.util.ArabicStrings
import com.example.ui.util.EnglishStrings
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.util.Date

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MediaAndCallTest {

  private lateinit var context: Context
  private lateinit var chatRepository: ChatRepository

  @Before
  fun setup() {
    context = ApplicationProvider.getApplicationContext()
    if (FirebaseApp.getApps(context).isEmpty()) {
      FirebaseApp.initializeApp(
        context,
        FirebaseOptions.Builder()
          .setApplicationId("com.aistudio.kora.qnxzmv")
          .setProjectId("kora-test")
          .setApiKey("fake-api-key")
          .build()
      )
    }
    chatRepository = ChatRepository(context)
  }

  @Test
  fun testEnhanceOpusSdpAddsHighClarityVoiceParams() {
    val rawSdp = listOf(
      "v=0",
      "m=audio 9 UDP/TLS/RTP/SAVPF 111 103",
      "a=rtpmap:111 opus/48000/2",
      "a=fmtp:111 minptime=10",
      "a=rtpmap:103 ISAC/16000"
    ).joinToString("\r\n")

    val tuned = VoiceCallEngine.enhanceOpusSdp(rawSdp)
    assertTrue(tuned.contains("useinbandfec=1"))
    assertTrue(tuned.contains("maxaveragebitrate=64000"))
    assertTrue(tuned.contains("usedtx=0"))
  }

  @Test
  fun testEnhanceOpusSdpInsertsFmtpWhenMissing() {
    val rawSdp = listOf(
      "v=0",
      "m=audio 9 UDP/TLS/RTP/SAVPF 111",
      "a=rtpmap:111 opus/48000/2"
    ).joinToString("\r\n")

    val tuned = VoiceCallEngine.enhanceOpusSdp(rawSdp)
    assertTrue(tuned.contains("a=fmtp:111 minptime=10;useinbandfec=1;usedtx=0;maxaveragebitrate=64000"))
  }

  @Test
  fun testMediaFormattingAndAttachmentDetection() {
    assertEquals("512 B", ChatMediaRepository.formatBytes(512))
    assertEquals("250 KB", ChatMediaRepository.formatBytes(250 * 1024L))
    assertEquals("4.5 MB", ChatMediaRepository.formatBytes((4.5 * 1024 * 1024).toLong()))

    assertEquals("0:00", ChatMediaRepository.formatDuration(0L))
    assertEquals("1:05", ChatMediaRepository.formatDuration(65_000L))
    assertEquals("03:42", formatCallDuration(222))

    val textMsg = Message(id = "1", content = "Hello", mediaType = "text")
    assertFalse(textMsg.hasMediaAttachment())

    val videoMsg = Message(id = "2", mediaType = "video", mediaId = "m_123")
    assertTrue(videoMsg.hasMediaAttachment())
  }

  @Test
  fun testSendMediaMessagePersistsMetadataInConversation() = runTest {
    val convoId = chatRepository.createConversation(
      type = "direct",
      targetUserIds = listOf("peer_2"),
      title = "Peer"
    ).getOrThrow()

    val tempFile = File(context.cacheDir, "test_clip.mp4").apply {
      writeBytes(byteArrayOf(1, 2, 3, 4))
    }
    val prepared = PreparedMediaMetadata(
      mediaId = "m_test_clip",
      mediaType = "video",
      fileName = "test_clip.mp4",
      mimeType = "video/mp4",
      sizeBytes = 4L,
      chunkCount = 1,
      durationMs = 8_000L,
      localFile = tempFile
    )

    val result = chatRepository.sendMediaMessage(
      conversationId = convoId,
      media = prepared,
      caption = "Check this HD video",
      senderName = "Tester"
    )
    assertTrue(result.isSuccess)

    val messages = chatRepository.observeMessages(convoId).first()
    assertEquals(1, messages.size)
    val saved = messages.first()
    assertEquals("video", saved.mediaType)
    assertEquals("m_test_clip", saved.mediaId)
    assertEquals("test_clip.mp4", saved.mediaFileName)
    assertEquals(8_000L, saved.mediaDurationMs)
    assertEquals("Check this HD video", saved.content)

    tempFile.delete()
  }

  @Test
  fun testIncomingMessageNotificationDetectionAndFiltering() {
    val me = "user_me"
    val peer = "user_peer"
    val tsInitial = Timestamp(Date(1_700_000_000_000L))
    val tsNew = Timestamp(Date(1_700_000_050_000L))

    val baselineConvo = Conversation(
      id = "dm_me_peer",
      type = "direct",
      title = "Sarah",
      participantIds = listOf(me, peer),
      lastMessageText = "Old message",
      lastMessageSenderId = peer,
      lastMessageTimestamp = tsInitial
    )

    val lastSeenTimestamps = mutableMapOf<String, Long>()
    val lastSeenSignatures = mutableMapOf<String, String>()

    // First snapshot creates baseline without spamming notifications for old messages
    val firstPass = KoraMessageNotificationManager.detectNewIncomingMessages(
      currentUid = me,
      activeConversationId = null,
      baselineInitialized = false,
      lastSeenTimestamps = lastSeenTimestamps,
      lastSeenSignatures = lastSeenSignatures,
      conversations = listOf(baselineConvo),
      aiConversationId = ChatRepository.AI_CHATBOT_CONVO_ID
    )
    assertTrue(firstPass.isEmpty())

    // New peer message while user is on Feed tab triggers alert
    val updatedConvo = baselineConvo.copy(
      lastMessageText = "مرحباً! كيف حالك؟",
      lastMessageSenderId = peer,
      lastMessageTimestamp = tsNew
    )
    val secondPass = KoraMessageNotificationManager.detectNewIncomingMessages(
      currentUid = me,
      activeConversationId = null,
      baselineInitialized = true,
      lastSeenTimestamps = lastSeenTimestamps,
      lastSeenSignatures = lastSeenSignatures,
      conversations = listOf(updatedConvo),
      aiConversationId = ChatRepository.AI_CHATBOT_CONVO_ID
    )
    assertEquals(1, secondPass.size)
    assertEquals("dm_me_peer", secondPass.first().id)

    // Own reply or muted/currently open conversation must NOT trigger notifications
    val myReplyConvo = updatedConvo.copy(
      lastMessageText = "بخير شكراً",
      lastMessageSenderId = me,
      lastMessageTimestamp = Timestamp(Date(1_700_000_090_000L))
    )
    val thirdPass = KoraMessageNotificationManager.detectNewIncomingMessages(
      currentUid = me,
      activeConversationId = null,
      baselineInitialized = true,
      lastSeenTimestamps = lastSeenTimestamps,
      lastSeenSignatures = lastSeenSignatures,
      conversations = listOf(myReplyConvo),
      aiConversationId = ChatRepository.AI_CHATBOT_CONVO_ID
    )
    assertTrue(thirdPass.isEmpty())

    assertTrue(ArabicStrings.newMessageNotificationTitle.contains("لديك رسالة جديدة"))
    assertTrue(String.format(ArabicStrings.newMessageFromTitle, "سارة").contains("لديك رسالة جديدة"))
    assertTrue(EnglishStrings.newMessageNotificationTitle.contains("new message"))
  }

  @Test
  fun testVoiceNoteAudioAttachmentAndLocalizedUpgrades() {
    val audioMsg = Message(
      id = "aud_1",
      mediaType = "audio",
      mediaId = "m_aud_1",
      mediaDurationMs = 14_000L
    )
    assertTrue(audioMsg.hasMediaAttachment())
    assertFalse(audioMsg.isPollMessage())

    assertTrue(ArabicStrings.inspectProfileAction.isNotBlank())
    assertTrue(ArabicStrings.voiceNoteLabel.isNotBlank())
    assertTrue(ArabicStrings.createPollAction.isNotBlank())
    assertTrue(ArabicStrings.pinMessageAction.isNotBlank())
    assertTrue(ArabicStrings.forwardAction.isNotBlank())
    assertTrue(ArabicStrings.spoilerHiddenTitle.isNotBlank())
    assertTrue(EnglishStrings.inspectProfileAction.isNotBlank())
    assertTrue(EnglishStrings.voiceNoteLabel.isNotBlank())
    assertTrue(EnglishStrings.createPollAction.isNotBlank())
  }
}
