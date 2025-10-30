package com.example.jeksoed.ui.screens.auth

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

class RegisterDriverViewModel : ViewModel() {
    var name by mutableStateOf("")
    var nim by mutableStateOf("")
    var email by mutableStateOf("")
    var phone by mutableStateOf("")
    var password by mutableStateOf("")
    var licensePlate by mutableStateOf("")
    var ktmUri by mutableStateOf<Uri?>(null)
    var stnkUri by mutableStateOf<Uri?>(null)
    var motorUri by mutableStateOf<Uri?>(null)
    var agreedToTerms by mutableStateOf(false)
    var isLoading by mutableStateOf(false)

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    suspend fun registerDriver(onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        if (!agreedToTerms) {
            onFailure("Anda harus menyetujui persyaratan.")
            return
        }
        val ktm = ktmUri
        val stnk = stnkUri
        val motor = motorUri
        if (ktm == null || stnk == null || motor == null) {
            onFailure("Harap lengkapi semua dokumen yang diperlukan.")
            return
        }

        isLoading = true
        try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val userId = authResult.user?.uid ?: throw Exception("Gagal mendapatkan User ID.")

            val ktmUrl = uploadFile(ktm, "driver_documents/$userId/ktm.jpg")
            val stnkUrl = uploadFile(stnk, "driver_documents/$userId/stnk.jpg")
            val motorUrl = uploadFile(motor, "driver_documents/$userId/motor.jpg")

            // --- PERBAIKAN: Lengkapi semua field User ---
            val userMap = hashMapOf(
                "uid" to userId,
                "nama" to name,
                "nim" to nim,
                "email" to email,
                "nomorHp" to phone,
                "role" to "driver",
                "licensePlate" to licensePlate,
                "ktmUrl" to ktmUrl,
                "stnkUrl" to stnkUrl,
                "motorUrl" to motorUrl,
                "createdAt" to FieldValue.serverTimestamp(),
                "totalRating" to 0L,
                "ratingCount" to 0L,
                "balance" to 0L
            )
            firestore.collection("users").document(userId).set(userMap).await()

            isLoading = false
            onSuccess()

        } catch (e: Exception) {
            isLoading = false
            onFailure(e.message ?: "Terjadi kesalahan tidak diketahui.")
        }
    }

    private suspend fun uploadFile(uri: Uri, path: String): String {
        val storageRef = storage.reference.child(path)
        storageRef.putFile(uri).await()
        return storageRef.downloadUrl.await().toString()
    }

    fun getFileName(uri: Uri?, context: Context): String {
        if (uri == null) return "Belum ada foto yang dipilih"
        var fileName: String? = null
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    fileName = it.getString(nameIndex)
                }
            }
        }
        return fileName ?: "Foto.jpeg"
    }
}