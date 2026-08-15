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

enum class JobKind { MESSAGE, INSTALL }

/**
 * A technician's task — either a customer contact-message inquiry or an
 * installation request assigned to them in the intratell backend. There is
 * no separate "scheduled visit" concept server-side (no time/duration/scope
 * fields), so this mirrors exactly what /api/tech/tasks returns.
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
    /** Only ever set for [JobKind.MESSAGE] — install requests aren't linked to a customer record. */
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
    val isUrgent: Boolean get() = kind == JobKind.MESSAGE && status == "new"
}

data class Device(
    val id: Long,
    val kind: String,
    val kindLabel: String,
    val model: String,
    val serial: String,
    val location: String,
    val status: String,
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

data class LmsOnlyMatch(val lmsCustomerId: Long, val name: String)

data class CustomerSearchResult(val customers: List<Customer>, val lmsOnly: List<LmsOnlyMatch>)
