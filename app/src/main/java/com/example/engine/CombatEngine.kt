package com.example.engine

import com.example.model.AdventureConfig
import com.example.model.CardCatalog
import com.example.model.CardEffectType
import com.example.model.CardTemplate
import com.example.model.CardType
import com.example.model.ChampionLoadout
import com.example.model.HandCardInstance
import com.example.model.ItemTemplate
import com.example.model.Keyword
import com.example.model.MapNode
import com.example.model.PowerTemplate
import com.example.model.RelicTemplate
import com.example.model.RunState
import com.example.model.SpellSpeed
import com.example.model.UnitOnBoard
import java.util.UUID
import kotlin.random.Random

object CombatEngine {

    private fun nextId(): String = UUID.randomUUID().toString().take(8)

    fun createEncounter(
        runState: RunState,
        loadout: ChampionLoadout,
        adventure: AdventureConfig,
        node: MapNode
    ): CombatState {
        val powers = runState.activePowerIds.map { CardCatalog.getPower(it) }
        val relics = loadout.equippedRelics.mapNotNull { id -> id?.let { CardCatalog.getRelic(it) } }

        // Expand RunState.runDeck into individual HandCardInstances
        val expandedDeck = mutableListOf<HandCardInstance>()
        runState.runDeck.forEach { entry ->
            val tmpl = CardCatalog.getCard(entry.cardId)
            val items = entry.attachedItemIds.map { CardCatalog.getItem(it) }
            val costRed = items.sumOf { it.costReduction }
            val bonusPow = items.sumOf { it.bonusPower }
            val bonusHp = items.sumOf { it.bonusHealth }
            val itemKeywords = items.mapNotNull { it.grantedKeyword }.toSet()

            repeat(entry.copies) {
                expandedDeck.add(
                    HandCardInstance(
                        instanceId = "hc_${nextId()}",
                        template = tmpl,
                        attachedItems = items,
                        effectiveCost = maxOf(0, tmpl.cost - costRed),
                        bonusPower = bonusPow,
                        bonusHealth = bonusHp,
                        extraKeywords = itemKeywords,
                        isFleeting = tmpl.keywords.contains(Keyword.FLEETING)
                    )
                )
            }
        }
        expandedDeck.shuffle()

        // Farsight Alteration: pull Farsight cards to top so they are drawn in opening hand
        val farsights = expandedDeck.filter { c -> c.attachedItems.any { it.farsightGameStartDraw } }
        val others = expandedDeck.filterNot { c -> c.attachedItems.any { it.farsightGameStartDraw } }
        val orderedDeck = farsights + others

        val openingHand = orderedDeck.take(4)
        val remainingDeck = orderedDeck.drop(4)

        val startingMana = (1 +
            loadout.startingManaBonus +
            (if (loadout.starLevel >= 5) 1 else 0) +
            relics.sumOf { it.startingManaBonus }).coerceIn(1, 10)

        val startingSpellMana = if (powers.any { it.id == "pow_sorcery" }) 3 else 0
        val enemyStartMana = node.enemyStartingMana.coerceIn(1, 10)

        val enemyPool = node.enemyDeckCardIds.map { CardCatalog.getCard(it) }
            .ifEmpty { listOf(CardCatalog.getCard("card_cithria")) }
        val openingEnemyHand = List(4) { enemyPool.random() }

        val initialLog = mutableListOf(
            "Encounter: ${node.title} vs ${node.enemyName}",
            "Your Starting Mana: $startingMana | Foe Starting Mana: $enemyStartMana"
        )
        if (node.enemyPowerTitle.isNotBlank()) {
            initialLog.add("Foe Power [${node.enemyPowerTitle}]: ${node.enemyPowerDescription}")
        }

        val effectiveEnemyMaxHp = node.enemyMaxHp + (if (node.enemyPowerTitle == "Stone Ward") 5 else 0)
        val initialEnemyHallowed = if (node.enemyPowerTitle == "Undying Terror") 2 else 0

        var state = CombatState(
            encounterNode = node,
            adventure = adventure,
            championId = loadout.championId,
            starLevel = loadout.starLevel,
            equippedRelics = relics,
            activePowers = powers,
            mutators = adventure.mutators,
            round = 1,
            playerNexusHp = runState.currentNexusHp,
            playerMaxHp = runState.maxNexusHp,
            enemyNexusHp = effectiveEnemyMaxHp,
            enemyMaxHp = effectiveEnemyMaxHp,
            playerMana = startingMana,
            playerMaxMana = startingMana,
            playerSpellMana = startingSpellMana,
            enemyMana = enemyStartMana,
            enemyMaxMana = enemyStartMana,
            enemySpellMana = 0,
            playerHasAttackToken = true,
            enemyHasAttackToken = false,
            enemyHallowedStacks = initialEnemyHallowed,
            phase = CombatPhase.MAIN_ACTION_PHASE,
            priorityHolder = PriorityHolder.PLAYER,
            consecutivePasses = 0,
            playerHand = openingHand,
            playerDrawPile = remainingDeck,
            enemyHand = openingEnemyHand,
            enemyDrawPool = enemyPool,
            combatLog = initialLog
        )

        // Apply Game Start / Round 1 Star Powers & Powers
        state = applyRoundStartHooks(state, isGameStart = true)
        return state
    }

