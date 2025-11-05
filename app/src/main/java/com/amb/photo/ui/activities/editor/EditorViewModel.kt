package com.amb.photo.ui.activities.editor

import com.amb.photo.R
import com.amb.photo.ui.activities.collage.components.CollageTool
import com.amb.photo.ui.activities.collage.components.ToolItem
import com.basesource.base.viewmodel.BaseViewModel
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class EditorViewModel: BaseViewModel() {

    val items = listOf(
            ToolItem(CollageTool.SQUARE, R.string.square, R.drawable.ic_square),
        ToolItem(CollageTool.CROP, R.string.crop, R.drawable.ic_crop),
        ToolItem(CollageTool.ADJUST, R.string.adjust, R.drawable.ic_adjust),
        ToolItem(CollageTool.FILTER, R.string.filter, R.drawable.ic_filter),
        ToolItem(CollageTool.BLUR, R.string.blur, R.drawable.ic_blur),
        ToolItem(CollageTool.BACKGROUND, R.string.background, R.drawable.ic_sticker_tool),
        ToolItem(CollageTool.ADD_PHOTO, R.string.add_photo_tool, R.drawable.ic_photo_tool)
    )
}