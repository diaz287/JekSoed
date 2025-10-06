// main/java/com/example/jeksoed/ui/screens/activity/ActivityScreen.kt

package com.example.jeksoed.ui.screens.activity

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.jeksoed.R
import com.example.jeksoed.navigation.Screen
import com.example.jeksoed.ui.theme.JekSoedTheme
import com.example.jeksoed.utils.formatCurrency
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
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) { // Kurung kurawal LazyColumn dibuka
                items(uiState.filteredRides) { historyItem ->
                    HistoryItemCard(
                        historyItem = historyItem,
                        isDriverView = uiState.isDriver, // Teruskan info peran
                        onDetailClick = {
                            navController.navigate(Screen.ActivityDetail.createRoute(historyItem.ride.id))
                        }
                    )
                } // <-- **ERROR 1: KURUNG KURAWAL 'items' SEHARUSNYA DI SINI**
            } // <-- **ERROR 3: KURUNG KURAWAL 'LazyColumn' SEHARUSNYA DI SINI**
        }
    }
}

// <-- **ERROR 2: PINDAHKAN FUNGSI INI KE LUAR 'ActivityScreen'**
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
        SimpleDateFormat("d MMMM yyyy", Locale("id", "ID")).format(it.toDate())
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
                    RouteRow(iconRes = R.drawable.blue_icon, location = "FK Unsoed") // Ganti data asli
                    Spacer(modifier = Modifier.height(8.dp))
                    RouteRow(iconRes = R.drawable.locatio_icon, location = "Rumah Sakit Wiradadi") // Ganti data asli
                }
                // Info
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                    Text(statusText, color = statusColor, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    // --- PERUBAHAN LABEL DI SINI ---
                    Text(if (isDriverView) "Penumpang" else "Driver", color = Color.Gray)
                    Text(historyItem.otherUserName, fontWeight = FontWeight.SemiBold)

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Total", color = Color.Gray)
                    Text(formatCurrency(10000), fontWeight = FontWeight.Bold) // Ganti harga asli
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

@Preview(showBackground = true)
@Composable
private fun HistoryScreenPreview() {
    JekSoedTheme {
        // ... (Anda bisa membuat data palsu di sini untuk preview)
    }
}