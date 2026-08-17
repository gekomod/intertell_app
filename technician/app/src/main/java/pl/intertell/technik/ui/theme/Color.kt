package pl.intertell.technik.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Neutral colors that actually invert between light and dark — background,
 * card surface, body text, dividers, the toggle-off track. Brand/status
 * colors (Accent, Navy, Green*, Danger*, Amber*, White, DoneScreenBg) live
 * as plain constants on [IntertellColors] below instead: a navy banner or
 * a green "online" chip is meant to read the same regardless of theme,
 * only the neutral chrome around it should invert.
 */
internal data class ThemePalette(
    val appBackground: Color,
    val screenBackground: Color,
    val surface: Color,
    val textPrimary: Color,
    val hairlineOnLight: Color,
    val hairlineOnLightSoft: Color,
    val hairlineOnLightFaint: Color,
    val toggleTrackOff: Color,
    val toggleTrackOffFaint: Color,
)

private val LightPalette = ThemePalette(
    appBackground = Color(0xFFEDEFF2),
    screenBackground = Color(0xFFF4F6F8),
    surface = Color(0xFFFFFFFF),
    textPrimary = Color(0xFF0E1B2A),
    hairlineOnLight = Color(0xFF0E1B2A).copy(alpha = 0.08f),
    hairlineOnLightSoft = Color(0xFF0E1B2A).copy(alpha = 0.07f),
    hairlineOnLightFaint = Color(0xFF0E1B2A).copy(alpha = 0.06f),
    toggleTrackOff = Color(0xFF0E1B2A).copy(alpha = 0.32f),
    toggleTrackOffFaint = Color(0xFF0E1B2A).copy(alpha = 0.12f),
)

private val DarkPalette = ThemePalette(
    appBackground = Color(0xFF10141B),
    screenBackground = Color(0xFF141922),
    surface = Color(0xFF1B212C),
    textPrimary = Color(0xFFE7EAEE),
    hairlineOnLight = Color(0xFFE7EAEE).copy(alpha = 0.10f),
    hairlineOnLightSoft = Color(0xFFE7EAEE).copy(alpha = 0.08f),
    hairlineOnLightFaint = Color(0xFFE7EAEE).copy(alpha = 0.07f),
    toggleTrackOff = Color(0xFFE7EAEE).copy(alpha = 0.28f),
    toggleTrackOffFaint = Color(0xFFE7EAEE).copy(alpha = 0.14f),
)

internal fun paletteFor(isDark: Boolean) = if (isDark) DarkPalette else LightPalette

internal val LocalThemePalette = staticCompositionLocalOf { LightPalette }

/**
 * Palette translated from the Claude Design prototype (oklch → sRGB approximations
 * where the source used oklch()). Exact figures don't matter here, only the
 * relative weight/warmth relationships from the design.
 */
object IntertellColors {
    // Brand/status colors — constant across light/dark (see ThemePalette's doc comment above).
    val Accent = Color(0xFF22A06B) // technician primary action green, oklch(0.62 0.16 148)
    val Green = Color(0xFF178F5C) // status/label green, oklch(0.5 0.14 150)
    val Navy = Color(0xFF0E1B2A)
    val DoneScreenBg = Color(0xFF111C14)
    val White = Color(0xFFFFFFFF)

    val Danger = Color(0xFFD93B1E)
    val DangerChipBg = Danger.copy(alpha = 0.10f)
    val Amber = Color(0xFFC97A1F)
    val AmberChipBg = Color(0xFFC87814).copy(alpha = 0.12f)
    val GreenChipBg = Color(0xFF148F46).copy(alpha = 0.10f)
    val GreenCheck = Color(0xFF3FC97F)

    // Neutral colors — resolved from whichever ThemePalette IntertellTheme provided.
    val AppBackground: Color @Composable get() = LocalThemePalette.current.appBackground
    val ScreenBackground: Color @Composable get() = LocalThemePalette.current.screenBackground

    /** Card/sheet/container background — was folded into [White] before dark mode existed. */
    val Surface: Color @Composable get() = LocalThemePalette.current.surface

    val HairlineOnLight: Color @Composable get() = LocalThemePalette.current.hairlineOnLight
    val HairlineOnLightSoft: Color @Composable get() = LocalThemePalette.current.hairlineOnLightSoft
    val HairlineOnLightFaint: Color @Composable get() = LocalThemePalette.current.hairlineOnLightFaint

    val TextPrimary: Color @Composable get() = LocalThemePalette.current.textPrimary
    val Text6: Color @Composable get() = LocalThemePalette.current.textPrimary.copy(alpha = 0.6f)
    val Text55: Color @Composable get() = LocalThemePalette.current.textPrimary.copy(alpha = 0.55f)
    val Text5: Color @Composable get() = LocalThemePalette.current.textPrimary.copy(alpha = 0.5f)
    val Text45: Color @Composable get() = LocalThemePalette.current.textPrimary.copy(alpha = 0.45f)
    val Text42: Color @Composable get() = LocalThemePalette.current.textPrimary.copy(alpha = 0.42f)
    val Text4: Color @Composable get() = LocalThemePalette.current.textPrimary.copy(alpha = 0.4f)
    val Text35: Color @Composable get() = LocalThemePalette.current.textPrimary.copy(alpha = 0.35f)
    val ToggleTrackOff: Color @Composable get() = LocalThemePalette.current.toggleTrackOff
    val ToggleTrackOffFaint: Color @Composable get() = LocalThemePalette.current.toggleTrackOffFaint
}
