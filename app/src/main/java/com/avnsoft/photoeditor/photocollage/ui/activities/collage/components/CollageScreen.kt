package com.avnsoft.photoeditor.photocollage.ui.activities.collage.components

import android.app.Activity
import android.content.ContextWrapper
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.avnsoft.photoeditor.photocollage.data.repository.StickerRepoImpl
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.CollageTemplates
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.CollageViewModel
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.sticker.StickerData
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.sticker.StickerToolPanel
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.sticker.StickerUIState
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.sticker.StickerViewCompose
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.sticker.lib.StickerView
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.text_sticker.TextStickerUIState
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.text_sticker.lib.AddTextProperties
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.text_sticker.lib.TextStickerLib
import com.avnsoft.photoeditor.photocollage.ui.theme.Background2
import com.avnsoft.photoeditor.photocollage.ui.theme.BackgroundWhite
import com.basesource.base.result.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun CollageScreen(
    uris: List<Uri>,
    vm: CollageViewModel,
    onBack: () -> Unit,
) {
    // Observe state from ViewModel
    val templates by vm.templates.collectAsState()
    val selected by vm.selected.collectAsState()
    val collageState by vm.collageState.collectAsState()
    val canUndo by vm.canUndo.collectAsState()
    val canRedo by vm.canRedo.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    var showGridsSheet by remember { mutableStateOf(false) }
    var showRatioSheet by remember { mutableStateOf(false) }
    var showBackgroundSheet by remember { mutableStateOf(false) }
    var showFrameSheet by remember { mutableStateOf(false) }
    var showStickerSheet by remember { mutableStateOf(false) }
    var showTextSheet by remember { mutableStateOf(false) }

    // Sticker state

    val stickerRepo: StickerRepoImpl = koinInject()
    var stickerUIState by remember { mutableStateOf(StickerUIState()) }
    var currentStickerData by remember { mutableStateOf<StickerData?>(null) }
    var currentTextData by remember { mutableStateOf<AddTextProperties?>(null) }

    // Extract values from state
    val topMargin = collageState.topMargin
    val columnMargin = collageState.columnMargin
    val cornerRadius = collageState.cornerRadius
    val ratio = collageState.ratio
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            currentStickerData = StickerData.StickerFromGallery(
                pathSticker = it.toString()
            )
        }
    }
    LaunchedEffect(Unit) {
        vm.load(uris.size.coerceAtLeast(1))
    }

    LaunchedEffect(collageState.stickerBitmapPath) {
        try {
            currentStickerData = collageState.stickerBitmapPath?.takeIf { it.isNotEmpty() }?.let { path ->
                StickerData.StickerFromAsset(pathSticker = path)
            } ?: null
        } catch (e: Exception) {
            e.printStackTrace()
            currentStickerData = null
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Background2)
    ) {
        Column(Modifier.fillMaxSize()) {
            // Header
            FeaturePhotoHeader(
                onBack = onBack,
                onUndo = { vm.undo() },
                onRedo = { vm.redo() },
                onSave = { /* TODO */ },
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
                    .weight(1f)
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

            ) {
                val templateToUse = selected ?: templates.firstOrNull()
                ?: CollageTemplates.defaultFor(uris.size.coerceAtLeast(1))
                // Map slider values to Dp
                val gapValue = (1 + columnMargin * 19).dp // columnMargin: 0-1 -> gap: 1-20dp
                val cornerValue = (1 + cornerRadius * 19).dp // cornerRadius: 0-1 -> corner: 1-20dp

                CollagePreview(
                    images = uris,
                    template = templateToUse,
                    gap = gapValue,
                    corner = cornerValue,
                    backgroundSelection = collageState.backgroundSelection,
                    imageTransforms = collageState.imageTransforms,
                    onImageClick = { uri ->
                        // Callback về path của image khi click
                        // TODO: Xử lý callback này (ví dụ: mở editor cho image này)
                    },
                    onImageTransformsChange = { transforms ->
                        // Lưu transforms vào ViewModel và confirm vào undo stack
                        vm.updateImageTransforms(transforms)
                        vm.confirmImageTransformChanges()
                    }
                )
                currentStickerData?.let {
                    StickerViewCompose(
                        modifier = Modifier.fillMaxSize(),
                        input = it,
                    )
                }
            }

            // Bottom tools
            FeatureBottomTools(
                tools = toolsCollage,
                onToolClick = { tool ->
                    when (tool) {
                        CollageTool.GRIDS -> {
                            showGridsSheet = true
                            showRatioSheet = false
                            showFrameSheet = false
                            showTextSheet = false
                        }

                        CollageTool.RATIO -> {
                            showRatioSheet = true
                            showGridsSheet = false
                            showFrameSheet = false
                            showTextSheet = false
                        }

                        CollageTool.BACKGROUND -> {
                            showBackgroundSheet = true
                            showGridsSheet = false
                            showRatioSheet = false
                            showFrameSheet = false
                            showTextSheet = false

                        }

                        CollageTool.FRAME -> {
                            showFrameSheet = true
                            showGridsSheet = false
                            showRatioSheet = false
                            showBackgroundSheet = false
                            showStickerSheet = false
                            showTextSheet = false

                        }

                        CollageTool.STICKER -> {
                            showStickerSheet = true
                            showGridsSheet = false
                            showRatioSheet = false
                            showBackgroundSheet = false
                            showFrameSheet = false
                            showTextSheet = false
                        }

                        CollageTool.TEXT -> {
                            showTextSheet = true
                            showStickerSheet = false
                            showGridsSheet = false
                            showRatioSheet = false
                            showBackgroundSheet = false
                            showFrameSheet = false
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
                }
            )
        }

        if (showTextSheet) {
            TextStickerLib()
        }
        if (showGridsSheet) {
            GridsSheet(
                templates = templates,
                selectedTemplate = selected,
                onTemplateSelect = { template ->
                    vm.selectTemplate(template)
                },
                onClose = { showGridsSheet = false },
                onConfirm = { tab ->
                    vm.confirmGridsChanges(tab)
                    showGridsSheet = false
                },
                topMargin = topMargin,
                onTopMarginChange = { vm.updateTopMargin(it) },
                columnMargin = columnMargin,
                onColumnMarginChange = { vm.updateColumnMargin(it) },
                cornerRadius = cornerRadius,
                onCornerRadiusChange = { vm.updateCornerRadius(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .align(Alignment.BottomCenter)
            )
        }

        // Ratio Sheet (hiện khi click Ratio tool)
        if (showRatioSheet) {
            RatioSheet(
                selectedRatio = ratio,
                onRatioSelect = { aspect ->
                    vm.updateRatio(aspect.label)
                },
                onClose = {
                    vm.cancelRatioChanges()
                    showRatioSheet = false
                },
                onConfirm = {
                    vm.confirmRatioChanges()
                    showRatioSheet = false
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .align(Alignment.BottomCenter)
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
                    vm.confirmBackgroundChanges()
                    showBackgroundSheet = false
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .align(Alignment.BottomCenter)
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
                    vm.confirmFrameChanges()
                    showFrameSheet = false
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .align(Alignment.BottomCenter)
            )
        }

        if (showStickerSheet) {
            if(stickerUIState.currentTab == null)
                stickerUIState = stickerUIState.copy(currentTab = stickerUIState.stickers.firstOrNull())
            StickerToolPanel(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .align(Alignment.BottomCenter),
                uiState = stickerUIState,
                onTabSelected = { tab ->
                    stickerUIState = stickerUIState.copy(currentTab = tab)
                },
                onCancel = {
                    showStickerSheet = false
                    currentStickerData = null
                },
                onApply = {
                    showStickerSheet = false
                    vm.updateStickerBitmapPath(
                        when (currentStickerData) {
                            is StickerData.StickerFromAsset -> (currentStickerData as StickerData.StickerFromAsset).pathSticker
                            is StickerData.StickerFromGallery -> (currentStickerData as StickerData.StickerFromGallery).pathSticker
                            null -> ""
                        }
                    )
                },
                onStickerSelected = { path ->
                    currentStickerData = StickerData.StickerFromAsset(path)
                },
                onAddStickerFromGallery = {
                    launcher.launch("image/*")
                }
            )
        }
    }

    // Initialize sticker emoji tabs when sticker sheet is shown
    LaunchedEffect(showStickerSheet) {
        coroutineScope.launch(Dispatchers.IO) {
            if (showStickerSheet && stickerUIState.stickers.isEmpty()) {
                val emojiTabs = stickerRepo.getStickers()
                when (emojiTabs) {
                    is Result.Success -> {
                        stickerUIState = stickerUIState.copy(stickers = emojiTabs.data)
                    }

                    else -> {

                    }
                }
            }
        }
    }
}

private fun android.content.Context.findActivity(): Activity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
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
                onBack = {},
                onUndo = {},
                onRedo = {},
                onSave = {},
                canUndo = false,
                canRedo = false
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
                tools = toolsCollage,
                onToolClick = {}
            )
        }
    }
}