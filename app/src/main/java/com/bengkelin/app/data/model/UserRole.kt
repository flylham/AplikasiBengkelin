package com.bengkelin.app.data.model

/**
 * Representasi peran pengguna Bengkelin Malang Raya.
 * Catatan Kebijakan:
 * - Pendaftaran publik HANYA untuk PENGGUNA (Pengendara motor/mobil).
 * - Akun ADMIN disediakan dari awal secara sistem (admin@bengkelin.id / admin123) sehingga tidak ada yang dapat mendaftar admin.
 */
enum class UserRole(val roleKey: String, val displayName: String, val description: String) {
    PENGGUNA(
        roleKey = "PENGGUNA",
        displayName = "Pengguna (Pengendara)",
        description = "Mencari bengkel terdekat, booking servis, dan panggilan montir darurat."
    ),
    ADMIN(
        roleKey = "ADMIN",
        displayName = "Administrator (Bawaan)",
        description = "Akun bawaan default untuk pengawasan bengkel dan operasional Malang Raya."
    ),
    @Deprecated("Pendaftaran bengkel dihapus. Pendaftaran baru hanya untuk Pengguna.")
    PEMILIK_BENGKEL(
        roleKey = "PEMILIK_BENGKEL",
        displayName = "Mitra Bengkel (Arsip)",
        description = "Data bengkel rekanan yang diverifikasi oleh Admin."
    );

    companion object {
        fun fromString(value: String?): UserRole {
            return entries.find { it.roleKey.equals(value, ignoreCase = true) } ?: PENGGUNA
        }
    }
}
