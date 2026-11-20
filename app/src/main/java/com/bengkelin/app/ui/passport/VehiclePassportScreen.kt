package com.bengkelin.app.ui.passport

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bengkelin.app.ui.theme.BengkelinOrange

data class ComponentWearStatus(
    val id: String,
    val name: String,
    val category: String,
    val maxIntervalKm: Int,
    val remainingKm: Int,
    val tip: String
)

data class ServiceLogEntry(
    val id: String,
    val date: String,
    val odometer: Int,
    val serviceTitle: String,
    val workshopName: String,
    val cost: Long,
    val hasWarranty: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehiclePassportScreen(
    plateNumber: String = "N 4821 ABD",
    vehicleName: String = "Honda Vario 160 eSP+",
    currentOdometer: Int = 14250,
    onNavigateBack: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(0) }
    var odoState by remember { mutableStateOf(currentOdometer) }

    var components by remember {
        mutableStateOf(
            listOf(
                ComponentWearStatus("1", "Oli Mesin Matic (10W-30)", "Pelumasan", 3000, 420, "Ganti oli segera sebelum tanjakan Batu."),
                ComponentWearStatus("2", "Kampas Rem Depan & Belakang", "Pengereman", 10000, 4800, "Ketebalan masih aman untuk rute Kota Malang."),
                ComponentWearStatus("3", "V-Belt & Roller CVT", "Transmisi", 15000, 2100, "Periksa kisi karet belt dari keretakan."),
                ComponentWearStatus("4", "Aki Kering MF & Kelistrikan", "Kelistrikan", 20000, 16500, "Tegangan 12.6V, starter dinamo responsif."),
                ComponentWearStatus("5", "Busi Iridium & Filter Udara", "Pengapian", 8000, 1450, "Bersihkan box filter udara dari debu."),
                ComponentWearStatus("6", "Ban Tubeless Depan & Belakang", "Roda", 18000, 11200, "Ketebalan alur 3.5mm, tekanan angin aman.")
            )
        )
    }

    // Perhitungan Health Score (Rata-rata % sisa kilometer)
    val healthScore = remember(components) {
        val totalPct = components.sumOf { (it.remainingKm.toFloat() / it.maxIntervalKm.toFloat() * 100).toInt().coerceIn(5, 100) }
        totalPct / components.size
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Buku Servis & Paspor Digital", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text("Malang Raya • Pantau Komponen & Riwayat", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Plat Nomor Asli Indonesia & Odometer
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(text = vehicleName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Plat Nomor Hitam
                            Box(
                                modifier = Modifier
                                    .background(Color.Black, RoundedCornerShape(8.dp))
                                    .border(1.5.dp, Color.White, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = plateNumber,
                                    color = Color.White,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp
                                )
                            }

                            // Odometer Box
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Odometer", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("$odoState KM", fontWeight = FontWeight.Black, fontSize = 15.sp, color = BengkelinOrange)
                            }
                        }

                        // Tombol Simulasi Kilometer
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = {
                                    odoState += 500
                                    components = components.map { it.copy(remainingKm = (it.remainingKm - 500).coerceAtLeast(0)) }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("+500 KM (Batu)", fontSize = 11.sp)
                            }
                            OutlinedButton(
                                onClick = {
                                    odoState += 1000
                                    components = components.map { it.copy(remainingKm = (it.remainingKm - 1000).coerceAtLeast(0)) }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("+1.000 KM (Bromo)", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            // Health Score Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (healthScore >= 80) Color(0xFF064E3B) else Color(0xFF78350F)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("SKOR KESEHATAN KENDARAAN", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.8f))
                            Text(
                                if (healthScore >= 80) "Kondisi Prima" else "Perlu Servis Ringan",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                "Komponen pelumasan dan rem dimonitor berkala.",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.15f))
                                .border(2.dp, Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("$healthScore%", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
                        }
                    }
                }
            }

            // Tabs: 6 Komponen / Riwayat
            item {
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("6 Komponen Vital") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Riwayat Servis") }
                    )
                }
            }

            // List Komponen
            if (selectedTab == 0) {
                items(components) { comp ->
                    val pct = (comp.remainingKm.toFloat() / comp.maxIntervalKm.toFloat()).coerceIn(0f, 1f)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(comp.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("${(pct * 100).toInt()}%", fontWeight = FontWeight.Black, fontSize = 12.sp)
                            }

                            LinearProgressIndicator(
                                progress = { pct },
                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                color = if (pct < 0.2f) Color.Red else if (pct < 0.45f) Color(0xFFF59E0B) else Color(0xFF10B981)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Sisa: ${comp.remainingKm} KM", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Button(
                                    onClick = {
                                        components = components.map {
                                            if (it.id == comp.id) it.copy(remainingKm = it.maxIntervalKm) else it
                                        }
                                    },
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text("Ganti / Servis", fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
