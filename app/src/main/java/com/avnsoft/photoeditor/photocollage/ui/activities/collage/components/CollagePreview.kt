package com.avnsoft.photoeditor.photocollage.ui.activities.collage.components

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.zIndex
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil.compose.AsyncImage
import com.avnsoft.photoeditor.photocollage.R
import com.avnsoft.photoeditor.photocollage.data.model.collage.CellSpec
import com.avnsoft.photoeditor.photocollage.data.model.collage.CollageTemplate
import com.avnsoft.photoeditor.photocollage.data.model.collage.FreePolygonShape
import com.avnsoft.photoeditor.photocollage.ui.theme.BackgroundWhite
import com.avnsoft.photoeditor.photocollage.ui.theme.Primary500
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

data class ImageTransformState(
    val offset: Offset = Offset.Zero,
    val scale: Float = 1f
)

@Composable
fun CollagePreview(
    images: List<Uri>,
    template: CollageTemplate,
    gap: Dp = 6.dp,
    corner: Dp = 1.dp,
    borderWidth: Dp = 5.dp,
    backgroundSelection: BackgroundSelection? = null,
    imageTransforms: Map<Int, ImageTransformState> = emptyMap(),
    onImageClick: ((Uri) -> Unit)? = null,
    onImageTransformsChange: ((Map<Int, ImageTransformState>) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier) {
        val density = LocalDensity.current
        val context = LocalContext.current
        val canvasWidth = constraints.maxWidth.toFloat()
        val canvasHeight = constraints.maxHeight.toFloat()

        BackgroundLayer(
            backgroundSelection = backgroundSelection,
            modifier = Modifier.fillMaxSize()
        )

        var processedCells by remember { mutableStateOf<List<ProcessedCellData>>(emptyList()) }

        LaunchedEffect(template, images, canvasWidth, canvasHeight) {
            processedCells = withContext(Dispatchers.IO) {
                CollagePreviewDataProcessor.processTemplate(
                    template = template,
                    images = images,
                    canvasWidth = canvasWidth,
                    canvasHeight = canvasHeight
                )
            }
        }

        val imageStates = remember { mutableStateMapOf<Int, Pair<ImageTransformState, Boolean>>() }

        LaunchedEffect(processedCells, imageTransforms) {
            if (imageTransforms.isEmpty() && processedCells.isNotEmpty()) {
                delay(100)
                val initialTransforms = calculateInitialTransforms(context, processedCells)
                initialTransforms.forEach { (index, transform) ->
                    imageStates[index] = transform to false
                }
                if (initialTransforms.isNotEmpty()) {
                    onImageTransformsChange?.invoke(initialTransforms)
                }
            }
        }

        LaunchedEffect(imageTransforms) {
            if (imageTransforms.isNotEmpty()) {
                imageTransforms.forEach { (index, transform) ->
                    val currentState = imageStates[index]
                    imageStates[index] = transform to (currentState?.second ?: false)
                }
            }
        }

        CollageContent(
            processedCells = processedCells,
            gap = gap,
            corner = corner,
            borderWidthPx = with(density) { borderWidth.toPx() },
            cornerRadiusPx = with(density) { corner.toPx() },
            density = density,
            imageStates = imageStates,
            onImageClick = onImageClick,
            onImageTransformsChange = onImageTransformsChange
        )
    }
}

@Composable
private fun CollageContent(
    processedCells: List<ProcessedCellData>,
    gap: Dp,
    corner: Dp,
    borderWidthPx: Float,
    cornerRadiusPx: Float,
    density: Density,
    imageStates: @JvmSuppressWildcards MutableMap<Int, Pair<ImageTransformState, Boolean>>,
    onImageClick: ((Uri) -> Unit)?,
    onImageTransformsChange: ((Map<Int, ImageTransformState>) -> Unit)?
) {
    val gapPx = with(density) { gap.toPx() }
    
    // Render cells trước
    processedCells.forEachIndexed { index, cellData ->
        val (transformState, isSelected) = imageStates[index] ?: (ImageTransformState() to false)
        val shape = cellData.createShape(corner, cornerRadiusPx, gapPx)

        CollageImageCell(
            cellData = cellData,
            transformState = transformState,
            isSelected = isSelected,
            shape = shape,
            gap = gap,
            borderWidthPx = borderWidthPx,
            cornerRadiusPx = cornerRadiusPx,
            corner = corner,
            density = density,
            imageStates = imageStates,
            index = index,
            onImageClick = onImageClick,
            onImageTransformsChange = onImageTransformsChange
        )
    }
}

