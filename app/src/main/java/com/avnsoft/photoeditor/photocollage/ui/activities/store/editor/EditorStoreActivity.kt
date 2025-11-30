package com.avnsoft.photoeditor.photocollage.ui.activities.store.editor

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ExperimentalComposeApi
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.BackgroundLayer
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.CollageTool
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.FeatureBottomTools
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.FeaturePhotoHeader
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.ToolItem
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.EditorActivity
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.crop.ToolInput
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.filter.FilterActivity
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.initEditorLib
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.sticker.StickerActivity
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.text_sticker.TextStickerActivity
import com.avnsoft.photoeditor.photocollage.ui.activities.export_image.ExportImageBottomSheet
import com.avnsoft.photoeditor.photocollage.ui.activities.export_image.ExportImageData
import com.avnsoft.photoeditor.photocollage.ui.activities.export_image.ExportImageResultActivity
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.uriToBitmap
import com.avnsoft.photoeditor.photocollage.ui.dialog.DiscardChangesDialog
import com.avnsoft.photoeditor.photocollage.ui.theme.AppColor
import com.avnsoft.photoeditor.photocollage.ui.theme.AppStyle
import com.avnsoft.photoeditor.photocollage.utils.FileUtil
import com.avnsoft.photoeditor.photocollage.utils.FileUtil.toFile
import com.avnsoft.photoeditor.photocollage.utils.getInput
import com.basesource.base.ui.base.BaseActivity
import com.basesource.base.utils.capturable
import com.basesource.base.utils.clickableWithAlphaEffect
import com.basesource.base.utils.launchActivity
import com.basesource.base.utils.rememberCaptureController
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

private fun String?.toBitmap(): android.graphics.Bitmap? {
    return this?.let { android.graphics.BitmapFactory.decodeFile(it) }
}

class EditorStoreActivity : BaseActivity() {

    private val viewmodel: EditorStoreViewModel by viewModel()
    private val screenInput: ToolInput? by lazy {
        intent.getInput()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initEditorLib()
        viewmodel.setPathBitmap(
            pathBitmap = screenInput?.pathBitmap,
            bitmap = screenInput?.pathBitmap.uriToBitmap(this),
            tool = null
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
                                            bitmap = pathBitmap.toBitmap()
                                        )
                                    }
                                }
                            )
                        }

                        CollageTool.STICKER -> {
                            launchActivity(
                                toActivity = StickerActivity::class.java,
                                input = ToolInput(pathBitmap = viewmodel.pathBitmapResult),
                                callback = { result ->
                                    if (result.resultCode == RESULT_OK) {
                                        val pathBitmap =
                                            result.data?.getStringExtra(EditorActivity.PATH_BITMAP)
                                        viewmodel.updateBitmap(
                                            pathBitmap = pathBitmap,
                                            bitmap = pathBitmap.toBitmap()
                                        )
                                    }
                                }
                            )
                        }

                        CollageTool.TEXT -> {
                            launchActivity(
                                toActivity = TextStickerActivity::class.java,
                                input = ToolInput(pathBitmap = viewmodel.pathBitmapResult),
                                callback = { result ->
                                    if (result.resultCode == RESULT_OK) {
                                        val pathBitmap =
                                            result.data?.getStringExtra(EditorActivity.PATH_BITMAP)
                                        viewmodel.updateBitmap(
                                            pathBitmap = pathBitmap,
                                            bitmap = pathBitmap.toBitmap()
                                        )
                                    }
                                }
                            )
                        }

                        CollageTool.REPLACE -> {
                            finish()
                        }

                        CollageTool.TEMPLATE -> {
                            // TODO: Handle template
                        }

                        else -> {

                        }
                    }
                }
            }
            Scaffold(
                containerColor = AppColor.White
            ) { inner ->
                val uiState by viewmodel.uiState.collectAsStateWithLifecycle()

                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    EditorStoreScreen(
                        viewmodel = viewmodel,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(
                                bottom = inner.calculateBottomPadding()
                            ),
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
                            finish()
                        },
                        onCancel = {
                            viewmodel.hideDiscardDialog()
                        }
                    )
                }
            }
        }
    }

    override fun onBackPressed() {
        viewmodel.showDiscardDialog()
    }
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalComposeApi::class)
@Composable
fun EditorStoreScreen(
    modifier: Modifier = Modifier,
    viewmodel: EditorStoreViewModel,
    onBack: () -> Unit,
    onToolClick: (CollageTool) -> Unit,
    onDownloadSuccess: (ExportImageData) -> Unit
) {
    val uiState by viewmodel.uiState.collectAsStateWithLifecycle()
    val captureController = rememberCaptureController()
    val scope = rememberCoroutineScope()
    var pathBitmap by remember { mutableStateOf("") }
    val context = LocalContext.current
    var showBottomSheetSaveImage by remember { mutableStateOf(false) }

    Box(modifier) {
        Column(modifier = Modifier.fillMaxSize()) {
            FeaturePhotoHeader(
                onBack = onBack,
                onUndo = {
                    viewmodel.undo()
                },
                onRedo = {
                    viewmodel.redo()
                },
                onSave = {
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
                },
                canUndo = uiState.canUndo,
                canRedo = uiState.canRedo
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(top = 20.dp, bottom = 23.dp)
                    .capturable(captureController)
            ) {
                BackgroundLayer(
                    backgroundSelection = uiState.backgroundColor,
                    modifier = Modifier
                        .fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .aspectRatio(1f)
                        .onSizeChanged { layout ->
                            viewmodel.canvasSize = layout.toSize()
                            viewmodel.scaleBitmapToBox(layout.toSize())
                        }
                ) {
                    uiState.originBitmap?.let {
                        Image(
                            bitmap = it.asImageBitmap(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                        )
                    }
                    uiState.bitmap?.let {
                        Image(
                            bitmap = it.asImageBitmap(),
                            contentDescription = null,
                            contentScale = ContentScale.None,
                            alignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxSize()
                        )
                    }
                }
            }

            FeatureBottomTools(
                tools = uiState.items,
                onToolClick = onToolClick
            )
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