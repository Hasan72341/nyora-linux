package com.nyora.linux.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Future Modern 2026 Palette — neutral DARK base
private val primaryColor    = Color(0xFFE63946) // Vibrant Red
private val surfaceColor    = Color(0xFF16161C) // Slightly elevated dark (surface1)
private val backgroundColor = Color(0xFF0E0E12) // Neutral dark base

/**
 * The visual appearance modes Nyora supports.
 *
 * - [DARK]  : a tasteful neutral dark scheme (~#0E0E12 background, slightly elevated
 *             surfaces — NOT pure black), the professional default.
 * - [LIGHT] : a tasteful light scheme.
 */
enum class AppearanceMode { DARK, LIGHT }

/**
 * The shared named Nyora color schemes (ported from nyora-android). Each scheme carries
 * BOTH a light and a dark primary; the active [AppearanceMode] selects which one becomes
 * the Material3 `primary`/accent via [primary]. The resolved color is also surfaced via
 * [LocalNyoraAccent] so the DesignSystem brushes/glows recolor live.
 *
 * Picker order: Dynamic first (the default), then the named anime schemes.
 *
 * [SYSTEM] ("Dynamic") follows the desktop/wallpaper accent colour (read via [LinuxNative]),
 * falling back to red when the desktop accent cannot be determined.
 *
 * - [lightPrimary]/[darkPrimary] : the Material primary for the light/dark appearance.
 * - [darkSecondary]              : the muted preview-bar tone for the picker card.
 * - [label]                      : the human-facing scheme name.
 */
enum class Accent(
    val lightPrimary: Color,
    val darkPrimary: Color,
    val darkSecondary: Color,
    val label: String,
) {
    SYSTEM(
        lightPrimary  = LinuxNative.accentColor ?: Color(0xFFE63946),
        darkPrimary   = LinuxNative.accentColor ?: Color(0xFFE63946),
        darkSecondary = (LinuxNative.accentColor ?: Color(0xFFE63946)).copy(alpha = 0.55f),
        label         = "Dynamic",
    ),
    TOTORO(Color(0xFF3C6090), Color(0xFFA6C8FF), Color(0xFFBCC7DC), "Totoro"),
    MIKU  (Color(0xFF00696D), Color(0xFF6FDDE2), Color(0xFFA6CECF), "Miku"),
    ASUKA (Color(0xFF904A40), Color(0xFFFFB4A8), Color(0xFFE7BDB6), "Asuka"),
    MION  (Color(0xFF3B693A), Color(0xFFA1D39A), Color(0xFFEEBF6D), "Mion"),
    RIKKA (Color(0xFF68548D), Color(0xFFD3BBFD), Color(0xFFCDC2DB), "Rikka"),
    SAKURA(Color(0xFF8C4A60), Color(0xFFFFB1C8), Color(0xFFE3BDC6), "Sakura"),
    MAMIMI(Color(0xFF465D91), Color(0xFFAFC6FF), Color(0xFFBFC6DC), "Mamimi"),
    KANADE(Color(0xFF353543), Color(0xFFFFFFFF), Color(0xFFDDDCDC), "Kanade"),
    ITSUKA(Color(0xFF974800), Color(0xFFFFBA8F), Color(0xFFF7B993), "Itsuka"),
    YUKI  (Color(0xFF43474A), Color(0xFFFFFFFF), Color(0xFFC6C6C9), "Yuki");

    /** The Material primary for the active appearance (light vs dark). */
    fun primary(appearance: AppearanceMode): Color =
        if (appearance == AppearanceMode.LIGHT) lightPrimary else darkPrimary

    /**
     * Back-compat accessor mapped to [darkPrimary] (the app default appearance is DARK).
     * Retained so the decorative `LocalNyoraAccent.current.color` call sites keep compiling.
     */
    val color: Color get() = darkPrimary
}

/**
 * Live accent exposed to the composition so downstream design tokens (gradients,
 * glows) can recolor reactively. Defaults to [Accent.SYSTEM] (the wallpaper accent).
 */
val LocalNyoraAccent = staticCompositionLocalOf { Accent.SYSTEM }

// Neutral dark scheme (NOT pure black), parameterized by accent.
fun nyoraDarkColorScheme(accent: Color): ColorScheme = darkColorScheme(
    primary         = accent,
    secondary       = Color(0xFFF1FAEE),
    tertiary        = Color(0xFFA8DADC),
    background      = Color(0xFF0E0E12),
    surface         = Color(0xFF16161C),
    onPrimary       = Color.White,
    onBackground    = Color(0xFFF1FAEE),
    onSurface       = Color(0xFFF1FAEE),
    surfaceVariant  = Color(0xFF1F1F26),
)

// Tasteful light scheme, parameterized by accent.
fun nyoraLightColorScheme(accent: Color): ColorScheme = lightColorScheme(
    primary         = accent,
    secondary       = Color(0xFF457B9D),
    tertiary        = Color(0xFF1D3557),
    background      = Color(0xFFFAFAFA),
    surface         = Color(0xFFFFFFFF),
    onPrimary       = Color.White,
    onBackground    = Color(0xFF1A1A1C),
    onSurface       = Color(0xFF1A1A1C),
    surfaceVariant  = Color(0xFFEDEDF0),
)

/**
 * Backward-compatible dark scheme using the default [primaryColor].
 * Retained so existing references keep compiling.
 */
val NyoraDarkColors: ColorScheme = nyoraDarkColorScheme(primaryColor)

// Hyper-Modern 24px (ROUND_TWELVE) Shapes
val NyoraShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small      = RoundedCornerShape(12.dp),
    medium     = RoundedCornerShape(16.dp),
    large      = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

@Composable
fun NyoraTheme(
    appearance: AppearanceMode = AppearanceMode.DARK,
    accent: Accent = Accent.SYSTEM,
    content: @Composable () -> Unit,
) {
    val colorScheme = when (appearance) {
        AppearanceMode.DARK  -> nyoraDarkColorScheme(accent.primary(AppearanceMode.DARK))
        AppearanceMode.LIGHT -> nyoraLightColorScheme(accent.primary(AppearanceMode.LIGHT))
    }
    // Push the theme-reactive palette into NyoraTokens so the ~397 token call sites recolor
    // on light/dark swap. SideEffect runs after a successful composition, so the snapshot
    // writes here are applied outside composition and safely invalidate token readers.
    androidx.compose.runtime.SideEffect {
        NyoraTokens.applyPalette(if (appearance == AppearanceMode.LIGHT) LightPalette else DarkPalette)
    }
    CompositionLocalProvider(LocalNyoraAccent provides accent) {
        MaterialTheme(
            colorScheme = colorScheme,
            shapes      = NyoraShapes,
            content     = content,
        )
    }
}
