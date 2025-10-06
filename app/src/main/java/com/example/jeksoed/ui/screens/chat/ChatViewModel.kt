// main/java/com/example/jeksoed/ui/screens/chat/ChatViewModel.kt

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
import kotlinx.coroutines.tasks.await

// Data class Message tidak berubah
data class Message(
    val id: String = "",
    val text: String = "",
    val senderId: String = "",
    val timestamp: Timestamp? = null
)

// --- PERBARUI UI STATE ---
data class ChatUiState(
    val messages: List<Message> = emptyList(),
    val messageText: String = "",
    val otherUserName: String = "Memuat...", // Nama lawan bicara
    val otherUserPhotoUrl: String? = null, // Foto lawan bicara
    val currentUserPhotoUrl: String? = null // Foto pengguna saat ini
)

class ChatViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val rideRequestId: String = savedStateHandle.get<String>("rideRequestId")!!
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    val currentUserId = auth.currentUser?.uid

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState = _uiState.asStateFlow()

    init {
        if (currentUserId != null) {
            listenForMessages()
            loadUsersInfo()
        }
    }

    // --- FUNGSI BARU UNTUK MENGAMBIL INFO PENGGUNA ---
    private fun loadUsersInfo() {
        viewModelScope.launch {
            try {
                // 1. Ambil data ride request untuk menemukan ID passenger dan driver
                val rideRequestDoc = db.collection("ride_requests").document(rideRequestId).get().await()
                val passengerId = rideRequestDoc.getString("passengerId")
                val driverId = rideRequestDoc.getString("driverId")

                val otherUserId = if (currentUserId == passengerId) driverId else passengerId

                // Ambil foto profil user saat ini
                val currentUserDoc = db.collection("users").document(currentUserId!!).get().await()
                val currentUserPhoto = currentUserDoc.getString("photoUrl") // Asumsi nama field 'photoUrl'

                if (otherUserId != null) {
                    // 2. Ambil data user lawan bicara dari koleksi 'users'
                    val otherUserDoc = db.collection("users").document(otherUserId).get().await()
                    val otherUserName = otherUserDoc.getString("nama") ?: "User"
                    val otherUserPhoto = otherUserDoc.getString("photoUrl")

                    _uiState.update {
                        it.copy(
                            otherUserName = otherUserName,
                            otherUserPhotoUrl = otherUserPhoto,
                            currentUserPhotoUrl = currentUserPhoto
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(otherUserName = "Error") }
            }
        }
    }

    private fun listenForMessages() {
        // Fungsi ini tidak berubah
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

    fun onMessageChanged(newText: String) {
        // Fungsi ini tidak berubah
        _uiState.update { it.copy(messageText = newText) }
    }

    fun sendMessage() {
        // Fungsi ini tidak berubah
        val textToSend = _uiState.value.messageText.trim()
        if (textToSend.isBlank() || currentUserId == null) return

        val message = hashMapOf(
            "text" to textToSend,
            "senderId" to currentUserId,
            "timestamp" to Timestamp.now()
        )
        db.collection("chats").document(rideRequestId).collection("messages").add(message)

        _uiState.update { it.copy(messageText = "") }
    }
}