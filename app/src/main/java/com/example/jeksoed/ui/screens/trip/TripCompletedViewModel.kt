// main/java/com/example/jeksoed/ui/screens/trip/TripCompletedViewModel.kt

package com.example.jeksoed.ui.screens.trip

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jeksoed.data.model.RideRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

data class TripCompletedUiState(
    val isLoading: Boolean = true,
    val rideRequest: RideRequest? = null,
    val feedbackText: String = "",
    // Tambahan untuk detail pembayaran
    val totalFare: Int = 10000, // Hardcode sesuai contoh
    val deposit: Int = 1000, // Hardcode 10%
    val earnings: Int = 9000 // Hardcode
)

class TripCompletedViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val rideRequestId: String = savedStateHandle.get<String>("rideRequestId")!!
    private val db = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow(TripCompletedUiState())
    val uiState = _uiState.asStateFlow()

    init {
        fetchRideDetails()
    }

    private fun fetchRideDetails() {
        viewModelScope.launch {
            try {
                val snapshot = db.collection("ride_requests").document(rideRequestId).get().await()
                val request = snapshot.toObject(RideRequest::class.java)?.copy(id = snapshot.id)
                _uiState.update { it.copy(isLoading = false, rideRequest = request) }
            } catch (e: Exception) {
                // Handle error
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onFeedbackChanged(text: String) {
        _uiState.update { it.copy(feedbackText = text) }
    }

    fun submitAndFinish() {
        // Di sini Anda bisa menambahkan logika untuk menyimpan feedback ke Firestore
        // val feedback = _uiState.value.feedbackText
        // if (feedback.isNotBlank()) { ... }

        // Setelah selesai, lanjutkan navigasi (akan ditangani di Screen)
    }

    fun getFormattedDate(timestamp: com.google.firebase.Timestamp?): String {
        if (timestamp == null) return ""
        val sdf = SimpleDateFormat("d MMMM yyyy", Locale("id", "ID"))
        return sdf.format(timestamp.toDate())
    }
}