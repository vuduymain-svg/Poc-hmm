package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.SportsKabaddi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.AdventureCatalog
import com.example.model.MapNode
import com.example.model.NodeType
import com.example.model.RunState
import com.example.ui.components.LorScreenBackdrop
import com.example.ui.components.RunDeckDialog
import com.example.ui.components.RunResourceHud
import com.example.ui.theme.ArcaneCyan
import com.example.ui.theme.CrimsonNexus
import com.example.ui.theme.EmeraldHeal
import com.example.ui.theme.EpicViolet
import com.example.ui.theme.HextechGold
import com.example.ui.theme.HextechGoldBright
import com.example.ui.theme.LegendaryAmber
import com.example.ui.theme.MutedSilver
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianSurfaceElevated
import com.example.ui.theme.ParchmentWhite
import com.example.ui.theme.SlateBorder

@Composable
fun AdventureMapScreen(
    run: RunState,
    isNodeAvailable: (RunState, MapNode) -> Boolean,
    onSelectNode: (String) -> Unit,
    onAbandonRun: () -> Unit,
    onBackHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBackHome)

    var showDeck by remember { mutableStateOf(false) }
    val adventure = AdventureCatalog.getAdventure(run.adventureId)
    val maxRow = adventure.mapNodes.maxOfOrNull { it.row } ?: 6
    val rowHeightDp = 96.dp
    val totalHeight = rowHeightDp * (maxRow + 1) + 40.dp

    val mapCanvasContent: @Composable (Modifier) -> Unit = { mapModifier ->
        Box(
            modifier = mapModifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(totalHeight)
            ) {
                // Ley-line paths connecting parent -> nextNodeIds
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val rowSpacing = size.height / (maxRow + 1).toFloat()
                    fun nodeCenter(node: MapNode): Offset {
                        val x = when (node.column) {
                            0 -> size.width * 0.24f
                            2 -> size.width * 0.76f
                            else -> size.width * 0.50f
                        }
                        val y = rowSpacing * node.row + rowSpacing * 0.5f
                        return Offset(x, y)
                    }

                    adventure.mapNodes.forEach { fromNode ->
                        val start = nodeCenter(fromNode)
                        fromNode.nextNodeIds.forEach { targetId ->
                            val toNode = adventure.mapNodes.find { it.id == targetId }
                            if (toNode != null) {
                                val end = nodeCenter(toNode)
                                val isPathActive = run.currentNodeId == fromNode.id &&
                                    isNodeAvailable(run, toNode)
                                val isPathCompleted = run.completedNodeIds.contains(fromNode.id) &&
                                    run.completedNodeIds.contains(toNode.id)
                                val lineColor = when {
                                    isPathActive -> HextechGoldBright
                                    isPathCompleted -> EmeraldHeal.copy(alpha = 0.75f)
                                    else -> SlateBorder
                                }

                                val path = Path().apply {
                                    moveTo(start.x, start.y)
                                    val midY = (start.y + end.y) / 2f
                                    cubicTo(start.x, midY, end.x, midY, end.x, end.y)
                                }
                                drawPath(
                                    path = path,
                                    color = lineColor,
                                    style = Stroke(
                                        width = if (isPathActive) 6f else 4f,
                                        cap = StrokeCap.Round
                                    )
                                )
                            }
                        }
                    }
                }

                // Render rows bottom-up or top-down (row 0 at top -> row 6 Final Boss at bottom)
                Column(modifier = Modifier.fillMaxSize()) {
                    for (rowIndex in 0..maxRow) {
                        val nodesInRow = adventure.mapNodes.filter { it.row == rowIndex }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(rowHeightDp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val left = nodesInRow.find { it.column == 0 }
                            val mid = nodesInRow.find { it.column == 1 }
                            val right = nodesInRow.find { it.column == 2 }

                            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                if (left != null) {
                                    MapNodeChip(
                                        node = left,
                                        completed = run.completedNodeIds.contains(left.id),
                                        available = isNodeAvailable(run, left),
                                        isCurrent = run.currentNodeId == left.id,
                                        onClick = { onSelectNode(left.id) }
                                    )
                                }
                            }
                            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                if (mid != null) {
                                    MapNodeChip(
                                        node = mid,
                                        completed = run.completedNodeIds.contains(mid.id),
                                        available = isNodeAvailable(run, mid),
                                        isCurrent = run.currentNodeId == mid.id,
                                        onClick = { onSelectNode(mid.id) }
                                    )
                                }
                            }
                            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                if (right != null) {
                                    MapNodeChip(
                                        node = right,
                                        completed = run.completedNodeIds.contains(right.id),
                                        available = isNodeAvailable(run, right),
                                        isCurrent = run.currentNodeId == right.id,
                                        onClick = { onSelectNode(right.id) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    LorScreenBackdrop(modifier = modifier) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
        if (maxWidth >= 600.dp) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 1080.dp)
            ) {
                Column(
                    modifier = Modifier
                        .width(280.dp)
                        .fillMaxHeight()
                        .background(ObsidianSurface)
                        .verticalScroll(rememberScrollState())
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onBackHome,
                            modifier = Modifier.testTag("map_back_home_button")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Save & Home", tint = ParchmentWhite)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = adventure.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = HextechGoldBright,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = adventure.difficultyLabel,
                                style = MaterialTheme.typography.bodySmall,
                                color = MutedSilver
                            )
                        }
                    }

                    RunResourceHud(
                        run = run,
                        onOpenDeck = { showDeck = true },
                        onAbandon = onAbandonRun
                    )

                    Text(
                        text = "Select the next glowing node along the ley-line path.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedSilver
                    )
                }

                mapCanvasContent(
                    Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 720.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ObsidianSurface)
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBackHome,
                        modifier = Modifier.testTag("map_back_home_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Save & Home", tint = ParchmentWhite)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = adventure.name,
                            style = MaterialTheme.typography.titleLarge,
                            color = HextechGoldBright
                        )
                        Text(
                            text = "${adventure.difficultyLabel} • Select next glowing path node",
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedSilver
                        )
                    }
                }

                RunResourceHud(
                    run = run,
                    onOpenDeck = { showDeck = true },
                    onAbandon = onAbandonRun,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )

                mapCanvasContent(Modifier.fillMaxSize())
            }
        }
        }
    }

    if (showDeck) {
        RunDeckDialog(runDeck = run.runDeck, onDismiss = { showDeck = false })
    }
}

