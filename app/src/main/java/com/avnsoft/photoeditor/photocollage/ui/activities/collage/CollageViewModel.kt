package com.avnsoft.photoeditor.photocollage.ui.activities.collage

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.avnsoft.photoeditor.photocollage.data.model.collage.CollageState
import com.avnsoft.photoeditor.photocollage.data.model.collage.CollageTemplate
import com.avnsoft.photoeditor.photocollage.data.repository.CollageTemplateRepository
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.GridsTab
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.sticker.lib.Sticker
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.text_sticker.TextStickerUIState
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.text_sticker.lib.FontAsset
import com.avnsoft.photoeditor.photocollage.ui.activities.freestyle.lib.FreeStyleStickerView
import com.avnsoft.photoeditor.photocollage.utils.FileUtil.toFile
import com.basesource.base.result.Result
import com.tanishranjan.cropkit.CropController
import com.tanishranjan.cropkit.CropDefaults
import com.tanishranjan.cropkit.CropOptions
import com.tanishranjan.cropkit.CropShape
import com.tanishranjan.cropkit.CropColors
import com.tanishranjan.cropkit.GridLinesVisibility
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.annotation.KoinViewModel
import androidx.core.net.toUri
import java.util.Stack

@KoinViewModel
class CollageViewModel(
    private val repository: CollageTemplateRepository
) : ViewModel() {

    private val undoStack = Stack<CollageState>()
    private val redoStack = Stack<CollageState>()

    // Templates
    private val _templates = MutableStateFlow<List<CollageTemplate>>(emptyList())
    val templates: StateFlow<List<CollageTemplate>> = _templates.asStateFlow()

    // Collage State
    private val _collageState = MutableStateFlow(
        CollageState(
            topMargin = 0f,
            columnMargin = 0f,
            cornerRadius = 0f
        )
    )
    val collageState: StateFlow<CollageState> = _collageState.asStateFlow()

    // Lưu initial state để có thể undo về ban đầu khi confirm lần đầu
    private var initialState: CollageState? = null
    private var hasInitialStateBeenSaved = false

    // Undo/Redo state
    private val _canUndo = MutableStateFlow(false)
    val canUndo: StateFlow<Boolean> = _canUndo.asStateFlow()

    private val _canRedo = MutableStateFlow(false)
    val canRedo: StateFlow<Boolean> = _canRedo.asStateFlow()

    // State để trigger unselect all images
    private val _unselectAllImagesTrigger = MutableStateFlow(0)
    val unselectAllImagesTrigger: StateFlow<Int> = _unselectAllImagesTrigger.asStateFlow()

    fun triggerUnselectAllImages() {
        viewModelScope.launch(Dispatchers.Main) {
            _unselectAllImagesTrigger.value = _unselectAllImagesTrigger.value + 1
        }
    }

    // Lưu ratio tạm thời khi đang chọn (chưa confirm)
    private var tempRatio: String? = null

    // Lưu background selection tạm thời khi đang chọn (chưa confirm)
    private var tempBackgroundSelection: com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.BackgroundSelection? = null

    // Lưu frame selection tạm thời khi đang chọn (chưa confirm)
    private var tempFrameSelection: com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.FrameSelection? = null


    // Lưu image transforms tạm thời khi đang chỉnh sửa (chưa confirm)
    private var tempImageTransforms: Map<Int, com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.ImageTransformState>? = null

    // Reference đến FreeStyleStickerView để lấy/restore stickers
    var stickerView: FreeStyleStickerView? = null
        set(value) {
            field = value
            // Không push initial state ở đây, vì đã push trong load()
            // Chỉ update stickerList vào current state
            if (value != null) {
                val currentState = _collageState.value
                val stickers = getStickersFromView(value)
                val stateWithStickers = currentState.copy(stickerList = stickers)
                _collageState.value = stateWithStickers
                // Update initial state nếu chưa có
                if (initialState == null) {
                    initialState = stateWithStickers.copy()
                }
            }
        }

    /**
     * Lấy danh sách stickers từ StickerView bằng reflection
     */
    private fun getStickersFromView(stickerView: FreeStyleStickerView): List<Sticker> {
        return try {
            val field = stickerView.javaClass.superclass.getDeclaredField("stickers")
            field.isAccessible = true
            @Suppress("UNCHECKED_CAST")
            (field.get(stickerView) as? MutableList<Sticker>)?.toList() ?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Lưu state của stickers khi apply sticker/text
     */
    fun confirmStickerChanges() {
        viewModelScope.launch(Dispatchers.IO) {
            val currentState = _collageState.value
            val stickers = stickerView?.let { getStickersFromView(it) } ?: emptyList()
            val stateToSave = currentState.copy(stickerList = stickers)
            
            push(stateToSave)
        }
    }

    /**
     * Restore stickers từ state (được gọi khi undo/redo)
     */
    fun restoreStickers(stickers: List<Sticker>) {
        stickerView?.let { view ->
            // Tạm thời disable callbacks để tránh side effects
            val originalListener = view.onStickerOperationListener
            view.setOnStickerOperationListener(null)
            
            view.removeAllStickers()
            
            stickers.forEach { originalSticker ->
                // Clone sticker để tránh reference issues
                val clonedSticker = cloneSticker(originalSticker)
                if (clonedSticker != null) {
                    // Add sticker trực tiếp vào list để tránh trigger callbacks và set position mặc định
                    try {
                        val field = view.javaClass.superclass.getDeclaredField("stickers")
                        field.isAccessible = true
                        @Suppress("UNCHECKED_CAST")
                        val stickersList = field.get(view) as? MutableList<Sticker>
                        
                        // Matrix đã được copy trong cloneSticker, nhưng đảm bảo nó được set đúng
                        // (cloneSticker đã copy matrix rồi)
                        
                        stickersList?.add(clonedSticker)
                        
                        // Set handling sticker nếu đây là sticker đầu tiên
                        if (stickersList?.size == 1) {
                            val handlingField = view.javaClass.superclass.getDeclaredField("handlingSticker")
                            handlingField.isAccessible = true
                            handlingField.set(view, clonedSticker)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        // Fallback: dùng addSticker nếu reflection fail
                        view.addSticker(clonedSticker)
                        // Restore matrix sau khi add (vì addSticker sẽ set position mặc định)
                        clonedSticker.matrix.set(originalSticker.matrix)
                    }
                }
            }
            
            // Restore callbacks
            view.setOnStickerOperationListener(originalListener)
            view.invalidate()
        }
    }

    /**
     * Clone một Sticker (tạo instance mới với cùng properties)
     */
    private fun cloneSticker(sticker: Sticker): Sticker? {
        return try {
            // Tạo sticker mới dựa trên type
            when (sticker) {
                is com.avnsoft.photoeditor.photocollage.ui.activities.editor.text_sticker.lib.TextSticker -> {
                    val props = sticker.getAddTextProperties() ?: return null
                    val newSticker = com.avnsoft.photoeditor.photocollage.ui.activities.editor.text_sticker.lib.TextSticker(
                        stickerView?.context ?: return null,
                        props
                    )
                    // Copy matrix
                    newSticker.matrix.set(sticker.matrix)
                    // Copy flip state
                    if (sticker.isFlippedHorizontally) newSticker.setFlippedHorizontally(true)
                    if (sticker.isFlippedVertically) newSticker.setFlippedVertically(true)
                    // Copy alpha
                    newSticker.setAlpha(sticker.alpha)
                    newSticker
                }
                is com.avnsoft.photoeditor.photocollage.ui.activities.editor.sticker.lib.DrawableSticker -> {
                    // Clone DrawableSticker
                    val drawable = sticker.drawable
                    if (drawable != null) {
                        val newSticker = com.avnsoft.photoeditor.photocollage.ui.activities.editor.sticker.lib.DrawableSticker(drawable)
                        // Copy matrix
                        newSticker.matrix.set(sticker.matrix)
                        // Copy flip state
                        if (sticker.isFlippedHorizontally) newSticker.setFlippedHorizontally(true)
                        if (sticker.isFlippedVertically) newSticker.setFlippedVertically(true)
                        // Copy alpha
                        newSticker.setAlpha(sticker.alpha)
                        newSticker
                    } else {
                        null
                    }
                }
                is com.avnsoft.photoeditor.photocollage.ui.activities.freestyle.lib.FreeStyleSticker -> {
                    // Clone FreeStyleSticker
                    val drawable = sticker.drawable
                    val id = sticker.id
                    val photo = try {
                        val photoField = sticker.javaClass.getDeclaredField("photo")
                        photoField.isAccessible = true
                        photoField.get(sticker) as? com.avnsoft.photoeditor.photocollage.ui.activities.freestyle.lib.Photo
                    } catch (e: Exception) {
                        null
                    }
                    if (drawable != null) {
                        val newSticker = com.avnsoft.photoeditor.photocollage.ui.activities.freestyle.lib.FreeStyleSticker(id, photo, drawable)
                        // Copy matrix
                        newSticker.matrix.set(sticker.matrix)
                        // Copy flip state
                        if (sticker.isFlippedHorizontally) newSticker.setFlippedHorizontally(true)
                        if (sticker.isFlippedVertically) newSticker.setFlippedVertically(true)
                        // Copy alpha
                        newSticker.setAlpha(sticker.alpha)
                        newSticker
                    } else {
                        null
                    }
                }
                else -> null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun load(count: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            getConfigTextSticker()
            when (val res = repository.getTemplates()) {
                is Result.Success -> {
                    val all = res.data
                    val filtered = all.filter { it.cells.size == count }
                    val options = filtered.ifEmpty { all }
                    _templates.value = options

                    // Update state với template đầu tiên (chỉ update, không save vào undo stack)
                    options.firstOrNull()?.let { template ->
                        val stateWithTemplate = _collageState.value.copy(templateId = template)
                        _collageState.value = stateWithTemplate
                        // Lưu initial state và push vào undo stack (chỉ một lần)
                        if (initialState == null && undoStack.isEmpty()) {
                            initialState = stateWithTemplate.copy()
                            // Push initial state vào undo stack (chỉ push một lần)
                            undoStack.push(stateWithTemplate.copy())
                            redoStack.push(stateWithTemplate.copy())
                        }
                    }
                }
                else -> { /* no-op */ }
            }
        }
    }

    fun selectTemplate(template: CollageTemplate) {
        viewModelScope.launch(Dispatchers.IO) {
            _collageState.value = _collageState.value.copy(templateId = template)
        }
    }

    fun updateTopMargin(value: Float) {
        viewModelScope.launch(Dispatchers.IO) {
            _collageState.value = _collageState.value.copy(topMargin = value)
        }
    }

    fun updateColumnMargin(value: Float) {
        viewModelScope.launch(Dispatchers.IO) {
            _collageState.value = _collageState.value.copy(columnMargin = value)
        }
    }

    fun updateCornerRadius(value: Float) {
        viewModelScope.launch(Dispatchers.IO) {
            _collageState.value = _collageState.value.copy(cornerRadius = value)
        }
    }

    fun updateRatio(ratio: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            tempRatio = ratio
            _collageState.value = _collageState.value.copy(ratio = ratio)
        }
    }

    fun cancelRatioChanges() {
        viewModelScope.launch(Dispatchers.IO) {
            // Khôi phục ratio về state đã lưu cuối cùng
            val lastSavedState = undoStack.lastOrNull()
            val ratioToRestore = lastSavedState?.ratio ?: initialState?.ratio
            tempRatio = null
            _collageState.value = _collageState.value.copy(ratio = ratioToRestore)
        }
    }

    fun getConfigTextSticker() {
        viewModelScope.launch(Dispatchers.IO) {
            _collageState.value = _collageState.value.copy(
                textState = TextStickerUIState().copy(
                    originBitmap = null,
                    items = FontAsset.listFonts
                )
            )
        }
    }
    fun confirmChanges() {
        viewModelScope.launch(Dispatchers.IO) {
            val currentState = _collageState.value
            val stateToSave = currentState.copy()

            push(stateToSave)
            tempRatio = null
        }
    }

    fun updateBackground(selection: com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.BackgroundSelection) {
        viewModelScope.launch(Dispatchers.IO) {
            tempBackgroundSelection = selection
            _collageState.value = _collageState.value.copy(
                backgroundSelection = selection
            )
        }
    }
    
    // Deprecated: Use updateBackground instead
    @Deprecated("Use updateBackground instead", ReplaceWith("updateBackground(BackgroundSelection.Solid(color))"))
    fun updateBackgroundColor(color: String?) {
        color?.let {
            updateBackground(com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.BackgroundSelection.Solid(it))
        }
    }

    fun cancelBackgroundChanges() {
        viewModelScope.launch(Dispatchers.IO) {
            // Khôi phục background về state đã lưu cuối cùng
            val lastSavedState = undoStack.lastOrNull()
            val backgroundSelectionToRestore = lastSavedState?.backgroundSelection ?: initialState?.backgroundSelection
            tempBackgroundSelection = null
            _collageState.value = _collageState.value.copy(
                backgroundSelection = backgroundSelectionToRestore
            )
        }
    }

    private fun push(state: CollageState) {
        // Luôn push state vào undo stack khi confirm
        // Chỉ skip duplicate nếu đã có nhiều hơn 1 state (tránh skip lần confirm đầu tiên)
        val lastState = undoStack.lastOrNull()
        if (lastState != null && lastState == state && undoStack.size > 1) {
            // Chỉ skip duplicate nếu không phải lần confirm đầu tiên
            return
        }
        
        undoStack.push(state.copy())
        _collageState.value = state
        _canUndo.value = undoStack.size >= 2
        _canRedo.value = false
        redoStack.clear()
    }

    fun undo() {
        if (undoStack.size < 2) return // không thể undo

        val current = undoStack.pop()            // Lấy state hiện tại
        redoStack.push(current.copy())           // Đẩy vào redo stack

        val previous = undoStack.peek()          // Lấy state trước đó (sau khi pop)

        _collageState.value = previous.copy()
        // Restore stickers
        //restoreStickers(previous.stickerList)

        _canUndo.value = undoStack.size > 1
        _canRedo.value = true
    }

    fun redo() {
        if (redoStack.isEmpty()) return

        val state = redoStack.pop()              // Lấy lại từ redo
        undoStack.push(state.copy())             // Push lại vào undo stack

        _collageState.value = state.copy()
        // Restore stickers
        //restoreStickers(state.stickerList)

        _canUndo.value = undoStack.size > 1
        _canRedo.value = redoStack.isNotEmpty()
    }

    // Helper để update state từ các tools khác (mở rộng sau)
    fun updateFrame(selection: com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.FrameSelection) {
        viewModelScope.launch(Dispatchers.IO) {
            tempFrameSelection = selection
            _collageState.value = _collageState.value.copy(
                frameSelection = selection
            )
        }
    }

    fun cancelFrameChanges() {
        viewModelScope.launch(Dispatchers.IO) {
            // Khôi phục frame về state đã lưu cuối cùng
            val lastSavedState = undoStack.lastOrNull()
            val frameSelectionToRestore = lastSavedState?.frameSelection ?: initialState?.frameSelection
            tempFrameSelection = null
            _collageState.value = _collageState.value.copy(
                frameSelection = frameSelectionToRestore
            )
        }
    }


    // Image Transform methods
    fun updateImageTransforms(transforms: Map<Int, com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.ImageTransformState>) {
        viewModelScope.launch(Dispatchers.IO) {
            tempImageTransforms = transforms
            _collageState.value = _collageState.value.copy(
                imageTransforms = transforms
            )
        }
    }

    fun confirmImageTransformChanges() {
        viewModelScope.launch(Dispatchers.IO) {
            val currentState = _collageState.value
            tempImageTransforms = null // Clear temp transforms sau khi confirm
        }
    }

    // Image URIs management - tất cả đều push vào collageState
    // Load bitmap ban đầu vào state khi set URIs
    fun setImageUris(context: Context, uris: List<Uri>) {
        viewModelScope.launch(Dispatchers.IO) {
            val newBitmaps = mutableMapOf<Int, Bitmap>()
            uris.forEachIndexed { index, uri ->
                uriToBitmap(context, uri)?.let { bitmap ->
                    newBitmaps[index] = bitmap
                }
            }
            _collageState.update { 
                it.copy(
                    imageUris = uris,
                    imageBitmaps = newBitmaps
                )
            }
        }
    }

    fun addImageUri(context: Context, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentState = _collageState.value
            val newIndex = currentState.imageUris.size
            val newBitmaps = currentState.imageBitmaps.toMutableMap()
            uriToBitmap(context, uri)?.let { bitmap ->
                newBitmaps[newIndex] = bitmap
            }
            _collageState.update { 
                it.copy(
                    imageUris = it.imageUris + uri,
                    imageBitmaps = newBitmaps
                )
            }
        }
    }

    // Image transformation methods - sử dụng CropController như CropActivity
    // Lưu bitmap trực tiếp vào state thay vì chỉ lưu path
    fun rotateImage(context: Context, index: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentState = _collageState.value
            // Lấy bitmap từ state hoặc load từ URI
            val bitmap = currentState.imageBitmaps[index] 
                ?: (if (index < currentState.imageUris.size) {
                    uriToBitmap(context, currentState.imageUris[index])
                } else null)
            
            bitmap?.let {
                // Tạo CropController với bitmap hiện tại
                val cropController = CropController(
                    bitmap = it,
                    cropOptions = CropDefaults.cropOptions(
                        cropShape = CropShape.FreeForm,
                        gridLinesVisibility = GridLinesVisibility.NEVER,
                        touchPadding = 0.dp,
                        initialPadding = 0.dp,
                        zoomScale = null,
                        rotationZBitmap = null
                    ),
                    cropColors = CropColors(
                        overlay = Color.Transparent,
                        overlayActive = Color.Transparent,
                        gridlines = Color.Transparent,
                        cropRectangle = Color.Transparent,
                        handle = Color.Transparent
                    )
                )
                
                // Sử dụng rotateClockwise từ CropController
                cropController.rotateClockwise { newBitmap ->
                    // Lưu bitmap trực tiếp vào state
                    _collageState.update { state ->
                        val newBitmaps = state.imageBitmaps.toMutableMap()
                        newBitmaps[index] = newBitmap
                        state.copy(imageBitmaps = newBitmaps)
                    }
                }
            }
        }
    }

    fun flipImageHorizontal(context: Context, index: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentState = _collageState.value
            // Lấy bitmap từ state hoặc load từ URI
            val bitmap = currentState.imageBitmaps[index] 
                ?: (if (index < currentState.imageUris.size) {
                    uriToBitmap(context, currentState.imageUris[index])
                } else null)
            
            bitmap?.let {
                // Tạo CropController với bitmap hiện tại
                val cropController = CropController(
                    bitmap = it,
                    cropOptions = CropDefaults.cropOptions(
                        cropShape = CropShape.FreeForm,
                        gridLinesVisibility = GridLinesVisibility.NEVER,
                        touchPadding = 0.dp,
                        initialPadding = 0.dp,
                        zoomScale = null,
                        rotationZBitmap = null
                    ),
                    cropColors = CropColors(
                        overlay = Color.Transparent,
                        overlayActive = Color.Transparent,
                        gridlines = Color.Transparent,
                        cropRectangle = Color.Transparent,
                        handle = Color.Transparent
                    )
                )
                
                // Sử dụng flipHorizontally từ CropController
                cropController.flipHorizontally { newBitmap ->
                    // Lưu bitmap trực tiếp vào state
                    _collageState.update { state ->
                        val newBitmaps = state.imageBitmaps.toMutableMap()
                        newBitmaps[index] = newBitmap
                        state.copy(imageBitmaps = newBitmaps)
                    }
                }
            }
        }
    }

    fun flipImageVertical(context: Context, index: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentState = _collageState.value
            // Lấy bitmap từ state hoặc load từ URI
            val bitmap = currentState.imageBitmaps[index] 
                ?: (if (index < currentState.imageUris.size) {
                    uriToBitmap(context, currentState.imageUris[index])
                } else null)
            
            bitmap?.let {
                // Tạo CropController với bitmap hiện tại
                val cropController = CropController(
                    bitmap = it,
                    cropOptions = CropDefaults.cropOptions(
                        cropShape = CropShape.FreeForm,
                        gridLinesVisibility = GridLinesVisibility.NEVER,
                        touchPadding = 0.dp,
                        initialPadding = 0.dp,
                        zoomScale = null,
                        rotationZBitmap = null
                    ),
                    cropColors = CropColors(
                        overlay = Color.Transparent,
                        overlayActive = Color.Transparent,
                        gridlines = Color.Transparent,
                        cropRectangle = Color.Transparent,
                        handle = Color.Transparent
                    )
                )
                
                // Sử dụng flipVertically từ CropController
                cropController.flipVertically { newBitmap ->
                    // Lưu bitmap trực tiếp vào state
                    _collageState.update { state ->
                        val newBitmaps = state.imageBitmaps.toMutableMap()
                        newBitmaps[index] = newBitmap
                        state.copy(imageBitmaps = newBitmaps)
                    }
                }
            }
        }
    }

    private suspend fun uriToBitmap(context: Context, uri: Uri): Bitmap? {
        return withContext(Dispatchers.IO) {
            try {
                // Nếu URI là file path (file://), dùng BitmapFactory.decodeFile
                val uriString = uri.toString()
                if (uriString.startsWith("file://")) {
                    val filePath = uriString.removePrefix("file://")
                    return@withContext android.graphics.BitmapFactory.decodeFile(filePath)
                }
                
                // Nếu URI là content:// hoặc http://, dùng contentResolver
                if (android.os.Build.VERSION.SDK_INT < 28) {
                    android.provider.MediaStore.Images.Media.getBitmap(
                        context.contentResolver, uri
                    )
                } else {
                    val source = android.graphics.ImageDecoder.createSource(context.contentResolver, uri)
                    android.graphics.ImageDecoder.decodeBitmap(source)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}


