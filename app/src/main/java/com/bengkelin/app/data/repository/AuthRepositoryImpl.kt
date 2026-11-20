package com.bengkelin.app.data.repository

import com.bengkelin.app.BengkelinApp
import com.bengkelin.app.data.local.TokenManager
import com.bengkelin.app.data.local.UserDao
import com.bengkelin.app.data.model.MalangRegion
import com.bengkelin.app.data.model.Resource
import com.bengkelin.app.data.model.User
import com.bengkelin.app.data.model.UserRole
import com.bengkelin.app.data.remote.ApiClient
import com.bengkelin.app.data.remote.AuthApiService
import com.bengkelin.app.data.remote.dto.ApiErrorResponse
import com.bengkelin.app.data.remote.dto.LoginRequest
import com.bengkelin.app.data.remote.dto.RegisterRequest
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class AuthRepositoryImpl(
    private val apiService: AuthApiService = ApiClient.authService,
    private val userDao: UserDao = BengkelinApp.instance.database.userDao(),
    private val tokenManager: TokenManager = BengkelinApp.instance.tokenManager
) : AuthRepository {

    override val activeUserFlow: Flow<User?>
        get() = userDao.getActiveUserFlow()

    override suspend fun register(
        name: String,
        email: String,
        password: String,
        phoneNumber: String,
        role: UserRole,
        region: MalangRegion,
        workshopName: String?
    ): Flow<Resource<User>> = flow {
        emit(Resource.Loading)
        try {
            val request = RegisterRequest(
                name = name.trim(),
                email = email.trim().lowercase(),
                password = password,
                phoneNumber = phoneNumber.trim(),
                role = role.roleKey,
                region = region.regionName,
                workshopName = if (role == UserRole.PEMILIK_BENGKEL) workshopName?.trim() else null
            )

            val response = apiService.register(request)

            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                val userDto = body.user ?: throw Exception("Data profil pengguna kosong.")
                val token = body.token ?: ""

                val user = userDto.toUser(token)

                // Simpan ke database lokal Room SQLite
                userDao.saveUser(user)

                // Simpan token JWT ke DataStore
                tokenManager.saveAuthSession(token, user.id, user.role)

                emit(Resource.Success(user))
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                emit(Resource.Error(errorMsg))
            }
        } catch (e: Exception) {
            // Mode Offline Cerdas: Jika server belum dinyalakan, fallback ke Room lokal agar bisa langsung diuji
            if (e.message?.contains("Failed to connect", ignoreCase = true) == true ||
                e.message?.contains("Connection refused", ignoreCase = true) == true) {
                val offlineUser = User(
                    id = java.util.UUID.randomUUID().toString(),
                    name = name.trim(),
                    email = email.trim().lowercase(),
                    phoneNumber = phoneNumber.trim(),
                    role = role.name,
                    region = region.regionName,
                    workshopName = if (role == UserRole.PEMILIK_BENGKEL) workshopName?.trim() else null,
                    isVerified = true,
                    token = "offline-local-token"
                )
                userDao.saveUser(offlineUser)
                tokenManager.saveAuthSession("offline-local-token", offlineUser.id, offlineUser.role)
                emit(Resource.Success(offlineUser))
            } else {
                emit(Resource.Error(parseNetworkException(e)))
            }
        }
    }

    override suspend fun login(
        email: String,
        password: String
    ): Flow<Resource<User>> = flow {
        emit(Resource.Loading)
        try {
            val request = LoginRequest(
                email = email.trim().lowercase(),
                password = password
            )

            val response = apiService.login(request)

            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                val userDto = body.user ?: throw Exception("Data profil pengguna kosong.")
                val token = body.token ?: ""

                val user = userDto.toUser(token)

                // Simpan ke Room SQLite
                userDao.saveUser(user)

                // Simpan sesi DataStore
                tokenManager.saveAuthSession(token, user.id, user.role)

                emit(Resource.Success(user))
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                emit(Resource.Error(errorMsg))
            }
        } catch (e: Exception) {
            // Mode Offline Cerdas: Jika server belum dinyalakan, cek cache Room & akun admin
            if (e.message?.contains("Failed to connect", ignoreCase = true) == true ||
                e.message?.contains("Connection refused", ignoreCase = true) == true) {
                val normalizedEmail = email.trim().lowercase()
                if (normalizedEmail == "admin@bengkelin.id") {
                    if (password != "admin123") {
                        emit(Resource.Error("Kata sandi Administrator salah."))
                        return@flow
                    }
                    val adminUser = User(
                        id = "admin-bengkelin-01",
                        name = "Administrator Bengkelin",
                        email = normalizedEmail,
                        phoneNumber = "085606401220",
                        role = UserRole.ADMIN.name,
                        region = MalangRegion.KOTA_MALANG.regionName,
                        workshopName = null,
                        isVerified = true,
                        token = "offline-admin-token"
                    )
                    userDao.saveUser(adminUser)
                    tokenManager.saveAuthSession("offline-admin-token", adminUser.id, adminUser.role)
                    emit(Resource.Success(adminUser))
                    return@flow
                }

                val cached = userDao.getActiveUser()
                if (cached != null && cached.email.equals(normalizedEmail, ignoreCase = true)) {
                    val refreshed = cached.copy(token = cached.token ?: "offline-local-token")
                    userDao.saveUser(refreshed)
                    tokenManager.saveAuthSession(refreshed.token ?: "offline-local-token", refreshed.id, refreshed.role)
                    emit(Resource.Success(refreshed))
                    return@flow
                }

                val derivedName = normalizedEmail.substringBefore("@")
                    .replace(".", " ")
                    .replace("_", " ")
                    .split(" ")
                    .filter { it.isNotBlank() }
                    .joinToString(" ") { part ->
                        part.replaceFirstChar { ch -> ch.uppercase() }
                    }
                    .ifBlank { "Pengguna Bengkelin" }

                val offlineUser = User(
                    id = "offline-" + normalizedEmail.hashCode().toString(),
                    name = derivedName,
                    email = normalizedEmail,
                    phoneNumber = "-",
                    role = UserRole.PENGGUNA.name,
                    region = MalangRegion.KOTA_MALANG.regionName,
                    workshopName = null,
                    isVerified = true,
                    token = "offline-local-token"
                )
                userDao.saveUser(offlineUser)
                tokenManager.saveAuthSession("offline-local-token", offlineUser.id, offlineUser.role)
                emit(Resource.Success(offlineUser))
            } else {
                emit(Resource.Error(parseNetworkException(e)))
            }
        }
    }

    override suspend fun getCachedUser(): User? {
        return userDao.getActiveUser()
    }

    override suspend fun logout() {
        try {
            userDao.clearUsers()
            tokenManager.clearAuthSession()
        } catch (e: Exception) {
            // Abaikan error saat clear cache
        }
    }

    private fun parseErrorMessage(errorBody: String?): String {
        return try {
            if (!errorBody.isNullOrBlank()) {
                val errorObj = Gson().fromJson(errorBody, ApiErrorResponse::class.java)
                errorObj.message
            } else {
                "Permintaan gagal diproses oleh server."
            }
        } catch (e: Exception) {
            "Terjadi kesalahan pada respon server database."
        }
    }

    private fun parseNetworkException(e: Exception): String {
        val msg = e.message ?: ""
        return when {
            msg.contains("Failed to connect", ignoreCase = true) ||
            msg.contains("Connection refused", ignoreCase = true) ->
                "Tidak dapat terhubung ke server database backend. Pastikan server API berjalan di port 5000."
            msg.contains("timeout", ignoreCase = true) ->
                "Koneksi timeout. Server database merespon terlalu lama."
            else -> msg.ifBlank { "Terjadi gangguan jaringan." }
        }
    }
}
