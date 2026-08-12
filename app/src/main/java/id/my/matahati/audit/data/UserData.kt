package id.my.matahati.audit.data

import com.google.gson.annotations.SerializedName

data class UserData(
    @SerializedName("id")
    val nid: Int,
    val name: String,
    val email: String,
    val company: String?,
    val department_id: Int?,
    val department_name: String?,
    val role: UserRole
)