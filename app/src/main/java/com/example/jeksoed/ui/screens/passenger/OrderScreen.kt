package com.example.jeksoed.ui.screens.passenger

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.jeksoed.R
import com.example.jeksoed.ui.theme.JekSoedTheme
import com.example.jeksoed.utils.bitmapDescriptorFromVector
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.libraries.places.api.Places
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch
import com.example.jeksoed.utils.bitmapDescriptorFromComposable
import com.google.android.gms.maps.model.BitmapDescriptor
import com.example.jeksoed.ui.screens.passenger.OrderStage
import com.example.jeksoed.ui.screens.passenger.OrderUiState
import com.example.jeksoed.ui.screens.passenger.OrderViewModel
import com.example.jeksoed.ui.screens.passenger.components.OrderSheetContent

// Bagian TopRouteInfoBar dan RouteInfoRow tidak diubah, tetap sama.
@Composable
private fun TopRouteInfoBar(pickup: String, destination: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 48.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)) {
            RouteInfoRow(icon = R.drawable.blue_icon, text = pickup)
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp, horizontal = 12.dp))
            RouteInfoRow(icon = R.drawable.locatio_icon, text = destination)
        }
    }
}

@Composable
private fun RouteInfoRow(icon: Int, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp)
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = Color.Unspecified
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = text, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun OrderScreen(
    navController: NavController,
    orderViewModel: OrderViewModel = viewModel() // Cukup satu instance ViewModel
) {
    val uiState by orderViewModel.uiState.collectAsState()
    val userLocation by orderViewModel.userLocation.collectAsState() // Ambil lokasi dari ViewModel
    val cameraPositionState = rememberCameraPositionState()
    val bottomSheetState = rememberBottomSheetScaffoldState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val fusedLocationProviderClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    // State untuk izin lokasi
    var hasLocationPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
    }

    // Launcher untuk meminta izin
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted -> hasLocationPermission = isGranted }
    )

    // --- MENGELOLA LOGIKA LOKASI DAN KAMERA ---

    // 1. Minta izin jika belum ada, lalu dapatkan lokasi awal
    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            orderViewModel.getCurrentLocation(fusedLocationProviderClient)
        } else {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    // 2. Animasikan kamera ke lokasi pengguna saat pertama kali didapatkan
    LaunchedEffect(userLocation) {
        userLocation?.let {
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(it, 15f),
                durationMs = 1000
            )
        }
    }

    // 3. Animasikan kamera untuk menunjukkan rute saat sudah ada
    LaunchedEffect(uiState.routeInfo) {
        uiState.routeInfo?.let {
            val pickup = uiState.pickupLocation
            val destination = uiState.destinationLocation
            if (pickup != null && destination != null) {
                val bounds = LatLngBounds.builder().include(pickup).include(destination).build()
                cameraPositionState.animate(CameraUpdateFactory.newLatLngBounds(bounds, 150))
            }
        }
    }

    // Navigasi ke TripScreen jika ada perjalanan aktif
    LaunchedEffect(Unit) {
        orderViewModel.listenToActiveRide { rideId ->
            navController.navigate("trip/$rideId") {
                popUpTo("order") { inclusive = true }
            }
        }
    }

    // Aksi untuk membuka bottom sheet
    val expandSheet: () -> Unit = { scope.launch { bottomSheetState.bottomSheetState.expand() } }

    val placesClient = remember { Places.createClient(context) }
    val apiKey = remember {
        context.packageManager.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
            .metaData.getString("com.google.android.geo.API_KEY") ?: ""
    }

    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    // Menampilkan UI menggunakan Dumb Composable
    OrderScreenLayout(
        uiState = uiState,
        userLocation = userLocation, // Kirim lokasi user ke layout
        cameraPositionState = cameraPositionState,
        bottomSheetState = bottomSheetState,
        sheetPeekHeight = when (uiState.stage) {
            OrderStage.SEARCHING -> screenHeight * 0.6f
            OrderStage.PICKUP_CONFIRM -> 220.dp
            OrderStage.ROUTE_CONFIRM -> screenHeight * 0.5f
            OrderStage.FINDING_DRIVER -> 300.dp
        },
        onBackClick = {
            if (uiState.stage == OrderStage.ROUTE_CONFIRM || uiState.stage == OrderStage.PICKUP_CONFIRM) {
                orderViewModel.goBackToSearch()
            } else {
                navController.popBackStack()
            }
        },
        sheetContent = {
            OrderSheetContent(
                uiState = uiState,
                viewModel = orderViewModel,
                placesClient = placesClient,
                apiKey = apiKey,
                onTextFieldFocus = expandSheet
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OrderScreenLayout(
    uiState: OrderUiState,
    userLocation: LatLng?, // Terima lokasi user
    cameraPositionState: CameraPositionState,
    bottomSheetState: BottomSheetScaffoldState,
    sheetPeekHeight: Dp,
    onBackClick: () -> Unit,
    sheetContent: @Composable ColumnScope.() -> Unit
) {
    val context = LocalContext.current
    var pickupMarker by remember { mutableStateOf<BitmapDescriptor?>(null) }

    LaunchedEffect(Unit) {
        pickupMarker = bitmapDescriptorFromComposable(context) {
            PickupMarkerComposable()
        }
    }

    BottomSheetScaffold(
        scaffoldState = bottomSheetState,
        sheetPeekHeight = sheetPeekHeight,
        sheetShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        sheetContent = sheetContent
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            GoogleMap(
                modifier = Modifier.fillMaxSize().then(
                    if (uiState.stage == OrderStage.FINDING_DRIVER) Modifier.blur(radius = 8.dp)
                    else Modifier
                ),
                cameraPositionState = cameraPositionState,
                uiSettings = MapUiSettings(zoomControlsEnabled = false)
            ) {
                // PERBAIKAN: Tampilkan marker lokasi user saat pencarian
                if (uiState.stage == OrderStage.SEARCHING) {
                    userLocation?.let {
                        Marker(
                            state = MarkerState(position = it),
                            title = "Lokasi Anda"
                            // Anda bisa menambahkan ikon kustom di sini jika mau
                        )
                    }
                }

                // Marker untuk lokasi jemput (setelah dipilih)
                if (pickupMarker != null) {
                    uiState.pickupLocation?.let {
                        Marker(
                            state = MarkerState(position = it),
                            title = "Lokasi Jemput",
                            icon = pickupMarker
                        )
                    }
                }

                // Marker tujuan
                uiState.destinationLocation?.let {
                    Marker(
                        state = MarkerState(position = it),
                        title = "Lokasi Tujuan"
                    )
                }

                // Marker driver di sekitar
                uiState.driverLocations.forEach { driverLatLng ->
                    Marker(
                        state = MarkerState(position = driverLatLng),
                        title = "Driver",
                        icon = bitmapDescriptorFromVector(context, R.drawable.motor_icon)
                    )
                }

                // PERBAIKAN: Polyline akan otomatis tampil saat routeInfo ada
                // Ini berlaku untuk stage PICKUP_CONFIRM dan ROUTE_CONFIRM
                uiState.routeInfo?.let {
                    Polyline(points = it.polylinePoints, color = MaterialTheme.colorScheme.primary, width = 15f)
                }
            }

            if (uiState.stage == OrderStage.ROUTE_CONFIRM || uiState.stage == OrderStage.FINDING_DRIVER) {
                TopRouteInfoBar(
                    pickup = uiState.pickupQuery,
                    destination = uiState.destinationQuery
                )
            }

            if (uiState.stage == OrderStage.ROUTE_CONFIRM) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 16.dp, bottom = sheetPeekHeight + 16.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                }
            } else if (uiState.stage != OrderStage.FINDING_DRIVER) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .align(Alignment.TopStart)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                }
            }
        }
    }
}

