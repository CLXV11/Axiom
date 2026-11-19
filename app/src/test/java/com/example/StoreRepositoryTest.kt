package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.DecorationType
import com.example.data.repository.StoreRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
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
    val context = ApplicationProvider.getApplicationContext<Context>()
    context.getSharedPreferences("kora_store_prefs", Context.MODE_PRIVATE)
      .edit()
      .clear()
      .commit()
    storeRepository = StoreRepository(db = null, prefs = context.getSharedPreferences("kora_store_prefs", Context.MODE_PRIVATE), auth = null)
  }

  @Test
  fun testStoreCatalog_containsFramesAndNameplates() {
    val catalog = storeRepository.catalog
    assertTrue(catalog.isNotEmpty())

    val frames = catalog.filter { it.type == DecorationType.AVATAR_FRAME }
    val nameplates = catalog.filter { it.type == DecorationType.ROLE_NAMEPLATE }

    assertTrue("Catalog must contain avatar frames", frames.isNotEmpty())
    assertTrue("Catalog must contain role nameplates", nameplates.isNotEmpty())

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
  fun testClaimDailyReward_addsCoinsAndEnforcesCooldown() = runTest {
    val initialCoins = storeRepository.coinBalance.value
    assertEquals(StoreRepository.INITIAL_COINS, initialCoins)

    val firstClaim = storeRepository.claimDailyReward()
    assertTrue(firstClaim.isSuccess)
    assertEquals(StoreRepository.DAILY_REWARD_COINS, firstClaim.getOrNull())
    assertEquals(initialCoins + StoreRepository.DAILY_REWARD_COINS, storeRepository.coinBalance.value)

    // Immediate second claim must fail due to 24h cooldown
    val secondClaim = storeRepository.claimDailyReward()
    assertTrue("Daily reward cannot be claimed twice within cooldown", secondClaim.isFailure)
    assertEquals(initialCoins + StoreRepository.DAILY_REWARD_COINS, storeRepository.coinBalance.value)
  }

  @Test
  fun testClaimMilestone_awardsCoinsOnceOnly() = runTest {
    val initialCoins = storeRepository.coinBalance.value

    val firstClaim = storeRepository.claimMilestone("m_flux")
    assertTrue(firstClaim.isSuccess)
    assertEquals(250, firstClaim.getOrNull())
    assertEquals(initialCoins + 250, storeRepository.coinBalance.value)
    assertTrue(storeRepository.isMilestoneClaimed("m_flux"))

    val duplicateClaim = storeRepository.claimMilestone("m_flux")
    assertTrue("Milestone cannot be claimed more than once", duplicateClaim.isFailure)
    assertEquals(initialCoins + 250, storeRepository.coinBalance.value)
  }

  @Test
  fun testBuyAndEquipItem_updatesState() = runTest {
    val testItem = storeRepository.catalog.first {
      it.type == DecorationType.AVATAR_FRAME && it.price <= storeRepository.coinBalance.value
    }

    val initialCoins = storeRepository.coinBalance.value
    val purchased = storeRepository.buyItem(testItem)

    assertTrue(purchased.isSuccess)
    assertEquals(true, purchased.getOrNull())
    assertTrue(storeRepository.isOwned(testItem.id))
    assertEquals(testItem.id, storeRepository.equippedFrame.value)
    assertEquals(initialCoins - testItem.price, storeRepository.coinBalance.value)

    val unequipResult = storeRepository.unequipFrame()
    assertTrue(unequipResult.isSuccess)
    assertEquals(null, storeRepository.equippedFrame.value)

    val reEquipResult = storeRepository.equipItem(testItem)
    assertTrue(reEquipResult.isSuccess)
    assertEquals(testItem.id, storeRepository.equippedFrame.value)
  }
}
