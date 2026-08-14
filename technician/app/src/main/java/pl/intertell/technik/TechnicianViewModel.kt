package pl.intertell.technik

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job as CoroutineJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.intertell.technik.data.ApiException
import pl.intertell.technik.data.Customer
import pl.intertell.technik.data.CustomerSearchResult
import pl.intertell.technik.data.Job
import pl.intertell.technik.data.TeamMember
import pl.intertell.technik.data.TechnicianRepository
import pl.intertell.technik.data.api.ApiTechnicianRepository

class TechnicianViewModel(application: Application) : AndroidViewModel(application) {

    private val apiRepository = ApiTechnicianRepository(application)
    private val repository: TechnicianRepository = apiRepository

    private val _uiState = MutableStateFlow(TechnicianUiState())
    val uiState: StateFlow<TechnicianUiState> = _uiState.asStateFlow()

    private val _me = MutableStateFlow<TeamMember?>(null)
    val me: StateFlow<TeamMember?> = _me.asStateFlow()

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

    private var searchJob: CoroutineJob? = null

    init {
        viewModelScope.launch {
            _uiState.update { it.copy(serverUrl = apiRepository.serverConfig.getBaseUrl()) }
        }
    }

    fun setServerUrl(url: String) {
        viewModelScope.launch {
            apiRepository.serverConfig.setBaseUrl(url)
            _uiState.update { it.copy(serverUrl = apiRepository.serverConfig.getBaseUrl()) }
        }
    }

    fun login(email: String, password: String) {
        if (_uiState.value.loginLoading) return
        viewModelScope.launch {
            _uiState.update { it.copy(loginLoading = true, loginError = null) }
            try {
                val tech = repository.login(email, password)
                _me.value = tech
                _uiState.update { it.copy(loginLoading = false, screen = TechScreen.JOBS) }
                refreshTasks()
            } catch (e: ApiException) {
                _uiState.update { it.copy(loginLoading = false, loginError = e.message) }
            }
        }
    }

    fun logout() {
        viewModelScope.launch { repository.logout() }
        _me.value = null
        _jobs.value = emptyList()
        _team.value = emptyList()
        _selectedJob.value = null
        _selectedCustomer.value = null
        _uiState.update { TechnicianUiState(serverUrl = it.serverUrl) }
    }

    fun goJobs() = navigate(TechScreen.JOBS).also { refreshTasks() }
    fun goAdmin() = navigate(TechScreen.ADMIN).also { refreshTeam() }
    fun goSearch() = navigate(TechScreen.SEARCH).also {
        if (_searchResult.value.customers.isEmpty()) runSearch(_uiState.value.searchQuery)
    }
    fun goReport() = navigate(TechScreen.REPORT)
    fun goRouter() = navigate(TechScreen.ROUTER).also { loadFullCustomerDetail() }
    fun goCust() = navigate(TechScreen.CUST)
    fun goJob() = navigate(TechScreen.JOB)

    private fun navigate(screen: TechScreen) = _uiState.update { it.copy(screen = screen) }

    // --- Jobs (Zlecenia) ---

    fun refreshTasks() {
        viewModelScope.launch {
            _uiState.update { it.copy(jobsLoading = true, errorMessage = null) }
            try {
                _jobs.value = repository.getTasks()
            } catch (e: ApiException) {
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
            } catch (e: ApiException) {
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
                }
            } catch (e: ApiException) {
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
            } catch (e: ApiException) {
                _uiState.update { it.copy(errorMessage = e.message) }
            }
            _uiState.update { it.copy(searchLoading = false) }
        }
    }

    fun openCustomer(customer: Customer) {
        _selectedCustomer.value = customer
        navigate(TechScreen.CUST)
    }

    private fun loadFullCustomerDetail() {
        val current = _selectedCustomer.value ?: return
        if (current.router != null) return // already have it
        viewModelScope.launch {
            _uiState.update { it.copy(customerDetailLoading = true) }
            try {
                _selectedCustomer.value = repository.getCustomerDetail(current.id)
            } catch (e: ApiException) {
                _uiState.update { it.copy(errorMessage = e.message) }
            }
            _uiState.update { it.copy(customerDetailLoading = false) }
        }
    }

    // --- Team (Zespół) ---

    fun refreshTeam() {
        viewModelScope.launch {
            _uiState.update { it.copy(teamLoading = true) }
            try {
                _team.value = repository.getTechnicians()
            } catch (e: ApiException) {
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
            } catch (e: ApiException) {
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
            } catch (e: ApiException) {
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
            } catch (e: ApiException) {
                _uiState.update { it.copy(errorMessage = e.message) }
            }
            _uiState.update { it.copy(actionInFlight = false) }
        }
    }

    fun clearMessages() = _uiState.update { it.copy(errorMessage = null, infoMessage = null) }
}
