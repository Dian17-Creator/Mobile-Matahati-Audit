package id.my.matahati.audit.data

import com.google.gson.annotations.SerializedName

data class StockOpnameCreateRequest(
    @SerializedName("department_id") val departmentId: Int,
    @SerializedName("nid_auditor") val auditorId: Int
)

data class StockOpnameCreateData(
    @SerializedName("id") val id: Int,
    @SerializedName("document_id") val documentId: String,
    @SerializedName("status") val status: String
)

data class StockOpnameCreateResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: StockOpnameCreateData?
)

data class StockOpnameHeader(
    @SerializedName("id") val id: Int,
    @SerializedName("document_id") val documentId: String,
    @SerializedName("department_name") val departmentName: String,
    @SerializedName("auditor_name") val auditorName: String,
    @SerializedName("status") val status: String,
    @SerializedName("audit_date") val auditDate: String?,
    @SerializedName("auditee_name") val auditeeName: String?,
    @SerializedName("verification_photo") val verificationPhoto: String?,
    @SerializedName("started_at") val startedAt: String?,
    @SerializedName("submitted_at") val submittedAt: String?
)

data class StockOpnamePhoto(
    @SerializedName("id") val id: Int,
    @SerializedName("response_id") val responseId: Int,
    @SerializedName("photo_path") val photoPath: String,
    @SerializedName("remark") val remark: String?,
    @SerializedName("uploaded_at") val uploadedAt: String?
)

data class StockOpnameItemResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("qty_stock") val qtyStock: String?,
    @SerializedName("qty_real") val qtyReal: String?,
    @SerializedName("diff") val diff: String?,
    @SerializedName("diff_under") val diffUnder: String?,
    @SerializedName("diff_over") val diffOver: String?,
    @SerializedName("remark") val remark: String?
)

data class StockOpnameItem(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("sequence") val sequence: Int,
    @SerializedName("response") val response: StockOpnameItemResponse?,
    @SerializedName("photos") val photos: List<StockOpnamePhoto> = emptyList()
)

data class StockOpnameCategory(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("items") val items: List<StockOpnameItem>
)

data class StockOpnameDetailData(
    @SerializedName("header") val header: StockOpnameHeader,
    @SerializedName("categories") val categories: List<StockOpnameCategory>
)

data class StockOpnameDetailResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: StockOpnameDetailData?
)

data class StockOpnameUpdateRequest(
    @SerializedName("audit_id") val auditId: Int,
    @SerializedName("item_id") val itemId: Int,
    @SerializedName("qty_stock") val qtyStock: Double?,
    @SerializedName("qty_real") val qtyReal: Double?,
    @SerializedName("remark") val remark: String?
)

data class StockOpnameUpdateResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("incomplete_items") val incompleteItems: List<Int>? = null
)

data class StockOpnamePhotoUpdateRequest(
    @SerializedName("photo_id") val photoId: Int,
    @SerializedName("remark") val remark: String?
)

data class StockOpnamePhotoDeleteRequest(
    @SerializedName("photo_id") val photoId: Int
)
