package com.avnsoft.photoeditor.photocollage.ui.activities.editor.ai_enhance

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.avnsoft.photoeditor.photocollage.R
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.adjust.OriginalButton
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.background.HeaderApply
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.crop.ToolInput
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.remove_background.LoadingAnimation
import com.avnsoft.photoeditor.photocollage.ui.theme.AppColor
import com.avnsoft.photoeditor.photocollage.ui.theme.AppStyle
import com.avnsoft.photoeditor.photocollage.utils.getInput
import com.basesource.base.ui.base.BaseActivity
import com.basesource.base.ui.image.LoadImage
import com.basesource.base.utils.ImageWidget
import com.basesource.base.utils.clickableWithAlphaEffect
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.getValue

class AIEnhanceActivity : BaseActivity() {

    private val screenInput: ToolInput? by lazy {
        intent.getInput()
    }
    private val viewmodel: AIEnhanceViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewmodel.initData(screenInput?.pathBitmap)
        setContent {
            Scaffold(
                containerColor = Color.White
            ) { inner ->
                val uiState by viewmodel.uiState.collectAsStateWithLifecycle()

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(
                                top = inner.calculateTopPadding(),
                                bottom = inner.calculateBottomPadding()
                            )
                            .background(Color(0xFFF2F4F8))
                    ) {
                        HeaderApply(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White)
                                .padding(horizontal = 16.dp, vertical = 16.dp),
                            onBack = {
                                finish()
                            },
                            onSave = {

                            }
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                               ,
                            contentAlignment = Alignment.Center
                        ) {
                            if (uiState.imageUrl.isNotEmpty()){
                                LoadImage(
                                    model = uiState.imageUrl,
                                    contentDescription = null,
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier
                                        .fillMaxSize(),
                                    onSuccess = {
                                        viewmodel.hideImageOriginalAfterLoaded()
                                    }
                                )
                            }
                            LoadImage(
                                model = screenInput?.pathBitmap,
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .alpha(if (uiState.showOriginal) 1f else 0f)
                                ,
                                contentScale = ContentScale.Fit,
                            )
                            OriginalButton(
                                resId = R.drawable.ic_show_ui_original,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(bottom = 16.dp, end = 16.dp)
                            ) {
                                viewmodel.updateIsOriginal(it)
                            }
                        }
                    }

                    LoadingAnimation(
                        isShowLoading = uiState.isShowLoading,
                        content = stringResource(R.string.content_magic_ai_is_enhancing),
                        isCancel = true,
                        onCancel = {
                            finish()
                        }
                    )
                }
            }
        }
    }
}