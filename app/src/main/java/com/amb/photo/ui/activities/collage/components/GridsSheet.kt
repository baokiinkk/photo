package com.amb.photo.ui.activities.collage.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.amb.photo.R
import com.amb.photo.data.model.collage.CellSpec
import com.amb.photo.data.model.collage.CollageTemplate
import com.amb.photo.ui.theme.AppStyle
import com.basesource.base.utils.clickableWithAlphaEffect

enum class GridsTab {
    LAYOUT,
    MARGIN
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
    modifier: Modifier = Modifier
) {
    var selectedTab by remember(selectedType) { mutableStateOf(selectedType) }

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
                text = "Layout",
                style = AppStyle.body2().medium().let {
                    if (selectedTab == GridsTab.LAYOUT) it.white() else it.gray900()
                },
                modifier = Modifier
                    .background(if (selectedTab == GridsTab.LAYOUT) Color(0xFF9747FF) else Color(0xFFF3F4F6), RoundedCornerShape(24.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
                    .clickableWithAlphaEffect {
                        selectedTab = GridsTab.LAYOUT
                    }
            )

            Text(
                text = "Margin",
                style = AppStyle.body2().medium().let {
                    if (selectedTab == GridsTab.MARGIN) it.white() else it.gray900()
                },
                modifier = Modifier
                    .background(if (selectedTab == GridsTab.MARGIN) Color(0xFF9747FF) else Color(0xFFF3F4F6), RoundedCornerShape(24.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
                    .clickableWithAlphaEffect {
                        selectedTab = GridsTab.MARGIN
                    }
            )
        }

        // Grid Layout Selection
        if (selectedTab == GridsTab.LAYOUT) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(6),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(templates) { template ->
                    GridItem(
                        template = template,
                        isSelected = template.id == selectedTemplate?.id,
                        onClick = { onTemplateSelect(template) }
                    )
                }
            }
            // Bottom Action Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.Black
                    )
                }

                Text(
                    text = "Grids",
                    style = AppStyle.title2().medium().gray900()
                )

                IconButton(onClick = { onConfirm(selectedTab) }) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Confirm",
                        tint = Color(0xFF9747FF)
                    )
                }
            }

        } else {
            // Margin tab
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Top Margin Slider
                MarginSlider(
                    icon = R.drawable.ic_margin_tool,
                    value = topMargin,
                    onValueChange = onTopMarginChange
                )

                // Column Margin Slider
                MarginSlider(
                    icon = R.drawable.ic_padding_tool,
                    value = columnMargin,
                    onValueChange = onColumnMarginChange
                )

                // Corner Radius Slider
                MarginSlider(
                    icon = R.drawable.ic_border_tool,
                    value = cornerRadius,
                    onValueChange = onCornerRadiusChange
                )
            }

            // Bottom Action Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.Black
                    )
                }

                Text(
                    text = "Grids",
                    style = AppStyle.title2().medium().gray900()
                )

                IconButton(onClick = { onConfirm(selectedTab) }) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Confirm",
                        tint = Color(0xFF9747FF)
                    )
                }
            }
        }
    }
}

@Composable
private fun GridItem(
    template: CollageTemplate,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clickableWithAlphaEffect(onClick = onClick)
    ) {
        CollagePreview(
            images = template.cells.map {
                "$isSelected".toUri()
            },
            template = template,
            gap = 2.2.dp,
            corner = 4.dp,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun MarginSlider(
    @DrawableRes icon: Int,
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 32.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(icon),
            contentDescription = "",
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(24.dp)
        )

        // Slider
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 0f..1f,
            modifier = Modifier.weight(1f),
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFF1F2937),
                activeTrackColor = Color(0xFF1F2937),
                inactiveTrackColor = Color(0xFFE5E7EB)
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
                CellSpec(points = listOf(0f,0f, 0.63f,0f, 0.63f,1f, 0f,1f)),
                CellSpec(points = listOf(0.66f,0f, 1f,0f, 1f,0.48f, 0.66f,0.48f)),
                CellSpec(points = listOf(0.66f,0.52f, 1f,0.52f, 1f,1f, 0.66f,1f))
            )
        ),
        CollageTemplate(
            "2-horizontal", listOf(
                CellSpec(points = listOf(0f,0f, 1f,0f, 1f,0.5f, 0f,0.5f)),
                CellSpec(points = listOf(0f,0.5f, 1f,0.5f, 1f,1f, 0f,1f))
            )
        ),
        CollageTemplate(
            "3-horizontal", listOf(
                CellSpec(points = listOf(0f,0f, 1f,0f, 1f,0.33f, 0f,0.33f)),
                CellSpec(points = listOf(0f,0.33f, 1f,0.33f, 1f,0.66f, 0f,0.66f)),
                CellSpec(points = listOf(0f,0.66f, 1f,0.66f, 1f,1f, 0f,1f))
            )
        ),
        CollageTemplate("1-full", listOf(
            CellSpec(points = listOf(0f,0f, 1f,0f, 1f,1f, 0f,1f))
        )),
        CollageTemplate(
            "2-vertical", listOf(
                CellSpec(points = listOf(0f,0f, 0.5f,0f, 0.5f,1f, 0f,1f)),
                CellSpec(points = listOf(0.5f,0f, 1f,0f, 1f,1f, 0.5f,1f))
            )
        ),
        CollageTemplate(
            "4-equal", listOf(
                CellSpec(points = listOf(0f,0f, 0.5f,0f, 0.5f,0.5f, 0f,0.5f)),
                CellSpec(points = listOf(0.5f,0f, 1f,0f, 1f,0.5f, 0.5f,0.5f)),
                CellSpec(points = listOf(0f,0.5f, 0.5f,0.5f, 0.5f,1f, 0f,1f)),
                CellSpec(points = listOf(0.5f,0.5f, 1f,0.5f, 1f,1f, 0.5f,1f))
            )
        ),
        CollageTemplate(
            "3-vertical", listOf(
                CellSpec(points = listOf(0f,0f, 0.33f,0f, 0.33f,1f, 0f,1f)),
                CellSpec(points = listOf(0.33f,0f, 0.66f,0f, 0.66f,1f, 0.33f,1f)),
                CellSpec(points = listOf(0.66f,0f, 1f,0f, 1f,1f, 0.66f,1f))
            )
        )
    )
    GridsSheet(
        templates = mockTemplates,
        selectedTemplate = mockTemplates.first(),
        onTemplateSelect = {},
        onClose = {},
        onConfirm = { _ -> }
    )
}

@Preview(showBackground = true)
@Composable
private fun GridsSheetMarginPreview() {
    GridsSheet(
        selectedType = GridsTab.MARGIN,
        onTemplateSelect = {},
        onClose = {},
        onConfirm = { _ -> }
    )
}
