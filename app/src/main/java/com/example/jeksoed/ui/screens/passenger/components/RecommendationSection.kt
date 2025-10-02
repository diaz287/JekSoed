package com.example.jeksoed.ui.screens.passenger.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.jeksoed.R // Ganti dengan R dari package Anda
import com.example.jeksoed.ui.theme.JekSoedTheme

// 1. Data Class untuk menampung informasi setiap item
data class Recommendation(
    val imageResId: Int,
    val title: String,
    val description: String,
    val italicWord: String
)

// --- Composable Utama ---
// Di file RecommendationSection.kt

@Composable
fun RecommendationSection(modifier: Modifier = Modifier) {
    // Data dummy untuk ditampilkan dalam daftar
    val recommendations = listOf(
        Recommendation(
            imageResId = R.drawable.cafe_banner, // Ganti dengan gambar Anda
            title = "Butuh Rekomendasi Kafe?",
            description = "Butuh tempat belajar, nongki, rapat, atau me-time, nih? Cuss, liat rekomendasi dari Mimin!",
            italicWord = "liat"
        ),
        Recommendation(
            imageResId = R.drawable.wisata_banner, // Ganti dengan gambar Anda
            title = "Cari Tempat Wisata Seru?",
            description = "Bingung mau healing ke mana? Tenang, Mimin punya daftar tempat wisata hits yang wajib kamu kunjungi!",
            italicWord = "Tenang"
        )
    )

    // HAPUS LazyColumn, ganti dengan Column biasa
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp) // Memberi jarak antar item
    ) {
        // Bagian Header
        Column {
            Text(
                text = "Cari berbagai rekomendasi tempat & kegiatan seru di Purwokerto!",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }

        // Looping manual untuk menampilkan setiap kartu
        recommendations.forEach { recommendation ->
            RecommendationItemCard(
                recommendation = recommendation,
                onClick = { /* TODO: Logika saat kartu di-klik */ }
            )
        }
    }
}

// --- Composable untuk Satu Kartu ---

@Composable
fun RecommendationItemCard(
    recommendation: Recommendation,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            // Bagian Gambar
            // Jika Anda belum punya gambar, gunakan Box sebagai placeholder
            Image(
                painter = painterResource(id = recommendation.imageResId),
                contentDescription = recommendation.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                contentScale = ContentScale.Crop
            )
            /*
            // --- Placeholder jika gambar belum ada ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = "Image Placeholder",
                    tint = Color.Gray,
                    modifier = Modifier.size(48.dp)
                )
            }
            */

            // Bagian Teks
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = recommendation.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Menggunakan AnnotatedString untuk membuat sebagian teks menjadi miring (italic)
                val annotatedDescription = buildAnnotatedString {
                    val parts = recommendation.description.split(recommendation.italicWord)
                    append(parts[0])
                    withStyle(style = SpanStyle(fontStyle = FontStyle.Italic)) {
                        append(recommendation.italicWord)
                    }
                    if (parts.size > 1) {
                        append(parts[1])
                    }
                }

                Text(
                    text = annotatedDescription,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.DarkGray
                )
            }
        }
    }
}


// --- Preview untuk melihat hasil ---

@Preview(showBackground = true)
@Composable
fun RecommendationSectionPreview() {
    JekSoedTheme {
        RecommendationSection(modifier = Modifier.padding(vertical = 16.dp))
    }
}