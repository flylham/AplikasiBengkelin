package com.bengkelin.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entitas User yang disimpan di tabel lokal Room SQLite 'users'
 * dan dipetakan dari respon REST API backend.
 */
@Entity(tableName = "users")
data class User(
    @PrimaryKey
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val role: String = UserRole.PENGGUNA.roleKey,
    val region: String = MalangRegion.KOTA_MALANG.regionName,
    val workshopName: String? = null,
    val isVerified: Boolean = false,
    val token: String? = null,
    val createdAt: String = "",
    val updatedAt: String = ""
) {
    val userRole: UserRole
        get() = UserRole.fromString(role)

    val malangRegion: MalangRegion
        get() = MalangRegion.fromString(region)
}
