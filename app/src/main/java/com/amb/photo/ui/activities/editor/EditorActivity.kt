package com.amb.photo.ui.activities.editor

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.amb.photo.ui.activities.collage.components.CollageTool
import com.amb.photo.ui.activities.collage.components.FeatureBottomTools
import com.amb.photo.ui.activities.collage.components.FeaturePhotoHeader
import com.amb.photo.ui.activities.editor.adjust.AdjustActivity
import com.amb.photo.ui.activities.editor.crop.CropActivity
import com.amb.photo.ui.activities.editor.crop.ToolInput
import com.amb.photo.utils.getInput
import com.basesource.base.ui.base.BaseActivity
import com.basesource.base.ui.base.IScreenData
import com.basesource.base.utils.launchActivity
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.wysaid.nativePort.CGENativeLibrary
import org.wysaid.nativePort.CGENativeLibrary.LoadImageCallback
import java.io.IOException

data class EditorInput(
    val pathBitmap: String? = null
) : IScreenData

class EditorActivity : BaseActivity() {

    private val viewmodel: EditorViewModel by viewModel()

    private val screenInput: EditorInput? by lazy {
        intent.getInput()
    }

    fun String?.toBitmap(context: Context): Bitmap? {
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            System.loadLibrary("ffmpeg")
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }

        CGENativeLibrary.setLoadImageCallback(this.mLoadImageCallback, null)


        viewmodel.setPathBitmap(screenInput?.pathBitmap, screenInput?.pathBitmap.toBitmap(this))
        enableEdgeToEdge()
        setContent {

            Scaffold(
                containerColor = Color(0xFFF2F4F8)
            ) { inner ->
                EditorScreen(
                    viewmodel = viewmodel,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            top = inner.calculateTopPadding(),
                            bottom = inner.calculateBottomPadding()
                        ),
                    onBack = {

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
                                            viewmodel.setPathBitmap(
                                                pathBitmap,
                                                pathBitmap.toBitmap(this)
                                            )
                                        }
                                    }
                                )
                            }

                            CollageTool.ADJUST -> {
                                launchActivity(
                                    toActivity = AdjustActivity::class.java,
                                    input = ToolInput(pathBitmap = viewmodel.pathBitmapResult)
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
}


@Composable
fun EditorScreen(
    modifier: Modifier = Modifier,
    viewmodel: EditorViewModel,
    onBack: () -> Unit,
    onToolClick: (CollageTool) -> Unit
) {
    val uiState by viewmodel.uiState.collectAsStateWithLifecycle()
    var boxSize by remember { mutableStateOf(IntSize.Zero) }

    Column(modifier) {
        FeaturePhotoHeader(
            onBack = onBack,
            onUndo = { /* TODO */ },
            onRedo = { /* TODO */ },
            onSave = { /* TODO */ },
            canUndo = false,
            canRedo = false
        )
        if (uiState.isOriginal) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(top = 20.dp, bottom = 23.dp)
                    .onSizeChanged { layout ->
                        val newSize = layout
                        if (newSize != boxSize) {
                            boxSize = newSize
                            // Báo kích thước Box lên ViewModel
                            viewmodel.scaleBitmapToBox(newSize.toSize())
                        }
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    uiState.bitmap?.let { bmp ->
                        drawImage(
                            image = bmp.asImageBitmap(),
                            topLeft = Offset(
                                (size.width - bmp.width) / 2,
                                (size.height - bmp.height) / 2
                            )
                        )
                    }
                }
//                uiState.bitmap?.let {
//                    Image(
//                        bitmap = it.asImageBitmap(),
//                        contentDescription = null,
//                        contentScale = ContentScale.None,
//                        alignment = Alignment.Center,
//                        modifier = Modifier
//                            .fillMaxSize()
//                    )
//                }
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
                            val newSize = layout
                            if (newSize != boxSize) {
                                boxSize = newSize
                                // Báo kích thước Box lên ViewModel
                                viewmodel.scaleBitmapToBox(newSize.toSize())
                            }
                        }
                ) {
                    uiState.originBitmap?.let {
                        Image(
                            bitmap = it.asImageBitmap(),
                            contentDescription = null,
                            contentScale = ContentScale.None,
                            alignment = Alignment.Center,
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

