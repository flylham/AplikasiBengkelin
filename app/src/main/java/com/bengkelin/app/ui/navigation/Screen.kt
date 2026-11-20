package com.bengkelin.app.ui.navigation

sealed class Screen(val route: String) {
    data object Splash : Screen("splash_screen")
    data object Login : Screen("login_screen")
    data object Register : Screen("register_screen")
    
    // Halaman Pencarian Bengkel Terdekat Malang Raya (Google Maps)
    data object WorkshopList : Screen("workshop_list_screen")

    // Fitur Unik: Estimator Biaya & Cek Tanjakan Ekstrem Batu/Cangar
    data object CostEstimator : Screen("cost_estimator_screen")

    // Fitur Unik: Paspor Kesehatan Kendaraan & Buku Servis Digital
    data object VehiclePassport : Screen("vehicle_passport_screen")

    // Halaman Dashboard berdasarkan peran
    data object HomePengguna : Screen("home_pengguna_screen")
    data object HomePemilikBengkel : Screen("home_pemilik_bengkel_screen")
    data object HomeAdmin : Screen("home_admin_screen")
}
