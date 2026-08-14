package pl.intertell.client

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.intertell.client.data.IntertellRepository
import pl.intertell.client.data.MockIntertellRepository
import pl.intertell.client.data.PlanDirection

class ClientViewModel(
    private val repository: IntertellRepository = MockIntertellRepository(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(ClientUiState())
    val uiState: StateFlow<ClientUiState> = _uiState.asStateFlow()

    val account = repository.getAccount()
    val serviceStatus = repository.getServiceStatus()
    val invoices = repository.getInvoices()
    val documents = repository.getContractDocuments()
    val plans = repository.getPlans()
    val contractEndDate = repository.getContractEndDate()

    fun login(contractOrEmail: String, password: String) {
        if (_uiState.value.loginLoading) return
        viewModelScope.launch {
            _uiState.update { it.copy(loginLoading = true, loginError = null) }
            val ok = repository.login(contractOrEmail, password)
            _uiState.update {
                if (ok) it.copy(loginLoading = false, screen = ClientScreen.HOME)
                else it.copy(loginLoading = false, loginError = "Podaj numer umowy/e-mail i hasło")
            }
        }
    }

    fun logout() = _uiState.update { ClientUiState() }

    fun goHome() = navigate(ClientScreen.HOME)
    fun goInvoices() = navigate(ClientScreen.INVOICES)
    fun goPlan() = navigate(ClientScreen.PLAN)
    fun goSettings() = navigate(ClientScreen.SETTINGS)
    fun goContracts() = navigate(ClientScreen.CONTRACTS)
    fun goDmz() = navigate(ClientScreen.DMZ)
    fun goContact() = navigate(ClientScreen.CONTACT)

    private fun navigate(screen: ClientScreen) = _uiState.update { it.copy(screen = screen) }

    fun openInvoice(index: Int) = _uiState.update { it.copy(invoiceIndex = index, screen = ClientScreen.INVOICE) }

    fun openPlanSheet(index: Int) {
        if (plans[index].direction == PlanDirection.CURRENT) return
        _uiState.update { it.copy(sheetPlanIndex = index) }
    }

    fun closeSheet() = _uiState.update { it.copy(sheetPlanIndex = null) }

    fun confirmPlanChange() = _uiState.update { it.copy(planChangeDone = true) }

    fun closeDone() = _uiState.update { it.copy(planChangeDone = false, sheetPlanIndex = null, screen = ClientScreen.PLAN) }

    fun toggleEFaktura() = _uiState.update { it.copy(eFakturaOnly = !it.eFakturaOnly) }
    fun togglePush() = _uiState.update { it.copy(pushNotifications = !it.pushNotifications) }
    fun toggleSms() = _uiState.update { it.copy(smsAlerts = !it.smsAlerts) }
    fun toggleDmz() = _uiState.update { it.copy(dmzEnabled = !it.dmzEnabled) }

    fun openReset() = _uiState.update { it.copy(resetSheetOpen = true) }
    fun closeReset() = _uiState.update { it.copy(resetSheetOpen = false) }
    fun confirmReset() = _uiState.update { it.copy(resetInProgress = true) }
    fun closeResetDone() = _uiState.update {
        it.copy(resetInProgress = false, resetSheetOpen = false, screen = ClientScreen.SETTINGS)
    }

    fun currentPlanSheet() = _uiState.value.sheetPlanIndex?.let { plans[it] }
    fun selectedInvoice() = invoices[_uiState.value.invoiceIndex]
}
