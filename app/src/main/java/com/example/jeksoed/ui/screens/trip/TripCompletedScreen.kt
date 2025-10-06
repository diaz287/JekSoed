// main/java/com/example/jeksoed/ui/screens/trip/TripCompletedScreen.kt

package com.example.jeksoed.ui.screens.trip

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.jeksoed.R
import com.example.jeksoed.navigation.Screen
import com.example.jeksoed.ui.components.PrimaryButton
import com.example.jeksoed.ui.theme.JekSoedTheme
import com.example.jeksoed.utils.formatCurrency

@Composable
fun TripCompletedScreen(
    navController: NavController,
    rideRequestId: String,
    viewModel: TripCompletedViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    TripCompletedScreenUI(
        uiState = uiState,
        formattedDate = viewModel.getFormattedDate(uiState.rideRequest?.createdAt),
        onFeedbackChange = viewModel::onFeedbackChanged,
        onFinishClick = {
            viewModel.submitAndFinish()
            navController.navigate(Screen.DriverMain.route) {
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
            }
        }
    )
}

@Composable
private fun TripCompletedScreenUI(
    uiState: TripCompletedUiState,
    formattedDate: String,
    onFeedbackChange: (String) -> Unit,
    onFinishClick: () -> Unit
) {
    Scaffold(
        containerColor = Color(0xFFF5F5F5)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically ) {
                Image(painter = painterResource(id = R.drawable.jeksoed_logo), contentDescription = "Logo", modifier = Modifier.size(64.dp) )
                Image(painter = painterResource(id = R.drawable.jeksoed_name), contentDescription = "Jeksoed", modifier = Modifier.size(144.dp))
            }
            Text("Cihuy Selesai!", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(24.dp))

            if (uiState.isLoading) {
                CircularProgressIndicator()
            } else {
                RideSummaryCard(uiState, formattedDate)
                Spacer(modifier = Modifier.height(24.dp))
                FeedbackCard(uiState, onFeedbackChange)
            }

            Spacer(modifier = Modifier.height(24.dp))

            PrimaryButton(
                text = "Selesai",
                onClick = onFinishClick,
                containerColor = Color(0xFFFFC107),
                contentColor = Color.Black
            )
        }
    }
}

@Composable
private fun RideSummaryCard(uiState: TripCompletedUiState, formattedDate: String) {
    // --- TAMBAHKAN INI untuk manajemen clipboard ---
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val orderNumber = uiState.rideRequest?.id?.take(8) ?: "JR-FN-00001"

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(painter = painterResource(id = R.drawable.motor_icon), contentDescription = "JekRide", tint = Color.Unspecified)
                Spacer(modifier = Modifier.width(8.dp))
                Text("JekRide", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically // Pastikan alignment vertikal
            ) {
                Text(formattedDate, fontSize = 12.sp, color = Color.Gray)

                // --- PERUBAHAN DI SINI: Baris Nomor Pesanan ---
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable {
                            clipboardManager.setText(AnnotatedString(orderNumber))
                            Toast
                                .makeText(context, "No. Pesanan disalin!", Toast.LENGTH_SHORT)
                                .show()
                        }
                        .padding(4.dp) // Beri padding agar area klik lebih besar
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy, // Ikon Salin
                        contentDescription = "Salin Nomor Pesanan",
                        modifier = Modifier.size(14.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(orderNumber, fontSize = 12.sp, color = Color.Gray)
                }
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                PaymentRow("Tagih Tunai", formatCurrency(uiState.totalFare), isHighlighted = true)
                PaymentRow("JEKSOED Deposit", "-${formatCurrency(uiState.deposit)}")
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            PaymentRow("Pendapatan", formatCurrency(uiState.earnings), isBold = true)

            Spacer(modifier = Modifier.height(16.dp))
            Text("Detail Pesanan >", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterHorizontally))
        }
    }
}

// --- Composable lain di bawah ini tidak ada perubahan ---

@Composable
private fun PaymentRow(label: String, value: String, isHighlighted: Boolean = false, isBold: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(
            text = label,
            color = if (isHighlighted) MaterialTheme.colorScheme.primary else Color.Gray,
            fontWeight = if(isHighlighted) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            text = value,
            color = Color.Black,
            fontWeight = if (isBold || isHighlighted) FontWeight.Bold else FontWeight.Normal
        )
    }
}


@Composable
private fun FeedbackCard(uiState: TripCompletedUiState, onFeedbackChange: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Penumpangnya aman?", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Ada keluhan tentang penumpang? Masukanmu enggak akan berpengaruh ke akunmu kok",
                fontSize = 12.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = uiState.feedbackText,
                onValueChange = onFeedbackChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Text box") },
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
private fun TripCompletedScreenPreview() {
    JekSoedTheme {
        TripCompletedScreenUI(
            uiState = TripCompletedUiState(isLoading = false),
            formattedDate = "1 September 2025",
            onFeedbackChange = {},
            onFinishClick = {}
        )
    }
}