package com.example.jeksoed.ui.screens.passenger.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.jeksoed.R
import kotlinx.coroutines.delay

@Composable
fun BannerSlider(modifier: Modifier = Modifier) {
    val promoBanners = listOf(
        R.drawable.motor_banner,
        R.drawable.car_banner,
        R.drawable.cleaning_banner
    )
    val pagerState = rememberPagerState(pageCount = { promoBanners.size })

    LaunchedEffect(pagerState) {
        while (true) {
            delay(5000)
            val nextPage = (pagerState.currentPage + 1) % pagerState.pageCount
            pagerState.animateScrollToPage(nextPage)
        }
    }

    HorizontalPager(
        state = pagerState,
        modifier = modifier
    ) { page ->
        Image(
            painter = painterResource(id = promoBanners[page]),
            contentDescription = "Promo Banner ${page + 1}",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
        )
    }
}