// main/java/com/example/jeksoed/ui/screens/trip/components/PaymentConfirmationCard.kt
package com.example.jeksoed.ui.screens.trip.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.jeksoed.R
import com.example.jeksoed.ui.theme.JekSoedTheme
import com.example.jeksoed.utils.formatCurrency

@Composable
fun PaymentConfirmationCard(
    totalPayment: String,
    onConfirmClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text("Konfirmasi Pembayaran", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Kalau udah sampai, jangan lupa ingatkan penumpang tagihannya, yaa!",
                textAlign = TextAlign.Start,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(painter = painterResource(id = R.drawable.cash_icon), contentDescription = "Cash", tint = Color.Unspecified)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cash (Tunai)")
                }
                Text(totalPayment, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onConfirmClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107)),
                shape = RoundedCornerShape(50)
            ) {
                Text("Amann, udah dibayar", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
private fun PaymentConfirmationCardPreview() {
    JekSoedTheme {
        PaymentConfirmationCard(
            totalPayment = formatCurrency(10000),
            onConfirmClick = {}
        )
    }
}