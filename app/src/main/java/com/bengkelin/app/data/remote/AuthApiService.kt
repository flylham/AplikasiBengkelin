package com.bengkelin.app.data.remote

import com.bengkelin.app.data.remote.dto.AuthResponse
import com.bengkelin.app.data.remote.dto.LoginRequest
import com.bengkelin.app.data.remote.dto.RegisterRequest
import com.bengkelin.app.data.remote.dto.UserDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * Kontrak Retrofit HTTP REST API untuk server database backend Bengkelin.
 */
interface AuthApiService {

    @POST("api/auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<AuthResponse>

    @POST("api/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<AuthResponse>

    @GET("api/auth/me")
    suspend fun getCurrentUserProfile(
        @Header("Authorization") token: String
    ): Response<UserDto>

    @POST("api/auth/logout")
    suspend fun logout(
        @Header("Authorization") token: String
    ): Response<Unit>
}
