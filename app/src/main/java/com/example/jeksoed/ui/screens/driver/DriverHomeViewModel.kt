// main/java/com/example/jeksoed/ui/screens/driver/DriverHomeViewModel.kt

package com.example.jeksoed.ui.screens.driver

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jeksoed.data.model.RideRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DriverHomeUiState(
    val rideRequests: List<RideRequest> = emptyList(),
    val isLoading: Boolean = true,
    val acceptingRideId: String? = null,
    // --- TAMBAHKAN STATE BARU UNTUK POP-UP ---
    val popupRideRequest: RideRequest? = null
)

class DriverHomeViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var rideRequestListener: ListenerRegistration? = null

    private val _uiState = MutableStateFlow(DriverHomeUiState())
    val uiState = _uiState.asStateFlow()

    init {
        listenToRideRequests()
    }

    private fun listenToRideRequests() {
        _uiState.update { it.copy(isLoading = true) }
        rideRequestListener = firestore.collection("ride_requests")
            .whereEqualTo("status", "pending")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("DriverHomeVM", "Listen failed.", e)
                    _uiState.update { it.copy(isLoading = false) }
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val currentRequests = _uiState.value.rideRequests
                    val newRequests = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(RideRequest::class.java)?.copy(id = doc.id)
                    }

                    // --- LOGIKA UNTUK MENDETEKSI PESANAN BARU ---
                    val newPopupRequest = newRequests.firstOrNull()
                    val lastShownPopupId = _uiState.value.popupRideRequest?.id
                    val currentTopRequestId = currentRequests.firstOrNull()?.id

                    // Tampilkan pop-up jika ada request baru dan request teratas berbeda dari sebelumnya
                    val shouldShowPopup = newPopupRequest != null && newPopupRequest.id != currentTopRequestId

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            rideRequests = newRequests,
                            // Set state pop-up jika kondisi terpenuhi
                            popupRideRequest = if (shouldShowPopup) newPopupRequest else it.popupRideRequest
                        )
                    }
                }
            }
    }

    // --- FUNGSI BARU UNTUK MENGHILANGKAN POP-UP ---
    fun dismissPopup() {
        _uiState.update { it.copy(popupRideRequest = null) }
    }

    fun acceptRide(rideId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        _uiState.update { it.copy(acceptingRideId = rideId) }
        val driverId = auth.currentUser?.uid

        if (driverId == null) {
            onFailure(Exception("Driver tidak login"))
            _uiState.update { it.copy(acceptingRideId = null) }
            return
        }

        // Saat menerima, langsung sembunyikan pop-up juga
        dismissPopup()

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

    // --- FUNGSI BARU UNTUK MENOLAK PESANAN ---
    fun rejectRide(rideId: String) {
        // Logika untuk menolak bisa berupa menghapus atau mengupdate status
        // Untuk saat ini, kita anggap menolak berarti menyembunyikan pop-up
        dismissPopup()
        // Anda bisa menambahkan logika lain di sini, misal update status ke "rejected"
    }


    fun logout() {
        auth.signOut()
    }

    override fun onCleared() {
        super.onCleared()
        rideRequestListener?.remove()
    }
}