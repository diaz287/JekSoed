package com.example.jeksoed.ui.screens.chat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.jeksoed.ui.theme.JekSoedTheme
import com.google.firebase.Timestamp

@Composable
fun ChatScreen(
    navController: NavController,
    viewModel: ChatViewModel = viewModel(),
    rideRequestId: String
) {
    val uiState by viewModel.uiState.collectAsState()

    ChatScreenContent(
        uiState = uiState,
        currentUserId = viewModel.currentUserId ?: "",
        onMessageChange = { viewModel.onMessageChanged(it) },
        onSendClick = { viewModel.sendMessage() },
        onBackClick = { navController.popBackStack() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreenContent(
    uiState: ChatUiState,
    currentUserId: String,
    onMessageChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chat") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            LazyColumn(
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                reverseLayout = true // Pesan baru muncul dari bawah
            ) {
                items(uiState.messages.reversed()) { message ->
                    MessageBubble(
                        message = message,
                        isMyMessage = message.senderId == currentUserId
                    )
                }
            }
            MessageInput(
                value = uiState.messageText,
                onValueChange = onMessageChange,
                onSendClick = onSendClick
            )
        }
    }
}

@Composable
fun MessageBubble(message: Message, isMyMessage: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = if (isMyMessage) Arrangement.End else Arrangement.Start
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isMyMessage) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Text(
                text = message.text,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
            )
        }
    }
}

@Composable
fun MessageInput(value: String, onValueChange: (String) -> Unit, onSendClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Ketik pesan...") }
        )
        IconButton(onClick = onSendClick, enabled = value.isNotBlank()) {
            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Kirim")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ChatScreenPreview() {
    val dummyMessages = listOf(
        Message(text = "Halo, posisi di mana?", senderId = "passenger123", timestamp = Timestamp.now()),
        Message(text = "Saya di depan gerbang utama mas", senderId = "driverABC", timestamp = Timestamp.now()),
        Message(text = "Oke, ditunggu ya", senderId = "passenger123", timestamp = Timestamp.now())
    )
    val dummyUiState = ChatUiState(
        messages = dummyMessages,
        messageText = "Siap"
    )
    JekSoedTheme {
        ChatScreenContent(
            uiState = dummyUiState,
            currentUserId = "driverABC",
            onMessageChange = {},
            onSendClick = {},
            onBackClick = {}
        )
    }
}