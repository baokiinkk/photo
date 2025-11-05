package com.amb.photo.ui.activities.collage.components

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.amb.photo.R
import com.amb.photo.ui.theme.AppStyle
import com.basesource.base.utils.clickableWithAlphaEffect

enum class CollageTool {
    GRIDS, RATIO, BACKGROUND, FRAME, TEXT, STICKER, ADD_PHOTO
}

data class ToolItem(
    val tool: CollageTool, @StringRes val label: Int, @DrawableRes val icon: Int
)

@Composable
fun CollageBottomTools(
    onToolClick: (CollageTool) -> Unit, modifier: Modifier = Modifier
) {
    val tools = listOf(
        ToolItem(CollageTool.GRIDS, R.string.grids, R.drawable.ic_grid),
        ToolItem(CollageTool.RATIO, R.string.ratio_tool, R.drawable.ic_ratio),
        ToolItem(CollageTool.BACKGROUND, R.string.background_tool, R.drawable.ic_background_tool),
        ToolItem(CollageTool.FRAME, R.string.frame_tool, R.drawable.ic_frame_tool),
        ToolItem(CollageTool.TEXT, R.string.text_tool, R.drawable.ic_text_tool),
        ToolItem(CollageTool.STICKER, R.string.sticker_tool, R.drawable.ic_sticker_tool),
        ToolItem(CollageTool.ADD_PHOTO, R.string.add_photo_tool, R.drawable.ic_photo_tool)
    )
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White)
            .horizontalScroll(rememberScrollState())
    ) {
        tools.forEach { item ->
            ToolItem(
                item = item,
                onClick = { onToolClick(item.tool) }
            )
        }
    }
}

@Composable
private fun ToolItem(
    item: ToolItem, onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(65.dp)
            .padding(vertical = 12.dp)
            .clickableWithAlphaEffect(onClick = onClick)
    ) {
        Image(
            painterResource(item.icon),
            contentDescription = "",
            modifier = Modifier.size(24.dp)
        )
        Text(
            modifier = Modifier.padding(top = 2.dp),
            text = stringResource(item.label),
            style = AppStyle.caption2().medium().gray800()
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CollageBottomToolsPreview() {
    CollageBottomTools(
        onToolClick = {})
}

