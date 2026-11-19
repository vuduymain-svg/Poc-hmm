package com.example.model

object CardCatalog {

    val items: List<ItemTemplate> = listOf(
        ItemTemplate(
            id = "item_studded_leather",
            name = "Studded Leather",
            rarity = Rarity.COMMON,
            category = ItemCategory.STAT_KEYWORD,
            description = "+1|+1",
            bonusPower = 1,
            bonusHealth = 1
        ),
        ItemTemplate(
            id = "item_phage",
            name = "Phage",
            rarity = Rarity.RARE,
            category = ItemCategory.STAT_KEYWORD,
            description = "+2|+2",
            bonusPower = 2,
            bonusHealth = 2
        ),
        ItemTemplate(
            id = "item_pickaxe",
            name = "Pickaxe",
            rarity = Rarity.COMMON,
            category = ItemCategory.STAT_KEYWORD,
            description = "+2|+0",
            bonusPower = 2
        ),
        ItemTemplate(
            id = "item_giants_belt",
            name = "Giant's Belt",
            rarity = Rarity.COMMON,
            category = ItemCategory.STAT_KEYWORD,
            description = "+0|+2",
            bonusHealth = 2
        ),
        ItemTemplate(
            id = "item_quickstrike",
            name = "Quickstrike Blade",
            rarity = Rarity.RARE,
            category = ItemCategory.STAT_KEYWORD,
            description = "Quick Attack",
            grantedKeyword = Keyword.QUICK_ATTACK
        ),
        ItemTemplate(
            id = "item_double_strike",
            name = "Double Strike Rune",
            rarity = Rarity.EPIC,
            category = ItemCategory.STAT_KEYWORD,
            description = "Double Attack",
            grantedKeyword = Keyword.DOUBLE_ATTACK
        ),
        ItemTemplate(
            id = "item_colossus",
            name = "Colossus Hammer",
            rarity = Rarity.EPIC,
            category = ItemCategory.STAT_KEYWORD,
            description = "+3|+3 and Overwhelm",
            bonusPower = 3,
            bonusHealth = 3,
            grantedKeyword = Keyword.OVERWHELM
        ),
        ItemTemplate(
            id = "item_elusive_cloak",
            name = "Shadow Cloak",
            rarity = Rarity.RARE,
            category = ItemCategory.STAT_KEYWORD,
            description = "Elusive",
            grantedKeyword = Keyword.ELUSIVE
        ),
        ItemTemplate(
            id = "item_lifesteal_orb",
            name = "Vampiric Scepter",
            rarity = Rarity.EPIC,
            category = ItemCategory.STAT_KEYWORD,
            description = "Lifesteal",
            grantedKeyword = Keyword.LIFESTEAL
        ),
        ItemTemplate(
            id = "item_deathless_aegis",
            name = "Deathless Aegis",
            rarity = Rarity.EPIC,
            category = ItemCategory.STAT_KEYWORD,
            description = "Deathless",
            grantedKeyword = Keyword.DEATHLESS
        ),
        ItemTemplate(
            id = "item_charging_sigil",
            name = "Charging Sigil I",
            rarity = Rarity.COMMON,
            category = ItemCategory.ECONOMY,
            description = "-1 Cost",
            costReduction = 1
        ),
        ItemTemplate(
            id = "item_nomads_medallion",
            name = "Nomad's Medallion",
            rarity = Rarity.EPIC,
            category = ItemCategory.ECONOMY,
            description = "-2 Cost",
            costReduction = 2
        ),
        ItemTemplate(
            id = "item_farsight",
            name = "Farsight Alteration",
            rarity = Rarity.RARE,
            category = ItemCategory.TRIGGER,
            description = "Game Start: Draw me if not in hand.",
            farsightGameStartDraw = true
        ),
        ItemTemplate(
            id = "item_shadow_totem",
            name = "Shadow Totem",
            rarity = Rarity.EPIC,
            category = ItemCategory.TRIGGER,
            description = "Summon: Summon an Ephemeral copy of me.",
            onSummonEphemeralCopy = true
        ),
        ItemTemplate(
            id = "item_hextech_fabricator",
            name = "Hextech Fabricator",
            rarity = Rarity.RARE,
            category = ItemCategory.TRIGGER,
            description = "Summon: Grant me a random positive keyword & Spirit.",
            spiritStacks = 1,
            grantRandomKeywordOnSummon = true
        ),
        ItemTemplate(
            id = "item_mana_gem",
            name = "Tear of the Goddess",
            rarity = Rarity.RARE,
            category = ItemCategory.ECONOMY,
            description = "-1 Cost and Play: Draw 1.",
            costReduction = 1,
            onPlayDrawOne = true
        )
    )

