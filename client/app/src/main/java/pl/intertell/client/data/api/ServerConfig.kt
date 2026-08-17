package pl.intertell.client.data.api

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import pl.intertell.client.ui.theme.ThemeMode

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

    // --- Notification bookkeeping for the invoice/ticket poller (see
    // notifications/) — mirrors the technician app's seen-task keys.
    // Invoice ids already notified about (or already loaded on the Home
    // screen, which counts as "seen" the same way). Ticket entries are
    // "id:status" pairs — the poller diffs the current status against the
    // last-known one per id to catch a BOK reply/status change.
    private val seenInvoiceIdsKey = stringSetPreferencesKey("seen_invoice_ids")
    private val ticketStatusKey = stringSetPreferencesKey("ticket_status_snapshot")

    suspend fun getSeenInvoiceIds(): Set<String> =
        context.dataStore.data.map { it[seenInvoiceIdsKey] ?: emptySet() }.first()

    suspend fun addSeenInvoiceIds(ids: Collection<String>) {
        if (ids.isEmpty()) return
        context.dataStore.edit { prefs ->
            prefs[seenInvoiceIdsKey] = (prefs[seenInvoiceIdsKey] ?: emptySet()) + ids
        }
    }

    suspend fun getTicketStatusSnapshot(): Set<String> =
        context.dataStore.data.map { it[ticketStatusKey] ?: emptySet() }.first()

    suspend fun setTicketStatusSnapshot(pairs: Collection<String>) {
        context.dataStore.edit { it[ticketStatusKey] = pairs.toSet() }
    }

    suspend fun clearNotificationState() {
        context.dataStore.edit {
            it.remove(seenInvoiceIdsKey)
            it.remove(ticketStatusKey)
        }
    }

    private val themeModeKey = stringPreferencesKey("theme_mode")

    suspend fun getThemeMode(): ThemeMode =
        context.dataStore.data.map { it[themeModeKey] }.first()
            ?.let { raw -> runCatching { ThemeMode.valueOf(raw) }.getOrNull() }
            ?: ThemeMode.SYSTEM

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[themeModeKey] = mode.name }
    }
}
