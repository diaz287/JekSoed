package com.example.jeksoed.ui.auth

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.jeksoed.navigation.Screen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun LoginScreen(navController: NavController) {
    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) } // State untuk loading

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "Selamat Datang Kembali", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") }, modifier = Modifier.fillMaxWidth(), visualTransformation = PasswordVisualTransformation())
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (email.isBlank() || password.isBlank()) {
                    Toast.makeText(context, "Email dan Password tidak boleh kosong!", Toast.LENGTH_SHORT).show()
                } else {
                    isLoading = true // Mulai loading
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                // Jika login Auth berhasil, cek role di Firestore
                                val uid = auth.currentUser!!.uid
                                firestore.collection("users").document(uid).get()
                                    .addOnSuccessListener { document ->
                                        val userRole = document.getString("role")
                                        val destination = when (userRole) {
                                            "penumpang" -> Screen.PassengerHome.route
                                            "driver" -> Screen.DriverHome.route
                                            else -> Screen.Login.route
                                        }
                                        Toast.makeText(context, "Login Berhasil!", Toast.LENGTH_SHORT).show()
                                        navController.navigate(destination) {
                                            popUpTo(Screen.Login.route) { inclusive = true }
                                        }
                                    }
                                    .addOnFailureListener {
                                        isLoading = false // Hentikan loading
                                        Toast.makeText(context, "Gagal mengambil data user.", Toast.LENGTH_SHORT).show()
                                    }
                            } else {
                                isLoading = false // Hentikan loading
                                Toast.makeText(context, "Login Gagal: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("Login")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Belum punya akun? Daftar",
            modifier = Modifier.clickable { navController.navigate(Screen.Register.route) },
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}