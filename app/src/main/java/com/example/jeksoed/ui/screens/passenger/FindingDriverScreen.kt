package com.example.jeksoed.ui.screens.passenger

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun FindingDriverScreen(navController: NavController, rideRequestId: String) {
    val context = LocalContext.current

    // Snapshot Listener yang akan terus mengamati dokumen pesanan
    DisposableEffect(Unit) {
        val db = FirebaseFirestore.getInstance()
        val rideRequestRef = db.collection("ride_requests").document(rideRequestId)

        val listener = rideRequestRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w("FindingDriver", "Listen failed.", e)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val status = snapshot.getString("status")
                val driverId = snapshot.getString("driverId")

                // Cek jika driver sudah ditemukan
                if (status == "accepted" && driverId != null) {
                    Toast.makeText(context, "Driver ditemukan!", Toast.LENGTH_LONG).show()
                    // TODO: Navigasi ke halaman perjalanan (TripScreen)
                }
            }
        }

        // Hentikan listener saat layar ini ditinggalkan untuk mencegah memory leak
        onDispose {
            listener.remove()
        }
    }

    // UI Sederhana untuk layar tunggu
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(modifier = Modifier.size(64.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Mencari Driver Terdekat...",
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}