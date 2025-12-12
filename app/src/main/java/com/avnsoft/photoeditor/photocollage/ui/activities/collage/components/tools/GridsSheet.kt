package com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.tools

import android.graphics.BitmapFactory
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.avnsoft.photoeditor.photocollage.R
import com.avnsoft.photoeditor.photocollage.data.model.collage.CellSpec
import com.avnsoft.photoeditor.photocollage.data.model.collage.CollageTemplate
import com.avnsoft.photoeditor.photocollage.ui.theme.AppColor.Companion.Gray500
import com.avnsoft.photoeditor.photocollage.ui.theme.AppColor.Companion.Gray900
import com.avnsoft.photoeditor.photocollage.ui.theme.AppStyle
import com.avnsoft.photoeditor.photocollage.ui.theme.Primary500
import com.avnsoft.photoeditor.photocollage.utils.FrameGridItem
import com.avnsoft.photoeditor.photocollage.utils.FrameGridLoader
import com.basesource.base.utils.clickableWithAlphaEffect
import java.io.IOException

enum class GridsTab {
    LAYOUT, MARGIN
}

@Composable
fun GridsSheet(
    templates: List<CollageTemplate> = emptyList(),
    selectedTemplate: CollageTemplate? = null,
    onTemplateSelect: (CollageTemplate) -> Unit,
    selectedType: GridsTab = GridsTab.LAYOUT,
    onClose: () -> Unit,
    onConfirm: (GridsTab) -> Unit, // Truyền tab hiện tại khi confirm
    // Margin values và callbacks
    topMargin: Float = 0f,
    onTopMarginChange: (Float) -> Unit = {},
    columnMargin: Float = 0f,
    onColumnMarginChange: (Float) -> Unit = {},
    cornerRadius: Float = 0f,
    onCornerRadiusChange: (Float) -> Unit = {},
    imageCount: Int = templates.firstOrNull()?.cells?.size ?: 1, // Số lượng ảnh để load frame phù hợp
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember(selectedType) { mutableStateOf(selectedType) }
    
    // Load frame grid items từ assets
    var frameGridItems by remember(imageCount, templates) {
        mutableStateOf<List<FrameGridItem>>(emptyList())
    }
    
    LaunchedEffect(imageCount, templates) {
        frameGridItems = FrameGridLoader.loadFrameGridItems(context, imageCount, templates)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
    ) {
        Row(
            modifier = Modifier
                .padding(vertical = 16.dp)
                .align(Alignment.CenterHorizontally),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.layout),
                style = AppStyle.body2().medium().let {
                    if (selectedTab == GridsTab.LAYOUT) it.white() else it.gray900()
                },
                modifier = Modifier
                    .background(
                        if (selectedTab == GridsTab.LAYOUT) Color(0xFF6425F3) else Color(0xFFF3F4F6), RoundedCornerShape(24.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 4.dp)
                    .clickableWithAlphaEffect {
                        selectedTab = GridsTab.LAYOUT
                    })

            Text(
                text = stringResource(R.string.margin),
                style = AppStyle.body2().medium().let {
                    if (selectedTab == GridsTab.MARGIN) it.white() else it.gray900()
                },
                modifier = Modifier
                    .background(
                        if (selectedTab == GridsTab.MARGIN) Color(0xFF6425F3) else Color(0xFFF3F4F6), RoundedCornerShape(24.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 4.dp)
                    .clickableWithAlphaEffect {
                        selectedTab = GridsTab.MARGIN
                    })
        }

        // Grid Layout Selection
        if (selectedTab == GridsTab.LAYOUT) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(6),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 100.dp)
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(frameGridItems) { frameItem ->
                    FrameGridItem(
                        frameItem = frameItem,
                        isSelected = frameItem.templateId == selectedTemplate?.id,
                        onClick = {
                            // Chọn template tương ứng với frame
                            frameItem.template?.let { template ->
                                onTemplateSelect(template)
                            }
                        }
                    )
                }
            }

        } else {
            // Margin tab
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                // Top Margin Slider
                MarginSlider(
                    icon = R.drawable.ic_margin_tool, value = topMargin, onValueChange = onTopMarginChange
                )

                // Column Margin Slider
                MarginSlider(
                    icon = R.drawable.ic_padding_tool, value = columnMargin, onValueChange = onColumnMarginChange
                )

                // Corner Radius Slider
                MarginSlider(
                    icon = R.drawable.ic_border_tool, value = cornerRadius, onValueChange = onCornerRadiusChange
                )
            }
        }
        // Bottom Action Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose) {
                Icon(
                    modifier = Modifier.size(28.dp), painter = painterResource(R.drawable.ic_close), contentDescription = "Close", tint = Gray500
                )
            }

            Text(
                text = stringResource(R.string.grids), style = AppStyle.title2().semibold().gray900()
            )

            IconButton(onClick = {
                onConfirm.invoke(selectedTab)
            }) {
                Icon(
                    modifier = Modifier.size(28.dp),
                    painter = painterResource(R.drawable.ic_confirm),
                    contentDescription = "Confirm",
                    tint = Gray900
                )
            }
        }
    }
}

