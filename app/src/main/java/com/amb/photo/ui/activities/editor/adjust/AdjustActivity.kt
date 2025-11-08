package com.amb.photo.ui.activities.editor.adjust

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.PixelFormat
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.amb.photo.R
import com.amb.photo.ui.activities.collage.components.CollageTool
import com.amb.photo.ui.activities.collage.components.FeatureBottomTools
import com.amb.photo.ui.activities.editor.crop.FooterEditor
import com.amb.photo.ui.activities.editor.crop.ToolInput
import com.amb.photo.ui.theme.AppColor
import com.amb.photo.ui.theme.AppStyle
import com.amb.photo.utils.getInput
import com.basesource.base.ui.base.BaseActivity
import com.basesource.base.utils.ImageWidget
import com.tanishranjan.cropkit.CropShape
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.wysaid.nativePort.CGENativeLibrary
import org.wysaid.nativePort.CGENativeLibrary.LoadImageCallback
import org.wysaid.view.ImageGLSurfaceView
import java.io.IOException


class AdjustActivity : BaseActivity() {

    private val screenInput: ToolInput? by lazy {
        intent.getInput()
    }

    private val viewmodel: AdjustViewModel by viewModel()

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
        CGENativeLibrary.setLoadImageCallback(this.mLoadImageCallback, null)

        viewmodel.initBitmap(screenInput?.getBitmap(this@AdjustActivity))
        enableEdgeToEdge()
        setContent {
            Scaffold(
                containerColor = Color(0xFFF2F4F8)
            ) { inner ->
//                GpuAdjustExample(viewmodel)
                AdjustScreen(
                    modifier = Modifier.padding(
                        top = inner.calculateTopPadding(),
                        bottom = inner.calculateBottomPadding()
                    ),
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
    modifier: Modifier,
    viewmodel: AdjustViewModel,
    onCancel: () -> Unit,
    onApply: () -> Unit,
) {
    val uiState by viewmodel.uiState.collectAsStateWithLifecycle()
//    var brightness by remember { mutableStateOf(50f) }
    var contrast by remember { mutableStateOf(1f) }
    var saturation by remember { mutableStateOf(1f) }
    var warmth by remember { mutableStateOf(0f) }
    var hue by remember { mutableStateOf(0f) }
    var shadow by remember { mutableStateOf(0f) }
    var highlight by remember { mutableStateOf(0f) }
    var vignetteStart by remember { mutableStateOf(0f) }
    var vignetteEnd by remember { mutableStateOf(0f) }
    var sharpen by remember { mutableStateOf(0f) }
    var showOriginal by remember { mutableStateOf(false) }
    Column(
        modifier = modifier
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        uiState.bitmap?.let {
            Box(
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .aspectRatio(it.width / it.height.toFloat())
                            .background(Color.Green)
                            .align(Alignment.Center)

                    ) {
                        GpuImageAdjustView2(
                            modifier = Modifier
                                .fillMaxSize(),
                            bitmap = it,
                            index = uiState.index,
                            intensity = getIntensity(uiState)
                        )
                        Image(
                            bitmap = it.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .alpha(if (showOriginal) 1f else 0f),
                        )

                    }


                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 16.dp, start = 16.dp, end = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ResetButton(
                            modifier = Modifier
                                .height(32.dp),
                            onClick = {
                                viewmodel.reset()
                            }
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        OriginalButton { pressed ->
                            showOriginal = pressed
                        }
                    }
                }
            }

        }
        Spacer(modifier = Modifier.height(52.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(
                    RoundedCornerShape(
                        topStart = 24.dp,
                        topEnd = 24.dp
                    )
                )
                .background(Color.White)
        ) {
            when (uiState.tool) {
                CollageTool.BRIGHTNESS -> {
                    SliderAdjust(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 23.dp),
                        value = uiState.brightness,
                        onValueChange = {
                            viewmodel.updateBrightness(it)
                        },
                        valueRange = 0f..100f
                    )
                }

                CollageTool.CONTRAST -> {

                }

                CollageTool.SATURATION -> {

                }

                CollageTool.WARMTH -> {

                }

                CollageTool.FADE -> {

                }

                CollageTool.HIGHLIGHT -> {

                }

                CollageTool.SHADOW -> {

                }

                CollageTool.HUE -> {

                }

                CollageTool.VIGNETTE -> {

                }

                CollageTool.SHARPEN -> {

                }

                CollageTool.GRAIN -> {

                }

                else -> {

                }
            }


            FeatureBottomTools(
                tools = uiState.items,
                tool = uiState.tool,
                onItemSelect = {
                    viewmodel.itemSelected(
                        index = it.index,
                        tool = it.tool
                    )
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
}


fun calcIntensity(intensity: Float, minValue: Float, maxValue: Float, originValue: Float): Float {
    val result: Float
    if (intensity <= 0.0f) {
        result = minValue
    } else if (intensity >= 1.0f) {
        result = maxValue
    } else if (intensity <= 0.5f) {
        result = minValue + (originValue - minValue) * intensity * 2.0f
    } else {
        result = maxValue + (originValue - maxValue) * (1.0f - intensity) * 2.0f
    }
    return result
}

fun getIntensity(
    uiState: AdjustUIState
): Float {
    return when (uiState.tool) {
        CollageTool.BRIGHTNESS -> {
//            val newBrightness = (uiState.brightness / 50f) - 1f
//            Log.d("aaaa", "$newBrightness")
//            newBrightness
            calcIntensity(
                intensity = uiState.brightness / 100f,
                minValue = -1f,
                maxValue = 1f,
                originValue = 0f
            )
        }

        else -> {
            0f
        }
    }
}

@Composable
fun OriginalButton(
    modifier: Modifier = Modifier,
    onPressedChange: (Boolean) -> Unit
) {
    Box(
        modifier = modifier
            .width(32.dp)
            .height(32.dp)
            .background(
                color = Color(0xFF1B1F1E).copy(alpha = 0.8f),
                shape = CircleShape
            )
            .pointerInteropFilter { event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        onPressedChange(true)
                        true
                    }

                    MotionEvent.ACTION_UP,
                    MotionEvent.ACTION_CANCEL -> {
                        onPressedChange(false)
                        true
                    }

                    else -> false
                }
            }
    ) {
        ImageWidget(
            modifier = Modifier.align(Alignment.Center),
            resId = R.drawable.ic_compare
        )
    }
}

@Composable
fun ResetButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(8), // bo tròn như pill
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF1B1F1E).copy(alpha = 0.8f), // nền tối mờ
            contentColor = Color.White
        ),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 7.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        ImageWidget(
            resId = R.drawable.ic_refresh
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = stringResource(R.string.reset),
            style = AppStyle.buttonMedium().semibold().white()
        )
    }
}

