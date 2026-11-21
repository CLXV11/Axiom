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
  fun testStoreCatalog_containsFramesNameplatesAndWalls() {
    val catalog = storeRepository.catalog
    assertTrue(catalog.isNotEmpty())

    val frames = catalog.filter { it.type == DecorationType.AVATAR_FRAME }
    val nameplates = catalog.filter { it.type == DecorationType.ROLE_NAMEPLATE }
    val walls = catalog.filter { it.type == DecorationType.ACHIEVEMENT_WALL }

    assertEquals(18, frames.size)
    assertEquals(13, nameplates.size)
    assertEquals(6, walls.size)

    // Classic & newly added collector avatar frames
    listOf(
      "haunted_spirits", "cat_bell", "violet_night", "violet_wreath",
      "sakura_tide", "sunlit_pattern", "crimson_silver", "skull_eye",
      "octopus_crown", "violet_butterfly", "steampunk_engine", "feather_totem",
      "sunlit_garden", "future_mech", "celestial_wings", "imperial_gold",
      "dragon_flame", "cosmic_swirl"
    ).forEach { key ->
      assertNotNull("Expected frame $key", catalog.find { it.styleKey == key })
    }

    // Illustrated role nameplates
    listOf(
      "np_peachy_bubble", "np_pear_bubbles", "np_midnight_rose",
      "np_midnight_haunt", "np_aurora", "np_prismatic", "np_golden",
      "np_cracked_stone", "np_deep_space", "np_raven_engraving"
    ).forEach { key ->
      assertNotNull("Expected nameplate $key", catalog.find { it.styleKey == key })
    }

    // Achievement wall backgrounds
    listOf(
      "wall_moonlight_holo", "wall_digital_matrix", "wall_crimson_forest",
      "wall_dusk_castle", "wall_neon_nexus", "wall_dragon_throne"
    ).forEach { key ->
      assertNotNull("Expected wall $key", catalog.find { it.styleKey == key })
    }
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

  @Test
  fun testBuyAndEquipAchievementWall_updatesEquippedWallState() = runTest {
    val wallItem = storeRepository.catalog.first { it.id == "wall_crimson_forest" }
    val initialCoins = storeRepository.coinBalance.value

    val purchased = storeRepository.buyItem(wallItem)
    assertTrue(purchased.isSuccess)
    assertEquals(true, purchased.getOrNull())
    assertTrue(storeRepository.isOwned(wallItem.id))
    assertEquals(wallItem.id, storeRepository.equippedWall.value)
    assertEquals(initialCoins - wallItem.price, storeRepository.coinBalance.value)

    val unequipResult = storeRepository.unequipWall()
    assertTrue(unequipResult.isSuccess)
    assertEquals(null, storeRepository.equippedWall.value)

    val reEquipResult = storeRepository.equipItem(wallItem)
    assertTrue(reEquipResult.isSuccess)
    assertEquals(wallItem.id, storeRepository.equippedWall.value)
  }
}
