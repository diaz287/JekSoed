package com.example.jeksoed.ui.screens.passenger

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jeksoed.data.model.RideRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class HomeUiState(
    val userName: String = "Sobat Jeksoed",
    val recentTrips: List<RideRequest> = emptyList(),
    val isLoading: Boolean = true
)

class HomeViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()
    private val _navigateToActiveTrip = MutableSharedFlow<String>()
    val navigateToActiveTrip = _navigateToActiveTrip.asSharedFlow()

    init {
        fetchInitialData()
        checkForActiveTrip()
    }

    private fun checkForActiveTrip() {
        val userId = auth.currentUser?.uid
        if (userId == null) return

        viewModelScope.launch {
            try {
                val activeTripSnapshot = firestore.collection("ride_requests")
                    .whereEqualTo("passengerId", userId)
                    .whereIn("status", listOf("accepted", "arrived", "started"))
                    .limit(1)
                    .get()
                    .await()

                if (!activeTripSnapshot.isEmpty) {
                    val activeTripId = activeTripSnapshot.documents.first().id
                    Log.d("HomeViewModel", "Perjalanan aktif ditemukan: $activeTripId")
                    _navigateToActiveTrip.emit(activeTripId)
                } else {
                    Log.d("HomeViewModel", "Tidak ada perjalanan aktif untuk penumpang.")
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error saat mengecek perjalanan aktif", e)
            }
        }
    }
    private fun fetchInitialData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val userId = auth.currentUser?.uid
            if (userId == null) {
                Log.e("HomeViewModel", "KESALAHAN: Pengguna tidak login (auth.currentUser.uid is null).")
                _uiState.update { it.copy(isLoading = false, userName = "Sobat Jeksoed") }
                return@launch
            }

            // --- LOG 1: Tampilkan UID yang sedang digunakan ---
            Log.d("HomeViewModel", "Mencoba mengambil data untuk UID: $userId")

            try {
                val userDoc = firestore.collection("users").document(userId).get().await()

                if (userDoc.exists()) {
                    val userNameFromDb = userDoc.getString("nama")

                    // --- LOG 2: Tampilkan hasil pembacaan field 'nama' ---
                    Log.d("HomeViewModel", "Dokumen ditemukan. Isi field 'nama': $userNameFromDb")

                    val userName = userNameFromDb?.split(" ")?.firstOrNull() ?: "Sobat Jeksoed"

                    val historySnapshot = firestore.collection("ride_requests")
                        .whereEqualTo("passengerId", userId)
                        .whereEqualTo("status", "completed")
                        .orderBy("createdAt", Query.Direction.DESCENDING)
                        .limit(2)
                        .get()
                        .await()

                    val recentTrips = historySnapshot.toObjects(RideRequest::class.java)

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            userName = userName,
                            recentTrips = recentTrips
                        )
                    }
                } else {
                    // --- LOG 3: Jika dokumen tidak ada ---
                    Log.e("HomeViewModel", "KESALAHAN: Dokumen untuk UID $userId tidak ditemukan di koleksi 'users'.")
                    _uiState.update { it.copy(isLoading = false) }
                }
            } catch (e: Exception) {
                // --- LOG 4: Jika ada error lain (misal: masalah jaringan atau security rules) ---
                Log.e("HomeViewModel", "KESALAHAN saat mengambil data Firestore: ", e)
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}