    private fun applyRoundStartHooks(state: CombatState, isGameStart: Boolean): CombatState {
        var s = state
        val logs = s.combatLog.toMutableList()

        // Mutator: Spirit Breeze
        if (s.mutators.any { it.id == "mut_spirit_breeze" }) {
            s = s.copy(
                playerSpellMana = minOf(3, s.playerSpellMana + 1),
                enemySpellMana = minOf(3, s.enemySpellMana + 1)
            )
        }

        // Power: Sorcery
        if (s.activePowers.any { it.id == "pow_sorcery" }) {
            s = s.copy(playerSpellMana = 3)
        }

        // Power: Bountiful Vitality
        if (s.activePowers.any { it.id == "pow_vitality" }) {
            val healed = minOf(s.playerMaxHp, s.playerNexusHp + 2)
            if (healed > s.playerNexusHp) {
                s = s.copy(playerNexusHp = healed)
                logs.add("Vitality healed your Nexus +2 HP.")
            }
        }

        // Power: Domination (Round Start: Rally)
        if (s.activePowers.any { it.id == "pow_domination" }) {
            s = s.copy(playerHasAttackToken = true)
        }

        // Power: Perfected Manaflow (triggers every Round Start including Round 1)
        if (s.activePowers.any { it.id == "pow_manaflow" }) {
            s = gainPlayerManaGem(s, empty = false, logs, "Perfected Manaflow")
        }

        // Champion Star Powers
        when (s.championId) {
            "champ_jinx" -> {
                if (isGameStart && s.starLevel >= 2 && s.playerHand.size < 10) {
                    val rocket = createHandInstance(CardCatalog.getCard("card_mystic_shot"), isFleeting = true)
                    s = s.copy(playerHand = s.playerHand + rocket)
                    logs.add("Jinx 2★ created Fleeting Mystic Shot.")
                }
            }
            "champ_lux" -> {
                if (s.starLevel >= 2) {
                    s = s.copy(playerSpellMana = minOf(3, s.playerSpellMana + 1))
                }
                if (s.starLevel >= 6) {
                    s = s.copy(
                        playerHasAttackToken = true,
                        playerBoard = s.playerBoard.map { it.copy(hasBarrier = true, keywords = it.keywords + Keyword.BARRIER) }
                    )
                    logs.add("Lux 6★ rallied & granted allies Barrier!")
                }
            }
            "champ_yasuo" -> {
                if (s.starLevel >= 1 && s.enemyBoard.isNotEmpty()) {
                    val target = if (s.starLevel >= 3) {
                        s.enemyBoard.maxByOrNull { it.effectivePower }
                    } else {
                        s.enemyBoard.minByOrNull { it.effectivePower }
                    }
                    if (target != null) {
                        s = stunEnemyUnit(s, target.instanceId, logs)
                    }
                }
                if (s.starLevel >= 3 && s.playerHand.size < 10) {
                    val tempest = createHandInstance(CardCatalog.getCard("card_steel_tempest"), isFleeting = true)
                    s = s.copy(playerHand = s.playerHand + tempest)
                }
            }
            "champ_garen" -> {
                if (s.starLevel >= 6 || s.playerBoard.any { it.template.id == "card_garen" && it.isLeveledUp }) {
                    s = s.copy(playerHasAttackToken = true)
                    if (!isGameStart && s.playerBoard.any { it.template.id == "card_garen" && it.isLeveledUp }) {
                        logs.add("Leveled Garen triggered Rally!")
                    }
                }
            }
            "champ_ashe" -> {
                if (s.starLevel >= 1 && s.enemyBoard.isNotEmpty()) {
                    if (s.starLevel >= 6) {
                        val newlyFrostbitten = s.enemyBoard.count { !it.isFrostbitten }
                        s = s.copy(
                            enemyBoard = s.enemyBoard.map { it.copy(isFrostbitten = true) },
                            enemiesFrostbittenCount = s.enemiesFrostbittenCount + newlyFrostbitten
                        )
                        logs.add("Ashe 6★ Frostbit ALL enemies!")
                    } else {
                        val strongest = s.enemyBoard.maxByOrNull { it.effectivePower }
                        if (strongest != null) {
                            s = s.copy(
                                enemyBoard = s.enemyBoard.map {
                                    if (it.instanceId == strongest.instanceId) it.copy(isFrostbitten = true) else it
                                },
                                enemiesFrostbittenCount = s.enemiesFrostbittenCount + 1
                            )
                            logs.add("Ashe Star Power Frostbit ${strongest.template.name}.")
                        }
                    }
                }
                if (s.starLevel >= 3) {
                    s.playerBoard.maxByOrNull { it.effectivePower }?.let { topAlly ->
                        s = s.copy(playerBoard = s.playerBoard.map {
                            if (it.instanceId == topAlly.instanceId) {
                                it.copy(power = it.power + 2, health = it.health + 2, maxHealth = it.maxHealth + 2)
                            } else it
                        })
                        logs.add("Ashe 3★ granted ${topAlly.template.name} +2|+2!")
                    }
                }
            }
            "champ_anivia" -> {
                if (isGameStart && s.starLevel >= 2) {
                    logs.add("Anivia 2★ Glacial Overflow active (+1 Starting Mana).")
                }
            }
        }

        // Eggnivia revival at Round Start when Enlightened (10 Max Mana)
        if (!isGameStart && s.playerMaxMana >= 10 && s.playerBoard.any { it.template.id == "card_eggnivia" }) {
            val aniviaTmpl = CardCatalog.getCard("card_anivia")
            s = s.copy(
                playerBoard = s.playerBoard.map { u ->
                    if (u.template.id == "card_eggnivia") {
                        val bonusPow = aniviaTmpl.levelUpPowerBonus + s.equippedRelics.sumOf { it.bonusPower }
                        val bonusHp = aniviaTmpl.levelUpHealthBonus + s.equippedRelics.sumOf { it.bonusHealth }
                        val newPow = aniviaTmpl.basePower + bonusPow
                        val newHp = aniviaTmpl.baseHealth + bonusHp
                        u.copy(
                            template = aniviaTmpl,
                            power = newPow,
                            health = newHp,
                            maxHealth = newHp,
                            keywords = aniviaTmpl.keywords + s.equippedRelics.mapNotNull { it.grantedKeyword },
                            isLeveledUp = true
                        )
                    } else u
                }
            )
            logs.add("Eggnivia hatched into Leveled Anivia!")
        }

        // Leveled Garen works even when playing another champion via Reinforcement
        if (s.championId != "champ_garen" && s.playerBoard.any { it.template.id == "card_garen" && it.isLeveledUp }) {
            s = s.copy(playerHasAttackToken = true)
            logs.add("Leveled Garen triggered Rally!")
        }

        // Ballistic Bot Round Start: Create a Fleeting Ignition
        if (!isGameStart && s.playerBoard.any { it.template.id == "card_ballistic_bot" } && s.playerHand.size < 10) {
            val ignition = createHandInstance(CardCatalog.getCard("card_ignition"), isFleeting = true)
            s = s.copy(playerHand = s.playerHand + ignition)
            logs.add("Ballistic Bot created Fleeting Ignition.")
        }

        // Relic: Archangel's Staff (if leveled or on board)
        if (s.playerBoard.any { u -> u.template.isChampion } &&
            s.equippedRelics.any { it.id == "relic_archangels_staff" }
        ) {
            s = s.copy(playerSpellMana = 3)
        }

        // Relic: Grand General's Counterplan
        if (!isGameStart &&
            s.playerBoard.any { it.template.isChampion } &&
            s.equippedRelics.any { it.id == "relic_grand_generals" } &&
            s.playerHand.size < 10
        ) {
            val spellId = when (s.championId) {
                "champ_jinx" -> "card_get_excited"
                "champ_lux" -> "card_prismatic_barrier"
                "champ_yasuo" -> "card_steel_tempest"
                "champ_garen" -> "card_single_combat"
                "champ_anivia" -> "card_harsh_winds"
                else -> "card_flash_freeze"
            }
            s = s.copy(playerHand = s.playerHand + createHandInstance(CardCatalog.getCard(spellId), isFleeting = true))
        }

        // Boss / Node Round Start Powers
        if (!isGameStart) {
            when (s.encounterNode.enemyPowerTitle) {
                "For Demacia!" -> {
                    if (s.round % 2 == 0 && s.enemyBoard.isNotEmpty()) {
                        s = s.copy(enemyBoard = s.enemyBoard.map {
                            it.copy(power = it.power + 1, health = it.health + 1, maxHealth = it.maxHealth + 1)
                        })
                        logs.add("For Demacia! Foe's units gained +1|+1.")
                    }
                }
                "Tempest Training" -> {
                    if (s.round % 2 == 0) {
                        s.playerBoard.minByOrNull { it.effectivePower }?.let { weakest ->
                            s = s.copy(playerBoard = s.playerBoard.map {
                                if (it.instanceId == weakest.instanceId) {
                                    it.copy(isStunned = true, keywords = it.keywords + Keyword.STUN)
                                } else it
                            })
                            logs.add("Tempest Training stunned your ${weakest.template.name}!")
                        }
                    }
                }
                "Prismatic Shield" -> {
                    val topEnemy = s.enemyBoard.maxByOrNull { it.effectivePower }
                    if (topEnemy != null) {
                        s = s.copy(enemyBoard = s.enemyBoard.map {
                            if (it.instanceId == topEnemy.instanceId) it.copy(hasBarrier = true) else it
                        })
                        logs.add("Lux Mid-Boss granted ${topEnemy.template.name} Barrier.")
                    }
                }
                "Rocket Barrage" -> {
                    s = dealDamageToPlayerNexus(s, 1, logs)
                }
                "Endless Winter" -> {
                    val topAlly = s.playerBoard.maxByOrNull { it.effectivePower }
                    if (topAlly != null) {
                        s = s.copy(playerBoard = s.playerBoard.map {
                            if (it.instanceId == topAlly.instanceId) it.copy(isFrostbitten = true) else it
                        })
                        logs.add("Ashe Mid-Boss Frostbit ${topAlly.template.name}!")
                    }
                }
                "Star Forger" -> {
                    s = s.copy(enemyBoard = s.enemyBoard.map {
                        it.copy(power = it.power + 1, health = it.health + 1, maxHealth = it.maxHealth + 1)
                    })
                    if (s.round % 2 == 0 && s.playerHand.size < 10) {
                        s = s.copy(
                            playerHand = s.playerHand + createHandInstance(
                                CardCatalog.getCard("card_curse_suppression"),
                                isCreated = false
                            )
                        )
                        logs.add("Aurelion Sol afflicted your hand with Suppression Curse!")
                    }
                }
            }
        }

        // Tick Landmarks Countdown
        val tickedBoard = mutableListOf<UnitOnBoard>()
        var extraDraws = 0
        var extraSpellMana = 0
        s.playerBoard.forEach { u ->
            if (u.template.type == CardType.LANDMARK && u.countdownRemaining > 0) {
                val rem = u.countdownRemaining - 1
                if (rem <= 0) {
                    extraDraws += 2
                    extraSpellMana += 2
                    logs.add("${u.template.name} Countdown completed: Draw 2 & +2 Spell Mana!")
                } else {
                    tickedBoard.add(u.copy(countdownRemaining = rem))
                }
            } else {
                tickedBoard.add(u)
            }
        }
        s = s.copy(
            playerBoard = tickedBoard,
            playerSpellMana = minOf(3, s.playerSpellMana + extraSpellMana),
            combatLog = logs.takeLast(16)
        )
        repeat(extraDraws) { s = drawPlayerCard(s) }
        s = applyEnlightenedBoardBuffs(s, logs)
        s = checkChampionLevelUps(s, logs)

        return checkGameOver(s.copy(combatLog = logs.takeLast(16)))
    }

    private fun createHandInstance(
        template: CardTemplate,
        isFleeting: Boolean = false,
        isCreated: Boolean = true
    ): HandCardInstance {
        return HandCardInstance(
            instanceId = "hc_${nextId()}",
            template = template,
            effectiveCost = template.cost,
            isFleeting = isFleeting || template.keywords.contains(Keyword.FLEETING),
            isCreated = isCreated
        )
    }

    fun drawPlayerCard(state: CombatState): CombatState {
        if (state.playerDrawPile.isEmpty()) return state
        val topCard = state.playerDrawPile.first()
        val remainingPile = state.playerDrawPile.drop(1)
        val logs = state.combatLog.toMutableList()
        var s = state.copy(playerDrawPile = remainingPile)

        // Check Puffcap Traps
        if (topCard.hasPuffcapTrap > 0) {
            s = dealDamageToPlayerNexus(s, topCard.hasPuffcapTrap, logs)
            logs.add("Poison Puffcap trap dealt ${topCard.hasPuffcapTrap} damage!")
        }

        // Check Chime Boons
        if (topCard.hasChimeBoon && s.playerHand.isNotEmpty()) {
            val idx = Random.nextInt(s.playerHand.size)
            val updatedHand = s.playerHand.toMutableList()
            val chosen = updatedHand[idx]
            updatedHand[idx] = chosen.copy(bonusPower = chosen.bonusPower + 1, bonusHealth = chosen.bonusHealth + 1)
            s = s.copy(playerHand = updatedHand)
            logs.add("Chime Boon granted ${chosen.template.name} +1|+1!")
        }

        if (s.playerHand.size < 10) {
            s = s.copy(playerHand = s.playerHand + topCard, combatLog = logs.takeLast(16))
        } else {
            logs.add("Hand full! Obliterated ${topCard.template.name}.")
            s = s.copy(combatLog = logs.takeLast(16))
        }
        return s
    }

