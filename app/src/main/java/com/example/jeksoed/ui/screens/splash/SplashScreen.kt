package com.example.jeksoed.ui.screens.splash

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
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
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    LaunchedEffect(key1 = true) {
        // Jalankan penundaan dan logika navigasi secara bersamaan
        coroutineScope {
            launch {
                delay(1500) // Penundaan minimum agar logo terlihat
            }

            launch {
                if (auth.currentUser == null) {
                    navController.navigate(Screen.Cta.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                } else {
                    try {
                        // Ambil data peran dari Firestore menggunakan await() agar lebih bersih
                        val uid = auth.currentUser!!.uid
                        val document = Firebase.firestore.collection("users").document(uid).get().await()
                        val userRole = document.getString("role")

                        // Update FCM token di latar belakang (tidak perlu ditunggu)
                        launch {
                            try {
                                val token = FirebaseMessaging.getInstance().token.await()
                                FirebaseFirestore.getInstance().collection("users").document(uid)
                                    .update("fcmToken", token)
                            } catch (e: Exception) {
                                Log.w("SplashScreen", "Gagal update FCM token", e)
                            }
                        }

                        val destination = when (userRole) {
                            "penumpang" -> Screen.PassengerMain.route
                            "driver" -> Screen.DriverMain.route
                            else -> Screen.Login.route
                        }
                        navController.navigate(destination) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }

                    } catch (e: Exception) {
                        // Jika gagal mengambil data, arahkan ke Login
                        Log.e("SplashScreen", "Gagal mengambil data user", e)
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                }
            }
        }
    }

    // Tampilan UI tetap sama
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Image(
            painter = painterResource(id = R.drawable.apk_logo_2),
            contentDescription = "App Logo",
            modifier = Modifier.size(120.dp)
        )
    }
}

