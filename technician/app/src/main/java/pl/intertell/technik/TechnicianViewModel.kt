package pl.intertell.technik

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job as CoroutineJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.intertell.technik.data.Customer
import pl.intertell.technik.data.CustomerSearchResult
import pl.intertell.technik.data.InfrastructureMap
import pl.intertell.technik.data.Job
import pl.intertell.technik.data.LayerStyle
import pl.intertell.technik.data.computeLayerUnits
import pl.intertell.technik.data.TeamMember
import pl.intertell.technik.data.TechnicianRepository
import pl.intertell.technik.data.api.ApiTechnicianRepository
import pl.intertell.technik.data.geo.LatLng
import pl.intertell.technik.data.geo.NominatimGeocoder
import pl.intertell.technik.data.geo.OsrmRouter
import pl.intertell.technik.data.geo.RouteInfo
import pl.intertell.technik.notifications.TaskPollWorker
import pl.intertell.technik.ui.theme.ThemeMode
import pl.intertell.technik.update.DownloadProgress
import pl.intertell.technik.update.UpdateChecker
import pl.intertell.technik.update.UpdateInfo
import pl.intertell.technik.update.UpdateInstaller

class TechnicianViewModel(application: Application) : AndroidViewModel(application) {

    private val apiRepository = ApiTechnicianRepository(application)
    private val repository: TechnicianRepository = apiRepository

    private val _uiState = MutableStateFlow(TechnicianUiState())
    val uiState: StateFlow<TechnicianUiState> = _uiState.asStateFlow()

    private val _me = MutableStateFlow<TeamMember?>(null)
    val me: StateFlow<TeamMember?> = _me.asStateFlow()

    private val _officePhone = MutableStateFlow("")
    val officePhone: StateFlow<String> = _officePhone.asStateFlow()

    private val _jobs = MutableStateFlow<List<Job>>(emptyList())
    val jobs: StateFlow<List<Job>> = _jobs.asStateFlow()

    private val _selectedJob = MutableStateFlow<Job?>(null)
    val selectedJob: StateFlow<Job?> = _selectedJob.asStateFlow()

    private val _searchResult = MutableStateFlow(CustomerSearchResult(emptyList(), emptyList()))
    val searchResult: StateFlow<CustomerSearchResult> = _searchResult.asStateFlow()

    private val _selectedCustomer = MutableStateFlow<Customer?>(null)
    val selectedCustomer: StateFlow<Customer?> = _selectedCustomer.asStateFlow()

    private val _team = MutableStateFlow<List<TeamMember>>(emptyList())
    val team: StateFlow<List<TeamMember>> = _team.asStateFlow()

    /** address -> coordinates, or null if geocoding that address failed. Absent key = not yet looked up. */
    private val _geocodeCache = MutableStateFlow<Map<String, LatLng?>>(emptyMap())
    val geocodeCache: StateFlow<Map<String, LatLng?>> = _geocodeCache.asStateFlow()

    /** address -> driving route from the technician's current position, or null if unavailable (no fix/permission/API error). */
    private val _routeCache = MutableStateFlow<Map<String, RouteInfo?>>(emptyMap())
    val routeCache: StateFlow<Map<String, RouteInfo?>> = _routeCache.asStateFlow()

    private val _updateAvailable = MutableStateFlow<UpdateInfo?>(null)
    val updateAvailable: StateFlow<UpdateInfo?> = _updateAvailable.asStateFlow()

    private val _downloadProgress = MutableStateFlow<DownloadProgress?>(null)
    val downloadProgress: StateFlow<DownloadProgress?> = _downloadProgress.asStateFlow()

    private val _infrastructureGeoJson = MutableStateFlow<InfrastructureMap?>(null)
    val infrastructureGeoJson: StateFlow<InfrastructureMap?> = _infrastructureGeoJson.asStateFlow()