    fun playCardFromHand(state: CombatState, handCardId: String): CombatState {
        val card = state.playerHand.find { it.instanceId == handCardId } ?: return state
        if (!state.canPlayCard(card)) return state

        val cost = state.effectivePlayCost(card)
        val isSpellLike = card.template.type == CardType.SPELL || card.template.type == CardType.CURSE

        var newMana = state.playerMana
        var newSpellMana = state.playerSpellMana
        if (isSpellLike) {
            val spellUsed = minOf(maxOf(0, newSpellMana), cost)
            newSpellMana = maxOf(0, newSpellMana - spellUsed)
            newMana = maxOf(0, newMana - (cost - spellUsed))
        } else {
            newMana = maxOf(0, newMana - cost)
        }

        val logs = state.combatLog.toMutableList()
        val itemManaRefill = card.attachedItems.sumOf { it.refillManaOnPlay }
        if (itemManaRefill > 0) {
            newMana = minOf(state.playerMaxMana, newMana + itemManaRefill)
            logs.add("${card.template.name} refilled +$itemManaRefill Mana.")
        }

        var s = state.copy(
            playerMana = newMana,
            playerSpellMana = newSpellMana,
            playerHand = state.playerHand.filterNot { it.instanceId == handCardId },
            playerCardsPlayedThisRound = state.playerCardsPlayedThisRound + 1,
            combatLog = logs
        )

        // Jinx 1★ / 3★ Power: Playing a card deals burn damage
        if (s.championId == "champ_jinx" && s.starLevel >= 1) {
            s = dealDamageToEnemyNexus(s, 1, logs)
            if (s.starLevel >= 3 && s.enemyBoard.isNotEmpty()) {
                val target = s.enemyBoard.random()
                s = damageUnit(s, target.instanceId, isEnemy = true, damage = 1, logs = logs)
            }
            logs.add("Jinx Star Power blasted 1 damage!")
        }

        // Play: Draw 1 item or card hook
        if (card.attachedItems.any { it.onPlayDrawOne } ||
            card.template.id == "card_zaunite_urchin" ||
            card.template.id == "card_shadow_assassin"
        ) {
            s = drawPlayerCard(s)
        }

        // Mutator: Glorious Assembly
        if (s.mutators.any { it.id == "mut_glorious_assembly" } && isSpellLike) {
            s = s.copy(playerBoard = s.playerBoard.map { it.copy(power = it.power + 1) })
        }

        var nextPriority = PriorityHolder.PLAYER
        var nextPhase = s.phase
        var nextLastCaster = s.lastStackCaster

        when (card.template.type) {
            CardType.UNIT, CardType.CHAMPION, CardType.LANDMARK -> {
                val prevSize = s.playerBoard.size
                s = summonPlayerUnit(s, card, logs)
                val summonedStormbirdId = if (card.template.id == "card_crystal_stormbird" && s.playerBoard.size > prevSize) {
                    s.playerBoard.lastOrNull { it.template.id == "card_crystal_stormbird" }?.instanceId
                } else null

                if (summonedStormbirdId != null) {
                    s = onPlayerSkillOrElementalPlayed(s, logs, "Crystal Stormbird")
                    val skillDmg = 2 + s.equippedRelics.sumOf { it.spellSkillDamageBonus }
                    s.enemyBoard.toList().forEach { enemy ->
                        s = damageUnit(s, enemy.instanceId, isEnemy = true, damage = skillDmg, isSpell = true, fromAllyEffect = true, logs = logs)
                    }
                    s.playerBoard.toList().forEach { ally ->
                        if (ally.instanceId != summonedStormbirdId) {
                            s = damageUnit(s, ally.instanceId, isEnemy = false, damage = skillDmg, isSpell = true, fromAllyEffect = true, logs = logs)
                        }
                    }
                    logs.add("Crystal Stormbird's Skill dealt $skillDmg damage to ALL other units!")
                }
                if (card.template.effectType == CardEffectType.FROSTBITE_TARGET) {
                    s.enemyBoard.maxByOrNull { it.effectivePower }?.let { target ->
                        s = s.copy(
                            enemyBoard = s.enemyBoard.map {
                                if (it.instanceId == target.instanceId) it.copy(isFrostbitten = true) else it
                            },
                            enemiesFrostbittenCount = s.enemiesFrostbittenCount + 1
                        )
                        logs.add("${card.template.name} Frostbit ${target.template.name}.")
                    }
                }
                if (card.template.id == "card_brightsteel_protector") {
                    s.playerBoard.maxByOrNull { it.effectivePower }?.let { top ->
                        s = s.copy(playerBoard = s.playerBoard.map {
                            if (it.instanceId == top.instanceId) it.copy(hasBarrier = true) else it
                        })
                    }
                }
                // Rule 9.3: Summoning Unit/Landmark passes priority immediately to opponent
                nextPriority = PriorityHolder.ENEMY
            }
            CardType.EQUIPMENT -> {
                val host = s.playerBoard.maxByOrNull { it.effectivePower }
                if (host != null) {
                    s = s.copy(playerBoard = s.playerBoard.map {
                        if (it.instanceId == host.instanceId) {
                            it.copy(
                                power = it.power + card.template.basePower,
                                health = it.health + card.template.baseHealth,
                                maxHealth = it.maxHealth + card.template.baseHealth,
                                keywords = it.keywords + card.template.keywords,
                                equippedGearName = card.template.name
                            )
                        } else it
                    })
                    logs.add("Equipped ${card.template.name} onto ${host.template.name}!")
                }
                // Rule 9.3: Equipment passes priority immediately to opponent
                nextPriority = PriorityHolder.ENEMY
            }
            CardType.SPELL, CardType.CURSE -> {
                s = s.copy(
                    playerSpellsManaSpent = s.playerSpellsManaSpent + cost,
                    playerSpellsPlayedThisRound = s.playerSpellsPlayedThisRound + 1
                )

                if (card.template.keywords.contains(Keyword.ELEMENTAL)) {
                    s = onPlayerSkillOrElementalPlayed(s, logs, card.template.name)
                }

                // Lux Star Power 1★: Spell grants strongest ally +1|+1
                if (s.championId == "champ_lux" && s.starLevel >= 1) {
                    s.playerBoard.maxByOrNull { it.effectivePower }?.let { top ->
                        s = s.copy(playerBoard = s.playerBoard.map {
                            if (it.instanceId == top.instanceId) {
                                it.copy(power = it.power + 1, health = it.health + 1, maxHealth = it.maxHealth + 1)
                            } else it
                        })
                    }
                }
                // Lux 3★: Every 6 spell mana -> Rally + Final Spark
                if (s.championId == "champ_lux" && s.starLevel >= 3 && s.playerSpellsManaSpent >= 6) {
                    s = s.copy(
                        playerSpellsManaSpent = s.playerSpellsManaSpent - 6,
                        playerHasAttackToken = true
                    )
                    if (s.playerHand.size < 10) {
                        s = s.copy(playerHand = s.playerHand + createHandInstance(CardCatalog.getCard("card_final_spark"), true))
                    }
                    logs.add("Lux 3★ Triggered: Rally & created Final Spark!")
                }

                val spellDmgBonus = s.equippedRelics.sumOf { it.spellSkillDamageBonus }
                // Rule 9.3: Burst/Focus resolve immediately and KEEP priority; Fast/Slow enter stack and pass priority
                if (card.template.spellSpeed == SpellSpeed.BURST || card.template.spellSpeed == SpellSpeed.FOCUS) {
                    s = resolveSingleSpellEffect(s, card.template, sourceIsPlayer = true, bonusDmg = spellDmgBonus, logs = logs)
                    nextPriority = PriorityHolder.PLAYER
                } else {
                    val entry = SpellStackEntry(
                        id = "st_${nextId()}",
                        sourceIsPlayer = true,
                        card = card.template,
                        damageBonus = spellDmgBonus
                    )
                    s = s.copy(spellStack = s.spellStack + entry)
                    nextLastCaster = PriorityHolder.PLAYER
                    if (nextPhase == CombatPhase.MAIN_ACTION_PHASE) {
                        nextPhase = CombatPhase.SPELL_STACK_PHASE
                    }
                    nextPriority = PriorityHolder.ENEMY
                    logs.add("Placed ${card.template.name} (${card.template.spellSpeed.displayName}) on Spell Stack.")
                }
            }
        }

        // Check Augment triggers for created/spell cards
        if (isSpellLike) {
            s = s.copy(playerBoard = s.playerBoard.map { u ->
                if (u.keywords.contains(Keyword.AUGMENT)) u.copy(power = u.power + 1) else u
            })
        }

        s = checkChampionLevelUps(s, logs)
        s = s.copy(
            phase = nextPhase,
            priorityHolder = nextPriority,
            consecutivePasses = 0,
            lastStackCaster = nextLastCaster,
            combatLog = logs.takeLast(16)
        )
        return checkGameOver(s)
    }

    private fun summonPlayerUnit(
        state: CombatState,
        card: HandCardInstance,
        logs: MutableList<String>,
        forceEphemeral: Boolean = false
    ): CombatState {
        if (state.playerBoard.size >= 6) return state

        var s = state
        val tmpl = card.template
        val itemKeywords = card.extraKeywords
        var keywords = tmpl.keywords + itemKeywords
        if (forceEphemeral) keywords = keywords + Keyword.EPHEMERAL

        if (s.equippedRelics.any { it.id == "relic_troll_kings_crown" }) {
            keywords = keywords + Keyword.OVERWHELM
        }

        // Hextech Fabricator random keyword
        if (card.attachedItems.any { it.grantRandomKeywordOnSummon }) {
            keywords = keywords + Keyword.sharablePool.random()
        }

        var power = tmpl.basePower + card.bonusPower
        var health = tmpl.baseHealth + card.bonusHealth
        if (tmpl.isChampion) {
            power += s.equippedRelics.sumOf { it.bonusPower }
            health += s.equippedRelics.sumOf { it.bonusHealth }
            s.equippedRelics.mapNotNull { it.grantedKeyword }.forEach { keywords = keywords + it }
        }
        if (s.championId == "champ_garen" && s.starLevel >= 2) {
            power += 1; health += 1
        }
        if (s.championId == "champ_garen" && s.starLevel >= 4) {
            keywords = keywords + Keyword.TOUGH
        }
        if (s.championId == "champ_lux" && s.starLevel >= 4) {
            health += 2; keywords = keywords + Keyword.SHARPSIGHT
        }
        if (s.championId == "champ_jinx" && s.starLevel >= 4) {
            health += 1; keywords = keywords + Keyword.IMPACT
        }
        if (s.championId == "champ_yasuo" && s.starLevel >= 4) {
            power += 1; health += 1
        }
        if (s.championId == "champ_yasuo" && s.starLevel >= 2 && keywords.contains(Keyword.QUICK_ATTACK)) {
            power += 1
        }

        // Daybreak bonus
        if (keywords.contains(Keyword.DAYBREAK) && s.playerCardsPlayedThisRound == 1) {
            power += 1; health += 1
            keywords = keywords + Keyword.CHALLENGER
            logs.add("Daybreak triggered on ${tmpl.name} (+1|+1 & Challenger)!")
        }

        // Spirit stack bonus
        val spiritCount = tmpl.spiritStacks + card.attachedItems.sumOf { it.spiritStacks }
        power += spiritCount
        health += spiritCount

        val impactCount = tmpl.impactStacks + card.attachedItems.sumOf { it.impactStacks } +
            (if (s.championId == "champ_jinx" && s.starLevel >= 4) 1 else 0)

        // Track Evolve unique positive keywords
        val newSeenKeywords = s.playerUniqueKeywordsSeen + keywords.filter { it.isSharablePositive }
        if (keywords.contains(Keyword.EVOLVE) && newSeenKeywords.size >= 6) {
            power += 2; health += 2
        }

        var enlightenedApplied = false
        if (tmpl.id == "card_wild_mystics" && s.playerMaxMana >= 10) {
            power += 4
            health += 4
            enlightenedApplied = true
            logs.add("Wild Mystics (Enlightened) summoned as 6|6!")
        }

        // Power: Evolution (+1|+1 per positive keyword)
        if (s.activePowers.any { it.id == "pow_evolution" }) {
            val kwCount = keywords.count { it.isSharablePositive }
            power += kwCount; health += kwCount
        }

        val unit = UnitOnBoard(
            instanceId = "u_${nextId()}",
            template = tmpl,
            ownerIsPlayer = true,
            power = power,
            health = health,
            maxHealth = health,
            keywords = keywords,
            impactStacks = impactCount,
            spiritStacks = spiritCount,
            hasBarrier = keywords.contains(Keyword.BARRIER),
            hasSpellShield = keywords.contains(Keyword.SPELLSHIELD),
            hasDeathless = keywords.contains(Keyword.DEATHLESS),
            attachedItems = card.attachedItems,
            countdownRemaining = tmpl.countdownRounds,
            hasEnlightenedBonusApplied = enlightenedApplied
        )

        // Attune
        if (keywords.contains(Keyword.ATTUNE)) {
            s = s.copy(playerSpellMana = minOf(3, s.playerSpellMana + 1))
        }

        // Power: Sharing is Caring
        var updatedBoard = s.playerBoard
        if (s.activePowers.any { it.id == "pow_sharing_is_caring" }) {
            val sharable = keywords.filter { it.isSharablePositive }.toSet()
            updatedBoard = updatedBoard.map { existing ->
                existing.copy(keywords = existing.keywords + sharable)
            }
        }

        s = s.copy(
            playerBoard = updatedBoard + unit,
            playerUniqueKeywordsSeen = newSeenKeywords
        )
        logs.add("Summoned ${tmpl.name} (${unit.power}|${unit.health}).")

        // Power: Trifarian Might (5+ Power strikes weakest enemy)
        if (s.activePowers.any { it.id == "pow_trifarian_might" } && unit.effectivePower >= 5) {
            s.enemyBoard.minByOrNull { it.health }?.let { weakest ->
                s = damageUnit(s, weakest.instanceId, isEnemy = true, damage = unit.effectivePower, logs = logs)
                logs.add("Trifarian Might: ${tmpl.name} struck ${weakest.template.name} for ${unit.effectivePower}!")
            }
        }

        // Relic: Stalker's Blade
        if (tmpl.isChampion && s.equippedRelics.any { it.id == "relic_stalkers_blade" }) {
            s.enemyBoard.minByOrNull { it.health }?.let { weakest ->
                s = damageUnit(s, weakest.instanceId, isEnemy = true, damage = unit.effectivePower, logs = logs)
                logs.add("Stalker's Blade: ${tmpl.name} struck ${weakest.template.name}!")
            }
        }

        // Shadow Totem or Stabilize Ephemeral Copy
        if (!forceEphemeral && (card.attachedItems.any { it.onSummonEphemeralCopy } ||
                (tmpl.isChampion && s.activePowers.any { it.id == "pow_stabilize" }))
        ) {
            if (s.playerBoard.size < 6) {
                s = summonPlayerUnit(s, card, logs, forceEphemeral = true)
            }
        }

        return s
    }

