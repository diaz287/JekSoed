// main/java/com/example/jeksoed/ui/screens/driver/components/RideRequestPopup.kt

package com.example.jeksoed.ui.screens.driver.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jeksoed.R
import com.example.jeksoed.data.model.RideRequest
import com.example.jeksoed.ui.theme.JekSoedTheme
import com.google.firebase.Timestamp

@Composable
fun RideRequestPopup(
    rideRequest: RideRequest,
    onAccept: (String) -> Unit,
    onReject: (String) -> Unit,
    modifier: Modifier = Modifier
) {
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
                // Ganti dengan gambar profil penumpang jika ada
                Image(
                    painter = painterResource(id = R.drawable.person_icon),
                    contentDescription = "Foto Penumpang",
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    // Ganti dengan nama penumpang jika ada
                    Text("Imedia Sholem", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    // Rute
                    RouteInfoRow(iconRes = R.drawable.blue_icon, location = "FK Unsoed") // Ganti dengan data asli
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp))
                    RouteInfoRow(iconRes = R.drawable.locatio_icon, location = "Rumah Sakit Wiradadi") // Ganti dengan data asli
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bagian Tombol Aksi
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = { onReject(rideRequest.id) },
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
                    onClick = { onAccept(rideRequest.id) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDFF9E9)),
                    shape = RoundedCornerShape(50)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.check), // Pastikan Anda punya drawable 'check'
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

@Preview(name = "Ride Request Notification Popup", showBackground = true)
@Composable
private fun RideRequestPopupPreview() {
    // Kita buat data palsu (dummy) untuk ditampilkan di preview
    val dummyRideRequest = RideRequest(
        id = "dummy123",
        passengerId = "passengerXYZ",
        status = "pending",
        createdAt = Timestamp.now()
        // Anda bisa menambahkan data lokasi palsu jika diperlukan
    )

    JekSoedTheme {
        RideRequestPopup(
            rideRequest = dummyRideRequest,
            onAccept = { },
            onReject = { }
        )
    }
}