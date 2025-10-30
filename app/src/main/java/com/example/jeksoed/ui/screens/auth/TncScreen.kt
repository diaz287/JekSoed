package com.example.jeksoed.ui.screens.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.jeksoed.ui.theme.JekSoedTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TncScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("") },
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
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()) // Agar bisa di-scroll jika teksnya panjang
        ) {
            Text(
                text = "Syarat dan Ketentuan",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,

            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Selamat datang di Jeksoed, aplikasi ojek online buatan mahasiswa Unsoed untuk mahasiswa Unsoed. Dengan menggunakan aplikasi ini, kamu setuju untuk terikat dengan Syarat, Ketentuan, dan Kebijakan Privasi berikut:",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text("1. Penerimaan Ketentuan", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Dengan mengunduh, mendaftar, atau menggunakan aplikasi Jeksoed, pengguna dianggap telah membaca, memahami, dan menyetujui seluruh aturan yang berlaku. Jika tidak setuju, silakan hentikan penggunaan aplikasi.",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text("2. Penggunaan Aplikasi", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "• Jeksoed hanya diperuntukkan bagi mahasiswa Unsoed.",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "• Pengguna wajib menggunakan data yang benar dan valid (misalnya identitas mahasiswa).",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "• Dilarang menggunakan aplikasi untuk tindakan ilegal, merugikan, atau merusak layanan.",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "• Keamanan akun sepenuhnya menjadi tanggung jawab pengguna.",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text("3. Privasi", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "• Data pengguna akan dijaga sesuai dengan kebijakan privasi Jeksoed.",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "• Kami tidak akan menyalahgunakan informasi pribadi mahasiswa.",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text("4. Perubahan Ketentuan", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Jeksoed berhak memperbarui Syarat & Ketentuan kapan saja. Perubahan akan diumumkan melalui aplikasi.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun TncScreenPreview() {
    JekSoedTheme {
        TncScreen(navController = rememberNavController())
    }
}