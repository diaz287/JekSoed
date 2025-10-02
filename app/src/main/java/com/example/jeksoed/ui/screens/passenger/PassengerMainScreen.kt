package com.example.jeksoed.ui.screens.passenger

import com.example.jeksoed.R
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.jeksoed.navigation.Screen
import com.example.jeksoed.ui.theme.JekSoedTheme


// Data class untuk item di bottom bar
data class BottomNavItem(
    val title: String,
    val route: String,
    val icon: Painter,
    val iconVector: ImageVector? = null
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
                icon = { Icon(item.icon, contentDescription = item.title,modifier = Modifier.size(24.dp) ) },
                label = { Text(item.title) },
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        // Hindari menumpuk backstack saat menekan item yang sama
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
            HomeScreen(
                onSearchClick = {
                    mainNavController.navigate(Screen.CreateOrder.route)
                }
            )
        }
        composable("activity") { ActivityScreen() }

        // Ganti placeholder dengan ProfileScreen yang baru
        composable("profil") {
            // Kita butuh NavController utama untuk navigasi saat logout
            ProfileScreen(navController = mainNavController)
        }
    }
}

// --- Halaman Placeholder untuk Riwayat dan Profil ---

@Composable
fun ActivityScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Halaman Riwayat Perjalanan")
    }
}

@Composable
fun ProfilScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Halaman Profil Pengguna")
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PassengerMainScreenPreview() {
    JekSoedTheme {
        // Untuk preview, kita bisa menggunakan NavController palsu dari rememberNavController()
        PassengerMainScreen(mainNavController = rememberNavController())
    }
}