    private val _infrastructureLoading = MutableStateFlow(false)
    val infrastructureLoading: StateFlow<Boolean> = _infrastructureLoading.asStateFlow()

    private val _infrastructureError = MutableStateFlow<String?>(null)
    val infrastructureError: StateFlow<String?> = _infrastructureError.asStateFlow()

    /** Layer names currently shown on the QGIS map — defaults to all of them once loaded. */
    private val _visibleInfrastructureLayers = MutableStateFlow<Set<String>>(emptySet())
    val visibleInfrastructureLayers: StateFlow<Set<String>> = _visibleInfrastructureLayers.asStateFlow()

    private val _infrastructureStyles = MutableStateFlow<Map<String, LayerStyle>>(emptyMap())
    val infrastructureStyles: StateFlow<Map<String, LayerStyle>> = _infrastructureStyles.asStateFlow()

    private val _themeMode = MutableStateFlow(ThemeMode.SYSTEM)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private var searchJob: CoroutineJob? = null

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
        // No splash screen in this app — the stored-session check just runs
        // immediately; LoginScreen shows a bare spinner (state.checkingSession)
        // until it resolves, so the login form doesn't flash before a
        // resumed/biometric-gated session jumps straight to Jobs.
        viewModelScope.launch {
            val hasToken = !apiRepository.serverConfig.getToken().isNullOrBlank()
            when {
                !hasToken -> _uiState.update { it.copy(checkingSession = false) }
                apiRepository.serverConfig.getBiometricEnabled() -> _uiState.update { it.copy(awaitingBiometric = true) }
                else -> resumeSession()
            }
        }
        // Foreground top-up for TaskPollWorker's 15-minute floor (an
        // Android WorkManager periodic-work limit, not tunable) — while the
        // app process is alive, check for new zlecenia much more often so
        // notifications don't feel delayed. Cancelled automatically with
        // this ViewModel; the 15-minute worker still covers the app being
        // fully closed/killed.
        viewModelScope.launch {
            while (true) {
                delay(60_000)
                if (!apiRepository.serverConfig.getToken().isNullOrBlank()) {
                    runCatching { TaskPollWorker.checkOnce(getApplication()) }
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

    fun login(code: String, password: String) {
        if (_uiState.value.loginLoading) return
        viewModelScope.launch {
            _uiState.update { it.copy(loginLoading = true, loginError = null) }
            try {
                val result = repository.login(code, password)
                _me.value = result.technician
                _officePhone.value = result.officePhone
                _uiState.update { it.copy(loginLoading = false, screen = TechScreen.JOBS) }
                refreshTasks()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(loginLoading = false, loginError = e.message) }
            }
        }
    }

    fun logout() {
        viewModelScope.launch { repository.logout() }
        _me.value = null
        _officePhone.value = ""
        _jobs.value = emptyList()
        _team.value = emptyList()
        _selectedJob.value = null
        _selectedCustomer.value = null
        _infrastructureGeoJson.value = null
        _infrastructureError.value = null
        _visibleInfrastructureLayers.value = emptySet()
        _infrastructureStyles.value = emptyMap()
        // biometricEnabled is a standing preference, not session state —
        // carried over so logging back in doesn't silently turn it off.
        _uiState.update { TechnicianUiState(serverUrl = it.serverUrl, checkingSession = false, biometricEnabled = it.biometricEnabled) }
    }

    /** Validates the stored token and, if it's still good, jumps straight to Jobs. Falls back to a fresh login otherwise. */
    private fun resumeSession() {
        viewModelScope.launch {
            try {
                val result = repository.getMe()
                _me.value = result.technician
                _officePhone.value = result.officePhone
                _uiState.update { it.copy(screen = TechScreen.JOBS, checkingSession = false) }
                refreshTasks()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                apiRepository.serverConfig.setToken(null)
                _uiState.update { it.copy(checkingSession = false, loginError = "Sesja wygasła — zaloguj się ponownie.") }
            }
        }
    }

    /** Called by MainActivity once the OS biometric prompt (triggered by awaitingBiometric) resolves. */
    fun onBiometricSuccess() {
        _uiState.update { it.copy(awaitingBiometric = false) }
        resumeSession()
    }

    fun onBiometricFailed() {
        _uiState.update { it.copy(awaitingBiometric = false, checkingSession = false) }
    }

    fun toggleBiometric() {
        val enabled = !_uiState.value.biometricEnabled
        _uiState.update { it.copy(biometricEnabled = enabled) }
        viewModelScope.launch { apiRepository.serverConfig.setBiometricEnabled(enabled) }
    }

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
        viewModelScope.launch { apiRepository.serverConfig.setThemeMode(mode) }
    }

    fun changePassword(currentPassword: String, newPassword: String) {
        if (_uiState.value.passwordChangeInFlight) return
        viewModelScope.launch {
            _uiState.update { it.copy(passwordChangeInFlight = true, passwordChangeError = null) }
            try {
                repository.changePassword(currentPassword, newPassword)
                _uiState.update { it.copy(passwordChangeDone = true) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(passwordChangeError = e.message) }
            }
            _uiState.update { it.copy(passwordChangeInFlight = false) }
        }
    }

    fun clearPasswordChangeDone() = _uiState.update { it.copy(passwordChangeDone = false, passwordChangeError = null) }

    fun goJobs() = navigate(TechScreen.JOBS).also { refreshTasks() }
    fun goAdmin() = navigate(TechScreen.ADMIN).also { refreshTeam() }
    fun goSearch() = navigate(TechScreen.SEARCH).also {
        if (_searchResult.value.customers.isEmpty()) runSearch(_uiState.value.searchQuery)
    }
    fun goReport() = navigate(TechScreen.REPORT)
    fun goRouter() = navigate(TechScreen.ROUTER).also { loadFullCustomerDetail() }
    fun goCust() = navigate(TechScreen.CUST)
    fun goJob() = navigate(TechScreen.JOB)
    fun goQgis() = navigate(TechScreen.QGIS).also { loadInfrastructure() }
    fun goSettings() = navigate(TechScreen.SETTINGS)

    private fun loadInfrastructure() {
        if (_infrastructureGeoJson.value?.file?.exists() == true) return // loaded once per session — the network map doesn't change minute to minute
        viewModelScope.launch {
            _infrastructureLoading.value = true
            try {
                val map = repository.getInfrastructureGeoJson()
                _infrastructureGeoJson.value = map
                _infrastructureError.value = null
                // Best-effort — a missing/unparseable .qgs on the server
                // just means generated colors instead of QGIS's own, not a
                // reason to fail the whole map.
                val styles = runCatching { repository.getInfrastructureStyle() }.getOrDefault(emptyMap())
                _infrastructureStyles.value = styles
                // The visible set is keyed per menu row (a whole table, or
                // one category within a categorized table — see
                // computeLayerUnits) — computed only now that styles are
                // known, so a categorized table starts with every one of
                // its categories checked, not the table as a single unit.
                _visibleInfrastructureLayers.value = computeLayerUnits(map.layers, styles).map { it.key }.toSet()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _infrastructureError.value = e.message
            }
            _infrastructureLoading.value = false
        }
    }

    fun toggleInfrastructureLayer(layer: String) {
        _visibleInfrastructureLayers.update { if (layer in it) it - layer else it + layer }
    }

    fun geocodeAddress(address: String) {
        if (address.isBlank() || _geocodeCache.value.containsKey(address)) return
        viewModelScope.launch {
            val result = runCatching { NominatimGeocoder.geocode(address) }.getOrNull()
            _geocodeCache.update { it + (address to result) }
        }
    }

    /** Coordinates the server already resolved (see Job.lat/lon) — skips the client-side geocode entirely. */
    fun seedLocation(address: String, lat: Double, lon: Double) {
        _geocodeCache.update { it + (address to LatLng(lat, lon)) }
    }

    /** Needs both a resolved geocode for [address] and a location permission grant with a recent fix — silently no-ops otherwise. */
    fun computeRoute(address: String) {
        if (_routeCache.value.containsKey(address)) return
        val destination = _geocodeCache.value[address] ?: return
        val origin = lastKnownLocation() ?: return
        viewModelScope.launch {
            val result = runCatching { OsrmRouter.route(origin, destination) }.getOrNull()
            _routeCache.update { it + (address to result) }
        }
    }

    private fun lastKnownLocation(): LatLng? {
        val context = getApplication<Application>()
        val hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (!hasPermission) return null
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null
        return try {
            locationManager.getProviders(true)
                .mapNotNull { locationManager.getLastKnownLocation(it) }
                .maxByOrNull { it.time }
                ?.let { LatLng(it.latitude, it.longitude) }
        } catch (e: SecurityException) {
            null
        }
    }

    private fun navigate(screen: TechScreen) = _uiState.update { it.copy(screen = screen) }

    // --- Jobs (Zlecenia) ---

    fun refreshTasks() {
        viewModelScope.launch {
            _uiState.update { it.copy(jobsLoading = true, errorMessage = null) }
            try {
                val tasks = repository.getTasks()
                _jobs.value = tasks
                // Seen live in the Jobs screen — TaskPollWorker shouldn't
                // notify about these later just because it polls next.
                apiRepository.serverConfig.addSeenTaskKeys(tasks.map { "${it.kind}:${it.id}" })
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
            }
            _uiState.update { it.copy(jobsLoading = false) }
        }
    }

    fun openJob(job: Job) {
        _selectedJob.value = job
        navigate(TechScreen.JOB)
    }

    fun startJob() {
        val job = _selectedJob.value ?: return
        setSelectedJobStatus(job, "in_progress")
        navigate(TechScreen.REPORT)
    }

    fun finishJob() {
        val job = _selectedJob.value ?: return
        setSelectedJobStatus(job, "done") { _uiState.update { it.copy(jobDone = true) } }
    }

    private fun setSelectedJobStatus(job: Job, status: String, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            _uiState.update { it.copy(actionInFlight = true) }
            try {
                repository.setTaskStatus(job, status)
                val updated = job.copy(status = status)
                _selectedJob.value = updated
                _jobs.update { list -> list.map { if (it.id == job.id && it.kind == job.kind) updated else it } }
                onDone()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
            }
            _uiState.update { it.copy(actionInFlight = false) }
        }
    }

