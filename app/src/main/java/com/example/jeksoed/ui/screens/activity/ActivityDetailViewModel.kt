// main/java/com/example/jeksoed/ui/screens/activity/ActivityDetailViewModel.kt

package com.example.jeksoed.ui.screens.activity

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jeksoed.data.model.RideRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class ActivityDetailUiState(
    val isLoading: Boolean = true,
    val rideRequest: RideRequest? = null,
    val otherUserName: String = "",
    val isChatEnabled: Boolean = false,
    val isDriver: Boolean = false // State untuk menentukan peran pengguna
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
                val ride = rideDoc.toObject(RideRequest::class.java)
                if (ride != null) {
                    val currentUserId = auth.currentUser?.uid
                    val isDriver = ride.driverId == currentUserId // Logika penentuan peran ada di sini
                    val otherUserId = if (isDriver) ride.passengerId else ride.driverId

                    val otherUserDoc = db.collection("users").document(otherUserId ?: "").get().await()
                    val otherUserName = otherUserDoc.getString("nama") ?: "User"

                    // Cek apakah chat masih aktif (dalam 30 menit setelah pesanan dibuat)
                    val completedAt = ride.createdAt?.toDate()?.time ?: 0L
                    val thirtyMinutesInMillis = 30 * 60 * 1000
                    val isChatEnabled = (System.currentTimeMillis() - completedAt) < thirtyMinutesInMillis

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            rideRequest = ride,
                            otherUserName = otherUserName,
                            isChatEnabled = isChatEnabled,
                            isDriver = isDriver // Perbarui state peran
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}