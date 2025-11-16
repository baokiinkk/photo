package com.avnsoft.photoeditor.photocollage.ui.activities.collage.components

import android.app.Activity
import android.graphics.Rect
import android.graphics.Typeface
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.sticker.lib.Sticker
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.sticker.lib.StickerView
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.text_sticker.TEXT_ALIGN
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.text_sticker.TextStickerComposeView
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.text_sticker.TextStickerToolPanel
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.text_sticker.TextStickerUIState
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.text_sticker.lib.FontAsset
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.text_sticker.lib.TextSticker
import com.basesource.base.utils.clickableWithAlphaEffect
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@Composable
fun TextUI(sticker: StickerView?, stickerView: (StickerView) -> Unit, state: TextStickerUIState) {
    var boxBounds by remember { mutableStateOf<Rect?>(null) }
    var textFieldValue by remember {
        mutableStateOf(TextFieldValue(text = "Click to Edit"))
    }
    var textFieldSize by remember { mutableStateOf(IntSize.Zero) }
    var isVisibleTextField by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    var editTextFieldSize by remember { mutableStateOf(IntSize.Zero) }
    var opacityColor by remember { mutableFloatStateOf(0f) }
    val localView = LocalView.current

    var showColorWheel by remember { mutableStateOf(false) }
    var currentSelectedColor by remember {
        mutableStateOf(
            Color.White
        )
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(Color(0xFFF2F4F8))
            .clickableWithAlphaEffect {
                if (isVisibleTextField) {
                    isVisibleTextField = false
                    focusManager.clearFocus()
                    keyboardController?.hide()
                    state.addTextProperties?.text = textFieldValue.text
                    state.addTextProperties?.textWidth = textFieldSize.width
                    state.addTextProperties?.textHeight = textFieldSize.height
                    sticker?.replace(
                        TextSticker(
                            context, state.addTextProperties ?: return@clickableWithAlphaEffect
                        )
                    )
                    textFieldValue = textFieldValue.copy(text = "")
                }
            }) {
        Box(
            Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .align(Alignment.Center)
                    .graphicsLayer()
                    .clipToBounds()
                    .onGloballyPositioned { coords ->
                        val position = coords.positionInRoot()
                        val size = coords.size
                        boxBounds = Rect(
                            position.x.roundToInt(),
                            position.y.roundToInt(),
                            (position.x + size.width).roundToInt(),
                            (position.y + size.height).roundToInt()
                        )
                    }) {
                Box(
                    modifier = Modifier
                        .wrapContentWidth()
                        .align(Alignment.Center)
                        .onGloballyPositioned { layoutCoordinates ->
                            textFieldSize = layoutCoordinates.size
//                            if (!viewmodel.textMeasured) {
//                                viewmodel.addFirstTextSticker(textFieldSize)
//                                viewmodel.textMeasured = true
//                            }
                        }) {
                    val typeface = Typeface.createFromAsset(
                        context.assets, FontAsset.listFonts.first().fontPath
                    )

                    Log.d("aaa", "ssss ${textFieldValue.text}")
                    Text(
                        text = textFieldValue.text, modifier = Modifier.padding(16.dp), style = TextStyle(
                            fontSize = 18.sp,
                            lineHeight = 24.sp,
                            fontFamily = FontFamily(typeface),
                            color = Color.Black,
                        )
                    )
                }

                if (isVisibleTextField) {
                    state.addTextProperties?.let {
                        val typeface = Typeface.createFromAsset(
                            context.assets, state.editTextProperties?.fontName!!
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .align(Alignment.Center)
                        ) {
                            BasicTextField(
                                value = textFieldValue, onValueChange = { char ->
                                    textFieldValue = char
                                }, textStyle = TextStyle(
                                    fontFamily = FontFamily(typeface), color = Color(state.editTextProperties.textColor)
                                ), decorationBox = { innerTextField ->
                                    innerTextField()
                                }, modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                                    .focusRequester(focusRequester)
                            )
                        }


                        LaunchedEffect(focusRequester) {
                            if (isVisibleTextField) {
                                focusRequester.requestFocus()
                                delay(100) // Make sure you have delay here
                                keyboardController?.show()
                            }
                        }
                    }
                }
                TextStickerComposeView(modifier = Modifier.fillMaxSize(), input = state.addTextProperties, onTextStickerEdit = { textSticker ->
                    isVisibleTextField = true
                    textFieldValue = textFieldValue.copy(
                        text = textSticker.getAddTextProperties()?.text.orEmpty(),
                        selection = TextRange(textSticker.getAddTextProperties()?.text.orEmpty().length)
                    )
                    //viewmodel.editTextSticker(textSticker)
                }, onStickerTouchOutside = { stickerView ->
                    if (isVisibleTextField) {
                        isVisibleTextField = false
                        focusManager.clearFocus()
                        keyboardController?.hide()
                        val addTextProperties = state.editTextProperties!!
                        addTextProperties.text = textFieldValue.text
                        addTextProperties.textWidth = textFieldSize.width
                        addTextProperties.textHeight = textFieldSize.height
                        stickerView.replace(
                            TextSticker(
                                context, addTextProperties
                            )
                        )
                        textFieldValue = textFieldValue.copy(text = "")
                    }
                }, onResultStickerView = { view ->
                    stickerView.invoke(view)
                })

            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextStickerToolPanel(modifier = Modifier.fillMaxWidth(), items = state.items, onCancel = {

        }, onApply = {

        }, addTextSticker = { index, item ->

        }, uiState = state, onSelectedColor = { color ->
            sticker?.getCurrentTextSticker()?.getAddTextProperties()?.textColor = color.toArgb()
            sticker?.getCurrentTextSticker()?.getAddTextProperties()?.let {
                sticker?.replace(
                    TextSticker(
                        context, it
                    )
                )
            }
        }, opacityColorValue = opacityColor, onOpacityColor = {
            opacityColor = it
            sticker?.getCurrentTextSticker()?.getAddTextProperties()?.textAlpha = (255 - it).toInt()
            sticker?.getCurrentTextSticker()?.getAddTextProperties()?.let {
                sticker?.replace(
                    TextSticker(
                        context, it
                    )
                )
            }
        }, onAlign = {
            when (it) {
                TEXT_ALIGN.START -> {
                    sticker?.setStickerHorizontalPosition(Sticker.Position.LEFT)
                }

                TEXT_ALIGN.CENTER -> {
                    sticker?.setStickerHorizontalPosition(Sticker.Position.CENTER)
                }

                TEXT_ALIGN.END -> {
                    sticker?.setStickerHorizontalPosition(Sticker.Position.RIGHT)
                }
            }
        }, onShowSystemColor = {
            showColorWheel = true
        })
    }

}