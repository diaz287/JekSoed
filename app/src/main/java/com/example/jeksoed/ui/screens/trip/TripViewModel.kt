package com.example.jeksoed.ui.screens.trip

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jeksoed.data.model.RideRequest
import com.example.jeksoed.data.model.User
import com.example.jeksoed.data.remote.MapsApiService
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.maps.android.PolyUtil
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class TripNavEvent {
    object NavigateToHome : TripNavEvent()
    data class NavigateToRatingScreen(val driverId: String, val rideRequestId: String) : TripNavEvent()
    data class NavigateToTripCompleted(val rideRequestId: String) : TripNavEvent()
}

data class TripUiState(
    val rideRequest: RideRequest? = null,
    val dynamicPolylinePoints: List<LatLng> = emptyList(), // State untuk rute dinamis
    val isDriver: Boolean = false,
    val otherUser: User? = null
)

class TripViewModel(
    private val rideRequestId: String,
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val mapsApiService: MapsApiService = MapsApiService.create()
) : ViewModel() {

    private val currentUserId = auth.currentUser?.uid
    private var rideRequestListener: ListenerRegistration? = null
    private var locationCallback: LocationCallback? = null

    private val _uiState = MutableStateFlow(TripUiState())
    val uiState = _uiState.asStateFlow()

    private val _navEvent = MutableSharedFlow<TripNavEvent>()
    val navEvent = _navEvent.asSharedFlow()

    init {
        if (rideRequestId.isNotBlank()) {
            listenToTripUpdates()
        }
    }

    private fun listenToTripUpdates() {
        rideRequestListener = db.collection("ride_requests").document(rideRequestId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("TripViewModel", "Listen failed.", e)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val request = snapshot.toObject(RideRequest::class.java)?.copy(id = snapshot.id)
                    val isDriver = request?.driverId == currentUserId
                    val isFirstLoad = _uiState.value.rideRequest == null

                    // Ini hanya berjalan sekali saat data pertama kali dimuat untuk memastikan rute langsung tampil.
                    if (isFirstLoad && request?.status == "accepted" && !request.encodedPolyline.isNullOrBlank()) {
                        val initialPolyline = PolyUtil.decode(request.encodedPolyline)
                        _uiState.update { it.copy(dynamicPolylinePoints = initialPolyline) }
                    }

                    _uiState.update { it.copy(rideRequest = request, isDriver = isDriver) }

                    // Panggil fungsi untuk update rute dinamis setiap ada perubahan data
                    updateRouteBasedOnStatus()

                    loadOtherUserInfo(isDriver, request)

                    if (request?.status == "completed" && !isDriver) {
                        val driverId = request.driverId
                        val rideId = request.id
                        if (driverId != null) {
                            viewModelScope.launch {
                                // Kirim event yang benar untuk navigasi ke halaman rating
                                _navEvent.emit(TripNavEvent.NavigateToRatingScreen(driverId, rideId))
                            }
                        }
                    }
                }
            }
    }

    // FUNGSI INTI: Secara dinamis menentukan dan mengambil rute berdasarkan status.
    private fun updateRouteBasedOnStatus() {
        viewModelScope.launch {
            val request = _uiState.value.rideRequest ?: return@launch

            val driverLocation = request.driverCurrentLocation?.let { LatLng(it["latitude"] ?: 0.0, it["longitude"] ?: 0.0) }
            val pickupLocation = LatLng(request.pickupLocation["latitude"] ?: 0.0, request.pickupLocation["longitude"] ?: 0.0)
            val destinationLocation = LatLng(request.destinationLocation["latitude"] ?: 0.0, request.destinationLocation["longitude"] ?: 0.0)

            val origin: LatLng?
            val destination: LatLng?

            when (request.status) {
                // Status: Driver menuju lokasi penumpang
                "accepted", "arrived" -> {
                    origin = driverLocation
                    destination = pickupLocation
                }
                // Status: Penumpang sudah dijemput, menuju tujuan
                "started" -> {
                    origin = driverLocation // Lokasi driver sekarang = lokasi penumpang
                    destination = destinationLocation
                }
                // Status lain (fallback)
                else -> {
                    origin = null
                    destination = null
                }
            }

            // Panggil Directions API jika origin dan destination valid
            if (origin != null && destination != null) {
                try {
                    val result = mapsApiService.getDirections(origin, destination)
                    val points = result.routes.firstOrNull()?.overviewPolyline?.points
                    if (points != null) {
                        _uiState.update { it.copy(dynamicPolylinePoints = PolyUtil.decode(points)) }
                    }
                } catch (e: Exception) {
                    Log.e("TripViewModel", "Failed to get directions", e)
                }
            }
        }
    }

    private fun loadOtherUserInfo(isDriver: Boolean, rideRequest: RideRequest?) {
        viewModelScope.launch {
            if (rideRequest == null) return@launch
            val otherUserId = if (isDriver) rideRequest.passengerId else rideRequest.driverId

            if (!otherUserId.isNullOrBlank()) {
                try {
                    val userDoc = db.collection("users").document(otherUserId).get().await()
                    val user = userDoc.toObject(User::class.java)
                    _uiState.update { it.copy(otherUser = user) }
                } catch (e: Exception) {
                    Log.e("TripViewModel", "Gagal memuat info user lain", e)
                }
            }
        }
    }

    fun startLocationUpdates(fusedLocationClient: FusedLocationProviderClient, context: Context) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) return

        val locationRequest = LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    val newLocation = mapOf("latitude" to location.latitude, "longitude" to location.longitude)
                    db.collection("ride_requests").document(rideRequestId)
                        .update("driverCurrentLocation", newLocation)
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback!!, Looper.getMainLooper())
    }

    fun stopLocationUpdates(fusedLocationClient: FusedLocationProviderClient) {
        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
        }
    }

    override fun onCleared() {
        super.onCleared()
        rideRequestListener?.remove()
    }

    fun updateTripStatus(newStatus: String) {
        if (rideRequestId.isNotBlank()) {
            val updateData = mutableMapOf<String, Any>("status" to newStatus)
            if (newStatus == "completed") {
                updateData["completedAt"] = Timestamp.now()
            }

            db.collection("ride_requests").document(rideRequestId)
                .update(updateData)
                .addOnSuccessListener {
                    Log.d("TripViewModel", "Status berhasil diupdate menjadi $newStatus")
                }
                .addOnFailureListener { e ->
                    Log.w("TripViewModel", "Gagal mengupdate status", e)
                }
        }
    }

    fun cancelTrip() {
        updateTripStatus("cancelled")
        viewModelScope.launch {
            _navEvent.emit(TripNavEvent.NavigateToHome)
        }
    }
    fun confirmPaymentAndFinishTrip() {
        viewModelScope.launch {
            _navEvent.emit(TripNavEvent.NavigateToTripCompleted(rideRequestId))
        }
    }
}