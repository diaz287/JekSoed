package com.example.jeksoed.ui.screens.driver.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.jeksoed.R
import com.example.jeksoed.data.model.RideRequest
import com.example.jeksoed.ui.screens.driver.PassengerInfo // Mengimpor dari AllOrdersScreen
import com.example.jeksoed.ui.theme.JekSoedTheme
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

// --- DATA CLASS DUPLIKAT DIHAPUS DARI SINI ---

@Composable
fun RideRequestPopup(
    rideRequest: RideRequest,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    modifier: Modifier = Modifier
) {
    val passengerInfo by produceState(initialValue = PassengerInfo(), rideRequest.passengerId) {
        value = getPassengerInfo(rideRequest.passengerId)
    }
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Bagian Info Penumpang dan Rute
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = passengerInfo.photoUrl,
                    contentDescription = "Foto Penumpang",
                    placeholder = painterResource(id = R.drawable.person_icon),
                    error = painterResource(id = R.drawable.person_icon),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(passengerInfo.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    // --- PERBAIKAN: Gunakan data dinamis ---
                    RouteInfoRow(iconRes = R.drawable.blue_icon, location = rideRequest.pickupName ?: "Lokasi Jemput")
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp))
                    RouteInfoRow(iconRes = R.drawable.locatio_icon, location = rideRequest.destinationName ?: "Lokasi Tujuan")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bagian Tombol Aksi
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = onReject,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFDE0E0)),
                    shape = RoundedCornerShape(50)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.clear_icon),
                        contentDescription = "Tolak",
                        tint = Color.Red
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Tolak", color = Color.Red)
                }
                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = onAccept,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDFF9E9)),
                    shape = RoundedCornerShape(50)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.check),
                        contentDescription = "Terima",
                        tint = Color(0xFF219800)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Terima", color = Color(0xFF219800))
                }
            }
        }
    }
}

@Composable
private fun RouteInfoRow(iconRes: Int, location: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = Color.Unspecified
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = location,
            fontSize = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private suspend fun getPassengerInfo(passengerId: String): PassengerInfo {
    if (passengerId.isBlank()) return PassengerInfo()
    return try {
        val document = Firebase.firestore.collection("users").document(passengerId).get().await()
        val name = document.getString("nama") ?: "Penumpang"
        val photoUrl = document.getString("photoUrl")
        PassengerInfo(name, photoUrl)
    } catch (e: Exception) {
        PassengerInfo()
    }
}

@Preview(name = "Ride Request Notification Popup", showBackground = true)
@Composable
private fun RideRequestPopupPreview() {
    val dummyRideRequest = RideRequest(
        id = "dummy123",
        passengerId = "passengerXYZ",
        status = "pending",
        createdAt = Timestamp.now(),
        pickupName = "Fakultas Kedokteran",
        destinationName = "GOR Satria"
    )

    JekSoedTheme {
        RideRequestPopup(
            rideRequest = dummyRideRequest,
            onAccept = { },
            onReject = { }
        )
    }
}