    val relics: List<RelicTemplate> = listOf(
        RelicTemplate(
            id = "relic_stalkers_blade",
            name = "Stalker's Blade",
            rarity = Rarity.RARE,
            description = "When I'm summoned, I strike the weakest enemy.",
            strikeWeakestOnSummon = true
        ),
        RelicTemplate(
            id = "relic_archangels_staff",
            name = "Archangel's Staff",
            rarity = Rarity.RARE,
            description = "Round Start: Refill your Spell Mana.",
            refillSpellManaRoundStart = true
        ),
        RelicTemplate(
            id = "relic_ludens_tempest",
            name = "Luden's Tempest",
            rarity = Rarity.RARE,
            description = "All your spells and skills deal 1 extra damage.",
            spellSkillDamageBonus = 1
        ),
        RelicTemplate(
            id = "relic_guinsoos_rageblade",
            name = "Guinsoo's Rageblade",
            rarity = Rarity.COMMON,
            description = "Attack: Grant me +1|+1.",
            attackGainPlusOnePlusOne = true
        ),
        RelicTemplate(
            id = "relic_crownguard",
            name = "Crownguard Inheritance",
            rarity = Rarity.RARE,
            description = "When I Level Up, Rally.",
            levelUpRally = true
        ),
        RelicTemplate(
            id = "relic_troll_kings_crown",
            name = "Troll King's Crown",
            rarity = Rarity.RARE,
            description = "Allies have Overwhelm.",
            grantedKeyword = Keyword.OVERWHELM
        ),
        RelicTemplate(
            id = "relic_banshees_veil",
            name = "Banshee's Veil",
            rarity = Rarity.COMMON,
            description = "SpellShield.",
            grantedKeyword = Keyword.SPELLSHIELD
        ),
        RelicTemplate(
            id = "relic_starforged",
            name = "Starforged Gauntlets",
            rarity = Rarity.EPIC,
            description = "+1|+3 and +1 Starting Mana.",
            bonusPower = 1,
            bonusHealth = 3,
            startingManaBonus = 1
        ),
        RelicTemplate(
            id = "relic_grand_generals",
            name = "Grand General's Counterplan",
            rarity = Rarity.RARE,
            description = "Round Start: Create a Fleeting copy of Champion Spell in hand.",
            createChampionSpellRoundStart = true
        )
    )

    val powers: List<PowerTemplate> = listOf(
        PowerTemplate("pow_domination", "Domination", Rarity.RARE, "Round Start: Rally (gain the Attack Token every round)."),
        PowerTemplate("pow_evolution", "Evolution", Rarity.EPIC, "Allies have +1|+1 for each positive keyword they have."),
        PowerTemplate("pow_sorcery", "Sorcery", Rarity.RARE, "Round Start: Refill your Spell Mana."),
        PowerTemplate("pow_spellslinger", "Spellslinger", Rarity.COMMON, "Your spells cost 1 less."),
        PowerTemplate("pow_trifarian_might", "Trifarian Might", Rarity.EPIC, "When you summon an ally with 5+ Power, it strikes the weakest enemy."),
        PowerTemplate("pow_sharing_is_caring", "Sharing is Caring", Rarity.EPIC, "When you summon an ally, grant its positive keywords to all allies."),
        PowerTemplate("pow_wild_inspiration", "Wild Inspiration", Rarity.COMMON, "Your created cards cost 1 less."),
        PowerTemplate("pow_raiding_party", "Raiding Party", Rarity.COMMON, "When you damage the enemy Nexus, grant the top unit in hand +1|+1."),
        PowerTemplate("pow_enfeebling_strike", "Enfeebling Strike", Rarity.COMMON, "When you damage an enemy unit, reduce its Power by the damage dealt."),
        PowerTemplate("pow_stabilize", "Stabilize", Rarity.EPIC, "When you summon a Champion, summon an Ephemeral copy of it."),
        PowerTemplate("pow_vitality", "Bountiful Vitality", Rarity.RARE, "Game Start & Round Start: Heal your Nexus 2."),
        PowerTemplate("pow_manaflow", "Perfected Manaflow", Rarity.LEGENDARY, "Round Start: Gain +1 Max Mana gem this round (up to 10).")
    )

