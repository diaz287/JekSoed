package com.example.jeksoed.data.model

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
    val driverId: String? = null,
    val encodedPolyline: String? = null,
    val driverCurrentLocation: Map<String, Double>? = null
)