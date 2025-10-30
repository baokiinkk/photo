package com.amb.photo.ui.activities.discover

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.amb.photo.R
import com.amb.photo.ui.theme.AppStyle
import com.basesource.base.utils.clickableWithAlphaEffect

@Composable
fun DiscoverShortcuts(
    onRemoveObject: () -> Unit = {},
    onAIEnhance: () -> Unit = {},
    onRemoveBG: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ShortcutButton(
            modifier = Modifier.weight(1f),
            text = "Remove Object",
            onClick = onRemoveObject,
            icon = R.drawable.ic_remove_object
        )
        ShortcutButton(
            modifier = Modifier.weight(1f),
            text = "AI Enhance",
            onClick = onAIEnhance,
            icon = R.drawable.ic_enhance

        )
        ShortcutButton(
            modifier = Modifier.weight(1f),
            text = "Remove BG",
            onClick = onRemoveBG,
            icon = R.drawable.ic_remove_bg
        )
    }
}

@Composable
fun ShortcutButton(
    modifier: Modifier = Modifier,
    icon: Int,
    text: String, onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .height(80.dp)
            .background(Color.White, shape = RoundedCornerShape(12.dp))
            .clickableWithAlphaEffect { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp, bottom = 8.dp)
                .size(24.dp),
            painter = painterResource(icon),
            contentDescription = ""
        )
        Text(text = text, style = AppStyle.caption1().semibold().gray800())
    }
}

@Preview(showBackground = true)
@Composable
fun DiscoverShortcutsPreview() {
    Surface { DiscoverShortcuts() }
}
