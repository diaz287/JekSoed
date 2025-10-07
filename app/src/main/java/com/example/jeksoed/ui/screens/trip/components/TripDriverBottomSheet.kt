package com.example.jeksoed.ui.screens.trip.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.jeksoed.R
import com.example.jeksoed.data.model.User
import com.example.jeksoed.ui.screens.trip.TripUiState

@Composable
fun TripDriverBottomSheet(
    uiState: TripUiState,
    onUpdateStatus: (String) -> Unit,
    onCancelTrip: () -> Unit,
    onChatClick: () -> Unit
) {
    var showCancelDialog by remember { mutableStateOf(false) }
    val rideRequest = uiState.rideRequest ?: return // Jangan render jika data belum ada
    val passenger = uiState.otherUser // Mengambil data penumpang dari uiState

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // --- Bagian Info Penumpang & Rute (Sekarang menggunakan data dinamis) ---
            PassengerInfo(
                passenger = passenger,
                onChatClick = onChatClick
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            RouteDisplay(
                pickupName = rideRequest.pickupName ?: "Lokasi Jemput",
                destinationName = rideRequest.destinationName ?: "Lokasi Tujuan"
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // --- Bagian Aksi (menggunakan harga dinamis) ---
            when (rideRequest.status) {
                "accepted" -> StatusContent(
                    totalPayment = rideRequest.price ?: "Rp0",
                    buttonText = "Sudah sampai di lokasi jemput",
                    onSlideConfirmed = { onUpdateStatus("arrived") }
                )
                "arrived" -> StatusContent(
                    totalPayment = rideRequest.price ?: "Rp0",
                    buttonText = "Mulai Perjalanan",
                    onSlideConfirmed = { onUpdateStatus("started") }
                )
                "started" -> StatusContent(
                    totalPayment = rideRequest.price ?: "Rp0",
                    buttonText = "Selesaikan Perjalanan",
                    onSlideConfirmed = { onUpdateStatus("completed") }
                )
            }
            if (rideRequest.status != "completed") {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Batalkan Pesanan?",
                    color = Color.Gray,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .clickable { showCancelDialog = true }
                        .padding(8.dp)
                )
            }
        }
    }

    if (showCancelDialog) {
        CancelTripDialog(
            onDismiss = { showCancelDialog = false },
            onConfirm = {
                showCancelDialog = false
                onCancelTrip()
            }
        )
    }
}

// --- Komponen-komponen Kecil yang Sudah Diperbaiki ---

@Composable
private fun PassengerInfo(passenger: User?, onChatClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        AsyncImage(
            model = passenger?.photoUrl,
            contentDescription = "Foto Penumpang",
            placeholder = painterResource(id = R.drawable.person_icon),
            error = painterResource(id = R.drawable.person_icon),
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(40.dp).clip(CircleShape)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = passenger?.nama ?: "Memuat...", // Menampilkan nama penumpang dari data
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        IconButton(
            onClick = onChatClick,
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = Color(0xFFFFC107)
            )
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

@Composable
private fun StatusContent(
    totalPayment: String,
    buttonText: String,
    onSlideConfirmed: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Total pembayaran", color = Color.Gray)
            Text(totalPayment, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(16.dp))

        SlideToConfirmButton(
            text = buttonText,
            onConfirmed = onSlideConfirmed
        )
    }
}

@Composable
fun CancelTripDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Batalkan Perjalanan?") },
        text = { Text("Anda yakin ingin membatalkan perjalanan ini? Penumpang akan diberitahu.") },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
            ) {
                Text("Ya, Batalkan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Lanjut")
            }
        }
    )
}