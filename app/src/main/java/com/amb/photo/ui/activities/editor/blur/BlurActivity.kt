package com.amb.photo.ui.activities.editor.blur

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.amb.photo.R
import com.amb.photo.ui.activities.collage.components.FeaturePhotoHeader
import com.amb.photo.ui.activities.editor.adjust.SliderTool
import com.amb.photo.ui.activities.editor.crop.ToolInput
import com.amb.photo.ui.theme.AppColor
import com.amb.photo.ui.theme.AppStyle
import com.amb.photo.utils.getInput
import com.basesource.base.ui.base.BaseActivity
import com.basesource.base.ui.image.LoadImage
import com.basesource.base.utils.ImageWidget
import com.basesource.base.utils.clickableWithAlphaEffect
import org.koin.androidx.viewmodel.ext.android.viewModel

class BlurActivity : BaseActivity() {

    private val viewmodel: BlurViewModel by viewModel()

    private val screenInput: ToolInput? by lazy {
        intent.getInput()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewmodel.initBitmap(screenInput?.getBitmap(this))

        enableEdgeToEdge()
        setContent {
            var selectedTab by remember { mutableStateOf(0) }
            val uiState by viewmodel.uiState.collectAsStateWithLifecycle()

            Scaffold(
                containerColor = Color.White
            ) { inner ->
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
                        onUndo = { /* TODO */ },
                        onRedo = { /* TODO */ },
                        onSave = { /* TODO */ },
                        canUndo = false,
                        canRedo = false
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
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    EditorToolPanel(
                        uiState = uiState,
                        modifier = Modifier
                            .fillMaxWidth(),
                        onTabSelected = {
                            selectedTab = it
                        },
                        selectedTab = selectedTab,
                        onItemClick = {
                            viewmodel.selectedItem(it)
                        }
                    )
                }
            }

        }
    }
}

@Composable
fun EditorToolPanel(
    uiState: BlurUIState,
    modifier: Modifier = Modifier,
    selectedTab: Int = 0,
    onTabSelected: (Int) -> Unit,
    sliderValue: Float = 0.5f,
    onSliderChange: (Float) -> Unit = {},
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
            TabButton(0, "Shapes", selectedTab == 0, onTabSelected)
            TabButton(1, "Brush", selectedTab == 1, onTabSelected)
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Slider + value
        SliderTool(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            value = uiState.blur,
            onValueChange = {
//                viewmodel.updateBrightness(it)
            },
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
                FilterItem(
                    item = item,
                    isSelected = uiState.shapeId == item.id,
                    onClick = {
                        onItemClick.invoke(item)
                    }
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
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
            if (text == stringResource(R.string.shapes)) R.drawable.ic_shapes else R.drawable.ic_brush

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
private fun FilterItem(
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