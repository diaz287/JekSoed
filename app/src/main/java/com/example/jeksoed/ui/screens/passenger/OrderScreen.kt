package com.example.jeksoed.ui.screens.passenger

import android.Manifest
import android.content.pm.PackageManager
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
import androidx.compose.ui.layout.ContentScale
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
import coil.compose.AsyncImage
import com.example.jeksoed.R
import com.example.jeksoed.navigation.Screen
import com.example.jeksoed.ui.screens.passenger.components.OrderSheetContent
import com.example.jeksoed.ui.screens.passenger.components.SearchStage
import com.example.jeksoed.ui.theme.JekSoedTheme
import com.example.jeksoed.utils.bitmapDescriptorFromComposable
import com.example.jeksoed.utils.bitmapDescriptorFromVector
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.libraries.places.api.Places
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch

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
    orderViewModel: OrderViewModel = viewModel()
) {
    val uiState by orderViewModel.uiState.collectAsState()
    val userLocation by orderViewModel.userLocation.collectAsState()
    val cameraPositionState = rememberCameraPositionState()
    val bottomSheetState = rememberBottomSheetScaffoldState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val fusedLocationProviderClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    var hasLocationPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted -> hasLocationPermission = isGranted }
    )

    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            orderViewModel.getCurrentLocation(fusedLocationProviderClient)
        } else {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    LaunchedEffect(userLocation) {
        userLocation?.let {
            if (cameraPositionState.position.target.latitude == 0.0 && cameraPositionState.position.target.longitude == 0.0) {
                cameraPositionState.animate(
                    update = CameraUpdateFactory.newLatLngZoom(it, 15f),
                    durationMs = 1000
                )
            }
        }
    }

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

    LaunchedEffect(uiState.stage, uiState.activeRideRequestId) {
        if (uiState.stage == OrderStage.FINDING_DRIVER && uiState.activeRideRequestId != null) {
            orderViewModel.listenToActiveRide(uiState.activeRideRequestId!!) { rideId ->
                navController.navigate(Screen.Trip.createRoute(rideId)) {
                    popUpTo(Screen.CreateOrder.route) { inclusive = true }
                }
            }
        }
    }

    val placesClient = remember { Places.createClient(context) }
    val apiKey = remember {
        context.packageManager.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
            .metaData.getString("com.google.android.geo.API_KEY") ?: ""
    }

    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    val expandSheet: () -> Unit = { scope.launch { bottomSheetState.bottomSheetState.expand() } }

    OrderScreenLayout(
        uiState = uiState,
        userLocation = userLocation,
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
                onTextFieldFocus = expandSheet,
                onCreateOrderClick = {
                    orderViewModel.createOrder()
                }
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OrderScreenLayout(
    uiState: OrderUiState,
    userLocation: LatLng?,
    cameraPositionState: CameraPositionState,
    bottomSheetState: BottomSheetScaffoldState,
    sheetPeekHeight: Dp,
    onBackClick: () -> Unit,
    sheetContent: @Composable ColumnScope.() -> Unit
) {
    val context = LocalContext.current
    var pickupMarker by remember { mutableStateOf<BitmapDescriptor?>(null) }

    // Efek ini akan dijalankan ulang setiap kali URL foto profil pengguna berubah.
    // Ini memastikan marker diperbarui jika pengguna mengganti foto profilnya.
    LaunchedEffect(uiState.userPhotoUrl) {
        pickupMarker = bitmapDescriptorFromComposable(context) {
            PickupMarkerComposable(photoUrl = uiState.userPhotoUrl)
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
                // --- AWAL PERBAIKAN LOGIKA MARKER ---

                // Tampilkan marker kustom untuk lokasi JEMPUT jika lokasinya sudah ada.
                // Parameter 'icon' akan menangani pembaruan dari null (default) ke ikon kustom
                // setelah 'pickupMarker' selesai dibuat.
                uiState.pickupLocation?.let { location ->
                    Marker(
                        state = MarkerState(position = location),
                        title = "Lokasi Jemput",
                        icon = pickupMarker
                    )
                }

                // Tampilkan marker default untuk lokasi TUJUAN jika sudah ada.
                uiState.destinationLocation?.let { location ->
                    Marker(
                        state = MarkerState(position = location),
                        title = "Lokasi Tujuan"
                    )
                }

                // --- AKHIR PERBAIKAN LOGIKA MARKER ---

                uiState.driverLocations.forEach { driverLatLng ->
                    Marker(state = MarkerState(position = driverLatLng), title = "Driver", icon = bitmapDescriptorFromVector(context, R.drawable.motor_icon))
                }

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

@Composable
fun PickupMarkerComposable(photoUrl: String?) {
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
        AsyncImage(
            model = photoUrl,
            contentDescription = "Profil",
            placeholder = painterResource(id = R.drawable.person_icon),
            error = painterResource(id = R.drawable.person_icon),
            contentScale = ContentScale.Crop,
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
@Preview(showSystemUi = true, showBackground = true)
@Composable
private fun OrderScreenPreview() {
    JekSoedTheme {
        val dummyUiState = OrderUiState(stage = OrderStage.SEARCHING)
        val cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(LatLng(-7.432, 109.244), 15f)
        }
        val bottomSheetState = rememberBottomSheetScaffoldState()

        OrderScreenLayout(
            uiState = dummyUiState,
            userLocation = LatLng(-7.432, 109.244),
            cameraPositionState = cameraPositionState,
            bottomSheetState = bottomSheetState,
            sheetPeekHeight = 400.dp,
            onBackClick = {},
            sheetContent = {
                SearchStage(
                    uiState = dummyUiState,
                    viewModel = null,
                    placesClient = null,
                    onTextFieldFocus = {},
                    context = LocalContext.current,
                    apiKey = ""
                )
            }
        )
    }
}