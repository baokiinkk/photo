package com.avnsoft.photoeditor.photocollage.ui.activities.collage.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.avnsoft.photoeditor.photocollage.R
import com.avnsoft.photoeditor.photocollage.ui.theme.AppStyle
import com.basesource.base.components.ColorPickerDialog
import com.basesource.base.utils.clickableWithAlphaEffect
import kotlin.math.roundToInt

enum class BackgroundTab {
    SOLID,
    PATTERN,
    GRADIENT,
}

@Composable
fun BackgroundSheet(
    selectedTab: BackgroundTab = BackgroundTab.SOLID,
    selectedColor: String? = null,
    onColorSelect: (String) -> Unit,
    onClose: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentTab by remember(selectedTab) { mutableStateOf(selectedTab) }
    var showColorWheel by remember { mutableStateOf(false) }
    var initSelectColor by remember {
        mutableStateOf(
            selectedColor?.let {
                try {
                    Color((if (it.startsWith("#")) it else "#$it").toColorInt())
                } catch (e: Exception) {
                    Color.White
                }
            } ?: Color.White
        )
    }
    var currentSelectedColor by remember {
        mutableStateOf(
            selectedColor?.let {
                try {
                    Color((if (it.startsWith("#")) it else "#$it").toColorInt())
                } catch (e: Exception) {
                    Color.White
                }
            } ?: Color.White
        )
    }

    // Update currentSelectedColor when selectedColor changes externally
    LaunchedEffect(selectedColor) {
        selectedColor?.let {
            try {
                initSelectColor =  Color((if (it.startsWith("#")) it else "#$it").toColorInt())
                currentSelectedColor = Color((if (it.startsWith("#")) it else "#$it").toColorInt())
            } catch (e: Exception) {
                // Keep current color if parsing fails
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(Color.White, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
    ) {
        if (showColorWheel) {
            ColorPickerUI(
                onColorSelected = { color ->
                    currentSelectedColor = color
                    onColorSelect(currentSelectedColor.colorToHex())
                },
                selectedColor = initSelectColor,
                onDismiss = { showColorWheel = false },
                textStyle = AppStyle.body1().medium().gray900(),
                confirmText = R.string.confirm,
                cancelText = R.string.cancel
            )
        } else {
            // Tabs
            Row(
                modifier = Modifier
                    .padding(vertical = 16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf(BackgroundTab.SOLID, BackgroundTab.PATTERN, BackgroundTab.GRADIENT).forEach { tab ->
                    val tabText = when (tab) {
                        BackgroundTab.SOLID -> "Solid"
                        BackgroundTab.PATTERN -> "Pattern"
                        BackgroundTab.GRADIENT -> "Gradient"
                    }
                    Text(
                        text = tabText,
                        style = AppStyle.body2().medium().let {
                            if (currentTab == tab) it.white() else it.gray900()
                        },
                        modifier = Modifier
                            .background(
                                if (currentTab == tab) Color(0xFF9747FF) else Color(0xFFF3F4F6),
                                RoundedCornerShape(24.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                            .clickableWithAlphaEffect {
                                currentTab = tab
                            }
                    )
                    if (tab != BackgroundTab.GRADIENT) {
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(12.dp))
                    }
                }
            }
            when (currentTab) {
                BackgroundTab.SOLID -> {
                    SolidBackgroundTab(
                        selectedColor = currentSelectedColor,
                        onColorSelect = { color ->
                            currentSelectedColor = color
                            onColorSelect(currentSelectedColor.colorToHex())
                        },
                        onColorPickerClick = { showColorWheel = true },
                        onColorWheelClick = { showColorWheel = true }
                    )
                }

                BackgroundTab.PATTERN -> {
                    PatternBackgroundTab()
                }

                BackgroundTab.GRADIENT -> {
                    GradientBackgroundTab()
                }
            }
        }
        Spacer(modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Gray100))
        // Bottom Action Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose) {
                Icon(
                    modifier = Modifier.size(28.dp),
                    painter = painterResource(R.drawable.ic_close),
                    contentDescription = "Close",
                    tint = Gray500
                )
            }

            Text(
                text = stringResource(R.string.color),
                style = AppStyle.title2().semibold().gray900()
            )

            IconButton(onClick = onConfirm) {
                Icon(
                    modifier = Modifier.size(28.dp),
                    painter = painterResource(R.drawable.ic_confirm),
                    contentDescription = "Confirm",
                    tint = Gray900
                )
            }
        }
    }
}

@Composable
private fun SolidBackgroundTab(
    selectedColor: Color,
    onColorSelect: (Color) -> Unit,
    onColorPickerClick: () -> Unit,
    onColorWheelClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        // Color selection tools
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Color Picker (Eyedropper) button
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = Color(0xFF9747FF).copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickableWithAlphaEffect(onClick = onColorPickerClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check, // Placeholder - replace with proper icon
                    contentDescription = "Color Picker",
                    tint = Color(0xFF9747FF),
                    modifier = Modifier.size(20.dp)
                )
            }

            // Color Wheel button
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = Color(0xFFF3F4F6),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickableWithAlphaEffect(onClick = onColorWheelClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check, // Placeholder - replace with proper icon
                    contentDescription = "Color Wheel",
                    tint = Color(0xFF6B7280),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Color swatches grid
        val solidColors = listOf(
            Color(0xFFFFFFFF), // White
            Color(0xFF000000), // Black
            Color(0xFFE5E7EB), // Gray
            Color(0xFF9CA3AF), // Dark Gray
            Color(0xFFEF4444), // Red
            Color(0xFFF59E0B), // Orange
            Color(0xFFEAB308), // Yellow
            Color(0xFF84CC16), // Green
            Color(0xFF06B6D4), // Cyan
            Color(0xFF3B82F6), // Blue
            Color(0xFF8B5CF6), // Purple
            Color(0xFFEC4899), // Pink
            Color(0xFFF97316), // Orange Red
            Color(0xFF14B8A6), // Teal
            Color(0xFF6366F1), // Indigo
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(5),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(solidColors) { color ->
                SolidColorSwatch(
                    color = color,
                    isSelected = selectedColor.toArgb() == color.toArgb(),
                    onClick = { onColorSelect(color) }
                )
            }
        }
    }
}

@Composable
private fun SolidColorSwatch(
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .background(
                color = color,
                shape = CircleShape
            )
            .then(
                if (isSelected) {
                    Modifier.border(
                        width = 3.dp,
                        color = Color(0xFF9747FF),
                        shape = CircleShape
                    )
                } else {
                    Modifier.border(
                        width = 1.dp,
                        color = Color(0xFFE5E7EB),
                        shape = CircleShape
                    )
                }
            )
            .clickableWithAlphaEffect(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = if (color.toArgb() == Color.White.toArgb() || color.toArgb() == Color(0xFFE5E7EB).toArgb()) Color.Black else Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

fun Color.colorToHex(includeAlpha: Boolean = true): String {
    val a = (alpha * 255).roundToInt().coerceIn(0, 255)
    val r = (red * 255).roundToInt().coerceIn(0, 255)
    val g = (green * 255).roundToInt().coerceIn(0, 255)
    val b = (blue * 255).roundToInt().coerceIn(0, 255)

    return if (includeAlpha) {
        String.format("#%02X%02X%02X%02X", a, r, g, b)  // #AARRGGBB
    } else {
        String.format("#%02X%02X%02X", r, g, b)         // #RRGGBB
    }
}

@Composable
private fun PatternBackgroundTab() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 40.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Pattern options coming soon",
            style = AppStyle.body1().medium().gray600()
        )
    }
}

@Composable
private fun GradientBackgroundTab() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 40.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Gradient options coming soon",
            style = AppStyle.body1().medium().gray600()
        )
    }
}


@Preview(showBackground = true)
@Composable
private fun BackgroundSheetPreview() {
    BackgroundSheet(
        selectedTab = BackgroundTab.SOLID,
        selectedColor = "#3B82F6",
        onColorSelect = {},
        onClose = {},
        onConfirm = {}
    )
}

