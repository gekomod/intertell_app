package pl.intertell.technik.ui.components

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
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import pl.intertell.technik.TechnicianViewModel
import pl.intertell.technik.data.geo.NominatimGeocoder
import pl.intertell.technik.ui.theme.IntertellColors
import pl.intertell.technik.ui.theme.IntertellType

/** Static OpenStreetMap preview for a job/customer address — geocoded via Nominatim, no API key needed. */
@Composable
fun AddressMapPreview(address: String, viewModel: TechnicianViewModel, modifier: Modifier = Modifier) {
    if (address.isBlank()) return
    val cache by viewModel.geocodeCache.collectAsState()
    LaunchedEffect(address) { viewModel.geocodeAddress(address) }
    val latLng = cache[address]

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(IntertellColors.HairlineOnLightFaint),
        contentAlignment = Alignment.Center,
    ) {
        when {
            latLng != null -> AsyncImage(
                model = NominatimGeocoder.staticMapUrl(latLng),
                contentDescription = "Mapa: $address",
                modifier = Modifier.fillMaxWidth().height(160.dp),
                contentScale = ContentScale.Crop,
            )
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
