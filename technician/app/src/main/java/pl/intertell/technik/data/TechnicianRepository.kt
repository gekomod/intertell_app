package pl.intertell.technik.data

/**
 * Everything the UI needs from the intratell backend's technician API
 * (see the server's internal/handlers/api_tech.go and
 * api_tech_customers.go). [ApiTechnicianRepository] is the only
 * implementation — there's no mock/offline mode, per the requirement that
 * only technicians who actually exist in that database can sign in.
 */
interface TechnicianRepository {
    /** Throws [ApiException] on failure (bad credentials, network error, ...). */
    suspend fun login(code: String, password: String): LoginResult
    suspend fun logout()

    suspend fun getTasks(): List<Job>
    suspend fun setTaskStatus(job: Job, status: String)

    suspend fun searchCustomers(query: String): CustomerSearchResult
    suspend fun getCustomerDetail(id: Long): Customer

    suspend fun getTechnicians(): List<TeamMember>
    suspend fun addTechnician(name: String, email: String, phone: String, specialization: String, password: String): TeamMember
    suspend fun updateTechnician(id: Long, name: String, phone: String, specialization: String, active: Boolean): TeamMember
    suspend fun deleteTechnician(id: Long)
}

class ApiException(message: String, val httpStatus: Int = 0) : Exception(message)
