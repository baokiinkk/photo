package com.avnsoft.photoeditor.photocollage.ui.activities.store.tab

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.avnsoft.photoeditor.photocollage.data.model.template.TemplateModel
import com.basesource.base.ui.image.LoadImage

@Composable
fun TabTemplates(
    selectedTab: TemplateModel?,
    templates: List<TemplateModel>,
    onBannerClickable: (TemplateModel) -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
        ) {
            templates.forEach {
                Text("${it.tabName}")
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