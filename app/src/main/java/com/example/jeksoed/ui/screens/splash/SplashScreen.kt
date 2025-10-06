package com.example.jeksoed.ui.screens.splash

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.jeksoed.R
import com.example.jeksoed.navigation.Screen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await

@Composable
fun SplashScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    LaunchedEffect(key1 = true) {
        delay(2000)
        if (auth.currentUser == null) {
            // Jika tidak ada user yang login, langsung ke halaman Login
            navController.navigate(Screen.Cta.route) {
                popUpTo(Screen.Splash.route) { inclusive = true }
            }
        } else {
            try {
                val token = FirebaseMessaging.getInstance().token.await()
                FirebaseFirestore.getInstance().collection("users").document(auth.currentUser!!.uid)
                    .update("fcmToken", token)
            } catch (e: Exception) {
                Log.w("SplashScreen", "Gagal mendapatkan FCM token", e)
            }
            // Jika ada user yang login, cek perannya di Firestore
            val uid = auth.currentUser!!.uid
            Firebase.firestore.collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    val userRole = document.getString("role")
                    val destination = when (userRole) {
                        "penumpang" -> Screen.PassengerMain.route
                        "driver" -> Screen.DriverMain.route
                        else -> Screen.Login.route // Fallback jika role tidak ditemukan
                    }
                    navController.navigate(destination) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
                .addOnFailureListener {
                    // Jika gagal mengambil data, arahkan ke Login
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
        }
    }

    // Tampilan saat loading
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        // Ganti dengan logo baru Anda
        Image(
            painter = painterResource(id = R.drawable.apk_logo), // <-- GANTI DENGAN LOGO BARU ANDA
            contentDescription = "App Logo",
            modifier = Modifier.size(120.dp) // Sesuaikan ukurannya
        )
    }
}

