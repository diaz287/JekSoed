package com.example.jeksoed.services

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    // Fungsi ini akan dipanggil saat token baru dibuat atau diperbarui.
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "Token baru: $token")

        // Simpan token ini ke profil user jika user sedang login
        sendRegistrationToServer(token)
    }

    // Fungsi ini akan dipanggil saat ada pesan masuk ketika aplikasi di foreground.
    // Notifikasi di background ditangani otomatis oleh sistem.
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.d("FCM", "Pesan diterima dari: ${remoteMessage.from}")

        remoteMessage.notification?.let {
            Log.d("FCM", "Judul Notifikasi: ${it.title}")
            Log.d("FCM", "Isi Notifikasi: ${it.body}")
            // Di sini Anda bisa membuat notifikasi custom jika aplikasi sedang dibuka
        }
    }

    private fun sendRegistrationToServer(token: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            FirebaseFirestore.getInstance().collection("users").document(userId)
                .update("fcmToken", token)
                .addOnSuccessListener { Log.d("FCM", "Token berhasil disimpan ke Firestore.") }
                .addOnFailureListener { e -> Log.w("FCM", "Gagal menyimpan token.", e) }
        }
    }
}