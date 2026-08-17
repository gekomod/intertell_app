package pl.intertell.client.data.api

import android.content.Context
import java.util.Locale
import org.json.JSONArray
import org.json.JSONObject
import pl.intertell.client.data.Account
import pl.intertell.client.data.ChatMessage
import pl.intertell.client.data.DmzSettings
import pl.intertell.client.data.IntertellRepository
import pl.intertell.client.data.Invoice
import pl.intertell.client.data.Plan
import pl.intertell.client.data.PlanDirection
import pl.intertell.client.data.ServiceStatus

class ApiIntertellRepository(context: Context) : IntertellRepository {
    private val api = ApiClient(context)
    val serverConfig: ServerConfig get() = api.serverConfig

    override suspend fun login(contractOrEmail: String, password: String): Account {
        val response = api.post(
            "/api/client/login",
            JSONObject().put("login", contractOrEmail).put("password", password),
        )
        val token = response.getString("token")
        api.serverConfig.setToken(token)
        return response.getJSONObject("customer").toAccount()
    }

    override suspend fun logout() {
        runCatching { api.post("/api/client/logout") }
        api.serverConfig.setToken(null)
    }

    override suspend fun getAccountAndStatus(): Pair<Account, ServiceStatus> {
        val customer = api.get("/api/client/me").getJSONObject("customer")
        val account = customer.toAccount()
        val plan = customer.optJSONObject("plan")
        val lms = customer.optJSONObject("lms")
        val router = customer.optJSONObject("router")
        val status = ServiceStatus(
            planName = plan?.optString("name").orEmpty(),
            speedLabel = plan?.optString("speed").orEmpty(),
            connectionUp = lms?.let { it.optBoolean("connection_up") },
            routerUptimeDays = router?.let { it.optInt("uptime_days") },
        )
        return account to status
    }

    override suspend fun getDmzSettings(): DmzSettings {
        val router = api.get("/api/client/me").getJSONObject("customer").optJSONObject("router")
        return DmzSettings(
            enabled = router?.optBoolean("dmz_on") ?: false,
            hostIp = router?.optString("dmz_host").orEmpty(),
            publicIp = router?.optString("wan_ip").orEmpty(),
        )
    }

    override suspend fun setDmz(enabled: Boolean, hostIp: String) {
        api.put("/api/client/router/dmz", JSONObject().put("dmz_on", enabled).put("dmz_host", hostIp))
    }

    override suspend fun getInvoices(): List<Invoice> =
        api.get("/api/client/invoices").optJSONArrayOrEmpty("invoices").map { it.toInvoice() }

    override suspend fun getInvoice(id: Long): Invoice =
        api.get("/api/client/invoices/$id").getJSONObject("invoice").toInvoice()

    override suspend fun getPlans(): List<Plan> {
        val plans = api.get("/api/client/plans").optJSONArrayOrEmpty("plans")
        val currentPriceCents = (0 until plans.length())
            .map { plans.getJSONObject(it) }
            .firstOrNull { it.optBoolean("is_current") }
            ?.optLong("price_cents") ?: 0L
        return plans.map { it.toPlan(currentPriceCents) }
    }

    override suspend fun changePlan(planId: Long): Boolean =
        api.post("/api/client/plan", JSONObject().put("plan_id", planId)).optBoolean("applied")

    override suspend fun reportProblem(subject: String, message: String, phone: String) {
        api.post(
            "/api/client/tickets",
            JSONObject().put("subject", subject).put("message", message).put("phone", phone),
        )
    }

    override suspend fun sendChatMessage(history: List<ChatMessage>, message: String): String {
        val historyJSON = JSONArray()
        history.forEach { historyJSON.put(JSONObject().put("role", it.role).put("content", it.content)) }
        val response = api.post(
            "/api/client/chat",
            JSONObject().put("history", historyJSON).put("message", message),
        )
        return response.getString("reply")
    }
}

private fun JSONObject.toAccount(): Account {
    val name = optString("name")
    val address = optString("address")
    val city = address.substringAfterLast(',').trim().ifBlank { address }
    return Account(
        fullName = name,
        initials = name.split(" ").mapNotNull { it.firstOrNull()?.uppercaseChar() }.joinToString(""),
        email = optString("email"),
        contractNumber = optString("customer_no"),
        city = city,
    )
}

private fun JSONObject.toInvoice() = Invoice(
    id = getLong("id"), number = optString("number"), issuedOn = optString("issued_on"), dueOn = optString("due_on"),
    amountZl = optString("amount_zl"), status = optString("status"), statusLabel = optString("status_label"),
)

private fun JSONObject.toPlan(currentPriceCents: Long): Plan {
    val priceCents = optLong("price_cents")
    val isCurrent = optBoolean("is_current")
    val direction = when {
        isCurrent -> PlanDirection.CURRENT
        priceCents > currentPriceCents -> PlanDirection.HIGHER
        else -> PlanDirection.LOWER
    }
    val badge = when (direction) {
        PlanDirection.CURRENT -> "AKTUALNY"
        PlanDirection.HIGHER -> "WYŻEJ"
        PlanDirection.LOWER -> "NIŻEJ"
    }
    val deltaLabel = if (isCurrent) {
        "obecny"
    } else {
        val deltaCents = priceCents - currentPriceCents
        val sign = if (deltaCents >= 0) "+" else "−"
        String.format(Locale("pl", "PL"), "%s%.0f zł", sign, kotlin.math.abs(deltaCents) / 100.0)
    }
    return Plan(
        id = getLong("id"), name = optString("name"), speedLabel = optString("speed"),
        priceLabel = optString("price_zl") + " zł", priceCents = priceCents,
        direction = direction, badge = badge, deltaLabel = deltaLabel,
    )
}
