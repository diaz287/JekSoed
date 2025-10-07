// main/java/com/example/jeksoed/ui/screens/trip/TripScreen.kt

package com.example.jeksoed.ui.screens.trip

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.jeksoed.data.model.RideRequest
import com.example.jeksoed.navigation.Screen
import com.example.jeksoed.ui.screens.trip.components.TripDriverBottomSheet
import com.example.jeksoed.ui.theme.JekSoedTheme
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.PolyUtil
import com.google.maps.android.compose.*
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import com.example.jeksoed.ui.screens.trip.components.PaymentConfirmationCard
import com.example.jeksoed.ui.screens.trip.components.TripDriverBottomSheet
import com.example.jeksoed.ui.screens.trip.components.TripPassengerSheet
import com.example.jeksoed.utils.formatCurrency
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// Composable "Pintar" yang terhubung ke ViewModel
@Composable
fun TripScreen(
    navController: NavController,
    rideRequestId: String
) {
    val viewModel: TripViewModel = viewModel(
        factory = TripViewModelFactory(
            rideRequestId = rideRequestId,
            firestore = FirebaseFirestore.getInstance(),
            auth = FirebaseAuth.getInstance()
        )
    )

    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Logic untuk update lokasi driver (tidak berubah)
    if (uiState.isDriver) {
        val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
        DisposableEffect(Unit) {
            viewModel.startLocationUpdates(fusedLocationClient, context)
            onDispose {
                viewModel.stopLocationUpdates(fusedLocationClient)
            }
        }
    }

    // Logic untuk navigasi
    LaunchedEffect(Unit) {
        viewModel.navEvent.collect { event ->
            when (event) {
                is TripNavEvent.NavigateToDriverHome -> {
                    navController.navigate(Screen.DriverMain.route) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    }
                }
                is TripNavEvent.NavigateToRatingScreen -> {
                    navController.navigate(Screen.Rating.createRoute(event.driverId)) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    }
                }
                is TripNavEvent.NavigateToTripCompleted -> {
                    navController.navigate(Screen.TripCompleted.createRoute(event.rideRequestId)) {
                        popUpTo(Screen.Trip.route) { inclusive = true }
                    }
                }

            }
        }
    }

    // Panggil UI Composable yang baru
    TripScreenLayout(
        uiState = uiState,
        cameraPositionState = rememberCameraPositionState(),
        onUpdateStatus = viewModel::updateTripStatus,
        onCancelTrip = viewModel::cancelTrip,
        onFinishTrip = viewModel::confirmPaymentAndFinishTrip,
        onChatClick = { rideId ->
            navController.navigate(Screen.Chat.createRoute(rideId))
        },
        onBackClick = { navController.popBackStack() }
    )
}

// Composable "Biasa" yang hanya menampilkan UI
@Composable
fun TripScreenLayout(
    uiState: TripUiState,
    cameraPositionState: CameraPositionState,
    onUpdateStatus: (String) -> Unit,
    onCancelTrip: () -> Unit,
    onFinishTrip: () -> Unit,
    onChatClick: (rideId: String) -> Unit,
    onBackClick: () -> Unit
) {
    // Efek untuk menyesuaikan kamera (tidak berubah)
    LaunchedEffect(uiState.polylinePoints) {
        if (uiState.polylinePoints.isNotEmpty()) {
            val bounds = LatLngBounds.builder()
            uiState.polylinePoints.forEach { bounds.include(it) }
            cameraPositionState.animate(
                com.google.android.gms.maps.CameraUpdateFactory.newLatLngBounds(bounds.build(), 150)
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // --- PETA SEBAGAI LATAR BELAKANG ---
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState
        ) {
            // Polyline, Marker Jemput, Tujuan, dan Driver (tidak berubah)
            if (uiState.polylinePoints.isNotEmpty()) {
                Polyline(points = uiState.polylinePoints, color = MaterialTheme.colorScheme.primary, width = 15f)
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

        // --- TOMBOL KEMBALI DI ATAS KIRI ---
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(horizontal = 16.dp, vertical = 32.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
        }

        // --- BOTTOM SHEET DI BAWAH ---
        Box(modifier = Modifier.align(Alignment.BottomCenter)) {
            if (uiState.isDriver) {
                // Tampilan untuk Driver
                AnimatedVisibility(
                    visible = uiState.rideRequest?.status != "completed",
                    exit = slideOutVertically { it }
                ) {
                    TripDriverBottomSheet(
                        uiState = uiState,
                        onUpdateStatus = onUpdateStatus,
                        onCancelTrip = onCancelTrip,
                        onChatClick = { uiState.rideRequest?.id?.let { onChatClick(it) } }
                    )
                }

                AnimatedVisibility(
                    visible = uiState.rideRequest?.status == "completed",
                    enter = slideInVertically { it }
                ) {
                    PaymentConfirmationCard(
                        totalPayment = uiState.rideRequest?.price ?: "Rp0",
                        onConfirmClick = onFinishTrip
                    )
                }
            } else {
                // Tampilan untuk Penumpang
                TripPassengerSheet(
                    uiState = uiState,
                    onCancelTrip = onCancelTrip,
                    onChatClick = { uiState.rideRequest?.id?.let { onChatClick(it) } }
                )
            }
        }
    }
}
@Preview(showSystemUi = true, name = "Trip Screen - Status Started")
@Composable
private fun TripScreenLayoutStartedPreview() {
    TripScreenLayoutPreview(status = "started")
}

@Preview(showSystemUi = true, name = "Trip Screen - Status Completed")
@Composable
private fun TripScreenLayoutCompletedPreview() {
    TripScreenLayoutPreview(status = "completed")
}

@Composable
private fun TripScreenLayoutPreview(status: String) {
    val fakeRideRequest = RideRequest(status = status)
    val fakeUiState = TripUiState(rideRequest = fakeRideRequest, isDriver = true)
    JekSoedTheme {
        TripScreenLayout(
            uiState = fakeUiState,
            cameraPositionState = rememberCameraPositionState(),
            onUpdateStatus = {},
            onCancelTrip = {},
            onFinishTrip = {},
            onChatClick = {},
            onBackClick = {}
        )
    }
}