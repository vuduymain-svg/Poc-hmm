package com.example.model

enum class Rarity(val displayName: String, val shopCostGold: Int) {
    COMMON("Common", 75),
    RARE("Rare", 125),
    EPIC("Epic", 200),
    LEGENDARY("Legendary", 300)
}

enum class Region(val displayName: String) {
    PILTOVER_ZAUN("Piltover & Zaun"),
    DEMACIA("Demacia"),
    IONIA("Ionia"),
    FRELJORD("Freljord"),
    SHADOW_ISLES("Shadow Isles"),
    TARGON("Targon"),
    NOXUS("Noxus"),
    RUNETERRA("Runeterra")
}

enum class CardType(val displayName: String) {
    UNIT("Follower"),
    CHAMPION("Champion"),
    SPELL("Spell"),
    EQUIPMENT("Equipment"),
    LANDMARK("Landmark"),
    CURSE("Curse")
}

enum class SpellSpeed(val displayName: String, val description: String) {
    BURST("Burst", "Resolves instantaneously without passing initiative."),
    FOCUS("Focus", "Resolves instantaneously outside combat when the Spell Stack is empty."),
    FAST("Fast", "Enters the Spell Stack and passes initiative; playable in combat or response."),
    SLOW("Slow", "Enters the Spell Stack and passes initiative; playable only outside combat when stack is empty."),
    SKILL("Skill", "Unit-generated ability placed on the Spell Stack."),
    NONE("Unit", "Played directly onto the board.")
}

enum class KeywordCategory {
    COMBAT_POSITIVE,
    NEGATIVE_STATUS,
    CARD_PROPERTY
}

