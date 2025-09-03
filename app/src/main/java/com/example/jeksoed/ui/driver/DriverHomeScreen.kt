package com.example.jeksoed.ui.driver

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

@Composable
fun DriverHomeScreen(navController: NavController) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Halaman Utama Driver", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            FirebaseAuth.getInstance().signOut()
            navController.navigate(Screen.Login.route) {
                popUpTo(Screen.DriverHome.route) { inclusive = true }
            }
        }) {
            Text("Logout")
        }
    }
}