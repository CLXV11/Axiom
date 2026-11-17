package com.example.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Professional Discord & Nitro Theme Presets
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
    nameEn = "Discord Dark",
    nameAr = "ديسكورد داكن (كلاسيك)",
    subtitleEn = "The iconic Discord aesthetic",
    subtitleAr = "مظهر ديسكورد الأصلي الداكن",
    primaryColor = DiscordBlurple,
    backgroundColor = DiscordDarkPrimary,
    surfaceColor = DiscordDarkSecondary,
    isDark = true
  ),
  OLED_MIDNIGHT(
    id = "oled_midnight",
    nameEn = "Midnight OLED",
    nameAr = "منتصف الليل (سواد OLED)",
    subtitleEn = "Pure pitch black for battery saving",
    subtitleAr = "سواد تام 100% لتوفير فائق للبطارية",
    primaryColor = Color(0xFF00D2FF),
    backgroundColor = Color(0xFF000000),
    surfaceColor = Color(0xFF0E1015),
    isDark = true
  ),
  NITRO_MAGENTA(
    id = "nitro_magenta",
    nameEn = "Nitro Synthwave",
    nameAr = "نيترو سينثويف",
    subtitleEn = "Cyberpunk neon purple & magenta",
    subtitleAr = "بنفسجي وماجنتا نيون سايبربانك",
    primaryColor = DiscordNitroPink,
    backgroundColor = Color(0xFF130E1A),
    surfaceColor = Color(0xFF22162E),
    isDark = true
  ),
  CYBER_EMERALD(
    id = "cyber_emerald",
    nameEn = "Cyber Emerald",
    nameAr = "زمرد سايبر (ماتريكس)",
    subtitleEn = "Matrix terminal green on dark slate",
    subtitleAr = "أخضر زمردي نيون عالي التباين",
    primaryColor = DiscordGreen,
    backgroundColor = Color(0xFF0B1410),
    surfaceColor = Color(0xFF15261F),
    isDark = true
  ),
  CRIMSON_RUBY(
    id = "crimson_ruby",
    nameEn = "Crimson Ruby",
    nameAr = "ياقوت قرمزي",
    subtitleEn = "Deep wine charcoal & vivid red",
    subtitleAr = "فحم نبيذي داكن مع أحمر ياقوتي",
    primaryColor = DiscordRed,
    backgroundColor = Color(0xFF160B0E),
    surfaceColor = Color(0xFF27151A),
    isDark = true
  ),
  SOLAR_AMBER(
    id = "solar_amber",
    nameEn = "Solar Amber",
    nameAr = "كهرمان ذهبي",
    subtitleEn = "Warm obsidian & radiant amber gold",
    subtitleAr = "سبج دافئ مع ذهب كهرماني مشع",
    primaryColor = DiscordYellow,
    backgroundColor = Color(0xFF16120B),
    surfaceColor = Color(0xFF271F14),
    isDark = true
  ),
  DISCORD_LIGHT(
    id = "discord_light",
    nameEn = "Discord Light",
    nameAr = "ديسكورد ناصع (فاتح)",
    subtitleEn = "Crisp light canvas with Blurple",
    subtitleAr = "مظهر فاتح ناصع مع أزرق Blurple",
    primaryColor = DiscordBlurple,
    backgroundColor = PureWhite,
    surfaceColor = Color(0xFFF2F3F5),
    isDark = false
  ),
  SYSTEM(
    id = "system",
    nameEn = "System Default",
    nameAr = "تلقائي حسب النظام",
    subtitleEn = "Follows Android system appearance",
    subtitleAr = "يتبع إعدادات النظام تلقائياً",
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
 * Custom Accent Color Options
 */
enum class AccentColorChoice(
  val id: String,
  val nameEn: String,
  val nameAr: String,
  val color: Color
) {
  DEFAULT("default", "Theme Default", "افتراضي السمة", Color.Unspecified),
  BLURPLE("blurple", "Discord Blurple", "أزرق Blurple", DiscordBlurple),
  NITRO_PINK("nitro_pink", "Nitro Pink", "وردي نيترو", DiscordNitroPink),
  CYAN("cyan", "Cyber Cyan", "سماوي سايبر", Color(0xFF00E5FF)),
  EMERALD("emerald", "Emerald Green", "أخضر زمردي", DiscordGreen),
  GOLD("gold", "Solar Gold", "ذهبي كهرماني", DiscordYellow),
  VIOLET("violet", "Electric Violet", "بنفسجي كهربائي", Color(0xFFA855F7)),
  RED("red", "Ruby Red", "أحمر ياقوتي", DiscordRed);

  companion object {
    fun fromId(id: String?): AccentColorChoice {
      return entries.find { it.id.equals(id, ignoreCase = true) } ?: DEFAULT
    }
  }
}
