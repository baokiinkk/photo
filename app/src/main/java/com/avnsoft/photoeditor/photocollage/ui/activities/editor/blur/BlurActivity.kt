package com.avnsoft.photoeditor.photocollage.ui.activities.editor.blur

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.avnsoft.photoeditor.photocollage.R
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.FeaturePhotoHeader
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.TEXT_TYPE
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.adjust.SliderTool
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.crop.ToolInput
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.crop.saveImage
import com.avnsoft.photoeditor.photocollage.ui.theme.AppColor
import com.avnsoft.photoeditor.photocollage.ui.theme.AppStyle
import com.avnsoft.photoeditor.photocollage.utils.getInput
import com.basesource.base.ui.base.BaseActivity
import com.basesource.base.ui.image.LoadImage
import com.basesource.base.utils.ImageWidget
import com.basesource.base.utils.clickableWithAlphaEffect
import org.koin.androidx.viewmodel.ext.android.viewModel

enum class TAB_BLUR(val index: Int, val res: Int) {
    SHAPE(0, R.string.shapes),
    BRUSH(1, R.string.brush)
}

class BlurActivity : BaseActivity() {

    private val viewmodel: BlurViewModel by viewModel()

    private val screenInput: ToolInput? by lazy {
        intent.getInput()
    }

    private lateinit var blurView: BlurView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        blurView = BlurView(this)
        blurView.tabShape()
        blurView.addSticker(
            getShapes()[0].item
        )
        viewmodel.initBitmap(screenInput?.getBitmap(this))

        enableEdgeToEdge()
        setContent {
            var selectedTab by remember { mutableStateOf(TAB_BLUR.SHAPE.index) }
            val uiState by viewmodel.uiState.collectAsStateWithLifecycle()
            var canUndo by remember { mutableStateOf(false) }
            var canRedo by remember { mutableStateOf(false) }
            Scaffold(
                containerColor = Color.White
            ) { inner ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            bottom = inner.calculateBottomPadding()
                        )
                        .background(Color(0xFFF2F4F8))
                ) {
                    FeaturePhotoHeader(
                        onBack = {
                            finish()
                        },
                        onUndo = {
                            blurView.undo()
                        },
                        onRedo = {
                            blurView.redo()
                        },
                        onSave = {
                            uiState.bitmap?.let {
                                saveImage(
                                    context = this@BlurActivity,
                                    bitmap = blurView.getBitmap(it),
                                    onImageSaved = { pathBitmap ->
                                        val intent = Intent()
                                        intent.putExtra("pathBitmap", "$pathBitmap")
                                        setResult(RESULT_OK, intent)
                                        finish()
                                    }
                                )
                            }
                        },
                        canUndo = canUndo,
                        canRedo = canRedo,
                        canSave = true,
                        textRight = stringResource(R.string.apply),
                        type = TEXT_TYPE.TEXT
                    )

                    Spacer(modifier = Modifier.height(28.dp))
                    uiState.bitmap?.let {
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
                                BlurView(
                                    modifier = Modifier.fillMaxSize(),
                                    blurView = blurView,
                                    bitmap = it,
                                    intensity = uiState.blurBitmap
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    if (selectedTab == TAB_BLUR.SHAPE.index) {
                        blurView.tabShape()
                        canUndo = false
                        canRedo = false
                    } else {
                        canUndo = true
                        canRedo = true
                        blurView.tabBrush()
                    }
                    BlurToolPanel(
                        uiState = uiState,
                        modifier = Modifier
                            .fillMaxWidth(),
                        onTabSelected = {
                            selectedTab = it
                        },
                        selectedTab = selectedTab,
                        onItemClick = {
                            viewmodel.selectedItem(it)
                            blurView.addSticker(it.item)
                        },
                        onSliderShapeChange = {
                            viewmodel.updateBlur(it)
                        },
                        onSliderBrushChange = {
                            viewmodel.updateBlurBrush(it)
                            blurView.setBrushBitmapSize(it.toInt() + 25)
                        }
                    )
                }
            }

        }
    }
}

