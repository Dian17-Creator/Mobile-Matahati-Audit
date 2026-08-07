package id.my.matahati.audit.data

import com.google.gson.annotations.SerializedName

data class UserRole(

    @SerializedName("admin")
    val admin: Boolean,

    @SerializedName("super")
    val superAdmin: Boolean,

    @SerializedName("hrd")
    val hrd: Boolean,

    @SerializedName("audit")
    val audit: Boolean

)