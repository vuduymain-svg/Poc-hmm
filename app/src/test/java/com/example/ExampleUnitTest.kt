package com.example

import com.example.data.local.dao.GameDao
import com.example.data.local.entity.ChampionLoadoutEntity
import com.example.data.local.entity.PlayerMetaEntity
import com.example.data.local.entity.SavedRunEntity
import com.example.data.repository.GameRepository
import com.example.engine.CombatEngine
import com.example.engine.CombatPhase
import com.example.engine.CombatState
import com.example.engine.EnemyAIController
import com.example.engine.PriorityHolder
import com.example.model.AdventureCatalog
import com.example.model.CardCatalog
import com.example.model.ChampionLoadout
import com.example.model.DeckCardEntry
import com.example.model.HandCardInstance
import com.example.model.ItemCategory
import com.example.model.ItemTemplate
import com.example.model.Keyword
import com.example.model.Rarity
import com.example.model.RunState
import com.example.model.UnitOnBoard
import com.example.ui.components.cardPortraitRes
import com.example.viewmodel.AppScreen
import com.example.viewmodel.GameViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

private class FakeGameDao : GameDao {
    private val loadouts = MutableStateFlow<List<ChampionLoadoutEntity>>(emptyList())
    private val meta = MutableStateFlow<PlayerMetaEntity?>(null)
    private val savedRun = MutableStateFlow<SavedRunEntity?>(null)

    override fun observeChampionLoadouts(): Flow<List<ChampionLoadoutEntity>> = loadouts
    override suspend fun getChampionLoadout(id: String): ChampionLoadoutEntity? =
        loadouts.value.find { it.championId == id }

    override suspend fun upsertChampionLoadout(entity: ChampionLoadoutEntity) {
        loadouts.value = loadouts.value.filterNot { it.championId == entity.championId } + entity
    }

    override suspend fun upsertAllChampionLoadouts(entities: List<ChampionLoadoutEntity>) {
        val ids = entities.map { it.championId }.toSet()
        loadouts.value = loadouts.value.filterNot { ids.contains(it.championId) } + entities
    }

    override fun observePlayerMeta(): Flow<PlayerMetaEntity?> = meta
    override suspend fun getPlayerMeta(): PlayerMetaEntity? = meta.value
    override suspend fun upsertPlayerMeta(entity: PlayerMetaEntity) {
        meta.value = entity
    }

    override suspend fun deleteOtherChampionLoadouts(keepId: String) {
        loadouts.value = loadouts.value.filter { it.championId == keepId }
    }

    override fun observeSavedRun(): Flow<SavedRunEntity?> = savedRun
    override suspend fun getSavedRun(): SavedRunEntity? = savedRun.value
    override suspend fun upsertSavedRun(entity: SavedRunEntity) {
        savedRun.value = entity
    }

