package com.avnsoft.photoeditor.photocollage.ui.activities.store.tab

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.avnsoft.photoeditor.photocollage.data.model.template.TemplateModel
import com.avnsoft.photoeditor.photocollage.ui.theme.AppColor
import com.avnsoft.photoeditor.photocollage.ui.theme.AppStyle
import com.basesource.base.ui.image.LoadImage

@Composable
fun TabTemplates(
    selectedTab: TemplateModel?,
    templates: List<TemplateModel>,
    onBannerClickable: (TemplateModel) -> Unit
) {
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
            templates.forEach { item ->
                val isSelected = item == selectedTab
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
                ) {
                    Text(
                        modifier = Modifier
                            .padding(vertical = 4.dp, horizontal = 12.dp),
                        text = item.tabName,
                        style = if (isSelected) {
                            AppStyle.caption1().semibold().white()
                        } else {
                            AppStyle.caption1().semibold().gray500()
                        }
                    )
                }
            }
        }


        selectedTab?.content?.let { contents ->
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
            ) {
                itemsIndexed(
                    items = contents,
                    key = { index, item ->
                        index
                    }
                ) { index, item ->
                    LoadImage(
                        model = item.urlThumb,
                        modifier = Modifier
                            .aspectRatio(1f)
                    )
                }
            }
        }
    }
}