    fun resolveSpellStack(state: CombatState): CombatState {
        if (state.spellStack.isEmpty()) return state
        var s = state
        val logs = s.combatLog.toMutableList()
        // LIFO stack resolution (top of stack resolves first)
        for (entry in state.spellStack.reversed()) {
            s = resolveSingleSpellEffect(s, entry.card, entry.sourceIsPlayer, entry.damageBonus, logs)
        }
        val returnPhase = if (s.phase == CombatPhase.SPELL_STACK_PHASE) {
            CombatPhase.MAIN_ACTION_PHASE
        } else {
            s.phase
        }
        val nextPriority = when (returnPhase) {
            CombatPhase.COMBAT_DECLARE_ATTACK_PHASE -> PriorityHolder.PLAYER
            CombatPhase.COMBAT_DECLARE_BLOCK_PHASE ->
                if (s.playerInitiatedCombat) PriorityHolder.ENEMY else PriorityHolder.PLAYER
            else -> s.lastStackCaster?.opponent() ?: s.priorityHolder
        }
        s = s.copy(
            spellStack = emptyList(),
            phase = returnPhase,
            priorityHolder = nextPriority,
            consecutivePasses = 0,
            lastStackCaster = null,
            combatLog = logs.takeLast(16)
        )
        s = checkChampionLevelUps(s, logs)
        return checkGameOver(s)
    }

    internal fun resolveSingleSpellEffect(
        state: CombatState,
        card: CardTemplate,
        sourceIsPlayer: Boolean,
        bonusDmg: Int = 0,
        logs: MutableList<String>
    ): CombatState {
        var s = state
        val mult = if (sourceIsPlayer && s.championId == "champ_jinx" && s.starLevel >= 6) 2 else 1
        val dmg = (card.effectValue + bonusDmg) * mult

        when (card.effectType) {
            CardEffectType.DEAL_DAMAGE_ANY -> {
                if (sourceIsPlayer) {
                    val target = s.enemyBoard.minByOrNull { it.health }
                    if (target != null) {
                        s = damageUnit(s, target.instanceId, isEnemy = true, damage = dmg, isSpell = true, logs = logs)
                    } else {
                        s = dealDamageToEnemyNexus(s, dmg, logs)
                    }
                    logs.add("${card.name} dealt $dmg damage.")
                } else {
                    val target = s.playerBoard.minByOrNull { it.health }
                    if (target != null) {
                        s = damageUnit(s, target.instanceId, isEnemy = false, damage = dmg, isSpell = true, logs = logs)
                    } else {
                        s = dealDamageToPlayerNexus(s, dmg, logs)
                    }
                    logs.add("Foe's ${card.name} dealt $dmg damage.")
                }
            }
            CardEffectType.DEAL_DAMAGE_NEXUS -> {
                if (sourceIsPlayer) {
                    s = dealDamageToEnemyNexus(s, dmg, logs)
                    logs.add("${card.name} dealt $dmg to Enemy Nexus.")
                } else {
                    s = dealDamageToPlayerNexus(s, dmg, logs)
                    logs.add("Foe's ${card.name} dealt $dmg to your Nexus.")
                }
            }
            CardEffectType.DEAL_DAMAGE_UNIT_AND_NEXUS -> {
                if (sourceIsPlayer) {
                    s = dealDamageToEnemyNexus(s, dmg, logs)
                    s.enemyBoard.toList().forEach { enemy ->
                        s = damageUnit(s, enemy.instanceId, isEnemy = true, damage = maxOf(2, dmg / 2), isSpell = true, logs = logs)
                    }
                    logs.add("${card.name} blasted $dmg to Enemy Nexus & units!")
                } else {
                    s = dealDamageToPlayerNexus(s, dmg, logs)
                    s.playerBoard.toList().forEach { ally ->
                        s = damageUnit(s, ally.instanceId, isEnemy = false, damage = maxOf(2, dmg / 2), isSpell = true, logs = logs)
                    }
                    logs.add("Foe's ${card.name} blasted $dmg to your Nexus & units!")
                }
            }
            CardEffectType.STUN_TARGET -> {
                if (sourceIsPlayer) {
                    s.enemyBoard.maxByOrNull { it.effectivePower }?.let { target ->
                        s = stunEnemyUnit(s, target.instanceId, logs)
                    }
                } else {
                    s.playerBoard.maxByOrNull { it.effectivePower }?.let { target ->
                        s = s.copy(playerBoard = s.playerBoard.map {
                            if (it.instanceId == target.instanceId) it.copy(isStunned = true, keywords = it.keywords + Keyword.STUN) else it
                        })
                        logs.add("Foe's ${card.name} Stunned ${target.template.name}.")
                    }
                }
                if (card.id == "card_concussive_palm") {
                    val tailTmpl = CardCatalog.getCard("card_tail_of_the_dragon")
                    if (sourceIsPlayer && s.playerBoard.size < 6) {
                        s = summonPlayerUnit(s, createHandInstance(tailTmpl), logs)
                    } else if (!sourceIsPlayer && s.enemyBoard.size < 6) {
                        s = summonEnemyUnitFromPool(s, tailTmpl, spendMana = 0, logs = logs)
                    }
                }
            }
            CardEffectType.FROSTBITE_TARGET -> {
                if (sourceIsPlayer) {
                    s.enemyBoard.maxByOrNull { it.effectivePower }?.let { target ->
                        s = s.copy(
                            enemyBoard = s.enemyBoard.map {
                                if (it.instanceId == target.instanceId) it.copy(isFrostbitten = true) else it
                            },
                            enemiesFrostbittenCount = s.enemiesFrostbittenCount + 1
                        )
                        logs.add("${card.name} Frostbit ${target.template.name}.")
                    }
                } else {
                    s.playerBoard.maxByOrNull { it.effectivePower }?.let { target ->
                        s = s.copy(playerBoard = s.playerBoard.map {
                            if (it.instanceId == target.instanceId) it.copy(isFrostbitten = true) else it
                        })
                        logs.add("Foe's ${card.name} Frostbit ${target.template.name}.")
                    }
                }
            }
            CardEffectType.GIVE_BARRIER_AND_DRAW -> {
                if (sourceIsPlayer) {
                    s.playerBoard.maxByOrNull { it.effectivePower }?.let { top ->
                        s = s.copy(playerBoard = s.playerBoard.map {
                            if (it.instanceId == top.instanceId) it.copy(hasBarrier = true) else it
                        })
                    }
                    s = drawPlayerCard(s)
                    logs.add("${card.name} granted Barrier and drew 1.")
                } else {
                    s.enemyBoard.maxByOrNull { it.effectivePower }?.let { top ->
                        s = s.copy(enemyBoard = s.enemyBoard.map {
                            if (it.instanceId == top.instanceId) it.copy(hasBarrier = true) else it
                        })
                    }
                    s = drawEnemyCard(s)
                    logs.add("Foe's ${card.name} granted Barrier and drew 1.")
                }
            }
            CardEffectType.RALLY_AND_SHIELD -> {
                if (sourceIsPlayer) {
                    s.playerBoard.maxByOrNull { it.effectivePower }?.let { top ->
                        s = s.copy(playerBoard = s.playerBoard.map {
                            if (it.instanceId == top.instanceId) it.copy(hasBarrier = true) else it
                        })
                    }
                    s = s.copy(playerHasAttackToken = true)
                    logs.add("Golden Aegis triggered Rally!")
                } else {
                    s.enemyBoard.maxByOrNull { it.effectivePower }?.let { top ->
                        s = s.copy(enemyBoard = s.enemyBoard.map {
                            if (it.instanceId == top.instanceId) it.copy(hasBarrier = true) else it
                        })
                    }
                    s = s.copy(enemyHasAttackToken = true)
                    logs.add("Foe's Golden Aegis triggered Rally!")
                }
            }
            CardEffectType.SINGLE_COMBAT_STRIKE -> {
                val ally = if (sourceIsPlayer) s.playerBoard.maxByOrNull { it.strikePower }
                    else s.playerBoard.minByOrNull { it.health }
                val enemy = if (sourceIsPlayer) s.enemyBoard.minByOrNull { it.health }
                    else s.enemyBoard.maxByOrNull { it.strikePower }
                if (ally != null && enemy != null) {
                    s = damageUnit(s, enemy.instanceId, isEnemy = true, damage = ally.strikePower, logs = logs)
                    s = damageUnit(s, ally.instanceId, isEnemy = false, damage = enemy.strikePower, logs = logs)
                    logs.add("Single Combat: ${ally.template.name} & ${enemy.template.name} struck each other!")
                }
            }
            CardEffectType.PREDICT_DRAW -> {
                if (sourceIsPlayer) {
                    s = drawPlayerCard(s)
                    logs.add("Time Trick predicted and drew 1.")
                } else {
                    s = drawEnemyCard(s)
                    logs.add("Foe's Time Trick drew 1.")
                }
            }
            CardEffectType.PLANT_CHIMES -> {
                if (sourceIsPlayer) {
                    val updatedPile = s.playerDrawPile.toMutableList()
                    repeat(minOf(card.effectValue, updatedPile.size)) {
                        val idx = Random.nextInt(updatedPile.size)
                        updatedPile[idx] = updatedPile[idx].copy(hasChimeBoon = true)
                    }
                    s = s.copy(playerDrawPile = updatedPile)
                    if (s.playerBoard.isNotEmpty()) {
                        val top = s.playerBoard.first()
                        s = s.copy(playerBoard = s.playerBoard.map {
                            if (it.instanceId == top.instanceId) it.copy(power = it.power + 1, health = it.health + 1, maxHealth = it.maxHealth + 1) else it
                        })
                    }
                    logs.add("Planted ${card.effectValue} Chime Boons into deck!")
                }
            }
            CardEffectType.PLANT_PUFFCAPS -> {
                if (sourceIsPlayer) {
                    logs.add("Planted ${card.effectValue} Poison Puffcap Traps into enemy deck!")
                    s = dealDamageToEnemyNexus(s, 1, logs)
                } else {
                    val updatedPile = s.playerDrawPile.toMutableList()
                    repeat(minOf(card.effectValue, updatedPile.size)) {
                        val idx = Random.nextInt(updatedPile.size)
                        updatedPile[idx] = updatedPile[idx].copy(hasPuffcapTrap = updatedPile[idx].hasPuffcapTrap + 1)
                    }
                    s = s.copy(playerDrawPile = updatedPile)
                    logs.add("Foe planted ${card.effectValue} Poison Puffcaps into your deck!")
                }
            }
            CardEffectType.FROSTBITE_TWO -> {
                if (sourceIsPlayer && s.enemyBoard.isNotEmpty()) {
                    val targets = s.enemyBoard
                        .sortedWith(compareBy<UnitOnBoard> { it.isFrostbitten }.thenByDescending { it.effectivePower })
                        .take(2)
                    val targetIds = targets.map { it.instanceId }.toSet()
                    val newlyFrostbitten = targets.count { !it.isFrostbitten }
                    s = s.copy(
                        enemyBoard = s.enemyBoard.map {
                            if (targetIds.contains(it.instanceId)) it.copy(isFrostbitten = true) else it
                        },
                        enemiesFrostbittenCount = s.enemiesFrostbittenCount + newlyFrostbitten
                    )
                    logs.add("${card.name} Frostbit ${targets.joinToString(", ") { it.template.name }}.")
                } else if (!sourceIsPlayer && s.playerBoard.isNotEmpty()) {
                    val targets = s.playerBoard
                        .sortedWith(compareBy<UnitOnBoard> { it.isFrostbitten }.thenByDescending { it.effectivePower })
                        .take(2)
                    val targetIds = targets.map { it.instanceId }.toSet()
                    s = s.copy(playerBoard = s.playerBoard.map {
                        if (targetIds.contains(it.instanceId)) it.copy(isFrostbitten = true) else it
                    })
                    logs.add("Foe's ${card.name} Frostbit ${targets.size} unit(s).")
                }
            }
            CardEffectType.WINGS_OF_CRYOPHOENIX -> {
                if (sourceIsPlayer) {
                    s.enemyBoard.toList().forEach { enemy ->
                        s = damageUnit(s, enemy.instanceId, isEnemy = true, damage = dmg, isSpell = true, fromAllyEffect = true, logs = logs)
                    }
                    if (s.playerMaxMana >= 10) {
                        val healed = minOf(s.playerMaxHp, s.playerNexusHp + 3)
                        s = s.copy(playerNexusHp = healed)
                        logs.add("Wings of the Cryophoenix (Enlightened): Dealt $dmg to all enemies & healed Nexus +3!")
                    } else {
                        s.playerBoard.toList().forEach { ally ->
                            s = damageUnit(s, ally.instanceId, isEnemy = false, damage = dmg, isSpell = true, fromAllyEffect = true, logs = logs)
                        }
                        logs.add("Wings of the Cryophoenix dealt $dmg to ALL units.")
                    }
                } else {
                    s.playerBoard.toList().forEach { ally ->
                        s = damageUnit(s, ally.instanceId, isEnemy = false, damage = dmg, isSpell = true, logs = logs)
                    }
                    s.enemyBoard.toList().forEach { enemy ->
                        s = damageUnit(s, enemy.instanceId, isEnemy = true, damage = dmg, isSpell = true, fromAllyEffect = true, logs = logs)
                    }
                }
            }
            CardEffectType.SUMMON_TWO_GIANTS -> {
                val count = maxOf(1, card.effectValue)
                if (sourceIsPlayer) {
                    repeat(count) {
                        if (s.playerBoard.size < 6) {
                            val giant = CardCatalog.giantFollowersPool.random()
                            s = summonPlayerUnit(s, createHandInstance(giant), logs)
                        }
                    }
                    logs.add("Clash of Giants summoned Titanic Freljord allies!")
                } else {
                    repeat(count) {
                        if (s.enemyBoard.size < 6) {
                            val giant = CardCatalog.giantFollowersPool.random()
                            s = summonEnemyUnitFromPool(s, giant, spendMana = 0, logs = logs)
                        }
                    }
                }
            }
            CardEffectType.PURGE_SUPPRESSION_CURSE, CardEffectType.PURGE_SHACKLES_CURSE, CardEffectType.PURGE_TERROR_CURSE -> {
                logs.add("Purged ${card.name} Curse from hand.")
            }
            else -> {}
        }
        s = if (sourceIsPlayer) {
            s.copy(playerGraveyard = s.playerGraveyard + card)
        } else {
            s.copy(enemyGraveyard = s.enemyGraveyard + card)
        }
        return s
    }

