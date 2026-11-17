package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class PresetAvatarItem(
  val id: String,
  val nameEn: String,
  val nameAr: String,
  val icon: ImageVector,
  val gradient: List<Color>
)

object PresetAvatars {
  val list: List<PresetAvatarItem> = listOf(
    PresetAvatarItem(
      id = "preset:cyber_bot",
      nameEn = "Cyber Bot",
      nameAr = "الروبوت السيبراني",
      icon = Icons.Default.SmartToy,
      gradient = listOf(Color(0xFF06B6D4), Color(0xFF3B82F6))
    ),
    PresetAvatarItem(
      id = "preset:neon_fox",
      nameEn = "Neon Fox",
      nameAr = "الثعلب النيون",
      icon = Icons.Default.Pets,
      gradient = listOf(Color(0xFFFF007A), Color(0xFFFF8A00))
    ),
    PresetAvatarItem(
      id = "preset:astro_voyager",
      nameEn = "Astro Voyager",
      nameAr = "المستكشف الفضائي",
      icon = Icons.Default.RocketLaunch,
      gradient = listOf(Color(0xFF6366F1), Color(0xFF8B5CF6))
    ),
    PresetAvatarItem(
      id = "preset:lofi_beats",
      nameEn = "Lo-Fi Beats",
      nameAr = "ألحان هادئة",
      icon = Icons.Default.Headphones,
      gradient = listOf(Color(0xFF8B5CF6), Color(0xFFEC4899))
    ),
    PresetAvatarItem(
      id = "preset:royal_crown",
      nameEn = "Royal Crown",
      nameAr = "التاج الملكي",
      icon = Icons.Default.Diamond,
      gradient = listOf(Color(0xFFF59E0B), Color(0xFFEF4444))
    ),
    PresetAvatarItem(
      id = "preset:game_master",
      nameEn = "Game Master",
      nameAr = "لاعب محترف",
      icon = Icons.Default.SportsEsports,
      gradient = listOf(Color(0xFF5865F2), Color(0xFF23A55A))
    ),
    PresetAvatarItem(
      id = "preset:phoenix_fire",
      nameEn = "Phoenix Fire",
      nameAr = "شعلة العنقاء",
      icon = Icons.Default.LocalFireDepartment,
      gradient = listOf(Color(0xFFFF4500), Color(0xFFFFD700))
    ),
    PresetAvatarItem(
      id = "preset:code_wizard",
      nameEn = "Code Wizard",
      nameAr = "مبرمج الظل",
      icon = Icons.Default.Code,
      gradient = listOf(Color(0xFF10B981), Color(0xFF059669))
    )
  )

  fun findById(id: String?): PresetAvatarItem? {
    if (id == null) return null
    return list.firstOrNull { it.id.equals(id, ignoreCase = true) }
  }
}
