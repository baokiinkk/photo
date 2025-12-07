package com.avnsoft.photoeditor.photocollage.ui.activities.editor.remove_background

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.avnsoft.photoeditor.photocollage.R
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.tools.BackgroundLayer
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.tools.BackgroundSheet
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.EditorActivity
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.EditorInput
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.background.BackgroundViewModel
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.crop.ToolInput
import com.avnsoft.photoeditor.photocollage.ui.dialog.DiscardChangesDialog
import com.avnsoft.photoeditor.photocollage.ui.theme.AppStyle
import com.avnsoft.photoeditor.photocollage.utils.FileUtil
import com.avnsoft.photoeditor.photocollage.utils.getInput
import com.basesource.base.ui.base.BaseActivity
import com.basesource.base.ui.image.LoadImageUrl
import com.basesource.base.utils.ImageWidget
import com.basesource.base.utils.clickableWithAlphaEffect
import com.basesource.base.utils.toJson
import kotlinx.serialization.json.Json
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File

class RemoveBackgroundResultActivity : BaseActivity() {

    private val viewmodel: BackgroundViewModel by viewModel()

    private val screenInput: ToolInput? by lazy {
        intent.getInput()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewmodel.getConfigBackground(
            bitmap = screenInput?.getBitmap(this),
            isBackgroundTransparent = screenInput?.isBackgroundTransparent ?: false,
            pathBitmap = screenInput?.pathBitmap
        )
        enableEdgeToEdge()

        setContent {
            Scaffold(
                containerColor = if (screenInput?.isBackgroundTransparent == true) {
                    Color.White
                } else {
                    Color(0xFFF2F4F8)
                }
            ) { inner ->
                val uiState by viewmodel.uiState.collectAsStateWithLifecycle()

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            top = inner.calculateTopPadding(),
                            bottom = inner.calculateBottomPadding()
                        )
                ) {
                    // Background layer
                    BackgroundLayer(
                        backgroundSelection = uiState.backgroundSelection,
                        modifier = Modifier.fillMaxSize()
                    )

                    Column(
                        modifier = Modifier.fillMaxSize()
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
                                val file = File(screenInput?.pathBitmap.orEmpty())
                                val uri = file.toUri()
                                val intent =
                                    Intent(
                                        this@RemoveBackgroundResultActivity,
                                        EditorActivity::class.java
                                    )
                                val input = EditorInput(
                                    pathBitmap = uri.toString(),
                                )
                                intent.putExtra("arg", input.toJson())
                                intent.putExtra(
                                    "backgroundSelection",
                                    Json.encodeToString(uiState.backgroundSelection)
                                )
                                startActivity(intent)
                                finish()
                            }
                        )
//                        Spacer(modifier = Modifier.height(24.dp))
                        Box(
                            modifier = Modifier
                                .weight(1f)
                        ) {
                            uiState.pathBitmap?.let {
                                LoadImageUrl(
                                    modifier = Modifier.fillMaxSize(),
                                    model = screenInput?.pathBitmap,
                                    size = FileUtil.MAX_SIZE_FILE
                                )
                            }
                        }
                        BackgroundSheet(
                            isShowFooter = uiState.backgroundSelection == null,
                            selectedBackgroundSelection = uiState.backgroundSelection,
                            onBackgroundSelect = { _, selection ->
                                viewmodel.updateBackground(selection)
                            },
                            onClose = {
                                finish()
                            },
                            onConfirm = {
                                val intent = Intent()
                                intent.putExtra(
                                    "backgroundSelection",
                                    Json.encodeToString(uiState.backgroundSelection)
                                )
                                setResult(RESULT_OK, intent)
                                finish()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                        )
                    }

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

@Composable
fun HeaderApply(
    modifier: Modifier,
    onBack: () -> Unit,
    onSave: () -> Unit,
    textRight: String = stringResource(R.string.apply)
) {
    Row(
        modifier = modifier,
    ) {
        ImageWidget(
            modifier = Modifier
                .clickableWithAlphaEffect(onClick = onBack),
            resId = R.drawable.ic_arrow_left
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            modifier = Modifier
                .clickableWithAlphaEffect {
                    onSave.invoke()
                },
            text = textRight,
            textAlign = TextAlign.Center,
            style = AppStyle.buttonMedium().semibold().primary500()
        )
    }
}
