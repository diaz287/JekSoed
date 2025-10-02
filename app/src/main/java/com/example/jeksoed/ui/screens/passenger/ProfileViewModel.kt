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

// Data class untuk menampung state UI Profil
data class ProfileUiState(
    val name: String = "Memuat...",
    val email: String = "Memuat...",
    val isLoading: Boolean = true
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
                                isLoading = false
                            )
                        }
                    }
                } catch (e: Exception) {
                    _uiState.update {
                        it.copy(
                            name = "Gagal memuat data",
                            email = "Gagal memuat data",
                            isLoading = false
                        )
                    }
                }
            }
        }
    }

    fun logout() {
        auth.signOut()
    }
}