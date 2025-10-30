package com.example.jeksoed.ui.screens.trip

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class TripViewModelFactory(
    private val rideRequestId: String
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TripViewModel::class.java)) {
            // ViewModel akan mengurus instance Firebase-nya sendiri
            return TripViewModel(
                rideRequestId = rideRequestId,
                db = FirebaseFirestore.getInstance(),
                auth = FirebaseAuth.getInstance()
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}