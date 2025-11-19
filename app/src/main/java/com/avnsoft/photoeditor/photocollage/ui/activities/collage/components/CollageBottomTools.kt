package com.avnsoft.photoeditor.photocollage.ui.activities.collage.components

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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.avnsoft.photoeditor.photocollage.R
import com.avnsoft.photoeditor.photocollage.ui.theme.AppColor
import com.avnsoft.photoeditor.photocollage.ui.theme.AppStyle
import com.basesource.base.utils.clickableWithAlphaEffect

enum class CollageTool {
    GRIDS, RATIO, BACKGROUND, FRAME, TEXT, STICKER, ADD_PHOTO,
    SQUARE_OR_ORIGINAL, CROP, ADJUST, FILTER, BLUR, REMOVE, ENHANCE, REMOVE_BG, DRAW, NONE,
    BRIGHTNESS, CONTRAST, SATURATION, WARMTH, FADE, HIGHLIGHT, SHADOW, HUE, VIGNETTE, SHARPEN, GRAIN,
    TEMPLATE,REPLACE
}

data class ToolItem(
    val tool: CollageTool,
    @StringRes val label: Int,
    @DrawableRes val icon: Int,
    var isToggle: Boolean = false,
    val index: Int = 0,
)

val toolsCollage = listOf(
    ToolItem(CollageTool.GRIDS, R.string.grids, R.drawable.ic_grid),
    ToolItem(CollageTool.RATIO, R.string.ratio_tool, R.drawable.ic_ratio),
    ToolItem(CollageTool.BACKGROUND, R.string.background_tool, R.drawable.ic_background_tool),
    ToolItem(CollageTool.FRAME, R.string.frame_tool, R.drawable.ic_frame_tool),
    ToolItem(CollageTool.TEXT, R.string.text_tool, R.drawable.ic_text_tool),
    ToolItem(CollageTool.STICKER, R.string.sticker_tool, R.drawable.ic_sticker_tool),
    ToolItem(CollageTool.ADD_PHOTO, R.string.add_photo_tool, R.drawable.ic_photo_tool)
)

@Composable
fun FeatureBottomTools(
    modifier: Modifier = Modifier,
    tools: List<ToolItem> = listOf(),
    onToolClick: (CollageTool) -> Unit = {},
    tool: CollageTool = CollageTool.NONE,
    onItemSelect: (ToolItem) -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White)
            .horizontalScroll(rememberScrollState())
    ) {
        tools.forEach { item ->
            ToolItem(
                item = item,
                isSelect = tool == item.tool,
                onClick = {
                    onToolClick(item.tool)
                    onItemSelect.invoke(item)
                }
            )
        }
    }
}

@Composable
private fun ToolItem(
    item: ToolItem,
    isSelect: Boolean,
    onClick: () -> Unit
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
            modifier = Modifier.size(24.dp),
            colorFilter = if (isSelect) ColorFilter.tint(AppColor.Primary500) else null,
        )
        Text(
            modifier = Modifier.padding(top = 2.dp),
            text = stringResource(item.label),
            style = if (isSelect) AppStyle.caption2().medium().primary500() else AppStyle.caption2()
                .medium().gray800()
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CollageBottomToolsPreview() {
    FeatureBottomTools(
        tools = toolsCollage,
        onToolClick = {})
}

