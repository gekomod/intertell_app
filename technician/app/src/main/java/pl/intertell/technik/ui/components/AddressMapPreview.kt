package pl.intertell.technik.ui.components

import android.annotation.SuppressLint
import android.webkit.WebView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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

/**
 * Map preview for a job/customer address — geocoded via OSM's Nominatim, then
 * shown via openstreetmap.org's own official embeddable map (the same widget
 * behind its "Share > Embed" feature), rather than a third-party static-map
 * compositor: that's OSM's own, well-maintained infrastructure, so it's far
 * less likely to silently fail in the field than a small community service.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun AddressMapPreview(address: String, viewModel: TechnicianViewModel, modifier: Modifier = Modifier) {
    if (address.isBlank()) return
    val cache by viewModel.geocodeCache.collectAsState()
    LaunchedEffect(address) { viewModel.geocodeAddress(address) }
    val latLng = cache[address]

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(IntertellColors.HairlineOnLightFaint),
        contentAlignment = Alignment.Center,
    ) {
        when {
            latLng != null -> {
                val delta = 0.006
                val bbox = "${latLng.lon - delta},${latLng.lat - delta},${latLng.lon + delta},${latLng.lat + delta}"
                val url = "https://www.openstreetmap.org/export/embed.html?bbox=$bbox&marker=${latLng.lat},${latLng.lon}"
                AndroidView(
                    modifier = Modifier.fillMaxWidth().height(180.dp),
                    factory = { ctx ->
                        WebView(ctx).apply {
                            settings.javaScriptEnabled = true
                            loadUrl(url)
                        }
                    },
                    update = { it.loadUrl(url) },
                )
            }
            cache.containsKey(address) -> Text(
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
    }
}
