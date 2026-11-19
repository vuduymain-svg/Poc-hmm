package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.SportsKabaddi
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.engine.CombatPhase
import com.example.engine.CombatState
import com.example.engine.PriorityHolder
import com.example.ui.theme.ArcaneCyan
import com.example.ui.theme.CinzelFontFamily
import com.example.ui.theme.CrimsonNexus
import com.example.ui.theme.DarkButtonInk
import com.example.ui.theme.HextechGold
import com.example.ui.theme.HextechGoldBright
import com.example.ui.theme.HextechGoldDeep
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.LegendaryAmber
import com.example.ui.theme.ManaBlue
import com.example.ui.theme.MutedSilver
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ParchmentWhite
import com.example.ui.theme.SpellManaCyan
import kotlin.math.cos
import kotlin.math.sin

val StatDropShadow = Shadow(
    color = Color.Black.copy(alpha = 0.92f),
    offset = Offset(0f, 3f),
    blurRadius = 6f
)

fun tabularStatStyle(fontSize: TextUnit, color: Color = ParchmentWhite): TextStyle = TextStyle(
    fontFamily = JetBrainsMonoFamily,
    fontWeight = FontWeight.ExtraBold,
    fontSize = fontSize,
    color = color,
    fontFeatureSettings = "tnum",
    shadow = StatDropShadow
)

fun fantasyHeadingStyle(
    fontSize: TextUnit,
    color: Color = ParchmentWhite,
    withShadow: Boolean = true
): TextStyle = TextStyle(
    fontFamily = CinzelFontFamily,
    fontWeight = FontWeight.Bold,
    fontSize = fontSize,
    color = color,
    letterSpacing = 0.6.sp,
    shadow = if (withShadow) StatDropShadow else null
)

fun interBodyStyle(fontSize: TextUnit, color: Color = ParchmentWhite): TextStyle = TextStyle(
    fontFamily = InterFontFamily,
    fontWeight = FontWeight.Medium,
    fontSize = fontSize,
    color = color
)

/**
 * Subtle physical film grain / noise overlay (5–10% opacity) using SrcOver so it works reliably
 * across all Android GPU/software canvases without BlendMode.Overlay compatibility crashes.
 */
@Composable
fun FilmGrainOverlay(
    alpha: Float = 0.06f,
    modifier: Modifier = Modifier
) {
    val noiseBrush = remember {
        val size = 96
        val pixels = IntArray(size * size)
        var seed = 0x5F3759DF
        for (i in pixels.indices) {
            seed = seed * 1664525 + 1013904223
            val raw = (seed ushr 24) and 0xFF
            val isLight = raw >= 128
            val dist = if (isLight) raw - 128 else 128 - raw
            val pxAlpha = (dist * 2).coerceIn(0, 255)
            val lum = if (isLight) 248 else 8
            pixels[i] = android.graphics.Color.argb(pxAlpha, lum, lum, lum)
        }
        val bmp = Bitmap.createBitmap(pixels, size, size, Bitmap.Config.ARGB_8888).asImageBitmap()
        ShaderBrush(ImageShader(bmp, TileMode.Repeated, TileMode.Repeated))
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        if (size.width <= 1f || size.height <= 1f) return@Canvas
        drawRect(
            brush = noiseBrush,
            alpha = alpha.coerceIn(0f, 0.2f)
        )
    }
}

/**
 * Shared LoR Dark & Metallic Obsidian-Navy gradient backdrop with subtle corner glow & film grain
 * so non-combat screens share the same tactile richness as the combat arena.
 */
@Composable
fun LorScreenBackdrop(
    modifier: Modifier = Modifier,
    accentGlow: Color = ManaBlue,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0E182C),
                        ObsidianBg,
                        Color(0xFF070C17)
                    )
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            if (w <= 1f || h <= 1f) return@Canvas
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(accentGlow.copy(alpha = 0.16f), Color.Transparent),
                    center = Offset(w * 0.12f, h * 0.10f),
                    radius = w * 0.65f
                ),
                radius = w * 0.65f,
                center = Offset(w * 0.12f, h * 0.10f)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(HextechGold.copy(alpha = 0.10f), Color.Transparent),
                    center = Offset(w * 0.90f, h * 0.88f),
                    radius = w * 0.60f
                ),
                radius = w * 0.60f,
                center = Offset(w * 0.90f, h * 0.88f)
            )
        }
        content()
        FilmGrainOverlay(alpha = 0.045f, modifier = Modifier.fillMaxSize())
    }
}

