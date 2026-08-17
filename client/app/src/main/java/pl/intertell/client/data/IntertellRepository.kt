package pl.intertell.client.data

/**
 * Everything the UI needs from the intratell backend's client API (see the
 * server's internal/handlers/api_client.go). [ApiIntertellRepository] is the
 * only implementation — login is verified against LMS when the customer is
 * linked (server-side), matching the original design's "Zaloguj przez LMS" /
 * "Aplikacja nie przechowuje haseł" promise.
 *
 * Throws [ApiException] on failure (bad credentials, network error, ...).
 */
interface IntertellRepository {
    suspend fun login(contractOrEmail: String, password: String): Account
    suspend fun logout()

    suspend fun getAccountAndStatus(): Pair<Account, ServiceStatus>
    suspend fun getDmzSettings(): DmzSettings
    suspend fun setDmz(enabled: Boolean, hostIp: String)

    suspend fun getInvoices(): List<Invoice>
    suspend fun getInvoice(id: Long): Invoice

    suspend fun getPlans(): List<Plan>

    /**
     * Requests a plan change. Self-service changes are limited to once
     * every 30 days server-side — outside that window an upgrade is
     * applied immediately ([PlanChangeResult.applied] true), a downgrade
     * files a request for BOK to action at the next billing period; within
     * the window ANY change is filed for BOK regardless of direction (see
     * [PlanChangeResult.limited]).
     */
    suspend fun changePlan(planId: Long): PlanChangeResult

    /**
     * Reports a problem straight into the technician-assignable queue (the
     * same one the public site's contact form feeds) — an admin can assign
     * it to a technician on /admin/wiadomosci, and it then shows up in the
     * technician app's own task list with address and map.
     */
    suspend fun reportProblem(subject: String, message: String, phone: String)

    /**
     * Sends [message] to the "Czat z BOK" AI assistant along with prior
     * [history] for context, and returns its reply. The assistant can check
     * connection/signal status but cannot change account state or restart
     * hardware — see the server's internal/ai package.
     */
    suspend fun sendChatMessage(history: List<ChatMessage>, message: String): String
}

class ApiException(message: String, val httpStatus: Int = 0) : Exception(message)
