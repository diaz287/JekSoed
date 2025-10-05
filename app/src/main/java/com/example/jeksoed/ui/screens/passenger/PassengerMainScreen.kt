package com.example.jeksoed.ui.screens.passenger

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.jeksoed.R
import com.example.jeksoed.navigation.Screen
import com.example.jeksoed.ui.theme.JekSoedTheme

// Data class untuk item di bottom bar (tidak perlu diubah)
data class BottomNavItem(
    val title: String,
    val route: String,
    val icon: Painter
)

@Composable
fun PassengerMainScreen(mainNavController: NavController) {
    val bottomNavController = rememberNavController()
    Scaffold(
        bottomBar = { BottomNavigationBar(navController = bottomNavController) }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            // Navigasi internal untuk konten bottom bar
            BottomNavGraph(
                mainNavController = mainNavController,
                bottomNavController = bottomNavController
            )
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val items = listOf(
        BottomNavItem("Home", "home", painterResource(id = R.drawable.home_icon)),
        BottomNavItem("Activity", "activity", painterResource(id = R.drawable.maps_icon)),
        BottomNavItem("Profil", "profil", painterResource(id = R.drawable.profil_icon))
    )

    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.title, modifier = Modifier.size(24.dp)) },
                label = { Text(item.title) },
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}

@Composable
fun BottomNavGraph(mainNavController: NavController, bottomNavController: NavHostController) {
    NavHost(navController = bottomNavController, startDestination = "home") {
        composable("home") {
            // --- PERUBAHAN 1 ---
            // Teruskan navController utama ke HomeScreen
            HomeScreen(
                navController = mainNavController,
                onSearchClick = {
                    mainNavController.navigate(Screen.CreateOrder.route)
                }
            )
        }
        composable("activity") { ActivityScreen() }

        // --- PERUBAHAN 2 ---
        // Panggil ProfileScreen yang benar dan teruskan navController utama
        composable("profil") {
            ProfileScreen(navController = mainNavController)
        }
    }
}

// --- Halaman Placeholder untuk Riwayat ---
// Hapus ProfilScreen placeholder karena kita sudah punya implementasi aslinya

@Composable
fun ActivityScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Text("Halaman Riwayat Perjalanan")
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PassengerMainScreenPreview() {
    JekSoedTheme {
        PassengerMainScreen(mainNavController = rememberNavController())
    }
}