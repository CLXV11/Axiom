package com.example.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Curated Kora Luxury Theme Presets
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
    nameEn = "Midnight Sapphire",
    nameAr = "كورا الياقوتي (داكن)",
    subtitleEn = "Deep obsidian navy with luminous indigo",
    subtitleAr = "كحلي داكن عميق مع لمسات نيلية مشرقة",
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
    subtitleEn = "Rich plum slate & radiant rose-violet",
    subtitleAr = "درجات أرجوانية فاخرة وتباين ناعم",
    primaryColor = DiscordNitroPink,
    backgroundColor = Color(0xFF120C18),
    surfaceColor = Color(0xFF1D1426),
    isDark = true
  ),
  CYBER_EMERALD(
    id = "cyber_emerald",
    nameEn = "Emerald Mist",
    nameAr = "زمردي هادئ",
    subtitleEn = "Botanical deep forest & fresh jade mint",
    subtitleAr = "أخضر غابات عميق مع زمردي منعش",
    primaryColor = DiscordGreen,
    backgroundColor = Color(0xFF091411),
    surfaceColor = Color(0xFF11221D),
    isDark = true
  ),
  CRIMSON_RUBY(
    id = "crimson_ruby",
    nameEn = "Imperial Ruby",
    nameAr = "ياقوت قرمزي",
    subtitleEn = "Warm espresso slate & vivid coral ruby",
    subtitleAr = "فحم دافئ مع أحمر ياقوتي متوازن",
    primaryColor = DiscordRed,
    backgroundColor = Color(0xFF150B0E),
    surfaceColor = Color(0xFF221317),
    isDark = true
  ),
  SOLAR_AMBER(
    id = "solar_amber",
    nameEn = "Desert Gold",
    nameAr = "ذهب كهرماني",
    subtitleEn = "Warm charcoal & refined burnished gold",
    subtitleAr = "رمادي دافئ مع ذهبي كهرماني أنيق",
    primaryColor = DiscordYellow,
    backgroundColor = Color(0xFF14110A),
    surfaceColor = Color(0xFF211B11),
    isDark = true
  ),
  DISCORD_LIGHT(
    id = "discord_light",
    nameEn = "Porcelain Daylight",
    nameAr = "ضوء الصباح (فاتح)",
    subtitleEn = "Crisp porcelain white & serene sapphire",
    subtitleAr = "بياض نقي مريح للعين مع أزرق راقٍ",
    primaryColor = Color(0xFF4F46E5),
    backgroundColor = Color(0xFFF8FAFC),
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
  BLURPLE("blurple", "Sapphire", "ياقوتي", DiscordBlurple),
  NITRO_PINK("nitro_pink", "Rose", "وردي", DiscordNitroPink),
  CYAN("cyan", "Cyan", "سماوي", Color(0xFF06B6D4)),
  EMERALD("emerald", "Emerald", "زمردي", DiscordGreen),
  GOLD("gold", "Amber Gold", "ذهبي", DiscordYellow),
  VIOLET("violet", "Violet", "بنفسجي", Color(0xFF8B5CF6)),
  RED("red", "Coral Red", "مرجاني", DiscordRed);

  companion object {
    fun fromId(id: String?): AccentColorChoice {
      return entries.find { it.id.equals(id, ignoreCase = true) } ?: DEFAULT
    }
  }
}
