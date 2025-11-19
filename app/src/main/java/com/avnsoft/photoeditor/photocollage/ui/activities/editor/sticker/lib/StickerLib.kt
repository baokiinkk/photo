package com.avnsoft.photoeditor.photocollage.ui.activities.editor.sticker.lib

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.sticker.StickerToolPanel
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.sticker.StickerViewCompose
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.sticker.StickerViewModel
import com.basesource.base.utils.clickableWithAlphaEffect
import org.koin.androidx.compose.koinViewModel

@Composable
fun StickerLib(
    modifier: Modifier = Modifier,
    viewmodel: StickerViewModel = koinViewModel(),
    isShowToolPanel: Boolean,
    onApply: () -> Unit,
    onCancel: () -> Unit,
) {
    val uiState by viewmodel.uiState.collectAsStateWithLifecycle()
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewmodel.addStickerFromGallery(it.toString())
        }
    }
    var stickerView by remember { mutableStateOf<StickerView?>(null) }
    LaunchedEffect(Unit) {
        viewmodel.getConfigSticker()
    }
    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        StickerViewCompose(
            modifier = Modifier.fillMaxSize(),
            input = uiState.pathSticker,
            onReturnView = {
                stickerView = it
            },
        )
        if (isShowToolPanel) {
            stickerView?.setShowBorder(true)
            stickerView?.setShowIcons(true)
            StickerToolPanel(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .clickableWithAlphaEffect {

                    },
                uiState = uiState,
                onTabSelected = {
                    viewmodel.selectedTab(it)
                },
                onStickerSelected = {
                    viewmodel.addStickerFromAsset(it)
                },
                onCancel = {
                    stickerView?.setShowBorder(false)
                    stickerView?.setShowIcons(false)
                    onCancel.invoke()
                },
                onApply = {
                    stickerView?.setShowBorder(false)
                    stickerView?.setShowIcons(false)
                    onApply.invoke()
                },
                onAddStickerFromGallery = {
                    launcher.launch("image/*")
                }
            )
        }
    }

}