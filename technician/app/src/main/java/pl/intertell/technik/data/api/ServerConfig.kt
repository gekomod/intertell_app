package pl.intertell.technik.data.api

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import pl.intertell.technik.ui.theme.ThemeMode

private val Context.dataStore by preferencesDataStore(name = "intertell_technik_prefs")

/**
 * Where the app talks to, and the bearer token from the last successful
 * login. The base URL is intentionally editable at runtime (not just a
 * BuildConfig constant) — the deployment we're pointed at
 * (https://inter.nasdom.tech) was flagged as temporary, so a technician
 * shouldn't need a new APK just because the server moved.
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

    // --- Seen-task bookkeeping for TaskPollWorker (see notifications/) ---
    // A task ("kind:id", e.g. "MESSAGE:42") already in this set either
    // already got a notification, or was seen live in the Jobs screen —
    // either way TaskPollWorker shouldn't notify for it again.
    private val seenTasksKey = stringSetPreferencesKey("seen_task_keys")

    suspend fun getSeenTaskKeys(): Set<String> =
        context.dataStore.data.map { it[seenTasksKey] ?: emptySet() }.first()

    suspend fun addSeenTaskKeys(keys: Collection<String>) {
        if (keys.isEmpty()) return
        context.dataStore.edit { prefs ->
            val current = prefs[seenTasksKey] ?: emptySet()
            prefs[seenTasksKey] = current + keys
        }
    }

    suspend fun clearSeenTaskKeys() {
        context.dataStore.edit { it.remove(seenTasksKey) }
    }

    private val themeModeKey = stringPreferencesKey("theme_mode")

    suspend fun getThemeMode(): ThemeMode =
        context.dataStore.data.map { it[themeModeKey] }.first()
            ?.let { raw -> runCatching { ThemeMode.valueOf(raw) }.getOrNull() }
            ?: ThemeMode.SYSTEM

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[themeModeKey] = mode.name }
    }

    private val biometricEnabledKey = booleanPreferencesKey("biometric_enabled")

    suspend fun getBiometricEnabled(): Boolean =
        context.dataStore.data.map { it[biometricEnabledKey] ?: false }.first()

    suspend fun setBiometricEnabled(enabled: Boolean) {
        context.dataStore.edit { it[biometricEnabledKey] = enabled }
    }
}
