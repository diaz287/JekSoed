package com.example.jeksoed.ui.screens.auth

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.jeksoed.navigation.Screen
import com.example.jeksoed.ui.components.PrimaryButton
import com.example.jeksoed.ui.theme.JekSoedTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore


@Composable
fun RegisterScreen(
    navController: NavController,
    auth: FirebaseAuth = FirebaseAuth.getInstance(),
    firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    val context = LocalContext.current
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var selectedRole by rememberSaveable { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    RegisterScreenUI(
        name = name,
        email = email,
        password = password,
        selectedRole = selectedRole,
        isLoading = isLoading,
        onNameChange = { name = it },
        onEmailChange = { email = it },
        onPasswordChange = { password = it },
        onRoleSelected = { selectedRole = it },
        onRegisterClick = {
            if (name.isBlank() || email.isBlank() || password.isBlank() || selectedRole.isBlank()) {
                Toast.makeText(context, "Semua field harus diisi!", Toast.LENGTH_SHORT).show()
            } else {
                isLoading = true
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
                                    "createdAt" to FieldValue.serverTimestamp()
                                )
                                firestore.collection("users").document(uid).set(userMap)
                                    .addOnSuccessListener {
                                        Toast.makeText(context, "Registrasi Berhasil!", Toast.LENGTH_SHORT).show()
                                        // Arahkan ke login dan hapus backstack ke register
                                        navController.navigate(Screen.Login.route) {
                                            popUpTo(Screen.Register.route) { inclusive = true }
                                        }
                                    }
                                    .addOnFailureListener { e ->
                                        isLoading = false
                                        Toast.makeText(context, "Gagal menyimpan data: ${e.message}", Toast.LENGTH_LONG).show()
                                    }
                            }
                        } else {
                            isLoading = false
                            Toast.makeText(context, "Registrasi Gagal: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
            }
        },
        onLoginClick = { navController.navigate(Screen.Login.route) }
    )
}


/**
 * Composable "Bodoh" (Dumb Composable)
 * - Hanya bertanggung jawab untuk menampilkan UI.
 * - Menerima semua data dan fungsi callback sebagai parameter.
 * - Tidak memiliki logika bisnis, tidak tahu tentang Firebase atau NavController.
 * - Mudah untuk di-preview.
 */
@Composable
fun RegisterScreenUI(
    name: String,
    email: String,
    password: String,
    selectedRole: String,
    isLoading: Boolean,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onRoleSelected: (String) -> Unit,
    onRegisterClick: () -> Unit,
    onLoginClick: () -> Unit
) {
    val roles = listOf("Penumpang", "Driver")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Buat Akun Baru", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedTextField(value = name, onValueChange = onNameChange, label = { Text("Nama Lengkap") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = email, onValueChange = onEmailChange, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = password, onValueChange = onPasswordChange, label = { Text("Password") }, modifier = Modifier.fillMaxWidth(), visualTransformation = PasswordVisualTransformation())
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Daftar sebagai:", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.fillMaxWidth())
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            roles.forEach { role ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { onRoleSelected(role.lowercase()) }
                        .padding(end = 16.dp)
                ) {
                    RadioButton(
                        selected = (selectedRole == role.lowercase()),
                        onClick = { onRoleSelected(role.lowercase()) }
                    )
                    Text(text = role, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 4.dp))
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.onPrimary)
        } else {
            PrimaryButton(
                text = "Register",
                onClick = onRegisterClick
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Sudah punya akun? Login",
            modifier = Modifier.clickable(onClick = onLoginClick),
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RegisterScreenPreview() {
    JekSoedTheme {
        RegisterScreenUI(
            name = "John Doe",
            email = "john.doe@example.com",
            password = "password123",
            selectedRole = "penumpang",
            isLoading = false,
            onNameChange = {},
            onEmailChange = {},
            onPasswordChange = {},
            onRoleSelected = {},
            onRegisterClick = {},
            onLoginClick = {}
        )
    }
}