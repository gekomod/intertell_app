package pl.intertell.technik

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.intertell.technik.data.CustomerState
import pl.intertell.technik.data.MockTechnicianRepository
import pl.intertell.technik.data.TeamMember
import pl.intertell.technik.data.TechnicianRepository

class TechnicianViewModel(
    private val repository: TechnicianRepository = MockTechnicianRepository(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(TechnicianUiState())
    val uiState: StateFlow<TechnicianUiState> = _uiState.asStateFlow()

    val technicianId = repository.getTechnicianId()
    val todayLabel = repository.getTodayLabel()
    val jobs = repository.getJobs()
    val customers = repository.getCustomers()
    val networkClients = repository.getNetworkClients()

    private val _team = MutableStateFlow(repository.getTeam())
    val team: StateFlow<List<TeamMember>> = _team.asStateFlow()

    fun login(technicianId: String, password: String) {
        if (_uiState.value.loginLoading) return
        viewModelScope.launch {
            _uiState.update { it.copy(loginLoading = true, loginError = null) }
            val ok = repository.login(technicianId, password)
            _uiState.update {
                if (ok) it.copy(loginLoading = false, screen = TechScreen.JOBS)
                else it.copy(loginLoading = false, loginError = "Podaj identyfikator technika i hasło")
            }
        }
    }

    fun logout() = _uiState.update { TechnicianUiState() }

    fun goJobs() = navigate(TechScreen.JOBS)
    fun goAdmin() = navigate(TechScreen.ADMIN)
    fun goSearch() = navigate(TechScreen.SEARCH)
    fun goReport() = navigate(TechScreen.REPORT)
    fun goRouter() = navigate(TechScreen.ROUTER)
    fun goCust() = navigate(TechScreen.CUST)
    fun goJob() = navigate(TechScreen.JOB)

    private fun navigate(screen: TechScreen) = _uiState.update { it.copy(screen = screen) }

    fun openJob(index: Int) = _uiState.update {
        it.copy(screen = TechScreen.JOB, jobIndex = index, customerIndex = jobs[index].customerIndex)
    }

    fun openCustomer(index: Int) = _uiState.update { it.copy(screen = TechScreen.CUST, customerIndex = index) }

    fun currentJob() = jobs[_uiState.value.jobIndex]
    fun currentCustomer() = customers[_uiState.value.customerIndex]

    fun filteredCustomers(): List<Pair<Int, pl.intertell.technik.data.Customer>> {
        val state = _uiState.value
        val query = state.searchQuery.trim().lowercase()
        return customers.withIndex()
            .filter { (_, c) -> state.searchFilter != CustomerFilter.AWARIA || c.state == CustomerState.AWARIA }
            .filter { (_, c) ->
                query.isBlank() ||
                    c.address.lowercase().contains(query) ||
                    c.name.lowercase().contains(query) ||
                    c.contract.lowercase().contains(query) ||
                    c.sn.lowercase().contains(query)
            }
            .map { it.index to it.value }
    }

    fun setSearchQuery(value: String) = _uiState.update { it.copy(searchQuery = value) }
    fun setSearchFilter(filter: CustomerFilter) = _uiState.update { it.copy(searchFilter = filter) }

    fun toggleDevice() = _uiState.update { it.copy(deviceAdded = !it.deviceAdded) }
    fun finishJob() = _uiState.update { it.copy(jobDone = true) }
    fun closeJobDone() = _uiState.update { it.copy(jobDone = false, screen = TechScreen.JOBS) }

    fun toggleQos() = _uiState.update { it.copy(qosEnabled = !it.qosEnabled) }
    fun toggleWifi24() = _uiState.update { it.copy(wifi24Enabled = !it.wifi24Enabled) }
    fun toggleWifi5() = _uiState.update { it.copy(wifi5Enabled = !it.wifi5Enabled) }
    fun toggleMesh() = _uiState.update { it.copy(meshEnabled = !it.meshEnabled) }
    fun runSpeedTest() = _uiState.update { it.copy(speedTestRun = !it.speedTestRun) }

    fun addTeamMember(name: String, email: String, role: String, area: String) {
        if (_uiState.value.teamMemberInvited) return
        repository.addTeamMember(name, email, role, area)
        _team.value = repository.getTeam()
        _uiState.update { it.copy(teamMemberInvited = true) }
    }
}