    // --- ATTACK & BLOCK DECLARATION ---

    fun enterAttackDeclaration(state: CombatState): CombatState {
        if (state.priorityHolder != PriorityHolder.PLAYER ||
            state.phase != CombatPhase.MAIN_ACTION_PHASE ||
            !state.playerHasAttackToken ||
            state.playerBoard.none { it.canAttack }
        ) return state
        return state.copy(
            phase = CombatPhase.COMBAT_DECLARE_ATTACK_PHASE,
            attackingUnitIds = emptyList(),
            blockPairings = emptyMap()
        )
    }

    fun cancelAttackDeclaration(state: CombatState): CombatState {
        if (state.phase != CombatPhase.COMBAT_DECLARE_ATTACK_PHASE) return state
        return state.copy(
            phase = CombatPhase.MAIN_ACTION_PHASE,
            attackingUnitIds = emptyList(),
            blockPairings = emptyMap()
        )
    }

    fun toggleUnitAttack(state: CombatState, unitId: String): CombatState {
        if (state.phase != CombatPhase.COMBAT_DECLARE_ATTACK_PHASE) return state
        val unit = state.playerBoard.find { it.instanceId == unitId } ?: return state
        if (!unit.canAttack) return state

        val current = state.attackingUnitIds.toMutableList()
        if (current.contains(unitId)) {
            current.remove(unitId)
        } else {
            current.add(unitId)
        }
        return state.copy(attackingUnitIds = current)
    }

    fun commitPlayerAttack(state: CombatState): CombatState {
        if (state.phase != CombatPhase.COMBAT_DECLARE_ATTACK_PHASE || state.attackingUnitIds.isEmpty()) return state

        var s = state
        val logs = s.combatLog.toMutableList()
        val attackers = s.attackingUnitIds.mapNotNull { id -> s.playerBoard.find { it.instanceId == id } }

        // Check Scout Rally
        val allScout = attackers.all { it.keywords.contains(Keyword.SCOUT) }
        val scoutRally = allScout && !s.scoutUsedThisRound

        // Hallowed bonus for first attacker
        if (s.playerHallowedStacks > 0 && attackers.isNotEmpty()) {
            val firstId = attackers.first().instanceId
            s = s.copy(playerBoard = s.playerBoard.map {
                if (it.instanceId == firstId) it.copy(power = it.power + s.playerHallowedStacks) else it
            })
            logs.add("Hallowed granted ${attackers.first().template.name} +${s.playerHallowedStacks}|+0!")
        }

        // Guinsoo's Rageblade, Ashe & Anivia Attack triggers
        val skillDmgBonus = s.equippedRelics.sumOf { it.spellSkillDamageBonus }
        attackers.forEach { atk ->
            if (atk.template.isChampion && s.equippedRelics.any { it.id == "relic_guinsoos_rageblade" }) {
                s = s.copy(playerBoard = s.playerBoard.map {
                    if (it.instanceId == atk.instanceId) it.copy(power = it.power + 1, health = it.health + 1, maxHealth = it.maxHealth + 1) else it
                })
            }
            if (atk.template.id == "card_ashe") {
                s.enemyBoard.maxByOrNull { it.effectivePower }?.let { target ->
                    s = s.copy(
                        enemyBoard = s.enemyBoard.map {
                            if (it.instanceId == target.instanceId) it.copy(isFrostbitten = true) else it
                        },
                        enemiesFrostbittenCount = s.enemiesFrostbittenCount + 1
                    )
                    logs.add("Ashe Attack: Frostbit ${target.template.name}.")
                }
            }
            if (atk.template.id == "card_anivia") {
                s = onPlayerSkillOrElementalPlayed(s, logs, "Glacial Storm")
                val stormDmg = (if (atk.isLeveledUp) 2 else 1) + skillDmgBonus
                s.enemyBoard.toList().forEach { enemy ->
                    s = damageUnit(s, enemy.instanceId, isEnemy = true, damage = stormDmg, isSpell = true, fromAllyEffect = true, logs = logs)
                }
                s = dealDamageToEnemyNexus(s, stormDmg, logs)
                logs.add("Glacial Storm dealt $stormDmg damage to all enemies & Enemy Nexus!")
                if (s.playerWon != null) return checkGameOver(s.copy(combatLog = logs.takeLast(16)))
            }
        }

        // Resolve any queued Fast spells before transitioning to defender's block phase
        if (s.spellStack.isNotEmpty()) {
            s = resolveSpellStack(s)
            if (s.playerWon != null) return s
        }

        logs.add("Attack declared with ${attackers.size} unit(s)! Foe assigning blockers...")
        return s.copy(
            playerHasAttackToken = false,
            scoutUsedThisRound = s.scoutUsedThisRound || allScout,
            pendingScoutRally = scoutRally,
            playerInitiatedCombat = true,
            phase = CombatPhase.COMBAT_DECLARE_BLOCK_PHASE,
            priorityHolder = PriorityHolder.ENEMY,
            consecutivePasses = 0,
            blockPairings = emptyMap(),
            combatLog = logs.takeLast(16)
        )
    }

