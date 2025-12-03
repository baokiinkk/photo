package com.avnsoft.photoeditor.photocollage.ui.activities.collage.components

import android.net.Uri
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.avnsoft.photoeditor.photocollage.data.model.collage.CollageTemplate
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.CollageViewModel
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.tools.BackgroundSheet
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.tools.FrameSheet
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.tools.GridsSheet
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.tools.RatioSheet
import com.avnsoft.photoeditor.photocollage.ui.activities.freestyle.FreeStyleViewModel
import com.avnsoft.photoeditor.photocollage.ui.activities.freestyle.lib.FreeStyleStickerView

@Composable
fun CollageSheetsContainer(
    modifier: Modifier = Modifier,
    viewModel: CollageViewModel,
    freeStyleViewModel: FreeStyleViewModel,
    stickerView: FreeStyleStickerView,
    collageState: com.avnsoft.photoeditor.photocollage.data.model.collage.CollageState,
    templates: List<CollageTemplate>,
    currentUris: List<Uri>,
    showGridsSheet: Boolean,
    showRatioSheet: Boolean,
    showBackgroundSheet: Boolean,
    showFrameSheet: Boolean,
    showStickerSheet: Boolean,
    showTextSheet: Boolean,
    onCloseGridsSheet: () -> Unit,
    onCloseRatioSheet: () -> Unit,
    onCloseBackgroundSheet: () -> Unit,
    onCloseFrameSheet: () -> Unit,
    onCloseStickerSheet: () -> Unit,
    onCloseTextSheet: () -> Unit,
    onConfirmGridsSheet: () -> Unit,
    onConfirmRatioSheet: () -> Unit,
    onConfirmBackgroundSheet: () -> Unit,
    onConfirmFrameSheet: () -> Unit,
    onConfirmStickerSheet: () -> Unit,
    onConfirmTextSheet: () -> Unit
) {
    if (showGridsSheet) {
        GridsSheet(
            templates = templates,
            selectedTemplate = collageState.templateId,
            onTemplateSelect = { template -> viewModel.selectTemplate(template) },
            onClose = onCloseGridsSheet,
            onConfirm = {
                viewModel.confirmChanges()
                onConfirmGridsSheet()
            },
            topMargin = collageState.topMargin,
            onTopMarginChange = { viewModel.updateTopMargin(it) },
            columnMargin = collageState.columnMargin,
            onColumnMarginChange = { viewModel.updateColumnMargin(it) },
            cornerRadius = collageState.cornerRadius,
            onCornerRadiusChange = { viewModel.updateCornerRadius(it) },
            imageCount = currentUris.size.coerceAtLeast(1),
            modifier = modifier
                .fillMaxWidth()
                .wrapContentHeight()
        )
    }

    if (showRatioSheet) {
        RatioSheet(
            selectedRatio = collageState.ratio,
            onRatioSelect = { aspect -> viewModel.updateRatio(aspect.ratio) },
            onClose = {
                viewModel.cancelRatioChanges()
                onCloseRatioSheet()
            },
            onConfirm = {
                viewModel.confirmChanges()
                onConfirmRatioSheet()
            },
            modifier = modifier
                .fillMaxWidth()
                .wrapContentHeight()
        )
    }

    if (showBackgroundSheet) {
        BackgroundSheet(
            selectedBackgroundSelection = collageState.backgroundSelection,
            onBackgroundSelect = { _, selection ->
                viewModel.updateBackground(selection)
            },
            onClose = {
                viewModel.cancelBackgroundChanges()
                onCloseBackgroundSheet()
            },
            onConfirm = {
                viewModel.confirmChanges()
                onConfirmBackgroundSheet()
            },
            modifier = modifier
                .fillMaxWidth()
                .wrapContentHeight()
        )
    }

    if (showFrameSheet) {
        FrameSheet(
            selectedFrameSelection = collageState.frameSelection,
            onFrameSelect = { selection ->
                viewModel.updateFrame(selection)
            },
            onClose = {
                viewModel.cancelFrameChanges()
                onCloseFrameSheet()
            },
            onConfirm = {
                viewModel.confirmChanges()
                onConfirmFrameSheet()
            },
            modifier = modifier
                .fillMaxWidth()
                .wrapContentHeight()
        )
    }

    if (showStickerSheet) {
        com.avnsoft.photoeditor.photocollage.ui.activities.freestyle.StickerFooterTool(
            modifier = modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            stickerView = stickerView,
            onCancel = {
                stickerView.removeCurrentSticker()
                stickerView.setLocked(false)
                onCloseStickerSheet()
            },
            onApply = {
                stickerView.setLocked(false)
                viewModel.confirmStickerChanges()
                onConfirmStickerSheet()
            }
        )
    }

    if (showTextSheet) {
        val textStickerUIState by freeStyleViewModel.uiState.collectAsStateWithLifecycle()
        com.avnsoft.photoeditor.photocollage.ui.activities.freestyle.TextStickerFooterTool(
            modifier = modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            stickerView = stickerView,
            onCancel = {
                if (!freeStyleViewModel.isEditTextSticker) {
                    stickerView.removeCurrentSticker()
                }
                stickerView.setLocked(false)
                onCloseTextSheet()
            },
            onApply = {
                stickerView.setLocked(false)
                viewModel.confirmStickerChanges()
                onConfirmTextSheet()
            },
            onAddFirstText = {
                if (textStickerUIState.isVisibleTextField) return@TextStickerFooterTool
                stickerView.addSticker(
                    com.avnsoft.photoeditor.photocollage.ui.activities.editor.text_sticker.lib.TextSticker(
                        stickerView.context,
                        it
                    ),
                    com.avnsoft.photoeditor.photocollage.ui.activities.editor.sticker.lib.Sticker.Position.TOP
                )
            },
            addTextSticker = { font ->
                stickerView.replace(
                    com.avnsoft.photoeditor.photocollage.ui.activities.editor.text_sticker.lib.TextSticker(
                        stickerView.context,
                        font
                    )
                )
            }
        )
    }
}

