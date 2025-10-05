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
import androidx.navigation.compose.rememberNavController
import com.example.jeksoed.R
import com.example.jeksoed.ui.screens.passenger.components.OrderSheetContent
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
import kotlinx.coroutines.tasks.await
import com.example.jeksoed.utils.bitmapDescriptorFromComposable
import com.google.android.gms.maps.model.BitmapDescriptor

/**
 * =================================================================================
 * 1. SMART COMPOSABLE (SCREEN-LEVEL)
 * =================================================================================
 * Tugasnya:
 * - Mengelola state dan ViewModel.
 * - Menangani semua logika (permintaan izin, lokasi, API key).
 * - Memanggil Dumb Composable (OrderScreenLayout) untuk menampilkan UI.
 */

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
    val context = LocalContext.current
    val uiState by orderViewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    var hasLocationPermission by remember { mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) }
    val cameraPositionState = rememberCameraPositionState { position = CameraPosition.fromLatLngZoom(LatLng(-7.431, 109.245), 15f) }
    val bottomSheetState = rememberBottomSheetScaffoldState()

    val expandSheet: () -> Unit = {
        scope.launch {
            bottomSheetState.bottomSheetState.expand()
        }
    }

    val placesClient = remember { Places.createClient(context) }
    val apiKey = remember {
        context.packageManager.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
            .metaData.getString("com.google.android.geo.API_KEY") ?: ""
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted -> hasLocationPermission = isGranted }
    )

    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    val isKeyboardOpen by rememberUpdatedState(WindowInsets.isImeVisible)
    LaunchedEffect(isKeyboardOpen) {
        if (isKeyboardOpen) {
            scope.launch {
                bottomSheetState.bottomSheetState.expand()
            }
        }
    }

    LaunchedEffect(key1 = hasLocationPermission) {
        if (hasLocationPermission) {
            try {
                val location = LocationServices.getFusedLocationProviderClient(context).lastLocation.await()
                location?.let {
                    val latLng = LatLng(it.latitude, it.longitude)
                    orderViewModel.setUserLocationAsPickup(latLng)
                    cameraPositionState.animate(CameraUpdateFactory.newCameraPosition(CameraPosition.fromLatLngZoom(latLng, 15f)))
                }
            } catch (e: Exception) { Log.e("OrderScreen", "Gagal mendapatkan lokasi", e) }
        } else {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    LaunchedEffect(uiState.routeInfo) {
        if (uiState.routeInfo != null) {
            val pickup = uiState.pickupLocation
            val destination = uiState.destinationLocation
            if (pickup != null && destination != null) {
                val bounds = LatLngBounds.builder().include(pickup).include(destination).build()
                cameraPositionState.animate(CameraUpdateFactory.newLatLngBounds(bounds, 150))
            }
        }
    }

    // Memanggil Dumb Composable untuk menampilkan UI
    OrderScreenLayout(
        uiState = uiState,
        cameraPositionState = cameraPositionState,
        bottomSheetState = bottomSheetState,
        sheetPeekHeight = when (uiState.stage) {
            OrderStage.SEARCHING -> screenHeight * 0.6f
            OrderStage.PICKUP_CONFIRM -> 220.dp
            OrderStage.ROUTE_CONFIRM -> screenHeight * 0.5f
            OrderStage.FINDING_DRIVER -> 300.dp
        },
        // Tentukan aksi berdasarkan stage saat ini
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

/**
 * =================================================================================
 * 2. DUMB COMPOSABLE (UI-ONLY)
 * =================================================================================
 * Tugasnya:
 * - Hanya menampilkan UI berdasarkan parameter yang diberikan.
 * - Tidak tahu-menahu tentang ViewModel atau logika bisnis.
 * - Mudah untuk di-preview.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OrderScreenLayout(
    uiState: OrderUiState,
    cameraPositionState: CameraPositionState,
    bottomSheetState: BottomSheetScaffoldState,
    sheetPeekHeight: Dp,
    onBackClick: () -> Unit,
    sheetContent: @Composable ColumnScope.() -> Unit
) {
    val context = LocalContext.current
    var pickupMarker by remember { mutableStateOf<BitmapDescriptor?>(null) }

    // --- BUAT MARKER SECARA ASYNC SAAT KOMPOSISI ---
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
        Box(modifier = Modifier.fillMaxSize().padding(padding).then( // Gunakan .then untuk menambahkan modifier secara kondisional
            if (uiState.stage == OrderStage.FINDING_DRIVER)
                Modifier.blur(radius = 8.dp)
            else
                Modifier
        )) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                uiSettings = MapUiSettings(zoomControlsEnabled = false)
            ) {
                // --- GUNAKAN MARKER KUSTOM ---
                if (pickupMarker != null) {
                    uiState.pickupLocation?.let {
                        Marker(
                            state = MarkerState(position = it),
                            title = "Lokasi Jemput",
                            icon = pickupMarker
                        )
                    }
                }

                // --- MARKER TUJUAN (BAWAAN) ---
                uiState.destinationLocation?.let {
                    Marker(
                        state = MarkerState(position = it),
                        title = "Lokasi Tujuan"
                        // Tidak ada 'icon', jadi pakai default
                    )
                }

                // --- TAMBAHKAN MARKER DRIVER ---
                uiState.driverLocations.forEach { driverLatLng ->
                    Marker(
                        state = MarkerState(position = driverLatLng),
                        title = "Driver",
                        icon = bitmapDescriptorFromVector(context, R.drawable.motor_icon)
                    )
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

            // Aturan untuk tombol kembali
            if (uiState.stage == OrderStage.ROUTE_CONFIRM) {
                // Tombol kembali di atas sheet untuk RouteConfirm
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
                // Tombol kembali di atas untuk semua stage lain, KECUALI FindingDriver
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
fun PickupMarkerComposable() {
    Box(
        modifier = Modifier.wrapContentSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        // Lingkaran luar (border transparan) dan dalam (biru)
        Box(
            modifier = Modifier
                .padding(top = 10.dp) // Beri ruang untuk foto profil
                .size(70.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.5f))
                .padding(4.dp)
                .clip(CircleShape)
                .background(Color(0xFF3386FF)) // Warna biru
        )

        // Foto Profil
        Image(
            painter = painterResource(id = R.drawable.person_icon), // Ganti dengan gambar profil asli jika ada
            contentDescription = "Profil",
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .border(3.dp, Color.White, CircleShape)
        )

        // Segitiga Pin di bawah
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
/**
 * =================================================================================
 * 3. PREVIEW
 * =================================================================================
 * Tugasnya:
 * - Memanggil Dumb Composable (OrderScreenLayout) dengan data palsu.
 * - Tidak akan crash karena tidak menginisialisasi komponen runtime.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun OrderScreenPreview() {
    JekSoedTheme {
        // Siapkan data dan state palsu untuk preview
        val dummyUiState = OrderUiState(stage = OrderStage.SEARCHING)
        val cameraPositionState = rememberCameraPositionState()
        val bottomSheetState = rememberBottomSheetScaffoldState()

        // Panggil OrderScreenLayout yang hanya butuh data, bukan ViewModel
        OrderScreenLayout(
            uiState = dummyUiState,
            cameraPositionState = cameraPositionState,
            bottomSheetState = bottomSheetState,
            sheetPeekHeight = 400.dp, // Tinggi tetap untuk preview
            onBackClick = {},
            sheetContent = {
                // Tampilkan placeholder sederhana untuk konten sheet
                Box(modifier = Modifier.fillMaxWidth().height(400.dp), contentAlignment = Alignment.Center) {
                    Text("Bottom Sheet Content Preview")
                }
            }
        )
    }
}