@Composable
private fun FrameGridItem(
    frameItem: FrameGridItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    
    Box(
        modifier = Modifier
            .size(40.dp)
            .clickableWithAlphaEffect(onClick = onClick)
    ) {
        // Load frame image từ assets
        val painter = remember(frameItem.framePath) {
            try {
                val bitmap = context.assets.open(frameItem.framePath).use { inputStream ->
                    BitmapFactory.decodeStream(inputStream)
                }
                bitmap?.let { BitmapPainter(it.asImageBitmap()) }
            } catch (e: IOException) {
                e.printStackTrace()
                null
            }
        }
        
        painter?.let {
            Icon(
                painter = it,
                tint = if(isSelected) Primary500 else Gray900,
                contentDescription = "",
                modifier = Modifier.fillMaxSize()
            )
        } ?: run {
            // Fallback nếu không load được frame
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Gray.copy(alpha = 0.3f))
            )
        }
    }
}

@Composable
private fun MarginSlider(
    @DrawableRes icon: Int, value: Float, onValueChange: (Float) -> Unit, modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(icon), contentDescription = "", contentScale = ContentScale.Crop, modifier = Modifier.size(24.dp)
        )

        // Slider
        Slider(
            value = value, onValueChange = onValueChange, valueRange = 0f..1f, modifier = Modifier.weight(1f), colors = SliderDefaults.colors(
                thumbColor = Color(0xFF1F2937), activeTrackColor = Color(0xFF1F2937), inactiveTrackColor = Color(0xFFE5E7EB)
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun GridsSheetPreview() {
    val mockTemplates = listOf(
        CollageTemplate(
            "left-big-right-2", listOf(
                CellSpec(points = listOf(0f, 0f, 0.63f, 0f, 0.63f, 1f, 0f, 1f)),
                CellSpec(points = listOf(0.66f, 0f, 1f, 0f, 1f, 0.48f, 0.66f, 0.48f)),
                CellSpec(points = listOf(0.66f, 0.52f, 1f, 0.52f, 1f, 1f, 0.66f, 1f))
            )
        ), CollageTemplate(
            "2-horizontal", listOf(
                CellSpec(points = listOf(0f, 0f, 1f, 0f, 1f, 0.5f, 0f, 0.5f)), CellSpec(points = listOf(0f, 0.5f, 1f, 0.5f, 1f, 1f, 0f, 1f))
            )
        ), CollageTemplate(
            "3-horizontal", listOf(
                CellSpec(points = listOf(0f, 0f, 1f, 0f, 1f, 0.33f, 0f, 0.33f)),
                CellSpec(points = listOf(0f, 0.33f, 1f, 0.33f, 1f, 0.66f, 0f, 0.66f)),
                CellSpec(points = listOf(0f, 0.66f, 1f, 0.66f, 1f, 1f, 0f, 1f))
            )
        ), CollageTemplate(
            "1-full", listOf(
                CellSpec(points = listOf(0f, 0f, 1f, 0f, 1f, 1f, 0f, 1f))
            )
        ), CollageTemplate(
            "2-vertical", listOf(
                CellSpec(points = listOf(0f, 0f, 0.5f, 0f, 0.5f, 1f, 0f, 1f)), CellSpec(points = listOf(0.5f, 0f, 1f, 0f, 1f, 1f, 0.5f, 1f))
            )
        ), CollageTemplate(
            "4-equal", listOf(
                CellSpec(points = listOf(0f, 0f, 0.5f, 0f, 0.5f, 0.5f, 0f, 0.5f)),
                CellSpec(points = listOf(0.5f, 0f, 1f, 0f, 1f, 0.5f, 0.5f, 0.5f)),
                CellSpec(points = listOf(0f, 0.5f, 0.5f, 0.5f, 0.5f, 1f, 0f, 1f)),
                CellSpec(points = listOf(0.5f, 0.5f, 1f, 0.5f, 1f, 1f, 0.5f, 1f))
            )
        ), CollageTemplate(
            "3-vertical", listOf(
                CellSpec(points = listOf(0f, 0f, 0.33f, 0f, 0.33f, 1f, 0f, 1f)),
                CellSpec(points = listOf(0.33f, 0f, 0.66f, 0f, 0.66f, 1f, 0.33f, 1f)),
                CellSpec(points = listOf(0.66f, 0f, 1f, 0f, 1f, 1f, 0.66f, 1f))
            )
        )
    )
    GridsSheet(templates = mockTemplates, selectedTemplate = mockTemplates.first(), onTemplateSelect = {}, onClose = {}, onConfirm = { _ -> })
}

@Preview(showBackground = true)
@Composable
private fun GridsSheetMarginPreview() {
    GridsSheet(selectedType = GridsTab.MARGIN, onTemplateSelect = {}, onClose = {}, onConfirm = { _ -> })
}
