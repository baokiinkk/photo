package com.amb.photo.ui.activities.collage.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import com.basesource.base.utils.clickableWithAlphaEffect
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview

enum class CollageTool {
    GRIDS,
    RATIO,
    BACKGROUND,
    FRAME,
    TEXT,
    STICKER
}

data class ToolItem(
    val tool: CollageTool,
    val label: String,
    val icon: String
)

@Composable
fun CollageBottomTools(
    selectedTool: CollageTool?,
    onToolClick: (CollageTool) -> Unit,
    modifier: Modifier = Modifier
) {
    val tools = listOf(
        ToolItem(CollageTool.GRIDS, "Grids", "⊞"),
        ToolItem(CollageTool.RATIO, "Ratio", "□"),
        ToolItem(CollageTool.BACKGROUND, "Background", "▱"),
        ToolItem(CollageTool.FRAME, "Frame", "◈"),
        ToolItem(CollageTool.TEXT, "Text", "Aa"),
        ToolItem(CollageTool.STICKER, "Sticker", "😊")
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(vertical = 16.dp)
    ) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(tools) { item ->
                ToolItem(
                    item = item,
                    isSelected = selectedTool == item.tool,
                    onClick = { onToolClick(item.tool) }
                )
            }
        }
    }
}

@Composable
private fun ToolItem(
    item: ToolItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickableWithAlphaEffect(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    if (isSelected) Color(0xFFEEE1FF) else Color(0xFFF5F5F7),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = item.icon,
                fontSize = 20.sp,
                color = if (isSelected) Color(0xFF9747FF) else Color(0xFF666666)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = item.label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = if (isSelected) Color(0xFF9747FF) else Color(0xFF666666)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CollageBottomToolsPreview() {
    CollageBottomTools(
        selectedTool = CollageTool.GRIDS,
        onToolClick = {}
    )
}

