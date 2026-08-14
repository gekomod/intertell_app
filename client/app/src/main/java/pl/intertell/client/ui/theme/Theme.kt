package pl.intertell.client.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * The prototype specifies Manrope (headings/body) and IBM Plex Mono (technical
 * data: speeds, IDs, serial numbers, prices in tables). Neither font ships with
 * Android, so this uses the closest system fallback (Sans/Monospace). Drop the
 * real .ttf files into res/font/ and reference them here for a pixel-exact match.
 */
val ManropeFallback = FontFamily.SansSerif
val PlexMonoFallback = FontFamily.Monospace

object IntertellType {
    val display = TextStyle(fontFamily = ManropeFallback, fontWeight = FontWeight.ExtraBold, fontSize = 26.sp, letterSpacing = (-0.4).sp)
    val headline = TextStyle(fontFamily = ManropeFallback, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
    val titleBold = TextStyle(fontFamily = ManropeFallback, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    val bodyBold = TextStyle(fontFamily = ManropeFallback, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
    val body = TextStyle(fontFamily = ManropeFallback, fontWeight = FontWeight.Medium, fontSize = 13.sp)
    val bodySmall = TextStyle(fontFamily = ManropeFallback, fontWeight = FontWeight.Medium, fontSize = 12.sp)
    val label = TextStyle(fontFamily = ManropeFallback, fontWeight = FontWeight.Medium, fontSize = 11.sp)
    val chip = TextStyle(fontFamily = ManropeFallback, fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
    val mono = TextStyle(fontFamily = PlexMonoFallback, fontWeight = FontWeight.Normal, fontSize = 12.sp)
    val monoBold = TextStyle(fontFamily = PlexMonoFallback, fontWeight = FontWeight.Medium, fontSize = 14.sp)
    val monoSmall = TextStyle(fontFamily = PlexMonoFallback, fontWeight = FontWeight.Normal, fontSize = 10.sp)
    val monoFootnote = TextStyle(fontFamily = PlexMonoFallback, fontWeight = FontWeight.Normal, fontSize = 11.sp, lineHeight = 17.sp)
}

private val IntertellColorScheme = lightColorScheme(
    primary = IntertellColors.Accent,
    onPrimary = IntertellColors.White,
    background = IntertellColors.AppBackground,
    surface = IntertellColors.White,
    onSurface = IntertellColors.Navy,
    error = IntertellColors.Danger,
)

@Composable
fun IntertellTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = IntertellColorScheme,
        typography = Typography(),
        content = content,
    )
}
