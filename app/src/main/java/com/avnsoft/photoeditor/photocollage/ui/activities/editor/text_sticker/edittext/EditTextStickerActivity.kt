package com.avnsoft.photoeditor.photocollage.ui.activities.editor.text_sticker.edittext

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.avnsoft.photoeditor.photocollage.R
import com.avnsoft.photoeditor.photocollage.ui.theme.AppStyle
import com.avnsoft.photoeditor.photocollage.ui.theme.MainTheme
import com.basesource.base.ui.base.BaseActivity
import com.basesource.base.utils.ImageWidget
import com.basesource.base.utils.clickableWithAlphaEffect
import kotlinx.coroutines.delay

class EditTextStickerActivity : BaseActivity() {

    companion object {
        const val EXTRA_TEXT = "EXTRA_TEXT"
        const val EXTRA_WIDTH = "EXTRA_WIDTH"
        const val EXTRA_HEIGHT = "EXTRA_HEIGHT"
        fun newIntent(context: Context, messageContent: String?): Intent {
            return Intent(context, EditTextStickerActivity::class.java).apply {
                putExtra("messageContent", messageContent)
            }
        }
    }

    val messageContent by lazy {
        intent.getStringExtra("messageContent") ?: "Click to Edit"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MainTheme {
                var textFieldValue by remember {
                    mutableStateOf(
                        TextFieldValue(
                            text = messageContent,
                            selection = TextRange(
                                messageContent.length
                            )
                        )
                    )
                }

                val focusRequester = remember { FocusRequester() }
                val focusManager = LocalFocusManager.current
                val keyboardController = LocalSoftwareKeyboardController.current
                var textFieldSize by remember { mutableStateOf(IntSize.Zero) }


                LaunchedEffect(Unit) {
                    delay(200) // tránh trường hợp Activity chưa render kịp
                    focusRequester.requestFocus()
                    keyboardController?.show()
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .imePadding()   // Đây là dòng quan trọng!
                        .background(Color(0xFF000000).copy(alpha = 0.85f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .background(Color.Transparent),
                        contentAlignment = androidx.compose.ui.Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .wrapContentWidth()
                                .onGloballyPositioned { layoutCoordinates ->
                                    textFieldSize = layoutCoordinates.size
                                }
                        ) {
                            Text(
                                modifier = Modifier
                                    .padding(horizontal = 40.dp, vertical = 16.dp),
                                text = textFieldValue.text,
                                style = AppStyle.h2().medium().white()
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .background(Color.White)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            InputTextField(
                                value = textFieldValue,
                                onValueChange = { textFieldValue = it },
                                modifier = Modifier
                                    .weight(1f)
                                    .focusRequester(focusRequester)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            ImageWidget(
                                resId = R.drawable.ic_send,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clickableWithAlphaEffect {
                                        if (textFieldValue.text.isNotEmpty()) {
                                            val intent = Intent()
                                            intent.putExtra(EXTRA_TEXT, textFieldValue.text)
                                            intent.putExtra(EXTRA_WIDTH, textFieldSize.width)
                                            intent.putExtra(EXTRA_HEIGHT, textFieldSize.height)
                                            setResult(RESULT_OK, intent)
                                            finish()
                                        }
                                    }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InputTextField(
    modifier: Modifier,
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    placeholder: String = "Click to Edit"
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .background(
                color = Color(0xFFF2F4F7),
                shape = RoundedCornerShape(12.dp)
            ),
        contentAlignment = androidx.compose.ui.Alignment.CenterStart
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = AppStyle.body1().medium().gray900(),
            decorationBox = { innerTextField ->
                if (value.text.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = AppStyle.body1().medium().gray900()
                    )
                }
                innerTextField()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp)
        )
    }
}