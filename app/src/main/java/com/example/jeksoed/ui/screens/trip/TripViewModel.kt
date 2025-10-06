package com.example.jeksoed.ui.screens.trip

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jeksoed.data.model.RideRequest
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.tasks.await

sealed class TripNavEvent {
    object NavigateToDriverHome : TripNavEvent()
    data class NavigateToRatingScreen(val driverId: String) : TripNavEvent()
}

// Data class untuk menampung semua state UI dalam satu objek
data class TripUiState(
    val rideRequest: RideRequest? = null,
    val polylinePoints: List<LatLng> = emptyList(),
    val isDriver: Boolean = false,
    val otherUserName: String = "Memuat...",
    val otherUserPhotoUrl: String? = null,
    val otherUserExtraInfo: String? = null
)

class TripViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val rideRequestId: String = savedStateHandle.get<String>("rideRequestId")!!
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
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

                    _uiState.update { currentState ->
                        currentState.copy(
                            rideRequest = request,
                            isDriver = request?.driverId == currentUserId,
                            polylinePoints = request?.encodedPolyline?.let { PolyUtil.decode(it) } ?: emptyList()
                        )
                    }
                    loadOtherUserInfo(isDriver = isDriver, rideRequest = request)

                    if (request?.status == "completed") {
                        viewModelScope.launch {
                            // Navigasi ke RatingScreen untuk penumpang
                            if (!isDriver) {
                                val driverId = request.driverId
                                if (driverId != null) {
                                    _navEvent.emit(TripNavEvent.NavigateToRatingScreen(driverId))
                                }
                            }
                        }
                    }
                }
            }
    }

    private fun loadOtherUserInfo(isDriver: Boolean, rideRequest: RideRequest?) {
        viewModelScope.launch {
            if (rideRequest == null) return@launch
            val otherUserId = if (isDriver) rideRequest.passengerId else rideRequest.driverId

            if (otherUserId != null && otherUserId.isNotBlank()) {
                try {
                    val userDoc = db.collection("users").document(otherUserId).get().await()
                    _uiState.update {
                        it.copy(
                            otherUserName = userDoc.getString("nama") ?: "User",
                            otherUserPhotoUrl = userDoc.getString("photoUrl"),
                            otherUserExtraInfo = if (!isDriver) userDoc.getString("platNomor") else null // Ambil plat jika user adalah penumpang
                        )
                    }
                } catch (e: Exception) {
                    Log.e("TripViewModel", "Gagal memuat info user lain", e)
                    _uiState.update { it.copy(otherUserName = "Tidak Ditemukan") }
                }
            }
        }
    }

    fun startLocationUpdates(fusedLocationClient: FusedLocationProviderClient, context: Context) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return

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

    // Membersihkan listener saat ViewModel dihancurkan
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

    fun logout() {
        auth.signOut()
    }


    fun finishAndNavigateHome() {
        viewModelScope.launch {
            _navEvent.emit(TripNavEvent.NavigateToDriverHome)
        }
    }

    // --- FUNGSI BARU: Untuk membatalkan perjalanan ---
    fun cancelTrip() {
        // Logika pembatalan bisa lebih kompleks, misal: update status ke "cancelled"
        // Untuk saat ini, kita langsung arahkan driver kembali ke home
        updateTripStatus("cancelled") // Opsional: update status di DB
        viewModelScope.launch {
            _navEvent.emit(TripNavEvent.NavigateToDriverHome)
        }
    }
}