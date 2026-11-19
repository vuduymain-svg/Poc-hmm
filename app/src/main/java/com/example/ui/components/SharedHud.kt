package com.example.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.CardCatalog
import com.example.model.LegendProfile
import com.example.model.PowerTemplate
import com.example.model.Rarity
import com.example.model.Region
import com.example.model.RunState
import com.example.ui.theme.ArcaneCyan
import com.example.ui.theme.CommonIron
import com.example.ui.theme.CrimsonNexus
import com.example.ui.theme.CurseMagenta
import com.example.ui.theme.EmeraldHeal
import com.example.ui.theme.EpicViolet
import com.example.ui.theme.HextechGold
import com.example.ui.theme.HextechGoldBright
import com.example.ui.theme.LegendaryAmber
import com.example.ui.theme.ManaBlue
import com.example.ui.theme.MutedSilver
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianSurfaceElevated
import com.example.ui.theme.ParchmentWhite
import com.example.ui.theme.RareSapphire
import com.example.ui.theme.SlateBorder

fun rarityColor(rarity: Rarity): Color = when (rarity) {
    Rarity.COMMON -> CommonIron
    Rarity.RARE -> RareSapphire
    Rarity.EPIC -> EpicViolet
    Rarity.LEGENDARY -> LegendaryAmber
}

fun regionAccentColor(region: Region): Color = when (region) {
    Region.PILTOVER_ZAUN -> ArcaneCyan
    Region.DEMACIA -> HextechGoldBright
    Region.IONIA -> CurseMagenta
    Region.FRELJORD -> ManaBlue
    Region.SHADOW_ISLES -> EmeraldHeal
    Region.TARGON -> EpicViolet
    Region.NOXUS -> CrimsonNexus
    Region.RUNETERRA -> LegendaryAmber
}

@DrawableRes
fun championPortraitRes(championId: String): Int? = when (championId) {
    "champ_jinx" -> R.drawable.img_champ_jinx
    "champ_lux" -> R.drawable.img_champ_lux
    "champ_yasuo" -> R.drawable.img_champ_yasuo
    "champ_anivia" -> R.drawable.img_champ_anivia
    else -> null
}

