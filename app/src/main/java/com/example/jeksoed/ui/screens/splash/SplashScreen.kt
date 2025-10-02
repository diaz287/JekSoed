package com.example.jeksoed.ui.screens.splash

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.jeksoed.navigation.Screen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

@Composable
fun SplashScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    LaunchedEffect(key1 = true) {
        if (auth.currentUser == null) {
            // Jika tidak ada user yang login, langsung ke halaman Login
            navController.navigate(Screen.Login.route) {
                popUpTo(Screen.Splash.route) { inclusive = true }
            }
        } else {
            // Jika ada user yang login, cek perannya di Firestore
            val uid = auth.currentUser!!.uid
            Firebase.firestore.collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    val userRole = document.getString("role")
                    val destination = when (userRole) {
                        "penumpang" -> Screen.PassengerMain.route
                        "driver" -> Screen.DriverHome.route
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
        CircularProgressIndicator()
    }
}

