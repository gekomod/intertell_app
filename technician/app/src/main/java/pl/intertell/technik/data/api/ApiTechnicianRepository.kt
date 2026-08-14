package pl.intertell.technik.data.api

import android.content.Context
import org.json.JSONObject
import pl.intertell.technik.data.Customer
import pl.intertell.technik.data.CustomerSearchResult
import pl.intertell.technik.data.Device
import pl.intertell.technik.data.Job
import pl.intertell.technik.data.JobKind
import pl.intertell.technik.data.LmsOnlyMatch
import pl.intertell.technik.data.LmsStatus
import pl.intertell.technik.data.RouterInfo
import pl.intertell.technik.data.TeamMember
import pl.intertell.technik.data.TechnicianRepository

class ApiTechnicianRepository(context: Context) : TechnicianRepository {
    private val api = ApiClient(context)
    val serverConfig: ServerConfig get() = api.serverConfig

    override suspend fun login(email: String, password: String): TeamMember {
        val response = api.post(
            "/api/tech/login",
            JSONObject().put("email", email).put("password", password),
        )
        val token = response.getString("token")
        api.serverConfig.setToken(token)
        return response.getJSONObject("technician").toTeamMember()
    }

    override suspend fun logout() {
        runCatching { api.post("/api/tech/logout") }
        api.serverConfig.setToken(null)
        api.serverConfig.clearSeenTaskKeys()
    }

    override suspend fun getTasks(): List<Job> {
        val response = api.get("/api/tech/tasks")
        val messages = response.optJSONArrayOrEmpty("contact_messages").map { it.toMessageJob() }
        val installs = response.optJSONArrayOrEmpty("install_requests").map { it.toInstallJob() }
        return (messages + installs).sortedByDescending { it.createdAt }
    }

    override suspend fun setTaskStatus(job: Job, status: String) {
        val path = when (job.kind) {
            JobKind.MESSAGE -> "/api/tech/tasks/messages/${job.id}/status"
            JobKind.INSTALL -> "/api/tech/tasks/installs/${job.id}/status"
        }
        api.post(path, JSONObject().put("status", status))
    }

    override suspend fun searchCustomers(query: String): CustomerSearchResult {
        val path = if (query.isBlank()) "/api/tech/customers" else "/api/tech/customers?q=" + java.net.URLEncoder.encode(query, "UTF-8")
        val response = api.get(path)
        val customers = response.optJSONArrayOrEmpty("customers").map { it.toCustomer() }
        val lmsOnly = response.optJSONArrayOrEmpty("lms_only").map {
            LmsOnlyMatch(it.getLong("lms_customer_id"), it.optString("name"))
        }
        return CustomerSearchResult(customers, lmsOnly)
    }

    override suspend fun getCustomerDetail(id: Long): Customer {
        val response = api.get("/api/tech/customers/$id")
        return response.getJSONObject("customer").toCustomer()
    }

    override suspend fun getTechnicians(): List<TeamMember> {
        val response = api.get("/api/tech/technicians")
        return response.optJSONArrayOrEmpty("technicians").map { it.toTeamMember() }
    }

    override suspend fun addTechnician(name: String, email: String, phone: String, specialization: String, password: String): TeamMember {
        val body = JSONObject()
            .put("name", name).put("email", email).put("phone", phone)
            .put("specialization", specialization).put("password", password).put("active", true)
        val response = api.post("/api/tech/technicians", body)
        return response.getJSONObject("technician").toTeamMember()
    }

    override suspend fun updateTechnician(id: Long, name: String, phone: String, specialization: String, active: Boolean): TeamMember {
        val body = JSONObject()
            .put("name", name).put("phone", phone).put("specialization", specialization).put("active", active)
        val response = api.put("/api/tech/technicians/$id", body)
        return response.getJSONObject("technician").toTeamMember()
    }

    override suspend fun deleteTechnician(id: Long) {
        api.delete("/api/tech/technicians/$id")
    }
}

private fun JSONObject.toTeamMember() = TeamMember(
    id = getLong("id"), name = optString("name"), email = optString("email"),
    phone = optString("phone"), specialization = optString("specialization"),
    active = optBoolean("active", true),
)

private fun JSONObject.toMessageJob() = Job(
    id = getLong("id"), kind = JobKind.MESSAGE,
    title = optString("topic").ifBlank { "Zapytanie kontaktowe" },
    clientName = optString("name"), phone = optString("phone"), address = "",
    detail = optString("message"), createdAt = optString("created_at"), status = optString("status", "new"),
    customerNo = optString("customer_no"),
)

private fun JSONObject.toInstallJob() = Job(
    id = getLong("id"), kind = JobKind.INSTALL,
    title = "Zgłoszenie instalacyjne",
    clientName = optString("name"), phone = optString("phone"), address = optString("address"),
    detail = "", createdAt = optString("created_at"), status = optString("status", "new"),
)

private fun JSONObject.toDevice() = Device(
    id = getLong("id"), kind = optString("kind"), kindLabel = optString("kind_label"),
    model = optString("model"), serial = optString("serial"), location = optString("location"),
    status = optString("status"),
)

private fun JSONObject.toCustomer(): Customer {
    val devices = optJSONArrayOrEmpty("devices").map { it.toDevice() }
    val lms = optJSONObject("lms")?.let {
        LmsStatus(
            balanceCents = it.optLong("balance_cents"), balanceZl = it.optString("balance_zl"),
            connectionUp = it.optBoolean("connection_up"), lastSeenOnline = it.optString("last_seen_online"),
        )
    }
    val router = optJSONObject("router")?.let {
        RouterInfo(
            firmware = it.optString("firmware"), uptimeDays = it.optInt("uptime_days"),
            deviceCount = it.optInt("device_count"), wanIp = it.optString("wan_ip"),
            dmzOn = it.optBoolean("dmz_on"), dmzHost = it.optString("dmz_host"),
            proxyOn = it.optBoolean("proxy_on"), proxyHost = it.optString("proxy_host"), proxyPort = it.optString("proxy_port"),
            vpnClientOn = it.optBoolean("vpn_client_on"), vpnServerOn = it.optBoolean("vpn_server_on"),
        )
    }
    return Customer(
        id = getLong("id"), customerNo = optString("customer_no"), name = optString("name"),
        address = optString("address"), status = optString("status"), statusLabel = optString("status_label"),
        planId = optLong("plan_id"), isBusiness = optBoolean("is_business"),
        lmsCustomerId = optLong("lms_customer_id"), devices = devices, lms = lms, router = router,
    )
}
