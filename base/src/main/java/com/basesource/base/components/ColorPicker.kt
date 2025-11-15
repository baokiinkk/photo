package com.basesource.base.components

import android.util.Log
import androidx.annotation.StringRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.basesource.base.R
import com.basesource.base.utils.clickableWithAlphaEffect
import kotlin.math.roundToInt

@Composable
fun ColorPicker(
    modifier: Modifier = Modifier,
    title: String,
    titleStyle: TextStyle = LocalTextStyle.current,
    btnStyle: TextStyle = LocalTextStyle.current,
    colorsDefault: List<Color> = mutableListOf(),
    selectedColor: Color?,
    addColor: (Color) -> Unit,
    onColorSelected: (Color) -> Unit,
    @StringRes confirmText: Int,
    @StringRes cancelText: Int,
) {
    var showColorDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Text(
            text = title,
            style = titleStyle,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                modifier = Modifier
                    .size(30.dp)
                    .clickableWithAlphaEffect(onClick = {
                        showColorDialog = true
                    }),
                contentScale = ContentScale.Crop,
                painter = painterResource(R.drawable.ic_choose_color),
                contentDescription = "Icon Color Picker Icon",
            )
            Spacer(modifier = Modifier.width(12.dp))
            LazyRow(
                modifier = Modifier.weight(1f), // Allow LazyRow to take available space
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically // Also align items vertically in LazyRow
            ) {
                items(colorsDefault) { color ->
                    ThemeColorCircle(
                        color = color,
                        isSelected = selectedColor == color,
                        onClick = {
                            onColorSelected.invoke(color)
                        }
                    )
                }
            }
        }
    }
    if (showColorDialog) {
        ColorPickerDialog(
            textStyle = btnStyle,
            selectedColor = selectedColor,
            onColorSelected = { color ->
                onColorSelected(color)
                showColorDialog = false
                addColor(color)
            },
            onDismiss = { showColorDialog = false },
            confirmText = confirmText,
            cancelText = cancelText,
        )
    }
}

@Composable
fun ThemeColorCircle(color: Color, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .width(12.dp)
            .height(if (isSelected) 36.dp else 28.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(color)
            .clickableWithAlphaEffect(onClick = onClick)
    )
}

// Helper functions for color conversion
private fun Color.toHsv(): FloatArray {
    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(this.toArgb(), hsv)
    return hsv
}

fun hsvToColor(h: Float, s: Float, v: Float, alpha: Float = 1f): Color {
    val hh = ((h % 360f) + 360f) % 360f        // normalize
    val c = v * s                               // chroma
    val x = c * (1 - kotlin.math.abs((hh / 60f) % 2 - 1))
    val m = v - c

    val (r1, g1, b1) = when {
        hh < 60f  -> Triple(c, x, 0f)
        hh < 120f -> Triple(x, c, 0f)
        hh < 180f -> Triple(0f, c, x)
        hh < 240f -> Triple(0f, x, c)
        hh < 300f -> Triple(x, 0f, c)
        else      -> Triple(c, 0f, x)
    }

    val r = ((r1 + m) * 255).toInt().coerceIn(0, 255)
    val g = ((g1 + m) * 255).toInt().coerceIn(0, 255)
    val b = ((b1 + m) * 255).toInt().coerceIn(0, 255)
    val a = (alpha.coerceIn(0f, 1f) * 255).toInt()

    return Color(r, g, b, a)
}
@Composable
fun ColorPickerDialog(
    modifier: Modifier = Modifier,
    selectedColor: Color? = null,
    onColorSelected: (Color) -> Unit,
    onDismiss: () -> Unit,
    textStyle: TextStyle = LocalTextStyle.current,
    @StringRes confirmText: Int,
    @StringRes cancelText: Int,
) {
    Dialog(
        onDismissRequest = onDismiss,
    ) {
        ColorPickerUI(
            modifier = modifier,
            onColorSelected = onColorSelected,
            onDismiss = onDismiss,
            textStyle = textStyle,
            confirmText = confirmText,
            cancelText = cancelText
        )
    }
}


