package com.example.jeksoed.ui.screens.passenger

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jeksoed.data.model.RouteInfo
import com.example.jeksoed.utils.calculatePrice
import com.example.jeksoed.utils.formatCurrency
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.maps.DirectionsApi
import com.google.maps.GeoApiContext
import com.google.maps.android.PolyUtil
import com.google.maps.model.TravelMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

data class SavedPlace(
    val title: String,
    val address: String,
    val distance: String
)

enum class OrderStage {
    SEARCHING,
    PICKUP_CONFIRM,
    ROUTE_CONFIRM,
    FINDING_DRIVER
}

data class OrderUiState(
    val stage: OrderStage = OrderStage.SEARCHING,
    val pickupQuery: String = "Lokasi saat ini",
    val pickupAddress: String = "",
    val destinationQuery: String = "",
    val pickupLocation: LatLng? = null,
    val destinationLocation: LatLng? = null,
    val predictions: List<AutocompletePrediction> = emptyList(),
    val savedPlaces: List<SavedPlace> = emptyList(),
    val isSearchingPickup: Boolean = false,
    val isSearchingDestination: Boolean = false,
    val routeInfo: RouteInfo? = null,
    val isRouteLoading: Boolean = false,
    val driverLocations: List<LatLng> = emptyList(),
    val activeRideRequestId: String? = null,
    val userPhotoUrl: String? = null
)

class OrderViewModel : ViewModel() {
    private val _userLocation = MutableStateFlow<LatLng?>(null)
    val userLocation = _userLocation.asStateFlow()

    fun getCurrentLocation(fusedLocationProviderClient: FusedLocationProviderClient) {
        try {
            fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val latLng = LatLng(location.latitude, location.longitude)
                    _userLocation.value = latLng
                    if (_uiState.value.pickupLocation == null) {
                        setUserLocationAsPickup(latLng)
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e("OrderViewModel", "Location permission missing", e)
        }
    }

    private val _uiState = MutableStateFlow(OrderUiState())
    val uiState = _uiState.asStateFlow()

    private var searchJob: Job? = null
    private var activeRideListener: ListenerRegistration? = null

    // --- PERBAIKAN: Menggabungkan kedua blok init ---
    init {
        fetchCurrentUserData()

        val dummySavedPlaces = listOf(
            SavedPlace("RITA SuperMall Purwokerto", "Jl. Jend. Sudirman No.296, Pereng, Sokanegara, Kec. Purwokerto Tim., Kabupaten Banyumas", "3.6 Km"),
            SavedPlace("RSU Wiradadi Husada", "Jl. Menteri Supeno No.25, Dusun I Wiradadi, Kec. Sokaraja, Kabupaten Banyumas", "3.6 Km")
        )
        val dummyDriverLocations = listOf(
            LatLng(-7.430, 109.246),
            LatLng(-7.432, 109.244)
        )
        _uiState.update { it.copy(savedPlaces = dummySavedPlaces, driverLocations = dummyDriverLocations) }
    }

    private fun fetchCurrentUserData() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) return

