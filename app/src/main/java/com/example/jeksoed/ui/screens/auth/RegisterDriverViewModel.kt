package com.example.jeksoed.ui.screens.auth

import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
// import com.google.firebase.storage.FirebaseStorage // Dihapus
import kotlinx.coroutines.tasks.await

class RegisterDriverViewModel : ViewModel() {
    // --- Data dari Layar 1 (Data Dasar) ---
    var name by mutableStateOf("")
    var nim by mutableStateOf("")
    var email by mutableStateOf("")
    var phone by mutableStateOf("")
    var password by mutableStateOf("")

    // --- Data dari Layar 2 (Dokumen) ---
    var ktmUri by mutableStateOf<Uri?>(null)
    var stnkUri by mutableStateOf<Uri?>(null)
    var motorUri by mutableStateOf<Uri?>(null)

    // --- Data dari Layar 3 (Konfirmasi) ---
    var agreedToTerms by mutableStateOf(false)

    // --- State untuk proses loading ---
    var isLoading by mutableStateOf(false)

    // --- Instance Firebase ---
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    // private val storage = FirebaseStorage.getInstance() // Dihapus

    // --- FUNGSI REGISTRASI FINAL ---
    suspend fun registerDriver(onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        if (!agreedToTerms) {
            onFailure("Anda harus menyetujui persyaratan.")
            return
        }
        // Validasi sederhana untuk memastikan file sudah dipilih
        if (ktmUri == null || stnkUri == null || motorUri == null) {
            onFailure("Harap lengkapi semua dokumen yang diperlukan.")
            return
        }
        isLoading = true
        try {
            // 1. Buat user di Firebase Auth
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val userId = authResult.user?.uid ?: throw Exception("Gagal mendapatkan User ID.")

            // 2. Simpan URI sebagai String (tanpa upload ke Storage)
            val ktmUrl = ktmUri.toString()
            val stnkUrl = stnkUri.toString()
            val motorUrl = motorUri.toString()

            // 3. Simpan data user ke Firestore
            val userMap = hashMapOf(
                "uid" to userId,
                "nama" to name,
                "nim" to nim,
                "email" to email,
                "nomorHp" to phone,
                "role" to "driver",
                "ktmUrl" to ktmUrl, // Simpan URI sebagai String
                "stnkUrl" to stnkUrl, // Simpan URI sebagai String
                "motorUrl" to motorUrl, // Simpan URI sebagai String
                "createdAt" to FieldValue.serverTimestamp()
            )
            firestore.collection("users").document(userId).set(userMap).await()

            // Jika semua berhasil
            isLoading = false
            onSuccess()

        } catch (e: Exception) {
            isLoading = false
            onFailure(e.message ?: "Terjadi kesalahan tidak diketahui.")
        }
    }

    // Fungsi helper untuk upload file dihapus
    // private suspend fun uploadFile(uri: Uri, path: String): String { ... }


    fun getFileName(uri: Uri?, context: android.content.Context): String {
        if (uri == null) return "Belum ada foto yang dipilih"
        var fileName: String? = null
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    fileName = it.getString(nameIndex)
                }
            }
        }
        return fileName ?: "Foto.jpeg"
    }
}