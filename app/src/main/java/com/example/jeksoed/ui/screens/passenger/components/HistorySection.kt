package com.example.jeksoed.ui.screens.passenger.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jeksoed.R
import com.example.jeksoed.ui.screens.passenger.HistoryItem

@Composable
fun RecentHistoryList() {
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