        viewModelScope.launch {
            try {
                val document = FirebaseFirestore.getInstance().collection("users").document(userId).get().await()
                val photoUrl = document.getString("photoUrl")
                _uiState.update { it.copy(userPhotoUrl = photoUrl) }
            } catch (e: Exception) {
                Log.e("OrderViewModel", "Gagal mengambil data pengguna", e)
            }
        }
    }

    fun onPickupQueryChange(query: String, placesClient: PlacesClient) {
        _uiState.update { it.copy(pickupQuery = query, isSearchingPickup = true, isSearchingDestination = false, predictions = emptyList()) }
        searchPlaces(query, placesClient)
    }

    fun onDestinationQueryChange(query: String, placesClient: PlacesClient) {
        _uiState.update { it.copy(destinationQuery = query, isSearchingDestination = true, isSearchingPickup = false, predictions = emptyList()) }
        searchPlaces(query, placesClient)
    }

    fun clearQuery(isPickup: Boolean) {
        if (isPickup) {
            _uiState.update { it.copy(pickupQuery = "", pickupLocation = null, predictions = emptyList()) }
        } else {
            _uiState.update { it.copy(destinationQuery = "", destinationLocation = null, predictions = emptyList()) }
        }
    }

    fun setUserLocationAsPickup(location: LatLng) {
        _uiState.update { it.copy(pickupLocation = location, pickupQuery = "Lokasi saat ini", pickupAddress = "Menggunakan lokasi Anda saat ini") }
    }

    fun selectPrediction(prediction: AutocompletePrediction, placesClient: PlacesClient, context: Context, apiKey: String) {
        viewModelScope.launch {
            try {
                val request = FetchPlaceRequest.newInstance(prediction.placeId, listOf(Place.Field.LAT_LNG, Place.Field.NAME, Place.Field.ADDRESS))
                val response = placesClient.fetchPlace(request).await()
                val location = response.place.latLng ?: return@launch
                val name = response.place.name ?: ""
                val address = response.place.address ?: ""

                if (_uiState.value.isSearchingPickup) {
                    _uiState.update { it.copy(pickupLocation = location, pickupQuery = name, pickupAddress = address, predictions = emptyList(), isSearchingPickup = false) }
                } else {
                    _uiState.update { it.copy(destinationLocation = location, destinationQuery = name, predictions = emptyList(), isSearchingDestination = false) }
                }

                if (_uiState.value.pickupLocation != null && _uiState.value.destinationLocation != null) {
                    _uiState.update { it.copy(stage = OrderStage.PICKUP_CONFIRM) }
                }

            } catch (e: Exception) {
                Log.e("OrderViewModel", "Gagal fetch place details", e)
            }
        }
    }

    private fun searchPlaces(query: String, placesClient: PlacesClient) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300L)
            if (query.length > 2) {
                try {
                    val request = FindAutocompletePredictionsRequest.builder().setQuery(query).setCountries("ID").build()
                    val response = placesClient.findAutocompletePredictions(request).await()
                    _uiState.update { it.copy(predictions = response.autocompletePredictions) }
                } catch (e: Exception) {
                    Log.e("OrderViewModel", "Gagal mencari prediksi", e)
                }
            } else {
                _uiState.update { it.copy(predictions = emptyList()) }
            }
        }
    }

    fun findRoute(context: Context, apiKey: String) {
        val pickup = _uiState.value.pickupLocation
        val destination = _uiState.value.destinationLocation
        if (pickup == null || destination == null) return

        _uiState.update { it.copy(isRouteLoading = true, stage = OrderStage.ROUTE_CONFIRM) }
        viewModelScope.launch {
            try {
                val geoApiContext = GeoApiContext.Builder().apiKey(apiKey).build()
                val directionsResult = withContext(Dispatchers.IO) {
                    DirectionsApi.newRequest(geoApiContext)
                        .origin(com.google.maps.model.LatLng(pickup.latitude, pickup.longitude))
                        .destination(com.google.maps.model.LatLng(destination.latitude, destination.longitude))
                        .mode(TravelMode.DRIVING).await()
                }
                if (directionsResult.routes.isNotEmpty()) {
                    val route = directionsResult.routes[0]
                    val leg = route.legs[0]
                    val points = PolyUtil.decode(route.overviewPolyline.encodedPath)
                    val priceValue = calculatePrice(leg.distance.inMeters)
                    val formattedPrice = formatCurrency(priceValue)

                    _uiState.update {
                        it.copy(
                            isRouteLoading = false,
                            routeInfo = RouteInfo(
                                distance = leg.distance.humanReadable,
                                duration = leg.duration.humanReadable,
                                polylinePoints = points,
                                encodedPath = route.overviewPolyline.encodedPath,
                                price = formattedPrice
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("OrderViewModel", "Gagal mendapatkan rute", e)
                _uiState.update { it.copy(isRouteLoading = false) }
            }
        }
    }

    fun goBackToSearch() {
        _uiState.update { it.copy(stage = OrderStage.SEARCHING, routeInfo = null, destinationLocation = null, destinationQuery = "") }
    }

    fun createOrder() {
        val user = FirebaseAuth.getInstance().currentUser
        val pickup = _uiState.value.pickupLocation
        val destination = _uiState.value.destinationLocation
        val route = _uiState.value.routeInfo

        if (user == null || pickup == null || destination == null || route == null) {
            Log.e("OrderViewModel", "Data order belum lengkap!")
            return
        }

        viewModelScope.launch {
            try {
                val firestore = FirebaseFirestore.getInstance()
                val orderData = mapOf(
                    "passengerId" to user.uid,
                    "pickupName" to _uiState.value.pickupQuery,
                    "pickupAddress" to _uiState.value.pickupAddress,
                    "pickupLocation" to mapOf("latitude" to pickup.latitude, "longitude" to pickup.longitude),
                    "destinationName" to _uiState.value.destinationQuery,
                    "destinationAddress" to "",
                    "destinationLocation" to mapOf("latitude" to destination.latitude, "longitude" to destination.longitude),
                    "distance" to route.distance,
                    "duration" to route.duration,
                    "price" to route.price,
                    "status" to "pending",
                    "createdAt" to com.google.firebase.Timestamp.now(),
                    "encodedPolyline" to route.encodedPath
                )
                val documentReference = firestore.collection("ride_requests").add(orderData).await()
                Log.d("OrderViewModel", "Pesanan berhasil dibuat dengan ID: ${documentReference.id}")

                _uiState.update {
                    it.copy(
                        stage = OrderStage.FINDING_DRIVER,
                        activeRideRequestId = documentReference.id
                    )
                }
            } catch (e: Exception) {
                Log.e("OrderViewModel", "Gagal membuat order", e)
            }
        }
    }

    fun cancelFindingDriver() {
        val rideId = _uiState.value.activeRideRequestId
        if (rideId == null) {
            // Jika tidak ada ID, kembali saja ke state sebelumnya
            _uiState.update { it.copy(stage = OrderStage.ROUTE_CONFIRM, activeRideRequestId = null) }
            return
        }

        viewModelScope.launch {
            try {
                // Update status di Firestore menjadi "cancelled"
                FirebaseFirestore.getInstance().collection("ride_requests").document(rideId)
                    .update("status", "cancelled")
                    .await()
                Log.d("OrderViewModel", "Orderan dengan ID $rideId berhasil dibatalkan.")
            } catch (e: Exception) {
                Log.e("OrderViewModel", "Gagal membatalkan orderan di Firestore", e)
                // Tetap lanjutkan meski gagal update di DB agar UI tidak stuck
            } finally {
                // Kembali ke tahap sebelumnya (konfirmasi rute) dan hapus ID aktif
                _uiState.update { it.copy(stage = OrderStage.ROUTE_CONFIRM, activeRideRequestId = null) }
            }
        }
    }

    fun listenToActiveRide(rideId: String, onRideAccepted: (String) -> Unit) {
        val rideRequestRef = FirebaseFirestore.getInstance().collection("ride_requests").document(rideId)
        activeRideListener = rideRequestRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w("OrderViewModel", "Listen failed.", e)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val status = snapshot.getString("status")
                if (status == "accepted") {
                    onRideAccepted(rideId)
                    activeRideListener?.remove()
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        activeRideListener?.remove()
    }
}