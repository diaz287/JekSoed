package com.example.jeksoed.ui.screens.activity

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.jeksoed.R
import com.example.jeksoed.data.model.RideRequest
import com.example.jeksoed.data.model.User
import com.example.jeksoed.navigation.Screen
import com.example.jeksoed.ui.theme.JekSoedTheme
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.*
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityDetailScreen(
    navController: NavController,
    rideRequestId: String,
    viewModel: ActivityDetailViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detail") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.rideRequest == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Gagal memuat detail perjalanan.")
            }
        } else {
            ActivityDetailScreenUI(
                uiState = uiState,
                formattedRating = viewModel.getFormattedRating(uiState.otherUser),
                modifier = Modifier.padding(padding),
                onChatClick = {
                    navController.navigate(Screen.Chat.createRoute(rideRequestId))
                }
            )
        }
    }
}

@Composable
fun ActivityDetailScreenUI(
    uiState: ActivityDetailUiState,
    formattedRating: String,
    modifier: Modifier = Modifier,
    onChatClick: () -> Unit
) {
    val cameraPositionState = rememberCameraPositionState()

    // Efek untuk menyesuaikan kamera agar rute terlihat
    LaunchedEffect(uiState.polylinePoints) {
        if (uiState.polylinePoints.isNotEmpty()) {
            val boundsBuilder = LatLngBounds.builder()
            uiState.polylinePoints.forEach { boundsBuilder.include(it) }
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 100) // 100 adalah padding
            )
        }
    }

    Column(modifier = modifier.verticalScroll(rememberScrollState())) {
        // --- PERBAIKAN: Ganti Box dengan GoogleMap ---
        if (uiState.polylinePoints.isNotEmpty()) {
            GoogleMap(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                cameraPositionState = cameraPositionState,
                uiSettings = MapUiSettings(zoomControlsEnabled = false, scrollGesturesEnabled = false, zoomGesturesEnabled = false)
            ) {
                // Gambar rute di peta
                Polyline(points = uiState.polylinePoints, color = MaterialTheme.colorScheme.primary, width = 15f)

                // Marker Jemput
                uiState.rideRequest?.pickupLocation?.let {
                    val lat = it["latitude"] ?: 0.0
                    val lng = it["longitude"] ?: 0.0
                    Marker(state = MarkerState(position = LatLng(lat, lng)), title = "Jemput")
                }

                // Marker Tujuan
                uiState.rideRequest?.destinationLocation?.let {
                    val lat = it["latitude"] ?: 0.0
                    val lng = it["longitude"] ?: 0.0
                    Marker(state = MarkerState(position = LatLng(lat, lng)), title = "Tujuan")
                }
            }
        } else {
            // Fallback jika tidak ada data rute
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Data rute tidak tersedia", color = Color.Gray)
            }
        }

        DetailSheet(
            uiState = uiState,
            formattedRating = formattedRating,
            isDriverView = uiState.isDriver,
            onChatClick = onChatClick
        )
    }
}


