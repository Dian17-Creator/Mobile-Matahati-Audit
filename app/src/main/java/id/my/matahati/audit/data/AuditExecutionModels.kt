package id.my.matahati.audit.data

import com.google.gson.annotations.SerializedName

data class AuditCreateRequest(
    @SerializedName("department_id") val departmentId: Int,
    @SerializedName("auditor_id") val auditorId: Int
)

data class AuditCreateData(
    @SerializedName("id") val id: Int,
    @SerializedName("document_id") val documentId: String,
    @SerializedName("status") val status: String
)

data class AuditCreateResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: AuditCreateData?
)

data class AuditPhotoDetail(
    @SerializedName("id") val id: Int,
    @SerializedName("photo_path") val photoPath: String,
    @SerializedName("remark") val remark: String?,
    @SerializedName("action") val action: String?
)

data class AuditResponseDetail(
    @SerializedName("id") val id: Int,
    @SerializedName("score") val score: String?,
    @SerializedName("is_na") val isNa: Boolean,
    @SerializedName("remark") val remark: String?
)

data class AuditQuestionDetail(
    @SerializedName("id") val id: Int,
    @SerializedName("question") val question: String,
    @SerializedName("sequence") val sequence: Int,
    @SerializedName("response") val response: AuditResponseDetail?,
    @SerializedName("photos") val photos: List<AuditPhotoDetail> = emptyList()
)

data class AuditCategoryDetail(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("total_score") val totalScore: Double,
    @SerializedName("max_score") val maxScore: Double,
    @SerializedName("percentage") val percentage: Double,
    @SerializedName("questions") val questions: List<AuditQuestionDetail>
)

data class AuditInfo(
    @SerializedName("id") val id: Int,
    @SerializedName("document_id") val documentId: String,
    @SerializedName("status") val status: String,
    @SerializedName("audit_date") val auditDate: String?,
    @SerializedName("department_name") val departmentName: String,
    @SerializedName("auditor_name") val auditorName: String,
    @SerializedName("total_score") val totalScore: Double,
    @SerializedName("max_score") val maxScore: Double,
    @SerializedName("percentage") val percentage: Double,
    @SerializedName("verification_photo") val verificationPhoto: String?,
    @SerializedName("auditee_name") val auditeeName: String?,
    @SerializedName("submitted_at") val submittedAt: String?
)

data class AuditDetailContainer(
    @SerializedName("audit") val audit: AuditInfo,
    @SerializedName("categories") val categories: List<AuditCategoryDetail>
)

data class AuditDetailResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: AuditDetailContainer?
)

data class AuditAnswer(
    @SerializedName("question_id") val questionId: Int,
    @SerializedName("score") val score: String?,
    @SerializedName("remark") val notes: String?
)

data class AuditUpdateRequest(
    @SerializedName("audit_id") val auditId: Int,
    @SerializedName("answers") val answers: List<AuditAnswer>
)

data class AuditUpdateResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String
)

data class AuditPhotoUpdateData(
    @SerializedName("id") val id: Int,
    @SerializedName("observation") val observation: String?,
    @SerializedName("recommendation") val recommendation: String?
)

data class AuditDeletePhotoRequest(
    @SerializedName("photo_id") val photoId: Int
)

data class GenericIdRequest(
    @SerializedName("id") val id: Int
)

data class IncompleteQuestion(
    @SerializedName("id") val id: Int,
    @SerializedName("question") val question: String
)

data class AuditSubmitResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("incomplete_questions") val incompleteQuestions: List<IncompleteQuestion>? = null
)
