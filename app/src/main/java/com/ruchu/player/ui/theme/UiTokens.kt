package com.ruchu.player.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class UiSpacingTokens(
    val xxs: Dp = 4.dp,
    val xs: Dp = 8.dp,
    val sm: Dp = 12.dp,
    val md: Dp = 16.dp,
    val lg: Dp = 20.dp,
    val xl: Dp = 24.dp,
    val xxl: Dp = 32.dp
)

data class UiRadiusTokens(
    val sm: Dp = 8.dp,
    val md: Dp = 12.dp,
    val lg: Dp = 16.dp,
    val pill: Dp = 20.dp
)

data class UiElevationTokens(
    val level0: Dp = 0.dp,
    val level1: Dp = 2.dp,
    val level2: Dp = 4.dp
)

data class UiOpacityTokens(
    val faint: Float = 0.06f,
    val subtle: Float = 0.12f,
    val medium: Float = 0.2f,
    val strong: Float = 0.42f,
    val overlay: Float = 0.92f
)

data class UiIconSizeTokens(
    val sm: Dp = 16.dp,
    val md: Dp = 22.dp,
    val lg: Dp = 26.dp,
    val xl: Dp = 32.dp
)

data class UiTouchTargetTokens(
    val compact: Dp = 40.dp,
    val regular: Dp = 44.dp,
    val large: Dp = 48.dp,
    val primary: Dp = 64.dp
)

data class UiTokens(
    val spacing: UiSpacingTokens = UiSpacingTokens(),
    val radius: UiRadiusTokens = UiRadiusTokens(),
    val elevation: UiElevationTokens = UiElevationTokens(),
    val opacity: UiOpacityTokens = UiOpacityTokens(),
    val icon: UiIconSizeTokens = UiIconSizeTokens(),
    val touch: UiTouchTargetTokens = UiTouchTargetTokens()
)
