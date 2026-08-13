package id.my.matahati.audit.data.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import id.my.matahati.audit.data.*
import id.my.matahati.audit.data.repository.AuditQuestionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Collections

data class AuditQuestionUiState(
    val isLoading: Boolean = false,
    val questions: List<QuestionData> = emptyList(),
    val selectedQuestion: QuestionData? = null,
    val isAddDialogOpen: Boolean = false,
    val isEditDialogOpen: Boolean = false,
    val isDeleteDialogOpen: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class AuditQuestionViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AuditQuestionRepository = AuditQuestionRepository(application)

    private val _uiState = MutableStateFlow(AuditQuestionUiState())
    val uiState: StateFlow<AuditQuestionUiState> = _uiState.asStateFlow()

    fun fetchQuestions(categoryId: Int) {
        viewModelScope.launch {
            // Instant load
            if (_uiState.value.questions.isEmpty()) {
                repository.getCachedQuestions(categoryId)?.data?.let { items ->
                    _uiState.update { it.copy(questions = items) }
                }
            }

            if (_uiState.value.questions.isEmpty()) {
                _uiState.update { it.copy(isLoading = true) }
            }
            
            repository.getQuestions(categoryId).collect { result ->
                when (result) {
                    is ApiResult.Success -> {
                        _uiState.update { it.copy(isLoading = false, questions = result.data.data ?: emptyList()) }
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

    fun openEditDialog(question: QuestionData) {
        _uiState.update { it.copy(isEditDialogOpen = true, selectedQuestion = question) }
    }

    fun closeEditDialog() {
        _uiState.update { it.copy(isEditDialogOpen = false, selectedQuestion = null) }
    }

    fun openDeleteDialog(question: QuestionData) {
        _uiState.update { it.copy(isDeleteDialogOpen = true, selectedQuestion = question) }
    }

    fun closeDeleteDialog() {
        _uiState.update { it.copy(isDeleteDialogOpen = false, selectedQuestion = null) }
    }

    fun addQuestion(categoryId: Int, questionText: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val request = QuestionRequest(categoryId = categoryId, question = questionText)
            when (val result = repository.createQuestion(request)) {
                is ApiResult.Success -> {
                    if (result.data.success) {
                        _uiState.update { it.copy(isLoading = false, isAddDialogOpen = false, successMessage = result.data.message) }
                        fetchQuestions(categoryId)
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

    fun updateQuestion(categoryId: Int, id: Int, questionText: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val request = QuestionRequest(question = questionText)
            when (val result = repository.updateQuestion(id, request)) {
                is ApiResult.Success -> {
                    if (result.data.success) {
                        _uiState.update { it.copy(isLoading = false, isEditDialogOpen = false, successMessage = result.data.message) }
                        fetchQuestions(categoryId)
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

    fun deleteQuestion(categoryId: Int, id: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.deleteQuestion(id, categoryId).let { result ->
                when (result) {
                    is ApiResult.Success -> {
                        if (result.data.success) {
                            _uiState.update { it.copy(isLoading = false, isDeleteDialogOpen = false, successMessage = result.data.message) }
                            fetchQuestions(categoryId)
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
    }

    fun moveUp(categoryId: Int, index: Int) {
        if (index > 0) {
            val newList = _uiState.value.questions.toMutableList()
            Collections.swap(newList, index, index - 1)
            reorder(categoryId, newList)
        }
    }

    fun moveDown(categoryId: Int, index: Int) {
        if (index < _uiState.value.questions.size - 1) {
            val newList = _uiState.value.questions.toMutableList()
            Collections.swap(newList, index, index + 1)
            reorder(categoryId, newList)
        }
    }

    private fun reorder(categoryId: Int, newList: List<QuestionData>) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, questions = newList) }
            val ids = newList.map { it.id }
            val request = ReorderRequest(categoryId = categoryId, questionIds = ids)
            when (val result = repository.reorderQuestions(request)) {
                is ApiResult.Success -> {
                    if (result.data.success) {
                        _uiState.update { it.copy(isLoading = false, successMessage = result.data.message) }
                        fetchQuestions(categoryId)
                    } else {
                        _uiState.update { it.copy(isLoading = false, errorMessage = result.data.message) }
                        fetchQuestions(categoryId) // Revert to server state
                    }
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                    fetchQuestions(categoryId) // Revert to server state on error
                }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
