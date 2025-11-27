package com.avnsoft.photoeditor.photocollage.ui.activities.collage.components

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.avnsoft.photoeditor.photocollage.data.model.collage.CollageTemplate
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.CollageTemplates
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.CollageViewModel
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.sticker.lib.Sticker
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.sticker.lib.StickerLib
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.text_sticker.lib.TextSticker
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.text_sticker.lib.TextStickerLib
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.toBitmap
import com.avnsoft.photoeditor.photocollage.ui.activities.export_image.ExportImageBottomSheet
import com.avnsoft.photoeditor.photocollage.ui.activities.export_image.ExportImageData
import com.avnsoft.photoeditor.photocollage.ui.activities.freestyle.EditTextStickerLayer
import com.avnsoft.photoeditor.photocollage.ui.activities.freestyle.FreeStyleStickerComposeView
import com.avnsoft.photoeditor.photocollage.ui.activities.freestyle.FreeStyleViewModel
import com.avnsoft.photoeditor.photocollage.ui.activities.freestyle.StickerFooterTool
import com.avnsoft.photoeditor.photocollage.ui.activities.freestyle.TextStickerFooterTool
import com.avnsoft.photoeditor.photocollage.ui.activities.freestyle.lib.FreeStyleStickerView
import com.avnsoft.photoeditor.photocollage.ui.theme.Background2
import com.avnsoft.photoeditor.photocollage.ui.theme.BackgroundWhite
import com.avnsoft.photoeditor.photocollage.utils.FileUtil
import com.avnsoft.photoeditor.photocollage.utils.FileUtil.toFile
import dev.shreyaspatil.capturable.capturable
import dev.shreyaspatil.capturable.controller.rememberCaptureController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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

    // State để lưu trữ danh sách uris (có thể thay đổi khi thêm ảnh)
    var currentUris by remember(uris) { mutableStateOf(uris) }

    // Giới hạn tối đa 10 ảnh
    val MAX_PHOTOS = 10
    val canAddPhoto = currentUris.size < MAX_PHOTOS

    // Launcher để chọn ảnh từ gallery
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { newUri ->
            // Kiểm tra giới hạn trước khi thêm
            if (currentUris.size < MAX_PHOTOS) {
                // Thêm ảnh mới vào danh sách
                currentUris = currentUris + newUri
                // Cập nhật số lượng ảnh và load lại templates
                // vm.load() sẽ tự động chọn grid đầu tiên
                val newCount = currentUris.size.coerceAtLeast(1)
                vm.load(newCount)
            }
        }
    }

    // Hàm helper để reset và tính lại image transforms khi đổi grids hoặc add photo
    // Hàm này sẽ được gọi từ LaunchedEffect
    suspend fun resetImageTransforms(
        template: CollageTemplate,
        canvasWidth: Float,
        canvasHeight: Float,
        topMargin: Float = 0f
    ) {
        // Áp dụng topMargin: giảm kích thước vùng bound tổng
        val topMarginPx = topMargin * 0.2f * canvasHeight
        val leftMarginPx = topMargin * 0.2f * canvasWidth
        val rightMarginPx = topMargin * 0.2f * canvasWidth
        val bottomMarginPx = topMargin * 0.2f * canvasHeight

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

    val captureController = rememberCaptureController()
    val scope = rememberCoroutineScope()
    var pathBitmap by remember { mutableStateOf("") }
    var showBottomSheetSaveImage by remember { mutableStateOf(false) }

    // Observe state from ViewModel
    val templates by vm.templates.collectAsState()
    val collageState by vm.collageState.collectAsState()
    val canUndo by vm.canUndo.collectAsState()
    val canRedo by vm.canRedo.collectAsState()
    val unselectAllImagesTrigger by vm.unselectAllImagesTrigger.collectAsState()
    var showGridsSheet by remember { mutableStateOf(false) }
    var showRatioSheet by remember { mutableStateOf(false) }
    var showBackgroundSheet by remember { mutableStateOf(false) }
    var showFrameSheet by remember { mutableStateOf(false) }
    var showStickerSheet by remember { mutableStateOf(false) }
    var showTextSheet by remember { mutableStateOf(false) }

    // Sticker state
    // Extract values from state
    val topMargin = collageState.topMargin
    val columnMargin = collageState.columnMargin
    val cornerRadius = collageState.cornerRadius
    val ratio = collageState.ratio
    val template = collageState.templateId
    LaunchedEffect(Unit) {
        vm.load(currentUris.size.coerceAtLeast(1))
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(Background2)
    ) {
        // Header
        FeaturePhotoHeader(
            onBack = onBack,
            onUndo = { vm.undo() },
            onRedo = { vm.redo() },
            onSave = {
                scope.launch {
                    try {
                        stickerView.setShowFocus(false)
                        val bitmapAsync = captureController.captureAsync()
                        val bitmap = bitmapAsync.await().asAndroidBitmap()
                        pathBitmap = bitmap.toFile(context)
                        showBottomSheetSaveImage = true
                    } catch (ex: Throwable) {
                        Toast.makeText(context, "Error ${ex.message}", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            },
            canUndo = canUndo && !showGridsSheet && !showRatioSheet,
            canRedo = canRedo && !showGridsSheet && !showRatioSheet
        )
        // Calculate aspect ratio from ratio string (e.g., "1:1" -> 1.0, "4:5" -> 0.8)
        val aspectRatioValue = remember(ratio) {
            when (ratio) {
                "Original" -> null // No aspect ratio constraint for Original
                "1:1" -> 1f
                "4:5" -> 4f / 5f
                "5:4" -> 5f / 4f
                "3:4" -> 3f / 4f
                else -> null
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
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
                    .capturable(captureController)

            )
            {
                val templateToUse = template
                    ?: CollageTemplates.defaultFor(currentUris.size.coerceAtLeast(1))
                // Map slider values to Dp
                val gapValue = (1 + columnMargin * 19).dp // columnMargin: 0-1 -> gap: 1-20dp
                val cornerValue = (1 + cornerRadius * 19).dp // cornerRadius: 0-1 -> corner: 1-20dp

                val canvasWidth = constraints.maxWidth.toFloat()
                val canvasHeight = constraints.maxHeight.toFloat()

                // Reset transforms khi đổi template hoặc thêm ảnh (KHÔNG reset khi topMargin thay đổi)
                LaunchedEffect(templateToUse.id, currentUris.size) {
                    if (templateToUse.id.isNotEmpty() && currentUris.isNotEmpty() && canvasWidth > 0 && canvasHeight > 0) {
                        // Clear transforms trước để trigger lại tính toán
                        vm.updateImageTransforms(emptyMap())
                        // Delay để đảm bảo template và images đã được cập nhật hoàn toàn
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
                        onImageClick = { uri ->
                            // Callback về path của image khi click
                            // TODO: Xử lý callback này (ví dụ: mở editor cho image này)
                        },
                        onImageTransformsChange = { transforms ->
                            // Lưu transforms vào ViewModel và confirm vào undo stack
                            vm.updateImageTransforms(transforms)
                            vm.confirmImageTransformChanges()
                        },
                        unselectAllTrigger = unselectAllImagesTrigger
                    )
                    collageState.frameSelection?.takeIf { it is FrameSelection.Frame }?.let { frame ->
                        val data = frame as FrameSelection.Frame
                        val url = if (data.item.urlThumb?.startsWith("http://") == true || data.item.urlThumb?.startsWith("https://") == true) {
                            data.item.urlThumb
                        } else {
                            "${data.urlRoot}${data.item.urlThumb}"
                        }
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(url)
                                .build(),
                            contentDescription = "",
                            contentScale = ContentScale.FillBounds,
                            modifier = Modifier.fillMaxSize()
                        )

                    }
                }
            }

            FreeStyleStickerComposeView(
                modifier = Modifier
                    .fillMaxSize(),
                view = stickerView
            )

            Box(modifier = Modifier.align(Alignment.BottomCenter)) {
                if (!showTextSheet) {
                    FeatureBottomTools(
                        tools = toolsCollage,
                        onToolClick = { tool ->
                            when (tool) {
                                CollageTool.GRIDS -> {
                                    showGridsSheet = true
                                    showTextSheet = false
                                    showRatioSheet = false
                                    showBackgroundSheet = false
                                    showFrameSheet = false
                                    showStickerSheet = false
                                }

                                CollageTool.RATIO -> {
                                    showRatioSheet = true
                                    showTextSheet = false
                                    showGridsSheet = false
                                    showBackgroundSheet = false
                                    showFrameSheet = false
                                    showStickerSheet = false
                                }

                                CollageTool.BACKGROUND -> {
                                    showBackgroundSheet = true
                                    showTextSheet = false
                                    showGridsSheet = false
                                    showRatioSheet = false
                                    showFrameSheet = false
                                    showStickerSheet = false

                                }

                                CollageTool.FRAME -> {
                                    showFrameSheet = true
                                    showTextSheet = false
                                    showGridsSheet = false
                                    showRatioSheet = false
                                    showBackgroundSheet = false
                                    showStickerSheet = false

                                }

                                CollageTool.STICKER -> {
                                    showStickerSheet = true
                                    showTextSheet = false
                                    showGridsSheet = false
                                    showRatioSheet = false
                                    showBackgroundSheet = false
                                    showFrameSheet = false
                                }

                                CollageTool.TEXT -> {
                                    showTextSheet = true
                                    showGridsSheet = false
                                    showRatioSheet = false
                                    showBackgroundSheet = false
                                    showFrameSheet = false
                                    showStickerSheet = false
                                }

                                CollageTool.ADD_PHOTO -> {
                                    if (canAddPhoto) {
                                        launcher.launch("image/*")
                                        showTextSheet = false
                                        showGridsSheet = false
                                        showRatioSheet = false
                                        showBackgroundSheet = false
                                        showFrameSheet = false
                                        showStickerSheet = false
                                    }
                                }

                                else -> {
                                    showTextSheet = false
                                    showGridsSheet = false
                                    showRatioSheet = false
                                    showBackgroundSheet = false
                                    showFrameSheet = false
                                    showStickerSheet = false
                                }
                            }
                        },
                        disabledTools = if (!canAddPhoto) setOf(CollageTool.ADD_PHOTO) else emptySet()
                    )
                }
                if (showGridsSheet) {
                    GridsSheet(
                        templates = templates,
                        selectedTemplate = template,
                        onTemplateSelect = { template ->
                            vm.selectTemplate(template)
                        },
                        onClose = { showGridsSheet = false },
                        onConfirm = { tab ->
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
                        selectedRatio = ratio, onRatioSelect = { aspect ->
                            vm.updateRatio(aspect.label)
                        }, onClose = {
                            vm.cancelRatioChanges()
                            showRatioSheet = false
                        }, onConfirm = {
                            vm.confirmChanges()
                            showRatioSheet = false
                        }, modifier = Modifier
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
                        stickerView.setLocked(true)
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
                        stickerView.setLocked(true)
                        TextStickerFooterTool(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight(),
                            stickerView = stickerView,
                            onCancel = {
                                stickerView.removeCurrentSticker()
                                stickerView.setLocked(false)
                                showTextSheet = false
                            },
                            onApply = {
                                stickerView.setLocked(false)
                                vm.confirmStickerChanges()
                                showTextSheet = false
                            },
                            onAddFirstText = {
                                stickerView.addSticker(
                                    TextSticker(
                                        stickerView.context,
                                        it,

                                        ),
                                    Sticker.Position.TOP
                                )
                            },
                            addTextSticker = { font ->
                                stickerView.replace(
                                    TextSticker(
                                        stickerView.context,
                                        font
                                    )
                                )
                            },
                        )
                    }


                    else -> {

                    }
                }

                val textStickerUIState by freeStyleViewModel.uiState.collectAsStateWithLifecycle()
                if (textStickerUIState.isVisibleTextField) {
                    EditTextStickerLayer(
                        modifier = Modifier
                            .fillMaxSize(),
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
                                    FileUtil.addDiagonalWatermark(bitmap, "COLLAGE MAKER", 25);
                                val uriMark = FileUtil.saveImageToStorageWithQuality(
                                    context = context,
                                    quality = it.value,
                                    bitmap = bitmapMark
                                )
                                onDownloadSuccess.invoke(
                                    ExportImageData(
                                        pathUriMark = uriMark?.toString(),
                                        pathBitmapOriginal = pathBitmap
                                    )
                                )
                            } catch (ex: Throwable) {
                                Toast.makeText(context, "Error ${ex.message}", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                    } else {
                        Toast.makeText(context, "Save Image Error", Toast.LENGTH_SHORT).show()
                    }

                }
            )
        }
    }
}


@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun CollageScreenPreview() {
    // Mock ViewModel state cho preview
    val mockUris = listOf(Uri.EMPTY, Uri.EMPTY, Uri.EMPTY)

    // Preview đơn giản không dùng ViewModel
    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0xFF2C2C2E))
    ) {
        Column(Modifier.fillMaxSize()) {
            // Header
            FeaturePhotoHeader(
                onBack = {}, onUndo = {}, onRedo = {}, onSave = {}, canUndo = false, canRedo = false
            )

            // Preview area
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

            // Bottom tools
            FeatureBottomTools(
                tools = toolsCollage, onToolClick = {})
        }
    }
}