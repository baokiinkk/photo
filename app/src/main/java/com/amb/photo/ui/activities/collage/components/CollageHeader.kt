package com.amb.photo.ui.activities.collage.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.amb.photo.R
import com.amb.photo.ui.theme.AppStyle
import com.amb.photo.ui.theme.Gray300
import com.amb.photo.ui.theme.Gray900
import com.amb.photo.ui.theme.Primary500
import com.basesource.base.utils.clickableWithAlphaEffect

@Composable
fun FeaturePhotoHeader(
    onBack: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onSave: () -> Unit,
    canUndo: Boolean = false,
    canRedo: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White)
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                painter = painterResource(R.drawable.ic_arrow_left),
                contentDescription = "Back",
                tint = Color.Black
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onUndo, enabled = canUndo) {
                Icon(
                    painter = painterResource(R.drawable.ic_undo),
                    contentDescription = "Undo",
                    tint = if (canUndo) Gray900 else Gray300
                )
            }
            IconButton(onClick = onRedo, enabled = canRedo) {
                Icon(
                    painter = painterResource(R.drawable.ic_redo),
                    contentDescription = "Redo",
                    tint = if (canRedo) Gray900 else Gray300
                )
            }
        }

        Text(
            modifier = Modifier
                .background(Primary500, RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .clickableWithAlphaEffect {
                    onSave.invoke()
                },
            text = "Save",
            textAlign = TextAlign.Center,
            style = AppStyle.button().semibold().white()
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CollageHeaderPreview() {
    FeaturePhotoHeader(
        onBack = {},
        onUndo = {},
        onRedo = {},
        onSave = {},
        canUndo = true,
        canRedo = false
    )
}

