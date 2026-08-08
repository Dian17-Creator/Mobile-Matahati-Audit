package id.my.matahati.audit.data

import com.google.gson.annotations.SerializedName

data class StockItemData(
    @SerializedName("id") val id: Int,
    @SerializedName("category_id") val categoryId: Int,
    @SerializedName("name") val name: String?,
    @SerializedName("sequence") val sequence: Int
)

data class StockCategoryData(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("items") val items: List<StockItemData>? = emptyList()
)

data class StockCategoryResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: StockCategoryData?
)

data class StockCategoryListResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: List<StockCategoryData>?
)

data class StockItemResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: StockItemData?
)

data class StockCategoryRequest(
    @SerializedName("cnama") val name: String,
    @SerializedName("cket") val description: String?,
    @SerializedName("_method") val method: String? = null
)

data class StockItemRequest(
    @SerializedName("nid_grp") val categoryId: Int,
    @SerializedName("citemname") val name: String
)

data class StockDeleteRequest(
    @SerializedName("_method") val method: String = "DELETE"
)

data class StockReorderRequest(
    @SerializedName("category_id") val categoryId: Int,
    @SerializedName("item_ids") val itemIds: List<Int>
)

// Stock Department Mapping Models
data class StockMappingItem(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("sequence") val sequence: Int,
    @SerializedName("linked") val linked: Boolean
)

data class StockMappingCategory(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("items") val items: List<StockMappingItem>
)

data class StockDepartmentMappingData(
    @SerializedName("department") val department: DepartmentData,
    @SerializedName("categories") val categories: List<StockMappingCategory>
)

data class StockDepartmentMappingResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: StockDepartmentMappingData?
)

data class SaveStockMappingRequest(
    @SerializedName("department_id") val departmentId: Int,
    @SerializedName("item_ids") val itemIds: List<Int>
)

