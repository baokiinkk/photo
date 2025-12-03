package com.avnsoft.photoeditor.photocollage.ui.activities.store.editor

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.ContentScale
import androidx.core.graphics.scale
import androidx.core.net.toUri
import androidx.lifecycle.viewModelScope
import com.avnsoft.photoeditor.photocollage.R
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.tools.BackgroundSelection
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.tools.CollageTool
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.tools.ToolItem
import com.avnsoft.photoeditor.photocollage.utils.FileUtil
import com.basesource.base.viewmodel.BaseViewModel
import com.tanishranjan.cropkit.util.MathUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import java.io.File
import java.io.FileOutputStream
import java.util.Stack

@KoinViewModel
class EditorStoreViewModel(
    private val context: Application,
) : BaseViewModel() {

    private val _navigation = Channel<CollageTool>()

    val navigation = _navigation.receiveAsFlow()

    val items = listOf(
        ToolItem(
            tool = CollageTool.TEMPLATE,
            label = R.string.template,
            icon = R.drawable.ic_tstore_editor_emplate,
        ),
        ToolItem(
            CollageTool.TEXT,
            R.string.text_tool,
            R.drawable.ic_text_tool
        ),
        ToolItem(CollageTool.FILTER, R.string.filter, R.drawable.ic_filter),
        ToolItem(CollageTool.STICKER, R.string.sticker_tool, R.drawable.ic_sticker_tool),
        ToolItem(CollageTool.REPLACE, R.string.replace, R.drawable.ic_photo_tool)
    )

    val uiState = MutableStateFlow(EditorStoreUIState(items = items))

    var pathBitmapResult: String? = null

    var canvasSize: Size? = null

    fun setPathBitmap(
        pathBitmap: String?,
        bitmap: Bitmap?,
        tool: CollageTool?
    ) {
        viewModelScope.launch {
            pathBitmapResult = copyImageToAppStorage(context, pathBitmap?.toUri())
            uiState.update {
                it.copy(bitmap = bitmap)
            }
            tool?.let {
                onToolClick(tool)
            }
        }
    }

    var isFirst = true
    fun scaleBitmapToBox(canvasSize: Size) {
        val bitmap = uiState.value.bitmap ?: return
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

            if (isFirst) {
                pushFirstData(newBitmap)
                isFirst = false
            }
        }
    }

    fun pushFirstData(bitmap: Bitmap?) {
        bitmap?.let { newBitmap ->
            undoStack.push(
                StackData.EditorBitmap(
                    bitmap = newBitmap,
                    backgroundColor = null,
                    pathBitmapResult = pathBitmapResult
                )
            )
            redoStack.push(
                StackData.EditorBitmap(
                    bitmap = newBitmap,
                    backgroundColor = null,
                    pathBitmapResult = pathBitmapResult
                )
            )
        }
    }
    fun updateBitmap(
        pathBitmap: String?,
        bitmap: Bitmap?
    ) {
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
                it.copy(bitmap = newBitmap)
            }
            push(
                stackData = StackData.EditorBitmap(
                    bitmap = newBitmap,
                    backgroundColor = uiState.value.backgroundColor,
                    pathBitmapResult = pathBitmap,
                )
            )
        }
    }


    private val undoStack = Stack<StackData>()
    private val redoStack = Stack<StackData>()

    fun push(stackData: StackData) {
        undoStack.push(stackData)

        redoStack.clear()

        uiState.update {
            it.copy(
                canUndo = undoStack.size > 1,
                canRedo = false
            )
        }
    }

    fun undo() {
        if (undoStack.size < 2) {
            uiState.update {
                it.copy(
                    canUndo = false,
                    canRedo = redoStack.isNotEmpty()
                )
            }
            return
        }

        val current = undoStack.pop()
        redoStack.push(current)

        val previous = undoStack.peek()

        applyState(previous)

        uiState.update {
            it.copy(
                canUndo = undoStack.size > 1,
                canRedo = redoStack.isNotEmpty()
            )
        }
    }

    fun redo() {
        if (redoStack.isEmpty()) {
            uiState.update {
                it.copy(canRedo = false)
            }
            return
        }

        val state = redoStack.pop()
        undoStack.push(state)

        applyState(state)

        uiState.update {
            it.copy(
                canUndo = undoStack.size > 1,
                canRedo = redoStack.isNotEmpty()
            )
        }
    }

    private fun applyState(state: StackData) {
        when (state) {
            is StackData.EditorBitmap -> {
                uiState.update {
                    it.copy(
                        bitmap = state.bitmap,
                        backgroundColor = state.backgroundColor
                    )
                }
                pathBitmapResult = state.pathBitmapResult
            }

            is StackData.Background -> {
                uiState.update {
                    it.copy(
                        backgroundColor = state.backgroundColor,
                        bitmap = state.bitmap
                    )
                }
                pathBitmapResult = state.pathBitmapResult
            }

            StackData.NONE -> {}
        }
    }

    fun onToolClick(tool: CollageTool) {
        viewModelScope.launch {
            _navigation.send(tool)
        }
    }

    fun showDiscardDialog() {
        uiState.update {
            it.copy(
                showDiscardDialog = true
            )
        }
    }

    fun hideDiscardDialog() {
        uiState.update {
            it.copy(
                showDiscardDialog = false
            )
        }
    }
}

sealed class StackData {
    data class EditorBitmap(
        val bitmap: Bitmap,
        val backgroundColor: BackgroundSelection?,
        val pathBitmapResult: String?,
    ) : StackData()

    data class Background(
        val backgroundColor: BackgroundSelection?,
        val bitmap: Bitmap,
        val pathBitmapResult: String?,
    ) : StackData()

    data object NONE : StackData()
}

data class EditorStoreUIState(
    val items: List<ToolItem>,
    val bitmap: Bitmap? = null,
    val isOriginal: Boolean = false,
    val backgroundColor: BackgroundSelection? = null,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val showDiscardDialog: Boolean = false
)

suspend fun copyImageToAppStorage(context: Context, sourceUri: Uri?): String? {
    try {
        if (sourceUri == null) return null
        val inputStream =
            context.contentResolver.openInputStream(sourceUri) ?: return null
        val file = File(
            FileUtil.getCacheFolder(context),
            "theme_image_${System.currentTimeMillis()}.png"
        )
        val outputStream = FileOutputStream(file)

        inputStream.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }

        return file.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}