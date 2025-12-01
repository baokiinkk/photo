package com.avnsoft.photoeditor.photocollage.ui.activities.collage

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.avnsoft.photoeditor.photocollage.data.model.collage.CollageState
import com.avnsoft.photoeditor.photocollage.data.model.collage.CollageTemplate
import com.avnsoft.photoeditor.photocollage.data.repository.CollageTemplateRepository
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.BackgroundSelection
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.FrameSelection
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.preview.ImageTransformState
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.sticker.lib.Sticker
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.text_sticker.TextStickerUIState
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.text_sticker.lib.FontAsset
import com.avnsoft.photoeditor.photocollage.ui.activities.freestyle.lib.FreeStyleStickerView
import com.basesource.base.result.Result
import com.tanishranjan.cropkit.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.annotation.KoinViewModel
import java.util.Stack

@KoinViewModel
class CollageViewModel(
    private val repository: CollageTemplateRepository
) : ViewModel() {

    private val undoStack = Stack<CollageState>()
    private val redoStack = Stack<CollageState>()
    private var initialState: CollageState? = null

    private val _templates = MutableStateFlow(emptyList<CollageTemplate>())
    val templates = _templates.asStateFlow()

    private val _state = MutableStateFlow(CollageState())
    val collageState = _state.asStateFlow()

    private val _canUndo = MutableStateFlow(false)
    val canUndo = _canUndo.asStateFlow()

    private val _canRedo = MutableStateFlow(false)
    val canRedo = _canRedo.asStateFlow()

    private val _unselectTrigger = MutableStateFlow(0)
    val unselectAllImagesTrigger = _unselectTrigger.asStateFlow()

    private val _showDiscardDialog = MutableStateFlow(false)
    val showDiscardDialog = _showDiscardDialog.asStateFlow()

    var stickerView: FreeStyleStickerView? = null
        set(v) {
            field = v
            v ?: return
            val stickers = extractStickers(v)
            val newState = _state.value.copy(stickerList = stickers)
            _state.value = newState
            if (initialState == null) initialState = newState.copy()
        }

    private var tempRatio: String? = null
    private var tempBackground: BackgroundSelection? = null
    private var tempFrame: FrameSelection? = null
    private var tempTransforms: Map<Int, ImageTransformState>? = null

    fun triggerUnselectAllImages() {
        _unselectTrigger.update { it + 1 }
    }

    private fun extractStickers(view: FreeStyleStickerView): List<Sticker> {
        return try {
            val field = view.javaClass.superclass.getDeclaredField("stickers")
            field.isAccessible = true
            @Suppress("UNCHECKED_CAST")
            field.get(view) as? List<Sticker> ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun confirmStickerChanges() {
        viewModelScope.launch {
            val stickers = stickerView?.let { extractStickers(it) } ?: emptyList()
            push(_state.value.copy(stickerList = stickers))
        }
    }

    fun load(count: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            initTextStickerConfig()

            when (val res = repository.getTemplates()) {
                is Result.Success -> {
                    val all = res.data
                    val filtered = all.filter { it.cells.size == count }
                    val list = filtered.ifEmpty { all }

                    _templates.value = list
                    val first = list.firstOrNull() ?: return@launch

                    updateState { it.copy(templateId = first) }

                    if (initialState == null) {
                        initialState = _state.value.copy()
                        undoStack.push(initialState!!.copy())
                        redoStack.clear()
                        _canUndo.value = false
                        _canRedo.value = false
                    }
                }
                else -> Unit
            }
        }
    }

    fun selectTemplate(t: CollageTemplate) = updateState { it.copy(templateId = t) }
    fun updateTopMargin(v: Float) = updateState { it.copy(topMargin = v) }
    fun updateColumnMargin(v: Float) = updateState { it.copy(columnMargin = v) }
    fun updateCornerRadius(v: Float) = updateState { it.copy(cornerRadius = v) }

    fun updateRatio(r: String?) {
        tempRatio = r
        updateState { it.copy(ratio = r) }
    }

    fun cancelRatioChanges() {
        val restored = undoStack.lastOrNull()?.ratio ?: initialState?.ratio
        tempRatio = null
        updateState { it.copy(ratio = restored) }
    }

    fun updateBackground(bg: BackgroundSelection) {
        tempBackground = bg
        updateState { it.copy(backgroundSelection = bg) }
    }

    fun cancelBackgroundChanges() {
        val restored = undoStack.lastOrNull()?.backgroundSelection ?: initialState?.backgroundSelection
        tempBackground = null
        updateState { it.copy(backgroundSelection = restored) }
    }

    fun updateFrame(frame: FrameSelection) {
        tempFrame = frame
        updateState { it.copy(frameSelection = frame) }
    }

    fun cancelFrameChanges() {
        val restored = undoStack.lastOrNull()?.frameSelection ?: initialState?.frameSelection
        tempFrame = null
        updateState { it.copy(frameSelection = restored) }
    }

    fun confirmChanges() {
        val s = _state.value.copy()
        push(s)
        tempRatio = null
    }

    private fun push(s: CollageState) {
        if (undoStack.lastOrNull() == s && undoStack.size > 1) return

        undoStack.push(s.copy())
        redoStack.clear()

        _state.value = s
        _canUndo.value = undoStack.size > 1
        _canRedo.value = false
    }

    fun undo() {
        if (undoStack.size < 2) return
        val current = undoStack.pop()
        redoStack.push(current.copy())

        val prev = undoStack.peek()
        _state.value = prev.copy()

        _canUndo.value = undoStack.size > 1
        _canRedo.value = true
    }

    fun redo() {
        if (redoStack.isEmpty()) return
        val next = redoStack.pop()
        undoStack.push(next.copy())

        _state.value = next.copy()

        _canUndo.value = undoStack.size > 1
        _canRedo.value = redoStack.isNotEmpty()
    }

    fun updateImageTransforms(m: Map<Int, ImageTransformState>) {
        tempTransforms = m
        updateState { it.copy(imageTransforms = m) }
    }

    fun confirmImageTransformChanges() {
        tempTransforms = null
    }

    fun showDiscardDialog() {
        _showDiscardDialog.value = true
    }

    fun hideDiscardDialog() {
        _showDiscardDialog.value = false
    }

    fun setImageUris(context: Context, uris: List<Uri>) {
        viewModelScope.launch(Dispatchers.IO) {
            val bitmaps = uris.mapIndexedNotNull { i, uri ->
                uriToBitmap(context, uri)?.let { i to it }
            }.toMap()

            updateState { it.copy(imageUris = uris, imageBitmaps = bitmaps) }
        }
    }

    fun addImageUri(context: Context, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val maps = _state.value.imageBitmaps.toMutableMap()
            uriToBitmap(context, uri)?.let { maps[maps.size] = it }

            updateState {
                it.copy(
                    imageUris = it.imageUris + uri,
                    imageBitmaps = maps
                )
            }
        }
    }

    fun removeImageUri(index: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val s = _state.value
            if (s.imageUris.size <= 1 || index !in s.imageUris.indices) return@launch

            val newUris = s.imageUris.toMutableList().apply { removeAt(index) }
            val newMaps = mutableMapOf<Int, Bitmap>()

            newUris.forEachIndexed { i, _ ->
                s.imageBitmaps[i]?.let { newMaps[i] = it }
            }

            updateState { it.copy(imageUris = newUris, imageBitmaps = newMaps) }
        }
    }

    fun rotateImage(context: Context, index: Int) =
        applyTransform(context, index) { c, cb -> c.rotateClockwise(cb) }

    fun flipImageHorizontal(context: Context, index: Int) =
        applyTransform(context, index) { c, cb -> c.flipHorizontally(cb) }

    fun flipImageVertical(context: Context, index: Int) =
        applyTransform(context, index) { c, cb -> c.flipVertically(cb) }

    private fun applyTransform(
        context: Context,
        index: Int,
        action: (CropController, (Bitmap) -> Unit) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val s = _state.value
            val bmp = s.imageBitmaps[index]
                ?: s.imageUris.getOrNull(index)?.let { uriToBitmap(context, it) }
                ?: return@launch

            val controller = CropController(
                bmp,
                cropOptions = CropDefaults.cropOptions(CropShape.FreeForm),
                cropColors = CropColors(
                    overlay = Color.Transparent,
                    overlayActive = Color.Transparent,
                    gridlines = Color.Transparent,
                    cropRectangle = Color.Transparent,
                    handle = Color.Transparent
                )
            )

            action(controller) { newBmp ->
                val maps = s.imageBitmaps.toMutableMap()
                maps[index] = newBmp
                updateState { it.copy(imageBitmaps = maps) }
            }
        }
    }

    private fun updateState(block: (CollageState) -> CollageState) {
        _state.update(block)
    }

    private suspend fun uriToBitmap(ctx: Context, uri: Uri): Bitmap? =
        withContext(Dispatchers.IO) {
            try {
                val s = uri.toString()
                if (s.startsWith("file://"))
                    BitmapFactory.decodeFile(s.removePrefix("file://"))
                else if (android.os.Build.VERSION.SDK_INT < 28)
                    android.provider.MediaStore.Images.Media.getBitmap(ctx.contentResolver, uri)
                else
                    ImageDecoder.decodeBitmap(ImageDecoder.createSource(ctx.contentResolver, uri))
            } catch (_: Exception) {
                null
            }
        }

    private fun initTextStickerConfig() {
        updateState {
            it.copy(textState = TextStickerUIState().copy(items = FontAsset.listFonts))
        }
    }
}