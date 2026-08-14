package pl.intertell.technik

enum class TechScreen { LOGIN, JOBS, JOB, REPORT, SEARCH, CUST, ROUTER, ADMIN }

data class TechnicianUiState(
    val screen: TechScreen = TechScreen.LOGIN,
    val loginLoading: Boolean = false,
    val loginError: String? = null,
    val serverUrl: String = "",
    val jobsLoading: Boolean = false,
    val jobDone: Boolean = false,
    val actionInFlight: Boolean = false,
    val searchQuery: String = "",
    val searchLoading: Boolean = false,
    val customerDetailLoading: Boolean = false,
    val teamLoading: Boolean = false,
    val errorMessage: String? = null,
    val infoMessage: String? = null,
) {
    val showBottomBar: Boolean get() = screen != TechScreen.LOGIN && screen != TechScreen.JOB
}
