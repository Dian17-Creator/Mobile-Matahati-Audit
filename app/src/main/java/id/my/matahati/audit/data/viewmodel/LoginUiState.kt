package id.my.matahati.audit.data.viewmodel

import id.my.matahati.audit.data.UserData

data class LoginUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    val userData: UserData? = null
)
