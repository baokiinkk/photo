package com.avnsoft.photoeditor.photocollage.ui.activities.store.editor

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.ContentScale
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.scale
import androidx.core.net.toUri
import androidx.lifecycle.viewModelScope
import com.avnsoft.photoeditor.photocollage.BaseApplication
import com.avnsoft.photoeditor.photocollage.R
import com.avnsoft.photoeditor.photocollage.data.model.template.TemplateContentModel
import com.avnsoft.photoeditor.photocollage.data.model.template.TemplateModel
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.preview.ImageTransformState
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.tools.BackgroundSelection
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.tools.CollageTool
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.tools.ToolItem
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.sticker.lib.Sticker
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.text_sticker.lib.AddTextProperties
import com.avnsoft.photoeditor.photocollage.ui.activities.freestyle.lib.FreeStyleSticker
import com.avnsoft.photoeditor.photocollage.ui.activities.freestyle.lib.Photo
import com.avnsoft.photoeditor.photocollage.utils.FileUtil
import com.avnsoft.photoeditor.photocollage.utils.FileUtil.toFile
import com.avnsoft.photoeditor.photocollage.utils.FileUtil.urlToDrawable
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

    var isEditTextSticker: Boolean = false

    fun setPathBitmap(
        pathBitmap: String?,
        bitmap: Bitmap?,
        tool: CollageTool?,
        template: TemplateModel? = null,
        selectedImages: Map<Int, Uri> = emptyMap()
    ) {
        viewModelScope.launch {
            pathBitmapResult = copyImageToAppStorage(context, pathBitmap?.toUri())
            uiState.update {
                it.copy(
                    bitmap = bitmap,
                    template = template,
                    selectedImages = selectedImages
                )
            }
            tool?.let {
                onToolClick(tool)
            }
        }
    }

    fun setTemplateData(
        template: TemplateModel?,
        selectedImages: Map<Int, Uri> = emptyMap()
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val icons = template?.layer?.mapIndexed { index, model ->
                model.toFreeStyleSticker(index)
            }
            uiState.update {
                it.copy(
                    template = template,
                    selectedImages = selectedImages,
                    bitmap = null,
                    imageTransforms = emptyMap(),
                    icons = icons
                )
            }
        }
    }

    fun updateImageTransforms(transforms: Map<Int, ImageTransformState>) {
        uiState.update {
            it.copy(imageTransforms = transforms)
        }
    }

    fun updateLayerTransforms(transforms: Map<Int, ImageTransformState>) {
        uiState.update {
            it.copy(layerTransforms = transforms)
        }
    }

    fun deleteLayer(index: Int) {
        viewModelScope.launch {
            val currentTemplate = uiState.value.template ?: return@launch
            val updatedLayers = currentTemplate.layer?.toMutableList()?.apply {
                if (index in indices) {
                    removeAt(index)
                }
            }

            uiState.update {
                it.copy(
                    template = currentTemplate.copy(layer = updatedLayers),
                    layerTransforms = it.layerTransforms.filterKeys { it != index }
                        .mapKeys { if (it.key > index) it.key - 1 else it.key }
                )
            }
        }
    }

    fun duplicateLayer(index: Int) {
        viewModelScope.launch {
            val currentTemplate = uiState.value.template ?: return@launch
            val updatedLayers = currentTemplate.layer?.toMutableList()?.apply {
                if (index in indices) {
                    val layerToDuplicate = this[index]
                    add(layerToDuplicate) // Add duplicate at the end
                }
            }

            uiState.update {
                it.copy(
                    template = currentTemplate.copy(layer = updatedLayers)
                )
            }
        }
    }

    fun flipLayer(index: Int) {
        viewModelScope.launch {
            val currentFlip = uiState.value.layerFlip
            val newFlip = currentFlip.toMutableMap()
            val currentFlipValue = newFlip[index] ?: 1f
            newFlip[index] = currentFlipValue * -1f // Toggle between 1f and -1f

            uiState.update {
                it.copy(layerFlip = newFlip)
            }
        }
    }

    fun updateLayerZoom(index: Int, scale: Float) {
        viewModelScope.launch {
            val currentTransforms = uiState.value.layerTransforms.toMutableMap()
            val currentTransform = currentTransforms[index] ?: ImageTransformState()
            currentTransforms[index] = currentTransform.copy(scale = scale)

            uiState.update {
                it.copy(layerTransforms = currentTransforms)
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

    fun showStickerTool() {
        uiState.update {
            it.copy(
                isShowStickerTool = true
            )
        }
    }

    fun cancelSticker() {
        uiState.update {
            it.copy(
                isShowStickerTool = false
            )
        }
    }

    fun applySticker(sticker: Sticker?) {
        uiState.update {
            it.copy(
                isShowStickerTool = false
            )
        }
    }

    fun showTextSticker() {
        uiState.update {
            it.copy(
                isShowTextStickerTool = true
            )
        }
    }

    fun cancelTextSticker() {
        uiState.update {
            it.copy(
                isShowTextStickerTool = false,
                isVisibleTextField = false
            )
        }
        isEditTextSticker = false
    }

    fun applyTextSticker() {
        uiState.update {
            it.copy(
                isShowTextStickerTool = false,
                isVisibleTextField = false
            )
        }
        isEditTextSticker = false
    }

    fun showEditTextSticker() {
        uiState.update {
            it.copy(
                isVisibleTextField = true,
                isShowTextStickerTool = true
            )
        }
    }

    fun hideEditTextSticker() {
        uiState.update {
            it.copy(
                isVisibleTextField = false,
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
    val showDiscardDialog: Boolean = false,
    val template: TemplateModel? = null,
    val selectedImages: Map<Int, Uri> = emptyMap(),
    val imageTransforms: Map<Int, ImageTransformState> = emptyMap(),
    val layerTransforms: Map<Int, ImageTransformState> = emptyMap(),
    val layerFlip: Map<Int, Float> = emptyMap(), // Map<layerIndex, flipScale> where 1f = normal, -1f = flipped,
    val icons: List<FreeStyleSticker>? = null,
    val isShowStickerTool: Boolean = false,
    val isShowTextStickerTool: Boolean = false,
    val isVisibleTextField: Boolean = false,
    val editTextProperties: AddTextProperties = AddTextProperties.getAddTextProperties(),
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

suspend fun TemplateContentModel.toFreeStyleSticker(
    index: Int,
): FreeStyleSticker {
    val model = this
    val drawable = model.urlThumb?.urlToDrawable(BaseApplication.getInstanceApp())
    val file = drawable?.toBitmap()?.toFile(BaseApplication.getInstanceApp())
    val uri = file?.toUri()
    return FreeStyleSticker(
        id = index,
        photo = Photo(uri, 0),
        drawable = drawable,
        x = model.x ?: 0f,
        y = model.y ?: 0f,
        widthRatio = model.width ?: 0f,
        heightRatio = model.height ?: 0f,
        rotate = model.rotate?.toFloat() ?: 0f
    )
}