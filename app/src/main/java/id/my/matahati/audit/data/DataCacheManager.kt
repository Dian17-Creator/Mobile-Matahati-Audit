package id.my.matahati.audit.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson

class DataCacheManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val PREF_NAME = "audit_data_cache"
    }

    /**
     * Save data to cache as JSON string
     */
    fun <T> save(key: String, data: T) {
        val json = gson.toJson(data)
        prefs.edit().putString(key, json).apply()
    }

    /**
     * Get data from cache and parse from JSON
     */
    fun <T> get(key: String, type: Class<T>): T? {
        val json = prefs.getString(key, null) ?: return null
        return try {
            gson.fromJson(json, type)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Delete specific cache key
     */
    fun delete(key: String) {
        prefs.edit().remove(key).apply()
    }

    /**
     * Clear all cached data
     */
    fun clear() {
        prefs.edit().clear().apply()
    }
}
