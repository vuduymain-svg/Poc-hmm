package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val RuneterraDarkScheme = darkColorScheme(
    primary = HextechGold,
    onPrimary = DarkButtonInk,
    primaryContainer = HextechGoldDim,
    onPrimaryContainer = HextechGoldBright,
    secondary = ArcaneCyan,
    onSecondary = DarkButtonInk,
    secondaryContainer = ArcaneCyanDim,
    onSecondaryContainer = ArcaneCyan,
    tertiary = EpicViolet,
    onTertiary = ParchmentWhite,
    tertiaryContainer = EpicVioletDim,
    onTertiaryContainer = EpicViolet,
    background = ObsidianBg,
    onBackground = ParchmentWhite,
    surface = ObsidianSurface,
    onSurface = ParchmentWhite,
    surfaceVariant = ObsidianSurfaceElevated,
    onSurfaceVariant = MutedSilver,
    outline = SlateBorder,
    outlineVariant = SlateBorderHighlight,
    error = CrimsonNexus,
    onError = ParchmentWhite
)

@Composable
fun PathOfChampionsTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = RuneterraDarkScheme,
        typography = Typography,
        content = content
    )
}
