package id.my.matahati.audit.data.repository

import android.content.Context
import id.my.matahati.audit.data.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.IOException

class StockRepository(context: Context) {

    private val api = RetrofitClientLaravel.instance
    private val cache = DataCacheManager(context)

    companion object {
        private const val CACHE_KEY_STOCK_CATEGORIES = "stock_categories"
        private const val CACHE_KEY_STOCK_ITEMS_PREFIX = "stock_items_"
        private const val CACHE_KEY_STOCK_DEPARTMENTS = "stock_departments_list"
        private const val CACHE_KEY_STOCK_MAPPING_PREFIX = "stock_department_mapping_"
    }

    fun getCategories(): Flow<ApiResult<StockCategoryListResponse>> = flow {
        val cached = getCachedCategories()
        if (cached != null) {
            emit(ApiResult.Success(cached))
        }

        try {
            val response = api.getStockCategories()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    if (body != cached) {
                        cache.save(CACHE_KEY_STOCK_CATEGORIES, body)
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

    fun getCategory(id: Int): Flow<ApiResult<StockCategoryResponse>> = flow {
        val cacheKey = CACHE_KEY_STOCK_ITEMS_PREFIX + id
        val cached = getCachedItems(id)
        if (cached != null) {
            emit(ApiResult.Success(cached))
        }

        try {
            val response = api.getStockItems(id)
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

    suspend fun createCategory(request: StockCategoryRequest): ApiResult<StockCategoryResponse> {
        return try {
            val response = api.createStockCategory(request)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    invalidateCategoriesCache()
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

    suspend fun updateCategory(id: Int, request: StockCategoryRequest): ApiResult<StockCategoryResponse> {
        return try {
            val spoofedRequest = request.copy(method = "PUT")
            val response = api.updateStockCategory(id, spoofedRequest)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    invalidateCategoriesCache()
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

    suspend fun deleteCategory(id: Int): ApiResult<GenericResponse> {
        return try {
            val response = api.deleteStockCategory(id, StockDeleteRequest())
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    invalidateCategoriesCache()
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

    suspend fun createItem(request: StockItemRequest): ApiResult<StockItemResponse> {
        return try {
            val response = api.createStockItem(request)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    invalidateItemsCache(request.categoryId)
                    ApiResult.Success(body)
                }
                else ApiResult.Error("Gagal menambah barang")
            } else {
                ApiResult.Error(ApiErrorParser.parseError(response.errorBody()))
            }
        } catch (e: IOException) {
            ApiResult.Error("Kesalahan Koneksi: ${e.message}")
        } catch (e: Exception) {
            ApiResult.Error("Terjadi kesalahan: ${e.localizedMessage}")
        }
    }

    suspend fun deleteItem(categoryId: Int, id: Int): ApiResult<GenericResponse> {
        return try {
            val response = api.deleteStockItem(id, StockDeleteRequest())
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    invalidateItemsCache(categoryId)
                    ApiResult.Success(body)
                }
                else ApiResult.Error("Gagal menghapus barang")
            } else {
                ApiResult.Error(ApiErrorParser.parseError(response.errorBody()))
            }
        } catch (e: IOException) {
            ApiResult.Error("Kesalahan Koneksi: ${e.message}")
        } catch (e: Exception) {
            ApiResult.Error("Terjadi kesalahan: ${e.localizedMessage}")
        }
    }

    suspend fun reorderItems(request: StockReorderRequest): ApiResult<GenericResponse> {
        return try {
            val response = api.reorderStockItems(request)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    invalidateItemsCache(request.categoryId)
                    ApiResult.Success(body)
                }
                else ApiResult.Error("Gagal mengurutkan barang")
            } else {
                ApiResult.Error(ApiErrorParser.parseError(response.errorBody()))
            }
        } catch (e: IOException) {
            ApiResult.Error("Kesalahan Koneksi: ${e.message}")
        } catch (e: Exception) {
            ApiResult.Error("Terjadi kesalahan: ${e.localizedMessage}")
        }
    }

    fun invalidateCategoriesCache() {
        cache.delete(CACHE_KEY_STOCK_CATEGORIES)
    }

    fun invalidateItemsCache(categoryId: Int) {
        cache.delete(CACHE_KEY_STOCK_ITEMS_PREFIX + categoryId)
    }

    fun getCachedCategories(): StockCategoryListResponse? {
        return cache.get(CACHE_KEY_STOCK_CATEGORIES, StockCategoryListResponse::class.java)
    }

    fun getCachedItems(categoryId: Int): StockCategoryResponse? {
        return cache.get(CACHE_KEY_STOCK_ITEMS_PREFIX + categoryId, StockCategoryResponse::class.java)
    }

    fun getCachedDepartments(): DepartmentListResponse? {
        return cache.get(CACHE_KEY_STOCK_DEPARTMENTS, DepartmentListResponse::class.java)
    }

    fun getCachedMapping(departmentId: Int): StockDepartmentMappingResponse? {
        return cache.get(CACHE_KEY_STOCK_MAPPING_PREFIX + departmentId, StockDepartmentMappingResponse::class.java)
    }

    // Mapping Methods
    fun getDepartments(): Flow<ApiResult<DepartmentListResponse>> = flow {
        val cached = cache.get(CACHE_KEY_STOCK_DEPARTMENTS, DepartmentListResponse::class.java)
        if (cached != null) {
            emit(ApiResult.Success(cached))
        }

        try {
            val response = api.getStockDepartments()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    if (body != cached) {
                        cache.save(CACHE_KEY_STOCK_DEPARTMENTS, body)
                        emit(ApiResult.Success(body))
                    }
                } else if (cached == null) {
                    emit(ApiResult.Error("Data tidak ditemukan"))
                }
            } else if (cached == null) {
                emit(ApiResult.Error(ApiErrorParser.parseError(response.errorBody())))
            }
        } catch (e: IOException) {
            if (cached == null) emit(ApiResult.Error("Kesalahan Koneksi: ${e.message}"))
        } catch (e: Exception) {
            if (cached == null) emit(ApiResult.Error("Terjadi kesalahan: ${e.localizedMessage}"))
        }
    }

    fun getDepartmentMapping(id: Int): Flow<ApiResult<StockDepartmentMappingResponse>> = flow {
        val cacheKey = CACHE_KEY_STOCK_MAPPING_PREFIX + id
        val cached = cache.get(cacheKey, StockDepartmentMappingResponse::class.java)
        if (cached != null) {
            emit(ApiResult.Success(cached))
        }

        try {
            val response = api.getStockDepartmentMapping(id)
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
            if (cached == null) emit(ApiResult.Error("Kesalahan Koneksi: ${e.message}"))
        } catch (e: Exception) {
            if (cached == null) emit(ApiResult.Error("Terjadi kesalahan: ${e.localizedMessage}"))
        }
    }

    suspend fun saveMapping(request: SaveStockMappingRequest): ApiResult<GenericResponse> {
        return try {
            val response = api.saveStockDepartmentMapping(request)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    invalidateMappingCache(request.departmentId)
                    ApiResult.Success(body)
                }
                else ApiResult.Error("Gagal menyimpan pemetaan")
            } else {
                ApiResult.Error(ApiErrorParser.parseError(response.errorBody()))
            }
        } catch (e: IOException) {
            ApiResult.Error("Kesalahan Koneksi: ${e.message}")
        } catch (e: Exception) {
            ApiResult.Error("Terjadi kesalahan: ${e.localizedMessage}")
        }
    }

    fun invalidateMappingCache(departmentId: Int) {
        cache.delete(CACHE_KEY_STOCK_MAPPING_PREFIX + departmentId)
    }
}
