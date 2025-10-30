// main/java/com/example/jeksoed/ui/screens/passenger/EditProfileViewModel.kt

package com.example.jeksoed.ui.screens.passenger

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

data class EditProfileUiState(
    val name: String = "",
    val phone: String = "",
    val photoUrl: String = "",
    val oldPassword: String = "",
    val newPassword: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

class EditProfileViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val currentUser = auth.currentUser

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        if (currentUser != null) {
            viewModelScope.launch {
                val userDoc = firestore.collection("users").document(currentUser.uid).get().await()
                _uiState.update {
                    it.copy(
                        name = userDoc.getString("nama") ?: "",
                        phone = userDoc.getString("nomorHp") ?: "",
                        photoUrl = userDoc.getString("photoUrl") ?: ""
                    )
                }
            }
        }
    }

    fun onNameChange(newName: String) { _uiState.update { it.copy(name = newName) } }
    fun onPhoneChange(newPhone: String) { _uiState.update { it.copy(phone = newPhone) } }
    fun onOldPasswordChange(pass: String) { _uiState.update { it.copy(oldPassword = pass) } }
    fun onNewPasswordChange(pass: String) { _uiState.update { it.copy(newPassword = pass) } }
    fun uploadProfilePhoto(uri: Uri) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }

                val uid = currentUser?.uid ?: return@launch
                val fileName = "${UUID.randomUUID()}.jpg"
                val ref = storage.reference.child("profile_images/$uid/$fileName")

                // Upload foto
                ref.putFile(uri).await()
                val downloadUrl = ref.downloadUrl.await().toString()

                // Simpan URL ke Firestore
                firestore.collection("users").document(uid)
                    .update("photoUrl", downloadUrl)
                    .await()

                _uiState.update { it.copy(photoUrl = downloadUrl, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Gagal upload foto.") }
            }
        }
    }

    fun deleteProfilePhoto() {
        viewModelScope.launch {
            try {
                val uid = currentUser?.uid ?: return@launch
                _uiState.update { it.copy(isLoading = true) }

                // Hapus URL foto di Firestore
                firestore.collection("users").document(uid)
                    .update("photoUrl", "")
                    .await()

                // Update UI state agar foto hilang
                _uiState.update { it.copy(photoUrl = "", isLoading = false, successMessage = "Foto profil dihapus.") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Gagal menghapus foto profil.") }
            }
        }
    }


    fun saveChanges() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, successMessage = null) }
            if (currentUser == null) {
                _uiState.update { it.copy(isLoading = false, error = "User tidak ditemukan.") }
                return@launch
            }

            try {
                // Update Nama dan No. Hp di Firestore
                val userUpdates = mapOf(
                    "nama" to uiState.value.name,
                    "nomorHp" to uiState.value.phone
                )
                firestore.collection("users").document(currentUser.uid).update(userUpdates).await()

                // Update Password jika diisi
                if (uiState.value.oldPassword.isNotBlank() && uiState.value.newPassword.isNotBlank()) {
                    val credential = EmailAuthProvider.getCredential(currentUser.email!!, uiState.value.oldPassword)
                    currentUser.reauthenticate(credential).await()
                    currentUser.updatePassword(uiState.value.newPassword).await()
                }

                _uiState.update { it.copy(isLoading = false, successMessage = "Profil berhasil diperbarui!") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Gagal memperbarui profil.") }
            }
        }
    }
}