package com.example.jeksoed.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.jeksoed.ui.screens.auth.CtaScreen
import com.example.jeksoed.ui.screens.auth.ForgotPasswordScreen
import com.example.jeksoed.ui.screens.auth.LoginScreen
import com.example.jeksoed.ui.screens.auth.RegisterPassengerScreen
import com.example.jeksoed.ui.screens.auth.RoleSelectionScreen
import com.example.jeksoed.ui.screens.auth.TncScreen
import com.example.jeksoed.ui.screens.chat.ChatScreen
import com.example.jeksoed.ui.screens.driver.DriverHomeScreen
import com.example.jeksoed.ui.screens.passenger.FindingDriverScreen
import com.example.jeksoed.ui.screens.passenger.OrderScreen
import com.example.jeksoed.ui.screens.passenger.PassengerMainScreen
import com.example.jeksoed.ui.screens.rating.RatingScreen
import com.example.jeksoed.ui.screens.splash.SplashScreen
import com.example.jeksoed.ui.screens.trip.TripScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.navigation
import com.example.jeksoed.ui.screens.auth.RegisterDriverStep1Screen
import com.example.jeksoed.ui.screens.auth.RegisterDriverStep2Screen
import com.example.jeksoed.ui.screens.auth.RegisterDriverStep3Screen
import com.example.jeksoed.ui.screens.auth.RegisterDriverViewModel
import com.example.jeksoed.ui.screens.driver.DriverMainScreen

// Sealed class Screen tidak perlu diubah
sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Cta : Screen("cta")
    object Register : Screen("register/{role}") {
        fun createRoute(role: String) = "register/$role"
    }
    object RegisterDriverGraph : Screen("register_driver_graph") // <-- GRAF NAVIGASI BARU
    object RegisterDriverStep1 : Screen("register_driver_1")
    object RegisterDriverStep2 : Screen("register_driver_2")
    object RegisterDriverStep3 : Screen("register_driver_3")
    object RoleSelection : Screen("role_selection")
    object Tnc : Screen("tnc")
    object ForgotPassword : Screen("forgot_password")
    object PassengerMain : Screen("passenger_main")
    object CreateOrder : Screen("create_order")
    object DriverMain : Screen("driver_main")
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


@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController, startDestination = Screen.Splash.route) {
        composable(Screen.Splash.route) { SplashScreen(navController) }
        composable(Screen.Cta.route) { CtaScreen(navController) }
        composable(Screen.Login.route) { LoginScreen(navController) }
        composable(Screen.RoleSelection.route) { RoleSelectionScreen(navController) }
        composable(Screen.Tnc.route) { TncScreen(navController) }
        composable(Screen.ForgotPassword.route) { ForgotPasswordScreen(navController) }

        // --- HANYA SATU BLOK COMPOSABLE UNTUK REGISTER ---
        composable(
            route = Screen.Register.route,
            arguments = listOf(navArgument("role") { type = NavType.StringType })
        ) { backStackEntry ->
            val role = backStackEntry.arguments?.getString("role") ?: "penumpang"
            if (role == "penumpang") {
                RegisterPassengerScreen(navController = navController)
            } else {
                // Arahkan ke awal alur registrasi driver
                navController.navigate(Screen.RegisterDriverGraph.route)
            }
        }

        // --- TAMBAHKAN NESTED NAVIGATION GRAPH UNTUK REGISTRASI DRIVER ---
        driverRegistrationGraph(navController)

        // --- BLOK YANG DIDUPLIKASI DAN SALAH SUDAH DIHAPUS ---

        composable(Screen.PassengerMain.route) { PassengerMainScreen(navController) }
        composable(Screen.DriverMain.route) {
            DriverMainScreen(navController)
        }

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
fun NavGraphBuilder.driverRegistrationGraph(navController: NavController) {
    navigation(startDestination = Screen.RegisterDriverStep1.route, route = Screen.RegisterDriverGraph.route) {
        composable(Screen.RegisterDriverStep1.route) {
            val viewModel = it.sharedViewModel<RegisterDriverViewModel>(navController)
            RegisterDriverStep1Screen(navController = navController, viewModel = viewModel)
        }
        composable(Screen.RegisterDriverStep2.route) {
            val viewModel = it.sharedViewModel<RegisterDriverViewModel>(navController)
            RegisterDriverStep2Screen(navController = navController, viewModel = viewModel)
        }
        composable(Screen.RegisterDriverStep3.route) {
            val viewModel = it.sharedViewModel<RegisterDriverViewModel>(navController)
            RegisterDriverStep3Screen(navController = navController, viewModel = viewModel)
        }
    }
}

// Fungsi helper untuk berbagi ViewModel di dalam nested graph
@Composable
inline fun <reified T : ViewModel> androidx.navigation.NavBackStackEntry.sharedViewModel(
    navController: NavController,
): T {
    val navGraphRoute = destination.parent?.route ?: return viewModel()
    val parentEntry = remember(this) {
        navController.getBackStackEntry(navGraphRoute)
    }
    return viewModel(parentEntry)
}







