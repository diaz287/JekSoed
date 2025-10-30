package com.example.jeksoed

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.jeksoed.navigation.AppNavigation
import com.example.jeksoed.ui.theme.JekSoedTheme
import com.google.android.libraries.places.api.Places

class MainActivity : ComponentActivity() {

    // --- TAMBAHKAN INI: Launcher untuk meminta izin notifikasi ---
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Izin diberikan, tidak perlu melakukan apa-apa
        } else {
            // Pengguna menolak izin. Anda bisa menampilkan pesan jika perlu.
        }
    }

    private fun askNotificationPermission() {
        // Hanya berlaku untuk Android 13 (TIRAMISU) ke atas
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                // Langsung minta izin
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // --- PANGGIL FUNGSI PERMINTAAN IZIN ---
        askNotificationPermission()

        val apiKey = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
            .metaData.getString("com.google.android.geo.API_KEY")

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