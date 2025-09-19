// File: ui/trip/TripScreen.kt

package com.example.jeksoed.ui.trip

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.jeksoed.model.RideRequest // <-- Import data class
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.firebase.firestore.FirebaseFirestore
import com.google.maps.android.PolyUtil
import com.google.maps.android.compose.*

@Composable
fun TripScreen(navController: NavController, rideRequestId: String) {
    var rideRequest by remember { mutableStateOf<RideRequest?>(null) }
    val cameraPositionState = rememberCameraPositionState()

    DisposableEffect(rideRequestId) {
        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection("ride_requests").document(rideRequestId)

        val listener = docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w("TripScreen", "Listen failed.", e)
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                // Gunakan .toObject() yang lebih aman!
                rideRequest = snapshot.toObject(RideRequest::class.java)
            }
        }

        onDispose { listener.remove() }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState
        ) {
            rideRequest?.let { request ->
                // Gambar rute jika polyline ada
                request.encodedPolyline?.let {
                    val points = PolyUtil.decode(it)
                    Polyline(points = points, color = Color.Blue, width = 15f)
                }

                // Marker Jemput
                val pickupLatLng = LatLng(
                    request.pickupLocation["latitude"] ?: 0.0,
                    request.pickupLocation["longitude"] ?: 0.0
                )
                Marker(state = MarkerState(position = pickupLatLng), title = "Jemput di sini")

                // Marker Tujuan
                val destinationLatLng = LatLng(
                    request.destinationLocation["latitude"] ?: 0.0,
                    request.destinationLocation["longitude"] ?: 0.0
                )
                Marker(state = MarkerState(position = destinationLatLng), title = "Tujuan")
            }
        }

        // Efek untuk menyesuaikan kamera
        LaunchedEffect(rideRequest) {
            rideRequest?.let { request ->
                if (request.encodedPolyline != null) {
                    val pickupLatLng = LatLng(request.pickupLocation["latitude"]!!, request.pickupLocation["longitude"]!!)
                    val destinationLatLng = LatLng(request.destinationLocation["latitude"]!!, request.destinationLocation["longitude"]!!)
                    val bounds = LatLngBounds.builder()
                        .include(pickupLatLng)
                        .include(destinationLatLng)
                        .build()
                    cameraPositionState.animate(
                        com.google.android.gms.maps.CameraUpdateFactory.newLatLngBounds(bounds, 150)
                    )
                }
            }
        }

        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Status Perjalanan: ${rideRequest?.status?.uppercase() ?: "MEMUAT..."}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}