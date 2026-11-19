package com.example.model

object AdventureCatalog {

    val adventures: List<AdventureConfig> = listOf(
        AdventureConfig(
            id = "adv_demacia_outskirts",
            name = "Demacia Outskirts",
            region = Region.DEMACIA,
            starRating = 0.5f,
            difficultyLabel = "0.5★ Standard",
            description = "Patrol the silver-stone borderlands of Demacia and challenge the Dauntless Vanguard.",
            bossName = "Garen, Might of Demacia",
            rewardXp = 250,
            rewardFragments = 15,
            mutators = emptyList(),
            mapNodes = listOf(
                MapNode(
                    id = "d_start", act = 1, row = 0, column = 1, type = NodeType.START,
                    title = "Border Gate", subtitle = "Begin your journey",
                    nextNodeIds = listOf("d_power_1")
                ),
                MapNode(
                    id = "d_power_1", act = 1, row = 1, column = 1, type = NodeType.POWER_NODE,
                    title = "Shrine of Resolve", subtitle = "Draft 1 of 3 Powers",
                    nextNodeIds = listOf("d_fight_1a", "d_fight_1b")
                ),
                MapNode(
                    id = "d_fight_1a", act = 1, row = 2, column = 0, type = NodeType.COMBAT,
                    title = "Vanguard Scouts", subtitle = "Standard Combat",
                    enemyName = "Scout Platoon", enemyPowerTitle = "Ready Ranks",
                    enemyPowerDescription = "Foe's units gain +1|+0 when attacking.",
                    enemyMaxHp = 15, enemyStartingMana = 1,
                    enemyDeckCardIds = listOf("card_cithria", "card_brightsteel_protector", "card_greenfang_warden", "card_single_combat"),
                    goldReward = 70, nextNodeIds = listOf("d_reinf_1")
                ),
                MapNode(
                    id = "d_fight_1b", act = 1, row = 2, column = 2, type = NodeType.COMBAT,
                    title = "Petricite Watch", subtitle = "Standard Combat",
                    enemyName = "Petricite Sentinel", enemyPowerTitle = "Stone Ward",
                    enemyPowerDescription = "Foe starts with +5 Max Health.",
                    enemyMaxHp = 18, enemyStartingMana = 1,
                    enemyDeckCardIds = listOf("card_cithria", "card_petricite_broadwing", "card_laurent_protege", "card_prismatic_barrier"),
                    goldReward = 75, nextNodeIds = listOf("d_shop_1")
                ),
                MapNode(
                    id = "d_reinf_1", act = 1, row = 3, column = 0, type = NodeType.REINFORCEMENT_NODE,
                    title = "Allied Banner", subtitle = "Draft Support Champion",
                    nextNodeIds = listOf("d_midboss")
                ),
                MapNode(
                    id = "d_shop_1", act = 1, row = 3, column = 2, type = NodeType.SHOP_NODE,
                    title = "Smooth Soloist", subtitle = "Buy Cards & Items",
                    nextNodeIds = listOf("d_midboss")
                ),
                MapNode(
                    id = "d_midboss", act = 1, row = 4, column = 1, type = NodeType.MID_BOSS,
                    title = "Luxanna Crownguard", subtitle = "Act 1 Mid-Boss",
                    enemyName = "Lux (Mid-Boss)", enemyPowerTitle = "Prismatic Shield",
                    enemyPowerDescription = "Round Start: Foe's strongest unit gains Barrier.",
                    enemyMaxHp = 22, enemyStartingMana = 2,
                    enemyDeckCardIds = listOf("card_lux", "card_brightsteel_protector", "card_cithria", "card_prismatic_barrier", "card_golden_aegis"),
                    goldReward = 110, nextNodeIds = listOf("d_heal_2", "d_chest_2")
                ),
                MapNode(
                    id = "d_heal_2", act = 2, row = 5, column = 0, type = NodeType.HEALER_NODE,
                    title = "Silverwing Rest", subtitle = "Heal HP or Cut a Card",
                    nextNodeIds = listOf("d_boss")
                ),
                MapNode(
                    id = "d_chest_2", act = 2, row = 5, column = 2, type = NodeType.ITEM_CHEST,
                    title = "Armory Vault", subtitle = "Attach Item to Deck Card",
                    nextNodeIds = listOf("d_boss")
                ),
                MapNode(
                    id = "d_boss", act = 2, row = 6, column = 1, type = NodeType.FINAL_BOSS,
                    title = "Garen, Might of Demacia", subtitle = "Final Boss",
                    enemyName = "Garen", enemyPowerTitle = "For Demacia!",
                    enemyPowerDescription = "Foe starts at 2 Mana. Every 2 rounds, Foe gains +1|+1 on all allies.",
                    enemyMaxHp = 28, enemyStartingMana = 2,
                    enemyDeckCardIds = listOf("card_garen", "card_cithria", "card_laurent_protege", "card_radiant_guardian", "card_single_combat", "card_golden_aegis"),
                    goldReward = 200, nextNodeIds = emptyList()
                )
            )
        ),
        AdventureConfig(
            id = "adv_ionia_woods",
            name = "Ionia Spirit Woods",
            region = Region.IONIA,
            starRating = 1.5f,
            difficultyLabel = "1.5★ Standard",
            description = "Traverse mystical blossom groves guarded by shadow assassins and ancient wind masters.",
            bossName = "Karma, The Enlightened",
            rewardXp = 450,
            rewardFragments = 20,
            mutators = listOf(
                GlobalMutator("mut_spirit_breeze", "Spirit Breeze", "Round Start: Both players gain 1 Spell Mana.")
            ),
            mapNodes = listOf(
                MapNode(
                    id = "i_start", act = 1, row = 0, column = 1, type = NodeType.START,
                    title = "Blossom Trail", subtitle = "Enter the Spirit Woods",
                    nextNodeIds = listOf("i_power_1")
                ),
                MapNode(
                    id = "i_power_1", act = 1, row = 1, column = 1, type = NodeType.POWER_NODE,
                    title = "Spirit Altar", subtitle = "Draft 1 of 3 Powers",
                    nextNodeIds = listOf("i_fight_1a", "i_fight_1b")
                ),
                MapNode(
                    id = "i_fight_1a", act = 1, row = 2, column = 0, type = NodeType.COMBAT,
                    title = "Shadow Initiates", subtitle = "Elusive Skirmish",
                    enemyName = "Kinkou Shadows", enemyPowerTitle = "Unseen Path",
                    enemyPowerDescription = "Foe's Elusive units have +1|+0.",
                    enemyMaxHp = 20, enemyStartingMana = 2,
                    enemyDeckCardIds = listOf("card_shadow_assassin", "card_kinkou_student", "card_sumpworks_mapper", "card_steel_tempest"),
                    goldReward = 80, nextNodeIds = listOf("i_event_1")
                ),
                MapNode(
                    id = "i_fight_1b", act = 1, row = 2, column = 2, type = NodeType.ELITE_COMBAT,
                    title = "Bladetwirler Dojo", subtitle = "Elite Encounter",
                    enemyName = "Master Duels", enemyPowerTitle = "Tempest Training",
                    enemyPowerDescription = "Round Start: Foe stuns your weakest unit every 2 rounds.",
                    enemyMaxHp = 24, enemyStartingMana = 2,
                    enemyDeckCardIds = listOf("card_fae_blader", "card_shadow_assassin", "card_steel_tempest", "card_concussive_palm"),
                    goldReward = 110, nextNodeIds = listOf("i_reinf_1")
                ),
                MapNode(
                    id = "i_event_1", act = 1, row = 3, column = 0, type = NodeType.NARRATIVE_EVENT,
                    title = "Whispering Shrine", subtitle = "Risk & Reward Choice",
                    nextNodeIds = listOf("i_midboss")
                ),
                MapNode(
                    id = "i_reinf_1", act = 1, row = 3, column = 2, type = NodeType.REINFORCEMENT_NODE,
                    title = "Allied Wanderer", subtitle = "Draft Support Champion",
                    nextNodeIds = listOf("i_midboss")
                ),
                MapNode(
                    id = "i_midboss", act = 1, row = 4, column = 1, type = NodeType.MID_BOSS,
                    title = "Zed, Master of Shadows", subtitle = "Act 1 Mid-Boss",
                    enemyName = "Zed", enemyPowerTitle = "Living Shadow",
                    enemyPowerDescription = "Foe's attacking units gain +1|+1 on strike.",
                    enemyMaxHp = 28, enemyStartingMana = 2,
                    enemyDeckCardIds = listOf("card_zed", "card_shadow_assassin", "card_spectral_ironhound", "card_steel_tempest"),
                    goldReward = 130, nextNodeIds = listOf("i_shop_2", "i_heal_2")
                ),
                MapNode(
                    id = "i_shop_2", act = 2, row = 5, column = 0, type = NodeType.SHOP_NODE,
                    title = "Spirit Merchant", subtitle = "Buy Cards, Items & Powers",
                    nextNodeIds = listOf("i_boss")
                ),
                MapNode(
                    id = "i_heal_2", act = 2, row = 5, column = 2, type = NodeType.HEALER_NODE,
                    title = "Tranquil Spring", subtitle = "Heal HP or Cut a Card",
                    nextNodeIds = listOf("i_boss")
                ),
                MapNode(
                    id = "i_boss", act = 2, row = 6, column = 1, type = NodeType.FINAL_BOSS,
                    title = "Karma, The Enlightened", subtitle = "Final Boss",
                    enemyName = "Karma", enemyPowerTitle = "Enlightened Harmony",
                    enemyPowerDescription = "Foe starts at 3 Mana and heals Nexus 1 when casting spells.",
                    enemyMaxHp = 35, enemyStartingMana = 3,
                    enemyDeckCardIds = listOf("card_karma", "card_kinkou_student", "card_shadow_assassin", "card_concussive_palm", "card_prismatic_barrier"),
                    goldReward = 250, nextNodeIds = emptyList()
                )
            )
        ),
        AdventureConfig(
            id = "adv_zaun_foundry",
            name = "Piltover & Zaun Foundry",
            region = Region.PILTOVER_ZAUN,
            starRating = 3.0f,
            difficultyLabel = "3.0★ Elite",
            description = "Infiltrate Viktor's mechanized factories where augmented constructs and Puffcaps await.",
            bossName = "Viktor, Machine Herald",
            rewardXp = 750,
            rewardFragments = 25,
            mutators = listOf(
                GlobalMutator("mut_glorious_assembly", "Glorious Assembly", "Allies and Foes gain +1|+0 whenever playing a created or spell card.")
            ),
            mapNodes = listOf(
                MapNode(
                    id = "z_start", act = 1, row = 0, column = 1, type = NodeType.START,
                    title = "Sump Elevator", subtitle = "Descend into Zaun",
                    nextNodeIds = listOf("z_power_1")
                ),
                MapNode(
                    id = "z_power_1", act = 1, row = 1, column = 1, type = NodeType.POWER_NODE,
                    title = "Hextech Core", subtitle = "Draft 1 of 3 Powers",
                    nextNodeIds = listOf("z_fight_1a", "z_fight_1b")
                ),
                MapNode(
                    id = "z_fight_1a", act = 1, row = 2, column = 0, type = NodeType.COMBAT,
                    title = "Chem-Punks", subtitle = "Burn & Puffcaps",
                    enemyName = "Boomcrew Gang", enemyPowerTitle = "Volatile Munitions",
                    enemyPowerDescription = "Foe's units gain Impact 1.",
                    enemyMaxHp = 26, enemyStartingMana = 2,
                    enemyDeckCardIds = listOf("card_boomcrew_rookie", "card_zaunite_urchin", "card_mystic_shot", "card_teemo_puffcaps"),
                    goldReward = 95, nextNodeIds = listOf("z_chest_1")
                ),
                MapNode(
                    id = "z_fight_1b", act = 1, row = 2, column = 2, type = NodeType.ELITE_COMBAT,
                    title = "Augmented Patrol", subtitle = "Elite Construct Fight",
                    enemyName = "Ballistic Squadron", enemyPowerTitle = "Hextech Upgrade",
                    enemyPowerDescription = "Foe starts with 3 Mana.",
                    enemyMaxHp = 30, enemyStartingMana = 3,
                    enemyDeckCardIds = listOf("card_ballistic_bot", "card_void_abomination", "card_get_excited", "card_ruthless_raider"),
                    goldReward = 130, nextNodeIds = listOf("z_shop_1")
                ),
                MapNode(
                    id = "z_chest_1", act = 1, row = 3, column = 0, type = NodeType.ITEM_CHEST,
                    title = "Inventor's Locker", subtitle = "Attach Item to Deck Card",
                    nextNodeIds = listOf("z_midboss")
                ),
                MapNode(
                    id = "z_shop_1", act = 1, row = 3, column = 2, type = NodeType.SHOP_NODE,
                    title = "Chembaron Bazaar", subtitle = "Buy Cards & Items",
                    nextNodeIds = listOf("z_midboss")
                ),
                MapNode(
                    id = "z_midboss", act = 1, row = 4, column = 1, type = NodeType.MID_BOSS,
                    title = "Jinx, Loose Cannon", subtitle = "Act 1 Mid-Boss",
                    enemyName = "Jinx (Rival)", enemyPowerTitle = "Rocket Barrage",
                    enemyPowerDescription = "Round Start: Deals 1 damage to your Nexus.",
                    enemyMaxHp = 34, enemyStartingMana = 3,
                    enemyDeckCardIds = listOf("card_jinx", "card_boomcrew_rookie", "card_zaunite_urchin", "card_get_excited", "card_mystic_shot"),
                    goldReward = 160, nextNodeIds = listOf("z_reinf_2", "z_heal_2")
                ),
                MapNode(
                    id = "z_reinf_2", act = 2, row = 5, column = 0, type = NodeType.REINFORCEMENT_NODE,
                    title = "Piltover Enforcers", subtitle = "Draft Support Champion",
                    nextNodeIds = listOf("z_boss")
                ),
                MapNode(
                    id = "z_heal_2", act = 2, row = 5, column = 2, type = NodeType.HEALER_NODE,
                    title = "Med-Bay Station", subtitle = "Heal HP or Cut a Card",
                    nextNodeIds = listOf("z_boss")
                ),
                MapNode(
                    id = "z_boss", act = 2, row = 6, column = 1, type = NodeType.FINAL_BOSS,
                    title = "Viktor, Machine Herald", subtitle = "Final Boss",
                    enemyName = "Viktor", enemyPowerTitle = "Glorious Evolution",
                    enemyPowerDescription = "Foe starts at 3 Mana. Foe's units gain +1|+1 and random keywords!",
                    enemyMaxHp = 45, enemyStartingMana = 3,
                    enemyDeckCardIds = listOf("card_viktor", "card_ballistic_bot", "card_void_abomination", "card_rekindled_knight", "card_get_excited"),
                    goldReward = 320, nextNodeIds = emptyList()
                )
            )
        ),
        AdventureConfig(
            id = "adv_targon_nightmare",
            name = "Targon's Peak Nightmare",
            region = Region.TARGON,
            starRating = 4.5f,
            difficultyLabel = "4.5★ Nightmare",
            description = "Ascend the celestial summit against cosmic horrors, Curses, and Aurelion Sol.",
            bossName = "Aurelion Sol, Star Forger",
            rewardXp = 1200,
            rewardFragments = 40,
            mutators = listOf(
                GlobalMutator("mut_celestial_might", "Cosmic Threat", "Foe starts at 4 Mana!"),
                GlobalMutator("mut_nightmare_curse", "Nightmare Echoes", "Final Boss afflicts you with Suppression Curses.")
            ),
            mapNodes = listOf(
                MapNode(
                    id = "t_start", act = 1, row = 0, column = 1, type = NodeType.START,
                    title = "Mount Targon Base", subtitle = "Begin Nightmare Ascent",
                    nextNodeIds = listOf("t_power_1")
                ),
                MapNode(
                    id = "t_power_1", act = 1, row = 1, column = 1, type = NodeType.POWER_NODE,
                    title = "Celestial Conduit", subtitle = "Draft High-Tier Power",
                    nextNodeIds = listOf("t_fight_1a", "t_fight_1b")
                ),
                MapNode(
                    id = "t_fight_1a", act = 1, row = 2, column = 0, type = NodeType.ELITE_COMBAT,
                    title = "Solari Crusaders", subtitle = "Daybreak Assault",
                    enemyName = "Solari Vanguard", enemyPowerTitle = "Blazing Dawn",
                    enemyPowerDescription = "Foe starts at 4 Mana. Foe units have +1|+1.",
                    enemyMaxHp = 35, enemyStartingMana = 4,
                    enemyDeckCardIds = listOf("card_solari_soldier", "card_radiant_guardian", "card_petricite_broadwing", "card_golden_aegis"),
                    goldReward = 130, nextNodeIds = listOf("t_event_1")
                ),
                MapNode(
                    id = "t_fight_1b", act = 1, row = 2, column = 2, type = NodeType.ELITE_COMBAT,
                    title = "Shadow Revenants", subtitle = "Deathless & Fearsome",
                    enemyName = "Spectral Legion", enemyPowerTitle = "Undying Terror",
                    enemyPowerDescription = "Foe starts at 4 Mana with Hallowed stacks.",
                    enemyMaxHp = 35, enemyStartingMana = 4,
                    enemyDeckCardIds = listOf("card_rekindled_knight", "card_spectral_ironhound", "card_void_abomination", "card_flash_freeze"),
                    goldReward = 135, nextNodeIds = listOf("t_shop_1")
                ),
                MapNode(
                    id = "t_event_1", act = 1, row = 3, column = 0, type = NodeType.NARRATIVE_EVENT,
                    title = "Astral Altar", subtitle = "Sacrifice HP for Epic Power",
                    nextNodeIds = listOf("t_midboss")
                ),
                MapNode(
                    id = "t_shop_1", act = 1, row = 3, column = 2, type = NodeType.SHOP_NODE,
                    title = "Star Merchant", subtitle = "High-Tier Upgrades",
                    nextNodeIds = listOf("t_midboss")
                ),
                MapNode(
                    id = "t_midboss", act = 1, row = 4, column = 1, type = NodeType.MID_BOSS,
                    title = "Ashe, Frost Queen", subtitle = "Act 1 Mid-Boss",
                    enemyName = "Ashe (Nightmare)", enemyPowerTitle = "Endless Winter",
                    enemyPowerDescription = "Foe starts at 4 Mana. Round Start: Frostbite your strongest unit.",
                    enemyMaxHp = 42, enemyStartingMana = 4,
                    enemyDeckCardIds = listOf("card_ashe", "card_icevale_archer", "card_ruthless_raider", "card_flash_freeze"),
                    goldReward = 190, nextNodeIds = listOf("t_power_2", "t_heal_2")
                ),
                MapNode(
                    id = "t_power_2", act = 2, row = 5, column = 0, type = NodeType.POWER_NODE,
                    title = "Nebula Blessing", subtitle = "Draft Bonus Power",
                    nextNodeIds = listOf("t_boss")
                ),
                MapNode(
                    id = "t_heal_2", act = 2, row = 5, column = 2, type = NodeType.HEALER_NODE,
                    title = "Lunar Sanctuary", subtitle = "Heal HP or Cut a Card",
                    nextNodeIds = listOf("t_boss")
                ),
                MapNode(
                    id = "t_boss", act = 2, row = 6, column = 1, type = NodeType.FINAL_BOSS,
                    title = "Aurelion Sol", subtitle = "Nightmare Final Boss",
                    enemyName = "Aurelion Sol", enemyPowerTitle = "Star Forger",
                    enemyPowerDescription = "Foe starts at 4 Mana. Round Start: Foe gains +1|+1 on board & inflicts Suppression.",
                    enemyMaxHp = 60, enemyStartingMana = 4,
                    enemyDeckCardIds = listOf("card_aurelion_sol", "card_rekindled_knight", "card_void_abomination", "card_radiant_guardian", "card_golden_aegis"),
                    goldReward = 500, nextNodeIds = emptyList()
                )
            )
        )
    )

    fun getAdventure(id: String): AdventureConfig =
        adventures.find { it.id == id } ?: adventures.first()
}
