package com.example.jeksoed.ui.auth

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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
fun RegisterScreen(navController: NavController) {
    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("") }
    val roles = listOf("Penumpang", "Driver")


    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        // ... (UI untuk Text, TextField, RadioButton sama seperti sebelumnya) ...
        Text(text = "Buat Akun Baru", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nama Lengkap") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") }, modifier = Modifier.fillMaxWidth(), visualTransformation = PasswordVisualTransformation())
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Daftar sebagai:", style = MaterialTheme.typography.bodyLarge)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start, verticalAlignment = Alignment.CenterVertically) {
            roles.forEach { role ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { selectedRole = role.lowercase() }.padding(horizontal = 8.dp)) {
                    RadioButton(selected = (selectedRole == role.lowercase()), onClick = { selectedRole = role.lowercase() })
                    Text(text = role, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 4.dp))
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (name.isBlank() || email.isBlank() || password.isBlank() || selectedRole.isBlank()) {
                    Toast.makeText(context, "Semua field harus diisi!", Toast.LENGTH_SHORT).show()
                } else {
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val user = auth.currentUser
                                val uid = user?.uid
                                if (uid != null) {
                                    val userMap = hashMapOf(
                                        "uid" to uid,
                                        "nama" to name,
                                        "email" to email,
                                        "role" to selectedRole,
                                        "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                                    )
                                    firestore.collection("users").document(uid).set(userMap)
                                        .addOnSuccessListener {
                                            Toast.makeText(context, "Registrasi Berhasil!", Toast.LENGTH_SHORT).show()
                                            // Pindah ke halaman login setelah sukses
                                            navController.navigate(Screen.Login.route)
                                        }
                                }
                            } else {
                                Toast.makeText(context, "Registrasi Gagal: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Register")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Sudah punya akun? Login",
            //navController untuk pindah halaman
            modifier = Modifier.clickable { navController.navigate(Screen.Login.route) },
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}