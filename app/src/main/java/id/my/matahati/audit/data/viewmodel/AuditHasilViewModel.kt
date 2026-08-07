package id.my.matahati.audit.data.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.my.matahati.audit.data.*
import id.my.matahati.audit.data.repository.AuditDepartmentRepository
import id.my.matahati.audit.data.repository.AuditExecutionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class AuditHasilUiState(
    val isLoading: Boolean = false,
    val departments: List<DepartmentData> = emptyList(),
    val selectedDepartment: DepartmentData? = null,
    val dateFrom: String = "",
    val dateTo: String = "",
    val audits: List<AuditHistoryItem> = emptyList(),
    val selectedAuditDetail: AuditDetailContainer? = null,
    val errorMessage: String? = null
)

class AuditHasilViewModel(
    private val executionRepository: AuditExecutionRepository = AuditExecutionRepository(),
    private val departmentRepository: AuditDepartmentRepository = AuditDepartmentRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuditHasilUiState())
    val uiState: StateFlow<AuditHasilUiState> = _uiState.asStateFlow()

    init {
        val calendar = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = sdf.format(calendar.time)
        calendar.set(Calendar.DAY_OF_YEAR, 1)
        val startOfYear = sdf.format(calendar.time)

        _uiState.update { it.copy(dateFrom = startOfYear, dateTo = today) }
        fetchDepartments()
    }

    private fun fetchDepartments() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = departmentRepository.getDepartments()) {
                is ApiResult.Success -> {
                    val depts = result.data.data ?: emptyList()
                    _uiState.update { it.copy(isLoading = false, departments = depts) }
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
            }
        }
    }

    fun selectDepartment(department: DepartmentData) {
        _uiState.update { it.copy(selectedDepartment = department) }
        fetchAudits()
    }

    fun updateDates(from: String, to: String) {
        _uiState.update { it.copy(dateFrom = from, dateTo = to) }
        fetchAudits()
    }

    fun fetchAudits() {
        val state = _uiState.value
        val deptId = state.selectedDepartment?.id ?: return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, audits = emptyList()) }
            when (val result = executionRepository.getAudits(deptId, state.dateFrom, state.dateTo)) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isLoading = false, audits = result.data.data ?: emptyList()) }
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
            }
        }
    }

    fun fetchAuditDetail(auditId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = executionRepository.getAuditDetail(auditId)) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isLoading = false, selectedAuditDetail = result.data.data) }
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
            }
        }
    }

    fun clearDetail() {
        _uiState.update { it.copy(selectedAuditDetail = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun deleteAudit(auditId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = executionRepository.deleteAudit(auditId)) {
                is ApiResult.Success -> {
                    fetchAudits()
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
            }
        }
    }
}
