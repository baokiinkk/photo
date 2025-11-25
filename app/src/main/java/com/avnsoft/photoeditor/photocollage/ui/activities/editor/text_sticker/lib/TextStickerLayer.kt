package com.avnsoft.photoeditor.photocollage.ui.activities.editor.text_sticker.lib

import android.graphics.Typeface
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.avnsoft.photoeditor.photocollage.R
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.sticker.lib.StickerView
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.text_sticker.TextStickerComposeView
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.text_sticker.TextStickerViewModel
import com.basesource.base.utils.clickableWithAlphaEffect
import kotlinx.coroutines.delay

@Composable
fun TextStickerLayer(
    modifier: Modifier = Modifier,
    viewmodel: TextStickerViewModel,
    stickerView: StickerView?,
    onResultStickerView: (StickerView) -> Unit,
    isShowToolPanel: Boolean,
) {
    val uiState by viewmodel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val defaultText = stringResource(R.string.click_to_edit)
    var textFieldValue by remember {
        mutableStateOf(TextFieldValue(text = defaultText))
    }
    var textFieldSize by remember { mutableStateOf(IntSize.Zero) }
    var isVisibleTextField by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .clickableWithAlphaEffect {
                if (isVisibleTextField) {
                    isVisibleTextField = false
                    focusManager.clearFocus()
                    keyboardController?.hide()
                    val addTextProperties = uiState.editTextProperties!!
                    addTextProperties.text = textFieldValue.text
                    addTextProperties.textWidth = textFieldSize.width
                    addTextProperties.textHeight = textFieldSize.height
                    stickerView?.replace(
                        TextSticker(
                            context,
                            addTextProperties
                        )
                    )
                    textFieldValue = textFieldValue.copy(text = "")
                }
            }
            .clipToBounds()
    ) {
        Box(
            modifier = Modifier
                .wrapContentWidth()
                .align(Alignment.Center)
                .onGloballyPositioned { layoutCoordinates ->
                    textFieldSize = layoutCoordinates.size
                    if (!viewmodel.textMeasured && isShowToolPanel) {
                        viewmodel.addFirstTextSticker(textFieldSize, defaultText)
                        viewmodel.textMeasured = true
                    }
                }
                .alpha(0f)
        ) {
            val typeface = Typeface.createFromAsset(
                context.assets,
                FontAsset.listFonts.first().fontPath
            )

            Log.d("aaa", "ssss ${textFieldValue.text}")
            Text(
                text = textFieldValue.text,
                modifier = Modifier.padding(16.dp),
                style = TextStyle(
                    fontSize = 18.sp,
                    lineHeight = 24.sp,
                    fontFamily = FontFamily(typeface),
                    color = Color.Black,
                )
            )
        }

        if (isVisibleTextField) {
            uiState.editTextProperties?.let {
                val typeface = Typeface.createFromAsset(
                    context.assets,
                    uiState.editTextProperties?.fontName!!
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .align(Alignment.Center),
                    contentAlignment = Alignment.Center
                ) {
                    BasicTextField(
                        value = textFieldValue,
                        onValueChange = { char ->
                            textFieldValue = char
                        },
                        textStyle = TextStyle(
                            fontFamily = FontFamily(typeface),
                            color = Color(uiState.editTextProperties?.textColor!!),
                            textAlign = TextAlign.Center,
                        ),
                        decorationBox = { innerTextField ->
                            innerTextField()
                        },
                        modifier = Modifier
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
        TextStickerComposeView(
            modifier = Modifier.fillMaxSize(),
            input = uiState.addTextProperties,
            onTextStickerEdit = { textSticker ->
                isVisibleTextField = true
                textFieldValue = textFieldValue.copy(
                    text = textSticker.getAddTextProperties()?.text.orEmpty(),
                    selection = TextRange(textSticker.getAddTextProperties()?.text.orEmpty().length)
                )
                viewmodel.editTextSticker(textSticker)
            },
            onStickerTouchOutside = { stickerView ->
                if (isVisibleTextField) {
                    isVisibleTextField = false
                    focusManager.clearFocus()
                    keyboardController?.hide()
                    val addTextProperties = uiState.editTextProperties!!
                    addTextProperties.text = textFieldValue.text
                    addTextProperties.textWidth = textFieldSize.width
                    addTextProperties.textHeight = textFieldSize.height
                    stickerView.replace(
                        TextSticker(
                            context,
                            addTextProperties
                        )
                    )
                    textFieldValue = textFieldValue.copy(text = "")
                }
            },
            onResultStickerView = onResultStickerView
        )

    }
}