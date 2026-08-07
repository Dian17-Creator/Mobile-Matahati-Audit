package id.my.matahati.audit.data.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.my.matahati.audit.data.*
import id.my.matahati.audit.data.repository.AuditDepartmentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuditDepartmentUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val departments: List<DepartmentData> = emptyList(),
    val selectedDepartment: DepartmentData? = null,
    val categories: List<MappingCategory> = emptyList(),
    val selectedQuestionIds: Set<Int> = emptySet(),
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class AuditDepartmentViewModel(
    private val repository: AuditDepartmentRepository = AuditDepartmentRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuditDepartmentUiState())
    val uiState: StateFlow<AuditDepartmentUiState> = _uiState.asStateFlow()

    init {
        fetchDepartments()
    }

    fun fetchDepartments() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = repository.getDepartments()) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isLoading = false, departments = result.data.data ?: emptyList()) }
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
            }
        }
    }

    fun selectDepartment(department: DepartmentData) {
        _uiState.update { it.copy(selectedDepartment = department, categories = emptyList(), selectedQuestionIds = emptySet()) }
        fetchMapping(department.id)
    }

    private fun fetchMapping(departmentId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = repository.getDepartmentMapping(departmentId)) {
                is ApiResult.Success -> {
                    val mappingData = result.data.data
                    val categories = mappingData?.categories ?: emptyList()
                    val initialSelectedIds = categories.flatMap { it.questions }
                        .filter { it.linked }
                        .map { it.id }
                        .toSet()
                    
                    _uiState.update { it.copy(
                        isLoading = false, 
                        categories = categories, 
                        selectedQuestionIds = initialSelectedIds 
                    ) }
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
            }
        }
    }

    fun toggleQuestion(questionId: Int) {
        _uiState.update { state ->
            val newIds = if (state.selectedQuestionIds.contains(questionId)) {
                state.selectedQuestionIds - questionId
            } else {
                state.selectedQuestionIds + questionId
            }
            state.copy(selectedQuestionIds = newIds)
        }
    }

    fun toggleCategory(categoryId: Int, select: Boolean) {
        val category = _uiState.value.categories.find { it.id == categoryId } ?: return
        val questionIds = category.questions.map { it.id }
        
        _uiState.update { state ->
            val newIds = if (select) {
                state.selectedQuestionIds + questionIds
            } else {
                state.selectedQuestionIds - questionIds.toSet()
            }
            state.copy(selectedQuestionIds = newIds)
        }
    }

    fun toggleAll(select: Boolean) {
        _uiState.update { state ->
            val newIds = if (select) {
                state.categories.flatMap { it.questions }.map { it.id }.toSet()
            } else {
                emptySet()
            }
            state.copy(selectedQuestionIds = newIds)
        }
    }

    fun saveMapping() {
        val departmentId = _uiState.value.selectedDepartment?.id ?: return
        val questionIds = _uiState.value.selectedQuestionIds.toList()

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val request = SaveMappingRequest(departmentId, questionIds)
            when (val result = repository.saveDepartmentMapping(request)) {
                is ApiResult.Success -> {
                    if (result.data.success) {
                        _uiState.update { it.copy(isSaving = false, successMessage = result.data.message) }
                    } else {
                        _uiState.update { it.copy(isSaving = false, errorMessage = result.data.message) }
                    }
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isSaving = false, errorMessage = result.message) }
                }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
