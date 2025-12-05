package com.avnsoft.photoeditor.photocollage.ui.activities.editor.ai_enhance

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.avnsoft.photoeditor.photocollage.R
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.EditorActivity
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.EditorInput
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.adjust.OriginalButton
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.crop.ToolInput
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.remove_background.HeaderApply
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.remove_background.LoadingAnimation
import com.avnsoft.photoeditor.photocollage.ui.dialog.DiscardChangesDialog
import com.avnsoft.photoeditor.photocollage.utils.FileUtil
import com.avnsoft.photoeditor.photocollage.utils.getInput
import com.basesource.base.ui.base.BaseActivity
import com.basesource.base.ui.image.LoadImageUrl
import com.basesource.base.utils.toJson
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File

class AIEnhanceActivity : BaseActivity() {

    private val screenInput: ToolInput? by lazy {
        intent.getInput()
    }
    private val viewmodel: AIEnhanceViewModel by viewModel()

    override fun onBackPressed() {
        viewmodel.showDiscardDialog()
    }

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
                                viewmodel.showDiscardDialog()
                            },
                            onSave = {
                                val file = File(uiState.imageUrl)
                                val uri = file.toUri()
                                val intent = Intent(
                                    this@AIEnhanceActivity,
                                    EditorActivity::class.java
                                )
                                val input = EditorInput(
                                    pathBitmap = uri.toString(),
                                )
                                intent.putExtra("arg", input.toJson())
                                startActivity(intent)
                                finish()
                            }
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            if (uiState.imageUrl.isNotEmpty()) {
                                LoadImageUrl(
                                    model = uiState.imageUrl,
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier
                                        .fillMaxSize(),
                                    size = FileUtil.MAX_SIZE_FILE,
                                    onSuccess = {
                                        viewmodel.hideImageOriginalAfterLoaded()
                                    },
                                )
                            }
                            LoadImageUrl(
                                model = screenInput?.pathBitmap,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .alpha(if (uiState.showOriginal) 1f else 0f),
                                contentScale = ContentScale.Fit,
                                size = FileUtil.MAX_SIZE_FILE,
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

                    DiscardChangesDialog(
                        isVisible = uiState.showDiscardDialog,
                        onDiscard = {
                            finish()
                        },
                        onCancel = {
                            viewmodel.hideDiscardDialog()
                        }
                    )
                }
            }
        }
    }
}