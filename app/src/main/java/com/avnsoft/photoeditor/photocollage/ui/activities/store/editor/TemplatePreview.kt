package com.avnsoft.photoeditor.photocollage.ui.activities.store.editor

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.avnsoft.photoeditor.photocollage.R
import com.avnsoft.photoeditor.photocollage.data.model.template.TemplateModel
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.preview.ImageTransformState
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.preview.extractTransforms
import com.avnsoft.photoeditor.photocollage.ui.activities.freestyle.FreeStyleStickerComposeView
import com.avnsoft.photoeditor.photocollage.ui.activities.freestyle.lib.FreeStyleSticker
import com.avnsoft.photoeditor.photocollage.ui.activities.freestyle.lib.FreeStyleStickerView
import com.avnsoft.photoeditor.photocollage.ui.activities.store.tab.template.detail.baseBannerItemModifier
import com.basesource.base.ui.image.LoadImage
import com.basesource.base.utils.ImageWidget
import com.basesource.base.utils.clickableWithAlphaEffect
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun TemplatePreview(
    modifier: Modifier = Modifier,
    viewmodel: EditorStoreViewModel,
    uiState: EditorStoreUIState,
    stickerView: FreeStyleStickerView,
    template: TemplateModel?,
    icons: List<FreeStyleSticker>? = null,
    selectedImages: Map<Int, Uri>,
    imageTransforms: Map<Int, ImageTransformState> = emptyMap(),
    onImageTransformsChange: ((Map<Int, ImageTransformState>) -> Unit)? = null,
    unselectAllImagesTrigger: Int = 0,
    onOutsideClick: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    var isFirstSticker by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    if (template == null) return
    BoxWithConstraints(
        modifier = modifier
            .padding(20.dp)
            .then(
                if (template.width != null && template.height != null) {
                    val ratio = template.width.toFloat() / template.height.toFloat()
                    Modifier.aspectRatio(ratio)
                } else {
                    Modifier
                }
            )
    ) {
        val bannerWidth = constraints.maxWidth.toFloat()
        val bannerHeight = constraints.maxHeight.toFloat()

        // Separate selected state to avoid recomposition when unselecting
//        var selectedImageIndex by remember { mutableStateOf<Int>(0) }

        // States for image transforms only (no selected state to avoid recomposition)
        val imageTransformsLocal = remember { mutableStateMapOf<Int, ImageTransformState>() }

        // Sync with prop imageTransforms
        LaunchedEffect(imageTransforms) {
            if (imageTransforms.isNotEmpty()) {
                imageTransforms.forEach { (index, transform) ->
                    imageTransformsLocal[index] = transform
                }
            }
        }

        // Unselect all images when trigger is fired
        LaunchedEffect(unselectAllImagesTrigger) {
            if (unselectAllImagesTrigger > 0) {
                viewmodel.selectedImageIndex(null)
            }
        }

        // Calculate initial transforms only once when entering screen
        // Use a key that changes when template/selectedImages change, but only run once per combination
        LaunchedEffect(template, selectedImages) {
            // Only calculate if imageTransforms is empty (first time)
            if (imageTransforms.isEmpty() &&
                selectedImages.isNotEmpty() &&
                bannerWidth > 0f &&
                bannerHeight > 0f
            ) {
                val initialTransforms = calculateInitialTransformsForTemplate(
                    template = template,
                    selectedImages = selectedImages,
                    canvasWidth = bannerWidth,
                    canvasHeight = bannerHeight,
                    context = context
                )
                if (initialTransforms.isNotEmpty()) {
                    onImageTransformsChange?.invoke(initialTransforms)
                }
            }
            // After this block completes, LaunchedEffect won't run again unless template/selectedImages change
            // And if imageTransforms is not empty, it won't calculate again
        }

        // Detect tap outside cells to unselect (only when image is selected to avoid blocking sticker)
        if (uiState.selectedImageIndex != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(
                        template.cells?.size ?: 0,
                        bannerWidth,
                        bannerHeight,
                        uiState.selectedImageIndex
                    ) {
                        detectTapGestures { tapOffset ->
                            val x = tapOffset.x
                            val y = tapOffset.y

                            // Check if tap is inside any cell
                            val insideCell = template.cells?.any { cell ->
                                val cellX = (cell.x ?: 0f) * bannerWidth
                                val cellY = (cell.y ?: 0f) * bannerHeight
                                val cellWidth = (cell.width ?: 0f) * bannerWidth
                                val cellHeight = (cell.height ?: 0f) * bannerHeight

                                x >= cellX &&
                                        x <= cellX + cellWidth &&
                                        y >= cellY &&
                                        y <= cellY + cellHeight
                            } ?: false

                            if (!insideCell) {
                                onOutsideClick?.invoke()
                            }
                        }
                    }
            )
        }

        // Contents (cells) - Layer 1
        template.cells?.forEachIndexed { index, cell ->
            val width = cell.width ?: 0f
            val height = cell.height ?: 0f
            val cellWidth = width * bannerWidth
            val cellHeight = height * bannerHeight

            val transformState =
                imageTransformsLocal[index] ?: imageTransforms[index] ?: ImageTransformState()
            val isSelected = uiState.selectedImageIndex == index

            Box(
                modifier = Modifier
                    .baseBannerItemModifier(
                        x = cell.x,
                        y = cell.y,
                        width = cell.width,
                        height = cell.height,
                        rotate = cell.rotate?.toFloat(),
                    )
                    .then(
                        if (isSelected) {
                            Modifier
                                .border(
                                    width = 3.dp,
                                    color = Color(0xFF6425F3),
                                    shape = RoundedCornerShape(4.dp)
                                )
                        } else {
                            Modifier
                        }
                    )
                    .clipToBounds()
                    .then(
                        if (isSelected) {
                            // Use transformState from local state (user interactions) instead of prop
                            val currentTransformState =
                                imageTransformsLocal[index] ?: transformState
                            transformGestureModifier(
                                index = index,
                                transformState = currentTransformState,
                                cellWidth = cellWidth,
                                cellHeight = cellHeight,
                                imageTransforms = imageTransformsLocal,
                                selectedImageIndex = uiState.selectedImageIndex,
                                onSelectedImageIndexChange = {
                                    viewmodel.selectedImageIndex(it ?: 0)
                                },
                                onImageTransformsChange = onImageTransformsChange
                            )
                        } else {
                            selectGestureModifier(
                                index = index,
                                selectedImageIndex = uiState.selectedImageIndex,
                                onSelectedImageIndexChange = {
                                    viewmodel.selectedImageIndex(it ?: 0)
//                                    selectedImageIndex = it ?: 0
                                },
                                imageUri = selectedImages[index] ?: Uri.EMPTY,
                                onImageTransformsChange = onImageTransformsChange
                            )
                        }
                    )
            ) {
                selectedImages[index]?.let { uri ->
                    val currentTransform = imageTransformsLocal[index] ?: imageTransforms[index]
                    ?: ImageTransformState()
                    AsyncImage(
                        model = uri,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                translationX = currentTransform.offset.x
                                translationY = currentTransform.offset.y
                                scaleX = currentTransform.scale
                                scaleY = currentTransform.scale
                                transformOrigin = TransformOrigin.Center
                                clip = true
                            },
                    )
                }
            }
        }

        template.bannerUrl?.let { bannerUrl ->
            LoadImage(
                model = bannerUrl,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillWidth
            )
        }

        if (viewmodel.isFirstSticker) {
            template.layer?.forEachIndexed { index, model ->
                scope.launch(Dispatchers.IO) {
                    stickerView.addStickerFromServer(model.toFreeStyleSticker(index))
                }
            }
            viewmodel.isFirstSticker = false
            stickerView.setShowFocus(false)
        }
