package com.example.jeksoed.ui.screens.chat

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Data class untuk menampung data satu pesan
data class Message(
    val id: String = "",
    val text: String = "",
    val senderId: String = "",
    val timestamp: Timestamp? = null
)

// Data class untuk menampung semua state yang dibutuhkan UI
data class ChatUiState(
    val messages: List<Message> = emptyList(),
    val messageText: String = "" // State untuk teks di input field
)

class ChatViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val rideRequestId: String = savedStateHandle.get<String>("rideRequestId")!!
    private val db = FirebaseFirestore.getInstance()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState = _uiState.asStateFlow()

    init {
        listenForMessages()
    }

    private fun listenForMessages() {
        db.collection("chats").document(rideRequestId).collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, e ->
                if (snapshot != null) {
                    val messageList = snapshot.documents.mapNotNull {
                        it.toObject(Message::class.java)?.copy(id = it.id)
                    }
                    _uiState.update { it.copy(messages = messageList) }
                }
            }
    }

    // Fungsi untuk mengubah state saat user mengetik
    fun onMessageChanged(newText: String) {
        _uiState.update { it.copy(messageText = newText) }
    }

    // Fungsi untuk mengirim pesan
    fun sendMessage() {
        val textToSend = _uiState.value.messageText.trim()
        if (textToSend.isBlank() || currentUserId == null) return

        val message = hashMapOf(
            "text" to textToSend,
            "senderId" to currentUserId,
            "timestamp" to Timestamp.now()
        )
        db.collection("chats").document(rideRequestId).collection("messages").add(message)

        // Kosongkan input field setelah dikirim
        _uiState.update { it.copy(messageText = "") }
    }
}