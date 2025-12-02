package com.avnsoft.photoeditor.photocollage.ui.activities.collage.components

import android.app.Activity.RESULT_OK
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.BackHandler
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.avnsoft.photoeditor.photocollage.R
import com.avnsoft.photoeditor.photocollage.data.model.collage.CollageState
import com.avnsoft.photoeditor.photocollage.data.model.collage.CollageTemplate
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.CollageTemplates
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.CollageViewModel
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.copyImageToAppStorage
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.crop.CropActivity
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.crop.ToolInput
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.text_sticker.lib.TextSticker
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.toBitmap
import com.avnsoft.photoeditor.photocollage.ui.activities.export_image.ExportImageBottomSheet
import com.avnsoft.photoeditor.photocollage.ui.activities.export_image.ExportImageData
import com.avnsoft.photoeditor.photocollage.ui.activities.freestyle.EditTextStickerLayer
import com.avnsoft.photoeditor.photocollage.ui.activities.freestyle.FreeStyleStickerComposeView
import com.avnsoft.photoeditor.photocollage.ui.activities.freestyle.FreeStyleViewModel
import com.avnsoft.photoeditor.photocollage.ui.activities.freestyle.StickerFooterTool
import com.avnsoft.photoeditor.photocollage.ui.activities.freestyle.TextStickerFooterTool
import com.avnsoft.photoeditor.photocollage.ui.activities.freestyle.lib.FreeStyleStickerView
import com.avnsoft.photoeditor.photocollage.ui.activities.main.MainActivity
import com.avnsoft.photoeditor.photocollage.ui.dialog.DeleteImageDialog
import com.avnsoft.photoeditor.photocollage.ui.dialog.DiscardChangesDialog
import com.avnsoft.photoeditor.photocollage.ui.theme.Background2
import com.avnsoft.photoeditor.photocollage.ui.theme.BackgroundWhite
import com.avnsoft.photoeditor.photocollage.utils.FileUtil
import com.avnsoft.photoeditor.photocollage.utils.FileUtil.toFile
import com.basesource.base.ui.base.BaseActivity
import com.basesource.base.utils.capturable
import com.basesource.base.utils.launchActivity
import com.basesource.base.utils.rememberCaptureController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val MAX_PHOTOS = 10

