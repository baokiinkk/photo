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
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.ImageTransformState
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
    val templates: StateFlow<List<CollageTemplate>> = _templates.asStateFlow()

    private val _collageState = MutableStateFlow(
        CollageState(topMargin = 0f, columnMargin = 0f, cornerRadius = 0f)
    )
    val collageState: StateFlow<CollageState> = _collageState.asStateFlow()

    private val _canUndo = MutableStateFlow(false)
    val canUndo = _canUndo.asStateFlow()

    private val _canRedo = MutableStateFlow(false)
    val canRedo = _canRedo.asStateFlow()

    private val _unselectAllImagesTrigger = MutableStateFlow(0)
    val unselectAllImagesTrigger = _unselectAllImagesTrigger.asStateFlow()

    var stickerView: FreeStyleStickerView? = null
        set(value) {
            field = value
            value ?: return
            val stickers = extractStickers(value)
            val state = _collageState.value.copy(stickerList = stickers)
            _collageState.value = state
            if (initialState == null) initialState = state.copy()
        }

    private var tempRatio: String? = null
    private var tempBackground: BackgroundSelection? = null
    private var tempFrame: FrameSelection? = null
    private var tempTransforms: Map<Int, ImageTransformState>? = null

    fun triggerUnselectAllImages() {
        _unselectAllImagesTrigger.update { it + 1 }
    }

    // ---------------------------
    // Sticker Handling
    // ---------------------------

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
            push(_collageState.value.copy(stickerList = stickers))
        }
    }

    // ---------------------------
    // Template & State Loading
    // ---------------------------

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

                    _collageState.value = _collageState.value.copy(templateId = first)

                    if (initialState == null) {
                        initialState = _collageState.value.copy()
                        undoStack.push(initialState!!.copy())
                        redoStack.push(initialState!!.copy())
                    }
                }
                else -> Unit
            }
        }
    }

    // ---------------------------
    // Simple Updates
    // ---------------------------

    fun selectTemplate(t: CollageTemplate) = updateState { it.copy(templateId = t) }
    fun updateTopMargin(v: Float) = updateState { it.copy(topMargin = v) }
    fun updateColumnMargin(v: Float) = updateState { it.copy(columnMargin = v) }
    fun updateCornerRadius(v: Float) = updateState { it.copy(cornerRadius = v) }

    fun updateRatio(r: String?) {
        tempRatio = r
        updateState { it.copy(ratio = r) }
    }

    fun cancelRatioChanges() {
        val restore = undoStack.lastOrNull()?.ratio ?: initialState?.ratio
        tempRatio = null
        updateState { it.copy(ratio = restore) }
    }

    fun updateBackground(selection: BackgroundSelection) {
        tempBackground = selection
        updateState { it.copy(backgroundSelection = selection) }
    }

    fun cancelBackgroundChanges() {
        val restore = undoStack.lastOrNull()?.backgroundSelection ?: initialState?.backgroundSelection
        tempBackground = null
        updateState { it.copy(backgroundSelection = restore) }
    }

    fun updateFrame(selection: FrameSelection) {
        tempFrame = selection
        updateState { it.copy(frameSelection = selection) }
    }

    fun cancelFrameChanges() {
        val restore = undoStack.lastOrNull()?.frameSelection ?: initialState?.frameSelection
        tempFrame = null
        updateState { it.copy(frameSelection = restore) }
    }

    // ---------------------------
    // Undo / Redo
    // ---------------------------

    private fun push(s: CollageState) {
        if (undoStack.lastOrNull() == s && undoStack.size > 1) return
        undoStack.push(s.copy())
        redoStack.clear()

        _collageState.value = s
        _canUndo.value = undoStack.size > 1
        _canRedo.value = false
    }

    fun confirmChanges() {
        push(_collageState.value.copy())
        tempRatio = null
    }

    fun undo() {
        if (undoStack.size < 2) return
        val current = undoStack.pop()
        redoStack.push(current.copy())

        val prev = undoStack.peek()
        _collageState.value = prev.copy()

        _canUndo.value = undoStack.size > 1
        _canRedo.value = true
    }

    fun redo() {
        if (redoStack.isEmpty()) return
        val state = redoStack.pop()
        undoStack.push(state.copy())

        _collageState.value = state.copy()

        _canUndo.value = undoStack.size > 1
        _canRedo.value = redoStack.isNotEmpty()
    }

    // ---------------------------
    // Image Transform
    // ---------------------------

    fun updateImageTransforms(m: Map<Int, ImageTransformState>) {
        tempTransforms = m
        updateState { it.copy(imageTransforms = m) }
    }

    fun confirmImageTransformChanges() {
        tempTransforms = null
    }

    // ---------------------------
    // Image URI & Bitmap Management
    // ---------------------------

    fun setImageUris(context: Context, uris: List<Uri>) {
        viewModelScope.launch(Dispatchers.IO) {
            val bitmaps = uris.mapIndexedNotNull { i, uri ->
                uriToBitmap(context, uri)?.let { i to it }
            }.toMap()

            updateState {
                it.copy(imageUris = uris, imageBitmaps = bitmaps)
            }
        }
    }

    fun addImageUri(context: Context, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val bitmaps = collageState.value.imageBitmaps.toMutableMap()
            uriToBitmap(context, uri)?.let { bitmaps[bitmaps.size] = it }

            updateState {
                it.copy(
                    imageUris = it.imageUris + uri,
                    imageBitmaps = bitmaps
                )
            }
        }
    }

    fun removeImageUri(index: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val state = collageState.value
            val uris = state.imageUris.toMutableList()
            if (uris.size <= 1 || index !in uris.indices) return@launch

            uris.removeAt(index)

            val newBitmaps = mutableMapOf<Int, Bitmap>()
            uris.forEachIndexed { i, _ ->
                state.imageBitmaps[i]?.let { newBitmaps[i] = it }
            }

            updateState { it.copy(imageUris = uris, imageBitmaps = newBitmaps) }
        }
    }

    // ---------------------------
    // Image Tools (rotate, flip)
    // ---------------------------

    fun rotateImage(context: Context, index: Int) =
        applyCropTransform(context, index) { controller, cb -> controller.rotateClockwise(cb) }

    fun flipImageHorizontal(context: Context, index: Int) =
        applyCropTransform(context, index) { controller, cb -> controller.flipHorizontally(cb) }

    fun flipImageVertical(context: Context, index: Int) =
        applyCropTransform(context, index) { controller, cb -> controller.flipVertically(cb) }

    private fun applyCropTransform(
        context: Context,
        index: Int,
        action: (CropController, (Bitmap) -> Unit) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val bitmap = collageState.value.imageBitmaps[index]
                ?: collageState.value.imageUris.getOrNull(index)?.let { uriToBitmap(context, it) }
                ?: return@launch

            val controller = CropController(
                bitmap,
                cropOptions = CropDefaults.cropOptions(CropShape.FreeForm),
                cropColors = CropColors(
                    overlay = Color.Transparent,
                    overlayActive = Color.Transparent,
                    gridlines = Color.Transparent,
                    cropRectangle = Color.Transparent,
                    handle = Color.Transparent
                )
            )

            action(controller) { newBitmap ->
                val maps = collageState.value.imageBitmaps.toMutableMap()
                maps[index] = newBitmap
                updateState { it.copy(imageBitmaps = maps) }
            }
        }
    }

    // ---------------------------
    // Helper
    // ---------------------------

    private fun updateState(block: (CollageState) -> CollageState) {
        _collageState.update(block)
    }

    private suspend fun uriToBitmap(ctx: Context, uri: Uri): Bitmap? =
        withContext(Dispatchers.IO) {
            try {
                if (uri.toString().startsWith("file://"))
                    BitmapFactory.decodeFile(uri.toString().removePrefix("file://"))
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