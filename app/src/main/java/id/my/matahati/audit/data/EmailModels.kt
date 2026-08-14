package id.my.matahati.audit.data

import com.google.gson.annotations.SerializedName

data class SendEmailRequest(
    @SerializedName("audit_id")
    val auditId: Int,
    
    @SerializedName("recipient")
    val recipient: String,
    
    @SerializedName("message")
    val message: String? = null
)
