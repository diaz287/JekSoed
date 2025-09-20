package com.example.jeksoed.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import com.example.jeksoed.ui.theme.JekSoedTheme

@Composable
fun BottomBar(
    currentRoute: String?,
    onItemClick: (String) -> Unit
) {
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.History,
        BottomNavItem.Account
    )

    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.title) },
                label = { Text(item.title) },
                selected = currentRoute == item.screen_route,
                onClick = { onItemClick(item.screen_route) }
            )
        }
    }
}

sealed class BottomNavItem(var title: String, var icon: ImageVector, var screen_route: String) {
    object Home : BottomNavItem("Beranda", Icons.Filled.Home, "passenger_home")
    object History : BottomNavItem("Riwayat", Icons.Filled.ListAlt, "history")
    object Account : BottomNavItem("Akun", Icons.Filled.Person, "account")
}

@Preview(showBackground = true)
@Composable
private fun BottomBarPreview() {
    JekSoedTheme {
        BottomBar(currentRoute = "history", onItemClick = {})
    }
}