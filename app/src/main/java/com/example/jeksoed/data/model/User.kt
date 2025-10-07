package com.example.jeksoed.data.model

import com.google.firebase.firestore.ServerTimestamp
import com.google.firebase.Timestamp

/**
 * Data class ini merepresentasikan struktur data untuk seorang pengguna
 * di dalam koleksi "users" pada Firestore.
 */
data class User(
    val uid: String = "",
    val nama: String = "",
    val email: String = "",
    val role: String = "", // "penumpang" atau "driver"
    val photoUrl: String? = null,

    // Field khusus untuk driver
    val licensePlate: String? = null, // Plat Nomor Kendaraan
    val vehicleType: String? = null, // Jenis Kendaraan (misal: Motor, Mobil)

    // Field tambahan
    val fcmToken: String? = null, // Untuk notifikasi
    @ServerTimestamp
    val createdAt: Timestamp? = null,
    val totalRating: Long = 0L,
    val ratingCount: Long = 0L
)