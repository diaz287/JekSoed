// main/java/com/example/jeksoed/ui/screens/rating/RatingScreen.kt

package com.example.jeksoed.ui.screens.rating

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.jeksoed.R
import com.example.jeksoed.navigation.Screen
import com.example.jeksoed.ui.components.PrimaryButton
import com.example.jeksoed.ui.theme.JekSoedTheme
import com.example.jeksoed.utils.formatCurrency

@Composable
fun RatingScreen(
    navController: NavController,
    viewModel: RatingViewModel = viewModel(),
    driverId: String,
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    RatingScreenUI(
        uiState = uiState,
        onRatingChange = viewModel::onRatingChanged,
        onCommentChange = viewModel::onCommentChanged,
        onSubmitClick = {
            viewModel.submitRating()
            showDialog = true // Tampilkan dialog setelah submit
        }
    )

    if (showDialog) {
        TripFinishedDialog(onDismiss = {
            showDialog = false
            // Navigasi ke home setelah dialog ditutup
            navController.navigate(Screen.PassengerMain.route) {
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
            }
        })
    }
}

@Composable
private fun RatingScreenUI(
    uiState: RatingUiState,
    onRatingChange: (Int) -> Unit,
    onCommentChange: (String) -> Unit,
    onSubmitClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        Text("Kamu udah sampai di tujuanmu", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))

        // Driver Info
        Image(painter = painterResource(id = R.drawable.person_icon), contentDescription = "Foto Driver", modifier = Modifier.size(80.dp).clip(CircleShape))
        Spacer(modifier = Modifier.height(8.dp))
        Text("Fajar Nugros", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) // Ganti nama asli
        Text("R 6666 CA", color = Color.Gray) // Ganti plat asli
        Spacer(modifier = Modifier.height(24.dp))

        // Rute & Pembayaran
        RouteAndPayment()
        Spacer(modifier = Modifier.height(32.dp))

        // Rating
        Text("Gimana perjalananmu?", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        RatingStars(rating = uiState.selectedRating, onRatingChange = onRatingChange)
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = uiState.comment,
            onValueChange = onCommentChange,
            label = { Text("Ada pesan buat Kak Driver ga?") },
            modifier = Modifier.fillMaxWidth().height(120.dp)
        )
        Spacer(modifier = Modifier.height(32.dp))

        PrimaryButton(
            text = "Selesaikan Perjalanan",
            onClick = onSubmitClick,
            isEnabled = uiState.selectedRating > 0 && !uiState.isSubmitting,
            containerColor = Color(0xFFFFC107),
            contentColor = Color.Black
        )
    }
}

@Composable
private fun RouteAndPayment() {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(painter = painterResource(id = R.drawable.blue_icon), contentDescription = null, tint = Color.Unspecified)
            Spacer(modifier = Modifier.width(8.dp))
            Text("FK Unsoed")
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(painter = painterResource(id = R.drawable.locatio_icon), contentDescription = null, tint = Color.Unspecified)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Rumah Sakit Wiradadi")
        }
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Total pembayaran", color = Color.Gray)
            Text(formatCurrency(10000), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun RatingStars(rating: Int, onRatingChange: (Int) -> Unit) {
    Row {
        (1..5).forEach { index ->
            Icon(
                imageVector = if (index <= rating) Icons.Filled.Star else Icons.Outlined.StarBorder,
                contentDescription = null,
                tint = if (index <= rating) Color(0xFFFFC107) else Color.Gray,
                modifier = Modifier
                    .size(48.dp)
                    .clickable { onRatingChange(index) }
            )
        }
    }
}

@Composable
fun TripFinishedDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            PrimaryButton(text = "Kembali ke Home", onClick = onDismiss, containerColor = Color(0xFFFFC107), contentColor = Color.Black)
        },
        icon = { Image(painter = painterResource(id = R.drawable.jeksoed_logo), contentDescription = null) },
        title = { Text("Perjalanan Selesai 🏍️✨", textAlign = TextAlign.Center, fontWeight = FontWeight.Bold) },
        text = { Text("Makasih udah pakai JEKSOED! Semoga kita ketemu lagi di perjalanan selanjutnya ♥️", textAlign = TextAlign.Center) }
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun RatingScreenPassengerPreview() {
    JekSoedTheme {
        RatingScreenUI(uiState = RatingUiState(selectedRating = 4), onRatingChange = {}, onCommentChange = {}, onSubmitClick = {})
    }
}