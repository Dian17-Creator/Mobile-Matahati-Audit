package id.my.matahati.audit.data.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.my.matahati.audit.data.*
import id.my.matahati.audit.data.repository.AuditDepartmentRepository
import id.my.matahati.audit.data.repository.AuditExecutionRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

data class AuditExecutionUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isUploading: Boolean = false,
    val isSubmitting: Boolean = false,
    val departments: List<DepartmentData> = emptyList(),
    val selectedDepartment: DepartmentData? = null,
    val activeAudits: List<AuditHistoryItem> = emptyList(),
    val existingDraftId: Int? = null,
    val auditDetail: AuditDetailContainer? = null,
    val expandedCategoryIds: Set<Int> = emptySet(),
    val highlightedQuestionId: Int? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class AuditExecutionViewModel(
    private val executionRepository: AuditExecutionRepository = AuditExecutionRepository(),
    private val departmentRepository: AuditDepartmentRepository = AuditDepartmentRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuditExecutionUiState())
    val uiState: StateFlow<AuditExecutionUiState> = _uiState.asStateFlow()

    private var autosaveJob: Job? = null

    fun initialize(auditId: Int) {
        if (auditId != -1) {
            fetchAuditDetail(auditId)
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
        _uiState.update { it.copy(selectedDepartment = department, isLoading = true, existingDraftId = null) }
        
        viewModelScope.launch {
            val dateFrom = "2024-01-01"
            val dateTo = "2030-12-31"
            
            when (val result = executionRepository.getAudits(department.id, dateFrom, dateTo)) {
                is ApiResult.Success -> {
                    val audits = result.data.data ?: emptyList()
                    // Detect both "Draft" and "In Progress" as resumable
                    val draft = audits.find { 
                        it.status?.equals("Draft", ignoreCase = true) == true || 
                        it.status?.equals("In Progress", ignoreCase = true) == true
                    }
                    _uiState.update { it.copy(isLoading = false, existingDraftId = draft?.id) }
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
            }
        }
    }

    fun startAudit(auditorId: Int) {
        val state = _uiState.value
        val deptId = state.selectedDepartment?.id ?: return
        
        if (state.existingDraftId != null) {
            fetchAuditDetail(state.existingDraftId)
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = executionRepository.createAudit(deptId, auditorId)) {
                is ApiResult.Success -> {
                    val auditId = result.data.data?.id
                    if (auditId != null) {
                        fetchAuditDetail(auditId)
                    } else {
                        _uiState.update { it.copy(isLoading = false, errorMessage = "Audit ID tidak ditemukan") }
                    }
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
                    val detail = result.data.data
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            auditDetail = detail,
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

    fun onAnswerChanged(questionId: Int, score: String?, notes: String?) {
        val currentDetail = _uiState.value.auditDetail ?: return
        val updatedCategories = currentDetail.categories.map { category ->
            category.copy(questions = category.questions.map { question ->
                if (question.id == questionId) {
                    val currentResp = question.response
                    question.copy(
                        response = currentResp?.copy(score = score, remark = notes) 
                            ?: AuditResponseDetail(id = 0, score = score, isNa = score == "N/A", remark = notes)
                    )
                } else {
                    question
                }
            })
        }
        _uiState.update { it.copy(auditDetail = currentDetail.copy(categories = updatedCategories)) }

        autosaveJob?.cancel()
        autosaveJob = viewModelScope.launch {
            delay(600)
            performUpdate()
        }
    }

    private suspend fun performUpdate() {
        val container = _uiState.value.auditDetail ?: return
        val auditId = container.audit.id
        val answers = container.categories.flatMap { it.questions }.map {
            AuditAnswer(it.id, it.response?.score, it.response?.remark)
        }

        _uiState.update { it.copy(isSaving = true) }
        when (val result = executionRepository.updateAudit(auditId, answers)) {
            is ApiResult.Success -> {
                _uiState.update { it.copy(isSaving = false) }
            }
            is ApiResult.Error -> {
                _uiState.update { it.copy(isSaving = false, errorMessage = result.message) }
            }
        }
    }

    fun uploadPhoto(responseId: Int, photoFile: File) {
        val auditId = _uiState.value.auditDetail?.audit?.id ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isUploading = true) }
            when (val result = executionRepository.uploadPhoto(auditId, responseId, photoFile)) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isUploading = false) }
                    fetchAuditDetail(auditId)
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isUploading = false, errorMessage = result.message) }
                }
            }
        }
    }

    fun updatePhotoDetail(photoId: Int, observation: String?, recommendation: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            when (val result = executionRepository.updatePhotoDetail(photoId, observation, recommendation)) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isSaving = false) }
                    _uiState.value.auditDetail?.audit?.id?.let { fetchAuditDetail(it) }
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isSaving = false, errorMessage = result.message) }
                }
            }
        }
    }

    fun deletePhoto(photoId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            when (val result = executionRepository.deletePhoto(photoId)) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isSaving = false) }
                    _uiState.value.auditDetail?.audit?.id?.let { fetchAuditDetail(it) }
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isSaving = false, errorMessage = result.message) }
                }
            }
        }
    }

    fun submitAudit(auditeeName: String, verificationPhoto: File) {
        val auditId = _uiState.value.auditDetail?.audit?.id ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, highlightedQuestionId = null) }
            when (val result = executionRepository.submitAudit(auditId, auditeeName, verificationPhoto)) {
                is ApiResult.Success -> {
                    if (result.data.success) {
                        _uiState.update { it.copy(isSubmitting = false, successMessage = result.data.message) }
                        fetchAuditDetail(auditId)
                    } else {
                        val firstIncomplete = result.data.incompleteQuestions?.firstOrNull()
                        _uiState.update { 
                            it.copy(
                                isSubmitting = false, 
                                errorMessage = result.data.message,
                                highlightedQuestionId = firstIncomplete?.id
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
