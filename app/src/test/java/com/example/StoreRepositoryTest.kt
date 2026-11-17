package com.example

import androidx.test.core.app.ApplicationProvider
import com.example.data.model.DecorationItem
import com.example.data.model.DecorationType
import com.example.data.model.ItemRarity
import com.example.data.repository.StoreRepository
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
class StoreRepositoryTest {

  private lateinit var storeRepository: StoreRepository

  @Before
  fun setup() {
    val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    storeRepository = StoreRepository(context)
  }

  @Test
  fun testStoreCatalog_containsFramesAndNameplates() {
    val catalog = storeRepository.catalog
    assertTrue(catalog.isNotEmpty())

    val frames = catalog.filter { it.type == DecorationType.AVATAR_FRAME }
    val nameplates = catalog.filter { it.type == DecorationType.ROLE_NAMEPLATE }

    assertTrue("Catalog must contain avatar frames", frames.isNotEmpty())
    assertTrue("Catalog must contain role nameplates", nameplates.isNotEmpty())

    // Verify key custom and reference styles exist
    assertNotNull(catalog.find { it.styleKey == "celestial_wings" })
    assertNotNull(catalog.find { it.styleKey == "imperial_gold" })
    assertNotNull(catalog.find { it.styleKey == "future_mech" })
    assertNotNull(catalog.find { it.styleKey == "skull_eye" })
    assertNotNull(catalog.find { it.styleKey == "octopus_crown" })
    assertNotNull(catalog.find { it.styleKey == "sakura_tide" })
    assertNotNull(catalog.find { it.styleKey == "cat_bell" })
    assertNotNull(catalog.find { it.styleKey == "violet_night" })
  }

  @Test
  fun testClaimDailyReward_addsCoins() {
    val initialCoins = storeRepository.coinBalance.value
    val reward = storeRepository.claimDailyReward()
    assertEquals(250, reward)
    assertEquals(initialCoins + 250, storeRepository.coinBalance.value)
  }

  @Test
  fun testBuyAndEquipItem_updatesState() {
    // Ensure sufficient coins
    storeRepository.addCoins(1000)
    val testItem = storeRepository.catalog.first { it.type == DecorationType.AVATAR_FRAME }

    val initialCoins = storeRepository.coinBalance.value
    val purchased = storeRepository.buyItem(testItem)

    assertTrue(purchased)
    assertTrue(storeRepository.isOwned(testItem.id))
    assertEquals(testItem.id, storeRepository.equippedFrame.value)
    assertEquals(initialCoins - testItem.price, storeRepository.coinBalance.value)

    storeRepository.unequipFrame()
    assertEquals(null, storeRepository.equippedFrame.value)
  }
}