@Composable
private fun CollageImageCell(
    cellData: ProcessedCellData,
    transformState: ImageTransformState,
    isSelected: Boolean,
    shape: Shape,
    gap: Dp,
    borderWidthPx: Float,
    cornerRadiusPx: Float,
    corner: Dp,
    density: Density,
    imageStates: @JvmSuppressWildcards MutableMap<Int, Pair<ImageTransformState, Boolean>>,
    index: Int,
    onImageClick: ((Uri) -> Unit)?,
    onImageTransformsChange: ((Map<Int, ImageTransformState>) -> Unit)?
) {
    val shouldShowBorder = isSelected || cellData.imageUri.toString().contains("true")
    val imageBoxModifier = with(density) {
        Modifier
            .offset(x = cellData.left.toDp(), y = cellData.top.toDp())
            .size(cellData.width.toDp(), cellData.height.toDp())
            .clip(shape)
            .then(clearAreaModifier(cellData, cornerRadiusPx))
            .padding(gap / 2)
    }

    Box(modifier = imageBoxModifier.clipToBounds()) {
        val currentTransform = imageStates[index]?.first ?: ImageTransformState()
        val borderPadding = if (shouldShowBorder) {
            val borderWidth = if (isSelected) borderWidthPx * 1.5f else borderWidthPx
            with(density) { borderWidth.toDp() }
        } else {
            0.dp
        }

        AsyncImage(
            model = cellData.imageUri,
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
                }
                .then(
                    if (isSelected) {
                        transformGestureModifier(
                            index = index,
                            transformState = currentTransform,
                            cellWidth = cellData.width,
                            cellHeight = cellData.height,
                            imageStates = imageStates,
                            onImageTransformsChange = onImageTransformsChange
                        )
                    } else {
                        selectGestureModifier(
                            index = index,
                            imageStates = imageStates,
                            onImageClick = onImageClick,
                            imageUri = cellData.imageUri,
                            onImageTransformsChange = onImageTransformsChange
                        )
                    }
                ),
            error = painterResource(R.drawable.ic_empty_image)
        )

        // Vẽ border sau image để luôn nằm trên
        if (shouldShowBorder) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawBehind {
                        val outline = shape.createOutline(
                            size = size,
                            layoutDirection = LayoutDirection.Ltr,
                            density = this@drawBehind
                        )
                        if (outline is Outline.Generic) {
                            val borderWidth = if (isSelected) borderWidthPx * 1.5f else borderWidthPx
                            drawPath(
                                path = outline.path,
                                color = Primary500,
                                style = Stroke(
                                    width = borderWidth,
                                    cap = StrokeCap.Round,
                                    pathEffect = PathEffect.cornerPathEffect(cornerRadiusPx * 1.5f)
                                )
                            )
                        }
                    }
            )
        }
    }
}

