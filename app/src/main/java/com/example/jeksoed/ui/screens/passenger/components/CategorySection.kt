package com.example.jeksoed.ui.screens.passenger.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jeksoed.R
import com.example.jeksoed.ui.screens.passenger.Category
import com.example.jeksoed.ui.theme.JekSoedTheme

@Composable
fun CategoryGrid(onCategoryClick: (String) -> Unit) {
    val categories = listOf(
        Category("JekMotor", R.drawable.motor_icon, "START7K"),
        Category("JekMobil", R.drawable.car_icon, "START13K"),
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
    val pointerWidth = 10.dp

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() } // onClick diteruskan ke sini
    ) {
        Box {
            Image(
                painter = painterResource(id = category.iconResId),
                contentDescription = category.name,
                modifier = Modifier.size(48.dp)
            )
            if (category.tag != null) {
                Text(
                    text = category.tag,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .layout { measurable, constraints ->
                            val placeable = measurable.measure(constraints)
                            val pointerWidthPx = pointerWidth.roundToPx()
                            layout(0, 0) {
                                placeable.placeRelative(
                                    x = -pointerWidthPx,
                                    y = -placeable.height
                                )
                            }
                        }
                        .background(
                            color = Color(0xFF272343),
                            shape = TagShape(pointerWidth = pointerWidth)
                        )
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = category.name, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

// Class TagShape dan Preview tetap sama...
class TagShape(
    private val cornerRadius: Dp = 8.dp,
    private val pointerWidth: Dp = 10.dp,
    private val pointerHeight: Dp = 8.dp
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path().apply {
            val cornerRadiusPx = with(density) { cornerRadius.toPx() }
            val pointerWidthPx = with(density) { pointerWidth.toPx() }
            val pointerHeightPx = with(density) { pointerHeight.toPx() }
            moveTo(0f, 0f)
            lineTo(size.width - cornerRadiusPx, 0f)
            arcTo(
                rect = androidx.compose.ui.geometry.Rect(size.width - cornerRadiusPx * 2, 0f, size.width, cornerRadiusPx * 2),
                startAngleDegrees = 270f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
            lineTo(size.width, size.height - cornerRadiusPx)
            arcTo(
                rect = androidx.compose.ui.geometry.Rect(size.width - cornerRadiusPx * 2, size.height - cornerRadiusPx * 2, size.width, size.height),
                startAngleDegrees = 0f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
            lineTo(pointerWidthPx, size.height)
            lineTo(pointerWidthPx, size.height + pointerHeightPx)
            lineTo(0f, size.height)
            close()
        }
        return Outline.Generic(path)
    }
}

@Preview(showBackground = true, name = "Item dengan Tag")
@Composable
fun CategoryItemWithTagPreview() {
    JekSoedTheme {
        Box(modifier = Modifier.padding(20.dp)) {
            CategoryItem(
                category = Category("JekMotor", R.drawable.motor_icon, "START7K"),
                onClick = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "Item tanpa Tag")
@Composable
fun CategoryItemWithoutTagPreview() {
    JekSoedTheme {
        Box(modifier = Modifier.padding(20.dp)) {
            CategoryItem(
                category = Category(name = "Lainnya", iconResId = R.drawable.more_icon),
                onClick = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "Grid Kategori Lengkap")
@Composable
fun CategoryGridPreview() {
    JekSoedTheme {
        Box(modifier = Modifier.padding(20.dp)) {
            CategoryGrid(onCategoryClick = {})
        }
    }
}