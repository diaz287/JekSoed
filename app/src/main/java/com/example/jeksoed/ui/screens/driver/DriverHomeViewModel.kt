package com.example.jeksoed.ui.screens.driver

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
import com.example.jeksoed.data.remote.MapsApiService // Import service
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.DecimalFormat

// DriverProfile tidak berubah
data class DriverProfile(
    val name: String = "Memuat...",
    val licensePlate: String = "...",
    val photoUrl: String? = null,
    val balance: String = "Rp0",
    val rating: String = "0.0",
    val orderCount: String = "0"
)


data class DriverHomeUiState(
    val isOnline: Boolean = true,
    val rideRequests: List<RideRequest> = emptyList(),
    val popupRideRequest: RideRequest? = null,
    val driverProfile: DriverProfile = DriverProfile(),
    val isLoadingProfile: Boolean = true,
    val acceptingRideId: String? = null,
    val driverLocation: LatLng? = null // State untuk lokasi real-time driver
)

class DriverHomeViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var rideRequestListener: ListenerRegistration? = null
    private val mapsApiService = MapsApiService.create() // Inisialisasi service
    private var locationCallback: LocationCallback? = null // Callback untuk update lokasi

    private val _uiState = MutableStateFlow(DriverHomeUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadDriverProfile()
        listenToRideRequests()
    }

    fun startLocationUpdates(fusedLocationClient: FusedLocationProviderClient, context: Context) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        val locationRequest = LocationRequest.create().apply {
            interval = 10000 // Update setiap 10 detik
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    val newLatLng = LatLng(location.latitude, location.longitude)
                    _uiState.update { it.copy(driverLocation = newLatLng) }
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

    // Fungsi acceptRide diubah untuk mengambil rute
    fun acceptRide(rideRequest: RideRequest, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val driverId = auth.currentUser?.uid ?: return onFailure(Exception("Driver tidak login"))
        val driverLocation = _uiState.value.driverLocation ?: return onFailure(Exception("Lokasi driver tidak ditemukan"))

        _uiState.update { it.copy(acceptingRideId = rideRequest.id, popupRideRequest = null) }

        viewModelScope.launch {
            try {
                val passengerLocation = LatLng(
                    rideRequest.pickupLocation["latitude"] ?: 0.0,
                    rideRequest.pickupLocation["longitude"] ?: 0.0
                )
                // Panggil API untuk mendapatkan rute
                val result = mapsApiService.getDirections(driverLocation, passengerLocation)
                val points = result.routes.firstOrNull()?.overviewPolyline?.points ?: ""

                // Update status dan rute di Firestore
                firestore.collection("ride_requests").document(rideRequest.id)
                    .update(
                        mapOf(
                            "status" to "accepted",
                            "driverId" to driverId,
                            "encodedPolyline" to points // Simpan rute baru dari driver ke penumpang
                        )
                    )
                    .addOnSuccessListener {
                        _uiState.update { it.copy(acceptingRideId = null) }
                        onSuccess()
                    }
                    .addOnFailureListener { e ->
                        _uiState.update { it.copy(acceptingRideId = null) }
                        onFailure(e)
                    }

            } catch (e: Exception) {
                Log.e("DriverHomeVM", "Gagal mendapatkan rute atau update", e)
                _uiState.update { it.copy(acceptingRideId = null) }
                onFailure(e)
            }
        }
    }

    // Sisa ViewModel tidak berubah...
    private fun loadDriverProfile() {
        val userId = auth.currentUser?.uid ?: return
        _uiState.update { it.copy(isLoadingProfile = true) }

        viewModelScope.launch {
            try {
                val userDocument = firestore.collection("users").document(userId).get().await()
                val user = userDocument.toObject<User>() ?: User()

                val averageRating = if (user.ratingCount > 0) {
                    val avg = user.totalRating.toDouble() / user.ratingCount.toDouble()
                    DecimalFormat("#.#").format(avg)
                } else {
                    "0.0"
                }

                val completedOrdersQuery = firestore.collection("ride_requests")
                    .whereEqualTo("driverId", userId)
                    .whereEqualTo("status", "completed")
                    .get()
                    .await()
                val orderCount = completedOrdersQuery.size().toString()

                _uiState.update {
                    it.copy(
                        driverProfile = DriverProfile(
                            name = user.nama,
                            licensePlate = user.licensePlate ?: "Belum diatur",
                            photoUrl = user.photoUrl,
                            balance = "Rp150.000,-",
                            rating = averageRating,
                            orderCount = orderCount
                        ),
                        isLoadingProfile = false
                    )
                }
            } catch (e: Exception) {
                Log.e("DriverHomeVM", "Gagal memuat profil driver", e)
                _uiState.update { it.copy(isLoadingProfile = false) }
            }
        }
    }

    private fun listenToRideRequests() {
        rideRequestListener = firestore.collection("ride_requests")
            .whereEqualTo("status", "pending")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("DriverHomeVM", "Listen failed.", e)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val newRequests = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(RideRequest::class.java)?.copy(id = doc.id)
                    }
                    _uiState.update {
                        it.copy(
                            rideRequests = newRequests,
                            popupRideRequest = newRequests.firstOrNull()
                        )
                    }
                }
            }
    }

    fun rejectRide(rideId: String) {
        dismissPopup()
    }

    fun dismissPopup() {
        _uiState.update { it.copy(popupRideRequest = null) }
    }

    fun setOnlineStatus(isOnline: Boolean) {
        _uiState.update { it.copy(isOnline = isOnline) }
    }

    fun logout() {
        auth.signOut()
    }

    override fun onCleared() {
        super.onCleared()
        rideRequestListener?.remove()
    }
}