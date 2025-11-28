package com.avnsoft.photoeditor.photocollage.ui.activities.freestyle

import android.graphics.Typeface
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ExperimentalComposeApi
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toDrawable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.avnsoft.photoeditor.photocollage.R
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.BackgroundLayer
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.BackgroundSheet
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.CollageTool
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.FeatureBottomTools
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.FrameSelection
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.FrameSheet
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.RatioSheet
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.ToolItem
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.sticker.StickerToolPanel
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.sticker.StickerViewModel
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.sticker.lib.DrawableSticker
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.sticker.lib.Sticker
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.sticker.loadBitmap
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.text_sticker.TEXT_ALIGN
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.text_sticker.TextStickerToolPanel
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.text_sticker.TextStickerViewModel
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.text_sticker.lib.AddTextProperties
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.text_sticker.lib.FontAsset
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.text_sticker.lib.TextSticker
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.toBitmap
import com.avnsoft.photoeditor.photocollage.ui.activities.export_image.ExportImageBottomSheet
import com.avnsoft.photoeditor.photocollage.ui.activities.export_image.ExportImageData
import com.avnsoft.photoeditor.photocollage.ui.activities.freestyle.lib.FreeStyleStickerView
import com.avnsoft.photoeditor.photocollage.ui.theme.AppStyle
import com.avnsoft.photoeditor.photocollage.utils.FileUtil
import com.avnsoft.photoeditor.photocollage.utils.FileUtil.toFile
import com.basesource.base.components.ColorPickerDialog
import com.basesource.base.utils.capturable
import com.basesource.base.utils.clickableWithAlphaEffect
import com.basesource.base.utils.rememberCaptureController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

val toolsFreeStyle = listOf(
    ToolItem(CollageTool.RATIO, R.string.ratio_tool, R.drawable.ic_ratio),
    ToolItem(CollageTool.BACKGROUND, R.string.background_tool, R.drawable.ic_background_tool),
    ToolItem(CollageTool.FRAME, R.string.frame_tool, R.drawable.ic_frame_tool),
    ToolItem(CollageTool.TEXT, R.string.text_tool, R.drawable.ic_text_tool),
    ToolItem(CollageTool.STICKER, R.string.sticker_tool, R.drawable.ic_sticker_tool),
    ToolItem(CollageTool.ADD_PHOTO, R.string.add_photo_tool, R.drawable.ic_photo_tool)
)

