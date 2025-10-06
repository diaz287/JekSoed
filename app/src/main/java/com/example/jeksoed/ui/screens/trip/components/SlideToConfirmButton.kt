// main/java/com/example/jeksoed/ui/screens/trip/components/SlideToConfirmButton.kt

package com.example.jeksoed.ui.screens.trip.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.ExperimentalWearMaterialApi
import androidx.wear.compose.material.FractionalThreshold
import androidx.wear.compose.material.rememberSwipeableState
import androidx.wear.compose.material.swipeable
import com.example.jeksoed.ui.theme.JekSoedTheme
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn( ExperimentalWearMaterialApi::class)
@Composable
fun SlideToConfirmButton(
    text: String,
    onConfirmed: () -> Unit,
    modifier: Modifier = Modifier
) {
    val swipeableState = rememberSwipeableState(initialValue = 0)
    val scope = rememberCoroutineScope()
    var isConfirmed by remember { mutableStateOf(false) }
    val density = LocalDensity.current
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(50))
            .background(Color(0xFFFFC107))
            .animateContentSize(),
        contentAlignment = Alignment.CenterStart
    ) {
        val maxWidth = constraints.maxWidth.toFloat()
        val thumbSize = 56.dp
        val thumbSizePx = with(density) { thumbSize.toPx() }
        val anchors = mapOf(0f to 0, maxWidth - thumbSizePx to 1)

        // Text di tengah
        Text(
            text = text,
            color = Color.Black,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .align(Alignment.Center)
                .alpha(1f - (swipeableState.offset.value / (maxWidth - thumbSizePx)))
        )

        // Thumb yang bisa digeser
        Box(
            modifier = Modifier
                .offset { IntOffset(swipeableState.offset.value.roundToInt(), 0) }
                .swipeable(
                    state = swipeableState,
                    anchors = anchors,
                    thresholds = { _, _ -> FractionalThreshold(0.8f) },
                    orientation = Orientation.Horizontal
                )
                .size(thumbSize)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.1f))
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Geser untuk konfirmasi",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }

        LaunchedEffect(swipeableState.currentValue) {
            if (swipeableState.currentValue == 1 && !isConfirmed) {
                isConfirmed = true
                onConfirmed()
                // Animasi kembali ke awal setelah konfirmasi
                scope.launch {
                    swipeableState.animateTo(0, anim = tween(300))
                    isConfirmed = false
                }
            }
        }
    }
}

@Preview
@Composable
fun SlideToConfirmButtonPreview() {
    JekSoedTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            SlideToConfirmButton(
                text = "Geser untuk memulai",
                onConfirmed = {}
            )
        }
    }
}