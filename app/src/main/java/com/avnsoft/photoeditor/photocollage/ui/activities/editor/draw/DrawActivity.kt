package com.avnsoft.photoeditor.photocollage.ui.activities.editor.draw

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.avnsoft.photoeditor.photocollage.R
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.FeaturePhotoHeader
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.blur.BrushShapeSlider
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.crop.ToolInput
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.remove_object.RemoveObjectTab
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.text_sticker.TabColor
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.text_sticker.lib.TextSticker
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
                containerColor = Color(0xFFF2F4F8)
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
//                            viewmodel.undo()
                        },
                        onRedo = {
//                            viewmodel.redo()
                        },
                        onSave = { /* TODO */ },
                        canUndo = false,
                        canRedo = false
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
                                    drawInput = uiState.currentTab.drawInput
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
                            viewmodel.onSizeColorChange(it)
                        },
                        onSizePatternChange = {

                        },
                        onSelectedColor = {
                            viewmodel.onSelectedColor(it)
                        },
                        onShowSystemColor = {
                            showColorWheel = true
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
    onSizePatternChange: (Float) -> Unit,
    onSelectedColor: (Color) -> Unit,
    onShowSystemColor: () -> Unit,
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
                        isSelected = item.drawInput == uiState.currentTab,
                        onClick = {
                            onTabSelected.invoke(item)
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        when (uiState.currentTab.drawInput) {
            is DrawInput.DrawColor -> {
                TabColor(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    onSelectedColor = onSelectedColor,
                    sliderValue = uiState.currentTab.drawInput.size,
                    onSliderChange = onSizeColorChange,
                    onShowSystemColor = onShowSystemColor
                )
            }

            is DrawInput.DrawPattern -> {
                BrushShapeSlider(
                    value = uiState.currentTab.drawInput.size,
                    onValueChange = onSizePatternChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )
            }
        }

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