package com.amb.photo.ui.activities.imagepicker.components

import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.amb.photo.ui.activities.imagepicker.GalleryImage
import com.amb.photo.ui.theme.AppStyle
import com.basesource.base.utils.clickableWithAlphaEffect

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GalleryGrid(
    images: List<GalleryImage>,
    selected: List<Uri>,
    onImageClick: (GalleryImage) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = modifier.heightIn(min = 320.dp)
    ) {
        items(images) { img ->
            Box(modifier = Modifier
                .padding(4.dp)
                .aspectRatio(1f)
                .clip(RoundedCornerShape(14.dp))
                .clickableWithAlphaEffect() { onImageClick(img) }
            ) {
                AsyncImage(
                    model = img.uri,
                    contentDescription = img.displayName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                val idx = selected.indexOf(img.uri)
                if(idx >= 0) {
                    Box(
                        Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                            .size(24.dp)
                            .background(Color(0xFF9747FF), CircleShape), contentAlignment = Alignment.Center
                    ) {
                        Text((idx+1).toString(), style = AppStyle.body2().bold().white())
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GalleryGridPreview() {
    Surface {
        GalleryGrid(
            images = List(9) {
                GalleryImage(Uri.parse("file:///android_asset/pic$it.jpg"), bucketId = "123", displayName = null, dateAdded = 0)
            },
            selected = listOf(Uri.parse("file:///android_asset/pic2.jpg")),
            onImageClick = {})
    }
}