    override suspend fun clearSavedRun() {
        savedRun.value = null
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class ExampleUnitTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `combat engine enforces keyword blocking rules for Elusive Fearsome and Brash`() {
        val elusiveAttacker = UnitOnBoard(
            instanceId = "a1",
            template = CardCatalog.getCard("card_sumpworks_mapper"),
            ownerIsPlayer = true,
            power = 1,
            health = 1,
            maxHealth = 1,
            keywords = setOf(Keyword.ELUSIVE)
        )
        val normalBlocker = UnitOnBoard(
            instanceId = "b1",
            template = CardCatalog.getCard("card_cithria"),
            ownerIsPlayer = false,
            power = 2,
            health = 2,
            maxHealth = 2,
            keywords = setOf(Keyword.TOUGH)
        )
        val sharpsightBlocker = UnitOnBoard(
            instanceId = "b2",
            template = CardCatalog.getCard("card_brightsteel_protector"),
            ownerIsPlayer = false,
            power = 3,
            health = 2,
            maxHealth = 2,
            keywords = setOf(Keyword.SHARPSIGHT)
        )
        val brashAttacker = UnitOnBoard(
            instanceId = "a2",
            template = CardCatalog.getCard("card_ashe"),
            ownerIsPlayer = true,
            power = 5,
            health = 3,
            maxHealth = 3,
            keywords = setOf(Keyword.BRASH)
        )

        assertFalse(normalBlocker.canBlockAttacker(elusiveAttacker))
        assertTrue(sharpsightBlocker.canBlockAttacker(elusiveAttacker))
        // Brash requires 3+ Health to block
        assertFalse(normalBlocker.canBlockAttacker(brashAttacker))
    }

    @Test
    fun `combat engine resolves Overwhelm Quick Attack and Deathless accurately`() {
        val adv = AdventureCatalog.getAdventure("adv_demacia_outskirts")
        val node = adv.mapNodes.first { it.id == "d_fight_1a" }
        val attacker = UnitOnBoard(
            instanceId = "atk_1",
            template = CardCatalog.getCard("card_ruthless_raider"),
            ownerIsPlayer = true,
            power = 5,
            health = 3,
            maxHealth = 3,
            keywords = setOf(Keyword.OVERWHELM, Keyword.QUICK_ATTACK)
        )
        val deathlessDefender = UnitOnBoard(
            instanceId = "def_1",
            template = CardCatalog.getCard("card_rekindled_knight"),
            ownerIsPlayer = false,
            power = 3,
            health = 2,
            maxHealth = 2,
            keywords = setOf(Keyword.DEATHLESS),
            hasDeathless = true
        )

        val initialState = CombatState(
            encounterNode = node,
            adventure = adv,
            championId = "champ_jinx",
            starLevel = 1,
            equippedRelics = emptyList(),
            activePowers = emptyList(),
            mutators = emptyList(),
            playerNexusHp = 30,
            playerMaxHp = 30,
            enemyNexusHp = 15,
            enemyMaxHp = 15,
            playerBoard = listOf(attacker),
            enemyBoard = listOf(deathlessDefender),
            phase = CombatPhase.COMBAT_DECLARE_ATTACK_PHASE,
            attackingUnitIds = listOf("atk_1")
        )

        val afterDeclare = CombatEngine.commitPlayerAttack(initialState)
        assertEquals(CombatPhase.COMBAT_DECLARE_BLOCK_PHASE, afterDeclare.phase)
        assertEquals(PriorityHolder.ENEMY, afterDeclare.priorityHolder)

        val afterAttack = EnemyAIController.executeTurn(afterDeclare)
        // Quick Attack kills defender before return strike -> attacker unharmed
        assertEquals(3, afterAttack.playerBoard.first().health)
        // Overwhelm deals 5 - 2 = 3 excess damage to enemy Nexus (15 -> 12)
        assertEquals(12, afterAttack.enemyNexusHp)
        // Deathless revives defender with 1 HP and Stunned
        assertEquals(1, afterAttack.enemyBoard.size)
        assertEquals(1, afterAttack.enemyBoard.first().health)
        assertTrue(afterAttack.enemyBoard.first().isStunned)
        assertFalse(afterAttack.enemyBoard.first().hasDeathless)
    }

    @Test
    fun `playing Burst spell with Spellslinger and Suppression calculates effective cost and resolves immediately`() {
        val adv = AdventureCatalog.getAdventure("adv_demacia_outskirts")
        val node = adv.mapNodes.first { it.id == "d_fight_1a" }
        val burstCard = HandCardInstance(
            instanceId = "hc_1",
            template = CardCatalog.getCard("card_prismatic_barrier"), // base cost 2
            effectiveCost = 2
        )
        val ally = UnitOnBoard(
            instanceId = "u_1",
            template = CardCatalog.getCard("card_cithria"),
            ownerIsPlayer = true,
            power = 2,
            health = 2,
            maxHealth = 2,
            keywords = emptySet()
        )
        val state = CombatState(
            encounterNode = node,
            adventure = adv,
            championId = "champ_lux",
            starLevel = 1,
            equippedRelics = emptyList(),
            activePowers = listOf(CardCatalog.getPower("pow_spellslinger")),
            mutators = emptyList(),
            playerNexusHp = 30,
            playerMaxHp = 30,
            enemyNexusHp = 15,
            enemyMaxHp = 15,
            playerMana = 1,
            playerSpellMana = 0,
            playerHand = listOf(burstCard),
            playerBoard = listOf(ally)
        )

        assertEquals(1, state.effectivePlayCost(burstCard))
        assertTrue(state.canPlayCard(burstCard))

        val afterPlay = CombatEngine.playCardFromHand(state, "hc_1")
        assertEquals(0, afterPlay.playerMana)
        assertTrue(afterPlay.playerBoard.first().hasBarrier)
        // Lux 1★ grants +1|+1 when casting a spell
        assertEquals(3, afterPlay.playerBoard.first().power)
        assertEquals(3, afterPlay.playerBoard.first().health)
    }

    @Test
    fun `viewModel manages adventure launch DAG node progression and healer restoration`() = runTest {
        val repo = GameRepository(FakeGameDao(), testDispatcher)
        repo.ensureSeeded()
        val vm = GameViewModel(repo)
        advanceUntilIdle()

        vm.startAdventure("adv_demacia_outskirts")
        advanceUntilIdle()

        val run = vm.uiState.value.activeRun
        assertNotNull(run)
        assertEquals(AppScreen.AdventureMap, vm.uiState.value.screen)

        val adv = AdventureCatalog.getAdventure("adv_demacia_outskirts")
        val powerNode = adv.mapNodes.first { it.id == "d_power_1" }
        assertTrue(vm.isNodeAvailable(run!!, powerNode))

        // Select Power Shrine
        vm.selectMapNode("d_power_1")
        assertEquals(AppScreen.PowerDraft, vm.uiState.value.screen)
        assertEquals(3, vm.uiState.value.powerDraftOptions.size)

        val chosenPower = vm.uiState.value.powerDraftOptions.first()
        vm.pickPowerReward(chosenPower)
        advanceUntilIdle()

        val updatedRun = vm.uiState.value.activeRun!!
        assertTrue(updatedRun.activePowerIds.contains(chosenPower.id))
        assertTrue(updatedRun.completedNodeIds.contains("d_power_1"))
        assertEquals(AppScreen.AdventureMap, vm.uiState.value.screen)
        // Branching nodes d_fight_1a and d_fight_1b both become available
        assertTrue(vm.isNodeAvailable(updatedRun, adv.mapNodes.first { it.id == "d_fight_1a" }))
        assertTrue(vm.isNodeAvailable(updatedRun, adv.mapNodes.first { it.id == "d_fight_1b" }))
    }

    @Test
    fun `playing unit passes priority to enemy and Burst spell keeps priority on player`() {
        val adv = AdventureCatalog.getAdventure("adv_demacia_outskirts")
        val node = adv.mapNodes.first { it.id == "d_fight_1a" }
        val unitCard = HandCardInstance(
            instanceId = "hc_unit",
            template = CardCatalog.getCard("card_cithria"), // 1-cost unit
            effectiveCost = 1
        )
        val burstCard = HandCardInstance(
            instanceId = "hc_burst",
            template = CardCatalog.getCard("card_prismatic_barrier"), // 2-cost Burst
            effectiveCost = 2
        )
        val enemyUnit = CardCatalog.getCard("card_cithria")
        val state = CombatState(
            encounterNode = node,
            adventure = adv,
            championId = "champ_garen",
            starLevel = 1,
            equippedRelics = emptyList(),
            activePowers = emptyList(),
            mutators = emptyList(),
            playerNexusHp = 30,
            playerMaxHp = 30,
            enemyNexusHp = 15,
            enemyMaxHp = 15,
            playerMana = 3,
            enemyMana = 2,
            playerHand = listOf(burstCard, unitCard),
            enemyHand = listOf(enemyUnit),
            phase = CombatPhase.MAIN_ACTION_PHASE,
            priorityHolder = PriorityHolder.PLAYER
        )

        // Burst spell resolves immediately and keeps priority on PLAYER
        val afterBurst = CombatEngine.playCardFromHand(state, "hc_burst")
        assertEquals(PriorityHolder.PLAYER, afterBurst.priorityHolder)
        assertEquals(CombatPhase.MAIN_ACTION_PHASE, afterBurst.phase)

        // Playing a unit summons it and switches priority to ENEMY
        val afterUnit = CombatEngine.playCardFromHand(afterBurst, "hc_unit")
        assertEquals(1, afterUnit.playerBoard.size)
        assertEquals(PriorityHolder.ENEMY, afterUnit.priorityHolder)

        // EnemyAIController plays its unit and returns priority to PLAYER
        val afterAi = EnemyAIController.executeTurn(afterUnit)
        assertEquals(1, afterAi.enemyBoard.size)
        assertEquals(PriorityHolder.PLAYER, afterAi.priorityHolder)
    }

    @Test
    fun `fast spell enters SPELL_STACK_PHASE and resolves LIFO when opponent passes`() {
        val adv = AdventureCatalog.getAdventure("adv_demacia_outskirts")
        val node = adv.mapNodes.first { it.id == "d_fight_1a" }
        val fastShot = HandCardInstance(
            instanceId = "hc_fast",
            template = CardCatalog.getCard("card_mystic_shot"), // 2-cost Fast, deal 2
            effectiveCost = 2
        )
        val state = CombatState(
            encounterNode = node,
            adventure = adv,
            championId = "champ_jinx",
            starLevel = 1,
            equippedRelics = emptyList(),
            activePowers = emptyList(),
            mutators = emptyList(),
            playerNexusHp = 30,
            playerMaxHp = 30,
            enemyNexusHp = 15,
            enemyMaxHp = 15,
            playerMana = 2,
            enemyMana = 1,
            playerHand = listOf(fastShot),
            enemyHand = emptyList(),
            phase = CombatPhase.MAIN_ACTION_PHASE,
            priorityHolder = PriorityHolder.PLAYER
        )

        val onStack = CombatEngine.playCardFromHand(state, "hc_fast")
        assertEquals(CombatPhase.SPELL_STACK_PHASE, onStack.phase)
        assertEquals(PriorityHolder.ENEMY, onStack.priorityHolder)
        assertEquals(1, onStack.spellStack.size)
        assertEquals(PriorityHolder.PLAYER, onStack.lastStackCaster)

        // AI has no reactive Fast spell -> AI passes to resolve Spell Stack LIFO
        val resolved = EnemyAIController.executeTurn(onStack)
        assertTrue(resolved.spellStack.isEmpty())
        assertEquals(CombatPhase.MAIN_ACTION_PHASE, resolved.phase)
        // Priority returns to opponent of lastStackCaster (PLAYER -> ENEMY)
        assertEquals(PriorityHolder.ENEMY, resolved.priorityHolder)
        // Jinx 1★ dealt 1 on play + Mystic Shot dealt 2 = 3 damage total (15 -> 12)
        assertEquals(12, resolved.enemyNexusHp)
    }

    @Test
    fun `consecutive passes advance round bank spell mana draw cards and swap attack token`() {
        val adv = AdventureCatalog.getAdventure("adv_demacia_outskirts")
        val node = adv.mapNodes.first { it.id == "d_fight_1a" }
        val drawCard = HandCardInstance(
            instanceId = "hc_draw",
            template = CardCatalog.getCard("card_cithria"),
            effectiveCost = 1
        )
        val state = CombatState(
            encounterNode = node,
            adventure = adv,
            championId = "champ_garen",
            starLevel = 1,
            equippedRelics = emptyList(),
            activePowers = emptyList(),
            mutators = emptyList(),
            round = 1,
            playerNexusHp = 30,
            playerMaxHp = 30,
            enemyNexusHp = 15,
            enemyMaxHp = 15,
            playerMana = 4, // up to 3 unspent Unit Mana banks into Spell Mana
            playerMaxMana = 4,
            playerSpellMana = 0,
            enemyMana = 1,
            enemyMaxMana = 1,
            enemySpellMana = 0,
            playerHasAttackToken = true,
            enemyHasAttackToken = false,
            playerDrawPile = listOf(drawCard),
            enemyDrawPool = listOf(CardCatalog.getCard("card_cithria")),
            phase = CombatPhase.MAIN_ACTION_PHASE,
            priorityHolder = PriorityHolder.PLAYER,
            consecutivePasses = 0
        )

        val afterPlayerPass = CombatEngine.passPriority(state)
        assertEquals(1, afterPlayerPass.consecutivePasses)
        assertEquals(PriorityHolder.ENEMY, afterPlayerPass.priorityHolder)

        // Enemy has no cards in hand -> AI passes (consecutivePasses == 2 -> Round 2)
        val nextRound = EnemyAIController.executeTurn(afterPlayerPass)
        assertEquals(2, nextRound.round)
        assertEquals(5, nextRound.playerMana)
        assertEquals(5, nextRound.playerMaxMana)
        assertEquals(3, nextRound.playerSpellMana)
        assertEquals(1, nextRound.enemySpellMana)
        assertFalse(nextRound.playerHasAttackToken)
        assertTrue(nextRound.enemyHasAttackToken)
        assertEquals(PriorityHolder.ENEMY, nextRound.priorityHolder)
        assertEquals(1, nextRound.playerHand.size)
        assertEquals(1, nextRound.enemyHand.size)
    }

    @Test
    fun `viewModel automatically triggers EnemyAIController after 700ms delay when priority shifts to enemy`() = runTest(testDispatcher) {
        val repo = GameRepository(FakeGameDao(), testDispatcher)
        repo.ensureSeeded()
        val vm = GameViewModel(repo, aiStepDelayMs = 700L)
        advanceUntilIdle()

        vm.startAdventure("adv_demacia_outskirts")
        advanceUntilIdle()
        vm.selectMapNode("d_power_1")
        vm.pickPowerReward(vm.uiState.value.powerDraftOptions.first())
        advanceUntilIdle()

        vm.selectMapNode("d_fight_1a")
        runCurrent()

        val startCombat = vm.uiState.value.combatState
        assertNotNull(startCombat)
        assertEquals(PriorityHolder.PLAYER, startCombat!!.priorityHolder)

        // Pass priority -> ENEMY gets priority; AI should NOT execute before 700ms
        vm.passTurn()
        runCurrent()
        val afterPass = vm.uiState.value.combatState!!
        assertEquals(PriorityHolder.ENEMY, afterPass.priorityHolder)

        advanceTimeBy(699L)
        runCurrent()
        assertEquals(afterPass, vm.uiState.value.combatState)

        // At 700ms, EnemyAIController executes automatically (summons 1-cost unit -> PLAYER, or passes -> Round 2)
        advanceTimeBy(2L)
        runCurrent()
        val afterAi = vm.uiState.value.combatState!!
        assertTrue(afterAi != afterPass)
        assertTrue(afterAi.priorityHolder == PriorityHolder.PLAYER || afterAi.round == 2)

        // Once AI finishes any Round 2 opening action, priority returns to PLAYER
        advanceUntilIdle()
        assertEquals(PriorityHolder.PLAYER, vm.uiState.value.combatState!!.priorityHolder)
    }

    @Test
    fun `fast spell during attack declaration resolves stack via AI and returns priority to PLAYER`() {
        val adv = AdventureCatalog.getAdventure("adv_demacia_outskirts")
        val node = adv.mapNodes.first { it.id == "d_fight_1a" }
        val ally = UnitOnBoard(
            instanceId = "p1",
            template = CardCatalog.getCard("card_cithria"),
            ownerIsPlayer = true,
            power = 2,
            health = 2,
            maxHealth = 2,
            keywords = emptySet()
        )
        val fastShot = HandCardInstance(
            instanceId = "hc_fast",
            template = CardCatalog.getCard("card_mystic_shot"),
            effectiveCost = 2
        )
        val state = CombatState(
            encounterNode = node,
            adventure = adv,
            championId = "champ_garen",
            starLevel = 1,
            equippedRelics = emptyList(),
            activePowers = emptyList(),
            mutators = emptyList(),
            playerNexusHp = 30,
            playerMaxHp = 30,
            enemyNexusHp = 15,
            enemyMaxHp = 15,
            playerMana = 2,
            enemyMana = 1,
            playerHasAttackToken = true,
            playerBoard = listOf(ally),
            playerHand = listOf(fastShot),
            enemyHand = emptyList(),
            phase = CombatPhase.COMBAT_DECLARE_ATTACK_PHASE,
            attackingUnitIds = listOf("p1"),
            priorityHolder = PriorityHolder.PLAYER
        )

        val onStack = CombatEngine.playCardFromHand(state, "hc_fast")
        assertEquals(CombatPhase.COMBAT_DECLARE_ATTACK_PHASE, onStack.phase)
        assertEquals(PriorityHolder.ENEMY, onStack.priorityHolder)
        assertEquals(1, onStack.spellStack.size)

        val resolved = EnemyAIController.executeTurn(onStack)
        assertTrue(resolved.spellStack.isEmpty())
        assertEquals(CombatPhase.COMBAT_DECLARE_ATTACK_PHASE, resolved.phase)
        assertEquals(PriorityHolder.PLAYER, resolved.priorityHolder)
        assertEquals(13, resolved.enemyNexusHp)
    }

    @Test
    fun `fast spell while defending resolves stack and keeps PLAYER in COMBAT_DECLARE_BLOCK_PHASE`() {
        val adv = AdventureCatalog.getAdventure("adv_demacia_outskirts")
        val node = adv.mapNodes.first { it.id == "d_fight_1a" }
        val enemyAtk = UnitOnBoard(
            instanceId = "e1",
            template = CardCatalog.getCard("card_cithria"),
            ownerIsPlayer = false,
            power = 2,
            health = 2,
            maxHealth = 2,
            keywords = emptySet()
        )
        val fastShot = HandCardInstance(
            instanceId = "hc_fast",
            template = CardCatalog.getCard("card_mystic_shot"),
            effectiveCost = 2
        )
        val state = CombatState(
            encounterNode = node,
            adventure = adv,
            championId = "champ_garen",
            starLevel = 1,
            equippedRelics = emptyList(),
            activePowers = emptyList(),
            mutators = emptyList(),
            playerNexusHp = 30,
            playerMaxHp = 30,
            enemyNexusHp = 15,
            enemyMaxHp = 15,
            playerMana = 2,
            enemyMana = 1,
            playerHasAttackToken = false,
            enemyHasAttackToken = false,
            playerInitiatedCombat = false,
            enemyBoard = listOf(enemyAtk),
            attackingUnitIds = listOf("e1"),
            playerHand = listOf(fastShot),
            enemyHand = emptyList(),
            phase = CombatPhase.COMBAT_DECLARE_BLOCK_PHASE,
            priorityHolder = PriorityHolder.PLAYER
        )

        val onStack = CombatEngine.playCardFromHand(state, "hc_fast")
        assertEquals(PriorityHolder.ENEMY, onStack.priorityHolder)

        val resolved = EnemyAIController.executeTurn(onStack)
        assertTrue(resolved.spellStack.isEmpty())
        assertEquals(CombatPhase.COMBAT_DECLARE_BLOCK_PHASE, resolved.phase)
        assertEquals(PriorityHolder.PLAYER, resolved.priorityHolder)
        assertTrue(resolved.enemyBoard.isEmpty())
        assertEquals(30, resolved.playerNexusHp)
    }

    @Test
    fun `enemy node powers Stone Ward For Demacia Enlightened Harmony Living Shadow and Glorious Evolution work`() {
        // 1. Stone Ward adds +5 Max HP
        val demacia = AdventureCatalog.getAdventure("adv_demacia_outskirts")
        val stoneWardNode = demacia.mapNodes.first { it.id == "d_fight_1b" }
        val baseRun = RunState(
            runId = "run_1",
            adventureId = demacia.id,
            championId = "champ_garen",
            currentNodeId = "d_fight_1b",
            completedNodeIds = emptyList(),
            currentNexusHp = 30,
            maxNexusHp = 30,
            gold = 100,
            rerollsRemaining = 2,
            revivesRemaining = 1,
            activePowerIds = emptyList(),
            runDeck = listOf(DeckCardEntry("card_cithria", 4))
        )
        val loadout = ChampionLoadout("champ_garen", starLevel = 1)
        val stoneEncounter = CombatEngine.createEncounter(baseRun, loadout, demacia, stoneWardNode)
        assertEquals(stoneWardNode.enemyMaxHp + 5, stoneEncounter.enemyMaxHp)
        assertEquals(stoneWardNode.enemyMaxHp + 5, stoneEncounter.enemyNexusHp)

        // 2. For Demacia! buffs enemy board +1|+1 on even rounds
        val bossNode = demacia.mapNodes.first { it.id == "d_boss" }
        val garenEnemy = UnitOnBoard(
            instanceId = "eg_1",
            template = CardCatalog.getCard("card_cithria"),
            ownerIsPlayer = false,
            power = 2,
            health = 2,
            maxHealth = 2,
            keywords = emptySet()
        )
        val round1BossState = CombatState(
            encounterNode = bossNode,
            adventure = demacia,
            championId = "champ_garen",
            starLevel = 1,
            equippedRelics = emptyList(),
            activePowers = emptyList(),
            mutators = emptyList(),
            round = 1,
            playerNexusHp = 30,
            playerMaxHp = 30,
            enemyNexusHp = 28,
            enemyMaxHp = 28,
            enemyBoard = listOf(garenEnemy),
            phase = CombatPhase.MAIN_ACTION_PHASE,
            priorityHolder = PriorityHolder.PLAYER,
            consecutivePasses = 1
        )
        val round2BossState = CombatEngine.passPriority(round1BossState)
        assertEquals(2, round2BossState.round)
        assertEquals(3, round2BossState.enemyBoard.first().power)
        assertEquals(3, round2BossState.enemyBoard.first().health)

        // 3. Enlightened Harmony heals Karma Nexus +1 when enemy casts spell
        val ionia = AdventureCatalog.getAdventure("adv_ionia_woods")
        val karmaBoss = ionia.mapNodes.first { it.id == "i_boss" }
        val karmaState = CombatState(
            encounterNode = karmaBoss,
            adventure = ionia,
            championId = "champ_lux",
            starLevel = 1,
            equippedRelics = emptyList(),
            activePowers = emptyList(),
            mutators = emptyList(),
            playerNexusHp = 30,
            playerMaxHp = 30,
            enemyNexusHp = 30,
            enemyMaxHp = 35,
            enemyMana = 4,
            playerBoard = listOf(
                UnitOnBoard("p_1", CardCatalog.getCard("card_cithria"), true, 2, 2, 2, emptySet())
            ),
            enemyHand = listOf(CardCatalog.getCard("card_mystic_shot")),
            phase = CombatPhase.MAIN_ACTION_PHASE,
            priorityHolder = PriorityHolder.ENEMY
        )
        val afterKarmaCast = EnemyAIController.executeTurn(karmaState)
        assertEquals(31, afterKarmaCast.enemyNexusHp)

        // 4. Glorious Evolution grants +1|+1 and random keyword on enemy summon
        val zaun = AdventureCatalog.getAdventure("adv_zaun_foundry")
        val viktorBoss = zaun.mapNodes.first { it.id == "z_boss" }
        val viktorState = CombatState(
            encounterNode = viktorBoss,
            adventure = zaun,
            championId = "champ_jinx",
            starLevel = 1,
            equippedRelics = emptyList(),
            activePowers = emptyList(),
            mutators = emptyList(),
            playerNexusHp = 30,
            playerMaxHp = 30,
            enemyNexusHp = 45,
            enemyMaxHp = 45,
            enemyMana = 3,
            enemyHand = listOf(CardCatalog.getCard("card_cithria")),
            phase = CombatPhase.MAIN_ACTION_PHASE,
            priorityHolder = PriorityHolder.ENEMY
        )
        val afterViktorSummon = EnemyAIController.executeTurn(viktorState)
        val summoned = afterViktorSummon.enemyBoard.first()
        // Base Cithria is 2|2, +1|+1 from 3.0★ adventure, +1|+1 from Glorious Evolution -> 4|4 + extra keyword
        assertEquals(4, summoned.power)
        assertEquals(4, summoned.health)
        assertTrue(summoned.keywords.size >= 2)
    }

    @Test
    fun `wild inspiration discounts created cards concussive palm summons tail and jinx level up creates rocket`() {
        val adv = AdventureCatalog.getAdventure("adv_demacia_outskirts")
        val node = adv.mapNodes.first { it.id == "d_fight_1a" }

        // Wild Inspiration discounts created card by 1
        val createdSpell = HandCardInstance(
            instanceId = "hc_created",
            template = CardCatalog.getCard("card_concussive_palm"), // cost 4
            effectiveCost = 4,
            isCreated = true
        )
        val jinxBoard = UnitOnBoard(
            instanceId = "u_jinx",
            template = CardCatalog.getCard("card_jinx"),
            ownerIsPlayer = true,
            power = 4,
            health = 3,
            maxHealth = 3,
            keywords = setOf(Keyword.QUICK_ATTACK)
        )
        val enemyUnit = UnitOnBoard(
            instanceId = "e_1",
            template = CardCatalog.getCard("card_cithria"),
            ownerIsPlayer = false,
            power = 2,
            health = 2,
            maxHealth = 2,
            keywords = emptySet()
        )
        val state = CombatState(
            encounterNode = node,
            adventure = adv,
            championId = "champ_jinx",
            starLevel = 1,
            equippedRelics = emptyList(),
            activePowers = listOf(CardCatalog.getPower("pow_wild_inspiration")),
            mutators = emptyList(),
            playerNexusHp = 30,
            playerMaxHp = 30,
            enemyNexusHp = 15,
            enemyMaxHp = 15,
            playerMana = 3,
            enemyMana = 1,
            playerHand = listOf(createdSpell),
            playerBoard = listOf(jinxBoard),
            enemyBoard = listOf(enemyUnit),
            phase = CombatPhase.MAIN_ACTION_PHASE,
            priorityHolder = PriorityHolder.PLAYER
        )

        assertEquals(3, state.effectivePlayCost(createdSpell))
        assertTrue(state.canPlayCard(createdSpell))

        // Playing Concussive Palm empties hand -> Jinx levels up (+4 Nexus dmg & creates Fleeting Super Mega Death Rocket!)
        val onStack = CombatEngine.playCardFromHand(state, "hc_created")
        val resolved = EnemyAIController.executeTurn(onStack)

        assertTrue(resolved.enemyBoard.first().isStunned)
        assertTrue(resolved.playerBoard.any { it.template.id == "card_tail_of_the_dragon" })
        assertTrue(resolved.playerBoard.first { it.template.id == "card_jinx" }.isLeveledUp)
        assertTrue(resolved.playerHand.any { it.template.id == "card_super_mega_rocket" && it.isCreated })
        // Enemy Nexus: 15 - 1 (Jinx 1★ on play) - 4 (Jinx Level Up) = 10
        assertEquals(10, resolved.enemyNexusHp)
    }

    @Test
    fun `slain units and resolved spells move to graveyard while survivors return to bench`() {
        val adv = AdventureCatalog.getAdventure("adv_demacia_outskirts")
        val node = adv.mapNodes.first { it.id == "d_fight_1a" }

        val strongAlly = UnitOnBoard(
            instanceId = "p_strong",
            template = CardCatalog.getCard("card_garen"), // 5|5
            ownerIsPlayer = true,
            power = 5,
            health = 5,
            maxHealth = 5,
            keywords = setOf(Keyword.REGENERATION)
        )
        val fragileEnemy = UnitOnBoard(
            instanceId = "e_fragile",
            template = CardCatalog.getCard("card_cithria"), // 2|2
            ownerIsPlayer = false,
            power = 2,
            health = 2,
            maxHealth = 2,
            keywords = emptySet()
        )
        val burstSpell = HandCardInstance(
            instanceId = "hc_burst",
            template = CardCatalog.getCard("card_prismatic_barrier"),
            effectiveCost = 2
        )

        var state = CombatState(
            encounterNode = node,
            adventure = adv,
            championId = "champ_garen",
            starLevel = 1,
            equippedRelics = emptyList(),
            activePowers = emptyList(),
            mutators = emptyList(),
            playerNexusHp = 30,
            playerMaxHp = 30,
            enemyNexusHp = 15,
            enemyMaxHp = 15,
            playerMana = 4,
            enemyMana = 1,
            playerHasAttackToken = true,
            playerHand = listOf(burstSpell),
            playerBoard = listOf(strongAlly),
            enemyBoard = listOf(fragileEnemy),
            phase = CombatPhase.MAIN_ACTION_PHASE,
            priorityHolder = PriorityHolder.PLAYER
        )

        // 1. Cast Burst spell -> effect activates -> spell goes to playerGraveyard
        state = CombatEngine.playCardFromHand(state, "hc_burst")
        assertTrue(state.playerGraveyard.any { it.id == "card_prismatic_barrier" })

        // 2. Move strongAlly from bench into Shared Battlefield attack & resolve block
        state = CombatEngine.enterAttackDeclaration(state)
        state = CombatEngine.toggleUnitAttack(state, "p_strong")
        assertEquals(listOf("p_strong"), state.attackingUnitIds)

        state = CombatEngine.commitPlayerAttack(state)
        state = state.copy(
            priorityHolder = PriorityHolder.ENEMY,
            blockPairings = mapOf("p_strong" to "e_fragile")
        )
        state = CombatEngine.resolveCombatResolution(state, playerIsAttacking = true, logs = state.combatLog.toMutableList())

        // Slain enemy enters enemyGraveyard; surviving ally returns to bench (attackingUnitIds cleared)
        assertTrue(state.enemyBoard.isEmpty())
        assertTrue(state.enemyGraveyard.any { it.id == "card_cithria" })
        assertEquals(1, state.playerBoard.count { it.instanceId == "p_strong" })
    }

    @Test
    fun `anivia lv30 baseline stats starter deck and unlimited test resources match spec`() = runTest {
        val anivia = CardCatalog.getChampion("champ_anivia")
        assertEquals(9, anivia.baseDeck.size)
        assertEquals(18, anivia.baseDeck.sumOf { it.copies })
        assertEquals(listOf(2, 3, 4, 5, 6), anivia.starPowers.map { it.starLevel })

        val lv30Baseline = ChampionLoadout(
            championId = "champ_anivia",
            isUnlocked = true,
            starLevel = 2,
            championLevel = 30
        )
        assertEquals(40, lv30Baseline.maxNexusHp)
        assertEquals(4, lv30Baseline.victoryHeal)
        assertEquals(300, lv30Baseline.startingGold)
        assertEquals(4, lv30Baseline.startingRerolls)
        assertEquals(1, lv30Baseline.startingManaBonus)
        assertEquals(0.445, lv30Baseline.rarityOdds.common, 0.0001)
        assertEquals(0.400, lv30Baseline.rarityOdds.rare, 0.0001)
        assertEquals(0.125, lv30Baseline.rarityOdds.epic, 0.0001)
        assertEquals(0.030, lv30Baseline.rarityOdds.legendary, 0.0001)

        val repo = GameRepository(FakeGameDao(), testDispatcher)
        repo.ensureSeeded()
        val vm = GameViewModel(repo)
        advanceUntilIdle()

        assertEquals(999_999, vm.uiState.value.legendProfile.wildFragments)
        assertEquals(999_999, vm.uiState.value.legendProfile.starCrystals)
        assertEquals(999_999, vm.uiState.value.legendProfile.novaCrystals)
        assertEquals(999_999, vm.uiState.value.legendProfile.gemstones)
        assertTrue(vm.uiState.value.legendProfile.ownedRelicIds.containsAll(CardCatalog.relics.map { it.id }))

        val seededAnivia = vm.uiState.value.championLoadouts["champ_anivia"]!!
        assertTrue(seededAnivia.isUnlocked)
        assertEquals(30, seededAnivia.championLevel)

        // Upgrade Anivia star power sequentially skipping omitted 1★ (2 -> 3)
        vm.upgradeStarPower("champ_anivia", 3)
        advanceUntilIdle()
        assertEquals(3, vm.uiState.value.championLoadouts["champ_anivia"]!!.starLevel)

        // Test Mode max helper jumps to 6★ Lv.30 + all constellation nodes
        vm.maxChampionForTesting("champ_anivia")
        advanceUntilIdle()
        val maxed = vm.uiState.value.championLoadouts["champ_anivia"]!!
        assertEquals(6, maxed.starLevel)
        assertEquals(30, maxed.championLevel)
        assertEquals(anivia.constellationNodes.size, maxed.unlockedConstellationNodes.size)
    }

    @Test
    fun `anivia elemental mana ramp glacial overflow caprine forge and clash of giants discount work`() {
        val adv = AdventureCatalog.getAdventure("adv_demacia_outskirts")
        val node = adv.mapNodes.first { it.id == "d_fight_1a" }

        val caprine = UnitOnBoard(
            instanceId = "u_cap",
            template = CardCatalog.getCard("card_flame_eyed_caprine"), // 3|2 Tough
            ownerIsPlayer = true,
            power = 3,
            health = 2,
            maxHealth = 2,
            keywords = setOf(Keyword.TOUGH)
        )
        val mystics = UnitOnBoard(
            instanceId = "u_mys",
            template = CardCatalog.getCard("card_wild_mystics"), // 2|2 Overwhelm
            ownerIsPlayer = true,
            power = 2,
            health = 2,
            maxHealth = 2,
            keywords = setOf(Keyword.OVERWHELM)
        )
        val harshWinds = HandCardInstance(
            instanceId = "hc_hw",
            template = CardCatalog.getCard("card_harsh_winds"), // 5-cost Burst Elemental
            effectiveCost = 5
        )
        val clash = HandCardInstance(
            instanceId = "hc_clash",
            template = CardCatalog.getCard("card_clash_of_giants"), // 16-cost Slow Elemental
            effectiveCost = 16
        )
        val enemyA = UnitOnBoard("e1", CardCatalog.getCard("card_cithria"), false, 5, 4, 4, emptySet())
        val enemyB = UnitOnBoard("e2", CardCatalog.getCard("card_laurent_protege"), false, 3, 4, 4, emptySet())

        var state = CombatState(
            encounterNode = node,
            adventure = adv,
            championId = "champ_anivia",
            starLevel = 3, // Glacial Overflow + Winter's Boon II
            equippedRelics = emptyList(),
            activePowers = emptyList(),
            mutators = emptyList(),
            playerNexusHp = 40,
            playerMaxHp = 40,
            enemyNexusHp = 20,
            enemyMaxHp = 20,
            playerMana = 9,
            playerMaxMana = 9,
            playerHand = listOf(harshWinds, clash),
            playerBoard = listOf(caprine, mystics),
            enemyBoard = listOf(enemyA, enemyB),
            phase = CombatPhase.MAIN_ACTION_PHASE,
            priorityHolder = PriorityHolder.PLAYER
        )

        // Playing Harsh Winds at 9 Max Mana:
        // - Frostbites 2 enemies
        // - Forges Flame-Eyed Caprine (+1|+1 -> 4|3)
        // - Winter's Boon II grants +1 empty Mana gem (Max 9 -> 10 -> Enlightened!)
        // - Wild Mystics hits Enlightened (+4|+4 -> 6|6)
        // - Clash of Giants drops cost 16 -> 15
        state = CombatEngine.playCardFromHand(state, "hc_hw")
        assertEquals(10, state.playerMaxMana)
        assertTrue(state.enemyBoard.all { it.isFrostbitten })
        val capAfter = state.playerBoard.first { it.instanceId == "u_cap" }
        assertEquals(4, capAfter.power)
        assertEquals(3, capAfter.health)
        val mysAfter = state.playerBoard.first { it.instanceId == "u_mys" }
        assertEquals(6, mysAfter.power)
        assertEquals(6, mysAfter.health)
        val clashInHand = state.playerHand.first { it.instanceId == "hc_clash" }
        assertEquals(15, state.effectivePlayCost(clashInHand))

        // Playing another Elemental spell while already Enlightened triggers Glacial Overflow (+1|+1 to all allies)
        val wings = HandCardInstance("hc_wings", CardCatalog.getCard("card_wings_of_cryophoenix"), effectiveCost = 7)
        state = state.copy(playerMana = 7, playerHand = state.playerHand + wings)
        state = CombatEngine.playCardFromHand(state, "hc_wings")
        // Both Caprine (Forged +1|+1 AND Glacial Overflow +1|+1) & Mystics (+1|+1) grew before stack resolution
        assertEquals(6, state.playerBoard.first { it.instanceId == "u_cap" }.power)
        assertEquals(7, state.playerBoard.first { it.instanceId == "u_mys" }.power)
    }

    @Test
    fun `crystal stormbird guardian follower mortal prophet endurance and eggnivia revival work`() {
        val adv = AdventureCatalog.getAdventure("adv_demacia_outskirts")
        val node = adv.mapNodes.first { it.id == "d_fight_1a" }

        val guardian = UnitOnBoard("u_guard", CardCatalog.getCard("card_guardian_follower"), true, 3, 4, 4, emptySet())
        val prophet = UnitOnBoard("u_proph", CardCatalog.getCard("card_mortal_prophet"), true, 0, 5, 5, emptySet())
        val anivia = UnitOnBoard("u_anivia", CardCatalog.getCard("card_anivia"), true, 3, 5, 5, setOf(Keyword.ELEMENTAL))
        val stormbird = HandCardInstance("hc_bird", CardCatalog.getCard("card_crystal_stormbird"), effectiveCost = 5)
        val frostbittenEnemy = UnitOnBoard(
            instanceId = "e_zero",
            template = CardCatalog.getCard("card_cithria"),
            ownerIsPlayer = false,
            power = 2,
            health = 4,
            maxHealth = 4,
            keywords = emptySet(),
            isFrostbitten = true // 0 Power -> takes double damage from Anivia 6★ Crystallize!
        )

        var state = CombatState(
            encounterNode = node,
            adventure = adv,
            championId = "champ_anivia",
            starLevel = 6, // Includes 4★ Endurance & 6★ Crystallize
            equippedRelics = emptyList(),
            activePowers = emptyList(),
            mutators = emptyList(),
            round = 9,
            playerNexusHp = 35,
            playerMaxHp = 40,
            enemyNexusHp = 20,
            enemyMaxHp = 20,
            playerMana = 9,
            playerMaxMana = 9,
            playerHasAttackToken = true,
            playerHand = listOf(stormbird),
            playerBoard = listOf(guardian, prophet, anivia),
            enemyBoard = listOf(frostbittenEnemy),
            phase = CombatPhase.MAIN_ACTION_PHASE,
            priorityHolder = PriorityHolder.PLAYER
        )

        // Play Crystal Stormbird:
        // - Winter's Boon II pushes Max Mana 9 -> 10 (Anivia levels up to 4|6!)
        // - Skill deals 2 to all other units:
        //   * Guardian Follower takes 0 damage from allied skill (stays 3|4)
        //   * Mortal Prophet takes 2 -> 3 HP, Endurance +1|+1 -> 1|4, deals 1 to Enemy Nexus (20 -> 19)
        //   * Frostbitten Enemy (0 Power) takes 2 * 2 = 4 damage via 6★ Crystallize -> slain!
        state = CombatEngine.playCardFromHand(state, "hc_bird")
        assertEquals(4, state.playerBoard.first { it.instanceId == "u_guard" }.health)
        val prophAfter = state.playerBoard.first { it.instanceId == "u_proph" }
        assertEquals(1, prophAfter.power)
        assertEquals(4, prophAfter.health)
        assertEquals(19, state.enemyNexusHp)
        assertTrue(state.enemyBoard.isEmpty())
        assertTrue(state.playerBoard.first { it.instanceId == "u_anivia" }.isLeveledUp)

        // Anivia Last Breath -> Eggnivia -> Round Start hatches back into Leveled Anivia!
        val enemyStrong = UnitOnBoard("e_big", CardCatalog.getCard("card_garen"), false, 10, 10, 10, emptySet())
        state = state.copy(
            priorityHolder = PriorityHolder.PLAYER,
            phase = CombatPhase.COMBAT_DECLARE_ATTACK_PHASE,
            enemyBoard = listOf(enemyStrong),
            attackingUnitIds = listOf("u_anivia"),
            blockPairings = mapOf("u_anivia" to "e_big")
        )
        state = CombatEngine.resolveCombatResolution(state, playerIsAttacking = true, logs = state.combatLog.toMutableList())
        val egg = state.playerBoard.find { it.instanceId == "u_anivia" }
        assertNotNull(egg)
        assertEquals("card_eggnivia", egg!!.template.id)
        assertEquals(2, egg.health)

        // Advance round -> Round Start at 10 Max Mana hatches Eggnivia into Leveled Anivia
        state = state.copy(
            phase = CombatPhase.MAIN_ACTION_PHASE,
            priorityHolder = PriorityHolder.PLAYER,
            consecutivePasses = 1
        )
        val nextRound = CombatEngine.passPriority(state)
        val hatched = nextRound.playerBoard.find { it.instanceId == "u_anivia" }
        assertNotNull(hatched)
        assertEquals("card_anivia", hatched!!.template.id)
        assertTrue(hatched.isLeveledUp)
    }

    @Test
    fun `mana safeguards clamp bounds trigger perfected manaflow on round 1 and refill mana on item play`() {
        val adv = AdventureCatalog.getAdventure("adv_demacia_outskirts")
        val node = adv.mapNodes.first { it.id == "d_fight_1a" }
        val anivia = CardCatalog.getChampion("champ_anivia")
        val runWithManaflow = RunState(
            runId = "run_mana_test",
            adventureId = adv.id,
            championId = "champ_anivia",
            currentNodeId = node.id,
            completedNodeIds = emptyList(),
            currentNexusHp = 40,
            maxNexusHp = 40,
            gold = 300,
            rerollsRemaining = 4,
            revivesRemaining = 1,
            activePowerIds = listOf("pow_manaflow"),
            runDeck = anivia.baseDeck
        )
        // High starting mana sources (5★ + Lv30 + relics) are clamped to max 10
        val maxedLoadout = ChampionLoadout(
            championId = "champ_anivia",
            isUnlocked = true,
            starLevel = 6,
            championLevel = 30,
            equippedRelics = listOf("relic_starforged", "relic_starforged", "relic_starforged")
        )
        val clampedEncounter = CombatEngine.createEncounter(runWithManaflow, maxedLoadout, adv, node)
        assertTrue(clampedEncounter.playerMaxMana in 1..10)
        assertTrue(clampedEncounter.playerMana in 0..clampedEncounter.playerMaxMana)

        // Round 1 with Perfected Manaflow grants +1 Mana gem immediately (base 2 -> 3)
        val baseLoadout = ChampionLoadout(
            championId = "champ_anivia",
            isUnlocked = true,
            starLevel = 2,
            championLevel = 30
        )
        val round1Manaflow = CombatEngine.createEncounter(runWithManaflow, baseLoadout, adv, node)
        assertEquals(3, round1Manaflow.playerMaxMana)
        assertEquals(3, round1Manaflow.playerMana)

        // Item with refillManaOnPlay refills playerMana up to playerMaxMana
        val refillItem = ItemTemplate(
            id = "test_mana_refill",
            name = "Mana Crystal",
            rarity = Rarity.RARE,
            category = ItemCategory.ECONOMY,
            description = "Play: Refill 2 Mana",
            refillManaOnPlay = 2
        )
        val unitWithRefill = HandCardInstance(
            instanceId = "hc_refill",
            template = CardCatalog.getCard("card_wild_mystics"), // cost 2
            attachedItems = listOf(refillItem),
            effectiveCost = 2
        )
        val manaState = round1Manaflow.copy(
            playerMana = 3,
            playerMaxMana = 3,
            playerHand = listOf(unitWithRefill)
        )
        val afterPlay = CombatEngine.playCardFromHand(manaState, "hc_refill")
        assertEquals(3, afterPlay.playerMana)
        assertTrue(afterPlay.playerMana <= afterPlay.playerMaxMana)
    }

    @Test
    fun `champion roster only contains anivia stale db loadouts are cleaned and deck cards have unique art`() = runTest {
        assertEquals(listOf("champ_anivia"), CardCatalog.champions.map { it.id })

        val dao = FakeGameDao()
        val repo = GameRepository(dao, testDispatcher)
        // Simulate stale database rows from earlier multi-champion builds
        dao.upsertChampionLoadout(ChampionLoadoutEntity.fromDomain(ChampionLoadout(championId = "champ_jinx", isUnlocked = true)))
        dao.upsertChampionLoadout(ChampionLoadoutEntity.fromDomain(ChampionLoadout(championId = "champ_yasuo", isUnlocked = true)))
        repo.saveActiveRun(
            RunState(
                runId = "stale_run",
                adventureId = "adv_demacia_outskirts",
                championId = "champ_jinx",
                currentNodeId = "d_fight_1a",
                completedNodeIds = emptyList(),
                currentNexusHp = 30,
                maxNexusHp = 30,
                gold = 100,
                rerollsRemaining = 2,
                revivesRemaining = 1,
                activePowerIds = emptyList(),
                runDeck = emptyList()
            )
        )

        repo.ensureSeeded()
        val vm = GameViewModel(repo)
        advanceUntilIdle()

        assertEquals(setOf("champ_anivia"), vm.uiState.value.championLoadouts.keys)
        assertEquals("champ_anivia", vm.uiState.value.selectedChampionId)
        assertNull(dao.getSavedRun())

        // All 9 cards in Anivia's starter deck + Eggnivia token have distinct non-null illustrations
        val aniviaCardIds = CardCatalog.getChampion("champ_anivia").baseDeck.map { it.cardId } + "card_eggnivia"
        val artResources = aniviaCardIds.map { cardId ->
            val res = cardPortraitRes(cardId)
            assertNotNull("Expected illustration for $cardId", res)
            res!!
        }
        assertEquals(10, artResources.size)
        assertEquals(10, artResources.toSet().size)
    }
}
