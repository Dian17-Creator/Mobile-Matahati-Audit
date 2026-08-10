package id.my.matahati.audit.data.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.my.matahati.audit.data.*
import id.my.matahati.audit.data.repository.DashboardRepository
import id.my.matahati.audit.data.repository.StockRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StockUiState(
    val isLoading: Boolean = false,
    val totalKategoriStok: String = "0",
    val totalBarang: String = "0",
    val totalStokOpname: String = "0",
    val errorMessage: String? = null
)

class StockViewModel(
    private val dashboardRepository: DashboardRepository = DashboardRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(StockUiState())
    val uiState: StateFlow<StockUiState> = _uiState.asStateFlow()

    fun fetchDashboardSummary() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = dashboardRepository.getDashboardSummary()) {
                is ApiResult.Success -> {
                    val data = result.data.data
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            totalKategoriStok = data?.totalKategoriStok?.toString() ?: "0",
                            totalBarang = data?.totalBarang?.toString() ?: "0",
                            totalStokOpname = data?.totalStokOpname?.toString() ?: "0"
                        ) 
                    }
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
