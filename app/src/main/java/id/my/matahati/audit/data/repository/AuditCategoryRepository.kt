package id.my.matahati.audit.data.repository

import android.content.Context
import id.my.matahati.audit.data.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.IOException

class AuditCategoryRepository(context: Context) {

    private val api = RetrofitClientLaravel.instance
    private val cache = DataCacheManager(context)

    companion object {
        private const val CACHE_KEY_CATEGORIES = "audit_categories"
    }

    fun getCategories(): Flow<ApiResult<CategoryListResponse>> = flow {
        val cached = getCachedCategories()
        if (cached != null) {
            emit(ApiResult.Success(cached))
        }

        try {
            val response = api.getCategories()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    if (body != cached) {
                        cache.save(CACHE_KEY_CATEGORIES, body)
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

    suspend fun createCategory(request: CategoryRequest): ApiResult<CategoryResponse> {
        return try {
            val response = api.createCategory(request)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    invalidateCache()
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

    suspend fun updateCategory(id: Int, request: CategoryRequest): ApiResult<CategoryResponse> {
        return try {
            val response = api.updateCategory(id, request)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    invalidateCache()
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

    suspend fun deleteCategory(id: Int): ApiResult<CategoryResponse> {
        return try {
            val response = api.deleteCategory(id)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    invalidateCache()
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

    fun invalidateCache() {
        cache.delete(CACHE_KEY_CATEGORIES)
    }

    fun getCachedCategories(): CategoryListResponse? {
        return cache.get(CACHE_KEY_CATEGORIES, CategoryListResponse::class.java)
    }
}
