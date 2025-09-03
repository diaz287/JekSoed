package com.example.jeksoed.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.jeksoed.ui.splash.SplashScreen
import com.example.jeksoed.ui.auth.LoginScreen
import com.example.jeksoed.ui.auth.RegisterScreen
import com.example.jeksoed.ui.passenger.PassengerHomeScreen
import com.example.jeksoed.ui.driver.DriverHomeScreen

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    object PassengerHome : Screen("passenger_home")
    object DriverHome : Screen("driver_home")
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController, startDestination = Screen.Splash.route) {
        composable(Screen.Splash.route) { SplashScreen(navController) }
        composable(Screen.Login.route) { LoginScreen(navController) }
        composable(Screen.Register.route) { RegisterScreen(navController) }
        composable(Screen.PassengerHome.route) { PassengerHomeScreen(navController) }
        composable(Screen.DriverHome.route) { DriverHomeScreen(navController) }
    }
}
