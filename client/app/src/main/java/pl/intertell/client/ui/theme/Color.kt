package pl.intertell.client.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Palette translated from the Claude Design prototype (oklch → sRGB approximations
 * where the source used oklch()). Exact figures don't matter here, only the
 * relative weight/warmth relationships from the design.
 */
object IntertellColors {
    val Accent = Color(0xFF0E86C4)
    val AccentPressed = Color(0xFF0A6C9E)
    val Navy = Color(0xFF0E1B2A)
    val AppBackground = Color(0xFFEDEFF2)
    val ScreenBackground = Color(0xFFF4F6F8)
    val White = Color(0xFFFFFFFF)
    val HairlineOnLight = Navy.copy(alpha = 0.08f)
    val HairlineOnLightSoft = Navy.copy(alpha = 0.07f)

    val TextPrimary = Navy
    val Text55 = Navy.copy(alpha = 0.55f)
    val Text50 = Navy.copy(alpha = 0.5f)
    val Text45 = Navy.copy(alpha = 0.45f)
    val Text42 = Navy.copy(alpha = 0.42f)
    val Text38 = Navy.copy(alpha = 0.38f)
    val Text35 = Navy.copy(alpha = 0.35f)
    val ToggleTrackOff = Navy.copy(alpha = 0.32f)
    val ToggleTrackOffFaint = Navy.copy(alpha = 0.12f)
    val ToggleThumbFaint = Color(0xFFFFFFFF).copy(alpha = 0.8f)

    val Green = Color(0xFF178F5C)
    val GreenLightDot = Color(0xFF4CD08A)
    val GreenCheck = Color(0xFF3FC97F)
    val GreenChipBg = Color(0xFF148F46).copy(alpha = 0.10f)

    val Danger = Color(0xFFD6412B)
    val DangerChipFg = Color(0xFFD93B1E)
    val DangerChipBg = Color(0xFFDC3C28).copy(alpha = 0.10f)

    val Amber = Color(0xFFC97A1F)
    val AmberBannerBg = Color(0xFFFFD68C).copy(alpha = 0.28f)
    val AmberBannerBorder = Color(0xFFBF8C14).copy(alpha = 0.25f)
}
