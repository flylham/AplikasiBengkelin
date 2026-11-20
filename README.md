# Bengkelin - Android Native Starter (Malang Raya & Pasuruan)

Aplikasi Android Native untuk pencarian bengkel terdekat di Malang Raya (Kota Malang, Kabupaten Malang, Kota Batu) dan Pasuruan & Sekitarnya.
Arsitektur tanpa Firebase menggunakan REST API, Room SQLite, dan DataStore Preferences.

## Spesifikasi Teknis:
- **Bahasa**: Kotlin 2.0+
- **UI Framework**: Jetpack Compose dengan Material 3 Design
- **Arsitektur**: MVVM (Model-View-ViewModel) + Repository Pattern
- **Networking**: Retrofit 2 + OkHttp + Gson Converter
- **Database Lokal**: AndroidX Room (SQLite ORM) untuk caching offline
- **Manajemen Sesi**: AndroidX DataStore Preferences (JWT Bearer Token)
- **Lokasi GPS Nyata**: Runtime Permission + LocationManager + Geocoder + Kalkulasi Jarak Otomatis
- **Backend**: REST API Server (Node.js/Express) + Skema SQL (PostgreSQL/MySQL)

## Fitur Utama di Paket Ini:
- Autentikasi JWT & Role-Based Dashboard (Admin: `admin@bengkelin.id` / `admin123`, Pengguna Publik)
- Pencarian Bengkel Terdekat Malang Raya & Pasuruan + Navigasi Google Maps Intent & Dialer Telepon
- Sinkronisasi Lokasi GPS Real-time langsung di Beranda & Daftar Bengkel
- SOS Montir Darurat 24 Jam terhubung WhatsApp
- Estimator Biaya Servis & Cek Tanjakan Klemuk/Cangar
- Buku Servis Digital & Paspor Kesehatan Kendaraan

## Cara Menjalankan & Build APK di Android Studio:
1. Buka Android Studio (versi Hedgehog, Iguana, Jellyfish, Ladybug, Koala, atau Meerkat).
2. Pilih **File > Open**, lalu arahkan ke folder hasil ekstrak ini.
3. Lakukan **Gradle Sync** (pastikan Gradle JDK menggunakan Java 17 atau 21).
4. **Jalankan Langsung ke HP / Emulator**:
   - Colokkan HP Android via kabel USB (aktifkan USB Debugging) atau buat Emulator AVD.
   - Tekan tombol **Run (Shift + F10)** segitiga hijau di Android Studio.
5. **Cara Menghasilkan File APK (Bisa Dikirim & Diinstall di HP)**:
   - Di menu atas Android Studio: klik **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
   - Atau lewat Terminal: ketik `.\gradlew assembleDebug` (Windows) atau `./gradlew assembleDebug` (Mac/Linux).
   - Hasil file APK terletak di: `app/build/outputs/apk/debug/app-debug.apk`.

## Menjalankan Backend REST API (Opsional):
Folder `backend/` sudah dilengkapi dengan `package.json`, `server.js`, dan `schema.sql`:
```bash
cd backend
npm install
node server.js
```
(Server akan berjalan di http://localhost:5000)

Selamat mengembangkan Bengkelin!
