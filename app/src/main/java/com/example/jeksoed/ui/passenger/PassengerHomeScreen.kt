package com.example.jeksoed.ui.passenger

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
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import com.example.jeksoed.model.RouteInfo
import com.example.jeksoed.navigation.Screen
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
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

@Composable
fun PassengerHomeScreen(navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current
    var isCreatingOrder by remember { mutableStateOf(false) }

    val placesClient: PlacesClient = remember { com.google.android.libraries.places.api.Places.createClient(context) }

    // --- State Management ---
    var hasLocationPermission by remember { mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) }
    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(-6.933, 109.667), 12f) // Default Pemalang
    }

    // State untuk pencarian & rute
    var searchQuery by remember { mutableStateOf("") }
    var predictions by remember { mutableStateOf<List<AutocompletePrediction>>(emptyList()) }
    var destinationLocation by remember { mutableStateOf<LatLng?>(null) }
    var routeInfo by remember { mutableStateOf<RouteInfo?>(null) }
    var isSearching by remember { mutableStateOf(false) }


    // --- Logic ---
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

    // Efek untuk memanggil API pencarian saat user mengetik
    LaunchedEffect(searchQuery) {
        if (searchQuery.length > 2) {
            isSearching = true
            // Debounce: Tunda 300ms sebelum benar-benar mencari
            kotlinx.coroutines.delay(300L)
            try {
                val request = FindAutocompletePredictionsRequest.builder()
                    .setQuery(searchQuery)
                    .setCountries("ID") // Fokus pencarian di Indonesia
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

    //untuk mengambil data rute saat tujuan berubah
    LaunchedEffect(destinationLocation) {
        if (userLocation != null && destinationLocation != null) {
            coroutineScope.launch {
                try {
                    // Ambil API Key dari local.properties
                    val apiKey = context.packageManager.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
                        .metaData.getString("com.google.android.geo.API_KEY")

                    // Lakukan pemanggilan API di background thread
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
                        routeInfo = RouteInfo(
                            distance = leg.distance.humanReadable,
                            duration = leg.duration.humanReadable,
                            polylinePoints = points,
                            encodedPath = route.overviewPolyline.encodedPath
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

    // Efek untuk menyesuaikan zoom kamera
    LaunchedEffect(routeInfo) {
        if (routeInfo != null) {
            val bounds = LatLngBounds.builder()
                .include(userLocation!!)
                .include(destinationLocation!!)
                .build()
            cameraPositionState.animate(
                com.google.android.gms.maps.CameraUpdateFactory.newLatLngBounds(bounds, 150) // padding 150px
            )
        } else if (userLocation != null) {
            cameraPositionState.animate(
                com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(userLocation!!, 15f)
            )
        }
    }


    // --- UI ---
    Box(modifier = Modifier.fillMaxSize()) {
        if (hasLocationPermission) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState
            ) {
                userLocation?.let { Marker(state = MarkerState(position = it), title = "Lokasi Saya") }
                destinationLocation?.let { Marker(state = MarkerState(position = it), title = "Tujuan") }
                // Gambar garis biru (Polyline) jika data rute sudah ada
                routeInfo?.let {
                    Polyline(points = it.polylinePoints, color = Color.Blue, width = 15f)
                }
            }
        } else {
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Izin lokasi dibutuhkan untuk menampilkan peta.")
                Button(onClick = { permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION) }) { Text("Berikan Izin") }
            }
        }

        // --- UI Pencarian di atas Peta ---
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
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
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Hapus")
                        }
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
                        Column(modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                keyboardController?.hide()
                                val placeId = prediction.placeId
                                val placeFields = listOf(Place.Field.LAT_LNG)
                                val request = FetchPlaceRequest.newInstance(placeId, placeFields)
                                coroutineScope.launch {
                                    try {
                                        val response = placesClient.fetchPlace(request).await()
                                        destinationLocation = response.place.latLng
                                        searchQuery = "" // Kosongkan pencarian setelah memilih
                                        predictions = emptyList() // Sembunyikan daftar
                                    } catch (e: Exception) {
                                        Log.e("PlacesAPI", "Gagal mengambil detail lokasi", e)
                                    }
                                }
                            }
                            .padding(16.dp)
                        ) {
                            Text(prediction.getPrimaryText(null).toString(), fontWeight = FontWeight.Bold)
                            Text(prediction.getSecondaryText(null).toString(), fontSize = 12.sp)
                        }
                        Divider()
                    }
                }
            }
        }

        // --- UI Kartu Informasi Rute & Tombol Pesan ---
        AnimatedVisibility(visible = routeInfo != null, modifier = Modifier.align(Alignment.BottomCenter)) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
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
                            Text(routeInfo?.duration ?: "-", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Jarak Tempuh", style = MaterialTheme.typography.bodySmall)
                            Text(routeInfo?.distance ?: "-", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            if (userLocation != null && destinationLocation != null && routeInfo != null) {
                                isCreatingOrder = true
                                // Kumpulkan data untuk disimpan ke Firestore
                                val rideRequest = hashMapOf(
                                    "passengerId" to FirebaseAuth.getInstance().currentUser?.uid,
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
                                    "status" to "pending", // Status awal
                                    "createdAt" to Timestamp.now(),
                                    "driverId" to null,
                                    "encodedPolyline" to routeInfo!!.encodedPath
                                )

                                // Simpan ke Firestore
                                val db = FirebaseFirestore.getInstance()
                                db.collection("ride_requests")
                                    .add(rideRequest)
                                    .addOnSuccessListener { documentReference ->
                                        isCreatingOrder = false
                                        navController.navigate(Screen.FindingDriver.createRoute(documentReference.id)) {
                                            popUpTo(Screen.PassengerHome.route) { inclusive = true }
                                        }
                                    }
                                    .addOnFailureListener { e ->
                                        isCreatingOrder = false
                                        Log.w("Firestore", "Error adding document", e)
                                        Toast.makeText(context, "Gagal membuat permintaan: ${e.message}", Toast.LENGTH_LONG).show()
                                    }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isCreatingOrder
                    ) {
                        if (isCreatingOrder) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Pesan Sekarang")
                        }
                    }
                }
            }
        }
    }
}