    val cards: Map<String, CardTemplate> = listOf(
        // Champions
        CardTemplate(
            id = "card_jinx", name = "Jinx", region = Region.PILTOVER_ZAUN, type = CardType.CHAMPION,
            cost = 4, basePower = 4, baseHealth = 3, keywords = setOf(Keyword.QUICK_ATTACK),
            isChampion = true, levelUpRequirementText = "Empty your hand or play 2+ spells",
            description = "Quick Attack. Level Up: Deals 4 to enemy Nexus and generates Super Mega Death Rocket!",
            levelUpPowerBonus = 1, levelUpHealthBonus = 1
        ),
        CardTemplate(
            id = "card_lux", name = "Lux", region = Region.DEMACIA, type = CardType.CHAMPION,
            cost = 5, basePower = 3, baseHealth = 5, keywords = setOf(Keyword.BARRIER),
            isChampion = true, levelUpRequirementText = "Spend 6+ Mana on spells",
            description = "Barrier. Level Up: Create a 0-cost Final Spark (Fast: Deal 4 to enemy & Overwhelm Nexus).",
            levelUpPowerBonus = 1, levelUpHealthBonus = 1
        ),
        CardTemplate(
            id = "card_yasuo", name = "Yasuo", region = Region.IONIA, type = CardType.CHAMPION,
            cost = 4, basePower = 4, baseHealth = 4, keywords = setOf(Keyword.QUICK_ATTACK),
            isChampion = true, levelUpRequirementText = "Stun 3+ enemies",
            description = "Quick Attack. When you Stun an enemy, Yasuo strikes it for 2 (or full Power when leveled).",
            levelUpPowerBonus = 1, levelUpHealthBonus = 1
        ),
        CardTemplate(
            id = "card_garen", name = "Garen", region = Region.DEMACIA, type = CardType.CHAMPION,
            cost = 5, basePower = 5, baseHealth = 5, keywords = setOf(Keyword.REGENERATION),
            isChampion = true, levelUpRequirementText = "Strike 2 times",
            description = "Regeneration. Level Up: Round Start: Rally!",
            levelUpPowerBonus = 1, levelUpHealthBonus = 1
        ),
        CardTemplate(
            id = "card_ashe", name = "Ashe", region = Region.FRELJORD, type = CardType.CHAMPION,
            cost = 4, basePower = 5, baseHealth = 3, keywords = setOf(Keyword.BRASH),
            isChampion = true, levelUpRequirementText = "Frostbite 3+ enemies",
            description = "Brash. Attack: Frostbite the strongest enemy. Enemies with 0 Power can't block.",
            levelUpPowerBonus = 1, levelUpHealthBonus = 1, rarity = Rarity.LEGENDARY
        ),
        CardTemplate(
            id = "card_anivia", name = "Anivia", region = Region.FRELJORD, type = CardType.CHAMPION,
            cost = 6, basePower = 3, baseHealth = 5, keywords = setOf(Keyword.ELEMENTAL),
            isChampion = true, levelUpRequirementText = "Enlightened (10 Max Mana)",
            description = "Elemental. Attack: Glacial Storm deals 1 (2 Leveled) to all enemies & enemy Nexus. Last Breath: Revive as Eggnivia.",
            levelUpPowerBonus = 1, levelUpHealthBonus = 1, rarity = Rarity.LEGENDARY
        ),
        // AI Boss Champions & Reinforcement Champions
        CardTemplate(
            id = "card_zed", name = "Zed", region = Region.IONIA, type = CardType.CHAMPION,
            cost = 3, basePower = 3, baseHealth = 2, keywords = setOf(Keyword.QUICK_ATTACK),
            isChampion = true, description = "Quick Attack. Attack: Summon an attacking Living Shadow."
        ),
        CardTemplate(
            id = "card_karma", name = "Karma", region = Region.IONIA, type = CardType.CHAMPION,
            cost = 5, basePower = 4, baseHealth = 4, keywords = setOf(Keyword.ATTUNE),
            isChampion = true, description = "Attune. Round End: Create a random spell. Enlightened: Double spells."
        ),
        CardTemplate(
            id = "card_viktor", name = "Viktor", region = Region.PILTOVER_ZAUN, type = CardType.CHAMPION,
            cost = 4, basePower = 2, baseHealth = 4, keywords = setOf(Keyword.AUGMENT),
            isChampion = true, description = "Augment. Round Start: Create Hex Core Upgrade to gain a random keyword."
        ),
        CardTemplate(
            id = "card_aurelion_sol", name = "Aurelion Sol", region = Region.TARGON, type = CardType.CHAMPION,
            cost = 8, basePower = 8, baseHealth = 8, keywords = setOf(Keyword.FURY, Keyword.SPELLSHIELD),
            isChampion = true, description = "Fury, SpellShield. Round Start: Invoke a Celestial powerhouse."
        ),

        // Followers (Covering all PoC Keywords)
        CardTemplate(
            id = "card_zaunite_urchin", name = "Zaunite Urchin", region = Region.PILTOVER_ZAUN, type = CardType.UNIT,
            cost = 1, basePower = 2, baseHealth = 1, keywords = setOf(Keyword.AUGMENT),
            description = "Augment. Play: Draw 1."
        ),
        CardTemplate(
            id = "card_boomcrew_rookie", name = "Boomcrew Rookie", region = Region.PILTOVER_ZAUN, type = CardType.UNIT,
            cost = 2, basePower = 1, baseHealth = 3, keywords = setOf(Keyword.IMPACT), impactStacks = 1,
            description = "Impact 1. Attack: Deal 1 to the enemy Nexus."
        ),
        CardTemplate(
            id = "card_sumpworks_mapper", name = "Daring Poro", region = Region.PILTOVER_ZAUN, type = CardType.UNIT,
            cost = 1, basePower = 1, baseHealth = 1, keywords = setOf(Keyword.ELUSIVE),
            description = "Elusive."
        ),
        CardTemplate(
            id = "card_ballistic_bot", name = "Ballistic Bot", region = Region.PILTOVER_ZAUN, type = CardType.UNIT,
            cost = 2, basePower = 1, baseHealth = 3, keywords = setOf(Keyword.AUGMENT),
            description = "Augment. Round Start: Create a Fleeting Ignition (Burst: Deal 1 to enemy Nexus)."
        ),
        CardTemplate(
            id = "card_cithria", name = "Cithria of Cloudfield", region = Region.DEMACIA, type = CardType.UNIT,
            cost = 1, basePower = 2, baseHealth = 2, keywords = setOf(Keyword.TOUGH),
            description = "Tough. Reliable vanguard defender."
        ),
        CardTemplate(
            id = "card_brightsteel_protector", name = "Brightsteel Protector", region = Region.DEMACIA, type = CardType.UNIT,
            cost = 2, basePower = 3, baseHealth = 2, keywords = setOf(Keyword.SHARPSIGHT),
            description = "Sharpsight. Play: Give strongest ally Barrier this round."
        ),
        CardTemplate(
            id = "card_laurent_protege", name = "Laurent Protege", region = Region.DEMACIA, type = CardType.UNIT,
            cost = 3, basePower = 2, baseHealth = 4, keywords = setOf(Keyword.CHALLENGER),
            description = "Challenger. Pull priority enemy threats into combat."
        ),
        CardTemplate(
            id = "card_radiant_guardian", name = "Radiant Guardian", region = Region.DEMACIA, type = CardType.UNIT,
            cost = 5, basePower = 4, baseHealth = 5, keywords = setOf(Keyword.TOUGH, Keyword.LIFESTEAL),
            description = "Tough, Lifesteal. Restores Nexus Health every strike."
        ),
        CardTemplate(
            id = "card_greenfang_warden", name = "Greenfang Warden", region = Region.DEMACIA, type = CardType.UNIT,
            cost = 3, basePower = 2, baseHealth = 2, keywords = setOf(Keyword.SCOUT, Keyword.BARRIER),
            description = "Scout, Barrier."
        ),
        CardTemplate(
            id = "card_fae_blader", name = "Fae Bladetwirler", region = Region.IONIA, type = CardType.UNIT,
            cost = 2, basePower = 1, baseHealth = 3, keywords = setOf(Keyword.QUICK_ATTACK),
            description = "Quick Attack. Gains +2|+0 whenever you Stun an enemy."
        ),
        CardTemplate(
            id = "card_kinkou_student", name = "Kinkou Lifeblade", region = Region.IONIA, type = CardType.UNIT,
            cost = 4, basePower = 2, baseHealth = 3, keywords = setOf(Keyword.ELUSIVE, Keyword.LIFESTEAL),
            description = "Elusive, Lifesteal."
        ),
        CardTemplate(
            id = "card_shadow_assassin", name = "Shadow Assassin", region = Region.IONIA, type = CardType.UNIT,
            cost = 3, basePower = 2, baseHealth = 2, keywords = setOf(Keyword.ELUSIVE),
            description = "Elusive. Play: Draw 1."
        ),
        CardTemplate(
            id = "card_solari_soldier", name = "Solari Shieldbearer", region = Region.TARGON, type = CardType.UNIT,
            cost = 2, basePower = 3, baseHealth = 4, keywords = setOf(Keyword.FATED, Keyword.DAYBREAK),
            description = "Fated. Daybreak: Gain +1|+1 and Challenger."
        ),
        CardTemplate(
            id = "card_petricite_broadwing", name = "Petricite Broadwing", region = Region.DEMACIA, type = CardType.UNIT,
            cost = 2, basePower = 0, baseHealth = 3, keywords = setOf(Keyword.FORMIDABLE, Keyword.CHALLENGER),
            description = "Formidable, Challenger. Strikes using its current Health."
        ),
        CardTemplate(
            id = "card_void_abomination", name = "Voidling", region = Region.RUNETERRA, type = CardType.UNIT,
            cost = 3, basePower = 3, baseHealth = 3, keywords = setOf(Keyword.EVOLVE, Keyword.SPIRIT), spiritStacks = 1,
            description = "Evolve, Spirit 1. Gains +2|+2 once 6 unique positive keywords are reached."
        ),
        CardTemplate(
            id = "card_spectral_ironhound", name = "Phantom Prankster", region = Region.SHADOW_ISLES, type = CardType.UNIT,
            cost = 3, basePower = 2, baseHealth = 3, keywords = setOf(Keyword.HALLOWED, Keyword.FEARSOME),
            description = "Hallowed, Fearsome. On death, empowers Hallowed strikes."
        ),
        CardTemplate(
            id = "card_rekindled_knight", name = "Immortal Knight", region = Region.SHADOW_ISLES, type = CardType.UNIT,
            cost = 4, basePower = 3, baseHealth = 3, keywords = setOf(Keyword.DEATHLESS, Keyword.FURY),
            description = "Deathless, Fury."
        ),
        CardTemplate(
            id = "card_icevale_archer", name = "Icevale Archer", region = Region.FRELJORD, type = CardType.UNIT,
            cost = 2, basePower = 3, baseHealth = 1, keywords = setOf(Keyword.BRASH),
            description = "Brash. Play: Frostbite the strongest enemy.",
            effectType = CardEffectType.FROSTBITE_TARGET, effectValue = 1
        ),
        CardTemplate(
            id = "card_ruthless_raider", name = "Ruthless Raider", region = Region.FRELJORD, type = CardType.UNIT,
            cost = 2, basePower = 3, baseHealth = 1, keywords = setOf(Keyword.OVERWHELM, Keyword.TOUGH),
            description = "Overwhelm, Tough."
        ),
        // Anivia Deck Followers & Freljord Giants
        CardTemplate(
            id = "card_wild_mystics", name = "Wild Mystics", region = Region.FRELJORD, type = CardType.UNIT,
            cost = 2, basePower = 2, baseHealth = 2, keywords = setOf(Keyword.OVERWHELM),
            description = "Overwhelm. Enlightened: Gain +4|+4.",
            rarity = Rarity.COMMON
        ),
        CardTemplate(
            id = "card_mortal_prophet", name = "Mortal Prophet", region = Region.FRELJORD, type = CardType.UNIT,
            cost = 2, basePower = 0, baseHealth = 5, keywords = emptySet(),
            description = "When I survive damage, deal 1 to the enemy Nexus.",
            rarity = Rarity.COMMON
        ),
        CardTemplate(
            id = "card_guardian_follower", name = "Guardian Follower", region = Region.FRELJORD, type = CardType.UNIT,
            cost = 3, basePower = 3, baseHealth = 4, keywords = emptySet(),
            description = "I take no damage from allied spells or skills.",
            rarity = Rarity.RARE
        ),
        CardTemplate(
            id = "card_flame_eyed_caprine", name = "Flame-Eyed Caprine", region = Region.FRELJORD, type = CardType.UNIT,
            cost = 4, basePower = 3, baseHealth = 2, keywords = setOf(Keyword.TOUGH),
            description = "Tough. When you play a Skill or Elemental spell, Forge me (+1|+1).",
            rarity = Rarity.RARE
        ),
        CardTemplate(
            id = "card_crystal_stormbird", name = "Crystal Stormbird", region = Region.FRELJORD, type = CardType.UNIT,
            cost = 5, basePower = 3, baseHealth = 2, keywords = setOf(Keyword.ELEMENTAL),
            description = "Elemental. Play (Skill): Deal 2 damage to ALL other units.",
            rarity = Rarity.RARE
        ),
        CardTemplate(
            id = "card_freljord_titan", name = "She Who Wanders", region = Region.FRELJORD, type = CardType.UNIT,
            cost = 9, basePower = 8, baseHealth = 8, keywords = setOf(Keyword.OVERWHELM, Keyword.TOUGH),
            description = "Overwhelm, Tough. Colossal Freljord Titan.",
            rarity = Rarity.EPIC
        ),
        CardTemplate(
            id = "card_glacial_colossus", name = "Frostheart Behemoth", region = Region.FRELJORD, type = CardType.UNIT,
            cost = 7, basePower = 6, baseHealth = 7, keywords = setOf(Keyword.OVERWHELM),
            description = "Overwhelm. Massive glacier giant.",
            rarity = Rarity.RARE
        ),
        CardTemplate(
            id = "card_ancient_yeti", name = "Ancient Yeti", region = Region.FRELJORD, type = CardType.UNIT,
            cost = 7, basePower = 5, baseHealth = 6, keywords = setOf(Keyword.OVERWHELM, Keyword.REGENERATION),
            description = "Overwhelm, Regeneration.",
            rarity = Rarity.RARE
        ),

        // Spells (Burst, Focus, Fast, Slow)
        CardTemplate(
            id = "card_get_excited", name = "Get Excited!", region = Region.PILTOVER_ZAUN, type = CardType.SPELL,
            cost = 3, spellSpeed = SpellSpeed.FAST, keywords = setOf(Keyword.ELEMENTAL),
            description = "Fast. Deal 3 to any enemy unit or enemy Nexus.",
            effectType = CardEffectType.DEAL_DAMAGE_ANY, effectValue = 3
        ),
        CardTemplate(
            id = "card_mystic_shot", name = "Mystic Shot", region = Region.PILTOVER_ZAUN, type = CardType.SPELL,
            cost = 2, spellSpeed = SpellSpeed.FAST,
            description = "Fast. Deal 2 to the enemy Nexus or weakest enemy.",
            effectType = CardEffectType.DEAL_DAMAGE_ANY, effectValue = 2
        ),
        CardTemplate(
            id = "card_super_mega_rocket", name = "Super Mega Death Rocket!", region = Region.PILTOVER_ZAUN, type = CardType.SPELL,
            cost = 1, spellSpeed = SpellSpeed.SLOW, keywords = setOf(Keyword.FLEETING),
            description = "Slow, Fleeting. Deal 4 to the enemy Nexus and 2 to all enemy units.",
            effectType = CardEffectType.DEAL_DAMAGE_UNIT_AND_NEXUS, effectValue = 4,
            isCollectibleReward = false
        ),
        CardTemplate(
            id = "card_prismatic_barrier", name = "Prismatic Barrier", region = Region.DEMACIA, type = CardType.SPELL,
            cost = 2, spellSpeed = SpellSpeed.BURST,
            description = "Burst. Give your strongest ally Barrier this round and draw 1.",
            effectType = CardEffectType.GIVE_BARRIER_AND_DRAW, effectValue = 1
        ),
        CardTemplate(
            id = "card_golden_aegis", name = "Golden Aegis", region = Region.DEMACIA, type = CardType.SPELL,
            cost = 4, spellSpeed = SpellSpeed.SLOW,
            description = "Slow. Give your strongest ally Barrier and Rally!",
            effectType = CardEffectType.RALLY_AND_SHIELD, effectValue = 1
        ),
        CardTemplate(
            id = "card_final_spark", name = "Final Spark", region = Region.DEMACIA, type = CardType.SPELL,
            cost = 0, spellSpeed = SpellSpeed.FAST, keywords = setOf(Keyword.OVERWHELM, Keyword.FLEETING),
            description = "Fast, Overwhelm, Fleeting. Deal 4 to an enemy and excess to enemy Nexus.",
            effectType = CardEffectType.DEAL_DAMAGE_UNIT_AND_NEXUS, effectValue = 4,
            isCollectibleReward = false
        ),
        CardTemplate(
            id = "card_steel_tempest", name = "Steel Tempest", region = Region.IONIA, type = CardType.SPELL,
            cost = 2, spellSpeed = SpellSpeed.FAST,
            description = "Fast. Stun the strongest enemy.",
            effectType = CardEffectType.STUN_TARGET, effectValue = 1
        ),
        CardTemplate(
            id = "card_concussive_palm", name = "Concussive Palm", region = Region.IONIA, type = CardType.SPELL,
            cost = 4, spellSpeed = SpellSpeed.FAST,
            description = "Fast. Stun an enemy and summon a 3|2 Tail of the Dragon.",
            effectType = CardEffectType.STUN_TARGET, effectValue = 2
        ),
        CardTemplate(
            id = "card_flash_freeze", name = "Flash Freeze", region = Region.FRELJORD, type = CardType.SPELL,
            cost = 3, spellSpeed = SpellSpeed.BURST, keywords = setOf(Keyword.ELEMENTAL),
            description = "Burst, Elemental. Frostbite the strongest enemy.",
            effectType = CardEffectType.FROSTBITE_TARGET, effectValue = 1
        ),
        CardTemplate(
            id = "card_single_combat", name = "Single Combat", region = Region.DEMACIA, type = CardType.SPELL,
            cost = 2, spellSpeed = SpellSpeed.FAST,
            description = "Fast. Your strongest ally and the weakest enemy strike each other.",
            effectType = CardEffectType.SINGLE_COMBAT_STRIKE, effectValue = 1
        ),
        CardTemplate(
            id = "card_time_trick", name = "Time Trick", region = Region.PILTOVER_ZAUN, type = CardType.SPELL,
            cost = 2, spellSpeed = SpellSpeed.FOCUS,
            description = "Focus. Predict and Draw 1.",
            effectType = CardEffectType.PREDICT_DRAW, effectValue = 1
        ),
        CardTemplate(
            id = "card_bard_chimes", name = "Cosmic Binding", region = Region.TARGON, type = CardType.SPELL,
            cost = 2, spellSpeed = SpellSpeed.FOCUS,
            description = "Focus. Plant 3 Chime Boons in your deck and grant top ally +1|+1.",
            effectType = CardEffectType.PLANT_CHIMES, effectValue = 3
        ),
        CardTemplate(
            id = "card_teemo_puffcaps", name = "Mushroom Cloud", region = Region.PILTOVER_ZAUN, type = CardType.SPELL,
            cost = 1, spellSpeed = SpellSpeed.BURST,
            description = "Burst. Plant 5 Poison Puffcap Traps into the enemy deck.",
            effectType = CardEffectType.PLANT_PUFFCAPS, effectValue = 5
        ),
        CardTemplate(
            id = "card_harsh_winds", name = "Harsh Winds", region = Region.FRELJORD, type = CardType.SPELL,
            cost = 5, spellSpeed = SpellSpeed.BURST, keywords = setOf(Keyword.ELEMENTAL),
            description = "Burst, Elemental. Frostbite 2 enemies.",
            effectType = CardEffectType.FROSTBITE_TWO, effectValue = 2,
            rarity = Rarity.RARE
        ),
        CardTemplate(
            id = "card_wings_of_cryophoenix", name = "Wings of the Cryophoenix", region = Region.FRELJORD, type = CardType.SPELL,
            cost = 7, spellSpeed = SpellSpeed.SLOW, keywords = setOf(Keyword.ELEMENTAL),
            description = "Slow, Elemental. Deal 3 to ALL units. Enlightened: Instead deal 3 to all enemies and heal your Nexus 3.",
            effectType = CardEffectType.WINGS_OF_CRYOPHOENIX, effectValue = 3,
            rarity = Rarity.EPIC
        ),
        CardTemplate(
            id = "card_clash_of_giants", name = "Clash of Giants", region = Region.FRELJORD, type = CardType.SPELL,
            cost = 16, spellSpeed = SpellSpeed.SLOW, keywords = setOf(Keyword.ELEMENTAL),
            description = "Slow, Elemental. Costs 1 less per Skill or Elemental spell played (max -10). Summon 2 random Giants.",
            effectType = CardEffectType.SUMMON_TWO_GIANTS, effectValue = 2,
            rarity = Rarity.EPIC
        ),

        // Equipment & Landmark
        CardTemplate(
            id = "card_forged_blade", name = "Darkin Halberd", region = Region.DEMACIA, type = CardType.EQUIPMENT,
            cost = 2, basePower = 2, baseHealth = 1, keywords = setOf(Keyword.CHALLENGER),
            description = "Equipment. Grants +2|+1 and Challenger to your strongest ally."
        ),
        CardTemplate(
            id = "card_sun_disc", name = "Hextech Observatory", region = Region.PILTOVER_ZAUN, type = CardType.LANDMARK,
            cost = 2, countdownRounds = 2,
            description = "Landmark. Countdown 2: Draw 2 cards and refill 2 Spell Mana."
        ),

        // Non-collectible Curse Cards
        CardTemplate(
            id = "card_curse_shackles", name = "Shackles", region = Region.SHADOW_ISLES, type = CardType.CURSE,
            cost = 2, spellSpeed = SpellSpeed.FOCUS,
            description = "Curse. Play to free your Shackled unit.",
            effectType = CardEffectType.PURGE_SHACKLES_CURSE,
            isCollectibleReward = false
        ),
        CardTemplate(
            id = "card_curse_suppression", name = "Suppression", region = Region.DEMACIA, type = CardType.CURSE,
            cost = 2, spellSpeed = SpellSpeed.FOCUS,
            description = "Curse. While in hand, your spells cost 1 more. Play to purge.",
            effectType = CardEffectType.PURGE_SUPPRESSION_CURSE,
            isCollectibleReward = false
        ),
        CardTemplate(
            id = "card_curse_terror", name = "Terror", region = Region.SHADOW_ISLES, type = CardType.CURSE,
            cost = 2, spellSpeed = SpellSpeed.FOCUS,
            description = "Curse. Inflicts -1|-0 to allies while held. Play to purge.",
            effectType = CardEffectType.PURGE_TERROR_CURSE,
            isCollectibleReward = false
        ),
        CardTemplate(
            id = "card_ignition", name = "Ignition", region = Region.PILTOVER_ZAUN, type = CardType.SPELL,
            cost = 0, spellSpeed = SpellSpeed.BURST, keywords = setOf(Keyword.FLEETING),
            description = "Burst, Fleeting. Deal 1 to the enemy Nexus.",
            effectType = CardEffectType.DEAL_DAMAGE_NEXUS, effectValue = 1,
            isCollectibleReward = false
        ),
        CardTemplate(
            id = "card_tail_of_the_dragon", name = "Tail of the Dragon", region = Region.IONIA, type = CardType.UNIT,
            cost = 3, basePower = 3, baseHealth = 2, keywords = setOf(Keyword.ATTUNE),
            description = "Attune. Summoned by Concussive Palm.",
            isCollectibleReward = false
        ),
        CardTemplate(
            id = "card_eggnivia", name = "Eggnivia", region = Region.FRELJORD, type = CardType.UNIT,
            cost = 1, basePower = 0, baseHealth = 2, keywords = emptySet(),
            description = "Round Start: If Enlightened, transform back into Anivia and Level Up.",
            isCollectibleReward = false
        )
    ).associateBy { it.id }

