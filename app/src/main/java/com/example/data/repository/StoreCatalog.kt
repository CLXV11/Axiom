package com.example.data.repository

import com.example.data.model.DecorationItem
import com.example.data.model.DecorationType
import com.example.data.model.ItemRarity

object StoreCatalog {
  val items: List<DecorationItem> = listOf(
    // ======================== AVATAR FRAMES ========================
    DecorationItem(
      id = "haunted_spirits",
      nameEn = "Haunted Spirits",
      nameAr = "الأرواح المسكونة",
      descriptionEn = "Playful glowing ghosts and a carved jack-o'-lantern guard your portrait.",
      descriptionAr = "تحرس الأرواح المضطربة صورتك مع يقطينة متوهجة وهالة قوطية.",
      price = 100,
      type = DecorationType.AVATAR_FRAME,
      rarity = ItemRarity.EPIC,
      styleKey = "haunted_spirits",
      badgeTagEn = "Halloween 2026",
      badgeTagAr = "هالوين 2026"
    ),
    DecorationItem(
      id = "cat_bell",
      nameEn = "Cat Bell",
      nameAr = "كات بيل",
      descriptionEn = "Plush cat ears, golden bells, and violet silk bows create a sweet vibe.",
      descriptionAr = "آذان القطط والأجراس الذهبية والفيونكات البنفسجية تخلق جواً لطيفاً.",
      price = 200,
      type = DecorationType.AVATAR_FRAME,
      rarity = ItemRarity.LEGENDARY,
      styleKey = "cat_bell"
    ),
    DecorationItem(
      id = "violet_night",
      nameEn = "Violet Night",
      nameAr = "ليلة بنفسجية",
      descriptionEn = "Ornate carved baroque violet leaves add quiet regal elegance.",
      descriptionAr = "تضفي الزخارف والأزهار البنفسجية المنحوتة مظهراً هادئاً وأنيقاً.",
      price = 200,
      type = DecorationType.AVATAR_FRAME,
      rarity = ItemRarity.LEGENDARY,
      styleKey = "violet_night"
    ),
    DecorationItem(
      id = "violet_wreath",
      nameEn = "Violet Wreath",
      nameAr = "إكليل بنفسجي",
      descriptionEn = "Climbing luminous violet blossoms weave a romantic crown.",
      descriptionAr = "تخلق أزهار البنفسج المتسلقة أجواءً رومانسية أنيقة.",
      price = 100,
      type = DecorationType.AVATAR_FRAME,
      rarity = ItemRarity.LEGENDARY,
      styleKey = "violet_wreath"
    ),
    DecorationItem(
      id = "sakura_tide",
      nameEn = "Sakura Tide",
      nameAr = "ساكورا تايد",
      descriptionEn = "Blossoming cherry petals swirl over frothy indigo ocean waves.",
      descriptionAr = "تتداخل أزهار الكرز والأمواج لتشكّل نضارة ناعمة.",
      price = 50,
      type = DecorationType.AVATAR_FRAME,
      rarity = ItemRarity.LEGENDARY,
      styleKey = "sakura_tide"
    ),
    DecorationItem(
      id = "sunlit_pattern",
      nameEn = "Sunlit Pattern",
      nameAr = "نمط مضاء بأشعة الشمس",
      descriptionEn = "Turquoise, crimson, and gilded sun mandala petals radiate warm energy.",
      descriptionAr = "الأنماط الدافئة بالفيروز والذهب تجلب طاقة مشرقة.",
      price = 350,
      type = DecorationType.AVATAR_FRAME,
      rarity = ItemRarity.RARE,
      styleKey = "sunlit_pattern"
    ),
    DecorationItem(
      id = "crimson_silver",
      nameEn = "Crimson Silver",
      nameAr = "فضي قرمزي",
      descriptionEn = "Sculpted metallic silver blades and glowing ruby accents exude pride.",
      descriptionAr = "تضفي الأحجار الكريمة القرمزية والتفاصيل الفضية المنحوتة إحساساً بالفخر.",
      price = 500,
      type = DecorationType.AVATAR_FRAME,
      rarity = ItemRarity.LEGENDARY,
      styleKey = "crimson_silver"
    ),
    DecorationItem(
      id = "skull_eye",
      nameEn = "Skull Eye",
      nameAr = "عين الجمجمة",
      descriptionEn = "Carved bone, thorny spikes, and the mystic third eye radiate dark awe.",
      descriptionAr = "تضفي الجمجمة والعين الغامضة هالة قاتمة ومهيبة.",
      price = 200,
      type = DecorationType.AVATAR_FRAME,
      rarity = ItemRarity.LEGENDARY,
      styleKey = "skull_eye"
    ),
    DecorationItem(
      id = "octopus_crown",
      nameEn = "Abyssal Octopus",
      nameAr = "تاج الهاوية",
      descriptionEn = "Bioluminescent deep-sea purple tentacles coiled around an imperial jewel.",
      descriptionAr = "أذرع أخطبوط بنفسجية متوهجة من الأعماق متوجة بجوهرة ملكية.",
      price = 200,
      type = DecorationType.AVATAR_FRAME,
      rarity = ItemRarity.LEGENDARY,
      styleKey = "octopus_crown"
    ),
    DecorationItem(
      id = "violet_butterfly",
      nameEn = "Violet Butterfly Dream",
      nameAr = "حلم البنفسج",
      descriptionEn = "A romantic dream woven with violet roses, lotus blooms, and butterflies.",
      descriptionAr = "حلم رومانسي منسوج بزهور البنفسج والفراشات المضيئة.",
      price = 350,
      type = DecorationType.AVATAR_FRAME,
      rarity = ItemRarity.RARE,
      styleKey = "violet_butterfly"
    ),
    DecorationItem(
      id = "steampunk_engine",
      nameEn = "Steampunk Engine",
      nameAr = "محرك ستيمكور",
      descriptionEn = "Brass pipes, spinning cogs, pressure dials, and glowing cyan vials.",
      descriptionAr = "خيال صناعي مدعوم بآلات عتيقة وأنابيب نحاسية متوهجة.",
      price = 400,
      type = DecorationType.AVATAR_FRAME,
      rarity = ItemRarity.RARE,
      styleKey = "steampunk_engine"
    ),
    DecorationItem(
      id = "feather_totem",
      nameEn = "Feather Totem",
      nameAr = "طوطم الريشة",
      descriptionEn = "Vibrant turquoise and amber ceremonial feathers woven on a gold crest.",
      descriptionAr = "روح قبلية نابضة بالحياة تتشكل من ريش فيروزي وذهبي ملون.",
      price = 250,
      type = DecorationType.AVATAR_FRAME,
      rarity = ItemRarity.RARE,
      styleKey = "feather_totem"
    ),
    DecorationItem(
      id = "sunlit_garden",
      nameEn = "Sunlit Garden",
      nameAr = "حديقة مشمسة",
      descriptionEn = "Warm cream filigree glowing with bright sunflowers and spring leaves.",
      descriptionAr = "حيوية دافئة تتألق بفضل زهور عباد الشمس وأوراق الشجر.",
      price = 250,
      type = DecorationType.AVATAR_FRAME,
      rarity = ItemRarity.RARE,
      styleKey = "sunlit_garden"
    ),
    DecorationItem(
      id = "future_mech",
      nameEn = "Future Mech",
      nameAr = "الآلي المستقبلي النيوني",
      descriptionEn = "Cybernetic titanium plates and cyan neon plasma arcs from 2099.",
      descriptionAr = "دروع سايبر تقنية مع أضواء نيونية تدور بأسلوب مستقبلي جبار.",
      price = 200,
      type = DecorationType.AVATAR_FRAME,
      rarity = ItemRarity.EPIC,
      styleKey = "future_mech"
    ),
    DecorationItem(
      id = "celestial_wings",
      nameEn = "Celestial Seraph",
      nameAr = "أجنحة الملاك النورانية",
      descriptionEn = "Feathered silver archangel wings crowned by a radiant astral star.",
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
      descriptionEn = "Opulent 24k golden mandala filigree inlaid with royal rubies.",
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
      descriptionEn = "Blazing solar embers and sculpted phoenix flame aura.",
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

    // ======================== ROLE NAMEPLATES ========================
    DecorationItem(
      id = "np_peachy_bubble",
      nameEn = "Peachy Bubble",
      nameAr = "بيتشي بابل",
      descriptionEn = "Peachy-pink blossoms and a glowing blush bubble bring sweet energy.",
      descriptionAr = "الفقاعات ذات اللون الوردي الخوخي مع الزهور تجلب طاقة حلوة.",
      price = 200,
      type = DecorationType.ROLE_NAMEPLATE,
      rarity = ItemRarity.LEGENDARY,
      styleKey = "np_peachy_bubble"
    ),
    DecorationItem(
      id = "np_pear_bubbles",
      nameEn = "Pear Bubbles",
      nameAr = "فقاعات الكمثرى",
      descriptionEn = "Fresh citrus-lime flowers and glowing pear character brighten your badge.",
      descriptionAr = "فقاعات الكمثرى المنعشة تضفي إشراقة وحيوية على مظهرك.",
      price = 200,
      type = DecorationType.ROLE_NAMEPLATE,
      rarity = ItemRarity.LEGENDARY,
      styleKey = "np_pear_bubbles"
    ),
    DecorationItem(
      id = "np_midnight_rose",
      nameEn = "Midnight Rose",
      nameAr = "وردة منتصف الليل",
      descriptionEn = "Crimson roses, satin ribbons, and an antique clock tell a gothic romance.",
      descriptionAr = "تنسج الورود الحمراء والساعة العتيقة قصة حب قاتمة وأنيقة.",
      price = 100,
      type = DecorationType.ROLE_NAMEPLATE,
      rarity = ItemRarity.LEGENDARY,
      styleKey = "np_midnight_rose"
    ),
    DecorationItem(
      id = "np_midnight_haunt",
      nameEn = "Midnight Haunt",
      nameAr = "مطاردة منتصف الليل",
      descriptionEn = "Let your name glow through a moonlit haunted town and lantern mist.",
      descriptionAr = "دع اسمك يسطع في ليلة الهالوين المسكونة والمليئة بالفوانيس.",
      price = 100,
      type = DecorationType.ROLE_NAMEPLATE,
      rarity = ItemRarity.EPIC,
      styleKey = "np_midnight_haunt",
      badgeTagEn = "Halloween 2026",
      badgeTagAr = "هالوين 2026"
    ),
    DecorationItem(
      id = "np_aurora",
      nameEn = "Aurora Galaxy",
      nameAr = "مجرة الشفق القطبي",
      descriptionEn = "Colorful emerald and violet auroras flow into a distant galaxy.",
      descriptionAr = "تتدفق أضواء الشفق القطبي الملونة إلى مجرة متلألئة.",
      price = 100,
      type = DecorationType.ROLE_NAMEPLATE,
      rarity = ItemRarity.LEGENDARY,
      styleKey = "np_aurora"
    ),
    DecorationItem(
      id = "np_prismatic",
      nameEn = "Prismatic Tide",
      nameAr = "المد المنشوري",
      descriptionEn = "Soft rainbow iridescent light ripples over luminescent aqua waves.",
      descriptionAr = "يتدفق ضوء قوس قزح ناعم عبر موجة مائية متلألئة.",
      price = 100,
      type = DecorationType.ROLE_NAMEPLATE,
      rarity = ItemRarity.LEGENDARY,
      styleKey = "np_prismatic"
    ),
    DecorationItem(
      id = "np_golden",
      nameEn = "Golden Glow",
      nameAr = "التوهج الذهبي",
      descriptionEn = "Faceted molten gold shards release dazzling radiant energy.",
      descriptionAr = "يُطلق ضوء ذهبي كريستالي متوهج طاقة مبهرة.",
      price = 100,
      type = DecorationType.ROLE_NAMEPLATE,
      rarity = ItemRarity.LEGENDARY,
      styleKey = "np_golden"
    ),
    DecorationItem(
      id = "np_cracked_stone",
      nameEn = "Cracked Stone",
      nameAr = "حجر متصدع",
      descriptionEn = "Fractured basalt textures reveal stoic, carved ancient power.",
      descriptionAr = "تكشف ملامس الحجر المتشقق والمنحوت عن قوة صارمة.",
      price = 300,
      type = DecorationType.ROLE_NAMEPLATE,
      rarity = ItemRarity.RARE,
      styleKey = "np_cracked_stone"
    ),
    DecorationItem(
      id = "np_deep_space",
      nameEn = "Deep Space Whisper",
      nameAr = "همس الفضاء العميق",
      descriptionEn = "Silhouetted cosmic horizon whispering secrets of the unknown.",
      descriptionAr = "أفق فضائي غامض يهمس بأسرار الكون المجهول.",
      price = 300,
      type = DecorationType.ROLE_NAMEPLATE,
      rarity = ItemRarity.RARE,
      styleKey = "np_deep_space"
    ),
    DecorationItem(
      id = "np_raven_engraving",
      nameEn = "Raven Engraving",
      nameAr = "نقش الغراب",
      descriptionEn = "Dark emerald filigree and cryptic runes weave a mysterious aura.",
      descriptionAr = "ينسج الغراب والرموز الخفية هالة غامضة ومهيبة.",
      price = 300,
      type = DecorationType.ROLE_NAMEPLATE,
      rarity = ItemRarity.RARE,
      styleKey = "np_raven_engraving"
    ),
    DecorationItem(
      id = "np_midnight",
      nameEn = "Midnight Starscape",
      nameAr = "سماء منتصف الليل",
      descriptionEn = "Crystalline sapphire facets shine across a velvet starlight night.",
      descriptionAr = "نجوم كريستالية تشع بريقاً في عمق الليل الكحلي الهادئ.",
      price = 100,
      type = DecorationType.ROLE_NAMEPLATE,
      rarity = ItemRarity.RARE,
      styleKey = "np_midnight"
    ),
    DecorationItem(
      id = "np_moonlit",
      nameEn = "Moonlit Cottage",
      nameAr = "كوخ ضوء القمر",
      descriptionEn = "Warm moonlight illuminates a quiet lakeside fairy-tale sanctuary.",
      descriptionAr = "ضوء القمر الدافئ يضيء ضفاف البحيرة الهادئة وسحر الليل.",
      price = 300,
      type = DecorationType.ROLE_NAMEPLATE,
      rarity = ItemRarity.EPIC,
      styleKey = "np_moonlit"
    ),
    DecorationItem(
      id = "np_bone_noir",
      nameEn = "Bone Noir",
      nameAr = "الظلام الحالك",
      descriptionEn = "Gothic obsidian carvings framed with silver bone filigree.",
      descriptionAr = "نقش قوطي أسود يحمل غموض الظلام ورونق النوار الغامض.",
      price = 350,
      type = DecorationType.ROLE_NAMEPLATE,
      rarity = ItemRarity.EPIC,
      styleKey = "np_bone_noir"
    ),

    // ======================== ACHIEVEMENT / PROFILE WALLS ========================
    DecorationItem(
      id = "wall_moonlight_holo",
      nameEn = "Moonlight Holo",
      nameAr = "مونلايت هولو",
      descriptionEn = "A moonlit lantern sanctuary cradling your proudest achievements.",
      descriptionAr = "ملاذ مضاء بضوء القمر والفوانيس المتوهجة لإنجازاتك الثمينة.",
      price = 100,
      type = DecorationType.ACHIEVEMENT_WALL,
      rarity = ItemRarity.EPIC,
      styleKey = "wall_moonlight_holo",
      badgeTagEn = "Halloween 2026",
      badgeTagAr = "هالوين 2026"
    ),
    DecorationItem(
      id = "wall_digital_matrix",
      nameEn = "Digital Matrix",
      nameAr = "المصفوفة الرقمية",
      descriptionEn = "Step into a glowing emerald digital realm where truth runs in code.",
      descriptionAr = "ادخل عالماً رقمياً متوهجاً حيث تُكتب الحقيقة بالرموز.",
      price = 200,
      type = DecorationType.ACHIEVEMENT_WALL,
      rarity = ItemRarity.LEGENDARY,
      styleKey = "wall_digital_matrix"
    ),
    DecorationItem(
      id = "wall_crimson_forest",
      nameEn = "Crimson Forest",
      nameAr = "الغابة القرمزية",
      descriptionEn = "Deep inside the scarlet woodland mist, twilight pulses with life.",
      descriptionAr = "في أعماق الغابة القرمزية الساحرة، تنبض الظلمة بالحياة.",
      price = 200,
      type = DecorationType.ACHIEVEMENT_WALL,
      rarity = ItemRarity.LEGENDARY,
      styleKey = "wall_crimson_forest"
    ),
    DecorationItem(
      id = "wall_dusk_castle",
      nameEn = "Dusk Castle",
      nameAr = "قلعة الغسق",
      descriptionEn = "An ancient sapphire citadel standing watch beneath stormlit clouds.",
      descriptionAr = "قلعة قديمة تحرس الظلال وأسرار السماء العاصفة المنسية.",
      price = 200,
      type = DecorationType.ACHIEVEMENT_WALL,
      rarity = ItemRarity.LEGENDARY,
      styleKey = "wall_dusk_castle"
    ),
    DecorationItem(
      id = "wall_neon_nexus",
      nameEn = "Neon Nexus",
      nameAr = "نيون نيكسوس",
      descriptionEn = "Symmetrical cybernetic obsidian vault glowing with neon violet cores.",
      descriptionAr = "بوابة سايبرنية متناظرة تتوهج بأضواء النيون البنفسجية.",
      price = 200,
      type = DecorationType.ACHIEVEMENT_WALL,
      rarity = ItemRarity.LEGENDARY,
      styleKey = "wall_neon_nexus"
    ),
    DecorationItem(
      id = "wall_dragon_throne",
      nameEn = "Dragon Throne",
      nameAr = "عرش التنين",
      descriptionEn = "A grand carved obsidian dragon hall flanked by blazing torchlight.",
      descriptionAr = "قاعة عرش التنين المنحوتة في الصخر والمضاءة بالمشاعل الملكية.",
      price = 100,
      type = DecorationType.ACHIEVEMENT_WALL,
      rarity = ItemRarity.LEGENDARY,
      styleKey = "wall_dragon_throne"
    )
  )
}
