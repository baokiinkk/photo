package com.avnsoft.photoeditor.photocollage.ui.activities.editor.draw

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ExperimentalComposeApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.avnsoft.photoeditor.photocollage.R
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.FeaturePhotoHeader
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.EditorActivity
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.blur.BrushShapeSlider
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.crop.ToolInput
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.draw.lib.BrushDrawingView
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.draw.lib.BrushViewChangeListener
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.draw.lib.DrawBitmapModel
import com.avnsoft.photoeditor.photocollage.ui.theme.AppColor
import com.avnsoft.photoeditor.photocollage.ui.theme.AppStyle
import com.avnsoft.photoeditor.photocollage.utils.FileUtil.toFile
import com.avnsoft.photoeditor.photocollage.utils.getInput
import com.basesource.base.components.ColorPickerDialog
import com.basesource.base.ui.base.BaseActivity
import com.basesource.base.utils.ImageWidget
import com.basesource.base.utils.capturable
import com.basesource.base.utils.clickableWithAlphaEffect
import com.basesource.base.utils.rememberCaptureController
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class DrawActivity : BaseActivity() {

    private val viewmodel: DrawViewModel by viewModel()

    private val screenInput: ToolInput? by lazy {
        intent.getInput()
    }

    @OptIn(ExperimentalComposeUiApi::class, ExperimentalComposeApi::class)
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
                val captureController = rememberCaptureController()
                val scope = rememberCoroutineScope()
                val context = LocalContext.current


                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            bottom = inner.calculateBottomPadding()
                        )
                        .background(Color(0xFFF2F4F8))
                ) {
                    HeaderDraw(
                        viewmodel = viewmodel,
                        onBack = {
                            finish()
                        },
                        onSave = {
                            scope.launch {
                                try {
                                    val bitmap = captureController.toImageBitmap().asAndroidBitmap()
                                    val pathBitmap = bitmap.toFile(context)
                                    val intent = Intent()
                                    intent.putExtra(EditorActivity.PATH_BITMAP, "$pathBitmap")
                                    setResult(RESULT_OK, intent)
                                    finish()
                                } catch (ex: Throwable) {
                                    Toast.makeText(
                                        context,
                                        "Error ${ex.message}",
                                        Toast.LENGTH_SHORT
                                    )
                                        .show()
                                }
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    uiState.originBitmap?.let {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .capturable(captureController)
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
                                    drawInput = uiState.drawInput,
                                    listener = object : BrushViewChangeListener {
                                        override fun onStartDrawing() {
                                        }

                                        override fun onStopDrawing() {
                                            viewmodel.canUndo(true)
                                        }

                                        override fun onViewAdd(brushDrawingView: BrushDrawingView?) {
                                        }

                                        override fun onViewRemoved(brushDrawingView: BrushDrawingView?) {
                                        }

                                        override fun onUndo(isUndo: Boolean) {
                                            viewmodel.canUndo(isUndo)
                                            viewmodel.canRedo(true)
                                        }

                                        override fun onRedo(isRedo: Boolean) {
                                            viewmodel.canRedo(isRedo)
                                            viewmodel.canUndo(true)
                                        }

                                    }
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
fun HeaderDraw(
    viewmodel: DrawViewModel,
    onBack: () -> Unit,
    onSave: () -> Unit
) {
    val undoAndRedoState by viewmodel.undoAndRedoState.collectAsStateWithLifecycle()
    FeaturePhotoHeader(
        onBack = onBack,
        onUndo = {
            viewmodel.undo()
        },
        onRedo = {
            viewmodel.redo()
        },
        onSave = onSave,
        canUndo = undoAndRedoState.canUndo,
        canRedo = undoAndRedoState.canRedo
    )
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
                .padding(start = 16.dp, end = 16.dp)
                .horizontalScroll(rememberScrollState()),
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