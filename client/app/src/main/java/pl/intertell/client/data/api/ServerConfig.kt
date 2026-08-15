package pl.intertell.client.data.api

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "intertell_client_prefs")

/**
 * Where the app talks to, and the bearer token from the last successful
 * login. The base URL is intentionally editable at runtime (not just a
 * BuildConfig constant) — the deployment we're pointed at
 * (https://inter.nasdom.tech) was flagged as temporary.
 */
class ServerConfig(private val context: Context) {
    private val baseUrlKey = stringPreferencesKey("base_url")
    private val tokenKey = stringPreferencesKey("auth_token")

    companion object {
        const val DEFAULT_BASE_URL = "https://inter.nasdom.tech"
    }

    suspend fun getBaseUrl(): String =
        context.dataStore.data.map { it[baseUrlKey] ?: DEFAULT_BASE_URL }.first().trimEnd('/')

    suspend fun setBaseUrl(url: String) {
        val trimmed = url.trim().trimEnd('/')
        context.dataStore.edit { it[baseUrlKey] = trimmed.ifBlank { DEFAULT_BASE_URL } }
    }

    suspend fun getToken(): String? = context.dataStore.data.map { it[tokenKey] }.first()

    suspend fun setToken(token: String?) {
        context.dataStore.edit {
            if (token == null) it.remove(tokenKey) else it[tokenKey] = token
        }
    }
}
