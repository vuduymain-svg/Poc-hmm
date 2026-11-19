package com.example

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ApplicationProvider
import com.example.engine.CombatPhase
import com.example.engine.CombatState
import com.example.model.AdventureCatalog
import com.example.model.CardCatalog
import com.example.model.UnitOnBoard
import com.example.ui.components.BattleBoard
import com.example.ui.theme.PathOfChampionsTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w850dp-h390dp-land")
class ExampleRobolectricTest {

  @get:Rule
  val composeTestRule = createComposeRule()

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Path of Champions", appName)
  }

  @Test
  fun `battleBoard arranges AI bench top shared battlefield center and player bench bottom in landscape`() {
    val adv = AdventureCatalog.getAdventure("adv_demacia_outskirts")
    val node = adv.mapNodes.first { it.id == "d_fight_1a" }
    val enemyUnit = UnitOnBoard(
      instanceId = "e_1",
      template = CardCatalog.getCard("card_cithria"),
      ownerIsPlayer = false,
      power = 2,
      health = 2,
      maxHealth = 2,
      keywords = emptySet()
    )
    val playerUnits = (1..6).map { idx ->
      UnitOnBoard(
        instanceId = "p_$idx",
        template = CardCatalog.getCard("card_cithria"),
        ownerIsPlayer = true,
        power = 2,
        health = 2,
        maxHealth = 2,
        keywords = emptySet()
      )
    }
    val combatState = CombatState(
      encounterNode = node,
      adventure = adv,
      championId = "champ_anivia",
      starLevel = 2,
      equippedRelics = emptyList(),
      activePowers = emptyList(),
      mutators = emptyList(),
      playerNexusHp = 40,
      playerMaxHp = 40,
      enemyNexusHp = 15,
      enemyMaxHp = 15,
      playerBoard = playerUnits,
      enemyBoard = listOf(enemyUnit),
      phase = CombatPhase.COMBAT_DECLARE_ATTACK_PHASE,
      attackingUnitIds = listOf("p_1")
    )

    composeTestRule.setContent {
      PathOfChampionsTheme {
        Box(modifier = Modifier.size(width = 600.dp, height = 270.dp)) {
          BattleBoard(combat = combatState)
        }
      }
    }
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("battle_board").assertIsDisplayed()
    composeTestRule.onNodeWithTag("enemy_bench_zone").assertIsDisplayed()
    composeTestRule.onNodeWithTag("shared_battlefield_zone").assertIsDisplayed()
    composeTestRule.onNodeWithTag("player_bench_zone").assertIsDisplayed()

    val boardBounds = composeTestRule.onNodeWithTag("battle_board").getUnclippedBoundsInRoot()
    val aiBounds = composeTestRule.onNodeWithTag("enemy_bench_zone").getUnclippedBoundsInRoot()
    val fieldBounds = composeTestRule.onNodeWithTag("shared_battlefield_zone").getUnclippedBoundsInRoot()
    val playerBounds = composeTestRule.onNodeWithTag("player_bench_zone").getUnclippedBoundsInRoot()

    // Landscape orientation: board is wider than tall
    assertTrue((boardBounds.right - boardBounds.left) > (boardBounds.bottom - boardBounds.top))
    // Strict vertical ordering: AI Bench (top) -> Shared Battlefield (center) -> Player Bench (bottom)
    assertTrue(aiBounds.bottom <= fieldBounds.top)
    assertTrue(fieldBounds.bottom <= playerBounds.top)

    // Attacking unit p_1 renders once (in Shared Battlefield, not duplicated on Player Bench)
    assertEquals(1, composeTestRule.onAllNodesWithTag("board_unit_p_1").fetchSemanticsNodes().size)
    // Remaining bench units p_2..p_6 and AI bench unit e_1 render cleanly
    assertEquals(1, composeTestRule.onAllNodesWithTag("board_unit_e_1").fetchSemanticsNodes().size)
    assertEquals(1, composeTestRule.onAllNodesWithTag("board_unit_p_6").fetchSemanticsNodes().size)
  }

  @Test
  fun `combatScreen renders LoR layout with current_max mana and no wireframe labels`() {
    val adv = AdventureCatalog.getAdventure("adv_demacia_outskirts")
    val node = adv.mapNodes.first { it.id == "d_fight_1a" }
    val cithria = CardCatalog.getCard("card_cithria")
    val mysticShot = CardCatalog.getCard("card_mystic_shot")
    val hand = listOf(
      com.example.model.HandCardInstance(
        instanceId = "hc_1",
        template = cithria,
        effectiveCost = cithria.cost
      ),
      com.example.model.HandCardInstance(
        instanceId = "hc_2",
        template = mysticShot,
        effectiveCost = mysticShot.cost
      )
    )
    val combatState = CombatState(
      encounterNode = node,
      adventure = adv,
      championId = "champ_yasuo",
      starLevel = 2,
      equippedRelics = emptyList(),
      activePowers = emptyList(),
      mutators = emptyList(),
      round = 4,
      playerNexusHp = 45,
      playerMaxHp = 45,
      enemyNexusHp = 16,
      enemyMaxHp = 20,
      playerMana = 4,
      playerMaxMana = 10,
      playerSpellMana = 2,
      enemyMana = 3,
      enemyMaxMana = 10,
      enemySpellMana = 1,
      playerHand = hand,
      enemyHand = listOf(cithria, mysticShot),
      playerBoard = listOf(
        UnitOnBoard(
          instanceId = "p_1",
          template = cithria,
          ownerIsPlayer = true,
          power = 4,
          health = 3,
          maxHealth = 3,
          keywords = emptySet()
        )
      ),
      enemyBoard = listOf(
        UnitOnBoard(
          instanceId = "e_1",
          template = cithria,
          ownerIsPlayer = false,
          power = 2,
          health = 2,
          maxHealth = 2,
          keywords = emptySet()
        )
      )
    )

    composeTestRule.setContent {
      PathOfChampionsTheme {
        Box(modifier = Modifier.size(width = 850.dp, height = 390.dp)) {
          com.example.ui.screens.CombatScreen(
            combat = combatState,
            revivesRemaining = 1,
            onPlayCard = {},
            onEnterAttack = {},
            onCancelAttack = {},
            onToggleAttacker = {},
            onCommitAttack = {},
            onSelectEnemyAttackerForBlock = {},
            onAssignBlocker = { _, _ -> },
            onRemoveBlocker = {},
            onCommitBlock = {},
            onResolveStack = {},
            onPassTurn = {},
            onVictoryContinue = {},
            onUseRevive = {},
            onAcceptDefeat = {},
            onAbandonRun = {}
          )
        }
      }
    }
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("enemy_mana_text").assertIsDisplayed()
    composeTestRule.onNodeWithTag("player_mana_text").assertIsDisplayed()
    composeTestRule.onNodeWithTag("enemy_nexus_hp").assertIsDisplayed()
    composeTestRule.onNodeWithTag("player_nexus_hp").assertIsDisplayed()
    composeTestRule.onNodeWithTag("combat_log_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("end_round_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("enemy_hand_count").assertIsDisplayed()
    composeTestRule.onNodeWithTag("hand_card_card_cithria").assertIsDisplayed()
    composeTestRule.onNodeWithTag("hand_card_card_mystic_shot").assertIsDisplayed()

    // Verify hand cards stay within screen bottom & right bounds (never clipped)
    val cithriaBounds = composeTestRule.onNodeWithTag("hand_card_card_cithria").getUnclippedBoundsInRoot()
    val mysticBounds = composeTestRule.onNodeWithTag("hand_card_card_mystic_shot").getUnclippedBoundsInRoot()
    assertTrue(cithriaBounds.bottom <= 390.dp)
    assertTrue(mysticBounds.bottom <= 390.dp)
    assertTrue(mysticBounds.right <= 850.dp)

    // Verify mana texts show current/max format (3/10 and 4/10)
    assertEquals(
      1,
      composeTestRule.onAllNodes(
        androidx.compose.ui.test.hasText("3/10")
      ).fetchSemanticsNodes().size
    )
    assertEquals(
      1,
      composeTestRule.onAllNodes(
        androidx.compose.ui.test.hasText("4/10")
      ).fetchSemanticsNodes().size
    )

    // Verify wireframe placeholder labels are gone
    assertEquals(
      0,
      composeTestRule.onAllNodes(
        androidx.compose.ui.test.hasText("AI BENCH", substring = true)
      ).fetchSemanticsNodes().size
    )
    assertEquals(
      0,
      composeTestRule.onAllNodes(
        androidx.compose.ui.test.hasText("PLAYER BENCH", substring = true)
      ).fetchSemanticsNodes().size
    )
    assertEquals(
      0,
      composeTestRule.onAllNodes(
        androidx.compose.ui.test.hasText("Shared Battlefield", substring = true)
      ).fetchSemanticsNodes().size
    )
  }

  @Test
  fun `anivia newly generated card drawables decode and render in CombatScreen without crashing`() {
    val adv = AdventureCatalog.getAdventure("adv_demacia_outskirts")
    val node = adv.mapNodes.first { it.id == "d_fight_1a" }
    val aniviaCards = listOf(
      "card_anivia",
      "card_flame_eyed_caprine",
      "card_harsh_winds",
      "card_mortal_prophet",
      "card_guardian_follower",
      "card_wings_of_cryophoenix"
    ).mapIndexed { idx, id ->
      val tmpl = CardCatalog.getCard(id)
      com.example.model.HandCardInstance(
        instanceId = "anv_hc_$idx",
        template = tmpl,
        effectiveCost = tmpl.cost
      )
    }
    val eggUnit = UnitOnBoard(
      instanceId = "egg_1",
      template = CardCatalog.getCard("card_eggnivia"),
      ownerIsPlayer = true,
      power = 0,
      health = 1,
      maxHealth = 1,
      keywords = emptySet()
    )
    val stormbird = UnitOnBoard(
      instanceId = "storm_1",
      template = CardCatalog.getCard("card_crystal_stormbird"),
      ownerIsPlayer = false,
      power = 3,
      health = 3,
      maxHealth = 3,
      keywords = emptySet()
    )
    val combatState = CombatState(
      encounterNode = node,
      adventure = adv,
      championId = "champ_anivia",
      starLevel = 2,
      equippedRelics = emptyList(),
      activePowers = emptyList(),
      mutators = emptyList(),
      round = 2,
      playerNexusHp = 35,
      playerMaxHp = 35,
      enemyNexusHp = 20,
      enemyMaxHp = 20,
      playerMana = 5,
      playerMaxMana = 5,
      playerHand = aniviaCards,
      enemyHand = listOf(CardCatalog.getCard("card_harsh_winds")),
      playerBoard = listOf(eggUnit),
      enemyBoard = listOf(stormbird)
    )

    composeTestRule.setContent {
      PathOfChampionsTheme {
        Box(modifier = Modifier.size(width = 850.dp, height = 390.dp)) {
          com.example.ui.screens.CombatScreen(
            combat = combatState,
            revivesRemaining = 1,
            onPlayCard = {},
            onEnterAttack = {},
            onCancelAttack = {},
            onToggleAttacker = {},
            onCommitAttack = {},
            onSelectEnemyAttackerForBlock = {},
            onAssignBlocker = { _, _ -> },
            onRemoveBlocker = {},
            onCommitBlock = {},
            onResolveStack = {},
            onPassTurn = {},
            onVictoryContinue = {},
            onUseRevive = {},
            onAcceptDefeat = {},
            onAbandonRun = {}
          )
        }
      }
    }
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("battle_board").assertIsDisplayed()
    composeTestRule.onNodeWithTag("enemy_hand_count").assertIsDisplayed()
    composeTestRule.onNodeWithTag("end_round_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("hand_card_card_anivia").assertIsDisplayed()
    composeTestRule.onNodeWithTag("hand_card_card_harsh_winds").assertIsDisplayed()
    composeTestRule.onNodeWithTag("board_unit_egg_1").assertIsDisplayed()
  }

  @Test
  fun `homeHubScreen renders champion roster and command card with LoR backdrop`() {
    val aniviaLoadout = com.example.model.ChampionLoadout(
      championId = "champ_anivia",
      isUnlocked = true,
      starLevel = 2,
      championLevel = 5
    )
    val uiState = com.example.viewmodel.GameUiState(
      selectedChampionId = "champ_anivia",
      championLoadouts = mapOf("champ_anivia" to aniviaLoadout)
    )

    composeTestRule.setContent {
      PathOfChampionsTheme {
        Box(modifier = Modifier.size(width = 850.dp, height = 390.dp)) {
          com.example.ui.screens.HomeHubScreen(
            uiState = uiState,
            onSelectChampion = {},
            onUnlockChampion = {},
            onOpenChampionDetail = {},
            onOpenAdventures = {},
            onResumeRun = {},
            onAbandonRun = {},
            onGrantUnlimitedTestResources = {}
          )
        }
      }
    }
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("roster_champ_champ_anivia").assertIsDisplayed()
    composeTestRule.onNodeWithTag("embark_adventure_button").assertIsDisplayed()
  }
}
