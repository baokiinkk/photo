package com.amb.photo.ui.activities.imagepicker.components

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.amb.photo.R
import com.amb.photo.ui.theme.AppStyle
import com.basesource.base.utils.clickableWithAlphaEffect

@Composable
fun SelectedBar(selected: List<Uri>, onRemove: (Uri) -> Unit, onClearAll: () -> Unit) {
    if(selected.isEmpty()) return
    Column(
        Modifier
            .fillMaxWidth()
            .background(Color.White)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Select 1-10 photos",
                style = AppStyle.body1().medium().gray800(),
            )
            Text(
                text = "(${selected.size})",
                style = AppStyle.body1().semibold().primary500(),
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp)
            )
            IconButton(onClick = onClearAll) {
                Image(
                    painterResource(R.drawable.ic_delete_image),
                    modifier = Modifier.size(32.dp),
                    contentDescription = "Clear All"
                )
            }
        }
        Row(
            Modifier
                .padding(bottom = 20.dp, start = 16.dp, end = 16.dp)
                .horizontalScroll(rememberScrollState())
        ) {
            selected.forEach { uri ->
                Box(Modifier.padding(end = 8.dp)) {
                    AsyncImage(
                        model = uri,
                        contentScale = ContentScale.Crop,
                        contentDescription = null, modifier = Modifier
                            .padding(top = 6.dp, end = 6.dp)
                            .size(68.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                    Image(
                        painterResource(R.drawable.ic_close_image),
                        modifier = Modifier
                            .size(20.dp)
                            .align(Alignment.TopEnd)
                            .clickableWithAlphaEffect {
                                onRemove.invoke(uri)
                            },
                        contentDescription = "Clear All"
                    )
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










