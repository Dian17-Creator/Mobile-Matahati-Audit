package id.my.matahati.audit.data.repository

import android.content.Context
import id.my.matahati.audit.data.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.IOException

class StockOpnameRepository(context: Context) {

    private val api = RetrofitClientLaravel.instance
    private val cache = DataCacheManager(context)

    companion object {
        private const val CACHE_KEY_OPNAME_HISTORY_PREFIX = "stock_opname_history_"
        private const val CACHE_KEY_OPNAME_DETAIL_PREFIX = "stock_opname_detail_"
    }

    suspend fun createStockOpname(departmentId: Int, auditorId: Int): ApiResult<StockOpnameCreateResponse> {
        return try {
            val response = api.createStockOpname(StockOpnameCreateRequest(departmentId, auditorId))
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    invalidateHistoryCache()
                    ApiResult.Success(body)
                }
                else ApiResult.Error("Gagal membuat stok opname")
            } else {
                ApiResult.Error(ApiErrorParser.parseError(response.errorBody()))
            }
        } catch (e: IOException) {
            ApiResult.Error("Kesalahan Koneksi: ${e.message}")
        } catch (e: Exception) {
            ApiResult.Error("Terjadi kesalahan: ${e.localizedMessage}")
        }
    }

    fun getStockOpnameDetail(id: Int, auditorId: Int): Flow<ApiResult<StockOpnameDetailResponse>> = flow {
        val cacheKey = CACHE_KEY_OPNAME_DETAIL_PREFIX + id
        val cached = cache.get(cacheKey, StockOpnameDetailResponse::class.java)
        if (cached != null) {
            emit(ApiResult.Success(cached))
        }

        try {
            val response = api.getStockOpnameDetail(id, auditorId)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    if (body != cached) {
                        cache.save(cacheKey, body)
                        emit(ApiResult.Success(body))
                    }
                } else if (cached == null) {
                    emit(ApiResult.Error("Data stok opname tidak ditemukan"))
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

    suspend fun updateStockOpname(request: StockOpnameUpdateRequest): ApiResult<StockOpnameUpdateResponse> {
        return try {
            val response = api.updateStockOpname(request)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    invalidateDetailCache(request.auditId)
                    ApiResult.Success(body)
                }
                else ApiResult.Error("Gagal memperbarui stok opname")
            } else {
                ApiResult.Error(ApiErrorParser.parseError(response.errorBody()))
            }
        } catch (e: IOException) {
            ApiResult.Error("Kesalahan Koneksi: ${e.message}")
        } catch (e: Exception) {
            ApiResult.Error("Terjadi kesalahan: ${e.localizedMessage}")
        }
    }

    suspend fun uploadPhoto(auditId: Int, responseId: Int, photoFile: File, remark: String?): ApiResult<StockOpnameUpdateResponse> {
        return try {
            val responseIdBody = responseId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val remarkBody = remark?.toRequestBody("text/plain".toMediaTypeOrNull())
            val photoPart = MultipartBody.Part.createFormData(
                "photo",
                photoFile.name,
                photoFile.asRequestBody("image/*".toMediaTypeOrNull())
            )

            val response = api.uploadStockOpnamePhoto(responseIdBody, photoPart, remarkBody)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    invalidateDetailCache(auditId)
                    ApiResult.Success(body)
                }
                else ApiResult.Error("Gagal mengunggah foto")
            } else {
                ApiResult.Error(ApiErrorParser.parseError(response.errorBody()))
            }
        } catch (e: IOException) {
            ApiResult.Error("Kesalahan Koneksi: ${e.message}")
        } catch (e: Exception) {
            ApiResult.Error("Terjadi kesalahan: ${e.localizedMessage}")
        }
    }

    suspend fun updatePhotoRemark(auditId: Int, photoId: Int, remark: String?): ApiResult<StockOpnameUpdateResponse> {
        return try {
            val response = api.updateStockOpnamePhoto(StockOpnamePhotoUpdateRequest(photoId, remark))
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    invalidateDetailCache(auditId)
                    ApiResult.Success(body)
                }
                else ApiResult.Error("Gagal memperbarui keterangan foto")
            } else {
                ApiResult.Error(ApiErrorParser.parseError(response.errorBody()))
            }
        } catch (e: IOException) {
            ApiResult.Error("Kesalahan Koneksi: ${e.message}")
        } catch (e: Exception) {
            ApiResult.Error("Terjadi kesalahan: ${e.localizedMessage}")
        }
    }

    suspend fun deletePhoto(auditId: Int, photoId: Int): ApiResult<StockOpnameUpdateResponse> {
        return try {
            val response = api.deleteStockOpnamePhoto(StockOpnamePhotoDeleteRequest(photoId))
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    invalidateDetailCache(auditId)
                    ApiResult.Success(body)
                }
                else ApiResult.Error("Gagal menghapus foto")
            } else {
                ApiResult.Error(ApiErrorParser.parseError(response.errorBody()))
            }
        } catch (e: IOException) {
            ApiResult.Error("Kesalahan Koneksi: ${e.message}")
        } catch (e: Exception) {
            ApiResult.Error("Terjadi kesalahan: ${e.localizedMessage}")
        }
    }

    suspend fun submitStockOpname(auditId: Int, auditeeName: String, verificationPhotoFile: File): ApiResult<StockOpnameUpdateResponse> {
        return try {
            val auditIdBody = auditId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val auditeeNameBody = auditeeName.toRequestBody("text/plain".toMediaTypeOrNull())
            val photoPart = MultipartBody.Part.createFormData(
                "verification_photo",
                verificationPhotoFile.name,
                verificationPhotoFile.asRequestBody("image/*".toMediaTypeOrNull())
            )

            val response = api.submitStockOpname(auditIdBody, auditeeNameBody, photoPart)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    invalidateDetailCache(auditId)
                    invalidateHistoryCache()
                    ApiResult.Success(body)
                }
                else ApiResult.Error("Gagal mengirim stok opname")
            } else {
                ApiResult.Error(ApiErrorParser.parseError(response.errorBody()))
            }
        } catch (e: IOException) {
            ApiResult.Error("Kesalahan Koneksi: ${e.message}")
        } catch (e: Exception) {
            ApiResult.Error("Terjadi kesalahan: ${e.localizedMessage}")
        }
    }

    fun getStockOpnameHistories(
        auditorId: Int,
        departmentId: Int? = null,
        dateFrom: String? = null,
        dateTo: String? = null,
        page: Int? = null
    ): Flow<ApiResult<StockOpnameHistoryResponse>> = flow {
        val cacheKey = "${CACHE_KEY_OPNAME_HISTORY_PREFIX}${auditorId}_${departmentId}_${dateFrom}_${dateTo}_${page}"
        val cached = cache.get(cacheKey, StockOpnameHistoryResponse::class.java)
        if (cached != null) {
            emit(ApiResult.Success(cached))
        }

        try {
            val response = api.getStockOpnameHistories(auditorId, departmentId, dateFrom, dateTo, page)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    if (body != cached) {
                        cache.save(cacheKey, body)
                        emit(ApiResult.Success(body))
                    }
                } else if (cached == null) {
                    emit(ApiResult.Error("Data histori tidak ditemukan"))
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

    suspend fun downloadPdf(id: Int, auditorId: Int): ApiResult<okhttp3.ResponseBody> {
        return try {
            val response = api.exportStockOpnamePdf(id, auditorId)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) ApiResult.Success(body)
                else ApiResult.Error("Gagal mengunduh PDF")
            } else {
                ApiResult.Error(ApiErrorParser.parseError(response.errorBody()))
            }
        } catch (e: IOException) {
            ApiResult.Error("Kesalahan Koneksi: ${e.message}")
        } catch (e: Exception) {
            ApiResult.Error("Terjadi kesalahan: ${e.localizedMessage}")
        }
    }

    suspend fun sendEmail(auditId: Int, recipient: String, message: String?): ApiResult<GenericResponse> {
        return try {
            val response = api.sendStockOpnameEmail(SendEmailRequest(auditId, recipient, message))
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) ApiResult.Success(body)
                else ApiResult.Error("Gagal mengirim email")
            } else {
                ApiResult.Error(ApiErrorParser.parseError(response.errorBody()))
            }
        } catch (e: IOException) {
            ApiResult.Error("Kesalahan Koneksi: ${e.message}")
        } catch (e: Exception) {
            val msg = e.localizedMessage ?: "Terjadi kesalahan"
            val finalMsg = if (msg.contains("exhausted", ignoreCase = true)) {
                "Gagal mengirim, maksimal file 20 mb"
            } else msg
            ApiResult.Error(finalMsg)
        }
    }

    private fun invalidateDetailCache(auditId: Int) {
        cache.delete(CACHE_KEY_OPNAME_DETAIL_PREFIX + auditId)
    }

    private fun invalidateHistoryCache() {
        cache.clear()
    }

    fun getCachedDetail(id: Int): StockOpnameDetailResponse? {
        return cache.get(CACHE_KEY_OPNAME_DETAIL_PREFIX + id, StockOpnameDetailResponse::class.java)
    }

    fun getCachedHistories(
        auditorId: Int,
        departmentId: Int? = null,
        dateFrom: String? = null,
        dateTo: String? = null,
        page: Int? = null
    ): StockOpnameHistoryResponse? {
        val cacheKey = "${CACHE_KEY_OPNAME_HISTORY_PREFIX}${auditorId}_${departmentId}_${dateFrom}_${dateTo}_${page}"
        return cache.get(cacheKey, StockOpnameHistoryResponse::class.java)
    }
}
