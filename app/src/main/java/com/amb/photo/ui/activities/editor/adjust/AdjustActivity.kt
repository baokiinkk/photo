package com.amb.photo.ui.activities.editor.adjust

import android.content.Context
import android.graphics.Bitmap
import android.opengl.GLSurfaceView
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.amb.photo.R
import com.amb.photo.ui.activities.collage.components.FeatureBottomTools
import com.amb.photo.ui.activities.editor.crop.FooterEditor
import com.amb.photo.ui.activities.editor.crop.ToolInput
import com.amb.photo.utils.getInput
import com.basesource.base.ui.base.BaseActivity
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.wysaid.view.ImageGLSurfaceView


class AdjustActivity : BaseActivity() {

    private val screenInput: ToolInput? by lazy {
        intent.getInput()
    }

    private val viewmodel: AdjustViewModel by viewModel()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewmodel.initBitmap(screenInput?.getBitmap(this@AdjustActivity))
        enableEdgeToEdge()
        setContent {
            Scaffold(
                containerColor = Color(0xFFF2F4F8)
            ) { inner ->
//                GpuAdjustExample(viewmodel)
                AdjustScreen(
                    viewmodel = viewmodel,
                    onCancel = {
                        finish()
                    },
                    onApply = {
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
fun AdjustScreen(
    viewmodel: AdjustViewModel,
    onCancel: () -> Unit,
    onApply: () -> Unit,
) {
    val uiState by viewmodel.uiState.collectAsStateWithLifecycle()

    Column {

        uiState.bitmap?.let {
            Image(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                bitmap = uiState.bitmap?.asImageBitmap()!!,
                contentDescription = null,
            )
        }

        FeatureBottomTools(
            tools = uiState.items,
            tool = uiState.tool,
            onToolClick = {
                viewmodel.itemSelected(it)
            }
        )

        Spacer(modifier = Modifier.height(8.dp))
        FooterEditor(
            modifier = Modifier
                .fillMaxWidth(),
            title = stringResource(R.string.adjust),
            onCancel = onCancel,
            onApply = onApply
        )
        Spacer(modifier = Modifier.height(16.dp))

    }
}

@Composable
fun GpuAdjustExample(
    viewModel: AdjustViewModel
) {
    var brightness by remember { mutableStateOf(0f) }
    var contrast by remember { mutableStateOf(1f) }
    var saturation by remember { mutableStateOf(1f) }
    var warmth by remember { mutableStateOf(0f) }
    var hue by remember { mutableStateOf(0f) }
    var shadow by remember { mutableStateOf(0f) }
    var highlight by remember { mutableStateOf(0f) }
    var vignetteStart by remember { mutableStateOf(0f) }
    var vignetteEnd by remember { mutableStateOf(0f) }
    var sharpen by remember { mutableStateOf(0f) }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    uiState.bitmap?.let {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Red)
        ) {
            GpuImageAdjustView2(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                bitmap = it,
                brightness = brightness,
                contrast = contrast,
                saturation = saturation,
                warmth = warmth,
                hue = hue,
                vignetteSTart = vignetteStart,
                vignetteEnd = vignetteEnd,
                sharpen = sharpen,
            )

            Column(Modifier.padding(8.dp)) {
//                Text("Brightness: %.2f".format(brightness), color = Color.White)
//                Slider(
//                    value = brightness,
//                    onValueChange = { brightness = it },
//                    valueRange = -1f..1f
//                )
//
//                Text("Contrast: %.2f".format(contrast), color = Color.White)
//                Slider(value = contrast, onValueChange = { contrast = it }, valueRange = 0f..4f)
//
//                Text("Saturation: %.2f".format(saturation), color = Color.White)
//                Slider(value = saturation, onValueChange = { saturation = it }, valueRange = 0f..2f)
//
//                Text("warmth: %.2f".format(warmth), color = Color.White)
//                Slider(value = warmth, onValueChange = { warmth = it }, valueRange = 0.0f..2f)
//  ====================== đã theo tools =================================================
//                Text("shadow: %.2f".format(shadow), color = Color.White)
//                Slider(value = shadow, onValueChange = { shadow = it }, valueRange = -200f..100f)
//
//                Text("highlight: %.2f".format(highlight), color = Color.White)
//                Slider(
//                    value = highlight,
//                    onValueChange = { highlight = it },
//                    valueRange = -100f..200f
//                )
//
//                Text("Hue: %.2f".format(hue), color = Color.White)
//                Slider(value = hue, onValueChange = { hue = it }, valueRange = 0f..6f)

                Text("vignette start: %.2f".format(vignetteStart), color = Color.White)
                Slider(value = vignetteStart, onValueChange = {
                    vignetteStart = it
                }, valueRange = 0f..1f)
                Text("vignette end: %.2f".format(vignetteEnd), color = Color.White)
                Slider(
                    value = vignetteEnd,
                    onValueChange = { vignetteEnd = it },
                    valueRange = 0f..1f
                )

                Text("sharpen: %.2f".format(sharpen), color = Color.White)
                Slider(value = sharpen, onValueChange = { sharpen = it }, valueRange = 0f..10f)
            }
        }
    }
}

@Composable
fun GpuImageAdjustView2(
    bitmap: Bitmap,
    modifier: Modifier = Modifier,
    brightness: Float = 0.0f,
    contrast: Float = 1.0f,
    saturation: Float = 1.0f,
    warmth: Float = 0.0f,
    sharpen: Float = 0.0f,
    shadow: Float = 0f,
    highlight: Float = 0f,
    hue: Float = 0f,
    vignetteSTart: Float,
    vignetteEnd: Float
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            val glView = ImageGLSurfaceView(context, null)
            glView.displayMode = ImageGLSurfaceView.DisplayMode.DISPLAY_ASPECT_FIT
            glView.renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY

            // Load bitmap trước, nhưng đợi Surface ready
            glView.setSurfaceCreatedCallback {
                glView.setImageBitmap(bitmap)
                glView.setFilterWithConfig(
                    getConfig(
                        brightness = brightness,
                        contrast = contrast,
                        saturation = saturation,
                        sharpen = sharpen,
                        warmth = warmth,
                        shadow = shadow,
                        highlight = highlight,
                        hue = hue,
                        vignetteSTart = vignetteSTart,
                        vignetteEnd = vignetteEnd
                    )
                )
            }
            glView
        },
        update = { view ->
            // update lại filter nếu cần
            view.setFilterWithConfig(
                getConfig(
                    brightness = brightness,
                    contrast = contrast,
                    saturation = saturation,
                    sharpen = sharpen,
                    warmth = warmth,
                    shadow = shadow,
                    highlight = highlight,
                    hue = hue,
                    vignetteSTart = vignetteSTart,
                    vignetteEnd = vignetteEnd
                )
            )
            view.requestRender()
        }
    )
}

fun getConfig(
    brightness: Float,
    contrast: Float,
    saturation: Float,
    warmth: Float,
    shadow: Float,
    highlight: Float,
    hue: Float,
    vignetteSTart: Float,
    vignetteEnd: Float,
    sharpen: Float,
): String {
    return "@adjust brightness $brightness " +
            "@adjust contrast $contrast " +
            "@adjust saturation $saturation " +
            "@adjust sharpen $sharpen 10" +
            "@adjust whitebalance $warmth 1.0 " +
            "@adjust shadowhighlight $shadow $highlight " +
            "@adjust hue $hue " +
            "@vignette $vignetteSTart $vignetteEnd"
}

@Composable
fun GpuImageAdjustView(
    modifier: Modifier = Modifier,
    context: Context = LocalContext.current,
    bitmap: Bitmap,
    brightness: Float = 0.0f,
    contrast: Float = 1.0f,
    saturation: Float = 1.0f,
    warmth: Float = 1.0f,
    hue: Float = 0.0f
) {
    AndroidView(
        modifier = modifier,
        factory = {
            val glView = ImageGLSurfaceView(context, null)
            glView.setImageBitmap(bitmap)
//            glView.displayMode = ImageGLSurfaceView.DisplayMode.DISPLAY_ASPECT_FIT
//            glView.setImageBitmap(bitmap)
//            glView.setFilterWithConfig(
//                generateFilterConfig(
//                    brightness,
//                    contrast,
//                    saturation,
//                    warmth,
//                    hue
//                )
//            )
            glView
        },
        update = { view ->
//            view.setFilterWithConfig(
//                generateFilterConfig(
//                    brightness,
//                    contrast,
//                    saturation,
//                    warmth,
//                    hue
//                )
//            )
        }
    )
}

private fun generateFilterConfig(
    brightness: Float,
    contrast: Float,
    saturation: Float,
    warmth: Float,
    hue: Float
): String {
    return """
        @adjust brightness ${brightness};
        @adjust contrast ${contrast};
        @adjust saturation ${saturation};
        @adjust temperature ${warmth};
        @adjust hue ${hue};
    """.trimIndent()
}