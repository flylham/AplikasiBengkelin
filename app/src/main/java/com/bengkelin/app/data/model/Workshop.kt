package com.bengkelin.app.data.model

import com.google.gson.annotations.SerializedName

enum class WorkshopCategory(val key: String, val displayName: String) {
    MOTOR("MOTOR", "Bengkel Motor"),
    MOBIL("MOBIL", "Bengkel Mobil");

    companion object {
        fun fromString(value: String?): WorkshopCategory {
            return entries.find { it.key.equals(value, ignoreCase = true) } ?: MOTOR
        }
    }
}

/**
 * Model data Bengkel di Malang Raya yang bersumber dari Google Maps Places.
 */
data class Workshop(
    val id: String = "",
    val name: String = "",
    val category: String = "MOTOR",
    val region: String = "Kota Malang",
    val address: String = "",
    val latitude: Double = -7.9839,
    val longitude: Double = 112.6213,
    @SerializedName("distance_km")
    val distanceKm: Double = 0.0,
    val rating: Double = 4.5,
    @SerializedName("reviews_count")
    val reviewsCount: Int = 0,
    @SerializedName("is_open")
    val isOpen: Boolean = true,
    @SerializedName("opening_hours")
    val openingHours: String = "08:00 - 17:00 WIB",
    @SerializedName("phone_number")
    val phoneNumber: String = "",
    val specialties: List<String> = emptyList(),
    @SerializedName("google_maps_url")
    val googleMapsUrl: String = ""
) {
    val workshopCategory: WorkshopCategory
        get() = WorkshopCategory.fromString(category)

    val malangRegion: MalangRegion
        get() = MalangRegion.fromString(region)
}

data class WorkshopListResponse(
    val success: Boolean = true,
    val total: Int = 0,
    val workshops: List<Workshop> = emptyList()
)
