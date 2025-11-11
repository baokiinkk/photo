package com.amb.photo.ui.activities.editor.sticker

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.amb.photo.R
import com.amb.photo.ui.activities.editor.crop.FooterEditor
import com.amb.photo.ui.activities.editor.crop.ToolInput
import com.amb.photo.ui.activities.editor.sticker.lib.EmojiTab
import com.amb.photo.ui.theme.AppColor
import com.amb.photo.utils.getInput
import com.basesource.base.ui.base.BaseActivity
import com.basesource.base.ui.image.LoadImage
import com.basesource.base.utils.clickableWithAlphaEffect
import org.koin.androidx.viewmodel.ext.android.viewModel

class StickerActivity : BaseActivity() {

    private val viewmodel: StickerViewModel by viewModel()

    private val screenInput: ToolInput? by lazy {
        intent.getInput()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewmodel.getConfigSticker(screenInput?.getBitmap(this))
        setContent {
            Scaffold(
                containerColor = Color(0xFFF2F4F8)
            ) { inner ->
                val uiState by viewmodel.uiState.collectAsStateWithLifecycle()
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            top = inner.calculateTopPadding(),
                            bottom = inner.calculateBottomPadding()
                        )
                        .background(Color(0xFFF2F4F8))
                ) {
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
                                    .background(Color.Green)
                                    .align(Alignment.Center)
                                    .clipToBounds()

                            ) {
                                Image(
                                    bitmap = it.asImageBitmap(),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxSize()
                                )
                                StickerViewCompose(
                                    modifier = Modifier.fillMaxSize(),
                                    pathSticker = uiState.pathSticker
                                )
                            }
                        }
                    }
                    StickerToolPanel(
                        modifier = Modifier.fillMaxWidth(),
                        uiState = uiState,
                        onTabSelected = {
                            viewmodel.selectedTab(it)
                        },
                        onStickerSelected = {
                            viewmodel.selectedSticker(it)
                        },
                        onCancel = {
                            finish()
                        },
                        onApply = {
                            finish()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun StickerToolPanel(
    modifier: Modifier = Modifier,
    uiState: StickerUIState,
    onTabSelected: (EmojiTab) -> Unit,
    onCancel: () -> Unit,
    onApply: () -> Unit,
    onStickerSelected: (String) -> Unit
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .padding(vertical = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            Row(
                Modifier
                    .horizontalScroll(scrollState),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                uiState.emojiTabs.forEach { tab ->
                    CategoryButton(
                        selected = uiState.currentTab == tab,
                        item = tab,
                        onClick = {
                            onTabSelected.invoke(tab)
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(5),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(uiState.currentTab.items) { emoji ->
                    EmojiItem(
                        url = emoji,
                        onStickerSelected = onStickerSelected
                    )
                }
            }
        }

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

@Composable
fun CategoryButton(
    selected: Boolean,
    item: EmojiTab,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .background(
                if (selected) AppColor.Gray100 else Color.White,
                RoundedCornerShape(12.dp)
            )
            .clickableWithAlphaEffect { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(item.tabIcon),
            contentDescription = null,
            modifier = Modifier.size(28.dp)
        )
    }
}

@Composable
fun EmojiItem(url: String, onStickerSelected: (String) -> Unit) {
    LoadImage(
        model = "file:///android_asset/$url",
        contentDescription = null,
        modifier = Modifier.clickableWithAlphaEffect {
            onStickerSelected.invoke(url)
        }
    )
}