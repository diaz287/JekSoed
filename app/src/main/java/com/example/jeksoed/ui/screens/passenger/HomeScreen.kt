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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.jeksoed.R
import com.example.jeksoed.data.model.RideRequest // Pastikan import RideRequest ada
import com.example.jeksoed.navigation.Screen
import com.example.jeksoed.ui.screens.passenger.components.CategoryGrid
import com.example.jeksoed.ui.screens.passenger.components.RecentHistoryList
import com.example.jeksoed.ui.screens.passenger.components.RecommendationSection
import com.example.jeksoed.ui.screens.passenger.components.TopHeader
import com.example.jeksoed.ui.theme.JekSoedTheme

// Data class bisa tetap ada
data class Category(val name: String, val iconResId: Int, val tag: String? = null)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    onSearchClick: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    val hasNotification by remember { mutableStateOf(true) }

    // Navigasi ke trip aktif
    LaunchedEffect(Unit) {
        viewModel.navigateToActiveTrip.collect { rideId ->
            navController.navigate(Screen.Trip.createRoute(rideId))
        }
    }

    // UI-nya didelegasikan ke Composable "Dumb" di bawah
    HomeScreenContent(
        uiState = uiState,
        hasNotification = hasNotification,
        showDialog = showDialog,
        onSearchClick = onSearchClick,
        onNotificationClick = { /* TODO: Logika klik notifikasi */ },
        onCategoryClick = { categoryName ->
            when (categoryName) {
                "JekMotor" -> navController.navigate(Screen.CreateOrder.route)
                "JekClean", "Lainnya", "JekMobil" -> showDialog = true
            }
        },
        onDismissDialog = { showDialog = false }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenContent(
    uiState: HomeUiState,
    hasNotification: Boolean,
    showDialog: Boolean,
    onSearchClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onCategoryClick: (String) -> Unit,
    onDismissDialog: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFFBEB)) // Latar belakang kuning muda
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Column {
                    TopHeader(
                        name = uiState.userName,
                        hasNotification = hasNotification,
                        onNotificationClick = onNotificationClick
                    )
                    Image(
                        painter = painterResource(id = R.drawable.home_bg),
                        contentDescription = "Home Banner",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )
                }
            }

            // Item 2: Card yang berisi SearchBar dan semua konten lainnya
            item {
                Card(
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column {
                        // SearchBar
                        SearchBarFake(
                            onSearchClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 16.dp)
                        )

                        // Kategori
                        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                            Text("Kategori", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(40.dp))
                            CategoryGrid(onCategoryClick = onCategoryClick)
                            Spacer(modifier = Modifier.height(24.dp))
                        }

                        // Baru baru ini
                        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                            Text("Baru baru ini...", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(12.dp))
                            if (uiState.isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            } else {
                                RecentHistoryList(history = uiState.recentTrips)
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                        }

                        // Rekomendasi
                        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                            Text("Cucu Jendral belum pernah kesini? Rugi dong!", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(12.dp))
                            RecommendationSection()
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        DevelopmentDialog(onDismiss = onDismissDialog)
    }
}


// SearchBarFake tidak berubah
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
                .height(54.dp)
                .padding(horizontal = 16.dp),
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

// --- INI PERUBAHAN UTAMA UNTUK PREVIEW ---
// Preview diperbarui untuk mencocokkan model RideRequest
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenPreview() {
    // Import Timestamp jika diperlukan
    // import com.google.firebase.Timestamp

    JekSoedTheme {
        HomeScreenContent(
            // Kita berikan data palsu (fake data) yang valid
            uiState = HomeUiState(
                userName = "Sobat Jeksoed",
                recentTrips = listOf(
                    // Gunakan field yang benar: pickupName, destinationName, createdAt
                    RideRequest(
                        pickupName = "Fakultas Teknik",
                        destinationName = "Audit FP",
                        createdAt = com.google.firebase.Timestamp.now()
                    ),
                    RideRequest(
                        pickupName = "Rektorat",
                        destinationName = "FISIP",
                        createdAt = com.google.firebase.Timestamp.now()
                    )
                ),
                isLoading = false
            ),
            hasNotification = true,
            showDialog = false,
            onSearchClick = {},
            onNotificationClick = {},
            onCategoryClick = {},
            onDismissDialog = {}
        )
    }
}