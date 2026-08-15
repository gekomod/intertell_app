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
import pl.intertell.technik.data.geo.LatLng
import pl.intertell.technik.ui.theme.IntertellColors
import pl.intertell.technik.ui.theme.IntertellType
import java.util.Locale

/**
 * Map preview for a job/customer address. The server resolves and sends
 * coordinates for every job address up front (see api_tech.go's use of
 * internal/geo) — pass those in as [knownLocation] to skip client-side
 * geocoding entirely, which is what job-detail screens should always do,
 * since Nominatim's public API is unreliable when called directly from a
 * phone (see internal/geo/geocode.go's doc comment). Falls back to an
 * on-device Nominatim lookup only when the server didn't supply one.
 *
 * Rendered via openstreetmap.org's own official embeddable map (the same
 * widget behind its "Share > Embed" feature) rather than a third-party
 * static-map compositor.
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
    knownLocation: LatLng? = null,
) {
    if (address.isBlank()) return
    val geocodeCache by viewModel.geocodeCache.collectAsState()
    LaunchedEffect(address, knownLocation) {
        if (knownLocation != null) {
            viewModel.seedLocation(address, knownLocation.lat, knownLocation.lon)
        } else {
            viewModel.geocodeAddress(address)
        }
    }
    val latLng = knownLocation ?: geocodeCache[address]

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
