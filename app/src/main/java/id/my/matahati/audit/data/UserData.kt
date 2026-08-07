package id.my.matahati.audit.data

data class UserData(
    val id: Int,
    val name: String,
    val email: String,
    val company: String?,
    val department_id: Int?,
    val department_name: String?,
    val role: UserRole
)