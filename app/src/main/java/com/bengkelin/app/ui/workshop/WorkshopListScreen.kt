package com.bengkelin.app.ui.workshop

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
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bengkelin.app.data.model.Workshop
import com.bengkelin.app.ui.theme.BengkelinNavy
import com.bengkelin.app.ui.theme.BengkelinOrange
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkshopListScreen(
    onNavigateBack: () -> Unit,
    viewModel: WorkshopViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val locationManager = remember {
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    var isLocationPermissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }
    var userLat by remember { mutableStateOf<Double?>(null) }
    var userLng by remember { mutableStateOf<Double?>(null) }
    var userLocationName by remember { mutableStateOf("Menyinkronkan lokasi GPS...") }
    var useManualLocation by remember { mutableStateOf(false) }
    var showPointDialog by remember { mutableStateOf(false) }

    fun updateAddressFromLocation(location: Location) {
        if (useManualLocation) return
        userLat = location.latitude
        userLng = location.longitude
        val inEastJava = location.latitude in -8.8..-6.5 && location.longitude in 111.0..114.6
        coroutineScope.launch(Dispatchers.IO) {
            val resolved = try {
                @Suppress("DEPRECATION")
                val addresses = Geocoder(context, Locale("id", "ID"))
                    .getFromLocation(location.latitude, location.longitude, 1)
                val addr = addresses?.firstOrNull()
                listOfNotNull(addr?.subLocality, addr?.locality ?: addr?.subAdminArea)
                    .distinct()
                    .joinToString(", ")
                    .ifBlank {
                        String.format(Locale.US, "GPS Nyata (%.4f, %.4f)", location.latitude, location.longitude)
                    }
            } catch (_: Exception) {
                String.format(Locale.US, "GPS Nyata (%.4f, %.4f)", location.latitude, location.longitude)
            }
            withContext(Dispatchers.Main) {
                if (!useManualLocation) {
                    userLocationName = if (!inEastJava) "$resolved (GPS Emulator)" else resolved
                }
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        isLocationPermissionGranted = granted
        if (granted) useManualLocation = false
        if (!granted) {
            userLocationName = "Izin lokasi ditolak"
        }
    }

    // Sinkronisasi otomatis lokasi GPS perangkat secara real-time saat izin aktif
    @SuppressLint("MissingPermission")
    DisposableEffect(isLocationPermissionGranted, useManualLocation) {
        if (!isLocationPermissionGranted || useManualLocation) {
            onDispose { }
        } else {
            val lastGps = runCatching { locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) }.getOrNull()
            val lastNet = runCatching { locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) }.getOrNull()
            val bestLast = when {
                lastGps != null && lastNet != null -> if (lastGps.time >= lastNet.time) lastGps else lastNet
                else -> lastGps ?: lastNet
            }
            if (bestLast != null) {
                updateAddressFromLocation(bestLast)
            }

            val listener = LocationListener { loc -> updateAddressFromLocation(loc) }
            runCatching {
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 4000L, 15f, listener, Looper.getMainLooper())
                }
                if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 4000L, 15f, listener, Looper.getMainLooper())
                }
            }

            onDispose {
                runCatching { locationManager.removeUpdates(listener) }
            }
        }
    }

    // Hitung ulang jarak dan urutkan bengkel otomatis dari koordinat GPS asli pengguna
    val sortedWorkshops = remember(uiState.workshops, userLat, userLng) {
        val lat = userLat
        val lng = userLng
        if (lat == null || lng == null) {
            uiState.workshops
        } else {
            uiState.workshops.map { w ->
                val results = FloatArray(1)
                Location.distanceBetween(lat, lng, w.latitude, w.longitude, results)
                val km = ((results[0] / 1000.0) * 10.0).roundToInt() / 10.0
                w.copy(distanceKm = km)
            }.sortedBy { it.distanceKm }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Bengkel Terdekat", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(
                            if (isLocationPermissionGranted) "GPS Tersinkron • Terurut Terdekat" else "Nyalakan Lokasi untuk Sinkron Otomatis",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    if (isLocationPermissionGranted) {
                        IconButton(onClick = { isLocationPermissionGranted = false }) {
                            Icon(imageVector = Icons.Default.LocationOff, contentDescription = "Matikan Lokasi", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (!isLocationPermissionGranted) {
                // LOCATION PERMISSION GATE (Wajib Aktifkan Lokasi)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(40.dp))
                            .background(BengkelinOrange.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = null,
                            tint = BengkelinOrange,
                            modifier = Modifier.size(44.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Nyalakan & Sinkronkan Lokasi GPS",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Untuk mendeteksi lokasi asli Anda (Malang Raya, Batu, maupun Pasuruan) secara otomatis dan mengurutkan bengkel terdekat, izinkan akses GPS perangkat.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            permissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BengkelinOrange)
                    ) {
                        Icon(imageVector = Icons.Default.GpsFixed, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Nyalakan Lokasi & Sinkron Sekarang", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            } else {
                // STATUS GPS AKTIF BAR
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFDCFCE7)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text("Lokasi GPS Tersinkron:", fontSize = 10.sp, color = Color(0xFF166534), fontWeight = FontWeight.Bold)
                                Text(userLocationName, fontSize = 11.sp, color = Color(0xFF15803D), fontWeight = FontWeight.SemiBold, maxLines = 1)
                            }
                        }
                        Row {
                            TextButton(onClick = { showPointDialog = true }) {
                                Text("Ubah Titik", fontSize = 11.sp, color = BengkelinOrange, fontWeight = FontWeight.Bold)
                            }
                            TextButton(onClick = {
                                useManualLocation = false
                                isLocationPermissionGranted = false
                            }) {
                                Text("Matikan", fontSize = 11.sp, color = Color(0xFFDC2626), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Search Input
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.onEvent(WorkshopUiEvent.SearchQueryChanged(it)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    placeholder = { Text("Cari bengkel, jalan, atau nama montir...") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Outlined.Search, contentDescription = "Cari")
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp)
                )

                // Filter Jenis Kendaraan: Motor / Mobil / Semua
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "SEMUA" to "Semua",
                        "MOTOR" to "Bengkel Motor",
                        "MOBIL" to "Bengkel Mobil"
                    ).forEach { (catKey, label) ->
                        val isSelected = uiState.selectedCategory == catKey
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.onEvent(WorkshopUiEvent.CategoryChanged(catKey)) },
                            label = { Text(label, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            leadingIcon = {
                                when (catKey) {
                                    "MOTOR" -> Icon(imageVector = Icons.Default.TwoWheeler, contentDescription = null, modifier = Modifier.size(16.dp))
                                    "MOBIL" -> Icon(imageVector = Icons.Default.DirectionsCar, contentDescription = null, modifier = Modifier.size(16.dp))
                                    else -> null
                                }
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BengkelinOrange,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                // Filter Wilayah: Malang Raya & Pasuruan
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val regions = listOf("SEMUA", "Kota Malang", "Kabupaten Malang", "Kota Batu", "Pasuruan & Sekitarnya")
                    items(regions) { region ->
                        val isSelected = uiState.selectedRegion == region
                        SuggestionChip(
                            onClick = { viewModel.onEvent(WorkshopUiEvent.RegionChanged(region)) },
                            label = { Text(if (region == "SEMUA") "Semua Wilayah" else region, fontSize = 11.sp) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = if (isSelected) BengkelinNavy else MaterialTheme.colorScheme.surfaceVariant,
                                labelColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

            // Daftar Bengkel
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = BengkelinOrange)
                }
            } else if (sortedWorkshops.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = Icons.Default.SearchOff, contentDescription = null, modifier = Modifier.size(56.dp), tint = MaterialTheme.colorScheme.outline)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Tidak ada bengkel yang sesuai filter.", fontWeight = FontWeight.SemiBold)
                        Text("Coba ubah kata kunci atau ganti filter wilayah.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(sortedWorkshops) { workshop ->
                        WorkshopCard(
                            workshop = workshop,
                            onOpenGoogleMaps = {
                                val gmmIntentUri = Uri.parse("geo:${workshop.latitude},${workshop.longitude}?q=${Uri.encode(workshop.name)}")
                                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
                                    setPackage("com.google.android.apps.maps")
                                }
                                if (mapIntent.resolveActivity(context.packageManager) != null) {
                                    context.startActivity(mapIntent)
                                } else {
                                    // Buka via browser jika aplikasi Google Maps tidak tersedia
                                    val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(workshop.googleMapsUrl))
                                    context.startActivity(webIntent)
                                }
                            },
                            onCallWorkshop = {
                                val callIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${workshop.phoneNumber}"))
                                context.startActivity(callIntent)
                            }
                        )
                    }
                }
            }
        }
    }

    if (showPointDialog) {
        val presets = listOf(
            Triple("Jl. Soekarno Hatta (Suhat) Malang", -7.9450, 112.6160),
            Triple("Alun-Alun Tugu Kota Malang", -7.9785, 112.6318),
            Triple("Alun-Alun Kota Batu", -7.8712, 112.5272),
            Triple("Alun-Alun Kota Pasuruan", -7.6448, 112.9033),
            Triple("Pandaan, Kab. Pasuruan", -7.6521, 112.6953)
        )
        AlertDialog(
            onDismissRequest = { showPointDialog = false },
            title = { Text("Ubah Titik Lokasi", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            useManualLocation = false
                            userLocationName = "Menyinkronkan lokasi GPS..."
                            showPointDialog = false
                            permissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BengkelinOrange),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Deteksi Ulang GPS Perangkat", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    presets.forEach { (label, lat, lng) ->
                        OutlinedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    useManualLocation = true
                                    userLat = lat
                                    userLng = lng
                                    userLocationName = label
                                    isLocationPermissionGranted = true
                                    showPointDialog = false
                                }
                        ) {
                            Text(label, modifier = Modifier.padding(10.dp), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPointDialog = false }) { Text("Tutup") }
            }
        )
    }
}
}

@Composable
fun WorkshopCard(
    workshop: Workshop,
    onOpenGoogleMaps: () -> Unit,
    onCallWorkshop: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (workshop.category == "MOTOR") Color(0xFFE0F2FE) else Color(0xFFFEF3C7)
                        ) {
                            Text(
                                text = if (workshop.category == "MOTOR") "BENGKEL MOTOR" else "BENGKEL MOBIL",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (workshop.category == "MOTOR") Color(0xFF0369A1) else Color(0xFFB45309),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "• ${workshop.region}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = workshop.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = workshop.address,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }

                // Jarak & Rating
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${workshop.distanceKm} km",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = BengkelinOrange
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(text = "${workshop.rating}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Text(text = " (${workshop.reviewsCount})", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Layanan & Spesialisasi
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                workshop.specialties.take(3).forEach { tag ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = tag,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tombol Navigasi Google Maps & Telepon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onCallWorkshop,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Hubungi", fontSize = 12.sp)
                }

                Button(
                    onClick = onOpenGoogleMaps,
                    modifier = Modifier.weight(1.3f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BengkelinOrange)
                ) {
                    Icon(imageVector = Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Buka di Google Maps", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
