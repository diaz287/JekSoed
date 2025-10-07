package com.example.jeksoed.ui.screens.trip

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// Factory ini bertugas membuat instance dari TripViewModel dengan parameter yang dibutuhkan
class TripViewModelFactory(
    private val rideRequestId: String,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TripViewModel::class.java)) {
            return TripViewModel(rideRequestId, firestore, auth) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}