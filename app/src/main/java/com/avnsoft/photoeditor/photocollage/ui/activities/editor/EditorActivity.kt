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
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.avnsoft.photoeditor.photocollage.BaseApplication
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.CollageTool
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.FeatureBottomTools
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.FeaturePhotoHeader
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.adjust.AdjustActivity
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.background.BackgroundActivity
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.background.BackgroundResult
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.blur.BlurActivity
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.blur.BlurView
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.blur.tabShape
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.crop.CropActivity
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.crop.ToolInput
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.filter.FilterActivity
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.remove_object.RemoveObjectActivity
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.sticker.StickerActivity
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.text_sticker.TextStickerActivity
import com.avnsoft.photoeditor.photocollage.ui.theme.AppColor
import com.avnsoft.photoeditor.photocollage.utils.getInput
import com.basesource.base.ui.base.BaseActivity
import com.basesource.base.ui.base.IScreenData
import com.basesource.base.utils.fromJson
import androidx.core.graphics.toColorInt
import com.basesource.base.utils.launchActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.wysaid.nativePort.CGENativeLibrary
import org.wysaid.nativePort.CGENativeLibrary.LoadImageCallback
import java.io.File
import java.io.IOException

data class EditorInput(
    val pathBitmap: String? = null
) : IScreenData

fun String?.toBitmap(context: Context): Bitmap? {
//    val imageUri = this?.toUri()
//    val bitmap = imageUri?.toBitmap(context)
    val bitmap = BitmapFactory.decodeFile(this)
    return bitmap
}

class EditorActivity : BaseActivity() {

    private val viewmodel: EditorViewModel by viewModel()

    private val screenInput: EditorInput? by lazy {
        intent.getInput()
    }

    fun String?.uriToBitmap(context: Context): Bitmap? {
        val imageUri = this?.toUri()
        val bitmap = imageUri?.toBitmap(context)
        return bitmap
    }

    var mLoadImageCallback: LoadImageCallback = object : LoadImageCallback {
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

    private lateinit var blurView: BlurView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            System.loadLibrary("ffmpeg")
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }

        CGENativeLibrary.setLoadImageCallback(this.mLoadImageCallback, null)
        blurView = BlurView(this)
        blurView.tabShape()
        viewmodel.setPathBitmap(screenInput?.pathBitmap, screenInput?.pathBitmap.uriToBitmap(this))
        enableEdgeToEdge()
        setContent {

            Scaffold(
                containerColor = AppColor.backgroundAppColor
            ) { inner ->

                val uiState by viewmodel.uiState.collectAsStateWithLifecycle()


                EditorScreen(
                    blurView = blurView,
                    viewmodel = viewmodel,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            top = inner.calculateTopPadding(),
                            bottom = inner.calculateBottomPadding()
                        )
                        .background(uiState.backgroundColor),
                    onBack = {
                        finish()
                    },
                    onToolClick = {
                        when (it) {
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
                                                bitmap = pathBitmap.toBitmap(this)
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
                                                bitmap = pathBitmap.toBitmap(this)
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
                                                bitmap = pathBitmap.toBitmap(this)
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
                                                bitmap = pathBitmap.toBitmap(this)
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
                                                bitmap = pathBitmap.toBitmap(this)
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
                                                bitmap = pathBitmap.toBitmap(this)
                                            )
                                        }
                                    }
                                )
                            }

                            CollageTool.REMOVE -> {
                                launchActivity(
                                    toActivity = RemoveObjectActivity::class.java,
                                    input = ToolInput(pathBitmap = viewmodel.pathBitmapResult),
                                    callback = { result ->
                                        if (result.resultCode == RESULT_OK) {
                                            val pathBitmap =
                                                result.data?.getStringExtra("pathBitmap")
                                            viewmodel.updateBitmap(
                                                pathBitmap = pathBitmap,
                                                bitmap = pathBitmap.toBitmap(this)
                                            )
                                        }
                                    }
                                )
                            }

                            CollageTool.BACKGROUND -> {
                                launchActivity(
                                    toActivity = BackgroundActivity::class.java,
                                    input = ToolInput(pathBitmap = viewmodel.pathBitmapResult),
                                    callback = { result ->
                                        if (result.resultCode == RESULT_OK) {
                                            val output = result.data?.getStringExtra("pathBitmap")
                                                ?.fromJson<BackgroundResult>()
                                            
                                            // Extract color from backgroundSelection
                                            val bgColor = when (val selection = output?.backgroundSelection) {
                                                is com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.BackgroundSelection.Solid -> {
                                                    try {
                                                        Color(selection.color.toColorInt())
                                                    } catch (e: Exception) {
                                                        Color(0xFFF2F4F8)
                                                    }
                                                }
                                                else -> Color(0xFFF2F4F8)
                                            }
                                            
                                            bgColor.let { color ->
                                                viewmodel.push(
                                                    StackData.Background(color)
                                                )
                                            }

                                            viewmodel.updateBackgroundColor(bgColor)
                                        }
                                    }
                                )
                            }

                            else -> {

                            }
                        }
                    }
                )
//                Column(
//                    modifier = Modifier.padding(inner),
//                    verticalArrangement = Arrangement.Center
//                ) {
//                    CustomButton("crop") {
//                        launchActivity(
//                            toActivity = CropActivity::class.java,
//                            input = CropInput(pathBitmap = pathBitmapResult),
//                            callback = { result ->
//                                if (result.resultCode == Activity.RESULT_OK) {
//                                    val pathBitmap = result.data?.getStringExtra("pathBitmap")
//                                    pathBitmapResult = pathBitmap
//                                }
//                            }
//                        )
//                    }
//
//                    Spacer(modifier = Modifier.height(40.dp))
//
//                    pathBitmapResult?.let {
//                        val uri = it.toUri()
//                        LoadImage(model = uri)
//                    }
//                }
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


@Composable
fun EditorScreen(
    modifier: Modifier = Modifier,
    viewmodel: EditorViewModel,
    onBack: () -> Unit,
    onToolClick: (CollageTool) -> Unit,
    blurView: BlurView
) {
    val uiState by viewmodel.uiState.collectAsStateWithLifecycle()

    Column(modifier) {
        FeaturePhotoHeader(
            onBack = onBack,
            onUndo = {
                viewmodel.undo()
            },
            onRedo = {
                viewmodel.redo()
            },
            onSave = { /* TODO */ },
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
//                        val newSize = layout
//                        if (newSize != boxSize) {
//                            boxSize = newSize
//                            // Báo kích thước Box lên ViewModel
//                            viewmodel.scaleBitmapToBox(newSize.toSize())
//                        }
                    }
            ) {
//                Canvas(modifier = Modifier.fillMaxSize()) {
//                    uiState.bitmap?.let { bmp ->
//                        drawImage(
//                            image = bmp.asImageBitmap(),
//                            topLeft = Offset(
//                                (size.width - bmp.width) / 2,
//                                (size.height - bmp.height) / 2
//                            )
//                        )
//                    }
//                }
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
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .aspectRatio(1f)
                        .onSizeChanged { layout ->
                            viewmodel.canvasSize = layout.toSize()
                            viewmodel.scaleBitmapToBox(layout.toSize())
//                            val newSize = layout
//                            if (newSize != boxSize) {
//                                boxSize = newSize
//                                // Báo kích thước Box lên ViewModel
//                                viewmodel.scaleBitmapToBox(newSize.toSize())
//                            }
                        }
                        .background(Color.Red)
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
