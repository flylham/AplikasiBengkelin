package com.bengkelin.app.data.repository

import com.bengkelin.app.data.model.MalangRegion
import com.bengkelin.app.data.model.Resource
import com.bengkelin.app.data.model.User
import com.bengkelin.app.data.model.UserRole
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val activeUserFlow: Flow<User?>
    
    suspend fun register(
        name: String,
        email: String,
        password: String,
        phoneNumber: String,
        role: UserRole,
        region: MalangRegion,
        workshopName: String? = null
    ): Flow<Resource<User>>

    suspend fun login(
        email: String,
        password: String
    ): Flow<Resource<User>>

    suspend fun getCachedUser(): User?

    suspend fun logout()
}
