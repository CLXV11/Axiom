package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.R
import com.example.data.model.DecorationItem
import com.example.data.model.DecorationType
import com.example.data.util.OperationType
import com.example.data.util.handleFirestoreError
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

data class StoreMilestone(
  val id: String,
  val reward: Int
)

class StoreRepository(
  private val db: FirebaseFirestore?,
  private val prefs: SharedPreferences?,
  private val auth: FirebaseAuth? = null
) {
  constructor(context: Context) : this(
    resolveFirestore(context),
    context.applicationContext.getSharedPreferences("kora_store_prefs", Context.MODE_PRIVATE),
    resolveAuth()
  )

  companion object {
    const val INITIAL_COINS = 500
    const val DAILY_REWARD_COINS = 250
    const val DAILY_COOLDOWN_MS = 24L * 60L * 60L * 1000L

    val MILESTONE_REWARDS: Map<String, Int> = mapOf(
      "m_profile" to 150,
      "m_verified" to 200,
      "m_flux" to 250,
      "m_avatar" to 150
    )

    private fun resolveFirestore(context: Context): FirebaseFirestore? = try {
      FirebaseFirestore.getInstance(
        context.applicationContext.getString(R.string.firestore_database_id)
      )
    } catch (_: Exception) {
      null
    }

    private fun resolveAuth(): FirebaseAuth? = try {
      Firebase.auth
    } catch (_: Exception) {
      null
    }
  }

  private fun isSignedIn(): Boolean = auth?.currentUser != null && db != null

  private val currentUid: String?
    get() = auth?.currentUser?.uid

  private fun prefKey(base: String): String {
    val uid = currentUid
    return if (!uid.isNullOrBlank()) "${base}_$uid" else base
  }

  private val _coinBalance = MutableStateFlow(
    prefs?.getInt(prefKey("key_user_coins"), INITIAL_COINS) ?: INITIAL_COINS
  )
  val coinBalance: StateFlow<Int> = _coinBalance.asStateFlow()

  private val _equippedFrame = MutableStateFlow<String?>(
    prefs?.getString(prefKey("key_equipped_frame"), null)
  )
  val equippedFrame: StateFlow<String?> = _equippedFrame.asStateFlow()

  private val _equippedNameplate = MutableStateFlow<String?>(
    prefs?.getString(prefKey("key_equipped_nameplate"), null)
  )
  val equippedNameplate: StateFlow<String?> = _equippedNameplate.asStateFlow()

  private val _equippedWall = MutableStateFlow<String?>(
    prefs?.getString(prefKey("key_equipped_wall"), null)
  )
  val equippedWall: StateFlow<String?> = _equippedWall.asStateFlow()

  private val _ownedItemIds = MutableStateFlow<Set<String>>(
    prefs?.getStringSet(prefKey("key_owned_ids"), emptySet())?.toSet() ?: emptySet()
  )
  val ownedItemIds: StateFlow<Set<String>> = _ownedItemIds.asStateFlow()

  private val _claimedMilestones = MutableStateFlow<Set<String>>(
    prefs?.getStringSet(prefKey("key_claimed_milestones"), emptySet())?.toSet() ?: emptySet()
  )
  val claimedMilestones: StateFlow<Set<String>> = _claimedMilestones.asStateFlow()

  private val _lastDailyClaimAtMillis = MutableStateFlow<Long?>(
    prefs?.getLong(prefKey("key_last_daily_claim_at"), -1L)?.takeIf { it > 0L }
  )
  val lastDailyClaimAtMillis: StateFlow<Long?> = _lastDailyClaimAtMillis.asStateFlow()

  private val _isBusy = MutableStateFlow(false)
  val isBusy: StateFlow<Boolean> = _isBusy.asStateFlow()

  private var walletListener: ListenerRegistration? = null
  private var listeningUid: String? = null

  val catalog: List<DecorationItem>
    get() = StoreCatalog.items

  private fun walletDocRef(uid: String) =
    db!!.collection("users").document(uid).collection("wallet").document("state")

  private fun userDocRef(uid: String) =
    db!!.collection("users").document(uid)

  private fun equipFieldFor(type: DecorationType): String = when (type) {
    DecorationType.AVATAR_FRAME -> "equippedFrame"
    DecorationType.ROLE_NAMEPLATE -> "equippedNameplate"
    DecorationType.ACHIEVEMENT_WALL -> "equippedWall"
    DecorationType.CHAT_BUBBLE -> "equippedNameplate"
  }

  fun startSync() {
    val uid = currentUid
    if (!isSignedIn() || uid == null) return
    if (listeningUid == uid && walletListener != null) return

    walletListener?.remove()
    listeningUid = uid

    walletListener = walletDocRef(uid).addSnapshotListener { snapshot, error ->
      if (error != null) {
        handleFirestoreError(error, OperationType.GET, "users/$uid/wallet/state", auth)
        return@addSnapshotListener
      }
      if (snapshot != null && snapshot.exists()) {
        applySnapshot(snapshot)
      }
    }
  }

  suspend fun initializeWalletIfNeeded(): Result<Unit> {
    startSync()
    val uid = currentUid ?: return Result.success(Unit)
    if (!isSignedIn()) return Result.success(Unit)

    val ref = walletDocRef(uid)
    return try {
      val snapshot = ref.get().await()
      if (!snapshot.exists()) {
        val payload = hashMapOf<String, Any?>(
          "coinBalance" to INITIAL_COINS,
          "ownedItemIds" to emptyList<String>(),
          "claimedMilestones" to emptyList<String>(),
          "lastDailyClaimAt" to null,
          "equippedFrame" to null,
          "equippedNameplate" to null,
          "equippedWall" to null,
          "createdAt" to FieldValue.serverTimestamp(),
          "updatedAt" to FieldValue.serverTimestamp()
        )
        ref.set(payload).await()
      } else {
        applySnapshot(snapshot)
      }
      Result.success(Unit)
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, ref.path, auth)
      Result.failure(e)
    }
  }

  fun close() {
    walletListener?.remove()
    walletListener = null
    listeningUid = null
  }

  private fun applySnapshot(snapshot: DocumentSnapshot) {
    val coins = (snapshot.getLong("coinBalance") ?: INITIAL_COINS.toLong()).toInt().coerceAtLeast(0)
    @Suppress("UNCHECKED_CAST")
    val owned = (snapshot.get("ownedItemIds") as? List<*>)?.mapNotNull { it as? String }?.toSet() ?: emptySet()
    @Suppress("UNCHECKED_CAST")
    val claimed = (snapshot.get("claimedMilestones") as? List<*>)?.mapNotNull { it as? String }?.toSet() ?: emptySet()
    val dailyMs = snapshot.getTimestamp("lastDailyClaimAt", DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
      ?.toDate()?.time
    val frame = snapshot.getString("equippedFrame")?.takeIf { it.isNotBlank() }
    val nameplate = snapshot.getString("equippedNameplate")?.takeIf { it.isNotBlank() }
    val wall = snapshot.getString("equippedWall")?.takeIf { it.isNotBlank() }

    _coinBalance.value = coins
    _ownedItemIds.value = owned
    _claimedMilestones.value = claimed
    _lastDailyClaimAtMillis.value = dailyMs
    _equippedFrame.value = frame
    _equippedNameplate.value = nameplate
    _equippedWall.value = wall
    persistToPrefs(coins, owned, claimed, dailyMs, frame, nameplate, wall)
  }

  private fun persistToPrefs(
    coins: Int,
    owned: Set<String>,
    claimed: Set<String>,
    dailyMs: Long?,
    frame: String?,
    nameplate: String?,
    wall: String?
  ) {
    prefs?.edit()?.apply {
      putInt(prefKey("key_user_coins"), coins)
      putStringSet(prefKey("key_owned_ids"), owned)
      putStringSet(prefKey("key_claimed_milestones"), claimed)
      if (dailyMs != null) putLong(prefKey("key_last_daily_claim_at"), dailyMs)
      else remove(prefKey("key_last_daily_claim_at"))
      if (frame != null) putString(prefKey("key_equipped_frame"), frame)
      else remove(prefKey("key_equipped_frame"))
      if (nameplate != null) putString(prefKey("key_equipped_nameplate"), nameplate)
      else remove(prefKey("key_equipped_nameplate"))
      if (wall != null) putString(prefKey("key_equipped_wall"), wall)
      else remove(prefKey("key_equipped_wall"))
      apply()
    }
  }

  fun isOwned(itemId: String): Boolean = _ownedItemIds.value.contains(itemId)

  fun isEquipped(itemId: String): Boolean =
    _equippedFrame.value == itemId ||
      _equippedNameplate.value == itemId ||
      _equippedWall.value == itemId

  fun isMilestoneClaimed(milestoneId: String): Boolean = _claimedMilestones.value.contains(milestoneId)

  fun dailyCooldownRemainingMs(nowMs: Long = System.currentTimeMillis()): Long {
    val last = _lastDailyClaimAtMillis.value ?: return 0L
    return (last + DAILY_COOLDOWN_MS - nowMs).coerceAtLeast(0L)
  }

  suspend fun buyItem(item: DecorationItem): Result<Boolean> {
    if (isOwned(item.id)) {
      return equipItem(item).map { true }
    }

    if (isSignedIn()) {
      val uid = currentUid!!
      _isBusy.value = true
      return try {
        val initRes = initializeWalletIfNeeded()
        if (initRes.isFailure) return Result.failure(initRes.exceptionOrNull()!!)

        val wRef = walletDocRef(uid)
        val uRef = userDocRef(uid)
        val equipField = equipFieldFor(item.type)

        db!!.runTransaction { transaction ->
          val walletSnap = transaction.get(wRef)
          val userSnap = transaction.get(uRef)

          val currentCoins = (walletSnap.getLong("coinBalance") ?: 0L).toInt()
          @Suppress("UNCHECKED_CAST")
          val currentOwned = (walletSnap.get("ownedItemIds") as? List<*>)
            ?.mapNotNull { it as? String } ?: emptyList()

          if (currentOwned.contains(item.id)) {
            throw IllegalStateException("Item already owned")
          }
          if (currentCoins < item.price) {
            throw IllegalStateException("Insufficient coins")
          }

          val newCoins = currentCoins - item.price
          val newOwned = currentOwned + item.id

          transaction.update(
            wRef,
            mapOf(
              "coinBalance" to newCoins,
              "ownedItemIds" to newOwned,
              equipField to item.id,
              "updatedAt" to FieldValue.serverTimestamp()
            )
          )
          if (userSnap.exists()) {
            transaction.update(
              uRef,
              mapOf(
                equipField to item.id,
                "updatedAt" to FieldValue.serverTimestamp()
              )
            )
          }
          null
        }.await()

        val updatedOwned = _ownedItemIds.value + item.id
        val newCoins = (_coinBalance.value - item.price).coerceAtLeast(0)
        val newFrame = if (item.type == DecorationType.AVATAR_FRAME) item.id else _equippedFrame.value
        val newNameplate = if (item.type == DecorationType.ROLE_NAMEPLATE) item.id else _equippedNameplate.value
        val newWall = if (item.type == DecorationType.ACHIEVEMENT_WALL) item.id else _equippedWall.value

        _coinBalance.value = newCoins
        _ownedItemIds.value = updatedOwned
        _equippedFrame.value = newFrame
        _equippedNameplate.value = newNameplate
        _equippedWall.value = newWall
        persistToPrefs(newCoins, updatedOwned, _claimedMilestones.value, _lastDailyClaimAtMillis.value, newFrame, newNameplate, newWall)
        Result.success(true)
      } catch (e: Exception) {
        handleFirestoreError(e, OperationType.WRITE, "users/$uid/wallet/state", auth)
        Result.failure(e)
      } finally {
        _isBusy.value = false
      }
    }

    val current = _coinBalance.value
    if (current < item.price) return Result.success(false)

    val newBalance = current - item.price
    val updatedOwned = _ownedItemIds.value + item.id
    val newFrame = if (item.type == DecorationType.AVATAR_FRAME) item.id else _equippedFrame.value
    val newNameplate = if (item.type == DecorationType.ROLE_NAMEPLATE) item.id else _equippedNameplate.value
    val newWall = if (item.type == DecorationType.ACHIEVEMENT_WALL) item.id else _equippedWall.value

    _coinBalance.value = newBalance
    _ownedItemIds.value = updatedOwned
    _equippedFrame.value = newFrame
    _equippedNameplate.value = newNameplate
    _equippedWall.value = newWall
    persistToPrefs(newBalance, updatedOwned, _claimedMilestones.value, _lastDailyClaimAtMillis.value, newFrame, newNameplate, newWall)
    return Result.success(true)
  }

  suspend fun equipItem(item: DecorationItem): Result<Unit> {
    if (!isOwned(item.id)) {
      return Result.failure(IllegalStateException("Item must be purchased before equipping"))
    }
    val equipField = equipFieldFor(item.type)

    if (isSignedIn()) {
      val uid = currentUid!!
      _isBusy.value = true
      return try {
        val wRef = walletDocRef(uid)
        val uRef = userDocRef(uid)
        db!!.runTransaction { transaction ->
          val walletSnap = transaction.get(wRef)
          val userSnap = transaction.get(uRef)
          @Suppress("UNCHECKED_CAST")
          val currentOwned = (walletSnap.get("ownedItemIds") as? List<*>)
            ?.mapNotNull { it as? String } ?: emptyList()
          if (!currentOwned.contains(item.id)) {
            throw IllegalStateException("Item not owned")
          }
          transaction.update(
            wRef,
            mapOf(
              equipField to item.id,
              "updatedAt" to FieldValue.serverTimestamp()
            )
          )
          if (userSnap.exists()) {
            transaction.update(
              uRef,
              mapOf(
                equipField to item.id,
                "updatedAt" to FieldValue.serverTimestamp()
              )
            )
          }
          null
        }.await()

        val newFrame = if (item.type == DecorationType.AVATAR_FRAME) item.id else _equippedFrame.value
        val newNameplate = if (item.type == DecorationType.ROLE_NAMEPLATE) item.id else _equippedNameplate.value
        val newWall = if (item.type == DecorationType.ACHIEVEMENT_WALL) item.id else _equippedWall.value
        _equippedFrame.value = newFrame
        _equippedNameplate.value = newNameplate
        _equippedWall.value = newWall
        persistToPrefs(_coinBalance.value, _ownedItemIds.value, _claimedMilestones.value, _lastDailyClaimAtMillis.value, newFrame, newNameplate, newWall)
        Result.success(Unit)
      } catch (e: Exception) {
        handleFirestoreError(e, OperationType.UPDATE, "users/$uid/wallet/state", auth)
        Result.failure(e)
      } finally {
        _isBusy.value = false
      }
    }

    val newFrame = if (item.type == DecorationType.AVATAR_FRAME) item.id else _equippedFrame.value
    val newNameplate = if (item.type == DecorationType.ROLE_NAMEPLATE) item.id else _equippedNameplate.value
    val newWall = if (item.type == DecorationType.ACHIEVEMENT_WALL) item.id else _equippedWall.value
    _equippedFrame.value = newFrame
    _equippedNameplate.value = newNameplate
    _equippedWall.value = newWall
    persistToPrefs(_coinBalance.value, _ownedItemIds.value, _claimedMilestones.value, _lastDailyClaimAtMillis.value, newFrame, newNameplate, newWall)
    return Result.success(Unit)
  }

  suspend fun unequipFrame(): Result<Unit> = unequipField("equippedFrame")

  suspend fun unequipNameplate(): Result<Unit> = unequipField("equippedNameplate")

  suspend fun unequipWall(): Result<Unit> = unequipField("equippedWall")

  private suspend fun unequipField(field: String): Result<Unit> {
    if (isSignedIn()) {
      val uid = currentUid!!
      _isBusy.value = true
      return try {
        val wRef = walletDocRef(uid)
        val uRef = userDocRef(uid)
        db!!.runTransaction { transaction ->
          transaction.get(wRef)
          val userSnap = transaction.get(uRef)
          transaction.update(
            wRef,
            mapOf(
              field to null,
              "updatedAt" to FieldValue.serverTimestamp()
            )
          )
          if (userSnap.exists()) {
            transaction.update(
              uRef,
              mapOf(
                field to null,
                "updatedAt" to FieldValue.serverTimestamp()
              )
            )
          }
          null
        }.await()
        clearEquippedFieldLocal(field)
        persistToPrefs(
          _coinBalance.value,
          _ownedItemIds.value,
          _claimedMilestones.value,
          _lastDailyClaimAtMillis.value,
          _equippedFrame.value,
          _equippedNameplate.value,
          _equippedWall.value
        )
        Result.success(Unit)
      } catch (e: Exception) {
        handleFirestoreError(e, OperationType.UPDATE, "users/$uid/wallet/state", auth)
        Result.failure(e)
      } finally {
        _isBusy.value = false
      }
    }

    clearEquippedFieldLocal(field)
    persistToPrefs(
      _coinBalance.value,
      _ownedItemIds.value,
      _claimedMilestones.value,
      _lastDailyClaimAtMillis.value,
      _equippedFrame.value,
      _equippedNameplate.value,
      _equippedWall.value
    )
    return Result.success(Unit)
  }

  private fun clearEquippedFieldLocal(field: String) {
    when (field) {
      "equippedFrame" -> _equippedFrame.value = null
      "equippedNameplate" -> _equippedNameplate.value = null
      "equippedWall" -> _equippedWall.value = null
    }
  }

  suspend fun claimDailyReward(): Result<Int> {
    if (dailyCooldownRemainingMs() > 0L) {
      return Result.failure(IllegalStateException("Daily reward cooldown active"))
    }

    if (isSignedIn()) {
      val uid = currentUid!!
      _isBusy.value = true
      return try {
        val initRes = initializeWalletIfNeeded()
        if (initRes.isFailure) return Result.failure(initRes.exceptionOrNull()!!)

        val wRef = walletDocRef(uid)
        val updatedBalance = db!!.runTransaction { transaction ->
          val snap = transaction.get(wRef)
          val lastClaim = snap.getTimestamp("lastDailyClaimAt")?.toDate()?.time
          if (lastClaim != null && System.currentTimeMillis() - lastClaim < DAILY_COOLDOWN_MS) {
            throw IllegalStateException("Daily reward cooldown active")
          }
          val currentCoins = (snap.getLong("coinBalance") ?: 0L).toInt()
          val nextCoins = currentCoins + DAILY_REWARD_COINS
          transaction.update(
            wRef,
            mapOf(
              "coinBalance" to nextCoins,
              "lastDailyClaimAt" to FieldValue.serverTimestamp(),
              "updatedAt" to FieldValue.serverTimestamp()
            )
          )
          nextCoins
        }.await()

        val now = System.currentTimeMillis()
        _coinBalance.value = updatedBalance
        _lastDailyClaimAtMillis.value = now
        persistToPrefs(updatedBalance, _ownedItemIds.value, _claimedMilestones.value, now, _equippedFrame.value, _equippedNameplate.value, _equippedWall.value)
        Result.success(DAILY_REWARD_COINS)
      } catch (e: Exception) {
        handleFirestoreError(e, OperationType.UPDATE, "users/$uid/wallet/state", auth)
        Result.failure(e)
      } finally {
        _isBusy.value = false
      }
    }

    val now = System.currentTimeMillis()
    val nextCoins = _coinBalance.value + DAILY_REWARD_COINS
    _coinBalance.value = nextCoins
    _lastDailyClaimAtMillis.value = now
    persistToPrefs(nextCoins, _ownedItemIds.value, _claimedMilestones.value, now, _equippedFrame.value, _equippedNameplate.value, _equippedWall.value)
    return Result.success(DAILY_REWARD_COINS)
  }

  suspend fun claimMilestone(milestoneId: String): Result<Int> {
    val reward = MILESTONE_REWARDS[milestoneId]
      ?: return Result.failure(IllegalArgumentException("Unknown milestone"))
    if (isMilestoneClaimed(milestoneId)) {
      return Result.failure(IllegalStateException("Milestone already claimed"))
    }

    if (isSignedIn()) {
      val uid = currentUid!!
      _isBusy.value = true
      return try {
        val initRes = initializeWalletIfNeeded()
        if (initRes.isFailure) return Result.failure(initRes.exceptionOrNull()!!)

        val wRef = walletDocRef(uid)
        val updatedBalance = db!!.runTransaction { transaction ->
          val snap = transaction.get(wRef)
          @Suppress("UNCHECKED_CAST")
          val existingClaimed = (snap.get("claimedMilestones") as? List<*>)
            ?.mapNotNull { it as? String } ?: emptyList()
          if (existingClaimed.contains(milestoneId)) {
            throw IllegalStateException("Milestone already claimed")
          }
          val currentCoins = (snap.getLong("coinBalance") ?: 0L).toInt()
          val nextCoins = currentCoins + reward
          transaction.update(
            wRef,
            mapOf(
              "coinBalance" to nextCoins,
              "claimedMilestones" to (existingClaimed + milestoneId),
              "updatedAt" to FieldValue.serverTimestamp()
            )
          )
          nextCoins
        }.await()

        val updatedClaimed = _claimedMilestones.value + milestoneId
        _coinBalance.value = updatedBalance
        _claimedMilestones.value = updatedClaimed
        persistToPrefs(updatedBalance, _ownedItemIds.value, updatedClaimed, _lastDailyClaimAtMillis.value, _equippedFrame.value, _equippedNameplate.value, _equippedWall.value)
        Result.success(reward)
      } catch (e: Exception) {
        handleFirestoreError(e, OperationType.UPDATE, "users/$uid/wallet/state", auth)
        Result.failure(e)
      } finally {
        _isBusy.value = false
      }
    }

    val nextCoins = _coinBalance.value + reward
    val updatedClaimed = _claimedMilestones.value + milestoneId
    _coinBalance.value = nextCoins
    _claimedMilestones.value = updatedClaimed
    persistToPrefs(nextCoins, _ownedItemIds.value, updatedClaimed, _lastDailyClaimAtMillis.value, _equippedFrame.value, _equippedNameplate.value, _equippedWall.value)
    return Result.success(reward)
  }
}