/**
 * Full-bleed LoR Dark & Metallic Obsidian Stone Battlefield (#0A111F) with textured stone slabs,
 * subtle corner arcane glows, bottom-up battlefield mist lighting, and glowing #D4AF37->#FFD700 gold centerline.
 */
@Composable
fun FreljordBattlefieldBackground(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBg)
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_board_stone_bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            alpha = 0.68f,
            modifier = Modifier.fillMaxSize()
        )

        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            if (w <= 1f || h <= 1f) return@Canvas
            val midY = h * 0.5f

            // 1. Deep Obsidian-Navy Atmospheric Scrim (#0A111F) keeping battlefield crisp & uncluttered
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x66162642),
                        Color(0x990D1629),
                        Color(0xE00A111F)
                    ),
                    center = Offset(w * 0.5f, midY),
                    radius = w * 0.72f
                )
            )

            // 2. Subtle Arcane Blue (#00BFFF) Corner Glows
            val cornerRadius = w * 0.36f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x3000BFFF), Color.Transparent),
                    center = Offset(0f, 0f),
                    radius = cornerRadius
                ),
                radius = cornerRadius,
                center = Offset(0f, 0f)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x2800BFFF), Color.Transparent),
                    center = Offset(w, 0f),
                    radius = cornerRadius
                ),
                radius = cornerRadius,
                center = Offset(w, 0f)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x2E00BFFF), Color.Transparent),
                    center = Offset(0f, h),
                    radius = cornerRadius
                ),
                radius = cornerRadius,
                center = Offset(0f, h)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x3600BFFF), Color.Transparent),
                    center = Offset(w, h),
                    radius = cornerRadius
                ),
                radius = cornerRadius,
                center = Offset(w, h)
            )

            // 3. Bottom-Up Mist & Upward Battlefield Illumination in Clash Zone
            drawOval(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color(0x2600BFFF),
                        Color(0x3B1D4E89),
                        Color.Transparent
                    ),
                    startY = h * 0.24f,
                    endY = h * 0.80f
                ),
                topLeft = Offset(w * 0.14f, h * 0.24f),
                size = Size(w * 0.72f, h * 0.56f)
            )

            // Subtle stone fracture runes in center clash zone
            val iceVeins = listOf(
                Offset(w * 0.25f, h * 0.34f) to Offset(w * 0.42f, h * 0.44f),
                Offset(w * 0.42f, h * 0.44f) to Offset(w * 0.36f, h * 0.57f),
                Offset(w * 0.58f, h * 0.31f) to Offset(w * 0.67f, h * 0.47f),
                Offset(w * 0.67f, h * 0.47f) to Offset(w * 0.78f, h * 0.39f),
                Offset(w * 0.31f, h * 0.66f) to Offset(w * 0.49f, h * 0.56f),
                Offset(w * 0.61f, h * 0.69f) to Offset(w * 0.73f, h * 0.58f)
            )
            iceVeins.forEach { (start, end) ->
                drawLine(
                    color = Color(0x2E00BFFF),
                    start = start,
                    end = end,
                    strokeWidth = 1.8f,
                    cap = StrokeCap.Round
                )
            }

            // 4. Top Carved Dark Stone Bench Rim (Enemy Territory)
            val topStoneHeight = h * 0.23f
            val topStonePath = Path().apply {
                moveTo(0f, 0f)
                lineTo(w, 0f)
                lineTo(w, topStoneHeight * 0.88f)
                lineTo(w * 0.84f, topStoneHeight * 0.96f)
                lineTo(w * 0.66f, topStoneHeight)
                lineTo(w * 0.34f, topStoneHeight)
                lineTo(w * 0.16f, topStoneHeight * 0.94f)
                lineTo(0f, topStoneHeight * 0.86f)
                close()
            }
            drawPath(
                path = topStonePath,
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xDD101829), Color(0xDD19253B), Color(0xCC121C2E))
                )
            )
            drawLine(
                color = Color(0x5542526E),
                start = Offset(w * 0.16f, topStoneHeight * 0.94f),
                end = Offset(w * 0.84f, topStoneHeight * 0.96f),
                strokeWidth = 2.4f
            )
            for (i in 1..7) {
                val bx = w * (0.14f + i * 0.105f)
                drawLine(
                    color = Color(0x50060B14),
                    start = Offset(bx, 0f),
                    end = Offset(bx - w * 0.01f, topStoneHeight * 0.95f),
                    strokeWidth = 2f
                )
            }
            drawLine(
                color = Color(0x66D4AF37),
                start = Offset(w * 0.22f, topStoneHeight * 0.72f),
                end = Offset(w * 0.78f, topStoneHeight * 0.72f),
                strokeWidth = 1.6f
            )

            // 5. Bottom Carved Dark Stone Bench Rim (Player Territory)
            val botStoneTop = h * 0.77f
            val botStonePath = Path().apply {
                moveTo(0f, h)
                lineTo(w, h)
                lineTo(w, botStoneTop + h * 0.03f)
                lineTo(w * 0.84f, botStoneTop + h * 0.01f)
                lineTo(w * 0.66f, botStoneTop)
                lineTo(w * 0.34f, botStoneTop)
                lineTo(w * 0.16f, botStoneTop + h * 0.015f)
                lineTo(0f, botStoneTop + h * 0.035f)
                close()
            }
            drawPath(
                path = botStonePath,
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xCC19253B), Color(0xDD121B2E), Color(0xEE0A111F))
                )
            )
            drawLine(
                color = Color(0x5542526E),
                start = Offset(w * 0.16f, botStoneTop + h * 0.015f),
                end = Offset(w * 0.84f, botStoneTop + h * 0.01f),
                strokeWidth = 2.4f
            )
            for (i in 1..7) {
                val bx = w * (0.14f + i * 0.105f)
                drawLine(
                    color = Color(0x50060B14),
                    start = Offset(bx, botStoneTop + h * 0.01f),
                    end = Offset(bx + w * 0.01f, h),
                    strokeWidth = 2f
                )
            }
            drawLine(
                color = Color(0x66D4AF37),
                start = Offset(w * 0.22f, botStoneTop + h * 0.06f),
                end = Offset(w * 0.78f, botStoneTop + h * 0.06f),
                strokeWidth = 1.6f
            )

            // 6. Left & Right Dark Obsidian Stone Flanking Ridges
            val leftGlacier = Path().apply {
                moveTo(0f, 0f)
                lineTo(w * 0.11f, 0f)
                lineTo(w * 0.085f, h * 0.24f)
                lineTo(w * 0.115f, midY)
                lineTo(w * 0.085f, h * 0.76f)
                lineTo(w * 0.12f, h)
                lineTo(0f, h)
                close()
            }
            drawPath(
                path = leftGlacier,
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xD80A111F), Color(0x8816233B), Color.Transparent)
                )
            )

            val rightGlacier = Path().apply {
                moveTo(w, 0f)
                lineTo(w * 0.89f, 0f)
                lineTo(w * 0.915f, h * 0.24f)
                lineTo(w * 0.885f, midY)
                lineTo(w * 0.915f, h * 0.76f)
                lineTo(w * 0.88f, h)
                lineTo(w, h)
                close()
            }
            drawPath(
                path = rightGlacier,
                brush = Brush.horizontalGradient(
                    colors = listOf(Color.Transparent, Color(0x8816233B), Color(0xD80A111F))
                )
            )

            // 7. Glowing Metallic-Gold Centerline (#D4AF37 -> #FFD700)
            val lineStartX = w * 0.075f
            val lineEndX = w * 0.915f

            drawLine(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0x18D4AF37),
                        Color(0x5EFFD700),
                        Color(0x5EFFD700),
                        Color(0x18D4AF37)
                    ),
                    startX = lineStartX,
                    endX = lineEndX
                ),
                start = Offset(lineStartX, midY),
                end = Offset(lineEndX, midY),
                strokeWidth = 14f,
                cap = StrokeCap.Round
            )

            drawLine(
                color = Color(0xCC070B12),
                start = Offset(lineStartX, midY + 3.2f),
                end = Offset(lineEndX, midY + 3.2f),
                strokeWidth = 5.5f
            )

            drawLine(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFFFD700), Color(0xFFD4AF37), Color(0xFF7D5C12)),
                    startY = midY - 3f,
                    endY = midY + 3f
                ),
                start = Offset(lineStartX, midY),
                end = Offset(lineEndX, midY),
                strokeWidth = 5f,
                cap = StrokeCap.Round
            )

            // Carved Golden Boss Medallion on left side of centerline
            val bossCenter = Offset(w * 0.195f, midY)
            val bossRadius = (h * 0.044f).coerceIn(14f, 26f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFFFD700), Color(0xFFD4AF37), Color(0xFF563B0E)),
                    center = bossCenter - Offset(bossRadius * 0.25f, bossRadius * 0.25f),
                    radius = bossRadius * 1.4f
                ),
                radius = bossRadius,
                center = bossCenter
            )
            drawCircle(
                color = Color(0xFF1D160B),
                radius = bossRadius,
                center = bossCenter,
                style = Stroke(width = 2.5f)
            )
            drawCircle(
                color = Color(0x99FFF3BA),
                radius = bossRadius * 0.72f,
                center = bossCenter,
                style = Stroke(width = 1.5f)
            )
        }
    }
}

