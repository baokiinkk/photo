package com.avnsoft.photoeditor.photocollage.ui.activities.editor.draw

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.avnsoft.photoeditor.photocollage.R
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.FeaturePhotoHeader
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.blur.BrushShapeSlider
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.crop.ToolInput
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.draw.lib.DrawBitmapModel
import com.avnsoft.photoeditor.photocollage.ui.theme.AppColor
import com.avnsoft.photoeditor.photocollage.ui.theme.AppStyle
import com.avnsoft.photoeditor.photocollage.utils.getInput
import com.basesource.base.components.ColorPickerDialog
import com.basesource.base.ui.base.BaseActivity
import com.basesource.base.utils.ImageWidget
import com.basesource.base.utils.clickableWithAlphaEffect
import org.koin.androidx.viewmodel.ext.android.viewModel

class DrawActivity : BaseActivity() {

    private val viewmodel: DrawViewModel by viewModel()

    private val screenInput: ToolInput? by lazy {
        intent.getInput()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewmodel.getConfig(screenInput?.getBitmap(this))

        setContent {
            Scaffold(
                containerColor = Color.White
            ) { inner ->

                val uiState by viewmodel.uiState.collectAsStateWithLifecycle()
                var showColorWheel by remember { mutableStateOf(false) }
                var currentSelectedColor by remember {
                    mutableStateOf(
                        Color.White
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            top = inner.calculateTopPadding(),
                            bottom = inner.calculateBottomPadding()
                        )
                        .background(Color(0xFFF2F4F8))
                ) {
                    FeaturePhotoHeader(
                        onBack = {
                            finish()
                        },
                        onUndo = {
                            viewmodel.undo()
                        },
                        onRedo = {
                            viewmodel.redo()
                        },
                        onSave = {

                        },
                        canUndo = true,
                        canRedo = true
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    uiState.originBitmap?.let {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .aspectRatio(it.width / it.height.toFloat())
                                    .align(Alignment.Center)

                            ) {
                                Image(
                                    bitmap = it.asImageBitmap(),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxSize()
                                )
                                DrawComposeView(
                                    modifier = Modifier
                                        .fillMaxSize(),
                                    drawInput = uiState.drawInput
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(36.dp))
                    DrawFooter(
                        modifier = Modifier
                            .fillMaxWidth(),
                        uiState = uiState,
                        onTabSelected = {
                            viewmodel.onTabSelected(it)
                        },
                        onSizeColorChange = {
                            viewmodel.onSizeChange(it)
                        },
                        onSelectedColor = {
                            viewmodel.onSelectedColor(it)
                        },
                        onShowSystemColor = {
                            showColorWheel = true
                        },
                        onPatternSelected = {
                            viewmodel.onPatternSelected(it)
                        }
                    )
                }

                if (showColorWheel) {
                    ColorPickerDialog(
                        selectedColor = currentSelectedColor,
                        onColorSelected = { color ->
                            currentSelectedColor = color
                            viewmodel.onSelectedColor(color)
//                            showColorWheel = false
                        },
                        onDismiss = {
                            showColorWheel = false
                        },
                        textStyle = AppStyle.body1().medium().gray900(),
                        confirmText = R.string.confirm,
                        cancelText = R.string.cancel
                    )
                }
            }
        }
    }
}

@Composable
fun DrawFooter(
    modifier: Modifier,
    uiState: DrawUIState,
    onTabSelected: (DrawTabData) -> Unit,
    onSizeColorChange: (Float) -> Unit,
    onSelectedColor: (Color) -> Unit,
    onShowSystemColor: () -> Unit,
    onPatternSelected: (DrawBitmapModel) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                uiState.tabs.forEach { item ->
                    DrawTabView(
                        item = item,
                        isSelected = item.tab == uiState.currentTab,
                        onClick = {
                            onTabSelected.invoke(item)
                        }
                    )
                }
            }
        }
        when (uiState.currentTab) {
            DrawTabData.TAB.Solid -> {
                val solid = uiState.drawColor
                Spacer(modifier = Modifier.height(20.dp))
                TabDrawColor(
                    sliderValue = solid.size,
                    onSliderChange = onSizeColorChange,
                    onSelectedColor = onSelectedColor,
                    onShowSystemColor = onShowSystemColor,
                    selectedColor = solid.color
                )
            }

            DrawTabData.TAB.Pattern -> {
                Spacer(modifier = Modifier.height(20.dp))
                TabPattern(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    sliderValue = uiState.drawPattern.size,
                    onSliderChange = onSizeColorChange,
                    uiState = uiState,
                    onPatternSelected = onPatternSelected
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            DrawTabData.TAB.Neon -> {
                val solid = uiState.drawNeon
                Spacer(modifier = Modifier.height(20.dp))
                TabDrawColor(
                    sliderValue = solid.size,
                    onSliderChange = onSizeColorChange,
                    onSelectedColor = onSelectedColor,
                    onShowSystemColor = onShowSystemColor,
                    selectedColor = solid.color
                )
            }
        }

    }
}

@Composable
fun TabPattern(
    modifier: Modifier,
    uiState: DrawUIState,
    sliderValue: Float,
    onSliderChange: (Float) -> Unit,
    onPatternSelected: (DrawBitmapModel) -> Unit
) {
    Column {
        BrushShapeSlider(
            value = sliderValue,
            onValueChange = onSliderChange,
            modifier = modifier
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(uiState.patterns) { item ->
                ItemPattern(
                    isSelected = item.mainIcon == uiState.patternSelected,
                    item = item,
                    onPatternSelected = onPatternSelected
                )
            }
        }

    }
}

@Composable
fun ItemPattern(
    isSelected: Boolean,
    item: DrawBitmapModel,
    onPatternSelected: (DrawBitmapModel) -> Unit
) {
    val modifier = if (isSelected) {
        Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = 1.dp,
                color = AppColor.Primary500,
                shape = RoundedCornerShape(12.dp)
            )
            .clickableWithAlphaEffect {
                onPatternSelected.invoke(item)
            }
    } else {
        Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickableWithAlphaEffect {
                onPatternSelected.invoke(item)
            }
    }
    Box(
        modifier = modifier
    ) {
        ImageWidget(
            resId = item.mainIcon,
            modifier = Modifier
                .fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
fun TabDrawColor(
    modifier: Modifier = Modifier,
    sliderValue: Float,
    selectedColor: Color,
    onSliderChange: (Float) -> Unit,
    onSelectedColor: (Color) -> Unit,
    onShowSystemColor: () -> Unit
) {
    val colors: List<Color> = listOf(
        Color(0xFFF7F8F3),
        Color(0xFFFFF7EC),
        Color(0xFFFAEDE7),
        Color(0xFFA9E2F5),
        Color(0xFFFFBBBE),
        Color(0xFFFF8B0D),
        Color(0xFFAADE87)
    )
    Column {
        BrushShapeSlider(
            value = sliderValue,
            onValueChange = onSliderChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ImageWidget(
                resId = R.drawable.ic_color,
                modifier = Modifier
                    .size(32.dp)
                    .clickableWithAlphaEffect(onClick = onShowSystemColor)
            )
            colors.forEach { item ->
                val selected = item == selectedColor
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(item, CircleShape)
                            .clickableWithAlphaEffect {
                                onSelectedColor.invoke(item)
                            }
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .background(
                                if (selected) AppColor.Primary500 else Color.Transparent,
                                CircleShape
                            )
                    )
                }

            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

}


@Composable
fun DrawTabView(
    item: DrawTabData,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val color = if (isSelected) {
        AppColor.Primary500
    } else {
        Color(0xFF1D2939)
    }
    Column(
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .clickableWithAlphaEffect(onClick = onClick)
    ) {
        ImageWidget(
            resId = item.icon,
            modifier = Modifier.size(32.dp),
            tintColor = color
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = stringResource(item.stringResId),
            style = if (isSelected) AppStyle.caption2().medium()
                .primary500() else AppStyle.caption2().medium().Color_1D2939(),
        )
    }
}