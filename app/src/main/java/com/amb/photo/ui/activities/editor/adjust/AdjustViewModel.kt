package com.amb.photo.ui.activities.editor.adjust

import android.graphics.Bitmap
import com.amb.photo.R
import com.amb.photo.ui.activities.collage.components.CollageTool
import com.amb.photo.ui.activities.collage.components.ToolItem
import com.basesource.base.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class AdjustViewModel : BaseViewModel() {

    val items = listOf(
        ToolItem(
            CollageTool.SQUARE_OR_ORIGINAL,
            R.string.square,
            R.drawable.ic_square,
            isToggle = false
        ),
        ToolItem(CollageTool.CROP, R.string.crop, R.drawable.ic_crop),
        ToolItem(CollageTool.ADJUST, R.string.adjust, R.drawable.ic_adjust),
        ToolItem(CollageTool.FILTER, R.string.filter, R.drawable.ic_filter),
        ToolItem(CollageTool.BLUR, R.string.blur, R.drawable.ic_blur),
        ToolItem(CollageTool.BACKGROUND, R.string.background, R.drawable.ic_sticker_tool),
        ToolItem(CollageTool.STICKER, R.string.sticker_tool, R.drawable.ic_sticker_tool),
        ToolItem(CollageTool.TEXT, R.string.text_tool, R.drawable.ic_text_tool),
        ToolItem(CollageTool.REMOVE, R.string.remove, R.drawable.ic_remove),
        ToolItem(CollageTool.ENHANCE, R.string.enhance, R.drawable.ic_ai_enhance),
        ToolItem(CollageTool.REMOVE_BG, R.string.remove_bg, R.drawable.ic_removebg),
        ToolItem(CollageTool.REMOVE_BG, R.string.draw, R.drawable.ic_draw),
    )

    val uiState = MutableStateFlow(AdjustUIState())

    fun initBitmap(bitmap: Bitmap?) {
        uiState.update {
            it.copy(
                bitmap = bitmap,
                originBitmap = bitmap
            )
        }
    }

}

data class AdjustUIState(
    val bitmap: Bitmap? = null,
    val originBitmap: Bitmap? = null,
)