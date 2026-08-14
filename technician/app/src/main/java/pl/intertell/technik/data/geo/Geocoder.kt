package pl.intertell.technik.data.geo

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import java.io.IOException

data class LatLng(val lat: Double, val lon: Double)

/**
 * Free, keyless geocoding via OSM's Nominatim — no Google Maps API key exists
 * for this project. Nominatim's usage policy requires a real User-Agent and
 * caps usage at ~1 req/s, which is fine for a technician looking up one job
 * address at a time.
 */
object NominatimGeocoder {
    private val client = OkHttpClient()

    suspend fun geocode(address: String): LatLng? = withContext(Dispatchers.IO) {
        if (address.isBlank()) return@withContext null
        val url = "https://nominatim.openstreetmap.org/search?format=json&limit=1&q=" +
            java.net.URLEncoder.encode(address, "UTF-8")
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "IntertellTechnik/1.0 (inter.nasdom.tech)")
            .build()
        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext null
                val body = response.body?.string() ?: return@withContext null
                val results = JSONArray(body)
                if (results.length() == 0) return@withContext null
                val first = results.getJSONObject(0)
                LatLng(first.getString("lat").toDouble(), first.getString("lon").toDouble())
            }
        } catch (e: IOException) {
            null
        } catch (e: Exception) {
            null
        }
    }

    fun staticMapUrl(latLng: LatLng, width: Int = 640, height: Int = 320, zoom: Int = 16): String =
        "https://staticmap.openstreetmap.de/staticmap.php?center=${latLng.lat},${latLng.lon}" +
            "&zoom=$zoom&size=${width}x$height&markers=${latLng.lat},${latLng.lon},lightblue1"
}
