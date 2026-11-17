package com.example.data.model

enum class DecorationType {
  AVATAR_FRAME,
  ROLE_NAMEPLATE,
  CHAT_BUBBLE
}

enum class ItemRarity(val labelEn: String, val labelAr: String) {
  COMMON("Common", "عادي"),
  RARE("Rare", "نادر"),
  EPIC("Epic", "ملحمي"),
  LEGENDARY("Legendary", "أسطوري")
}

data class DecorationItem(
  val id: String,
  val nameEn: String,
  val nameAr: String,
  val descriptionEn: String,
  val descriptionAr: String,
  val price: Int,
  val type: DecorationType,
  val rarity: ItemRarity,
  val styleKey: String
)
