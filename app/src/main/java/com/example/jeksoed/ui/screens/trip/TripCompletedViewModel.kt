package com.example.jeksoed.ui.screens.trip

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jeksoed.data.model.RideRequest
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

data class TripCompletedUiState(
    val isLoading: Boolean = true,
    val rideRequest: RideRequest? = null,
    val feedbackText: String = "",
    val totalFare: Int = 0,
    val deposit: Int = 0,
    val earnings: Int = 0,
    val isSubmitting: Boolean = false // Tambahkan state untuk loading
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

                // --- HITUNG DETAIL PEMBAYARAN DI SINI ---
                val totalFare = parseCurrency(request?.price)
                val deposit = (totalFare * 0.1).toInt() // Potongan 10%
                val earnings = totalFare - deposit

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        rideRequest = request,
                        totalFare = totalFare,
                        deposit = deposit,
                        earnings = earnings
                    )
                }
            } catch (e: Exception) {
                Log.e("TripCompletedVM", "Gagal mengambil detail perjalanan", e)
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    // --- FUNGSI BARU UNTUK UPDATE SALDO ---
    fun finishTripAndUpdateBalance(onFinished: () -> Unit) {
        val driverId = _uiState.value.rideRequest?.driverId
        val earnings = _uiState.value.earnings

        if (driverId.isNullOrBlank() || earnings <= 0) {
            Log.w("TripCompletedVM", "Driver ID atau pendapatan tidak valid.")
            onFinished() // Langsung selesaikan jika data tidak valid
            return
        }

        _uiState.update { it.copy(isSubmitting = true) }
        val driverRef = db.collection("users").document(driverId)

        // Gunakan FieldValue.increment untuk menambahkan saldo secara aman
        db.runTransaction { transaction ->
            transaction.update(driverRef, "balance", FieldValue.increment(earnings.toLong()))
            null
        }.addOnSuccessListener {
            Log.d("TripCompletedVM", "Saldo driver berhasil diperbarui.")
            _uiState.update { it.copy(isSubmitting = false) }
            onFinished() // Panggil callback setelah berhasil
        }.addOnFailureListener { e ->
            Log.e("TripCompletedVM", "Gagal memperbarui saldo driver.", e)
            _uiState.update { it.copy(isSubmitting = false) }
            onFinished() // Tetap panggil callback meski gagal
        }
    }

    // --- FUNGSI BANTUAN UNTUK MENGUBAH STRING MATA UANG KE INT ---
    private fun parseCurrency(price: String?): Int {
        if (price.isNullOrBlank()) return 0
        return try {
            val cleanString = price.replace(Regex("[^\\d]"), "")
            cleanString.toInt()
        } catch (e: NumberFormatException) {
            0
        }
    }

    fun onFeedbackChanged(text: String) {
        _uiState.update { it.copy(feedbackText = text) }
    }

    fun getFormattedDate(timestamp: com.google.firebase.Timestamp?): String {
        if (timestamp == null) return ""
        val sdf = SimpleDateFormat("d MMMM yyyy", Locale("id", "ID"))
        return sdf.format(timestamp.toDate())
    }
}