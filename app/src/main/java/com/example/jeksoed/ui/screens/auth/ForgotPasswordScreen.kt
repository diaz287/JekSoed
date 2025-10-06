package com.example.jeksoed.ui.screens.auth

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.jeksoed.ui.components.PrimaryButton
import com.example.jeksoed.ui.theme.JekSoedTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lupa Password") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Jangan panik! Masukkan email kamu di bawah untuk reset password.")
            Spacer(modifier = Modifier.height(24.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(32.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            PrimaryButton(
                text = "Kirim Link Reset",
                onClick = {
                    // TODO: Tambahkan logika kirim email reset password via Firebase Auth
                    Toast.makeText(context, "Fitur ini dalam pengembangan", Toast.LENGTH_SHORT).show()
                },
                containerColor = Color(0xFFFFC107),
                contentColor = Color.Black
            )
        }
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun ForgotPasswordScreenPreview() {
    JekSoedTheme {
        ForgotPasswordScreen(navController = rememberNavController())
    }
}