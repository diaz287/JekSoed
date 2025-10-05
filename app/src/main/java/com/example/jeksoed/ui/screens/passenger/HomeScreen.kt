package com.example.jeksoed.ui.screens.passenger

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.jeksoed.R
import com.example.jeksoed.navigation.Screen
import com.example.jeksoed.ui.screens.passenger.components.CategoryGrid
import com.example.jeksoed.ui.screens.passenger.components.RecentHistoryList
import com.example.jeksoed.ui.screens.passenger.components.RecommendationSection
import com.example.jeksoed.ui.screens.passenger.components.TopHeader
import com.example.jeksoed.ui.theme.JekSoedTheme

// Data class bisa tetap ada
data class Category(val name: String, val iconResId: Int, val tag: String? = null)
data class HistoryItem(val title: String, val address: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    onSearchClick: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    val hasNotification by remember { mutableStateOf(true) }
    val userName = "Rafi Purnama"

    // --- MENGGUNAKAN BOX UNTUK MENUMPUK SEMUA KOMPONEN ---
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFFBEB)) // Latar belakang kuning muda
    ) {
        // LazyColumn sekarang menjadi lapisan dasar untuk semua konten
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            // Item 1: Gambar Banner dan SearchBar
            item {
                TopHeader(
                    name = userName,
                    hasNotification = hasNotification,
                    onNotificationClick = { /* TODO: Logika klik notifikasi */ }
                )
            }
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp) // Sesuaikan tinggi sesuai kebutuhan
                ) {
                    // Gambar statis sebagai latar belakang
                    Image(
                        painter = painterResource(id = R.drawable.home_bg),
                        contentDescription = "Home Banner",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    // SearchBar ditumpuk di bagian bawah-tengah Box ini
                    SearchBarFake(
                        onSearchClick,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .offset(y = 54.dp) // Offset agar setengah tumpang tindih
                    )
                }
            }

            // Item 2: Spacer untuk memberi ruang setelah SearchBar
            item {
                Spacer(modifier = Modifier.height(70.dp))
            }

            // Item 3: Kategori
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text("Kategori", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(40.dp))
                    CategoryGrid(onCategoryClick = { categoryName ->
                        when (categoryName) {
                            "JekMotor" -> navController.navigate(Screen.CreateOrder.route)
                            "JekClean", "Lainnya", "JekMobil" -> showDialog = true
                        }
                    })
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            // Item 4: Baru baru ini
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text("Baru baru ini...", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    RecentHistoryList()
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            // Item 5: Rekomendasi
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text("Cucu Jendral belum pernah kesini? Rugi dong!", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    RecommendationSection()
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

    }

    if (showDialog) {
        DevelopmentDialog(onDismiss = { showDialog = false })
    }
}


// SearchBarFake diubah sedikit untuk menerima Modifier
@Composable
fun SearchBarFake(onSearchClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.clickable { onSearchClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(50),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Mau ke mana hari ini?",
                color = Color.Gray,
                fontSize = 16.sp
            )
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Cari",
                tint = Color.Gray
            )
        }
    }
}

// DevelopmentDialog tidak berubah
@Composable
fun DevelopmentDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Dalam Pengembangan") },
        text = { Text("Fitur ini masih dalam tahap pengembangan dan akan segera tersedia.") },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}

// Preview diperbarui
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenPreview() {
    JekSoedTheme {
        HomeScreen(
            onSearchClick = {},
            navController = rememberNavController()
        )
    }
}