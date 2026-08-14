package pl.intertell.technik.data

/**
 * Everything the UI needs from "the backend" (intertell.pl account system +
 * LMS + the Huawei ONT panels reached through it). [MockTechnicianRepository]
 * is a fully working in-memory stand-in. Swap it for a real implementation of
 * this interface once those integrations exist — no UI code should change.
 */
interface TechnicianRepository {
    suspend fun login(technicianId: String, password: String): Boolean
    fun getTechnicianId(): String
    fun getTodayLabel(): String
    fun getJobs(): List<Job>
    fun getCustomers(): List<Customer>
    fun getNetworkClients(): List<NetworkClient>
    fun getTeam(): List<TeamMember>
    fun addTeamMember(name: String, email: String, role: String, area: String): TeamMember
}
