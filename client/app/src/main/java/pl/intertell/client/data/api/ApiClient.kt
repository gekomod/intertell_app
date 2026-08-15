package pl.intertell.client.data.api

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
import pl.intertell.client.data.ApiException

private val JSON = "application/json; charset=utf-8".toMediaType()

/**
 * Thin JSON/HTTP wrapper around OkHttp for the client API (see ServerConfig
 * for the base URL and bearer token it reads). Kept dependency-light on
 * purpose — plain OkHttp + org.json instead of Retrofit/kotlinx-serialization
 * — mirrors the technician app's ApiClient exactly, including normalizing
 * every failure mode into ApiException so nothing upstream ever has to deal
 * with a raw JSONException/IOException crashing the app.
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

    private suspend fun execute(method: String, path: String, body: JSONObject?): JSONObject =
        withContext(Dispatchers.IO) {
            try {
                val baseUrl = config.getBaseUrl()
                val token = config.getToken()
                val requestBuilder = Request.Builder().url(baseUrl + path)
                if (!token.isNullOrBlank()) {
                    requestBuilder.header("Authorization", "Bearer $token")
                }
                val requestBody = when {
                    body != null -> body.toString().toRequestBody(JSON)
                    method == "POST" || method == "PUT" -> "{}".toRequestBody(JSON)
                    else -> null
                }
                requestBuilder.method(method, requestBody)

                val response = http.newCall(requestBuilder.build()).execute()
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
