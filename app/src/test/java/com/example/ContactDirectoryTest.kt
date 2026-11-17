package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.UserProfile
import com.example.data.repository.UserRepository
import com.example.ui.components.DefaultProfessionCategories
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
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
class ContactDirectoryTest {

  private lateinit var context: Context
  private lateinit var userRepository: UserRepository

  private val sampleDirectory = listOf(
    UserProfile(
      id = "u_1",
      displayName = "Sophia Zhang",
      handle = "@sophia_ux",
      profession = "Principal Systems Architect",
      isProfessionalVerified = true,
      professionalOrg = "ACM Distinguished Engineer",
      openToNetworking = true,
      bio = "Systems Architect",
      isVerified = true,
      verifiedTier = "AMETHYST"
    ),
    UserProfile(
      id = "u_2",
      displayName = "طارق منصور",
      handle = "@tariq_dev",
      profession = "Cloud Infrastructure Engineer",
      isProfessionalVerified = true,
      professionalOrg = "Cloud Fellow",
      openToNetworking = true,
      bio = "مهندس بنية تحتية سحابية",
      isVerified = true,
      verifiedTier = "ONYX"
    ),
    UserProfile(
      id = "u_3",
      displayName = "Elena Rostova",
      handle = "@elena_motion",
      profession = "Motion Designer",
      isProfessionalVerified = true,
      professionalOrg = "Design Guild",
      openToNetworking = true,
      bio = "Fluid interface design",
      isVerified = true,
      verifiedTier = "ROSE_GOLD"
    ),
    UserProfile(
      id = "u_4",
      displayName = "Liam Vance",
      handle = "@liam_sec",
      profession = "Security Researcher",
      isProfessionalVerified = true,
      professionalOrg = "Security Institute",
      openToNetworking = true,
      bio = "Zero-trust researcher",
      isVerified = true,
      verifiedTier = "ONYX"
    ),
    UserProfile(
      id = "u_alex",
      displayName = "Alexandre Chen",
      handle = "@alex_code",
      profession = "Full Stack Engineer",
      isProfessionalVerified = false,
      professionalOrg = "Independent Developer",
      openToNetworking = true,
      bio = "Reactive apps",
      isVerified = false
    )
  )

  @Before
  fun setUp() {
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
    userRepository = UserRepository(context)
  }

  @Test
  fun testDefaultDirectoryUsersIsEmptyWithoutFakeContacts() {
    assertTrue("Directory should have zero fake contacts by default", userRepository.getDirectoryUsers().isEmpty())
  }

  @Test
  fun testDefaultProfessionCategories() {
    assertTrue(DefaultProfessionCategories.isNotEmpty())
    val allCategory = DefaultProfessionCategories.find { it.id == "all" }
    assertNotNull(allCategory)
    assertEquals("All", allCategory?.labelEn)
    assertEquals("الكل", allCategory?.labelAr)

    val verifiedProCategory = DefaultProfessionCategories.find { it.id == "verified_pro" }
    assertNotNull(verifiedProCategory)
    assertEquals("Verified Pros", verifiedProCategory?.labelEn)
    assertEquals("محترفون موثقون", verifiedProCategory?.labelAr)
    assertEquals("verified_pro", verifiedProCategory?.filterKeyword)

    val engineering = DefaultProfessionCategories.find { it.id == "engineering" }
    assertNotNull(engineering)
    assertEquals("engineer", engineering?.filterKeyword)
  }

  @Test
  fun testFilterByName() = runTest {
    val sophiaResults = userRepository.filterDirectoryUsers(sampleDirectory, query = "Sophia")
    assertTrue(sophiaResults.any { it.displayName.contains("Sophia", ignoreCase = true) })

    val tariqResults = userRepository.filterDirectoryUsers(sampleDirectory, query = "طارق")
    assertTrue(tariqResults.any { it.displayName.contains("طارق") })
  }

  @Test
  fun testFilterByProfession() = runTest {
    val architectResults = userRepository.filterDirectoryUsers(sampleDirectory, query = "Architect")
    assertEquals(1, architectResults.size)

    val securityResults = userRepository.filterDirectoryUsers(sampleDirectory, query = "Security")
    assertEquals(1, securityResults.size)

    val motionResults = userRepository.filterDirectoryUsers(sampleDirectory, query = "Motion")
    assertEquals(1, motionResults.size)
  }

  @Test
  fun testFilterByProfessionCategoryChip() = runTest {
    val engResults = userRepository.filterDirectoryUsers(sampleDirectory, professionCategory = "engineer")
    assertTrue(engResults.isNotEmpty())
    assertTrue(engResults.all { it.profession.contains("engineer", ignoreCase = true) || it.bio.contains("engineer", ignoreCase = true) })

    val designResults = userRepository.filterDirectoryUsers(sampleDirectory, professionCategory = "design")
    assertTrue(designResults.isNotEmpty())
    assertTrue(designResults.all { it.profession.contains("design", ignoreCase = true) || it.bio.contains("design", ignoreCase = true) })

    val proResults = userRepository.filterDirectoryUsers(sampleDirectory, professionCategory = "verified_pro")
    assertTrue(proResults.isNotEmpty())
    assertTrue(proResults.all { it.isProfessionalVerified })
    assertFalse(proResults.any { it.id == "u_alex" })
  }

  @Test
  fun testFilterVerifiedProfessionalOnlyFlag() = runTest {
    val proOnly = userRepository.filterDirectoryUsers(sampleDirectory, verifiedProOnly = true)
    assertTrue(proOnly.isNotEmpty())
    assertTrue(proOnly.all { it.isProfessionalVerified })
    assertFalse(proOnly.any { it.id == "u_alex" })
  }

  @Test
  fun testCombinedNameAndProfessionFilter() = runTest {
    val results = userRepository.filterDirectoryUsers(
      users = sampleDirectory,
      query = "Elena",
      professionCategory = "design"
    )
    assertEquals(1, results.size)
    assertEquals("Elena Rostova", results.first().displayName)

    val emptyResults = userRepository.filterDirectoryUsers(
      users = sampleDirectory,
      query = "Elena",
      professionCategory = "security"
    )
    assertTrue(emptyResults.isEmpty())
  }
}
