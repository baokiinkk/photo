package com.amb.photo.ui.activities.editor

import android.graphics.Bitmap
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.ContentScale
import androidx.core.graphics.scale
import androidx.lifecycle.viewModelScope
import com.amb.photo.R
import com.amb.photo.ui.activities.collage.components.CollageTool
import com.amb.photo.ui.activities.collage.components.ToolItem
import com.basesource.base.viewmodel.BaseViewModel
import com.tanishranjan.cropkit.util.MathUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class EditorViewModel : BaseViewModel() {

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


    val uiState = MutableStateFlow(EditorUIState(items = items))

    var pathBitmapResult: String? = null

    fun setPathBitmap(pathBitmap: String?, bitmap: Bitmap?) {
        pathBitmapResult = pathBitmap
        uiState.update {
            it.copy(
                bitmap = bitmap,
                originBitmap = bitmap
            )
        }
    }

    fun scaleBitmapToBox(canvasSize: Size) {
        val bitmap = uiState.value.originBitmap ?: return
        viewModelScope.launch(Dispatchers.Default) {
            val imageWidth = bitmap.width.toFloat()
            val imageHeight = bitmap.height.toFloat()

            val scaledSize = MathUtils.calculateScaledSize(
                srcWidth = imageWidth,
                srcHeight = imageHeight,
                dstWidth = canvasSize.width,
                dstHeight = canvasSize.height,
                contentScale = ContentScale.Fit
            )

            val newBitmap = bitmap.scale(scaledSize.width.toInt(), scaledSize.height.toInt())
            uiState.update { it.copy(bitmap = newBitmap) }
        }
    }

    fun toggleOriginal() {
        uiState.update {
            it.copy(
                items = it.items.toMutableList().apply {
                    val isToggle = !this[0].isToggle
                    this[0] = this[0].copy(
                        isToggle = isToggle,
                        label = if (isToggle) R.string.original else R.string.square,
                        icon = if (isToggle) R.drawable.ic_original_tool else R.drawable.ic_square
                    )
                },
                isOriginal = !it.isOriginal
            )
        }

    }

}

data class EditorUIState(
    val items: List<ToolItem>,
    val bitmap: Bitmap? = null,
    val originBitmap: Bitmap? = null,
    val isOriginal: Boolean = false
)