    fun getCard(id: String): CardTemplate = cards[id] ?: cards.values.first()
    fun getItem(id: String): ItemTemplate = items.find { it.id == id } ?: items.first()
    fun getPower(id: String): PowerTemplate = powers.find { it.id == id } ?: powers.first()
    fun getRelic(id: String): RelicTemplate = relics.find { it.id == id } ?: relics.first()

    val giantFollowersPool: List<CardTemplate> by lazy {
        listOf("card_freljord_titan", "card_glacial_colossus", "card_ancient_yeti").map { getCard(it) }
    }

    val collectibleRewardPool: List<CardTemplate> by lazy {
        cards.values.filter { it.isCollectibleReward && !it.isChampion }
    }

    val champions: List<ChampionTemplate> = listOf(
        ChampionTemplate(
            id = "champ_anivia",
            name = "Anivia",
            title = "The Cryophoenix",
            region = Region.FRELJORD,
            cardId = "card_anivia",
            archetypeSummary = "Control • Mana Ramp & Elemental AoE (Enlightened)",
            unlockFragmentCost = 0,
            baseDeck = listOf(
                DeckCardEntry("card_anivia", 2),
                DeckCardEntry("card_wild_mystics", 2),
                DeckCardEntry("card_mortal_prophet", 2),
                DeckCardEntry("card_guardian_follower", 2),
                DeckCardEntry("card_flame_eyed_caprine", 2),
                DeckCardEntry("card_crystal_stormbird", 2),
                DeckCardEntry("card_harsh_winds", 2),
                DeckCardEntry("card_wings_of_cryophoenix", 2),
                DeckCardEntry("card_clash_of_giants", 2)
            ),
            starPowers = listOf(
                StarPowerSpec(2, "Glacial Overflow", "+1 Starting Mana. If Enlightened, whenever you gain a Mana gem, grant all allies +1|+1 instead.", 20, starCrystalCost = 2),
                StarPowerSpec(3, "Winter's Boon II", "Whenever you play an Elemental spell or Skill, gain +1 empty Mana gem permanently.", 40, starCrystalCost = 5),
                StarPowerSpec(4, "Endurance", "Whenever an ally survives damage, grant it +1|+1.", 40, starCrystalCost = 5),
                StarPowerSpec(5, "Manaflow", "Game Start: Gain +1 Starting Mana gem.", 50, starCrystalCost = 10),
                StarPowerSpec(6, "Crystallize", "When enemies attack, Frostbite the strongest attacker. Enemies with 0 Power take double damage.", 100, novaCrystalCost = 1)
            ),
            constellationNodes = listOf(
                ConstellationMinorNode("anivia_hp", "True Ice Heart", "+5 Max Nexus HP", 10, gemstoneCost = 5, bonusMaxHp = 5),
                ConstellationMinorNode("anivia_reroll", "Freljord Tempest Sight", "+1 Reroll Token", 15, gemstoneCost = 5, bonusRerolls = 1),
                ConstellationMinorNode("anivia_gold", "Glacial Vault", "+50 Starting Gold", 15, gemstoneCost = 10, bonusStartingGold = 50)
            )
        )
    )

