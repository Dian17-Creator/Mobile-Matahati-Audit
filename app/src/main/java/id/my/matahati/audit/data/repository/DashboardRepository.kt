package id.my.matahati.audit.data.repository

import android.content.Context
import id.my.matahati.audit.data.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.IOException

class DashboardRepository(context: Context) {

    private val api = RetrofitClientLaravel.instance
    private val cache = DataCacheManager(context)

    companion object {
        private const val CACHE_KEY_DASHBOARD = "dashboard_summary"
    }

    fun getDashboardSummary(): Flow<ApiResult<DashboardSummaryResponse>> = flow {
        // 1. Emit cache
        val cached = getCachedSummary()
        if (cached != null) {
            emit(ApiResult.Success(cached))
        }

        // 2. Fetch from API
        try {
            val response = api.getDashboardSummary()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    if (body != cached) {
                        cache.save(CACHE_KEY_DASHBOARD, body)
                        emit(ApiResult.Success(body))
                    }
                } else if (cached == null) {
                    emit(ApiResult.Error("Data tidak ditemukan"))
                }
            } else if (cached == null) {
                emit(ApiResult.Error(ApiErrorParser.parseError(response.errorBody())))
            }
        } catch (e: IOException) {
            if (cached == null) {
                emit(ApiResult.Error("Kesalahan Koneksi: ${e.message ?: "Tidak dapat terhubung ke server"}"))
            }
        } catch (e: Exception) {
            if (cached == null) {
                emit(ApiResult.Error("Terjadi kesalahan: ${e.localizedMessage ?: "Silakan coba lagi"}"))
            }
        }
    }
    fun invalidateCache() {
        cache.delete(CACHE_KEY_DASHBOARD)
    }

    fun getCachedSummary(): DashboardSummaryResponse? {
        return cache.get(CACHE_KEY_DASHBOARD, DashboardSummaryResponse::class.java)
    }
}
