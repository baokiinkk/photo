package com.amb.photo.ui.activities.imagepicker.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
    Column(modifier
        .background(Color(0xFFF9F9FB))
        .padding(vertical = 8.dp)) {
        buckets.forEach { bucket ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickableWithAlphaEffect { onSelect(bucket) }
                    .padding(horizontal = 18.dp, vertical = 12.dp)
                    .background(if (currentBucketId == bucket.id) Color(0xFFEFE9FF) else Color.Transparent, RoundedCornerShape(10.dp)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (bucket.thumbnail != null) {
                    AsyncImage(
                        bucket.thumbnail,
                        contentDescription = bucket.name,
                        modifier = Modifier
                            .size(48.dp)
                            .padding(end = 8.dp)
                            .background(Color.LightGray, RoundedCornerShape(8.dp))
                    )
                } else {
                    Image(
                        painterResource(android.R.drawable.ic_menu_gallery),
                        contentDescription = "placeholder",
                        Modifier
                            .size(48.dp)
                            .padding(end = 8.dp)
                    )
                }
                Column(Modifier.weight(1f)) {
                    Text(bucket.name, style = AppStyle.body1().semibold().black())
                }
                Text(bucket.count.toString(), style = AppStyle.body2().regular().primary())
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
