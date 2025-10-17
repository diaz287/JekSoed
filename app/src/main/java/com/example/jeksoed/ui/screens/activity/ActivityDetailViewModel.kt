package com.example.jeksoed.ui.screens.activity

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jeksoed.data.model.RideRequest
import com.example.jeksoed.data.model.User
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.google.maps.android.PolyUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.DecimalFormat

data class ActivityDetailUiState(
    val isLoading: Boolean = true,
    val rideRequest: RideRequest? = null,
    val otherUser: User? = null,
    val isChatEnabled: Boolean = false,
    val isDriver: Boolean = false,
    // --- TAMBAHKAN STATE UNTUK POIN RUTE ---
    val polylinePoints: List<LatLng> = emptyList()
)

class ActivityDetailViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val rideRequestId: String = savedStateHandle.get<String>("rideRequestId")!!
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow(ActivityDetailUiState())
    val uiState = _uiState.asStateFlow()

    init {
        fetchRideDetails()
    }

    private fun fetchRideDetails() {
        viewModelScope.launch {
            try {
                val rideDoc = db.collection("ride_requests").document(rideRequestId).get().await()
                val ride = rideDoc.toObject<RideRequest>()?.copy(id = rideDoc.id)
                if (ride != null) {
                    val currentUserId = auth.currentUser?.uid
                    val isDriver = ride.driverId == currentUserId
                    val otherUserId = if (isDriver) ride.passengerId else ride.driverId

                    var otherUser: User? = null
                    if (!otherUserId.isNullOrBlank()) {
                        val otherUserDoc = db.collection("users").document(otherUserId).get().await()
                        otherUser = otherUserDoc.toObject<User>()
                    }

                    val completedAt = ride.createdAt?.toDate()?.time ?: 0L
                    val thirtyMinutesInMillis = 30 * 60 * 1000
                    val isChatEnabled = (System.currentTimeMillis() - completedAt) < thirtyMinutesInMillis

                    // --- DECODE POLYLINE DI SINI ---
                    val polylinePoints = if (!ride.encodedPolyline.isNullOrBlank()) {
                        PolyUtil.decode(ride.encodedPolyline)
                    } else {
                        emptyList()
                    }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            rideRequest = ride,
                            otherUser = otherUser,
                            isChatEnabled = isChatEnabled,
                            isDriver = isDriver,
                            polylinePoints = polylinePoints // Simpan poin rute ke state
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun getFormattedRating(user: User?): String {
        if (user == null || user.ratingCount == 0L) return "0.0"
        val avg = user.totalRating.toDouble() / user.ratingCount.toDouble()
        return DecimalFormat("#.#").format(avg)
    }
}