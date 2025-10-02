package com.example.jeksoed.ui.screens.passenger

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.jeksoed.data.model.RouteInfo
import com.example.jeksoed.navigation.Screen
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.maps.DirectionsApi
import com.google.maps.GeoApiContext
import com.google.maps.android.PolyUtil
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.model.TravelMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import androidx.compose.ui.tooling.preview.Preview
import com.example.jeksoed.ui.theme.JekSoedTheme
import com.example.jeksoed.utils.calculatePrice
import com.example.jeksoed.utils.formatCurrency

/**
 * =================================================================================
 * SMART COMPOSABLE
 * - Mengelola SEMUA state (lokasi, pencarian, rute, loading, dll).
 * - Menangani SEMUA logic & side effects (izin lokasi, API calls, Firestore writes).
 * =================================================================================
 */
@Composable
fun OrderScreen(
    navController: NavController,
    auth: FirebaseAuth = FirebaseAuth.getInstance(),
    firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current
    val placesClient: PlacesClient = remember { Places.createClient(context) }

    // --- State Management ---
    var hasLocationPermission by remember { mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) }
    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    val cameraPositionState = rememberCameraPositionState { position = CameraPosition.fromLatLngZoom(LatLng(-7.431, 109.245), 12f) } // Default Purwokerto

    var searchQuery by remember { mutableStateOf("") }
    var predictions by remember { mutableStateOf<List<AutocompletePrediction>>(emptyList()) }
    var destinationLocation by remember { mutableStateOf<LatLng?>(null) }
    var routeInfo by remember { mutableStateOf<RouteInfo?>(null) }
    var isSearching by remember { mutableStateOf(false) }
    var isCreatingOrder by remember { mutableStateOf(false) }

    // --- Logic & Side Effects ---
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted -> hasLocationPermission = isGranted }
    )

    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            try {
                val location = LocationServices.getFusedLocationProviderClient(context).lastLocation.await()
                if (location != null) {
                    userLocation = LatLng(location.latitude, location.longitude)
                }
            } catch (e: Exception) { Log.e("LocationError", "Gagal mendapatkan lokasi", e) }
        } else {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    LaunchedEffect(searchQuery) {
        if (searchQuery.length > 2) {
            isSearching = true
            delay(300L)
            try {
                val request = FindAutocompletePredictionsRequest.builder()
                    .setQuery(searchQuery)
                    .setCountries("ID")
                    .build()
                val response = placesClient.findAutocompletePredictions(request).await()
                predictions = response.autocompletePredictions
            } catch (e: Exception) {
                Log.e("PlacesAPI", "Gagal mencari prediksi", e)
            } finally {
                isSearching = false
            }
        } else {
            predictions = emptyList()
        }
    }

    LaunchedEffect(destinationLocation) {
        if (userLocation != null && destinationLocation != null) {
            coroutineScope.launch {
                try {
                    val apiKey = context.packageManager.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
                        .metaData.getString("com.google.android.geo.API_KEY")

                    val geoApiContext = GeoApiContext.Builder().apiKey(apiKey).build()
                    val directionsResult = withContext(Dispatchers.IO) {
                        DirectionsApi.newRequest(geoApiContext)
                            .origin(com.google.maps.model.LatLng(userLocation!!.latitude, userLocation!!.longitude))
                            .destination(com.google.maps.model.LatLng(destinationLocation!!.latitude, destinationLocation!!.longitude))
                            .mode(TravelMode.DRIVING)
                            .await()
                    }

                    if (directionsResult.routes.isNotEmpty()) {
                        val route = directionsResult.routes[0]
                        val leg = route.legs[0]
                        val points = PolyUtil.decode(route.overviewPolyline.encodedPath)
                        val priceValue = calculatePrice(leg.distance.inMeters)
                        val formattedPrice = formatCurrency(priceValue)

                        routeInfo = RouteInfo(
                            distance = leg.distance.humanReadable,
                            duration = leg.duration.humanReadable,
                            polylinePoints = points,
                            encodedPath = route.overviewPolyline.encodedPath,
                            price = formattedPrice
                        )
                    } else {
                        Toast.makeText(context, "Tidak dapat menemukan rute.", Toast.LENGTH_SHORT).show()
                    }

                } catch (e: Exception) {
                    Log.e("DirectionsAPI", "Gagal mendapatkan rute", e)
                    Toast.makeText(context, "Error saat mencari rute: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    LaunchedEffect(routeInfo) {
        if (routeInfo != null) {
            val bounds = LatLngBounds.builder()
                .include(userLocation!!)
                .include(destinationLocation!!)
                .build()
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngBounds(bounds, 150)
            )
        } else if (userLocation != null) {
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(userLocation!!, 15f)
            )
        }
    }

    OrderScreenUI(
        hasPermission = hasLocationPermission,
        cameraPositionState = cameraPositionState,
        userLocation = userLocation,
        destinationLocation = destinationLocation,
        routeInfo = routeInfo,
        searchQuery = searchQuery,
        isSearching = isSearching,
        predictions = predictions,
        isCreatingOrder = isCreatingOrder,
        onPermissionRequest = { permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION) },
        onSearchQueryChange = { newQuery -> searchQuery = newQuery },
        onClearSearch = { searchQuery = "" },
        onPredictionClick = { prediction ->
            keyboardController?.hide()
            val request = FetchPlaceRequest.newInstance(prediction.placeId, listOf(Place.Field.LAT_LNG))
            coroutineScope.launch {
                try {
                    val response = placesClient.fetchPlace(request).await()
                    destinationLocation = response.place.latLng
                    searchQuery = ""
                    predictions = emptyList()
                } catch (e: Exception) { Log.e("PlacesAPI", "Gagal fetch place", e) }
            }
        },
        onCreateOrderClick = {
            if (userLocation != null && destinationLocation != null && routeInfo != null) {
                isCreatingOrder = true
                val rideRequest = hashMapOf(
                    "passengerId" to auth.currentUser?.uid,
                    "pickupLocation" to hashMapOf(
                        "latitude" to userLocation!!.latitude,
                        "longitude" to userLocation!!.longitude
                    ),
                    "destinationLocation" to hashMapOf(
                        "latitude" to destinationLocation!!.latitude,
                        "longitude" to destinationLocation!!.longitude
                    ),
                    "distance" to routeInfo!!.distance,
                    "duration" to routeInfo!!.duration,
                    "status" to "pending",
                    "createdAt" to Timestamp.now(),
                    "driverId" to null,
                    "encodedPolyline" to routeInfo!!.encodedPath
                )
                firestore.collection("ride_requests").add(rideRequest)
                    .addOnSuccessListener { docRef ->
                        isCreatingOrder = false
                        navController.navigate(Screen.FindingDriver.createRoute(docRef.id)) {
                            popUpTo(Screen.CreateOrder.route) { inclusive = true }
                        }
                    }
                    .addOnFailureListener { e ->
                        isCreatingOrder = false
                        Toast.makeText(context, "Gagal membuat order: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            }
        }
    )
}

/**
 * =================================================================================
 * DUMB UI COMPOSABLE
 * - Bertanggung jawab untuk menyusun layout utama (Peta, Search bar, Info card).
 * - Menerima semua state dan meneruskan semua event.
 * =================================================================================
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderScreenUI(
    hasPermission: Boolean,
    cameraPositionState: com.google.maps.android.compose.CameraPositionState,
    userLocation: LatLng?,
    destinationLocation: LatLng?,
    routeInfo: RouteInfo?,
    searchQuery: String,
    isSearching: Boolean,
    predictions: List<AutocompletePrediction>,
    isCreatingOrder: Boolean,
    onPermissionRequest: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onClearSearch: () -> Unit,
    onPredictionClick: (AutocompletePrediction) -> Unit,
    onCreateOrderClick: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("JekSoed Penumpang") },
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (hasPermission) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState
                ) {
                    userLocation?.let { Marker(state = MarkerState(position = it), title = "Lokasi Saya") }
                    destinationLocation?.let { Marker(state = MarkerState(position = it), title = "Tujuan") }
                    routeInfo?.let { Polyline(points = it.polylinePoints, color = Color.Blue, width = 15f) }
                }
            } else {
                PermissionRequestUI(onRequest = onPermissionRequest)
            }

            SearchUI(
                searchQuery = searchQuery,
                isSearching = isSearching,
                predictions = predictions,
                onQueryChange = onSearchQueryChange,
                onClear = onClearSearch,
                onPredictionClick = onPredictionClick
            )

            AnimatedVisibility(visible = routeInfo != null, modifier = Modifier.align(Alignment.BottomCenter)) {
                RouteInfoCard(
                    routeInfo = routeInfo!!,
                    isCreatingOrder = isCreatingOrder,
                    onOrderClick = onCreateOrderClick
                )
            }
        }
    }
}

@Composable
private fun PermissionRequestUI(onRequest: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Izin lokasi dibutuhkan untuk menampilkan peta.")
        Button(onClick = onRequest) { Text("Berikan Izin") }
    }
}

@Composable
private fun SearchUI(
    searchQuery: String,
    isSearching: Boolean,
    predictions: List<AutocompletePrediction>,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    onPredictionClick: (AutocompletePrediction) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
        TextField(
            value = searchQuery,
            onValueChange = onQueryChange,
            placeholder = { Text("Mau ke mana?") },
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(8.dp))
                .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp)),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { keyboardController?.hide() }),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                disabledContainerColor = Color.White,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            trailingIcon = {
                if (isSearching) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = onClear) { Icon(Icons.Default.Close, "Hapus") }
                }
            }
        )
        AnimatedVisibility(visible = predictions.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
                    .border(1.dp, Color.LightGray, RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
            ) {
                items(predictions) { prediction ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPredictionClick(prediction) }
                            .padding(16.dp)
                    ) {
                        Text(prediction.getPrimaryText(null).toString(), fontWeight = FontWeight.Bold)
                        Text(prediction.getSecondaryText(null).toString(), fontSize = 12.sp)
                    }
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun RouteInfoCard(
    routeInfo: RouteInfo,
    isCreatingOrder: Boolean,
    onOrderClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Siap Berangkat?",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Estimasi Waktu", style = MaterialTheme.typography.bodySmall)
                    Text(routeInfo.duration, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Jarak Tempuh", style = MaterialTheme.typography.bodySmall)
                    Text(routeInfo.distance, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Harga", style = MaterialTheme.typography.bodySmall)
                    Text(routeInfo?.price ?: "-", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onOrderClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isCreatingOrder
            ) {
                if (isCreatingOrder) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Text("Pesan Sekarang")
                }
            }
        }
    }
}


// Data class sederhana untuk data palsu, karena AutocompletePrediction sulit dibuat manual.
private data class DummyPrediction(
    val primaryText: String,
    val secondaryText: String
)

// Data palsu untuk digunakan di berbagai preview
private val DUMMY_USER_LOCATION = LatLng(-7.431, 109.245) // Purwokerto
private val DUMMY_DESTINATION_LOCATION = LatLng(-7.420, 109.255)
private val DUMMY_ROUTE_INFO = RouteInfo(distance = "5.2 km", duration = "15 min", polylinePoints = emptyList(), encodedPath = "dummy_encoded_path_string", price = "Rp 11.000")
private val DUMMY_PREDICTIONS = listOf(
    DummyPrediction("Alun-Alun Purwokerto", "Jl. Jend. Soedirman, Purwokerto"),
    DummyPrediction("Stasiun Purwokerto", "Jl. Stasiun, Kober, Purwokerto Barat"),
    DummyPrediction("Rita Supermall Purwokerto", "Jl. Jend. Soedirman No. 296")
)


/**
 * =================================================================================
 * KUMPULAN PREVIEW
 * =================================================================================
 */

/**
 * Preview 1: Saat Izin Lokasi Belum Diberikan
 * Menampilkan tampilan awal yang meminta pengguna untuk memberikan izin.
 */
@Preview(name = "UI State: No Permission", showBackground = true)
@Composable
private fun OrderPreview_NoPermission() {
    JekSoedTheme {
        OrderScreenUI(
            hasPermission = false, // <-- State Kunci
            cameraPositionState = rememberCameraPositionState(),
            userLocation = null,
            destinationLocation = null,
            routeInfo = null,
            searchQuery = "",
            isSearching = false,
            predictions = emptyList(),
            isCreatingOrder = false,
            onPermissionRequest = {}, onSearchQueryChange = {}, onClearSearch = {},
            onPredictionClick = {}, onCreateOrderClick = {},
        )
    }
}

/**
 * Preview 2: Kondisi Awal (Peta Siap)
 * Menampilkan peta setelah izin diberikan, sebelum pengguna melakukan aksi apa pun.
 */
@Preview(name = "UI State: Idle With Map", showBackground = true)
@Composable
private fun OrderPreview_IdleWithMap() {
    JekSoedTheme {
        OrderScreenUI(
            hasPermission = true, // <-- State Kunci
            cameraPositionState = rememberCameraPositionState(),
            userLocation = DUMMY_USER_LOCATION, // <-- State Kunci
            destinationLocation = null,
            routeInfo = null,
            searchQuery = "",
            isSearching = false,
            predictions = emptyList(),
            isCreatingOrder = false,
            onPermissionRequest = {}, onSearchQueryChange = {}, onClearSearch = {},
            onPredictionClick = {}, onCreateOrderClick = {},
        )
    }
}

/**
 * Preview 3: Saat Pengguna Mencari Lokasi
 * Menampilkan daftar hasil pencarian (predictions) di bawah search bar.
 * Kita buat preview khusus untuk SearchUI karena object `AutocompletePrediction` sulit dibuat.
 */
@Preview(name = "Component State: Searching", showBackground = true)
@Composable
private fun SearchUI_Preview_WithPredictions() {
    JekSoedTheme {
        // Kita preview komponen SearchUI secara terisolasi
        Column {
            TextField(
                value = "Purwokerto",
                onValueChange = {},
                placeholder = { Text("Mau ke mana?") },
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            )
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .background(Color.White)
                    .border(1.dp, Color.LightGray)
            ) {
                items(DUMMY_PREDICTIONS) { prediction ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(prediction.primaryText, fontWeight = FontWeight.Bold)
                        Text(prediction.secondaryText, fontSize = 12.sp)
                    }
                    HorizontalDivider()
                }
            }
        }
    }
}


/**
 * Preview 4: Rute Ditemukan & Siap Pesan
 * Ini adalah "happy path" utama, menampilkan kartu informasi rute di bagian bawah.
 */
@Preview(name = "UI State: Route Found", showBackground = true)
@Composable
private fun OrderPreview_RouteFound() {
    JekSoedTheme {
        OrderScreenUI(
            hasPermission = true,
            cameraPositionState = rememberCameraPositionState(),
            userLocation = DUMMY_USER_LOCATION,
            destinationLocation = DUMMY_DESTINATION_LOCATION,
            routeInfo = DUMMY_ROUTE_INFO, // <-- State Kunci
            searchQuery = "",
            isSearching = false,
            predictions = emptyList(),
            isCreatingOrder = false, // <-- State Kunci
            onPermissionRequest = {}, onSearchQueryChange = {}, onClearSearch = {},
            onPredictionClick = {}, onCreateOrderClick = {},
        )
    }
}

/**
 * Preview 5: Saat Proses Pembuatan Order
 * Menampilkan indikator loading di tombol "Pesan Sekarang".
 */
@Preview(name = "UI State: Creating Order", showBackground = true)
@Composable
private fun OrderPreview_CreatingOrder() {
    JekSoedTheme {
        OrderScreenUI(
            hasPermission = true,
            cameraPositionState = rememberCameraPositionState(),
            userLocation = DUMMY_USER_LOCATION,
            destinationLocation = DUMMY_DESTINATION_LOCATION,
            routeInfo = DUMMY_ROUTE_INFO,
            searchQuery = "",
            isSearching = false,
            predictions = emptyList(),
            isCreatingOrder = true, // <-- State Kunci
            onPermissionRequest = {}, onSearchQueryChange = {}, onClearSearch = {},
            onPredictionClick = {}, onCreateOrderClick = {},
        )
    }
}