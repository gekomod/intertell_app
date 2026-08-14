package pl.intertell.client

enum class ClientScreen { LOGIN, HOME, INVOICES, INVOICE, CONTRACTS, PLAN, SETTINGS, DMZ, CONTACT }

data class ClientUiState(
    val screen: ClientScreen = ClientScreen.LOGIN,
    val loginLoading: Boolean = false,
    val loginError: String? = null,
    val invoiceIndex: Int = 0,
    val sheetPlanIndex: Int? = null,
    val planChangeDone: Boolean = false,
    val eFakturaOnly: Boolean = true,
    val pushNotifications: Boolean = true,
    val smsAlerts: Boolean = false,
    val dmzEnabled: Boolean = false,
    val resetSheetOpen: Boolean = false,
    val resetInProgress: Boolean = false,
) {
    val showBottomBar: Boolean get() = screen != ClientScreen.LOGIN
    val showPlanSheet: Boolean get() = sheetPlanIndex != null && !planChangeDone
}
