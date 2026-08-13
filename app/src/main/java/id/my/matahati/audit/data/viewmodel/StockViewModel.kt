package id.my.matahati.audit.data.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
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
    val recentActivities: List<RecentActivityData> = emptyList(),
    val errorMessage: String? = null
)

class StockViewModel(application: Application) : AndroidViewModel(application) {

    private val dashboardRepository: DashboardRepository = DashboardRepository(application)

    private val _uiState = MutableStateFlow(StockUiState())
    val uiState: StateFlow<StockUiState> = _uiState.asStateFlow()

    init {
        // Instant load from cache
        dashboardRepository.getCachedSummary()?.data?.let { data ->
            _uiState.update {
                it.copy(
                    totalKategoriStok = data.totalKategoriStok.toString(),
                    totalBarang = data.totalBarang.toString(),
                    totalStokOpname = data.totalStokOpname.toString(),
                    recentActivities = data.recentStockOpname ?: emptyList()
                )
            }
        }
        fetchDashboardSummary()
    }

    fun fetchDashboardSummary() {
        viewModelScope.launch {
            // Only show loading if we don't have data yet
            if (_uiState.value.totalKategoriStok == "0") {
                _uiState.update { it.copy(isLoading = true) }
            }
            
            dashboardRepository.getDashboardSummary().collect { result ->
                when (result) {
                    is ApiResult.Success -> {
                        val data = result.data.data
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                totalKategoriStok = data?.totalKategoriStok?.toString() ?: "0",
                                totalBarang = data?.totalBarang?.toString() ?: "0",
                                totalStokOpname = data?.totalStokOpname?.toString() ?: "0",
                                recentActivities = data?.recentStockOpname ?: emptyList()
                            )
                        }
                    }
                    is ApiResult.Error -> {
                        _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                    }
                }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
