package id.my.matahati.audit.data.repository

import id.my.matahati.audit.data.ApiErrorParser
import id.my.matahati.audit.data.ApiResult
import id.my.matahati.audit.data.DashboardSummaryResponse
import id.my.matahati.audit.data.RetrofitClientLaravel
import java.io.IOException

class DashboardRepository {

    private val api = RetrofitClientLaravel.instance

    suspend fun getDashboardSummary(): ApiResult<DashboardSummaryResponse> {
        return try {
            val response = api.getDashboardSummary()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) ApiResult.Success(body)
                else ApiResult.Error("Data tidak ditemukan")
            } else {
                ApiResult.Error(ApiErrorParser.parseError(response.errorBody()))
            }
        } catch (e: IOException) {
            ApiResult.Error("Kesalahan Koneksi: ${e.message ?: "Tidak dapat terhubung ke server"}. Periksa koneksi internet Anda.")
        } catch (e: Exception) {
            ApiResult.Error("Terjadi kesalahan: ${e.localizedMessage ?: "Silakan coba lagi"}")
        }
    }
}
