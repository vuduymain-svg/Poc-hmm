package com.example.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SportsKabaddi
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.R
import com.example.model.CardCatalog
import com.example.model.CardTemplate
import com.example.model.CardType
import com.example.model.DeckCardEntry
import com.example.model.HandCardInstance
import com.example.model.ItemTemplate
import com.example.model.Keyword
import com.example.model.KeywordCategory
import com.example.model.SpellSpeed
import com.example.model.StateCondition
import com.example.model.UnitOnBoard
import com.example.ui.theme.ArcaneCyan
import com.example.ui.theme.CinzelFontFamily
import com.example.ui.theme.CrimsonNexus
import com.example.ui.theme.CurseMagenta
import com.example.ui.theme.EmeraldHeal
import com.example.ui.theme.EpicViolet
import com.example.ui.theme.FollowerBronze
import com.example.ui.theme.FollowerSilverBright
import com.example.ui.theme.FollowerSilverDark
import com.example.ui.theme.HextechGold
import com.example.ui.theme.HextechGoldBright
import com.example.ui.theme.HextechGoldDeep
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.LegendaryAmber
import com.example.ui.theme.ManaBlue
import com.example.ui.theme.MutedSilver
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianSurfaceElevated
import com.example.ui.theme.ParchmentWhite
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateBorderHighlight

