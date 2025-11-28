package com.avnsoft.photoeditor.photocollage.ui.activities.editor.frame

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.FrameSelection
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.FrameSheet
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.EditorActivity
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.crop.ToolInput
import com.avnsoft.photoeditor.photocollage.ui.activities.freestyle.HeaderSave
import com.avnsoft.photoeditor.photocollage.ui.theme.AppStyle
import com.avnsoft.photoeditor.photocollage.utils.FileUtil.toFile
import com.avnsoft.photoeditor.photocollage.utils.getInput
import com.basesource.base.ui.base.BaseActivity
import dev.shreyaspatil.capturable.capturable
import dev.shreyaspatil.capturable.controller.rememberCaptureController
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class FrameActivity : BaseActivity() {

    private val viewmodel: FrameViewModel by viewModel()

    private val screenInput: ToolInput? by lazy {
        intent.getInput()
    }

    @OptIn(ExperimentalComposeUiApi::class, ExperimentalComposeApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewmodel.getConfig(screenInput)

        setContent {
            Scaffold(
                containerColor = Color.White
            ) { inner ->
                val uiState by viewmodel.uiState.collectAsStateWithLifecycle()
                val captureController = rememberCaptureController()
                val scope = rememberCoroutineScope()
                val context = LocalContext.current

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            top = inner.calculateTopPadding(),
                            bottom = inner.calculateBottomPadding()
                        )
                        .background(Color(0xFFF2F4F8))
                ) {
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

                                // Hiển thị frame overlay nếu có
                                uiState.frameSelection
                                    ?.takeIf { it is FrameSelection.Frame }
                                    ?.let { frame ->
                                        val data = frame as FrameSelection.Frame
                                        val url = if (data.item.urlThumb?.startsWith("http://") == true ||
                                            data.item.urlThumb?.startsWith("https://") == true
                                        ) {
                                            data.item.urlThumb
                                        } else {
                                            "${data.urlRoot}${data.item.urlThumb}"
                                        }

                                        AsyncImage(
                                            model = ImageRequest.Builder(context).data(url).build(),
                                            contentDescription = "",
                                            contentScale = ContentScale.FillBounds,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                            }
                        }
                    }
                    FrameSheet(
                        selectedFrameSelection = uiState.frameSelection,
                        onFrameSelect = { selection ->
                            viewmodel.updateFrame(selection)
                        },
                        onClose = {
                            finish()
                        },
                        onConfirm = {
                            scope.launch {
                                try {
                                    val bitmapAsync = captureController.captureAsync()
                                    val bitmap = bitmapAsync.await().asAndroidBitmap()
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
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                    )
                }
            }

        }
    }
}