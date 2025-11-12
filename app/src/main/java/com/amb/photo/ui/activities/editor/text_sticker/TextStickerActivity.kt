package com.amb.photo.ui.activities.editor.text_sticker

import android.app.Activity
import android.content.Context
import android.graphics.Rect
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.amb.photo.R
import com.amb.photo.databinding.ActivityAirplaneBinding
import com.amb.photo.ui.activities.editor.crop.FooterEditor
import com.amb.photo.ui.activities.editor.crop.ToolInput
import com.amb.photo.ui.activities.editor.sticker.lib.Sticker
import com.amb.photo.ui.activities.editor.sticker.lib.StickerView
import com.amb.photo.ui.activities.editor.text_sticker.lib.AddTextProperties
import com.amb.photo.ui.activities.editor.text_sticker.lib.FontAsset
import com.amb.photo.ui.activities.editor.text_sticker.lib.FontItem
import com.amb.photo.ui.activities.editor.text_sticker.lib.TextSticker
import com.amb.photo.ui.theme.fontFamily
import com.amb.photo.utils.getInput
import com.basesource.base.ui.base.BaseActivity
import com.basesource.base.utils.clickableWithAlphaEffect
import kotlinx.coroutines.delay
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.math.roundToInt

class TextStickerActivity : BaseActivity() {

    private val viewmodel: TextStickerViewModel by viewModel()

    private val screenInput: ToolInput? by lazy {
        intent.getInput()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewmodel.getConfigTextSticker(screenInput?.getBitmap(this))
        enableEdgeToEdge()

        setContent {
            Scaffold(
                containerColor = Color(0xFFF2F4F8)
            ) { inner ->
                val uiState by viewmodel.uiState.collectAsStateWithLifecycle()
                var boxBounds by remember { mutableStateOf<Rect?>(null) }
//                var text by remember { mutableStateOf("Click to Edit") }
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

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            top = inner.calculateTopPadding(),
                            bottom = inner.calculateBottomPadding()
                        )
                        .background(Color(0xFFF2F4F8))
                        .clickableWithAlphaEffect {
                            if (isVisibleTextField) {
                                isVisibleTextField = false
                                focusManager.clearFocus()
                                keyboardController?.hide()
                                val addTextProperties = uiState.addTextProperties!!
                                addTextProperties.text = textFieldValue.text
                                addTextProperties.textWidth = editTextFieldSize.width
                                addTextProperties.textHeight = editTextFieldSize.height
//                                stickerView.replace(
//                                    TextSticker(
//                                        context,
//                                        addTextProperties
//                                    )
//                                )
                            }
                        }
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
                                    }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp)
                                        .onGloballyPositioned { layoutCoordinates ->
                                            textFieldSize = layoutCoordinates.size
                                            if (!viewmodel.textMeasured) {
                                                viewmodel.addFirstTextSticker(textFieldSize)
                                                viewmodel.textMeasured = true
                                            }
                                        }
                                ) {
                                    TextField(
                                        value = "Click to Edit",
                                        onValueChange = { char ->

                                        },
                                    )
                                }
                                Image(
                                    bitmap = it.asImageBitmap(),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxSize()
                                )

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
                                                .align(Alignment.Center)
                                                .onGloballyPositioned { layoutCoordinates ->
                                                    editTextFieldSize = layoutCoordinates.size
                                                }
                                        ) {
                                            BasicTextField(
                                                value = textFieldValue,
                                                onValueChange = { char ->
                                                    textFieldValue = char
                                                },
                                                textStyle = TextStyle(
                                                    fontFamily = FontFamily(typeface),
                                                    color = Color(uiState.editTextProperties?.textColor!!)
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
                                            val addTextProperties = uiState.addTextProperties!!
                                            addTextProperties.text = textFieldValue.text
                                            addTextProperties.textWidth = editTextFieldSize.width
                                            addTextProperties.textHeight = editTextFieldSize.height
                                            stickerView.replace(
                                                TextSticker(
                                                    context,
                                                    addTextProperties
                                                )
                                            )
                                            textFieldValue = textFieldValue.copy(text = "")
                                        }
                                    },
                                    onResultStickerView = { view ->
//                                        stickerView = view
                                    }
                                )

                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    TextStickerToolPanel(
                        modifier = Modifier.fillMaxWidth(),
                        items = uiState.items,
                        onCancel = {
                            finish()
                        },
                        onApply = {
//                            viewmodel.addTextSticker()
                        },
                        addTextSticker = { index, item ->
                            viewmodel.addTextSticker(index = index, item = item, textFieldSize)
                        }
                    )
                }
            }
        }
    }
}


