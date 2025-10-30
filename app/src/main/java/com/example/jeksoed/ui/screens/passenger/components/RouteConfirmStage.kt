package com.example.jeksoed.ui.screens.passenger.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jeksoed.R
import com.example.jeksoed.data.model.RouteInfo
import com.example.jeksoed.ui.components.PrimaryButton
import com.example.jeksoed.ui.screens.passenger.OrderStage
import com.example.jeksoed.ui.screens.passenger.OrderUiState
import com.example.jeksoed.ui.theme.JekSoedTheme

@Composable
fun RouteConfirmStage(
    uiState: OrderUiState,
    onCreateOrderClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Header JekMotor
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(R.drawable.motor_icon),
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = Color.Unspecified
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text("JekMotor", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

        // Detail Informasi Pesanan
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            InfoRow(label = "Estimasi Biaya", value = uiState.routeInfo?.price ?: "-")
            InfoRow(label = "Jarak", value = uiState.routeInfo?.distance ?: "-")
            InfoRow(label = "Kapasitas", value = "1 orang")
            InfoRow(label = "Metode Pembayaran") {
                Row(
                    modifier = Modifier.clickable { /* TODO: Buka Opsi Pembayaran */ },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.cash_icon), // Ganti dengan ikon cash Anda
                        contentDescription = "Cash",
                        modifier = Modifier.size(24.dp),
                        tint = Color.Unspecified
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cash (Tunai)", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = null)
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        PrimaryButton(
            text = "Lanjut bro",
            onClick = onCreateOrderClick,
            containerColor = Color(0xFFFFC107),
            contentColor = Color.Black
        )
    }
}

// Composable bantuan untuk menampilkan baris informasi
@Composable
private fun InfoRow(label: String, value: String) {
    InfoRow(label = label) {
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun InfoRow(label: String, valueContent: @Composable () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 14.sp, color = Color.Black, style = MaterialTheme.typography.bodyMedium)
        valueContent()
    }
}


@Preview(showBackground = true)
@Composable
private fun RouteConfirmStagePreview() {
    JekSoedTheme {
        RouteConfirmStage(
            uiState = OrderUiState(
                stage = OrderStage.ROUTE_CONFIRM,
                routeInfo = RouteInfo(
                    distance = "3.1 km",
                    duration = "10 min",
                    polylinePoints = emptyList(),
                    encodedPath = "",
                    price = "Rp 10.000"
                )
            ),
            onCreateOrderClick = {}
        )
    }
}