//        if(isFirstSticker) {
//            isFirstSticker = false
//            icons?.forEach {
//                stickerView.addStickerFromServer(it)
//            }
//            stickerView.setShowFocus(false)
//
//        }

        // Only set showFocus to false when image is selected, not on every recomposition
        // This prevents resetting sticker focus when unselecting images
        LaunchedEffect(uiState.selectedImageIndex) {
            if (uiState.selectedImageIndex != null) {
                stickerView.setShowFocus(false)
            }
        }

        FreeStyleStickerComposeView(
            modifier = Modifier
                .fillMaxSize()
                .clipToBounds(),
            view = stickerView
        )
    }
}

@Composable
fun transformGestureModifier(
    index: Int,
    transformState: ImageTransformState,
    cellWidth: Float,
    cellHeight: Float,
    imageTransforms: MutableMap<Int, ImageTransformState>,
    selectedImageIndex: Int?,
    onSelectedImageIndexChange: (Int?) -> Unit,
    onImageTransformsChange: ((Map<Int, ImageTransformState>) -> Unit)?
): Modifier {
    return Modifier.pointerInput(index, cellWidth, cellHeight) {
        val centerX = cellWidth / 2f
        val centerY = cellHeight / 2f

        // Initialize from current transformState (from prop)
        var localScale = transformState.scale
        var localOffset = transformState.offset

        detectTransformGestures(panZoomLock = false) { centroid, pan, zoom, _ ->
            localScale = (localScale * zoom).coerceIn(0.5f, 3f)

            val localCentroidX = centroid.x - centerX
            val localCentroidY = centroid.y - centerY
            val scaleChange = zoom

            localOffset = Offset(
                x = localOffset.x + pan.x - localCentroidX * (scaleChange - 1f),
                y = localOffset.y + pan.y - localCentroidY * (scaleChange - 1f)
            )

            val maxOffsetX = (cellWidth * (localScale - 1f) / 2f).coerceAtLeast(0f)
            val maxOffsetY = (cellHeight * (localScale - 1f) / 2f).coerceAtLeast(0f)

            localOffset = Offset(
                x = localOffset.x.coerceIn(-maxOffsetX, maxOffsetX),
                y = localOffset.y.coerceIn(-maxOffsetY, maxOffsetY)
            )

            val currentTransform = ImageTransformState(localOffset, localScale)
            imageTransforms[index] = currentTransform
        }

        detectTapGestures {
            // Sync transforms when tap (which happens after transform gesture ends)
            val currentTransform = ImageTransformState(localOffset, localScale)
            imageTransforms[index] = currentTransform
            onImageTransformsChange?.invoke(imageTransforms.toMap())
        }
    }
}

