package pl.intertell.technik

enum class TechScreen { LOGIN, JOBS, JOB, REPORT, SEARCH, CUST, ROUTER, ADMIN }

enum class CustomerFilter { ALL, AWARIA }

data class TechnicianUiState(
    val screen: TechScreen = TechScreen.LOGIN,
    val loginLoading: Boolean = false,
    val loginError: String? = null,
    val jobIndex: Int = 0,
    val customerIndex: Int = 0,
    val jobDone: Boolean = false,
    val deviceAdded: Boolean = false,
    val qosEnabled: Boolean = true,
    val wifi24Enabled: Boolean = true,
    val wifi5Enabled: Boolean = true,
    val meshEnabled: Boolean = true,
    val speedTestRun: Boolean = false,
    val teamMemberInvited: Boolean = false,
    val searchQuery: String = "",
    val searchFilter: CustomerFilter = CustomerFilter.ALL,
) {
    val showBottomBar: Boolean get() = screen != TechScreen.LOGIN && screen != TechScreen.JOB
}
