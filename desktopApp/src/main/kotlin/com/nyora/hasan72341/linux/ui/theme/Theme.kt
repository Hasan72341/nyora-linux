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
import com.materialkolor.PaletteStyle
import com.materialkolor.rememberDynamicColorScheme

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

/**
 * Derive the theme-reactive [NyoraPalette] (the surface/text/hairline ladder consumed by the
 * custom design tokens on ~397 call sites) from the Material You [scheme] generated by
 * MaterialKolor. This is what makes the dynamic accent cascade across EVERY screen, not just
 * stock Material components: surface1/2/3 map onto the M3 surfaceContainer* tones, text maps
 * onto on-surface roles (so contrast is guaranteed against each tone), and hairlines map onto
 * the outline roles (giving borders a subtle accent tint). The frosted-glass alpha ramp and
 * the black hero mask stay identity-defined.
 */
private fun paletteFromScheme(s: ColorScheme, isDark: Boolean): NyoraPalette {
    val ink = if (isDark) Color.White else Color.Black
    return NyoraPalette(
        bg             = s.surfaceContainerLowest,
        surface1       = s.surfaceContainerLow,
        surface2       = s.surfaceContainer,
        surface3       = s.surfaceContainerHigh,
        glass1         = ink.copy(alpha = 0.03f),
        glass2         = ink.copy(alpha = 0.05f),
        glass3         = ink.copy(alpha = 0.08f),
        glass4         = ink.copy(alpha = 0.12f),
        glass5         = ink.copy(alpha = 0.18f),
        hairlineStrong = s.outline.copy(alpha = 0.55f),
        hairlineFaint  = s.outlineVariant.copy(alpha = 0.40f),
        maskNoir       = Color(0xFF000000).copy(alpha = if (isDark) 0.92f else 0.60f),
        onSurfaceHigh  = s.onSurface,
        onSurfaceBody  = s.onSurface.copy(alpha = 0.86f),
        onSurfaceMuted = s.onSurfaceVariant,
        onSurfaceFaint = s.onSurfaceVariant.copy(alpha = 0.55f),
    )
}

// Material 3 Expressive shape scale — larger, more varied corners for a softer, more
// characterful feel. Stock M3 components (dialogs, menus, cards, chips) read these.
val NyoraShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small      = RoundedCornerShape(14.dp),
    medium     = RoundedCornerShape(20.dp),
    large      = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(40.dp),
)

@Composable
fun NyoraTheme(
    appearance: AppearanceMode = AppearanceMode.DARK,
    accent: Accent = Accent.SYSTEM,
    content: @Composable () -> Unit,
) {
    // Material You: generate a COMPLETE M3 tonal scheme (secondary/tertiary/containers and
    // the surfaceContainer* surface ladder) from the live accent seed via MaterialKolor,
    // instead of the old hand-set primary-only scheme. Every stock Material 3 component now
    // harmonizes with whatever accent the user picks (incl. the live OS accent).
    // Vibrant keeps the seed hue punchy; dark stays near-black (isAmoled = false so the
    // surfaceContainer tones stay distinguishable for elevation).
    val isDark = appearance == AppearanceMode.DARK
    val colorScheme = rememberDynamicColorScheme(
        seedColor = accent.primary(appearance),
        isDark = isDark,
        isAmoled = false,
        style = PaletteStyle.Vibrant,
    )
    // Push the theme-reactive palette into NyoraTokens so the ~397 token call sites recolor
    // on light/dark swap. SideEffect runs after a successful composition, so the snapshot
    // writes here are applied outside composition and safely invalidate token readers.
    androidx.compose.runtime.SideEffect {
        NyoraTokens.applyPalette(paletteFromScheme(colorScheme, isDark))
    }
    CompositionLocalProvider(LocalNyoraAccent provides accent) {
        MaterialTheme(
            colorScheme = colorScheme,
            shapes      = NyoraShapes,
            content     = content,
        )
    }
}
