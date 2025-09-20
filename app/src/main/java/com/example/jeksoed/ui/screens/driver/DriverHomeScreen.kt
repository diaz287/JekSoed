package com.example.jeksoed.ui.screens.driver

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.jeksoed.data.model.RideRequest
import com.example.jeksoed.navigation.Screen
import com.example.jeksoed.ui.theme.JekSoedTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query


/**
 * SMART COMPOSABLE
 * - Mengelola semua state (isLoading, rideRequests, acceptingRideId).
 * - Menangani semua logika dan side-effect (Listener Firestore, update data, logout, Toast).
 */
@Composable
fun DriverHomeScreen(
    navController: NavController,
    auth: FirebaseAuth = FirebaseAuth.getInstance(),
    firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    val context = LocalContext.current
    var rideRequests by remember { mutableStateOf<List<RideRequest>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var acceptingRideId by remember { mutableStateOf<String?>(null) } // State untuk tahu orderan mana yg sedang di-accept

    // Listener untuk mengambil daftar orderan "pending" secara real-time
    DisposableEffect(Unit) {
        val listener = firestore.collection("ride_requests")
            .whereEqualTo("status", "pending")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                isLoading = false
                if (e != null) {
                    Log.w("DriverHome", "Listen failed.", e)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    rideRequests = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(RideRequest::class.java)?.copy(id = doc.id)
                    }
                }
            }
        onDispose { listener.remove() }
    }

    DriverHomeScreenUI(
        rideRequests = rideRequests,
        isLoading = isLoading,
        acceptingRideId = acceptingRideId,
        onAcceptRide = { rideId ->
            acceptingRideId = rideId
            val driverId = auth.currentUser?.uid
            firestore.collection("ride_requests").document(rideId)
                .update(mapOf("status" to "accepted", "driverId" to driverId))
                .addOnSuccessListener {
                    acceptingRideId = null
                    Toast.makeText(context, "Orderan berhasil diambil!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    acceptingRideId = null
                    Toast.makeText(context, "Gagal mengambil orderan: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        },
        onLogoutClick = {
            auth.signOut()
            navController.navigate(Screen.Login.route) {
                popUpTo(Screen.DriverHome.route) { inclusive = true }
            }
        },
        navController = navController
    )
}

/**
 * DUMB UI COMPOSABLE
 * - Hanya menampilkan data yang diberikan.
 * - Meneruskan semua aksi pengguna ke atas melalui lambda.
 * - Mudah di-preview.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverHomeScreenUI(
    rideRequests: List<RideRequest>,
    isLoading: Boolean,
    acceptingRideId: String?,
    onAcceptRide: (rideId: String) -> Unit,
    onLogoutClick: () -> Unit,
    navController: NavController
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Orderan Tersedia") },
                actions = {
                    Button(onClick = onLogoutClick) {
                        Text("Logout")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (rideRequests.isEmpty()) {
                Text("Belum ada orderan tersedia.", modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(rideRequests) { request ->
                        RideRequestCard(
                            rideRequest = request,
                            isAccepting = (acceptingRideId == request.id),
                            onAcceptClick = { onAcceptRide(request.id) },
                            navController = navController
                        )
                    }
                }
            }
        }
    }
}

/**
 * DUMB COMPONENT CARD
 * - Hanya menampilkan data satu orderan.
 * - Tidak punya logika internal.
 */
@Composable
fun RideRequestCard(
    rideRequest: RideRequest,
    isAccepting: Boolean,
    onAcceptClick: () -> Unit,
    navController: NavController
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Orderan Baru", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Jarak: ${rideRequest.distance}")
                Text("Waktu: ${rideRequest.duration}")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onAcceptClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isAccepting
            ) {
                if (isAccepting) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Ambil Orderan")
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
private fun DriverHomeScreenPreview() {
    val dummyRequests = listOf(
        RideRequest(id = "1", distance = "5.2 km", duration = "15 min"),
        RideRequest(id = "2", distance = "3.1 km", duration = "8 min"),
    )
    JekSoedTheme {
        DriverHomeScreenUI(
            rideRequests = dummyRequests,
            isLoading = false,
            acceptingRideId = "2", // Contoh jika orderan kedua sedang di-accept
            onAcceptRide = {},
            onLogoutClick = {},
            navController = NavController(LocalContext.current)
        )
    }
}