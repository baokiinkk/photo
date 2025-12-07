package com.avnsoft.photoeditor.photocollage.ui.activities.editor.filter

import android.graphics.Bitmap
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import org.wysaid.view.ImageGLSurfaceView

@Composable
fun FilterComposeLib(
    modifier: Modifier = Modifier,
    bitmap: Bitmap,
    viewmodel: FilterViewModel = koinViewModel(),
    isShowToolPanel: Boolean,
    onApply: () -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewmodel.uiState.collectAsStateWithLifecycle()
    var glView: ImageGLSurfaceView by remember { mutableStateOf(ImageGLSurfaceView(context, null)) }

    LaunchedEffect(Unit) {
//        viewmodel.getConfigFilter(bitmap)
    }
    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        GpuImageFilterView(
            bitmap = bitmap,
            modifier = Modifier
                .fillMaxSize(),
            config = uiState.currentConfig,
            glView = glView
        )
        if (isShowToolPanel) {
            FilterToolPanel(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter),
                uiState = uiState,
                onItemClick = {
                    viewmodel.onItemClick(it)
                },
                onCancel = onCancel,
                onApply = onApply
            )
        }
    }
}