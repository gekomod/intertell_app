package pl.intertell.client

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.intertell.client.data.Account
import pl.intertell.client.data.ChatMessage
import pl.intertell.client.data.DmzSettings
import pl.intertell.client.data.IntertellRepository
import pl.intertell.client.data.Invoice
import pl.intertell.client.data.Plan
import pl.intertell.client.data.ServiceStatus
import pl.intertell.client.data.api.ApiIntertellRepository
import pl.intertell.client.notifications.ClientPollWorker
import pl.intertell.client.ui.theme.ThemeMode
import pl.intertell.client.update.DownloadProgress
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

    private val _downloadProgress = MutableStateFlow<DownloadProgress?>(null)
    val downloadProgress: StateFlow<DownloadProgress?> = _downloadProgress.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _themeMode = MutableStateFlow(ThemeMode.SYSTEM)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.update { it.copy(serverUrl = apiRepository.serverConfig.getBaseUrl()) }
        }
        viewModelScope.launch {
            _themeMode.value = apiRepository.serverConfig.getThemeMode()
        }
        viewModelScope.launch {
            _uiState.update { it.copy(biometricEnabled = apiRepository.serverConfig.getBiometricEnabled()) }
        }
        viewModelScope.launch {
            _updateAvailable.value = runCatching { UpdateChecker.checkForUpdate(BuildConfig.BUILD_NUMBER) }.getOrNull()
        }
        viewModelScope.launch {
            delay(1600)
            if (_uiState.value.screen != ClientScreen.SPLASH) return@launch
            val hasToken = !apiRepository.serverConfig.getToken().isNullOrBlank()
            when {
                !hasToken -> _uiState.update { it.copy(screen = ClientScreen.LOGIN) }
                apiRepository.serverConfig.getBiometricEnabled() -> _uiState.update { it.copy(awaitingBiometric = true) }
                else -> resumeSession()
            }
        }
        // Foreground top-up for ClientPollWorker's 15-minute floor (an
        // Android WorkManager periodic-work limit, not tunable) — while the
        // app process is alive, check for new invoices/ticket updates much
        // more often so notifications don't feel delayed. Cancelled
        // automatically with this ViewModel; the 15-minute worker still
        // covers the app being fully closed/killed.
        viewModelScope.launch {
            while (true) {
                delay(60_000)
                if (!apiRepository.serverConfig.getToken().isNullOrBlank()) {
                    runCatching { ClientPollWorker.checkOnce(getApplication()) }
                }
            }
        }
    }

    fun dismissUpdate() {
        _updateAvailable.value = null
    }

    fun startUpdateDownload() {
        val update = _updateAvailable.value ?: return
        _updateAvailable.value = null
        viewModelScope.launch {
            UpdateInstaller.download(getApplication(), update).collect { progress ->
                _downloadProgress.value = progress
                if (progress.done || progress.failed) {
                    delay(if (progress.failed) 2500 else 700)
                    _downloadProgress.value = null
                }
            }
        }
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
        _chatMessages.value = emptyList()
        // Explicit LOGIN, not the ClientUiState() default (SPLASH) — the
        // splash→login timer in init{} only ever fires once at app startup,
        // so resetting to its default screen left logout stuck on Splash.
        // biometricEnabled is a standing preference, not session state —
        // carried over so logging back in doesn't silently turn it off.
        _uiState.update { ClientUiState(serverUrl = it.serverUrl, screen = ClientScreen.LOGIN, biometricEnabled = it.biometricEnabled) }
    }

    /** Validates the stored token and, if it's still good, jumps straight to Home. Falls back to a fresh login otherwise. */
    private fun resumeSession() {
        viewModelScope.launch {
            try {
                repository.getAccountAndStatus() // just a validity check — loadHome() below does the real state population
                _uiState.update { it.copy(screen = ClientScreen.HOME) }
                loadHome()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                apiRepository.serverConfig.setToken(null)
                _uiState.update { it.copy(screen = ClientScreen.LOGIN, loginError = "Sesja wygasła — zaloguj się ponownie.") }
            }
        }
    }

    /** Called by MainActivity once the OS biometric prompt (triggered by awaitingBiometric) resolves. */
    fun onBiometricSuccess() {
        _uiState.update { it.copy(awaitingBiometric = false) }
        resumeSession()
    }

    fun onBiometricFailed() {
        _uiState.update { it.copy(awaitingBiometric = false, screen = ClientScreen.LOGIN) }
    }

    fun toggleBiometric() {
        val enabled = !_uiState.value.biometricEnabled
        _uiState.update { it.copy(biometricEnabled = enabled) }
        viewModelScope.launch { apiRepository.serverConfig.setBiometricEnabled(enabled) }
    }

    fun goHome() = navigate(ClientScreen.HOME).also { loadHome() }
    fun goInvoices() = navigate(ClientScreen.INVOICES).also { loadInvoices() }
    fun goPlan() = navigate(ClientScreen.PLAN).also { loadPlans() }
    fun goSettings() = navigate(ClientScreen.SETTINGS).also { loadDmz() }
    fun goContracts() = navigate(ClientScreen.CONTRACTS)
    fun goDmz() = navigate(ClientScreen.DMZ).also { loadDmz() }
    fun goContact() = navigate(ClientScreen.CONTACT)
    fun goChat() = navigate(ClientScreen.CHAT)

    private fun navigate(screen: ClientScreen) = _uiState.update { it.copy(screen = screen) }

    private fun loadHome() {
        viewModelScope.launch {
            _uiState.update { it.copy(homeLoading = true, errorMessage = null) }
            try {
                val (acc, status) = repository.getAccountAndStatus()
                _account.value = acc
                _serviceStatus.value = status
                val invoices = repository.getInvoices()
                _invoices.value = invoices
                // Seen live on Home — the poller shouldn't notify about
                // these later just because it polls next (mirrors the
                // technician app's refreshTasks()).
                apiRepository.serverConfig.addSeenInvoiceIds(invoices.map { it.id.toString() })
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
                val invoices = repository.getInvoices()
                _invoices.value = invoices
                apiRepository.serverConfig.addSeenInvoiceIds(invoices.map { it.id.toString() })
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

    fun confirmPlanChange() {
        val planId = _uiState.value.sheetPlanId ?: return
        if (_uiState.value.actionInFlight) return
        viewModelScope.launch {
            _uiState.update { it.copy(actionInFlight = true, errorMessage = null) }
            try {
                val result = repository.changePlan(planId)
                _plans.value = repository.getPlans()
                if (result.applied) {
                    val (acc, status) = repository.getAccountAndStatus()
                    _account.value = acc
                    _serviceStatus.value = status
                }
                _uiState.update {
                    it.copy(
                        planChangeDone = true,
                        planChangeApplied = result.applied,
                        planChangeLimited = result.limited,
                        planChangeMessage = result.message,
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
            }
            _uiState.update { it.copy(actionInFlight = false) }
        }
    }

    fun closeDone() = _uiState.update {
        it.copy(
            planChangeDone = false, planChangeApplied = false, planChangeLimited = false, planChangeMessage = null,
            sheetPlanId = null, screen = ClientScreen.PLAN,
        )
    }

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

    fun sendChatMessage(text: String) {
        if (text.isBlank() || _uiState.value.chatSending) return
        val history = _chatMessages.value
        _chatMessages.value = history + ChatMessage("user", text)
        _uiState.update { it.copy(chatSending = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                val reply = repository.sendChatMessage(history, text)
                _chatMessages.value = _chatMessages.value + ChatMessage("assistant", reply)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _chatMessages.value = _chatMessages.value + ChatMessage("assistant", e.message ?: "Czat AI jest chwilowo niedostępny.")
            }
            _uiState.update { it.copy(chatSending = false) }
        }
    }

    fun clearError() = _uiState.update { it.copy(errorMessage = null) }

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
        viewModelScope.launch { apiRepository.serverConfig.setThemeMode(mode) }
    }
}
