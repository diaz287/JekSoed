package com.example.jeksoed.ui.screens.passenger

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.jeksoed.R
import com.example.jeksoed.ui.components.BottomBar
import com.example.jeksoed.ui.components.BottomNavItem
import com.example.jeksoed.ui.theme.JekSoedTheme


// Data class sederhana untuk data palsu
data class LocationData(val title: String, val address: String)

/**
 * =================================================================================
 * SMART COMPOSABLE
 * - Bertugas menyediakan data (nama user, riwayat lokasi, dll).
 * - Menangani semua logika navigasi dan interaksi data.
 * =================================================================================
 */
@Composable
fun PassengerHomeScreen(
    navController: NavController
) {
    // Data palsu untuk tujuan UI development
    val userName = "Fawwaz"
    val recentLocations = listOf(
        LocationData("Jl. Wayu No.48", "Jl. Wayu No.48, Cigimbal, Tritih Kulon, Kec..."),
        LocationData("Jalan Turi No. 61", "Jalan Turi No. 61, Lomanis, Cilacap Tengah,..."),
        LocationData("dr. H. Marwanto", "Jl. Gatot Subroto No.198, Klempang, Gunu..."),
        LocationData("jalan mt haryono no 194", "Jl. MT. Haryono No.194, Rawakeong, Loma...")
    )

    // Ambil state rute saat ini dari NavController
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Definisikan aksi navigasi di sini
    val onBottomNavItemClick = { route: String ->
        navController.navigate(route) {
            navController.graph.startDestinationRoute?.let { popUpTo(it) { saveState = true } }
            launchSingleTop = true
            restoreState = true
        }
    }

    PassengerHomeScreenUI(
        userName = userName,
        recentLocations = recentLocations,
        currentRoute = currentRoute, // <-- Kirim route aktif sebagai String
        onBottomNavItemClick = onBottomNavItemClick, // <-- Kirim lambda aksi
        onNotificationClick = { /* TODO: Logika klik notifikasi */ },
        onSearchClick = { /* TODO: Logika klik search bar */ }
    )
}

/**
 * =================================================================================
 * DUMB UI COMPOSABLE
 * - TIDAK tahu tentang NavController.
 * - Hanya menerima data (String, List, dll) dan fungsi lambda.
 * =================================================================================
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PassengerHomeScreenUI(
    userName: String,
    recentLocations: List<LocationData>,
    currentRoute: String?,
    onBottomNavItemClick: (String) -> Unit,
    onNotificationClick: () -> Unit,
    onSearchClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { /* ... */ },
                actions = {
                    IconButton(onClick = onNotificationClick) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notifikasi")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF00880C))
            )
        },
        bottomBar = {
            // UI hanya meneruskan state dan aksi ke komponen BottomBar
            BottomBar(
                currentRoute = currentRoute,
                onItemClick = onBottomNavItemClick
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            item { HomeHeader() }
            item {
                Column(Modifier.background(MaterialTheme.colorScheme.surface)) {
                    GreetingSection(name = userName)
                    PromoCard()
                    SearchSection(onSearchClick = onSearchClick)
                }
            }
            item {
                Text(
                    text = "Tujuan Terakhir",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp)
                )
            }
            items(recentLocations) { location ->
                RecentLocationItem(location = location)
            }
        }
    }
}

/**
 * =================================================================================
 * KUMPULAN HELPER UI COMPONENTS
 * =================================================================================
 */
@Composable
private fun HomeHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .background(Color(0xFF00880C)), // Warna hijau Gojek
        contentAlignment = Alignment.Center
    ) {
        // Di sini Anda bisa menaruh gambar ilustrasi SVG atau PNG
        Text("Ilustrasi Header", color = Color.White, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun GreetingSection(name: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Siap buat nemenin $name",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Walau udah malam, tetep berangkat!",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray
        )
    }
}

@Composable
private fun PromoCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFEFFFF0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_launcher_foreground), // Ganti dengan ikon yang sesuai
                contentDescription = "Promo",
                tint = Color(0xFF00880C),
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text("Maw HEMAT setiap hari?", fontWeight = FontWeight.Bold)
                Text("Let's GoRide Hemat! Ke mana aja selalu diskon~", fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.weight(1f))
            Icon(Icons.Default.ArrowForwardIos, contentDescription = null, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun SearchSection(onSearchClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(180.dp)
            .clip(RoundedCornerShape(16.dp))
    ) {
        Image(
            painter = painterResource(id = R.drawable.map_placeholder), // Pastikan ada gambar ini
            contentDescription = "Peta",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            // Search Bar Palsu
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable { onSearchClick() }
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Search, contentDescription = "Cari", tint = Color.Red)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cari lokasi tujuan", color = Color.Gray)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            // Tombol Rumah
            Button(
                onClick = { /* TODO */ },
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                Icon(Icons.Default.Home, contentDescription = "Rumah", tint = Color.DarkGray)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Rumah", color = Color.Black)
            }
        }
    }
}

@Composable
private fun RecentLocationItem(location: LocationData) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .clickable { /* TODO */ }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.History, contentDescription = "Riwayat", tint = Color.Gray)
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(location.title, fontWeight = FontWeight.Bold)
            Text(location.address, fontSize = 14.sp, color = Color.Gray, maxLines = 1)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Icon(Icons.Default.BookmarkBorder, contentDescription = "Simpan", tint = Color.Gray)
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PassengerHomeDashboardPreview() {
    val dummyUserName = "Fawwaz"
    val dummyRecentLocations = listOf(
        LocationData("Jl. Wayu No.48", "Jl. Wayu No.48, Cigimbal, Tritih Kulon, Kec..."),
        LocationData("Jalan Turi No. 61", "Jalan Turi No. 61, Lomanis, Cilacap Tengah,...")
    )

    JekSoedTheme {
        PassengerHomeScreenUI(
            userName = dummyUserName,
            recentLocations = dummyRecentLocations,
            currentRoute = BottomNavItem.Home.screen_route, // Simulasi item "Beranda" aktif
            onBottomNavItemClick = { }, // Di preview, aksi tidak melakukan apa-apa
            onNotificationClick = {},
            onSearchClick = {}
        )
    }
}