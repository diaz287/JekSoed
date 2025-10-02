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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jeksoed.R // Pastikan import R ini benar
import com.example.jeksoed.ui.theme.JekSoedTheme
import kotlinx.coroutines.delay

// --- Data class untuk mock data (data palsu) ---
data class Category(val name: String, val iconResId: Int, val tag: String? = null)
data class HistoryItem(val title: String, val address: String)

@Composable
fun HomeScreen(
    onSearchClick: () -> Unit
) {
    // State untuk mengelola dialog "dalam pengembangan"
    var showDialog by remember { mutableStateOf(false) }

    // State untuk status notifikasi (true = ada notif, false = tidak ada)
    val hasNotification by remember { mutableStateOf(true) }

    // State untuk nama user (dalam aplikasi nyata, ini datang dari ViewModel/login state)
    val userName = "Rafi Purnama"

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            // Bagian 1: Header Atas (Sapaan & Notifikasi)
            TopHeader(
                name = userName,
                hasNotification = hasNotification,
                onNotificationClick = { /* TODO: Logika klik notifikasi */ }
            )

            // Bagian 2: Konten Utama dengan Latar Belakang Putih
            Card(
                modifier = Modifier
                    .fillMaxSize(),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                // Gunakan LazyColumn agar bisa di-scroll jika kontennya panjang
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    // Item untuk Search Bar
                    item {
                        SearchBarFake(onSearchClick)
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    // Item untuk Kategori
                    item {
                        Text("Kategori", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(16.dp))
                        CategoryGrid(onCategoryClick = { categoryName ->
                            if (categoryName == "JekClean" || categoryName == "Lainnya") {
                                showDialog = true
                            } else {
                                // TODO: Navigasi untuk JekMotor & JekMobil
                            }
                        })
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    // Item untuk "Baru baru ini"
                    item {
                        Text("Baru baru ini...", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))
                        RecentHistoryList()
                    }
                }
            }
        }
    }

    // Tampilkan dialog jika showDialog bernilai true
    if (showDialog) {
        DevelopmentDialog(onDismiss = { showDialog = false })
    }
}

@Composable
fun TopHeader(name: String, hasNotification: Boolean, onNotificationClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 32.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Halo, $name!",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        IconButton(onClick = onNotificationClick) {
            Box {
                Icon(
                    imageVector = if (hasNotification) Icons.Outlined.Notifications else Icons.Outlined.Notifications,
                    contentDescription = "Notifikasi",
                    modifier = Modifier.size(28.dp)
                )
                if (hasNotification) {
                    // Titik merah notifikasi
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(colorResource(R.color.unsoed))
                            .align(Alignment.TopEnd)
                    )
                }
            }
        }
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
fun CategoryGrid(onCategoryClick: (String) -> Unit) {
    // Mock data untuk kategori
    val categories = listOf(
        Category("JekMotor", R.drawable.motor_icon, "START 7K"),
        Category("JekMobil", R.drawable.car_icon, "START 13K"),
        Category("JekClean", R.drawable.cleaning_icon, "-10%"),
        Category(name = "Lainnya", iconResId = R.drawable.more_icon)
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.Top
    ) {
        categories.forEach { category ->
            CategoryItem(category = category, onClick = { onCategoryClick(category.name) })
        }
    }
}

@Composable
fun CategoryItem(category: Category, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            // Box ini hanya untuk mengatur posisi tag agar sedikit keluar
            modifier = Modifier.padding(top = 8.dp, start = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = category.iconResId),
                    contentDescription = category.name,
                    modifier = Modifier.size(40.dp).offset(x= (-5).dp)

                )
            }
            // Tag promo di atas ikon
            if (category.tag != null) {
                Text(
                    text = category.tag,
                    color = Color.White,
                    fontWeight = FontWeight.Normal,
                    fontSize = 11.sp,
                    modifier = Modifier
                        .align(Alignment.TopStart) // 1. Posisi di kiri atas
                        .offset(x = (-14).dp, y = (-16).dp) // 2. Digeser agar "menggantung"
                        .background(
                            color = Color(0xFF272343), // 3. Warna diubah sesuai gambar
                            shape = TagShape() // 4. Gunakan Shape kustom kita
                        )
                        .padding(horizontal = 5.dp, vertical = 0.dp)
                )
            }
        }
        Text(text = category.name, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}
@Composable
fun RecentHistoryList() {
    // Mock data untuk riwayat perjalanan
    val history = listOf(
        HistoryItem("Fakultas Kedokteran - Unsoed", "Jl. Dr. Gumbreg No.1, Mersi, Purwokerto..."),
        HistoryItem("Moro Mall", "Jl. Perintis Kemerdekaan, Purwokerto...")
    )

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        history.forEach { item ->
            HistoryRowItem(item = item)
        }
    }
}

@Composable
fun HistoryRowItem(item: HistoryItem) {
    Column {
        HorizontalDivider(thickness = 1.dp, color = Color.LightGray, modifier = Modifier.padding(bottom = 14.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(id = R.drawable.locatio_icon),
                tint = colorResource(R.color.unsoed),
                contentDescription = "Riwayat",
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(text = item.title, fontWeight = FontWeight.Medium)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = item.address, fontSize = 14.sp, color = Color.Gray)

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

@Composable
fun BannerSlider() {
    // 1. Siapkan data banner (ganti dengan gambar Anda di folder drawable)
    val promoBanners = listOf(
        R.drawable.car_banner,
        R.drawable.motor_banner,
        R.drawable.cleaning_banner
    )

    // 2. Buat PagerState untuk mengontrol pager
    val pagerState = rememberPagerState(pageCount = { promoBanners.size })

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // 3. Komponen HorizontalPager untuk slider
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp) // Agar banner di samping terlihat sedikit
        ) { page ->
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp) // Jarak antar banner
            ) {
                Image(
                    painter = painterResource(id = promoBanners[page]),
                    contentDescription = "Promo Banner ${page + 1}",
                    contentScale = ContentScale.Crop, // Agar gambar memenuhi card
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4. Indikator titik-titik di bawah slider
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            repeat(pagerState.pageCount) { index ->
                val color = if (pagerState.currentPage == index) MaterialTheme.colorScheme.primary else Color.LightGray
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(color)
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenPreview() {
    JekSoedTheme {
        HomeScreen(onSearchClick = {})
    }
}