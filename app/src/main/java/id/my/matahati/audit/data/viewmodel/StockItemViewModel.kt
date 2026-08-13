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
import java.util.Collections

data class StockItemUiState(
    val isLoading: Boolean = false,
    val items: List<StockItemData> = emptyList(),
    val selectedItem: StockItemData? = null,
    val isAddDialogOpen: Boolean = false,
    val isEditDialogOpen: Boolean = false,
    val isDeleteDialogOpen: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class StockItemViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: StockRepository = StockRepository(application)

    private val _uiState = MutableStateFlow(StockItemUiState())
    val uiState: StateFlow<StockItemUiState> = _uiState.asStateFlow()

    fun fetchItems(categoryId: Int) {
        viewModelScope.launch {
            // Instant load
            if (_uiState.value.items.isEmpty()) {
                repository.getCachedItems(categoryId)?.data?.items?.let { items ->
                    _uiState.update { it.copy(items = items) }
                }
            }
            
            if (_uiState.value.items.isEmpty()) {
                _uiState.update { it.copy(isLoading = true) }
            }
            
            repository.getCategory(categoryId).collect { result ->
                when (result) {
                    is ApiResult.Success -> {
                        _uiState.update { it.copy(isLoading = false, items = result.data.data?.items ?: emptyList()) }
                    }
                    is ApiResult.Error -> {
                        _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                    }
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

    fun openEditDialog(item: StockItemData) {
        _uiState.update { it.copy(isEditDialogOpen = true, selectedItem = item) }
    }

    fun closeEditDialog() {
        _uiState.update { it.copy(isEditDialogOpen = false, selectedItem = null) }
    }

    fun openDeleteDialog(item: StockItemData) {
        _uiState.update { it.copy(isDeleteDialogOpen = true, selectedItem = item) }
    }

    fun closeDeleteDialog() {
        _uiState.update { it.copy(isDeleteDialogOpen = false, selectedItem = null) }
    }

    fun addItem(categoryId: Int, itemName: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val request = StockItemRequest(categoryId = categoryId, name = itemName)
            when (val result = repository.createItem(request)) {
                is ApiResult.Success -> {
                    if (result.data.success) {
                        _uiState.update { it.copy(isLoading = false, isAddDialogOpen = false, successMessage = result.data.message) }
                        fetchItems(categoryId)
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

    fun deleteItem(categoryId: Int, id: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = repository.deleteItem(categoryId, id)) {
                is ApiResult.Success -> {
                    if (result.data.success) {
                        _uiState.update { it.copy(isLoading = false, isDeleteDialogOpen = false, successMessage = result.data.message) }
                        fetchItems(categoryId)
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

    fun moveUp(categoryId: Int, index: Int) {
        if (index > 0) {
            val newList = _uiState.value.items.toMutableList()
            Collections.swap(newList, index, index - 1)
            reorder(categoryId, newList)
        }
    }

    fun moveDown(categoryId: Int, index: Int) {
        if (index < _uiState.value.items.size - 1) {
            val newList = _uiState.value.items.toMutableList()
            Collections.swap(newList, index, index + 1)
            reorder(categoryId, newList)
        }
    }

    private fun reorder(categoryId: Int, newList: List<StockItemData>) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, items = newList) }
            val ids = newList.map { it.id }
            val request = StockReorderRequest(categoryId = categoryId, itemIds = ids)
            when (val result = repository.reorderItems(request)) {
                is ApiResult.Success -> {
                    if (result.data.success) {
                        _uiState.update { it.copy(isLoading = false, successMessage = result.data.message) }
                        fetchItems(categoryId)
                    } else {
                        _uiState.update { it.copy(isLoading = false, errorMessage = result.data.message) }
                        fetchItems(categoryId) // Revert
                    }
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                    fetchItems(categoryId) // Revert
                }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
