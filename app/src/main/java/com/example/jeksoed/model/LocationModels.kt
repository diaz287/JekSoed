package com.example.jeksoed.model

import com.google.android.gms.maps.model.LatLng
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GeocodingResult(
    @SerialName("place_id") val placeId: Long,
    @SerialName("lat") val lat: String,
    @SerialName("lon") val lon: String,
    @SerialName("display_name") val displayName: String
)

// Data class untuk menyimpan informasi rute
data class RouteInfo(
    val distance: String,
    val duration: String,
    val polylinePoints: List<LatLng>
)