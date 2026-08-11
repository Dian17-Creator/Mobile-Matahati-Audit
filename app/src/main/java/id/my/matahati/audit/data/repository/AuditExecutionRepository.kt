package id.my.matahati.audit.data.repository

import id.my.matahati.audit.data.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.IOException

class AuditExecutionRepository {

    private val api = RetrofitClientLaravel.instance

    suspend fun createAudit(departmentId: Int, auditorId: Int): ApiResult<AuditCreateResponse> {
        return try {
            val response = api.createAudit(AuditCreateRequest(departmentId, auditorId))
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) ApiResult.Success(body)
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

    suspend fun getAuditDetail(auditId: Int): ApiResult<AuditDetailResponse> {
        return try {
            val response = api.getAuditDetail(auditId)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) ApiResult.Success(body)
                else ApiResult.Error("Data audit tidak ditemukan")
            } else {
                ApiResult.Error(ApiErrorParser.parseError(response.errorBody()))
            }
        } catch (e: IOException) {
            ApiResult.Error("Kesalahan Koneksi: ${e.message}. Periksa koneksi internet Anda.")
        } catch (e: Exception) {
            ApiResult.Error("Terjadi kesalahan: ${e.localizedMessage}")
        }
    }

    suspend fun updateAudit(auditId: Int, answers: List<AuditAnswer>): ApiResult<AuditUpdateResponse> {
        return try {
            val response = api.updateAudit(AuditUpdateRequest(auditId, answers))
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) ApiResult.Success(body)
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
                if (body != null) ApiResult.Success(body)
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

    suspend fun updatePhotoDetail(photoId: Int, observation: String?, recommendation: String?): ApiResult<AuditUpdateResponse> {
        return try {
            val response = api.updateAuditPhoto(AuditPhotoUpdateData(photoId, observation, recommendation))
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) ApiResult.Success(body)
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

    suspend fun deletePhoto(photoId: Int): ApiResult<AuditUpdateResponse> {
        return try {
            val response = api.deleteAuditPhoto(AuditDeletePhotoRequest(photoId))
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) ApiResult.Success(body)
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
                if (body != null) ApiResult.Success(body)
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

    suspend fun getAudits(departmentId: Int? = null, dateFrom: String? = null, dateTo: String? = null): ApiResult<AuditHistoryResponse> {
        return try {
            val response = api.getAudits(departmentId, dateFrom, dateTo)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) ApiResult.Success(body)
                else ApiResult.Error("Data audit tidak ditemukan")
            } else {
                ApiResult.Error(ApiErrorParser.parseError(response.errorBody()))
            }
        } catch (e: IOException) {
            ApiResult.Error("Kesalahan Koneksi: ${e.message}. Periksa koneksi internet Anda.")
        } catch (e: Exception) {
            ApiResult.Error("Terjadi kesalahan: ${e.localizedMessage}")
        }
    }

    suspend fun deleteAudit(auditId: Int): ApiResult<AuditUpdateResponse> {
        return try {
            val response = api.deleteAudit(GenericIdRequest(auditId))
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) ApiResult.Success(body)
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
}