@Composable
private fun MapNodeChip(
    node: MapNode,
    completed: Boolean,
    available: Boolean,
    isCurrent: Boolean,
    onClick: () -> Unit
) {
    val accent = nodeAccent(node.type)
    val borderColor = when {
        available -> HextechGoldBright
        completed -> EmeraldHeal
        else -> SlateBorder
    }

    Surface(
        modifier = Modifier
            .width(126.dp)
            .minimumInteractiveComponentSize()
            .testTag("map_node_${node.id}")
            .clickable(enabled = available, onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = when {
            available -> ObsidianSurfaceElevated
            completed -> ObsidianCard
            else -> ObsidianBg
        },
        border = BorderStroke(if (available) 2.5.dp else 1.2.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 7.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = if (completed) Icons.Default.CheckCircle else nodeIcon(node.type),
                    contentDescription = node.type.displayName,
                    tint = if (completed) EmeraldHeal else accent,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = if (isCurrent) "HERE" else node.type.shortCode,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (available) HextechGoldBright else accent
                )
            }
            Text(
                text = node.title,
                style = MaterialTheme.typography.labelMedium,
                color = if (available || completed) ParchmentWhite else MutedSilver,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (node.type == NodeType.COMBAT || node.type == NodeType.ELITE_COMBAT ||
                node.type == NodeType.MID_BOSS || node.type == NodeType.FINAL_BOSS
            ) {
                Text(
                    text = "${node.enemyMaxHp} HP • +${node.goldReward}g",
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedSilver,
                    maxLines = 1
                )
            } else {
                Text(
                    text = node.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedSilver,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

private fun nodeIcon(type: NodeType): ImageVector = when (type) {
    NodeType.START -> Icons.Default.Flag
    NodeType.COMBAT -> Icons.Default.SportsKabaddi
    NodeType.ELITE_COMBAT -> Icons.Default.LocalFireDepartment
    NodeType.MID_BOSS, NodeType.FINAL_BOSS -> Icons.Default.LocalFireDepartment
    NodeType.POWER_NODE -> Icons.Default.Bolt
    NodeType.HEALER_NODE -> Icons.Default.Favorite
    NodeType.SHOP_NODE -> Icons.Default.ShoppingBag
    NodeType.REINFORCEMENT_NODE -> Icons.Default.GroupAdd
    NodeType.ITEM_CHEST -> Icons.Default.Inventory2
    NodeType.NARRATIVE_EVENT -> Icons.Default.QuestionMark
}

private fun nodeAccent(type: NodeType): Color = when (type) {
    NodeType.START -> ArcaneCyan
    NodeType.COMBAT -> LegendaryAmber
    NodeType.ELITE_COMBAT, NodeType.MID_BOSS, NodeType.FINAL_BOSS -> CrimsonNexus
    NodeType.POWER_NODE -> EpicViolet
    NodeType.HEALER_NODE -> EmeraldHeal
    NodeType.SHOP_NODE -> HextechGoldBright
    NodeType.REINFORCEMENT_NODE -> ArcaneCyan
    NodeType.ITEM_CHEST -> EpicViolet
    NodeType.NARRATIVE_EVENT -> HextechGold
}