@OptIn(ExperimentalComposeUiApi::class, ExperimentalComposeApi::class)
@Composable
fun FreeStyleScreen(
    modifier: Modifier,
    viewmodel: FreeStyleViewModel,
    stickerView: FreeStyleStickerView,
    onToolClick: (CollageTool) -> Unit,
    onBack: () -> Unit,
    onDownloadSuccess: (ExportImageData) -> Unit
) {
    val captureController = rememberCaptureController()

    val scope = rememberCoroutineScope()
    val uiState by viewmodel.uiState.collectAsStateWithLifecycle()
    var showBottomSheetSaveImage by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var pathBitmap by remember { mutableStateOf("") }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            HeaderSave(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                onBack = onBack,
                onActionRight = {
                    scope.launch {
                        try {
                            stickerView.setShowFocus(false)
                            val bitmap = captureController.toImageBitmap().asAndroidBitmap()
                            pathBitmap = bitmap.toFile(context)
                            showBottomSheetSaveImage = true
                        } catch (ex: Throwable) {
                            Toast.makeText(context, "Error ${ex.message}", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                }
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
                    .padding(top = 80.dp, bottom = 175.dp)
                    .then(
                        remember(uiState.ratio) {
                            val aspectRatioValue = when (uiState.ratio) {
                                "Original" -> null
                                "1:1" -> 1f
                                "4:5" -> 4f / 5f
                                "5:4" -> 5f / 4f
                                "3:4" -> 3f / 4f
                                else -> null
                            }
                            if (aspectRatioValue != null) {
                                Modifier.aspectRatio(aspectRatioValue)
                            } else {
                                Modifier
                            }
                        }
                    )
                    .clipToBounds()
                    .capturable(captureController)
            ) {
                BackgroundLayer(
                    backgroundSelection = uiState.backgroundSelection,
                    modifier = Modifier.fillMaxSize()
                )
                FreeStyleStickerComposeView(
                    modifier = Modifier.fillMaxSize(),
                    view = stickerView
                )
                uiState.frameSelection?.let { frame ->
                    when (frame) {
                        is FrameSelection.Frame -> {
                            val context = LocalContext.current
                            val data = frame as FrameSelection.Frame
                            val url =
                                if (data.item.urlThumb?.startsWith("http://") == true || data.item.urlThumb?.startsWith(
                                        "https://"
                                    ) == true
                                ) {
                                    data.item.urlThumb
                                } else {
                                    "${data.urlRoot}${data.item.urlThumb}"
                                }
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(url)
                                    .build(),
                                contentDescription = "",
                                contentScale = ContentScale.FillBounds,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        else -> {}
                    }
                }

            }

            FeatureBottomTools(
                tools = toolsFreeStyle,
                onToolClick = onToolClick
            )
        }

        when {
            uiState.isShowStickerTool -> {
                stickerView.setLocked(true)
                StickerFooterTool(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter),
                    stickerView = stickerView,
                    onCancel = {
                        stickerView.removeCurrentSticker()
                        stickerView.setLocked(false)
                        viewmodel.cancelSticker()
                    },
                    onApply = {
                        stickerView.setLocked(false)
                        viewmodel.applySticker(stickerView.getCurrentDrawableSticker())
                    }
                )
            }

            uiState.isShowTextStickerTool -> {
                stickerView.setLocked(true)
                TextStickerFooterTool(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter),
                    stickerView = stickerView,
                    onCancel = {
                        stickerView.removeCurrentSticker()
                        stickerView.setLocked(false)
                        viewmodel.cancelTextSticker()
                    },
                    onApply = {
                        stickerView.setLocked(false)
                        viewmodel.applyTextSticker()
                    },
                    onAddFirstText = {
                        stickerView.addSticker(
                            TextSticker(
                                stickerView.context,
                                it,

                                ),
                            Sticker.Position.TOP
                        )
                    },
                    addTextSticker = { font ->
                        stickerView.replace(
                            TextSticker(
                                stickerView.context,
                                font
                            )
                        )
                    },
                )
            }

            uiState.isShowBackgroundTool -> {
                BackgroundSheet(
                    selectedBackgroundSelection = uiState.backgroundSelection,
                    onBackgroundSelect = { _, selection ->
                        viewmodel.updateBackground(selection)
                    },
                    onClose = {
                        viewmodel.cancelBackgroundTool()
                    },
                    onConfirm = {
                        viewmodel.applyBackgroundTool()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .align(Alignment.BottomCenter)
                )
            }

            uiState.isShowRatioTool -> {
                RatioSheet(
                    selectedRatio = uiState.ratio,
                    onRatioSelect = { aspect ->
                        viewmodel.updateRatio(aspect.label)
                    },
                    onClose = {
                        viewmodel.cancelRatioTool()
                    },
                    onConfirm = {
                        viewmodel.applyRatioTool()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .align(Alignment.BottomCenter)
                )
            }

            uiState.isShowFrameTool -> {
                FrameSheet(
                    selectedFrameSelection = uiState.frameSelection,
                    onFrameSelect = { selection ->
                        viewmodel.updateFrame(selection)
                    },
                    onClose = {
                        viewmodel.cancelFrameTool()
                    },
                    onConfirm = {
                        viewmodel.applyFrameTool()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .align(Alignment.BottomCenter)
                )
            }

            else -> {

            }
        }

        if (uiState.isVisibleTextField) {
            EditTextStickerLayer(
                modifier = Modifier
                    .fillMaxSize(),
                onEditText = {
                    viewmodel.hideEditTextSticker()
                    stickerView.replace(
                        TextSticker(
                            stickerView.context,
                            it
                        )
                    )
                },
                editTextProperties = uiState.editTextProperties
            )
        }

        if (showBottomSheetSaveImage) {
            ExportImageBottomSheet(
                pathBitmap = pathBitmap,
                onDismissRequest = {
                    showBottomSheetSaveImage = false
                },
                onDownload = {
                    if (pathBitmap.isNotEmpty()) {
                        scope.launch {
                            try {
                                val bitmap = pathBitmap.toBitmap() ?: return@launch
                                val bitmapMark =
                                    FileUtil.addDiagonalWatermark(bitmap, "COLLAGE MAKER", 25);
                                val uriMark = FileUtil.saveImageToStorageWithQuality(
                                    context = context,
                                    quality = it,
                                    bitmap = bitmapMark
                                )
                                onDownloadSuccess.invoke(
                                    ExportImageData(
                                        pathUriMark = uriMark?.toString(),
                                        pathBitmapOriginal = pathBitmap,
                                        quality = it
                                    )
                                )
                            } catch (ex: Throwable) {
                                Toast.makeText(context, "Error ${ex.message}", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                    } else {
                        Toast.makeText(context, "Save Image Error", Toast.LENGTH_SHORT).show()
                    }

                }
            )
        }
    }
}

@Composable
fun StickerFooterTool(
    modifier: Modifier,
    viewmodel: StickerViewModel = koinViewModel(),
    stickerView: FreeStyleStickerView,
    onCancel: () -> Unit,
    onApply: () -> Unit
) {
    var isAddSticker by remember { mutableStateOf(true) }
    val uiState by viewmodel.uiState.collectAsStateWithLifecycle()

    val scope = rememberCoroutineScope()
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val bitmap = uri.toBitmap(stickerView.context)
            if (isAddSticker) {
                isAddSticker = false
                stickerView.addSticker(
                    DrawableSticker(bitmap?.toDrawable(stickerView.resources))
                )
            } else {
                stickerView.replace(
                    DrawableSticker(bitmap?.toDrawable(stickerView.resources))
                )
            }
        }
    }

    LaunchedEffect(Unit) {
        viewmodel.getConfigSticker()
    }

    uiState.currentTab?.let {
        if (isAddSticker) {
            scope.launch {
                val bitmap = loadBitmap(stickerView.context, it.content.first().urlThumb)
                stickerView.addSticker(
                    DrawableSticker(bitmap?.toDrawable(stickerView.resources)),
                    Sticker.Position.TOP
                )
                isAddSticker = false
            }
        }
    }
    StickerToolPanel(
        modifier = modifier,
        uiState = uiState,
        onTabSelected = {
            viewmodel.selectedTab(it)
        },
        onStickerSelected = {
            if (it.isNotEmpty()) {
                CoroutineScope(Dispatchers.Main).launch {
                    val bitmap = loadBitmap(stickerView.context, it)
                    if (isAddSticker) {
                        isAddSticker = false
                        stickerView.addSticker(
                            DrawableSticker(bitmap?.toDrawable(stickerView.resources)),
                            Sticker.Position.TOP
                        )
                    } else {
                        stickerView.replace(
                            DrawableSticker(bitmap?.toDrawable(stickerView.resources))
                        )
                    }
                }
            }
        },
        onCancel = onCancel,
        onApply = onApply,
        onAddStickerFromGallery = {
            launcher.launch("image/*")
        }
    )
}


@Composable
fun TextStickerFooterTool(
    modifier: Modifier = Modifier,
    viewmodel: TextStickerViewModel = koinViewModel(),
    stickerView: FreeStyleStickerView,
    onCancel: () -> Unit,
    onApply: () -> Unit,
    onAddFirstText: (AddTextProperties) -> Unit,
    addTextSticker: (AddTextProperties) -> Unit,
) {
    val uiState by viewmodel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var opacityColor by remember { mutableFloatStateOf(0f) }
    var showColorWheel by remember { mutableStateOf(false) }
    var currentSelectedColor by remember {
        mutableStateOf(
            Color.White
        )
    }

    LaunchedEffect(Unit) {
        viewmodel.getConfigTextSticker()
    }
    Box(
        modifier = modifier
            .clipToBounds()
    ) {
        BoxAddFirstTextSticker(
            viewmodel = viewmodel,
            onAddFirstText = onAddFirstText
        )

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
                viewmodel.addTextPropertiesDefault.apply {
                    fontName = item.fontPath
                }
                addTextSticker.invoke(viewmodel.addTextPropertiesDefault)
            },
            uiState = uiState,
            onSelectedColor = { color ->
                stickerView.getCurrentTextSticker()?.getAddTextProperties()?.textColor =
                    color.toArgb()
                stickerView.getCurrentTextSticker()?.getAddTextProperties()?.let {
                    stickerView.replace(
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
                stickerView.getCurrentTextSticker()
                    ?.getAddTextProperties()?.textAlpha = (255 - it).toInt()
                stickerView.getCurrentTextSticker()?.getAddTextProperties()?.let {
                    stickerView.replace(
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
                        stickerView.setStickerHorizontalPosition(Sticker.Position.LEFT)
                    }

                    TEXT_ALIGN.CENTER -> {
                        stickerView.setStickerHorizontalPosition(Sticker.Position.CENTER)
                    }

                    TEXT_ALIGN.END -> {
                        stickerView.setStickerHorizontalPosition(Sticker.Position.RIGHT)
                    }
                }
            },
            onShowSystemColor = {
                showColorWheel = true
            }
        )

        if (showColorWheel) {
            ColorPickerDialog(
                selectedColor = currentSelectedColor,
                onColorSelected = { color ->
                    currentSelectedColor = color
                    stickerView.getCurrentTextSticker()?.getAddTextProperties()?.textColor =
                        color.toArgb()
                    stickerView.getCurrentTextSticker()?.getAddTextProperties()?.let {
                        stickerView.replace(
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

@Composable
fun BoxAddFirstTextSticker(
    viewmodel: TextStickerViewModel,
    onAddFirstText: (AddTextProperties) -> Unit,
) {
    val context = LocalContext.current

    var textMeasured by remember { mutableStateOf(false) }
    val defaultText = stringResource(R.string.click_to_edit)
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .wrapContentWidth()
                .align(Alignment.Center)
                .onGloballyPositioned { layoutCoordinates ->
                    if (!textMeasured) {
                        val addTextProperties = AddTextProperties.defaultProperties
                        addTextProperties.fontName = FontAsset.listFonts.first().fontPath
                        addTextProperties.fontIndex = 0
                        addTextProperties.text = defaultText
                        addTextProperties.textWidth = layoutCoordinates.size.width
                        addTextProperties.textHeight = layoutCoordinates.size.height
                        viewmodel.addTextPropertiesDefault = addTextProperties
                        onAddFirstText.invoke(addTextProperties)
                        textMeasured = true
                    }
                }
                .alpha(0f)
        ) {
            val typeface = Typeface.createFromAsset(
                context.assets,
                viewmodel.addTextPropertiesDefault.fontName ?: FontAsset.listFonts.first().fontPath
            )

            Text(
                text = defaultText,
                modifier = Modifier.padding(16.dp),
                style = TextStyle(
                    fontSize = 18.sp,
                    lineHeight = 24.sp,
                    fontFamily = FontFamily(typeface),
                    color = Color.Black,
                )
            )
        }
    }
}

@Composable
fun EditTextStickerLayer(
    modifier: Modifier = Modifier,
    onEditText: (AddTextProperties) -> Unit,
    editTextProperties: AddTextProperties
) {
    val context = LocalContext.current
    val defaultText = stringResource(R.string.click_to_edit)
    var textFieldValue by remember {
        mutableStateOf(
            TextFieldValue(
                text = editTextProperties.text.orEmpty().ifEmpty { defaultText },
                selection = TextRange(
                    editTextProperties.text.orEmpty().ifEmpty { defaultText }.length
                )
            )
        )
    }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    var textFieldSize by remember { mutableStateOf(IntSize.Zero) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clickableWithAlphaEffect {
                focusManager.clearFocus()
                keyboardController?.hide()
                editTextProperties.text = textFieldValue.text
                editTextProperties.textWidth = textFieldSize.width
                editTextProperties.textHeight = textFieldSize.height
                onEditText.invoke(editTextProperties)
//                textFieldValue = textFieldValue.copy(text = "")
            }
            .clipToBounds()
    ) {
        val typeface = Typeface.createFromAsset(
            context.assets,
            editTextProperties.fontName!!
        )

        Box(
            modifier = Modifier
                .wrapContentWidth()
                .align(Alignment.Center)
                .onGloballyPositioned { layoutCoordinates ->
                    textFieldSize = layoutCoordinates.size
                }
                .alpha(0f)
        ) {
            Text(
                text = defaultText,
                modifier = Modifier.padding(16.dp),
                style = TextStyle(
                    fontSize = 18.sp,
                    lineHeight = 24.sp,
                    fontFamily = FontFamily(typeface),
                    color = Color.Black,
                )
            )
        }

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
                    color = Color(editTextProperties.textColor),
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
            focusRequester.requestFocus()
            delay(100) // Make sure you have delay here
            keyboardController?.show()
        }
    }
}