/**
 * Faceted #FF4500 Crimson (Enemy) or #00BFFF Sapphire (Player) Nexus crystal inside an ornate #D4AF37->#FFD700 gold bezel
 * with attached HP nameplate displaying e.g. 16/20 or 45/45.
 */
@Composable
fun FacetedNexusCrystal(
    hp: Int,
    maxHp: Int,
    isEnemy: Boolean,
    hitDelta: Int,
    crystalSize: Dp,
    hpFontSize: TextUnit,
    hpTestTag: String? = null,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val primaryGem = if (isEnemy) Color(0xFFFF4500) else Color(0xFF00BFFF)
    val brightGem = if (isEnemy) Color(0xFFFF8C5A) else Color(0xFF7BE4FF)
    val deepGem = if (isEnemy) Color(0xFF8C1B00) else Color(0xFF0A4F8C)
    val glowColor = if (isEnemy) Color(0x66FF4500) else Color(0x6600BFFF)

    Box(
        modifier = modifier
            .then(if (hpTestTag != null) Modifier.testTag(hpTestTag) else Modifier)
            .then(
                if (onClick != null) Modifier
                    .minimumInteractiveComponentSize()
                    .clickable(onClick = onClick)
                else Modifier
            )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy((-6).dp)
        ) {
            // Faceted Gem inside Ornate Gold Bezel
            Box(
                modifier = Modifier.size(crystalSize),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    if (size.width <= 1f || size.height <= 1f) return@Canvas
                    val cx = size.width / 2f
                    val cy = size.height / 2f
                    val r = minOf(cx, cy)

                    // Outer glow on damage or steady aura
                    drawCircle(
                        color = if (hitDelta > 0) CrimsonNexus.copy(alpha = 0.80f) else glowColor,
                        radius = r * 1.05f,
                        center = Offset(cx, cy)
                    )

                    // Outer carved dark iron & gold bezel ring
                    drawCircle(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFFF6D77B), Color(0xFFB5862A), Color(0xFF543C11))
                        ),
                        radius = r * 0.92f,
                        center = Offset(cx, cy)
                    )
                    drawCircle(
                        color = Color(0xFF28354D),
                        radius = r * 0.78f,
                        center = Offset(cx, cy)
                    )

                    // Faceted octagonal/hexagonal crystal gem
                    val gemR = r * 0.74f
                    val points = List(6) { i ->
                        val angle = Math.toRadians((i * 60 - 90).toDouble())
                        Offset(
                            cx + (gemR * cos(angle)).toFloat(),
                            cy + (gemR * sin(angle)).toFloat()
                        )
                    }
                    val gemPath = Path().apply {
                        moveTo(points[0].x, points[0].y)
                        for (i in 1 until points.size) lineTo(points[i].x, points[i].y)
                        close()
                    }
                    drawPath(
                        path = gemPath,
                        brush = Brush.linearGradient(
                            colors = listOf(brightGem, primaryGem, deepGem),
                            start = Offset(cx - gemR, cy - gemR),
                            end = Offset(cx + gemR, cy + gemR)
                        )
                    )

                    // Top-left bright facet highlight
                    val topFacet = Path().apply {
                        moveTo(cx, cy)
                        lineTo(points[0].x, points[0].y)
                        lineTo(points[5].x, points[5].y)
                        close()
                    }
                    drawPath(path = topFacet, color = Color.White.copy(alpha = 0.36f))

                    // Bottom-right deep shadow facet
                    val bottomFacet = Path().apply {
                        moveTo(cx, cy)
                        lineTo(points[2].x, points[2].y)
                        lineTo(points[3].x, points[3].y)
                        close()
                    }
                    drawPath(path = bottomFacet, color = Color.Black.copy(alpha = 0.32f))

                    // Inner crown facet lines
                    val innerR = gemR * 0.44f
                    val innerPts = List(6) { i ->
                        val angle = Math.toRadians((i * 60 - 90).toDouble())
                        Offset(cx + (innerR * cos(angle)).toFloat(), cy + (innerR * sin(angle)).toFloat())
                    }
                    val innerPath = Path().apply {
                        moveTo(innerPts[0].x, innerPts[0].y)
                        for (i in 1 until innerPts.size) lineTo(innerPts[i].x, innerPts[i].y)
                        close()
                    }
                    drawPath(path = innerPath, color = brightGem.copy(alpha = 0.45f))
                    drawPath(path = innerPath, color = Color.White.copy(alpha = 0.45f), style = Stroke(1.2f))

                    for (i in 0 until 6) {
                        drawLine(
                            color = Color.White.copy(alpha = 0.30f),
                            start = innerPts[i],
                            end = points[i],
                            strokeWidth = 1.2f
                        )
                    }

                    // Gold bezel rim stroke
                    drawCircle(
                        color = Color(0xFFF3CE6B),
                        radius = r * 0.90f,
                        center = Offset(cx, cy),
                        style = Stroke(width = 2.2f)
                    )
                }
            }

            // Attached Dark-Obsidian & Gold HP Banner Plate
            Surface(
                shape = RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp, topStart = 3.dp, bottomStart = 3.dp),
                color = Color(0xF00A111F),
                border = BorderStroke(1.4.dp, if (hitDelta > 0) CrimsonNexus else HextechGold),
                shadowElevation = 6.dp
            ) {
                Text(
                    text = "$hp/$maxHp",
                    style = tabularStatStyle(
                        fontSize = hpFontSize,
                        color = if (hitDelta > 0) CrimsonNexus else ParchmentWhite
                    ),
                    maxLines = 1,
                    modifier = Modifier.padding(start = 8.dp, end = 7.dp, top = 2.dp, bottom = 2.dp)
                )
            }
        }

        // Hit delta popup badge
        AnimatedVisibility(
            visible = hitDelta > 0,
            enter = fadeIn(tween(120)),
            exit = fadeOut(tween(220)),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 6.dp, y = (-6).dp)
        ) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = CrimsonNexus,
                border = BorderStroke(1.dp, ParchmentWhite)
            ) {
                Text(
                    text = "-$hitDelta",
                    style = tabularStatStyle((hpFontSize.value * 0.78f).coerceAtLeast(10f).sp),
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                )
            }
        }
    }
}

