package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.model.CardCatalog
import com.example.model.DeckCardEntry
import com.example.model.PowerTemplate
import com.example.model.RunState
import com.example.ui.components.CardTile
import com.example.ui.components.LorScreenBackdrop
import com.example.ui.components.rarityColor
import com.example.ui.theme.ArcaneCyan
import com.example.ui.theme.CrimsonNexus
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
import com.example.viewmodel.CardRewardOffer
import com.example.viewmodel.GameUiState
import com.example.viewmodel.RunRewardsSummary

@Composable
fun CardDraftScreen(
    run: RunState,
    offers: List<CardRewardOffer>,
    onPick: (CardRewardOffer) -> Unit,
    onReroll: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onSkip)

    EncounterFrame(
        title = "Draft a Reinforcement Card",
        subtitle = "Each card comes pre-equipped with a stackable Item",
        run = run,
        modifier = modifier
    ) {
        BoxWithConstraints(modifier = Modifier.weight(1f)) {
            if (maxWidth >= 560.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    offers.forEachIndexed { idx, offer ->
                        CardTile(
                            template = offer.card,
                            effectiveCost = maxOf(0, offer.card.cost - offer.attachedItem.costReduction),
                            bonusPower = offer.attachedItem.bonusPower,
                            bonusHealth = offer.attachedItem.bonusHealth,
                            attachedItems = listOf(offer.attachedItem),
                            testTag = "draft_card_option_$idx",
                            onClick = { onPick(offer) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    itemsIndexed(offers) { idx, offer ->
                        CardTile(
                            template = offer.card,
                            effectiveCost = maxOf(0, offer.card.cost - offer.attachedItem.costReduction),
                            bonusPower = offer.attachedItem.bonusPower,
                            bonusHealth = offer.attachedItem.bonusHealth,
                            attachedItems = listOf(offer.attachedItem),
                            testTag = "draft_card_option_$idx",
                            onClick = { onPick(offer) }
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                enabled = run.rerollsRemaining > 0,
                onClick = onReroll,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("reroll_card_draft_button")
            ) {
                Icon(Icons.Default.Casino, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Reroll (${run.rerollsRemaining})")
            }
            TextButton(
                onClick = onSkip,
                modifier = Modifier.height(48.dp)
            ) {
                Text("Skip", color = MutedSilver)
            }
        }
    }
}

@Composable
fun PowerDraftScreen(
    run: RunState,
    powers: List<PowerTemplate>,
    onPick: (PowerTemplate) -> Unit,
    onReroll: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { /* Must choose a Power to proceed on Power Shrine */ }

    EncounterFrame(
        title = "Power Shrine",
        subtitle = "Choose 1 passive Power for the rest of your run",
        run = run,
        modifier = modifier
    ) {
        BoxWithConstraints(modifier = Modifier.weight(1f)) {
            if (maxWidth >= 560.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    powers.forEach { power ->
                        val accent = rarityColor(power.rarity)
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 160.dp)
                                .minimumInteractiveComponentSize()
                                .testTag("draft_power_${power.id}")
                                .clickable { onPick(power) },
                            shape = RoundedCornerShape(14.dp),
                            color = ObsidianCard,
                            border = BorderStroke(1.5.dp, accent)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Bolt,
                                        contentDescription = power.name,
                                        tint = accent,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Text(
                                        text = power.rarity.displayName,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = accent
                                    )
                                }
                                Text(
                                    text = power.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = accent
                                )
                                Text(
                                    text = power.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ParchmentWhite
                                )
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(powers, key = { it.id }) { power ->
                        val accent = rarityColor(power.rarity)
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .minimumInteractiveComponentSize()
                                .testTag("draft_power_${power.id}")
                                .clickable { onPick(power) },
                            shape = RoundedCornerShape(14.dp),
                            color = ObsidianCard,
                            border = BorderStroke(1.5.dp, accent)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(Icons.Default.Bolt, contentDescription = power.name, tint = accent, modifier = Modifier.size(28.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${power.name} (${power.rarity.displayName})",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = accent
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = power.description,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = ParchmentWhite
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedButton(
            enabled = run.rerollsRemaining > 0,
            onClick = onReroll,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("reroll_power_draft_button")
        ) {
            Icon(Icons.Default.Casino, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Reroll Powers (${run.rerollsRemaining})")
        }
    }
}

@Composable
fun ShopScreen(
    uiState: GameUiState,
    onBuyCard: (Int) -> Unit,
    onSelectItem: (Int?) -> Unit,
    onAttachItemToCard: (String) -> Unit,
    onBuyPower: () -> Unit,
    onLeave: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onLeave)
    val run = uiState.activeRun ?: return

    EncounterFrame(
        title = "Smooth Soloist Merchant",
        subtitle = "Spend Gold on Cards, Stackable Items, and Powers",
        run = run,
        modifier = modifier
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            uiState.shopPower?.let { powOffer ->
                item {
                    Text("Featured Power", style = MaterialTheme.typography.titleSmall, color = EpicViolet)
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = ObsidianCard,
                        border = BorderStroke(1.dp, rarityColor(powOffer.power.rarity))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(powOffer.power.name, style = MaterialTheme.typography.titleSmall, color = HextechGoldBright)
                                Text(powOffer.power.description, style = MaterialTheme.typography.bodySmall, color = MutedSilver)
                            }
                            Button(
                                enabled = !powOffer.purchased && run.gold >= powOffer.goldCost,
                                onClick = onBuyPower,
                                modifier = Modifier.testTag("buy_shop_power_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = HextechGold, contentColor = ObsidianBg)
                            ) {
                                Text(if (powOffer.purchased) "Owned" else "${powOffer.goldCost}g")
                            }
                        }
                    }
                }
            }

            item {
                Text("Stackable Items (Attach to Deck Card)", style = MaterialTheme.typography.titleSmall, color = ArcaneCyan)
            }
            itemsIndexed(uiState.shopItems) { idx, itemOffer ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = ObsidianCard,
                    border = BorderStroke(1.dp, rarityColor(itemOffer.item.rarity))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(itemOffer.item.name, style = MaterialTheme.typography.titleSmall, color = ParchmentWhite)
                            Text(itemOffer.item.description, style = MaterialTheme.typography.bodySmall, color = MutedSilver)
                        }
                        Button(
                            enabled = !itemOffer.purchased && run.gold >= itemOffer.goldCost,
                            onClick = { onSelectItem(idx) },
                            modifier = Modifier.testTag("buy_shop_item_$idx"),
                            colors = ButtonDefaults.buttonColors(containerColor = ArcaneCyan, contentColor = ObsidianBg)
                        ) {
                            Text(if (itemOffer.purchased) "Bought" else "${itemOffer.goldCost}g")
                        }
                    }
                }
            }

            item {
                Text("Cards for Sale", style = MaterialTheme.typography.titleSmall, color = HextechGoldBright)
                Spacer(modifier = Modifier.height(6.dp))
                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    if (maxWidth >= 560.dp) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            uiState.shopCards.forEachIndexed { idx, cardOffer ->
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    CardTile(
                                        template = cardOffer.card,
                                        attachedItems = listOf(cardOffer.attachedItem),
                                        playable = !cardOffer.purchased && run.gold >= cardOffer.goldCost
                                    )
                                    Button(
                                        enabled = !cardOffer.purchased && run.gold >= cardOffer.goldCost,
                                        onClick = { onBuyCard(idx) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp)
                                            .testTag("buy_shop_card_$idx"),
                                        colors = ButtonDefaults.buttonColors(containerColor = HextechGold, contentColor = ObsidianBg)
                                    ) {
                                        Icon(Icons.Default.ShoppingBag, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(if (cardOffer.purchased) "Purchased" else "${cardOffer.goldCost}g")
                                    }
                                }
                            }
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            uiState.shopCards.forEachIndexed { idx, cardOffer ->
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    CardTile(
                                        template = cardOffer.card,
                                        attachedItems = listOf(cardOffer.attachedItem),
                                        playable = !cardOffer.purchased && run.gold >= cardOffer.goldCost
                                    )
                                    Button(
                                        enabled = !cardOffer.purchased && run.gold >= cardOffer.goldCost,
                                        onClick = { onBuyCard(idx) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp)
                                            .testTag("buy_shop_card_$idx"),
                                        colors = ButtonDefaults.buttonColors(containerColor = HextechGold, contentColor = ObsidianBg)
                                    ) {
                                        Icon(Icons.Default.ShoppingBag, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(if (cardOffer.purchased) "Purchased" else "Buy Card (${cardOffer.goldCost}g)")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Button(
            onClick = onLeave,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("leave_shop_button"),
            colors = ButtonDefaults.buttonColors(containerColor = ObsidianSurfaceElevated, contentColor = ParchmentWhite)
        ) {
            Text("Continue Journey")
        }
    }

    if (uiState.pendingShopItemIndex != null) {
        DeckCardTargetDialog(
            title = "Attach Item to Card",
            deck = run.runDeck,
            onSelectCard = onAttachItemToCard,
            onDismiss = { onSelectItem(null) }
        )
    }
}

@Composable
fun HealerScreen(
    run: RunState,
    onHeal: () -> Unit,
    onCutCard: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { /* Choose Heal or Cut Card */ }
    var showCutDialog by remember { mutableStateOf(false) }

    EncounterFrame(
        title = "Healer Sanctuary",
        subtitle = "Restore your Nexus or prune a weak card from your deck",
        run = run,
        modifier = modifier
    ) {
        BoxWithConstraints(modifier = Modifier.weight(1f)) {
            if (maxWidth >= 560.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 140.dp)
                            .minimumInteractiveComponentSize()
                            .testTag("healer_restore_button")
                            .clickable(onClick = onHeal),
                        shape = RoundedCornerShape(14.dp),
                        color = ObsidianCard,
                        border = BorderStroke(1.5.dp, EmeraldHeal)
                    ) {
                        Row(
                            modifier = Modifier.padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Default.Favorite, contentDescription = "Heal", tint = EmeraldHeal, modifier = Modifier.size(30.dp))
                            Column {
                                Text("Restore 40% Nexus Health", style = MaterialTheme.typography.titleMedium, color = EmeraldHeal)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "Heal up to +${maxOf(6, (run.maxNexusHp * 0.4f).toInt())} HP (Current: ${run.currentNexusHp}/${run.maxNexusHp})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MutedSilver
                                )
                            }
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 140.dp)
                            .minimumInteractiveComponentSize()
                            .testTag("healer_cut_button")
                            .clickable { showCutDialog = true },
                        shape = RoundedCornerShape(14.dp),
                        color = ObsidianCard,
                        border = BorderStroke(1.5.dp, CrimsonNexus)
                    ) {
                        Row(
                            modifier = Modifier.padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Default.ContentCut, contentDescription = "Cut Card", tint = CrimsonNexus, modifier = Modifier.size(30.dp))
                            Column {
                                Text("Cut a Card from Deck", style = MaterialTheme.typography.titleMedium, color = CrimsonNexus)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Improve draw consistency by removing 1 copy of a card.", style = MaterialTheme.typography.bodySmall, color = MutedSilver)
                            }
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .minimumInteractiveComponentSize()
                            .testTag("healer_restore_button")
                            .clickable(onClick = onHeal),
                        shape = RoundedCornerShape(14.dp),
                        color = ObsidianCard,
                        border = BorderStroke(1.5.dp, EmeraldHeal)
                    ) {
                        Row(
                            modifier = Modifier.padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Default.Favorite, contentDescription = "Heal", tint = EmeraldHeal, modifier = Modifier.size(30.dp))
                            Column {
                                Text("Restore 40% Nexus Health", style = MaterialTheme.typography.titleMedium, color = EmeraldHeal)
                                Text(
                                    "Heal up to +${maxOf(6, (run.maxNexusHp * 0.4f).toInt())} HP (Current: ${run.currentNexusHp}/${run.maxNexusHp})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MutedSilver
                                )
                            }
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .minimumInteractiveComponentSize()
                            .testTag("healer_cut_button")
                            .clickable { showCutDialog = true },
                        shape = RoundedCornerShape(14.dp),
                        color = ObsidianCard,
                        border = BorderStroke(1.5.dp, CrimsonNexus)
                    ) {
                        Row(
                            modifier = Modifier.padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Default.ContentCut, contentDescription = "Cut Card", tint = CrimsonNexus, modifier = Modifier.size(30.dp))
                            Column {
                                Text("Cut a Card from Deck", style = MaterialTheme.typography.titleMedium, color = CrimsonNexus)
                                Text("Improve draw consistency by removing 1 copy of a card.", style = MaterialTheme.typography.bodySmall, color = MutedSilver)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCutDialog) {
        DeckCardTargetDialog(
            title = "Select Card Copy to Cut",
            deck = run.runDeck,
            onSelectCard = {
                showCutDialog = false
                onCutCard(it)
            },
            onDismiss = { showCutDialog = false }
        )
    }
}

@Composable
fun ReinforcementScreen(
    run: RunState,
    packages: List<CardCatalog.ReinforcementPackage>,
    onPick: (CardCatalog.ReinforcementPackage) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { packages.firstOrNull()?.let(onPick) }

    EncounterFrame(
        title = "Support Champion Reinforcements",
        subtitle = "Recruit a Secondary Champion package into your deck",
        run = run,
        modifier = modifier
    ) {
        BoxWithConstraints(modifier = Modifier.weight(1f)) {
            if (maxWidth >= 560.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    packages.forEach { pkg ->
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 160.dp)
                                .minimumInteractiveComponentSize()
                                .testTag("reinf_pkg_${pkg.championId}")
                                .clickable { onPick(pkg) },
                            shape = RoundedCornerShape(14.dp),
                            color = ObsidianCard,
                            border = BorderStroke(1.5.dp, ArcaneCyan)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.GroupAdd, contentDescription = null, tint = ArcaneCyan)
                                    Text(pkg.title, style = MaterialTheme.typography.titleMedium, color = HextechGoldBright)
                                }
                                Text(pkg.subtitle, style = MaterialTheme.typography.bodySmall, color = MutedSilver)
                                Text(
                                    text = "Includes: ${pkg.cardIds.joinToString { CardCatalog.getCard(it).name }}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = ParchmentWhite
                                )
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(packages, key = { it.championId }) { pkg ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .minimumInteractiveComponentSize()
                                .testTag("reinf_pkg_${pkg.championId}")
                                .clickable { onPick(pkg) },
                            shape = RoundedCornerShape(14.dp),
                            color = ObsidianCard,
                            border = BorderStroke(1.5.dp, ArcaneCyan)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.GroupAdd, contentDescription = null, tint = ArcaneCyan)
                                    Text(pkg.title, style = MaterialTheme.typography.titleMedium, color = HextechGoldBright)
                                }
                                Text(pkg.subtitle, style = MaterialTheme.typography.bodySmall, color = MutedSilver)
                                Text(
                                    text = "Includes: ${pkg.cardIds.joinToString { CardCatalog.getCard(it).name }}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = ParchmentWhite
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ItemChestScreen(
    uiState: GameUiState,
    onAttachToCard: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val run = uiState.activeRun ?: return
    val item = uiState.chestOfferedItem ?: return
    BackHandler { run.runDeck.firstOrNull()?.let { onAttachToCard(it.cardId) } }

    EncounterFrame(
        title = "Item Vault",
        subtitle = "Choose a card in your deck to permanently equip this Item",
        run = run,
        modifier = modifier
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = ObsidianSurfaceElevated,
            border = BorderStroke(1.5.dp, rarityColor(item.rarity))
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(Icons.Default.Inventory2, contentDescription = item.name, tint = rarityColor(item.rarity), modifier = Modifier.size(28.dp))
                Column {
                    Text("${item.name} (${item.rarity.displayName})", style = MaterialTheme.typography.titleMedium, color = HextechGoldBright)
                    Text(item.description, style = MaterialTheme.typography.bodyMedium, color = ParchmentWhite)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(run.runDeck, key = { it.cardId }) { entry ->
                val card = CardCatalog.getCard(entry.cardId)
                CardTile(
                    template = card,
                    attachedItems = entry.attachedItemIds.map { CardCatalog.getItem(it) },
                    testTag = "chest_target_${entry.cardId}",
                    onClick = { onAttachToCard(entry.cardId) }
                )
            }
        }
    }
}

@Composable
fun NarrativeEventScreen(
    uiState: GameUiState,
    onChooseOption: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val run = uiState.activeRun ?: return
    val event = uiState.narrativeEvent ?: return
    BackHandler { onChooseOption(event.options.last().id) }

    EncounterFrame(
        title = event.title,
        subtitle = event.flavor,
        run = run,
        modifier = modifier
    ) {
        BoxWithConstraints(modifier = Modifier.weight(1f)) {
            if (maxWidth >= 560.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    event.options.forEach { opt ->
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 140.dp)
                                .minimumInteractiveComponentSize()
                                .testTag("event_opt_${opt.id}")
                                .clickable { onChooseOption(opt.id) },
                            shape = RoundedCornerShape(14.dp),
                            color = ObsidianCard,
                            border = BorderStroke(1.5.dp, HextechGold)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(opt.title, style = MaterialTheme.typography.titleMedium, color = HextechGoldBright)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(opt.description, style = MaterialTheme.typography.bodyMedium, color = ParchmentWhite)
                            }
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    event.options.forEach { opt ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .minimumInteractiveComponentSize()
                                .testTag("event_opt_${opt.id}")
                                .clickable { onChooseOption(opt.id) },
                            shape = RoundedCornerShape(14.dp),
                            color = ObsidianCard,
                            border = BorderStroke(1.5.dp, HextechGold)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(opt.title, style = MaterialTheme.typography.titleMedium, color = HextechGoldBright)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(opt.description, style = MaterialTheme.typography.bodyMedium, color = ParchmentWhite)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RunSummaryScreen(
    summary: RunRewardsSummary,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onDone)

    LorScreenBackdrop(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
        Surface(
            modifier = Modifier.widthIn(max = 580.dp).fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            color = ObsidianSurface,
            border = BorderStroke(2.dp, if (summary.isVictory) HextechGoldBright else CrimsonNexus)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = "Run Complete",
                    tint = if (summary.isVictory) HextechGoldBright else CrimsonNexus,
                    modifier = Modifier.size(54.dp)
                )
                Text(
                    text = if (summary.isVictory) "ADVENTURE CONQUERED!" else "RUN ENDED",
                    style = MaterialTheme.typography.headlineMedium,
                    color = if (summary.isVictory) HextechGoldBright else CrimsonNexus
                )
                Text(
                    text = "${summary.championName} • ${summary.adventureName}",
                    style = MaterialTheme.typography.titleMedium,
                    color = ParchmentWhite
                )

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = ObsidianCard,
                    border = BorderStroke(1.dp, SlateBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        SummaryRow("Battles Won", "${summary.battlesWon}")
                        SummaryRow("Champion XP Earned", "+${summary.xpGained} XP")
                        if (summary.leveledUpTo != null) {
                            SummaryRow("Champion Level Up!", "Level ${summary.leveledUpTo}")
                        }
                        SummaryRow("Champion Fragments", "+${summary.fragmentsGained}")
                        SummaryRow("Wild Fragments", "+${summary.wildFragmentsGained}")
                        SummaryRow("Legend Points", "+${summary.legendPointsGained}")
                    }
                }

                Button(
                    onClick = onDone,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("run_summary_done_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = HextechGold, contentColor = ObsidianBg)
                ) {
                    Text("RETURN TO HUB")
                }
            }
        }
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MutedSilver)
        Text(value, style = MaterialTheme.typography.labelLarge, color = ArcaneCyan)
    }
}

@Composable
private fun EncounterFrame(
    title: String,
    subtitle: String,
    run: RunState,
    modifier: Modifier = Modifier,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit
) {
    LorScreenBackdrop(modifier = modifier) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 960.dp)
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, style = MaterialTheme.typography.headlineSmall, color = HextechGoldBright, modifier = Modifier.weight(1f))
                Text(
                    text = "HP ${run.currentNexusHp}/${run.maxNexusHp} • ${run.gold}g",
                    style = MaterialTheme.typography.labelLarge,
                    color = ArcaneCyan
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MutedSilver)
            Spacer(modifier = Modifier.height(14.dp))
            content()
        }
        }
    }
}

@Composable
private fun DeckCardTargetDialog(
    title: String,
    deck: List<DeckCardEntry>,
    onSelectCard: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ObsidianSurface,
        title = { Text(title, color = HextechGoldBright) },
        text = {
            LazyColumn(
                modifier = Modifier.heightIn(max = 380.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(deck, key = { it.cardId }) { entry ->
                    val card = CardCatalog.getCard(entry.cardId)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .minimumInteractiveComponentSize()
                            .clickable { onSelectCard(entry.cardId) },
                        shape = RoundedCornerShape(10.dp),
                        color = ObsidianCard,
                        border = BorderStroke(1.dp, SlateBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("${entry.copies}x ${card.name}", style = MaterialTheme.typography.titleSmall, color = ParchmentWhite)
                                Text(card.description, style = MaterialTheme.typography.bodySmall, color = MutedSilver)
                            }
                            Text("${card.cost}m", style = MaterialTheme.typography.labelMedium, color = ArcaneCyan)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MutedSilver)
            }
        }
    )
}
