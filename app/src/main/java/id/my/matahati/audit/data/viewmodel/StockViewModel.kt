package id.my.matahati.audit.data.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.my.matahati.audit.data.*
import id.my.matahati.audit.data.repository.StockRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StockUiState(
    val isLoading: Boolean = false,
    val categories: List<StockCategoryData> = emptyList(),
    val selectedCategory: StockCategoryData? = null,
    val selectedItem: StockItemData? = null,
    val isAddCategoryDialogOpen: Boolean = false,
    val isEditCategoryDialogOpen: Boolean = false,
    val isDeleteCategoryDialogOpen: Boolean = false,
    val isAddItemDialogOpen: Boolean = false,
    val isDeleteItemDialogOpen: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class StockViewModel(
    private val repository: StockRepository = StockRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(StockUiState())
    val uiState: StateFlow<StockUiState> = _uiState.asStateFlow()

    fun fetchCategories() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = repository.getCategories()) {
                is ApiResult.Success -> {
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            categories = result.data.data ?: emptyList() 
                        ) 
                    }
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
            }
        }
    }

    // Category Dialog Controls
    fun openAddCategoryDialog() = _uiState.update { it.copy(isAddCategoryDialogOpen = true) }
    fun closeAddCategoryDialog() = _uiState.update { it.copy(isAddCategoryDialogOpen = false) }
    
    fun openEditCategoryDialog(category: StockCategoryData) = 
        _uiState.update { it.copy(isEditCategoryDialogOpen = true, selectedCategory = category) }
    fun closeEditCategoryDialog() = 
        _uiState.update { it.copy(isEditCategoryDialogOpen = false, selectedCategory = null) }
    
    fun openDeleteCategoryDialog(category: StockCategoryData) = 
        _uiState.update { it.copy(isDeleteCategoryDialogOpen = true, selectedCategory = category) }
    fun closeDeleteCategoryDialog() = 
        _uiState.update { it.copy(isDeleteCategoryDialogOpen = false, selectedCategory = null) }

    // Item Dialog Controls
    fun openAddItemDialog(category: StockCategoryData) = 
        _uiState.update { it.copy(isAddItemDialogOpen = true, selectedCategory = category) }
    fun closeAddItemDialog() = 
        _uiState.update { it.copy(isAddItemDialogOpen = false, selectedCategory = null) }
    
    fun openDeleteItemDialog(item: StockItemData) = 
        _uiState.update { it.copy(isDeleteItemDialogOpen = true, selectedItem = item) }
    fun closeDeleteItemDialog() = 
        _uiState.update { it.copy(isDeleteItemDialogOpen = false, selectedItem = null) }

    // Category Operations
    fun addCategory(name: String, description: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val request = StockCategoryRequest(name, description)
            when (val result = repository.createCategory(request)) {
                is ApiResult.Success -> {
                    if (result.data.success) {
                        _uiState.update { it.copy(isLoading = false, isAddCategoryDialogOpen = false, successMessage = result.data.message) }
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
            val request = StockCategoryRequest(name, description)
            when (val result = repository.updateCategory(id, request)) {
                is ApiResult.Success -> {
                    if (result.data.success) {
                        _uiState.update { it.copy(isLoading = false, isEditCategoryDialogOpen = false, successMessage = result.data.message) }
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
                        _uiState.update { it.copy(isLoading = false, isDeleteCategoryDialogOpen = false, successMessage = result.data.message) }
                        fetchCategories()
                    } else {
                        _uiState.update { it.copy(isLoading = false, isDeleteCategoryDialogOpen = false, errorMessage = result.data.message) }
                    }
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, isDeleteCategoryDialogOpen = false, errorMessage = result.message) }
                }
            }
        }
    }

    // Item Operations
    fun addItem(categoryId: Int, name: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val request = StockItemRequest(categoryId, name)
            when (val result = repository.createItem(request)) {
                is ApiResult.Success -> {
                    if (result.data.success) {
                        _uiState.update { it.copy(isLoading = false, isAddItemDialogOpen = false, successMessage = result.data.message) }
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

    fun deleteItem(id: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = repository.deleteItem(id)) {
                is ApiResult.Success -> {
                    if (result.data.success) {
                        _uiState.update { it.copy(isLoading = false, isDeleteItemDialogOpen = false, successMessage = result.data.message) }
                        fetchCategories()
                    } else {
                        _uiState.update { it.copy(isLoading = false, isDeleteItemDialogOpen = false, errorMessage = result.data.message) }
                    }
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, isDeleteItemDialogOpen = false, errorMessage = result.message) }
                }
            }
        }
    }

    fun reorderItems(categoryId: Int, itemIds: List<Int>) {
        viewModelScope.launch {
            // Local update for immediate feedback
            _uiState.update { state ->
                val updatedCategories = state.categories.map { cat ->
                    if (cat.id == categoryId) {
                        val newItemList = itemIds.mapNotNull { id -> cat.items?.find { it.id == id } }
                        cat.copy(items = newItemList)
                    } else cat
                }
                state.copy(categories = updatedCategories)
            }
            
            val request = StockReorderRequest(categoryId, itemIds)
            when (val result = repository.reorderItems(request)) {
                is ApiResult.Success -> {
                    if (!result.data.success) {
                        _uiState.update { it.copy(errorMessage = result.data.message) }
                        fetchCategories() // Revert local change
                    }
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(errorMessage = result.message) }
                    fetchCategories() // Revert local change
                }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
