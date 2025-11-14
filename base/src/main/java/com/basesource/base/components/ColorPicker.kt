package com.basesource.base.components

import androidx.annotation.StringRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.basesource.base.R
import com.basesource.base.utils.clickableWithAlphaEffect
import kotlin.math.max
import kotlin.math.min
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

@Composable
fun PresetColorCircle(color: Color, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .background(
                color = color,
                shape = CircleShape
            )
            .then(
                if (isSelected) {
                    Modifier.border(
                        width = 2.dp,
                        color = Color(0xFF2961EE),
                        shape = CircleShape
                    )
                } else {
                    Modifier
                }
            )
            .clickableWithAlphaEffect(onClick = onClick)
    )
}

// Helper functions for color conversion
private fun Color.toHsv(): FloatArray {
    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(this.toArgb(), hsv)
    return hsv
}

private fun hsvToColor(h: Float, s: Float, v: Float, alpha: Float = 1f): Color {
    val argb = android.graphics.Color.HSVToColor(
        (alpha * 255).roundToInt(),
        floatArrayOf(h, s, v)
    )
    return Color(argb)
}

@Composable
fun ColorPickerDialog(
    selectedColor: Color? = null,
    onColorSelected: (Color) -> Unit,
    onDismiss: () -> Unit,
    textStyle: TextStyle = LocalTextStyle.current,
    @StringRes confirmText: Int,
    @StringRes cancelText: Int,
) {
    val initialColor = selectedColor ?: Color(0xFF8F82FF)
    val initialHsv = remember { initialColor.toHsv() }
    
    var hue by remember { mutableFloatStateOf(initialHsv[0]) }
    var saturation by remember { mutableFloatStateOf(initialHsv[1]) }
    var brightness by remember { mutableFloatStateOf(initialHsv[2]) }
    var alpha by remember { mutableFloatStateOf(initialColor.alpha) }
    
    // Calculate current color from HSV
    val currentColor = remember(hue, saturation, brightness, alpha) {
        hsvToColor(hue, saturation, brightness, alpha)
    }

    // Initialize with selected color
    LaunchedEffect(selectedColor) {
        selectedColor?.let { color ->
            val hsv = color.toHsv()
            hue = hsv[0]
            saturation = hsv[1]
            brightness = hsv[2]
            alpha = color.alpha
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header with X and Checkmark
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // X button - using text for now, can be replaced with icon if available
                    Text(
                        text = "✕",
                        style = textStyle,
                        modifier = Modifier
                            .size(24.dp)
                            .clickableWithAlphaEffect(onClick = onDismiss)
                    )
                    Text(
                        text = "Color",
                        style = textStyle
                    )
                    // Checkmark button - using text for now, can be replaced with icon if available
                    Text(
                        text = "✓",
                        style = textStyle,
                        modifier = Modifier
                            .size(24.dp)
                            .clickableWithAlphaEffect(onClick = {
                                onColorSelected(currentColor)
                            })
                    )
                }

                // Color Field (Saturation and Brightness)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragEnd = {},
                                onDrag = { change, _ ->
                                    val size = this.size
                                    val x = (change.position.x / size.width).coerceIn(0f, 1f)
                                    val y = (change.position.y / size.height).coerceIn(0f, 1f)
                                    saturation = x
                                    brightness = 1f - y
                                }
                            )
                        }
                        .pointerInput(Unit) {
                            detectTapGestures { tapOffset ->
                                val size = this.size
                                val x = (tapOffset.x / size.width).coerceIn(0f, 1f)
                                val y = (tapOffset.y / size.height).coerceIn(0f, 1f)
                                saturation = x
                                brightness = 1f - y
                            }
                        }
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val width = size.width
                        val height = size.height
                        
                        // Draw color field: horizontal = saturation, vertical = brightness
                        // Draw saturation gradient (left to right)
                        val steps = 20
                        for (i in 0 until steps) {
                            val x1 = (i.toFloat() / steps) * width
                            val x2 = ((i + 1).toFloat() / steps) * width
                            val sat = (i + 0.5f) / steps
                            val colorAtSat = hsvToColor(hue, sat, 1f)
                            drawRect(
                                color = colorAtSat,
                                topLeft = Offset(x1, 0f),
                                size = Size(x2 - x1, height)
                            )
                        }
                        
                        // Draw brightness gradient overlay (top = transparent, bottom = black)
                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black)
                            )
                        )
                        
                        // Draw selector circle
                        val selectorX = saturation * width
                        val selectorY = (1f - brightness) * height
                        drawCircle(
                            color = Color.White,
                            radius = 12.dp.toPx(),
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
                }

                // Hue Slider (Rainbow)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragEnd = {},
                                onDrag = { change, _ ->
                                    val size = this.size
                                    val x = (change.position.x / size.width).coerceIn(0f, 1f)
                                    hue = x * 360f
                                }
                            )
                        }
                        .pointerInput(Unit) {
                            detectTapGestures { tapOffset ->
                                val size = this.size
                                val x = (tapOffset.x / size.width).coerceIn(0f, 1f)
                                hue = x * 360f
                            }
                        }
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val width = size.width
                        val height = size.height
                        
                        // Draw rainbow gradient
                        val steps = 20
                        for (i in 0 until steps) {
                            val x1 = (i.toFloat() / steps) * width
                            val x2 = ((i + 1).toFloat() / steps) * width
                            val h = ((i + 0.5f) / steps) * 360f
                            val color = hsvToColor(h, 1f, 1f)
                            drawRect(
                                color = color,
                                topLeft = Offset(x1, 0f),
                                size = Size(x2 - x1, height)
                            )
                        }
                        
                        // Draw selector
                        val selectorX = (hue / 360f) * width
                        drawCircle(
                            color = Color.White,
                            radius = 8.dp.toPx(),
                            center = Offset(selectorX, height / 2f),
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }
                }

                // Alpha/Opacity Slider (Custom with checkered background)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragEnd = {},
                                onDrag = { change, _ ->
                                    val size = this.size
                                    val x = (change.position.x / size.width).coerceIn(0f, 1f)
                                    alpha = x
                                }
                            )
                        }
                        .pointerInput(Unit) {
                            detectTapGestures { tapOffset ->
                                val size = this.size
                                val x = (tapOffset.x / size.width).coerceIn(0f, 1f)
                                alpha = x
                            }
                        }
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
                        
                        // Draw selector
                        val selectorX = alpha * width
                        drawCircle(
                            color = Color.White,
                            radius = 8.dp.toPx(),
                            center = Offset(selectorX, height / 2f),
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }
                }
            }
        }
    }
}

