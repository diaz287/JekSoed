// main/java/com/example/jeksoed/ui/screens/trip/components/TripPassengerSheet.kt

package com.example.jeksoed.ui.screens.trip.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jeksoed.R
import com.example.jeksoed.ui.components.PrimaryButton
import com.example.jeksoed.ui.screens.trip.TripUiState
import com.example.jeksoed.ui.theme.JekSoedTheme
import com.example.jeksoed.utils.formatCurrency

@Composable
fun TripPassengerSheet(
    uiState: TripUiState,
    onCancelTrip: () -> Unit,
    onChatClick: () -> Unit
) {
    val rideRequest = uiState.rideRequest ?: return
    val driver = uiState.otherUser
    val isCancelDisabled = rideRequest.status == "started"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Status Perjalanan
            val statusText = when (rideRequest.status) {
                "accepted" -> "Driver menuju lokasimu"
                "arrived" -> "Driver telah sampai"
                "started" -> "Kalian sedang menuju tujuan"
                else -> "Memuat status..."
            }
            Text(statusText, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            // Info Driver
            DriverInfo(
                name = driver?.nama ?: "Memuat...",
                plate = driver?.licensePlate ?: "Belum diatur",
                onChatClick = onChatClick
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // Rute
            RouteDisplay()
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // Pembayaran
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total pembayaran", color = Color.Gray)
                Text(formatCurrency(10000), fontWeight = FontWeight.Bold) // Ganti harga asli
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Tombol Batal
            PrimaryButton(
                text = "Batal Pesan?",
                onClick = onCancelTrip,
                isEnabled = !isCancelDisabled,
                containerColor = Color(0xFFFFC107),
                contentColor = Color.Black
            )
        }
    }
}

@Composable
private fun DriverInfo(name: String, plate: String, onChatClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Image(
            painter = painterResource(id = R.drawable.person_icon),
            contentDescription = "Foto Driver",
            modifier = Modifier.size(48.dp).clip(CircleShape)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(plate, color = Color.Gray, fontSize = 14.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("4.8", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Icon(Icons.Default.Star, contentDescription = "Rating", tint = Color(0xFFFFC107), modifier = Modifier.size(16.dp))
            }
        }
        IconButton(
            onClick = onChatClick,
            colors = IconButtonDefaults.iconButtonColors(containerColor = Color(0xFFFFC107))
        ) {
            Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "Chat", tint = Color.Black)
        }
    }
}

// Anda bisa menggunakan kembali RouteDisplay dari TripDriverBottomSheet.kt
@Composable
private fun RouteDisplay() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        RouteRow(iconRes = R.drawable.blue_icon, location = "FK Unsoed")
        RouteRow(iconRes = R.drawable.locatio_icon, location = "Rumah Sakit Wiradadi")
    }
}

@Composable
private fun RouteRow(iconRes: Int, location: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(painter = painterResource(id = iconRes), contentDescription = null, tint = Color.Unspecified)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = location, fontSize = 14.sp)
    }
}


@Preview(showBackground = true)
@Composable
private fun TripPassengerSheetPreview() {
    JekSoedTheme {
        TripPassengerSheet(
            uiState = TripUiState(rideRequest = com.example.jeksoed.data.model.RideRequest(status = "accepted")),
            onCancelTrip = {},
            onChatClick = {}
        )
    }
}