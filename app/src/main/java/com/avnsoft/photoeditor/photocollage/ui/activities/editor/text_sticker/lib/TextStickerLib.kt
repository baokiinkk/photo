package com.avnsoft.photoeditor.photocollage.ui.activities.editor.text_sticker.lib

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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.avnsoft.photoeditor.photocollage.R
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.sticker.lib.Sticker
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.sticker.lib.StickerView
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.text_sticker.TEXT_ALIGN
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.text_sticker.TextStickerToolPanel
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.text_sticker.TextStickerViewModel
import com.avnsoft.photoeditor.photocollage.ui.theme.AppStyle
import com.basesource.base.components.ColorPickerDialog
import com.basesource.base.utils.clickableWithAlphaEffect
import org.koin.androidx.compose.koinViewModel

@Composable
fun TextStickerLib(
    modifier: Modifier = Modifier,
    viewmodel: TextStickerViewModel = koinViewModel(),
    isShowToolPanel: Boolean,
    onApply: () -> Unit,
    onCancel: () -> Unit
) {
    LaunchedEffect(Unit) {
        viewmodel.getConfigTextSticker()
    }
    Box(
        modifier = modifier
            .fillMaxSize()
            .clipToBounds()
    ) {
        val uiState by viewmodel.uiState.collectAsStateWithLifecycle()
        val context = LocalContext.current
        var opacityColor by remember { mutableStateOf(0f) }
        var showColorWheel by remember { mutableStateOf(false) }
        var currentSelectedColor by remember {
            mutableStateOf(
                Color.White
            )
        }

        var stickerView by remember { mutableStateOf<StickerView?>(null) }


        Box(
            modifier = Modifier
                .fillMaxSize()
        )
        {
            TextStickerLayer(
                modifier = Modifier
//                    .weight(1f)
                ,
                viewmodel = viewmodel,
                stickerView = stickerView,
                onResultStickerView = { stickerView = it },
                isShowToolPanel = isShowToolPanel
            )

            if (isShowToolPanel) {
                TextStickerToolPanel(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickableWithAlphaEffect {

                        }
                        .align(Alignment.BottomCenter),
                    items = uiState.items,
                    onCancel = {
                        viewmodel.resetTextIndex()
                        onCancel.invoke()
                    },
                    onApply = {
                        viewmodel.resetTextIndex()
                        onApply.invoke()
                    },
                    addTextSticker = { index, item ->
                        viewmodel.addTextSticker(
                            index = index,
                            item = item,
                        )
                    },
                    uiState = uiState,
                    onSelectedColor = { color ->
                        stickerView?.getCurrentTextSticker()
                            ?.getAddTextProperties()?.textColor = color.toArgb()
                        stickerView?.getCurrentTextSticker()?.getAddTextProperties()?.let {
                            stickerView?.replace(
                                TextSticker(
                                    context,
                                    it
                                )
                            )
                        }
                    },
                    opacityColorValue = opacityColor,
                    onOpacityColor = {
                        opacityColor = it
                        stickerView?.getCurrentTextSticker()
                            ?.getAddTextProperties()?.textAlpha = (255 - it).toInt()
                        stickerView?.getCurrentTextSticker()?.getAddTextProperties()?.let {
                            stickerView?.replace(
                                TextSticker(
                                    context,
                                    it
                                )
                            )
                        }
                    },
                    onAlign = {
                        when (it) {
                            TEXT_ALIGN.START -> {
                                stickerView?.setStickerHorizontalPosition(Sticker.Position.LEFT)
                            }

                            TEXT_ALIGN.CENTER -> {
                                stickerView?.setStickerHorizontalPosition(Sticker.Position.CENTER)
                            }

                            TEXT_ALIGN.END -> {
                                stickerView?.setStickerHorizontalPosition(Sticker.Position.RIGHT)
                            }
                        }
                    },
                    onShowSystemColor = {
                        showColorWheel = true
                    }
                )
            }
        }

        if (showColorWheel) {
            ColorPickerDialog(
                selectedColor = currentSelectedColor,
                onColorSelected = { color ->
                    currentSelectedColor = color
                    stickerView?.getCurrentTextSticker()
                        ?.getAddTextProperties()?.textColor = color.toArgb()
                    stickerView?.getCurrentTextSticker()?.getAddTextProperties()?.let {
                        stickerView?.replace(
                            TextSticker(
                                context,
                                it
                            )
                        )
                    }
                    showColorWheel = false
                },
                onDismiss = { showColorWheel = false },
                textStyle = AppStyle.body1().medium().gray900(),
                confirmText = R.string.confirm,
                cancelText = R.string.cancel
            )
        }

    }
}