/**
 * Square carved-gold scroll/history button placed on the left end of the golden centerline.
 */
@Composable
fun CenterlineScrollButton(
    size: Dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.92f else 1f,
        animationSpec = spring(),
        label = "scroll_btn_scale"
    )

    Surface(
        modifier = modifier
            .size(maxOf(42.dp, size))
            .minimumInteractiveComponentSize()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .testTag("combat_log_button")
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF1F170A),
        border = BorderStroke(2.dp, HextechGoldBright),
        shadowElevation = 6.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF4F3B17), Color(0xFF23190B))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.History,
                contentDescription = "Combat Log",
                tint = HextechGoldBright,
                modifier = Modifier.size(size * 0.56f)
            )
        }
    }
}

/**
 * Visual stacked deck pile beside the lower-left stone border with card count badge.
 */
@Composable
fun StackedDeckPile(
    cardCount: Int,
    deckWidth: Dp,
    deckHeight: Dp,
    fontSize: TextUnit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(width = deckWidth + 8.dp, height = deckHeight + 8.dp),
        contentAlignment = Alignment.Center
    ) {
        // Bottom 2 stacked card backs for depth
        if (cardCount > 2) {
            Box(
                modifier = Modifier
                    .offset(x = (-3).dp, y = 3.dp)
                    .size(width = deckWidth, height = deckHeight)
                    .clip(RoundedCornerShape(5.dp))
                    .background(Color(0xFF0E1729))
                    .border(1.dp, HextechGoldDeep, RoundedCornerShape(5.dp))
            )
        }
        if (cardCount > 1) {
            Box(
                modifier = Modifier
                    .offset(x = (-1.5).dp, y = 1.5.dp)
                    .size(width = deckWidth, height = deckHeight)
                    .clip(RoundedCornerShape(5.dp))
                    .background(Color(0xFF14223B))
                    .border(1.dp, HextechGold, RoundedCornerShape(5.dp))
            )
        }
        // Top card back with Metallic Runeterra Crest
        Box(
            modifier = Modifier
                .size(width = deckWidth, height = deckHeight)
                .clip(RoundedCornerShape(5.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF1C3359), Color(0xFF0C172B))
                    )
                )
                .border(1.4.dp, HextechGold, RoundedCornerShape(5.dp)),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize().padding(3.dp)) {
                if (size.width <= 1f || size.height <= 1f) return@Canvas
                val cx = size.width / 2f
                val cy = size.height / 2f
                val diamond = Path().apply {
                    moveTo(cx, cy - size.height * 0.30f)
                    lineTo(cx + size.width * 0.28f, cy)
                    lineTo(cx, cy + size.height * 0.30f)
                    lineTo(cx - size.width * 0.28f, cy)
                    close()
                }
                drawPath(path = diamond, color = Color(0x4500BFFF))
                drawPath(path = diamond, color = HextechGold, style = Stroke(1.4f))
            }
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = Color(0xE80A111F),
                border = BorderStroke(0.9.dp, HextechGoldBright)
            ) {
                Text(
                    text = "$cardCount",
                    style = tabularStatStyle(fontSize, HextechGoldBright),
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                )
            }
        }
    }
}

