package com.example.jeksoed.ui.screens.driver

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.jeksoed.data.model.RideRequest
import com.example.jeksoed.navigation.Screen
import com.example.jeksoed.ui.theme.JekSoedTheme

/**
 * SMART COMPOSABLE (SCREEN-LEVEL)
 * - Membuat ViewModel.
 * - Mengumpulkan state dari ViewModel.
 * - Menangani navigasi.
 * - Meneruskan state dan event handler ke UI "bodoh".
 */
@Composable
fun DriverHomeScreen(
    navController: NavController,
    viewModel: DriverHomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    DriverHomeScreenUI(
        uiState = uiState,
        onAcceptRide = { rideId ->
            viewModel.acceptRide(
                rideId = rideId,
                onSuccess = {
                    Toast.makeText(context, "Orderan berhasil diambil!", Toast.LENGTH_SHORT).show()
                    navController.navigate(Screen.Trip.createRoute(rideId))
                },
                onFailure = { e ->
                    Toast.makeText(context, "Gagal mengambil orderan: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            )
        },
        onLogoutClick = {
            viewModel.logout()
            navController.navigate(Screen.Login.route) {
                popUpTo(Screen.DriverHome.route) { inclusive = true }
            }
        }
    )
}

/**
 * DUMB UI COMPOSABLE
 * - Sepenuhnya dikontrol dari luar.
 * - Tidak memiliki state internal atau logika bisnis.
 * - Mudah di-preview.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverHomeScreenUI(
    uiState: DriverHomeUiState,
    onAcceptRide: (rideId: String) -> Unit,
    onLogoutClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Orderan Tersedia") },
                actions = { Button(onClick = onLogoutClick) { Text("Logout") } }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator()
            } else if (uiState.rideRequests.isEmpty()) {
                Text("Belum ada orderan tersedia.")
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.rideRequests) { request ->
                        RideRequestCard(
                            rideRequest = request,
                            isAccepting = (uiState.acceptingRideId == request.id),
                            onAcceptClick = { onAcceptRide(request.id) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * DUMB COMPONENT CARD
 * - Hanya menampilkan data satu orderan.
 */
@Composable
fun RideRequestCard(
    rideRequest: RideRequest,
    isAccepting: Boolean,
    onAcceptClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Orderan Baru", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Jarak: ${rideRequest.distance}")
                Text("Waktu: ${rideRequest.duration}")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onAcceptClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isAccepting
            ) {
                if (isAccepting) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Ambil Orderan")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DriverHomeScreenPreview() {
    val dummyRequests = listOf(
        RideRequest(id = "1", distance = "5.2 km", duration = "15 min"),
        RideRequest(id = "2", distance = "3.1 km", duration = "8 min"),
    )
    val dummyUiState = DriverHomeUiState(
        rideRequests = dummyRequests,
        isLoading = false,
        acceptingRideId = "2" // Contoh jika orderan kedua sedang di-accept
    )
    JekSoedTheme {
        DriverHomeScreenUI(
            uiState = dummyUiState,
            onAcceptRide = {},
            onLogoutClick = {}
        )
    }
}