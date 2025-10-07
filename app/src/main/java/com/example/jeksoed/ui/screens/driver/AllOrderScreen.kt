package com.example.jeksoed.ui.screens.driver

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.jeksoed.R
import com.example.jeksoed.data.model.RideRequest
import com.example.jeksoed.navigation.Screen
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

// Data class untuk menampung info penumpang
data class PassengerInfo(
    val name: String = "Penumpang",
    val photoUrl: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllOrdersScreen(navController: NavController, viewModel: DriverHomeViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Daftar Orderan Masuk") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.rideRequests.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Belum ada orderan yang masuk.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(uiState.rideRequests) { request ->
                    RideRequestCard(
                        rideRequest = request,
                        onAccept = { rideId ->
                            viewModel.acceptRide(
                                rideId = rideId,
                                onSuccess = {
                                    Toast.makeText(context, "Orderan diterima!", Toast.LENGTH_SHORT).show()
                                    navController.navigate(Screen.Trip.createRoute(rideId)) {
                                        popUpTo(Screen.AllOrders.route) { inclusive = true }
                                    }
                                },
                                onFailure = { error ->
                                    Toast.makeText(context, "Gagal: ${error.message}", Toast.LENGTH_SHORT).show()
                                }
                            )
                        },
                        onReject = { rideId ->
                            viewModel.rejectRide(rideId)
                            Toast.makeText(context, "Orderan ditolak", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun RideRequestCard(
    rideRequest: RideRequest,
    onAccept: (String) -> Unit,
    onReject: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Mengambil info lengkap penumpang (nama & foto) dari Firestore
    val passengerInfo by produceState(initialValue = PassengerInfo(), rideRequest.passengerId) {
        value = getPassengerInfo(rideRequest.passengerId)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Gunakan AsyncImage dari Coil untuk memuat foto profil
                AsyncImage(
                    model = passengerInfo.photoUrl, // <-- DIUBAH DARI profilePictureUrl
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
                    RouteInfoRow(
                        iconRes = R.drawable.blue_icon,
                        location = rideRequest.pickupName ?: "Lokasi Jemput"
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp))
                    RouteInfoRow(
                        iconRes = R.drawable.locatio_icon,
                        location = rideRequest.destinationName ?: "Lokasi Tujuan"
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${rideRequest.distance} • ${rideRequest.duration}",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
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

/**
 * Mengambil nama dan URL foto profil penumpang dari koleksi 'users' di Firestore.
 */
private suspend fun getPassengerInfo(passengerId: String): PassengerInfo {
    if (passengerId.isBlank()) return PassengerInfo()
    return try {
        val document = Firebase.firestore.collection("users").document(passengerId).get().await()
        val name = document.getString("nama") ?: "Penumpang"
        val photoUrl = document.getString("photoUrl") // <-- DIUBAH DARI profilePictureUrl
        PassengerInfo(name, photoUrl)
    } catch (e: Exception) {
        PassengerInfo()
    }
}