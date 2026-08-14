package pl.intertell.technik.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Palette translated from the Claude Design prototype (oklch → sRGB approximations
 * where the source used oklch()). Exact figures don't matter here, only the
 * relative weight/warmth relationships from the design.
 */
object IntertellColors {
    val Accent = Color(0xFF22A06B) // technician primary action green, oklch(0.62 0.16 148)
    val Green = Color(0xFF178F5C) // status/label green, oklch(0.5 0.14 150)
    val Navy = Color(0xFF0E1B2A)
    val DoneScreenBg = Color(0xFF111C14)
    val AppBackground = Color(0xFFEDEFF2)
    val ScreenBackground = Color(0xFFF4F6F8)
    val White = Color(0xFFFFFFFF)
    val HairlineOnLight = Navy.copy(alpha = 0.08f)
    val HairlineOnLightSoft = Navy.copy(alpha = 0.07f)
    val HairlineOnLightFaint = Navy.copy(alpha = 0.06f)

    val TextPrimary = Navy
    val Text6 = Navy.copy(alpha = 0.6f)
    val Text55 = Navy.copy(alpha = 0.55f)
    val Text5 = Navy.copy(alpha = 0.5f)
    val Text45 = Navy.copy(alpha = 0.45f)
    val Text42 = Navy.copy(alpha = 0.42f)
    val Text4 = Navy.copy(alpha = 0.4f)
    val Text35 = Navy.copy(alpha = 0.35f)
    val ToggleTrackOff = Navy.copy(alpha = 0.32f)
    val ToggleTrackOffFaint = Navy.copy(alpha = 0.12f)

    val Danger = Color(0xFFD93B1E)
    val DangerChipBg = Danger.copy(alpha = 0.10f)
    val Amber = Color(0xFFC97A1F)
    val AmberChipBg = Color(0xFFC87814).copy(alpha = 0.12f)
    val GreenChipBg = Color(0xFF148F46).copy(alpha = 0.10f)

    val GreenCheck = Color(0xFF3FC97F)
}
