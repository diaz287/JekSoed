package com.example.jeksoed.ui.screens.passenger.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.jeksoed.R
import com.example.jeksoed.ui.theme.JekSoedTheme

@Composable
fun FindingDriverStage(
    onCancelClick: () -> Unit
) {
    // Animasi untuk menggerakkan motor (0f -> 1f)
    val infiniteTransition = rememberInfiniteTransition(label = "motor_animation")
    val motorPosition by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "motor_position"
    )

    // Dapatkan lebar layar untuk perhitungan posisi
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val iconSize = 32.dp
    // Lebar maksimal yang bisa dijelajahi ikon (lebar layar - lebar ikon - padding)
    val travelDistance = screenWidth - iconSize - (16.dp * 2) // 16.dp adalah padding horizontal Column utama

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Sedang mencari driver",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Tunggu dulu, yaa",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(32.dp))

        // --- Layout Animasi yang Diperbarui ---
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start // Penting: agar ikon mulai dari kiri
        ) {
            // Ikon Motor
            Icon(
                painter = painterResource(id = R.drawable.motor_icon),
                contentDescription = "Mencari driver",
                tint = Color.Unspecified,
                modifier = Modifier
                    .size(iconSize)
                    .offset(x = travelDistance * motorPosition) // Gerakkan ikon dengan padding
            )

            // Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(motorPosition)
                        .height(8.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF3B3781)) // Warna ungu tua
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Tombol Batal
        Button(
            onClick = onCancelClick,
            modifier = Modifier.fillMaxWidth(),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFFFBEB), // Kuning muda
                contentColor = Color.Black
            )
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Close, contentDescription = "Batal")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Mau dibatalin?")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FindingDriverStagePreview() {
    JekSoedTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            FindingDriverStage(onCancelClick = {})
        }
    }
}