    fun assignPlayerBlocker(state: CombatState, attackerId: String, blockerId: String): CombatState {
        if (state.phase != CombatPhase.COMBAT_DECLARE_BLOCK_PHASE || state.priorityHolder != PriorityHolder.PLAYER) return state
        val attacker = state.enemyBoard.find { it.instanceId == attackerId } ?: return state
        val blocker = state.playerBoard.find { it.instanceId == blockerId } ?: return state
        if (!blocker.canBlockAttacker(attacker)) return state

        val updated = state.blockPairings.toMutableMap()
        val prevKey = updated.entries.find { it.value == blockerId }?.key
        if (prevKey != null) updated.remove(prevKey)
        updated[attackerId] = blockerId
        return state.copy(blockPairings = updated, selectedEnemyAttackerForBlock = null)
    }

    fun removePlayerBlocker(state: CombatState, attackerId: String): CombatState {
        if (state.phase != CombatPhase.COMBAT_DECLARE_BLOCK_PHASE || state.priorityHolder != PriorityHolder.PLAYER) return state
        val updated = state.blockPairings.toMutableMap()
        updated.remove(attackerId)
        return state.copy(blockPairings = updated)
    }

    fun commitPlayerBlock(state: CombatState): CombatState {
        if (state.phase != CombatPhase.COMBAT_DECLARE_BLOCK_PHASE || state.priorityHolder != PriorityHolder.PLAYER) return state
        val logs = state.combatLog.toMutableList()
        var s = state
        if (s.spellStack.isNotEmpty()) {
            s = resolveSpellStack(s)
            if (s.playerWon != null) return s
        }
        s = resolveCombatResolution(s, playerIsAttacking = false, logs = logs)
        return s.copy(
            enemyHasAttackToken = false,
            phase = if (s.playerWon != null) CombatPhase.GAME_OVER else CombatPhase.MAIN_ACTION_PHASE,
            priorityHolder = PriorityHolder.PLAYER,
            consecutivePasses = 0,
            attackingUnitIds = emptyList(),
            blockPairings = emptyMap(),
            selectedEnemyAttackerForBlock = null,
            combatLog = logs.takeLast(16)
        )
    }

    internal fun resolveCombatResolution(
        state: CombatState,
        playerIsAttacking: Boolean,
        logs: MutableList<String>
    ): CombatState {
        var s = state
        val attackers = s.attackingUnitIds.toList()
        val playerFrostbiteAura = if (s.championId == "champ_ashe" && s.starLevel >= 2) {
            s.enemyBoard.count { it.isFrostbitten }
        } else 0

        for (atkId in attackers) {
            val atk = (if (playerIsAttacking) s.playerBoard else s.enemyBoard).find { it.instanceId == atkId } ?: continue
            val blkId = s.blockPairings[atkId]
            val blk = blkId?.let { id -> (if (playerIsAttacking) s.enemyBoard else s.playerBoard).find { it.instanceId == id } }

            val atkStrikePower = atk.strikePower + (if (playerIsAttacking) playerFrostbiteAura else 0)
            val blkStrikePower = (blk?.strikePower ?: 0) + (if (!playerIsAttacking && blk != null) playerFrostbiteAura else 0)

            // Impact damage on attack strike
            if (atk.impactStacks > 0) {
                if (playerIsAttacking) {
                    s = dealDamageToEnemyNexus(s, atk.impactStacks, logs)
                } else {
                    s = dealDamageToPlayerNexus(s, atk.impactStacks, logs)
                }
                logs.add("${atk.template.name} Impact dealt ${atk.impactStacks} Nexus damage!")
            }

            if (blk == null) {
                // Unblocked strike directly to Nexus
                val dmg = atkStrikePower
                if (dmg > 0) {
                    if (playerIsAttacking) {
                        s = dealDamageToEnemyNexus(s, dmg, logs)
                        if (atk.keywords.contains(Keyword.LIFESTEAL)) {
                            s = s.copy(playerNexusHp = minOf(s.playerMaxHp, s.playerNexusHp + dmg))
                        }
                    } else {
                        s = dealDamageToPlayerNexus(s, dmg, logs)
                    }
                    logs.add("${atk.template.name} struck Nexus for $dmg!")
                }
                if (atk.keywords.contains(Keyword.DOUBLE_ATTACK) && dmg > 0) {
                    if (playerIsAttacking) s = dealDamageToEnemyNexus(s, dmg, logs)
                    else s = dealDamageToPlayerNexus(s, dmg, logs)
                }
            } else {
                // Combat between Attacker and Blocker
                val hasFirstStrike = atk.keywords.contains(Keyword.QUICK_ATTACK) || atk.keywords.contains(Keyword.DOUBLE_ATTACK)
                val strikesCount = if (atk.keywords.contains(Keyword.DOUBLE_ATTACK)) 2 else 1

                if (hasFirstStrike) {
                    val atkDmg = atkStrikePower
                    val blkHpBefore = blk.health
                    s = damageUnit(s, blk.instanceId, isEnemy = playerIsAttacking, damage = atkDmg, logs = logs)
                    if (atk.keywords.contains(Keyword.LIFESTEAL) && playerIsAttacking && !blk.hasBarrier) {
                        s = s.copy(playerNexusHp = minOf(s.playerMaxHp, s.playerNexusHp + atkDmg))
                    }
                    if (atk.keywords.contains(Keyword.OVERWHELM) && !blk.hasBarrier && atkDmg > blkHpBefore) {
                        val excess = atkDmg - blkHpBefore
                        if (playerIsAttacking) s = dealDamageToEnemyNexus(s, excess, logs)
                        else s = dealDamageToPlayerNexus(s, excess, logs)
                    }

                    val survivingBlk = (if (playerIsAttacking) s.enemyBoard else s.playerBoard).find { it.instanceId == blk.instanceId }
                    if (survivingBlk != null && !survivingBlk.isStunned) {
                        val blkDmg = survivingBlk.strikePower + (if (!playerIsAttacking) playerFrostbiteAura else 0)
                        if (strikesCount == 2) {
                            s = damageUnit(s, survivingBlk.instanceId, isEnemy = playerIsAttacking, damage = atkDmg, logs = logs)
                        }
                        s = damageUnit(s, atk.instanceId, isEnemy = !playerIsAttacking, damage = blkDmg, logs = logs)
                    } else {
                        s = applyKillBonuses(s, atk.instanceId, isPlayerKiller = playerIsAttacking, logs = logs)
                        if (strikesCount == 2 && atk.keywords.contains(Keyword.OVERWHELM)) {
                            if (playerIsAttacking) s = dealDamageToEnemyNexus(s, atkDmg, logs)
                        }
                    }
                } else {
                    val atkDmg = atkStrikePower
                    val blkDmg = blkStrikePower
                    val blkHpBefore = blk.health

                    s = damageUnit(s, blk.instanceId, isEnemy = playerIsAttacking, damage = atkDmg, logs = logs)
                    s = damageUnit(s, atk.instanceId, isEnemy = !playerIsAttacking, damage = blkDmg, logs = logs)

                    if (atk.keywords.contains(Keyword.LIFESTEAL) && playerIsAttacking && !blk.hasBarrier) {
                        s = s.copy(playerNexusHp = minOf(s.playerMaxHp, s.playerNexusHp + atkDmg))
                    }
                    if (atk.keywords.contains(Keyword.OVERWHELM) && !blk.hasBarrier && atkDmg > blkHpBefore) {
                        val excess = atkDmg - blkHpBefore
                        if (playerIsAttacking) s = dealDamageToEnemyNexus(s, excess, logs)
                        else s = dealDamageToPlayerNexus(s, excess, logs)
                    }
                }
            }

            // Enemy Node Power: Living Shadow (Zed Mid-Boss) — Attacking units gain +1|+1 on strike
            if (!playerIsAttacking && s.encounterNode.enemyPowerTitle == "Living Shadow") {
                val survivingAtk = s.enemyBoard.find { it.instanceId == atkId }
                if (survivingAtk != null && !survivingAtk.keywords.contains(Keyword.EPHEMERAL)) {
                    s = s.copy(enemyBoard = s.enemyBoard.map { u ->
                        if (u.instanceId == atkId) {
                            u.copy(power = u.power + 1, health = u.health + 1, maxHealth = u.maxHealth + 1)
                        } else u
                    })
                    logs.add("Living Shadow empowered ${survivingAtk.template.name} (+1|+1)!")
                }
            }

            // Ephemeral unit dies after striking
            val postBoard = if (playerIsAttacking) s.playerBoard else s.enemyBoard
            val postAtk = postBoard.find { it.instanceId == atkId }
            if (postAtk != null && postAtk.keywords.contains(Keyword.EPHEMERAL)) {
                s = removeUnit(s, postAtk.instanceId, isEnemy = !playerIsAttacking, logs = logs)
            }
        }

        // Garen 1★/3★ strike scaling
        if (s.championId == "champ_garen" && s.starLevel >= 1 && playerIsAttacking) {
            val bonus = if (s.starLevel >= 3) 2 else 1
            s = s.copy(playerBoard = s.playerBoard.map { u ->
                if (attackers.contains(u.instanceId)) {
                    u.copy(power = u.power + bonus, health = u.health + bonus, maxHealth = u.maxHealth + bonus)
                } else u
            })
            if (s.starLevel >= 3 && attackers.isNotEmpty() && s.playerHand.size < 10) {
                s = s.copy(playerHand = s.playerHand + createHandInstance(CardCatalog.getCard("card_single_combat"), isFleeting = true))
                logs.add("Garen 3★ created Fleeting Single Combat!")
            }
        }

        return checkGameOver(s)
    }

