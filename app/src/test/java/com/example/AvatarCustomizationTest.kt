package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.repository.UserRepository
import com.example.ui.components.PresetAvatars
import com.example.ui.util.PreferencesManager
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AvatarCustomizationTest {

  private lateinit var context: Context
  private lateinit var preferencesManager: PreferencesManager
  private lateinit var userRepository: UserRepository

  @Before
  fun setUp() {
    context = ApplicationProvider.getApplicationContext()
    if (FirebaseApp.getApps(context).isEmpty()) {
      FirebaseApp.initializeApp(
        context,
        FirebaseOptions.Builder()
          .setApplicationId("com.aistudio.kora.network")
          .setProjectId("kora-test")
          .setApiKey("fake-api-key-for-unit-test")
          .build()
      )
    }
    preferencesManager = PreferencesManager(context)
    userRepository = UserRepository(context)
  }

  @Test
  fun testPresetAvatarsListAndLookup() {
    val presets = PresetAvatars.list
    assertEquals(8, presets.size)

    val bot = PresetAvatars.findById("preset:cyber_bot")
    assertNotNull(bot)
    assertEquals("Cyber Bot", bot?.nameEn)
    assertEquals("الروبوت السيبراني", bot?.nameAr)
    assertTrue(bot!!.gradient.size >= 2)

    val caseInsensitive = PresetAvatars.findById("PRESET:NEON_FOX")
    assertNotNull(caseInsensitive)
    assertEquals("Neon Fox", caseInsensitive?.nameEn)

    val nonExistent = PresetAvatars.findById("unknown_preset")
    assertNull(nonExistent)
  }

  @Test
  fun testPreferencesManagerAvatarPersistence() {
    preferencesManager.guestAvatarUrl = "preset:royal_crown"
    assertEquals("preset:royal_crown", preferencesManager.guestAvatarUrl)

    preferencesManager.guestAvatarUrl = "https://example.com/custom_avatar.png"
    assertEquals("https://example.com/custom_avatar.png", preferencesManager.guestAvatarUrl)

    preferencesManager.guestAvatarUrl = ""
    assertEquals("", preferencesManager.guestAvatarUrl)
  }

  @Test
  fun testUserRepositoryUpdateAvatarUrl() = runTest {
    val uid = userRepository.requireUserId()

    // Update to preset avatar
    val result1 = userRepository.updateAvatarUrl("preset:astro_voyager")
    assertTrue(result1.isSuccess)

    val profile = userRepository.observeUserProfile(uid).first { it != null }
    assertEquals("preset:astro_voyager", profile?.avatarUrl)
    assertEquals("preset:astro_voyager", preferencesManager.guestAvatarUrl)

    // Update to custom URI or empty
    val result2 = userRepository.updateAvatarUrl("content://media/picker/123")
    assertTrue(result2.isSuccess)

    val updatedProfile = userRepository.observeUserProfile(uid).first { it?.avatarUrl == "content://media/picker/123" }
    assertEquals("content://media/picker/123", updatedProfile?.avatarUrl)
  }
}
