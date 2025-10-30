package com.example.jeksoed.ui.screens.rating

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jeksoed.data.model.RideRequest
import com.example.jeksoed.data.model.User
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class RatingUiState(
    val selectedRating: Int = 0,
    val comment: String = "",
    val isSubmitting: Boolean = false,
    val error: String? = null,
    val driver: User? = null,
    val rideRequest: RideRequest? = null,
    val isLoading: Boolean = true
)

// --- PERBAIKAN: Gunakan SavedStateHandle sepenuhnya ---
class RatingViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val driverId: String = savedStateHandle.get<String>("driverId")!!
    private val rideRequestId: String = savedStateHandle.get<String>("rideRequestId")!!
    private val firestore = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow(RatingUiState())
    val uiState = _uiState.asStateFlow()

    init {
        fetchTripAndDriverDetails()
    }

    private fun fetchTripAndDriverDetails() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val driverDoc = firestore.collection("users").document(driverId).get().await()
                val driver = driverDoc.toObject<User>()

                val rideDoc = firestore.collection("ride_requests").document(rideRequestId).get().await()
                val ride = rideDoc.toObject<RideRequest>()

                _uiState.update {
                    it.copy(
                        driver = driver,
                        rideRequest = ride,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                Log.e("RatingViewModel", "Gagal mengambil data", e)
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun onRatingChanged(newRating: Int) {
        _uiState.update { it.copy(selectedRating = newRating) }
    }

    fun onCommentChanged(newComment: String) {
        _uiState.update { it.copy(comment = newComment) }
    }

    fun submitRating() {
        val rating = _uiState.value.selectedRating
        if (rating == 0) return
        _uiState.update { it.copy(isSubmitting = true) }

        val driverRef = firestore.collection("users").document(driverId)
        val rideRef = firestore.collection("ride_requests").document(rideRequestId)

        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(driverRef)
            val currentTotalRating = snapshot.getLong("totalRating") ?: 0L
            val currentRatingCount = snapshot.getLong("ratingCount") ?: 0L
            transaction.update(driverRef, "totalRating", currentTotalRating + rating)
            transaction.update(driverRef, "ratingCount", currentRatingCount + 1)

            transaction.update(rideRef, "rating", rating)
            null
        }.addOnSuccessListener {
            Log.d("RatingViewModel", "Rating berhasil disimpan.")
            _uiState.update { it.copy(isSubmitting = false) }
        }.addOnFailureListener { e ->
            Log.w("RatingViewModel", "Gagal mengirim ulasan", e)
            _uiState.update { it.copy(isSubmitting = false, error = e.message) }
        }
    }
}