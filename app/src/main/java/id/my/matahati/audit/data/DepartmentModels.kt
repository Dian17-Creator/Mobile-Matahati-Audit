package id.my.matahati.audit.data

import com.google.gson.annotations.SerializedName

data class DepartmentData(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String
)

data class DepartmentListResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: List<DepartmentData>?
)

data class MappingQuestion(
    @SerializedName("id") val id: Int,
    @SerializedName("question") val question: String,
    @SerializedName("linked") val linked: Boolean
)

data class MappingCategory(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("questions") val questions: List<MappingQuestion>
)

data class DepartmentMappingData(
    @SerializedName("department") val department: DepartmentData,
    @SerializedName("categories") val categories: List<MappingCategory>
)

data class DepartmentMappingResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: DepartmentMappingData?
)

data class SaveMappingRequest(
    @SerializedName("department_id") val departmentId: Int,
    @SerializedName("question_ids") val questionIds: List<Int>
)

data class GenericResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String
)
