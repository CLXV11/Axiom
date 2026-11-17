package com.example.ui.util

import android.content.Context
import android.content.SharedPreferences
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
    get() = prefs.getBoolean("key_amoled_pure_black", false)
    set(value) {
      prefs.edit().putBoolean("key_amoled_pure_black", value).apply()
    }

  var accentChoice: com.example.ui.theme.AccentColorChoice
    get() = com.example.ui.theme.AccentColorChoice.fromId(prefs.getString("key_accent_choice", "default") ?: "default")
    set(value) {
      prefs.edit().putString("key_accent_choice", value.id).apply()
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
    get() = prefs.getBoolean("key_guest_is_verified", true)
    set(value) {
      prefs.edit().putBoolean("key_guest_is_verified", value).apply()
    }

  var guestVerifiedTier: String
    get() = prefs.getString("key_guest_verified_tier", "GOLD") ?: "GOLD"
    set(value) {
      prefs.edit().putString("key_guest_verified_tier", value).apply()
    }

  var guestAvatarUrl: String
    get() = prefs.getString("key_guest_avatar_url", "") ?: ""
    set(value) {
      prefs.edit().putString("key_guest_avatar_url", value).apply()
    }
}
