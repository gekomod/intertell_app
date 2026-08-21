package pl.intertell.technik.data

data class TeamMember(
    val id: Long,
    val code: String,
    val name: String,
    val email: String,
    val phone: String,
    val specialization: String,
    val active: Boolean,
) {
    val initials: String
        get() = name.split(" ").mapNotNull { it.firstOrNull()?.uppercaseChar() }.joinToString("")
}

data class LoginResult(val technician: TeamMember, val officePhone: String)

enum class JobKind { MESSAGE, INSTALL, LMS_OUTAGE }

/**
 * A technician's task — a customer contact-message inquiry, an
 * installation request, or an open outage from LMS ("problem techniczny")
 * assigned to them in the intratell backend. There is no separate
 * "scheduled visit" concept server-side (no time/duration/scope fields), so
 * this mirrors exactly what /api/tech/tasks returns. LMS_OUTAGE jobs always
 * arrive with status "in_progress" — LMS has no local "started" concept, so
 * completing one is the only transition, and it also closes the event in
 * LMS itself (see TechnicianRepository.setTaskStatus).
 */
data class Job(
    val id: Long,
    val kind: JobKind,
    val title: String,
    val clientName: String,
    val phone: String,
    val address: String,
    val detail: String,
    val createdAt: String,
    val status: String, // new | assigned | in_progress | done
    /** Set for [JobKind.MESSAGE] and, when the LMS customer is linked to a local one, [JobKind.LMS_OUTAGE] — install requests aren't linked to a customer record. */
    val customerNo: String = "",
    /** Server-geocoded coordinates for [address], when the server could resolve it — see api_tech.go's use of internal/geo. */
    val lat: Double? = null,
    val lon: Double? = null,
) {
    val statusLabel: String
        get() = when (status) {
            "in_progress" -> "W trakcie"
            "done" -> "Zakończone"
            "assigned" -> "Przypisane"
            else -> "Nowe"
        }
    val isUrgent: Boolean get() = (kind == JobKind.MESSAGE && status == "new") || kind == JobKind.LMS_OUTAGE
}

data class OpticalInfo(
    val online: Boolean,
    val txDbm: Double,
    val rxDbm: Double,
    val tempC: Double,
)

data class Device(
    val id: Long,
    val kind: String,
    val kindLabel: String,
    val model: String,
    val serial: String,
    val location: String,
    val status: String,
    /** "" if never set (e.g. a decoder, which has no LMS node of its own). Editable by a technician during a hardware swap — see TechnicianRepository.setDeviceMac. */
    val mac: String = "",
    /** Live signal from the configured Huawei OLT (see server's internal/onu) — null when unavailable (not the router/ONT device, OLT not configured, SNMP lookup failed, ...). */
    val optical: OpticalInfo? = null,
)

data class LmsStatus(
    val balanceCents: Long,
    val balanceZl: String,
    val connectionUp: Boolean,
    val lastSeenOnline: String,
)

data class RouterInfo(
    val firmware: String,
    val uptimeDays: Int,
    val deviceCount: Int,
    val wanIp: String,
    val dmzOn: Boolean,
    val dmzHost: String,
    val proxyOn: Boolean,
    val proxyHost: String,
    val proxyPort: String,
    val vpnClientOn: Boolean,
    val vpnServerOn: Boolean,
    // Router-level operational/security settings a technician can edit
    // from RouterScreen — unlike the customer self-service fields above,
    // which stay read-only in the technician app.
    val wifiOn: Boolean,
    val ipv6On: Boolean,
    val ipv4Mode: String, // "dhcp" | "static"
    val firewallOn: Boolean,
    val wpsOn: Boolean,
)

data class ServiceHistoryEntry(
    val id: Long,
    val topic: String,
    val message: String,
    val createdAt: String,
    val status: String,
    val statusLabel: String,
    val technicianName: String,
)

data class Customer(
    val id: Long,
    val customerNo: String,
    val name: String,
    val address: String,
    val status: String,
    val statusLabel: String,
    val planId: Long,
    val isBusiness: Boolean,
    val lmsCustomerId: Long,
    val devices: List<Device>,
    val lms: LmsStatus?,
    val router: RouterInfo? = null,
    val history: List<ServiceHistoryEntry> = emptyList(),
)

data class DeviceMacResult(val device: Device, val lmsSynced: Boolean)

data class LmsOnlyMatch(val lmsCustomerId: Long, val name: String)

data class CustomerSearchResult(val customers: List<Customer>, val lmsOnly: List<LmsOnlyMatch>)
