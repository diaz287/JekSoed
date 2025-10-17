package com.example.jeksoed.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

data class User(
    val uid: String = "",
    val nama: String = "",
    val email: String = "",
    val role: String = "",
    val photoUrl: String? = null,
    val nim: String = "",
    val nomorHp: String = "",

    // Field khusus untuk driver
    val licensePlate: String? = null,
    val vehicleType: String? = null,
    val ktmUrl: String? = null,
    val stnkUrl: String? = null,
    val motorUrl: String? = null,
    val balance: Long = 0L,

    // Field tambahan
    val fcmToken: String? = null,
    @ServerTimestamp
    val createdAt: Timestamp? = null,
    val totalRating: Long = 0L,
    val ratingCount: Long = 0L
)