package id.my.matahati.audit.data.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import id.my.matahati.audit.data.*
import id.my.matahati.audit.data.repository.StockRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StockDepartmentUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val departments: List<DepartmentData> = emptyList(),
    val selectedDepartment: DepartmentData? = null,
    val categories: List<StockMappingCategory> = emptyList(),
    val selectedItemIds: Set<Int> = emptySet(),
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val conflictData: ConflictInfo? = null
)

data class ConflictInfo(
    val message: String,
    val documentId: String?,
    val status: String?
)

class StockDepartmentViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: StockRepository = StockRepository(application)

    private val _uiState = MutableStateFlow(StockDepartmentUiState())
    val uiState: StateFlow<StockDepartmentUiState> = _uiState.asStateFlow()

    init {
        // Instant load from cache
        repository.getCachedDepartments()?.data?.let { data ->
            _uiState.update { it.copy(departments = data) }
        }
        fetchDepartments()
    }

    fun fetchDepartments() {
        viewModelScope.launch {
            if (_uiState.value.departments.isEmpty()) {
                _uiState.update { it.copy(isLoading = true) }
            }
            repository.getDepartments().collect { result ->
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

    fun selectDepartment(department: DepartmentData) {
        _uiState.update { it.copy(selectedDepartment = department, categories = emptyList(), selectedItemIds = emptySet()) }
        fetchMapping(department.id)
    }

    private fun fetchMapping(departmentId: Int) {
        viewModelScope.launch {
            // Instant load
            repository.getCachedMapping(departmentId)?.data?.let { mappingData ->
                val categories = mappingData.categories ?: emptyList()
                val initialSelectedIds = categories.flatMap { it.items }
                    .filter { it.linked }
                    .map { it.id }
                    .toSet()

                _uiState.update { it.copy(
                    categories = categories,
                    selectedItemIds = initialSelectedIds
                ) }
            }

            if (_uiState.value.categories.isEmpty()) {
                _uiState.update { it.copy(isLoading = true) }
            }
            
            repository.getDepartmentMapping(departmentId).collect { result ->
                when (result) {
                    is ApiResult.Success -> {
                        val mappingData = result.data.data
                        val categories = mappingData?.categories ?: emptyList()
                        val initialSelectedIds = categories.flatMap { it.items }
                            .filter { it.linked }
                            .map { it.id }
                            .toSet()

                        _uiState.update { it.copy(
                            isLoading = false,
                            categories = categories,
                            selectedItemIds = initialSelectedIds
                        ) }
                    }
                    is ApiResult.Error -> {
                        _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                    }
                }
            }
        }
    }

    fun toggleItem(itemId: Int) {
        _uiState.update { state ->
            val newIds = if (state.selectedItemIds.contains(itemId)) {
                state.selectedItemIds - itemId
            } else {
                state.selectedItemIds + itemId
            }
            state.copy(selectedItemIds = newIds)
        }
    }

    fun toggleCategory(categoryId: Int, select: Boolean) {
        val category = _uiState.value.categories.find { it.id == categoryId } ?: return
        val itemIds = category.items.map { it.id }
        
        _uiState.update { state ->
            val newIds = if (select) {
                state.selectedItemIds + itemIds
            } else {
                state.selectedItemIds - itemIds.toSet()
            }
            state.copy(selectedItemIds = newIds)
        }
    }

    fun toggleAll(select: Boolean) {
        _uiState.update { state ->
            val newIds = if (select) {
                state.categories.flatMap { it.items }.map { it.id }.toSet()
            } else {
                emptySet()
            }
            state.copy(selectedItemIds = newIds)
        }
    }

    fun saveMapping() {
        val departmentId = _uiState.value.selectedDepartment?.id ?: return
        val itemIds = _uiState.value.selectedItemIds.toList()

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val request = SaveStockMappingRequest(departmentId, itemIds)
            when (val result = repository.saveMapping(request)) {
                is ApiResult.Success -> {
                    if (result.data.success) {
                        _uiState.update { it.copy(isSaving = false, successMessage = result.data.message) }
                        // Refresh mapping from API after save
                        fetchMapping(departmentId)
                    } else {
                        // Handle 409 or other business failures from JSON
                        _uiState.update { it.copy(isSaving = false, errorMessage = result.data.message) }
                    }
                }
                is ApiResult.Error -> {
                    // Check if it's 409 conflict
                    if (result.message.contains("409")) {
                        _uiState.update { it.copy(
                            isSaving = false, 
                            conflictData = ConflictInfo(
                                message = "Department sedang digunakan dalam audit inventory.",
                                documentId = null, // Backend should ideally provide this
                                status = "In Progress"
                            )
                        ) }
                    } else {
                        _uiState.update { it.copy(isSaving = false, errorMessage = result.message) }
                    }
                }
            }
        }
    }

    fun closeConflictDialog() {
        _uiState.update { it.copy(conflictData = null) }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
