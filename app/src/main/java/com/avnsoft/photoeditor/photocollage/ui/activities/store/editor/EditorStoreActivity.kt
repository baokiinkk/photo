package com.avnsoft.photoeditor.photocollage.ui.activities.store.editor

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.avnsoft.photoeditor.photocollage.R
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.CollageTool
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.FeatureBottomTools
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.FeaturePhotoHeader
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.ToolItem
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.crop.ToolInput
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.filter.FilterComposeLib
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.initEditorLib
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.sticker.StickerViewModel
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.sticker.lib.StickerLib
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.sticker.lib.StickerView
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.text_sticker.TextStickerViewModel
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.text_sticker.lib.TextStickerLib
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.uriToBitmap
import com.avnsoft.photoeditor.photocollage.ui.activities.store.HeaderStore
import com.avnsoft.photoeditor.photocollage.ui.theme.AppColor
import com.avnsoft.photoeditor.photocollage.ui.theme.AppStyle
import com.avnsoft.photoeditor.photocollage.utils.getInput
import com.basesource.base.ui.base.BaseActivity
import com.basesource.base.utils.clickableWithAlphaEffect
import org.koin.androidx.compose.koinViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.wysaid.nativePort.CGENativeLibrary
import org.wysaid.nativePort.CGENativeLibrary.LoadImageCallback
import java.io.IOException

class EditorStoreActivity : BaseActivity() {

    private val viewmodel: EditorStoreViewModel by viewModel()
    private val screenInput: ToolInput? by lazy {
        intent.getInput()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initEditorLib()

        viewmodel.initData(screenInput?.pathBitmap.uriToBitmap(this))
        setContent {
            val uiState by viewmodel.uiState.collectAsStateWithLifecycle()
            Scaffold(
                containerColor = AppColor.White
            ) { inner ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                ) {
                    FeaturePhotoHeader(
                        onBack = {
                            finish()
                        },
                        onUndo = {
                        },
                        onRedo = {
                        },
                        onSave = { /* TODO */ },
                        canUndo = false,
                        canRedo = false
                    )
                    ContentStoreEditor(
                        modifier = Modifier
                            .weight(1f)
                            .background(Color(0xFFF2F4F8)),
                        viewModel = viewmodel,
                        uiState = uiState
                    )
                    FooterStoreEditor(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .background(Color.White),
                        items = uiState.items,
                        onClick = {
                            viewmodel.onToolClick(it.tool)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ContentStoreEditor(
    modifier: Modifier,
    viewModel: EditorStoreViewModel,
    uiState: EditorStoreUIState,
) {
    Box(
        modifier = modifier
    ) {
        val isText = uiState.tool == CollageTool.TEXT
        val isSticker = uiState.tool == CollageTool.STICKER
        val isFilter = uiState.tool == CollageTool.FILTER

        uiState.bitmap?.let {
            FilterComposeLib(
                modifier = Modifier
                    .zIndex(if (isFilter) 1f else 0f),
                bitmap = uiState.bitmap,
                isShowToolPanel = uiState.isShowFilter,
                onApply = {
                    viewModel.hideFilter()
                },
                onCancel = {
                    viewModel.hideFilter()
                }
            )
        }

        TextStickerLib(
            modifier = Modifier
                .zIndex(if (isText) 1f else 0f),
            isShowToolPanel = uiState.isShowTextSticker,
            onApply = {
                viewModel.hideTextSticker()
            },
            onCancel = {
                viewModel.hideTextSticker()
            }
        )

        StickerLib(
            modifier = Modifier
                .zIndex(if (isSticker) 1f else 0f),
            isShowToolPanel = uiState.isShowSticker,
            onApply = {
                viewModel.hideSticker()
            },
            onCancel = {
                viewModel.hideSticker()
            }
        )
    }
}

@Composable
fun FooterStoreEditor(
    modifier: Modifier = Modifier,
    items: List<ToolItem>,
    tool: CollageTool = CollageTool.NONE,
    onClick: (ToolItem) -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
    ) {
        items.forEach { item ->
            val isSelect = tool == item.tool
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clickableWithAlphaEffect(onClick = {
                        onClick.invoke(item)
                    })
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Image(
                        painterResource(item.icon),
                        contentDescription = "",
                        modifier = Modifier.size(24.dp),
                        colorFilter = if (isSelect) ColorFilter.tint(AppColor.Primary500) else null,
                    )
                    Text(
                        modifier = Modifier.padding(top = 2.dp),
                        text = stringResource(item.label),
                        style = if (isSelect) AppStyle.caption2().medium()
                            .primary500() else AppStyle.caption2()
                            .medium().gray800()
                    )
                }
            }
        }
    }
}