@Composable
fun CustomTextField(
    fontPath: String,
    textColor: Int,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester
) {
    val context = LocalContext.current
    val customFont = rememberFontFromAssets(context, fontPath)

//    val keyboardController = LocalSoftwareKeyboardController.current
//
//    LaunchedEffect(Unit) {
//        focusRequester.requestFocus()
//        delay(100)
//        keyboardController?.show()
//    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.CenterStart
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = TextStyle(
                fontFamily = customFont,
                color = Color(textColor)
            ),
            decorationBox = { innerTextField ->
                innerTextField()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp)
                .focusRequester(focusRequester)
                .focusable()
        )
    }
}

enum class TEXT_TAB(val index: Int, val res: Int) {
    FONT(0, R.string.font),
    COLOR(1, R.string.color),
    ALIGN(2, R.string.align)
}

@Composable
fun TextStickerToolPanel(
    modifier: Modifier = Modifier,
    items: List<FontItem>,
    onCancel: () -> Unit,
    onApply: () -> Unit,
    addTextSticker: (Int, FontItem) -> Unit
) {
    val tabs = listOf(
        stringResource(TEXT_TAB.FONT.res),
        stringResource(TEXT_TAB.COLOR.res),
        stringResource(TEXT_TAB.ALIGN.res)
    )
    var selectedTab by remember { mutableIntStateOf(TEXT_TAB.FONT.index) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
    ) {

        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp, top = 16.dp)
        ) {
            tabs.forEachIndexed { index, tab ->
                val isSelected = selectedTab == index
                TabTextSticker(
                    isSelected = isSelected,
                    tabName = tab,
                    onSelectedTab = { selectedTab = index }
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
        }

        when (selectedTab) {
            TEXT_TAB.FONT.index -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    itemsIndexed(items) { index, item ->
                        CustomFontText(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clickableWithAlphaEffect {
                                    addTextSticker.invoke(index, item)
                                },
                            itemFont = item
                        )
                    }
                }
            }

            TEXT_TAB.COLOR.index -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {

                }
            }

            TEXT_TAB.ALIGN.index -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {

                }
            }
        }


        FooterEditor(
            modifier = Modifier
                .fillMaxWidth(),
            title = stringResource(R.string.text_tool),
            onCancel = onCancel,
            onApply = onApply
        )
        Spacer(modifier = Modifier.height(16.dp))

    }
}

@Composable
fun TabTextSticker(
    isSelected: Boolean,
    tabName: String,
    onSelectedTab: () -> Unit,
) {
    Box(
        modifier = Modifier
            .width(64.dp)
            .height(24.dp)
            .clip(RoundedCornerShape(50))
            .clickableWithAlphaEffect(onClick = onSelectedTab)
            .background(
                color = if (isSelected) Color(0xFF6425F3) else Color(0xFFF2F4F7),
                shape = RoundedCornerShape(size = 24.dp)
            )
            .padding(start = 12.dp, top = 4.dp, end = 12.dp, bottom = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = tabName,
            style = TextStyle(
                fontSize = 12.sp,
                lineHeight = 16.sp,
                fontFamily = fontFamily,
                fontWeight = FontWeight(600),
                color = if (isSelected) Color(0xFFFFFFFF) else Color(0xFF667085),
            )
        )
    }
}


fun View.showKeyboard(activity: Activity) {
    requestFocus()
    post {
        val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
    }
}

fun hideKeyboard(activity: Activity) {
    val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    val view = activity.currentFocus ?: View(activity)
    imm.hideSoftInputFromWindow(view.windowToken, 0)
}