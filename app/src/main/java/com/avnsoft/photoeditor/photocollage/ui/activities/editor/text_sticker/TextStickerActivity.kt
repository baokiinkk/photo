package com.avnsoft.photoeditor.photocollage.ui.activities.editor.text_sticker

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Rect
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
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
import com.avnsoft.photoeditor.photocollage.R
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.crop.FooterEditor
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.crop.ToolInput
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.crop.saveImage
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.sticker.captureView
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.sticker.lib.Sticker
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.sticker.lib.StickerView
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.text_sticker.lib.FontAsset
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.text_sticker.lib.FontItem
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.text_sticker.lib.TextSticker
import com.avnsoft.photoeditor.photocollage.ui.theme.AppColor
import com.avnsoft.photoeditor.photocollage.ui.theme.AppStyle
import com.avnsoft.photoeditor.photocollage.ui.theme.LoadingScreen
import com.avnsoft.photoeditor.photocollage.ui.theme.MainTheme
import com.avnsoft.photoeditor.photocollage.ui.theme.fontFamily
import com.avnsoft.photoeditor.photocollage.utils.getInput
import com.basesource.base.components.ColorPickerUI
import com.basesource.base.ui.base.BaseActivity
import com.basesource.base.utils.ImageWidget
import com.basesource.base.utils.clickableWithAlphaEffect
import kotlinx.coroutines.delay
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.math.roundToInt

class TextStickerActivity : BaseActivity() {

    private val viewmodel: TextStickerViewModel by viewModel()

    private val screenInput: ToolInput? by lazy {
        intent.getInput()
    }

    private var stickerView: StickerView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewmodel.getConfigTextSticker(screenInput?.getBitmap(this))

        setContent {
            MainTheme {
                Scaffold(
                    containerColor = Color(0xFFF2F4F8)
                ) { inner ->
                    val uiState by viewmodel.uiState.collectAsStateWithLifecycle()
                    var boxBounds by remember { mutableStateOf<Rect?>(null) }
                    val defaultText = stringResource(R.string.click_to_edit)
                    var textFieldValue by remember {
                        mutableStateOf(TextFieldValue(text = defaultText))
                    }
                    var textFieldSize by remember { mutableStateOf(IntSize.Zero) }
                    var isVisibleTextField by remember { mutableStateOf(false) }
                    val focusRequester = remember { FocusRequester() }
                    val keyboardController = LocalSoftwareKeyboardController.current
                    val context = LocalContext.current
                    val focusManager = LocalFocusManager.current
                    var editTextFieldSize by remember { mutableStateOf(IntSize.Zero) }
                    var opacityColor by remember { mutableStateOf(0f) }
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
                    )
                    {
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
                                            .wrapContentWidth()
                                            .align(Alignment.Center)
                                            .onGloballyPositioned { layoutCoordinates ->
                                                textFieldSize = layoutCoordinates.size
                                                if (!viewmodel.textMeasured) {
                                                    viewmodel.addFirstTextSticker(textFieldSize, defaultText)
                                                    viewmodel.textMeasured = true
                                                }
                                            }
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
                                        onResultStickerView = { view ->
                                            stickerView = view
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
                                stickerView?.setShowFocus(false)
                                stickerView?.post {
                                    captureView(
                                        localView,
                                        callback = { bitmap ->
                                            bitmap ?: return@captureView
                                            val bounds = boxBounds ?: return@captureView
                                            val captured = Bitmap.createBitmap(
                                                bitmap,
                                                bounds.left,
                                                bounds.top,
                                                bounds.width(),
                                                bounds.height()
                                            )
                                            saveImage(
                                                context = this@TextStickerActivity,
                                                bitmap = captured,
                                                onImageSaved = { pathBitmap ->
                                                    val intent = Intent()
                                                    intent.putExtra("pathBitmap", "$pathBitmap")
                                                    setResult(RESULT_OK, intent)
                                                    finish()
                                                }
                                            )
                                        }
                                    )
                                }
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
                                            this@TextStickerActivity,
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
                                            this@TextStickerActivity,
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
    addTextSticker: (Int, FontItem) -> Unit,
    uiState: TextStickerUIState,
    onSelectedColor: (Color) -> Unit,
    opacityColorValue: Float,
    onOpacityColor: (Float) -> Unit,
    onAlign: (TEXT_ALIGN) -> Unit,
) {
    val tabs = listOf(
        stringResource(TEXT_TAB.FONT.res),
        stringResource(TEXT_TAB.COLOR.res),
        stringResource(TEXT_TAB.ALIGN.res)
    )
    var selectedTab by remember { mutableIntStateOf(TEXT_TAB.FONT.index) }
    var selectedTabAlign by remember { mutableStateOf(TEXT_ALIGN.CENTER) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
    ) {

        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
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
                            itemFont = item,
                            isSelected = uiState.textIndex == index
                        )
                    }
                }
            }

            TEXT_TAB.COLOR.index -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(vertical = 16.dp)
                ) {
                    TabColor(
                        modifier = Modifier
                            .fillMaxWidth(),
                        onSelectedColor = onSelectedColor,
                        sliderValue = opacityColorValue,
                        onSliderChange = onOpacityColor,
                        sliderValueRange = 0f..255f
                    )
                }
//                Spacer(modifier = Modifier.height(26.dp))
            }

            TEXT_TAB.ALIGN.index -> {
//                Spacer(modifier = Modifier.height(28.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .background(color = AppColor.Gray100, RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, 8.dp)
                            .align(Alignment.Center),
                    ) {
                        TabAlign(
                            onAlign = {
                                onAlign.invoke(it)
                                selectedTabAlign = it
                            },
                            selectedTab = selectedTabAlign
                        )
                    }
                }
