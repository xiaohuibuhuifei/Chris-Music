package com.ruchu.player.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ===== Extended colors for app-specific components =====
data class ExtendedColors(
    val playerGradientTop: Color,
    val playerGradientBottom: Color,
    val vinylOuter: Color,
    val vinylInner: Color,
    val vinylGroove: Color,
    val lyricsOverlay: Color,
    val quoteGradientStart: Color,
    val quoteGradientEnd: Color,
)

private val DarkExtended = ExtendedColors(
    playerGradientTop = DarkPlayerGradientTop,
    playerGradientBottom = DarkPlayerGradientBottom,
    vinylOuter = DarkVinylOuter,
    vinylInner = DarkVinylInner,
    vinylGroove = DarkVinylGroove,
    lyricsOverlay = DarkLyricsOverlay,
    quoteGradientStart = DarkQuoteGradientStart,
    quoteGradientEnd = DarkQuoteGradientEnd,
)

private val LightExtended = ExtendedColors(
    playerGradientTop = LightPlayerGradientTop,
    playerGradientBottom = LightPlayerGradientBottom,
    vinylOuter = LightVinylOuter,
    vinylInner = LightVinylInner,
    vinylGroove = LightVinylGroove,
    lyricsOverlay = LightLyricsOverlay,
    quoteGradientStart = LightQuoteGradientStart,
    quoteGradientEnd = LightQuoteGradientEnd,
)

val LocalExtendedColors = staticCompositionLocalOf { DarkExtended }

// ===== Material color schemes =====
private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    secondary = DarkSecondary,
    onSecondary = DarkOnPrimary,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
)

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    secondary = LightSecondary,
    onSecondary = LightOnPrimary,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
)

@Composable
fun RuChuTheme(
    content: @Composable () -> Unit
) {
    val darkTheme = isSystemInDarkTheme()
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val extended = if (darkTheme) DarkExtended else LightExtended

    CompositionLocalProvider(LocalExtendedColors provides extended) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

object RuChuTheme {
    val extended: ExtendedColors
        @Composable
        @ReadOnlyComposable
        get() = LocalExtendedColors.current
}
