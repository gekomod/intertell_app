package pl.intertell.technik.data.api

import android.content.Context
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import pl.intertell.technik.data.ApiException

private val JSON = "application/json; charset=utf-8".toMediaType()

/**
 * Thin JSON/HTTP wrapper around OkHttp for the technician API
 * (see ServerConfig for the base URL and bearer token it reads). Kept
 * dependency-light on purpose — plain OkHttp + org.json instead of
 * Retrofit/kotlinx-serialization, since this app can't be build-verified
 * locally (see the repo README) and fewer moving parts means fewer ways
 * for a real CI build to fail on something only discoverable there.
 */
class ApiClient(context: Context) {
    private val config = ServerConfig(context.applicationContext)
    private val http = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    val serverConfig: ServerConfig get() = config

    suspend fun get(path: String): JSONObject = execute("GET", path, null)
    suspend fun post(path: String, body: JSONObject? = null): JSONObject = execute("POST", path, body)
    suspend fun put(path: String, body: JSONObject): JSONObject = execute("PUT", path, body)
    suspend fun delete(path: String): JSONObject = execute("DELETE", path, null)

    private suspend fun execute(method: String, path: String, body: JSONObject?): JSONObject =
        withContext(Dispatchers.IO) {
            val baseUrl = config.getBaseUrl()
            val token = config.getToken()
            val requestBuilder = Request.Builder().url(baseUrl + path)
            if (!token.isNullOrBlank()) {
                requestBuilder.header("Authorization", "Bearer $token")
            }
            // OkHttp requires a non-null body for methods that mandate one
            // (POST/PUT/...) even when there's nothing to send — passing
            // null there throws IllegalArgumentException at request-build
            // time, not just "no body sent".
            val requestBody = when {
                body != null -> body.toString().toRequestBody(JSON)
                method == "POST" || method == "PUT" -> "{}".toRequestBody(JSON)
                else -> null
            }
            requestBuilder.method(method, requestBody)

            val response = try {
                http.newCall(requestBuilder.build()).execute()
            } catch (e: IOException) {
                throw ApiException("Brak połączenia z serwerem (${config.getBaseUrl()}).")
            }

            response.use {
                val text = it.body?.string().orEmpty()
                val json = if (text.isBlank()) JSONObject() else JSONObject(text)
                if (!it.isSuccessful) {
                    val message = json.optString("error").ifBlank { "Błąd serwera (${it.code})." }
                    throw ApiException(message, it.code)
                }
                json
            }
        }
}

fun JSONObject.optJSONArrayOrEmpty(key: String): JSONArray = optJSONArray(key) ?: JSONArray()

inline fun <T> JSONArray.map(transform: (JSONObject) -> T): List<T> =
    (0 until length()).map { transform(getJSONObject(it)) }
