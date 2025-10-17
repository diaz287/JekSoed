package com.example.jeksoed.ui.screens.passenger.components

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jeksoed.R
import com.example.jeksoed.ui.screens.passenger.OrderStage
import com.example.jeksoed.ui.screens.passenger.OrderUiState
import com.example.jeksoed.ui.screens.passenger.OrderViewModel
import com.example.jeksoed.ui.screens.passenger.SavedPlace
import com.example.jeksoed.ui.theme.JekSoedTheme
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.net.PlacesClient

@Composable
fun SearchStage(
    uiState: OrderUiState,
    viewModel: OrderViewModel?,
    placesClient: PlacesClient?,
    onTextFieldFocus: () -> Unit,
    // --- TAMBAHKAN DUA PARAMETER INI ---
    context: Context,
    apiKey: String
) {
    val focusManager = LocalFocusManager.current

    Column {
        Card(shape = RoundedCornerShape(24.dp), border = BorderStroke(1.dp, Color.LightGray), colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(modifier = Modifier.padding(4.dp)) {
                SearchTextField(
                    query = uiState.pickupQuery,
                    onQueryChange = { query -> placesClient?.let { viewModel?.onPickupQueryChange(query, it) } },
                    onClear = { viewModel?.clearQuery(isPickup = true) },
                    onFocusChanged = { isFocused ->
                        if (isFocused) onTextFieldFocus()
                        if (isFocused && uiState.pickupQuery == "Lokasi saat ini") {
                            viewModel?.clearQuery(isPickup = true)
                        }
                    },
                    placeholder = "Lokasi Jemput",
                    onLocationClick = {
                        viewModel?.userLocation?.value?.let { viewModel.setUserLocationAsPickup(it) }
                    }
                )
                HorizontalDivider(
                    thickness = DividerDefaults.Thickness,
                    color = DividerDefaults.color
                )
                SearchTextField(
                    query = uiState.destinationQuery,
                    onQueryChange = { query -> placesClient?.let { viewModel?.onDestinationQueryChange(query, it) } },
                    onClear = { viewModel?.clearQuery(isPickup = false) },
                    onFocusChanged = { isFocused -> if (isFocused) onTextFieldFocus() },
                    placeholder = "Mau ke mana, nih?",
                    onLocationClick = {}
                )
            }
        }

        if (uiState.predictions.isNotEmpty()) {
            LazyColumn(modifier = Modifier.fillMaxHeight(0.7f).padding(top = 8.dp)) {
                items(uiState.predictions) { prediction ->
                    PredictionItem(prediction = prediction) {
                        focusManager.clearFocus()
                        if (placesClient != null) {
                            // --- PERBAIKAN: Tambahkan context dan apiKey ---
                            viewModel?.selectPrediction(prediction, placesClient, context, apiKey)
                        }
                    }
                }
            }
        } else {
            TersimpanSection(places = uiState.savedPlaces)
        }
    }
}
@Composable
fun SearchTextField(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    onFocusChanged: (isFocused: Boolean) -> Unit,
    placeholder: String,
    onLocationClick: () -> Unit
) {
    val isPickup = placeholder == "Lokasi Jemput"

    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged { onFocusChanged(it.isFocused) },
        placeholder = { Text(placeholder, color = Color.Gray) },
        leadingIcon = {
            Icon(
                painter = painterResource(if (isPickup) R.drawable.blue_icon else R.drawable.locatio_icon),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = Color.Unspecified
            )
        },
        trailingIcon = {
            if (isPickup) {
                if (query.isEmpty() && query != "Lokasi saat ini") {
                    IconButton(onClick = onLocationClick) {
                        Icon(
                            painter = painterResource(id = R.drawable.point_icon),
                            contentDescription = "Gunakan Lokasi Saat Ini",
                            modifier = Modifier.size(24.dp),
                            tint = Color.Unspecified
                        )
                    }
                } else {
                    IconButton(onClick = onClear) {
                        Icon(
                            painter = painterResource(id = R.drawable.clear_icon),
                            contentDescription = "Clear",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            } else { // For destination input
                if (query.isNotEmpty()) {
                    IconButton(onClick = onClear) {
                        Icon(
                            painter = painterResource(id = R.drawable.clear_icon),
                            contentDescription = "Clear",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent,
            cursorColor = MaterialTheme.colorScheme.primary
        ),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { /* Logic dipindah ke onQueryChange */ }),
        singleLine = true
    )
}

@Composable
fun PredictionItem(prediction: AutocompletePrediction, onClick: () -> Unit) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .clickable(onClick = onClick)
        .padding(16.dp)) {
        Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Gray)
        Spacer(Modifier.width(16.dp))
        Column {
            Text(prediction.getPrimaryText(null).toString(), fontWeight = FontWeight.Bold)
            Text(prediction.getSecondaryText(null).toString(), fontSize = 12.sp, color = Color.Gray)
        }
    }
}


// --- COMPOSABLE BARU UNTUK SECTION TERSIMPAN ---
@Composable
fun TersimpanSection(places: List<SavedPlace>) {
    Column(modifier = Modifier.padding(top = 24.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp).border(border = BorderStroke(1.dp, Color.LightGray), shape = RoundedCornerShape(32.dp)).padding(6.dp)

        ) {
            Icon(
                painter = painterResource(id = R.drawable.love_icon),
                contentDescription = "Tersimpan",
                modifier = Modifier.size(20.dp),
                tint = Color.Red
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Tersimpan", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), thickness = DividerDefaults.Thickness, color = DividerDefaults.color)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(places) { place ->
                SavedPlaceItem(place = place)
            }
            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), thickness = DividerDefaults.Thickness, color = DividerDefaults.color)
            }
        }
    }
}