    fun getChampion(id: String): ChampionTemplate = champions.find { it.id == id } ?: champions.first()

    data class ReinforcementPackage(
        val championId: String,
        val championCardId: String,
        val title: String,
        val subtitle: String,
        val cardIds: List<String>
    )

    val reinforcementPackages = listOf(
        ReinforcementPackage(
            "reinf_zed", "card_zed", "Zed — Shadow Strike",
            "Quick Attack aggression & Elusive clones",
            listOf("card_zed", "card_shadow_assassin", "card_spectral_ironhound")
        ),
        ReinforcementPackage(
            "reinf_garen", "card_garen", "Garen — Vanguard Might",
            "Regeneration, Challenger & Single Combat",
            listOf("card_garen", "card_laurent_protege", "card_single_combat")
        ),
        ReinforcementPackage(
            "reinf_karma", "card_karma", "Karma — Arcane Flow",
            "Attune, Predict & Spell Scaling",
            listOf("card_karma", "card_time_trick", "card_prismatic_barrier")
        ),
        ReinforcementPackage(
            "reinf_viktor", "card_viktor", "Viktor — Glorious Evolution",
            "Augment, Evolve & Keyword Soup",
            listOf("card_viktor", "card_ballistic_bot", "card_void_abomination")
        ),
        ReinforcementPackage(
            "reinf_ashe", "card_ashe", "Ashe — Frozen Arrow",
            "Frostbite control & Overwhelm finishers",
            listOf("card_ashe", "card_icevale_archer", "card_flash_freeze")
        ),
        ReinforcementPackage(
            "reinf_anivia", "card_anivia", "Anivia — Cryophoenix",
            "Elemental AoE, Mana Ramp & Enlightened Giants",
            listOf("card_anivia", "card_crystal_stormbird", "card_harsh_winds")
        )
    )
}
