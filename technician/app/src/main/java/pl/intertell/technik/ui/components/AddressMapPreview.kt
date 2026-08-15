package pl.intertell.technik.ui.components

import android.annotation.SuppressLint
import android.webkit.WebView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import pl.intertell.technik.TechnicianViewModel
import pl.intertell.technik.ui.theme.IntertellColors
import pl.intertell.technik.ui.theme.IntertellType
import java.util.Locale

/**
 * Map preview for a job/customer address — geocoded via OSM's Nominatim, then
 * shown via openstreetmap.org's own official embeddable map (the same widget
 * behind its "Share > Embed" feature), rather than a third-party static-map
 * compositor: that's OSM's own, well-maintained infrastructure, so it's far
 * less likely to silently fail in the field than a small community service.
 *
 * With [showRoute], also overlays a "mapa · trasa X km · Y min" caption —
 * matching the original design's job-detail header — computed from the
 * technician's live location via OSRM, when location permission is granted.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun AddressMapPreview(
    address: String,
    viewModel: TechnicianViewModel,
    modifier: Modifier = Modifier,
    height: androidx.compose.ui.unit.Dp = 180.dp,
    rounded: Boolean = true,
    showRoute: Boolean = false,
) {
    if (address.isBlank()) return
    val geocodeCache by viewModel.geocodeCache.collectAsState()
    LaunchedEffect(address) { viewModel.geocodeAddress(address) }
    val latLng = geocodeCache[address]

    val routeCache by viewModel.routeCache.collectAsState()
    LaunchedEffect(latLng) {
        if (showRoute && latLng != null) viewModel.computeRoute(address)
    }
    val route = routeCache[address]

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .let { if (rounded) it.clip(RoundedCornerShape(14.dp)) else it }
            .background(IntertellColors.HairlineOnLightFaint),
        contentAlignment = Alignment.Center,
    ) {
        when {
            latLng != null -> {
                val delta = 0.006
                val bbox = "${latLng.lon - delta},${latLng.lat - delta},${latLng.lon + delta},${latLng.lat + delta}"
                val url = "https://www.openstreetmap.org/export/embed.html?bbox=$bbox&marker=${latLng.lat},${latLng.lon}"
                AndroidView(
                    modifier = Modifier.fillMaxWidth().height(height),
                    factory = { ctx ->
                        WebView(ctx).apply {
                            settings.javaScriptEnabled = true
                            loadUrl(url)
                        }
                    },
                    update = { it.loadUrl(url) },
                )
            }
            geocodeCache.containsKey(address) -> Text(
                "Nie udało się ustalić lokalizacji na mapie.",
                style = IntertellType.bodySmall,
                color = IntertellColors.Text45,
            )
            else -> Text(
                "Ładowanie mapy…",
                style = IntertellType.bodySmall,
                color = IntertellColors.Text45,
            )
        }

        if (showRoute && latLng != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(IntertellColors.TextPrimary.copy(alpha = 0.72f))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
            ) {
                val label = if (route != null) {
                    val km = String.format(Locale("pl", "PL"), "%.1f", route.distanceKm)
                    "mapa · trasa $km km · ${route.durationMin} min"
                } else {
                    "mapa"
                }
                Text(label, style = IntertellType.monoSmall, color = IntertellColors.White)
            }
        }
    }
}