enum class Keyword(
    val displayName: String,
    val category: KeywordCategory,
    val isSharablePositive: Boolean,
    val isStackable: Boolean,
    val description: String
) {
    // 8.1 Combat & Transferable Positive Keywords
    OVERWHELM(
        "Overwhelm", KeywordCategory.COMBAT_POSITIVE, true, false,
        "Excess combat damage dealt beyond a blocker's remaining Health carries over to the enemy Nexus."
    ),
    QUICK_ATTACK(
        "Quick Attack", KeywordCategory.COMBAT_POSITIVE, true, false,
        "While attacking, strikes before its blocker. If the blocker dies, takes no return combat damage."
    ),
    DOUBLE_ATTACK(
        "Double Attack", KeywordCategory.COMBAT_POSITIVE, true, false,
        "While attacking, strikes twice: once before its blocker and once simultaneously."
    ),
    ELUSIVE(
        "Elusive", KeywordCategory.COMBAT_POSITIVE, true, false,
        "Can only be blocked by enemy units that also have Elusive or Sharpsight."
    ),
    SHARPSIGHT(
        "Sharpsight", KeywordCategory.COMBAT_POSITIVE, true, false,
        "Enables a defending unit to block attackers with Elusive."
    ),
    FEARSOME(
        "Fearsome", KeywordCategory.COMBAT_POSITIVE, true, false,
        "Can only be blocked by enemy units with 3 or more Power."
    ),
    BRASH(
        "Brash", KeywordCategory.COMBAT_POSITIVE, true, false,
        "Can only be blocked by enemy units with 3 or more current Health."
    ),
    CHALLENGER(
        "Challenger", KeywordCategory.COMBAT_POSITIVE, true, false,
        "When attacking, may force an eligible enemy unit to block it."
    ),
    IMPACT(
        "Impact", KeywordCategory.COMBAT_POSITIVE, true, true,
        "Stackable. When striking while attacking, deals 1 direct damage to the enemy Nexus per stack."
    ),
    LIFESTEAL(
        "Lifesteal", KeywordCategory.COMBAT_POSITIVE, true, false,
        "Damage dealt by this unit heals its controller's Nexus for an equal amount."
    ),
    REGENERATION(
        "Regeneration", KeywordCategory.COMBAT_POSITIVE, true, false,
        "Heals all damage at Round End, restoring the unit to full Max Health."
    ),
    TOUGH(
        "Tough", KeywordCategory.COMBAT_POSITIVE, true, false,
        "Reduces all incoming damage from any source by 1."
    ),
    BARRIER(
        "Barrier", KeywordCategory.COMBAT_POSITIVE, true, false,
        "Negates the next instance of damage the unit would take this round."
    ),
    SPELLSHIELD(
        "SpellShield", KeywordCategory.COMBAT_POSITIVE, true, false,
        "Nullifies the next enemy spell or skill that targets or affects this unit."
    ),
    FURY(
        "Fury", KeywordCategory.COMBAT_POSITIVE, true, false,
        "Grants +1|+1 whenever this unit kills another unit."
    ),
    SCOUT(
        "Scout", KeywordCategory.COMBAT_POSITIVE, true, false,
        "The first time each round you attack with only Scout units, Rally."
    ),
    LURK(
        "Lurk", KeywordCategory.COMBAT_POSITIVE, true, false,
        "When attacking with a Lurk card on top of your deck, grants +1|+0 to allied Lurk units."
    ),
    EVOLVE(
        "Evolve", KeywordCategory.COMBAT_POSITIVE, true, false,
        "Permanently grants +2|+2 once you have possessed 6+ unique positive keywords in the match."
    ),
    AUGMENT(
        "Augment", KeywordCategory.COMBAT_POSITIVE, true, false,
        "Grants +1|+0 whenever you play a created card."
    ),
    FATED(
        "Fated", KeywordCategory.COMBAT_POSITIVE, true, false,
        "Once per round, grants +1|+1 when targeted by an allied card or effect."
    ),
    FORMIDABLE(
        "Formidable", KeywordCategory.COMBAT_POSITIVE, true, false,
        "Strikes using its current Health value instead of its Power value."
    ),
    HALLOWED(
        "Hallowed", KeywordCategory.COMBAT_POSITIVE, true, false,
        "On death, increases global Hallowed stacks by 1. First attacker gains +X|+0."
    ),
    SPIRIT(
        "Spirit", KeywordCategory.COMBAT_POSITIVE, true, true,
        "Stackable. Grants +1|+1 per stack of Spirit applied to the unit."
    ),
    DEATHLESS(
        "Deathless", KeywordCategory.COMBAT_POSITIVE, true, false,
        "First time this unit dies, revive it with 1 Health and Stunned, then remove Deathless."
    ),

    // 8.2 Negative & Status Control Keywords
    EPHEMERAL(
        "Ephemeral", KeywordCategory.NEGATIVE_STATUS, false, false,
        "Dies immediately after striking in combat or at Round End."
    ),
    VULNERABLE(
        "Vulnerable", KeywordCategory.NEGATIVE_STATUS, false, false,
        "Opponents may force this unit into combat as a blocker against any attacker."
    ),
    STUN(
        "Stun", KeywordCategory.NEGATIVE_STATUS, false, false,
        "Removes unit from combat and prevents attacking or blocking for the round."
    ),
    FROSTBITE(
        "Frostbite", KeywordCategory.NEGATIVE_STATUS, false, false,
        "Sets unit's Power to 0 for the current round (subsequent buffs apply normally)."
    ),
    SILENCE(
        "Silence", KeywordCategory.NEGATIVE_STATUS, false, false,
        "Strips all card text, keywords, and stat modifiers from a unit."
    ),
    GLOOM(
        "Gloom", KeywordCategory.NEGATIVE_STATUS, false, true,
        "Stackable. Inflicts -1|-1 per stack of Gloom applied to the unit."
    ),
    CANT_BLOCK(
        "Can't Block", KeywordCategory.NEGATIVE_STATUS, false, false,
        "Restricts the unit from declaring blocks."
    ),
    IMMOBILE(
        "Immobile", KeywordCategory.NEGATIVE_STATUS, false, false,
        "Restricts the unit from declaring attacks or blocks."
    ),
    SHACKLED(
        "Shackled", KeywordCategory.NEGATIVE_STATUS, false, false,
        "Binds unit until the linked Shackles Curse card is played from hand."
    ),

    // 8.3 Card Properties
    ATTUNE(
        "Attune", KeywordCategory.CARD_PROPERTY, false, false,
        "Restores 1 Spell Mana when summoned."
    ),
    FLEETING(
        "Fleeting", KeywordCategory.CARD_PROPERTY, false, false,
        "Discarded from hand at Round End if unplayed."
    ),
    ELEMENTAL(
        "Elemental", KeywordCategory.CARD_PROPERTY, false, false,
        "Elemental spell/skill subtype for synergy and cost reduction."
    ),
    DAYBREAK(
        "Daybreak", KeywordCategory.CARD_PROPERTY, false, false,
        "Bonus effect triggers when this is the first card you play in a round."
    );

    companion object {
        val sharablePool: List<Keyword> by lazy {
            entries.filter { it.isSharablePositive && it != EVOLVE }
        }
    }
}

enum class TriggerHook(val displayName: String) {
    PLAY("Play"),
    SUMMON("Summon"),
    STRIKE("Strike"),
    NEXUS_STRIKE("Nexus Strike"),
    ATTACK("Attack"),
    SUPPORT("Support"),
    LAST_BREATH("Last Breath"),
    ROUND_START("Round Start"),
    ROUND_END("Round End"),
    LEVEL_UP("Level Up")
}

enum class StateCondition(val displayName: String, val description: String) {
    SLAY("Slay", "Counts enemy units killed by your damage or kill effects."),
    PLUNDER("Plunder", "Active if you damaged the enemy Nexus this round."),
    REPUTATION("Reputation", "Active once 5+ Power allies have struck 4+ times."),
    FLOW("Flow", "Active at Round Start if you played 2+ spells/skills last round."),
    DAYBREAK("Daybreak", "Active if this is the first card played this round."),
    NIGHTFALL("Nightfall", "Active if this is NOT the first card played this round."),
    ENLIGHTENED("Enlightened", "Active upon reaching 10 Max Mana."),
    DEEP("Deep", "Active with 15 or fewer cards remaining in deck.")
}
