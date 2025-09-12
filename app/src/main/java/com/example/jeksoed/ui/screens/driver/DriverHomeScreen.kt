package com.example.jeksoed.ui.screens.driver

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.jeksoed.navigation.Screen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.Timestamp
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

data class RideRequest(
    val id: String = "", // ID Dokumen Firestore
    val passengerId: String = "",
    val pickupLocation: Map<String, Double> = emptyMap(),
    val destinationLocation: Map<String, Double> = emptyMap(),
    val distance: String = "",
    val duration: String = "",
    val status: String = "",
    val createdAt: Timestamp? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverHomeScreen(navController: NavController) {
    val context = LocalContext.current
    var rideRequests by remember { mutableStateOf<List<RideRequest>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Listener untuk mengambil daftar orderan "pending" secara real-time
    DisposableEffect(Unit) {
        val db = FirebaseFirestore.getInstance()
        val listener = db.collection("ride_requests")
            .whereEqualTo("status", "pending")
            .orderBy("createdAt", Query.Direction.DESCENDING) // Tampilkan yang terbaru di atas
            .addSnapshotListener { snapshot, e ->
                isLoading = false
                if (e != null) {
                    Log.w("DriverHome", "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val requests = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(RideRequest::class.java)?.copy(id = doc.id)
                    }
                    rideRequests = requests
                }
            }

        // Hentikan listener saat layar ditinggalkan
        onDispose {
            listener.remove()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Orderan Tersedia") },
                actions = {
                    Button(onClick = {
                        FirebaseAuth.getInstance().signOut()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.DriverHome.route) { inclusive = true }
                        }
                    }) {
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
                        RideRequestCard(rideRequest = request)
                    }
                }
            }
        }
    }
}

@Composable
fun RideRequestCard(rideRequest: RideRequest) {
    val context = LocalContext.current
    var isAccepting by remember { mutableStateOf(false) }

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
                onClick = {
                    isAccepting = true
                    val db = FirebaseFirestore.getInstance()
                    val driverId = FirebaseAuth.getInstance().currentUser?.uid

                    db.collection("ride_requests").document(rideRequest.id)
                        .update(mapOf(
                            "status" to "accepted",
                            "driverId" to driverId
                        ))
                        .addOnSuccessListener {
                            isAccepting = false
                            Toast.makeText(context, "Orderan berhasil diambil!", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            isAccepting = false
                            Toast.makeText(context, "Gagal mengambil orderan: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                },
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