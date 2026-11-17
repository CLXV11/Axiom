package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.DecorationItem
import com.example.data.model.DecorationType
import com.example.data.model.ItemRarity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class StoreRepository(context: Context) {
  private val prefs: SharedPreferences =
    context.getSharedPreferences("kora_store_prefs", Context.MODE_PRIVATE)

  private val _coinBalance = MutableStateFlow(prefs.getInt("key_user_coins", 800))
  val coinBalance: StateFlow<Int> = _coinBalance.asStateFlow()

  private val _equippedFrame = MutableStateFlow<String?>(prefs.getString("key_equipped_frame", null))
  val equippedFrame: StateFlow<String?> = _equippedFrame.asStateFlow()

  private val _equippedNameplate = MutableStateFlow<String?>(prefs.getString("key_equipped_nameplate", null))
  val equippedNameplate: StateFlow<String?> = _equippedNameplate.asStateFlow()

  private val ownedIds = prefs.getStringSet("key_owned_ids", mutableSetOf())?.toMutableSet() ?: mutableSetOf()

  val catalog: List<DecorationItem> = listOf(
    // Avatar Frames (From Reference Screenshots & Custom Incredible Designs)
    DecorationItem(
      id = "cat_bell",
      nameEn = "Cat Bell",
      nameAr = "أجراس القطة اللطيفة",
      descriptionEn = "Cat ears, bells, and golden ribbons create a playful aura.",
      descriptionAr = "آذان قطة بيضاء مع أشرطة بنفسجية وأجراس ذهبية مبهجة.",
      price = 200,
      type = DecorationType.AVATAR_FRAME,
      rarity = ItemRarity.RARE,
      styleKey = "cat_bell"
    ),
    DecorationItem(
      id = "violet_night",
      nameEn = "Violet Night",
      nameAr = "ليلة البنفسج الإمبراطورية",
      descriptionEn = "Ornate baroque violet florals create a calm, regal presence.",
      descriptionAr = "زخارف ملكية باروكية بنفسجية مذهلة تضفي فخامة استثنائية.",
      price = 200,
      type = DecorationType.AVATAR_FRAME,
      rarity = ItemRarity.RARE,
      styleKey = "violet_night"
    ),
    DecorationItem(
      id = "skull_eye",
      nameEn = "Skull Eye",
      nameAr = "عين الجمجمة القوطية",
      descriptionEn = "A silver skull and mystic eye radiate an enigmatic dark aura.",
      descriptionAr = "جمجمة فضية منقوشة مع عين غامضة تنبض بهيبة مظلمة.",
      price = 200,
      type = DecorationType.AVATAR_FRAME,
      rarity = ItemRarity.EPIC,
      styleKey = "skull_eye"
    ),
    DecorationItem(
      id = "octopus_crown",
      nameEn = "Octopus Crown",
      nameAr = "تاج الأعماق البنفسجي",
      descriptionEn = "Luminescent deep-sea tentacles crowned with imperial jewels.",
      descriptionAr = "أذرع أخطبوط مضيئة من الأعماق متوجة بجواهر ملكية براقة.",
      price = 200,
      type = DecorationType.AVATAR_FRAME,
      rarity = ItemRarity.EPIC,
      styleKey = "octopus_crown"
    ),
    DecorationItem(
      id = "future_mech",
      nameEn = "Future Mech",
      nameAr = "الآلي المستقبلي النيوني",
      descriptionEn = "Cybernetic armor plates and cyan energy nodes from 2099.",
      descriptionAr = "دروع سايبر تقنية مع أضواء نيونية تدور بأسلوب مستقبلي جبار.",
      price = 200,
      type = DecorationType.AVATAR_FRAME,
      rarity = ItemRarity.EPIC,
      styleKey = "future_mech"
    ),
    DecorationItem(
      id = "sakura_tide",
      nameEn = "Sakura Tide",
      nameAr = "مد أزهار الكرز",
      descriptionEn = "Delicate spring blossoms swirling with pastel ocean waves.",
      descriptionAr = "بتلات ساكورا رقيقة تتناغم مع نسيم الربيع وأمواج الباستيل.",
      price = 50,
      type = DecorationType.AVATAR_FRAME,
      rarity = ItemRarity.COMMON,
      styleKey = "sakura_tide"
    ),
    DecorationItem(
      id = "celestial_wings",
      nameEn = "Celestial Seraph",
      nameAr = "أجنحة الملاك النورانية",
      descriptionEn = "Divine silver-white wings flanked by a radiant celestial star.",
      descriptionAr = "أجنحة ملائكية نقية مع هالة نورانية ونجمة ماسية ساحرة.",
      price = 500,
      type = DecorationType.AVATAR_FRAME,
      rarity = ItemRarity.LEGENDARY,
      styleKey = "celestial_wings"
    ),
    DecorationItem(
      id = "imperial_gold",
      nameEn = "Imperial Sunburst",
      nameAr = "التاج الشمسي الملكي",
      descriptionEn = "Opulent golden mandala filigree with embedded royal rubies.",
      descriptionAr = "زخرفة ماندالا ذهبية نقية مرصعة بالياقوت الملكي الفاخر.",
      price = 400,
      type = DecorationType.AVATAR_FRAME,
      rarity = ItemRarity.LEGENDARY,
      styleKey = "imperial_gold"
    ),
    DecorationItem(
      id = "dragon_flame",
      nameEn = "Infernal Phoenix",
      nameAr = "لهيب العنقاء الأسطوري",
      descriptionEn = "Blazing magma embers and blazing dragon flame aura.",
      descriptionAr = "ألسنة لهب أسطورية وشظايا بركانية مشتعلة تفيض بالقوة.",
      price = 450,
      type = DecorationType.AVATAR_FRAME,
      rarity = ItemRarity.LEGENDARY,
      styleKey = "dragon_flame"
    ),
    DecorationItem(
      id = "cosmic_swirl",
      nameEn = "Cosmic Swirl",
      nameAr = "الدوامة الكونية النجمية",
      descriptionEn = "Hypnotic starlight vortex shifting across iridescent galaxies.",
      descriptionAr = "دوامة نجمية غامضة بألوان المجرات المتلألئة وغبار النجوم.",
      price = 300,
      type = DecorationType.AVATAR_FRAME,
      rarity = ItemRarity.RARE,
      styleKey = "cosmic_swirl"
    ),

    // Role Nameplates (From Reference Screenshots)
    DecorationItem(
      id = "np_aurora",
      nameEn = "Aurora Galaxy",
      nameAr = "شريط الشفق القطبي",
      descriptionEn = "Colorful auroras flow into a sparkling distant galaxy.",
      descriptionAr = "أضواء الشفق المتدفقة بألوان كونية خلابة ونجوم ساطعة.",
      price = 100,
      type = DecorationType.ROLE_NAMEPLATE,
      rarity = ItemRarity.RARE,
      styleKey = "np_aurora"
    ),
    DecorationItem(
      id = "np_prismatic",
      nameEn = "Prismatic Tide",
      nameAr = "المد المنشوري الساحر",
      descriptionEn = "Soft rainbow iridescent light flowing across aqua waters.",
      descriptionAr = "أطياف قوس قزح ناعمة تتدفق فوق أمواج فيروزية نقية.",
      price = 100,
      type = DecorationType.ROLE_NAMEPLATE,
      rarity = ItemRarity.RARE,
      styleKey = "np_prismatic"
    ),
    DecorationItem(
      id = "np_golden",
      nameEn = "Golden Flare",
      nameAr = "التوهج الشمسي الذهبي",
      descriptionEn = "Fiery crystalline gold releasing dazzling, radiant energy.",
      descriptionAr = "طاقة شمسية ذهبية مبهرة بطبقات بلورية متألقة.",
      price = 100,
      type = DecorationType.ROLE_NAMEPLATE,
      rarity = ItemRarity.RARE,
      styleKey = "np_golden"
    ),
    DecorationItem(
      id = "np_midnight",
      nameEn = "Midnight Starscape",
      nameAr = "سماء منتصف الليل",
      descriptionEn = "Crystalline stars shine across a velvet deep-blue night.",
      descriptionAr = "نجوم كريستالية تشع بريقًا في عمق الليل الكحلي الهادئ.",
      price = 100,
      type = DecorationType.ROLE_NAMEPLATE,
      rarity = ItemRarity.RARE,
      styleKey = "np_midnight"
    ),
    DecorationItem(
      id = "np_moonlit",
      nameEn = "Moonlit Cottage",
      nameAr = "كوخ ضوء القمر",
      descriptionEn = "Warm moonlight embraces a quiet lakeside fairy-tale sanctuary.",
      descriptionAr = "ضوء القمر الدافئ يضيء ضفاف البحيرة الهادئة وسحر الليل.",
      price = 300,
      type = DecorationType.ROLE_NAMEPLATE,
      rarity = ItemRarity.EPIC,
      styleKey = "np_moonlit"
    ),
    DecorationItem(
      id = "np_bone_noir",
      nameEn = "Bone Noir",
      nameAr = "الظلام الحالك والجمجمة",
      descriptionEn = "A chilling dark gothic skull gaze shrouded in dark velvet.",
      descriptionAr = "نقش قوطي أسود يحمل غموض الظلام ورونق النوار الغامض.",
      price = 350,
      type = DecorationType.ROLE_NAMEPLATE,
      rarity = ItemRarity.EPIC,
      styleKey = "np_bone_noir"
    )
  )

  fun isOwned(itemId: String): Boolean {
    return ownedIds.contains(itemId)
  }

  fun isEquipped(itemId: String): Boolean {
    return _equippedFrame.value == itemId || _equippedNameplate.value == itemId
  }

  fun buyItem(item: DecorationItem): Boolean {
    val current = _coinBalance.value
    if (current < item.price) return false

    val newBalance = current - item.price
    _coinBalance.value = newBalance
    prefs.edit().putInt("key_user_coins", newBalance).apply()

    ownedIds.add(item.id)
    prefs.edit().putStringSet("key_owned_ids", ownedIds).apply()

    // Auto-equip upon purchase
    equipItem(item)
    return true
  }

  fun equipItem(item: DecorationItem) {
    if (item.type == DecorationType.AVATAR_FRAME) {
      _equippedFrame.value = item.id
      prefs.edit().putString("key_equipped_frame", item.id).apply()
    } else if (item.type == DecorationType.ROLE_NAMEPLATE) {
      _equippedNameplate.value = item.id
      prefs.edit().putString("key_equipped_nameplate", item.id).apply()
    }
  }

  fun unequipFrame() {
    _equippedFrame.value = null
    prefs.edit().remove("key_equipped_frame").apply()
  }

  fun unequipNameplate() {
    _equippedNameplate.value = null
    prefs.edit().remove("key_equipped_nameplate").apply()
  }

  fun claimDailyReward(): Int {
    val reward = 250
    val newBalance = _coinBalance.value + reward
    _coinBalance.value = newBalance
    prefs.edit().putInt("key_user_coins", newBalance).apply()
    return reward
  }

  fun addCoins(amount: Int) {
    val newBalance = _coinBalance.value + amount
    _coinBalance.value = newBalance
    prefs.edit().putInt("key_user_coins", newBalance).apply()
  }
}
