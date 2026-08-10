package id.my.matahati.audit.data

import com.google.gson.annotations.SerializedName

data class StockOpnameHistoryItem(
    @SerializedName("id") val id: Int,
    @SerializedName("document_id") val documentId: String?,
    @SerializedName("department_id") val departmentId: Int?,
    @SerializedName("department_name") val departmentName: String?,
    @SerializedName("auditor_id") val auditorId: Int?,
    @SerializedName("auditor_name") val auditorName: String?,
    @SerializedName("audit_date") val auditDate: String?,
    @SerializedName("status") val status: String?,
    @SerializedName("auditee_name") val auditeeName: String?,
    @SerializedName("started_at") val startedAt: String?,
    @SerializedName("updated_at") val updatedAt: String?,
    @SerializedName("submitted_at") val submittedAt: String?
)

data class PaginationInfo(
    @SerializedName("current_page") val currentPage: Int,
    @SerializedName("last_page") val lastPage: Int,
    @SerializedName("per_page") val perPage: Int,
    @SerializedName("total") val total: Int,
    @SerializedName("from") val from: Int?,
    @SerializedName("to") val to: Int?
)

data class StockOpnameHistoryListData(
    @SerializedName("items") val items: List<StockOpnameHistoryItem>?,
    @SerializedName("pagination") val pagination: PaginationInfo?
)

data class StockOpnameHistoryResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: StockOpnameHistoryListData?
)
