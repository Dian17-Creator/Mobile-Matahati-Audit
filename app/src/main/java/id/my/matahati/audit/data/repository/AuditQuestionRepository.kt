package id.my.matahati.audit.data.repository

import android.content.Context
import id.my.matahati.audit.data.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.IOException

class AuditQuestionRepository(context: Context) {

    private val api = RetrofitClientLaravel.instance
    private val cache = DataCacheManager(context)

    companion object {
        private const val CACHE_KEY_QUESTIONS_PREFIX = "audit_questions_"
    }

    fun getQuestions(categoryId: Int): Flow<ApiResult<QuestionListResponse>> = flow {
        val cacheKey = CACHE_KEY_QUESTIONS_PREFIX + categoryId
        val cached = getCachedQuestions(categoryId)
        if (cached != null) {
            emit(ApiResult.Success(cached))
        }

        try {
            val response = api.getQuestions(categoryId)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    if (body != cached) {
                        cache.save(cacheKey, body)
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
                emit(ApiResult.Error("Kesalahan Koneksi: ${e.message}"))
            }
        } catch (e: Exception) {
            if (cached == null) {
                emit(ApiResult.Error("Terjadi kesalahan: ${e.localizedMessage}"))
            }
        }
    }

    suspend fun createQuestion(request: QuestionRequest): ApiResult<QuestionResponse> {
        return try {
            val response = api.createQuestion(request)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    request.categoryId?.let { invalidateCache(it) }
                    ApiResult.Success(body)
                }
                else ApiResult.Error("Gagal menyimpan data")
            } else {
                ApiResult.Error(ApiErrorParser.parseError(response.errorBody()))
            }
        } catch (e: IOException) {
            ApiResult.Error("Kesalahan Koneksi: ${e.message}")
        } catch (e: Exception) {
            ApiResult.Error("Terjadi kesalahan: ${e.localizedMessage}")
        }
    }

    suspend fun updateQuestion(id: Int, request: QuestionRequest): ApiResult<QuestionResponse> {
        return try {
            val response = api.updateQuestion(id, request)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    request.categoryId?.let { invalidateCache(it) }
                    ApiResult.Success(body)
                }
                else ApiResult.Error("Gagal memperbarui data")
            } else {
                ApiResult.Error(ApiErrorParser.parseError(response.errorBody()))
            }
        } catch (e: IOException) {
            ApiResult.Error("Kesalahan Koneksi: ${e.message}")
        } catch (e: Exception) {
            ApiResult.Error("Terjadi kesalahan: ${e.localizedMessage}")
        }
    }

    suspend fun deleteQuestion(id: Int, categoryId: Int): ApiResult<QuestionResponse> {
        return try {
            val response = api.deleteQuestion(id)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    invalidateCache(categoryId)
                    ApiResult.Success(body)
                }
                else ApiResult.Error("Gagal menghapus data")
            } else {
                ApiResult.Error(ApiErrorParser.parseError(response.errorBody()))
            }
        } catch (e: IOException) {
            ApiResult.Error("Kesalahan Koneksi: ${e.message}")
        } catch (e: Exception) {
            ApiResult.Error("Terjadi kesalahan: ${e.localizedMessage}")
        }
    }

    suspend fun reorderQuestions(request: ReorderRequest): ApiResult<QuestionResponse> {
        return try {
            val response = api.reorderQuestions(request)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    invalidateCache(request.categoryId)
                    ApiResult.Success(body)
                }
                else ApiResult.Error("Gagal mengatur ulang data")
            } else {
                ApiResult.Error(ApiErrorParser.parseError(response.errorBody()))
            }
        } catch (e: IOException) {
            ApiResult.Error("Kesalahan Koneksi: ${e.message}")
        } catch (e: Exception) {
            ApiResult.Error("Terjadi kesalahan: ${e.localizedMessage}")
        }
    }

    fun invalidateCache(categoryId: Int) {
        cache.delete(CACHE_KEY_QUESTIONS_PREFIX + categoryId)
    }

    fun getCachedQuestions(categoryId: Int): QuestionListResponse? {
        return cache.get(CACHE_KEY_QUESTIONS_PREFIX + categoryId, QuestionListResponse::class.java)
    }
}
