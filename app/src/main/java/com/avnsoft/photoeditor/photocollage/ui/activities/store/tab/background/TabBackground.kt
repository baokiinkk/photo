package com.avnsoft.photoeditor.photocollage.ui.activities.store.tab.background

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.avnsoft.photoeditor.photocollage.R
import com.avnsoft.photoeditor.photocollage.data.model.pattern.PatternModel
import com.avnsoft.photoeditor.photocollage.ui.theme.AppStyle
import com.basesource.base.ui.image.LoadImage
import com.basesource.base.utils.ImageWidget
import com.basesource.base.utils.clickableWithAlphaEffect


@Composable
fun TabBackground(
    patterns: List<PatternModel>,
    onBannerClickable: (PatternModel) -> Unit,
    onUseClick: (PatternModel) -> Unit
) {

    LazyColumn(

        verticalArrangement = Arrangement.spacedBy(24.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        itemsIndexed(
            items = patterns,
            key = { index, item ->
                item.eventId
            }
        ) { index, item ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                LoadImage(
                    model = item.bannerUrl,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .height(140.dp)
                        .clickableWithAlphaEffect {
                            onBannerClickable.invoke(item)
                        },
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            text = item.tabName,
                            style = AppStyle.title2().bold().Color_101828()
                        )
                        Text(
                            text = "${item.total} Backgrounds",
                            style = AppStyle.caption1().medium().gray500()
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    if (item.isUsed) {
                        Box(
                            modifier = Modifier
                                .height(32.dp)
                                .background(
                                    color = Color(0xFFEEECFE),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickableWithAlphaEffect {
                                    onUseClick.invoke(item)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                modifier = Modifier
                                    .padding(vertical = 4.dp, horizontal = 20.dp),
                                text = stringResource(R.string.use),
                                style = AppStyle.buttonMedium().semibold().primary500()
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .height(32.dp)
                                .background(
                                    brush = Brush.linearGradient(
                                        listOf(
                                            Color(0xFFF7ACEF),
                                            Color(0xFF6425F3),
                                        )
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickableWithAlphaEffect {
                                    onUseClick.invoke(item)
                                },
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(vertical = 6.dp, horizontal = 8.dp)
                            ) {
                                ImageWidget(
                                    resId = R.drawable.ic_store_star,
                                    modifier = Modifier
                                        .size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = stringResource(R.string.use),
                                    style = AppStyle.buttonMedium().semibold().white()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}