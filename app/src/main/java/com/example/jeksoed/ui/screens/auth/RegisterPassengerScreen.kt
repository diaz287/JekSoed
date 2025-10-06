package com.example.jeksoed.ui.screens.auth

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.jeksoed.navigation.Screen
import com.example.jeksoed.ui.components.PrimaryButton
import com.example.jeksoed.ui.theme.JekSoedTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore


@Composable
fun RegisterPassengerScreen(
    navController: NavController,
    auth: FirebaseAuth = FirebaseAuth.getInstance(),
    firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    val context = LocalContext.current
    var name by rememberSaveable { mutableStateOf("") }
    var nim by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    RegisterPassengerScreenUI(
        name = name,
        nim = nim,
        email = email,
        phone = phone,
        password = password,
        isLoading = isLoading,
        onNameChange = { name = it },
        onNimChange = { nim = it },
        onEmailChange = { email = it },
        onPhoneChange = { phone = it },
        onPasswordChange = { password = it },
        onRegisterClick = {
            if (name.isBlank() || nim.isBlank() || email.isBlank() || phone.isBlank() || password.isBlank()) {
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
                                    "nim" to nim,
                                    "email" to email,
                                    "nomorHp" to phone,
                                    "role" to "penumpang",
                                    "createdAt" to FieldValue.serverTimestamp()
                                )
                                firestore.collection("users").document(uid).set(userMap)
                                    .addOnSuccessListener {
                                        Toast.makeText(context, "Registrasi Berhasil!", Toast.LENGTH_SHORT).show()
                                        navController.navigate(Screen.Login.route) {
                                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
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
        onLoginClick = { navController.navigate(Screen.Login.route) },
        onBackClick = { navController.popBackStack() },
        // --- TERUSKAN NAVCONTROLLER KE UI ---
        navController = navController
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterPassengerScreenUI(
    name: String,
    nim: String,
    email: String,
    phone: String,
    password: String,
    isLoading: Boolean,
    onNameChange: (String) -> Unit,
    onNimChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onRegisterClick: () -> Unit,
    onLoginClick: () -> Unit,
    onBackClick: () -> Unit,
    // --- TAMBAHKAN NAVCONTROLLER SEBAGAI PARAMETER ---
    navController: NavController
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { /* Kosongkan judul */ },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(text = "Selamat Bergabung!", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text(text = "Daftar dulu, Kak!", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(32.dp))

            // Input Fields
            OutlinedTextField(value = name, onValueChange = onNameChange, label = { Text("Nama") }, placeholder = { Text("Masukan nama kamu") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(32.dp))
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = nim, onValueChange = onNimChange, label = { Text("NIM") }, placeholder = { Text("Masukan NIM kamu") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(32.dp))
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = email, onValueChange = onEmailChange, label = { Text("Email") }, placeholder = { Text("Masukan email kamu") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(32.dp))
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = phone, onValueChange = onPhoneChange, label = { Text("Nomor Hp") }, placeholder = { Text("Masukan nomor hp kamu") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(32.dp))
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = password, onValueChange = onPasswordChange, label = { Text("Password") }, placeholder = { Text("Masukan password kamu") }, modifier = Modifier.fillMaxWidth(), visualTransformation = PasswordVisualTransformation(), shape = RoundedCornerShape(32.dp))
            Spacer(modifier = Modifier.height(32.dp))

            // Tombol Daftar
            if (isLoading) {
                CircularProgressIndicator()
            } else {
                PrimaryButton(
                    text = "Daftar",
                    onClick = onRegisterClick,
                    containerColor = Color(0xFFFFC107),
                    contentColor = Color.Black
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Link ke Login
            val loginAnnotatedString = buildAnnotatedString {
                append("Udah ada akun? ")
                pushStringAnnotation(tag = "LOGIN", annotation = "login_link")
                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)) {
                    append("Masuk")
                }
                pop()
            }
            ClickableText(
                text = loginAnnotatedString,
                onClick = { offset ->
                    loginAnnotatedString.getStringAnnotations(tag = "LOGIN", start = offset, end = offset)
                        .firstOrNull()?.let { onLoginClick() }
                }
            )

            Spacer(modifier = Modifier.weight(1f))

            // Syarat & Ketentuan
            val tncAnnotatedString = buildAnnotatedString {
                append("Aku setuju sama ")
                pushStringAnnotation(tag = "TNC", annotation = "tnc_link")
                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)) {
                    append("Syarat & Ketentuan Privasi JEKSOED.")
                }
                pop()
            }
            ClickableText(
                text = tncAnnotatedString,
                style = MaterialTheme.typography.bodySmall.copy(textAlign = TextAlign.Center, color = Color.Gray),
                onClick = { offset ->
                    tncAnnotatedString.getStringAnnotations(tag = "TNC", start = offset, end = offset)
                        .firstOrNull()?.let { navController.navigate(Screen.Tnc.route) }
                }
            )
        }
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun RegisterPassengerScreenPreview() {
    JekSoedTheme {
        RegisterPassengerScreenUI(
            name = "", nim = "", email = "", phone = "", password = "",
            isLoading = false,
            onNameChange = {}, onNimChange = {}, onEmailChange = {}, onPhoneChange = {}, onPasswordChange = {},
            onRegisterClick = {}, onLoginClick = {}, onBackClick = {},
            // Teruskan NavController palsu untuk Preview
            navController = rememberNavController()
        )
    }
}