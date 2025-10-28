package com.basesource.base.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun CustomCardView(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.medium,
    elevation: Dp = 1.dp,
    border: BorderStroke? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .shadow(
                elevation = elevation,
                shape = shape,
            ),
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = Color.White,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = elevation
        ),
        border = border
    ) {
        content()
    }
}

@Preview(showBackground = true, name = "CustomCardView - Default")
@Composable
fun CustomCardViewPreview() {
    CustomCardView(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Sample Card Content",
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = "This is a preview of CustomCardView",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
    }
}

@Preview(showBackground = true, name = "CustomCardView - Custom")
@Composable
fun CustomCardViewCustomPreview() {
    CustomCardView(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = 12.dp,
    ) {
        Text(
            text = "Custom Card",
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = "With custom elevation and padding",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
    }
}
