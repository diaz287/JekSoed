// File: ui/rating/RatingScreen.kt

package com.example.jeksoed.ui.screens.rating

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.jeksoed.navigation.Screen
import com.example.jeksoed.ui.theme.JekSoedTheme

@Composable
fun RatingScreen(
    navController: NavController,
    viewModel: RatingViewModel = viewModel(),
    driverId: String,
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Listener untuk event navigasi
    LaunchedEffect(Unit) {
        viewModel.navEvent.collect { event ->
            when (event) {
                is RatingNavEvent.NavigateToHome -> {
                    Toast.makeText(context, "Terima kasih atas ulasan Anda!", Toast.LENGTH_LONG).show()
                    navController.navigate(Screen.PassengerMain.route) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    }
                }
            }
        }
    }

    RatingScreenContent(
        uiState = uiState,
        onRatingChange = { viewModel.onRatingChanged(it) },
        onCommentChange = { viewModel.onCommentChanged(it) },
        onSubmitClick = { viewModel.submitRating() }
    )
}

// Tampilan ui
@Composable
fun RatingScreenContent(
    uiState: RatingUiState,
    onRatingChange: (Int) -> Unit,
    onCommentChange: (String) -> Unit,
    onSubmitClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Perjalanan Selesai!", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Berikan ulasan Anda untuk driver.")
        Spacer(modifier = Modifier.height(32.dp))

        // Bintang Rating
        Row {
            (1..5).forEach { index ->
                Icon(
                    imageVector = if (index <= uiState.selectedRating) Icons.Filled.Star else Icons.Outlined.StarBorder,
                    contentDescription = "Bintang $index",
                    tint = if (index <= uiState.selectedRating) Color(0xFFFFD700) else Color.Gray,
                    modifier = Modifier
                        .size(48.dp)
                        .clickable { onRatingChange(index) }
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = uiState.comment,
            onValueChange = onCommentChange,
            label = { Text("Tulis ulasan (opsional)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onSubmitClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState.selectedRating > 0 && !uiState.isSubmitting
        ) {
            if (uiState.isSubmitting) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
            } else {
                Text("Kirim Ulasan")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RatingScreenPreview() {
    JekSoedTheme {
        RatingScreenContent(
            uiState = RatingUiState(selectedRating = 4, comment = "Mantap, drivernya ramah!"),
            onRatingChange = {},
            onCommentChange = {},
            onSubmitClick = {}
        )
    }
}