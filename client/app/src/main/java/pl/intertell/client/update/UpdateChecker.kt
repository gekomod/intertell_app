package pl.intertell.client.update

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

data class UpdateInfo(val versionLabel: String, val downloadUrl: String)

/**
 * Checks the GitHub Releases API for a newer client-app build than the one
 * currently installed. There's no Play Store distribution for this app — it
 * ships as a debug APK via GitHub Releases (see .github/workflows/build-apk.yml,
 * which tags each release "build-<run number>" and stamps that same number
 * into the app's versionCode/BuildConfig.BUILD_NUMBER) — so this is the
 * closest equivalent to Play Store's automatic update check.
 */
object UpdateChecker {
    private val client = OkHttpClient()
    private const val RELEASES_URL = "https://api.github.com/repos/gekomod/intertell_app/releases/latest"
    private const val ASSET_NAME = "intertell-client-debug.apk"

    suspend fun checkForUpdate(currentBuildNumber: Int): UpdateInfo? = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(RELEASES_URL)
                .header("Accept", "application/vnd.github+json")
                .build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext null
                val body = response.body?.string() ?: return@withContext null
                val json = JSONObject(body)
                val tag = json.optString("tag_name")
                val latestBuild = tag.substringAfter("build-").toIntOrNull() ?: return@withContext null
                if (latestBuild <= currentBuildNumber) return@withContext null

                val assets = json.optJSONArray("assets") ?: return@withContext null
                var downloadUrl: String? = null
                for (i in 0 until assets.length()) {
                    val asset = assets.getJSONObject(i)
                    if (asset.optString("name") == ASSET_NAME) {
                        downloadUrl = asset.optString("browser_download_url")
                        break
                    }
                }
                downloadUrl?.let { UpdateInfo(versionLabel = tag, downloadUrl = it) }
            }
        } catch (e: Exception) {
            null
        }
    }
}
