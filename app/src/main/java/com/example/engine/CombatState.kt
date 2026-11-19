package com.example.engine

import com.example.model.AdventureConfig
import com.example.model.CardTemplate
import com.example.model.CardType
import com.example.model.GlobalMutator
import com.example.model.HandCardInstance
import com.example.model.Keyword
import com.example.model.MapNode
import com.example.model.PowerTemplate
import com.example.model.RelicTemplate
import com.example.model.SpellSpeed
import com.example.model.UnitOnBoard

enum class PriorityHolder {
    PLAYER,
    ENEMY;

    fun opponent(): PriorityHolder = if (this == PLAYER) ENEMY else PLAYER
}

enum class CombatPhase {
    MAIN_ACTION_PHASE,
    SPELL_STACK_PHASE,
    COMBAT_DECLARE_ATTACK_PHASE,
    COMBAT_DECLARE_BLOCK_PHASE,
    COMBAT_RESOLUTION_PHASE,
    GAME_OVER
}

data class SpellStackEntry(
    val id: String,
    val sourceIsPlayer: Boolean,
    val card: CardTemplate,
    val targetUnitId: String? = null,
    val damageBonus: Int = 0
)

data class CombatState(
    val encounterNode: MapNode,
    val adventure: AdventureConfig,
    val championId: String,
    val starLevel: Int,
    val equippedRelics: List<RelicTemplate>,
    val activePowers: List<PowerTemplate>,
    val mutators: List<GlobalMutator>,

    val round: Int = 1,
    val playerNexusHp: Int,
    val playerMaxHp: Int,
    val enemyNexusHp: Int,
    val enemyMaxHp: Int,

    val playerMana: Int = 1,
    val playerMaxMana: Int = 1,
    val playerSpellMana: Int = 0,
    val enemyMana: Int = 1,
    val enemyMaxMana: Int = 1,
    val enemySpellMana: Int = 0,

    val playerHasAttackToken: Boolean = true,
    val enemyHasAttackToken: Boolean = false,
    val scoutUsedThisRound: Boolean = false,
    val pendingScoutRally: Boolean = false,
    val playerInitiatedCombat: Boolean = true,

    val phase: CombatPhase = CombatPhase.MAIN_ACTION_PHASE,
    val priorityHolder: PriorityHolder = PriorityHolder.PLAYER,
    val consecutivePasses: Int = 0,
    val lastStackCaster: PriorityHolder? = null,

    val playerHand: List<HandCardInstance> = emptyList(),
    val playerDrawPile: List<HandCardInstance> = emptyList(),
    val playerGraveyard: List<CardTemplate> = emptyList(),
    val enemyHand: List<CardTemplate> = emptyList(),
    val enemyDrawPool: List<CardTemplate> = emptyList(),
    val enemyGraveyard: List<CardTemplate> = emptyList(),

    val playerBoard: List<UnitOnBoard> = emptyList(),
    val enemyBoard: List<UnitOnBoard> = emptyList(),
    val attackingUnitIds: List<String> = emptyList(),
    val blockPairings: Map<String, String> = emptyMap(), // attackerId -> blockerId
    val spellStack: List<SpellStackEntry> = emptyList(),

    val selectedAttackerForChallenger: String? = null,
    val selectedEnemyAttackerForBlock: String? = null,

    val playerHallowedStacks: Int = 0,
    val enemyHallowedStacks: Int = 0,
    val playerUniqueKeywordsSeen: Set<Keyword> = emptySet(),
    val playerSpellsManaSpent: Int = 0,
    val playerCardsPlayedThisRound: Int = 0,
    val playerSpellsPlayedThisRound: Int = 0,
    val playerPlunderActive: Boolean = false,
    val enemiesStunnedCount: Int = 0,
    val enemiesFrostbittenCount: Int = 0,
    val enemySuppressionCount: Int = 0,
    val enemyTerrorCount: Int = 0,
    val playerElementalAndSkillsPlayed: Int = 0,

    val playerWon: Boolean? = null,
    val combatLog: List<String> = emptyList()
) {
    val enemyHandCount: Int
        get() = enemyHand.size

    val totalPlayerManaAvailable: Int
        get() = playerMana + playerSpellMana

    val totalEnemyManaAvailable: Int
        get() = enemyMana + enemySpellMana

    fun canPlayCard(card: HandCardInstance): Boolean {
        if (priorityHolder != PriorityHolder.PLAYER) return false
        if (phase == CombatPhase.GAME_OVER || phase == CombatPhase.COMBAT_RESOLUTION_PHASE) return false

        val type = card.template.type
        val speed = card.template.spellSpeed

        // Check mana
        val cost = effectivePlayCost(card)
        val isSpellLike = type == CardType.SPELL || type == CardType.CURSE
        val available = if (isSpellLike) playerMana + playerSpellMana else playerMana
        if (available < cost) return false

        // Check board space for units/landmarks
        if ((type == CardType.UNIT || type == CardType.CHAMPION || type == CardType.LANDMARK) &&
            playerBoard.size >= 6
        ) return false

        // Equipment requires at least 1 unit on board
        if (type == CardType.EQUIPMENT && playerBoard.isEmpty()) return false

        // Check timing windows per Section 9.2 & 9.3
        return when (phase) {
            CombatPhase.MAIN_ACTION_PHASE -> true
            CombatPhase.SPELL_STACK_PHASE,
            CombatPhase.COMBAT_DECLARE_ATTACK_PHASE,
            CombatPhase.COMBAT_DECLARE_BLOCK_PHASE -> {
                speed == SpellSpeed.BURST || speed == SpellSpeed.FAST
            }
            CombatPhase.COMBAT_RESOLUTION_PHASE,
            CombatPhase.GAME_OVER -> false
        }
    }

    fun effectivePlayCost(card: HandCardInstance): Int {
        var cost = card.effectiveCost
        val isSpell = card.template.type == CardType.SPELL
        if (isSpell && activePowers.any { it.id == "pow_spellslinger" }) {
            cost -= 1
        }
        if (card.isCreated && activePowers.any { it.id == "pow_wild_inspiration" }) {
            cost -= 1
        }
        if (isSpell && playerHand.any { it.template.id == "card_curse_suppression" }) {
            cost += 1
        }
        if (card.template.id == "card_clash_of_giants") {
            cost -= minOf(10, playerElementalAndSkillsPlayed)
        }
        return maxOf(0, cost)
    }
}
