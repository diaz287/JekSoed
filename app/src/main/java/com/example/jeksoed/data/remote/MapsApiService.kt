package com.example.jeksoed.data.remote

import com.example.jeksoed.BuildConfig
import com.google.android.gms.maps.model.LatLng
import com.google.gson.annotations.SerializedName // <-- Tambahkan import ini
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

// Data class untuk menampung respons dari API
data class DirectionsResponse(val routes: List<Route>)

// PERBAIKAN DI SINI
data class Route(
    @SerializedName("overview_polyline") // Anotasi untuk mapping JSON ke snake_case
    val overviewPolyline: PolylineData
)

data class PolylineData(val points: String)

interface MapsApiService {

    @GET("maps/api/directions/json")
    suspend fun getDirections(
        @Query("origin") origin: String,
        @Query("destination") destination: String,
        @Query("key") apiKey: String
    ): DirectionsResponse

    // Fungsi bantuan agar lebih mudah memanggil dari ViewModel
    suspend fun getDirections(origin: LatLng, destination: LatLng): DirectionsResponse {
        val apiKey = BuildConfig.MAPS_API_KEY
        val originStr = "${origin.latitude},${origin.longitude}"
        val destStr = "${destination.latitude},${destination.longitude}"
        return getDirections(originStr, destStr, apiKey)
    }

    companion object {
        private const val BASE_URL = "https://maps.googleapis.com/"

        fun create(): MapsApiService {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(MapsApiService::class.java)
        }
    }
}