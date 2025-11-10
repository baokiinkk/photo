package com.amb.photo.ui.activities.editor.adjust

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.amb.photo.R
import com.amb.photo.ui.activities.collage.components.CollageTool
import com.amb.photo.ui.activities.collage.components.FeatureBottomTools
import com.amb.photo.ui.activities.editor.crop.FooterEditor
import com.amb.photo.ui.activities.editor.crop.ToolInput
import com.amb.photo.ui.activities.editor.crop.saveImage
import com.amb.photo.ui.theme.AppColor
import com.amb.photo.ui.theme.AppStyle
import com.amb.photo.utils.getInput
import com.basesource.base.ui.base.BaseActivity
import com.basesource.base.utils.ImageWidget
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

    private lateinit var glView: ImageGLSurfaceView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CGENativeLibrary.setLoadImageCallback(this.mLoadImageCallback, null)
        glView = ImageGLSurfaceView(this, null)
        viewmodel.initBitmap(screenInput?.getBitmap(this@AdjustActivity))
        enableEdgeToEdge()
        setContent {
            val coroutineScope = rememberCoroutineScope()
            Scaffold(
                containerColor = Color(0xFFF2F4F8)
            ) { inner ->
//                GpuAdjustExample(viewmodel)
                val view = LocalView.current

                AdjustScreen(
                    glView = glView,
                    modifier = Modifier.padding(
                        top = inner.calculateTopPadding(),
                        bottom = inner.calculateBottomPadding()
                    ),
                    viewmodel = viewmodel,
                    onCancel = {
                        finish()
                    },
                    onApply = {
                        glView.getResultBitmap {
//                            val pathBitmap = ImageUtil.saveBitmap(it)
                            saveImage(
                                context = this,
                                bitmap = it,
                                onImageSaved = {pathBitmap->
                                    val intent = Intent()
                                    intent.putExtra("pathBitmap", "$pathBitmap")
                                    setResult(RESULT_OK, intent)
                                    finish()
                                }
                            )
                        }
//                        coroutineScope.launch {
//                            val bitmap =
//                                captureComposableToBitmapFinal(view, viewmodel.captureRect!!)
//                            saveImage(
//                                context = this@AdjustActivity,
//                                croppedImage = bitmap,
//                                onImageSaved = { uri ->
//                                    val intent = Intent()
//                                    intent.putExtra("adjust", uri)
//                                    setResult(RESULT_OK, intent)
//                                    finish()
//                                }
//                            )
//                        }
                    }
                )
            }
        }
    }
}

@Composable
fun AdjustScreen(
    glView: ImageGLSurfaceView,
    modifier: Modifier,
    viewmodel: AdjustViewModel,
    onCancel: () -> Unit,
    onApply: () -> Unit,
) {
    val uiState by viewmodel.uiState.collectAsStateWithLifecycle()
    var showOriginal by remember { mutableStateOf(false) }

//    val captureRect = remember { mutableStateOf<IntRect?>(null) }


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
                            .align(Alignment.Center)
                            .captureComposableBounds { rect ->
                                viewmodel.captureRect = rect
                            }

                    ) {
                        GpuImageAdjustView(
                            glView = glView,
                            modifier = Modifier
                                .fillMaxSize(),
                            bitmap = it,
                            uiState = uiState
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
                    SliderTool(
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
                    SliderTool(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 23.dp),
                        value = uiState.contrast,
                        onValueChange = {
                            viewmodel.updateContrast(it)
                        },
                        valueRange = 0f..100f
                    )
                }

                CollageTool.SATURATION -> {
                    SliderTool(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 23.dp),
                        value = uiState.saturation,
                        onValueChange = {
                            viewmodel.updateSaturation(it)
                        },
                        valueRange = 0f..100f
                    )
                }

                CollageTool.WARMTH -> {
                    SliderTool(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 23.dp),
                        value = uiState.warmth,
                        onValueChange = {
                            viewmodel.updateWarmth(it)
                        },
                        valueRange = 0f..100f
                    )
                }

                CollageTool.FADE -> {
                    SliderTool(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 23.dp),
                        value = uiState.fade,
                        onValueChange = {
                            viewmodel.updateFade(it)
                        },
                        valueRange = 0f..100f
                    )
                }

                CollageTool.HIGHLIGHT -> {
                    SliderTool(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 23.dp),
                        value = uiState.highlight,
                        onValueChange = {
                            viewmodel.updateHighlight(it)
                        },
                        valueRange = 0f..100f
                    )
                }

                CollageTool.SHADOW -> {
                    SliderTool(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 23.dp),
                        value = uiState.shadow,
                        onValueChange = {
                            viewmodel.updateShadow(it)
                        },
                        valueRange = 0f..100f
                    )
                }

                CollageTool.HUE -> {
                    SliderTool(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 23.dp),
                        value = uiState.hue,
                        onValueChange = {
                            viewmodel.updateHue(it)
                        },
                        valueRange = 0f..100f
                    )
                }

                CollageTool.VIGNETTE -> {
                    SliderTool(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 23.dp),
                        value = uiState.vignette,
                        onValueChange = {
                            viewmodel.updateVignette(it)
                        },
                        valueRange = 0f..100f
                    )
                }

                CollageTool.SHARPEN -> {
                    SliderTool(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 23.dp),
                        value = uiState.sharpen,
                        onValueChange = {
                            viewmodel.updateSharpen(it)
                        },
                        valueRange = 0f..100f
                    )
                }

                CollageTool.GRAIN -> {
                    SliderTool(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 23.dp),
                        value = uiState.grain,
                        onValueChange = {
                            viewmodel.updateGrain(it)
                        },
                        valueRange = 0f..100f
                    )
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
fun SliderTool(
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
fun GpuImageAdjustView(
    bitmap: Bitmap,
    modifier: Modifier = Modifier,
    uiState: AdjustUIState,
    glView: ImageGLSurfaceView
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            Log.d("updateV", "22222")
            glView.displayMode = ImageGLSurfaceView.DisplayMode.DISPLAY_ASPECT_FIT
            glView.renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
            glView.setSurfaceCreatedCallback {
                glView.setImageBitmap(bitmap)
                glView.setFilterWithConfig(
                    getNewConfig(uiState)
                )
            }
            glView
        },
        update = { view ->
            view.queueEvent { // đảm bảo chạy trong GL thread
                val config = getNewConfig(uiState)
                view.setFilterWithConfig(config)
            }
        }
    )
}

fun getNewConfig(
    uiState: AdjustUIState
): String {
    val brightness = getBrightness(uiState.brightness)
    val contrast = getContrast(uiState.contrast)
    val saturation = getSaturation(uiState.saturation)
    val warmth = getWarmth(uiState.warmth)
    val highlight = getHighlight(uiState.highlight)
    val shadow = getShadow(uiState.shadow)
    val hue = getHue(uiState.hue)
    val vignette = getVignette(uiState.vignette)
    val sharpen = getSharpen(uiState.sharpen)
    val fade = getFade(uiState.fade)
    val grain = getGrain(uiState.grain)
    Log.d("getNewConfig", "brightness: $brightness && vignette: $vignette")
    return "@adjust brightness $brightness " +
            "@adjust contrast $contrast " +
            "@adjust saturation $saturation " +
            "@adjust whitebalance $warmth 1.0 " +
            "@adjust shadowhighlight $shadow $highlight " +
            "@adjust hue $hue " +
            "@adjust sharpen $sharpen " +
            "@vignette $vignette 1.0" +
            "@adjust fade $fade" +
            "@adjust grain $grain"
}