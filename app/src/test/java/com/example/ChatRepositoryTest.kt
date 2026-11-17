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
}
