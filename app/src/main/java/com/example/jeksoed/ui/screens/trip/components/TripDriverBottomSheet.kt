// main/java/com/example/jeksoed/ui/screens/trip/components/TripDriverBottomSheet.kt

package com.example.jeksoed.ui.screens.trip.components

import androidx.compose.foundation.Image
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jeksoed.R
import com.example.jeksoed.data.model.RideRequest
import com.example.jeksoed.ui.screens.trip.TripUiState
import com.example.jeksoed.ui.theme.JekSoedTheme
import com.example.jeksoed.utils.formatCurrency


@Composable
fun TripDriverBottomSheet(
    uiState: TripUiState,
    onUpdateStatus: (String) -> Unit,
    onCancelTrip: () -> Unit,
    onChatClick: () -> Unit
) {
    var showCancelDialog by remember { mutableStateOf(false) }
    val rideRequest = uiState.rideRequest ?: return // Jangan render jika data belum ada

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // --- Bagian Info Penumpang & Rute ---
            PassengerInfo(onChatClick = onChatClick)
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            RouteDisplay()
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // --- Bagian Aksi (berubah sesuai status) ---
            when (rideRequest.status) {
                "accepted" -> StatusContent(
                    totalPayment = formatCurrency(10000), // Ganti dengan harga asli
                    buttonText = "Geser jika sudah sampai",
                    onSlideConfirmed = { onUpdateStatus("arrived") }
                )
                "arrived" -> StatusContent(
                    totalPayment = formatCurrency(10000), // Ganti dengan harga asli
                    buttonText = "Geser untuk memulai perjalanan",
                    onSlideConfirmed = { onUpdateStatus("started") }
                )
                "started" -> StatusContent(
                    totalPayment = formatCurrency(10000), // Ganti dengan harga asli
                    buttonText = "Geser jika sudah sampai tujuan",
                    onSlideConfirmed = { onUpdateStatus("completed") }
                )
            }
            if (rideRequest.status != "completed") {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Mau dibatalin?",
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

// --- Komponen-komponen Kecil untuk Bottom Sheet ---

@Composable
private fun PassengerInfo(onChatClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Image(
            painter = painterResource(id = R.drawable.person_icon),
            contentDescription = "Foto Penumpang",
            modifier = Modifier.size(40.dp).clip(CircleShape)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "Imedia Sholem", // Ganti dengan nama penumpang asli
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onChatClick,
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = Color(0xFFFFC107)
            )
        ) {
            Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "Chat", tint = Color.Black)
        }
    }
}

@Composable
private fun RouteDisplay() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        RouteRow(iconRes = R.drawable.blue_icon, location = "FK Unsoed") // Ganti data asli
        RouteRow(iconRes = R.drawable.locatio_icon, location = "Rumah Sakit Wiradadi") // Ganti data asli
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

@Composable
private fun StatusContent(
    totalPayment: String,
    buttonText: String,
    onSlideConfirmed: () -> Unit // Callback diubah namanya
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
        // Ganti Button dengan SlideToConfirmButton
        SlideToConfirmButton(
            text = buttonText,
            onConfirmed = onSlideConfirmed
        )
    }
}
