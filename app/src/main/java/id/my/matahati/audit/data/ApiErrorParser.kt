package id.my.matahati.audit.data

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.ResponseBody

object ApiErrorParser {

    fun parseError(errorBody: ResponseBody?): String {
        return try {
            val jsonString = errorBody?.string()
            if (jsonString.isNullOrEmpty()) {
                return "Terjadi kesalahan. Silakan coba lagi."
            }

            val type = object : TypeToken<Map<String, Any>>() {}.type
            val errorMap: Map<String, Any> = Gson().fromJson(jsonString, type)

            val message = errorMap["message"]?.toString() ?: "Terjadi kesalahan. Silakan coba lagi."
            
            // Handle Laravel Validation Errors (HTTP 422)
            val errors = errorMap["errors"] as? Map<*, *>
            if (errors != null && errors.isNotEmpty()) {
                val firstEntry = errors.entries.first()
                val fieldName = firstEntry.key.toString()
                val fieldErrors = firstEntry.value as? List<*>
                val detail = fieldErrors?.firstOrNull()?.toString() ?: ""
                
                return "$message\n$fieldName: $detail"
            }

            // Return plain message for 409, 404, 500 etc.
            message
        } catch (e: Exception) {
            "Terjadi kesalahan. Silakan coba lagi."
        }
    }
}
