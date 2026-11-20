package com.bengkelin.app.ui.auth

import com.bengkelin.app.data.model.MalangRegion
import com.bengkelin.app.data.model.User
import com.bengkelin.app.data.model.UserRole

data class AuthUiState(
    val name: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val selectedRole: UserRole = UserRole.PENGGUNA,
    val selectedRegion: MalangRegion = MalangRegion.KOTA_MALANG,
    val workshopName: String = "",
    val passwordVisible: Boolean = false,
    val confirmPasswordVisible: Boolean = false,

    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val currentUser: User? = null,
    val isAuthSuccessful: Boolean = false
)

sealed class AuthUiEvent {
    data class NameChanged(val name: String) : AuthUiEvent()
    data class EmailChanged(val email: String) : AuthUiEvent()
    data class PhoneChanged(val phone: String) : AuthUiEvent()
    data class PasswordChanged(val password: String) : AuthUiEvent()
    data class ConfirmPasswordChanged(val confirmPassword: String) : AuthUiEvent()
    data class RoleSelected(val role: UserRole) : AuthUiEvent()
    data class RegionSelected(val region: MalangRegion) : AuthUiEvent()
    data class WorkshopNameChanged(val workshopName: String) : AuthUiEvent()
    data object TogglePasswordVisibility : AuthUiEvent()
    data object ToggleConfirmPasswordVisibility : AuthUiEvent()
    data object SubmitLogin : AuthUiEvent()
    data object SubmitRegister : AuthUiEvent()
    data object ClearError : AuthUiEvent()
    data object ResetAuthState : AuthUiEvent()
}
