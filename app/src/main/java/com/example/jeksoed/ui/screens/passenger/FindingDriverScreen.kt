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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.jeksoed.navigation.Screen
import com.example.jeksoed.ui.theme.JekSoedTheme
import com.google.firebase.firestore.FirebaseFirestore

/**
 * =================================================================================
 * SMART COMPOSABLE
 * - Menangani logic dan side-effect (Listener Firestore & Navigasi).
 * =================================================================================
 */
@Composable
fun FindingDriverScreen(navController: NavController, rideRequestId: String) {
    val context = LocalContext.current

    // Snapshot Listener yang akan terus mengamati dokumen pesanan.
    // rideRequestId digunakan sebagai 'key' agar effect dijalankan ulang jika ID berubah.
    DisposableEffect(rideRequestId) {
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

                    // TODO: Ganti dengan navigasi ke TripScreen/OnGoingRideScreen
                    // Contoh: navController.navigate(Screen.Trip.createRoute(rideRequestId, driverId)) {
                    //     popUpTo(Screen.FindingDriver.route) { inclusive = true }
                    // }
                }
            }
        }

        // Hentikan listener saat layar ini ditinggalkan untuk mencegah memory leak
        onDispose {
            listener.remove()
        }
    }

    // Memanggil Composable UI yang "bodoh"
    FindingDriverScreenUI()
}

/**
 * =================================================================================
 * DUMB UI COMPOSABLE
 * - Hanya menampilkan UI statis.
 * - Tidak memiliki state atau logic sama sekali.
 * =================================================================================
 */
@Composable
private fun FindingDriverScreenUI() {
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

/**
 * =================================================================================
 * PREVIEW
 * =================================================================================
 */
@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun FindingDriverScreenPreview() {
    JekSoedTheme {
        FindingDriverScreenUI()
    }
}