private fun aspectRatioFor(ratio: String?): Float? = when (ratio) {
    "1:1" -> 1f
    "4:5" -> 4f / 5f
    "5:4" -> 5f / 4f
    "3:4" -> 3f / 4f
    "Original" -> null
    else -> null
}

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

    // Intercept back press
    BackHandler {
        vm.showDiscardDialog()
    }

    LaunchedEffect(uris, collageState.imageUris) {
        if (collageState.imageUris.isEmpty() && uris.isNotEmpty()) {
            vm.setImageUris(context, uris)
        }
    }

    val currentUris = collageState.imageUris.ifEmpty { uris }
    val canAddPhoto = currentUris.size < MAX_PHOTOS

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

    val topMargin = collageState.topMargin
    val columnMargin = collageState.columnMargin
    val cornerRadius = collageState.cornerRadius
    val ratio = collageState.ratio
    val templateId = collageState.templateId

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

    val aspectRatioValue = remember(ratio) { aspectRatioFor(ratio) }

    val textStickerUIState by freeStyleViewModel.uiState.collectAsStateWithLifecycle()


    suspend fun resetImageTransforms(
        template: CollageTemplate,
        canvasWidth: Float,
        canvasHeight: Float,
        topMarginValue: Float = 0f
    ) {
        val topMarginPx = topMarginValue * 0.2f * canvasHeight
        val leftMarginPx = topMarginValue * 0.2f * canvasWidth
        val rightMarginPx = topMarginValue * 0.2f * canvasWidth
        val bottomMarginPx = topMarginValue * 0.2f * canvasHeight

        val effectiveCanvasWidth = canvasWidth - leftMarginPx - rightMarginPx
        val effectiveCanvasHeight = canvasHeight - topMarginPx - bottomMarginPx

        val initialTransforms = ImageTransformCalculator.calculateInitialTransformsFromTemplate(
            context = context,
            template = template,
            images = currentUris,
            canvasWidth = effectiveCanvasWidth,
            canvasHeight = effectiveCanvasHeight
        )
        vm.updateImageTransforms(initialTransforms)
        vm.confirmImageTransformChanges()
    }

    fun clearAllSheets() {
        showGridsSheet = false
        showRatioSheet = false
        showBackgroundSheet = false
        showFrameSheet = false
        showStickerSheet = false
        showTextSheet = false
    }

    fun handleToolClick(tool: CollageTool) {
        when (tool) {
            CollageTool.GRIDS -> {
                clearAllSheets()
                showGridsSheet = true
            }

            CollageTool.RATIO -> {
                clearAllSheets()
                showRatioSheet = true
            }

            CollageTool.BACKGROUND -> {
                clearAllSheets()
                showBackgroundSheet = true
            }

            CollageTool.FRAME -> {
                clearAllSheets()
                showFrameSheet = true
            }

            CollageTool.STICKER -> {
                clearAllSheets()
                showStickerSheet = true
            }

            CollageTool.TEXT -> {
                clearAllSheets()
                showTextSheet = true
            }

            CollageTool.ADD_PHOTO -> {
                if (canAddPhoto) {
                    clearAllSheets()
                    launcher.launch("image/*")
                }
            }

            else -> {
                clearAllSheets()
            }
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
                Toast.makeText(context, R.string.copy, Toast.LENGTH_SHORT).show()
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

            ImageEditAction.ROTATE -> {
                vm.rotateImage(context, index)
            }

            ImageEditAction.FLIP_HORIZONTAL -> {
                vm.flipImageHorizontal(context, index)
            }

            ImageEditAction.FLIP_VERTICAL -> {
                vm.flipImageVertical(context, index)
            }

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
                stickerView.setShowFocus(false)
                stickerView.post {
                    scope.launch {
                        try {
                            val bitmap = captureController.toImageBitmap().asAndroidBitmap()
                            pathBitmap = bitmap.toFile(context)
                            showBottomSheetSaveImage = true
                        } catch (ex: Throwable) {
                            Toast.makeText(context, "Error ${ex.message}", Toast.LENGTH_SHORT)
                                .show()
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
                }
                .capturable(captureController)
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(top = 80.dp, bottom = 175.dp)
                    .then(
                        if (aspectRatioValue != null) {
                            Modifier.aspectRatio(aspectRatioValue)
                        } else {
                            Modifier
                        }
                    )
                    .background(BackgroundWhite)
                    .pointerInput(selectedImageIndex) {
                        detectTapGestures {
                            if (selectedImageIndex != null) {
                                selectedImageIndex = null
                                vm.triggerUnselectAllImages()
                            }
                        }
                    }
            ) {
                val templateToUse =
                    templateId ?: CollageTemplates.defaultFor(currentUris.size.coerceAtLeast(1))
                val gapValue = (1 + columnMargin * 19).dp
                val cornerValue = (1 + cornerRadius * 19).dp

                val canvasWidth = constraints.maxWidth.toFloat()
                val canvasHeight = constraints.maxHeight.toFloat()

                LaunchedEffect(templateToUse.id, currentUris.size) {
                    if (templateToUse.id.isNotEmpty() &&
                        currentUris.isNotEmpty() &&
                        canvasWidth > 0 &&
                        canvasHeight > 0
                    ) {
                        vm.updateImageTransforms(emptyMap())
                        delay(500)
                        resetImageTransforms(templateToUse, canvasWidth, canvasHeight, topMargin)
                    }
                }

                Box(modifier = Modifier.wrapContentSize()) {
                    CollagePreview(
                        images = currentUris,
                        template = templateToUse,
                        gap = gapValue,
                        corner = cornerValue,
                        backgroundSelection = collageState.backgroundSelection,
                        imageTransforms = collageState.imageTransforms,
                        topMargin = topMargin,
                        imageBitmaps = collageState.imageBitmaps,
                        onImageClick = { index, _ ->
                            if (isSwapMode && selectedImageIndex != null && selectedImageIndex != index) {
                                val firstIndex = selectedImageIndex!!
                                if (firstIndex < currentUris.size && index < currentUris.size) {
                                    val newUris = currentUris.toMutableList().apply {
                                        val temp = this[firstIndex]
                                        this[firstIndex] = this[index]
                                        this[index] = temp
                                    }
                                    vm.setImageUris(context, newUris)
                                    isSwapMode = false
                                }
                            } else {
                                selectedImageIndex =
                                    if (selectedImageIndex == index) null else index
                                if (selectedImageIndex == null) {
                                    isSwapMode = false
                                }
                            }
                        },
                        onImageTransformsChange = { transforms ->
                            vm.updateImageTransforms(transforms)
                            vm.confirmImageTransformChanges()
                        },
                        unselectAllTrigger = unselectAllImagesTrigger,
                        onOutsideClick = {
                            selectedImageIndex = null
                            vm.triggerUnselectAllImages()
                        }
                    )

                    collageState.frameSelection
                        ?.takeIf { it is FrameSelection.Frame }
                        ?.let { frame ->
                            val data = frame as FrameSelection.Frame
                            val url =
                                if (data.item.urlThumb?.startsWith("http://") == true ||
                                    data.item.urlThumb?.startsWith("https://") == true
                                ) {
                                    data.item.urlThumb
                                } else {
                                    "${data.urlRoot}${data.item.urlThumb}"
                                }

                            AsyncImage(
                                model = ImageRequest.Builder(context).data(url).build(),
                                contentDescription = "",
                                contentScale = ContentScale.FillBounds,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                }
            }

            FreeStyleStickerComposeView(
                modifier = Modifier.fillMaxSize(),
                view = stickerView
            )

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
                        disabledTools = if (!canAddPhoto) {
                            setOf(CollageTool.ADD_PHOTO)
                        } else {
                            emptySet()
                        }
                    )
                }

                if (showGridsSheet) {
                    GridsSheet(
                        templates = templates,
                        selectedTemplate = templateId,
                        onTemplateSelect = { template -> vm.selectTemplate(template) },
                        onClose = { showGridsSheet = false },
                        onConfirm = {
                            vm.confirmChanges()
                            showGridsSheet = false
                        },
                        topMargin = topMargin,
                        onTopMarginChange = { vm.updateTopMargin(it) },
                        columnMargin = columnMargin,
                        onColumnMarginChange = { vm.updateColumnMargin(it) },
                        cornerRadius = cornerRadius,
                        onCornerRadiusChange = { vm.updateCornerRadius(it) },
                        imageCount = currentUris.size.coerceAtLeast(1),
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                    )
                }

                if (showRatioSheet) {
                    RatioSheet(
                        selectedRatio = ratio,
                        onRatioSelect = { aspect -> vm.updateRatio(aspect.label) },
                        onClose = {
                            vm.cancelRatioChanges()
                            showRatioSheet = false
                        },
                        onConfirm = {
                            vm.confirmChanges()
                            showRatioSheet = false
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                    )
                }

                if (showBackgroundSheet) {
                    BackgroundSheet(
                        selectedBackgroundSelection = collageState.backgroundSelection,
                        onBackgroundSelect = { _, selection ->
                            vm.updateBackground(selection)
                        },
                        onClose = {
                            vm.cancelBackgroundChanges()
                            showBackgroundSheet = false
                        },
                        onConfirm = {
                            vm.confirmChanges()
                            showBackgroundSheet = false
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                    )
                }

                if (showFrameSheet) {
                    FrameSheet(
                        selectedFrameSelection = collageState.frameSelection,
                        onFrameSelect = { selection ->
                            vm.updateFrame(selection)
                        },
                        onClose = {
                            vm.cancelFrameChanges()
                            showFrameSheet = false
                        },
                        onConfirm = {
                            vm.confirmChanges()
                            showFrameSheet = false
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                    )
                }

                when {
                    showStickerSheet -> {
//                        stickerView.setLocked(true)
                        StickerFooterTool(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight(),
                            stickerView = stickerView,
                            onCancel = {
                                stickerView.removeCurrentSticker()
                                stickerView.setLocked(false)
                                showStickerSheet = false
                            },
                            onApply = {
                                stickerView.setLocked(false)
                                vm.confirmStickerChanges()
                                showStickerSheet = false
                            }
                        )
                    }

                    showTextSheet -> {
//                        stickerView.setLocked(true)
                        TextStickerFooterTool(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight(),
                            stickerView = stickerView,
                            onCancel = {
                                if (!freeStyleViewModel.isEditTextSticker){
                                    stickerView.removeCurrentSticker()
                                }
                                stickerView.setLocked(false)
                                showTextSheet = false
                            },
                            onApply = {
                                stickerView.setLocked(false)
                                vm.confirmStickerChanges()
                                showTextSheet = false
                            },
                            onAddFirstText = {
                                if (textStickerUIState.isVisibleTextField) return@TextStickerFooterTool
                                stickerView.addSticker(
                                    TextSticker(
                                        stickerView.context,
                                        it
                                    ),
                                    com.avnsoft.photoeditor.photocollage.ui.activities.editor.sticker.lib.Sticker.Position.TOP
                                )
                            },
                            addTextSticker = { font ->
                                stickerView.replace(
                                    TextSticker(
                                        stickerView.context,
                                        font
                                    )
                                )
                            }
                        )
                    }

                    else -> Unit
                }

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