//                Spacer(modifier = Modifier.height(24.dp))
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

enum class TEXT_ALIGN(val index: Int) {
    START(0), CENTER(1), END(2)
}

@Composable
fun TabAlign(
    selectedTab: TEXT_ALIGN,
    onAlign: (TEXT_ALIGN) -> Unit,
) {
//    var selectedTab by remember { mutableStateOf(TEXT_ALIGN.CENTER) }
    val items = listOf(
        R.drawable.ic_align_start,
        R.drawable.ic_align_center,
        R.drawable.ic_align_end
    )
    Row(
        modifier = Modifier,
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEachIndexed { index, item ->
            ItemAlign(
                isSelected = selectedTab.index == index,
                onSelectedTab = {
                    val selectedTab = TEXT_ALIGN.entries.toTypedArray()[index]
                    onAlign.invoke(selectedTab)
                }
            )
        }
    }
}

@Composable
fun ItemAlign(
    isSelected: Boolean,
    onSelectedTab: () -> Unit,
) {
    ImageWidget(
        resId = R.drawable.ic_align_start,
        tintColor = if (isSelected) AppColor.Primary500 else AppColor.Gray300,
        modifier = Modifier.clickableWithAlphaEffect(onClick = onSelectedTab)
    )
}

@Composable
fun TabColor(
    modifier: Modifier = Modifier,
    onSelectedColor: (Color) -> Unit,
    sliderValue: Float,
    onSliderChange: (Float) -> Unit,
    sliderValueRange: ClosedFloatingPointRange<Float> = 0f..100f
) {
    var showColorWheel by remember { mutableStateOf(false) }

    val colors: List<Color> = listOf(
        Color(0xFFF7F8F3),
        Color(0xFFFFF7EC),
        Color(0xFFFAEDE7),
        Color(0xFFA9E2F5),
        Color(0xFFFFBBBE),
        Color(0xFFFF8B0D),
    )
    if (showColorWheel) {
        ColorPickerUI(
            modifier = modifier,
            onColorSelected = {
                onSelectedColor.invoke(it)
            },
            onDismiss = { showColorWheel = false },
            textStyle = AppStyle.body1().medium().gray900(),
            confirmText = R.string.confirm,
            cancelText = R.string.cancel
        )
    } else {
        Column {
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(top = 30.dp, start = 16.dp, end = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ImageWidget(
                    resId = R.drawable.ic_color,
                    modifier = Modifier
                        .size(32.dp)
                        .clickableWithAlphaEffect {
                            showColorWheel = true
                        }
                )
                colors.forEach { item ->
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(item, CircleShape)
                            .clickableWithAlphaEffect {
                                onSelectedColor.invoke(item)
                            }
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, start = 16.dp, end = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ImageWidget(
                    resId = R.drawable.ic_opacity
                )
                Slider(
                    value = sliderValue,
                    onValueChange = onSliderChange,
                    valueRange = sliderValueRange,
                    modifier = Modifier
                        .weight(1f)
                        .height(2.dp)
                        .padding(start = 20.dp),
                    colors = SliderDefaults.colors(
                        thumbColor = AppColor.Gray800,
                        activeTrackColor = AppColor.Gray800,
                        inactiveTrackColor = AppColor.Gray200
                    )
                )
            }
        }
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