// Composable PickupMarkerComposable dan Preview tidak diubah, tetap sama.
@Composable
fun PickupMarkerComposable() {
    Box(
        modifier = Modifier.wrapContentSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Box(
            modifier = Modifier
                .padding(top = 10.dp)
                .size(70.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.5f))
                .padding(4.dp)
                .clip(CircleShape)
                .background(Color(0xFF3386FF))
        )
        Image(
            painter = painterResource(id = R.drawable.person_icon),
            contentDescription = "Profil",
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .border(3.dp, Color.White, CircleShape)
        )
        Canvas(modifier = Modifier
            .size(20.dp, 10.dp)
            .align(Alignment.BottomCenter)
            .offset(y = (4).dp)
        ) {
            val path = Path().apply {
                moveTo(size.width / 2f, size.height)
                lineTo(0f, 0f)
                lineTo(size.width, 0f)
                close()
            }
            drawPath(path, color = Color.White)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun OrderScreenPreview() {
    JekSoedTheme {
        val dummyUiState = OrderUiState(stage = OrderStage.SEARCHING)
        val cameraPositionState = rememberCameraPositionState()
        val bottomSheetState = rememberBottomSheetScaffoldState()

        OrderScreenLayout(
            uiState = dummyUiState,
            userLocation = LatLng(0.0, 0.0), // Beri lokasi dummy untuk preview
            cameraPositionState = cameraPositionState,
            bottomSheetState = bottomSheetState,
            sheetPeekHeight = 400.dp,
            onBackClick = {},
            sheetContent = {
                Box(modifier = Modifier.fillMaxWidth().height(400.dp), contentAlignment = Alignment.Center) {
                    Text("Bottom Sheet Content Preview")
                }
            }
        )
    }
}