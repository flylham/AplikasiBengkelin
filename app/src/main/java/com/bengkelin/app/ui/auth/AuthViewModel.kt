package com.bengkelin.app.ui.auth

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bengkelin.app.data.model.Resource
import com.bengkelin.app.data.model.UserRole
import com.bengkelin.app.data.repository.AuthRepository
import com.bengkelin.app.data.repository.AuthRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repository: AuthRepository = AuthRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        checkCachedSession()
    }

    private fun checkCachedSession() {
        viewModelScope.launch {
            val user = repository.getCachedUser()
            if (user != null && !user.token.isNullOrBlank()) {
                _uiState.update {
                    it.copy(
                        currentUser = user,
                        isAuthSuccessful = true
                    )
                }
            }
        }
    }

    fun onEvent(event: AuthUiEvent) {
        when (event) {
            is AuthUiEvent.NameChanged -> _uiState.update { it.copy(name = event.name, errorMessage = null) }
            is AuthUiEvent.EmailChanged -> _uiState.update { it.copy(email = event.email, errorMessage = null) }
            is AuthUiEvent.PhoneChanged -> _uiState.update { it.copy(phoneNumber = event.phone, errorMessage = null) }
            is AuthUiEvent.PasswordChanged -> _uiState.update { it.copy(password = event.password, errorMessage = null) }
            is AuthUiEvent.ConfirmPasswordChanged -> _uiState.update { it.copy(confirmPassword = event.confirmPassword, errorMessage = null) }
            is AuthUiEvent.RoleSelected -> _uiState.update { it.copy(selectedRole = event.role, errorMessage = null) }
            is AuthUiEvent.RegionSelected -> _uiState.update { it.copy(selectedRegion = event.region, errorMessage = null) }
            is AuthUiEvent.WorkshopNameChanged -> _uiState.update { it.copy(workshopName = event.workshopName, errorMessage = null) }
            is AuthUiEvent.TogglePasswordVisibility -> _uiState.update { it.copy(passwordVisible = !it.passwordVisible) }
            is AuthUiEvent.ToggleConfirmPasswordVisibility -> _uiState.update { it.copy(confirmPasswordVisible = !it.confirmPasswordVisible) }
            is AuthUiEvent.ClearError -> _uiState.update { it.copy(errorMessage = null) }
            is AuthUiEvent.ResetAuthState -> _uiState.update { AuthUiState() }
            is AuthUiEvent.SubmitLogin -> login()
            is AuthUiEvent.SubmitRegister -> register()
        }
    }

    private fun login() {
        val state = _uiState.value
        val email = state.email.trim()
        val password = state.password

        if (email.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Email wajib diisi.") }
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.update { it.copy(errorMessage = "Format email tidak valid.") }
            return
        }
        if (password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Kata sandi wajib diisi.") }
            return
        }

        viewModelScope.launch {
            repository.login(email, password).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                currentUser = resource.data,
                                isAuthSuccessful = true,
                                successMessage = "Selamat datang kembali, ${resource.data.name}!"
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update { it.copy(isLoading = false, errorMessage = resource.message) }
                    }
                    is Resource.Idle -> Unit
                }
            }
        }
    }

    private fun register() {
        val state = _uiState.value
        val name = state.name.trim()
        val email = state.email.trim()
        val phone = state.phoneNumber.trim()
        val password = state.password
        val confirmPassword = state.confirmPassword
        val role = UserRole.PENGGUNA // Pendaftaran publik selalu untuk peran Pengguna

        if (name.length < 3) {
            _uiState.update { it.copy(errorMessage = "Nama lengkap minimal 3 karakter.") }
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.update { it.copy(errorMessage = "Format email tidak valid.") }
            return
        }
        if (email.contains("admin", ignoreCase = true)) {
            _uiState.update { it.copy(errorMessage = "Pendaftaran akun Admin dinonaktifkan. Gunakan akun Admin bawaan di layar Masuk.") }
            return
        }
        if (phone.length < 10) {
            _uiState.update { it.copy(errorMessage = "Nomor WhatsApp/HP minimal 10 digit.") }
            return
        }
        if (password.length < 6) {
            _uiState.update { it.copy(errorMessage = "Kata sandi minimal 6 karakter.") }
            return
        }
        if (password != confirmPassword) {
            _uiState.update { it.copy(errorMessage = "Konfirmasi kata sandi tidak cocok.") }
            return
        }

        viewModelScope.launch {
            repository.register(
                name = name,
                email = email,
                password = password,
                phoneNumber = phone,
                role = UserRole.PENGGUNA,
                region = state.selectedRegion,
                workshopName = null
            ).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                currentUser = resource.data,
                                isAuthSuccessful = true,
                                successMessage = "Pendaftaran berhasil! Selamat datang di Bengkelin, ${resource.data.name}."
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update { it.copy(isLoading = false, errorMessage = resource.message) }
                    }
                    is Resource.Idle -> Unit
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _uiState.value = AuthUiState()
        }
    }
}