@Composable
fun transformGestureModifierForLayer(
    index: Int,
    transformState: ImageTransformState,
    initialX: Float,
    initialY: Float,
    layerStates: MutableMap<Int, Pair<ImageTransformState, Boolean>>,
    onLayerTransformsChange: ((Map<Int, ImageTransformState>) -> Unit)?,
    onDragOffsetChange: (Offset) -> Unit,
    onDragStateChange: (Boolean) -> Unit
): Modifier {
    return Modifier.pointerInput(index, transformState) {
        var localScale = transformState.scale
        var localOffset = transformState.offset
        var accumulatedDrag = Offset.Zero

        // Only allow pan (move), not zoom - zoom is handled by LayerControlButtons
        detectDragGestures(
            onDragStart = {
                onDragStateChange(true)
                accumulatedDrag = Offset.Zero
            },
            onDrag = { change, dragAmount ->
                change.consume()
                // Accumulate drag amount for smooth UI update
                accumulatedDrag += dragAmount
                onDragOffsetChange(accumulatedDrag)
            },
            onDragEnd = {
                onDragStateChange(false)
                // Apply accumulated drag to final offset
                localOffset = Offset(
                    x = localOffset.x + accumulatedDrag.x,
                    y = localOffset.y + accumulatedDrag.y
                )
                // Only call callback when drag ends to avoid lag
                val currentTransform = ImageTransformState(localOffset, localScale)
                layerStates[index] = currentTransform to true
                onLayerTransformsChange?.invoke(extractTransforms(layerStates))
                // Reset drag offset
                accumulatedDrag = Offset.Zero
                onDragOffsetChange(Offset.Zero)
            },
            onDragCancel = {
                onDragStateChange(false)
                // Reset on cancel
                accumulatedDrag = Offset.Zero
                onDragOffsetChange(Offset.Zero)
            }
        )

        detectTapGestures {
            // Tap to toggle select state
            val current = layerStates[index]
            val isSelected = current?.second == true
            val currentTransform = ImageTransformState(localOffset, localScale)
            layerStates[index] = currentTransform to !isSelected
            onLayerTransformsChange?.invoke(extractTransforms(layerStates))
        }
    }
}

