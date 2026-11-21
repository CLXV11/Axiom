package com.example.ui.util

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.UserProfile
import org.json.JSONObject
import java.util.UUID

class PreferencesManager(context: Context) {
  private val prefs: SharedPreferences =
    context.getSharedPreferences("kora_preferences", Context.MODE_PRIVATE)

  var language: AppLanguage
    get() {
      val code = prefs.getString("key_language", "ar") ?: "ar" // Default to Arabic as requested by user
      return if (code == "en") AppLanguage.EN else AppLanguage.AR
    }
    set(value) {
      prefs.edit().putString("key_language", value.code).apply()
    }

  var themeMode: String // "light", "dark", "system"
    get() = prefs.getString("key_theme", "dark") ?: "dark"
    set(value) {
      prefs.edit().putString("key_theme", value).apply()
    }

  var themePreset: com.example.ui.theme.AppThemePreset
    get() {
      val raw = prefs.getString("key_theme_preset", null)
      if (raw != null) return com.example.ui.theme.AppThemePreset.fromId(raw)
      return when (themeMode) {
        "light" -> com.example.ui.theme.AppThemePreset.DISCORD_LIGHT
        "system" -> com.example.ui.theme.AppThemePreset.SYSTEM
        else -> com.example.ui.theme.AppThemePreset.DISCORD_DARK
      }
    }
    set(value) {
      prefs.edit().putString("key_theme_preset", value.id).apply()
      themeMode = if (!value.isDark) "light" else if (value == com.example.ui.theme.AppThemePreset.SYSTEM) "system" else "dark"
    }

  var amoledPureBlack: Boolean
    get() = prefs.getBoolean("key_amoled_pure_black", true)
    set(value) {
      prefs.edit().putBoolean("key_amoled_pure_black", value).apply()
    }

  var accentChoice: com.example.ui.theme.AccentColorChoice
    get() = com.example.ui.theme.AccentColorChoice.fromId(prefs.getString("key_accent_choice", "default") ?: "default")
    set(value) {
      prefs.edit().putString("key_accent_choice", value.id).apply()
    }

  fun saveCachedProfile(profile: UserProfile) {
    if (profile.id.isBlank()) return
    val json = JSONObject().apply {
      put("id", profile.id)
      put("displayName", profile.displayName)
      put("handle", profile.handle)
      put("bio", profile.bio)
      put("profession", profile.profession)
      put("isProfessionalVerified", profile.isProfessionalVerified)
      put("professionalOrg", profile.professionalOrg)
      put("openToNetworking", profile.openToNetworking)
      put("avatarUrl", profile.avatarUrl)
      put("isVerified", profile.isVerified)
      put("verifiedTier", profile.verifiedTier)
      put("equippedFrame", profile.equippedFrame ?: "")
      put("equippedNameplate", profile.equippedNameplate ?: "")
      put("equippedWall", profile.equippedWall ?: "")
      put("nameFontId", profile.nameFontId)
      put("nameThemeId", profile.nameThemeId)
      put("followersCount", profile.followersCount)
      put("followingCount", profile.followingCount)
      put("quietMode", profile.quietMode)
      put("readReceiptsEnabled", profile.readReceiptsEnabled)
    }
    prefs.edit().putString("key_cached_profile_json", json.toString()).apply()
  }

  fun getCachedProfile(uid: String): UserProfile? {
    if (uid.isBlank()) return null
    val raw = prefs.getString("key_cached_profile_json", null) ?: return null
    return try {
      val json = JSONObject(raw)
      if (json.optString("id") != uid) return null
      val frame = json.optString("equippedFrame", "").ifBlank { null }
      val nameplate = json.optString("equippedNameplate", "").ifBlank { null }
      val wall = json.optString("equippedWall", "").ifBlank { null }
      UserProfile(
        id = uid,
        displayName = json.optString("displayName", ""),
        handle = json.optString("handle", ""),
        bio = json.optString("bio", ""),
        profession = json.optString("profession", ""),
        isProfessionalVerified = json.optBoolean("isProfessionalVerified", false),
        professionalOrg = json.optString("professionalOrg", ""),
        openToNetworking = json.optBoolean("openToNetworking", true),
        avatarUrl = json.optString("avatarUrl", ""),
        isVerified = json.optBoolean("isVerified", false),
        verifiedTier = json.optString("verifiedTier", "BLUE"),
        equippedFrame = frame,
        equippedNameplate = nameplate,
        equippedWall = wall,
        nameFontId = json.optString("nameFontId", "default").ifBlank { "default" },
        nameThemeId = json.optString("nameThemeId", "default").ifBlank { "default" },
        followersCount = json.optLong("followersCount", 0L).coerceAtLeast(0L),
        followingCount = json.optLong("followingCount", 0L).coerceAtLeast(0L),
        quietMode = json.optBoolean("quietMode", false),
        readReceiptsEnabled = json.optBoolean("readReceiptsEnabled", true)
      )
    } catch (_: Exception) {
      null
    }
  }

  fun clearCachedProfile() {
    prefs.edit().remove("key_cached_profile_json").apply()
  }

