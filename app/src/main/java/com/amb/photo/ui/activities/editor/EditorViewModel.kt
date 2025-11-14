package com.amb.photo.ui.activities.editor

import android.graphics.Bitmap
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.core.graphics.scale
import androidx.lifecycle.viewModelScope
import com.amb.photo.R
import com.amb.photo.ui.activities.collage.components.CollageTool
import com.amb.photo.ui.activities.collage.components.ToolItem
import com.amb.photo.ui.theme.AppColor
import com.basesource.base.viewmodel.BaseViewModel
import com.tanishranjan.cropkit.util.MathUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import java.util.Stack

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

    var canvasSize: Size? = null

    fun setPathBitmap(pathBitmap: String?, bitmap: Bitmap?) {
        pathBitmapResult = pathBitmap
        uiState.update {
            it.copy(
                bitmap = bitmap,
                originBitmap = bitmap
            )
        }
    }

    fun updateBitmap(tool: CollageTool = CollageTool.NONE, pathBitmap: String?, bitmap: Bitmap?) {
        if (bitmap == null || canvasSize == null) return
        pathBitmapResult = pathBitmap
        viewModelScope.launch(Dispatchers.Default) {
            val imageWidth = bitmap.width.toFloat()
            val imageHeight = bitmap.height.toFloat()

            val scaledSize = MathUtils.calculateScaledSize(
                srcWidth = imageWidth,
                srcHeight = imageHeight,
                dstWidth = canvasSize!!.width,
                dstHeight = canvasSize!!.height,
                contentScale = ContentScale.Fit
            )

            val newBitmap = bitmap.scale(scaledSize.width.toInt(), scaledSize.height.toInt())
            uiState.update {
                it.copy(
                    bitmap = newBitmap,
                    originBitmap = bitmap
                )
            }
            push(
                stackData = StackData.EditorBitmap(
                    bitmap = newBitmap,
                    backgroundColor = uiState.value.backgroundColor,
                    pathBitmapResult = pathBitmap,
                    originBitmap = bitmap
                )
            )
        }
    }

    var isFirstInit: Boolean = true

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

            if (isFirstInit) {
                pushFirstData(newBitmap)
                isFirstInit = false
            }
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

    fun updateBackgroundColor(color: Color) {
        uiState.update {
            it.copy(
                backgroundColor = color,
            )
        }
    }


    private val undoStack = Stack<StackData>()
    private val redoStack = Stack<StackData>()

    var firstBitmap: Bitmap? = null
    fun pushFirstData(bitmap: Bitmap?) {
        bitmap?.let { newBitmap ->
            firstBitmap = newBitmap
            undoStack.push(
                StackData.EditorBitmap(
                    bitmap = newBitmap,
                    backgroundColor = AppColor.backgroundAppColor,
                    originBitmap = uiState.value.originBitmap,
                    pathBitmapResult = pathBitmapResult
                )
            )
            redoStack.push(
                StackData.EditorBitmap(
                    bitmap = newBitmap,
                    backgroundColor = AppColor.backgroundAppColor,
                    originBitmap = uiState.value.originBitmap,
                    pathBitmapResult = pathBitmapResult
                )
            )
        }
    }

    fun push(stackData: StackData) {
        undoStack.push(stackData)
        uiState.update {
            it.copy(
                canUndo = true,
                canRedo = false
            )
        }
        redoStack.clear()
        firstBitmap?.let {
            redoStack.push(
                StackData.EditorBitmap(
                    bitmap = it,
                    AppColor.backgroundAppColor,
                    pathBitmapResult = pathBitmapResult,
                    uiState.value.originBitmap
                )
            )
        }
    }

    fun undo() {
        if (undoStack.size < 2) {
            uiState.update {
                it.copy(
                    canUndo = false,
                    canRedo = false,
                    backgroundColor = Color(0xFFF2F4F8)
                )
            }
        } else {
            val stack = undoStack.pop()
            val previous = undoStack.peek()
            when (previous) {
                is StackData.EditorBitmap -> {
                    uiState.update {
                        it.copy(
                            bitmap = previous.bitmap,
                            canUndo = undoStack.size >= 2,
                            canRedo = undoStack.size >= 2,
                            backgroundColor = previous.backgroundColor,
                            originBitmap = previous.originBitmap
                        )
                    }
                    pathBitmapResult = previous.pathBitmapResult
                }

                is StackData.Background -> {
                    uiState.update {
                        it.copy(
                            backgroundColor = previous.backgroundColor,
                            canUndo = undoStack.size >= 2,
                            canRedo = undoStack.size >= 2
                        )
                    }
                }

                else -> {

                }
            }
            redoStack.push(stack)
        }
    }

    fun redo() {
        if (redoStack.size < 2) {
            uiState.update {
                it.copy(
                    canRedo = false
                )
            }
        } else {
            val stack = redoStack.pop()
            when (stack) {
                is StackData.EditorBitmap -> {
                    uiState.update {
                        it.copy(
                            bitmap = stack.bitmap,
                            canUndo = true,
                            canRedo = redoStack.size >= 2,
                            originBitmap = stack.originBitmap
                        )
                    }
                    pathBitmapResult = stack.pathBitmapResult
                }

                is StackData.Background -> {
                    uiState.update {
                        it.copy(
                            backgroundColor = stack.backgroundColor,
                            canUndo = true,
                            canRedo = redoStack.size >= 2,
                        )
                    }
                }

                else -> {

                }
            }

            undoStack.push(stack)
        }
    }
}

sealed class StackData {
    data class EditorBitmap(
        val bitmap: Bitmap,
        val backgroundColor: Color,
        val pathBitmapResult: String?,
        val originBitmap: Bitmap?
    ) : StackData()

    data class Background(val backgroundColor: Color) : StackData()
    data object NONE : StackData()
}


data class EditorUIState(
    val items: List<ToolItem>,
    val bitmap: Bitmap? = null,
    val originBitmap: Bitmap? = null,
    val isOriginal: Boolean = false,
    val backgroundColor: Color = AppColor.backgroundAppColor,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false
)