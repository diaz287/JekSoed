package com.example.jeksoed.utils

import java.text.NumberFormat
import java.util.Locale
import kotlin.math.ceil

/**
 * Menghitung harga perjalanan berdasarkan jarak dalam meter.
 */
fun calculatePrice(distanceInMeters: Long): Int {
    val baseFare = 7000
    val baseDistanceKm = 1.5
    val perKmRate = 2000

    val distanceInKm = distanceInMeters / 1000.0

    if (distanceInKm <= baseDistanceKm) {
        return baseFare
    }

    val extraDistance = distanceInKm - baseDistanceKm
    // ceil() = pembulatan ke atas
    val extraCharge = ceil(extraDistance).toInt() * perKmRate

    return baseFare + extraCharge
}

/**
 * Mengubah angka integer menjadi format mata uang Rupiah (misal: "Rp 11.000").
 */
fun formatCurrency(price: Int): String {
    val localeID = Locale("in", "ID")
    val numberFormat = NumberFormat.getCurrencyInstance(localeID)
    numberFormat.maximumFractionDigits = 0 // Menghilangkan ,00 di belakang
    return numberFormat.format(price)
}