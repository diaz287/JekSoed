// File: ui/trip/TripScreen.kt

package com.example.jeksoed.ui.screens.trip

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.jeksoed.data.model.RideRequest
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.firebase.firestore.FirebaseFirestore
import com.google.maps.android.PolyUtil
import com.google.maps.android.compose.*

@Composable
fun TripScreen(navController: NavController, rideRequestId: String) {
    var rideRequest by remember { mutableStateOf<RideRequest?>(null) }
    var routePolyline by remember { mutableStateOf<List<LatLng>>(emptyList()) }
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
                // Konversi ke data class, ini lebih aman
                val request = snapshot.toObject(RideRequest::class.java)?.copy(id = snapshot.id)
                rideRequest = request

                // Log untuk debugging
                Log.d("TripScreen", "Data Diterima: $request")

                // Proses polyline HANYA jika datanya ada
                if (request?.encodedPolyline != null) {
                    routePolyline = PolyUtil.decode(request.encodedPolyline)
                    Log.d("TripScreen", "Polyline berhasil di-decode, jumlah titik: ${routePolyline.size}")
                } else {
                    Log.w("TripScreen", "encodedPolyline tidak ditemukan di dokumen!")
                }
            }
        }

        onDispose { listener.remove() }
    }

    // Efek ini akan berjalan HANYA jika routePolyline sudah terisi
    LaunchedEffect(routePolyline) {
        if (routePolyline.isNotEmpty()) {
            val bounds = LatLngBounds.builder()
            routePolyline.forEach { point ->
                bounds.include(point)
            }
            cameraPositionState.animate(
                com.google.android.gms.maps.CameraUpdateFactory.newLatLngBounds(bounds.build(), 150) // 150px padding
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState
        ) {
            // Gambar rute JIKA routePolyline tidak kosong
            if (routePolyline.isNotEmpty()) {
                Polyline(points = routePolyline, color = Color.Blue, width = 15f)
            }

            // Tampilkan marker berdasarkan rideRequest
            rideRequest?.let { request ->
                val pickupLatLng = LatLng(request.pickupLocation["latitude"] ?: 0.0, request.pickupLocation["longitude"] ?: 0.0)
                Marker(state = MarkerState(position = pickupLatLng), title = "Jemput di sini")

                val destinationLatLng = LatLng(request.destinationLocation["latitude"] ?: 0.0, request.destinationLocation["longitude"] ?: 0.0)
                Marker(state = MarkerState(position = destinationLatLng), title = "Tujuan")
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
                    text = "Status: ${rideRequest?.status?.replaceFirstChar { it.titlecase() } ?: "Memuat..."}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                // Di sini kita bisa menambahkan info driver, tombol, dll.
            }
        }
    }
}