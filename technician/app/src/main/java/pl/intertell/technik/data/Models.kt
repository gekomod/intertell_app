package pl.intertell.technik.data

enum class CustomerState { OK, AWARIA, ZAWIESZONA }

data class HistoryEntry(val what: String, val date: String, val note: String)

data class Customer(
    val address: String,
    val name: String,
    val contract: String,
    val plan: String,
    val ont: String,
    val sn: String,
    val phone: String,
    val rx: String,
    val olt: String,
    val state: CustomerState,
    val history: List<HistoryEntry>,
)

enum class JobStatus { ZAPLANOWANE, PILNE }

data class Job(
    val id: String,
    val time: String,
    val duration: String,
    val type: String,
    val status: JobStatus,
    val scope: String,
    val customerIndex: Int,
)

enum class TechStatus { NA_SLUZBIE, WOLNE, ZAPROSZONY }

data class TeamMember(
    val name: String,
    val id: String,
    val area: String,
    val status: TechStatus,
) {
    val initials: String get() = name.split(" ").mapNotNull { it.firstOrNull() }.joinToString("")
}

data class NetworkClient(val name: String, val ip: String, val link: String, val rate: String)
