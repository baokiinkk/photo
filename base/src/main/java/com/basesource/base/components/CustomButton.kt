package com.basesource.base.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun CustomButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: List<Color> = listOf(
        Color(0xFF94F5EC),
        Color(0xFF82A2FC),
        Color(0xFFB17BFE)
    ),
    disabledColor: Color = Color(0xFFB9BCBF),
    height: Dp = 48.dp,
    cornerRadius: Dp = 16.dp,
    textStyle: TextStyle = LocalTextStyle.current,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .background(
                brush = if (enabled) {
                    Brush.linearGradient(colors)
                } else {
                    Brush.linearGradient(listOf(disabledColor, disabledColor))
                },
                shape = RoundedCornerShape(cornerRadius)
            ),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            contentColor = Color.White,
            disabledContentColor = Color.White
        ),
        shape = RoundedCornerShape(cornerRadius)
    ) {
        Text(
            text = text,
            style = textStyle
        )
    }
}

@Preview(showBackground = true, name = "CustomButton - Default")
@Composable
fun CustomButtonPreview() {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CustomButton(
            text = "Save",
            onClick = { }
        )

        CustomButton(
            text = "Cancel",
            onClick = { },
            enabled = false
        )
    }
}

@Preview(showBackground = true, name = "CustomButton - Custom")
@Composable
fun CustomButtonCustomPreview() {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CustomButton(
            text = "Custom Button",
            onClick = { },
            colors = listOf(
                Color(0xFF4CAF50),
                Color(0xFF8BC34A),
                Color(0xFF4CAF50)
            ),
            height = 56.dp,
            cornerRadius = 16.dp
        )

        CustomButton(
            text = "Small Button",
            onClick = { },
            height = 40.dp,
            cornerRadius = 8.dp
        )
    }
}
