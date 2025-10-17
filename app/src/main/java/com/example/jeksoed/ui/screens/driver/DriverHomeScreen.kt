package com.example.jeksoed.ui.screens.driver

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.jeksoed.R
import com.example.jeksoed.navigation.Screen
import com.example.jeksoed.ui.screens.driver.components.RideRequestPopup
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.maps.android.compose.*
import kotlinx.coroutines.delay

@Composable
fun DriverHomeScreen(navController: NavController, viewModel: DriverHomeViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showOfflineDialog by remember { mutableStateOf(false) }

    val cameraPositionState = rememberCameraPositionState()
    val driverLocation = uiState.driverLocation

    // Launcher untuk meminta izin lokasi
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                // Izin diberikan, logika akan berjalan di DisposableEffect
            } else {
                Toast.makeText(context, "Izin lokasi dibutuhkan untuk fitur ini", Toast.LENGTH_SHORT).show()
            }
        }
    )

    LaunchedEffect(Unit) {
        viewModel.navigateToActiveTrip.collect { rideId ->
            // Gunakan NavController utama untuk navigasi
            navController.navigate(Screen.Trip.createRoute(rideId))
        }
    }

    // Efek untuk menggerakkan kamera
    LaunchedEffect(driverLocation) {
        driverLocation?.let {
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(it, 15f),
                durationMs = 1000
            )
        }
    }

    // Mengelola siklus hidup pembaruan lokasi
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    DisposableEffect(Unit) {
        // Minta izin terlebih dahulu
        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        // Mulai update lokasi
        viewModel.startLocationUpdates(fusedLocationClient, context)
        // Hentikan saat composable hilang
        onDispose {
            viewModel.stopLocationUpdates(fusedLocationClient)
        }
    }

    LaunchedEffect(uiState.popupRideRequest) {
        if (uiState.popupRideRequest != null) {
            delay(30000) // Waktu tunggu orderan
            viewModel.dismissPopup()
        }
    }

    if (showOfflineDialog) {
        OfflineConfirmationDialog(
            onDismiss = { showOfflineDialog = false },
            onConfirm = {
                viewModel.setOnlineStatus(false)
                showOfflineDialog = false
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            uiSettings = MapUiSettings(zoomControlsEnabled = false)
        ) {
            // Tampilkan marker di lokasi driver
            driverLocation?.let {
                Marker(
                    state = MarkerState(position = it),
                    title = "Lokasi Anda"
                )
            }
        }

        StatusIndicator(
            isOnline = uiState.isOnline,
            modifier = Modifier.align(Alignment.TopCenter)
        )

        NotificationButton(
            onNotificationClick = {
                navController.navigate(Screen.AllOrders.route)
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 16.dp, end = 16.dp)
        )

        DriverInfoCard(
            uiState = uiState,
            onToggleStatus = {
                if (uiState.isOnline) {
                    showOfflineDialog = true
                } else {
                    viewModel.setOnlineStatus(true)
                }
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        AnimatedVisibility(
            visible = uiState.popupRideRequest != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            uiState.popupRideRequest?.let { request ->
                RideRequestPopup(
                    rideRequest = request,
                    onAccept = {
                        viewModel.acceptRide(
                            rideRequest = request,
                            onSuccess = {
                                navController.navigate(Screen.Trip.createRoute(request.id))
                            },
                            onFailure = { error ->
                                Toast.makeText(context, "Gagal menerima: ${error.message}", Toast.LENGTH_SHORT).show()
                            }
                        )
                    },
                    onReject = {
                        viewModel.rejectRide(request.id)
                    }
                )
            }
        }
    }
}

@Composable
private fun StatusIndicator(isOnline: Boolean, modifier: Modifier = Modifier) {
    val backgroundColor = if (isOnline) Color(0xFFFFC107) else Color.White
    val textColor = if (isOnline) Color.Black else Color.Gray

    Card(
        modifier = modifier.offset(y = 24.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Text(
            text = if (isOnline) "Kamu Online" else "Kamu Offline",
            color = textColor,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun NotificationButton(onNotificationClick: () -> Unit, modifier: Modifier = Modifier) {
    IconButton(
        onClick = onNotificationClick,
        modifier = modifier
            .clip(CircleShape)
            .background(Color.White)
    ) {
        Icon(Icons.Default.Notifications, contentDescription = "Notifikasi")
    }
}

@Composable
private fun DriverInfoCard(
    uiState: DriverHomeUiState,
    onToggleStatus: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                AsyncImage(
                    model = uiState.driverProfile.photoUrl,
                    contentDescription = "Foto Profil",
                    placeholder = painterResource(id = R.drawable.person_icon),
                    error = painterResource(id = R.drawable.person_icon),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(uiState.driverProfile.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(uiState.driverProfile.licensePlate, color = Color.Gray, fontSize = 14.sp)
                }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (uiState.isOnline) colorResource(R.color.unsoed) else Color.White)
                        .clickable { onToggleStatus() }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.power_icon),
                        contentDescription = "Toggle Status",
                        tint = if (uiState.isOnline) Color.Black else Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF272343))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    InfoItem("Balance", uiState.driverProfile.balance)
                    InfoItem("Rating", uiState.driverProfile.rating)
                    InfoItem("Orderan", uiState.driverProfile.orderCount)
                }
            }
        }
    }
}

@Composable
private fun InfoItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = Color.Gray, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(4.dp))
        if (label == "Rating") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFC107), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(value, color = Color.White, fontWeight = FontWeight.SemiBold)
            }
        } else {
            Text(value, color = Color.White, fontWeight = FontWeight.SemiBold)
        }
        Spacer(modifier = Modifier.height(8.dp))
        when (label) {
            "Balance" -> Icon(
                painter = painterResource(id = R.drawable.cash_icon),
                contentDescription = "Balance Icon",
                tint = Color.Unspecified,
                modifier = Modifier.size(20.dp)
            )
            "Orderan" -> Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Orderan Icon",
                tint = Color.Green,
                modifier = Modifier.size(20.dp)
            )
            else -> {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OfflineConfirmationDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(24.dp)) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.motor_icon),
                    contentDescription = "Logo",
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Kamu mau offline?",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Mau istirahat dulu ya? 😴\nKalau offline, kamu gak bakal dapet order dulu.",
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onConfirm,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107)),
                    shape = RoundedCornerShape(50)
                ) {
                    Text("Offline dulu", color = Color.Black)
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(50),
                    border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp)
                ) {
                    Text("Tetap Online", color = Color.Black)
                }
            }
        }
    }
}