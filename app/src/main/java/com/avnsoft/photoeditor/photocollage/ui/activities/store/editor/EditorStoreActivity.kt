package com.avnsoft.photoeditor.photocollage.ui.activities.store.editor

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.avnsoft.photoeditor.photocollage.data.model.template.TemplateModel
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.FeaturePhotoHeader
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.tools.CollageTool
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.tools.FeatureBottomTools
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.EditorActivity
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.crop.ToolInput
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.crop.ToolInput.TYPE
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.filter.FilterActivity
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.initEditorLib
import androidx.core.net.toUri
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.sticker.lib.DrawableSticker
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.sticker.lib.Sticker
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.sticker.lib.StickerView
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.text_sticker.edittext.EditTextStickerActivity
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.text_sticker.lib.AddTextProperties
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.text_sticker.lib.TextSticker
import com.avnsoft.photoeditor.photocollage.ui.activities.export_image.ExportImageBottomSheet
import com.avnsoft.photoeditor.photocollage.ui.activities.export_image.ExportImageData
import com.avnsoft.photoeditor.photocollage.ui.activities.export_image.ExportImageResultActivity
import com.avnsoft.photoeditor.photocollage.ui.activities.freestyle.StickerFooterTool
import com.avnsoft.photoeditor.photocollage.ui.activities.freestyle.TextStickerFooterTool
import com.avnsoft.photoeditor.photocollage.ui.activities.freestyle.lib.FreeStyleSticker
import com.avnsoft.photoeditor.photocollage.ui.activities.freestyle.lib.FreeStyleStickerView
import com.avnsoft.photoeditor.photocollage.ui.activities.store.StoreActivity
import com.avnsoft.photoeditor.photocollage.ui.activities.store.StoreActivityInput
import com.avnsoft.photoeditor.photocollage.ui.activities.store.tab.template.detail.TemplateDetailActivity
import com.avnsoft.photoeditor.photocollage.ui.activities.store.tab.template.detail.TemplateDetailInput
import com.avnsoft.photoeditor.photocollage.ui.dialog.DiscardChangesDialog
import com.avnsoft.photoeditor.photocollage.ui.theme.BackgroundGray
import com.avnsoft.photoeditor.photocollage.ui.theme.LoadingScreen
import com.avnsoft.photoeditor.photocollage.utils.FileUtil
import com.avnsoft.photoeditor.photocollage.utils.FileUtil.toBitmap
import com.avnsoft.photoeditor.photocollage.utils.FileUtil.toFile
import com.avnsoft.photoeditor.photocollage.utils.getInput
import com.basesource.base.ui.base.BaseActivity
import com.basesource.base.ui.base.IScreenData
import com.basesource.base.utils.capturable
import com.basesource.base.utils.clickableWithAlphaEffect
import com.basesource.base.utils.fromJson
import com.basesource.base.utils.launchActivity
import com.basesource.base.utils.rememberCaptureController
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

data class ToolTemplateInput(
    @SerializedName("template") val template: TemplateModel?,
    @SerializedName("selectedImages") val selectedImages: Map<Int, String> = emptyMap() // Store as String to avoid Uri serialization issues
) : IScreenData

class EditorStoreActivity : BaseActivity() {

    private val viewmodel: EditorStoreViewModel by viewModel()
    private val screenInput: ToolTemplateInput? by lazy {
        intent.getInput()
    }

    private lateinit var stickerView: FreeStyleStickerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
        initEditorLib()
        // Convert String URIs back to Uri objects
        val selectedImagesUri = screenInput?.selectedImages?.mapValues {
            it.value.toUri()
        } ?: emptyMap()