@Composable
fun ChampionAvatarBadge(
    championId: String,
    name: String,
    region: Region,
    size: Dp = 56.dp,
    modifier: Modifier = Modifier
) {
    val res = championPortraitRes(championId)
    val accent = regionAccentColor(region)
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(accent.copy(alpha = 0.35f), ObsidianSurface)
                )
            )
            .border(2.dp, accent, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (res != null) {
            Image(
                painter = painterResource(id = res),
                contentDescription = "$name portrait",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Text(
                text = name.take(2).uppercase(),
                style = MaterialTheme.typography.titleMedium,
                color = accent,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun MetaCurrencyBar(
    profile: LegendProfile,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CurrencyPill(
            icon = Icons.Default.Star,
            label = "Legend ${profile.legendLevel}",
            tint = HextechGoldBright
        )
        CurrencyPill(
            icon = Icons.Default.AutoAwesome,
            label = "${profile.wildFragments} Wild",
            tint = ArcaneCyan
        )
        CurrencyPill(
            icon = Icons.Default.Bolt,
            label = "${profile.starCrystals} Star",
            tint = LegendaryAmber
        )
        CurrencyPill(
            icon = Icons.Default.Shield,
            label = "${profile.novaCrystals} Nova",
            tint = EpicViolet
        )
        CurrencyPill(
            icon = Icons.Default.Diamond,
            label = "${profile.gemstones} Gems",
            tint = EmeraldHeal
        )
    }
}

@Composable
fun CurrencyPill(
    icon: ImageVector,
    label: String,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = ObsidianSurfaceElevated,
        border = BorderStroke(1.dp, tint.copy(alpha = 0.45f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = ParchmentWhite,
                maxLines = 1
            )
        }
    }
}

@Composable
fun RunResourceHud(
    run: RunState,
    onOpenDeck: () -> Unit,
    onAbandon: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = ObsidianSurface,
        border = BorderStroke(1.2.dp, HextechGold.copy(alpha = 0.45f)),
        shape = RoundedCornerShape(14.dp),
        shadowElevation = 6.dp
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CurrencyPill(
                        icon = Icons.Default.Favorite,
                        label = "${run.currentNexusHp}/${run.maxNexusHp}",
                        tint = CrimsonNexus
                    )
                    CurrencyPill(
                        icon = Icons.Default.MonetizationOn,
                        label = "${run.gold}g",
                        tint = HextechGoldBright
                    )
                    CurrencyPill(
                        icon = Icons.Default.Casino,
                        label = "${run.rerollsRemaining}",
                        tint = ArcaneCyan
                    )
                    CurrencyPill(
                        icon = Icons.Default.Refresh,
                        label = "${run.revivesRemaining}",
                        tint = EmeraldHeal
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("run_deck_button")
                            .clickable(onClick = onOpenDeck),
                        shape = RoundedCornerShape(10.dp),
                        color = ObsidianSurfaceElevated,
                        border = BorderStroke(1.dp, HextechGold.copy(alpha = 0.6f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Style,
                                contentDescription = "View Run Deck",
                                tint = HextechGoldBright,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "${run.runDeck.sumOf { it.copies }}",
                                style = MaterialTheme.typography.labelMedium,
                                color = HextechGoldBright
                            )
                        }
                    }

                    if (onAbandon != null) {
                        IconButton(
                            onClick = onAbandon,
                            modifier = Modifier.testTag("abandon_run_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Abandon Run",
                                tint = MutedSilver
                            )
                        }
                    }
                }
            }

            if (run.activePowerIds.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    run.activePowerIds.forEach { powerId ->
                        PowerChip(power = CardCatalog.getPower(powerId))
                    }
                }
            }
        }
    }
}

@Composable
fun PowerChip(
    power: PowerTemplate,
    modifier: Modifier = Modifier
) {
    val accent = rarityColor(power.rarity)
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = accent.copy(alpha = 0.14f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Bolt,
                contentDescription = power.name,
                tint = accent,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = power.name,
                style = MaterialTheme.typography.labelSmall,
                color = ParchmentWhite,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun RelicSlotCard(
    slotIndex: Int,
    relicId: String?,
    unlocked: Boolean,
    isCosmicForged: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val relic = relicId?.let { CardCatalog.getRelic(it) }
    val borderColor = when {
        !unlocked -> SlateBorder
        isCosmicForged -> EpicViolet
        relic != null -> rarityColor(relic.rarity)
        else -> HextechGold.copy(alpha = 0.5f)
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .minimumInteractiveComponentSize()
            .testTag("relic_slot_$slotIndex")
            .clickable(enabled = unlocked, onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (unlocked) ObsidianCard else ObsidianBg,
        border = BorderStroke(1.5.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(borderColor.copy(alpha = 0.2f))
                    .border(1.dp, borderColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (!unlocked) Icons.Default.Lock else Icons.Default.Shield,
                    contentDescription = if (unlocked) "Relic Slot ${slotIndex + 1}" else "Locked Slot",
                    tint = borderColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = when {
                            !unlocked -> "Slot ${slotIndex + 1} (Locked)"
                            relic != null -> relic.name
                            else -> "Empty Relic Slot ${slotIndex + 1}"
                        },
                        style = MaterialTheme.typography.titleSmall,
                        color = if (unlocked) ParchmentWhite else MutedSilver
                    )
                    if (isCosmicForged) {
                        Text(
                            text = "EPIC FORGED",
                            style = MaterialTheme.typography.labelSmall,
                            color = EpicViolet
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = when {
                        !unlocked -> "Level up Champion or Cosmic Forge to unlock."
                        relic != null -> relic.description
                        else -> "Tap to equip a persistent Relic."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedSilver
                )
            }
        }
    }
}
