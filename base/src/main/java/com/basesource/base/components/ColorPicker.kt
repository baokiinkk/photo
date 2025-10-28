package com.basesource.base.components

import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.basesource.base.R
import com.basesource.base.utils.clickableWithAlphaEffect
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController

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

@Composable
fun ColorPickerDialog(
    selectedColor: Color? = null,
    onColorSelected: (Color) -> Unit,
    onDismiss: () -> Unit,
    textStyle: TextStyle = LocalTextStyle.current,
    @StringRes confirmText: Int,
    @StringRes cancelText: Int,
) {
    val controller = rememberColorPickerController()
    var currentSelectedColor by remember { mutableStateOf(selectedColor ?: Color(0xFF8F82FF)) }

    // Initialize color picker with selected color
    LaunchedEffect(selectedColor) {
        selectedColor?.let { color ->
            currentSelectedColor = color
        }
    }

    // Preset colors matching the design
    val presetColors = listOf(
        Color.Black,
        Color(0xFF2196F3), // Bright Blue
        Color(0xFF4CAF50), // Green
        Color(0xFFFFEB3B), // Yellow
        Color(0xFFF44336), // Red
        Color(0xFF03DAC6), // Light Blue
        Color(0xFF9C27B0), // Purple
        Color(0xFF1976D2), // Dark Blue
        Color(0xFFE91E63),  // Pink/Red
        Color(0xFFD004F5),

    )

    Dialog(
        onDismissRequest = onDismiss,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Color Wheel Section
                Box(
                    modifier = Modifier
                        .size(248.dp)
                        .border(
                            1.dp,
                            color = Color(0xFF9BC9FF),
                            shape = RoundedCornerShape(900.dp)
                        )
                        .align(Alignment.CenterHorizontally)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    HsvColorPicker(
                        modifier = Modifier
                            .fillMaxSize(),
                        controller = controller,
                        initialColor = selectedColor,
                        onColorChanged = { colorEnvelope ->
                            currentSelectedColor = colorEnvelope.color
                        }
                    )
                }

                // Selected Color Display and Preset Colors
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Selected Color Display
                    Box(
                        modifier = Modifier
                            .size(78.dp)
                            .background(
                                color = currentSelectedColor,
                                shape = RoundedCornerShape(8.dp)
                            )
                    )

                    // Preset Colors Grid
                    Column(
                        modifier = Modifier.padding(start = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        // First row
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            presetColors.take(5).forEach { color ->
                                PresetColorCircle(
                                    color = color,
                                    isSelected = currentSelectedColor == color,
                                    onClick = {
                                        currentSelectedColor = color
                                    }
                                )
                            }
                        }

                        // Second row
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            presetColors.drop(5).forEach { color ->
                                PresetColorCircle(
                                    color = color,
                                    isSelected = currentSelectedColor == color,
                                    onClick = {
                                        currentSelectedColor = color
                                    }
                                )
                            }
                        }
                    }
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Close Button
                    CustomButton(
                        text = stringResource(cancelText),
                        onClick = {
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .border(
                                width = 1.dp,
                                color = Color(0xFFA59BFF),
                                RoundedCornerShape(16.dp)
                            ),
                        colors = listOf(Color.White,Color.White),
                        textStyle = textStyle.copy(
                            color = Color(0xFFA59BFF)
                        )
                    )
                    // Save Button
                    CustomButton(
                        text = stringResource(confirmText),
                        onClick = {
                            onColorSelected(currentSelectedColor)
                        },
                        modifier = Modifier.weight(1f),
                        textStyle = textStyle
                    )
                }
            }
        }
    }
}

