package id.my.matahati.audit.data.repository

import id.my.matahati.audit.data.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.IOException

class StockOpnameRepository {

    private val api = RetrofitClientLaravel.instance

    suspend fun createStockOpname(departmentId: Int, auditorId: Int): ApiResult<StockOpnameCreateResponse> {
        return try {
            val response = api.createStockOpname(StockOpnameCreateRequest(departmentId, auditorId))
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) ApiResult.Success(body)
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

    suspend fun getStockOpnameDetail(id: Int, auditorId: Int): ApiResult<StockOpnameDetailResponse> {
        return try {
            val response = api.getStockOpnameDetail(id, auditorId)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) ApiResult.Success(body)
                else ApiResult.Error("Data stok opname tidak ditemukan")
            } else {
                ApiResult.Error(ApiErrorParser.parseError(response.errorBody()))
            }
        } catch (e: IOException) {
            ApiResult.Error("Kesalahan Koneksi: ${e.message}")
        } catch (e: Exception) {
            ApiResult.Error("Terjadi kesalahan: ${e.localizedMessage}")
        }
    }

    suspend fun updateStockOpname(request: StockOpnameUpdateRequest): ApiResult<StockOpnameUpdateResponse> {
        return try {
            val response = api.updateStockOpname(request)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) ApiResult.Success(body)
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

    suspend fun uploadPhoto(responseId: Int, photoFile: File, remark: String?): ApiResult<StockOpnameUpdateResponse> {
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
                if (body != null) ApiResult.Success(body)
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

    suspend fun updatePhotoRemark(photoId: Int, remark: String?): ApiResult<StockOpnameUpdateResponse> {
        return try {
            val response = api.updateStockOpnamePhoto(StockOpnamePhotoUpdateRequest(photoId, remark))
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) ApiResult.Success(body)
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

    suspend fun deletePhoto(photoId: Int): ApiResult<StockOpnameUpdateResponse> {
        return try {
            val response = api.deleteStockOpnamePhoto(StockOpnamePhotoDeleteRequest(photoId))
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) ApiResult.Success(body)
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
                if (body != null) ApiResult.Success(body)
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

    suspend fun getStockOpnameHistories(
        auditorId: Int,
        departmentId: Int? = null,
        dateFrom: String? = null,
        dateTo: String? = null,
        page: Int? = null
    ): ApiResult<StockOpnameHistoryResponse> {
        return try {
            val response = api.getStockOpnameHistories(auditorId, departmentId, dateFrom, dateTo, page)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) ApiResult.Success(body)
                else ApiResult.Error("Data histori tidak ditemukan")
            } else {
                ApiResult.Error(ApiErrorParser.parseError(response.errorBody()))
            }
        } catch (e: IOException) {
            ApiResult.Error("Kesalahan Koneksi: ${e.message}")
        } catch (e: Exception) {
            ApiResult.Error("Terjadi kesalahan: ${e.localizedMessage}")
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
}
