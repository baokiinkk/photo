package com.amb.photo.ui.activities.collage.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
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
    templates: List<CollageTemplate>,
    selectedTemplate: CollageTemplate?,
    onTemplateSelect: (CollageTemplate) -> Unit,
    onClose: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(GridsTab.LAYOUT) }

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
                text = "Layout",
                style = AppStyle.body2().medium().let {
                    if (selectedTab == GridsTab.MARGIN) it.white() else it.gray900()
                },
                modifier = Modifier
                    .background(if (selectedTab == GridsTab.MARGIN) Color(0xFF9747FF) else Color(0xFFF3F4F6), RoundedCornerShape(24.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
                    .clickableWithAlphaEffect {
                        selectedTab == GridsTab.MARGIN
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
                        templates = templates,
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

                IconButton(onClick = onConfirm) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Confirm",
                        tint = Color(0xFF9747FF)
                    )
                }
            }

        } else {
            // Margin tab - TODO: implement margin controls
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Margin controls coming soon",
                    style = AppStyle.body2().medium().gray500()
                )
            }
        }
    }
}

@Composable
private fun GridItem(
    templates: List<CollageTemplate>,
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
            images = templates.map {
                "$isSelected".toUri()
            },
            template = template,
            gap = 2.2.dp,
            corner = 4.dp,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun GridsSheetPreview() {
    val mockTemplates = listOf(
        CollageTemplate(
            "left-big-right-2", listOf(
                CellSpec(0f, 0f, 0.63f, 1f),
                CellSpec(0.66f, 0f, 0.34f, 0.48f),
                CellSpec(0.66f, 0.52f, 0.34f, 0.48f)
            )
        ),
        CollageTemplate(
            "2-horizontal", listOf(
                CellSpec(0f, 0f, 1f, 0.5f),
                CellSpec(0f, 0.5f, 1f, 0.5f)
            )
        ),
        CollageTemplate(
            "3-horizontal", listOf(
                CellSpec(0f, 0f, 1f, 0.33f),
                CellSpec(0f, 0.33f, 1f, 0.33f),
                CellSpec(0f, 0.66f, 1f, 0.34f)
            )
        ),
        CollageTemplate("1-full", listOf(CellSpec(0f, 0f, 1f, 1f))),
        CollageTemplate(
            "2-vertical", listOf(
                CellSpec(0f, 0f, 0.5f, 1f),
                CellSpec(0.5f, 0f, 0.5f, 1f)
            )
        ),
        CollageTemplate(
            "4-equal", listOf(
                CellSpec(0f, 0f, 0.5f, 0.5f),
                CellSpec(0.5f, 0f, 0.5f, 0.5f),
                CellSpec(0f, 0.5f, 0.5f, 0.5f),
                CellSpec(0.5f, 0.5f, 0.5f, 0.5f)
            )
        ),
        CollageTemplate(
            "3-vertical", listOf(
                CellSpec(0f, 0f, 0.33f, 1f),
                CellSpec(0.33f, 0f, 0.33f, 1f),
                CellSpec(0.66f, 0f, 0.34f, 1f)
            )
        )
    )
    GridsSheet(
        templates = mockTemplates,
        selectedTemplate = mockTemplates.first(),
        onTemplateSelect = {},
        onClose = {},
        onConfirm = {}
    )
}
