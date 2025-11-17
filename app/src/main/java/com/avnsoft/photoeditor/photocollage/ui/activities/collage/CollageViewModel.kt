package com.avnsoft.photoeditor.photocollage.ui.activities.collage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.avnsoft.photoeditor.photocollage.data.model.collage.CollageState
import com.avnsoft.photoeditor.photocollage.data.model.collage.CollageTemplate
import com.avnsoft.photoeditor.photocollage.data.repository.CollageTemplateRepository
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.GridsTab
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.text_sticker.TextStickerUIState
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.text_sticker.lib.FontAsset
import com.basesource.base.result.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class CollageViewModel(
    private val repository: CollageTemplateRepository
) : ViewModel() {

    private val undoRedoManager = CollageUndoRedoManager()

    // Templates
    private val _templates = MutableStateFlow<List<CollageTemplate>>(emptyList())
    val templates: StateFlow<List<CollageTemplate>> = _templates.asStateFlow()

    private val _selected = MutableStateFlow<CollageTemplate?>(null)
    val selected: StateFlow<CollageTemplate?> = _selected.asStateFlow()

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

    // Undo/Redo state
    private val _canUndo = MutableStateFlow(false)
    val canUndo: StateFlow<Boolean> = _canUndo.asStateFlow()

    private val _canRedo = MutableStateFlow(false)
    val canRedo: StateFlow<Boolean> = _canRedo.asStateFlow()

    // Lưu ratio tạm thời khi đang chọn (chưa confirm)
    private var tempRatio: String? = null

    // Lưu background selection tạm thời khi đang chọn (chưa confirm)
    private var tempBackgroundSelection: com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.BackgroundSelection? = null

    // Lưu frame selection tạm thời khi đang chọn (chưa confirm)
    private var tempFrameSelection: com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.FrameSelection? = null

    // Lưu sticker bitmap path tạm thời khi đang chọn (chưa confirm)
    private var tempStickerBitmapPath: String? = null

    // Lưu image transforms tạm thời khi đang chỉnh sửa (chưa confirm)
    private var tempImageTransforms: Map<Int, com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.ImageTransformState>? = null

    fun load(count: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            getConfigTextSticker()
            when (val res = repository.getTemplates()) {
                is Result.Success -> {
                    val all = res.data
                    val filtered = all.filter { it.cells.size == count }
                    val options = filtered.ifEmpty { all }
                    _templates.value = options
                    _selected.value = options.firstOrNull()
                    
                    // Update state với template đầu tiên (chỉ update, không save vào undo stack)
                    options.firstOrNull()?.let { template ->
                        val stateWithTemplate = _collageState.value.copy(templateId = template.id)
                        _collageState.value = stateWithTemplate
                        // Lưu initial state (để có thể undo về ban đầu khi confirm lần đầu)
                        if (initialState == null) {
                            initialState = stateWithTemplate.copy()
                        }
                    }
                }
                else -> { /* no-op */ }
            }
        }
    }

    fun selectTemplate(template: CollageTemplate) {
        viewModelScope.launch(Dispatchers.IO) {
            _selected.value = template
            _collageState.value = _collageState.value.copy(templateId = template.id)
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
            val lastSavedState = undoRedoManager.getLastState()
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

    fun confirmRatioChanges() {
        viewModelScope.launch(Dispatchers.IO) {
            val currentState = _collageState.value
            val lastSavedState = undoRedoManager.getLastState()
            
            // Tạo state mới với ratio đã chọn
            val newState = currentState.copy(
                ratio = currentState.ratio
            )
            
            // Merge với lastSavedState để giữ nguyên các giá trị không thay đổi
            val stateToSave = lastSavedState?.let { last ->
                newState.copy(
                    // Giữ các giá trị khác từ last saved state
                    templateId = last.templateId,
                    topMargin = last.topMargin,
                    columnMargin = last.columnMargin,
                    cornerRadius = last.cornerRadius,
                    backgroundSelection = last.backgroundSelection,
                    frameSelection = last.frameSelection,
                    texts = last.texts,
                    stickers = last.stickers,
                    filter = last.filter,
                    blur = last.blur,
                    brightness = last.brightness,
                    contrast = last.contrast,
                    saturation = last.saturation
                )
            } ?: newState
            
            // Nếu đây là lần đầu confirm (redo stack rỗng) và có initial state, lưu initial state trước
            if (!undoRedoManager.canUndo() && initialState != null) {
                val initial = initialState!!
                // Kiểm tra xem có thay đổi so với initial state không
                val hasChanges = initial.ratio != stateToSave.ratio
                
                if (hasChanges) {
                    // Lưu initial state vào redo stack trước (để có thể undo về ban đầu)
                    undoRedoManager.saveState(initial.copy())
                }
            }
            
            // Lưu state vào redo stack
            undoRedoManager.saveState(stateToSave)
            _collageState.value = stateToSave
            tempRatio = null // Clear temp ratio sau khi confirm
            updateUndoRedoState()
        }
    }

    fun confirmGridsChanges(tab: GridsTab) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentState = _collageState.value
            val lastSavedState = undoRedoManager.getLastState()
            
            // Tạo state mới chỉ với thay đổi tương ứng với tab
            val newState = when (tab) {
                GridsTab.LAYOUT -> {
                    // Chỉ lưu layout (templateId), giữ nguyên margin values từ currentState
                    currentState.copy(
                        templateId = _selected.value?.id ?: currentState.templateId
                    )
                }
                GridsTab.MARGIN -> {
                    // Chỉ lưu margin values, giữ nguyên templateId từ currentState
                    currentState.copy(
                        topMargin = currentState.topMargin,
                        columnMargin = currentState.columnMargin,
                        cornerRadius = currentState.cornerRadius
                    )
                }
            }
            
            // Merge với lastSavedState để giữ nguyên các giá trị không thay đổi
            val stateToSave = lastSavedState?.let { last ->
                when (tab) {
                    GridsTab.LAYOUT -> newState.copy(
                        // Giữ margin từ last saved state
                        topMargin = last.topMargin,
                        columnMargin = last.columnMargin,
                        cornerRadius = last.cornerRadius,
                        // Giữ ratio và các giá trị khác
                        ratio = last.ratio,
                        backgroundSelection = last.backgroundSelection,
                        frameSelection = last.frameSelection,
                        texts = last.texts,
                        stickers = last.stickers,
                        filter = last.filter,
                        blur = last.blur,
                        brightness = last.brightness,
                        contrast = last.contrast,
                        saturation = last.saturation
                    )
                    GridsTab.MARGIN -> newState.copy(
                        // Giữ templateId từ last saved state
                        templateId = last.templateId,
                        // Giữ ratio và các giá trị khác
                        ratio = last.ratio,
                        backgroundSelection = last.backgroundSelection,
                        frameSelection = last.frameSelection,
                        texts = last.texts,
                        stickers = last.stickers,
                        filter = last.filter,
                        blur = last.blur,
                        brightness = last.brightness,
                        contrast = last.contrast,
                        saturation = last.saturation
                    )
                }
            } ?: newState
            
            // Nếu đây là lần đầu confirm (redo stack rỗng) và có initial state, lưu initial state trước
            if (!undoRedoManager.canUndo() && initialState != null) {
                val initial = initialState!!
                // Kiểm tra xem có thay đổi so với initial state không
                val hasChanges = when (tab) {
                    GridsTab.LAYOUT -> initial.templateId != stateToSave.templateId
                    GridsTab.MARGIN -> initial.topMargin != stateToSave.topMargin ||
                            initial.columnMargin != stateToSave.columnMargin ||
                            initial.cornerRadius != stateToSave.cornerRadius
                }
                
                if (hasChanges) {
                    // Lưu initial state vào redo stack trước (để có thể undo về ban đầu)
                    undoRedoManager.saveState(initial.copy())
                }
            }
            
            // Lưu state vào redo stack
            undoRedoManager.saveState(stateToSave)
            _collageState.value = stateToSave
            updateUndoRedoState()
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
            val lastSavedState = undoRedoManager.getLastState()
            val backgroundSelectionToRestore = lastSavedState?.backgroundSelection ?: initialState?.backgroundSelection
            tempBackgroundSelection = null
            _collageState.value = _collageState.value.copy(
                backgroundSelection = backgroundSelectionToRestore
            )
        }
    }

    fun confirmBackgroundChanges() {
        viewModelScope.launch(Dispatchers.IO) {
            val currentState = _collageState.value
            val lastSavedState = undoRedoManager.getLastState()
            
            // Tạo state mới với background đã chọn
            val newState = currentState.copy(
                backgroundSelection = currentState.backgroundSelection
            )
            
            // Merge với lastSavedState để giữ nguyên các giá trị không thay đổi
            val stateToSave = lastSavedState?.let { last ->
                newState.copy(
                    // Giữ các giá trị khác từ last saved state
                    templateId = last.templateId,
                    topMargin = last.topMargin,
                    columnMargin = last.columnMargin,
                    cornerRadius = last.cornerRadius,
                    ratio = last.ratio,
                    // Lưu backgroundSelection từ currentState (đã chọn mới), không phải từ last
                    backgroundSelection = currentState.backgroundSelection,
                    frameSelection = last.frameSelection,
                    texts = last.texts,
                    stickers = last.stickers,
                    filter = last.filter,
                    blur = last.blur,
                    brightness = last.brightness,
                    contrast = last.contrast,
                    saturation = last.saturation
                )
            } ?: newState
            
            // Nếu đây là lần đầu confirm (redo stack rỗng) và có initial state, lưu initial state trước
            if (!undoRedoManager.canUndo() && initialState != null) {
                val initial = initialState!!
                // Kiểm tra xem có thay đổi so với initial state không
                val hasChanges = initial.backgroundSelection != stateToSave.backgroundSelection
                
                if (hasChanges) {
                    // Lưu initial state vào redo stack trước (để có thể undo về ban đầu)
                    undoRedoManager.saveState(initial.copy())
                }
            }
            
            // Lưu state vào redo stack
            undoRedoManager.saveState(stateToSave)
            _collageState.value = stateToSave
            tempBackgroundSelection = null // Clear temp background sau khi confirm
            updateUndoRedoState()
        }
    }

    fun undo() {
        viewModelScope.launch(Dispatchers.IO) {
            val previousState = undoRedoManager.undo()
            previousState?.let { state ->
                _collageState.value = state
                // Update selected template nếu có
                state.templateId?.let { templateId ->
                    _templates.value.find { it.id == templateId }?.let { template ->
                        _selected.value = template
                    }
                }
                updateUndoRedoState()
            }
        }
    }

    fun redo() {
        viewModelScope.launch(Dispatchers.IO) {
            val nextState = undoRedoManager.redo()
            nextState?.let { state ->
                _collageState.value = state
                // Update selected template nếu có
                state.templateId?.let { templateId ->
                    _templates.value.find { it.id == templateId }?.let { template ->
                        _selected.value = template
                    }
                }
                updateUndoRedoState()
            }
        }
    }


    private fun updateUndoRedoState() {
        _canUndo.value = undoRedoManager.canUndo()
        _canRedo.value = undoRedoManager.canRedo()
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
            val lastSavedState = undoRedoManager.getLastState()
            val frameSelectionToRestore = lastSavedState?.frameSelection ?: initialState?.frameSelection
            tempFrameSelection = null
            _collageState.value = _collageState.value.copy(
                frameSelection = frameSelectionToRestore
            )
        }
    }

    fun confirmFrameChanges() {
        viewModelScope.launch(Dispatchers.IO) {
            val currentState = _collageState.value
            val lastSavedState = undoRedoManager.getLastState()
            
            // Tạo state mới với frame đã chọn
            val newState = currentState.copy(
                frameSelection = currentState.frameSelection
            )
            
            // Merge với lastSavedState để giữ nguyên các giá trị không thay đổi
            val stateToSave = lastSavedState?.let { last ->
                newState.copy(
                    // Giữ các giá trị khác từ last saved state
                    templateId = last.templateId,
                    topMargin = last.topMargin,
                    columnMargin = last.columnMargin,
                    cornerRadius = last.cornerRadius,
                    ratio = last.ratio,
                    backgroundSelection = last.backgroundSelection,
                    // Lưu frameSelection từ currentState (đã chọn mới), không phải từ last
                    frameSelection = currentState.frameSelection,
                    texts = last.texts,
                    stickers = last.stickers,
                    filter = last.filter,
                    blur = last.blur,
                    brightness = last.brightness,
                    contrast = last.contrast,
                    saturation = last.saturation
                )
            } ?: newState
            
            // Nếu đây là lần đầu confirm (redo stack rỗng) và có initial state, lưu initial state trước
            if (!undoRedoManager.canUndo() && initialState != null) {
                val initial = initialState!!
                // Kiểm tra xem có thay đổi so với initial state không
                val hasChanges = initial.frameSelection != stateToSave.frameSelection
                
                if (hasChanges) {
                    // Lưu initial state vào redo stack trước (để có thể undo về ban đầu)
                    undoRedoManager.saveState(initial.copy())
                }
            }
            
            // Lưu state vào redo stack
            undoRedoManager.saveState(stateToSave)
            _collageState.value = stateToSave
            tempFrameSelection = null // Clear temp frame sau khi confirm
            updateUndoRedoState()
        }
    }

    // Helper để update state từ các tools khác (mở rộng sau)
    fun updateStickerBitmapPath(path: String) {
        viewModelScope.launch(Dispatchers.IO) {
            tempStickerBitmapPath = path
            _collageState.value = _collageState.value.copy(
                stickerBitmapPath = path
            )
        }
    }

    fun cancelStickerChanges() {
        viewModelScope.launch(Dispatchers.IO) {
            // Khôi phục sticker bitmap path về state đã lưu cuối cùng
            val lastSavedState = undoRedoManager.getLastState()
            val stickerBitmapPathToRestore = lastSavedState?.stickerBitmapPath ?: initialState?.stickerBitmapPath
            tempStickerBitmapPath = null
            _collageState.value = _collageState.value.copy(
                stickerBitmapPath = stickerBitmapPathToRestore
            )
        }
    }

    fun confirmStickerChanges() {
        viewModelScope.launch(Dispatchers.IO) {
            val currentState = _collageState.value
            val lastSavedState = undoRedoManager.getLastState()
            
            // Tạo state mới với sticker bitmap path đã chọn
            val newState = currentState.copy(
                stickerBitmapPath = currentState.stickerBitmapPath
            )
            
            // Merge với lastSavedState để giữ nguyên các giá trị không thay đổi
            val stateToSave = lastSavedState?.let { last ->
                newState.copy(
                    // Giữ các giá trị khác từ last saved state
                    templateId = last.templateId,
                    topMargin = last.topMargin,
                    columnMargin = last.columnMargin,
                    cornerRadius = last.cornerRadius,
                    ratio = last.ratio,
                    backgroundSelection = last.backgroundSelection,
                    frameSelection = last.frameSelection,
                    texts = last.texts,
                    stickers = last.stickers,
                    // Lưu stickerBitmapPath từ currentState (đã chọn mới), không phải từ last
                    stickerBitmapPath = currentState.stickerBitmapPath,
                    filter = last.filter,
                    blur = last.blur,
                    brightness = last.brightness,
                    contrast = last.contrast,
                    saturation = last.saturation
                )
            } ?: newState
            
            // Nếu đây là lần đầu confirm (redo stack rỗng) và có initial state, lưu initial state trước
            if (!undoRedoManager.canUndo() && initialState != null) {
                val initial = initialState!!
                // Kiểm tra xem có thay đổi so với initial state không
                val hasChanges = initial.stickerBitmapPath != stateToSave.stickerBitmapPath
                
                if (hasChanges) {
                    // Lưu initial state vào redo stack trước (để có thể undo về ban đầu)
                    undoRedoManager.saveState(initial.copy())
                }
            }
            
            // Lưu state vào redo stack
            undoRedoManager.saveState(stateToSave)
            _collageState.value = stateToSave
            tempStickerBitmapPath = null // Clear temp sticker path sau khi confirm
            updateUndoRedoState()
        }
    }

    fun updateState(update: (CollageState) -> CollageState) {
        viewModelScope.launch(Dispatchers.IO) {
            _collageState.value = update(_collageState.value)
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
            val lastSavedState = undoRedoManager.getLastState()

            val newState = currentState.copy(
                imageTransforms = currentState.imageTransforms
            )

            val stateToSave = lastSavedState?.let { last ->
                newState.copy(
                    templateId = last.templateId,
                    topMargin = last.topMargin,
                    columnMargin = last.columnMargin,
                    cornerRadius = last.cornerRadius,
                    ratio = last.ratio,
                    backgroundSelection = last.backgroundSelection,
                    frameSelection = last.frameSelection,
                    texts = last.texts,
                    stickers = last.stickers,
                    stickerBitmapPath = last.stickerBitmapPath,
                    imageTransforms = currentState.imageTransforms,
                    filter = last.filter,
                    blur = last.blur,
                    brightness = last.brightness,
                    contrast = last.contrast,
                    saturation = last.saturation,
                    textState = last.textState
                )
            } ?: newState

            // Nếu đây là lần đầu confirm (redo stack rỗng) và có initial state, lưu initial state trước
            if (!undoRedoManager.canUndo() && initialState != null) {
                val initial = initialState!!
                // Kiểm tra xem có thay đổi so với initial state không
                val hasChanges = initial.imageTransforms != stateToSave.imageTransforms

                if (hasChanges) {
                    // Lưu initial state vào redo stack trước (để có thể undo về ban đầu)
                    undoRedoManager.saveState(initial.copy())
                }
            }

            // Lưu state vào redo stack
            undoRedoManager.saveState(stateToSave)
            _collageState.value = stateToSave
            tempImageTransforms = null // Clear temp transforms sau khi confirm
            updateUndoRedoState()
        }
    }
}


