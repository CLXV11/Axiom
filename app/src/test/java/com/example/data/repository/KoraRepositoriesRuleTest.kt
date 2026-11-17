package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.UserProfile
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class KoraRepositoriesRuleTest : FirestoreEmulatorTestBase() {

  @Test
  fun userProfile_createDiscoverUpdateAndCrossUserIsolation() = runBlocking {
    val suffix = UUID.randomUUID().toString().take(6)
    val aliceUid = signInTestUser("alice_$suffix@kora.test")
    val aliceRepo = UserRepository(firestore, null, auth)

    // Initially missing doc returns null (triggers Profile Setup in AuthScreen)
    val initial = withTimeout(TIMEOUT_MS) { aliceRepo.getUserProfile(aliceUid) }
    assertNull("New auth user without Firestore doc must return null profile", initial)

    val createRes = withTimeout(TIMEOUT_MS) {
      aliceRepo.createOrUpdateProfile(
        UserProfile(
          id = aliceUid,
          displayName = "Alice Engineer",
          handle = "@alice_$suffix",
          bio = "Building Kora",
          profession = "Android Architect",
          openToNetworking = true,
          avatarUrl = "preset:cyber_bot"
        )
      )
    }
    assertTrue("Alice profile create failed: ${createRes.exceptionOrNull()}", createRes.isSuccess)

    // Verify @DocumentId deserializes cleanly without 'id' field crash
    val fetched = withTimeout(TIMEOUT_MS) { aliceRepo.getUserProfile(aliceUid) }
    assertNotNull(fetched)
    assertEquals(aliceUid, fetched!!.id)
    assertEquals("Alice Engineer", fetched.displayName)
    assertEquals("Android Architect", fetched.profession)

    // Switch to Bob: Bob discovers Alice in directory and cannot modify Alice's profile
    val bobUid = signInTestUser("bob_$suffix@kora.test")
    val bobRepo = UserRepository(firestore, null, auth)

    val directory = withTimeout(TIMEOUT_MS) { bobRepo.searchDirectoryUsers("Android Architect") }
    assertTrue("Bob must discover Alice by profession", directory.any { it.id == aliceUid })

    // Negative test: Bob cannot tamper with Alice's doc
    val tamperResult = runCatching {
      withTimeout(TIMEOUT_MS) {
        firestore.collection("users").document(aliceUid)
          .update(mapOf("displayName" to "Hacked", "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()))
          .await()
      }
    }
    val ex = tamperResult.exceptionOrNull()?.unwrapFirestoreException()
    assertNotNull("Expected PERMISSION_DENIED when Bob updates Alice", ex)
    assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, ex!!.code)
  }

  @Test
  fun directChat_twoFriendsExchangeMessagesPinReadAndNonParticipantBlocked() = runBlocking {
    val suffix = UUID.randomUUID().toString().take(6)
    val aliceUid = signInTestUser("alice_chat_$suffix@kora.test")
    val bobUid = signInTestUser("bob_chat_$suffix@kora.test")
    val eveUid = signInTestUser("eve_chat_$suffix@kora.test")

    // Sign back in as Alice and create DM with Bob
    signInTestUser("alice_chat_$suffix@kora.test")
    val aliceChatRepo = ChatRepository(firestore, null, AiChatService(), auth)

    val convoId1 = withTimeout(TIMEOUT_MS) {
      aliceChatRepo.findOrCreateDirectConversation(
        targetUserId = bobUid,
        title = "Bob"
      ).getOrThrow()
    }
    assertTrue(convoId1.isNotBlank())

    // Duplicate DM check returns the exact same conversationId
    val convoId2 = withTimeout(TIMEOUT_MS) {
      aliceChatRepo.findOrCreateDirectConversation(
        targetUserId = bobUid,
        title = "Bob"
      ).getOrThrow()
    }
    assertEquals("Direct chat must not duplicate existing thread", convoId1, convoId2)

    val sendRes = withTimeout(TIMEOUT_MS) {
      aliceChatRepo.sendMessage(
        conversationId = convoId1,
        content = "Hey Bob, can you see this?",
        senderName = "Alice"
      )
    }
    assertTrue("Alice send message failed: ${sendRes.exceptionOrNull()}", sendRes.isSuccess)

    // Sign in as Bob: Bob sees the conversation, reads the message, marks as read, pins, and replies
    signInTestUser("bob_chat_$suffix@kora.test")
    val bobChatRepo = ChatRepository(firestore, null, AiChatService(), auth)

    val bobConvos = withTimeout(FLOW_TIMEOUT_MS) {
      bobChatRepo.observeConversations().first { list -> list.any { it.id == convoId1 } }
    }
    assertTrue(bobConvos.any { it.id == convoId1 && it.lastMessageText.contains("Hey Bob") })

    val bobMessages = withTimeout(FLOW_TIMEOUT_MS) {
      bobChatRepo.observeMessages(convoId1).first { it.isNotEmpty() }
    }
    assertEquals(1, bobMessages.size)
    val aliceMsgId = bobMessages.first().id

    val readRes = withTimeout(TIMEOUT_MS) { bobChatRepo.markAsRead(convoId1) }
    assertTrue("Bob markAsRead failed: ${readRes.exceptionOrNull()}", readRes.isSuccess)

    val pinRes = withTimeout(TIMEOUT_MS) { bobChatRepo.togglePinConversation(convoId1, false) }
    assertTrue("Bob pin failed: ${pinRes.exceptionOrNull()}", pinRes.isSuccess)

    // Negative: Bob cannot edit Alice's message content
    val editOtherRes = withTimeout(TIMEOUT_MS) {
      bobChatRepo.editMessage(convoId1, aliceMsgId, "Edited by Bob")
    }
    assertFalse("Bob must not edit Alice's message", editOtherRes.isSuccess)
    assertEquals(
      FirebaseFirestoreException.Code.PERMISSION_DENIED,
      editOtherRes.exceptionOrNull()?.unwrapFirestoreException()?.code
    )

    // Bob replies
    val replyRes = withTimeout(TIMEOUT_MS) {
      bobChatRepo.sendMessage(
        conversationId = convoId1,
        content = "Loud and clear Alice!",
        senderName = "Bob"
      )
    }
    assertTrue("Bob reply failed: ${replyRes.exceptionOrNull()}", replyRes.isSuccess)

    // Sign in as Eve (outsider): cannot write message or read conversation
    signInTestUser("eve_chat_$suffix@kora.test")
    assertNotNull(eveUid)
    val eveChatRepo = ChatRepository(firestore, null, AiChatService(), auth)
    val eveSendRes = withTimeout(TIMEOUT_MS) {
      eveChatRepo.sendMessage(
        conversationId = convoId1,
        content = "Intruder message",
        senderName = "Eve"
      )
    }
    assertFalse("Outsider Eve must be denied writing to Alice+Bob chat", eveSendRes.isSuccess)
    assertEquals(
      FirebaseFirestoreException.Code.PERMISSION_DENIED,
      eveSendRes.exceptionOrNull()?.unwrapFirestoreException()?.code
    )

    val eveReadRes = runCatching {
      withTimeout(TIMEOUT_MS) {
        firestore.collection("conversations").document(convoId1).get().await()
      }
    }
    assertEquals(
      FirebaseFirestoreException.Code.PERMISSION_DENIED,
      eveReadRes.exceptionOrNull()?.unwrapFirestoreException()?.code
    )
  }

  @Test
  fun feedPostsAndSavedMemories_privacyAndUnauthenticatedDenial() = runBlocking {
    val suffix = UUID.randomUUID().toString().take(6)
    val aliceUid = signInTestUser("alice_post_$suffix@kora.test")
    val alicePostRepo = PostRepository(firestore, null, auth)

    val createPostRes = withTimeout(TIMEOUT_MS) {
      alicePostRepo.createPost(
        content = "Real cloud post #$suffix",
        audience = "public",
        authorName = "Alice",
        authorHandle = "@alice"
      )
    }
    assertTrue("Create post failed: ${createPostRes.exceptionOrNull()}", createPostRes.isSuccess)

    val posts = withTimeout(FLOW_TIMEOUT_MS) {
      alicePostRepo.observePosts("all").first { list -> list.any { it.content.contains(suffix) } }
    }
    val post = posts.first { it.content.contains(suffix) }

    // Alice saves private memory
    val memRes = withTimeout(TIMEOUT_MS) {
      alicePostRepo.saveToMemory(
        messageId = post.id,
        snippet = "Important note #$suffix",
        senderName = "Alice",
        note = "Private vault note"
      )
    }
    assertTrue("Save memory failed: ${memRes.exceptionOrNull()}", memRes.isSuccess)

    // Bob likes and comments on post, but cannot read Alice's saved memories
    signInTestUser("bob_post_$suffix@kora.test")
    val bobPostRepo = PostRepository(firestore, null, auth)
    assertTrue(withTimeout(TIMEOUT_MS) { bobPostRepo.toggleLikePost(post.id, emptyList()) }.isSuccess)
    assertTrue(
      withTimeout(TIMEOUT_MS) {
        bobPostRepo.addComment(post.id, "Great insight!", "Bob", "@bob")
      }.isSuccess
    )

    val bobReadsAliceMem = runCatching {
      withTimeout(TIMEOUT_MS) {
        firestore.collection("users").document(aliceUid).collection("saved_memories").get().await()
      }
    }
    assertEquals(
      FirebaseFirestoreException.Code.PERMISSION_DENIED,
      bobReadsAliceMem.exceptionOrNull()?.unwrapFirestoreException()?.code
    )

    // Unauthenticated user cannot list protected collections
    auth.signOut()
    val unauthUsers = runCatching {
      withTimeout(TIMEOUT_MS) {
        firestore.collection("users").limit(5).get().await()
      }
    }
    assertEquals(
      FirebaseFirestoreException.Code.PERMISSION_DENIED,
      unauthUsers.exceptionOrNull()?.unwrapFirestoreException()?.code
    )

    val unauthConvos = runCatching {
      withTimeout(TIMEOUT_MS) {
        firestore.collection("conversations").whereArrayContains("participantIds", aliceUid).get().await()
      }
    }
    assertEquals(
      FirebaseFirestoreException.Code.PERMISSION_DENIED,
      unauthConvos.exceptionOrNull()?.unwrapFirestoreException()?.code
    )
  }

  private companion object {
    const val TIMEOUT_MS = 5000L
    const val FLOW_TIMEOUT_MS = 4000L
  }
}
