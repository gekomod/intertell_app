package pl.intertell.technik.data.geo

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

data class RouteInfo(val distanceKm: Double, val durationMin: Int)

/** Free, keyless driving-route lookup via OSRM's public demo router — no API key exists for this project. */
object OsrmRouter {
    private val client = OkHttpClient()

    suspend fun route(from: LatLng, to: LatLng): RouteInfo? = withContext(Dispatchers.IO) {
        val url = "https://router.project-osrm.org/route/v1/driving/" +
            "${from.lon},${from.lat};${to.lon},${to.lat}?overview=false"
        val request = Request.Builder().url(url).build()
        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext null
                val body = response.body?.string() ?: return@withContext null
                val json = JSONObject(body)
                if (json.optString("code") != "Ok") return@withContext null
                val route = json.getJSONArray("routes").getJSONObject(0)
                RouteInfo(
                    distanceKm = route.getDouble("distance") / 1000.0,
                    durationMin = (route.getDouble("duration") / 60.0).toInt(),
                )
            }
        } catch (e: Exception) {
            null
        }
    }
}
