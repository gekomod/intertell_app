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
    /** Validates the stored bearer token and returns the caller's own profile — used to resume a session on app launch. */
    suspend fun getMe(): LoginResult
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

    /** Throws [ApiException] (400/401) if currentPassword doesn't match or newPassword is too short. */
    suspend fun changePassword(currentPassword: String, newPassword: String)
}

data class InfrastructureMap(val file: File, val layers: List<String>)

/** [default]/[categories] values are "#rrggbb" hex strings. */
data class LayerStyle(val default: String?, val field: String?, val categories: Map<String, String>)

/**
 * One independently toggleable/colorable item in the QGIS map's layers
 * menu — either a whole GeoPackage table ([categoryValue] null, for a table
 * with no recognized categorization) or a single category within a table
 * that has one (e.g. one cable-capacity value within "Linie kablowe"),
 * mirroring how QGIS/QField's own legend breaks a categorized layer down
 * into one row per category rather than one row for the whole layer.
 */
data class LayerUnit(val key: String, val table: String, val categoryValue: String?)

private const val UNIT_KEY_SEPARATOR = "||"

/** Every table/category combination the layers menu and map should offer, in a stable order. */
fun computeLayerUnits(layers: List<String>, styles: Map<String, LayerStyle>): List<LayerUnit> =
    layers.flatMap { table ->
        val style = styles[table]
        if (style?.field != null && style.categories.isNotEmpty()) {
            style.categories.keys.sorted().map { value ->
                LayerUnit(key = table + UNIT_KEY_SEPARATOR + value, table = table, categoryValue = value)
            }
        } else {
            listOf(LayerUnit(key = table, table = table, categoryValue = null))
        }
    }

class ApiException(message: String, val httpStatus: Int = 0) : Exception(message)
