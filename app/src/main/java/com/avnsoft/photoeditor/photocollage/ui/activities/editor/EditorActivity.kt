package com.avnsoft.photoeditor.photocollage.ui.activities.editor

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.avnsoft.photoeditor.photocollage.BaseApplication
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.BackgroundLayer
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.BackgroundSelection
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.CollageTool
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.FeatureBottomTools
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.FeaturePhotoHeader
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.adjust.AdjustActivity
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.ai_enhance.AIEnhanceActivity
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.background.BackgroundActivity
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.blur.BlurActivity
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.blur.BlurView
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.blur.tabShape
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.crop.CropActivity
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.crop.ToolInput
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.draw.DrawActivity
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.filter.FilterActivity
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.remove_background.RemoveBackgroundActivity
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.sticker.StickerActivity
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.text_sticker.TextStickerActivity
import com.avnsoft.photoeditor.photocollage.ui.activities.export_image.ExportImageBottomSheet
import com.avnsoft.photoeditor.photocollage.ui.activities.export_image.ExportImageData
import com.avnsoft.photoeditor.photocollage.ui.activities.export_image.ExportImageResultActivity
import com.avnsoft.photoeditor.photocollage.ui.activities.main.MainActivity
import com.avnsoft.photoeditor.photocollage.ui.dialog.DiscardChangesDialog
import com.avnsoft.photoeditor.photocollage.ui.theme.AppColor
import com.avnsoft.photoeditor.photocollage.utils.FileUtil
import com.avnsoft.photoeditor.photocollage.utils.FileUtil.toFile
import com.avnsoft.photoeditor.photocollage.utils.getInput
import com.basesource.base.ui.base.BaseActivity
import com.basesource.base.ui.base.IScreenData
import com.basesource.base.utils.launchActivity
import dev.shreyaspatil.capturable.capturable
import dev.shreyaspatil.capturable.controller.rememberCaptureController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.wysaid.nativePort.CGENativeLibrary
import org.wysaid.nativePort.CGENativeLibrary.LoadImageCallback
import java.io.File
import java.io.IOException

data class EditorInput(
    val pathBitmap: String? = null,
    val tool: CollageTool? = null,
) : IScreenData

fun String?.toBitmap(context: Context? = null): Bitmap? {
//    val imageUri = this?.toUri()
//    val bitmap = imageUri?.toBitmap(context)
    val bitmap = BitmapFactory.decodeFile(this)
    return bitmap
}

fun String?.uriToBitmap(context: Context): Bitmap? {
    val imageUri = this?.toUri()
    val bitmap = imageUri?.toBitmap(context)
    return bitmap
}

class EditorActivity : BaseActivity() {

    private val viewmodel: EditorViewModel by viewModel()

    private val screenInput: EditorInput? by lazy {
        intent.getInput()
    }

    val backgroundSelection: BackgroundSelection? by lazy {
        val json = intent.getStringExtra("backgroundSelection")
        json?.let {
            Json.decodeFromString<BackgroundSelection>(json)
        } ?: run {
            null
        }
    }

