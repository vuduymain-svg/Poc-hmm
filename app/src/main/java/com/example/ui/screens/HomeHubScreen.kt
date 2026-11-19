package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.AdventureCatalog
import com.example.model.CardCatalog
import com.example.ui.components.ChampionAvatarBadge
import com.example.ui.components.KeywordGlossaryDialog
import com.example.ui.components.LorScreenBackdrop
import com.example.ui.components.MetaCurrencyBar
import com.example.ui.components.fantasyHeadingStyle
import com.example.ui.components.regionAccentColor
import com.example.ui.theme.ArcaneCyan
import com.example.ui.theme.CrimsonNexus
import com.example.ui.theme.DarkButtonInk
import com.example.ui.theme.EmeraldHeal
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
fun HomeHubScreen(
    uiState: GameUiState,
    onSelectChampion: (String) -> Unit,
    onUnlockChampion: (String) -> Unit,
    onOpenChampionDetail: (String) -> Unit,
    onOpenAdventures: () -> Unit,
    onResumeRun: () -> Unit,
    onAbandonRun: () -> Unit,
    onGrantUnlimitedTestResources: () -> Unit,
    onOpenStretchDisplay: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showGlossary by remember { mutableStateOf(false) }

    val selectedChamp = CardCatalog.getChampion(uiState.selectedChampionId)
    val selectedLoadout = uiState.championLoadouts[selectedChamp.id]

    val heroBannerBlock: @Composable (androidx.compose.ui.unit.Dp) -> Unit = { bannerHeight ->
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(bannerHeight)
                .clip(RoundedCornerShape(16.dp))
                .background(ObsidianSurface)
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_adventure_banner),
                contentDescription = "Runeterra Adventure Map Banner",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, ObsidianBg.copy(alpha = 0.72f))
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
            ) {
                Text(
                    text = "THE PATH OF CHAMPIONS",
                    style = MaterialTheme.typography.headlineSmall,
                    color = HextechGoldBright
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Legend Lv.${uiState.legendProfile.legendLevel}",
                        style = MaterialTheme.typography.labelLarge,
                        color = ArcaneCyan
                    )
                    LinearProgressIndicator(
                        progress = {
                            (uiState.legendProfile.legendPoints.toFloat() /
                                uiState.legendProfile.pointsToNextLevel.coerceAtLeast(1)).coerceIn(0f, 1f)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = HextechGoldBright,
                        trackColor = ObsidianSurfaceElevated
                    )
                    Text(
                        text = "${uiState.legendProfile.totalRunsWon} Wins",
                        style = MaterialTheme.typography.labelMedium,
                        color = MutedSilver
                    )
                }
            }
        }
    }

    val savedRunBlock: @Composable () -> Unit = {
        if (uiState.savedRun != null) {
            val savedChamp = CardCatalog.getChampion(uiState.savedRun.championId)
            val savedAdv = AdventureCatalog.getAdventure(uiState.savedRun.adventureId)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = ObsidianSurfaceElevated,
                border = BorderStroke(1.5.dp, ArcaneCyan)
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "ACTIVE RUN IN PROGRESS",
                            style = MaterialTheme.typography.labelSmall,
                            color = ArcaneCyan
                        )
                        Text(
                            text = "${savedChamp.name} • ${savedAdv.name}",
                            style = MaterialTheme.typography.titleMedium,
                            color = ParchmentWhite,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "HP ${uiState.savedRun.currentNexusHp}/${uiState.savedRun.maxNexusHp} • ${uiState.savedRun.gold}g • ${uiState.savedRun.battlesWon} Wins",
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedSilver
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(
                            onClick = onAbandonRun,
                            modifier = Modifier.testTag("abandon_saved_run_button")
                        ) {
                            Text("Drop", color = CrimsonNexus)
                        }
                        Button(
                            onClick = onResumeRun,
                            modifier = Modifier.testTag("resume_run_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ArcaneCyan,
                                contentColor = DarkButtonInk
                            )
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Resume", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Resume")
                        }
                    }
                }
            }
        }
    }

    val rosterBlock: @Composable () -> Unit = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Champion Roster",
                    style = MaterialTheme.typography.titleLarge,
                    color = ParchmentWhite
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onGrantUnlimitedTestResources,
                        modifier = Modifier.testTag("grant_unlimited_resources_button"),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        border = BorderStroke(1.dp, EmeraldHeal.copy(alpha = 0.7f))
                    ) {
                        Text("∞ Test Res", style = MaterialTheme.typography.labelMedium, color = EmeraldHeal)
                    }
                    TextButton(
                        onClick = onOpenStretchDisplay,
                        modifier = Modifier.testTag("open_stretch_button")
                    ) {
                        Icon(Icons.Default.SelfImprovement, contentDescription = "Stretch Break", tint = EmeraldHeal, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Stretch", color = EmeraldHeal)
                    }
                    TextButton(
                        onClick = { showGlossary = true },
                        modifier = Modifier.testTag("open_glossary_button")
                    ) {
                        Icon(Icons.Default.AutoStories, contentDescription = "Keywords Codex", tint = ArcaneCyan, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Codex", color = ArcaneCyan)
                    }
                }
            }

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(CardCatalog.champions, key = { it.id }) { champ ->
                    val loadout = uiState.championLoadouts[champ.id]
                    val isSelected = champ.id == uiState.selectedChampionId
                    val unlocked = loadout?.isUnlocked == true
                    val stars = loadout?.starLevel ?: 0

                    Surface(
                        modifier = Modifier
                            .width(120.dp)
                            .minimumInteractiveComponentSize()
                            .testTag("roster_champ_${champ.id}")
                            .clickable { onSelectChampion(champ.id) },
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) ObsidianSurfaceElevated else ObsidianCard,
                        border = BorderStroke(
                            if (isSelected) 2.dp else 1.dp,
                            if (isSelected) HextechGoldBright else SlateBorder
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Box(contentAlignment = Alignment.BottomEnd) {
                                ChampionAvatarBadge(
                                    championId = champ.id,
                                    name = champ.name,
                                    region = champ.region,
                                    size = 50.dp
                                )
                                if (!unlocked) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = ObsidianBg,
                                        border = BorderStroke(1.dp, CrimsonNexus)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = "Locked",
                                            tint = CrimsonNexus,
                                            modifier = Modifier
                                                .padding(4.dp)
                                                .size(13.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = champ.name,
                                style = MaterialTheme.typography.titleSmall,
                                color = if (isSelected) HextechGoldBright else ParchmentWhite,
                                maxLines = 1
                            )
                            Text(
                                text = if (unlocked) "${stars}★ • Lv.${loadout.championLevel}" else "${champ.unlockFragmentCost} Frags",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (unlocked) ArcaneCyan else MutedSilver
                            )
                        }
                    }
                }
            }
        }
    }

    val commandCardBlock: @Composable () -> Unit = {
        if (selectedLoadout != null) {
            val accent = regionAccentColor(selectedChamp.region)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = ObsidianSurface,
                border = BorderStroke(1.6.dp, Brush.linearGradient(listOf(HextechGoldBright, accent, HextechGold))),
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ChampionAvatarBadge(
                            championId = selectedChamp.id,
                            name = selectedChamp.name,
                            region = selectedChamp.region,
                            size = 56.dp
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${selectedChamp.name} — ${selectedChamp.title}",
                                style = MaterialTheme.typography.titleMedium,
                                color = HextechGoldBright,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = selectedChamp.archetypeSummary,
                                style = MaterialTheme.typography.bodySmall,
                                color = accent,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                for (star in 1..6) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = if (star <= selectedLoadout.starLevel) HextechGoldBright else SlateBorder,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        StatChip("HP ${selectedLoadout.maxNexusHp}", EmeraldHeal, Modifier.weight(1f))
                        StatChip("+${selectedLoadout.victoryHeal}/Win", ArcaneCyan, Modifier.weight(1f))
                        StatChip("${selectedLoadout.startingGold}g", HextechGoldBright, Modifier.weight(1f))
                        StatChip("${selectedLoadout.startingRerolls} Roll", MutedSilver, Modifier.weight(1f))
                    }

                    val activeStarPower = selectedChamp.starPowers.lastOrNull { it.starLevel <= selectedLoadout.starLevel }
                    if (activeStarPower != null) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = ObsidianCard,
                            border = BorderStroke(1.dp, SlateBorder)
                        ) {
                            Column(modifier = Modifier.padding(9.dp)) {
                                Text(
                                    text = "Active Star Power (${activeStarPower.starLevel}★): ${activeStarPower.title}",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = HextechGoldBright
                                )
                                Text(
                                    text = activeStarPower.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MutedSilver,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    val equippedCount = selectedLoadout.equippedRelics.count { it != null }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onOpenChampionDetail(selectedChamp.id) },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("champion_mastery_button"),
                            border = BorderStroke(1.dp, HextechGold)
                        ) {
                            Icon(Icons.Default.Shield, contentDescription = null, tint = HextechGoldBright, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Stars & Relics ($equippedCount/${selectedLoadout.unlockedRelicSlots})",
                                color = HextechGoldBright,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    if (selectedLoadout.isUnlocked) {
                        Button(
                            onClick = onOpenAdventures,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("embark_adventure_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = HextechGold,
                                contentColor = DarkButtonInk
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Explore, contentDescription = "Embark", modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "EMBARK ON ADVENTURE",
                                style = fantasyHeadingStyle(
                                    fontSize = MaterialTheme.typography.labelLarge.fontSize,
                                    color = DarkButtonInk,
                                    withShadow = false
                                ),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Button(
                            onClick = { onUnlockChampion(selectedChamp.id) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("unlock_champion_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ArcaneCyan,
                                contentColor = DarkButtonInk
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = "Unlock", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "UNLOCK CHAMPION (${selectedChamp.unlockFragmentCost} FRAGMENTS)",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }
        }
    }

    LorScreenBackdrop(
        modifier = modifier,
        accentGlow = regionAccentColor(selectedChamp.region)
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
        if (maxWidth >= 600.dp) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 1080.dp)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(0.52f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    heroBannerBlock(118.dp)
                    MetaCurrencyBar(profile = uiState.legendProfile)
                    savedRunBlock()
                    rosterBlock()
                }
                Column(
                    modifier = Modifier
                        .weight(0.48f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.Top
                ) {
                    commandCardBlock()
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 720.dp),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item { heroBannerBlock(168.dp) }
                item { MetaCurrencyBar(profile = uiState.legendProfile) }
                if (uiState.savedRun != null) {
                    item { savedRunBlock() }
                }
                item { rosterBlock() }
                if (selectedLoadout != null) {
                    item { commandCardBlock() }
                }
            }
        }
        }
    }

    if (showGlossary) {
        KeywordGlossaryDialog(onDismiss = { showGlossary = false })
    }
}

@Composable
private fun StatChip(text: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.14f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.45f))
    ) {
        Box(
            modifier = Modifier.padding(vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = ParchmentWhite
            )
        }
    }
}
