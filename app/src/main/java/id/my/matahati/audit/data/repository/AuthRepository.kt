package id.my.matahati.audit.data.repository

import id.my.matahati.audit.data.ApiErrorParser
import id.my.matahati.audit.data.ApiResult
import id.my.matahati.audit.data.LoginRequest
import id.my.matahati.audit.data.LoginResponse
import id.my.matahati.audit.data.RetrofitClientLaravel
import java.io.IOException

class AuthRepository {

    suspend fun login(
        email: String,
        password: String
    ): ApiResult<LoginResponse> {
        return try {
            val response = RetrofitClientLaravel.instance.login(
                LoginRequest(email, password)
            )

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    ApiResult.Success(body)
                } else {
                    ApiResult.Error("Terjadi kesalahan. Silakan coba lagi.")
                }
            } else {
                val errorMessage = ApiErrorParser.parseError(response.errorBody())
                ApiResult.Error(errorMessage)
            }
        } catch (e: IOException) {
            ApiResult.Error("Tidak dapat terhubung ke server. Periksa koneksi internet Anda.")
        } catch (e: Exception) {
            ApiResult.Error("Terjadi kesalahan. Silakan coba lagi.")
        }
    }
}
