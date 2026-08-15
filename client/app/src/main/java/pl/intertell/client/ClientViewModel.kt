package pl.intertell.client

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.intertell.client.data.Account
import pl.intertell.client.data.DmzSettings
import pl.intertell.client.data.IntertellRepository
import pl.intertell.client.data.Invoice
import pl.intertell.client.data.Plan
import pl.intertell.client.data.ServiceStatus
import pl.intertell.client.data.api.ApiIntertellRepository
import pl.intertell.client.update.UpdateChecker
import pl.intertell.client.update.UpdateInfo
import pl.intertell.client.update.UpdateInstaller

class ClientViewModel(application: Application) : AndroidViewModel(application) {

    private val apiRepository = ApiIntertellRepository(application)
    private val repository: IntertellRepository = apiRepository

    private val _uiState = MutableStateFlow(ClientUiState())
    val uiState: StateFlow<ClientUiState> = _uiState.asStateFlow()

    private val _account = MutableStateFlow<Account?>(null)
    val account: StateFlow<Account?> = _account.asStateFlow()

    private val _serviceStatus = MutableStateFlow<ServiceStatus?>(null)
    val serviceStatus: StateFlow<ServiceStatus?> = _serviceStatus.asStateFlow()

    private val _invoices = MutableStateFlow<List<Invoice>>(emptyList())
    val invoices: StateFlow<List<Invoice>> = _invoices.asStateFlow()

    private val _selectedInvoice = MutableStateFlow<Invoice?>(null)
    val selectedInvoice: StateFlow<Invoice?> = _selectedInvoice.asStateFlow()

    private val _plans = MutableStateFlow<List<Plan>>(emptyList())
    val plans: StateFlow<List<Plan>> = _plans.asStateFlow()

    private val _dmz = MutableStateFlow<DmzSettings?>(null)
    val dmz: StateFlow<DmzSettings?> = _dmz.asStateFlow()

