// main/java/com/example/jeksoed/ui/screens/activity/ActivityViewModel.kt

package com.example.jeksoed.ui.screens.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jeksoed.data.model.RideRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
data class RideHistoryDisplay(
    val ride: RideRequest,
    val otherUserName: String
)

data class ActivityUiState(
    val isLoading: Boolean = true,
    val rideHistoryItems: List<RideHistoryDisplay> = emptyList(), // Ganti list lama
    val filteredRides: List<RideHistoryDisplay> = emptyList(), // Ganti list lama
    val selectedTab: Int = 0,
    val isDriver: Boolean = false // Tambahkan role
)

class ActivityViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val currentUserId = auth.currentUser?.uid

    private val _uiState = MutableStateFlow(ActivityUiState())
    val uiState = _uiState.asStateFlow()

    init {
        fetchUserRoleAndHistory()
    }

    private fun fetchUserRoleAndHistory() {
        if (currentUserId == null) {
            _uiState.update { it.copy(isLoading = false) }
            return
        }
        viewModelScope.launch {
            try {
                val userDoc = db.collection("users").document(currentUserId).get().await()
                val role = userDoc.getString("role")
                fetchHistory(role)
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun fetchHistory(role: String?) {
        if (role == null || currentUserId == null) return

        val isDriver = role == "driver"
        _uiState.update { it.copy(isDriver = isDriver) }

        val fieldToQuery = if (isDriver) "driverId" else "passengerId"

        db.collection("ride_requests")
            .whereEqualTo(fieldToQuery, currentUserId)
            .whereIn("status", listOf("completed", "cancelled"))
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) {
                    _uiState.update { it.copy(isLoading = false) }
                    return@addSnapshotListener
                }

                // --- PROSES DATA GABUNGAN DI SINI ---
                viewModelScope.launch {
                    val rides = snapshot.toObjects(RideRequest::class.java)
                    val historyItems = rides.mapNotNull { ride ->
                        val otherUserId = if (isDriver) ride.passengerId else ride.driverId
                        if (otherUserId == null) return@mapNotNull null

                        try {
                            val userDoc = db.collection("users").document(otherUserId).get().await()
                            val otherUserName = userDoc.getString("nama") ?: "User"
                            RideHistoryDisplay(ride = ride, otherUserName = otherUserName)
                        } catch (ex: Exception) {
                            null
                        }
                    }
                    _uiState.update { it.copy(isLoading = false, rideHistoryItems = historyItems) }
                    // Update juga filtered list
                    onTabSelected(_uiState.value.selectedTab)
                }
            }
    }

    fun onTabSelected(tabIndex: Int) {
        val filtered = when (tabIndex) {
            1 -> _uiState.value.rideHistoryItems.filter { it.ride.status == "completed" }
            2 -> _uiState.value.rideHistoryItems.filter { it.ride.status == "cancelled" }
            else -> _uiState.value.rideHistoryItems
        }
        _uiState.update { it.copy(selectedTab = tabIndex, filteredRides = filtered) }
    }
}