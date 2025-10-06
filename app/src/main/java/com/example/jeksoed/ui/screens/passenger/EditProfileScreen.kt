// main/java/com/example/jeksoed/ui/screens/passenger/EditProfileScreen.kt

package com.example.jeksoed.ui.screens.passenger

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.jeksoed.R
import com.example.jeksoed.ui.components.PrimaryButton
import com.example.jeksoed.ui.theme.JekSoedTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    navController: NavController,
    viewModel: EditProfileViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            navController.popBackStack()
        }
    }
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Edit Foto
            Box(contentAlignment = Alignment.Center) {
                Image(
                    painter = painterResource(id = R.drawable.person_icon),
                    contentDescription = "Foto Profil",
                    modifier = Modifier.size(100.dp).clip(CircleShape)
                )
                Text(
                    "Tap to edit",
                    color = Color.White,
                    modifier = Modifier
                        .clickable { /*TODO: Image Picker Logic*/ }
                        .padding(4.dp)
                )
            }
            Spacer(modifier = Modifier.height(32.dp))

            // Input Fields
            OutlinedTextField(value = uiState.name, onValueChange = viewModel::onNameChange, label = {Text("Nama")}, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp))
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = uiState.phone, onValueChange = viewModel::onPhoneChange, label = {Text("No. Hp")}, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp))
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = uiState.oldPassword, onValueChange = viewModel::onOldPasswordChange, label = {Text("Password Lama")}, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), visualTransformation = PasswordVisualTransformation())
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = uiState.newPassword, onValueChange = viewModel::onNewPasswordChange, label = {Text("Password Baru")}, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), visualTransformation = PasswordVisualTransformation())

            Spacer(modifier = Modifier.weight(1f))

            PrimaryButton(
                text = if(uiState.isLoading) "Menyimpan..." else "Simpan",
                onClick = viewModel::saveChanges,
                isEnabled = !uiState.isLoading,
                containerColor = Color(0xFFFFC107),
                contentColor = Color.Black
            )
        }
    }
}

@Preview(showSystemUi = true)
@Composable
private fun EditProfileScreenPreview() {
    JekSoedTheme {
        // Tampilan ini tidak memerlukan NavController untuk preview
        // Jadi kita bisa langsung memanggilnya
    }
}