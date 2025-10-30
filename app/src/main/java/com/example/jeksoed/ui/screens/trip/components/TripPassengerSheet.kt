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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.jeksoed.R
import com.example.jeksoed.data.model.User
import com.example.jeksoed.ui.components.PrimaryButton
import com.example.jeksoed.ui.screens.trip.TripUiState
import com.example.jeksoed.ui.theme.JekSoedTheme
import java.text.DecimalFormat

@Composable
fun TripPassengerSheet(
    uiState: TripUiState,
    onCancelTrip: () -> Unit,
    onChatClick: () -> Unit
) {
    val rideRequest = uiState.rideRequest ?: return
    val driver = uiState.otherUser
    // Tombol batal nonaktif jika perjalanan sudah dimulai
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
                "started" -> "Kalian sedang dalam perjalanan"
                else -> "Memuat status..."
            }
            Text(statusText, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            // Info Driver (sekarang dinamis)
            DriverInfo(
                driver = driver,
                onChatClick = onChatClick
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // Rute (sekarang dinamis)
            RouteDisplay(
                pickupName = rideRequest.pickupName ?: "Lokasi Jemput",
                destinationName = rideRequest.destinationName ?: "Lokasi Tujuan"
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // Pembayaran (sekarang dinamis)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total pembayaran", color = Color.Gray)
                Text(rideRequest.price ?: "Rp0", fontWeight = FontWeight.Bold)
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
private fun DriverInfo(driver: User?, onChatClick: () -> Unit) {
    // Hitung rata-rata rating driver
    val averageRating = if (driver != null && driver.ratingCount > 0) {
        val avg = driver.totalRating.toDouble() / driver.ratingCount.toDouble()
        DecimalFormat("#.#").format(avg)
    } else {
        "0.0"
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        AsyncImage(
            model = driver?.photoUrl,
            contentDescription = "Foto Driver",
            placeholder = painterResource(id = R.drawable.person_icon),
            error = painterResource(id = R.drawable.person_icon),
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(48.dp).clip(CircleShape)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(driver?.nama ?: "Memuat...", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(driver?.licensePlate ?: "...", color = Color.Gray, fontSize = 14.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(averageRating, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Spacer(modifier = Modifier.width(4.dp))
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

@Composable
private fun RouteDisplay(pickupName: String, destinationName: String) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        RouteRow(iconRes = R.drawable.blue_icon, location = pickupName)
        RouteRow(iconRes = R.drawable.locatio_icon, location = destinationName)
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
    val fakeDriver = User(
        nama = "Fajar Nugros",
        licensePlate = "R 6666 CA",
        totalRating = 48,
        ratingCount = 10
    )
    val fakeRideRequest = com.example.jeksoed.data.model.RideRequest(
        status = "accepted",
        price = "Rp25.000",
        pickupName = "Fakultas Kedokteran UNSOED",
        destinationName = "RSUD Margono Soekarjo"
    )
    JekSoedTheme {
        TripPassengerSheet(
            uiState = TripUiState(rideRequest = fakeRideRequest, otherUser = fakeDriver),
            onCancelTrip = {},
            onChatClick = {}
        )
    }
}