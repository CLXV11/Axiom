package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.repository.ChatRepository
import com.example.data.repository.PostRepository
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ChatRepositoryTest {

  private lateinit var context: Context
  private lateinit var chatRepository: ChatRepository
  private lateinit var postRepository: PostRepository

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
    postRepository = PostRepository(context)
  }

  @Test
  fun testOnlySingleAiChatbotConversationExistsInitially() = runTest {
    val conversations = chatRepository.observeConversations().first()
    assertEquals("Must contain exactly 1 AI chatbot conversation", 1, conversations.size)
    val aiConvo = conversations.first()
    assertEquals(ChatRepository.AI_CHATBOT_CONVO_ID, aiConvo.id)
    assertEquals(ChatRepository.AI_CHATBOT_NAME, aiConvo.title)
    assertTrue(chatRepository.isAiChatbotConversation(aiConvo.id))
  }

  @Test
  fun testAiChatbotStartsWithCleanEmptyMessageStream() = runTest {
    val messages = chatRepository.observeMessages(ChatRepository.AI_CHATBOT_CONVO_ID).first()
    assertTrue("AI chatbot should start clean without fake messages", messages.isEmpty())
  }

  @Test
  fun testPostRepositoryStartsWithoutFakeSeededContent() = runTest {
    val posts = postRepository.observePosts().first()
    val memories = postRepository.observeSavedMemories().first()
    assertTrue("Feed should start with zero fake posts", posts.isEmpty())
    assertTrue("Memory vault should start with zero fake memories", memories.isEmpty())
  }

  @Test
  fun testNonAiConversationDoesNotTriggerFakePeerReplies() = runTest {
    val createdId = chatRepository.createConversation(
      type = "direct",
      targetUserIds = listOf("real_user_2"),
      title = "Peer"
    ).getOrThrow()

    chatRepository.sendMessage(
      conversationId = createdId,
      content = "Hello peer",
      senderName = "Tester"
    )

    val messages = chatRepository.observeMessages(createdId).first()
    assertEquals(1, messages.size)
    assertEquals("Hello peer", messages.first().content)
  }

  @Test
  fun testPollCreateAndVoteToggle() = runTest {
    val convoId = chatRepository.createConversation(
      type = "group",
      targetUserIds = listOf("u2", "u3"),
      title = "Team Sync"
    ).getOrThrow()

    val sendRes = chatRepository.sendPollMessage(
      conversationId = convoId,
      question = "Best launch slot?",
      options = listOf("Morning", "Evening"),
      senderName = "Tester"
    )
    assertTrue(sendRes.isSuccess)

    val pollMsg = chatRepository.observeMessages(convoId).first().single()
    assertEquals("poll", pollMsg.mediaType)
    assertTrue(pollMsg.isPollMessage())
    assertEquals(listOf("Morning", "Evening"), pollMsg.pollOptions)

    chatRepository.votePollOption(convoId, pollMsg.id, 0)
    val afterVote0 = chatRepository.observeMessages(convoId).first().single()
    assertEquals(1, afterVote0.pollVotes["0"]?.size)
    assertTrue(afterVote0.pollVotes["1"].isNullOrEmpty())

    // Switching vote moves user from option 0 to option 1
    chatRepository.votePollOption(convoId, pollMsg.id, 1)
    val afterVote1 = chatRepository.observeMessages(convoId).first().single()
    assertTrue(afterVote1.pollVotes["0"].isNullOrEmpty())
    assertEquals(1, afterVote1.pollVotes["1"]?.size)

    // Tapping option 1 again removes the vote
    chatRepository.votePollOption(convoId, pollMsg.id, 1)
    val afterToggleOff = chatRepository.observeMessages(convoId).first().single()
    assertTrue(afterToggleOff.pollVotes.isEmpty())
  }

  @Test
  fun testPinAndUnpinMessageUpdatesConversationBanner() = runTest {
    val convoId = chatRepository.createConversation(
      type = "direct",
      targetUserIds = listOf("peer_pin"),
      title = "Peer"
    ).getOrThrow()

    chatRepository.sendMessage(
      conversationId = convoId,
      content = "Important meeting link",
      senderName = "Tester"
    )
    val msg = chatRepository.observeMessages(convoId).first().single()

    assertTrue(chatRepository.pinMessageInConversation(convoId, msg).isSuccess)
    val pinnedConvo = chatRepository.observeConversations().first().first { it.id == convoId }
    assertEquals(msg.id, pinnedConvo.pinnedMessageId)
    assertEquals("Important meeting link", pinnedConvo.pinnedMessageText)
    assertEquals("Tester", pinnedConvo.pinnedMessageSender)

    assertTrue(chatRepository.pinMessageInConversation(convoId, null).isSuccess)
    val unpinnedConvo = chatRepository.observeConversations().first().first { it.id == convoId }
    assertEquals(null, unpinnedConvo.pinnedMessageId)
  }

  @Test
  fun testSpoilerAndForwardMessagePreservesOriginAndFlag() = runTest {
    val convoA = chatRepository.createConversation(
      type = "direct",
      targetUserIds = listOf("peer_a"),
      title = "Peer A"
    ).getOrThrow()
    val convoB = chatRepository.createConversation(
      type = "direct",
      targetUserIds = listOf("peer_b"),
      title = "Peer B"
    ).getOrThrow()

    chatRepository.sendMessage(
      conversationId = convoA,
      content = "Secret ending details",
      senderName = "SpoilerSender",
      isSpoiler = true
    )
    val spoilerMsg = chatRepository.observeMessages(convoA).first().single()
    assertTrue(spoilerMsg.isSpoiler)

    val convoASnapshot = chatRepository.observeConversations().first().first { it.id == convoA }
    assertTrue(convoASnapshot.lastMessageText.startsWith("🙈 "))

    val forwardRes = chatRepository.forwardMessage(
      targetConversationId = convoB,
      sourceMessage = spoilerMsg,
      senderName = "Forwarder"
    )
    assertTrue(forwardRes.isSuccess)

    val forwardedMsg = chatRepository.observeMessages(convoB).first().single()
    assertEquals("SpoilerSender", forwardedMsg.forwardedFromName)
    assertEquals("Secret ending details", forwardedMsg.content)
    assertTrue(forwardedMsg.isSpoiler)
  }

  @Test
  fun testTypingIndicatorLocalFallbackFlow() = runTest {
    val convoId = chatRepository.createConversation(
      type = "direct",
      targetUserIds = listOf("peer_typing"),
      title = "Peer"
    ).getOrThrow()

    chatRepository.setTyping(convoId, "Nadia", true)
    assertTrue(chatRepository.observeTyping(convoId).first().contains("Nadia"))

    chatRepository.setTyping(convoId, "Nadia", false)
    assertTrue(chatRepository.observeTyping(convoId).first().isEmpty())
  }
}
