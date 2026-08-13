package id.my.matahati.audit.data.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import id.my.matahati.audit.data.*
import id.my.matahati.audit.data.repository.AuditDepartmentRepository
import id.my.matahati.audit.data.repository.StockOpnameRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class StockOpnameHasilUiState(
    val isLoading: Boolean = false,
    val departments: List<DepartmentData> = emptyList(),
    val selectedDepartment: DepartmentData? = null,
    val dateFrom: String = "",
    val dateTo: String = "",
    val opnames: List<StockOpnameHistoryItem> = emptyList(),
    val selectedOpnameDetail: StockOpnameDetailData? = null,
    val errorMessage: String? = null
)

class StockOpnameHasilViewModel(application: Application) : AndroidViewModel(application) {

    private val opnameRepository: StockOpnameRepository = StockOpnameRepository()
    private val departmentRepository: AuditDepartmentRepository = AuditDepartmentRepository(application)

    private val _uiState = MutableStateFlow(StockOpnameHasilUiState())
    val uiState: StateFlow<StockOpnameHasilUiState> = _uiState.asStateFlow()

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
            departmentRepository.getDepartments().collect { result ->
                when (result) {
                    is ApiResult.Success -> {
                        _uiState.update { it.copy(isLoading = false, departments = result.data.data ?: emptyList()) }
                    }
                    is ApiResult.Error -> {
                        _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                    }
                }
            }
        }
    }

    fun selectDepartment(department: DepartmentData, auditorId: Int) {
        _uiState.update { it.copy(selectedDepartment = department) }
        fetchOpnames(auditorId)
    }

    fun updateDates(from: String, to: String, auditorId: Int) {
        _uiState.update { it.copy(dateFrom = from, dateTo = to) }
        fetchOpnames(auditorId)
    }

    fun fetchOpnames(auditorId: Int) {
        val state = _uiState.value
        val deptId = state.selectedDepartment?.id ?: return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, opnames = emptyList()) }
            when (val result = opnameRepository.getStockOpnameHistories(auditorId, deptId, state.dateFrom, state.dateTo)) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isLoading = false, opnames = result.data.data?.items ?: emptyList()) }
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
            }
        }
    }

    fun fetchOpnameDetail(id: Int, auditorId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = opnameRepository.getStockOpnameDetail(id, auditorId)) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isLoading = false, selectedOpnameDetail = result.data.data) }
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
            }
        }
    }

    fun clearDetail() {
        _uiState.update { it.copy(selectedOpnameDetail = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