@Composable
private fun transformGestureModifier(
    index: Int,
    transformState: ImageTransformState,
    cellWidth: Float,
    cellHeight: Float,
    imageStates: @JvmSuppressWildcards MutableMap<Int, Pair<ImageTransformState, Boolean>>,
    onImageTransformsChange: ((Map<Int, ImageTransformState>) -> Unit)?
): Modifier {
    return Modifier.pointerInput(index, cellWidth, cellHeight) {
        val imageCenterX = cellWidth / 2f
        val imageCenterY = cellHeight / 2f
        var localScale = transformState.scale
        var localOffset = transformState.offset

        detectTransformGestures(panZoomLock = false) { centroid, pan, zoom, _ ->
            localScale = (localScale * zoom).coerceIn(0.5f, 3f)
            val localCentroidX = centroid.x - imageCenterX
            val localCentroidY = centroid.y - imageCenterY
            val scaleChange = zoom

            localOffset = Offset(
                localOffset.x + pan.x - (localCentroidX * (scaleChange - 1f)),
                localOffset.y + pan.y - (localCentroidY * (scaleChange - 1f))
            )

            val maxOffsetX = (cellWidth * (localScale - 1f) / 2f).coerceAtLeast(0f)
            val maxOffsetY = (cellHeight * (localScale - 1f) / 2f).coerceAtLeast(0f)

            localOffset = Offset(
                localOffset.x.coerceIn(-maxOffsetX, maxOffsetX),
                localOffset.y.coerceIn(-maxOffsetY, maxOffsetY)
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
private fun selectGestureModifier(
    index: Int,
    imageStates: @JvmSuppressWildcards MutableMap<Int, Pair<ImageTransformState, Boolean>>,
    onImageClick: ((Uri) -> Unit)?,
    imageUri: Uri,
    onImageTransformsChange: ((Map<Int, ImageTransformState>) -> Unit)?
): Modifier {
    return Modifier.pointerInput(index) {
        detectTapGestures {
            val currentState = imageStates[index]
            val isCurrentlySelected = currentState?.second == true

            if (isCurrentlySelected) {
                val (existingTransform, _) = currentState
                imageStates[index] = existingTransform to false
            } else {
                imageStates.keys.forEach { key ->
                    if (key != index) {
                        val otherState = imageStates[key]
                        val (existingTransform, _) = otherState ?: (ImageTransformState() to false)
                        imageStates[key] = existingTransform to false
                    }
                }
                val (existingTransform, _) = currentState ?: (ImageTransformState() to false)
                imageStates[index] = existingTransform to true
                onImageClick?.invoke(imageUri)
            }
        }
    }
}

@Composable
private fun clearAreaModifier(
    cellData: ProcessedCellData,
    cornerRadiusPx: Float
): Modifier {
    return when {
        cellData.clearPathType != null && cellData.clearPathRatioBound != null -> {
            Modifier
                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                .drawWithContent {
                drawContent()
                val actualWidth = size.width
                val actualHeight = size.height
                val ratioBound = cellData.clearPathRatioBound
                val left = ratioBound[0] * actualWidth
                val top = ratioBound[1] * actualHeight
                val right = ratioBound[2] * actualWidth
                val bottom = ratioBound[3] * actualHeight
                val pathWidth = right - left
                val pathHeight = bottom - top

                val (finalLeft, finalTop) = if (cellData.shrinkMethod == "3_8") {
                    // Với SHRINK_METHOD_3_8, căn giữa hoàn toàn
                    val centerX = actualWidth / 2f
                    val centerY = actualHeight / 2f
                    (centerX - pathWidth / 2f) to (centerY - pathHeight / 2f)
                } else if (cellData.shrinkMethod == "3_6") {
                    // Với SHRINK_METHOD_3_6 cho clearPath, logic đặc biệt
                    val ratioBound = cellData.clearPathRatioBound
                    val x = if (ratioBound != null && ratioBound.size >= 4 && ratioBound[0] > 0) {
                        actualWidth - pathWidth / 2f
                    } else {
                        -pathWidth / 2f
                    }
                    val y = actualHeight / 2f - pathHeight / 2f
                    x to y
                } else {
                    calculateCenteredPosition(
                        left = left,
                        top = top,
                        width = pathWidth,
                        height = pathHeight,
                        containerWidth = actualWidth,
                        containerHeight = actualHeight,
                        centerHorizontal = cellData.clearPathInCenterHorizontal,
                        centerVertical = cellData.clearPathInCenterVertical
                    )
                }

                val useRoundedCorner = cellData.cornerMethod == "3_13"
                val useHexagonCorner = cellData.cornerMethod == "3_6"
                val clearPath = if (useHexagonCorner) {
                    // Với CORNER_METHOD_3_6, tạo hexagon với size = min(pathWidth, pathHeight)
                    val hexSize = kotlin.math.min(pathWidth, pathHeight)
                    val centerX = finalLeft + pathWidth / 2f
                    val centerY = finalTop + pathHeight / 2f
                    Path().createHexagonPath(
                        left = centerX - hexSize / 2f,
                        top = centerY - hexSize / 2f,
                        width = hexSize,
                        height = hexSize,
                        cornerRadiusPx = cornerRadiusPx
                    )
                } else {
                    when (cellData.clearPathType) {
                        "CIRCLE" -> Path().createCirclePath(finalLeft, finalTop, pathWidth, pathHeight)
                        "HEART" -> {
                            val heartPath = Path().createHeartPath(pathWidth, pathHeight)
                            Path().apply {
                                addPath(heartPath, Offset(finalLeft, finalTop))
                            }
                        }
                        "RECT" -> {
                            if (useRoundedCorner && cornerRadiusPx > 0f) {
                                Path().createRoundedRectPath(
                                    finalLeft,
                                    finalTop,
                                    pathWidth,
                                    pathHeight,
                                    cornerRadiusPx
                                )
                            } else {
                                Path().createRectPath(finalLeft, finalTop, pathWidth, pathHeight)
                            }
                        }
                        else -> null
                    }
                }

                clearPath?.let {
                    drawPath(
                        path = it,
                        color = Color.Transparent,
                        blendMode = BlendMode.Clear
                    )
                }
            }
        }
        cellData.clearAreaPoints != null -> {
            val clearPoints = cellData.clearAreaPoints
            if (clearPoints.size >= 6 && clearPoints.size % 2 == 0) {
                Modifier
                    .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                    .drawWithContent {
                    drawContent()
                    val actualWidth = size.width
                    val actualHeight = size.height
                    val clearPath = Path().apply {
                        val x0 = clearPoints[0] * actualWidth
                        val y0 = clearPoints[1] * actualHeight
                        moveTo(x0, y0)
                        for (i in 2 until clearPoints.size step 2) {
                            val x = clearPoints[i] * actualWidth
                            val y = clearPoints[i + 1] * actualHeight
                            lineTo(x, y)
                        }
                        close()
                    }
                    drawPath(
                        path = clearPath,
                        color = Color.Transparent,
                        blendMode = BlendMode.Clear
                    )
                }
            } else {
                Modifier
            }
        }
        else -> Modifier
    }
}

private suspend fun calculateInitialTransforms(
    context: Context,
    processedCells: List<ProcessedCellData>
): Map<Int, ImageTransformState> {
    return withContext(Dispatchers.IO) {
        processedCells.mapIndexedNotNull { index, cellData ->
            val imageSize = getImageSizeFromUri(context, cellData.imageUri)
            if (imageSize != null) {
                val scale = calculateMaxZoomScale(
                    boundWidth = cellData.width,
                    boundHeight = cellData.height,
                    imageWidth = imageSize.width,
                    imageHeight = imageSize.height
                )
                index to ImageTransformState(offset = Offset.Zero, scale = scale)
            } else {
                index to ImageTransformState(offset = Offset.Zero, scale = 1f)
            }
        }.toMap()
    }
}

private fun calculateMaxZoomScale(
    boundWidth: Float,
    boundHeight: Float,
    imageWidth: Float,
    imageHeight: Float
): Float {
    val widthRatio = boundWidth / imageWidth
    val heightRatio = boundHeight / imageHeight
    val fitScale = kotlin.math.min(widthRatio, heightRatio)
    val fillScale = kotlin.math.max(widthRatio, heightRatio)
    return if (fitScale > 0f) {
        fillScale / fitScale
    } else {
        1f
    }
}

private suspend fun getImageSizeFromUri(context: Context, uri: Uri): Size? {
    return try {
        withContext(Dispatchers.IO) {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            val inputStream = context.contentResolver.openInputStream(uri)
            inputStream?.use {
                BitmapFactory.decodeStream(it, null, options)
                if (options.outWidth > 0 && options.outHeight > 0) {
                    Size(options.outWidth.toFloat(), options.outHeight.toFloat())
                } else {
                    null
                }
            } ?: null
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

private fun extractTransforms(
    imageStates: @JvmSuppressWildcards MutableMap<Int, Pair<ImageTransformState, Boolean>>
): Map<Int, ImageTransformState> {
    return imageStates.mapNotNull { (idx, pair) ->
        val (transform, _) = pair
        idx to transform
    }.toMap()
}

private fun Path.createHeartPath(width: Float, height: Float): Path {
    val size = kotlin.math.min(width, height)
    val top = 0f
    moveTo(top, top + size / 4f)
    quadraticTo(top, top, top + size / 4f, top)
    quadraticTo(top + size / 2f, top, top + size / 2f, top + size / 4f)
    quadraticTo(top + size / 2f, top, top + size * 3f / 4f, top)
    quadraticTo(top + size, top, top + size, top + size / 4f)
    quadraticTo(top + size, top + size / 2f, top + size * 3f / 4f, top + size * 3f / 4f)
    lineTo(top + size / 2f, top + size)
    lineTo(top + size / 4f, top + size * 3f / 4f)
    quadraticTo(top, top + size / 2f, top, top + size / 4f)
    close()
    return this
}

private fun Path.createCirclePath(
    left: Float,
    top: Float,
    width: Float,
    height: Float
): Path {
    val circleSize = kotlin.math.min(width, height)
    val circleX = left + width / 2f
    val circleY = top + height / 2f
    addOval(
        Rect(
            circleX - circleSize / 2f,
            circleY - circleSize / 2f,
            circleX + circleSize / 2f,
            circleY + circleSize / 2f
        )
    )
    return this
}

private fun Path.createRectPath(
    left: Float,
    top: Float,
    width: Float,
    height: Float
): Path {
    addRect(
        Rect(
            left,
            top,
            left + width,
            top + height
        )
    )
    return this
}

private fun Path.createRoundedRectPath(
    left: Float,
    top: Float,
    width: Float,
    height: Float,
    cornerRadiusPx: Float
): Path {
    val radius = cornerRadiusPx.coerceAtMost(kotlin.math.min(width, height) / 2f)
    addRoundRect(
        androidx.compose.ui.geometry.RoundRect(
            left,
            top,
            left + width,
            top + height,
            androidx.compose.ui.geometry.CornerRadius(radius, radius)
        )
    )
    return this
}

private fun Path.createHexagonPath(
    left: Float,
    top: Float,
    width: Float,
    height: Float,
    cornerRadiusPx: Float
): Path {
    val size = kotlin.math.min(width, height)
    val centerX = left + width / 2f
    val centerY = top + height / 2f
    val radius = size / 2f
    val vertexCount = 6
    val section = (2.0 * kotlin.math.PI / vertexCount).toFloat()
    
    val points = mutableListOf<Offset>()
    for (i in 0 until vertexCount) {
        val angle = section * i
        val x = centerX + radius * kotlin.math.cos(angle).toFloat()
        val y = centerY + radius * kotlin.math.sin(angle).toFloat()
        points.add(Offset(x, y))
    }
    
    if (cornerRadiusPx > 0f && points.size >= 3) {
        // Tạo path với corner radius
        moveTo(points[0].x, points[0].y)
        for (i in 1 until points.size) {
            val prev = points[(i - 1 + points.size) % points.size]
            val curr = points[i]
            val next = points[(i + 1) % points.size]
            
            // Tính vector từ prev đến curr và từ curr đến next
            val v1x = curr.x - prev.x
            val v1y = curr.y - prev.y
            val v2x = next.x - curr.x
            val v2y = next.y - curr.y
            
            // Tính độ dài vector
            val len1 = kotlin.math.sqrt((v1x * v1x + v1y * v1y).toDouble()).toFloat()
            val len2 = kotlin.math.sqrt((v2x * v2x + v2y * v2y).toDouble()).toFloat()
            
            // Tính điểm bắt đầu và kết thúc của corner
            val cornerRadius = cornerRadiusPx.coerceAtMost(kotlin.math.min(len1, len2) / 2f)
            val t1 = cornerRadius / len1
            val t2 = cornerRadius / len2
            
            val startX = prev.x + v1x * (1f - t1)
            val startY = prev.y + v1y * (1f - t1)
            val endX = curr.x + v2x * t2
            val endY = curr.y + v2y * t2
            
            if (i == 1) {
                moveTo(startX, startY)
            } else {
                lineTo(startX, startY)
            }
            
            // Vẽ corner với quadratic bezier
            quadraticTo(curr.x, curr.y, endX, endY)
        }
        close()
    } else {
        // Không có corner radius, vẽ hexagon đơn giản
        moveTo(points[0].x, points[0].y)
        for (i in 1 until points.size) {
            lineTo(points[i].x, points[i].y)
        }
        close()
    }
    
    return this
}

private fun calculateCenteredPosition(
    left: Float,
    top: Float,
    width: Float,
    height: Float,
    containerWidth: Float,
    containerHeight: Float,
    centerHorizontal: Boolean?,
    centerVertical: Boolean?
): Pair<Float, Float> {
    return when {
        centerHorizontal == true && centerVertical == true -> {
            val centerX = containerWidth / 2f
            val centerY = containerHeight / 2f
            (centerX - width / 2f) to (centerY - height / 2f)
        }
        centerHorizontal == true -> {
            val centerX = containerWidth / 2f
            (centerX - width / 2f) to top
        }
        centerVertical == true -> {
            val centerY = containerHeight / 2f
            left to (centerY - height / 2f)
        }
        else -> left to top
    }
}

private fun ProcessedCellData.createShape(
    corner: Dp,
    cornerRadiusPx: Float,
    gapPx: Float
): Shape {
    val useRoundedCorner = cornerMethod == "3_13"
    val useHexagonCorner = cornerMethod == "3_6"
    
    // Nếu có CORNER_METHOD_3_6, tạo hexagon shape từ bound
    val baseShape = when {
        pathType == "CIRCLE" && pathRatioBound != null && pathRatioBound.size >= 4 -> {
            createCircleShapeWithBounds(
                pathRatioBound = pathRatioBound,
                pathInCenterHorizontal = pathInCenterHorizontal,
                pathInCenterVertical = pathInCenterVertical,
                shrinkMethod = shrinkMethod
            )
        }
        pathType == "HEART" && pathRatioBound != null && pathRatioBound.size >= 4 -> {
            createHeartShapeWithBounds(
                pathRatioBound = pathRatioBound,
                pathInCenterHorizontal = pathInCenterHorizontal,
                pathInCenterVertical = pathInCenterVertical,
                shrinkMethod = shrinkMethod
            )
        }
        pathType == "RECT" && pathRatioBound != null && pathRatioBound.size >= 4 -> {
            createRectShapeWithBounds(
                pathRatioBound = pathRatioBound,
                pathInCenterHorizontal = pathInCenterHorizontal,
                pathInCenterVertical = pathInCenterVertical,
                cornerRadiusPx = if (useRoundedCorner) cornerRadiusPx else 0f,
                shrinkMethod = shrinkMethod
            )
        }
        pathType == "CIRCLE" -> CircleShape
        normalizedPoints != null && normalizedPoints.isNotEmpty() ->
            FreePolygonShape(
                points = normalizedPoints,
                shrinkMap = shrinkMap,
                shrinkSpacingPx = gapPx
            )
        else -> {
            if (useRoundedCorner) {
                RoundedCornerShape(corner)
            } else {
                RoundedCornerShape(corner)
            }
        }
    }
    
    // Nếu có CORNER_METHOD_3_6, wrap shape với hexagon transformation
    return if (useHexagonCorner) {
        createHexagonWrapperShape(baseShape, cornerRadiusPx)
    } else {
        baseShape
    }
}

private fun createHexagonWrapperShape(
    baseShape: Shape,
    cornerRadiusPx: Float
): Shape {
    return object : Shape {
        override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
            // Tính bound của base shape
            val baseOutline = baseShape.createOutline(size, layoutDirection, density)
            val boundsLeft: Float
            val boundsTop: Float
            val boundsRight: Float
            val boundsBottom: Float
            
            when (baseOutline) {
                is Outline.Generic -> {
                    val pathBounds = baseOutline.path.getBounds()
                    boundsLeft = pathBounds.left
                    boundsTop = pathBounds.top
                    boundsRight = pathBounds.right
                    boundsBottom = pathBounds.bottom
                }
                is Outline.Rounded -> {
                    val roundRect = baseOutline.roundRect
                    boundsLeft = roundRect.left
                    boundsTop = roundRect.top
                    boundsRight = roundRect.right
                    boundsBottom = roundRect.bottom
                }
                is Outline.Rectangle -> {
                    val rect = baseOutline.rect
                    boundsLeft = rect.left
                    boundsTop = rect.top
                    boundsRight = rect.right
                    boundsBottom = rect.bottom
                }
            }
            
            // Tạo hexagon với size = min(bound.width(), bound.height())
            val boundsWidth = boundsRight - boundsLeft
            val boundsHeight = boundsBottom - boundsTop
            val hexSize = kotlin.math.min(boundsWidth, boundsHeight)
            val centerX = boundsLeft + boundsWidth / 2f
            val centerY = boundsTop + boundsHeight / 2f
            
            val path = Path().createHexagonPath(
                left = centerX - hexSize / 2f,
                top = centerY - hexSize / 2f,
                width = hexSize,
                height = hexSize,
                cornerRadiusPx = cornerRadiusPx
            )
            
            return Outline.Generic(path)
        }
    }
}

private fun createCircleShapeWithBounds(
    pathRatioBound: List<Float>,
    pathInCenterHorizontal: Boolean?,
    pathInCenterVertical: Boolean?,
    shrinkMethod: String? = null
): Shape {
    return object : Shape {
        override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
            val left = pathRatioBound[0] * size.width
            val top = pathRatioBound[1] * size.height
            val right = pathRatioBound[2] * size.width
            val bottom = pathRatioBound[3] * size.height
            val pathWidth = right - left
            val pathHeight = bottom - top

            val (finalLeft, finalTop) = if (shrinkMethod == "3_8" || shrinkMethod == "3_6") {
                // Với SHRINK_METHOD_3_8 hoặc 3_6, căn giữa hoàn toàn
                val centerX = size.width / 2f
                val centerY = size.height / 2f
                (centerX - pathWidth / 2f) to (centerY - pathHeight / 2f)
            } else {
                calculateCenteredPosition(
                    left = left,
                    top = top,
                    width = pathWidth,
                    height = pathHeight,
                    containerWidth = size.width,
                    containerHeight = size.height,
                    centerHorizontal = pathInCenterHorizontal,
                    centerVertical = pathInCenterVertical
                )
            }

            val path = Path().createCirclePath(
                left = finalLeft,
                top = finalTop,
                width = pathWidth,
                height = pathHeight
            )

            return Outline.Generic(path)
        }
    }
}

private fun createHeartShapeWithBounds(
    pathRatioBound: List<Float>,
    pathInCenterHorizontal: Boolean?,
    pathInCenterVertical: Boolean?,
    shrinkMethod: String? = null
): Shape {
    return object : Shape {
        override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
            val left = pathRatioBound[0] * size.width
            val top = pathRatioBound[1] * size.height
            val right = pathRatioBound[2] * size.width
            val bottom = pathRatioBound[3] * size.height
            val pathWidth = right - left
            val pathHeight = bottom - top

            val (finalLeft, finalTop) = if (shrinkMethod == "3_8" || shrinkMethod == "3_6") {
                // Với SHRINK_METHOD_3_8 hoặc 3_6, căn giữa hoàn toàn
                val centerX = size.width / 2f
                val centerY = size.height / 2f
                (centerX - pathWidth / 2f) to (centerY - pathHeight / 2f)
            } else {
                calculateCenteredPosition(
                    left = left,
                    top = top,
                    width = pathWidth,
                    height = pathHeight,
                    containerWidth = size.width,
                    containerHeight = size.height,
                    centerHorizontal = pathInCenterHorizontal,
                    centerVertical = pathInCenterVertical
                )
            }

            val heartPath = Path().createHeartPath(pathWidth, pathHeight)
            val translatedPath = Path()
            translatedPath.addPath(heartPath, Offset(finalLeft, finalTop))
            return Outline.Generic(translatedPath)
        }
    }
}

private fun createRectShapeWithBounds(
    pathRatioBound: List<Float>,
    pathInCenterHorizontal: Boolean?,
    pathInCenterVertical: Boolean?,
    cornerRadiusPx: Float,
    shrinkMethod: String? = null
): Shape {
    return object : Shape {
        override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
            val left = pathRatioBound[0] * size.width
            val top = pathRatioBound[1] * size.height
            val right = pathRatioBound[2] * size.width
            val bottom = pathRatioBound[3] * size.height
            val pathWidth = right - left
            val pathHeight = bottom - top

            val (finalLeft, finalTop) = if (shrinkMethod == "3_8" || shrinkMethod == "3_6") {
                // Với SHRINK_METHOD_3_8 hoặc 3_6, căn giữa hoàn toàn
                val centerX = size.width / 2f
                val centerY = size.height / 2f
                (centerX - pathWidth / 2f) to (centerY - pathHeight / 2f)
            } else {
                calculateCenteredPosition(
                    left = left,
                    top = top,
                    width = pathWidth,
                    height = pathHeight,
                    containerWidth = size.width,
                    containerHeight = size.height,
                    centerHorizontal = pathInCenterHorizontal,
                    centerVertical = pathInCenterVertical
                )
            }

            val path = if (cornerRadiusPx > 0f) {
                Path().createRoundedRectPath(
                    left = finalLeft,
                    top = finalTop,
                    width = pathWidth,
                    height = pathHeight,
                    cornerRadiusPx = cornerRadiusPx
                )
            } else {
                Path().createRectPath(
                    left = finalLeft,
                    top = finalTop,
                    width = pathWidth,
                    height = pathHeight
                )
            }

            return Outline.Generic(path)
        }
    }
}

class TemplatePreviewProvider : PreviewParameterProvider<CollageTemplate> {
    override val values = sequenceOf(
        CollageTemplate(
            "1-full", listOf(
                CellSpec(points = listOf(0f, 0f, 1f, 0f, 1f, 1f, 0f, 1f))
            )
        ),
        CollageTemplate(
            "2-0", listOf(
                CellSpec(points = listOf(0f, 0f, 0.5f, 0f, 0.5f, 1f, 0f, 1f)),
                CellSpec(points = listOf(0.5f, 0f, 1f, 0f, 1f, 1f, 0.5f, 1f))
            )
        ),
        CollageTemplate(
            "left-big-right-2", listOf(
                CellSpec(points = listOf(0f, 0f, 0.63f, 0f, 0.63f, 1f, 0f, 1f)),
                CellSpec(points = listOf(0.66f, 0f, 1f, 0f, 1f, 0.48f, 0.66f, 0.48f)),
                CellSpec(points = listOf(0.66f, 0.52f, 1f, 0.52f, 1f, 1f, 0.66f, 1f))
            )
        )
    )
}

@Preview(showBackground = true, widthDp = 300, heightDp = 400)
@Composable
private fun CollagePreviewPreview(
    @PreviewParameter(TemplatePreviewProvider::class) template: CollageTemplate
) {
    val mockUris = when (template.cells.size) {
        1 -> listOf("true".toUri())
        2 -> listOf(Uri.EMPTY, Uri.EMPTY)
        else -> listOf(Uri.EMPTY, Uri.EMPTY, Uri.EMPTY)
    }

    CollagePreview(
        images = mockUris,
        template = template,
        gap = 6.dp,
        corner = 12.dp,
        modifier = Modifier.size(300.dp, 400.dp)
    )
}
