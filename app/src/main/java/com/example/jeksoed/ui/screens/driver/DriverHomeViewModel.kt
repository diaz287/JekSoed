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
import com.example.jeksoed.data.remote.MapsApiService
import com.example.jeksoed.utils.formatCurrency
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.DecimalFormat

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
    val driverLocation: LatLng? = null
)

class DriverHomeViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var rideRequestListener: ListenerRegistration? = null
    private val mapsApiService = MapsApiService.create()
    private var locationCallback: LocationCallback? = null
    private var userProfileListener: ListenerRegistration? = null

    // --- TAMBAHKAN FusedLocationProviderClient sebagai properti ---
    private var fusedLocationClient: FusedLocationProviderClient? = null

    private val _uiState = MutableStateFlow(DriverHomeUiState())
    val uiState = _uiState.asStateFlow()

    private val _navigateToActiveTrip = MutableSharedFlow<String>()
    val navigateToActiveTrip = _navigateToActiveTrip.asSharedFlow()

    init {
        listenToDriverProfile()
        checkForActiveTrip()
        if (_uiState.value.isOnline) {
            startListeningForRides()
        }
    }

    private fun checkForActiveTrip() {
        val userId = auth.currentUser?.uid
        if (userId == null) return

        viewModelScope.launch {
            try {
                val activeTripSnapshot = firestore.collection("ride_requests")
                    .whereEqualTo("driverId", userId)
                    .whereIn("status", listOf("accepted", "arrived", "started"))
                    .limit(1)
                    .get()
                    .await()

                if (!activeTripSnapshot.isEmpty) {
                    val activeTripId = activeTripSnapshot.documents.first().id
                    Log.d("DriverHomeViewModel", "Perjalanan aktif ditemukan: $activeTripId")
                    _navigateToActiveTrip.emit(activeTripId)
                } else {
                    Log.d("DriverHomeViewModel", "Tidak ada perjalanan aktif untuk driver.")
                }
            } catch (e: Exception) {
                Log.e("DriverHomeViewModel", "Error saat mengecek perjalanan aktif", e)
            }
        }
    }

    fun startLocationUpdates(fusedLocationClient: FusedLocationProviderClient, context: Context) {
        this.fusedLocationClient = fusedLocationClient // Simpan instance
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        val locationRequest = LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    _uiState.update { it.copy(driverLocation = LatLng(location.latitude, location.longitude)) }
                }
            }
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback!!, Looper.getMainLooper())
    }

    fun stopLocationUpdates(fusedLocationClient: FusedLocationProviderClient) {
        locationCallback?.let { fusedLocationClient.removeLocationUpdates(it) }
    }

    // --- PERBAIKAN UTAMA DI FUNGSI INI ---
    fun acceptRide(
        rideRequest: RideRequest,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val driverId = auth.currentUser?.uid ?: return onFailure(Exception("Driver tidak login"))
        val client = fusedLocationClient ?: return onFailure(Exception("Layanan lokasi tidak aktif"))

        _uiState.update { it.copy(acceptingRideId = rideRequest.id, popupRideRequest = null) }

        // Minta lokasi terbaru secara paksa
        try {
            client.lastLocation.addOnSuccessListener { location ->
                if (location == null) {
                    onFailure(Exception("Tidak bisa mendapatkan lokasi driver saat ini. Coba lagi."))
                    _uiState.update { it.copy(acceptingRideId = null) }
                    return@addOnSuccessListener
                }

                val driverLocation = LatLng(location.latitude, location.longitude)

                viewModelScope.launch {
                    try {
                        val passengerLocation = LatLng(
                            rideRequest.pickupLocation["latitude"] ?: 0.0,
                            rideRequest.pickupLocation["longitude"] ?: 0.0
                        )
                        val result = mapsApiService.getDirections(driverLocation, passengerLocation)
                        val points = result.routes.firstOrNull()?.overviewPolyline?.points ?: ""

                        firestore.collection("ride_requests").document(rideRequest.id)
                            .update(mapOf("status" to "accepted", "driverId" to driverId, "encodedPolyline" to points))
                            .addOnSuccessListener {
                                _uiState.update { it.copy(acceptingRideId = null) }
                                onSuccess()
                            }
                            .addOnFailureListener(onFailure)
                    } catch (e: Exception) {
                        Log.e("DriverHomeVM", "Gagal mendapatkan rute atau update", e)
                        _uiState.update { it.copy(acceptingRideId = null) }
                        onFailure(e)
                    }
                }
            }.addOnFailureListener { e ->
                onFailure(e)
                _uiState.update { it.copy(acceptingRideId = null) }
            }
        } catch (e: SecurityException) {
            onFailure(e)
            _uiState.update { it.copy(acceptingRideId = null) }
        }
    }

    private fun listenToDriverProfile() {
        val userId = auth.currentUser?.uid ?: return
        _uiState.update { it.copy(isLoadingProfile = true) }
        userProfileListener = firestore.collection("users").document(userId)
            .addSnapshotListener { userSnapshot, error ->
                if (error != null) {
                    Log.e("DriverHomeVM", "Gagal listen ke profil driver", error)
                    _uiState.update { it.copy(isLoadingProfile = false) }
                    return@addSnapshotListener
                }
                if (userSnapshot != null && userSnapshot.exists()) {
                    val user = userSnapshot.toObject<User>() ?: User()
                    val averageRating = if (user.ratingCount > 0) {
                        DecimalFormat("#.#").format(user.totalRating.toDouble() / user.ratingCount.toDouble())
                    } else "0.0"

                    viewModelScope.launch {
                        val orderCount = try {
                            firestore.collection("ride_requests")
                                .whereEqualTo("driverId", userId)
                                .whereEqualTo("status", "completed")
                                .get().await().size().toString()
                        } catch (e: Exception) { "0" }
                        _uiState.update {
                            it.copy(
                                driverProfile = DriverProfile(
                                    name = user.nama,
                                    licensePlate = user.licensePlate ?: "Belum diatur",
                                    photoUrl = user.photoUrl,
                                    balance = formatCurrency(user.balance.toInt()),
                                    rating = averageRating,
                                    orderCount = orderCount
                                ),
                                isLoadingProfile = false
                            )
                        }
                    }
                } else {
                    _uiState.update { it.copy(isLoadingProfile = false) }
                }
            }
    }

    private fun startListeningForRides() {
        if (rideRequestListener != null) return
        rideRequestListener = firestore.collection("ride_requests")
            .whereEqualTo("status", "pending")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("DriverHomeVM", "Listen failed.", e)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val newRequests = snapshot.documents.mapNotNull { it.toObject(RideRequest::class.java)?.copy(id = it.id) }

                    // --- PERBAIKAN LOGIKA ADA DI SINI ---
                    _uiState.update { currentState ->
                        // 1. Cek apakah popup yang sedang tampil masih valid (ada di daftar baru)
                        val currentPopupId = currentState.popupRideRequest?.id
                        val isPopupStillValid = newRequests.any { it.id == currentPopupId }

                        // 2. Tentukan popup baru atau bersihkan yang lama
                        val nextPopup = if (currentState.acceptingRideId != null) {
                            // Jika sedang proses accept, jangan tampilkan popup baru
                            null
                        } else if (isPopupStillValid) {
                            // Jika popup lama masih valid, pertahankan
                            currentState.popupRideRequest
                        } else {
                            // Jika tidak, ambil orderan pertama dari daftar baru
                            newRequests.firstOrNull()
                        }

                        currentState.copy(
                            rideRequests = newRequests,
                            popupRideRequest = nextPopup
                        )
                    }
                }
            }
    }

    private fun stopListeningForRides() {
        rideRequestListener?.remove()
        rideRequestListener = null
        _uiState.update { it.copy(rideRequests = emptyList(), popupRideRequest = null) }
    }

    fun rejectRide(rideId: String) {
        if (_uiState.value.popupRideRequest?.id == rideId) {
            dismissPopup()
        }
        _uiState.update {
            it.copy(rideRequests = it.rideRequests.filterNot { req -> req.id == rideId })
        }
    }

    fun dismissPopup() {
        _uiState.update { it.copy(popupRideRequest = null) }
    }

    fun setOnlineStatus(isOnline: Boolean) {
        _uiState.update { it.copy(isOnline = isOnline) }
        if (isOnline) {
            startListeningForRides()
        } else {
            stopListeningForRides()
        }
    }

    fun logout() {
        auth.signOut()
    }

    override fun onCleared() {
        super.onCleared()
        stopListeningForRides()
        userProfileListener?.remove()
    }
}