package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SportsKabaddi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.CombatPhase
import com.example.engine.CombatState
import com.example.engine.PriorityHolder
import com.example.engine.SpellStackEntry
import com.example.model.UnitOnBoard
import com.example.ui.theme.ArcaneCyan
import com.example.ui.theme.CrimsonNexus
import com.example.ui.theme.DarkButtonInk
import com.example.ui.theme.EmeraldHeal
import com.example.ui.theme.HextechGold
import com.example.ui.theme.HextechGoldBright
import com.example.ui.theme.LegendaryAmber
import com.example.ui.theme.ParchmentWhite

enum class BattleBoardDropHighlight {
    NONE,
    VALID,
    INVALID
}

/**
 * Seamless LoR Path of Champions three-tier landscape battlefield:
 * 1. Top: AI Bench anchored along top carved stone rim (up to 6 units, no empty slot boxes)
 * 2. Center: Frosted ice arena over the golden centerline for active combat lanes & floating circular spell stack
 * 3. Bottom: Player Bench anchored along bottom carved stone rim (up to 6 units)
 */
@Composable
fun BattleBoard(
    combat: CombatState,
    modifier: Modifier = Modifier,
    playerBenchHighlight: BattleBoardDropHighlight = BattleBoardDropHighlight.NONE,
    battlefieldHighlight: BattleBoardDropHighlight = BattleBoardDropHighlight.NONE,
    draggingBenchUnitId: String? = null,
    draggingCombatUnitId: String? = null,
    isDraggingBenchBlocker: Boolean = false,
    draggedBenchUnit: UnitOnBoard? = null,
    tileWidthOverride: Dp? = null,
    tileHeightOverride: Dp? = null,
    unitGapOverride: Dp? = null,
    statFontSizeOverride: TextUnit? = null,
    descFontSizeOverride: TextUnit? = null,
    onPlayerBenchBoundsChanged: (Rect) -> Unit = {},
    onSharedBattlefieldBoundsChanged: (Rect) -> Unit = {},
    onEnemyAttackerBoundsChanged: (String, Rect) -> Unit = { _, _ -> },
    onPlayerUnitClick: (UnitOnBoard) -> Unit = {},
    onEnemyUnitClick: (UnitOnBoard) -> Unit = {},
    onUnitLongClick: (UnitOnBoard) -> Unit = {},
    onBenchUnitDragStart: (UnitOnBoard, Offset) -> Unit = { _, _ -> },
    onBenchUnitDrag: (UnitOnBoard, Offset) -> Unit = { _, _ -> },
    onBenchUnitDragEnd: (UnitOnBoard, Offset) -> Unit = { _, _ -> },
    onCombatUnitDragStart: (UnitOnBoard, Offset) -> Unit = { _, _ -> },
    onCombatUnitDrag: (UnitOnBoard, Offset) -> Unit = { _, _ -> },
    onCombatUnitDragEnd: (UnitOnBoard, Offset) -> Unit = { _, _ -> },
    onResolveStack: () -> Unit = {}
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .testTag("battle_board")
    ) {
        val boardWidth = maxWidth
        val isLandscape = boardWidth >= maxHeight
        val zoneSpacing = (maxHeight * 0.012f).coerceIn(2.dp, 5.dp)
        val usableHeight = (maxHeight - zoneSpacing * 2).coerceAtLeast(156.dp)

        val benchWeight = if (isLandscape) 0.27f else 0.27f
        val fieldWeight = if (isLandscape) 0.46f else 0.46f
        val benchZoneHeight = usableHeight * benchWeight
        val fieldZoneHeight = usableHeight * fieldWeight

        val unitGap = unitGapOverride ?: (boardWidth * 0.014f).coerceIn(5.dp, 14.dp)
        val maxSlotWidth = ((boardWidth - 16.dp - unitGap * 5) / 6f).coerceAtLeast(48.dp)
        val computedTileHeight = (benchZoneHeight * 0.88f).coerceAtLeast(48.dp)
        val computedTileWidth = minOf(maxSlotWidth, computedTileHeight * 0.80f).coerceAtLeast(48.dp)

        val tileWidth = tileWidthOverride ?: computedTileWidth
        val tileHeight = tileHeightOverride ?: computedTileHeight
        val statFontSize = statFontSizeOverride ?: (usableHeight.value * 0.036f).coerceIn(11f, 17f).sp
        val descFontSize = descFontSizeOverride ?: (usableHeight.value * 0.025f).coerceIn(9.5f, 13f).sp

        // Partition units so no unit renders simultaneously on Bench and Shared Battlefield
        val enemyCombatIds = if (combat.phase == CombatPhase.COMBAT_DECLARE_BLOCK_PHASE && !combat.playerInitiatedCombat) {
            combat.attackingUnitIds.toSet()
        } else {
            emptySet()
        }
        val enemyBenchUnits = combat.enemyBoard.filterNot { enemyCombatIds.contains(it.instanceId) }

        val playerAttackingIds = if (combat.phase == CombatPhase.COMBAT_DECLARE_ATTACK_PHASE ||
            (combat.phase == CombatPhase.COMBAT_DECLARE_BLOCK_PHASE && combat.playerInitiatedCombat)
        ) {
            combat.attackingUnitIds.toSet()
        } else {
            emptySet()
        }
        val playerBlockingIds = if (combat.phase == CombatPhase.COMBAT_DECLARE_BLOCK_PHASE && !combat.playerInitiatedCombat) {
            combat.blockPairings.values.toSet()
        } else {
            emptySet()
        }
        val playerBenchUnits = combat.playerBoard.filterNot {
            playerAttackingIds.contains(it.instanceId) || playerBlockingIds.contains(it.instanceId)
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(zoneSpacing)
        ) {
            // 1. TOP RIM: Seamless AI Bench (up to 6 units, auto-centered)
            SeamlessBenchZone(
                units = enemyBenchUnits,
                isEnemy = true,
                combat = combat,
                availableWidth = boardWidth,
                tileWidth = tileWidth,
                tileHeight = tileHeight,
                unitGap = unitGap,
                statFontSize = statFontSize,
                dropHighlight = BattleBoardDropHighlight.NONE,
                draggingUnitId = null,
                zoneTestTag = "enemy_bench_zone",
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(benchWeight),
                onUnitClick = onEnemyUnitClick,
                onUnitLongClick = onUnitLongClick,
                onUnitDragStart = null,
                onUnitDrag = null,
                onUnitDragEnd = null
            )

            // 2. CENTER ARENA: Open Frosted Ice Battlefield + Floating Center-Left Circular Spell Stack
            SeamlessIceBattlefieldZone(
                combat = combat,
                availableWidth = boardWidth,
                zoneHeight = fieldZoneHeight,
                tileWidth = tileWidth,
                tileHeight = tileHeight,
                unitGap = unitGap,
                statFontSize = statFontSize,
                descFontSize = descFontSize,
                dropHighlight = battlefieldHighlight,
                draggingUnitId = draggingCombatUnitId,
                isDraggingBenchBlocker = isDraggingBenchBlocker,
                draggedBenchUnit = draggedBenchUnit,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(fieldWeight),
                onBoundsChanged = onSharedBattlefieldBoundsChanged,
                onEnemyAttackerBoundsChanged = onEnemyAttackerBoundsChanged,
                onPlayerUnitClick = onPlayerUnitClick,
                onEnemyUnitClick = onEnemyUnitClick,
                onUnitLongClick = onUnitLongClick,
                onCombatUnitDragStart = onCombatUnitDragStart,
                onCombatUnitDrag = onCombatUnitDrag,
                onCombatUnitDragEnd = onCombatUnitDragEnd,
                onResolveStack = onResolveStack
            )

            // 3. BOTTOM RIM: Seamless Player Bench (up to 6 units, auto-centered)
            SeamlessBenchZone(
                units = playerBenchUnits,
                isEnemy = false,
                combat = combat,
                availableWidth = boardWidth,
                tileWidth = tileWidth,
                tileHeight = tileHeight,
                unitGap = unitGap,
                statFontSize = statFontSize,
                dropHighlight = playerBenchHighlight,
                draggingUnitId = draggingBenchUnitId,
                zoneTestTag = "player_bench_zone",
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(benchWeight)
                    .onGloballyPositioned { onPlayerBenchBoundsChanged(it.boundsInRoot()) },
                onUnitClick = onPlayerUnitClick,
                onUnitLongClick = onUnitLongClick,
                onUnitDragStart = onBenchUnitDragStart,
                onUnitDrag = onBenchUnitDrag,
                onUnitDragEnd = onBenchUnitDragEnd
            )
        }
    }
}

