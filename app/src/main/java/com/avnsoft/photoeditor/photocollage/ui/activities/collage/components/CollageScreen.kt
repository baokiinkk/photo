package com.avnsoft.photoeditor.photocollage.ui.activities.collage.components

import android.app.Activity.RESULT_OK
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ExperimentalComposeApi
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.avnsoft.photoeditor.photocollage.R
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.CollageTemplates
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.CollageViewModel
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.preview.CollagePreview
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.tools.CollageTool
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.tools.FeatureBottomTools
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.tools.ImageEditAction
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.tools.ImageEditToolbar
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.tools.toolsCollage
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.copyImageToAppStorage
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.crop.CropActivity
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.crop.ToolInput
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.text_sticker.lib.TextSticker
import com.avnsoft.photoeditor.photocollage.ui.activities.export_image.ExportImageBottomSheet
import com.avnsoft.photoeditor.photocollage.ui.activities.export_image.ExportImageData
import com.avnsoft.photoeditor.photocollage.ui.activities.freestyle.EditTextStickerLayer
import com.avnsoft.photoeditor.photocollage.ui.activities.freestyle.FreeStyleStickerComposeView
import com.avnsoft.photoeditor.photocollage.ui.activities.freestyle.FreeStyleViewModel
import com.avnsoft.photoeditor.photocollage.ui.activities.freestyle.lib.FreeStyleStickerView
import com.avnsoft.photoeditor.photocollage.ui.dialog.DeleteImageDialog
import com.avnsoft.photoeditor.photocollage.ui.dialog.DiscardChangesDialog
import com.avnsoft.photoeditor.photocollage.ui.theme.Background2
import com.avnsoft.photoeditor.photocollage.utils.FileUtil
import com.avnsoft.photoeditor.photocollage.utils.FileUtil.toBitmap
import com.avnsoft.photoeditor.photocollage.utils.FileUtil.toFile
import com.basesource.base.ui.base.BaseActivity
import com.basesource.base.utils.capturable
import com.basesource.base.utils.launchActivity
import com.basesource.base.utils.rememberCaptureController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val MAX_PHOTOS = 10