        viewmodel.setTemplateData(
            template = screenInput?.template,
            selectedImages = selectedImagesUri
        )
        enableEdgeToEdge()
        setContent {
            LaunchedEffect(Unit) {
                viewmodel.navigation.collect { event ->
                    when (event) {
                        CollageTool.FILTER -> {
                            launchActivity(
                                toActivity = FilterActivity::class.java,
                                input = ToolInput(pathBitmap = viewmodel.pathBitmapResult),
                                callback = { result ->
                                    if (result.resultCode == RESULT_OK) {
                                        val pathBitmap =
                                            result.data?.getStringExtra(EditorActivity.PATH_BITMAP)
                                        viewmodel.updateBitmap(
                                            pathBitmap = pathBitmap,
                                        )
                                    }
                                }
                            )
                        }

                        CollageTool.STICKER -> {
                            viewmodel.showStickerTool()
                        }

                        CollageTool.TEXT -> {
                            viewmodel.showTextSticker()
                        }

                        CollageTool.REPLACE -> {
                            launchActivity(
                                toActivity = TemplateDetailActivity::class.java,
                                input = TemplateDetailInput(
                                    template = screenInput?.template,
                                    type = ToolInput.TYPE.BACK_AND_RETURN
                                )
                            ) {
                                if (it.resultCode == RESULT_OK) {
                                    val result = it.data?.getStringExtra("PATH")
                                        ?.fromJson<ToolTemplateInput>()
                                    val selectedImagesUri = result?.selectedImages?.mapValues {
                                        it.value.toUri()
                                    } ?: emptyMap()

                                    viewmodel.setTemplateData(
                                        template = result?.template,
                                        selectedImages = selectedImagesUri
                                    )
                                }
                            }
                        }

                        CollageTool.TEMPLATE -> {
                            launchActivity(
                                toActivity = StoreActivity::class.java,
                                input = StoreActivityInput(TYPE.BACK_AND_RETURN)
                            ) {
                                if (it.resultCode == RESULT_OK) {
                                    setResult(RESULT_OK)
                                    finish()
                                }
                            }
                        }

                        else -> {

                        }
                    }
                }
            }
            val uiState by viewmodel.uiState.collectAsStateWithLifecycle()

            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                EditorStoreScreen(
                    viewmodel = viewmodel,
                    stickerView = stickerView,
                    modifier = Modifier
                        .fillMaxSize(),
                    onBack = {
                        viewmodel.showDiscardDialog()
                    },
                    onToolClick = {
                        viewmodel.onToolClick(it)
                    },
                    onDownloadSuccess = {
                        launchActivity(
                            toActivity = ExportImageResultActivity::class.java,
                            input = it
                        )
                    }
                )

                DiscardChangesDialog(
                    isVisible = uiState.showDiscardDialog,
                    onDiscard = {
                        setResult(RESULT_OK)
                        finish()
                    },
                    onCancel = {
                        viewmodel.hideDiscardDialog()
                    }
                )
            }
        }
    }

    override fun onBackActivity() {
        viewmodel.showDiscardDialog()
    }

    private fun initView() {
        stickerView = FreeStyleStickerView(this)
        stickerView.setLocked(false)
        stickerView.setConstrained(true)
        stickerView.configDefaultIcons()
        stickerView.setOnStickerOperationListener(object : StickerView.OnStickerOperationListener {
            public override fun onTextStickerEdit(param1Sticker: Sticker) {
                if (param1Sticker is TextSticker) {
                    viewmodel.showEditTextSticker()
                    val intent = EditTextStickerActivity.newIntent(
                        this@EditorStoreActivity,
                        param1Sticker.getAddTextProperties()?.text
                    )
                    activityResultManager.launchActivity(intent, null) {
                        if (it.resultCode == Activity.RESULT_OK) {
                            val textResult =
                                it.data?.getStringExtra(EditTextStickerActivity.EXTRA_TEXT)
                                    .orEmpty()
                            val widthResult =
                                it.data?.getIntExtra(EditTextStickerActivity.EXTRA_WIDTH, 0)
                            val heightResult =
                                it.data?.getIntExtra(EditTextStickerActivity.EXTRA_HEIGHT, 0)

                            val paramAddTextProperties = param1Sticker.getAddTextProperties()
                                ?: AddTextProperties.defaultProperties
                            paramAddTextProperties.apply {
                                this.text = textResult
                                this.textWidth = widthResult ?: 0
                                this.textHeight = heightResult ?: 0
                            }
                            stickerView.replace(
                                TextSticker(
                                    stickerView.context,
                                    paramAddTextProperties
                                )
                            )
                            viewmodel.hideEditTextSticker()
                        }
                    }
                }
            }

            public override fun onStickerAdded(sticker: Sticker) {
                Log.d("stickerView", "onStickerAdded")
                if (sticker is TextSticker) {
                    stickerView.configDefaultIcons()
                } else if (sticker is DrawableSticker || sticker is FreeStyleSticker) {
                    stickerView.configStickerIcons()
                }
                stickerView.invalidate()
            }

            public override fun onStickerClicked(sticker: Sticker) {
            }

            public override fun onStickerDeleted(sticker: Sticker) {
            }

            public override fun onStickerDragFinished(sticker: Sticker) {
            }


            public override fun onStickerZoomFinished(sticker: Sticker) {
            }

            public override fun onTouchDownForBeauty(param1Float1: Float, param1Float2: Float) {
            }

            public override fun onTouchDragForBeauty(param1Float1: Float, param1Float2: Float) {
            }

            public override fun onTouchUpForBeauty(param1Float1: Float, param1Float2: Float) {
            }


            public override fun onStickerFlipped(sticker: Sticker) {
            }

            public override fun onStickerTouchOutside(param1Sticker: Sticker?) {
            }

            public override fun onStickerTouchedDown(sticker: Sticker) {
                Log.d("stickerView", "onStickerTouchedDown")
                viewmodel.triggerUnselectAllImages()

                stickerView.setShowFocus(true)
                if (sticker is TextSticker) {
                    stickerView.configDefaultIcons()
                } else if (sticker is DrawableSticker) {
                    stickerView.configStickerIcons()
                } else if (sticker is FreeStyleSticker) {
                }
                stickerView.swapLayers()
                stickerView.invalidate()
            }

            public override fun onStickerDoubleTapped(sticker: Sticker) {
            }
        })
    }
}