@Composable
private fun SeamlessBenchZone(
    units: List<UnitOnBoard>,
    isEnemy: Boolean,
    combat: CombatState,
    availableWidth: Dp,
    tileWidth: Dp,
    tileHeight: Dp,
    unitGap: Dp,
    statFontSize: TextUnit,
    dropHighlight: BattleBoardDropHighlight,
    draggingUnitId: String?,
    zoneTestTag: String,
    modifier: Modifier = Modifier,
    onUnitClick: (UnitOnBoard) -> Unit,
    onUnitLongClick: (UnitOnBoard) -> Unit,
    onUnitDragStart: ((UnitOnBoard, Offset) -> Unit)?,
    onUnitDrag: ((UnitOnBoard, Offset) -> Unit)?,
    onUnitDragEnd: ((UnitOnBoard, Offset) -> Unit)?
) {
    val dropModifier = when (dropHighlight) {
        BattleBoardDropHighlight.VALID -> Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.verticalGradient(
                    listOf(ArcaneCyan.copy(alpha = 0.14f), ArcaneCyan.copy(alpha = 0.05f))
                )
            )
            .border(1.6.dp, ArcaneCyan.copy(alpha = 0.72f), RoundedCornerShape(14.dp))
        BattleBoardDropHighlight.INVALID -> Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(CrimsonNexus.copy(alpha = 0.12f))
            .border(1.4.dp, CrimsonNexus.copy(alpha = 0.65f), RoundedCornerShape(14.dp))
        BattleBoardDropHighlight.NONE -> Modifier
    }

    Box(
        modifier = modifier
            .testTag(zoneTestTag)
            .then(dropModifier),
        contentAlignment = if (isEnemy) Alignment.TopCenter else Alignment.BottomCenter
    ) {
        if (units.isNotEmpty()) {
            val isBlockCandidatePhase = !isEnemy &&
                combat.priorityHolder == PriorityHolder.PLAYER &&
                combat.phase == CombatPhase.COMBAT_DECLARE_BLOCK_PHASE &&
                !combat.playerInitiatedCombat &&
                combat.selectedEnemyAttackerForBlock != null
            val targetEnemy = if (isBlockCandidatePhase) {
                combat.enemyBoard.find { it.instanceId == combat.selectedEnemyAttackerForBlock }
            } else {
                null
            }

            val visibleUnits = units.take(6)
            val totalRowWidth = tileWidth * visibleUnits.size + unitGap * (visibleUnits.size - 1).coerceAtLeast(0)
            val needsScroll = totalRowWidth > (availableWidth - 8.dp)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    )
                    .then(if (needsScroll) Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 4.dp) else Modifier),
                horizontalArrangement = Arrangement.spacedBy(unitGap, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                visibleUnits.forEach { unit ->
                    val canDragUnit = !isEnemy &&
                        onUnitDragStart != null &&
                        combat.priorityHolder == PriorityHolder.PLAYER &&
                        ((combat.phase == CombatPhase.MAIN_ACTION_PHASE && combat.playerHasAttackToken && unit.canAttack) ||
                            (combat.phase == CombatPhase.COMBAT_DECLARE_ATTACK_PHASE && unit.canAttack) ||
                            (combat.phase == CombatPhase.COMBAT_DECLARE_BLOCK_PHASE && !combat.playerInitiatedCombat && unit.canBlock))

                    var itemOrigin by remember(unit.instanceId) { mutableStateOf(Offset.Zero) }

                    Box(
                        modifier = Modifier
                            .onGloballyPositioned { itemOrigin = it.positionInRoot() }
                            .then(
                                if (canDragUnit) {
                                    Modifier.pointerInput(unit.instanceId, combat.phase, combat.priorityHolder) {
                                        var latestRootPos = Offset.Zero
                                        detectDragGestures(
                                            onDragStart = { localOffset ->
                                                latestRootPos = itemOrigin + localOffset
                                                onUnitDragStart.invoke(unit, latestRootPos)
                                            },
                                            onDrag = { change, _ ->
                                                change.consume()
                                                latestRootPos = itemOrigin + change.position
                                                onUnitDrag?.invoke(unit, latestRootPos)
                                            },
                                            onDragEnd = { onUnitDragEnd?.invoke(unit, latestRootPos) },
                                            onDragCancel = { onUnitDragEnd?.invoke(unit, Offset(-9999f, -9999f)) }
                                        )
                                    }
                                } else {
                                    Modifier
                                }
                            )
                    ) {
                        UnitToken(
                            unit = unit,
                            isAttacking = combat.attackingUnitIds.contains(unit.instanceId),
                            isSelected = isEnemy && combat.selectedEnemyAttackerForBlock == unit.instanceId,
                            isValidBlockCandidate = targetEnemy != null && unit.canBlockAttacker(targetEnemy),
                            isDragging = draggingUnitId == unit.instanceId,
                            tileWidth = tileWidth,
                            tileHeight = tileHeight,
                            statFontSize = statFontSize,
                            testTag = "board_unit_${unit.instanceId}",
                            onClick = { onUnitClick(unit) },
                            onLongClick = { onUnitLongClick(unit) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SeamlessIceBattlefieldZone(
    combat: CombatState,
    availableWidth: Dp,
    zoneHeight: Dp,
    tileWidth: Dp,
    tileHeight: Dp,
    unitGap: Dp,
    statFontSize: TextUnit,
    descFontSize: TextUnit,
    dropHighlight: BattleBoardDropHighlight,
    draggingUnitId: String?,
    isDraggingBenchBlocker: Boolean,
    draggedBenchUnit: UnitOnBoard?,
    modifier: Modifier = Modifier,
    onBoundsChanged: (Rect) -> Unit,
    onEnemyAttackerBoundsChanged: (String, Rect) -> Unit,
    onPlayerUnitClick: (UnitOnBoard) -> Unit,
    onEnemyUnitClick: (UnitOnBoard) -> Unit,
    onUnitLongClick: (UnitOnBoard) -> Unit,
    onCombatUnitDragStart: (UnitOnBoard, Offset) -> Unit,
    onCombatUnitDrag: (UnitOnBoard, Offset) -> Unit,
    onCombatUnitDragEnd: (UnitOnBoard, Offset) -> Unit,
    onResolveStack: () -> Unit
) {
    val isPlayerPriority = combat.priorityHolder == PriorityHolder.PLAYER
    val hasActiveCombatUnits = combat.attackingUnitIds.isNotEmpty() &&
        (combat.phase == CombatPhase.COMBAT_DECLARE_ATTACK_PHASE || combat.phase == CombatPhase.COMBAT_DECLARE_BLOCK_PHASE)

    val dropModifier = when (dropHighlight) {
        BattleBoardDropHighlight.VALID -> Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    listOf(ArcaneCyan.copy(alpha = 0.14f), ArcaneCyan.copy(alpha = 0.06f))
                )
            )
            .border(1.6.dp, ArcaneCyan.copy(alpha = 0.72f), RoundedCornerShape(16.dp))
        BattleBoardDropHighlight.INVALID -> Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(CrimsonNexus.copy(alpha = 0.12f))
            .border(1.4.dp, CrimsonNexus.copy(alpha = 0.65f), RoundedCornerShape(16.dp))
        BattleBoardDropHighlight.NONE -> Modifier
    }

    Box(
        modifier = modifier
            .testTag("shared_battlefield_zone")
            .onGloballyPositioned { onBoundsChanged(it.boundsInRoot()) }
            .then(dropModifier),
        contentAlignment = Alignment.Center
    ) {
        // Active Combat Lanes across the horizontal golden centerline
        if (hasActiveCombatUnits) {
            val pairTileHeight = minOf(tileHeight, (zoneHeight * 0.42f).coerceAtLeast(48.dp))
            val pairTileWidth = minOf(tileWidth, pairTileHeight * 0.88f).coerceAtLeast(48.dp)
            val connectorHeight = (zoneHeight - pairTileHeight * 2 - 4.dp).coerceIn(4.dp, 8.dp)

            if (combat.playerInitiatedCombat) {
                val attackers = combat.attackingUnitIds.mapNotNull { id ->
                    combat.playerBoard.find { it.instanceId == id }
                }.take(6)
                val totalRowWidth = pairTileWidth * attackers.size + unitGap * (attackers.size - 1).coerceAtLeast(0)
                val needsScroll = totalRowWidth > (availableWidth - 8.dp)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize(spring(stiffness = Spring.StiffnessMediumLow))
                        .then(if (needsScroll) Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 4.dp) else Modifier),
                    horizontalArrangement = Arrangement.spacedBy(unitGap, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    attackers.forEach { atk ->
                        var atkOrigin by remember(atk.instanceId) { mutableStateOf(Offset.Zero) }
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(1.dp)
                        ) {
                            // Opposing target strike indicator above centerline
                            Box(
                                modifier = Modifier
                                    .size(width = pairTileWidth, height = (pairTileHeight * 0.64f).coerceAtLeast(28.dp))
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(LegendaryAmber.copy(alpha = 0.10f))
                                    .border(1.dp, LegendaryAmber.copy(alpha = 0.40f), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SportsKabaddi,
                                    contentDescription = "Attacking Lane",
                                    tint = LegendaryAmber,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Canvas(modifier = Modifier.size(width = 8.dp, height = connectorHeight)) {
                                if (size.width <= 1f || size.height <= 1f) return@Canvas
                                drawLine(
                                    color = LegendaryAmber,
                                    start = Offset(size.width / 2f, size.height),
                                    end = Offset(size.width / 2f, 0f),
                                    strokeWidth = 3.5f,
                                    cap = StrokeCap.Round
                                )
                            }

                            // Bottom Player Attacker Token
                            Box(
                                modifier = Modifier
                                    .onGloballyPositioned { atkOrigin = it.positionInRoot() }
                                    .then(
                                        if (combat.phase == CombatPhase.COMBAT_DECLARE_ATTACK_PHASE && isPlayerPriority) {
                                            Modifier.pointerInput(atk.instanceId, combat.phase) {
                                                var latestRoot = Offset.Zero
                                                detectDragGestures(
                                                    onDragStart = { local ->
                                                        latestRoot = atkOrigin + local
                                                        onCombatUnitDragStart(atk, latestRoot)
                                                    },
                                                    onDrag = { change, _ ->
                                                        change.consume()
                                                        latestRoot = atkOrigin + change.position
                                                        onCombatUnitDrag(atk, latestRoot)
                                                    },
                                                    onDragEnd = { onCombatUnitDragEnd(atk, latestRoot) },
                                                    onDragCancel = { onCombatUnitDragEnd(atk, Offset(-9999f, -9999f)) }
                                                )
                                            }
                                        } else {
                                            Modifier
                                        }
                                    )
                            ) {
                                UnitToken(
                                    unit = atk,
                                    isAttacking = true,
                                    isSelected = true,
                                    isDragging = draggingUnitId == atk.instanceId,
                                    tileWidth = pairTileWidth,
                                    tileHeight = pairTileHeight,
                                    statFontSize = (statFontSize.value * 0.88f).coerceAtLeast(10f).sp,
                                    testTag = "board_unit_${atk.instanceId}",
                                    onClick = { onPlayerUnitClick(atk) },
                                    onLongClick = { onUnitLongClick(atk) }
                                )
                            }
                        }
                    }
                }
            } else {
                val enemyAttackers = combat.attackingUnitIds.mapNotNull { id ->
                    combat.enemyBoard.find { it.instanceId == id }
                }.take(6)
                val totalRowWidth = pairTileWidth * enemyAttackers.size + unitGap * (enemyAttackers.size - 1).coerceAtLeast(0)
                val needsScroll = totalRowWidth > (availableWidth - 8.dp)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize(spring(stiffness = Spring.StiffnessMediumLow))
                        .then(if (needsScroll) Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 4.dp) else Modifier),
                    horizontalArrangement = Arrangement.spacedBy(unitGap, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    enemyAttackers.forEach { enemyAtk ->
                        val blockerId = combat.blockPairings[enemyAtk.instanceId]
                        val blocker = blockerId?.let { id -> combat.playerBoard.find { it.instanceId == id } }
                        val isTargeted = combat.selectedEnemyAttackerForBlock == enemyAtk.instanceId
                        val canDraggedBlockThis = isDraggingBenchBlocker &&
                            draggedBenchUnit?.canBlockAttacker(enemyAtk) == true

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(1.dp)
                        ) {
                            // Top Enemy Attacker Token
                            Box(
                                modifier = Modifier.onGloballyPositioned {
                                    onEnemyAttackerBoundsChanged(enemyAtk.instanceId, it.boundsInRoot())
                                }
                            ) {
                                UnitToken(
                                    unit = enemyAtk,
                                    isAttacking = true,
                                    isSelected = isTargeted || canDraggedBlockThis,
                                    tileWidth = pairTileWidth,
                                    tileHeight = pairTileHeight,
                                    statFontSize = (statFontSize.value * 0.85f).coerceAtLeast(10f).sp,
                                    testTag = "board_unit_${enemyAtk.instanceId}",
                                    onClick = { onEnemyUnitClick(enemyAtk) },
                                    onLongClick = { onUnitLongClick(enemyAtk) }
                                )
                            }

                            // Clash Connector
                            Canvas(modifier = Modifier.size(width = 8.dp, height = connectorHeight)) {
                                if (size.width <= 1f || size.height <= 1f) return@Canvas
                                drawLine(
                                    color = if (blocker != null) EmeraldHeal else CrimsonNexus.copy(alpha = 0.8f),
                                    start = Offset(size.width / 2f, 0f),
                                    end = Offset(size.width / 2f, size.height),
                                    strokeWidth = if (blocker != null) 4f else 2.5f,
                                    cap = StrokeCap.Round
                                )
                            }

                            // Bottom Player Blocker or Blocker Target Slot
                            if (blocker != null) {
                                var blkOrigin by remember(blocker.instanceId) { mutableStateOf(Offset.Zero) }
                                Box(
                                    modifier = Modifier
                                        .onGloballyPositioned { blkOrigin = it.positionInRoot() }
                                        .then(
                                            if (isPlayerPriority) {
                                                Modifier.pointerInput(blocker.instanceId, combat.phase) {
                                                    var latestRoot = Offset.Zero
                                                    detectDragGestures(
                                                        onDragStart = { local ->
                                                            latestRoot = blkOrigin + local
                                                            onCombatUnitDragStart(blocker, latestRoot)
                                                        },
                                                        onDrag = { change, _ ->
                                                            change.consume()
                                                            latestRoot = blkOrigin + change.position
                                                            onCombatUnitDrag(blocker, latestRoot)
                                                        },
                                                        onDragEnd = { onCombatUnitDragEnd(blocker, latestRoot) },
                                                        onDragCancel = { onCombatUnitDragEnd(blocker, Offset(-9999f, -9999f)) }
                                                    )
                                                }
                                            } else {
                                                Modifier
                                            }
                                        )
                                ) {
                                    UnitToken(
                                        unit = blocker,
                                        isBlockingLabel = "BLK",
                                        isDragging = draggingUnitId == blocker.instanceId,
                                        tileWidth = pairTileWidth,
                                        tileHeight = pairTileHeight,
                                        statFontSize = (statFontSize.value * 0.85f).coerceAtLeast(10f).sp,
                                        testTag = "board_unit_${blocker.instanceId}",
                                        onClick = { onPlayerUnitClick(blocker) },
                                        onLongClick = { onUnitLongClick(blocker) }
                                    )
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(width = pairTileWidth, height = pairTileHeight)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (canDraggedBlockThis) ArcaneCyan.copy(alpha = 0.22f) else Color(0x361C3454)
                                        )
                                        .border(
                                            if (isTargeted || canDraggedBlockThis) 1.8.dp else 1.2.dp,
                                            if (isTargeted || canDraggedBlockThis) ArcaneCyan else CrimsonNexus.copy(alpha = 0.65f),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable { onEnemyUnitClick(enemyAtk) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Shield,
                                        contentDescription = "Assign Blocker",
                                        tint = if (isTargeted || canDraggedBlockThis) ArcaneCyan else ParchmentWhite.copy(alpha = 0.65f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Floating Circular Spell/Skill Stack Medallions (Center-Left Lane, Auto-Hidden When Empty)
        AnimatedVisibility(
            visible = combat.spellStack.isNotEmpty(),
            enter = fadeIn() + scaleIn(initialScale = 0.82f),
            exit = fadeOut() + scaleOut(targetScale = 0.82f),
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 6.dp)
        ) {
            FloatingSpellStackLane(
                stack = combat.spellStack,
                isPlayerPriority = isPlayerPriority,
                medallionSize = (zoneHeight * 0.34f).coerceIn(38.dp, 54.dp),
                descFontSize = descFontSize,
                onResolveStack = onResolveStack
            )
        }
    }
}

@Composable
private fun FloatingSpellStackLane(
    stack: List<SpellStackEntry>,
    isPlayerPriority: Boolean,
    medallionSize: Dp,
    descFontSize: TextUnit,
    onResolveStack: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        stack.take(5).forEach { item ->
            val ringColor = if (item.sourceIsPlayer) ArcaneCyan else CrimsonNexus
            val portraitRes = cardPortraitRes(item.card.id)
            Box(
                modifier = Modifier
                    .size(medallionSize)
                    .clip(CircleShape)
                    .background(Color(0xEE223656))
                    .border(2.2.dp, ringColor, CircleShape)
                    .border(3.5.dp, HextechGold.copy(alpha = 0.65f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (portraitRes != null) {
                    Image(
                        painter = painterResource(id = portraitRes),
                        contentDescription = item.card.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(2.dp)
                            .clip(CircleShape)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.AutoFixHigh,
                        contentDescription = item.card.name,
                        tint = if (item.sourceIsPlayer) ArcaneCyan else CrimsonNexus,
                        modifier = Modifier.size(medallionSize * 0.48f)
                    )
                }

                // Speed / Owner mini badge
                Surface(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xE81B2C47),
                    border = BorderStroke(0.8.dp, HextechGoldBright)
                ) {
                    Text(
                        text = item.card.spellSpeed.displayName.take(4).uppercase(),
                        fontSize = 7.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = ParchmentWhite,
                        modifier = Modifier.padding(horizontal = 3.dp, vertical = 0.5.dp)
                    )
                }
            }
        }

        if (isPlayerPriority) {
            Button(
                onClick = onResolveStack,
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                modifier = Modifier
                    .height(48.dp)
                    .testTag("resolve_stack_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = HextechGoldBright,
                    contentColor = DarkButtonInk
                )
            ) {
                Text(
                    text = "Resolve",
                    fontSize = descFontSize,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
