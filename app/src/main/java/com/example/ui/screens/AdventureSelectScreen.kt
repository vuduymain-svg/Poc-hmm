package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.model.AdventureCatalog
import com.example.model.CardCatalog
import com.example.ui.components.LorScreenBackdrop
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
import com.example.ui.theme.ParchmentWhite
import com.example.viewmodel.GameUiState

@Composable
fun AdventureSelectScreen(
    uiState: GameUiState,
    onStartAdventure: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)

    val champ = CardCatalog.getChampion(uiState.selectedChampionId)

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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ObsidianSurface)
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("adv_select_back_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = ParchmentWhite)
                }
                Column {
                    Text(
                        text = "World Adventures",
                        style = MaterialTheme.typography.headlineSmall,
                        color = HextechGoldBright
                    )
                    Text(
                        text = "Deploying with ${champ.name}",
                        style = MaterialTheme.typography.bodySmall,
                        color = ArcaneCyan
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(AdventureCatalog.adventures, key = { it.id }) { adv ->
                    val completed = uiState.legendProfile.completedAdventureIds.contains(adv.id)
                    val accent = regionAccentColor(adv.region)

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = ObsidianCard,
                        border = BorderStroke(1.5.dp, if (completed) EmeraldHeal else accent.copy(alpha = 0.6f))
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = accent.copy(alpha = 0.2f),
                                    border = BorderStroke(1.dp, accent)
                                ) {
                                    Text(
                                        text = "${adv.difficultyLabel} • ${adv.region.displayName}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = accent,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                                if (completed) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = "Completed", tint = EmeraldHeal, modifier = Modifier.size(16.dp))
                                        Text("CLEARED", style = MaterialTheme.typography.labelSmall, color = EmeraldHeal)
                                    }
                                }
                            }

                            Text(
                                text = adv.name,
                                style = MaterialTheme.typography.titleLarge,
                                color = HextechGoldBright
                            )
                            Text(
                                text = adv.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MutedSilver
                            )
                            Text(
                                text = "Boss: ${adv.bossName}  •  Rewards: ${adv.rewardXp} XP, ${adv.rewardFragments} Frags",
                                style = MaterialTheme.typography.labelMedium,
                                color = ParchmentWhite
                            )

                            if (adv.mutators.isNotEmpty()) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    adv.mutators.forEach { mut ->
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = CrimsonNexus.copy(alpha = 0.14f),
                                            border = BorderStroke(1.dp, CrimsonNexus.copy(alpha = 0.45f))
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(8.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Icon(Icons.Default.Warning, contentDescription = "Mutator", tint = CrimsonNexus, modifier = Modifier.size(14.dp))
                                                Text(
                                                    text = "${mut.name}: ${mut.description}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = ParchmentWhite
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Button(
                                onClick = { onStartAdventure(adv.id) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("start_adv_${adv.id}"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = HextechGold,
                                    contentColor = DarkButtonInk
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("BEGIN ADVENTURE", style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }
                }
            }
        }
        }
    }
}