    private val _updateAvailable = MutableStateFlow<UpdateInfo?>(null)
    val updateAvailable: StateFlow<UpdateInfo?> = _updateAvailable.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.update { it.copy(serverUrl = apiRepository.serverConfig.getBaseUrl()) }
        }
        viewModelScope.launch {
            _updateAvailable.value = runCatching { UpdateChecker.checkForUpdate(BuildConfig.BUILD_NUMBER) }.getOrNull()
        }
    }

    fun dismissUpdate() {
        _updateAvailable.value = null
    }

    fun startUpdateDownload() {
        val update = _updateAvailable.value ?: return
        UpdateInstaller.startDownload(getApplication(), update)
        _updateAvailable.value = null
    }

    fun setServerUrl(url: String) {
        viewModelScope.launch {
            apiRepository.serverConfig.setBaseUrl(url)
            _uiState.update { it.copy(serverUrl = apiRepository.serverConfig.getBaseUrl()) }
        }
    }

    fun login(contractOrEmail: String, password: String) {
        if (_uiState.value.loginLoading) return
        viewModelScope.launch {
            _uiState.update { it.copy(loginLoading = true, loginError = null) }
            try {
                _account.value = repository.login(contractOrEmail, password)
                _uiState.update { it.copy(loginLoading = false, screen = ClientScreen.HOME) }
                loadHome()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(loginLoading = false, loginError = e.message) }
            }
        }
    }

    fun logout() {
        viewModelScope.launch { repository.logout() }
        _account.value = null
        _serviceStatus.value = null
        _invoices.value = emptyList()
        _plans.value = emptyList()
        _dmz.value = null
        _uiState.update { ClientUiState(serverUrl = it.serverUrl) }
    }

    fun goHome() = navigate(ClientScreen.HOME).also { loadHome() }
    fun goInvoices() = navigate(ClientScreen.INVOICES).also { loadInvoices() }
    fun goPlan() = navigate(ClientScreen.PLAN).also { loadPlans() }
    fun goSettings() = navigate(ClientScreen.SETTINGS).also { loadDmz() }
    fun goContracts() = navigate(ClientScreen.CONTRACTS)
    fun goDmz() = navigate(ClientScreen.DMZ).also { loadDmz() }
    fun goContact() = navigate(ClientScreen.CONTACT)

    private fun navigate(screen: ClientScreen) = _uiState.update { it.copy(screen = screen) }

    private fun loadHome() {
        viewModelScope.launch {
            _uiState.update { it.copy(homeLoading = true, errorMessage = null) }
            try {
                val (acc, status) = repository.getAccountAndStatus()
                _account.value = acc
                _serviceStatus.value = status
                _invoices.value = repository.getInvoices()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
            }
            _uiState.update { it.copy(homeLoading = false) }
        }
    }

    private fun loadInvoices() {
        viewModelScope.launch {
            _uiState.update { it.copy(invoicesLoading = true, errorMessage = null) }
            try {
                _invoices.value = repository.getInvoices()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
            }
            _uiState.update { it.copy(invoicesLoading = false) }
        }
    }

    fun openInvoice(id: Long) {
        navigate(ClientScreen.INVOICE)
        viewModelScope.launch {
            _uiState.update { it.copy(actionInFlight = true, errorMessage = null) }
            try {
                _selectedInvoice.value = repository.getInvoice(id)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
            }
            _uiState.update { it.copy(actionInFlight = false) }
        }
    }

    private fun loadPlans() {
        viewModelScope.launch {
            _uiState.update { it.copy(plansLoading = true, errorMessage = null) }
            try {
                _plans.value = repository.getPlans()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
            }
            _uiState.update { it.copy(plansLoading = false) }
        }
    }

    private fun loadDmz() {
        viewModelScope.launch {
            _uiState.update { it.copy(dmzLoading = true, errorMessage = null) }
            try {
                _dmz.value = repository.getDmzSettings()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
            }
            _uiState.update { it.copy(dmzLoading = false) }
        }
    }

    fun toggleDmz() {
        val current = _dmz.value ?: return
        val updated = current.copy(enabled = !current.enabled)
        _dmz.value = updated
        viewModelScope.launch {
            try {
                repository.setDmz(updated.enabled, updated.hostIp)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _dmz.value = current // roll back on failure
                _uiState.update { it.copy(errorMessage = e.message) }
            }
        }
    }

    fun openPlanSheet(planId: Long) {
        val plan = _plans.value.firstOrNull { it.id == planId } ?: return
        if (plan.direction == pl.intertell.client.data.PlanDirection.CURRENT) return
        _uiState.update { it.copy(sheetPlanId = planId) }
    }

    fun closeSheet() = _uiState.update { it.copy(sheetPlanId = null) }

    // Changing plans server-side isn't wired up yet (no admin-facing "plan change
    // request" queue exists), so this — like in the original design's own "Wniosek
    // przyjęty" copy for a downgrade — just confirms the intent locally.
    fun confirmPlanChange() = _uiState.update { it.copy(planChangeDone = true) }

    fun closeDone() = _uiState.update { it.copy(planChangeDone = false, sheetPlanId = null, screen = ClientScreen.PLAN) }

    fun currentPlanSheet(): Plan? = _uiState.value.sheetPlanId?.let { id -> _plans.value.firstOrNull { it.id == id } }

    fun toggleEFaktura() = _uiState.update { it.copy(eFakturaOnly = !it.eFakturaOnly) }
    fun togglePush() = _uiState.update { it.copy(pushNotifications = !it.pushNotifications) }
    fun toggleSms() = _uiState.update { it.copy(smsAlerts = !it.smsAlerts) }

    fun openReset() = _uiState.update { it.copy(resetSheetOpen = true) }
    fun closeReset() = _uiState.update { it.copy(resetSheetOpen = false) }
    fun confirmReset() = _uiState.update { it.copy(resetInProgress = true) }
    fun closeResetDone() = _uiState.update {
        it.copy(resetInProgress = false, resetSheetOpen = false, screen = ClientScreen.SETTINGS)
    }

    fun reportProblem(subject: String, message: String, phone: String) {
        if (subject.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Podaj temat zgłoszenia.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(actionInFlight = true, errorMessage = null) }
            try {
                repository.reportProblem(subject, message, phone)
                _uiState.update { it.copy(ticketSent = true) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
            }
            _uiState.update { it.copy(actionInFlight = false) }
        }
    }

    fun closeTicketSent() = _uiState.update { it.copy(ticketSent = false) }

    fun clearError() = _uiState.update { it.copy(errorMessage = null) }
}
