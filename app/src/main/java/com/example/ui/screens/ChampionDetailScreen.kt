package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.model.CardCatalog
import com.example.ui.components.CardTile
import com.example.ui.components.ChampionAvatarBadge
import com.example.ui.components.LorScreenBackdrop
import com.example.ui.components.MetaCurrencyBar
import com.example.ui.components.RelicSlotCard
import com.example.ui.components.rarityColor
import com.example.ui.theme.ArcaneCyan
import com.example.ui.theme.DarkButtonInk
import com.example.ui.theme.EmeraldHeal
import com.example.ui.theme.EpicViolet
import com.example.ui.theme.HextechGold
import com.example.ui.theme.HextechGoldBright
import com.example.ui.theme.MutedSilver
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianSurfaceElevated
import com.example.ui.theme.ParchmentWhite
import com.example.ui.theme.SlateBorder
import com.example.viewmodel.GameUiState

@Composable
fun ChampionDetailScreen(
    championId: String,
    uiState: GameUiState,
    onUpgradeStar: (String, Int) -> Unit,
    onUnlockConstellation: (String, String) -> Unit,
    onEquipRelic: (String, Int, String?) -> Unit,
    onCosmicForge: (String) -> Unit,
    onMaxChampionForTesting: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)

    val champion = CardCatalog.getChampion(championId)
    val loadout = uiState.championLoadouts[championId] ?: return
    var selectedTab by remember { mutableIntStateOf(0) }
    var relicPickerSlot by remember { mutableStateOf<Int?>(null) }

    LorScreenBackdrop(modifier = modifier) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 960.dp)
        ) {
            // Top Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ObsidianSurface)
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("champ_detail_back_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = ParchmentWhite)
                }
                ChampionAvatarBadge(
                    championId = champion.id,
                    name = champion.name,
                    region = champion.region,
                    size = 42.dp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${champion.name} Mastery",
                        style = MaterialTheme.typography.headlineSmall,
                        color = HextechGoldBright
                    )
                    Text(
                        text = "Lv.${loadout.championLevel} • ${loadout.starLevel}★ • ${loadout.fragments} Champ Frags",
                        style = MaterialTheme.typography.bodySmall,
                        color = ArcaneCyan
                    )
                }
                OutlinedButton(
                    onClick = { onMaxChampionForTesting(championId) },
                    modifier = Modifier.testTag("max_champ_test_button"),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    border = BorderStroke(1.dp, EmeraldHeal)
                ) {
                    Text("Max Lv.30 • 6★", style = MaterialTheme.typography.labelMedium, color = EmeraldHeal)
                }
            }

            MetaCurrencyBar(
                profile = uiState.legendProfile,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
            )

            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = ObsidianSurface,
                contentColor = HextechGoldBright
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Stars & Const.") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Relics (${loadout.unlockedRelicSlots}/3)") }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Starter Deck") }
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (selectedTab) {
                    0 -> {
                        item {
                            val odds = loadout.rarityOdds
                            val pct = { v: Double -> String.format(java.util.Locale.US, "%.1f%%", v * 100.0) }
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                color = ObsidianSurface,
                                border = BorderStroke(1.dp, SlateBorder)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "Lv.${loadout.championLevel} Run Baseline: ${loadout.maxNexusHp} HP • Heal +${loadout.victoryHeal}/Win • ${loadout.startingGold}g • ${loadout.startingRerolls} Rerolls",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = ParchmentWhite
                                    )
                                    Text(
                                        text = "Reward/Shop Odds: Common ${pct(odds.common)} | Rare ${pct(odds.rare)} | Epic ${pct(odds.epic)} | Legendary ${pct(odds.legendary)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = ArcaneCyan
                                    )
                                }
                            }
                        }
                        item {
                            Text(
                                text = "Star Powers (Constellation)",
                                style = MaterialTheme.typography.titleMedium,
                                color = HextechGoldBright
                            )
                        }
                        items(champion.starPowers, key = { it.starLevel }) { spec ->
                            val unlocked = loadout.starLevel >= spec.starLevel
                            val nextStarSpec = champion.nextStarPower(loadout.starLevel)
                            val isNext = loadout.isUnlocked && nextStarSpec?.starLevel == spec.starLevel

                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                color = if (unlocked) ObsidianSurfaceElevated else ObsidianCard,
                                border = BorderStroke(
                                    1.dp,
                                    when {
                                        unlocked -> HextechGold
                                        isNext -> ArcaneCyan
                                        else -> SlateBorder
                                    }
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = if (unlocked) Icons.Default.CheckCircle else Icons.Default.Star,
                                        contentDescription = "${spec.starLevel} Star",
                                        tint = if (unlocked) EmeraldHeal else HextechGoldBright,
                                        modifier = Modifier.size(26.dp)
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${spec.starLevel}★ ${spec.title}",
                                            style = MaterialTheme.typography.titleSmall,
                                            color = if (unlocked) HextechGoldBright else ParchmentWhite
                                        )
                                        Text(
                                            text = spec.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MutedSilver
                                        )
                                        if (!unlocked) {
                                            val costText = buildString {
                                                append("Cost: ${spec.fragmentCost} Frags")
                                                if (spec.starCrystalCost > 0) append(" • ${spec.starCrystalCost} Star Crystals")
                                                if (spec.novaCrystalCost > 0) append(" • ${spec.novaCrystalCost} Nova Crystal")
                                            }
                                            Text(
                                                text = costText,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = ArcaneCyan
                                            )
                                        }
                                    }
                                    if (isNext) {
                                        Button(
                                            onClick = { onUpgradeStar(championId, spec.starLevel) },
                                            modifier = Modifier.testTag("upgrade_star_${spec.starLevel}"),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = ArcaneCyan,
                                                contentColor = DarkButtonInk
                                            )
                                        ) {
                                            Text("Ascend")
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Constellation Nodes",
                                style = MaterialTheme.typography.titleMedium,
                                color = ArcaneCyan
                            )
                        }
                        items(champion.constellationNodes, key = { it.id }) { node ->
                            val unlocked = loadout.unlockedConstellationNodes.contains(node.id)
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                color = ObsidianCard,
                                border = BorderStroke(1.dp, if (unlocked) EmeraldHeal else SlateBorder)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = node.title,
                                            style = MaterialTheme.typography.titleSmall,
                                            color = ParchmentWhite
                                        )
                                        Text(
                                            text = node.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MutedSilver
                                        )
                                        if (!unlocked) {
                                            Text(
                                                text = "Cost: ${node.fragmentCost} Frags • ${node.gemstoneCost} Gemstones",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = EmeraldHeal
                                            )
                                        }
                                    }
                                    if (unlocked) {
                                        Text("ACTIVE", style = MaterialTheme.typography.labelMedium, color = EmeraldHeal)
                                    } else {
                                        OutlinedButton(
                                            onClick = { onUnlockConstellation(championId, node.id) },
                                            modifier = Modifier.testTag("unlock_node_${node.id}")
                                        ) {
                                            Text("Unlock", color = ArcaneCyan)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    1 -> {
                        item {
                            Text(
                                text = "Equipped Relics (Tap slot to change)",
                                style = MaterialTheme.typography.titleMedium,
                                color = HextechGoldBright
                            )
                        }
                        items(3) { idx ->
                            RelicSlotCard(
                                slotIndex = idx,
                                relicId = loadout.equippedRelics.getOrNull(idx),
                                unlocked = idx < loadout.unlockedRelicSlots,
                                isCosmicForged = idx < loadout.cosmicForgedSlots,
                                onClick = { relicPickerSlot = idx }
                            )
                        }
                        item {
                            Spacer(modifier = Modifier.height(6.dp))
                            Button(
                                onClick = { onCosmicForge(championId) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("cosmic_forge_button"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = EpicViolet,
                                    contentColor = ParchmentWhite
                                )
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = "Cosmic Forge", modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Cosmic Forge Epic Slot (15 Gemstones)")
                            }
                        }
                    }

                    2 -> {
                        items(champion.baseDeck, key = { it.cardId }) { entry ->
                            val card = CardCatalog.getCard(entry.cardId)
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = "${entry.copies}x in Starter Deck",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = HextechGoldBright
                                )
                                CardTile(template = card)
                            }
                        }
                    }
                }
            }
        }
        }
    }

    relicPickerSlot?.let { slot ->
        AlertDialog(
            onDismissRequest = { relicPickerSlot = null },
            containerColor = ObsidianSurface,
            title = {
                Text("Equip Relic — Slot ${slot + 1}", color = HextechGoldBright)
            },
            text = {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 360.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        OutlinedButton(
                            onClick = {
                                onEquipRelic(championId, slot, null)
                                relicPickerSlot = null
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Unequip Slot", color = MutedSilver)
                        }
                    }
                    items(CardCatalog.relics, key = { it.id }) { relic ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .minimumInteractiveComponentSize()
                                .clickable {
                                    onEquipRelic(championId, slot, relic.id)
                                    relicPickerSlot = null
                                },
                            shape = RoundedCornerShape(10.dp),
                            color = ObsidianCard,
                            border = BorderStroke(1.dp, rarityColor(relic.rarity))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "${relic.name} (${relic.rarity.displayName})",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = rarityColor(relic.rarity)
                                )
                                Text(
                                    text = relic.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MutedSilver
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { relicPickerSlot = null }) {
                    Text("Cancel", color = HextechGoldBright)
                }
            }
        )
    }
}