    private fun applyKillBonuses(
        state: CombatState,
        killerId: String,
        isPlayerKiller: Boolean,
        logs: MutableList<String>
    ): CombatState {
        val board = if (isPlayerKiller) state.playerBoard else state.enemyBoard
        val updated = board.map { u ->
            if (u.instanceId == killerId && u.keywords.contains(Keyword.FURY)) {
                logs.add("${u.template.name} Fury triggered (+1|+1)!")
                u.copy(power = u.power + 1, health = u.health + 1, maxHealth = u.maxHealth + 1)
            } else u
        }
        return if (isPlayerKiller) state.copy(playerBoard = updated) else state.copy(enemyBoard = updated)
    }

    private fun damageUnit(
        state: CombatState,
        unitId: String,
        isEnemy: Boolean,
        damage: Int,
        isSpell: Boolean = false,
        fromAllyEffect: Boolean = false,
        logs: MutableList<String>
    ): CombatState {
        val board = if (isEnemy) state.enemyBoard else state.playerBoard
        val target = board.find { it.instanceId == unitId } ?: return state

        if (!isEnemy && target.template.id == "card_guardian_follower" && fromAllyEffect) {
            logs.add("${target.template.name} took 0 damage from allied spell/skill.")
            return state
        }

        if (isSpell && target.hasSpellShield) {
            val updated = board.map {
                if (it.instanceId == unitId) it.copy(hasSpellShield = false, keywords = it.keywords - Keyword.SPELLSHIELD) else it
            }
            logs.add("${target.template.name}'s SpellShield negated the spell!")
            return if (isEnemy) state.copy(enemyBoard = updated) else state.copy(playerBoard = updated)
        }

        if (target.hasBarrier) {
            val updated = board.map {
                if (it.instanceId == unitId) it.copy(hasBarrier = false, keywords = it.keywords - Keyword.BARRIER) else it
            }
            logs.add("${target.template.name}'s Barrier absorbed the damage!")
            return if (isEnemy) state.copy(enemyBoard = updated) else state.copy(playerBoard = updated)
        }

        val afterTough = if (target.keywords.contains(Keyword.TOUGH)) maxOf(0, damage - 1) else damage
        val crystallizeMult = if (isEnemy && target.effectivePower == 0 && state.championId == "champ_anivia" && state.starLevel >= 6) 2 else 1
        val actualDmg = afterTough * crystallizeMult
        val newHp = target.health - actualDmg

        if (newHp <= 0) {
            return removeUnit(state, unitId, isEnemy, logs)
        } else {
            var newPow = target.power
            var updatedMaxHp = target.maxHealth
            var finalHp = newHp

            if (isEnemy && state.activePowers.any { it.id == "pow_enfeebling_strike" }) {
                newPow = maxOf(0, newPow - actualDmg)
            }
            if (!isEnemy && actualDmg > 0 && state.championId == "champ_anivia" && state.starLevel >= 4) {
                newPow += 1
                finalHp += 1
                updatedMaxHp += 1
                logs.add("Endurance (4★): ${target.template.name} survived damage and gained +1|+1!")
            }

            val updated = board.map {
                if (it.instanceId == unitId) it.copy(health = finalHp, maxHealth = updatedMaxHp, power = newPow) else it
            }
            var s = if (isEnemy) state.copy(enemyBoard = updated) else state.copy(playerBoard = updated)

            if (actualDmg > 0 && target.template.id == "card_mortal_prophet") {
                if (!isEnemy) {
                    s = dealDamageToEnemyNexus(s, 1, logs)
                    logs.add("Mortal Prophet survived damage: Dealt 1 to Enemy Nexus!")
                } else {
                    s = dealDamageToPlayerNexus(s, 1, logs)
                }
            }
            return s
        }
    }

    private fun removeUnit(
        state: CombatState,
        unitId: String,
        isEnemy: Boolean,
        logs: MutableList<String>
    ): CombatState {
        var s = state
        val board = if (isEnemy) s.enemyBoard else s.playerBoard
        val target = board.find { it.instanceId == unitId } ?: return s

        // Deathless revival
        if (target.hasDeathless) {
            val revived = target.copy(
                health = 1,
                hasDeathless = false,
                isStunned = true,
                keywords = (target.keywords - Keyword.DEATHLESS) + Keyword.STUN
            )
            val updated = board.map { if (it.instanceId == unitId) revived else it }
            logs.add("${target.template.name} revived via Deathless (1 HP, Stunned)!")
            return if (isEnemy) s.copy(enemyBoard = updated) else s.copy(playerBoard = updated)
        }

        // Anivia Last Breath: Transform into Eggnivia (0|2)
        if (!isEnemy && target.template.id == "card_anivia") {
            val eggTmpl = CardCatalog.getCard("card_eggnivia")
            val egg = UnitOnBoard(
                instanceId = target.instanceId,
                template = eggTmpl,
                ownerIsPlayer = true,
                power = eggTmpl.basePower,
                health = eggTmpl.baseHealth,
                maxHealth = eggTmpl.baseHealth,
                keywords = eggTmpl.keywords,
                attachedItems = target.attachedItems
            )
            logs.add("Anivia's Last Breath: Revived as Eggnivia (0|2)!")
            return s.copy(playerBoard = s.playerBoard.map { if (it.instanceId == unitId) egg else it })
        }

        val updated = board.filterNot { it.instanceId == unitId }
        s = if (isEnemy) {
            s.copy(enemyBoard = updated, enemyGraveyard = s.enemyGraveyard + target.template)
        } else {
            s.copy(playerBoard = updated, playerGraveyard = s.playerGraveyard + target.template)
        }
        logs.add("${target.template.name} was slain.")

        if (target.keywords.contains(Keyword.HALLOWED)) {
            s = if (!isEnemy) s.copy(playerHallowedStacks = s.playerHallowedStacks + 1)
            else s.copy(enemyHallowedStacks = s.enemyHallowedStacks + 1)
        }

        return s
    }

    private fun stunEnemyUnit(state: CombatState, unitId: String, logs: MutableList<String>): CombatState {
        var s = state
        val target = s.enemyBoard.find { it.instanceId == unitId } ?: return s
        s = s.copy(
            enemyBoard = s.enemyBoard.map {
                if (it.instanceId == unitId) it.copy(isStunned = true, keywords = it.keywords + Keyword.STUN) else it
            },
            enemiesStunnedCount = s.enemiesStunnedCount + 1
        )
        logs.add("Stunned ${target.template.name}.")

        val skillBonus = s.equippedRelics.sumOf { it.spellSkillDamageBonus }
        val yasuos = s.playerBoard.filter { it.template.id == "card_yasuo" }
        yasuos.forEach { yasuo ->
            val dmg = (if (yasuo.isLeveledUp) yasuo.effectivePower else 2) + skillBonus
            s = damageUnit(s, unitId, isEnemy = true, damage = dmg, logs = logs)
            logs.add("Yasuo struck stunned ${target.template.name} for $dmg!")
        }
        if (s.championId == "champ_yasuo" && s.starLevel >= 6) {
            s = damageUnit(s, unitId, isEnemy = true, damage = 5 + skillBonus, logs = logs)
        }
        s = s.copy(playerBoard = s.playerBoard.map {
            if (it.template.id == "card_fae_blader") it.copy(power = it.power + 2) else it
        })
        return s
    }

    private fun dealDamageToEnemyNexus(state: CombatState, dmg: Int, logs: MutableList<String>): CombatState {
        if (dmg <= 0) return state
        var s = state.copy(
            enemyNexusHp = maxOf(0, state.enemyNexusHp - dmg),
            playerPlunderActive = true
        )
        if (s.activePowers.any { it.id == "pow_raiding_party" } && s.playerHand.isNotEmpty()) {
            val firstUnitIdx = s.playerHand.indexOfFirst {
                it.template.type == CardType.UNIT || it.template.type == CardType.CHAMPION
            }
            if (firstUnitIdx >= 0) {
                val updatedHand = s.playerHand.toMutableList()
                val card = updatedHand[firstUnitIdx]
                updatedHand[firstUnitIdx] = card.copy(bonusPower = card.bonusPower + 1, bonusHealth = card.bonusHealth + 1)
                s = s.copy(playerHand = updatedHand)
            }
        }
        return checkGameOver(s)
    }

    private fun dealDamageToPlayerNexus(state: CombatState, dmg: Int, logs: MutableList<String>): CombatState {
        if (dmg <= 0) return state
        val s = state.copy(playerNexusHp = maxOf(0, state.playerNexusHp - dmg))
        return checkGameOver(s)
    }

    // --- PRIORITY PASS & ROUND TRANSITION ---

    fun passPriority(state: CombatState): CombatState {
        if (state.phase == CombatPhase.GAME_OVER || state.priorityHolder != PriorityHolder.PLAYER) return state
        val logs = state.combatLog.toMutableList()

        // Rule 9.2: Passing during DECLARE_BLOCK with 0+ blockers commits blocks
        if (state.phase == CombatPhase.COMBAT_DECLARE_BLOCK_PHASE) {
            return commitPlayerBlock(state)
        }

        // Rule 9.2: Passing while Spell Stack is non-empty resolves stack LIFO and transfers priority
        if (state.spellStack.isNotEmpty()) {
            logs.add("You passed — resolving Spell Stack.")
            return resolveSpellStack(state.copy(combatLog = logs.takeLast(16)))
        }

        // Rule 9.3: Passing in MAIN_ACTION_PHASE increments consecutivePasses; 2 passes ends round
        val newPasses = state.consecutivePasses + 1
        if (newPasses >= 2) {
            logs.add("Both players passed. Ending Round ${state.round}.")
            return advanceToNextRound(state, logs)
        }

        logs.add("You passed priority.")
        return state.copy(
            consecutivePasses = newPasses,
            priorityHolder = PriorityHolder.ENEMY,
            combatLog = logs.takeLast(16)
        )
    }

    internal fun drawEnemyCard(state: CombatState): CombatState {
        if (state.enemyDrawPool.isEmpty() || state.enemyHand.size >= 10) return state
        return state.copy(enemyHand = state.enemyHand + state.enemyDrawPool.random())
    }

