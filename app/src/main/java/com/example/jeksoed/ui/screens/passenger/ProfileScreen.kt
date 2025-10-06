// main/java/com/example/jeksoed/ui/screens/passenger/ProfileScreen.kt

package com.example.jeksoed.ui.screens.passenger

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.jeksoed.R
import com.example.jeksoed.navigation.Screen
import com.example.jeksoed.ui.theme.JekSoedTheme

@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    ProfileScreenUI(
        uiState = uiState,
        onAccountClick = { navController.navigate(Screen.EditProfile.route) },
        onAboutClick = { /*TODO*/ },
        onTncClick = { navController.navigate(Screen.Tnc.route) },
        onLogoutClick = viewModel::onLogoutClick,
        onDeleteClick = viewModel::onDeleteClick
    )

    // Tampilkan Dialog Logout
    if (uiState.showLogoutDialog) {
        LogoutDialog(
            onDismiss = viewModel::onDismissLogoutDialog,
            onConfirm = {
                viewModel.confirmLogout()
                navController.navigate(Screen.Cta.route) {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                }
            }
        )
    }

    // Tampilkan Dialog Hapus Akun
    if (uiState.showDeleteDialog) {
        DeleteAccountDialog(
            onDismiss = viewModel::onDismissDeleteDialog,
            onConfirm = {
                viewModel.confirmDeleteAccount()
                navController.navigate(Screen.Cta.route) {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                }
            }
        )
    }
}

@Composable
fun ProfileScreenUI(
    uiState: ProfileUiState,
    onAccountClick: () -> Unit,
    onAboutClick: () -> Unit,
    onTncClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val profileImageSize = 90.dp
    val yellowColor = Color(0xFFFFD803)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp), // Tinggi header kuning
            contentAlignment = Alignment.TopStart
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val path = Path().apply {
                    moveTo(0f, 0f)
                    lineTo(size.width, 0f)
                    lineTo(size.width, size.height * 0.75f)
                    quadraticBezierTo(
                        x1 = size.width / 2,
                        y1 = size.height * 1.1f, // Lengkungan lebih panjang ke bawah
                        x2 = 0f,
                        y2 = size.height * 0.75f
                    )
                    close()
                }
                drawPath(path, color = yellowColor)
            }
            Text("Profile", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(16.dp).offset(y = 16.dp), fontWeight = FontWeight.Bold, textAlign = TextAlign.Left)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = -(profileImageSize + 16.dp)), // Geser ke atas
                contentAlignment = Alignment.TopCenter
            ) {

                // --- PERUBAHAN 1: CARD DITARUH SEBELUM FOTO ---
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = profileImageSize / 2),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(profileImageSize / 2))
                        Text(uiState.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(uiState.email, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                        ProfileMenuItem(
                            icon = Icons.Default.Person,
                            text = "Akunku",
                            onClick = onAccountClick
                        )
                    }
                }

                // --- PERUBAHAN 2: FOTO DITARUH SETELAH CARD AGAR MENUMPUK DI ATAS ---
                Image(
                    painter = painterResource(id = R.drawable.person_icon),
                    contentDescription = "Foto Profil",
                    modifier = Modifier
                        .size(profileImageSize)
                        .clip(CircleShape)
                        .border(4.dp, yellowColor, CircleShape),
                    contentScale = ContentScale.Crop
                )
            }

            // Mengatur ulang spacer agar jarak antar card pas
            Spacer(modifier = Modifier.height(24.dp - (profileImageSize + 16.dp)))

            // Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Tentang JEKSOED", modifier = Modifier.clickable(onClick = onAboutClick).padding(vertical = 12.dp))
                    HorizontalDivider()
                    Text("Ketentuan Syarat dan Privasi", modifier = Modifier.clickable(onClick = onTncClick).padding(vertical = 12.dp))
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Tombol Aksi
            OutlinedButton(
                onClick = onLogoutClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(50)
            ) {
                Text("Keluar", color = Color.Black)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onDeleteClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                border = ButtonDefaults.outlinedButtonBorder.copy(brush = SolidColor(Color.Red))
            ) {
                Text("Hapus Akun", color = Color.Red)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ... (Sisa kode tidak perlu diubah)
@Composable
fun ProfileMenuItem(icon: ImageVector, text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = text, tint = Color.Gray)
        Spacer(modifier = Modifier.width(16.dp))
        Text(text, modifier = Modifier.weight(1f))
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.Gray)
    }
}

@Composable
fun LogoutDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107))) {
                Text("Logout", color = Color.Black)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Ga jadi logout", color = Color.Black) }
        },
        icon = { Image(painter = painterResource(id = R.drawable.jeksoed_logo), contentDescription = null) },
        title = { Text("Kamu yakin mau logout?", textAlign = TextAlign.Center, fontWeight = FontWeight.Bold) },
        text = { Text("Yakin mau cabut dulu dari JEKSOED? Nanti balik lagi ya! ♥️", textAlign = TextAlign.Center) }
    )
}

@Composable
fun DeleteAccountDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107))) {
                Text("Hapus Akun", color = Color.Black)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Ga jadi", color = Color.Black) }
        },
        icon = { Image(painter = painterResource(id = R.drawable.jeksoed_logo), contentDescription = null) },
        title = { Text("Yakin mau hapus akun?", textAlign = TextAlign.Center, fontWeight = FontWeight.Bold) },
        text = { Text("Semua data bakal ilang permanen. Pikirin lagi sebelum klik \"Hapus\" ♥️", textAlign = TextAlign.Center) }
    )
}

@Preview(showSystemUi = true)
@Composable
private fun ProfileScreenPreview() {
    JekSoedTheme {
        ProfileScreenUI(
            uiState = ProfileUiState(name = "Imedia Sholem", email = "ime.sholem@gmail.com", isLoading = false),
            onAccountClick = {}, onAboutClick = {}, onTncClick = {}, onLogoutClick = {}, onDeleteClick = {}
        )
    }
}