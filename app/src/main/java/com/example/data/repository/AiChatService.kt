package com.example.data.repository

import android.util.Log
import com.example.data.model.Message
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Content
import com.google.firebase.ai.type.content
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeout

private const val TAG = "AiChatService"
private const val AI_MODEL_NAME = "gemini-3.8-flash"
private const val AI_TIMEOUT_MS = 60_000L

/**
 * Provides genuine streaming responses for Kora's single AI Assistant using Firebase AI Logic.
 * No hardcoded or simulated fallback strings are returned on error.
 */
class AiChatService {

  private val model by lazy {
    Firebase.ai.generativeModel(
      modelName = AI_MODEL_NAME,
      systemInstruction = content {
        text(
          "You are Kora AI (مساعد كورا الذكي), the dedicated intelligent assistant inside Kora. " +
            "Respond naturally in the user's language (Arabic or English). " +
            "Be calm, concise, well-structured, accurate, and genuinely helpful."
        )
      }
    )
  }

  /**
   * Streams assistant tokens using multi-turn conversation history.
   * Emits accumulated partial text chunks and enforces a 60-second timeout.
   */
  fun streamReply(
    priorMessages: List<Message>,
    userPrompt: String,
    aiSenderId: String
  ): Flow<String> = flow {
    val history: List<Content> = priorMessages
      .filter { !it.isDeleted && it.content.isNotBlank() }
      .takeLast(20)
      .map { msg ->
        val role = if (msg.senderId == aiSenderId) "model" else "user"
        content(role) { text(msg.content) }
      }

    val chat = model.startChat(history = history)
    val accumulated = StringBuilder()

    try {
      withTimeout(AI_TIMEOUT_MS) {
        chat.sendMessageStream(userPrompt).collect { chunk ->
          val chunkText = chunk.text.orEmpty()
          if (chunkText.isNotEmpty()) {
            accumulated.append(chunkText)
            emit(accumulated.toString())
          }
        }
      }
      if (accumulated.isBlank()) {
        throw IllegalStateException("AI assistant returned an empty response.")
      }
    } catch (e: TimeoutCancellationException) {
      Log.e(TAG, "AI streaming timed out after 60s", e)
      throw IllegalStateException("Request timed out after 60 seconds. Please try again.", e)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      Log.e(TAG, "Firebase AI chat failed", e)
      throw e
    }
  }
}