    internal fun summonEnemyUnitFromPool(state: CombatState, chosen: CardTemplate, spendMana: Int, logs: MutableList<String>): CombatState {
        var bonusPow = 0
        var bonusHp = 0
        val extraKw = mutableSetOf<Keyword>()
        if (state.adventure.starRating >= 3.0f) {
            bonusPow += 1; bonusHp += 1
        }
        when (state.encounterNode.enemyPowerTitle) {
            "Volatile Munitions" -> extraKw.add(Keyword.IMPACT)
            "Unseen Path" -> if (chosen.keywords.contains(Keyword.ELUSIVE)) bonusPow += 1
            "Glorious Evolution" -> {
                bonusPow += 1; bonusHp += 1
                extraKw.add(Keyword.sharablePool.random())
            }
            "Blazing Dawn" -> {
                bonusPow += 1; bonusHp += 1
            }
        }

        val enemyUnit = UnitOnBoard(
            instanceId = "eu_${nextId()}",
            template = chosen,
            ownerIsPlayer = false,
            power = chosen.basePower + bonusPow,
            health = chosen.baseHealth + bonusHp,
            maxHealth = chosen.baseHealth + bonusHp,
            keywords = chosen.keywords + extraKw,
            impactStacks = chosen.impactStacks + (if (extraKw.contains(Keyword.IMPACT)) 1 else 0),
            hasBarrier = chosen.keywords.contains(Keyword.BARRIER),
            hasSpellShield = chosen.keywords.contains(Keyword.SPELLSHIELD),
            hasDeathless = chosen.keywords.contains(Keyword.DEATHLESS)
        )
        logs.add("Foe summoned ${chosen.name} (${enemyUnit.power}|${enemyUnit.health}).")
        return state.copy(
            enemyMana = maxOf(0, state.enemyMana - spendMana),
            enemyHand = removeFirstTemplate(state.enemyHand, chosen.id),
            enemyBoard = state.enemyBoard + enemyUnit
        )
    }

    internal fun removeFirstTemplate(hand: List<CardTemplate>, cardId: String): List<CardTemplate> {
        val idx = hand.indexOfFirst { it.id == cardId }
        if (idx < 0) return hand
        return hand.toMutableList().apply { removeAt(idx) }
    }

    internal fun advanceToNextRound(state: CombatState, logs: MutableList<String>): CombatState {
        var s = state

        val cleanPlayerBoard = s.playerBoard.filterNot { it.keywords.contains(Keyword.EPHEMERAL) }.map { u ->
            val healedHp = if (u.keywords.contains(Keyword.REGENERATION)) u.maxHealth else u.health
            u.copy(
                health = healedHp,
                hasBarrier = false,
                isStunned = false,
                isFrostbitten = false,
                keywords = u.keywords - Keyword.BARRIER - Keyword.STUN
            )
        }
        val cleanEnemyBoard = s.enemyBoard.filterNot { it.keywords.contains(Keyword.EPHEMERAL) }.map { u ->
            val healedHp = if (u.keywords.contains(Keyword.REGENERATION)) u.maxHealth else u.health
            u.copy(
                health = healedHp,
                hasBarrier = false,
                isStunned = false,
                isFrostbitten = false,
                keywords = u.keywords - Keyword.BARRIER - Keyword.STUN
            )
        }

        val cleanHand = s.playerHand.filterNot { it.isFleeting }

        val nextRound = s.round + 1
        val newPlayerMax = minOf(10, s.playerMaxMana + 1)
        val newEnemyMax = minOf(10, s.enemyMaxMana + 1)
        val playerSpellBank = minOf(3, maxOf(0, s.playerSpellMana) + maxOf(0, s.playerMana))
        val enemySpellBank = minOf(3, maxOf(0, s.enemySpellMana) + maxOf(0, s.enemyMana))

        val playerGetsToken = nextRound % 2 == 1
        val roundInitiator = if (playerGetsToken) PriorityHolder.PLAYER else PriorityHolder.ENEMY
        logs.add("--- Round $nextRound (${if (playerGetsToken) "Your" else "Foe's"} Attack) ---")

        s = s.copy(
            round = nextRound,
            playerMana = newPlayerMax,
            playerMaxMana = newPlayerMax,
            playerSpellMana = playerSpellBank,
            enemyMana = newEnemyMax,
            enemyMaxMana = newEnemyMax,
            enemySpellMana = enemySpellBank,
            playerHasAttackToken = playerGetsToken,
            enemyHasAttackToken = !playerGetsToken,
            scoutUsedThisRound = false,
            pendingScoutRally = false,
            playerBoard = cleanPlayerBoard,
            enemyBoard = cleanEnemyBoard,
            playerHand = cleanHand,
            playerCardsPlayedThisRound = 0,
            playerSpellsPlayedThisRound = 0,
            playerPlunderActive = false,
            phase = CombatPhase.MAIN_ACTION_PHASE,
            priorityHolder = roundInitiator,
            consecutivePasses = 0,
            lastStackCaster = null,
            combatLog = logs.takeLast(16)
        )

        s = drawPlayerCard(s)
        s = drawEnemyCard(s)
        s = applyRoundStartHooks(s, isGameStart = false)
        return s
    }

    internal fun checkChampionLevelUps(state: CombatState, logs: MutableList<String>): CombatState {
        var s = state
        var rallyTriggered = false
        val newlyLeveledIds = mutableListOf<String>()
        val updatedBoard = s.playerBoard.map { u ->
            if (u.template.isChampion && !u.isLeveledUp) {
                val shouldLevel = when (u.template.id) {
                    "card_jinx" -> s.playerHand.isEmpty() || s.playerSpellsPlayedThisRound >= 2
                    "card_lux" -> s.playerSpellsManaSpent >= 6
                    "card_yasuo" -> s.enemiesStunnedCount >= 3 || s.round >= 3
                    "card_ashe" -> s.enemiesFrostbittenCount >= 3 || s.round >= 3
                    "card_garen" -> s.round >= 3
                    "card_anivia" -> s.playerMaxMana >= 10
                    else -> s.round >= 3
                }
                if (shouldLevel) {
                    logs.add("★ ${u.template.name} LEVELED UP! (+${u.template.levelUpPowerBonus}|+${u.template.levelUpHealthBonus})")
                    newlyLeveledIds.add(u.template.id)
                    if (s.equippedRelics.any { it.id == "relic_crownguard" }) rallyTriggered = true
                    u.copy(
                        isLeveledUp = true,
                        power = u.power + u.template.levelUpPowerBonus,
                        health = u.health + u.template.levelUpHealthBonus,
                        maxHealth = u.maxHealth + u.template.levelUpHealthBonus
                    )
                } else u
            } else u
        }
        s = s.copy(playerBoard = updatedBoard)
        if (rallyTriggered) {
            s = s.copy(playerHasAttackToken = true)
            logs.add("Crownguard Inheritance triggered Rally!")
        }
        for (champId in newlyLeveledIds) {
            when (champId) {
                "card_jinx" -> {
                    s = dealDamageToEnemyNexus(s, 4, logs)
                    if (s.playerHand.size < 10) {
                        s = s.copy(
                            playerHand = s.playerHand + createHandInstance(
                                CardCatalog.getCard("card_super_mega_rocket"),
                                isFleeting = true
                            )
                        )
                    }
                    logs.add("Jinx Level Up blasted 4 Nexus damage & created Super Mega Death Rocket!")
                }
                "card_lux" -> {
                    if (s.playerHand.size < 10) {
                        s = s.copy(
                            playerHand = s.playerHand + createHandInstance(
                                CardCatalog.getCard("card_final_spark"),
                                isFleeting = true
                            )
                        )
                    }
                    logs.add("Lux Level Up created 0-cost Final Spark!")
                }
            }
        }
        return s
    }

    internal fun onPlayerSkillOrElementalPlayed(
        state: CombatState,
        logs: MutableList<String>,
        sourceName: String
    ): CombatState {
        var s = state.copy(playerElementalAndSkillsPlayed = state.playerElementalAndSkillsPlayed + 1)

        // Flame-Eyed Caprine: When you play a Skill or Elemental spell, Forge me (+1|+1)
        if (s.playerBoard.any { it.template.id == "card_flame_eyed_caprine" }) {
            s = s.copy(
                playerBoard = s.playerBoard.map { u ->
                    if (u.template.id == "card_flame_eyed_caprine") {
                        u.copy(power = u.power + 1, health = u.health + 1, maxHealth = u.maxHealth + 1)
                    } else u
                }
            )
            logs.add("Flame-Eyed Caprine Forged (+1|+1) from $sourceName!")
        }

        // Anivia 3★ Winter's Boon II: Gain +1 empty Mana gem permanently
        if (s.championId == "champ_anivia" && s.starLevel >= 3) {
            s = gainPlayerManaGem(s, empty = true, logs, "Winter's Boon II")
        }
        return s
    }

    internal fun gainPlayerManaGem(
        state: CombatState,
        empty: Boolean,
        logs: MutableList<String>,
        sourceLabel: String = "Mana Ramp"
    ): CombatState {
        var s = state
        if (s.playerMaxMana >= 10) {
            // Anivia 2★ Glacial Overflow: When Enlightened, grant all allies +1|+1 instead
            if (s.championId == "champ_anivia" && s.starLevel >= 2 && s.playerBoard.isNotEmpty()) {
                s = s.copy(
                    playerBoard = s.playerBoard.map {
                        it.copy(power = it.power + 1, health = it.health + 1, maxHealth = it.maxHealth + 1)
                    }
                )
                logs.add("Glacial Overflow (Enlightened): All allies gained +1|+1!")
            }
            return s
        }
        val newMax = minOf(10, s.playerMaxMana + 1)
        val newCurrent = if (empty) minOf(newMax, s.playerMana) else minOf(newMax, s.playerMana + 1)
        s = s.copy(playerMaxMana = newMax, playerMana = newCurrent)
        logs.add("$sourceLabel: Gained +1${if (empty) " empty" else ""} Mana gem (Max: $newMax).")
        if (newMax >= 10) {
            logs.add("ENLIGHTENED reached (10 Max Mana)!")
            s = applyEnlightenedBoardBuffs(s, logs)
            s = checkChampionLevelUps(s, logs)
        }
        return s
    }

    private fun applyEnlightenedBoardBuffs(state: CombatState, logs: MutableList<String>): CombatState {
        if (state.playerMaxMana < 10) return state
        var changed = false
        val updated = state.playerBoard.map { u ->
            if (u.template.id == "card_wild_mystics" && !u.hasEnlightenedBonusApplied) {
                changed = true
                logs.add("Wild Mystics reached Enlightened (+4|+4)!")
                u.copy(
                    power = u.power + 4,
                    health = u.health + 4,
                    maxHealth = u.maxHealth + 4,
                    hasEnlightenedBonusApplied = true
                )
            } else u
        }
        return if (changed) state.copy(playerBoard = updated) else state
    }

    internal fun checkGameOver(state: CombatState): CombatState {
        return when {
            state.enemyNexusHp <= 0 -> state.copy(phase = CombatPhase.GAME_OVER, playerWon = true)
            state.playerNexusHp <= 0 -> state.copy(phase = CombatPhase.GAME_OVER, playerWon = false)
            else -> state
        }
    }

    internal fun generateId(): String = nextId()
}