@Composable
fun DetailSheet(
    uiState: ActivityDetailUiState,
    isDriverView: Boolean,
    formattedRating: String,
    onChatClick: () -> Unit
) {
    val ride = uiState.rideRequest!!

    val formattedDate = ride.createdAt?.let { SimpleDateFormat("d MMMM yyyy", Locale("id", "ID")).format(it.toDate()) } ?: "-"
    val formattedStartTime = ride.createdAt?.let { SimpleDateFormat("HH:mm", Locale("id", "ID")).format(it.toDate()) } ?: "-"
    val formattedEndTime = ride.completedAt?.let { SimpleDateFormat("HH:mm", Locale("id", "ID")).format(it.toDate()) } ?: formattedStartTime

    Column(modifier = Modifier.padding(16.dp)) {
        if (isDriverView) {
            UserInfoRow(user = uiState.otherUser, isChatEnabled = uiState.isChatEnabled, onChatClick = onChatClick)
        } else {
            DriverInfoRow(
                driver = uiState.otherUser,
                rating = formattedRating,
                isChatEnabled = uiState.isChatEnabled,
                onChatClick = onChatClick
            )
        }
        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

        RouteRow(iconRes = R.drawable.blue_icon, location = ride.pickupName ?: "Lokasi Jemput")
        Spacer(modifier = Modifier.height(8.dp))
        RouteRow(iconRes = R.drawable.locatio_icon, location = ride.destinationName ?: "Lokasi Tujuan")
        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            TimeColumn("Waktu Berangkat", formattedStartTime)
            TimeColumn("Waktu Tiba", formattedEndTime)
        }
        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Tanggal", color = Color.Gray)
            Text(formattedDate, fontWeight = FontWeight.SemiBold)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Total Pembayaran", color = Color.Gray)
            Text(ride.price ?: "Rp0", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("* Fitur chat dan telepon hanya tersedia hingga 30 menit setelah perjalanan selesai.", color = Color.Gray, style = MaterialTheme.typography.bodySmall)

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

        // --- PERBAIKAN: Tampilkan rating secara dinamis ---
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            val rating = ride.rating ?: 0
            Text(
                text = if (isDriverView) "Rating dari penumpang" else "Rating dari kamu",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (rating > 0) {
                Row {
                    repeat(5) { index ->
                        Icon(
                            imageVector = if (index < rating) Icons.Filled.Star else Icons.Outlined.StarBorder,
                            contentDescription = null,
                            tint = if (index < rating) Color(0xFFFFC107) else Color.Gray,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            } else {
                Text("Belum ada rating", color = Color.Gray)
            }
        }
    }
}

@Composable
fun TimeColumn(label: String, time: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = Color.Gray)
        Text(time, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun RouteRow(iconRes: Int, location: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = Color.Unspecified
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(location)
    }
}

@Composable
fun UserInfoRow(user: User?, isChatEnabled: Boolean, onChatClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        AsyncImage(
            model = user?.photoUrl,
            contentDescription = "Foto",
            placeholder = painterResource(id = R.drawable.person_icon),
            error = painterResource(id = R.drawable.person_icon),
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(48.dp).clip(CircleShape)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(user?.nama ?: "Pengguna", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        IconButton(onClick = onChatClick, enabled = isChatEnabled) {
            Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "Chat")
        }
    }
}

@Composable
fun DriverInfoRow(driver: User?, rating: String, isChatEnabled: Boolean, onChatClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        AsyncImage(
            model = driver?.photoUrl,
            contentDescription = "Foto",
            placeholder = painterResource(id = R.drawable.person_icon),
            error = painterResource(id = R.drawable.person_icon),
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(48.dp).clip(CircleShape)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(driver?.nama ?: "Driver", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(driver?.licensePlate ?: "...", color = Color.Gray)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(rating, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.Default.Star, contentDescription = "Rating", tint = Color(0xFFFFC107), modifier = Modifier.size(16.dp))
            }
        }
        IconButton(onClick = onChatClick, enabled = isChatEnabled) {
            Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "Chat")
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Preview(showSystemUi = true, name = "Detail (Tampilan Penumpang)")
@Composable
private fun ActivityDetailScreenPassengerPreview() {
    JekSoedTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Detail") },
                    navigationIcon = {
                        IconButton(onClick = { }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                        }
                    }
                )
            }
        ) { padding ->
            ActivityDetailScreenUI(
                uiState = ActivityDetailUiState(
                    isLoading = false,
                    rideRequest = RideRequest(
                        pickupName = "Fakultas Kedokteran",
                        destinationName = "RS Margono",
                        price = "Rp12.000",
                        createdAt = Timestamp.now()
                    ),
                    otherUser = User(nama = "Fajar Nugros", licensePlate = "R 1234 AB"),
                    isDriver = false
                ),
                formattedRating = "4.8",
                modifier = Modifier.padding(padding),
                onChatClick = {}
            )
        }
    }
}