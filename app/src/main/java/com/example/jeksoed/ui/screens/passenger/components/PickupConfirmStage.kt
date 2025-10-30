package com.example.jeksoed.ui.screens.passenger.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jeksoed.ui.components.PrimaryButton
import com.example.jeksoed.ui.screens.passenger.OrderStage
import com.example.jeksoed.ui.screens.passenger.OrderUiState
import com.example.jeksoed.ui.theme.JekSoedTheme
import com.example.jeksoed.R

@Composable
fun PickupConfirmStage(
    uiState: OrderUiState,
    onProceedClick: () -> Unit,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // --- Bagian Header ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Double check titik penjemputan",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Jangan sampai salah titik penjemputannya,yaa",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            // Tombol Edit dengan border
            OutlinedButton(
                onClick = onBackClick,
                shape = CircleShape,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black),
                border = BorderStroke(1.dp, Color(0xFFFFC107)) // Warna kuning
            ) {
                Text("Edit")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // --- Kartu Informasi Lokasi ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)) // Latar belakang kuning muda
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.locatio_icon),
                    contentDescription = "Lokasi Jemput",
                    tint = Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = uiState.pickupQuery,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        // Note: Alamat ini juga perlu disediakan dari ViewModel
                        text = uiState.pickupAddress,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- Tombol Lanjut ---
        PrimaryButton(
            text = "Lanjut bro",
            onClick = onProceedClick,
            isEnabled = !uiState.isRouteLoading,
            containerColor = Color(0xFFFFC107), // Warna kuning
            contentColor = Color.Black
        )
    }
}

@Preview(name = "Pickup Confirm Stage", showBackground = true)
@Composable
private fun PickupConfirmStagePreview() {
    JekSoedTheme {
        PickupConfirmStage(
            uiState = OrderUiState(
                stage = OrderStage.PICKUP_CONFIRM,
                pickupQuery = "Fakultas Kedokteran Unsoed",
                destinationQuery = "Rumah Sakit Wiradadi"
            ),
            onProceedClick = {},
            onBackClick = {}
        )
    }
}