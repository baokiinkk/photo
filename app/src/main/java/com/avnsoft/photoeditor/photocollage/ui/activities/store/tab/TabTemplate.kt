package com.avnsoft.photoeditor.photocollage.ui.activities.store.tab

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.avnsoft.photoeditor.photocollage.R
import com.avnsoft.photoeditor.photocollage.data.model.template.TemplateCategoryModel
import com.avnsoft.photoeditor.photocollage.data.model.template.TemplateModel
import com.avnsoft.photoeditor.photocollage.ui.theme.AppColor
import com.avnsoft.photoeditor.photocollage.ui.theme.AppStyle
import com.basesource.base.ui.image.LoadImage
import com.basesource.base.utils.ImageWidget
import com.basesource.base.utils.Shimmer
import com.basesource.base.utils.clickableWithAlphaEffect

@Composable
fun TabTemplates(
    templates: List<TemplateCategoryModel>,
    onItemClicked: (TemplateModel) -> Unit
) {
    var tabIndex by remember { mutableIntStateOf(0) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF8FAFC))
            .padding(top = 16.dp, start = 16.dp, end = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            templates.takeIf { it.isNotEmpty() }?.forEachIndexed { index, item ->
                val isSelected = index == tabIndex
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            if (isSelected) {
                                AppColor.Primary500
                            } else {
                                AppColor.Gray100
                            }
                        )
                        .clickableWithAlphaEffect {
                            tabIndex = index
                        }
                ) {
                    Text(
                        modifier = Modifier
                            .padding(vertical = 4.dp, horizontal = 12.dp),
                        text = item.category.orEmpty(),
                        style = if (isSelected) {
                            AppStyle.caption1().semibold().white()
                        } else {
                            AppStyle.caption1().semibold().gray500()
                        }
                    )
                }
            }
        }

        templates.getOrNull(tabIndex)?.templates.takeIf { it?.isNotEmpty() == true }?.let {
            LazyVerticalGrid(
                modifier = Modifier
                    .padding(top = 16.dp, bottom = 16.dp),
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                itemsIndexed(items = it) { index, item ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(1f)
                            .height(194.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .clickableWithAlphaEffect {
                                onItemClicked.invoke(item)
                            }
                    ) {
                        LoadImage(
                            model = item.previewUrl,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        if (item.isPro == true && item.isUsed == false) {
                            ImageWidget(
                                resId = R.drawable.button_pro,
                                modifier = Modifier
                                    .width(45.dp)
                                    .height(20.dp)
                                    .align(androidx.compose.ui.Alignment.TopEnd)
                                    .padding(top = 8.dp, end = 8.dp)
                            )
                        }
                    }
                }
            }
        } ?: run {
            //preview loading
            LazyVerticalGrid(
                modifier = Modifier
                    .padding(top = 16.dp, bottom = 16.dp),
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                itemsIndexed(items = listOf("", "", "", "", "", "", "")) { index, item ->
                    Shimmer(
                        modifier = Modifier
                            .fillMaxWidth(1f)
                            .height(194.dp)
                            .clip(RoundedCornerShape(20.dp))
                    )
                }
            }
        }
    }
}

