package com.avnsoft.photoeditor.photocollage.ui.activities.editor.filter

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.Bitmap
import android.opengl.GLSurfaceView
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.avnsoft.photoeditor.photocollage.R
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.crop.FooterEditor
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.crop.ToolInput
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.crop.saveImage
import com.avnsoft.photoeditor.photocollage.ui.theme.AppColor
import com.avnsoft.photoeditor.photocollage.ui.theme.LoadingScreen
import com.avnsoft.photoeditor.photocollage.utils.getInput
import com.basesource.base.ui.base.BaseActivity
import com.basesource.base.ui.image.LoadImage
import com.basesource.base.utils.clickableWithAlphaEffect
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.wysaid.view.ImageGLSurfaceView

class FilterActivity : BaseActivity() {

    private val viewmodel: FilterViewModel by viewModel()

    private val screenInput: ToolInput? by lazy {
        intent.getInput()
    }

    private lateinit var glView: ImageGLSurfaceView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        glView = ImageGLSurfaceView(this, null)
        viewmodel.getConfigFilter(screenInput?.getBitmap(this))
        enableEdgeToEdge()

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
                                    .align(Alignment.Center)

                            ) {
                                GpuImageFilterView(
                                    bitmap = it,
                                    modifier = Modifier
                                        .fillMaxSize(),
                                    config = uiState.currentConfig,
                                    glView = glView
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(36.dp))
                    FilterToolPanel(
                        modifier = Modifier.fillMaxWidth(),
                        uiState = uiState,
                        onItemClick = {
                            viewmodel.onItemClick(it)
                        },
                        onCancel = {
                            finish()
                        },
                        onApply = {
                            viewmodel.showLoading()
                            glView.getResultBitmap {
                                saveImage(
                                    context = this@FilterActivity,
                                    bitmap = it,
                                    onImageSaved = { pathBitmap ->
                                        viewmodel.hideLoading()
                                        val intent = Intent()
                                        intent.putExtra("pathBitmap", "$pathBitmap")
                                        setResult(RESULT_OK, intent)
                                        finish()
                                    }
                                )
                            }
                        }
                    )
                }

                if (uiState.isLoading) {
                    LoadingScreen()
                }
            }
        }
    }
}

@Composable
fun FilterToolPanel(
    modifier: Modifier = Modifier,
    uiState: FilterUIState,
    onItemClick: (FilterBean) -> Unit,
    onCancel: () -> Unit,
    onApply: () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .padding(vertical = 16.dp)
    ) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(uiState.filters) { item ->
                FilterItem(
                    item = item,
                    isSelected = uiState.filterId == item.name,
                    onClick = {
                        onItemClick.invoke(item)
                    }
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        FooterEditor(
            modifier = Modifier
                .fillMaxWidth(),
            title = stringResource(R.string.filter),
            onCancel = onCancel,
            onApply = onApply
        )
    }
}


@Composable
private fun FilterItem(
    item: FilterBean,
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
            LoadImage(
                model = item.bitmap,
                contentScale = ContentScale.Crop,
            )
        }
    }
}

@Composable
fun GpuImageFilterView(
    bitmap: Bitmap,
    modifier: Modifier = Modifier,
    config: String,
    glView: ImageGLSurfaceView
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            glView.displayMode = ImageGLSurfaceView.DisplayMode.DISPLAY_ASPECT_FIT
            glView.renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
            glView.setSurfaceCreatedCallback {
                glView.setImageBitmap(bitmap)
                glView.setFilterWithConfig(config)
            }
            glView
        },
        update = { view ->
            view.queueEvent {
                view.setFilterWithConfig(config)
            }
        }
    )
}