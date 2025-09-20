// File: ui/trip/TripScreen.kt

package com.example.jeksoed.ui.screens.trip

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.jeksoed.data.model.RideRequest
import com.example.jeksoed.navigation.Screen
import com.example.jeksoed.ui.theme.JekSoedTheme
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.firebase.Timestamp
import com.google.maps.android.PolyUtil
import com.google.maps.android.compose.*

// Composable yang terhubung ke ViewModel
@Composable
fun TripScreen(
    navController: NavController,
    viewModel: TripViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Logic khusus untuk Driver (start/stop location updates)
    if (uiState.isDriver) {
        val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
        DisposableEffect(Unit) {
            viewModel.startLocationUpdates(fusedLocationClient, context)
            onDispose {
                viewModel.stopLocationUpdates(fusedLocationClient)
            }
        }
    }

    TripScreenContent(
        uiState = uiState,
        cameraPositionState = rememberCameraPositionState(),
        onUpdateStatus = { newStatus ->
            viewModel.updateTripStatus(newStatus)
        },
        onLogoutClick = {
            viewModel.logout()
            navController.navigate(Screen.Login.route) {
                popUpTo(Screen.Trip.route) { inclusive = true }
            }
        }
    )
}

// Composable yang hanya bertugas menampilkan UI
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripScreenContent(
    uiState: TripUiState,
    cameraPositionState: CameraPositionState,
    onUpdateStatus: (String) -> Unit,
    onLogoutClick: () -> Unit
) {

    // Efek untuk menyesuaikan kamera
    LaunchedEffect(uiState.polylinePoints) {
        if (uiState.polylinePoints.isNotEmpty()) {
            val bounds = LatLngBounds.builder()
            uiState.polylinePoints.forEach { bounds.include(it) }
            cameraPositionState.animate(
                com.google.android.gms.maps.CameraUpdateFactory.newLatLngBounds(bounds.build(), 150)
            )
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Trip") },
                actions = { Button(onClick = onLogoutClick) { Text("Logout") } }
            )
        }
    ) { paddingValues ->

        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState
            ) {
                if (uiState.polylinePoints.isNotEmpty()) {
                    Polyline(points = uiState.polylinePoints, color = Color.Blue, width = 15f)
                }

                uiState.rideRequest?.let { request ->
                    val pickupLatLng = LatLng(
                        request.pickupLocation["latitude"] ?: 0.0,
                        request.pickupLocation["longitude"] ?: 0.0
                    )
                    Marker(state = MarkerState(position = pickupLatLng), title = "Jemput")

                    val destLatLng = LatLng(
                        request.destinationLocation["latitude"] ?: 0.0,
                        request.destinationLocation["longitude"] ?: 0.0
                    )
                    Marker(state = MarkerState(position = destLatLng), title = "Tujuan")

                    request.driverCurrentLocation?.let {
                        val driverLatLng = LatLng(it["latitude"] ?: 0.0, it["longitude"] ?: 0.0)
                        Marker(
                            state = MarkerState(position = driverLatLng),
                            title = "Driver",
                            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                        )
                    }
                }
            }

            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Tampilkan status untuk kedua pengguna
                    Text(
                        text = "Status: ${uiState.rideRequest?.status?.replaceFirstChar { it.titlecase() } ?: "Memuat..."}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Tampilkan tombol HANYA untuk driver
                    if (uiState.isDriver) {
                        when (uiState.rideRequest?.status) {
                            "accepted" -> {
                                Button(
                                    onClick = { onUpdateStatus("arrived") },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Sudah Sampai di Lokasi Jemput")
                                }
                            }

                            "arrived" -> {
                                Button(
                                    onClick = { onUpdateStatus("started") },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Mulai Perjalanan")
                                }
                            }

                            "started" -> {
                                Button(
                                    onClick = { onUpdateStatus("completed") },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Selesaikan Perjalanan")
                                }
                            }

                            "completed" -> {
                                Text("Perjalanan Selesai!")
                            }
                        }
                    }
                }
            }
        }
    }
}

    @Preview(showBackground = true)
    @Composable
    fun TripScreenPreview() {
//    data dummy
        val fakeRideRequest = RideRequest(
            status = "accepted",
            pickupLocation = mapOf("latitude" to -6.892, "longitude" to 109.670),
            destinationLocation = mapOf("latitude" to -6.902, "longitude" to 109.680),
            driverCurrentLocation = mapOf("latitude" to -6.895, "longitude" to 109.675)
        )
        val fakePolyline = PolyUtil.decode("mp`_F~`|iS_@y@g@s@o@u@") // Contoh polyline pendek

//    fake state
        val fakeUiState = TripUiState(
            rideRequest = fakeRideRequest,
            polylinePoints = fakePolyline,
            isDriver = false
        )

//    untuk nampilin ui dengan data dummmy
        val cameraState = rememberCameraPositionState()
        JekSoedTheme {
            TripScreenContent(
                uiState = fakeUiState,
                cameraPositionState = cameraState,
                onUpdateStatus = {},
                onLogoutClick = {}
            )
        }
    }