    private lateinit var blurView: BlurView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initEditorLib()
        blurView = BlurView(this)
        blurView.tabShape()
        viewmodel.setPathBitmap(
            pathBitmap = screenInput?.pathBitmap,
            bitmap = screenInput?.pathBitmap.uriToBitmap(this),
            tool = screenInput?.tool,
            backgroundSelection = backgroundSelection
        )
        enableEdgeToEdge()
        setContent {
            LaunchedEffect(Unit) {
                viewmodel.navigation.collect { event ->
                    when (event) {
                        CollageTool.SQUARE_OR_ORIGINAL -> {
                            viewmodel.toggleOriginal()
                        }

                        CollageTool.CROP -> {
                            launchActivity(
                                toActivity = CropActivity::class.java,
                                input = ToolInput(pathBitmap = viewmodel.pathBitmapResult),
                                callback = { result ->
                                    if (result.resultCode == RESULT_OK) {
                                        val pathBitmap =
                                            result.data?.getStringExtra("pathBitmap")
                                        viewmodel.updateBitmap(
                                            pathBitmap = pathBitmap,
                                            bitmap = pathBitmap.toBitmap()
                                        )
                                    }
                                }
                            )
                        }

                        CollageTool.ADJUST -> {
                            launchActivity(
                                toActivity = AdjustActivity::class.java,
                                input = ToolInput(pathBitmap = viewmodel.pathBitmapResult),
                                callback = { result ->
                                    if (result.resultCode == RESULT_OK) {
                                        val pathBitmap =
                                            result.data?.getStringExtra("pathBitmap")
                                        Log.d("aaaa", "asdasdasd $pathBitmap")
                                        viewmodel.updateBitmap(
                                            pathBitmap = pathBitmap,
                                            bitmap = pathBitmap.toBitmap()
                                        )
                                    }
                                }
                            )
                        }

                        CollageTool.BLUR -> {
                            launchActivity(
                                toActivity = BlurActivity::class.java,
                                input = ToolInput(pathBitmap = viewmodel.pathBitmapResult),
                                callback = { result ->
                                    if (result.resultCode == RESULT_OK) {
                                        val pathBitmap =
                                            result.data?.getStringExtra("pathBitmap")
                                        viewmodel.updateBitmap(
                                            pathBitmap = pathBitmap,
                                            bitmap = pathBitmap.toBitmap()
                                        )
                                    }
                                }
                            )
                        }

                        CollageTool.FILTER -> {
                            launchActivity(
                                toActivity = FilterActivity::class.java,
                                input = ToolInput(pathBitmap = viewmodel.pathBitmapResult),
                                callback = { result ->
                                    if (result.resultCode == RESULT_OK) {
                                        val pathBitmap =
                                            result.data?.getStringExtra("pathBitmap")
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
                                            result.data?.getStringExtra("pathBitmap")
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
                                            result.data?.getStringExtra("pathBitmap")
                                        viewmodel.updateBitmap(
                                            pathBitmap = pathBitmap,
                                            bitmap = pathBitmap.toBitmap()
                                        )
                                    }
                                }
                            )
                        }

//                        CollageTool.REMOVE -> {
//                            launchActivity(
//                                toActivity = RemoveObjectActivity::class.java,
//                                input = ToolInput(
//                                    pathBitmap = viewmodel.pathBitmapResult,
//                                    type = ToolInput.TYPE.BACK_AND_RETURN
//                                ),
//                                callback = { result ->
//                                    if (result.resultCode == RESULT_OK) {
//                                        val pathBitmap =
//                                            result.data?.getStringExtra("pathBitmap")
//                                        viewmodel.updateBitmap(
//                                            pathBitmap = pathBitmap,
//                                            bitmap = pathBitmap.toBitmap()
//                                        )
//                                    }
//                                }
//                            )
//                        }

                        CollageTool.BACKGROUND -> {
                            launchActivity(
                                toActivity = BackgroundActivity::class.java,
                                input = ToolInput(
                                    pathBitmap = viewmodel.pathBitmapResult,
                                ),
                                callback = { result ->
                                    if (result.resultCode == RESULT_OK) {
                                        val json =
                                            result.data?.getStringExtra("backgroundSelection")
                                                ?: return@launchActivity
                                        val data = Json.decodeFromString<BackgroundSelection>(json)

                                        val pathBitmap =
                                            result.data?.getStringExtra("pathBitmap")


                                        viewmodel.push(
                                            StackData.Background(
                                                backgroundColor = data,
                                                bitmap = viewmodel.uiState.value.bitmap!!,
                                                pathBitmapResult = viewmodel.pathBitmapResult,
                                                originBitmap = viewmodel.uiState.value.originBitmap
                                            )
                                        )
                                        viewmodel.updateBackgroundColor(data)
                                    }
                                }
                            )
                        }

                        CollageTool.DRAW -> {
                            launchActivity(
                                toActivity = DrawActivity::class.java,
                                input = ToolInput(pathBitmap = viewmodel.pathBitmapResult),
                                callback = { result ->
                                    if (result.resultCode == RESULT_OK) {
                                        val pathBitmap =
                                            result.data?.getStringExtra("pathBitmap")
                                        viewmodel.updateBitmap(
                                            pathBitmap = pathBitmap,
                                            bitmap = pathBitmap.toBitmap()
                                        )
                                    }
                                }
                            )
                        }

                        CollageTool.REMOVE_BG -> {
                            launchActivity(
                                toActivity = RemoveBackgroundActivity::class.java,
                                input = ToolInput(
                                    pathBitmap = viewmodel.pathBitmapResult,
                                    type = ToolInput.TYPE.BACK_AND_RETURN
                                ),
                                callback = { result ->
                                    if (result.resultCode == RESULT_OK) {
                                        val pathBitmap =
                                            result.data?.getStringExtra("pathBitmap")
                                        viewmodel.updateBitmap(
                                            pathBitmap = pathBitmap,
                                            bitmap = pathBitmap.toBitmap()
                                        )
                                    }
                                }
                            )
                        }

                        CollageTool.ENHANCE -> {
                            launchActivity(
                                toActivity = AIEnhanceActivity::class.java,
                                input = ToolInput(
                                    pathBitmap = viewmodel.pathBitmapResult,
                                    type = ToolInput.TYPE.BACK_AND_RETURN
                                ),
                                callback = { result ->
                                    if (result.resultCode == RESULT_OK) {
                                        val pathBitmap =
                                            result.data?.getStringExtra("pathBitmap")
                                        viewmodel.updateBitmap(
                                            pathBitmap = pathBitmap,
                                            bitmap = pathBitmap.toBitmap()
                                        )
                                    }
                                }
                            )
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
                    BackgroundLayer(
                        backgroundSelection = uiState.backgroundColor,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(
                                top = inner.calculateTopPadding(),
                                bottom = inner.calculateBottomPadding()
                            )
                    )
                    EditorScreen(
                        blurView = blurView,
                        viewmodel = viewmodel,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(
                                top = inner.calculateTopPadding(),
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
                            MainActivity.newScreen(this@EditorActivity)
                        },
                        onCancel = {
                            viewmodel.hideDiscardDialog()
                        }
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        CoroutineScope(Dispatchers.IO).launch {
            deleteShareImageFolder(BaseApplication.getInstanceApp())
        }
    }
}


@OptIn(ExperimentalComposeUiApi::class, ExperimentalComposeApi::class)
@Composable
fun EditorScreen(
    modifier: Modifier = Modifier,
    viewmodel: EditorViewModel,
    onBack: () -> Unit,
    onToolClick: (CollageTool) -> Unit,
    blurView: BlurView,
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
                canUndo = uiState.canUndo,
                canRedo = uiState.canRedo
            )
            if (uiState.isOriginal) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(top = 20.dp, bottom = 23.dp)
                        .onSizeChanged { layout ->
                            viewmodel.canvasSize = layout.toSize()
                            viewmodel.scaleBitmapToBox(layout.toSize())
                        }
                        .capturable(captureController)
                ) {
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
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(top = 20.dp, bottom = 23.dp)
                        .capturable(captureController)
                ) {
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
                            BlurView(
                                modifier = Modifier
                                    .fillMaxSize(),
                                blurView = blurView,
                                bitmap = it,
                                intensity = 30f,
                                scaleType = ImageView.ScaleType.CENTER_CROP
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


fun Uri.toBitmap(context: Context): Bitmap? {
    val bitmap = if (Build.VERSION.SDK_INT < 28) {
        MediaStore.Images.Media.getBitmap(
            context.contentResolver,
            this
        )
    } else {
        val source = ImageDecoder.createSource(
            context.contentResolver,
            this
        )
        ImageDecoder.decodeBitmap(source)
    }
    return bitmap
}

fun deleteShareImageFolder(context: Context) {
    val folder = context.getExternalFilesDir("editor_image_success")
    folder?.let {
        deleteDirectoryRecursively(it)
    }
}

fun deleteDirectoryRecursively(dir: File): Boolean {
    if (dir.isDirectory) {
        dir.listFiles()?.forEach { file ->
            deleteDirectoryRecursively(file)
        }
    }
    return dir.delete()  // xóa file hoặc folder rỗng
}


fun BaseActivity.initEditorLib() {
    val mLoadImageCallback: LoadImageCallback = object : LoadImageCallback {
        override fun loadImage(str: String?, obj: Any?): Bitmap? {
            try {
                return BitmapFactory.decodeStream(assets.open(str!!))
            } catch (io: IOException) {
                io.printStackTrace()
                return null
            }
        }

        override fun loadImageOK(bitmap: Bitmap, obj: Any?) {
            bitmap.recycle()
        }
    }
    try {
        System.loadLibrary("ffmpeg")
    } catch (e: java.lang.Exception) {
        e.printStackTrace()
    }

    CGENativeLibrary.setLoadImageCallback(mLoadImageCallback, null)
}