package com.example

import org.junit.Assert.*
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testVerificationBadgeModels() {
    val user = com.example.data.model.UserProfile(
      id = "test_1",
      displayName = "Verified Lead",
      isVerified = true
    )
    assertTrue("User should be verified", user.isVerified)
    assertEquals(true, user.toMap()["isVerified"])

    val post = com.example.data.model.Post(
      id = "post_1",
      authorName = "Sophia Zhang",
      isAuthorVerified = true
    )
    assertTrue("Post author should be verified", post.isAuthorVerified)
    assertEquals(true, post.toMap()["isAuthorVerified"])

    val convo = com.example.data.model.Conversation(
      id = "c_1",
      title = "Official Team",
      isVerified = true
    )
    assertTrue("Conversation should be verified", convo.isVerified)
    assertEquals(true, convo.toMap()["isVerified"])

    val msg = com.example.data.model.Message(
      id = "m_1",
      senderName = "Sophia",
      isSenderVerified = true
    )
    assertTrue("Message sender should be verified", msg.isSenderVerified)
    assertEquals(true, msg.toMap()["isSenderVerified"])
  }

  @Test
  fun testTypingLocalizationStrings() {
    val en = com.example.ui.util.EnglishStrings
    val ar = com.example.ui.util.ArabicStrings

    assertTrue(en.isTyping.isNotBlank())
    assertTrue(en.typingSingle.contains("%s"))
    assertTrue(ar.isTyping.contains("يكتب"))
    assertTrue(ar.typingSingle.contains("%s"))

    val enFormatted = String.format(en.typingSingle, "Elena")
    assertEquals("Elena is typing...", enFormatted)

    val arFormatted = String.format(ar.typingSingle, "طارق")
    assertEquals("طارق يكتب الآن...", arFormatted)
  }

  @Test
  fun testVerificationTiers() {
    val tiers = com.example.ui.components.VerificationTier.values()
    assertEquals(7, tiers.size)

    val gold = com.example.ui.components.VerificationTier.fromId("GOLD")
    assertEquals(com.example.ui.components.VerificationTier.GOLD, gold)
    assertTrue(gold.gradientColors.size >= 3)
    assertTrue(gold.titleAr.contains("الذهب"))

    val onyx = com.example.ui.components.VerificationTier.fromId("onyx")
    assertEquals(com.example.ui.components.VerificationTier.ONYX, onyx)

    val emerald = com.example.ui.components.VerificationTier.fromId("EMERALD")
    assertEquals(com.example.ui.components.VerificationTier.EMERALD, emerald)

    val amethyst = com.example.ui.components.VerificationTier.fromId("AMETHYST")
    assertEquals(com.example.ui.components.VerificationTier.AMETHYST, amethyst)

    val roseGold = com.example.ui.components.VerificationTier.fromId("ROSE_GOLD")
    assertEquals(com.example.ui.components.VerificationTier.ROSE_GOLD, roseGold)

    val platinum = com.example.ui.components.VerificationTier.fromId("PLATINUM")
    assertEquals(com.example.ui.components.VerificationTier.PLATINUM, platinum)
  }
}
