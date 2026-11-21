package com.example

import com.example.data.model.Message
import com.example.ui.components.buildChatDisplayItems
import com.example.ui.components.countPureJumboEmojis
import com.example.ui.util.ArabicStrings
import com.example.ui.util.EnglishStrings
import com.example.ui.util.formatHandleForDisplay
import com.google.firebase.Timestamp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ChatBubbleLayoutTest {

  private fun timestampAt(daysOffset: Int, hour: Int, minute: Int): Timestamp {
    val cal = Calendar.getInstance().apply {
      add(Calendar.DAY_OF_YEAR, daysOffset)
      set(Calendar.HOUR_OF_DAY, hour)
      set(Calendar.MINUTE, minute)
      set(Calendar.SECOND, 0)
      set(Calendar.MILLISECOND, 0)
    }
    return Timestamp(Date(cal.timeInMillis))
  }

  @Test
  fun countPureJumboEmojis_acceptsOneToThreePureEmojisAndRejectsTextOrOverflow() {
    assertEquals(1, countPureJumboEmojis("🗿"))
    assertEquals(1, countPureJumboEmojis("  🗿  "))
    assertEquals(3, countPureJumboEmojis("😂😂😂"))
    assertEquals(2, countPureJumboEmojis("❤️🔥"))
    assertEquals(0, countPureJumboEmojis("😂😂😂😂"))
    assertEquals(0, countPureJumboEmojis("Hello"))
    assertEquals(0, countPureJumboEmojis("😂 hello"))
    assertEquals(0, countPureJumboEmojis("123"))
    assertEquals(0, countPureJumboEmojis(""))
    assertEquals(0, countPureJumboEmojis("   "))
  }

  @Test
  fun buildChatDisplayItems_groupsSameSenderWithinThreeMinutesAndSplitsAcrossDaysOrSenders() {
    val twoDaysAgo = timestampAt(daysOffset = -2, hour = 14, minute = 10)
    val yesterday = timestampAt(daysOffset = -1, hour = 11, minute = 0)
    val today1 = timestampAt(daysOffset = 0, hour = 10, minute = 0)
    val today2 = timestampAt(daysOffset = 0, hour = 10, minute = 2)
    val today3 = timestampAt(daysOffset = 0, hour = 10, minute = 8) // > 3 min gap
    val today4 = timestampAt(daysOffset = 0, hour = 10, minute = 9) // different sender

    val messages = listOf(
      Message(id = "m0", senderId = "u1", senderName = "Ali", content = "Old", createdAt = twoDaysAgo),
      Message(id = "m1", senderId = "u1", senderName = "Ali", content = "🗿", createdAt = yesterday),
      Message(id = "m2", senderId = "u1", senderName = "Ali", content = "Hey", createdAt = today1),
      Message(id = "m3", senderId = "u1", senderName = "Ali", content = "How are you?", createdAt = today2),
      Message(id = "m4", senderId = "u1", senderName = "Ali", content = "Still here", createdAt = today3),
      Message(id = "m5", senderId = "u2", senderName = "Sara", content = "😂😂😂", createdAt = today4)
    )

    val items = buildChatDisplayItems(
      messages = messages,
      todayLabel = EnglishStrings.chatDateToday,
      yesterdayLabel = EnglishStrings.chatDateYesterday,
      locale = Locale.US
    )

    assertEquals(6, items.size)

    // Day boundaries
    assertTrue(items[0].showDateDivider)
    assertTrue(items[1].showDateDivider)
    assertEquals("Yesterday", items[1].dateLabel)
    assertTrue(items[2].showDateDivider)
    assertEquals("Today", items[2].dateLabel)
    assertFalse(items[3].showDateDivider)
    assertFalse(items[4].showDateDivider)
    assertFalse(items[5].showDateDivider)

    // Grouping within 3 minutes
    assertTrue(items[2].isFirstInGroup)
    assertFalse(items[2].isLastInGroup)
    assertFalse(items[3].isFirstInGroup)
    assertTrue(items[3].isLastInGroup)

    // Split after 6 minute gap
    assertTrue(items[4].isFirstInGroup)
    assertTrue(items[4].isLastInGroup)

    // Split on sender switch & jumbo count check
    assertTrue(items[5].isFirstInGroup)
    assertTrue(items[5].isLastInGroup)
    assertTrue(items[1].isJumboEmoji)
    assertEquals(1, items[1].jumboEmojiCount)
    assertTrue(items[5].isJumboEmoji)
    assertEquals(3, items[5].jumboEmojiCount)
  }

  @Test
  fun buildChatDisplayItems_suppressesJumboEmojiForRepliesSpoilersDeletedOrMedia() {
    val now = timestampAt(daysOffset = 0, hour = 12, minute = 0)
    val messages = listOf(
      Message(id = "a", senderId = "u1", content = "🗿", isDeleted = true, createdAt = now),
      Message(id = "b", senderId = "u1", content = "🗿", isSpoiler = true, createdAt = now),
      Message(id = "c", senderId = "u1", content = "🗿", replyToText = "Hi", createdAt = now),
      Message(id = "d", senderId = "u1", content = "🗿", forwardedFromName = "Noor", createdAt = now),
      Message(id = "e", senderId = "u1", content = "🗿", mediaType = "image", mediaId = "img1", createdAt = now)
    )

    val items = buildChatDisplayItems(messages, ArabicStrings.chatDateToday, ArabicStrings.chatDateYesterday)
    items.forEach { item ->
      assertFalse("Expected jumbo disabled for ${item.message.id}", item.isJumboEmoji)
    }
  }

  @Test
  fun dateStringsAndHandleFormattingAreValidInArabicAndEnglish() {
    assertEquals("Today", EnglishStrings.chatDateToday)
    assertEquals("Yesterday", EnglishStrings.chatDateYesterday)
    assertEquals("اليوم", ArabicStrings.chatDateToday)
    assertEquals("أمس", ArabicStrings.chatDateYesterday)
    assertEquals("\u2066@jmpjonv\u2069", formatHandleForDisplay("jmpjonv"))
  }
}
