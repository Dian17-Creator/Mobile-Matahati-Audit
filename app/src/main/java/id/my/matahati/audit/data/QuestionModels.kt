package id.my.matahati.audit.data

import com.google.gson.annotations.SerializedName

data class QuestionData(
    @SerializedName("id") val id: Int,
    @SerializedName("category_id") val categoryId: Int,
    @SerializedName("question") val question: String,
    @SerializedName("sequence") val sequence: Int,
    @SerializedName("active") val active: Boolean
)

data class QuestionResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: QuestionData?
)

data class QuestionListResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: List<QuestionData>?
)

data class QuestionRequest(
    @SerializedName("category_id") val categoryId: Int? = null,
    @SerializedName("question") val question: String
)

data class ReorderRequest(
    @SerializedName("category_id") val categoryId: Int,
    @SerializedName("question_ids") val questionIds: List<Int>
)