@Composable
fun selectGestureModifier(
    index: Int,
    selectedImageIndex: Int?,
    onSelectedImageIndexChange: (Int?) -> Unit,
    imageUri: Uri,
    onImageTransformsChange: ((Map<Int, ImageTransformState>) -> Unit)?
): Modifier {
    return Modifier.pointerInput(index, selectedImageIndex) {
        detectTapGestures {
            val isSelected = selectedImageIndex == index

            if (isSelected) {
                onSelectedImageIndexChange(null)
            } else {
                onSelectedImageIndexChange(index)
            }
        }
    }
}

@Composable
fun LayerControlButtons(
    onDelete: () -> Unit,
    onFlip: () -> Unit,
    onZoomChange: (Float) -> Unit,
    currentScale: Float,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    // Track drag offset to make button follow finger
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    var isDragging by remember { mutableStateOf(false) }

    // Function to update drag offset (will be called from pointerInput)
    val updateDragOffset: (Offset) -> Unit = remember {
        { newOffset ->
            dragOffset = newOffset
        }
    }

    val updateIsDragging: (Boolean) -> Unit = remember {
        { dragging ->
            isDragging = dragging
        }
    }

    Box(modifier = modifier) {
        // Delete button - Top left
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = (-12).dp, y = (-12).dp)
                .size(32.dp)
                .background(Color(0xFF6425F3), CircleShape)
                .clickableWithAlphaEffect(onClick = onDelete),
            contentAlignment = Alignment.Center
        ) {
            ImageWidget(
                resId = R.drawable.ic_close_sticker,
                modifier = Modifier.size(20.dp)
            )
        }

        // Flip button - Bottom left
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = (-12).dp, y = 12.dp)
                .size(32.dp)
                .background(Color(0xFF6425F3), CircleShape)
                .clickableWithAlphaEffect(onClick = onFlip),
            contentAlignment = Alignment.Center
        ) {
            ImageWidget(
                resId = R.drawable.ic_flip_horizontal,
                modifier = Modifier.size(20.dp)
            )
        }

        // Zoom button - Bottom right (with drag gesture)
        val buttonOffsetPx = with(density) { 12.dp.toPx() }
        val buttonSizePx = with(density) { 32.dp.toPx() }
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(
                    x = with(density) { (12.dp + dragOffset.x.toDp()) },
                    y = with(density) { (12.dp + dragOffset.y.toDp()) }
                )
                .size(32.dp)
                .background(Color(0xFF6425F3), CircleShape)
                .pointerInput(currentScale, buttonOffsetPx, updateDragOffset, updateIsDragging) {
                    var initialDistance: Float? = null
                    var initialScale: Float = currentScale
                    var initialButtonCenter: Offset? = null
                    var initialTouchPosition: Offset? = null
                    var accumulatedDrag = Offset.Zero

                    // Sticker center position (relative to parent Box)
                    val stickerCenterX = size.width / 2f
                    val stickerCenterY = size.height / 2f

                    // Button center position relative to parent Box
                    val buttonCenterX = size.width - buttonOffsetPx - buttonSizePx / 2
                    val buttonCenterY = size.height - buttonOffsetPx - buttonSizePx / 2

                    detectDragGestures(
                        onDragStart = { offset ->
                            updateIsDragging(true)
                            accumulatedDrag = Offset.Zero
                            updateDragOffset(Offset.Zero)

                            // Store initial button center position
                            initialButtonCenter = Offset(buttonCenterX, buttonCenterY)
                            initialTouchPosition = offset

                            // Calculate initial distance from sticker center to button center
                            initialDistance = sqrt(
                                (buttonCenterX - stickerCenterX).pow(2) +
                                        (buttonCenterY - stickerCenterY).pow(2)
                            )
                            initialScale = currentScale
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()

                            if (initialDistance != null && initialButtonCenter != null && initialTouchPosition != null) {
                                // Calculate new button center position by following finger
                                accumulatedDrag += dragAmount
                                val newButtonCenter = initialButtonCenter!! + accumulatedDrag

                                // Update icon offset to make it follow finger
                                updateDragOffset(accumulatedDrag)

                                // Calculate new distance from sticker center to new button center
                                val newDistance = sqrt(
                                    (newButtonCenter.x - stickerCenterX).pow(2) +
                                            (newButtonCenter.y - stickerCenterY).pow(2)
                                )

                                // Calculate scale based on distance ratio
                                val scaleRatio = newDistance / initialDistance!!
                                val newScale = (initialScale * scaleRatio).coerceIn(0.1f, 10f)
                                onZoomChange(newScale)
                            }
                        },
                        onDragEnd = {
                            updateIsDragging(false)
                            // Reset icon position when drag ends
                            accumulatedDrag = Offset.Zero
                            updateDragOffset(Offset.Zero)
                        },
                        onDragCancel = {
                            updateIsDragging(false)
                            // Reset on drag cancel
                            accumulatedDrag = Offset.Zero
                            updateDragOffset(Offset.Zero)
                            initialDistance = null
                            initialScale = currentScale
                            initialButtonCenter = null
                            initialTouchPosition = null
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            ImageWidget(
                resId = R.drawable.ic_swap_tool,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

private suspend fun calculateInitialTransformsForTemplate(
    template: TemplateModel,
    selectedImages: Map<Int, Uri>,
    canvasWidth: Float,
    canvasHeight: Float,
    context: android.content.Context
): Map<Int, ImageTransformState> {
    return withContext(Dispatchers.IO) {
        val transforms = mutableMapOf<Int, ImageTransformState>()

        template.cells?.forEachIndexed { index, cell ->
            val imageUri = selectedImages[index] ?: return@forEachIndexed

            // Calculate cell bounds in pixels
            val cellWidth = (cell.width ?: 0f) * canvasWidth
            val cellHeight = (cell.height ?: 0f) * canvasHeight

            // Get image size
            val imageSize = getImageSizeFromUri(context, imageUri)

            // Calculate initial scale to fill the cell
            val scale = if (imageSize != null && cellWidth > 0f && cellHeight > 0f) {
                calculateFillScale(
                    boundWidth = cellWidth,
                    boundHeight = cellHeight,
                    imageWidth = imageSize.width,
                    imageHeight = imageSize.height
                )
            } else {
                1f
            }

            transforms[index] = ImageTransformState(
                offset = Offset.Zero,
                scale = scale
            )
        }

        transforms
    }
}

private fun calculateFillScale(
    boundWidth: Float,
    boundHeight: Float,
    imageWidth: Float,
    imageHeight: Float
): Float {
    val widthRatio = boundWidth / imageWidth
    val heightRatio = boundHeight / imageHeight
    val fitScale = min(widthRatio, heightRatio)
    val fillScale = max(widthRatio, heightRatio)
    return if (fitScale > 0f) fillScale / fitScale else 1f
}

private suspend fun getImageSizeFromUri(context: android.content.Context, uri: Uri): Size? {
    return try {
        withContext(Dispatchers.IO) {
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            val inputStream = context.contentResolver.openInputStream(uri)
            inputStream?.use {
                BitmapFactory.decodeStream(it, null, options)
                if (options.outWidth > 0 && options.outHeight > 0) {
                    Size(options.outWidth.toFloat(), options.outHeight.toFloat())
                } else {
                    null
                }
            }
        }
    } catch (_: Exception) {
        null
    }
}

