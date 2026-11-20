package com.bengkelin.app.ui.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.bengkelin.app.data.model.User
import com.bengkelin.app.data.model.UserRole
import com.bengkelin.app.ui.theme.BengkelinNavy
import com.bengkelin.app.ui.theme.BengkelinOrange
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

private data class HomeLocationPreset(
    val title: String,
    val subtitle: String,
    val lat: Double,
    val lng: Double
)

private val HOME_LOCATION_PRESETS = listOf(
    HomeLocationPreset("Jl. Soekarno Hatta (Suhat)", "Lowokwaru, Kota Malang", -7.9450, 112.6160),
    HomeLocationPreset("Alun-Alun Tugu Kota Malang", "Klojen, Kota Malang", -7.9785, 112.6318),
    HomeLocationPreset("Alun-Alun Kota Batu", "Sisir, Kota Batu", -7.8712, 112.5272),
    HomeLocationPreset("Alun-Alun Kota Pasuruan", "Pusat Kota Pasuruan", -7.6448, 112.9033),
    HomeLocationPreset("Pandaan, Kab. Pasuruan", "Jalur Malang - Surabaya", -7.6521, 112.6953),
    HomeLocationPreset("Purwosari, Kab. Pasuruan", "Purwosari, Pasuruan", -7.7554, 112.7483)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoleHomeScreen(
    user: User?,
    onNavigateToWorkshopSearch: () -> Unit = {},
    onNavigateToEstimator: () -> Unit = {},
    onNavigateToPassport: () -> Unit = {},
    onLogout: () -> Unit
) {
    val role = user?.userRole ?: UserRole.PENGGUNA
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val locationManager = remember {
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }
    var isLocationGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }
    var syncedLocationText by remember { mutableStateOf("Nyalakan Lokasi (Sinkron GPS)") }
    var isEmulatorDefaultLocation by remember { mutableStateOf(false) }
    var useManualPreset by remember { mutableStateOf(false) }
    var showLocationDialog by remember { mutableStateOf(false) }

    fun syncLocationAddress(location: Location) {
        if (useManualPreset) return
        val inEastJava = location.latitude in -8.8..-6.5 && location.longitude in 111.0..114.6
        isEmulatorDefaultLocation = !inEastJava
        coroutineScope.launch(Dispatchers.IO) {
            val label = try {
                @Suppress("DEPRECATION")
                val list = Geocoder(context, Locale("id", "ID"))
                    .getFromLocation(location.latitude, location.longitude, 1)
                val addr = list?.firstOrNull()
                listOfNotNull(addr?.subLocality, addr?.locality ?: addr?.subAdminArea)
                    .distinct()
                    .joinToString(", ")
                    .ifBlank {
                        String.format(Locale.US, "GPS (%.4f, %.4f)", location.latitude, location.longitude)
                    }
            } catch (_: Exception) {
                String.format(Locale.US, "GPS (%.4f, %.4f)", location.latitude, location.longitude)
            }
            withContext(Dispatchers.Main) {
                if (!useManualPreset) {
                    syncedLocationText = if (!inEastJava) "$label (GPS Emulator)" else label
                }
            }
        }
    }

    val homeLocationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val granted = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        isLocationGranted = granted
        if (granted) useManualPreset = false
    }

    @SuppressLint("MissingPermission")
    DisposableEffect(isLocationGranted, useManualPreset) {
        if (!isLocationGranted || useManualPreset) {
            onDispose { }
        } else {
            val lastGps = runCatching { locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) }.getOrNull()
            val lastNet = runCatching { locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) }.getOrNull()
            val best = lastGps ?: lastNet
            if (best != null) syncLocationAddress(best)

            val listener = LocationListener { loc -> syncLocationAddress(loc) }
            runCatching {
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000L, 20f, listener, Looper.getMainLooper())
                }
                if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000L, 20f, listener, Looper.getMainLooper())
                }
            }
            onDispose { runCatching { locationManager.removeUpdates(listener) } }
        }
    }

    var showSosDialog by remember { mutableStateOf(false) }
    var selectedVehicle by remember { mutableStateOf("Motor") }
    var selectedIssue by remember { mutableStateOf("Ban Bocor / Kempes") }
    var locationNotes by remember { mutableStateOf("") }

    val emergencyPhone = "+6285606401220"
    val rawWaNumber = "6285606401220"
    val firstName = user?.name?.trim()?.split(" ")?.firstOrNull().orEmpty().ifBlank { "Pengguna" }
    val avatarChar = firstName.firstOrNull()?.uppercase() ?: "B"

    Scaffold(
        containerColor = Color(0xFFF8FAFC),
        topBar = {
            Surface(
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFFEA580C), Color(0xFFF59E0B))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = avatarChar,
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Halo, $firstName 👋",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color(0xFF0F172A),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable {
                                        if (!isLocationGranted && !useManualPreset) {
                                            homeLocationLauncher.launch(
                                                arrayOf(
                                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                                )
                                            )
                                        } else {
                                            showLocationDialog = true
                                        }
                                    }
                                    .padding(vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = if (isEmulatorDefaultLocation) Color(0xFFD97706) else BengkelinOrange,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = if (isLocationGranted || useManualPreset) syncedLocationText else "Nyalakan Lokasi (Sinkron GPS)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isEmulatorDefaultLocation) Color(0xFFB45309) else Color(0xFF475569),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = onLogout,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFF1F5F9))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Keluar",
                            tint = Color(0xFF475569),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Alert Lokasi jika GPS belum aktif atau Emulator masih di Mountain View
            if (!isLocationGranted && !useManualPreset) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                    border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.GpsFixed, contentDescription = null, tint = BengkelinOrange, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Lokasi GPS Belum Aktif", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF92400E))
                            Text("Nyalakan agar posisi Anda tersinkron otomatis.", fontSize = 11.sp, color = Color(0xFFB45309))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                homeLocationLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BengkelinOrange),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("Nyalakan", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else if (isEmulatorDefaultLocation && !useManualPreset) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showLocationDialog = true },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7ED)),
                    border = BorderStroke(1.dp, Color(0xFFFED7AA)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Explore, contentDescription = null, tint = BengkelinOrange, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("GPS Emulator di Mountain View", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF9A3412))
                            Text("Ketuk di sini untuk memilih titik Malang Raya / Pasuruan.", fontSize = 11.sp, color = Color(0xFFC2410C))
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = BengkelinOrange)
                    }
                }
            }

            // 2. High-Alert SOS Emergency Banner (serupa Simulator)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFFDC2626), Color(0xFFEA580C))
                        )
                    )
                    .clickable { showSosDialog = true }
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Emergency, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "BUTUH MONTIR DARURAT 24 JAM?",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFFEE2E2)
                            )
                            Text(
                                text = "Panggil SOS via WhatsApp",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Siaga Malang Raya & Pasuruan • $emergencyPhone",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
                    Surface(
                        color = Color.White,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "SOS",
                            color = Color(0xFFDC2626),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // 3. Kartu Ringkasan Akun & Peran
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = BengkelinNavy),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = user?.name ?: "Pengguna Bengkelin",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Surface(
                            color = BengkelinOrange.copy(alpha = 0.22f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = role.displayName,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFDBA74),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${user?.email ?: "-"} • Telp: ${user?.phoneNumber ?: "-"}",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    if (role == UserRole.PEMILIK_BENGKEL && !user?.workshopName.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Bengkel: ${user?.workshopName}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = BengkelinOrange
                        )
                    }
                }
            }

            // 4. Menu Utama 2x2 Grid (Menyerupai Simulator HP)
            Text(
                text = "Layanan Utama Bengkelin",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickMenuCard(
                    title = "Bengkel Terdekat",
                    subtitle = "Maps & Filter",
                    icon = Icons.Default.Search,
                    accentColor = Color(0xFFEA580C),
                    bgColor = Color(0xFFFFF7ED),
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToWorkshopSearch
                )
                QuickMenuCard(
                    title = "SOS Darurat 24J",
                    subtitle = "Jemput Lokasi",
                    icon = Icons.Default.Emergency,
                    accentColor = Color(0xFFDC2626),
                    bgColor = Color(0xFFFEF2F2),
                    modifier = Modifier.weight(1f),
                    onClick = { showSosDialog = true }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickMenuCard(
                    title = "Estimator & Rem",
                    subtitle = "Cek Tanjakan Batu",
                    icon = Icons.Default.Calculate,
                    accentColor = Color(0xFF0284C7),
                    bgColor = Color(0xFFF0F9FF),
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToEstimator
                )
                QuickMenuCard(
                    title = "Paspor Servis",
                    subtitle = "Health & Garansi",
                    icon = Icons.Default.VerifiedUser,
                    accentColor = Color(0xFF059669),
                    bgColor = Color(0xFFECFDF5),
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToPassport
                )
            }

            // 5. Kartu Khusus Role Admin / Pemilik Bengkel jika login sebagai pengelola
            if (role == UserRole.ADMIN) {
                FeatureCard(
                    title = "Panel Administrator Bengkelin",
                    desc = "Kelola verifikasi bengkel & pengguna aktif di Malang Raya & Pasuruan.",
                    icon = Icons.Default.AdminPanelSettings,
                    onClick = onNavigateToWorkshopSearch
                )
            } else if (role == UserRole.PEMILIK_BENGKEL) {
                FeatureCard(
                    title = "Status Verifikasi Bengkel",
                    desc = if (user?.isVerified == true) "Bengkel aktif dan tampil di pencarian." else "Sedang dalam proses verifikasi oleh Admin.",
                    icon = Icons.Default.Storefront,
                    onClick = onNavigateToWorkshopSearch
                )
            }

            // 6. Tips Perjalanan Lokal
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Shield, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Siaga Jalur Malang – Batu – Pasuruan",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "Selalu cek kampas rem & oli sebelum melintasi tanjakan Klemuk, Cangar, atau jalur Purwodadi–Pandaan.",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }
        }
    }

    // DIALOG UBAH TITIK LOKASI (Berguna jika Emulator GPS di Mountain View)
    if (showLocationDialog) {
        AlertDialog(
            onDismissRequest = { showLocationDialog = false },
            title = {
                Text("Pilih Titik Lokasi Aktif", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            useManualPreset = false
                            isEmulatorDefaultLocation = false
                            syncedLocationText = "Menyinkronkan GPS..."
                            showLocationDialog = false
                            homeLocationLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BengkelinOrange),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.GpsFixed, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Sinkron Ulang GPS Perangkat", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Text(
                        text = "Atau Pilih Titik Malang Raya & Pasuruan:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF64748B)
                    )

                    HOME_LOCATION_PRESETS.forEach { preset ->
                        OutlinedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    useManualPreset = true
                                    isEmulatorDefaultLocation = false
                                    isLocationGranted = true
                                    syncedLocationText = preset.title
                                    showLocationDialog = false
                                },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(preset.title, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text(preset.subtitle, fontSize = 10.sp, color = Color(0xFF64748B))
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLocationDialog = false }) {
                    Text("Tutup")
                }
            }
        )
    }

    // DIALOG SOS 24 JAM TERHUBUNG KE WHATSAPP +6285606401220
    if (showSosDialog) {
        val problems = listOf(
            "Ban Bocor / Kempes",
            "Aki Mati / Jumper",
            "Mesin Mogok / Overheat",
            "Kehabisan Bensin",
            "Derek / Towing Darurat"
        )

        AlertDialog(
            onDismissRequest = { showSosDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Emergency,
                    contentDescription = null,
                    tint = Color(0xFFDC2626),
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Layanan Montir Darurat (SOS 24 Jam)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Malang Raya & Pasuruan • Respon Cepat WhatsApp",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFDCFCE7)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Phone, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(18.dp))
                            Column {
                                Text(text = "WhatsApp Montir Siaga 24 Jam:", fontSize = 11.sp, color = Color(0xFF166534))
                                Text(text = emergencyPhone, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF15803D))
                            }
                        }
                    }

                    Text(text = "Pilih Kendaraan:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedVehicle == "Motor",
                            onClick = { selectedVehicle = "Motor" },
                            label = { Text("🛵 Motor") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = selectedVehicle == "Mobil",
                            onClick = { selectedVehicle = "Mobil" },
                            label = { Text("🚗 Mobil") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Text(text = "Kendala Darurat:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        problems.forEach { problem ->
                            FilterChip(
                                selected = selectedIssue == problem,
                                onClick = { selectedIssue = problem },
                                label = { Text(problem, fontSize = 11.sp) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    OutlinedTextField(
                        value = locationNotes,
                        onValueChange = { locationNotes = it },
                        label = { Text("Patokan Lokasi", fontSize = 12.sp) },
                        placeholder = { Text("Contoh: Dekat SPBU Suhat / Pandaan", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = false,
                        maxLines = 2
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val message = "🚨 *DARURAT SOS 24 JAM BENGKELIN*\n\n" +
                                "👤 *Nama:* ${user?.name ?: "Pengguna Bengkelin"}\n" +
                                "📞 *Nomor HP:* ${user?.phoneNumber ?: "-"}\n" +
                                "🚗 *Kendaraan:* $selectedVehicle\n" +
                                "⚠️ *Kendala:* $selectedIssue\n" +
                                "📍 *Lokasi GPS:* $syncedLocationText\n" +
                                "📝 *Patokan:* ${if (locationNotes.isNotBlank()) locationNotes else "Sekitar ${user?.region ?: "Malang / Pasuruan"}"}\n\n" +
                                "Mohon segera kirimkan montir darurat ke lokasi saya. Terima kasih."

                        val encodedMessage = Uri.encode(message)
                        val waUrl = "https://api.whatsapp.com/send?phone=$rawWaNumber&text=$encodedMessage"

                        val waIntent = Intent(Intent.ACTION_VIEW, Uri.parse(waUrl)).apply {
                            setPackage("com.whatsapp")
                        }

                        try {
                            context.startActivity(waIntent)
                        } catch (e: Exception) {
                            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(waUrl))
                            context.startActivity(browserIntent)
                        }

                        showSosDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.Chat, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Kirim SOS ke WhatsApp (+6285606401220)",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showSosDialog = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Batal", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }
}

@Composable
private fun QuickMenuCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    bgColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(bgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = Color(0xFF0F172A),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = Color(0xFF64748B),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun FeatureCard(
    title: String,
    desc: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(BengkelinOrange.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = BengkelinOrange,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF0F172A))
                Text(text = desc, fontSize = 11.sp, color = Color(0xFF64748B))
            }
        }
    }
}
