package com.example.jeksoed.ui.screens.driver

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.jeksoed.R
import com.example.jeksoed.ui.theme.JekSoedTheme
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun DriverHomeScreen(navController: NavController) {
    var isOnline by remember { mutableStateOf(true) }
    var showOfflineDialog by remember { mutableStateOf(false) }

    val unsoedLocation = LatLng(-7.431, 109.245)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(unsoedLocation, 15f)
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
        // Peta di latar belakang
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

        // Kontrol di bagian atas (Status & Notifikasi)
        TopControls(
            isOnline = isOnline,
            onNotificationClick = { /*TODO*/ },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp)
        )

        // Kartu Informasi Driver di bagian bawah
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
private fun TopControls(
    isOnline: Boolean,
    onNotificationClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = { },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107)),
            shape = RoundedCornerShape(50)
        ) {
            Text(if (isOnline) "Kamu Online" else "Kamu Offline", color = Color.Black, fontWeight = FontWeight.Bold)
        }
        IconButton(
            onClick = onNotificationClick,
            modifier = Modifier
                .clip(CircleShape)
                .background(Color.White)
        ) {
            Icon(Icons.Default.Notifications, contentDescription = "Notifikasi")
        }
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
            // Baris Profil & Tombol Power
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
                IconButton(onClick = onToggleStatus) {
                    Icon(
                        painter = painterResource(id = R.drawable.power_icon), // Pastikan ada drawable power_icon
                        contentDescription = "Toggle Status",
                        tint = if(isOnline) Color(0xFFFFC107) else Color.Gray,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            // Kartu Detail (Balance, Rating, Orderan)
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
    }
}

@Composable
private fun OfflineConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        icon = {
            Image(
                painter = painterResource(id = R.drawable.motor_icon), // Ganti dengan logo Jeksoed
                contentDescription = "Logo",
                modifier = Modifier.size(40.dp)
            )
        },
        title = { Text("Kamu mau offline?", fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
        text = {
            Text(
                "Mau istirahat dulu ya? 😴\nKalau offline, kamu gak bakal dapet order dulu.",
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                lineHeight = 20.sp
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107)),
                shape = RoundedCornerShape(50)
            ) {
                Text("Offline dulu", color = Color.Black)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(50),
                border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp)
            ) {
                Text("Tetap Online", color = Color.Black)
            }
        }
    )
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

@Preview(name = "Dialog Konfirmasi Offline")
@Composable
private fun OfflineConfirmationDialogPreview() {
    JekSoedTheme {
        OfflineConfirmationDialog(onDismiss = {}, onConfirm = {})
    }
}