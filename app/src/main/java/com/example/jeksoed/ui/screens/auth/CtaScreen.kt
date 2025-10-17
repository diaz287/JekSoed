package com.example.jeksoed.ui.screens.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withLink
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.jeksoed.R
import com.example.jeksoed.navigation.Screen
import com.example.jeksoed.ui.components.PrimaryButton
import com.example.jeksoed.ui.theme.JekSoedTheme
import kotlinx.coroutines.delay

// Data untuk setiap halaman di Pager
data class CtaPage(
    val title: String,
    val description: String,
    val subDescription: String
)

@Composable
fun CtaScreen(navController: NavController) {
    val pages = listOf(
        CtaPage("Selamat datang di JEKSOED!", "Ojek andalan anak Unsoed!", "Siap anterin kamu ke sekitar Unsoed kapanpun."),
        CtaPage("Mau kemana hari ini?", "Mau berpergian, tapi ragu keamanan?", "Dengan JEKSOED dijamin aman! " +
                "Ayo cobain."),
        CtaPage("Cari freelance?", "Ga cuma jadi penumpang", "kalian, mahasiswa, bisa banget gabung jadi driver.")
    )
    val pagerState = rememberPagerState(pageCount = { pages.size })

    LaunchedEffect(Unit) {
        while(true) {
            delay(5000L)
            val nextPage = (pagerState.currentPage + 1) % pagerState.pageCount
            pagerState.animateScrollToPage(nextPage)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ... (Bagian Logo, Ilustrasi, Pager, dan Tombol tidak berubah)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.jeksoed_logo),
                contentDescription = "Logo Jeksoed",
                modifier = Modifier.size(48.dp)
            )
            Image(
                painter = painterResource(id = R.drawable.jeksoed_name),
                contentDescription = "Nama Jeksoed",
                modifier = Modifier.size(72.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Image(
            painter = painterResource(id = R.drawable.home_bg),
            contentDescription = "Ilustrasi Driver",
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth()
        ) { pageIndex ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Text(
                    text = pages[pageIndex].title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = buildAnnotatedString {
                        append(pages[pageIndex].description)
                        append("\n")
                        append(pages[pageIndex].subDescription)
                    },
                    textAlign = TextAlign.Center,
                    color = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            repeat(pages.size) { index ->
                val color = if (pagerState.currentPage == index) Color(0xFF272343) else Color.LightGray
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(color)
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        PrimaryButton(
            text = "Masuk dulu, yuk!",
            onClick = { navController.navigate(Screen.Login.route) },
            containerColor = Color(0xFFFFC107),
            contentColor = Color.Black
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(
            onClick = { navController.navigate(Screen.RoleSelection.route) },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            border = BorderStroke(1.dp, Color(0xFFFFC107))
        ) {
            Text("Belum ada akun? Gas bikin!", color = Color.Black)
        }

        Spacer(modifier = Modifier.height(16.dp))

        val annotatedText = buildAnnotatedString {
            append("Masuk atau daftar artinya kamu udah oke dan setuju sama ")

            withLink(
                LinkAnnotation.Clickable(
                    tag = "TNC",
                    linkInteractionListener = {
                        navController.navigate(Screen.Tnc.route)
                    }
                )
            ) {
                withStyle(
                    style = SpanStyle(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                ) {
                    append("Syarat & Ketentuan")
                }
            }

            append(" Privasi kita.")
        }

        Text(
            text = annotatedText,
            style = MaterialTheme.typography.bodySmall.copy(
                textAlign = TextAlign.Center,
                color = Color.Gray
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun CtaScreenPreview() {
    JekSoedTheme {
        CtaScreen(navController = rememberNavController())
    }
}