@Composable
fun ColorPickerUI(
    modifier: Modifier = Modifier,
    onColorSelected: (Color) -> Unit,
    onDismiss: () -> Unit,
    textStyle: TextStyle = LocalTextStyle.current,
    @StringRes confirmText: Int,
    @StringRes cancelText: Int,
) {

    val initialColor = Color(0xFF8F82FF)
    val initialHsv = remember { initialColor.toHsv() }

    var hue by remember { mutableFloatStateOf(initialHsv[0]) }
    var saturation by remember { mutableFloatStateOf(initialHsv[1]) }
    var brightness by remember { mutableFloatStateOf(initialHsv[2]) }
    var alpha by remember { mutableFloatStateOf(initialColor.alpha) }

    // Call onColorSelected whenever color changes
    // Calculate current color directly in LaunchedEffect to ensure it's always up-to-date
    LaunchedEffect(hue, saturation, brightness, alpha) {
        val currentColor = hsvToColor(hue, saturation, brightness, alpha)
        Log.d("quocbao",alpha.toString() +"- color:"+ currentColor.toString())
        onColorSelected(currentColor)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = Color.White,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(20.dp)
    )
    {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Color Field (Saturation and Brightness)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomEnd = 10.dp, bottomStart = 10.dp))
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                val size = this.size
                                val x = (offset.x / size.width).coerceIn(0f, 1f)
                                val y = (offset.y / size.height).coerceIn(0f, 1f)
                                saturation = x
                                brightness = 1f - y
                            },
                            onDrag = { change, _ ->
                                val size = this.size
                                val x = (change.position.x / size.width).coerceIn(0f, 1f)
                                val y = (change.position.y / size.height).coerceIn(0f, 1f)
                                saturation = x
                                brightness = 1f - y
                            }
                        )
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height

                    // Draw color field: horizontal = saturation, vertical = brightness
                    // Draw smooth saturation gradient (left to right) using Brush.horizontalGradient
                    // Gradient sẽ tự động cập nhật khi hue thay đổi vì Canvas recompose
                    val saturationSteps = 300 // Tăng số bước để gradient mịn như hue slider
                    val saturationColors = (0..saturationSteps).map { step ->
                        val sat = step.toFloat() / saturationSteps
                        hsvToColor(hue, sat, 1f, 1f)
                    }

                    drawRect(
                        brush = Brush.horizontalGradient(saturationColors),
                        topLeft = Offset(0f, 0f),
                        size = Size(width, height)
                    )

                    // Draw brightness gradient overlay (top = transparent, bottom = black)
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black)
                        )
                    )

                    // Draw selector circle - giới hạn trong bounds
                    val selectorRadius = 12.dp.toPx()
                    val selectorX = (saturation * width).coerceIn(selectorRadius, width - selectorRadius)
                    val selectorY = ((1f - brightness) * height).coerceIn(selectorRadius, height - selectorRadius)
                    drawCircle(
                        color = Color.White,
                        radius = selectorRadius,
                        center = Offset(selectorX, selectorY),
                        style = Stroke(width = 3.dp.toPx())
                    )
                    drawCircle(
                        color = Color.Black,
                        radius = 10.dp.toPx(),
                        center = Offset(selectorX, selectorY),
                        style = Stroke(width = 1.dp.toPx())
                    )
                }

                // Hue Slider (Rainbow)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(18.dp)
                        .align(Alignment.BottomCenter)
                        .clip(RoundedCornerShape(34.dp))
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    val size = this.size
                                    val x = (offset.x / size.width).coerceIn(0f, 1f)
                                    hue = x * 360f
                                },
                                onDrag = { change, _ ->
                                    val size = this.size
                                    val x = (change.position.x / size.width).coerceIn(0f, 1f)
                                    hue = x * 360f
                                }
                            )
                        }
                )
                {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val width = size.width
                        val height = size.height

                        // Draw smooth rainbow gradient using Brush.horizontalGradient
                        // Tạo nhiều color stops để gradient mịn mượt, không bị từng khúc
                        val gradientSteps = 360 // Mỗi độ hue một màu để đảm bảo mịn nhất
                        val colors = (0..gradientSteps).map { step ->
                            val h = (step.toFloat() / gradientSteps) * 360f
                            hsvToColor(h, 1f, 1f, 1f)
                        }

                        drawRect(
                            brush = Brush.horizontalGradient(colors),
                            topLeft = Offset(0f, 0f),
                            size = Size(width, height)
                        )

                        // Draw selector - giới hạn trong bounds
                        val selectorRadius = 8.dp.toPx()
                        val selectorX = ((hue / 360f) * width).coerceIn(selectorRadius, width - selectorRadius)
                        drawCircle(
                            color = Color.White,
                            radius = selectorRadius,
                            center = Offset(selectorX, height / 2f),
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }
                }
            }

            // Alpha/Opacity Slider (Custom with checkered background)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp)
                    .clip(RoundedCornerShape(50.dp))
            ) {
                // Checkered background and alpha gradient
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val tileSize = 8.dp.toPx()
                    val width = size.width
                    val height = size.height

                    // Draw checkered pattern
                    for (x in 0..(width / tileSize).toInt()) {
                        for (y in 0..(height / tileSize).toInt()) {
                            val isEven = (x + y) % 2 == 0
                            drawRect(
                                color = if (isEven) Color.LightGray else Color.White,
                                topLeft = Offset(x * tileSize, y * tileSize),
                                size = Size(tileSize, tileSize)
                            )
                        }
                    }

                    // Draw alpha gradient overlay (transparent to opaque)
                    val baseColor = hsvToColor(hue, saturation, brightness, 1f)
                    drawRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                baseColor.copy(alpha = 0f),
                                baseColor.copy(alpha = 1f)
                            )
                        )
                    )

                    // Draw selector - giới hạn trong bounds
                    val selectorRadius = 8.dp.toPx()
                    val selectorX = (alpha * width).coerceIn(selectorRadius, width - selectorRadius)
                    drawCircle(
                        color = Color.White,
                        radius = selectorRadius,
                        center = Offset(selectorX, height / 2f),
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
                
                // Pointer input layer on top of Canvas to handle gestures
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    val size = this.size
                                    val x = (offset.x / size.width).coerceIn(0f, 1f)
                                    alpha = x
                                },
                                onDrag = { change, _ ->
                                    val size = this.size
                                    val x = (change.position.x / size.width).coerceIn(0f, 1f)
                                    alpha = x
                                }
                            )
                        }
                )
            }
        }
    }
}

