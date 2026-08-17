package pl.intertell.technik.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

/**
 * The prototype specifies Manrope (headings/body) and IBM Plex Mono (technical
 * data: RX/TX levels, IDs, serial numbers, IP addresses). Neither font ships
 * with Android, so this uses the closest system fallback (Sans/Monospace).
 * Drop the real .ttf files into res/font/ and reference them here for a
 * pixel-exact match.
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

/** Persisted via ServerConfig.getThemeMode()/setThemeMode(); SYSTEM follows the OS setting. */
enum class ThemeMode { LIGHT, DARK, SYSTEM }

@Composable
fun IntertellTheme(themeMode: ThemeMode = ThemeMode.SYSTEM, content: @Composable () -> Unit) {
    val isDark = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    val palette = paletteFor(isDark)
    val colorScheme = if (isDark) {
        darkColorScheme(
            primary = IntertellColors.Accent,
            onPrimary = IntertellColors.White,
            background = palette.appBackground,
            surface = palette.surface,
            onSurface = palette.textPrimary,
            error = IntertellColors.Danger,
        )
    } else {
        lightColorScheme(
            primary = IntertellColors.Accent,
            onPrimary = IntertellColors.White,
            background = palette.appBackground,
            surface = palette.surface,
            onSurface = palette.textPrimary,
            error = IntertellColors.Danger,
        )
    }

    // No enableEdgeToEdge() in this app (see MainActivity) — the status bar
    // still takes its background from a real pixel color, not transparency,
    // so it has to be pushed to match the resolved theme explicitly here
    // rather than left to follow the OS's own light/dark switch, since
    // ThemeMode can override that.
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = palette.appBackground.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDark
        }
    }

    CompositionLocalProvider(LocalThemePalette provides palette) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography(),
            content = content,
        )
    }
}
