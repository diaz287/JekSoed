package com.example.jeksoed.ui.screens.chat

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Mood
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.jeksoed.R
import com.example.jeksoed.ui.theme.JekSoedTheme
import com.google.firebase.Timestamp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    navController: NavController,
    rideRequestId: String,
    viewModel: ChatViewModel = viewModel(factory = ChatViewModelFactory(rideRequestId))
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri: Uri? ->
            uri?.let { viewModel.sendImage(it) }
        }
    )

    LaunchedEffect(uiState.messages) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            ChatTopBar(
                name = uiState.otherUserName,
                photoUrl = uiState.otherUserPhotoUrl,
                onBackClick = { navController.popBackStack() }
            )
        },
        bottomBar = {
            // Bungkus MessageInput dengan Box yang memiliki imePadding
            Box(modifier = Modifier.imePadding()) {
                MessageInput(
                    value = uiState.messageText,
                    isUploading = uiState.isUploading,
                    onValueChange = viewModel::onMessageChanged,
                    onSendClick = viewModel::sendMessage,
                    onAttachClick = {
                        imagePickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            items(uiState.messages) { message ->
                MessageBubble(
                    message = message,
                    isMyMessage = message.senderId == viewModel.currentUserId,
                    myPhotoUrl = uiState.currentUserPhotoUrl,
                    otherUserPhotoUrl = uiState.otherUserPhotoUrl
                )
            }
        }
    }
}

@Composable
fun ChatTopBar(name: String, photoUrl: String?, onBackClick: () -> Unit) {
    Surface(shadowElevation = 4.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
            }
            // --- PERBAIKAN: Gunakan AsyncImage ---
            AsyncImage(
                model = photoUrl,
                contentDescription = "Foto Profil",
                placeholder = painterResource(id = R.drawable.person_icon),
                error = painterResource(id = R.drawable.person_icon),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = name, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun MessageBubble(
    message: Message,
    isMyMessage: Boolean,
    myPhotoUrl: String?,
    otherUserPhotoUrl: String?
) {
    val arrangement = if (isMyMessage) Arrangement.End else Arrangement.Start
    val bubbleColor = if (isMyMessage) Color(0xFF272343) else Color(0xFFFFD803)
    val textColor = if (isMyMessage) Color.White else Color.Black
    val bubbleShape = if (isMyMessage) {
        RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp)
    } else {
        RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = arrangement,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isMyMessage) {
            // --- PERBAIKAN: Gunakan AsyncImage ---
            AsyncImage(
                model = otherUserPhotoUrl,
                contentDescription = "Foto Profil",
                placeholder = painterResource(id = R.drawable.person_icon),
                error = painterResource(id = R.drawable.person_icon),
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(24.dp).clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        Card(
            shape = bubbleShape,
            colors = CardDefaults.cardColors(containerColor = bubbleColor)
        ) {
            when (message.type) {
                "image" -> {
                    AsyncImage(
                        model = message.imageUrl,
                        contentDescription = "Gambar terkirim",
                        modifier = Modifier
                            .padding(4.dp)
                            .sizeIn(maxWidth = 200.dp, maxHeight = 250.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
                else -> {
                    Text(
                        text = message.text,
                        color = textColor,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                    )
                }
            }
        }

        if (isMyMessage) {
            Spacer(modifier = Modifier.width(8.dp))
            // --- PERBAIKAN: Gunakan AsyncImage ---
            AsyncImage(
                model = myPhotoUrl,
                contentDescription = "Foto Profil",
                placeholder = painterResource(id = R.drawable.person_icon),
                error = painterResource(id = R.drawable.person_icon),
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(24.dp).clip(CircleShape)
            )
        }
    }
}

// --- COMPOSABLE YANG DIPERBARUI UNTUK INPUT PESAN ---
@Composable
fun MessageInput(
    value: String,
    isUploading: Boolean,
    onValueChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onAttachClick: () -> Unit
) {
    Surface(shadowElevation = 8.dp) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            placeholder = { Text("Ketik pesan kamu") },
            shape = RoundedCornerShape(50),
            leadingIcon = {
                Icon(Icons.Outlined.Mood, contentDescription = "Emoji")
            },
            trailingIcon = {
                if (value.isBlank()) {
                    Row {
                        Icon(Icons.Outlined.Mic, contentDescription = "Voice Message", modifier = Modifier.padding(vertical = 12.dp))
                        IconButton(onClick = onAttachClick, enabled = !isUploading) {
                            if (isUploading) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            } else {
                                Icon(Icons.Outlined.PhotoCamera, contentDescription = "Kirim Gambar")
                            }
                        }
                    }
                } else {
                    IconButton(onClick = onSendClick) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Kirim")
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Gray,
                unfocusedBorderColor = Color.LightGray
            )
        )
    }
}


@Preview(showSystemUi = true)
@Composable
private fun ChatScreenPreview() {
    val dummyMessages = listOf(
        Message(text = "Posisi sesuai map kan, Kak?", senderId = "driverABC", timestamp = Timestamp.now()),
        Message(text = "Saya otw, Kak!", senderId = "driverABC", timestamp = Timestamp.now()),
        Message(text = "Oke, Kak! Saya di lobby FK.", senderId = "passenger123", timestamp = Timestamp.now())
    )
    val dummyUiState = ChatUiState(
        messages = dummyMessages,
        messageText = "",
        otherUserName = "Imedia Sholem",
    )
    JekSoedTheme {
        // Simulasi Scaffold untuk preview
        Scaffold(
            topBar = {
                ChatTopBar(
                    name = dummyUiState.otherUserName,
                    photoUrl = null,
                    onBackClick = {}
                )
            },
            bottomBar = {
                MessageInput(
                    value = dummyUiState.messageText,
                    isUploading = false,
                    onValueChange = {},
                    onSendClick = {},
                    onAttachClick = {}
                )
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier.padding(padding).padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                items(dummyMessages) { msg ->
                    MessageBubble(
                        message = msg,
                        isMyMessage = msg.senderId == "driverABC",
                        myPhotoUrl = null,
                        otherUserPhotoUrl = null
                    )
                }
            }
        }
    }
}