@Composable
fun SavedPlaceItem(place: SavedPlace) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.locatio_icon),
            contentDescription = "Lokasi",
            modifier = Modifier.size(24.dp),
            tint = Color.Gray
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = place.title, style = MaterialTheme.typography.titleMedium,fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = place.distance, fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
            }
            Text(
                text = place.address,
                fontSize = 12.sp,
                color = Color.Gray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

        }
        Spacer(modifier = Modifier.width(16.dp))
        Icon(
            painter = painterResource(id = R.drawable.love_icon),
            contentDescription = "Favorit",
            modifier = Modifier.size(24.dp),
            tint = Color.Unspecified
        )
    }
}


// --- PREVIEWS (Tidak perlu diubah) ---
@Preview(name = "Search Stage - Default", showBackground = true)
@Composable
private fun SearchStagePreview() {
    JekSoedTheme {
        SearchStage(
            uiState = OrderUiState(
                stage = OrderStage.SEARCHING,
                pickupQuery = "Lokasi saat ini",
                savedPlaces = listOf(
                    SavedPlace("RITA SuperMall Purwokerto", "Jl. Jend. Sudirman No.296, Pereng...", "3.6 Km")
                )
            ),
            viewModel = null,
            placesClient = null,
            onTextFieldFocus = {},
            context = LocalContext.current,
            apiKey = ""
        )
    }
}

@Preview(name = "Search Stage - Custom Pickup", showBackground = true)
@Composable
private fun SearchStage_CustomPickupPreview() {
    JekSoedTheme {
        SearchStage(
            uiState = OrderUiState(
                stage = OrderStage.SEARCHING,
                pickupQuery = "" // Teks jemputan kosong
            ),
            viewModel = null,
            placesClient = null,
            onTextFieldFocus = {},
            context = LocalContext.current,
            apiKey = ""
        )
    }
}

@Preview(name = "Search Stage - Typing", showBackground = true)
@Composable
private fun SearchStage_TypingPreview() {
    JekSoedTheme {
        SearchStage(
            uiState = OrderUiState(
                stage = OrderStage.SEARCHING,
                pickupQuery = "Jepang",
                destinationQuery = "Moro Mall Purwokerto"
            ),
            viewModel = null,
            placesClient = null,
            onTextFieldFocus = {},
            context = LocalContext.current,
            apiKey = ""
        )
    }
}