@Composable
fun BlurToolPanel(
    uiState: BlurUIState,
    modifier: Modifier = Modifier,
    selectedTab: Int = TAB_BLUR.SHAPE.index,
    onTabSelected: (Int) -> Unit,
    onSliderShapeChange: (Float) -> Unit,
    onSliderBrushChange: (Float) -> Unit,
    onItemClick: (Shape) -> Unit
) {

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .padding(vertical = 16.dp)
    ) {

        // Tabs (Shapes / Brush)
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            TabButton(
                0,
                stringResource(TAB_BLUR.SHAPE.res),
                selectedTab == TAB_BLUR.SHAPE.index,
                onTabSelected
            )
            TabButton(
                1,
                stringResource(TAB_BLUR.BRUSH.res),
                selectedTab == TAB_BLUR.BRUSH.index,
                onTabSelected
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Slider + value
        if (selectedTab == TAB_BLUR.SHAPE.index) {
            SliderTool(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                value = uiState.blurBitmap,
                onValueChange = onSliderShapeChange,
                valueRange = 0f..100f
            )
            Spacer(modifier = Modifier.height(20.dp))

            // Horizontal filter list
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(uiState.shapes) { item ->
                    BlurItem(
                        item = item,
                        isSelected = uiState.shapeId == item.id,
                        onClick = {
                            onItemClick.invoke(item)
                        }
                    )
                }
            }
        } else {
            SliderTool(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                value = uiState.blurBitmap,
                onValueChange = onSliderShapeChange,
                valueRange = 0f..100f
            )

            Spacer(modifier = Modifier.height(20.dp))

            BrushShapeSlider(
                value = uiState.blurBrush,
                onValueChange = onSliderBrushChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))
    }
}

@Composable
fun BrushShapeSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    onValueChangeFinished: (() -> Unit)? = null
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ImageWidget(
            resId = R.drawable.ic_brush_24,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(20.dp))

        SliderZoom(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .weight(1f),
            onValueChangeFinished = onValueChangeFinished
        )

        Spacer(modifier = Modifier.width(20.dp))

        Text(
            text = value.toInt().toString(),
            style = AppStyle.body1().medium().gray800()
        )
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SliderZoom(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    onValueChangeFinished: (() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
//        Canvas(
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(14.dp)
//        ) {
//            val width = size.width
//            val height = size.height
//
//            // Độ dày đầu nhỏ / đầu to
//            val startThickness = height * 0.3f
//            val endThickness = height * 0.9f
//
//            val startRadius = startThickness / 2f
//            val endRadius = endThickness / 2f
//            val centerY = height / 2f
//
//            // Vẽ phần giữa (hình tứ giác nối hai đầu)
//            val path = Path().apply {
//                moveTo(startRadius, centerY - startRadius)
//                lineTo(width - endRadius, centerY - endRadius)
//                lineTo(width - endRadius, centerY + endRadius)
//                lineTo(startRadius, centerY + startRadius)
//                close()
//            }
//
//            drawPath(
//                path = path,
//                color = Color(0xFFE9EDF3)
//            )
//
//            // Vẽ hai đầu tròn
//            drawCircle(
//                color = Color(0xFFE9EDF3),
//                radius = startRadius,
//                center = Offset(startRadius, centerY)
//            )
//
//            drawCircle(
//                color = Color(0xFFE9EDF3),
//                radius = endRadius,
//                center = Offset(width - endRadius, centerY)
//            )
//        }

        ImageWidget(
            resId = R.drawable.bg_line_blur,
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
        )
        // Slider chỉ để nhận drag và hiển thị thumb
        Slider(
            value = value,
            onValueChange = onValueChange,
            onValueChangeFinished = onValueChangeFinished,
            valueRange = 0f..100f,
            modifier = Modifier
                .fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFF0F1826),
                activeTrackColor = Color.Transparent,
                inactiveTrackColor = Color.Transparent
            ),
            thumb = {
                Box(
                    modifier = Modifier
                        .size(20.dp) // Tăng kích thước!
                        .background(
                            color = AppColor.Gray800,
                            shape = CircleShape
                        )
                )
            }
        )
    }
}


@Composable
private fun TabButton(
    tabIndex: Int,
    text: String,
    selected: Boolean,
    onTabSelected: (Int) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickableWithAlphaEffect { onTabSelected(tabIndex) }
    ) {
        val icon =
            if (tabIndex == TAB_BLUR.SHAPE.index) R.drawable.ic_shapes else R.drawable.ic_brush

        ImageWidget(
            modifier = Modifier.size(32.dp),
            resId = icon,
            tintColor = if (selected) AppColor.Primary500 else AppColor.Gray900,
        )

        Text(
            text = text,
            color = if (selected) AppColor.Primary500 else Color.Gray,
            style = AppStyle.caption2().semibold().primary500()
        )
    }
}

@Composable
private fun BlurItem(
    item: Shape,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(64.dp)
            .clickableWithAlphaEffect(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .aspectRatio(1f)
                .clip(RoundedCornerShape(12.dp))
                .border(
                    width = if (isSelected) 2.dp else 0.dp,
                    color = if (isSelected) AppColor.Primary500 else Color.Transparent,
                    shape = RoundedCornerShape(12.dp)
                )
        ) {
//            Image(
//                painter = painterResource(id = item.icon),
//                contentDescription = null,
//                contentScale = ContentScale.Crop,
//                modifier = Modifier.fillMaxSize()
//            )
            LoadImage(
                model = "file:///android_asset/${item.iconUrl}",
                contentScale = ContentScale.Crop,
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = stringResource(item.text),
            style = if (isSelected) AppStyle.caption2().medium()
                .primary500() else AppStyle.caption2().medium().gray800()
        )
    }
}