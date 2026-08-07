package id.my.matahati.audit.data.repository

import id.my.matahati.audit.data.*
import java.io.IOException

class AuditQuestionRepository {

    private val api = RetrofitClientLaravel.instance

    suspend fun getQuestions(categoryId: Int): ApiResult<QuestionListResponse> {
        return try {
            val response = api.getQuestions(categoryId)
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

    suspend fun createQuestion(request: QuestionRequest): ApiResult<QuestionResponse> {
        return try {
            val response = api.createQuestion(request)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) ApiResult.Success(body)
                else ApiResult.Error("Gagal menyimpan data")
            } else {
                ApiResult.Error(ApiErrorParser.parseError(response.errorBody()))
            }
        } catch (e: IOException) {
            ApiResult.Error("Kesalahan Koneksi: ${e.message ?: "Tidak dapat terhubung ke server"}. Periksa koneksi internet Anda.")
        } catch (e: Exception) {
            ApiResult.Error("Terjadi kesalahan: ${e.localizedMessage ?: "Silakan coba lagi"}")
        }
    }

    suspend fun updateQuestion(id: Int, request: QuestionRequest): ApiResult<QuestionResponse> {
        return try {
            val response = api.updateQuestion(id, request)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) ApiResult.Success(body)
                else ApiResult.Error("Gagal memperbarui data")
            } else {
                ApiResult.Error(ApiErrorParser.parseError(response.errorBody()))
            }
        } catch (e: IOException) {
            ApiResult.Error("Kesalahan Koneksi: ${e.message ?: "Tidak dapat terhubung ke server"}. Periksa koneksi internet Anda.")
        } catch (e: Exception) {
            ApiResult.Error("Terjadi kesalahan: ${e.localizedMessage ?: "Silakan coba lagi"}")
        }
    }

    suspend fun deleteQuestion(id: Int): ApiResult<QuestionResponse> {
        return try {
            val response = api.deleteQuestion(id)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) ApiResult.Success(body)
                else ApiResult.Error("Gagal menghapus data")
            } else {
                ApiResult.Error(ApiErrorParser.parseError(response.errorBody()))
            }
        } catch (e: IOException) {
            ApiResult.Error("Kesalahan Koneksi: ${e.message ?: "Tidak dapat terhubung ke server"}. Periksa koneksi internet Anda.")
        } catch (e: Exception) {
            ApiResult.Error("Terjadi kesalahan: ${e.localizedMessage ?: "Silakan coba lagi"}")
        }
    }

    suspend fun reorderQuestions(request: ReorderRequest): ApiResult<QuestionResponse> {
        return try {
            val response = api.reorderQuestions(request)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) ApiResult.Success(body)
                else ApiResult.Error("Gagal mengatur ulang data")
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
