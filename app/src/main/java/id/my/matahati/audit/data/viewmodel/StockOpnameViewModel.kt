package id.my.matahati.audit.data.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.my.matahati.audit.data.*
import id.my.matahati.audit.data.repository.AuditDepartmentRepository
import id.my.matahati.audit.data.repository.StockOpnameRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

data class StockOpnameUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isUploading: Boolean = false,
    val isSubmitting: Boolean = false,
    val departments: List<DepartmentData> = emptyList(),
    val selectedDepartment: DepartmentData? = null,
    val existingDraftId: Int? = null,
    val opnameDetail: StockOpnameDetailData? = null,
    val expandedCategoryIds: Set<Int> = emptySet(),
    val highlightedItemId: Int? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class StockOpnameViewModel(
    private val opnameRepository: StockOpnameRepository = StockOpnameRepository(),
    private val departmentRepository: AuditDepartmentRepository = AuditDepartmentRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(StockOpnameUiState())
    val uiState: StateFlow<StockOpnameUiState> = _uiState.asStateFlow()

    private val autosaveJobs = mutableMapOf<Int, Job>()

    fun initialize(auditId: Int, auditorId: Int) {
        if (auditId != -1) {
            fetchDetail(auditId, auditorId)
        }
        fetchInitialData()
    }

    private fun fetchInitialData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = departmentRepository.getDepartments()) {
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
        _uiState.update { it.copy(selectedDepartment = department, existingDraftId = null) }
        // In Stock Opname, the create endpoint handles resume logic automatically.
        // But for UI feedback, we could try to detect existing drafts if there was a list API.
        // Since we don't have getStockOpnames, we rely on the create endpoint.
    }

    fun startOpname(auditorId: Int) {
        val deptId = _uiState.value.selectedDepartment?.id ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = opnameRepository.createStockOpname(deptId, auditorId)) {
                is ApiResult.Success -> {
                    val id = result.data.data?.id
                    if (id != null) {
                        fetchDetail(id, auditorId)
                    } else {
                        _uiState.update { it.copy(isLoading = false, errorMessage = "ID tidak ditemukan") }
                    }
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
            }
        }
    }

    fun fetchDetail(id: Int, auditorId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = opnameRepository.getStockOpnameDetail(id, auditorId)) {
                is ApiResult.Success -> {
                    val detail = result.data.data
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            opnameDetail = detail,
                            expandedCategoryIds = detail?.categories?.map { it.id }?.toSet() ?: emptySet()
                        ) 
                    }
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
            }
        }
    }

    fun toggleCategory(categoryId: Int) {
        _uiState.update { state ->
            val newIds = if (state.expandedCategoryIds.contains(categoryId)) {
                state.expandedCategoryIds - categoryId
            } else {
                state.expandedCategoryIds + categoryId
            }
            state.copy(expandedCategoryIds = newIds)
        }
    }

    fun onItemChanged(itemId: Int, qtyStock: String?, qtyReal: String?, notes: String?) {
        val currentDetail = _uiState.value.opnameDetail ?: return
        
        // Update local state immediately
        val updatedCategories = currentDetail.categories.map { category ->
            category.copy(items = category.items.map { item ->
                if (item.id == itemId) {
                    item.copy(
                        response = item.response?.copy(
                            qtyStock = qtyStock,
                            qtyReal = qtyReal,
                            remark = notes
                        ) ?: StockOpnameItemResponse(
                            id = 0,
                            qtyStock = qtyStock,
                            qtyReal = qtyReal,
                            diff = null,
                            diffUnder = null,
                            diffOver = null,
                            remark = notes
                        )
                    )
                } else item
            })
        }
        _uiState.update { it.copy(opnameDetail = currentDetail.copy(categories = updatedCategories)) }

        // Debounced autosave
        autosaveJobs[itemId]?.cancel()
        autosaveJobs[itemId] = viewModelScope.launch {
            delay(600)
            performAutosave(itemId, qtyStock, qtyReal, notes)
        }
    }

    private suspend fun performAutosave(itemId: Int, qtyStock: String?, qtyReal: String?, notes: String?) {
        val auditId = _uiState.value.opnameDetail?.header?.id ?: return
        
        val request = StockOpnameUpdateRequest(
            auditId = auditId,
            itemId = itemId,
            qtyStock = qtyStock?.toDoubleOrNull(),
            qtyReal = qtyReal?.toDoubleOrNull(),
            remark = notes
        )

        _uiState.update { it.copy(isSaving = true) }
        when (val result = opnameRepository.updateStockOpname(request)) {
            is ApiResult.Success -> {
                _uiState.update { it.copy(isSaving = false) }
            }
            is ApiResult.Error -> {
                _uiState.update { it.copy(isSaving = false, errorMessage = result.message) }
            }
        }
    }

    fun uploadPhoto(responseId: Int, photoFile: File, remark: String?, auditorId: Int) {
        val auditId = _uiState.value.opnameDetail?.header?.id ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isUploading = true) }
            when (val result = opnameRepository.uploadPhoto(responseId, photoFile, remark)) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isUploading = false) }
                    fetchDetail(auditId, auditorId)
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isUploading = false, errorMessage = result.message) }
                }
            }
        }
    }

    fun updatePhotoRemark(photoId: Int, remark: String?, auditorId: Int) {
        val auditId = _uiState.value.opnameDetail?.header?.id ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            when (val result = opnameRepository.updatePhotoRemark(photoId, remark)) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isSaving = false) }
                    fetchDetail(auditId, auditorId)
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isSaving = false, errorMessage = result.message) }
                }
            }
        }
    }

    fun deletePhoto(photoId: Int, auditorId: Int) {
        val auditId = _uiState.value.opnameDetail?.header?.id ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            when (val result = opnameRepository.deletePhoto(photoId)) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isSaving = false) }
                    fetchDetail(auditId, auditorId)
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isSaving = false, errorMessage = result.message) }
                }
            }
        }
    }

    fun submitOpname(auditeeName: String, verificationPhoto: File, auditorId: Int) {
        val auditId = _uiState.value.opnameDetail?.header?.id ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, highlightedItemId = null) }
            when (val result = opnameRepository.submitStockOpname(auditId, auditeeName, verificationPhoto)) {
                is ApiResult.Success -> {
                    if (result.data.success) {
                        _uiState.update { it.copy(isSubmitting = false, successMessage = result.data.message) }
                        fetchDetail(auditId, auditorId)
                    } else {
                        val firstIncomplete = result.data.incompleteItems?.firstOrNull()
                        _uiState.update { 
                            it.copy(
                                isSubmitting = false, 
                                errorMessage = result.data.message,
                                highlightedItemId = firstIncomplete
                            ) 
                        }
                    }
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isSubmitting = false, errorMessage = result.message) }
                }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
