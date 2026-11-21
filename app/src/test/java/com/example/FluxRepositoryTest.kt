package com.example

import androidx.test.core.app.ApplicationProvider
import com.example.data.repository.FluxRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class FluxRepositoryTest {

  private lateinit var fluxRepository: FluxRepository

  @Before
  fun setup() {
    val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    context.getSharedPreferences("flux_storage_prefs", android.content.Context.MODE_PRIVATE)
      .edit()
      .clear()
      .commit()
    fluxRepository = FluxRepository(context)
  }

  @Test
  fun testInitialVideos_emptyWhenNoUserPublishedClipsExist() = runTest {
    val videos = fluxRepository.observeVideos().first()
    assertTrue("Flux feed should contain no fake seeded videos initially", videos.isEmpty())
  }

  @Test
  fun testPublishFlux_addsVideo() = runTest {
    val newVideo = fluxRepository.publishFlux(
      authorId = "test_user_1",
      authorName = "GamerOne",
      authorHandle = "@gamerone",
      isAuthorVerified = true,
      authorVerifiedTier = "ONYX",
      caption = "Check out this insane play! #clutch #esports",
      videoUri = "content://media/external/video/media/123",
      audioTitle = "Epic Gaming Synth",
      gradientTheme = "NITRO"
    )

    assertNotNull(newVideo.id)
    assertEquals("GamerOne", newVideo.authorName)
    assertEquals("ONYX", newVideo.authorVerifiedTier)
    assertTrue("Should extract #clutch tag", newVideo.tags.contains("#clutch"))

    val updatedVideos = fluxRepository.observeVideos().first()
    assertEquals(1, updatedVideos.size)
    assertEquals(newVideo.id, updatedVideos.first().id)
  }

  @Test
  fun testToggleLike_updatesCountAndLikedBy() = runTest {
    val target = fluxRepository.publishFlux(
      authorId = "author_1",
      authorName = "Creator",
      authorHandle = "@creator",
      isAuthorVerified = false,
      authorVerifiedTier = "GOLD",
      caption = "Test clip #flux",
      videoUri = null,
      audioTitle = "Sound"
    )
    val initialLikes = target.likesCount
    val userId = "tester_101"

    fluxRepository.toggleLike(target.id, userId)
    val afterLike = fluxRepository.observeVideos().first().first { it.id == target.id }
    assertTrue(afterLike.likedBy.contains(userId))
    assertEquals(initialLikes + 1, afterLike.likesCount)

    fluxRepository.toggleLike(target.id, userId)
    val afterUnlike = fluxRepository.observeVideos().first().first { it.id == target.id }
    assertFalse(afterUnlike.likedBy.contains(userId))
    assertEquals(initialLikes, afterUnlike.likesCount)
  }

  @Test
  fun testToggleSave_updatesSavedList() = runTest {
    val target = fluxRepository.publishFlux(
      authorId = "author_1",
      authorName = "Creator",
      authorHandle = "@creator",
      isAuthorVerified = false,
      authorVerifiedTier = "GOLD",
      caption = "Saveable clip #flux",
      videoUri = null,
      audioTitle = "Sound"
    )
    val userId = "bookmark_user"

    fluxRepository.toggleSave(target.id, userId)
    val afterSave = fluxRepository.observeVideos().first().first { it.id == target.id }
    assertTrue(afterSave.savedBy.contains(userId))

    fluxRepository.toggleSave(target.id, userId)
    val afterUnsave = fluxRepository.observeVideos().first().first { it.id == target.id }
    assertFalse(afterUnsave.savedBy.contains(userId))
  }

  @Test
  fun testAddComment_updatesCommentsCount() = runTest {
    val target = fluxRepository.publishFlux(
      authorId = "author_1",
      authorName = "Creator",
      authorHandle = "@creator",
      isAuthorVerified = false,
      authorVerifiedTier = "GOLD",
      caption = "Commentable clip #flux",
      videoUri = null,
      audioTitle = "Sound"
    )
    val initialComments = target.commentsCount

    val comment = fluxRepository.addComment(
      fluxId = target.id,
      authorId = "commenter_1",
      authorName = "SuperFan",
      authorHandle = "@superfan",
      isAuthorVerified = true,
      authorVerifiedTier = "EMERALD",
      content = "Best Flux clip ever!"
    )

    assertNotNull(comment.id)
    assertEquals("Best Flux clip ever!", comment.content)

    val updatedTarget = fluxRepository.observeVideos().first().first { it.id == target.id }
    assertEquals(initialComments + 1, updatedTarget.commentsCount)
  }

  @Test
  fun testPublishFlux_extractsArabicAndUnicodeHashtags() = runTest {
    val arabicVideo = fluxRepository.publishFlux(
      authorId = "ar_creator_1",
      authorName = "سامي",
      authorHandle = "@sami_flux",
      isAuthorVerified = true,
      authorVerifiedTier = "BLUE",
      caption = "تجربة رائعة في #فلوكس #تطبيق_كورا #Kora2026",
      videoUri = "content://media/external/video/media/777",
      audioTitle = "Original Sound"
    )

    assertTrue("Should extract #فلوكس", arabicVideo.tags.contains("#فلوكس"))
    assertTrue("Should extract #تطبيق_كورا", arabicVideo.tags.contains("#تطبيق_كورا"))
    assertTrue("Should extract #kora2026", arabicVideo.tags.contains("#kora2026"))
  }

  @Test
  fun testToggleFluxReaction_updatesAndTogglesReactions() = runTest {
    val clip = fluxRepository.publishFlux(
      authorId = "author_r1",
      authorName = "Creator",
      authorHandle = "@creator",
      isAuthorVerified = false,
      authorVerifiedTier = "BLUE",
      caption = "React to this #flux",
      videoUri = null,
      audioTitle = "Sound"
    )

    fluxRepository.toggleFluxReaction(clip.id, "🔥", "viewer_1").getOrThrow()
    fluxRepository.toggleFluxReaction(clip.id, "❤️", "viewer_2").getOrThrow()
    val afterReact = fluxRepository.observeVideos().first().first { it.id == clip.id }
    assertEquals(listOf("viewer_1"), afterReact.reactions["🔥"])
    assertEquals(listOf("viewer_2"), afterReact.reactions["❤️"])

    fluxRepository.toggleFluxReaction(clip.id, "🔥", "viewer_1").getOrThrow()
    val afterToggleOff = fluxRepository.observeVideos().first().first { it.id == clip.id }
    assertFalse(afterToggleOff.reactions.containsKey("🔥"))
    assertEquals(listOf("viewer_2"), afterToggleOff.reactions["❤️"])
  }

  @Test
  fun testUpdateAndDeleteFluxVideo_updatesCaptionAndRemovesClip() = runTest {
    val clip = fluxRepository.publishFlux(
      authorId = "author_edit_1",
      authorName = "Creator",
      authorHandle = "@creator",
      isAuthorVerified = false,
      authorVerifiedTier = "BLUE",
      caption = "Initial caption #one",
      videoUri = null,
      audioTitle = "Track One"
    )

    fluxRepository.updateFluxVideo(clip.id, "Updated caption #two #vibes", "Track Two").getOrThrow()
    val updated = fluxRepository.observeVideos().first().first { it.id == clip.id }
    assertEquals("Updated caption #two #vibes", updated.caption)
    assertEquals("Track Two", updated.audioTitle)
    assertTrue(updated.tags.contains("#two"))
    assertTrue(updated.tags.contains("#vibes"))

    fluxRepository.deleteFluxVideo(clip.id).getOrThrow()
    val remaining = fluxRepository.observeVideos().first()
    assertTrue(remaining.none { it.id == clip.id })
  }
}
