
package com.example.jeksoed.ui.screens.passenger

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class ProfileUiState(
    val name: String = "Memuat...",
    val email: String = "Memuat...",
    val photoUrl: String = "",
    val isLoading: Boolean = true,
    val showLogoutDialog: Boolean = false, // State untuk dialog logout
    val showDeleteDialog: Boolean = false  // State untuk dialog hapus akun
)

class ProfileViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState = _uiState.asStateFlow()

    init {
        fetchUserData()
    }

    private fun fetchUserData() {
        viewModelScope.launch {
            val user = auth.currentUser
            if (user != null) {
                try {
                    val document = firestore.collection("users").document(user.uid).get().await()
                    if (document.exists()) {
                        _uiState.update {
                            it.copy(
                                name = document.getString("nama") ?: "Nama tidak ditemukan",
                                email = user.email ?: "Email tidak ditemukan",
                                photoUrl = document.getString("photoUrl") ?: "",
                                isLoading = false
                            )
                        }
                    }
                } catch (e: Exception) {
                    _uiState.update {
                        it.copy(
                            name = "Gagal memuat data",
                            email = "Gagal memuat data",
                            photoUrl = "",
                            isLoading = false
                        )
                    }
                }
            }
        }
    }

    fun onLogoutClick() {
        _uiState.update { it.copy(showLogoutDialog = true) }
    }

    fun onDismissLogoutDialog() {
        _uiState.update { it.copy(showLogoutDialog = false) }
    }

    fun onDeleteClick() {
        _uiState.update { it.copy(showDeleteDialog = true) }
    }

    fun onDismissDeleteDialog() {
        _uiState.update { it.copy(showDeleteDialog = false) }
    }


    fun confirmLogout() {
        auth.signOut()
        onDismissLogoutDialog() // Tutup dialog setelah logout
    }

    fun confirmDeleteAccount() {
        // PERHATIAN: Ini adalah operasi berbahaya.
        // Implementasi ini hanya contoh sederhana.
        viewModelScope.launch {
            try {
                val user = auth.currentUser
                val uid = user?.uid
                if (uid != null) {
                    // 1. Hapus data dari Firestore
                    firestore.collection("users").document(uid).delete().await()
                    // 2. Hapus user dari Auth
                    user.delete().await()
                }
            } catch (e: Exception) {
                // Handle error (misal: perlu re-autentikasi)
            } finally {
                onDismissDeleteDialog()
            }
        }
    }
}