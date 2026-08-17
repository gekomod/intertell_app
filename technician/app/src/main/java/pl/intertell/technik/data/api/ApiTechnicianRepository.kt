package pl.intertell.technik.data.api

import android.content.Context
import java.io.File
import java.net.URLDecoder
import org.json.JSONObject
import pl.intertell.technik.data.Customer
import pl.intertell.technik.data.CustomerSearchResult
import pl.intertell.technik.data.Device
import pl.intertell.technik.data.InfrastructureMap
import pl.intertell.technik.data.Job
import pl.intertell.technik.data.LayerStyle
import pl.intertell.technik.data.JobKind
import pl.intertell.technik.data.LmsOnlyMatch
import pl.intertell.technik.data.LoginResult
import pl.intertell.technik.data.LmsStatus
import pl.intertell.technik.data.OpticalInfo
import pl.intertell.technik.data.RouterInfo
import pl.intertell.technik.data.ServiceHistoryEntry
import pl.intertell.technik.data.TeamMember
import pl.intertell.technik.data.TechnicianRepository

class ApiTechnicianRepository(context: Context) : TechnicianRepository {
    private val appContext = context.applicationContext
    private val api = ApiClient(context)
    val serverConfig: ServerConfig get() = api.serverConfig

    override suspend fun login(code: String, password: String): LoginResult {
        val response = api.post(
            "/api/tech/login",
            JSONObject().put("code", code).put("password", password),
        )
        val token = response.getString("token")
        api.serverConfig.setToken(token)
        return LoginResult(
            technician = response.getJSONObject("technician").toTeamMember(),
            officePhone = response.optString("office_phone"),
        )
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
        val outages = response.optJSONArrayOrEmpty("lms_outages").map { it.toLmsOutageJob() }
        return (messages + installs + outages).sortedByDescending { it.createdAt }
    }

    override suspend fun setTaskStatus(job: Job, status: String) {
        when (job.kind) {
            JobKind.MESSAGE -> api.post("/api/tech/tasks/messages/${job.id}/status", JSONObject().put("status", status))
            JobKind.INSTALL -> api.post("/api/tech/tasks/installs/${job.id}/status", JSONObject().put("status", status))
            // LMS outages support only one transition (complete) — it also
            // closes the event in LMS itself, so there's no separate status
            // payload to send.
            JobKind.LMS_OUTAGE -> api.post("/api/tech/tasks/lms-outages/${job.id}/complete")
        }
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

    // Longer timeout than the default 15s — a cold-cache fetch downloads a
    // file over WebDAV and parses it server-side (see server's
    // internal/qfield), which can genuinely take longer than a normal JSON
    // endpoint. Once cached server-side (10 min TTL), later calls return
    // quickly well within this window regardless.
    //
    // Streamed straight to a file (ApiClient.getToFile) instead of read into
    // a String — avoids ever holding the full HTTP response as a Java String
    // during download; it's read back afterward, but by then it's a bounded
    // few-MB file, not a network response.
    override suspend fun getInfrastructureGeoJson(): InfrastructureMap {
        val dest = File(appContext.cacheDir, "infrastructure.geojson")
        val headers = api.getToFile("/api/tech/infrastruktura", dest, readTimeoutSeconds = 60)
        val layers = headers["X-Qfield-Layers"]
            ?.split(",")
            ?.mapNotNull { runCatching { URLDecoder.decode(it, "UTF-8") }.getOrNull() }
            ?.filter { it.isNotBlank() }
            ?: emptyList()
        return InfrastructureMap(dest, layers)
    }

    override suspend fun getInfrastructureStyle(): Map<String, LayerStyle> {
        val response = api.get("/api/tech/infrastruktura/style", readTimeoutSeconds = 40)
        val out = mutableMapOf<String, LayerStyle>()
        response.keys().forEach { table ->
            val obj = response.getJSONObject(table)
            val categories = obj.optJSONObject("categories")?.let { catObj ->
                catObj.keys().asSequence().associateWith { key -> catObj.getString(key) }
            } ?: emptyMap()
            out[table] = LayerStyle(
                default = obj.optString("default").ifBlank { null },
                field = obj.optString("field").ifBlank { null },
                categories = categories,
            )
        }
        return out
    }
}

private fun JSONObject.toTeamMember() = TeamMember(
    id = getLong("id"), code = optString("code"), name = optString("name"), email = optString("email"),
    phone = optString("phone"), specialization = optString("specialization"),
    active = optBoolean("active", true),
)

private fun JSONObject.optDoubleOrNull(key: String): Double? = if (has(key) && !isNull(key)) optDouble(key) else null

private fun JSONObject.toMessageJob() = Job(
    id = getLong("id"), kind = JobKind.MESSAGE,
    title = optString("topic").ifBlank { "Zapytanie kontaktowe" },
    clientName = optString("name"), phone = optString("phone"), address = optString("address"),
    detail = optString("message"), createdAt = optString("created_at"), status = optString("status", "new"),
    customerNo = optString("customer_no"),
    lat = optDoubleOrNull("lat"), lon = optDoubleOrNull("lon"),
)

private fun JSONObject.toInstallJob() = Job(
    id = getLong("id"), kind = JobKind.INSTALL,
    title = "Zgłoszenie instalacyjne",
    clientName = optString("name"), phone = optString("phone"), address = optString("address"),
    detail = "", createdAt = optString("created_at"), status = optString("status", "new"),
    lat = optDoubleOrNull("lat"), lon = optDoubleOrNull("lon"),
)

private fun JSONObject.toLmsOutageJob() = Job(
    id = getLong("id"), kind = JobKind.LMS_OUTAGE,
    title = optString("title").ifBlank { "Awaria techniczna" },
    clientName = optString("name"), phone = "", address = optString("address"),
    detail = optString("description"), createdAt = optString("created_at"),
    // Always "in_progress" — LMS has no "not yet started" concept for an
    // open event, so the job detail screen goes straight to "Zakończ
    // zgłoszenie" instead of an extra "Rozpocznij" step.
    status = "in_progress",
    customerNo = optString("customer_no"),
    lat = optDoubleOrNull("lat"), lon = optDoubleOrNull("lon"),
)

private fun JSONObject.toDevice() = Device(
    id = getLong("id"), kind = optString("kind"), kindLabel = optString("kind_label"),
    model = optString("model"), serial = optString("serial"), location = optString("location"),
    status = optString("status"), optical = optJSONObject("optical")?.toOpticalInfo(),
)

private fun JSONObject.toOpticalInfo() = OpticalInfo(
    online = optBoolean("online"), txDbm = optDouble("tx_dbm"), rxDbm = optDouble("rx_dbm"), tempC = optDouble("temp_c"),
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
    val history = optJSONArrayOrEmpty("history").map { it.toServiceHistoryEntry() }
    return Customer(
        id = getLong("id"), customerNo = optString("customer_no"), name = optString("name"),
        address = optString("address"), status = optString("status"), statusLabel = optString("status_label"),
        planId = optLong("plan_id"), isBusiness = optBoolean("is_business"),
        lmsCustomerId = optLong("lms_customer_id"), devices = devices, lms = lms, router = router,
        history = history,
    )
}

private fun JSONObject.toServiceHistoryEntry() = ServiceHistoryEntry(
    id = getLong("id"), topic = optString("topic"), message = optString("message"),
    createdAt = optString("created_at"), status = optString("status"),
    statusLabel = optString("status_label"), technicianName = optString("technician_name"),
)
