package com.amb.photo.ui.activities.imagepicker.components

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.amb.photo.ui.theme.AppStyle
import com.basesource.base.utils.clickableWithAlphaEffect

@Composable
fun SelectedBar(selected: List<Uri>, onRemove: (Uri) -> Unit, onClearAll: () -> Unit) {
    if(selected.isEmpty()) return
    Box(
        Modifier
            .fillMaxWidth()
            .background(Color.White)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Select 1-10 photos (${selected.size})",
                style = AppStyle.body1().semibold().primary(),
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onClearAll) {
                Icon(painterResource(android.R.drawable.ic_menu_delete), contentDescription = "Clear All")
            }
        }
        Row(
            Modifier
                .align(Alignment.BottomStart)
                .padding(top = 34.dp, bottom = 2.dp)
        ) {
            selected.forEach { uri ->
                Box(Modifier.padding(end = 8.dp)) {
                    AsyncImage(
                        model = uri,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp).clip(RoundedCornerShape(10.dp))
                    )
                    Box(
                        Modifier
                            .offset(x = 30.dp, y = (-6).dp)
                            .size(20.dp)
                            .background(Color(0xFFD72E2C), CircleShape)
                            .clickableWithAlphaEffect { onRemove(uri) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("×", style = AppStyle.body2().bold().white())
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SelectedBarPreview() {
    Surface {
        SelectedBar(
            selected = listOf(Uri.parse("file:///android_asset/picA.jpg"), Uri.parse("file:///android_asset/picB.jpg")),
            onRemove = {},
            onClearAll = {}
        )
    }
}