@OptIn(ExperimentalComposeApi::class, ExperimentalComposeUiApi::class)
@Composable
fun CollageScreen(
    uris: List<Uri>,
    vm: CollageViewModel,
    freeStyleViewModel: FreeStyleViewModel,
    stickerView: FreeStyleStickerView,
    onBack: () -> Unit,
    onDownloadSuccess: (ExportImageData) -> Unit
) {
    val context = LocalContext.current

    val templates by vm.templates.collectAsState()
    val collageState by vm.collageState.collectAsState()
    val canUndo by vm.canUndo.collectAsState()
    val canRedo by vm.canRedo.collectAsState()
    val unselectAllImagesTrigger by vm.unselectAllImagesTrigger.collectAsState()
    val showDiscardDialog by vm.showDiscardDialog.collectAsState()

    val networkUIState by freeStyleViewModel.networkUIState.collectAsStateWithLifecycle()

    // Intercept back press
    BackHandler {
        vm.showDiscardDialog()
    }

    LaunchedEffect(uris, collageState.imageUris) {
        if (collageState.imageUris.isEmpty() && uris.isNotEmpty()) {
            vm.setImageUris(context, uris)
        }
    }

    val currentUris: List<Uri> =
        if (collageState.imageUris.isEmpty()) uris else collageState.imageUris
    val canAddPhoto = currentUris.size < MAX_PHOTOS
    val canUseGrid = currentUris.size > 1

    var selectedImageIndex by remember { mutableStateOf<Int?>(null) }
    var isSwapMode by remember { mutableStateOf(false) }
    var replaceImageIndex by remember { mutableStateOf<Int?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var deleteImageIndex by remember { mutableStateOf<Int?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { newUri ->
            if (currentUris.size < MAX_PHOTOS) {
                vm.addImageUri(context, newUri)
                val newCount = (currentUris.size + 1).coerceAtLeast(1)
                vm.load(newCount)
            }
        }
    }

    val replaceLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { newUri ->
            replaceImageIndex?.let { index ->
                if (index < currentUris.size) {
                    val newUris = currentUris.toMutableList().apply {
                        this[index] = newUri
                    }
                    vm.setImageUris(context, newUris)
                    replaceImageIndex = null
                }
            }
        }
    }

    LaunchedEffect(unselectAllImagesTrigger) {
        if (unselectAllImagesTrigger > 0) {
            selectedImageIndex = null
        }
    }

    LaunchedEffect(Unit) {
        vm.load(currentUris.size.coerceAtLeast(1))
    }

    val captureController = rememberCaptureController()
    val scope = rememberCoroutineScope()
    var pathBitmap by remember { mutableStateOf("") }
    var showBottomSheetSaveImage by remember { mutableStateOf(false) }

    var showGridsSheet by remember { mutableStateOf(false) }
    var showRatioSheet by remember { mutableStateOf(false) }
    var showBackgroundSheet by remember { mutableStateOf(false) }
    var showFrameSheet by remember { mutableStateOf(false) }
    var showStickerSheet by remember { mutableStateOf(false) }
    var showTextSheet by remember { mutableStateOf(false) }

    val textStickerUIState by freeStyleViewModel.uiState.collectAsStateWithLifecycle()

    fun clearAllSheets() {
        showGridsSheet = false
        showRatioSheet = false
        showBackgroundSheet = false
        showFrameSheet = false
        showStickerSheet = false
        showTextSheet = false
    }
    fun isHasTool() = showGridsSheet || showRatioSheet || showBackgroundSheet || showFrameSheet || showStickerSheet || showTextSheet


    fun handleToolClick(tool: CollageTool) {
        if (isHasTool()) return
        clearAllSheets()
        when (tool) {
            CollageTool.GRIDS -> showGridsSheet = true
            CollageTool.RATIO -> showRatioSheet = true
            CollageTool.BACKGROUND -> showBackgroundSheet = true
            CollageTool.FRAME -> showFrameSheet = true
            CollageTool.STICKER -> showStickerSheet = true
            CollageTool.TEXT -> showTextSheet = true
            CollageTool.ADD_PHOTO -> {
                if (canAddPhoto) {
                    launcher.launch("image/*")
                }
            }

            else -> Unit
        }
    }

    fun handleImageEditAction(action: ImageEditAction) {
        val index = selectedImageIndex ?: return
        when (action) {
            ImageEditAction.REPLACE -> {
                replaceImageIndex = index
                replaceLauncher.launch("image/*")
            }

            ImageEditAction.SWAP -> {
                isSwapMode = true
                Toast.makeText(context, R.string.swab, Toast.LENGTH_SHORT).show()
            }

            ImageEditAction.CROP -> {
                val selectedUri = currentUris.getOrNull(index) ?: return
                scope.launch(Dispatchers.IO) {
                    val path = copyImageToAppStorage(context, selectedUri) ?: return@launch
                    scope.launch(Dispatchers.Main) {
                        (context as? BaseActivity)?.launchActivity(
                            toActivity = CropActivity::class.java,
                            input = ToolInput(pathBitmap = path),
                            callback = { result ->
                                if (result.resultCode == RESULT_OK) {
                                    val resultPathBitmap =
                                        result.data?.getStringExtra("pathBitmap")
                                    if (index < currentUris.size && resultPathBitmap != null) {
                                        val newUris = currentUris.toMutableList().apply {
                                            this[index] = resultPathBitmap.toUri()
                                        }
                                        vm.setImageUris(context, newUris)
                                    }
                                }
                            }
                        )
                    }
                }
            }

            ImageEditAction.ROTATE -> vm.rotateImage(context, index)
            ImageEditAction.FLIP_HORIZONTAL -> vm.flipImageHorizontal(context, index)
            ImageEditAction.FLIP_VERTICAL -> vm.flipImageVertical(context, index)
            ImageEditAction.DELETE -> {
                if (currentUris.size > 1) {
                    deleteImageIndex = index
                    showDeleteDialog = true
                }
            }
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(Background2)
    ) {
        FeaturePhotoHeader(
            onBack = {
                vm.showDiscardDialog()
            },
            onUndo = { vm.undo() },
            onRedo = { vm.redo() },
            onSave = {
                clearAllSheets()
                vm.triggerUnselectAllImages()
                stickerView.setShowFocus(false){
                    scope.launch {
                        delay(200)
                        try {
                            val bitmap = captureController.toImageBitmap().asAndroidBitmap()
                            pathBitmap = bitmap.toFile(context)
                            showBottomSheetSaveImage = true
                        } catch (ex: Throwable) {
                            Toast.makeText(context, "Error ${ex.message}", Toast.LENGTH_SHORT)
                                .show()
                        } finally {
//                            freeStyleViewModel.hideLoading()
                        }
                    }
                }
            },
            canUndo = canUndo && !showGridsSheet && !showRatioSheet,
            canRedo = canRedo && !showGridsSheet && !showRatioSheet
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .pointerInput(selectedImageIndex) {
                    detectTapGestures {
                        if (selectedImageIndex != null) {
                            selectedImageIndex = null
                            vm.triggerUnselectAllImages()
                        }
                    }
                },
        )
        {
            Box(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(top = 80.dp)
                    .capturable(captureController),
                contentAlignment = Alignment.TopCenter
            )
            {
                CollagePreviewContainer(
                    modifier = Modifier,
                    viewModel = vm,
                    collageState = collageState,
                    currentUris = currentUris,
                    selectedImageIndex = selectedImageIndex,
                    isSwapMode = isSwapMode,
                    unselectAllImagesTrigger = unselectAllImagesTrigger,
                    onImageClick = { index ->
                        if (selectedImageIndex == index) {
                            selectedImageIndex = null
                            isSwapMode = false
                        } else {
                            selectedImageIndex = index
                        }
                    },
                    onImageSwap = { firstIndex, secondIndex ->
                        if (firstIndex < currentUris.size && secondIndex < currentUris.size) {
                            val newUris = currentUris.toMutableList().apply {
                                val temp = this[firstIndex]
                                this[firstIndex] = this[secondIndex]
                                this[secondIndex] = temp
                            }
                            vm.setImageUris(context, newUris)
                            isSwapMode = false
                        }
                    },
                    onOutsideClick = {
                        selectedImageIndex = null
                        vm.triggerUnselectAllImages()
                    },
                    onUnselectAll = {
                        selectedImageIndex = null
                        vm.triggerUnselectAllImages()
                    }
                )

                FreeStyleStickerComposeView(
                    modifier = Modifier.matchParentSize(),
                    view = stickerView
                )
            }

            Box(modifier = Modifier.align(Alignment.BottomCenter)) {
                if (selectedImageIndex != null &&
                    !showTextSheet &&
                    !showStickerSheet &&
                    !showGridsSheet &&
                    !showRatioSheet &&
                    !showBackgroundSheet &&
                    !showFrameSheet
                ) {
                    ImageEditToolbar(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(),
                        onActionClick = { action -> handleImageEditAction(action) },
                        onClose = {
                            selectedImageIndex = null
                            vm.triggerUnselectAllImages()
                        },
                        disabledActions = if (currentUris.size <= 1) {
                            setOf(ImageEditAction.DELETE)
                        } else {
                            emptySet()
                        }
                    )
                } else if (!showTextSheet) {
                    FeatureBottomTools(
                        tools = toolsCollage,
                        onToolClick = { tool -> handleToolClick(tool) },
                        disabledTools = buildSet {
                            if (!canAddPhoto) add(CollageTool.ADD_PHOTO)
                            if (!canUseGrid) add(CollageTool.GRIDS)
                        }
                    )
                }

                CollageSheetsContainer(
                    modifier = Modifier,
                    viewModel = vm,
                    freeStyleViewModel = freeStyleViewModel,
                    stickerView = stickerView,
                    collageState = collageState,
                    templates = templates,
                    currentUris = currentUris,
                    showGridsSheet = showGridsSheet,
                    showRatioSheet = showRatioSheet,
                    showBackgroundSheet = showBackgroundSheet,
                    showFrameSheet = showFrameSheet,
                    showStickerSheet = showStickerSheet,
                    showTextSheet = showTextSheet,
                    onCloseGridsSheet = { showGridsSheet = false },
                    onCloseRatioSheet = { showRatioSheet = false },
                    onCloseBackgroundSheet = { showBackgroundSheet = false },
                    onCloseFrameSheet = { showFrameSheet = false },
                    onCloseStickerSheet = { showStickerSheet = false },
                    onCloseTextSheet = { showTextSheet = false },
                    onConfirmGridsSheet = { showGridsSheet = false },
                    onConfirmRatioSheet = { showRatioSheet = false },
                    onConfirmBackgroundSheet = { showBackgroundSheet = false },
                    onConfirmFrameSheet = { showFrameSheet = false },
                    onConfirmStickerSheet = { showStickerSheet = false },
                    onConfirmTextSheet = { showTextSheet = false }
                )

                if (textStickerUIState.isVisibleTextField) {
                    EditTextStickerLayer(
                        modifier = Modifier.fillMaxSize(),
                        onEditText = {
                            freeStyleViewModel.hideEditTextSticker()
                            stickerView.replace(
                                TextSticker(
                                    stickerView.context,
                                    it
                                )
                            )
                        },
                        editTextProperties = textStickerUIState.editTextProperties
                    )
                }
            }
        }

        if (showBottomSheetSaveImage) {
            ExportImageBottomSheet(
                pathBitmap = pathBitmap,
                onDismissRequest = {
                    showBottomSheetSaveImage = false
                },
                onDownload = {
                    if (pathBitmap.isNotEmpty()) {
                        scope.launch {
                            try {
                                val bitmap = pathBitmap.toBitmap() ?: return@launch
                                val bitmapMark =
                                    FileUtil.addDiagonalWatermark(bitmap, "COLLAGE MAKER", 25)
                                val uriMark =
                                    FileUtil.saveImageToStorageWithQuality(
                                        context = context,
                                        quality = it,
                                        bitmap = bitmapMark
                                    )
                                onDownloadSuccess.invoke(
                                    ExportImageData(
                                        pathUriMark = uriMark?.toString(),
                                        pathBitmapOriginal = pathBitmap,
                                        quality = it
                                    )
                                )
                            } catch (ex: Throwable) {
                                Toast.makeText(
                                    context,
                                    "Error ${ex.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    } else {
                        Toast.makeText(context, "Save Image Error", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }

        DeleteImageDialog(
            isVisible = showDeleteDialog,
            onDelete = {
                deleteImageIndex?.let { index ->
                    if (currentUris.size > 1 && index < currentUris.size) {
                        vm.removeImageUri(index)
                        selectedImageIndex = null
                        vm.triggerUnselectAllImages()
                        val newCount = (currentUris.size - 1).coerceAtLeast(1)
                        vm.load(newCount)
                    }
                }
                showDeleteDialog = false
                deleteImageIndex = null
            },
            onCancel = {
                showDeleteDialog = false
                deleteImageIndex = null
            },
            onDismiss = {
                showDeleteDialog = false
                deleteImageIndex = null
            }
        )

        DiscardChangesDialog(
            isVisible = showDiscardDialog,
            onDiscard = {
                onBack()
            },
            onCancel = {
                vm.hideDiscardDialog()
            }
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun CollageScreenPreview() {
    val mockUris = listOf(Uri.EMPTY, Uri.EMPTY, Uri.EMPTY)

    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0xFF2C2C2E))
    ) {
        Column(Modifier.fillMaxSize()) {
            FeaturePhotoHeader(
                onBack = {},
                onUndo = {},
                onRedo = {},
                onSave = {},
                canUndo = false,
                canRedo = false
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                val templateToUse = CollageTemplates.LEFT_BIG_RIGHT_2
                CollagePreview(
                    images = mockUris,
                    template = templateToUse,
                    gap = 6.dp,
                    corner = 12.dp,
                    modifier = Modifier.fillMaxSize()
                )
            }

            FeatureBottomTools(
                tools = toolsCollage,
                onToolClick = {}
            )
        }
    }
}