private fun cardVariantFrameBrush(
    template: CardTemplate,
    focused: Boolean = false,
    playable: Boolean = true
): Brush = when {
    focused -> Brush.linearGradient(listOf(HextechGoldBright, ArcaneCyan, HextechGoldBright))
    template.type == CardType.CURSE -> Brush.linearGradient(
        listOf(CurseMagenta, Color(0xFF9D174D), Color(0xFF4C0519))
    )
    template.isChampion -> Brush.linearGradient(
        listOf(HextechGoldBright, HextechGold, HextechGoldDeep, HextechGold)
    )
    template.type == CardType.SPELL -> Brush.linearGradient(
        listOf(ManaBlue, Color(0xFF0284C7), Color(0xFF1E3A8A))
    )
    playable -> Brush.linearGradient(
        listOf(FollowerSilverBright, FollowerSilverDark, FollowerBronze, FollowerSilverBright)
    )
    else -> Brush.linearGradient(
        listOf(FollowerSilverDark, SlateBorder, Color(0xFF3A3532))
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CardTile(
    template: CardTemplate,
    effectiveCost: Int = template.cost,
    bonusPower: Int = 0,
    bonusHealth: Int = 0,
    attachedItems: List<ItemTemplate> = emptyList(),
    extraKeywords: Set<Keyword> = emptySet(),
    isFleeting: Boolean = false,
    playable: Boolean = true,
    highlighted: Boolean = false,
    compact: Boolean = false,
    testTag: String? = null,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val regionColor = regionAccentColor(template.region)
    val frameBrush = cardVariantFrameBrush(template, focused = highlighted, playable = playable)
    val accentColor = when {
        highlighted -> HextechGoldBright
        template.type == CardType.CURSE -> CurseMagenta
        template.isChampion -> HextechGoldBright
        template.type == CardType.SPELL -> ManaBlue
        else -> FollowerSilverBright
    }

    val allKeywords = template.keywords + extraKeywords + attachedItems.mapNotNull { it.grantedKeyword }
    val hasUnitStats = template.type == CardType.UNIT ||
        template.type == CardType.CHAMPION ||
        template.type == CardType.EQUIPMENT

    Surface(
        modifier = modifier
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
            .then(
                if (onClick != null) Modifier
                    .minimumInteractiveComponentSize()
                    .clickable(onClick = onClick)
                else Modifier
            ),
        shape = RoundedCornerShape(12.dp),
        color = if (playable) ObsidianCard else ObsidianSurface,
        border = BorderStroke(if (highlighted || template.isChampion) 2.2.dp else 1.6.dp, frameBrush),
        shadowElevation = if (highlighted) 12.dp else 7.dp
    ) {
        val tileArtRes = cardPortraitRes(template.id)
        Box(modifier = Modifier.fillMaxWidth()) {
            // Subtle metallic filigree overlay across card frame
            Image(
                painter = painterResource(id = R.drawable.img_metal_filigree_tex),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                alpha = if (template.isChampion) 0.22f else 0.09f,
                modifier = Modifier.matchParentSize()
            )
            // 4-Edge Inner Shadow vignette for recessed metallic frame depth
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.45f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.52f)
                            )
                        )
                    )
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.28f),
                                Color.Transparent,
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.28f)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier.padding(if (compact) 8.dp else 10.dp),
                verticalArrangement = Arrangement.spacedBy(if (compact) 4.dp else 6.dp)
            ) {
                // Header Row: Top-left #00BFFF Mana Gem + Cinzel Nameplate Ribbon + Item Count
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Glowing #00BFFF Mana Gem
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(Color(0xFF93E8FF), ManaBlue, Color(0xFF0A4A8A)),
                                    center = Offset(0.35f, 0.30f)
                                )
                            )
                            .border(1.5.dp, Color(0xFFE0F7FF), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$effectiveCost",
                            style = tabularStatStyle(13.sp, ParchmentWhite)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = template.name,
                            style = fantasyHeadingStyle(
                                fontSize = if (compact) 12.5.sp else 14.sp,
                                color = if (template.isChampion) HextechGoldBright else ParchmentWhite
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = buildString {
                                append(template.region.displayName)
                                append(" • ")
                                append(
                                    if (template.spellSpeed != SpellSpeed.NONE) template.spellSpeed.displayName
                                    else template.type.displayName
                                )
                                if (isFleeting) append(" • Fleeting")
                            },
                            fontFamily = InterFontFamily,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = regionColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (attachedItems.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = EpicViolet.copy(alpha = 0.25f),
                            border = BorderStroke(1.dp, EpicViolet)
                        ) {
                            Text(
                                text = "${attachedItems.size}★",
                                style = MaterialTheme.typography.labelSmall,
                                color = ParchmentWhite,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Artwork Mask: distinct oval/arched medallion for Spells, 3D break-frame pop-out for Units/Champions
                if (tileArtRes != null && !compact) {
                    val isSpellCard = template.type == CardType.SPELL
                    val artShape = if (isSpellCard) RoundedCornerShape(percent = 45) else RoundedCornerShape(8.dp)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(if (isSpellCard) 76.dp else 80.dp)
                            .padding(horizontal = if (isSpellCard) 12.dp else 0.dp)
                            .zIndex(2f)
                            .graphicsLayer {
                                translationY = (-3).dp.toPx()
                            },
                        shape = artShape,
                        color = ObsidianBg,
                        border = BorderStroke(
                            width = if (isSpellCard) 1.8.dp else 1.5.dp,
                            color = if (isSpellCard) ManaBlue.copy(alpha = 0.90f) else accentColor.copy(alpha = 0.85f)
                        ),
                        shadowElevation = 6.dp
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Image(
                                painter = painterResource(id = tileArtRes),
                                contentDescription = template.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.42f))
                                        )
                                    )
                            )
                        }
                    }
                }

                // Dark Translucent Text Box (Keywords + Horizontal Divider + Crisp White Inter Description)
                if (!compact || allKeywords.isNotEmpty()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xD90A111F),
                        border = BorderStroke(0.8.dp, accentColor.copy(alpha = 0.35f))
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            if (allKeywords.isNotEmpty()) {
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    allKeywords.take(if (compact) 3 else 6).forEach { kw ->
                                        val kwColor = when (kw.category) {
                                            KeywordCategory.COMBAT_POSITIVE -> ArcaneCyan
                                            KeywordCategory.NEGATIVE_STATUS -> CrimsonNexus
                                            KeywordCategory.CARD_PROPERTY -> HextechGoldBright
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = kwColor.copy(alpha = 0.16f),
                                            border = BorderStroke(0.8.dp, kwColor.copy(alpha = 0.55f))
                                        ) {
                                            Text(
                                                text = kw.displayName,
                                                fontFamily = InterFontFamily,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = kwColor,
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            if (!compact && allKeywords.isNotEmpty() && template.description.isNotBlank()) {
                                HorizontalDivider(
                                    thickness = 0.8.dp,
                                    color = accentColor.copy(alpha = 0.40f)
                                )
                            }

                            if (!compact) {
                                Text(
                                    text = template.description,
                                    fontFamily = InterFontFamily,
                                    fontSize = 11.5.sp,
                                    lineHeight = 15.sp,
                                    color = ParchmentWhite,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                if (attachedItems.isNotEmpty()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        attachedItems.forEach { item ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = rarityColor(item.rarity).copy(alpha = 0.18f),
                                border = BorderStroke(0.8.dp, rarityColor(item.rarity).copy(alpha = 0.7f))
                            ) {
                                Text(
                                    text = "${item.name} (${item.description})",
                                    fontFamily = InterFontFamily,
                                    fontSize = 10.sp,
                                    color = ParchmentWhite,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                // Bottom-Right Side-by-Side Attack (#FFA500) & Health (#FF4500) Gems
                if (hasUnitStats) {
                    val totalPow = template.basePower + bonusPower
                    val totalHp = template.baseHealth + bonusHealth
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (template.isChampion) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Champion",
                                tint = HextechGoldBright,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.weight(1f))
                        }
                        StatBadge(value = totalPow, label = "ATK", color = LegendaryAmber)
                        Spacer(modifier = Modifier.width(6.dp))
                        StatBadge(value = totalHp, label = "HP", color = CrimsonNexus)
                    }
                }
            }
        }
    }
}

@Composable
fun StatBadge(
    value: Int,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.24f),
        border = BorderStroke(1.1.dp, color.copy(alpha = 0.90f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = "$value",
                style = tabularStatStyle(12.sp, ParchmentWhite)
            )
            Text(
                text = label,
                fontFamily = InterFontFamily,
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@DrawableRes
fun cardPortraitRes(cardId: String): Int? = when (cardId) {
    "card_jinx", "card_super_mega_rocket" -> R.drawable.img_champ_jinx
    "card_lux", "card_final_spark" -> R.drawable.img_champ_lux
    "card_yasuo", "card_steel_tempest" -> R.drawable.img_champ_yasuo
    "card_anivia" -> R.drawable.img_champ_anivia
    "card_eggnivia" -> R.drawable.img_anivia_egg
    "card_wild_mystics" -> R.drawable.img_wild_mystics
    "card_mortal_prophet" -> R.drawable.img_mortal_prophet
    "card_guardian_follower" -> R.drawable.img_guardian_follower
    "card_flame_eyed_caprine" -> R.drawable.img_flame_eyed_caprine
    "card_crystal_stormbird" -> R.drawable.img_crystal_stormbird
    "card_harsh_winds" -> R.drawable.img_harsh_winds
    "card_wings_of_cryophoenix" -> R.drawable.img_wings_of_cryophoenix
    "card_clash_of_giants", "card_freljord_titan", "card_glacial_colossus", "card_ancient_yeti" -> R.drawable.img_clash_of_giants
    else -> null
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HandCardView(
    handCard: HandCardInstance,
    effectiveCost: Int,
    playable: Boolean,
    isFocused: Boolean,
    cardWidth: Dp,
    cardHeight: Dp,
    descFontSize: TextUnit,
    statFontSize: TextUnit,
    fanAngle: Float = 0f,
    fanLiftY: Dp = 0.dp,
    isDragging: Boolean = false,
    testTag: String? = null,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val template = handCard.template
    val regionColor = regionAccentColor(template.region)
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val elevated = (isPressed || isFocused) && !isDragging

    val scale by animateFloatAsState(
        targetValue = if (elevated) 1.44f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "hand_card_scale"
    )
    val liftY by animateDpAsState(
        targetValue = if (elevated) -cardHeight * 0.45f else fanLiftY,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "hand_card_lift"
    )
    val rotation by animateFloatAsState(
        targetValue = if (elevated || isDragging) 0f else fanAngle,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "hand_card_rotation"
    )

    val frameBrush = cardVariantFrameBrush(template, focused = isFocused, playable = playable)
    val accentColor = when {
        isFocused -> HextechGoldBright
        playable && template.isChampion -> HextechGoldBright
        playable && template.type == CardType.SPELL -> ManaBlue
        playable -> ArcaneCyan
        template.type == CardType.CURSE -> CurseMagenta
        template.isChampion -> HextechGold
        else -> FollowerSilverDark
    }

    val portraitRes = cardPortraitRes(template.id)
    val artHeight = cardHeight * 0.42f
    val smallTextSize = (descFontSize.value * 0.70f).coerceAtLeast(7f).sp
    val tinyStatSize = (statFontSize.value * 0.82f).coerceAtLeast(9.5f).sp
    val cornerRadius = cardWidth * 0.11f
    val hasUnitStats = template.type == CardType.UNIT ||
        template.type == CardType.CHAMPION ||
        template.type == CardType.EQUIPMENT

    Surface(
        modifier = modifier
            .zIndex(if (elevated) 50f else 1f)
            .alpha(if (isDragging) 0.25f else 1f)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                rotationZ = rotation
                translationY = liftY.toPx()
            }
            .size(width = cardWidth, height = cardHeight)
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(cornerRadius),
        color = if (playable) ObsidianCard else Color(0xFF0E1727),
        border = BorderStroke(
            width = if (elevated) 2.2.dp else if (template.isChampion) 1.9.dp else 1.4.dp,
            brush = frameBrush
        ),
        shadowElevation = if (elevated) 16.dp else 8.dp
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Subtle metallic filigree pattern texture across card backing
            Image(
                painter = painterResource(id = R.drawable.img_metal_filigree_tex),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                alpha = if (template.isChampion) 0.24f else 0.10f,
                modifier = Modifier.fillMaxSize()
            )
            // 4-Edge Inner Shadow along top, bottom & sides so metallic frame feels carved & deep
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.48f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.56f)
                            )
                        )
                    )
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.28f),
                                Color.Transparent,
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.28f)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Upper Metallic Nameplate Banner (Cinzel) offset slightly right of Mana Gem
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xD90A111F),
                                    Color(0xEA18243A),
                                    Color(0xD90A111F)
                                )
                            )
                        )
                        .padding(start = cardWidth * 0.30f, end = cardWidth * 0.06f, top = 2.dp, bottom = 2.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = template.name,
                        style = fantasyHeadingStyle(
                            fontSize = smallTextSize,
                            color = if (template.isChampion) HextechGoldBright else ParchmentWhite
                        ),
                        lineHeight = smallTextSize * 1.05f,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Center Art Mask: distinct oval window for Spells, 3D break-frame pop-out for Units/Champions
                val isSpellCard = template.type == CardType.SPELL
                val artShape = if (isSpellCard) RoundedCornerShape(percent = 45) else RoundedCornerShape(4.dp)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(artHeight)
                        .padding(horizontal = if (isSpellCard) cardWidth * 0.11f else cardWidth * 0.03f)
                        .zIndex(2f)
                        .graphicsLayer { translationY = (-2).dp.toPx() },
                    shape = artShape,
                    color = ObsidianSurface,
                    border = BorderStroke(
                        width = if (isSpellCard) 1.2.dp else 0.9.dp,
                        color = if (isSpellCard) ManaBlue.copy(alpha = 0.85f) else accentColor.copy(alpha = 0.72f)
                    ),
                    shadowElevation = 4.dp
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(regionColor.copy(alpha = 0.42f), ObsidianSurface)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (portraitRes != null) {
                            Image(
                                painter = painterResource(id = portraitRes),
                                contentDescription = template.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(
                                imageVector = when (template.type) {
                                    CardType.SPELL -> Icons.Default.AutoFixHigh
                                    CardType.CHAMPION -> Icons.Default.Star
                                    CardType.EQUIPMENT -> Icons.Default.Gavel
                                    CardType.LANDMARK -> Icons.Default.HourglassBottom
                                    CardType.CURSE -> Icons.Default.Bolt
                                    CardType.UNIT -> Icons.Default.SportsKabaddi
                                },
                                contentDescription = template.name,
                                tint = if (template.isChampion) HextechGoldBright else regionColor,
                                modifier = Modifier.size(cardWidth * 0.36f)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, Color(0x550A111F))
                                    )
                                )
                        )
                    }
                }

                // Lower Dark Translucent Text Box (Inter White Text + Divider) + Bottom-Right Attack/Health Gems
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = cardWidth * 0.05f, vertical = 2.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xE00A111F))
                        .padding(horizontal = cardWidth * 0.05f, vertical = 2.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    val kwHeader = if (template.keywords.isNotEmpty()) {
                        template.keywords.take(2).joinToString(" • ") { it.displayName }
                    } else if (template.spellSpeed != SpellSpeed.NONE) {
                        template.spellSpeed.displayName.uppercase()
                    } else null

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(1.dp)
                    ) {
                        if (kwHeader != null) {
                            Text(
                                text = kwHeader,
                                fontFamily = InterFontFamily,
                                fontSize = (smallTextSize.value * 0.78f).coerceAtLeast(6f).sp,
                                fontWeight = FontWeight.Bold,
                                color = if (template.spellSpeed != SpellSpeed.NONE) ManaBlue else ArcaneCyan,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            HorizontalDivider(
                                thickness = 0.6.dp,
                                color = accentColor.copy(alpha = 0.40f)
                            )
                        }
                        Text(
                            text = template.description,
                            fontFamily = InterFontFamily,
                            fontSize = (smallTextSize.value * 0.80f).coerceAtLeast(6.2f).sp,
                            lineHeight = (smallTextSize.value * 0.92f).coerceAtLeast(7.2f).sp,
                            color = ParchmentWhite,
                            maxLines = if (kwHeader != null) 1 else 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Bottom-Right Side-by-Side Gems: Orange ATK (#FFA500) + Red HP (#FF4500)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (hasUnitStats) {
                            val totalPow = template.basePower + handCard.bonusPower
                            val totalHp = template.baseHealth + handCard.bonusHealth
                            if (handCard.attachedItems.isNotEmpty()) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "Items",
                                    tint = EpicViolet,
                                    modifier = Modifier.size(cardWidth * 0.14f)
                                )
                                Spacer(modifier = Modifier.weight(1f))
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(LegendaryAmber, Color(0xFFB45309))
                                        )
                                    )
                                    .border(0.8.dp, Color(0xFFFDE68A), RoundedCornerShape(4.dp))
                                    .padding(horizontal = cardWidth * 0.06f, vertical = 0.5.dp)
                            ) {
                                Text(
                                    text = "$totalPow",
                                    style = tabularStatStyle(tinyStatSize, ParchmentWhite)
                                )
                            }
                            Spacer(modifier = Modifier.width(3.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(CrimsonNexus, Color(0xFF991B1B))
                                        )
                                    )
                                    .border(0.8.dp, Color(0xFFFCA5A5), RoundedCornerShape(4.dp))
                                    .padding(horizontal = cardWidth * 0.06f, vertical = 0.5.dp)
                            ) {
                                Text(
                                    text = "$totalHp",
                                    style = tabularStatStyle(tinyStatSize, ParchmentWhite)
                                )
                            }
                        } else {
                            Text(
                                text = template.spellSpeed.displayName.uppercase(),
                                fontFamily = InterFontFamily,
                                fontSize = (smallTextSize.value * 0.78f).coerceAtLeast(6.5f).sp,
                                fontWeight = FontWeight.Bold,
                                color = ManaBlue,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            // Glowing #00BFFF Mana Gem Overlay (Top-Left, highest Z-layer)
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(cardWidth * 0.04f)
                    .size(cardWidth * 0.28f)
                    .zIndex(5f)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(Color(0xFF93E8FF), ManaBlue, Color(0xFF0A4A8A)),
                            center = Offset(0.35f, 0.30f)
                        )
                    )
                    .border(1.3.dp, Color(0xFFE0F7FF), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$effectiveCost",
                    style = tabularStatStyle(tinyStatSize, ParchmentWhite)
                )
            }

            // Fleeting indicator (Top-Right)
            if (handCard.isFleeting) {
                Icon(
                    imageVector = Icons.Default.HourglassBottom,
                    contentDescription = "Fleeting",
                    tint = EpicViolet,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(cardWidth * 0.05f)
                        .size(cardWidth * 0.18f)
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UnitToken(
    unit: UnitOnBoard,
    isAttacking: Boolean = false,
    isBlockingLabel: String? = null,
    isSelected: Boolean = false,
    isValidBlockCandidate: Boolean = false,
    isDragging: Boolean = false,
    tileWidth: Dp = 88.dp,
    tileHeight: Dp = 110.dp,
    statFontSize: TextUnit = 14.sp,
    testTag: String? = null,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    // Summon entrance pop-in animation
    val spawnScale = remember(unit.instanceId) { Animatable(0.84f) }
    LaunchedEffect(unit.instanceId) {
        spawnScale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        )
    }

    // Damage hit feedback when unit health drops
    var prevHealth by remember(unit.instanceId) { mutableIntStateOf(unit.health) }
    var damageDelta by remember(unit.instanceId) { mutableIntStateOf(0) }
    LaunchedEffect(unit.health) {
        if (unit.health < prevHealth) {
            damageDelta = prevHealth - unit.health
            prevHealth = unit.health
            kotlinx.coroutines.delay(650)
            damageDelta = 0
        } else {
            prevHealth = unit.health
        }
    }

    val isChampion = unit.template.isChampion
    val frameBrush = when {
        damageDelta > 0 -> Brush.verticalGradient(listOf(CrimsonNexus, Color(0xFF7F1D1D)))
        isSelected -> Brush.verticalGradient(listOf(Color(0xFF7DD3FC), ArcaneCyan))
        isAttacking -> Brush.verticalGradient(listOf(Color(0xFFFDE68A), LegendaryAmber))
        isBlockingLabel != null -> Brush.verticalGradient(listOf(Color(0xFF6EE7B7), EmeraldHeal))
        isValidBlockCandidate -> Brush.verticalGradient(listOf(ArcaneCyan, Color(0xFF0369A1)))
        isChampion -> Brush.linearGradient(listOf(HextechGoldBright, HextechGold, HextechGoldDeep))
        else -> Brush.linearGradient(listOf(FollowerSilverBright, FollowerSilverDark, FollowerBronze))
    }

    val regionColor = regionAccentColor(unit.template.region)
    val portraitRes = cardPortraitRes(unit.template.id)
    val cornerRadius = tileWidth * 0.12f
    val statTextSize = (statFontSize.value * 1.02f).coerceIn(11f, 17f).sp

    val activeKeywords = remember(unit) {
        buildList {
            if (unit.hasBarrier) add(Icons.Default.Shield to HextechGoldBright to "Barrier")
            if (unit.hasSpellShield) add(Icons.Default.AutoFixHigh to EpicViolet to "SpellShield")
            if (unit.isFrostbitten) add(Icons.Default.AcUnit to ManaBlue to "Frostbitten")
            if (unit.isStunned) add(Icons.Default.FlashOn to CrimsonNexus to "Stunned")
            if (unit.keywords.contains(Keyword.QUICK_ATTACK) || unit.keywords.contains(Keyword.DOUBLE_ATTACK)) {
                add(Icons.Default.Bolt to LegendaryAmber to "Quick Attack")
            }
            if (unit.keywords.contains(Keyword.OVERWHELM)) {
                add(Icons.Default.Whatshot to CrimsonNexus to "Overwhelm")
            }
            if (unit.keywords.contains(Keyword.ELUSIVE)) {
                add(Icons.Default.VisibilityOff to ArcaneCyan to "Elusive")
            }
            if (unit.keywords.contains(Keyword.TOUGH) || unit.keywords.contains(Keyword.REGENERATION)) {
                add(Icons.Default.Shield to EmeraldHeal to "Tough/Regen")
            }
        }
    }

    Box(
        modifier = modifier
            .alpha(if (isDragging) 0.25f else 1f)
            .graphicsLayer {
                scaleX = spawnScale.value
                scaleY = spawnScale.value
            }
            .size(width = tileWidth, height = tileHeight)
            .minimumInteractiveComponentSize()
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
        // Carved Metallic / Gold Ornate Frame Body
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 3.dp),
            shape = RoundedCornerShape(cornerRadius),
            color = if (unit.isFrostbitten) Color(0xFF2A4E7A) else Color(0xFF223654),
            border = BorderStroke(
                width = if (damageDelta > 0 || isSelected || isAttacking || isChampion) 2.2.dp else 1.6.dp,
                brush = frameBrush
            ),
            shadowElevation = if (isSelected || isAttacking || isChampion) 6.dp else 3.dp
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Full-bleed Character Portrait
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(regionColor.copy(alpha = 0.44f), ObsidianBg)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (portraitRes != null) {
                        Image(
                            painter = painterResource(id = portraitRes),
                            contentDescription = unit.template.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = when {
                                unit.template.type == CardType.LANDMARK -> Icons.Default.HourglassBottom
                                isChampion -> Icons.Default.Star
                                else -> Icons.Default.SportsKabaddi
                            },
                            contentDescription = unit.template.name,
                            tint = if (isChampion) HextechGoldBright else regionColor,
                            modifier = Modifier.size(tileWidth * 0.44f)
                        )
                    }

                    // Top + Bottom edge gradient scrims so top keywords and bottom ATK/HP pop cleanly
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 0.16f),
                                        Color.Transparent,
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.48f)
                                    )
                                )
                            )
                    )
                }

                // Bottom-Left Orange ATK (#FFA500) & Bottom-Right Crimson HP (#FF4500)
                if (unit.template.type != CardType.LANDMARK) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(start = tileWidth * 0.05f, bottom = tileWidth * 0.05f)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(LegendaryAmber, Color(0xFFB45309))
                                )
                            )
                            .border(1.dp, Color(0xFFFDE68A), RoundedCornerShape(4.dp))
                            .padding(horizontal = tileWidth * 0.09f, vertical = 1.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${unit.strikePower}",
                            style = tabularStatStyle(
                                fontSize = statTextSize,
                                color = if (unit.isFrostbitten) Color(0xFFBFDBFE) else ParchmentWhite
                            )
                        )
                    }

                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = tileWidth * 0.05f, bottom = tileWidth * 0.05f)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(CrimsonNexus, Color(0xFF991B1B))
                                )
                            )
                            .border(1.dp, Color(0xFFFCA5A5), RoundedCornerShape(4.dp))
                            .padding(horizontal = tileWidth * 0.09f, vertical = 1.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${unit.health}",
                            style = tabularStatStyle(
                                fontSize = statTextSize,
                                color = if (unit.health < unit.maxHealth) HextechGoldBright else ParchmentWhite
                            )
                        )
                    }
                }
            }
        }

        // Top-Edge Gold Champion Crest / Landmark Countdown / Compact Keyword Medallions
        if (unit.template.type == CardType.LANDMARK) {
            Surface(
                modifier = Modifier.align(Alignment.TopCenter),
                shape = RoundedCornerShape(5.dp),
                color = Color(0xEE181B24),
                border = BorderStroke(1.2.dp, HextechGoldBright),
                shadowElevation = 4.dp
            ) {
                Text(
                    text = "${unit.countdownRemaining}",
                    style = tabularStatStyle(statTextSize, ParchmentWhite),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                )
            }
        } else if (activeKeywords.isNotEmpty() || isChampion) {
            val iconSize = (tileWidth * 0.21f).coerceIn(10.dp, 15.dp)
            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .clip(RoundedCornerShape(7.dp))
                    .background(Color(0xE81C2942))
                    .border(
                        width = 1.dp,
                        color = if (isChampion) HextechGoldBright else Color(0xFFA0844D),
                        shape = RoundedCornerShape(7.dp)
                    )
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isChampion) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = if (unit.isLeveledUp) "Leveled Up Champion" else "Champion",
                        tint = HextechGoldBright,
                        modifier = Modifier.size(iconSize)
                    )
                }
                activeKeywords.take(3).forEach { (pair, label) ->
                    val (icon, tint) = pair
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = tint,
                        modifier = Modifier.size(iconSize)
                    )
                }
            }
        }

        // Floating Damage Impact Pill
        AnimatedVisibility(
            visible = damageDelta > 0,
            enter = fadeIn(tween(120)),
            exit = fadeOut(tween(240)),
            modifier = Modifier
                .align(Alignment.Center)
        ) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = CrimsonNexus,
                border = BorderStroke(1.dp, ParchmentWhite),
                shadowElevation = 4.dp
            ) {
                Text(
                    text = "-$damageDelta",
                    style = tabularStatStyle((statFontSize.value * 0.9f).coerceAtLeast(10f).sp, ParchmentWhite),
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                )
            }
        }
    }
}

