// main/java/com/example/jeksoed/ui/screens/activity/ActivityScreen.kt

package com.example.jeksoed.ui.screens.activity

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.jeksoed.R
import com.example.jeksoed.data.model.RideRequest
import com.example.jeksoed.navigation.Screen
import com.example.jeksoed.ui.theme.JekSoedTheme
import com.example.jeksoed.utils.formatCurrency
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ActivityScreen(
    navController: NavController,
    viewModel: ActivityViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(painter = painterResource(id = R.drawable.maps_icon), contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Riwayat Aktivitas", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }

        // Tabs
        val tabs = listOf("Semua", "Selesai", "Dibatalkan")
        TabRow(selectedTabIndex = uiState.selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = uiState.selectedTab == index,
                    onClick = { viewModel.onTabSelected(index) },
                    text = { Text(title) }
                )
            }
        }

        // List
        Box(modifier = Modifier.fillMaxSize()) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.filteredRides.isEmpty()) {
                Text(
                    text = "Kamu belum punya riwayat perjalanan di kategori ini.",
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 32.dp)
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(uiState.filteredRides) { historyItem ->
                        HistoryItemCard(
                            historyItem = historyItem,
                            isDriverView = uiState.isDriver,
                            onDetailClick = {
                                navController.navigate(Screen.ActivityDetail.createRoute(historyItem.ride.id))
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryItemCard(
    historyItem: RideHistoryDisplay, // Gunakan data class baru
    isDriverView: Boolean, // Terima info peran
    onDetailClick: () -> Unit
) {
    val ride = historyItem.ride
    val statusText = if (ride.status == "completed") "Selesai" else "Dibatalkan"
    val statusColor = if (ride.status == "completed") Color(0xFF219800) else Color.Red
    val formattedDate = ride.createdAt?.let {
        SimpleDateFormat("d MMMM yyyy", Locale.forLanguageTag("id-ID")).format(it.toDate())
    } ?: ""

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onDetailClick,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(formattedDate, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                // Rute
                Column(modifier = Modifier.weight(1.5f)) {
                    RouteRow(iconRes = R.drawable.blue_icon, location = ride.pickupName ?: "Lokasi Jemput")
                    Spacer(modifier = Modifier.height(8.dp))
                    RouteRow(iconRes = R.drawable.locatio_icon, location = ride.destinationName ?: "Lokasi Tujuan")
                }
                // Info
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                    Text(statusText, color = statusColor, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(if (isDriverView) "Penumpang" else "Driver", color = Color.Gray)
                    Text(historyItem.otherUserName, fontWeight = FontWeight.SemiBold)

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Total", color = Color.Gray)
                    Text(ride.price ?: "Rp0", fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("Detail Pesanan >", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterHorizontally))
        }
    }
}

@Composable
private fun RouteRow(iconRes: Int, location: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(painter = painterResource(id = iconRes), contentDescription = null, tint = Color.Unspecified)
        Spacer(modifier = Modifier.width(8.dp))
        Text(location)
    }
}

// --- FUNGSI PREVIEW DENGAN DATA PALSU ---
@Preview(showSystemUi = true, showBackground = true)
@Composable
private fun HistoryScreenPreview() {
    JekSoedTheme {
        // 1. Buat data palsu
        val dummyRides = listOf(
            RideHistoryDisplay(
                ride = RideRequest(
                    id = "1",
                    status = "completed",
                    createdAt = Timestamp.now(),
                    pickupName = "Fakultas Kedokteran",
                    destinationName = "GOR Satria",
                    price = "Rp12.000"
                ),
                otherUserName = "Fajar Nugros"
            ),
            RideHistoryDisplay(
                ride = RideRequest(
                    id = "2",
                    status = "cancelled",
                    createdAt = Timestamp(Date(System.currentTimeMillis() - 86400000)), // Kemarin
                    pickupName = "Rita Supermall",
                    destinationName = "Stasiun Purwokerto",
                    price = "Rp15.000"
                ),
                otherUserName = "Rafi Purnama"
            ),
            RideHistoryDisplay(
                ride = RideRequest(
                    id = "3",
                    status = "completed",
                    createdAt = Timestamp(Date(System.currentTimeMillis() - 172800000)), // 2 hari lalu
                    pickupName = "Unsoed",
                    destinationName = "Moro Mall",
                    price = "Rp10.000"
                ),
                otherUserName = "Imedia Sholem"
            )
        )

        // 2. Tampilkan UI dengan data palsu
        // Kita tidak bisa menggunakan ViewModel di preview, jadi kita panggil Composable UI-nya langsung
        Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(painter = painterResource(id = R.drawable.maps_icon), contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Riwayat Aktivitas", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }

            TabRow(selectedTabIndex = 0) {
                Tab(selected = true, onClick = {}, text = { Text("Semua") })
                Tab(selected = false, onClick = {}, text = { Text("Selesai") })
                Tab(selected = false, onClick = {}, text = { Text("Dibatalkan") })
            }

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(dummyRides) { historyItem ->
                    HistoryItemCard(
                        historyItem = historyItem,
                        isDriverView = false, // Ganti jadi 'true' untuk melihat preview dari sisi driver
                        onDetailClick = {}
                    )
                }
            }
        }
    }
}