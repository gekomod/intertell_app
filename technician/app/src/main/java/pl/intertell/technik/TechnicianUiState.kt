package pl.intertell.technik

enum class TechScreen { LOGIN, JOBS, JOB, REPORT, SEARCH, CUST, ROUTER, ADMIN, QGIS, SETTINGS }

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
    // True until the stored-session check (see TechnicianViewModel's init)
    // resolves — LoginScreen shows a bare spinner instead of the form while
    // true, since resuming or gating behind a biometric prompt both start
    // there before this ever settles to a definite outcome.
    val checkingSession: Boolean = true,
    val biometricEnabled: Boolean = false,
    val awaitingBiometric: Boolean = false,
    val passwordChangeInFlight: Boolean = false,
    val passwordChangeError: String? = null,
    val passwordChangeDone: Boolean = false,
) {
    val showBottomBar: Boolean get() = screen != TechScreen.LOGIN && screen != TechScreen.JOB
}
