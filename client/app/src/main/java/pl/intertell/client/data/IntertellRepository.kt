package pl.intertell.client.data

/**
 * Everything the UI needs from "the backend". [MockIntertellRepository] is a
 * fully working in-memory stand-in for the real LMS integration described in
 * the design brief. Swap it for a Retrofit-backed implementation of this same
 * interface once LMS API credentials/endpoints exist — no UI code should need
 * to change.
 */
interface IntertellRepository {
    suspend fun login(contractOrEmail: String, password: String): Boolean
    fun getAccount(): Account
    fun getServiceStatus(): ServiceStatus
    fun getInvoices(): List<Invoice>
    fun getContractDocuments(): List<ContractDocument>
    fun getPlans(): List<Plan>
    fun getContractEndDate(): String
}
