package com.bengkelin.app.data.remote.dto

import com.bengkelin.app.data.model.User
import com.google.gson.annotations.SerializedName

/**
 * Request body untuk registrasi akun baru
 */
data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    @SerializedName("phone_number")
    val phoneNumber: String,
    val role: String,
    val region: String,
    @SerializedName("workshop_name")
    val workshopName: String? = null
)

/**
 * Request body untuk login akun
 */
data class LoginRequest(
    val email: String,
    val password: String
)

/**
 * Response dari endpoint login / register yang mengembalikan JWT token & profil
 */
data class AuthResponse(
    val success: Boolean,
    val message: String,
    val token: String?,
    val user: UserDto?
)

/**
 * Format representasi data pengguna dari database REST API backend
 */
data class UserDto(
    val id: String,
    val name: String,
    val email: String,
    @SerializedName("phone_number")
    val phoneNumber: String,
    val role: String,
    val region: String,
    @SerializedName("workshop_name")
    val workshopName: String?,
    @SerializedName("is_verified")
    val isVerified: Boolean,
    @SerializedName("created_at")
    val createdAt: String?,
    @SerializedName("updated_at")
    val updatedAt: String?
) {
    fun toUser(jwtToken: String?): User {
        return User(
            id = id,
            name = name,
            email = email,
            phoneNumber = phoneNumber,
            role = role,
            region = region,
            workshopName = workshopName,
            isVerified = isVerified,
            token = jwtToken,
            createdAt = createdAt ?: "",
            updatedAt = updatedAt ?: ""
        )
    }
}

/**
 * Response standar untuk pesan error backend
 */
data class ApiErrorResponse(
    val success: Boolean = false,
    val message: String = "Terjadi kesalahan pada server."
)
