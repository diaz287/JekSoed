package com.example.jeksoed.model

import com.google.firebase.Timestamp

data class RideRequest(
    val id: String = "",
    val passengerId: String = "",
    val pickupLocation: Map<String, Double> = emptyMap(),
    val destinationLocation: Map<String, Double> = emptyMap(),
    val distance: String = "",
    val duration: String = "",
    val status: String = "",
    val createdAt: Timestamp? = null,
    val encodedPolyline: String? = null
)