package com.example.jeksoed.ui.screens.passenger

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jeksoed.R // Pastikan import R ini benar
import com.example.jeksoed.ui.screens.passenger.components.BannerSlider
import com.example.jeksoed.ui.screens.passenger.components.CategoryGrid
import com.example.jeksoed.ui.screens.passenger.components.RecentHistoryList
import com.example.jeksoed.ui.screens.passenger.components.RecommendationSection
import com.example.jeksoed.ui.screens.passenger.components.TopHeader
import com.example.jeksoed.ui.theme.JekSoedTheme
import kotlinx.coroutines.delay

// --- Data class untuk mock data (data palsu) ---
data class Category(val name: String, val iconResId: Int, val tag: String? = null)
data class HistoryItem(val title: String, val address: String)
@Composable
fun HomeScreen(
    onSearchClick: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    val hasNotification by remember { mutableStateOf(true) }
    val userName = "Rafi Purnama"

    // --- PENGATURAN UNTUK SCROLLING EFFECT ---
    val bannerHeight = 240.dp // Tinggi banner yang terlihat di awal
    val topHeaderHeight = 100.dp // Perkiraan tinggi TopHeader + paddingnya

    // Konversi Dp ke Px untuk perhitungan
    val bannerHeightPx = with(LocalDensity.current) { bannerHeight.toPx() }
    val topHeaderHeightPx = with(LocalDensity.current) { topHeaderHeight.toPx() }

    // State untuk melacak seberapa jauh header telah "terlipat" (collapsed)
    // Nilainya akan bergerak dari 0 (terbuka penuh) hingga maxOffsetPx (terlipat penuh)
    val collapsedOffsetPx = remember { mutableStateOf(0f) }
    val maxOffsetPx = bannerHeightPx - topHeaderHeightPx

    // Objek yang akan menangani logika "mencuri" scroll
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                val newOffset = collapsedOffsetPx.value + delta
                // Batasi pergerakan offset antara 0 dan nilai maksimumnya
                collapsedOffsetPx.value = newOffset.coerceIn(0f, maxOffsetPx)

                // Kembalikan seberapa banyak scroll yang kita "curi"
                return Offset.Zero
            }
        }
    }

    // --- STRUKTUR LAYOUT BARU ---
    Box(
        modifier = Modifier
            .fillMaxSize()
            // Terapkan nested scroll di container utama
            .nestedScroll(nestedScrollConnection)
    ) {
        // 1. BANNER (di lapisan paling bawah)
        // Offset vertikalnya dikontrol oleh state scroll
        BannerSlider(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp) // Tinggi asli banner tetap
                .graphicsLayer {
                    // Efek parallax: banner bergerak lebih lambat dari scroll
                    translationY = -collapsedOffsetPx.value * 0.5f
                }
        )

        // 2. KONTEN UTAMA (LazyColumn di dalam Surface)
        // Surface ini adalah "Card" putih yang akan bergerak
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    // Pergerakan utama dari card putih
                    translationY = bannerHeightPx - collapsedOffsetPx.value
                },
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            LazyColumn(
                contentPadding = PaddingValues(top = 16.dp) // Padding atas untuk konten
            ) {
                // SearchBar
                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        SearchBarFake(onSearchClick)
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
                // Kategori
                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Text("Kategori", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(40.dp))
                        CategoryGrid(onCategoryClick = { categoryName ->
                            if (categoryName == "JekClean" || categoryName == "Lainnya") {
                                showDialog = true
                            }
                        })
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
                // Baru baru ini
                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Text("Baru baru ini...", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))
                        RecentHistoryList()
                    }
                }
                // Rekomendasi
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    RecommendationSection()
                }
            }
        }

        // 3. TOP HEADER (di lapisan paling atas)
        // Background-nya akan berubah dari transparan menjadi putih
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface.copy(
                // Alpha (transparansi) dihitung berdasarkan progres scroll
                alpha = (collapsedOffsetPx.value / maxOffsetPx).coerceIn(0f, 1f)
            ),
            shadowElevation = 4.dp // Beri sedikit bayangan saat background putih muncul
        ) {
            TopHeader(
                name = userName,
                hasNotification = hasNotification,
                onNotificationClick = { /* TODO: Logika klik notifikasi */ }
            )
        }
    }

    if (showDialog) {
        DevelopmentDialog(onDismiss = { showDialog = false })
    }
}
@Composable
fun SearchBarFake(onSearchClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSearchClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(50), // Membuat lebih melengkung
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Text(
                text = "Mau ke mana hari ini?",
                color = Color.Gray,
                fontSize = 16.sp
            )
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Cari",
                tint = Color.Gray
            )

        }
    }
}


@Composable
fun DevelopmentDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Dalam Pengembangan") },
        text = { Text("Fitur ini masih dalam tahap pengembangan dan akan segera tersedia.") },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}

class TagShape(
    private val cornerRadius: Float = 16f,
    private val pointerWidth: Float = 20f,
    private val pointerHeight: Float = 20f
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path().apply {
            // Lebar dan tinggi dari kotak utama (tanpa pointer)
            val rectWidth = size.width
            val rectHeight = size.height

            // Mulai dari sudut kiri atas
            moveTo(0f, cornerRadius)
            // Gambar lengkungan sudut kiri atas
            arcTo(
                rect = androidx.compose.ui.geometry.Rect(0f, 0f, cornerRadius * 2, cornerRadius * 2),
                startAngleDegrees = 180f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
            // Garis lurus ke kanan atas
            lineTo(rectWidth - cornerRadius, 0f)
            // Gambar lengkungan sudut kanan atas
            arcTo(
                rect = androidx.compose.ui.geometry.Rect(rectWidth - cornerRadius * 2, 0f, rectWidth, cornerRadius * 2),
                startAngleDegrees = 270f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
            // Garis lurus ke kanan bawah
            lineTo(rectWidth, rectHeight - cornerRadius)
            // Gambar lengkungan sudut kanan bawah
            arcTo(
                rect = androidx.compose.ui.geometry.Rect(rectWidth - cornerRadius * 2, rectHeight - cornerRadius * 2, rectWidth, rectHeight),
                startAngleDegrees = 0f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
            // Garis lurus ke kiri bawah (sampai awal pointer)
            lineTo(cornerRadius + pointerWidth, rectHeight)

            // --- Bagian Pointer (segitiga di bawah) ---
            lineTo(cornerRadius + (pointerWidth / 2), rectHeight + pointerHeight)
            lineTo(cornerRadius, rectHeight)
            // --- Akhir Bagian Pointer ---

            // Garis lurus ke sudut kiri bawah
            lineTo(cornerRadius, rectHeight)
            // Gambar lengkungan sudut kiri bawah
            arcTo(
                rect = androidx.compose.ui.geometry.Rect(0f, rectHeight - cornerRadius * 2, cornerRadius * 2, rectHeight),
                startAngleDegrees = 90f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )

            // Tutup path
            close()
        }
        return Outline.Generic(path)
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenPreview() {
    JekSoedTheme {
        HomeScreen(onSearchClick = {})
    }
}