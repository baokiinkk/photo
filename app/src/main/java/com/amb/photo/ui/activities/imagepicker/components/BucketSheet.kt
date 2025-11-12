package com.amb.photo.ui.activities.imagepicker.components

import androidx.compose.foundation.Image
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.amb.photo.ui.activities.imagepicker.GalleryBucket
import com.amb.photo.ui.theme.AppStyle
import com.basesource.base.utils.clickableWithAlphaEffect

@Composable
fun BucketSheet(
    buckets: List<GalleryBucket>,
    currentBucketId: String?,
    onSelect: (GalleryBucket) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier.padding(vertical = 8.dp)) {
        buckets.forEach { bucket ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(92.dp)
                    .clickableWithAlphaEffect { onSelect(bucket) }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (bucket.thumbnail != null) {
                    AsyncImage(
                        bucket.thumbnail,
                        contentDescription = bucket.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(68.dp)
                            .padding(end = 8.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                } else {
                    Image(
                        painterResource(android.R.drawable.ic_menu_gallery),
                        contentDescription = "placeholder",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(68.dp)
                            .padding(end = 8.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                }
                Text(bucket.name, style = AppStyle.title2().bold().gray800(), modifier = Modifier.weight(1f))
                Text(bucket.count.toString(), style = AppStyle.body2().medium().gray500())
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BucketSheetPreview() {
    Surface {
        BucketSheet(
            buckets = listOf(
                GalleryBucket("1", "Recent", null, 100),
                GalleryBucket("2", "Camera", null, 15),
                GalleryBucket("3", "Download", null, 45),
            ),
            currentBucketId = "2",
            onSelect = {}
        )
    }
}






