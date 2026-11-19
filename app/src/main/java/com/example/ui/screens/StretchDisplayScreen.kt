package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.ui.components.FilmGrainOverlay
import com.example.ui.theme.ArcaneCyan
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
import com.example.viewmodel.StretchExercise
import com.example.viewmodel.StretchViewModel

@Composable
fun StretchDisplayScreen(
    viewModel: StretchViewModel = viewModel(),
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val exercise by viewModel.currentExercise.collectAsStateWithLifecycle()

    if (onBack != null) {
        BackHandler(onBack = onBack)
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF325688), ObsidianBg)
                )
            )
            .testTag("stretch_display_screen"),
        contentAlignment = Alignment.Center
    ) {
        val isWide = maxWidth >= 600.dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 1040.dp)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (onBack != null) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("stretch_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.stretch_back_cd),
                                tint = HextechGoldBright
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.SelfImprovement,
                        contentDescription = null,
                        tint = ArcaneCyan,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = stringResource(R.string.stretch_screen_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = HextechGoldBright
                    )
                }

                Button(
                    onClick = { viewModel.getRandomExercise() },
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("next_stretch_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ArcaneCyan,
                        contentColor = DarkButtonInk
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.stretch_next_random),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            AnimatedContent(
                targetState = exercise,
                transitionSpec = {
                    fadeIn(tween(220)) togetherWith fadeOut(tween(180))
                },
                modifier = Modifier.weight(1f),
                label = "stretch_exercise_transition"
            ) { activeExercise ->
                if (isWide) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        StretchVisualCard(
                            exercise = activeExercise,
                            modifier = Modifier
                                .weight(0.46f)
                                .fillMaxHeight()
                        )
                        StretchInstructionsCard(
                            exercise = activeExercise,
                            modifier = Modifier
                                .weight(0.54f)
                                .fillMaxHeight()
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StretchVisualCard(
                            exercise = activeExercise,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(4f / 3f)
                        )
                        StretchInstructionsCard(
                            exercise = activeExercise,
                            enableScroll = false,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
        FilmGrainOverlay(alpha = 0.045f, modifier = Modifier.fillMaxSize())
    }
}

@Composable
private fun StretchVisualCard(
    exercise: StretchExercise,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = ObsidianSurface,
        border = BorderStroke(1.5.dp, HextechGold.copy(alpha = 0.7f)),
        shadowElevation = 6.dp
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = exercise.imageRes),
                contentDescription = exercise.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("stretch_image")
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.14f),
                                Color.Transparent,
                                ObsidianBg.copy(alpha = 0.68f)
                            )
                        )
                    )
            )

            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = ObsidianCard.copy(alpha = 0.9f),
                    border = BorderStroke(1.dp, ArcaneCyan.copy(alpha = 0.75f))
                ) {
                    Text(
                        text = exercise.targetArea,
                        style = MaterialTheme.typography.labelMedium,
                        color = ArcaneCyan,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = ObsidianCard.copy(alpha = 0.9f),
                    border = BorderStroke(1.dp, HextechGoldBright.copy(alpha = 0.75f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = HextechGoldBright,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "${exercise.durationSeconds}s",
                            style = MaterialTheme.typography.labelMedium,
                            color = HextechGoldBright,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StretchInstructionsCard(
    exercise: StretchExercise,
    enableScroll: Boolean = true,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = ObsidianSurfaceElevated,
        border = BorderStroke(1.2.dp, SlateBorder),
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .then(
                    if (enableScroll) {
                        Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    } else {
                        Modifier.fillMaxWidth()
                    }
                )
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = exercise.name,
                style = MaterialTheme.typography.headlineSmall,
                color = HextechGoldBright,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.testTag("stretch_title")
            )

            Text(
                text = stringResource(R.string.stretch_instructions_header),
                style = MaterialTheme.typography.labelLarge,
                color = ArcaneCyan
            )

            Column(
                modifier = Modifier.testTag("stretch_instructions"),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                exercise.instructions.forEachIndexed { index, step ->
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(HextechGold.copy(alpha = 0.22f))
                                .border(1.dp, HextechGoldBright, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${index + 1}",
                                style = MaterialTheme.typography.labelMedium,
                                color = HextechGoldBright,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = step,
                            style = MaterialTheme.typography.bodyMedium,
                            color = ParchmentWhite,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = EmeraldHeal.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, EmeraldHeal.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Air,
                        contentDescription = null,
                        tint = EmeraldHeal,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = exercise.breathingTip,
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedSilver
                    )
                }
            }
        }
    }
}
