package com.example.jeksoed.ui.screens.passenger.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.jeksoed.ui.screens.passenger.OrderStage
import com.example.jeksoed.ui.screens.passenger.OrderUiState
import com.example.jeksoed.ui.screens.passenger.OrderViewModel
import com.google.android.libraries.places.api.net.PlacesClient

@Composable
fun OrderSheetContent(
    uiState: OrderUiState,
    viewModel: OrderViewModel,
    placesClient: PlacesClient,
    apiKey: String,
    onTextFieldFocus: () -> Unit,
    onCreateOrderClick: () -> Unit
) {
    val context = LocalContext.current

    Column(modifier = Modifier.padding(horizontal = 16.dp).imePadding()) {
        Box(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(modifier = Modifier.width(40.dp).height(4.dp).clip(CircleShape).background(Color.LightGray))
        }

        AnimatedContent(
            targetState = uiState.stage,
            transitionSpec = { fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300)) }
        ) { stage ->
            when (stage) {
                OrderStage.SEARCHING -> SearchStage(
                    uiState = uiState,
                    viewModel = viewModel,
                    placesClient = placesClient,
                    onTextFieldFocus = onTextFieldFocus,
                    context = context,
                    apiKey = apiKey
                )
                OrderStage.PICKUP_CONFIRM -> PickupConfirmStage(
                    uiState = uiState,
                    onProceedClick = { viewModel.findRoute(context, apiKey) },
                    onBackClick = { viewModel.goBackToSearch() }
                )
                OrderStage.ROUTE_CONFIRM -> RouteConfirmStage(
                    uiState = uiState,
                    onCreateOrderClick = onCreateOrderClick,
                )
                OrderStage.FINDING_DRIVER -> FindingDriverStage(
                    onCancelClick = { viewModel.cancelFindingDriver() }
                )
            }
        }
    }
}