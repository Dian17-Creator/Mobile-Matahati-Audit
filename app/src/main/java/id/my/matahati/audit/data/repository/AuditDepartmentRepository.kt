package id.my.matahati.audit.data.repository

import android.content.Context
import id.my.matahati.audit.data.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.IOException

class AuditDepartmentRepository(context: Context) {

    private val api = RetrofitClientLaravel.instance
    private val cache = DataCacheManager(context)

    companion object {
        private const val CACHE_KEY_DEPARTMENTS = "audit_departments_list"
        private const val CACHE_KEY_MAPPING_PREFIX = "audit_department_mapping_"
    }

    fun getDepartments(): Flow<ApiResult<DepartmentListResponse>> = flow {
        // 1. Emit cache immediately if available
        val cached = cache.get(CACHE_KEY_DEPARTMENTS, DepartmentListResponse::class.java)
        if (cached != null) {
            emit(ApiResult.Success(cached))
        }

        // 2. Fetch from API
        try {
            val response = api.getDepartments()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    // Update cache if data changed or cache was empty
                    if (body != cached) {
                        cache.save(CACHE_KEY_DEPARTMENTS, body)
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

    fun getDepartmentMapping(id: Int): Flow<ApiResult<DepartmentMappingResponse>> = flow {
        val cacheKey = CACHE_KEY_MAPPING_PREFIX + id
        
        // 1. Emit cache immediately if available
        val cached = cache.get(cacheKey, DepartmentMappingResponse::class.java)
        if (cached != null) {
            emit(ApiResult.Success(cached))
        }

        // 2. Fetch from API
        try {
            val response = api.getDepartmentMapping(id)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    // Update cache if data changed or cache was empty
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
                emit(ApiResult.Error("Kesalahan Koneksi: ${e.message ?: "Tidak dapat terhubung ke server"}"))
            }
        } catch (e: Exception) {
            if (cached == null) {
                emit(ApiResult.Error("Terjadi kesalahan: ${e.localizedMessage ?: "Silakan coba lagi"}"))
            }
        }
    }

    suspend fun saveDepartmentMapping(request: SaveMappingRequest): ApiResult<GenericResponse> {
        return try {
            val response = api.saveDepartmentMapping(request)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    // Invalidate cache on success
                    invalidateMappingCache(request.departmentId)
                    ApiResult.Success(body)
                }
                else ApiResult.Error("Gagal menyimpan data")
            } else {
                ApiResult.Error(ApiErrorParser.parseError(response.errorBody()))
            }
        } catch (e: IOException) {
            ApiResult.Error("Kesalahan Koneksi: ${e.message ?: "Tidak dapat terhubung ke server"}")
        } catch (e: Exception) {
            ApiResult.Error("Terjadi kesalahan: ${e.localizedMessage ?: "Silakan coba lagi"}")
        }
    }

    fun invalidateMappingCache(departmentId: Int) {
        cache.delete(CACHE_KEY_MAPPING_PREFIX + departmentId)
    }
}
