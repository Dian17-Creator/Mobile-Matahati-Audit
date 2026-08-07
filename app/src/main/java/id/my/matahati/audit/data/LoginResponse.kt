package id.my.matahati.audit.data

data class LoginResponse(
    val success: Boolean,
    val message: String,
    val data: UserData?
)