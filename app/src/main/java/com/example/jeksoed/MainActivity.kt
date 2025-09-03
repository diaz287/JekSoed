package com.example.jeksoed

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.jeksoed.ui.theme.JekSoedTheme
import android.content.pm.PackageManager
import com.google.android.libraries.places.api.Places
import androidx.activity.enableEdgeToEdge
import com.example.jeksoed.navigation.AppNavigation

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val apiKey = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
            .metaData.getString("com.google.android.geo.API_KEY")

        // Inisialisasi Places SDK
        if (apiKey != null && !Places.isInitialized()) {
            Places.initialize(applicationContext, apiKey)
        }

        setContent {
            JekSoedTheme {
                AppNavigation()
            }
        }
    }
}