package com.example.jeksoed.ui.screens.driver

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.jeksoed.R
import com.example.jeksoed.navigation.Screen
import com.example.jeksoed.ui.screens.driver.components.RideRequestPopup
import com.example.jeksoed.ui.theme.JekSoedTheme
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.delay

@Composable
fun DriverHomeScreen(navController: NavController,viewModel: DriverHomeViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var isOnline by remember { mutableStateOf(true) }
    var showOfflineDialog by remember { mutableStateOf(false) }

    val unsoedLocation = LatLng(-7.431, 109.245)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(unsoedLocation, 15f)
    }

    LaunchedEffect(uiState.popupRideRequest) {
        if (uiState.popupRideRequest != null) {
            delay(3000) // Tunggu 3 detik
            viewModel.dismissPopup() // Hilangkan pop-up
        }
    }

    if (showOfflineDialog) {
        OfflineConfirmationDialog(
            onDismiss = { showOfflineDialog = false },
            onConfirm = {
                isOnline = false
                showOfflineDialog = false
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            uiSettings = com.google.maps.android.compose.MapUiSettings(zoomControlsEnabled = false)
        ) {
            Marker(
                state = MarkerState(position = unsoedLocation),
                title = "Lokasi Anda"
            )
        }

        if (uiState.rideRequests.isNotEmpty() && !isOnline) {
            LazyColumn(modifier = Modifier.align(Alignment.Center).fillMaxHeight(0.4f).background(Color.White)) {
                items(uiState.rideRequests) { request ->
                    Text("Pesanan dari: ${request.passengerId} - Status: ${request.status}", modifier = Modifier.padding(16.dp))
                }
            }
        }


        // --- POP-UP NOTIFIKASI ---
        AnimatedVisibility(
            visible = uiState.popupRideRequest != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            uiState.popupRideRequest?.let { request ->
                RideRequestPopup(
                    rideRequest = request,
                    onAccept = { rideId ->
                        viewModel.acceptRide(
                            rideId = rideId,
                            onSuccess = {
                                navController.navigate(Screen.Trip.createRoute(rideId))
                            },
                            onFailure = { error ->
                                Toast.makeText(context, "Gagal: ${error.message}", Toast.LENGTH_SHORT).show()
                            }
                        )
                    },
                    onReject = { rideId ->
                        viewModel.rejectRide(rideId)
                    }
                )
            }
        }

        StatusIndicator(
            isOnline = isOnline,
            modifier = Modifier.align(Alignment.TopCenter)
        )

        NotificationButton(
            onNotificationClick = { /*TODO*/ },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 16.dp, end = 16.dp)
        )

        DriverInfoCard(
            modifier = Modifier
                .align(Alignment.BottomCenter),
            onToggleStatus = {
                if (isOnline) {
                    showOfflineDialog = true
                } else {
                    isOnline = true
                }
            },
            isOnline = isOnline
        )
    }
}

@Composable
private fun StatusIndicator(isOnline: Boolean, modifier: Modifier = Modifier) {
    // --- PERUBAHAN WARNA KONDISIONAL ---
    val backgroundColor = if (isOnline) Color(0xFFFFC107) else Color.White
    val textColor = if (isOnline) Color.Black else Color.Gray

    Card(
        modifier = modifier.offset(y=24.dp),
        shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
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
    modifier: Modifier = Modifier,
    isOnline: Boolean,
    onToggleStatus: () -> Unit
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
                Image(
                    painter = painterResource(id = R.drawable.person_icon),
                    contentDescription = "Foto Profil",
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Fajar Nugros", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("R 6666 CA", color = Color.Gray, fontSize = 14.sp)
                }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        // --- PERUBAHAN WARNA KONDISIONAL ---
                        .background(if (isOnline) colorResource(R.color.unsoed) else Color.White)
                        .clickable { onToggleStatus() }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.power_icon),
                        contentDescription = "Toggle Status",
                        tint = if (isOnline) Color.Black else Color.Gray,
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
                    InfoItem("Balance", "Rp150.000,-")
                    InfoItem("Rating", "4.8")
                    InfoItem("Orderan", "5")
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

@Preview(showBackground = true, showSystemUi = true, name = "Halaman Home Driver")
@Composable
private fun DriverHomeScreenPreview() {
    JekSoedTheme {
        DriverHomeScreen(navController = rememberNavController())
    }
}

@Preview(name = "Info Card (Online)")
@Composable
private fun DriverInfoCardOnlinePreview() {
    JekSoedTheme {
        DriverInfoCard(isOnline = true, onToggleStatus = {})
    }
}
@Preview(name = "Info Card (Offline)")
@Composable
private fun DriverInfoCardOfflinePreview() {
    JekSoedTheme {
        DriverInfoCard(isOnline = false, onToggleStatus = {})
    }
}

@Preview(name = "Dialog Konfirmasi Offline", showBackground = true)
@Composable
private fun OfflineConfirmationDialogPreview() {
    JekSoedTheme {
        Card(
            modifier = Modifier.padding(16.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            // Preview kontennya, bukan AlertDialog-nya
        }
    }
}