  fun clearAllSessionCaches(context: Context) {
    clearCachedProfile()
    context.applicationContext
      .getSharedPreferences("kora_store_prefs", Context.MODE_PRIVATE)
      .edit()
      .clear()
      .apply()
    context.applicationContext
      .getSharedPreferences("flux_storage_prefs", Context.MODE_PRIVATE)
      .edit()
      .clear()
      .apply()
  }

  fun getOrCreateGuestUserId(): String {
    val existing = prefs.getString("key_guest_user_id", null)
    if (!existing.isNullOrBlank()) return existing
    val newId = "kora_" + UUID.randomUUID().toString().replace("-", "").take(10)
    prefs.edit().putString("key_guest_user_id", newId).apply()
    return newId
  }

  var guestDisplayName: String
    get() = prefs.getString("key_guest_display_name", "أليكس الكورا") ?: "أليكس الكورا"
    set(value) {
      prefs.edit().putString("key_guest_display_name", value).apply()
    }

  var guestHandle: String
    get() = prefs.getString("key_guest_handle", "@alex_elite") ?: "@alex_elite"
    set(value) {
      prefs.edit().putString("key_guest_handle", value).apply()
    }

  var guestBio: String
    get() = prefs.getString("key_guest_bio", "مصمم واجهات وتجارب رقمية • منصة كورا المميزة")
      ?: "مصمم واجهات وتجارب رقمية • منصة كورا المميزة"
    set(value) {
      prefs.edit().putString("key_guest_bio", value).apply()
    }

  var guestQuietMode: Boolean
    get() = prefs.getBoolean("key_guest_quiet_mode", false)
    set(value) {
      prefs.edit().putBoolean("key_guest_quiet_mode", value).apply()
    }

  var guestReadReceipts: Boolean
    get() = prefs.getBoolean("key_guest_read_receipts", true)
    set(value) {
      prefs.edit().putBoolean("key_guest_read_receipts", value).apply()
    }

  var guestIsVerified: Boolean
    get() = prefs.getBoolean("key_guest_is_verified", false)
    set(value) {
      prefs.edit().putBoolean("key_guest_is_verified", value).apply()
    }

  var guestVerifiedTier: String
    get() = prefs.getString("key_guest_verified_tier", "BLUE") ?: "BLUE"
    set(value) {
      prefs.edit().putString("key_guest_verified_tier", value).apply()
    }

  var guestAvatarUrl: String
    get() = prefs.getString("key_guest_avatar_url", "") ?: ""
    set(value) {
      prefs.edit().putString("key_guest_avatar_url", value).apply()
    }

  var guestNameFontId: String
    get() = prefs.getString("key_guest_name_font_id", "default") ?: "default"
    set(value) {
      prefs.edit().putString("key_guest_name_font_id", value).apply()
    }

  var guestNameThemeId: String
    get() = prefs.getString("key_guest_name_theme_id", "default") ?: "default"
    set(value) {
      prefs.edit().putString("key_guest_name_theme_id", value).apply()
    }

  var inAppBannersEnabled: Boolean
    get() = prefs.getBoolean("key_in_app_banners", true)
    set(value) {
      prefs.edit().putBoolean("key_in_app_banners", value).apply()
    }

  var notificationSoundEnabled: Boolean
    get() = prefs.getBoolean("key_notification_sound", true)
    set(value) {
      prefs.edit().putBoolean("key_notification_sound", value).apply()
    }

  var fluxStartMuted: Boolean
    get() = prefs.getBoolean("key_flux_start_muted", false)
    set(value) {
      prefs.edit().putBoolean("key_flux_start_muted", value).apply()
    }

  var fluxAutoDownload: Boolean
    get() = prefs.getBoolean("key_flux_auto_download", true)
    set(value) {
      prefs.edit().putBoolean("key_flux_auto_download", value).apply()
    }

  var compactMode: Boolean
    get() = prefs.getBoolean("key_compact_mode", false)
    set(value) {
      prefs.edit().putBoolean("key_compact_mode", value).apply()
    }

  var callSpeakerDefault: Boolean
    get() = prefs.getBoolean("key_call_speaker_default", false)
    set(value) {
      prefs.edit().putBoolean("key_call_speaker_default", value).apply()
    }

  fun calculateMediaCacheBytes(context: Context): Long {
    val appCtx = context.applicationContext
    val dirs = listOf(
      java.io.File(appCtx.filesDir, "flux_media"),
      java.io.File(appCtx.filesDir, "chat_media"),
      appCtx.cacheDir
    )
    fun dirSize(file: java.io.File?): Long {
      if (file == null || !file.exists()) return 0L
      if (file.isFile) return file.length()
      return file.listFiles()?.sumOf { dirSize(it) } ?: 0L
    }
    return dirs.sumOf { dirSize(it) }
  }

  fun clearMediaCache(context: Context): Long {
    val before = calculateMediaCacheBytes(context)
    val appCtx = context.applicationContext
    val targets = listOf(
      java.io.File(appCtx.filesDir, "flux_media"),
      java.io.File(appCtx.filesDir, "chat_media")
    )
    targets.forEach { dir ->
      if (dir.exists()) {
        dir.listFiles()?.forEach { child ->
          runCatching { child.deleteRecursively() }
        }
      }
    }
    appCtx.cacheDir.listFiles()?.forEach { child ->
      runCatching { child.deleteRecursively() }
    }
    return before
  }
}
