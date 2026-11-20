package com.bengkelin.app.data.model

/**
 * Cakupan wilayah operasional Bengkelin: Malang Raya & Pasuruan
 */
enum class MalangRegion(val regionName: String, val centerLatitude: Double, val centerLongitude: Double) {
    KOTA_MALANG("Kota Malang", -7.983908, 112.621391),
    KABUPATEN_MALANG("Kabupaten Malang", -8.133333, 112.566667),
    KOTA_BATU("Kota Batu", -7.871158, 112.527374),
    PASURUAN("Pasuruan & Sekitarnya", -7.645300, 112.907500);

    companion object {
        fun fromString(value: String?): MalangRegion {
            return entries.find { it.regionName.equals(value, ignoreCase = true) } ?: KOTA_MALANG
        }
    }
}
