package id.my.matahati.audit.data

import com.google.gson.annotations.SerializedName

data class AuditHistoryItem(
    @SerializedName("nid") val id: Int,
    @SerializedName("cdocid") val documentId: String?,
    @SerializedName("nid_dept") val departmentId: Int?,
    @SerializedName("department_name") val departmentName: String?,
    @SerializedName("cstatus") val status: String?,
    @SerializedName("created_at") val createdAt: String?
)

data class AuditHistoryResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: List<AuditHistoryItem>?
)
