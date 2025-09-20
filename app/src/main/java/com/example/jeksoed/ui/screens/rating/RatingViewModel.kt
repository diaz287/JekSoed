package com.example.jeksoed.ui.screens.rating

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Event sekali jalan untuk navigasi
sealed class RatingNavEvent {
    object NavigateToHome : RatingNavEvent()
}

// Wadah untuk state UI
data class RatingUiState(
    val selectedRating: Int = 0,
    val comment: String = "",
    val isSubmitting: Boolean = false,
    val error: String? = null
)

class RatingViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val driverId: String = savedStateHandle.get<String>("driverId")!!
    private val firestore = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow(RatingUiState())
    val uiState = _uiState.asStateFlow()

    private val _navEvent = MutableSharedFlow<RatingNavEvent>()
    val navEvent = _navEvent.asSharedFlow()

    fun onRatingChanged(newRating: Int) {
        _uiState.update { it.copy(selectedRating = newRating) }
    }

    fun onCommentChanged(newComment: String) {
        _uiState.update { it.copy(comment = newComment) }
    }

    fun submitRating() {
        if (_uiState.value.selectedRating == 0) return

        _uiState.update { it.copy(isSubmitting = true) }

        val driverRef = firestore.collection("users").document(driverId)

        // Menggunakan Transaction untuk update rating secara aman
        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(driverRef)
            val currentTotalRating = snapshot.getLong("totalRating") ?: 0L
            val currentRatingCount = snapshot.getLong("ratingCount") ?: 0L

            val newTotalRating = currentTotalRating + _uiState.value.selectedRating
            val newRatingCount = currentRatingCount + 1

            transaction.update(driverRef, "totalRating", newTotalRating)
            transaction.update(driverRef, "ratingCount", newRatingCount)

            null
        }.addOnSuccessListener {
            viewModelScope.launch {
                _navEvent.emit(RatingNavEvent.NavigateToHome)
            }
        }.addOnFailureListener { e ->
            Log.w("RatingViewModel", "Gagal mengirim ulasan", e)
            _uiState.update { it.copy(isSubmitting = false, error = e.message) }
        }
    }
}