package com.example.jeksoed.ui.screens.auth

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.jeksoed.navigation.Screen
import com.example.jeksoed.ui.components.PrimaryButton
import com.example.jeksoed.ui.theme.JekSoedTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.jeksoed.R

// Composable "Pintar" (Smart Composable)
@Composable
fun LoginScreen(
    navController: NavController,
    auth: FirebaseAuth = FirebaseAuth.getInstance(),
    firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    LoginScreenUI(
        email = email,
        password = password,
        isLoading = isLoading,
        onEmailChange = { email = it },
        onPasswordChange = { password = it },
        onLoginClick = {
            if (email.isBlank() || password.isBlank()) {
                Toast.makeText(context, "Email dan Password tidak boleh kosong!", Toast.LENGTH_SHORT).show()
            } else {
                isLoading = true
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val uid = auth.currentUser!!.uid
                            firestore.collection("users").document(uid).get()
                                .addOnSuccessListener { document ->
                                    val userRole = document.getString("role")
                                    val destination = when (userRole) {
                                        "penumpang" -> Screen.PassengerMain.route
                                        "driver" -> Screen.DriverMain.route
                                        else -> Screen.Login.route
                                    }
                                    Toast.makeText(context, "Login Berhasil!", Toast.LENGTH_SHORT).show()
                                    navController.navigate(destination) {
                                        popUpTo(Screen.Login.route) { inclusive = true }
                                        popUpTo(Screen.Cta.route) { inclusive = true }
                                    }
                                }
                                .addOnFailureListener {
                                    isLoading = false
                                    Toast.makeText(context, "Gagal mengambil data user.", Toast.LENGTH_SHORT).show()
                                }
                        } else {
                            isLoading = false
                            Toast.makeText(context, "Login Gagal: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
            }
        },
        // --- TERUSKAN NAVCONTROLLER UNTUK MENANGANI NAVIGASI ---
        navController = navController
    )
}

// Composable "Biasa" (Dumb Composable)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreenUI(
    email: String,
    password: String,
    isLoading: Boolean,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    navController: NavController // Menggunakan NavController langsung
) {
    val unsoedColor = colorResource(id = R.color.unsoed_dark)
    Scaffold(
        topBar = {
            TopAppBar(
                title = { /* Kosongkan judul */ },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
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
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(text = "Hai Bung!", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                Text(text = "Welcome Back!", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(48.dp))

            OutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
                label = { Text("Email") },
                placeholder = { Text("Masukan email kamu") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(32.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = password,
                onValueChange = onPasswordChange,
                label = { Text("Password") },
                placeholder = { Text("Masukan password kamu") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                shape = RoundedCornerShape(32.dp)
            )
            Box(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Lupa Password?",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(vertical = 8.dp)
                        .clickable { navController.navigate(Screen.ForgotPassword.route) }, // <-- NAVIGASI LUPA PASSWORD
                    fontSize = 12.sp,
                    textDecoration = TextDecoration.Underline
                )
            }
            Spacer(modifier = Modifier.height(24.dp))

            if (isLoading) {
                CircularProgressIndicator()
            } else {
                PrimaryButton(
                    text = "Masuk",
                    onClick = onLoginClick,
                    containerColor = Color(0xFFFFC107),
                    contentColor = Color.Black
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            val registerAnnotatedString = buildAnnotatedString {
                append("Belum punya akun? ")
                pushStringAnnotation(tag = "REGISTER", annotation = "register_link")
                withStyle(style = SpanStyle(color=unsoedColor , fontWeight = FontWeight.Bold)) {
                    append("Daftar dulu, yuk!")
                }
                pop()
            }
            ClickableText(
                text = registerAnnotatedString,
                onClick = { offset ->
                    registerAnnotatedString.getStringAnnotations(tag = "REGISTER", start = offset, end = offset)
                        .firstOrNull()?.let { navController.navigate(Screen.RoleSelection.route) } // <-- NAVIGASI DAFTAR
                }
            )

            Spacer(modifier = Modifier.weight(1f))

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
                        .firstOrNull()?.let { navController.navigate(Screen.Tnc.route) } // <-- NAVIGASI T&C
                }
            )
        }
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginScreenPreview() {
    JekSoedTheme {
        LoginScreenUI(
            email = "",
            password = "",
            isLoading = false,
            onEmailChange = {},
            onPasswordChange = {},
            onLoginClick = {},
            navController = rememberNavController() // Menggunakan NavController palsu untuk preview
        )
    }
}