package pl.intertell.technik.data.api

import android.content.Context
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CancellationException
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

    suspend fun get(path: String, readTimeoutSeconds: Long = 15): JSONObject = execute("GET", path, null, readTimeoutSeconds)
    suspend fun post(path: String, body: JSONObject? = null): JSONObject = execute("POST", path, body, 15)
    suspend fun put(path: String, body: JSONObject): JSONObject = execute("PUT", path, body, 15)
    suspend fun delete(path: String): JSONObject = execute("DELETE", path, null, 15)

    // Every failure mode below — bad network, wrong base URL, a route that
    // doesn't exist yet on an outdated server (plain-text 404, not JSON),
    // an unexpected response shape — is normalized into ApiException here,
    // once, so nothing upstream (ViewModel, screens) ever has to deal with
    // a raw JSONException/IOException/etc. An uncaught one of those inside
    // a viewModelScope coroutine crashes the whole app, which is exactly
    // what used to happen logging in against a server still running the
    // old API.
    private suspend fun execute(method: String, path: String, body: JSONObject?, readTimeoutSeconds: Long): JSONObject =
        withContext(Dispatchers.IO) {
            try {
                val baseUrl = config.getBaseUrl()
                val token = config.getToken()
                val requestBuilder = Request.Builder().url(baseUrl + path)
                if (!token.isNullOrBlank()) {
                    requestBuilder.header("Authorization", "Bearer $token")
                }
                // OkHttp requires a non-null body for methods that mandate
                // one (POST/PUT/...) even when there's nothing to send —
                // passing null there throws IllegalArgumentException at
                // request-build time, not just "no body sent".
                val requestBody = when {
                    body != null -> body.toString().toRequestBody(JSON)
                    method == "POST" || method == "PUT" -> "{}".toRequestBody(JSON)
                    else -> null
                }
                requestBuilder.method(method, requestBody)

                // Per-call override — e.g. the QField infrastructure map's
                // first (cold-cache) fetch downloads a file over WebDAV and
                // parses it server-side, which can easily exceed the default
                // 15s and previously surfaced as a false "Brak połączenia z
                // serwerem" even though the server was still working.
                val client = if (readTimeoutSeconds == 15L) http else http.newBuilder().readTimeout(readTimeoutSeconds, TimeUnit.SECONDS).build()
                val response = client.newCall(requestBuilder.build()).execute()
                response.use {
                    val text = it.body?.string().orEmpty()
                    if (!it.isSuccessful) {
                        val message = runCatching { JSONObject(text).optString("error") }
                            .getOrNull()?.takeIf { m -> m.isNotBlank() }
                            ?: "Błąd serwera (${it.code})."
                        throw ApiException(message, it.code)
                    }
                    if (text.isBlank()) JSONObject() else JSONObject(text)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: ApiException) {
                throw e
            } catch (e: IOException) {
                throw ApiException("Brak połączenia z serwerem (${config.getBaseUrl()}).")
            } catch (e: Exception) {
                throw ApiException("Nieprawidłowa odpowiedź serwera z ${config.getBaseUrl()} — sprawdź czy to właściwy adres i czy backend jest zaktualizowany.")
            }
        }
}

fun JSONObject.optJSONArrayOrEmpty(key: String): JSONArray = optJSONArray(key) ?: JSONArray()

inline fun <T> JSONArray.map(transform: (JSONObject) -> T): List<T> =
    (0 until length()).map { transform(getJSONObject(it)) }
