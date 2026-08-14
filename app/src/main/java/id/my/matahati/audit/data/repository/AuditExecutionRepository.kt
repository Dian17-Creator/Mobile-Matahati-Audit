package id.my.matahati.audit.data.repository

import android.content.Context
import id.my.matahati.audit.data.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.IOException

class AuditExecutionRepository(context: Context) {

    private val api = RetrofitClientLaravel.instance
    private val cache = DataCacheManager(context)

    companion object {
        private const val CACHE_KEY_AUDIT_HISTORY_PREFIX = "audit_history_"
        private const val CACHE_KEY_AUDIT_DETAIL_PREFIX = "audit_detail_"
    }

    suspend fun createAudit(departmentId: Int, auditorId: Int): ApiResult<AuditCreateResponse> {
        return try {
            val response = api.createAudit(AuditCreateRequest(departmentId, auditorId))
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    invalidateHistoryCache(departmentId)
                    ApiResult.Success(body)
                }
                else ApiResult.Error("Gagal membuat audit")
            } else {
                ApiResult.Error(ApiErrorParser.parseError(response.errorBody()))
            }
        } catch (e: IOException) {
            ApiResult.Error("Kesalahan Koneksi: ${e.message}. Periksa koneksi internet Anda.")
        } catch (e: Exception) {
            ApiResult.Error("Terjadi kesalahan: ${e.localizedMessage}")
        }
    }

    fun getAuditDetail(auditId: Int): Flow<ApiResult<AuditDetailResponse>> = flow {
        val cacheKey = CACHE_KEY_AUDIT_DETAIL_PREFIX + auditId
        val cached = getCachedAuditDetail(auditId)
        if (cached != null) {
            emit(ApiResult.Success(cached))
        }

        try {
            val response = api.getAuditDetail(auditId)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    if (body != cached) {
                        cache.save(cacheKey, body)
                        emit(ApiResult.Success(body))
                    }
                } else if (cached == null) {
                    emit(ApiResult.Error("Data audit tidak ditemukan"))
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

    suspend fun updateAudit(auditId: Int, answers: List<AuditAnswer>): ApiResult<AuditUpdateResponse> {
        return try {
            val response = api.updateAudit(AuditUpdateRequest(auditId, answers))
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    invalidateDetailCache(auditId)
                    ApiResult.Success(body)
                }
                else ApiResult.Error("Gagal memperbarui audit")
            } else {
                ApiResult.Error(ApiErrorParser.parseError(response.errorBody()))
            }
        } catch (e: IOException) {
            ApiResult.Error("Kesalahan Koneksi: ${e.message}. Periksa koneksi internet Anda.")
        } catch (e: Exception) {
            ApiResult.Error("Terjadi kesalahan: ${e.localizedMessage}")
        }
    }

    suspend fun uploadPhoto(auditId: Int, responseId: Int, photoFile: File): ApiResult<AuditUpdateResponse> {
        return try {
            val auditIdBody = auditId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val responseIdBody = responseId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val photoPart = MultipartBody.Part.createFormData(
                "photo",
                photoFile.name,
                photoFile.asRequestBody("image/*".toMediaTypeOrNull())
            )

            val response = api.uploadAuditPhoto(auditIdBody, responseIdBody, photoPart)
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
            ApiResult.Error("Kesalahan Koneksi: ${e.message}. Periksa koneksi internet Anda.")
        } catch (e: Exception) {
            ApiResult.Error("Terjadi kesalahan: ${e.localizedMessage}")
        }
    }

    suspend fun updatePhotoDetail(photoId: Int, observation: String?, recommendation: String?, auditId: Int): ApiResult<AuditUpdateResponse> {
        return try {
            val response = api.updateAuditPhoto(AuditPhotoUpdateData(photoId, observation, recommendation))
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    invalidateDetailCache(auditId)
                    ApiResult.Success(body)
                }
                else ApiResult.Error("Gagal memperbarui detail foto")
            } else {
                ApiResult.Error(ApiErrorParser.parseError(response.errorBody()))
            }
        } catch (e: IOException) {
            ApiResult.Error("Kesalahan Koneksi: ${e.message}. Periksa koneksi internet Anda.")
        } catch (e: Exception) {
            ApiResult.Error("Terjadi kesalahan: ${e.localizedMessage}")
        }
    }

    suspend fun deletePhoto(photoId: Int, auditId: Int): ApiResult<AuditUpdateResponse> {
        return try {
            val response = api.deleteAuditPhoto(AuditDeletePhotoRequest(photoId))
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
            ApiResult.Error("Kesalahan Koneksi: ${e.message}. Periksa koneksi internet Anda.")
        } catch (e: Exception) {
            ApiResult.Error("Terjadi kesalahan: ${e.localizedMessage}")
        }
    }

    suspend fun submitAudit(auditId: Int, auditeeName: String, verificationPhotoFile: File): ApiResult<AuditSubmitResponse> {
        return try {
            val auditIdBody = auditId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val auditeeNameBody = auditeeName.toRequestBody("text/plain".toMediaTypeOrNull())
            val photoPart = MultipartBody.Part.createFormData(
                "verification_photo",
                verificationPhotoFile.name,
                verificationPhotoFile.asRequestBody("image/*".toMediaTypeOrNull())
            )

            val response = api.submitAudit(auditIdBody, auditeeNameBody, photoPart)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    invalidateDetailCache(auditId)
                    // Also invalidate history as status changed
                    cache.clear() // Simple way to clear all history caches
                    ApiResult.Success(body)
                }
                else ApiResult.Error("Gagal mengirim audit")
            } else {
                ApiResult.Error(ApiErrorParser.parseError(response.errorBody()))
            }
        } catch (e: IOException) {
            ApiResult.Error("Kesalahan Koneksi: ${e.message}. Periksa koneksi internet Anda.")
        } catch (e: Exception) {
            ApiResult.Error("Terjadi kesalahan: ${e.localizedMessage}")
        }
    }

    fun getAudits(departmentId: Int? = null, dateFrom: String? = null, dateTo: String? = null): Flow<ApiResult<AuditHistoryResponse>> = flow {
        val cacheKey = "${CACHE_KEY_AUDIT_HISTORY_PREFIX}${departmentId}_${dateFrom}_${dateTo}"
        val cached = getCachedAudits(departmentId, dateFrom, dateTo)
        if (cached != null) {
            emit(ApiResult.Success(cached))
        }

        try {
            val response = api.getAudits(departmentId, dateFrom, dateTo)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    if (body != cached) {
                        cache.save(cacheKey, body)
                        emit(ApiResult.Success(body))
                    }
                } else if (cached == null) {
                    emit(ApiResult.Error("Data audit tidak ditemukan"))
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

    suspend fun deleteAudit(auditId: Int, departmentId: Int?): ApiResult<AuditUpdateResponse> {
        return try {
            val response = api.deleteAudit(GenericIdRequest(auditId))
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    invalidateDetailCache(auditId)
                    departmentId?.let { invalidateHistoryCache(it) }
                    ApiResult.Success(body)
                }
                else ApiResult.Error("Gagal menghapus audit")
            } else {
                ApiResult.Error(ApiErrorParser.parseError(response.errorBody()))
            }
        } catch (e: IOException) {
            ApiResult.Error("Kesalahan Koneksi: ${e.message}. Periksa koneksi internet Anda.")
        } catch (e: Exception) {
            ApiResult.Error("Terjadi kesalahan: ${e.localizedMessage}")
        }
    }

    suspend fun downloadPdf(id: Int): ApiResult<okhttp3.ResponseBody> {
        return try {
            val response = api.exportAuditPdf(id)
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
            val response = api.sendAuditEmail(SendEmailRequest(auditId, recipient, message))
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
        cache.delete(CACHE_KEY_AUDIT_DETAIL_PREFIX + auditId)
    }

    private fun invalidateHistoryCache(departmentId: Int) {
        cache.clear()
    }

    fun getCachedAuditDetail(auditId: Int): AuditDetailResponse? {
        return cache.get(CACHE_KEY_AUDIT_DETAIL_PREFIX + auditId, AuditDetailResponse::class.java)
    }

    fun getCachedAudits(departmentId: Int?, dateFrom: String?, dateTo: String?): AuditHistoryResponse? {
        val cacheKey = "${CACHE_KEY_AUDIT_HISTORY_PREFIX}${departmentId}_${dateFrom}_${dateTo}"
        return cache.get(cacheKey, AuditHistoryResponse::class.java)
    }
}