    fun closeJobDone() {
        _uiState.update { it.copy(jobDone = false, screen = TechScreen.JOBS) }
        refreshTasks()
    }

    /** Looks up the customer card for the selected job's customer_no, if any, and opens it. */
    fun openCustomerForSelectedJob() {
        val customerNo = _selectedJob.value?.customerNo?.trim().orEmpty()
        if (customerNo.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "To zgłoszenie nie ma powiązanego numeru klienta.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(actionInFlight = true) }
            try {
                val result = repository.searchCustomers(customerNo)
                val match = result.customers.firstOrNull()
                if (match == null) {
                    _uiState.update { it.copy(errorMessage = "Nie znaleziono klienta o numerze $customerNo.") }
                } else {
                    _selectedCustomer.value = match
                    navigate(TechScreen.CUST)
                    loadFullCustomerDetail()
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
            }
            _uiState.update { it.copy(actionInFlight = false) }
        }
    }

    // --- Customers (Klienci) ---

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300) // debounce so we don't fire a request per keystroke
            runSearch(query)
        }
    }

    private fun runSearch(query: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(searchLoading = true) }
            try {
                _searchResult.value = repository.searchCustomers(query)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
            }
            _uiState.update { it.copy(searchLoading = false) }
        }
    }

    fun openCustomer(customer: Customer) {
        _selectedCustomer.value = customer
        navigate(TechScreen.CUST)
        loadFullCustomerDetail()
    }

    // search/job results only carry the summary customerJSON() (name, address, devices, lms) —
    // history and router settings are only present on the /customers/{id} detail response, so
    // every entry point into the Customer screen must trigger this, not just the Router button.
    private var detailLoadedForCustomerId: Long? = null

    private fun loadFullCustomerDetail() {
        val current = _selectedCustomer.value ?: return
        if (detailLoadedForCustomerId == current.id) return // already have it
        viewModelScope.launch {
            _uiState.update { it.copy(customerDetailLoading = true) }
            try {
                _selectedCustomer.value = repository.getCustomerDetail(current.id)
                detailLoadedForCustomerId = current.id
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
            }
            _uiState.update { it.copy(customerDetailLoading = false) }
        }
    }

    /** [field] is one of "wifi", "ipv6", "firewall", "wps" — see RouterScreen. */
    fun toggleRouterSetting(field: String) {
        val customer = _selectedCustomer.value ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(actionInFlight = true) }
            try {
                val router = repository.toggleRouterSetting(customer.id, field)
                _selectedCustomer.update { it?.copy(router = router) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
            }
            _uiState.update { it.copy(actionInFlight = false) }
        }
    }

    /** [mode] is "dhcp" or "static". */
    fun setRouterIPv4Mode(mode: String) {
        val customer = _selectedCustomer.value ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(actionInFlight = true) }
            try {
                val router = repository.setRouterIPv4Mode(customer.id, mode)
                _selectedCustomer.update { it?.copy(router = router) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
            }
            _uiState.update { it.copy(actionInFlight = false) }
        }
    }

    fun setRouterDMZHost(host: String) {
        val customer = _selectedCustomer.value ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(actionInFlight = true) }
            try {
                val router = repository.setRouterDMZHost(customer.id, host)
                _selectedCustomer.update { it?.copy(router = router) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
            }
            _uiState.update { it.copy(actionInFlight = false) }
        }
    }

    fun setRouterProxy(host: String, port: String) {
        val customer = _selectedCustomer.value ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(actionInFlight = true) }
            try {
                val router = repository.setRouterProxy(customer.id, host, port)
                _selectedCustomer.update { it?.copy(router = router) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
            }
            _uiState.update { it.copy(actionInFlight = false) }
        }
    }

    // --- Team (Zespół) ---

    fun refreshTeam() {
        viewModelScope.launch {
            _uiState.update { it.copy(teamLoading = true) }
            try {
                _team.value = repository.getTechnicians()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
            }
            _uiState.update { it.copy(teamLoading = false) }
        }
    }

    fun addTechnician(name: String, email: String, phone: String, specialization: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(actionInFlight = true, errorMessage = null) }
            try {
                val created = repository.addTechnician(name, email, phone, specialization, password)
                _team.update { listOf(created) + it }
                _uiState.update { it.copy(infoMessage = "Zaproszenie wysłane do ${created.name}.") }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
            }
            _uiState.update { it.copy(actionInFlight = false) }
        }
    }

    fun updateTechnician(id: Long, name: String, phone: String, specialization: String, active: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(actionInFlight = true, errorMessage = null) }
            try {
                val updated = repository.updateTechnician(id, name, phone, specialization, active)
                _team.update { list -> list.map { if (it.id == id) updated else it } }
                _uiState.update { it.copy(infoMessage = "Zapisano zmiany.") }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
            }
            _uiState.update { it.copy(actionInFlight = false) }
        }
    }

    fun deleteTechnician(id: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(actionInFlight = true, errorMessage = null) }
            try {
                repository.deleteTechnician(id)
                _team.update { list -> list.filterNot { it.id == id } }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
            }
            _uiState.update { it.copy(actionInFlight = false) }
        }
    }

    fun clearMessages() = _uiState.update { it.copy(errorMessage = null, infoMessage = null) }
}
