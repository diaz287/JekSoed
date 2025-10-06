// main/java/com/example/jeksoed/ui/screens/passenger/EditProfileViewModel.kt

package com.example.jeksoed.ui.screens.passenger

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class EditProfileUiState(
    val name: String = "",
    val phone: String = "",
    val oldPassword: String = "",
    val newPassword: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

class EditProfileViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
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
                        phone = userDoc.getString("nomorHp") ?: ""
                    )
                }
            }
        }
    }

    fun onNameChange(newName: String) { _uiState.update { it.copy(name = newName) } }
    fun onPhoneChange(newPhone: String) { _uiState.update { it.copy(phone = newPhone) } }
    fun onOldPasswordChange(pass: String) { _uiState.update { it.copy(oldPassword = pass) } }
    fun onNewPasswordChange(pass: String) { _uiState.update { it.copy(newPassword = pass) } }

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