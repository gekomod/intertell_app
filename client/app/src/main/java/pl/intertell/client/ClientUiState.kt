package pl.intertell.client

enum class ClientScreen { SPLASH, LOGIN, HOME, INVOICES, INVOICE, CONTRACTS, PLAN, SETTINGS, DMZ, CONTACT, CHAT }

data class ClientUiState(
    val screen: ClientScreen = ClientScreen.SPLASH,
    val serverUrl: String = "",
    val loginLoading: Boolean = false,
    val loginError: String? = null,
    val homeLoading: Boolean = false,
    val invoicesLoading: Boolean = false,
    val plansLoading: Boolean = false,
    val dmzLoading: Boolean = false,
    val actionInFlight: Boolean = false,
    val errorMessage: String? = null,
    val sheetPlanId: Long? = null,
    val planChangeDone: Boolean = false,
    val planChangeApplied: Boolean = false,
    val planChangeLimited: Boolean = false,
    val planChangeMessage: String? = null,
    val resetSheetOpen: Boolean = false,
    val resetInProgress: Boolean = false,
    val ticketSent: Boolean = false,
    val chatSending: Boolean = false,
    // Local-only display preferences — there's no backend model for these yet.
    val eFakturaOnly: Boolean = true,
    val pushNotifications: Boolean = true,
    val smsAlerts: Boolean = false,
    // Persisted via ServerConfig; awaitingBiometric is a one-shot signal for
    // MainActivity to show the OS fingerprint/face prompt while a stored
    // session is being resumed (see ClientViewModel's splash-time logic).
    val biometricEnabled: Boolean = false,
    val awaitingBiometric: Boolean = false,
) {
    val showBottomBar: Boolean get() = screen != ClientScreen.LOGIN && screen != ClientScreen.SPLASH
    val showPlanSheet: Boolean get() = sheetPlanId != null && !planChangeDone
}
