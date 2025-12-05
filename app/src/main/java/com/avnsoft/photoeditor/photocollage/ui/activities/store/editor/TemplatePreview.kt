package com.avnsoft.photoeditor.photocollage.ui.activities.store.editor

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil.compose.AsyncImage
import com.avnsoft.photoeditor.photocollage.R
import com.avnsoft.photoeditor.photocollage.data.model.template.TemplateModel
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.preview.ImageTransformState
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.preview.extractTransforms
import com.basesource.base.ui.image.LoadImage
import com.basesource.base.utils.ImageWidget
import com.basesource.base.utils.clickableWithAlphaEffect
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.sqrt

@Composable
fun TemplatePreview(
    template: TemplateModel,
    selectedImages: Map<Int, Uri>,
    imageTransforms: Map<Int, ImageTransformState> = emptyMap(),
    layerTransforms: Map<Int, ImageTransformState> = emptyMap(),
    layerFlip: Map<Int, Float> = emptyMap(),
    onImageTransformsChange: ((Map<Int, ImageTransformState>) -> Unit)? = null,
    onLayerTransformsChange: ((Map<Int, ImageTransformState>) -> Unit)? = null,
    onLayerDelete: ((Int) -> Unit)? = null,
    onLayerFlip: ((Int) -> Unit)? = null,
    onLayerZoomChange: ((Int, Float) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val context = LocalContext.current

    BoxWithConstraints(modifier = modifier.clipToBounds()) {
        val bannerWidth = constraints.maxWidth.toFloat()
        val bannerHeight = constraints.maxHeight.toFloat()

        // States for image transforms
        val imageStates = remember { mutableStateMapOf<Int, Pair<ImageTransformState, Boolean>>() }
        val layerStates = remember { mutableStateMapOf<Int, Pair<ImageTransformState, Boolean>>() }

        // Initialize transforms - only update transform, preserve selected state
        imageTransforms.forEach { (index, transform) ->
            val existingState = imageStates[index]
            val isSelected = existingState?.second ?: false
            // Only update if transform actually changed to avoid unnecessary recomposition
            if (existingState?.first != transform) {
                imageStates[index] = transform to isSelected
            }
        }
        layerTransforms.forEach { (index, transform) ->
            val existingState = layerStates[index]
            val isSelected = existingState?.second ?: false
            // Only update if transform actually changed to avoid unnecessary recomposition
            if (existingState?.first != transform) {
                layerStates[index] = transform to isSelected
            }
        }

        // Banner background
        template.bannerUrl?.let { bannerUrl ->
            LoadImage(
                model = bannerUrl,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds
            )
        }

        // Contents (cells) - Layer 1
        template.cells?.forEachIndexed { index, cell ->
            val x = cell.x ?: 0f
            val y = cell.y ?: 0f
            val width = cell.width ?: 0f
            val height = cell.height ?: 0f
            val rotate = cell.rotate?.toFloat() ?: 0f

            val cellX = x * bannerWidth
            val cellY = y * bannerHeight
            val cellWidth = width * bannerWidth
            val cellHeight = height * bannerHeight

            val transformState = imageStates[index]?.first ?: ImageTransformState()
            val isSelected = imageStates[index]?.second ?: false
            val hasImage = selectedImages.containsKey(index)

            Box(
                modifier = Modifier
                    .offset(
                        x = with(density) { cellX.toDp() },
                        y = with(density) { cellY.toDp() }
                    )
                    .size(
                        width = with(density) { cellWidth.toDp() },
                        height = with(density) { cellHeight.toDp() }
                    )
                    .then(
                        if (hasImage && isSelected) {
                            Modifier
                                .border(
                                    width = 3.dp,
                                    color = Color(0xFF6425F3),
                                    shape = RoundedCornerShape(4.dp)
                                )
                        } else if (isSelected) {
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
                        if (hasImage && isSelected) {
                            transformGestureModifier(
                                index = index,
                                transformState = transformState,
                                cellWidth = cellWidth,
                                cellHeight = cellHeight,
                                imageStates = imageStates,
                                onImageTransformsChange = onImageTransformsChange
                            )
                        } else {
                            selectGestureModifier(
                                index = index,
                                imageStates = imageStates,
                                imageUri = selectedImages[index] ?: Uri.EMPTY,
                                onImageTransformsChange = onImageTransformsChange
                            )
                        }
                    )
            ) {
                selectedImages[index]?.let { uri ->
                    val currentTransform = imageStates[index]?.first ?: ImageTransformState()
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
                                rotationZ = rotate
                                transformOrigin = TransformOrigin.Center
                                clip = true
                            },
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }

        // Frame overlay - Layer 2
        template.frameUrl?.let { frameUrl ->
            LoadImage(
                model = frameUrl,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds
            )
        }

        // Layers overlay - Layer 3 (free move and zoom, no bounds)
        template.layer?.forEachIndexed { index, layerItem ->
            val x = layerItem.x ?: 0f
            val y = layerItem.y ?: 0f
            val width = layerItem.width ?: 0f
            val height = layerItem.height ?: 0f
            val rotate = layerItem.rotate?.toFloat() ?: 0f

            val layerX = x * bannerWidth
            val layerY = y * bannerHeight
            val layerWidth = width * bannerWidth
            val layerHeight = height * bannerHeight

            val layerState = layerStates[index]
            val transformState = layerState?.first ?: ImageTransformState()
            val isSelected = layerState?.second ?: false

            // Use local state for smooth dragging without triggering recomposition
            val localDragOffset = remember(index) { mutableStateOf(Offset.Zero) }
            val isDragging = remember(index) { mutableStateOf(false) }
            // Local scale state for zoom - only update this layer, not entire screen
            val localZoomScale = remember(index) { mutableStateOf(transformState.scale) }

            val currentTransform = layerStates[index]?.first ?: ImageTransformState()
            val dragOffset = if (isDragging.value) localDragOffset.value else Offset.Zero
            val finalX = layerX + currentTransform.offset.x + dragOffset.x
            val finalY = layerY + currentTransform.offset.y + dragOffset.y
            // Use local scale if dragging, otherwise use transform scale
            val finalScale = if (isDragging.value) localZoomScale.value else currentTransform.scale
            val finalRotate = rotate

            layerItem.urlThumb?.let { urlThumb ->
                Box(
                    modifier = Modifier
                        .offset(
                            x = with(density) { finalX.toDp() },
                            y = with(density) { finalY.toDp() }
                        )
                        .size(
                            width = with(density) { (layerWidth * finalScale).toDp() },
                            height = with(density) { (layerHeight * finalScale).toDp() }
                        )
                        .then(
                            if (isSelected) {
                                Modifier
                                    .border(
                                        width = 1.dp,
                                        color = Color.White,
                                        shape = RoundedCornerShape(4.dp)
                                    )
                            } else {
                                Modifier
                            }
                        )
                ) {
                    val flipScale = layerFlip[index] ?: 1f
                    LoadImage(
                        model = urlThumb,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                scaleX = flipScale
                                rotationZ = finalRotate
                                transformOrigin = TransformOrigin.Center
                            }
                            .then(
                                if (isSelected) {
                                    transformGestureModifierForLayer(
                                        index = index,
                                        transformState = transformState,
                                        initialX = layerX,
                                        initialY = layerY,
                                        layerStates = layerStates,
                                        onLayerTransformsChange = onLayerTransformsChange,
                                        onDragOffsetChange = { offset ->
                                            localDragOffset.value = offset
                                        },
                                        onDragStateChange = { dragging ->
                                            isDragging.value = dragging
                                        }
                                    )
                                } else {
                                    selectGestureModifier(
                                        index = index,
                                        imageStates = layerStates,
                                        imageUri = urlThumb.toUri(),
                                        onImageTransformsChange = onLayerTransformsChange
                                    )
                                }
                            ),
                        contentScale = ContentScale.FillBounds
                    )
                    
                    // Control buttons when selected
                    if (isSelected) {
                        LayerControlButtons(
                            onDelete = { onLayerDelete?.invoke(index) },
                            onFlip = { onLayerFlip?.invoke(index) },
                            onZoomChange = { newScale ->
                                localZoomScale.value = newScale
                                val newTransform = ImageTransformState(
                                    offset = currentTransform.offset + dragOffset,
                                    scale = newScale
                                )
                                layerStates[index] = newTransform to true
                                onLayerTransformsChange?.invoke(extractTransforms(layerStates))
                            },
                            currentScale = finalScale,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun transformGestureModifier(
    index: Int,
    transformState: ImageTransformState,
    cellWidth: Float,
    cellHeight: Float,
    imageStates: MutableMap<Int, Pair<ImageTransformState, Boolean>>,
    onImageTransformsChange: ((Map<Int, ImageTransformState>) -> Unit)?
): Modifier {
    return Modifier.pointerInput(index, cellWidth, cellHeight) {
        val centerX = cellWidth / 2f
        val centerY = cellHeight / 2f

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

            imageStates[index] = ImageTransformState(localOffset, localScale) to true
        }

        detectTapGestures {
            val currentTransform = ImageTransformState(localOffset, localScale)
            imageStates[index] = currentTransform to false
            onImageTransformsChange?.invoke(extractTransforms(imageStates))
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
    imageStates: MutableMap<Int, Pair<ImageTransformState, Boolean>>,
    imageUri: Uri,
    onImageTransformsChange: ((Map<Int, ImageTransformState>) -> Unit)?
): Modifier {
    return Modifier.pointerInput(index) {
        detectTapGestures {
            val current = imageStates[index]
            val isSelected = current?.second == true

            if (isSelected) {
                val (transform, _) = current
                imageStates[index] = transform to false
            } else {
                imageStates.keys.forEach { key ->
                    if (key != index) {
                        val (t, _) = imageStates[key] ?: (ImageTransformState() to false)
                        imageStates[key] = t to false
                    }
                }
                val (transform, _) = current ?: (ImageTransformState() to false)
                imageStates[index] = transform to true
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