@Composable
fun SliderAdjust(
    modifier: Modifier = Modifier,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float> = 0f..100f
) {
    Row(
        modifier = modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            modifier = Modifier
                .weight(1f)
                .height(14.dp),
            colors = SliderDefaults.colors(
                thumbColor = AppColor.Gray800,
                activeTrackColor = AppColor.Gray800,
                inactiveTrackColor = AppColor.Gray200
            )
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = value.toInt().toString(),
            style = AppStyle.body1().medium().gray800()
        )
    }
}

@Composable
fun GpuAdjustExample(
    viewModel: AdjustViewModel
) {
    var brightness by remember { mutableStateOf(50f) }
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
//            GpuImageAdjustView2(
//                modifier = Modifier
//                    .weight(1f)
//                    .fillMaxWidth(),
//                bitmap = it,
//                index = 0,
//                intensity = 1f
//            )

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
    index: Int,
    bitmap: Bitmap,
    modifier: Modifier = Modifier,
    intensity: Float,
) {

    AndroidView(
        modifier = modifier,
        factory = { context ->
            Log.d("updateV", "22222")
            val glView = ImageGLSurfaceView(context, null)
            glView.displayMode = ImageGLSurfaceView.DisplayMode.DISPLAY_ASPECT_FIT
            glView.renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
            // Load bitmap trước, nhưng đợi Surface ready
            glView.setSurfaceCreatedCallback {
                glView.setImageBitmap(bitmap)
                glView.setFilterWithConfig(
                    getConfig()
                )
            }
            glView
        },
        update = { view ->
            view.queueEvent { // đảm bảo chạy trong GL thread
                view.setFilterIntensityForIndex(intensity, index, true)
            }
        }
    )
}

@Composable
fun GpuImageAdjustDefault(
    bitmap: Bitmap,
    modifier: Modifier = Modifier,
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
                    getConfig()
                )
            }
            glView
        },
        update = { view ->

        }
    )
}

fun getConfig(
    brightness: Float = 0.0f,
    contrast: Float = 1.0f,
    saturation: Float = 1.0f,
    warmth: Float = 0.0f,
    sharpen: Float = 0.0f,
    shadow: Float = 0f,
    highlight: Float = 0f,
    hue: Float = 0f,
): String {
    return "@adjust brightness $brightness " +
            "@adjust contrast $contrast " +
            "@adjust saturation $saturation " +
            "@adjust sharpen $sharpen 10" +
            "@adjust whitebalance $warmth 1.0 " +
            "@adjust shadowhighlight $shadow $highlight " +
            "@adjust hue $hue " +
            "@vignette 1.0 1.0"
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