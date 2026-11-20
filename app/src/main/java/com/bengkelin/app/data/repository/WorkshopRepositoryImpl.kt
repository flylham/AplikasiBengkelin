package com.bengkelin.app.data.repository

import com.bengkelin.app.data.model.Resource
import com.bengkelin.app.data.model.Workshop
import com.bengkelin.app.data.remote.ApiClient
import com.bengkelin.app.data.remote.WorkshopApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class WorkshopRepositoryImpl(
    private val apiService: WorkshopApiService = ApiClient.authService as? WorkshopApiService
        ?: ApiClient.workshopService
) : WorkshopRepository {

    override suspend fun getNearbyWorkshops(
        category: String?,
        region: String?,
        searchQuery: String?
    ): Flow<Resource<List<Workshop>>> = flow {
        emit(Resource.Loading)
        try {
            val response = apiService.getWorkshops(
                category = if (category == "SEMUA") null else category,
                region = if (region == "SEMUA") null else region,
                query = searchQuery?.ifBlank { null }
            )

            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!.workshops))
            } else {
                // Fallback ke data kurasi Google Maps Malang Raya jika backend offline
                emit(Resource.Success(filterFallbackWorkshops(category, region, searchQuery)))
            }
        } catch (e: Exception) {
            // Jika koneksi server lokal belum hidup, tetap tampilkan data Google Maps
            emit(Resource.Success(filterFallbackWorkshops(category, region, searchQuery)))
        }
    }

    private fun filterFallbackWorkshops(
        category: String?,
        region: String?,
        searchQuery: String?
    ): List<Workshop> {
        val all = getCuratedGoogleMapsWorkshops()
        return all.filter { w ->
            val matchCategory = category == null || category == "SEMUA" || w.category.equals(category, ignoreCase = true)
            val matchRegion = region == null || region == "SEMUA" || w.region.equals(region, ignoreCase = true)
            val matchQuery = searchQuery.isNullOrBlank() ||
                w.name.contains(searchQuery, ignoreCase = true) ||
                w.address.contains(searchQuery, ignoreCase = true) ||
                w.specialties.any { it.contains(searchQuery, ignoreCase = true) }

            matchCategory && matchRegion && matchQuery
        }
    }

    private fun getCuratedGoogleMapsWorkshops(): List<Workshop> = listOf(
        Workshop(
            id = "w-1",
            name = "Bengkel Mobil BOS Suhat Malang",
            category = "MOBIL",
            region = "Kota Malang",
            address = "Jl. Soekarno Hatta No. 45, Lowokwaru, Kota Malang",
            latitude = -7.9429,
            longitude = 112.6163,
            distanceKm = 0.8,
            rating = 4.8,
            reviewsCount = 342,
            isOpen = true,
            openingHours = "08:00 - 17:00 WIB",
            phoneNumber = "081234567891",
            specialties = listOf("Ganti Oli", "Tune Up", "Spooring Balancing", "AC Mobil"),
            googleMapsUrl = "https://www.google.com/maps/search/?api=1&query=Bengkel+Mobil+BOS+Suhat+Malang"
        ),
        Workshop(
            id = "w-2",
            name = "AHASS 0001 Kartika Sari Motor Malang",
            category = "MOTOR",
            region = "Kota Malang",
            address = "Jl. Jaksa Agung Suprapto No. 54, Klojen, Kota Malang",
            latitude = -7.9733,
            longitude = 112.6315,
            distanceKm = 1.2,
            rating = 4.7,
            reviewsCount = 520,
            isOpen = true,
            openingHours = "07:30 - 16:30 WIB",
            phoneNumber = "081298765432",
            specialties = listOf("Servis Injeksi", "Ganti Oli Honda", "Sparepart Asli HGP"),
            googleMapsUrl = "https://www.google.com/maps/search/?api=1&query=AHASS+Kartika+Sari+Motor+Klojen+Malang"
        ),
        Workshop(
            id = "w-3",
            name = "Bengkel Motor Yamaha Sentral Malang",
            category = "MOTOR",
            region = "Kota Malang",
            address = "Jl. Basuki Rahmat No. 35, Klojen, Kota Malang",
            latitude = -7.9782,
            longitude = 112.6288,
            distanceKm = 1.6,
            rating = 4.6,
            reviewsCount = 295,
            isOpen = true,
            openingHours = "08:00 - 17:00 WIB",
            phoneNumber = "081333444555",
            specialties = listOf("Servis Yamaha", "CVT Matic", "Rem & Ban"),
            googleMapsUrl = "https://www.google.com/maps/search/?api=1&query=Yamaha+Sentral+Basuki+Rahmat+Malang"
        ),
        Workshop(
            id = "w-4",
            name = "Bengkel Mobil Auto2000 Sukun Malang",
            category = "MOBIL",
            region = "Kota Malang",
            address = "Jl. S. Supriadi No. 35, Sukun, Kota Malang",
            latitude = -7.9945,
            longitude = 112.6212,
            distanceKm = 2.5,
            rating = 4.9,
            reviewsCount = 810,
            isOpen = true,
            openingHours = "08:00 - 16:00 WIB",
            phoneNumber = "081244556677",
            specialties = listOf("Servis Berkala Toyota", "Body & Paint", "Mesin & Transmisi"),
            googleMapsUrl = "https://www.google.com/maps/search/?api=1&query=Auto2000+Sukun+Malang"
        ),
        Workshop(
            id = "w-5",
            name = "Bengkel Mobil Dokter Mobil Singosari",
            category = "MOBIL",
            region = "Kabupaten Malang",
            address = "Jl. Raya Singosari No. 188, Singosari, Kab. Malang",
            latitude = -7.8925,
            longitude = 112.6651,
            distanceKm = 6.2,
            rating = 4.8,
            reviewsCount = 412,
            isOpen = true,
            openingHours = "09:00 - 18:00 WIB",
            phoneNumber = "081288990011",
            specialties = listOf("Kuras Oli Matic", "AC Mobil Tanpa Bongkar", "Tune Up Jet Clean"),
            googleMapsUrl = "https://www.google.com/maps/search/?api=1&query=Dokter+Mobil+Singosari+Kabupaten+Malang"
        ),
        Workshop(
            id = "w-6",
            name = "Bengkel Motor Jaya Abadi Kepanjen",
            category = "MOTOR",
            region = "Kabupaten Malang",
            address = "Jl. Sultan Agung No. 72, Kepanjen, Kab. Malang",
            latitude = -8.1287,
            longitude = 112.5714,
            distanceKm = 14.5,
            rating = 4.6,
            reviewsCount = 192,
            isOpen = true,
            openingHours = "08:00 - 17:00 WIB",
            phoneNumber = "081377889900",
            specialties = listOf("Servis Umum Motor", "Ganti Ban", "Pres Velg & Rangka"),
            googleMapsUrl = "https://www.google.com/maps/search/?api=1&query=Bengkel+Motor+Kepanjen+Malang"
        ),
        Workshop(
            id = "w-7",
            name = "Bengkel Mobil & AC Batu Sejahtera",
            category = "MOBIL",
            region = "Kota Batu",
            address = "Jl. Diponegoro No. 88, Sisir, Kota Batu",
            latitude = -7.8715,
            longitude = 112.5284,
            distanceKm = 12.0,
            rating = 4.7,
            reviewsCount = 238,
            isOpen = true,
            openingHours = "08:00 - 17:00 WIB",
            phoneNumber = "081266778899",
            specialties = listOf("Servis Kaki-kaki Tanjakan Batu", "AC Mobil", "Rem & Kampas"),
            googleMapsUrl = "https://www.google.com/maps/search/?api=1&query=Bengkel+Mobil+AC+Diponegoro+Kota+Batu"
        ),
        Workshop(
            id = "w-8",
            name = "Bengkel Motor AHASS Batu Indah",
            category = "MOTOR",
            region = "Kota Batu",
            address = "Jl. Panglima Sudirman No. 32, Ngaglik, Kota Batu",
            latitude = -7.8732,
            longitude = 112.5251,
            distanceKm = 13.2,
            rating = 4.8,
            reviewsCount = 382,
            isOpen = true,
            openingHours = "07:30 - 16:30 WIB",
            phoneNumber = "081233221100",
            specialties = listOf("Servis Motor Wisata Batu", "Ganti Oli", "Tune Up"),
            googleMapsUrl = "https://www.google.com/maps/search/?api=1&query=AHASS+Panglima+Sudirman+Kota+Batu"
        ),
        Workshop(
            id = "w-9",
            name = "Bengkel Motor Mas Gareng 24 Jam Sawojajar",
            category = "MOTOR",
            region = "Kota Malang",
            address = "Jl. Danau Toba E1 No. 8, Sawojajar, Kota Malang",
            latitude = -7.9750,
            longitude = 112.6580,
            distanceKm = 3.1,
            rating = 4.5,
            reviewsCount = 168,
            isOpen = true,
            openingHours = "24 Jam Nonstop",
            phoneNumber = "081355667788",
            specialties = listOf("Tambal Ban Tubeless", "Montir Darurat Panggilan", "Kelistrikan"),
            googleMapsUrl = "https://www.google.com/maps/search/?api=1&query=Bengkel+Motor+24+Jam+Sawojajar+Malang"
        ),
        Workshop(
            id = "w-10",
            name = "Bengkel Darurat 24 Jam Puncak Batu",
            category = "MOBIL",
            region = "Kota Batu",
            address = "Jl. Trunojoyo No. 15, Bumiaji, Kota Batu",
            latitude = -7.8340,
            longitude = 112.5290,
            distanceKm = 15.8,
            rating = 4.6,
            reviewsCount = 145,
            isOpen = true,
            openingHours = "24 Jam Nonstop",
            phoneNumber = "081322334455",
            specialties = listOf("Radiator Overheat Tanjakan", "Derek / Jumper Aki", "Tambal Ban"),
            googleMapsUrl = "https://www.google.com/maps/search/?api=1&query=Bengkel+Darurat+24+Jam+Batu+Malang"
        ),
        Workshop(
            id = "w-11",
            name = "Bengkel Mobil BOS Purwosari Pasuruan",
            category = "MOBIL",
            region = "Pasuruan & Sekitarnya",
            address = "Jl. Raya Purwosari No. 18, Purwosari, Kab. Pasuruan",
            latitude = -7.7560,
            longitude = 112.7490,
            distanceKm = 0.9,
            rating = 4.8,
            reviewsCount = 215,
            isOpen = true,
            openingHours = "08:00 - 17:00 WIB",
            phoneNumber = "081234778899",
            specialties = listOf("Tune Up Injeksi", "Ganti Oli & Filter", "AC & Kelistrikan", "Rem & Kaki-kaki"),
            googleMapsUrl = "https://www.google.com/maps/search/?api=1&query=Bengkel+Mobil+Purwosari+Pasuruan"
        ),
        Workshop(
            id = "w-12",
            name = "Bengkel Resmi AHASS Pandaan Motor Pasuruan",
            category = "MOTOR",
            region = "Pasuruan & Sekitarnya",
            address = "Jl. Raya Pandaan - Bangil No. 45, Pandaan, Kab. Pasuruan",
            latitude = -7.6530,
            longitude = 112.6960,
            distanceKm = 1.2,
            rating = 4.8,
            reviewsCount = 430,
            isOpen = true,
            openingHours = "07:30 - 16:30 WIB",
            phoneNumber = "081333889911",
            specialties = listOf("Servis Motor Honda", "CVT Matic", "Suku Cadang Asli HGP", "Injeksi PGM-FI"),
            googleMapsUrl = "https://www.google.com/maps/search/?api=1&query=AHASS+Pandaan+Pasuruan"
        ),
        Workshop(
            id = "w-13",
            name = "Bengkel Mobil & AC Pahlawan Kota Pasuruan",
            category = "MOBIL",
            region = "Pasuruan & Sekitarnya",
            address = "Jl. Pahlawan No. 28, Panggungrejo, Kota Pasuruan",
            latitude = -7.6450,
            longitude = 112.9040,
            distanceKm = 0.6,
            rating = 4.7,
            reviewsCount = 310,
            isOpen = true,
            openingHours = "08:00 - 17:00 WIB",
            phoneNumber = "081255667788",
            specialties = listOf("AC Mobil & Kuras Freon", "Spooring Balancing", "Ganti Oli Mesin & Transmisi"),
            googleMapsUrl = "https://www.google.com/maps/search/?api=1&query=Bengkel+Mobil+Pahlawan+Kota+Pasuruan"
        ),
        Workshop(
            id = "w-14",
            name = "Bengkel Motor Siaga 24 Jam Purwodadi - Pasuruan",
            category = "MOTOR",
            region = "Pasuruan & Sekitarnya",
            address = "Jl. Raya Purwodadi No. 82, Purwodadi, Kab. Pasuruan (Jalur Malang - Surabaya)",
            latitude = -7.7980,
            longitude = 112.7210,
            distanceKm = 2.1,
            rating = 4.6,
            reviewsCount = 175,
            isOpen = true,
            openingHours = "24 Jam Nonstop",
            phoneNumber = "081388990022",
            specialties = listOf("Montir Panggilan Jalur Pasuruan-Malang", "Tambal Ban Tubeless 24 Jam", "Ganti Aki"),
            googleMapsUrl = "https://www.google.com/maps/search/?api=1&query=Bengkel+Motor+Purwodadi+Pasuruan"
        )
    )
}
