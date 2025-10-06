// main/java/com/example/jeksoed/ui/screens/activity/ActivityDetailScreen.kt

package com.example.jeksoed.ui.screens.activity

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.jeksoed.R
import com.example.jeksoed.ui.theme.JekSoedTheme
import com.example.jeksoed.utils.formatCurrency

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityDetailScreen(
    navController: NavController,
    rideRequestId: String,
    viewModel: ActivityDetailViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detail") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { padding ->
        if(uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            ActivityDetailScreenUI(
                uiState = uiState,
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@Composable
fun ActivityDetailScreenUI(
    uiState: ActivityDetailUiState,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.verticalScroll(rememberScrollState())) {
        // Placeholder Peta
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Placeholder untuk Peta Statis", color = Color.Gray)
        }

        // Detail Sheet
        DetailSheet(uiState = uiState, isDriverView = uiState.isDriver)
    }
}


@Composable
fun DetailSheet(uiState: ActivityDetailUiState, isDriverView: Boolean) {
    Column(modifier = Modifier.padding(16.dp)) {
        // Tampilkan info yang relevan berdasarkan peran
        if (isDriverView) {
            UserInfoRow(name = uiState.otherUserName, isChatEnabled = uiState.isChatEnabled)
        } else {
            DriverInfoRow(
                name = uiState.otherUserName,
                plate = "R 6666 CA", // Ganti dengan data asli
                rating = 4.8,      // Ganti dengan data asli
                isChatEnabled = uiState.isChatEnabled
            )
        }
        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

        // Rute
        RouteRow(iconRes = R.drawable.blue_icon, location = "FK Unsoed")
        Spacer(modifier = Modifier.height(8.dp))
        RouteRow(iconRes = R.drawable.locatio_icon, location = "Rumah Sakit Wiradadi")
        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

        // Waktu
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            TimeColumn("Waktu Berangkat", "09:17")
            TimeColumn("Waktu Tiba", "09:30")
        }
        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

        // Tanggal & Total
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Tanggal", color = Color.Gray)
            Text("1 Sep 2025", fontWeight = FontWeight.SemiBold)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Total Pembayaran", color = Color.Gray)
            Text(formatCurrency(10000), fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("* Fitur chat dan telepon ke driver hanya tersedia hingga 30 menit setelah perjalanan selesai.", color = Color.Gray, style = MaterialTheme.typography.bodySmall)

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

        // Rating
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(
                if (isDriverView) "Rating dari penumpang" else "Rating dari kamu",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row {
                repeat(5) {
                    Icon(Icons.Filled.Star, contentDescription = null, tint = Color(0xFFFFC107), modifier = Modifier.size(32.dp))
                }
            }
        }
    }
}

@Composable
fun TimeColumn(label: String, time: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = Color.Gray)
        Text(time, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun RouteRow(iconRes: Int, location: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = Color.Unspecified
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(location)
    }
}

@Composable
fun UserInfoRow(name: String, isChatEnabled: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Image(painter = painterResource(id = R.drawable.person_icon), contentDescription = "Foto", modifier = Modifier.size(48.dp).clip(CircleShape))
        Spacer(modifier = Modifier.width(12.dp))
        Text(name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        IconButton(onClick = { /*TODO*/ }, enabled = isChatEnabled) {
            Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "Chat")
        }
    }
}

@Composable
fun DriverInfoRow(name: String, plate: String, rating: Double, isChatEnabled: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Image(painter = painterResource(id = R.drawable.person_icon), contentDescription = "Foto", modifier = Modifier.size(48.dp).clip(CircleShape))
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(plate, color = Color.Gray)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(rating.toString(), fontWeight = FontWeight.SemiBold)
                Icon(Icons.Default.Star, contentDescription = "Rating", tint = Color(0xFFFFC107), modifier = Modifier.size(16.dp))
            }
        }
        IconButton(onClick = { /*TODO*/ }, enabled = isChatEnabled) {
            Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "Chat")
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Preview(showSystemUi = true, name = "Detail (Tampilan Penumpang)")
@Composable
private fun ActivityDetailScreenPassengerPreview() {
    JekSoedTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Detail") },
                    navigationIcon = {
                        IconButton(onClick = { }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                        }
                    }
                )
            }
        ) { padding ->
            ActivityDetailScreenUI(
                uiState = ActivityDetailUiState(
                    isLoading = false,
                    otherUserName = "Fajar Nugros",
                    isDriver = false // Mensimulasikan sebagai penumpang
                ),
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showSystemUi = true, name = "Detail (Tampilan Driver)")
@Composable
private fun ActivityDetailScreenDriverPreview() {
    JekSoedTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Detail") },
                    navigationIcon = {
                        IconButton(onClick = { }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                        }
                    }
                )
            }
        ) { padding ->
            ActivityDetailScreenUI(
                uiState = ActivityDetailUiState(
                    isLoading = false,
                    otherUserName = "Imedia Sholem",
                    isDriver = true // Mensimulasikan sebagai driver
                ),
                modifier = Modifier.padding(padding)
            )
        }
    }
}