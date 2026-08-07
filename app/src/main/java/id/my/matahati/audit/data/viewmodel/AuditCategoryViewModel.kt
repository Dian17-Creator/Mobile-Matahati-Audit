package id.my.matahati.audit.data.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.my.matahati.audit.data.ApiResult
import id.my.matahati.audit.data.CategoryData
import id.my.matahati.audit.data.CategoryRequest
import id.my.matahati.audit.data.repository.AuditCategoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuditCategoryUiState(
    val isLoading: Boolean = false,
    val categories: List<CategoryData> = emptyList(),
    val selectedCategory: CategoryData? = null,
    val isAddDialogOpen: Boolean = false,
    val isEditDialogOpen: Boolean = false,
    val isDeleteDialogOpen: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class AuditCategoryViewModel(
    private val repository: AuditCategoryRepository = AuditCategoryRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuditCategoryUiState())
    val uiState: StateFlow<AuditCategoryUiState> = _uiState.asStateFlow()

    fun fetchCategories() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = repository.getCategories()) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isLoading = false, categories = result.data.data ?: emptyList()) }
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
            }
        }
    }

    fun openAddDialog() {
        _uiState.update { it.copy(isAddDialogOpen = true) }
    }

    fun closeAddDialog() {
        _uiState.update { it.copy(isAddDialogOpen = false) }
    }

    fun openEditDialog(category: CategoryData) {
        _uiState.update { it.copy(isEditDialogOpen = true, selectedCategory = category) }
    }

    fun closeEditDialog() {
        _uiState.update { it.copy(isEditDialogOpen = false, selectedCategory = null) }
    }

    fun openDeleteDialog(category: CategoryData) {
        _uiState.update { it.copy(isDeleteDialogOpen = true, selectedCategory = category) }
    }

    fun closeDeleteDialog() {
        _uiState.update { it.copy(isDeleteDialogOpen = false, selectedCategory = null) }
    }

    fun addCategory(name: String, description: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val request = CategoryRequest(name, description)
            when (val result = repository.createCategory(request)) {
                is ApiResult.Success -> {
                    if (result.data.success) {
                        _uiState.update { it.copy(isLoading = false, isAddDialogOpen = false, successMessage = result.data.message) }
                        fetchCategories()
                    } else {
                        _uiState.update { it.copy(isLoading = false, errorMessage = result.data.message) }
                    }
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
            }
        }
    }

    fun updateCategory(id: Int, name: String, description: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val request = CategoryRequest(name, description)
            when (val result = repository.updateCategory(id, request)) {
                is ApiResult.Success -> {
                    if (result.data.success) {
                        _uiState.update { it.copy(isLoading = false, isEditDialogOpen = false, successMessage = result.data.message) }
                        fetchCategories()
                    } else {
                        _uiState.update { it.copy(isLoading = false, errorMessage = result.data.message) }
                    }
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
            }
        }
    }

    fun deleteCategory(id: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = repository.deleteCategory(id)) {
                is ApiResult.Success -> {
                    if (result.data.success) {
                        _uiState.update { it.copy(isLoading = false, isDeleteDialogOpen = false, successMessage = result.data.message) }
                        fetchCategories()
                    } else {
                        _uiState.update { it.copy(isLoading = false, isDeleteDialogOpen = false, errorMessage = result.data.message) }
                    }
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, isDeleteDialogOpen = false, errorMessage = result.message) }
                }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
