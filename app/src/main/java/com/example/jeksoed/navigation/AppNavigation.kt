package com.example.jeksoed.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.jeksoed.ui.screens.splash.SplashScreen
import com.example.jeksoed.ui.screens.auth.LoginScreen
import com.example.jeksoed.ui.screens.auth.RegisterScreen
import com.example.jeksoed.ui.screens.passenger.PassengerHomeScreen
import com.example.jeksoed.ui.screens.driver.DriverHomeScreen
import com.example.jeksoed.ui.screens.passenger.FindingDriverScreen
import com.example.jeksoed.ui.screens.trip.TripScreen

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    object PassengerHome : Screen("passenger_home")
    object DriverHome : Screen("driver_home")
    object FindingDriver : Screen("finding_driver/{rideRequestId}") {
        fun createRoute(rideRequestId: String) = "finding_driver/$rideRequestId"
    }
    object Trip : Screen("trip/{rideRequestId}") {
        fun createRoute(rideRequestId: String) = "trip/$rideRequestId"
    }
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
        composable(Screen.FindingDriver.route) { backStackEntry ->
            val rideRequestId = backStackEntry.arguments?.getString("rideRequestId")
            if (rideRequestId != null) {
                FindingDriverScreen(navController = navController, rideRequestId = rideRequestId)
            } else {
                // Handle kasus jika ID tidak ada, misal kembali ke login
                navController.navigate(Screen.Login.route) {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                }
            }
        }
        composable(Screen.Trip.route) {
            TripScreen(navController = navController)
        }
    }
}
