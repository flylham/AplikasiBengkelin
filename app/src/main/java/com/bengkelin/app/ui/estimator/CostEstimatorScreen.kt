package com.bengkelin.app.ui.estimator

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bengkelin.app.ui.theme.BengkelinNavy
import com.bengkelin.app.ui.theme.BengkelinOrange

data class ServiceItem(
    val id: String,
    val name: String,
    val price: Int,
    val estimatedMinutes: Int
)

data class CustomServiceItem(
    val id: String,
    val query: String,
    val name: String,
    val price: Int,
    val estimatedMinutes: Int
)

fun formatRupiah(amount: Int): String {
    val s = amount.toString()
    val sb = StringBuilder()
    for (i in s.indices) {
        if (i > 0 && (s.length - i) % 3 == 0) {
            sb.append('.')
        }
        sb.append(s[i])
    }
    return "Rp " + sb.toString()
}

fun detectProblemPrice(input: String, isMotor: Boolean): Pair<String, Int>? {
    val q = input.trim().lowercase()
    if (q.isBlank()) return null
    if (q.contains("ban") || q.contains("bocor") || q.contains("tubeless") || q.contains("tambal") || q.contains("kempes")) {
        return if (isMotor) Pair("Tambal Ban Tubeless / Bocor", 25000) else Pair("Tambal Ban Tubeless Mobil", 50000)
    }
    if (q.contains("rem") || q.contains("kampas") || q.contains("decit") || q.contains("blong")) {
        return if (isMotor) Pair("Ganti Kampas Rem & Minyak Rem", 40000) else Pair("Ganti Kampas Rem Depan", 220000)
    }
    if (q.contains("oli") || q.contains("pelumas")) {
        return if (isMotor) Pair("Ganti Oli Mesin + Oli Gardan", 55000) else Pair("Ganti Oli Mesin + Filter", 180000)
    }
    if (q.contains("aki") || q.contains("starter") || q.contains("tekor") || q.contains("jumper") || q.contains("soak")) {
        return if (isMotor) Pair("Cek & Jumper Aki Motor", 35000) else Pair("Jumper Aki Mobil & Cek Dinamo", 80000)
    }
    if (q.contains("brebet") || q.contains("injeksi") || q.contains("busi") || q.contains("karbu") || q.contains("mogok")) {
        return if (isMotor) Pair("Tune Up Injeksi & Ganti Busi", 45000) else Pair("Tune Up Carbon Clean Mesin", 250000)
    }
    if (q.contains("ac") || q.contains("freon") || q.contains("dingin") || q.contains("panas")) {
        return if (isMotor) Pair("Cek Sensor Suhu / Kipas", 35000) else Pair("Servis AC Dingin & Tambah Freon", 200000)
    }
    if (q.contains("radiator") || q.contains("coolant") || q.contains("overheat") || q.contains("mendidih")) {
        return if (isMotor) Pair("Kuras Air Radiator Coolant", 35000) else Pair("Kuras Radiator Coolant Mobil", 80000)
    }
    if (q.contains("cvt") || q.contains("roller") || q.contains("rantai") || q.contains("v-belt")) {
        return if (isMotor) Pair("Servis CVT & Roller Matic", 60000) else Pair("Servis Transmisi & Kopling", 250000)
    }
    if (q.contains("shock") || q.contains("suspensi") || q.contains("jedug") || q.contains("kaki")) {
        return if (isMotor) Pair("Servis Seal Shock Depan", 75000) else Pair("Servis Kaki-kaki Mobil", 280000)
    }
    if (q.contains("lampu") || q.contains("bohlam") || q.contains("kabel") || q.contains("sekring")) {
        return if (isMotor) Pair("Ganti Bohlam Lampu & Cek Soket", 25000) else Pair("Ganti Bohlam & Cek Sekring", 65000)
    }
    if (q.contains("derek") || q.contains("towing")) {
        return if (isMotor) Pair("Montir Jemput Lokasi Darurat", 50000) else Pair("Mobil Derek Towing Malang", 250000)
    }
    if (q.length >= 3) {
        val label = "Inspeksi & Servis: " + input.trim()
        return if (isMotor) Pair(label, 40000) else Pair(label, 130000)
    }
    return null
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CostEstimatorScreen(
    onNavigateBack: () -> Unit,
    onNavigateToWorkshops: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedVehicle by remember { mutableStateOf("MOTOR") }

    val motorServices = listOf(
        ServiceItem("m1", "Ganti Oli Mesin", 55000, 15),
        ServiceItem("m2", "Tune Up & Servis Injeksi", 45000, 25),
        ServiceItem("m3", "Ganti Kampas Rem", 40000, 20),
        ServiceItem("m4", "Servis CVT & Roller Matic", 60000, 35),
        ServiceItem("m5", "Kuras Radiator Coolant", 35000, 15),
        ServiceItem("m6", "Tambal Ban Tubeless", 25000, 10)
    )

    val mobilServices = listOf(
        ServiceItem("c1", "Ganti Oli Mesin + Filter", 180000, 30),
        ServiceItem("c2", "Tune Up & Carbon Clean", 250000, 45),
        ServiceItem("c3", "Ganti Kampas Rem Depan", 220000, 30),
        ServiceItem("c4", "Servis AC Dingin & Freon", 200000, 40),
        ServiceItem("c5", "Kuras Radiator Coolant", 80000, 20),
        ServiceItem("c6", "Spooring & Balancing 4 Roda", 150000, 30)
    )

    var selectedServiceIds by remember {
        mutableStateOf(setOf("m1", "m2"))
    }

    // Input teks masalah kendala & deteksi otomatis real-time
    var customProblemInput by remember { mutableStateOf("") }
    var customAddedServices by remember { mutableStateOf(listOf<CustomServiceItem>()) }
    val liveDetection = remember(customProblemInput, selectedVehicle) {
        detectProblemPrice(customProblemInput, selectedVehicle == "MOTOR")
    }

    var brakeOk by remember { mutableStateOf(true) }
    var radiatorOk by remember { mutableStateOf(true) }
    var tireOk by remember { mutableStateOf(true) }
    var heavyLoad by remember { mutableStateOf(false) }

    val currentServices = if (selectedVehicle == "MOTOR") motorServices else mobilServices
    val checklistCost = currentServices.filter { it.id in selectedServiceIds }.sumOf { it.price }
    val customCost = customAddedServices.sumOf { it.price }
    val totalCost = checklistCost + customCost
    val totalTime = currentServices.filter { it.id in selectedServiceIds }.sumOf { it.estimatedMinutes } + customAddedServices.sumOf { it.estimatedMinutes }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(text = "Estimator & Cek Tanjakan", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        Text(text = "Malang Raya • Biaya Riil & Keamanan Jalur", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Biaya", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Calculate, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Tanjakan Batu", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Speed, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Tanya AI Montir", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.AutoAwesome, contentDescription = null) }
                )
            }

            if (selectedTab == 0) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedVehicle == "MOTOR",
                            onClick = {
                                selectedVehicle = "MOTOR"
                                selectedServiceIds = setOf("m1", "m2")
                            },
                            label = { Text("🛵 Servis Motor") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = selectedVehicle == "MOBIL",
                            onClick = {
                                selectedVehicle = "MOBIL"
                                selectedServiceIds = setOf("c1", "c2")
                            },
                            label = { Text("🚗 Servis Mobil") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // SEKSI: KETIK KELUHAN DENGAN DETEKSI OTOMATIS REAL-TIME
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = BengkelinOrange, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = "Ketik Keluhan / Layanan:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                                Text(
                                    text = "Deteksi Otomatis",
                                    fontSize = 10.sp,
                                    color = BengkelinOrange,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            OutlinedTextField(
                                value = customProblemInput,
                                onValueChange = { customProblemInput = it },
                                placeholder = {
                                    Text(
                                        if (selectedVehicle == "MOTOR") "Contoh: ban bocor, rem decit, aki soak..."
                                        else "Contoh: ac panas, rem getar, kuras radiator...",
                                        fontSize = 12.sp
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true,
                                trailingIcon = {
                                    if (customProblemInput.isNotBlank() && liveDetection != null) {
                                        IconButton(onClick = {
                                            val newItem = CustomServiceItem(
                                                id = "c_${System.currentTimeMillis()}",
                                                query = customProblemInput,
                                                name = liveDetection.first,
                                                price = liveDetection.second,
                                                estimatedMinutes = if (selectedVehicle == "MOTOR") 20 else 30
                                            )
                                            customAddedServices = customAddedServices + newItem
                                            customProblemInput = ""
                                        }) {
                                            Icon(imageVector = Icons.Default.Add, contentDescription = "Tambah")
                                        }
                                    }
                                }
                            )

                            // Quick suggestion chips
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val chips = if (selectedVehicle == "MOTOR") {
                                    listOf("Ban bocor", "Rem decit", "Aki tekor", "Mesin brebet")
                                } else {
                                    listOf("Ban bocor", "AC panas", "Aki mati", "Radiator")
                                }
                                chips.forEach { chip ->
                                    FilterChip(
                                        selected = customProblemInput == chip,
                                        onClick = { customProblemInput = chip },
                                        label = { Text(chip, fontSize = 10.sp) }
                                    )
                                }
                            }

                            // Live detected preview
                            if (customProblemInput.isNotBlank()) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        val detectedTitle = liveDetection?.first ?: "Mencari perkiraan kendala..."
                                        Text(
                                            text = "✨ Terdeteksi: $detectedTitle",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF1D4ED8)
                                        )
                                        if (liveDetection != null) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "Perkiraan: " + formatRupiah(liveDetection.second),
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = Color(0xFF16A34A)
                                                )
                                                Button(
                                                    onClick = {
                                                        val newItem = CustomServiceItem(
                                                            id = "c_${System.currentTimeMillis()}",
                                                            query = customProblemInput,
                                                            name = liveDetection.first,
                                                            price = liveDetection.second,
                                                            estimatedMinutes = if (selectedVehicle == "MOTOR") 20 else 30
                                                        )
                                                        customAddedServices = customAddedServices + newItem
                                                        customProblemInput = ""
                                                    },
                                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                                    shape = RoundedCornerShape(8.dp),
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                                                ) {
                                                    Text("+ Tambahkan", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // List of added custom services
                            if (customAddedServices.isNotEmpty()) {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = "Layanan Ditambahkan (${customAddedServices.size}):",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    customAddedServices.forEach { customItem ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                                                .padding(horizontal = 10.dp, vertical = 8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(text = customItem.name, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                                Text(text = "Input: " + customItem.query, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = formatRupiah(customItem.price),
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF16A34A)
                                                )
                                                IconButton(
                                                    onClick = {
                                                        customAddedServices = customAddedServices.filter { it.id != customItem.id }
                                                    },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(imageVector = Icons.Default.Close, contentDescription = "Hapus", modifier = Modifier.size(16.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Text(
                        text = "Pilih Layanan Standar (Centang):",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )

                    currentServices.forEach { item ->
                        val isChecked = item.id in selectedServiceIds
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedServiceIds = if (isChecked) {
                                        selectedServiceIds - item.id
                                    } else {
                                        selectedServiceIds + item.id
                                    }
                                },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isChecked) BengkelinOrange.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = isChecked,
                                        onCheckedChange = { checked ->
                                            selectedServiceIds = if (checked == true) {
                                                selectedServiceIds + item.id
                                            } else {
                                                selectedServiceIds - item.id
                                            }
                                        }
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text(text = item.name, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                        Text(text = "± ${item.estimatedMinutes} menit", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                Text(
                                    text = formatRupiah(item.price),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF16A34A)
                                )
                            }
                        }
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = BengkelinNavy),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            val totalItemCount = selectedServiceIds.size + customAddedServices.size
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Total Estimasi ($totalItemCount Layanan):", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                                Text(text = "± $totalTime menit", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                            }
                            Text(
                                text = formatRupiah(totalCost),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = BengkelinOrange
                            )

                            // Rincian Sumber Layanan
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = "• Dicentang (${selectedServiceIds.size}):", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                                Text(text = formatRupiah(checklistCost), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                            if (customAddedServices.isNotEmpty()) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(text = "• Diketik & Terdeteksi (${customAddedServices.size}):", color = Color(0xFF86EFAC), fontSize = 11.sp)
                                    Text(text = formatRupiah(customCost), color = Color(0xFF86EFAC), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }

                            Text(
                                text = "✓ Rata-rata tarif standar bengkel terverifikasi di Malang Raya.",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )

                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = onNavigateToWorkshops,
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = BengkelinOrange)
                                ) {
                                    Text("Cari Bengkel", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                                Button(
                                    onClick = {
                                        val selectedNames = currentServices.filter { it.id in selectedServiceIds }.map { it.name }
                                        val customNames = customAddedServices.map { it.name + " (" + formatRupiah(it.price) + ")" }
                                        val sb = java.lang.StringBuilder()
                                        sb.append("Halo Bengkelin Malang Raya, saya ingin servis:\n")
                                        sb.append("- Kendaraan: ").append(selectedVehicle).append("\n")
                                        if (selectedNames.isNotEmpty()) {
                                            sb.append("- Layanan Dicentang: ").append(selectedNames.joinToString(", ")).append("\n")
                                        }
                                        if (customNames.isNotEmpty()) {
                                            sb.append("- Layanan Diketik (Terdeteksi): ").append(customNames.joinToString(", ")).append("\n")
                                        }
                                        sb.append("- Total Estimasi: ").append(formatRupiah(totalCost)).append(" (± ").append(totalTime).append(" menit)\n")
                                        sb.append("Apakah ada montir/bengkel yang senggang di Malang Raya?")
                                        val waUrl = "https://api.whatsapp.com/send?phone=6285606401220&text=" + Uri.encode(sb.toString())
                                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(waUrl)))
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                                ) {
                                    Text("Tanya ke WA", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            } else if (selectedTab == 1) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = Color(0xFFD97706))
                            Text(
                                text = "Jalur Tanjakan Klemuk Songgoriti, Cangar, dan Payung Pujon memiliki turunan curam yang rawan rem blong dan mesin overheat.",
                                fontSize = 12.sp,
                                color = Color(0xFF92400E)
                            )
                        }
                    }

                    Text(text = "Checklist Kesiapan Kendaraan:", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                    FilterChip(
                        selected = brakeOk,
                        onClick = { brakeOk = !brakeOk },
                        label = { Text("1. Kampas & Minyak Rem: " + if (brakeOk) "Normal" else "Beresiko") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    FilterChip(
                        selected = radiatorOk,
                        onClick = { radiatorOk = !radiatorOk },
                        label = { Text("2. Air Radiator & Pendingin: " + if (radiatorOk) "Normal" else "Beresiko Overheat") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    FilterChip(
                        selected = tireOk,
                        onClick = { tireOk = !tireOk },
                        label = { Text("3. Alur Ban: " + if (tireOk) "Tebal" else "Gundul/Licin") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    FilterChip(
                        selected = heavyLoad,
                        onClick = { heavyLoad = !heavyLoad },
                        label = { Text("4. Muatan/Penumpang Penuh: " + if (heavyLoad) "Ya (Beban Berat)" else "Tidak") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    var score = 100
                    if (!brakeOk) score -= 45
                    if (!radiatorOk) score -= 30
                    if (!tireOk) score -= 15
                    if (heavyLoad) score -= 10
                    if (score < 0) score = 0

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = when {
                                score >= 85 -> Color(0xFFDCFCE7)
                                score >= 55 -> Color(0xFFFEF3C7)
                                else -> Color(0xFFFEE2E2)
                            }
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Skor Kesiapan Tanjakan Batu:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(text = "${score}%", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                            }
                            Text(
                                text = when {
                                    score >= 85 -> "🟢 SIAP TANJAKAN BATU: Kendaraan Anda aman melintasi Songgoriti & Cangar."
                                    score >= 55 -> "🟡 PERINGATAN: Berisiko rem panas atau selip. Lakukan pengecekan sebelum menanjak."
                                    else -> "🔴 BAHAYA REM BLONG / OVERHEAT! Jangan melintasi Klemuk sebelum servis rem!"
                                },
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            )
                            Button(
                                onClick = onNavigateToWorkshops,
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = BengkelinNavy)
                            ) {
                                Text("Cari Bengkel di Kota Batu (Google Maps)", fontSize = 12.sp)
                            }
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E8FF)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFF9333EA), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Asisten Montir & Tanya Jawab AI", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF6B21A8))
                            }
                            Text(
                                "Tanya jawab cerdas seputar kendala mobil & motor di Malang Raya (tanjakan Payung, rem Klemuk, banjir Suhat).",
                                fontSize = 11.sp,
                                color = Color(0xFF581C87)
                            )
                        }
                    }

                    Card(
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("1. Rem Mencicit Melengking Nyaring", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Bahaya", color = Color(0xFFDC2626), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                            Text("Karakter: \"Ciiiit... ciiit... saat injak rem\"", fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, fontSize = 11.sp, color = BengkelinOrange)
                            Text("Penyebab: Plat besi kampas rem habis bergesekan langsung dengan cakram disc.", fontSize = 11.sp)
                            Text("Tindakan: Segera ganti kampas rem! Berisiko rem blong jika lewat tanjakan Batu/Songgoriti.", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            Text("Estimasi Biaya: " + if (selectedVehicle == "MOTOR") "± Rp 45.000" else "± Rp 250.000", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, color = BengkelinOrange)
                        }
                    }

                    Card(
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("2. Ketukan Kasar di Blok Mesin", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Bahaya", color = Color(0xFFDC2626), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                            Text("Karakter: \"Klotok-klotok... tek-tek saat digas\"", fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, fontSize = 11.sp, color = BengkelinOrange)
                            Text("Penyebab: Rantai keteng kendor, klep longgar, atau oli mesin kering/habis.", fontSize = 11.sp)
                            Text("Tindakan: Segera periksa oli & setel tensioner rantai keteng sebelum piston macet.", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            Text("Estimasi Biaya: " + if (selectedVehicle == "MOTOR") "± Rp 85.000" else "± Rp 450.000", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, color = BengkelinOrange)
                        }
                    }

                    Card(
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("3. Mendengung Kasar di Kaki Roda", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Peringatan", color = Color(0xFFD97706), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                            Text("Karakter: \"Nguuung... grok-grok semakin kencang saat melaju\"", fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, fontSize = 11.sp, color = BengkelinOrange)
                            Text("Penyebab: Bearing (klaher) roda oblak atau pelumas kering akibat genangan air.", fontSize = 11.sp)
                            Text("Tindakan: Ganti bearing roda sebelum pecah dan mengunci ban saat berkendara.", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            Text("Estimasi Biaya: " + if (selectedVehicle == "MOTOR") "± Rp 50.000" else "± Rp 320.000", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, color = BengkelinOrange)
                        }
                    }

                    Button(
                        onClick = onNavigateToWorkshops,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = BengkelinOrange)
                    ) {
                        Text("Temukan Bengkel Perbaikan Terdekat di Malang Raya", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
