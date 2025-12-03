package com.avnsoft.photoeditor.photocollage.ui.activities.store

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.avnsoft.photoeditor.photocollage.R
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.tools.CollageTool
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.EditorActivity
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.EditorInput
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.crop.ToolInput.TYPE
import com.avnsoft.photoeditor.photocollage.ui.activities.imagepicker.ImagePickerActivity
import com.avnsoft.photoeditor.photocollage.ui.activities.imagepicker.ImagePickerActivity.Companion.RESULT_URI
import com.avnsoft.photoeditor.photocollage.ui.activities.imagepicker.ImageRequest
import com.avnsoft.photoeditor.photocollage.ui.activities.imagepicker.TypeSelect
import com.avnsoft.photoeditor.photocollage.ui.activities.store.tab.TabTemplates
import com.avnsoft.photoeditor.photocollage.ui.activities.store.tab.background.TabBackground
import com.avnsoft.photoeditor.photocollage.ui.activities.store.tab.background.detail.StoreBackgroundDetailActivity
import com.avnsoft.photoeditor.photocollage.ui.activities.store.tab.sticker.TabSticker
import com.avnsoft.photoeditor.photocollage.ui.activities.store.tab.sticker.detail.StoreStickerDetailActivity
import com.avnsoft.photoeditor.photocollage.ui.activities.store.tab.template.detail.TemplateDetailActivity
import com.avnsoft.photoeditor.photocollage.ui.activities.store.tab.template.detail.TemplateDetailInput
import com.avnsoft.photoeditor.photocollage.ui.theme.AppColor
import com.avnsoft.photoeditor.photocollage.ui.theme.AppStyle
import com.avnsoft.photoeditor.photocollage.utils.getInput
import com.basesource.base.ui.base.BaseActivity
import com.basesource.base.ui.base.IScreenData
import com.basesource.base.utils.ImageWidget
import com.basesource.base.utils.clickableWithAlphaEffect
import com.basesource.base.utils.fromJsonTypeToken
import com.basesource.base.utils.launchActivity
import org.koin.androidx.viewmodel.ext.android.viewModel

data class StoreActivityInput(
    val type: TYPE = TYPE.NEW,
) : IScreenData
class StoreActivity : BaseActivity() {

    private val screenInput: StoreActivityInput? by lazy {
        intent.getInput()
    }

    private val viewModel: StoreViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var selectedTab by remember { mutableStateOf(StoreTab.TEMPLATE) }
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            Scaffold(
                containerColor = AppColor.White
            ) { inner ->
                Column(
                    modifier = Modifier
                        .padding(
                            top = inner.calculateTopPadding(),
                            bottom = inner.calculateBottomPadding()
                        )

                ) {
                    HeaderStore(
                        title = stringResource(R.string.store),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    if (screenInput?.type == TYPE.NEW) {
                        TabStore(
                            selectedTab = selectedTab,
                            onTabSelect = {
                                selectedTab = it
                            }
                        )
                    }

                    when (selectedTab) {
                        StoreTab.TEMPLATE -> {
                            TabTemplates(
                                templates = uiState.templates,
                                onItemClicked = { template ->
                                    launchActivity(
                                        toActivity = TemplateDetailActivity::class.java,
                                        input = TemplateDetailInput(template)
                                    ) {
                                        if (screenInput?.type == TYPE.BACK_AND_RETURN && it.resultCode == RESULT_OK) {
                                            setResult(RESULT_OK)
                                            finish()
                                        }
                                    }
                                }
                            )
                        }

                        StoreTab.STICKER -> {
                            TabSticker(
                                stickers = uiState.stickers,
                                onBannerClickable = {
                                    launchActivity(
                                        toActivity = StoreStickerDetailActivity::class.java,
                                        input = it
                                    )
                                },
                                onUseClick = {
                                    if (it.isUsed) {
                                        gotoEditPhoto(
                                            CollageTool.STICKER
                                        )
                                    } else {
                                        viewModel.updateIsUsedStickerById(it.eventId)
                                    }
                                }
                            )
                        }

                        StoreTab.BACKGROUND -> {
                            TabBackground(
                                patterns = uiState.patterns,
                                onBannerClickable = {
                                    launchActivity(
                                        toActivity = StoreBackgroundDetailActivity::class.java,
                                        input = it
                                    )
                                },
                                onUseClick = {
                                    if (it.isUsed) {
                                        gotoEditPhoto(
                                            CollageTool.BACKGROUND
                                        )
                                    } else {
                                        viewModel.updateIsUsedPatternById(it.eventId)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

    }

    private fun gotoEditPhoto(tool: CollageTool) {
        launchActivity(
            toActivity = ImagePickerActivity::class.java,
            ImageRequest(TypeSelect.SINGLE)
        ) { result ->
            val data: List<String>? = result.data?.getStringExtra(RESULT_URI)?.fromJsonTypeToken()
            data?.firstOrNull()?.let {
                launchActivity(
                    toActivity = EditorActivity::class.java,
                    input = EditorInput(
                        pathBitmap = it,
                        tool = tool
                    ),
                )
            }
        }
    }
}

enum class StoreTab(val iconRes: Int) {
    TEMPLATE(R.drawable.ic_store_template),
    STICKER(R.drawable.ic_sticker),
    BACKGROUND(R.drawable.ic_background)
}

@Composable
fun HeaderStore(
    modifier: Modifier = Modifier,
    title: String
) {
    val context = LocalContext.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        contentAlignment = Alignment.Center
    ) {
        ImageWidget(
            resId = R.drawable.ic_arrow_left,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .clickableWithAlphaEffect {
                    (context as? BaseActivity)?.finish()
                }
        )

        Text(
            text = title,
            style = AppStyle.title1().bold().Color_101828(),
            modifier = Modifier
                .align(Alignment.Center)
        )
    }
}

@Composable
fun TabStore(
    selectedTab: StoreTab,
    onTabSelect: (StoreTab) -> Unit
) {
    val tabs = StoreTab.entries.toTypedArray()
    Box(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .wrapContentWidth()
                .align(Alignment.Center),
            horizontalArrangement = Arrangement.spacedBy(40.dp)
        ) {
            tabs.forEach { item ->
                val isSelected = item == selectedTab
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clickableWithAlphaEffect {
                            onTabSelect.invoke(item)
                        },
                ) {
                    ImageWidget(
                        modifier = Modifier
                            .size(24.dp)
                            .align(Alignment.Center),
                        resId = item.iconRes,
                        tintColor = if (isSelected) AppColor.Primary500 else null
                    )
                    Box(
                        modifier = Modifier
                            .width(24.dp)
                            .height(2.dp)
                            .background(
                                if (isSelected) AppColor.Primary500 else Color.Transparent
                            )
                            .align(Alignment.BottomCenter)
                    )
                }

            }
        }
    }
}

