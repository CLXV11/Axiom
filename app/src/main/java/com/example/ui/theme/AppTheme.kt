package com.example.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Curated Kora Theme Presets
 */
enum class AppThemePreset(
  val id: String,
  val nameEn: String,
  val nameAr: String,
  val subtitleEn: String,
  val subtitleAr: String,
  val primaryColor: Color,
  val backgroundColor: Color,
  val surfaceColor: Color,
  val isDark: Boolean
) {
  DISCORD_DARK(
    id = "discord_dark",
    nameEn = "Discord Ash Charcoal",
    nameAr = "رمادي ديسكورد المريح (داكن)",
    subtitleEn = "Layered matte charcoal grays with calm Blurple",
    subtitleAr = "طبقات رمادية داكنة مريحة للعين مع أزرق هادئ",
    primaryColor = DiscordBlurple,
    backgroundColor = DiscordDarkPrimary,
    surfaceColor = DiscordDarkSecondary,
    isDark = true
  ),
  OLED_MIDNIGHT(
    id = "oled_midnight",
    nameEn = "Pure OLED Obsidian",
    nameAr = "سواد OLED النقي",
    subtitleEn = "True pitch black with crisp cyan highlights",
    subtitleAr = "سواد حقيقي 100% مع لمسات سماوية صافية",
    primaryColor = Color(0xFF06B6D4),
    backgroundColor = Color(0xFF000000),
    surfaceColor = Color(0xFF0D1117),
    isDark = true
  ),
  NITRO_MAGENTA(
    id = "nitro_magenta",
    nameEn = "Velvet Orchid",
    nameAr = "أوركيد مخملي",
    subtitleEn = "Muted plum charcoal & soft rose accent",
    subtitleAr = "درجات فحمية أرجوانية ناعمة ومريحة",
    primaryColor = DiscordNitroPink,
    backgroundColor = Color(0xFF241D2B),
    surfaceColor = Color(0xFF2D2535),
    isDark = true
  ),
  CYBER_EMERALD(
    id = "cyber_emerald",
    nameEn = "Emerald Slate",
    nameAr = "رمادي زمردي",
    subtitleEn = "Deep slate pine & calm emerald green",
    subtitleAr = "رمادي غامق هادئ مع لمسة زمردية",
    primaryColor = DiscordGreen,
    backgroundColor = Color(0xFF1F2A27),
    surfaceColor = Color(0xFF273430),
    isDark = true
  ),
  CRIMSON_RUBY(
    id = "crimson_ruby",
    nameEn = "Imperial Ruby",
    nameAr = "رمادي ياقوتي",
    subtitleEn = "Warm espresso slate & balanced coral",
    subtitleAr = "فحم دافئ مع أحمر متوازن",
    primaryColor = DiscordRed,
    backgroundColor = Color(0xFF291F22),
    surfaceColor = Color(0xFF33272B),
    isDark = true
  ),
  SOLAR_AMBER(
    id = "solar_amber",
    nameEn = "Desert Amber",
    nameAr = "رمادي كهرماني",
    subtitleEn = "Warm charcoal & refined matte gold",
    subtitleAr = "رمادي دافئ مع ذهبي هادئ",
    primaryColor = DiscordYellow,
    backgroundColor = Color(0xFF28241C),
    surfaceColor = Color(0xFF322D23),
    isDark = true
  ),
  DISCORD_LIGHT(
    id = "discord_light",
    nameEn = "Porcelain Daylight",
    nameAr = "ضوء الصباح (فاتح)",
    subtitleEn = "Crisp porcelain white & serene Blurple",
    subtitleAr = "بياض نقي مريح للعين مع أزرق راقٍ",
    primaryColor = Color(0xFF5865F2),
    backgroundColor = Color(0xFFF2F3F5),
    surfaceColor = PureWhite,
    isDark = false
  ),
  SYSTEM(
    id = "system",
    nameEn = "System Adaptive",
    nameAr = "تلقائي حسب النظام",
    subtitleEn = "Syncs smoothly with Android appearance",
    subtitleAr = "يتبع مظهر الهاتف تلقائياً",
    primaryColor = DiscordBlurple,
    backgroundColor = DiscordDarkPrimary,
    surfaceColor = DiscordDarkSecondary,
    isDark = true
  );

  companion object {
    fun fromId(id: String?): AppThemePreset {
      return entries.find { it.id.equals(id, ignoreCase = true) } ?: DISCORD_DARK
    }
  }
}

/**
 * Curated Accent Color Options
 */
enum class AccentColorChoice(
  val id: String,
  val nameEn: String,
  val nameAr: String,
  val color: Color
) {
  DEFAULT("default", "Preset Default", "افتراضي السمة", Color.Unspecified),
  BLURPLE("blurple", "Blurple", "أزرق ديسكورد", DiscordBlurple),
  NITRO_PINK("nitro_pink", "Rose", "وردي", DiscordNitroPink),
  CYAN("cyan", "Cyan", "سماوي", Color(0xFF00A8FC)),
  EMERALD("emerald", "Emerald", "زمردي", DiscordGreen),
  GOLD("gold", "Amber", "كهرماني", DiscordYellow),
  VIOLET("violet", "Violet", "بنفسجي", Color(0xFF7C6CF0)),
  RED("red", "Coral", "مرجاني", DiscordRed);

  companion object {
    fun fromId(id: String?): AccentColorChoice {
      return entries.find { it.id.equals(id, ignoreCase = true) } ?: DEFAULT
    }
  }
}
