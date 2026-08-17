package pl.intertell.technik.data

import java.io.File

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

    /**
     * Downloads the GeoJSON FeatureCollection (fiber runs, cabinets,
     * distribution points — see server's internal/qfield) straight to a
     * local file, rather than a String — even a post-compression few-MB
     * payload is enough to OOM-crash the app if it's round-tripped through a
     * Java String/JSONObject first. [InfrastructureMap.layers] lists the
     * underlying GeoPackage table names for a per-layer visibility toggle.
     */
    suspend fun getInfrastructureGeoJson(): InfrastructureMap

    /**
     * Per-table coloring parsed server-side from the QGIS project's own
     * .qgs file (see server's internal/qfield/qgsproject.go), so the map can
     * match QField's own symbology instead of a generated palette. A table
     * missing from the result just means "no recognized renderer" — not an
     * error.
     */
    suspend fun getInfrastructureStyle(): Map<String, LayerStyle>
}

data class InfrastructureMap(val file: File, val layers: List<String>)

/** [default]/[categories] values are "#rrggbb" hex strings. */
data class LayerStyle(val default: String?, val field: String?, val categories: Map<String, String>)

class ApiException(message: String, val httpStatus: Int = 0) : Exception(message)
