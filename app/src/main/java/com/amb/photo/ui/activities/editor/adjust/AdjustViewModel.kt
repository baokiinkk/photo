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
        ToolItem(CollageTool.BRIGHTNESS, R.string.brightness, R.drawable.ic_brightness),
        ToolItem(CollageTool.CONTRAST, R.string.contrast, R.drawable.ic_contrast),
        ToolItem(CollageTool.SATURATION, R.string.saturation, R.drawable.ic_saturation),
        ToolItem(CollageTool.WARMTH, R.string.warmth, R.drawable.ic_warmth),
        ToolItem(CollageTool.FADE, R.string.fade, R.drawable.ic_fade),
        ToolItem(CollageTool.HIGHLIGHT, R.string.highlight, R.drawable.ic_highlight),
        ToolItem(CollageTool.SHADOW, R.string.shadow, R.drawable.ic_shadow),
        ToolItem(CollageTool.HUE, R.string.hue, R.drawable.ic_hue),
        ToolItem(CollageTool.VIGNETTE, R.string.vignette, R.drawable.ic_vignette),
        ToolItem(CollageTool.SHARPEN, R.string.sharpen, R.drawable.ic_sharpen),
        ToolItem(CollageTool.GRAIN, R.string.grain, R.drawable.ic_grain),
    )

    val uiState = MutableStateFlow(AdjustUIState(items = items))

    fun initBitmap(bitmap: Bitmap?) {
        uiState.update {
            it.copy(
                bitmap = bitmap,
                originBitmap = bitmap
            )
        }
    }

    fun itemSelected(tool: CollageTool){
        uiState.update {
            it.copy(
                tool = tool
            )
        }
    }
}

data class AdjustUIState(
    val bitmap: Bitmap? = null,
    val originBitmap: Bitmap? = null,
    val items: List<ToolItem> = emptyList(),
    val tool: CollageTool = CollageTool.BRIGHTNESS
)