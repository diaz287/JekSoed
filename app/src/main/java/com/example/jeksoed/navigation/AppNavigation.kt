package com.example.jeksoed.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost // Import NavHost yang standar
import androidx.navigation.compose.composable // Import composable yang standar
import androidx.navigation.compose.rememberNavController // Import rememberNavController yang standar
import androidx.navigation.navArgument
import com.example.jeksoed.ui.screens.auth.LoginScreen
import com.example.jeksoed.ui.screens.auth.RegisterScreen
import com.example.jeksoed.ui.screens.chat.ChatScreen
import com.example.jeksoed.ui.screens.driver.DriverHomeScreen
import com.example.jeksoed.ui.screens.passenger.FindingDriverScreen
import com.example.jeksoed.ui.screens.passenger.OrderScreen
import com.example.jeksoed.ui.screens.passenger.PassengerMainScreen
import com.example.jeksoed.ui.screens.rating.RatingScreen
import com.example.jeksoed.ui.screens.splash.SplashScreen
import com.example.jeksoed.ui.screens.trip.TripScreen

// Sealed class Screen tetap sama
sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    object PassengerMain : Screen("passenger_main")
    object CreateOrder : Screen("create_order")
    object DriverHome : Screen("driver_home")
    object FindingDriver : Screen("finding_driver/{rideRequestId}") {
        fun createRoute(rideRequestId: String) = "finding_driver/$rideRequestId"
    }
    object Trip : Screen("trip/{rideRequestId}") {
        fun createRoute(rideRequestId: String) = "trip/$rideRequestId"
    }
    object Rating : Screen("rating/{driverId}") {
        fun createRoute(driverId: String) = "rating/$driverId"
    }
    object Chat : Screen("chat/{rideRequestId}") {
        fun createRoute(rideRequestId: String) = "chat/$rideRequestId"
    }
}

// Hapus anotasi @OptIn(ExperimentalAnimationApi::class)
@Composable
fun AppNavigation() {
    // 1. Gunakan rememberNavController() yang standar
    val navController = rememberNavController()

    // 2. Gunakan NavHost yang standar. Parameter animasi sekarang ada di dalam composable.
    NavHost(navController, startDestination = Screen.Splash.route) {
        composable(Screen.Splash.route) { SplashScreen(navController) }
        composable(Screen.Login.route) { LoginScreen(navController) }
        composable(Screen.Register.route) { RegisterScreen(navController) }
        composable(Screen.PassengerMain.route) { PassengerMainScreen(navController) }
        composable(Screen.DriverHome.route) { DriverHomeScreen(navController) }

        // 3. Definisi transisi sekarang menjadi parameter dari fungsi composable itu sendiri
        composable(
            route = Screen.CreateOrder.route,
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { fullWidth -> fullWidth },
                    animationSpec = tween(300)
                )
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { fullWidth -> -fullWidth },
                    animationSpec = tween(300)
                )
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { fullWidth -> -fullWidth },
                    animationSpec = tween(300)
                )
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { fullWidth -> fullWidth },
                    animationSpec = tween(300)
                )
            }
        ) {
            OrderScreen(navController)
        }

        composable(Screen.FindingDriver.route) { backStackEntry ->
            val rideRequestId = backStackEntry.arguments?.getString("rideRequestId")
            if (rideRequestId != null) {
                FindingDriverScreen(navController = navController, rideRequestId = rideRequestId)
            } else {
                navController.navigate(Screen.Login.route) {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                }
            }
        }
        composable(Screen.Trip.route) {
            TripScreen(navController = navController)
        }
        composable(
            route = Screen.Rating.route,
            arguments = listOf(
                navArgument("driverId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val driverId = backStackEntry.arguments?.getString("driverId")!!
            RatingScreen(navController = navController, driverId = driverId)
        }
        composable(Screen.Chat.route) { backStackEntry ->
            val rideRequestId = backStackEntry.arguments?.getString("rideRequestId")
            if (rideRequestId != null) {
                ChatScreen(navController = navController,rideRequestId = rideRequestId)
            }
        }
    }
}