@Composable
fun EditorStoreScreen(
    modifier: Modifier = Modifier,
    stickerView: FreeStyleStickerView,
    viewmodel: EditorStoreViewModel,
    onBack: () -> Unit,
    onToolClick: (CollageTool) -> Unit,
    onDownloadSuccess: (ExportImageData) -> Unit
) {
    val uiState by viewmodel.uiState.collectAsStateWithLifecycle()
    val unselectAllImagesTrigger by viewmodel.unselectAllImagesTrigger.collectAsStateWithLifecycle()
    val captureController = rememberCaptureController()
    val scope = rememberCoroutineScope()
    var pathBitmap by remember { mutableStateOf("") }
    val context = LocalContext.current
    var showBottomSheetSaveImage by remember { mutableStateOf(false) }

    val networkUIState by viewmodel.networkUIState.collectAsStateWithLifecycle()
    Box(
        modifier
            .background(BackgroundGray)
            .navigationBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            FeaturePhotoHeader(
                onBack = onBack,
                onUndo = {
                    viewmodel.undo()
                },
                onRedo = {
                    viewmodel.redo()
                },
                isUndo = false,
                onSave = {
                    viewmodel.showLoading()
                    viewmodel.triggerUnselectAllImages()
                    stickerView.setShowFocus(false) {
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
                                viewmodel.hideLoading()
                            }
                        }
                    }
                },
                canUndo = uiState.canUndo,
                canRedo = uiState.canRedo
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(BackgroundGray)
                    .padding(top = 20.dp, bottom = 23.dp)
                    .clickableWithAlphaEffect {
                        viewmodel.triggerUnselectAllImages()
                    }
                    .onSizeChanged { layout ->
                        viewmodel.canvasSize = layout.toSize()
                        if (uiState.template == null) {
                            viewmodel.scaleBitmapToBox(layout.toSize())
                        }
                    }
                    .capturable(captureController),
                contentAlignment = Alignment.Center
            ) {
                if (uiState.template != null) {
                    TemplatePreview(
                        viewmodel = viewmodel,
                        stickerView = stickerView,
                        template = uiState.template,
                        icons = uiState.icons,
                        selectedImages = uiState.selectedImages,
                        imageTransforms = uiState.imageTransforms,
                        onImageTransformsChange = { transforms ->
                            viewmodel.updateImageTransforms(transforms)
                        },
                        unselectAllImagesTrigger = unselectAllImagesTrigger,
                        onOutsideClick = {
                            viewmodel.triggerUnselectAllImages()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        uiState = uiState
                    )
                } else {
                    uiState.bitmap?.let {
                        Image(
                            bitmap = it.asImageBitmap(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            alignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                        )
                    }
                }
            }


            FeatureBottomTools(
                tools = uiState.items,
                onToolClick = onToolClick
            )
        }
        if (networkUIState.isLoading) {
            LoadingScreen()
        }
        when {
            showBottomSheetSaveImage -> {
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
                                    val uriMark = FileUtil.saveImageToStorageWithQuality(
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
                                    )
                                        .show()
                                }
                            }
                        } else {
                            Toast.makeText(context, "Save Image Error", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }

            uiState.isShowStickerTool -> {
//                stickerView.setLocked(true)
                StickerFooterTool(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter),
                    stickerView = stickerView,
                    onCancel = {
                        stickerView.removeCurrentSticker()
                        stickerView.setLocked(false)
                        viewmodel.cancelSticker()
                    },
                    onApply = {
                        stickerView.setLocked(false)
                        viewmodel.applySticker(stickerView.getCurrentDrawableSticker())
                    }
                )
            }

            uiState.isShowTextStickerTool -> {
//                stickerView.setLocked(true)
                TextStickerFooterTool(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter),
                    stickerView = stickerView,
                    onCancel = {
                        if (!viewmodel.isEditTextSticker) {
                            stickerView.removeCurrentSticker()
                        }
                        stickerView.setLocked(false)
                        viewmodel.cancelTextSticker()
                    },
                    onApply = {
                        stickerView.setLocked(false)
                        stickerView.setShowFocus(true)
                        viewmodel.applyTextSticker()
                    },
                    onAddFirstText = {
                        if (uiState.isVisibleTextField) return@TextStickerFooterTool
                        stickerView.addSticker(
                            TextSticker(
                                stickerView.context,
                                it,

                                ),
                            Sticker.Position.TOP
                        )
                    },
                    addTextSticker = { font ->
                        if (viewmodel.isEditTextSticker) {
                            val properties = uiState.editTextProperties
                            properties.fontName = font.fontName
                            stickerView.replace(
                                TextSticker(
                                    stickerView.context,
                                    properties
                                )
                            )
                        } else {
                            stickerView.replace(
                                TextSticker(
                                    stickerView.context,
                                    font
                                )
                            )
                        }
                    },
                )
            }
        }
    }
}