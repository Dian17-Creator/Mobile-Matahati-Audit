package id.my.matahati.audit.data

import com.google.gson.annotations.SerializedName

data class CategoryData(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String?,
    @SerializedName("question_count") val questionCount: Int,
    @SerializedName("created_at") val createdAt: String?
)

data class CategoryResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: CategoryData?
)

data class CategoryListResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: List<CategoryData>?
)

data class CategoryRequest(
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String?
)