@Composable
fun RunDeckDialog(
    runDeck: List<DeckCardEntry>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ObsidianSurface,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Run Deck (${runDeck.sumOf { it.copies }} Cards)",
                    style = MaterialTheme.typography.headlineSmall,
                    color = HextechGoldBright
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close Deck", tint = MutedSilver)
                }
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(runDeck, key = { it.cardId }) { entry ->
                    val card = CardCatalog.getCard(entry.cardId)
                    val items = entry.attachedItemIds.map { CardCatalog.getItem(it) }
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "${entry.copies}x ${card.name}",
                            style = MaterialTheme.typography.labelLarge,
                            color = ParchmentWhite
                        )
                        CardTile(
                            template = card,
                            effectiveCost = maxOf(0, card.cost - items.sumOf { it.costReduction }),
                            bonusPower = items.sumOf { it.bonusPower },
                            bonusHealth = items.sumOf { it.bonusHealth },
                            attachedItems = items,
                            compact = false
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = HextechGoldBright)
            }
        }
    )
}

@Composable
fun KeywordGlossaryDialog(onDismiss: () -> Unit) {
    var selectedCategory by remember { mutableStateOf<KeywordCategory?>(null) }
    val filteredKeywords = remember(selectedCategory) {
        if (selectedCategory == null) Keyword.entries
        else Keyword.entries.filter { it.category == selectedCategory }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ObsidianSurface,
        title = {
            Text(
                text = "Keywords & Rules Codex",
                style = MaterialTheme.typography.headlineSmall,
                color = HextechGoldBright
            )
        },
        text = {
            Column(modifier = Modifier.heightIn(max = 450.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterTag("All", selectedCategory == null) { selectedCategory = null }
                    FilterTag("Positive", selectedCategory == KeywordCategory.COMBAT_POSITIVE) {
                        selectedCategory = KeywordCategory.COMBAT_POSITIVE
                    }
                    FilterTag("Status", selectedCategory == KeywordCategory.NEGATIVE_STATUS) {
                        selectedCategory = KeywordCategory.NEGATIVE_STATUS
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filteredKeywords, key = { it.name }) { kw ->
                        Column {
                            Text(
                                text = kw.displayName + if (kw.isStackable) " (Stackable)" else "",
                                style = MaterialTheme.typography.titleSmall,
                                color = when (kw.category) {
                                    KeywordCategory.COMBAT_POSITIVE -> ArcaneCyan
                                    KeywordCategory.NEGATIVE_STATUS -> CrimsonNexus
                                    KeywordCategory.CARD_PROPERTY -> HextechGoldBright
                                }
                            )
                            Text(
                                text = kw.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MutedSilver
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            HorizontalDivider(color = SlateBorder)
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "State Conditions",
                            style = MaterialTheme.typography.titleSmall,
                            color = HextechGoldBright
                        )
                    }
                    items(StateCondition.entries, key = { it.name }) { sc ->
                        Column {
                            Text(
                                text = sc.displayName,
                                style = MaterialTheme.typography.labelLarge,
                                color = EmeraldHeal
                            )
                            Text(
                                text = sc.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MutedSilver
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = HextechGoldBright)
            }
        }
    )
}

@Composable
private fun FilterTag(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .minimumInteractiveComponentSize()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = if (selected) HextechGold.copy(alpha = 0.25f) else ObsidianBg,
        border = BorderStroke(1.dp, if (selected) HextechGoldBright else SlateBorder)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) HextechGoldBright else MutedSilver,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}
