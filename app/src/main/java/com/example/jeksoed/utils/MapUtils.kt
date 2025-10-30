package com.example.jeksoed.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Fungsi bantuan untuk mengubah vektor drawable menjadi BitmapDescriptor untuk Google Maps Marker.
 */
fun bitmapDescriptorFromVector(
    context: Context,
    @DrawableRes vectorResId: Int
): BitmapDescriptor? {
    return ContextCompat.getDrawable(context, vectorResId)?.run {
        setBounds(0, 0, intrinsicWidth, intrinsicHeight)
        val bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
        draw(Canvas(bitmap))
        BitmapDescriptorFactory.fromBitmap(bitmap)
    }
}

/**
 * --- FUNGSI BARU ---
 * Fungsi untuk mengubah Composable menjadi BitmapDescriptor.
 */
suspend fun bitmapDescriptorFromComposable(
    context: Context,
    content: @Composable () -> Unit
): BitmapDescriptor? = suspendCoroutine { continuation ->
    val composeView = ComposeView(context).apply {
        setContent {
            content()
        }
    }

    val viewGroup = object : ViewGroup(context) {
        override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {}
    }
    viewGroup.addView(composeView)

    viewGroup.post {
        val widthSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        val heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        viewGroup.measure(widthSpec, heightSpec)
        viewGroup.layout(0, 0, viewGroup.measuredWidth, viewGroup.measuredHeight)

        val bitmap = Bitmap.createBitmap(viewGroup.width, viewGroup.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        viewGroup.draw(canvas)

        continuation.resume(BitmapDescriptorFactory.fromBitmap(bitmap))
    }
}