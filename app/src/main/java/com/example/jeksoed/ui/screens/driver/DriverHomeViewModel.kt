package com.example.jeksoed.ui.screens.driver

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jeksoed.data.model.RideRequest
import com.example.jeksoed.data.model.User
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
    val acceptingRideId: String? = null
)

class DriverHomeViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var rideRequestListener: ListenerRegistration? = null

    private val _uiState = MutableStateFlow(DriverHomeUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadDriverProfile()
        listenToRideRequests()
    }

    private fun loadDriverProfile() {
        val userId = auth.currentUser?.uid ?: return
        _uiState.update { it.copy(isLoadingProfile = true) }

        viewModelScope.launch {
            try {
                // 1. Ambil data dokumen pengguna
                val userDocument = firestore.collection("users").document(userId).get().await()
                val user = userDocument.toObject<User>() ?: User()

                // --- PERUBAHAN LOGIKA UTAMA DI SINI ---
                // 2. Hitung rata-rata rating dari field yang ada
                val averageRating = if (user.ratingCount > 0) {
                    val avg = user.totalRating.toDouble() / user.ratingCount.toDouble()
                    DecimalFormat("#.#").format(avg)
                } else {
                    "0.0"
                }

                // 3. Hitung jumlah orderan yang selesai
                val completedOrdersQuery = firestore.collection("ride_requests")
                    .whereEqualTo("driverId", userId)
                    .whereEqualTo("status", "completed")
                    .get()
                    .await()
                val orderCount = completedOrdersQuery.size().toString()

                // 4. Update UI dengan semua data
                _uiState.update {
                    it.copy(
                        driverProfile = DriverProfile(
                            name = user.nama,
                            licensePlate = user.licensePlate ?: "Belum diatur",
                            photoUrl = user.photoUrl,
                            balance = "Rp150.000,-", // Ganti dengan data asli jika ada
                            rating = averageRating,      // Data rating dari field user
                            orderCount = orderCount      // Data orderan dari query
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

    fun setOnlineStatus(isOnline: Boolean) {
        _uiState.update { it.copy(isOnline = isOnline) }
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
                    val currentRequests = _uiState.value.rideRequests
                    val newRequests = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(RideRequest::class.java)?.copy(id = doc.id)
                    }
                    val newTopRequest = newRequests.firstOrNull()
                    val oldTopRequest = currentRequests.firstOrNull()
                    val shouldShowPopup = newTopRequest != null && newTopRequest.id != oldTopRequest?.id
                    _uiState.update {
                        it.copy(
                            rideRequests = newRequests,
                            popupRideRequest = if (shouldShowPopup) newTopRequest else it.popupRideRequest
                        )
                    }
                }
            }
    }

    fun acceptRide(rideId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val driverId = auth.currentUser?.uid
        if (driverId == null) {
            onFailure(Exception("Driver tidak login"))
            return
        }
        _uiState.update { it.copy(acceptingRideId = rideId, popupRideRequest = null) }
        firestore.collection("ride_requests").document(rideId)
            .update(mapOf("status" to "accepted", "driverId" to driverId))
            .addOnSuccessListener {
                _uiState.update { it.copy(acceptingRideId = null) }
                onSuccess()
            }
            .addOnFailureListener { e ->
                _uiState.update { it.copy(acceptingRideId = null) }
                onFailure(e)
            }
    }

    fun rejectRide(rideId: String) {
        dismissPopup()
    }

    fun dismissPopup() {
        _uiState.update { it.copy(popupRideRequest = null) }
    }

    fun logout() {
        auth.signOut()
    }

    override fun onCleared() {
        super.onCleared()
        rideRequestListener?.remove()
    }
}