package com.example.jeksoed.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jeksoed.ui.theme.JekSoedTheme

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        enabled = isEnabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
        )
    ) {
        Text(text, fontSize = 16.sp)
    }
}

@Preview(name = "Default Button")
@Composable
private fun PrimaryButtonPreview() {
    JekSoedTheme {
        PrimaryButton(text = "Login", onClick = {})
    }
}

@Preview(name = "Custom Color Button")
@Composable
private fun PrimaryButtonCustomColorPreview() {
    JekSoedTheme {
        PrimaryButton(
            text = "Register",
            onClick = {}
        )
    }
}