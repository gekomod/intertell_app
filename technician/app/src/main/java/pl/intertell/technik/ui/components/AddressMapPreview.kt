package pl.intertell.technik.ui.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import pl.intertell.technik.TechnicianViewModel
import pl.intertell.technik.data.geo.LatLng
import pl.intertell.technik.ui.theme.IntertellColors
import pl.intertell.technik.ui.theme.IntertellType
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.tan

/**
 * Map preview for a job/customer address. The server resolves and sends
 * coordinates for every job address up front (see api_tech.go's use of
 * internal/geo) — pass those in as [knownLocation] to skip client-side
 * geocoding entirely, which is what job-detail screens should always do,
 * since Nominatim's public API is unreliable when called directly from a
 * phone (see internal/geo/geocode.go's doc comment). Falls back to an
 * on-device Nominatim lookup only when the server didn't supply one.
 *
 * Rendered as a single raster map tile from MapTiler (needs
 * BuildConfig.MAPTILER_API_KEY, injected by CI from the MAPTILER_API_KEY repo
 * secret) — a plain image fetch, not an embedded webpage. Two earlier
 * approaches didn't hold up: an embedded openstreetmap.org page in a WebView
 * rendered as a blank box on a real device, and OSM's own raw tile server
 * (tile.openstreetmap.org) actively blocks direct app-embedded requests per
 * its usage policy ("Access blocked — App is not following the tile usage
 * policy..."). A plain PNG from a provider meant for exactly this has far
 * fewer ways to fail. It isn't pixel-centered on the pin — the marker shown
 * is the box's visual center, which is "the tile containing the address,"
 * accurate to within the tile's ground size — a deliberate precision-for-
 * reliability trade for what's only ever a rough visual aid; turn-by-turn
 * still goes through "Nawiguj".
 *
 * With [showRoute], also overlays a "mapa · trasa X km · Y min" caption —
 * matching the original design's job-detail header — computed from the
 * technician's live location via OSRM, when location permission is granted.
 */
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
    val uiState by viewModel.uiState.collectAsState()
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
                val zoom = 17
                val tileX = lonToTileX(latLng.lon, zoom)
                val tileY = latToTileY(latLng.lat, zoom)
                AsyncImage(
                    model = "https://api.maptiler.com/maps/streets-v2/$zoom/$tileX/$tileY.png?key=${pl.intertell.technik.BuildConfig.MAPTILER_API_KEY}",
                    contentDescription = "Mapa: $address",
                    modifier = Modifier.fillMaxWidth().height(height),
                    contentScale = ContentScale.Crop,
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(IntertellColors.Danger),
                )
            }
            geocodeCache.containsKey(address) -> {
                val debugUrl = "${uiState.serverUrl}/debug/geocode?address=${Uri.encode(address)}"
                Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                    Text(
                        "Nie udało się ustalić lokalizacji na mapie.",
                        style = IntertellType.bodySmall,
                        color = IntertellColors.Text45,
                    )
                    Text(
                        "Adres: $address",
                        style = IntertellType.monoSmall,
                        color = IntertellColors.Text45,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                    Text(
                        "Sprawdź: $debugUrl",
                        style = IntertellType.monoSmall,
                        color = IntertellColors.Text45,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
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
                Text(label, style = IntertellType.monoSmall, color = Color.White)
            }
        }
    }
}

private fun lonToTileX(lon: Double, zoom: Int): Int =
    floor((lon + 180.0) / 360.0 * (1 shl zoom)).toInt()

private fun latToTileY(lat: Double, zoom: Int): Int {
    val latRad = Math.toRadians(lat)
    return floor((1.0 - ln(tan(latRad) + 1.0 / cos(latRad)) / PI) / 2.0 * (1 shl zoom)).toInt()
}