/**
 * Iconic Right-Edge Circular Pass/Action & Mechanical Mana Dial matching LoR Path of Champions.
 * Displays Enemy Mana (e.g. 3/10) + Spell Mana sockets on the top arc,
 * Attack Token sword icon docked on the upper-left shoulder,
 * Player Mana (e.g. 4/10) + segmented glowing blue crystal bars + 3 Spell Mana gems on the bottom arc,
 * and the tactile royal-blue circular PASS / ATTACK / BLOCK / OK button on the right centerline.
 */
@Composable
fun RightManaActionDial(
    combat: CombatState,
    dialWidth: Dp,
    dialHeight: Dp,
    actionDiameter: Dp,
    descFontSize: TextUnit,
    statFontSize: TextUnit,
    onEnterAttack: () -> Unit,
    onCancelAttack: () -> Unit,
    onCommitAttack: () -> Unit,
    onCommitBlock: () -> Unit,
    onPassTurn: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isPlayerPriority = combat.priorityHolder == PriorityHolder.PLAYER
    val pulseTransition = rememberInfiniteTransition(label = "dial_pulse")
    val pulseScale by pulseTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(950),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dial_btn_pulse"
    )

    val (btnLabel, btnTag, btnAction) = when (combat.phase) {
        CombatPhase.COMBAT_DECLARE_ATTACK_PHASE -> Triple(
            "ATTACK\n${combat.attackingUnitIds.size}",
            "commit_attack_button",
            onCommitAttack
        )
        CombatPhase.COMBAT_DECLARE_BLOCK_PHASE -> Triple(
            "BLOCK\n${combat.blockPairings.size}",
            "commit_block_button",
            onCommitBlock
        )
        CombatPhase.SPELL_STACK_PHASE -> Triple(
            if (isPlayerPriority) "OK" else "WAIT",
            "end_round_button",
            onPassTurn
        )
        else -> Triple(
            when {
                !isPlayerPriority -> "WAIT"
                combat.spellStack.isNotEmpty() -> "OK"
                combat.consecutivePasses >= 1 -> "END"
                else -> "PASS"
            },
            "end_round_button",
            onPassTurn
        )
    }

    val btnEnabled = when (combat.phase) {
        CombatPhase.COMBAT_DECLARE_ATTACK_PHASE -> isPlayerPriority && combat.attackingUnitIds.isNotEmpty()
        else -> isPlayerPriority
    }

    val btnInteraction = remember { MutableInteractionSource() }
    val btnPressed by btnInteraction.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (btnPressed) 0.93f else 1f,
        animationSpec = spring(),
        label = "dial_press"
    )

    val canInitiateAttack = combat.phase == CombatPhase.MAIN_ACTION_PHASE &&
        isPlayerPriority &&
        combat.playerHasAttackToken &&
        combat.playerBoard.any { it.canAttack }

    val showAttackTokenOnShoulder = combat.playerHasAttackToken || combat.enemyHasAttackToken

    Box(
        modifier = modifier.size(width = dialWidth, height = dialHeight),
        contentAlignment = Alignment.CenterEnd
    ) {
        // Mechanical Curved Dark-Stone & Gold Housing behind the left side of the dial
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (size.width <= 1f || size.height <= 1f) return@Canvas
            val btnRadius = actionDiameter.toPx() * 0.5f
            val center = Offset(size.width - btnRadius - 6.dp.toPx(), size.height * 0.5f)
            val outerR = minOf(size.height * 0.48f, btnRadius * 2.25f)
            val innerR = btnRadius * 0.96f

            // Dark-obsidian mechanical crescent wrapping left of the center button
            val housingPath = Path().apply {
                addArc(
                    oval = androidx.compose.ui.geometry.Rect(
                        center.x - outerR * 1.18f,
                        center.y - outerR,
                        center.x + outerR * 0.35f,
                        center.y + outerR
                    ),
                    startAngleDegrees = 90f,
                    sweepAngleDegrees = 180f
                )
                lineTo(center.x, center.y - innerR)
                lineTo(center.x, center.y + innerR)
                close()
            }
            drawPath(
                path = housingPath,
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF131D30), Color(0xFF1E2D47), Color(0xFF0A111F))
                )
            )
            drawPath(
                path = housingPath,
                color = HextechGold,
                style = Stroke(width = 2.4.dp.toPx())
            )
            drawPath(
                path = housingPath,
                color = Color(0xFF070C15),
                style = Stroke(width = 1.dp.toPx())
            )

            // Horizontal gold centerline accent feeding into the dial
            drawLine(
                color = HextechGold,
                start = Offset(0f, center.y),
                end = Offset(center.x - btnRadius, center.y),
                strokeWidth = 3.dp.toPx()
            )
        }

        // TOP ARC: Enemy Mana Counter (e.g. 3/10) + Segmented Enemy Pips + 3 Spell Mana Sockets
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = actionDiameter * 0.42f, top = dialHeight * 0.045f),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            // 3 Enemy Spell Mana Sockets tucked along upper curve + spell mana count
            Row(
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${combat.enemySpellMana}",
                    style = tabularStatStyle(
                        (descFontSize.value * 0.82f).coerceAtLeast(9f).sp,
                        if (combat.enemySpellMana > 0) ArcaneCyan else MutedSilver
                    )
                )
                repeat(3) { idx ->
                    val filled = idx < combat.enemySpellMana
                    Box(
                        modifier = Modifier
                            .size(9.dp)
                            .clip(CircleShape)
                            .background(if (filled) SpellManaCyan else Color(0xFF1B2E47))
                            .border(1.dp, if (filled) ArcaneCyan else HextechGold.copy(alpha = 0.72f), CircleShape)
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "${combat.enemyMana}/${combat.enemyMaxMana}",
                    style = tabularStatStyle(
                        fontSize = (statFontSize.value * 0.92f).coerceIn(11f, 15.5f).sp,
                        color = ParchmentWhite
                    ),
                    modifier = Modifier.testTag("enemy_mana_text")
                )
                // Segmented Enemy Mana Crystals
                Row(horizontalArrangement = Arrangement.spacedBy(1.5.dp)) {
                    val maxEnemy = combat.enemyMaxMana.coerceIn(1, 10)
                    repeat(maxEnemy) { idx ->
                        val filled = idx < combat.enemyMana
                        Box(
                            modifier = Modifier
                                .width(3.8.dp)
                                .height(9.5.dp)
                                .clip(RoundedCornerShape(1.5.dp))
                                .background(if (filled) CrimsonNexus else Color(0xFF1F324D))
                                .border(0.6.dp, if (filled) Color(0xFFFDA4AF) else Color(0xFF5C7599), RoundedCornerShape(1.5.dp))
                        )
                    }
                }
            }
        }

        // UPPER-LEFT SHOULDER: Glowing Orange-Gold Attack Token Sword Icon (or Cancel Attack)
        if (combat.phase == CombatPhase.COMBAT_DECLARE_ATTACK_PHASE) {
            Surface(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset(x = 4.dp, y = -dialHeight * 0.20f)
                    .size(48.dp)
                    .clickable(enabled = isPlayerPriority, onClick = onCancelAttack),
                shape = CircleShape,
                color = Color(0xFF28181D),
                border = BorderStroke(1.8.dp, CrimsonNexus),
                shadowElevation = 6.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cancel Attack",
                        tint = ParchmentWhite,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        } else if (showAttackTokenOnShoulder) {
            val isPlayerToken = combat.playerHasAttackToken
            val swordBg = if (isPlayerToken) {
                Brush.radialGradient(colors = listOf(Color(0xFFFFD56B), LegendaryAmber, Color(0xFFB45309)))
            } else {
                Brush.radialGradient(colors = listOf(Color(0xFFFDA4AF), CrimsonNexus, Color(0xFF7F1D1D)))
            }
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset(x = 2.dp, y = -dialHeight * 0.21f)
                    .size(48.dp)
                    .then(
                        if (canInitiateAttack) {
                            Modifier
                                .testTag("enter_attack_button")
                                .clickable(onClick = onEnterAttack)
                        } else {
                            Modifier
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(swordBg)
                        .border(2.dp, HextechGoldBright, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SportsKabaddi,
                        contentDescription = if (isPlayerToken) "Your Attack Token" else "Enemy Attack Token",
                        tint = Color(0xFF1F1306),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // BOTTOM ARC: Player Mana Counter (e.g. 4/10) + Segmented Blue Crystal Bars + 3 Spell Mana Gems
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = actionDiameter * 0.42f, bottom = dialHeight * 0.045f),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "${combat.playerMana}/${combat.playerMaxMana}",
                    style = tabularStatStyle(
                        fontSize = (statFontSize.value * 1.02f).coerceIn(12f, 17f).sp,
                        color = ParchmentWhite
                    ),
                    modifier = Modifier.testTag("player_mana_text")
                )
                // Segmented Glowing Blue Crystal Bars
                Row(horizontalArrangement = Arrangement.spacedBy(1.5.dp)) {
                    val maxPips = combat.playerMaxMana.coerceIn(1, 10)
                    repeat(maxPips) { idx ->
                        val filled = idx < combat.playerMana
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(11.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(
                                    if (filled) Brush.verticalGradient(listOf(Color(0xFF93E5FF), ManaBlue, Color(0xFF2563EB)))
                                    else Brush.verticalGradient(listOf(Color(0xFF233754), Color(0xFF1A2A42)))
                                )
                                .border(
                                    0.7.dp,
                                    if (filled) Color(0xFFE0F2FE) else Color(0xFF5C7599),
                                    RoundedCornerShape(2.dp)
                                )
                        )
                    }
                }
            }

            // 3 Circular Spell Mana Gems tucked neatly underneath + always-visible spell mana count
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${combat.playerSpellMana}",
                    style = tabularStatStyle(
                        (descFontSize.value * 0.85f).coerceAtLeast(9.5f).sp,
                        if (combat.playerSpellMana > 0) ArcaneCyan else MutedSilver
                    )
                )
                repeat(3) { idx ->
                    val filled = idx < combat.playerSpellMana
                    Box(
                        modifier = Modifier
                            .size(11.dp)
                            .clip(CircleShape)
                            .background(
                                if (filled) Brush.radialGradient(listOf(Color(0xFFA5F3FC), SpellManaCyan, Color(0xFF0E7490)))
                                else Brush.radialGradient(listOf(Color(0xFF243A59), Color(0xFF182840)))
                            )
                            .border(
                                1.1.dp,
                                if (filled) Color(0xFFE0F2FE) else HextechGold.copy(alpha = 0.72f),
                                CircleShape
                            )
                    )
                }
            }
        }

        // RIGHT CENTERLINE: Prominent Burnished Gold/Brass Circular Pass/Action Button (#D4AF37 -> #FFD700)
        val buttonBg = if (btnEnabled) {
            Brush.radialGradient(
                colors = listOf(Color(0xFFFFEA8A), HextechGoldBright, HextechGold, HextechGoldDeep),
                center = Offset(0.35f, 0.28f)
            )
        } else {
            Brush.radialGradient(
                colors = listOf(Color(0xFF2B384F), Color(0xFF1A2436), Color(0xFF0E1522))
            )
        }

        // Outer Golden Aura Halo around active End Turn / Pass dial CTA
        if (btnEnabled) {
            Canvas(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 2.dp)
                    .size(maxOf(48.dp, actionDiameter) + 12.dp)
            ) {
                if (size.width <= 1f || size.height <= 1f) return@Canvas
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            HextechGoldBright.copy(alpha = 0.45f),
                            HextechGold.copy(alpha = 0.18f),
                            Color.Transparent
                        )
                    )
                )
            }
        }

        Surface(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 6.dp)
                .graphicsLayer {
                    val activePulse = if (btnEnabled) pulseScale else 1f
                    scaleX = activePulse * pressScale
                    scaleY = activePulse * pressScale
                }
                .size(maxOf(48.dp, actionDiameter))
                .minimumInteractiveComponentSize()
                .testTag(btnTag)
                .clickable(
                    enabled = btnEnabled,
                    interactionSource = btnInteraction,
                    indication = null,
                    onClick = btnAction
                ),
            shape = CircleShape,
            color = Color.Transparent,
            shadowElevation = if (btnEnabled) 14.dp else 4.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(HextechGoldBright, HextechGold, Color(0xFF5A410D))
                        )
                    )
                    .padding(3.dp)
                    .clip(CircleShape)
                    .background(buttonBg)
                    .border(
                        width = 1.4.dp,
                        color = if (btnEnabled) Color(0xCCFFF8D6) else Color(0x558C9BB0),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = btnLabel,
                    style = fantasyHeadingStyle(
                        fontSize = (descFontSize.value * 0.98f).coerceIn(10f, 14f).sp,
                        color = if (btnEnabled) DarkButtonInk else MutedSilver,
                        withShadow = !btnEnabled
                    ),
                    lineHeight = (descFontSize.value * 1.12f).sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
