package com.avnsoft.photoeditor.photocollage.ui.activities.collage.components

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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.ui.platform.LocalContext

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
    imageTransforms: Map<Int, ImageTransformState> = emptyMap(), // Transforms từ CollageState
    onImageClick: ((Uri) -> Unit)? = null,
    onImageTransformsChange: ((Map<Int, ImageTransformState>) -> Unit)? = null, // Callback khi transform thay đổi
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier) {
        val density = LocalDensity.current
        val context = LocalContext.current
        val w = constraints.maxWidth.toFloat()
        val h = constraints.maxHeight.toFloat()

        // Background layer
        BackgroundLayer(
            backgroundSelection = backgroundSelection,
            modifier = Modifier.fillMaxSize()
        )

        // Xử lý template trên IO thread
        var processedCells by remember { mutableStateOf<List<ProcessedCellData>>(emptyList()) }

        LaunchedEffect(template, images, w, h) {
            processedCells = withContext(Dispatchers.IO) {
                CollagePreviewDataProcessor.processTemplate(
                    template = template,
                    images = images,
                    canvasWidth = w,
                    canvasHeight = h
                )
            }
        }

        // Lưu state transform cho tất cả images - sử dụng mutableStateMapOf để tối ưu performance
        // Khởi tạo từ imageTransforms từ CollageState, nhưng giữ nguyên isSelected state
        val imageStates = remember { mutableStateMapOf<Int, Pair<ImageTransformState, Boolean>>() }
        
        // Tính toán initial transform để scale lớn nhất có thể khi vừa vào màn
        LaunchedEffect(processedCells, imageTransforms) {
            if (imageTransforms.isEmpty() && processedCells.isNotEmpty()) {
                // Delay một chút để đảm bảo processedCells đã render xong
                delay(100)
                
                // Tính toán trên IO thread để không block UI
                val initialTransforms = withContext(Dispatchers.IO) {
                    processedCells.mapIndexed { index, cellData ->
                        // Lấy width và height của bound (pixel)
                        val boundWidth = cellData.width
                        val boundHeight = cellData.height
                        
                        // Load image size từ Uri (chỉ đọc metadata, không load full bitmap)
                        val imageSize = getImageSizeFromUri(context, cellData.imageUri)
                        
                        if (imageSize != null) {
                            // Tính tỷ lệ giữa bound và ảnh gốc
                            val widthRatio = boundWidth / imageSize.width
                            val heightRatio = boundHeight / imageSize.height
                            
                            // ContentScale.Fit sẽ scale ảnh với scale = min(widthRatio, heightRatio)
                            // Để zoom hết mức có thể, cần scale thêm để đạt max(widthRatio, heightRatio)
                            val fitScale = kotlin.math.min(widthRatio, heightRatio)
                            val fillScale = kotlin.math.max(widthRatio, heightRatio)
                            
                            // Scale cần thêm để zoom hết mức = fillScale / fitScale (luôn >= 1)
                            val scale = if (fitScale > 0f) {
                                fillScale / fitScale
                            } else {
                                1f
                            }
                            
                            index to ImageTransformState(offset = Offset.Zero, scale = scale)
                        } else {
                            // Nếu không load được image size, dùng scale mặc định
                            index to ImageTransformState(offset = Offset.Zero, scale = 1f)
                        }
                    }.toMap()
                }
                
                // Update imageStates với initial transforms
                initialTransforms.forEach { (index, transform) ->
                    imageStates[index] = transform to false
                }
                
                // Gọi callback để update ViewModel (nếu có)
                if (initialTransforms.isNotEmpty()) {
                    onImageTransformsChange?.invoke(initialTransforms)
                }
            }
        }
        
        // Sync imageTransforms từ CollageState vào imageStates, nhưng giữ nguyên isSelected
        LaunchedEffect(imageTransforms) {
            if (imageTransforms.isNotEmpty()) {
                imageTransforms.forEach { (index, transform) ->
                    val currentState = imageStates[index]
                    // Chỉ update transform, giữ nguyên isSelected
                    imageStates[index] = transform to (currentState?.second ?: false)
                }
            }
        }

        processedCells.forEachIndexed { index, cellData ->
            val borderWidthPx = with(density) { borderWidth.toPx() }
            val cornerRadiusPx = with(density) { corner.toPx() }
            val gapPx = with(density) { gap.toPx() }

            // Lấy hoặc tạo state cho image này
            // Luôn lấy state mới nhất từ imageStates để đảm bảo có transform mới nhất
            val currentImageState = imageStates[index]
            val (transformState, isSelected) = currentImageState ?: (ImageTransformState() to false)

            // Tạo shape cho cell
            val shape = cellData.createShape(corner = corner)

            val imageBox = with(density) {
                Modifier
                    .offset(x = cellData.left.toDp(), y = cellData.top.toDp())
                    .size(cellData.width.toDp(), cellData.height.toDp())
                    .padding(gap / 2)
                    .background(BackgroundWhite)
            }

            Box(
                modifier = imageBox.then(
                    // Luôn clip để ảnh không vượt ra ngoài bound, dù selected hay không
                    Modifier.clipToBounds()
                )
            ) {
                AsyncImage(
                    model = cellData.imageUri,
                    contentDescription = null,
                    contentScale = if (cellData.imageUri.toString().contains("true")) {
                        ContentScale.Crop
                    } else ContentScale.Fit,
                    modifier = run {
                        // Áp dụng transform state cho cả selected và unselected
                        val currentTransform = imageStates[index]?.first ?: ImageTransformState()
                        Modifier
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
                                    // Khi selected: thêm gesture để zoom/move
                                    createTransformModifier(
                                        index = index,
                                        transformState = currentTransform,
                                        cellWidth = cellData.width,
                                        cellHeight = cellData.height,
                                        imageStates = imageStates,
                                        onImageTransformsChange = onImageTransformsChange
                                    )
                                } else {
                                    // Khi không selected: chỉ có tap để select
                                    createSelectModifier(
                                        index = index,
                                        imageStates = imageStates,
                                        onImageClick = onImageClick,
                                        imageUri = cellData.imageUri,
                                        onImageTransformsChange = onImageTransformsChange
                                    )
                                }
                            )
                            .then(
                                createClearAreaModifier(
                                    cellData = cellData,
                                    gapPx = gapPx
                                )
                            )
                    },
                    error = painterResource(R.drawable.ic_empty_image)
                )

                // Vẽ border khi selected
                if (isSelected || cellData.imageUri.toString().contains("true")) {
                    createBorderBox(
                        shape = shape,
                        isSelected = isSelected,
                        borderWidthPx = borderWidthPx,
                        cornerRadiusPx = cornerRadiusPx,
                        corner = corner
                    )
                }
            }
        }
    }
}

// ==================== Extension Functions ====================

/**
 * Tạo heart path với kích thước cho trước
 */
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

/**
 * Tạo circle path với vị trí và kích thước cho trước
 */
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

/**
 * Tạo rect path với vị trí và kích thước cho trước
 */
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

/**
 * Tính toán vị trí cuối cùng dựa trên center alignment
 */
private fun calculateFinalPosition(
    left: Float,
    top: Float,
    width: Float,
    height: Float,
    containerWidth: Float,
    containerHeight: Float,
    centerHorizontal: Boolean?,
    centerVertical: Boolean?
): Pair<Float, Float> {
    val finalLeft: Float
    val finalTop: Float

    when {
        centerHorizontal == true && centerVertical == true -> {
            val centerX = containerWidth / 2f
            val centerY = containerHeight / 2f
            finalLeft = centerX - width / 2f
            finalTop = centerY - height / 2f
        }

        centerHorizontal == true -> {
            val centerX = containerWidth / 2f
            finalLeft = centerX - width / 2f
            finalTop = top
        }

        centerVertical == true -> {
            val centerY = containerHeight / 2f
            finalLeft = left
            finalTop = centerY - height / 2f
        }

        else -> {
            finalLeft = left
            finalTop = top
        }
    }

    return finalLeft to finalTop
}

/**
 * Extension function để tạo Shape cho ProcessedCellData
 */
private fun ProcessedCellData.createShape(corner: Dp): Shape {
    val pathRatioBound = pathRatioBound
    val pathInCenterHorizontal = pathInCenterHorizontal
    val pathInCenterVertical = pathInCenterVertical

    return when {
        pathType == "CIRCLE" && pathRatioBound != null && pathRatioBound.size >= 4 -> {
            createCircleShapeWithBounds(
                pathRatioBound = pathRatioBound,
                pathInCenterHorizontal = pathInCenterHorizontal,
                pathInCenterVertical = pathInCenterVertical
            )
        }

        pathType == "HEART" && pathRatioBound != null && pathRatioBound.size >= 4 -> {
            createHeartShapeWithBounds(
                pathRatioBound = pathRatioBound,
                pathInCenterHorizontal = pathInCenterHorizontal,
                pathInCenterVertical = pathInCenterVertical
            )
        }

        pathType == "CIRCLE" -> CircleShape
        normalizedPoints != null && normalizedPoints.isNotEmpty() ->
            FreePolygonShape(normalizedPoints, shrinkMap)

        else -> RoundedCornerShape(corner)
    }
}

/**
 * Tạo Circle Shape với pathRatioBound
 */
private fun createCircleShapeWithBounds(
    pathRatioBound: List<Float>,
    pathInCenterHorizontal: Boolean?,
    pathInCenterVertical: Boolean?
): Shape {
    return object : Shape {
        override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
            val left = pathRatioBound[0] * size.width
            val top = pathRatioBound[1] * size.height
            val right = pathRatioBound[2] * size.width
            val bottom = pathRatioBound[3] * size.height

            val pathWidth = right - left
            val pathHeight = bottom - top

            val (finalLeft, finalTop) = calculateFinalPosition(
                left = left,
                top = top,
                width = pathWidth,
                height = pathHeight,
                containerWidth = size.width,
                containerHeight = size.height,
                centerHorizontal = pathInCenterHorizontal,
                centerVertical = pathInCenterVertical
            )

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

/**
 * Tạo Heart Shape với pathRatioBound
 */
private fun createHeartShapeWithBounds(
    pathRatioBound: List<Float>,
    pathInCenterHorizontal: Boolean?,
    pathInCenterVertical: Boolean?
): Shape {
    return object : Shape {
        override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
            val left = pathRatioBound[0] * size.width
            val top = pathRatioBound[1] * size.height
            val right = pathRatioBound[2] * size.width
            val bottom = pathRatioBound[3] * size.height

            val pathWidth = right - left
            val pathHeight = bottom - top

            val (finalLeft, finalTop) = calculateFinalPosition(
                left = left,
                top = top,
                width = pathWidth,
                height = pathHeight,
                containerWidth = size.width,
                containerHeight = size.height,
                centerHorizontal = pathInCenterHorizontal,
                centerVertical = pathInCenterVertical
            )

            val heartPath = Path().createHeartPath(pathWidth, pathHeight)
            val translatedPath = Path()
            translatedPath.addPath(heartPath, Offset(finalLeft, finalTop))
            return Outline.Generic(translatedPath)
        }
    }
}

/**
 * Tạo modifier cho transform gesture (zoom + pan)
 */
@Composable
private fun createTransformModifier(
    index: Int,
    transformState: ImageTransformState,
    cellWidth: Float,
    cellHeight: Float,
    imageStates: @JvmSuppressWildcards MutableMap<Int, Pair<ImageTransformState, Boolean>>,
    onImageTransformsChange: ((Map<Int, ImageTransformState>) -> Unit)? = null
): Modifier {
    return Modifier
        // Không cần graphicsLayer ở đây vì đã được áp dụng ở ngoài
        .pointerInput(index, cellWidth, cellHeight) {
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
                // Không lưu vào stack trong quá trình transform
            }

            detectTapGestures { _ ->
                // Giữ nguyên transform state hiện tại, chỉ unselect
                // Lấy transform state hiện tại từ imageStates (đã được update trong detectTransformGestures)
                // Sử dụng localScale và localOffset vì chúng đã được cập nhật trong detectTransformGestures
                val currentTransform = ImageTransformState(localOffset, localScale)
                imageStates[index] = currentTransform to false

                // Lưu vào stack khi unselect (sau khi hoàn thành zoom/move)
                val allTransforms = imageStates.mapNotNull { entry ->
                    val (idx, pair) = entry
                    val (transform, _) = pair
                    idx to transform
                }.toMap()
                onImageTransformsChange?.invoke(allTransforms)
            }
        }
}

/**
 * Tạo modifier cho select gesture
 */
@Composable
private fun createSelectModifier(
    index: Int,
    imageStates: @JvmSuppressWildcards MutableMap<Int, Pair<ImageTransformState, Boolean>>,
    onImageClick: ((Uri) -> Unit)?,
    imageUri: Uri,
    onImageTransformsChange: ((Map<Int, ImageTransformState>) -> Unit)? = null
): Modifier {
    return Modifier.pointerInput(index) {
        detectTapGestures {
            // Kiểm tra xem image này đã được selected chưa
            val currentState = imageStates[index]
            val isCurrentlySelected = currentState?.second == true
            
            if (isCurrentlySelected) {
                // Nếu đã selected, thì deselect (toggle)
                val (existingTransform, _) = currentState
                imageStates[index] = existingTransform to false
                
                // Lưu vào stack khi unselect
                val allTransforms = imageStates.mapNotNull { entry ->
                    val (idx, pair) = entry
                    val (transform, _) = pair
                    idx to transform
                }.toMap()
                onImageTransformsChange?.invoke(allTransforms)
            } else {
                // Nếu chưa selected, thì select image này và deselect các image khác
                // Lưu transform của các image đang được deselect vào stack
                var hasDeselected = false
                imageStates.keys.forEach { key ->
                    if (key != index) {
                        val otherState = imageStates[key]
                        if (otherState?.second == true) {
                            // Image này đang được selected, sẽ bị deselect
                            hasDeselected = true
                        }
                        val (existingTransform, _) = otherState ?: (ImageTransformState() to false)
                        imageStates[key] = existingTransform to false
                    }
                }
                // Select image này - giữ nguyên transform state nếu đã có, nếu không thì tạo mới
                val (existingTransform, _) = currentState ?: (ImageTransformState() to false)
                imageStates[index] = existingTransform to true
                onImageClick?.invoke(imageUri)
                
                // Lưu vào stack khi có deselect (unselect image khác)
                if (hasDeselected) {
                    val allTransforms = imageStates.mapNotNull { entry ->
                        val (idx, pair) = entry
                        val (transform, _) = pair
                        idx to transform
                    }.toMap()
                    onImageTransformsChange?.invoke(allTransforms)
                }
            }
        }
    }
}

/**
 * Tạo modifier cho clear area drawing
 */
@Composable
private fun createClearAreaModifier(
    cellData: ProcessedCellData,
    gapPx: Float
): Modifier {
    return when {
        cellData.clearPathType != null && cellData.clearPathRatioBound != null -> {
            Modifier.drawWithContent {
                drawContent()
                val actualWidth = cellData.width - gapPx
                val actualHeight = cellData.height - gapPx

                val ratioBound = cellData.clearPathRatioBound
                val left = ratioBound[0] * actualWidth
                val top = ratioBound[1] * actualHeight
                val right = ratioBound[2] * actualWidth
                val bottom = ratioBound[3] * actualHeight

                val pathWidth = right - left
                val pathHeight = bottom - top

                val (finalLeft, finalTop) = calculateFinalPosition(
                    left = left,
                    top = top,
                    width = pathWidth,
                    height = pathHeight,
                    containerWidth = actualWidth,
                    containerHeight = actualHeight,
                    centerHorizontal = cellData.clearPathInCenterHorizontal,
                    centerVertical = cellData.clearPathInCenterVertical
                )

                val clearPath = when (cellData.clearPathType) {
                    "CIRCLE" -> Path().createCirclePath(finalLeft, finalTop, pathWidth, pathHeight)
                    "HEART" -> {
                        val heartPath = Path().createHeartPath(pathWidth, pathHeight)
                        Path().apply {
                            addPath(heartPath, Offset(finalLeft, finalTop))
                        }
                    }

                    "RECT" -> Path().createRectPath(finalLeft, finalTop, pathWidth, pathHeight)
                    else -> null
                }

                clearPath?.let {
                    drawPath(
                        path = it,
                        color = Color.Transparent,
                        blendMode = BlendMode.SrcOver
                    )
                }
            }
        }

        cellData.clearAreaPoints != null -> {
            val clearPoints = cellData.clearAreaPoints
            if (clearPoints.size >= 6 && clearPoints.size % 2 == 0) {
                Modifier.drawWithContent {
                    drawContent()
                    val actualWidth = cellData.width - gapPx
                    val actualHeight = cellData.height - gapPx
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
                        blendMode = BlendMode.SrcOver
                    )
                }
            } else {
                Modifier
            }
        }

        else -> Modifier
    }
}

/**
 * Tạo Box với border drawing
 */
@Composable
private fun createBorderBox(
    shape: Shape,
    isSelected: Boolean,
    borderWidthPx: Float,
    cornerRadiusPx: Float,
    corner: Dp
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(corner))
            .drawBehind {
                val outline = shape.createOutline(
                    size = size,
                    layoutDirection = LayoutDirection.Ltr,
                    density = this@drawBehind
                )
                if (outline is Outline.Generic) {
                    val borderColor = com.avnsoft.photoeditor.photocollage.ui.theme.Primary500
                    val borderWidth = if (isSelected) borderWidthPx * 1.5f else borderWidthPx
                    drawPath(
                        path = outline.path,
                        color = borderColor,
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

/**
 * Load image size từ Uri (chỉ đọc metadata, không load toàn bộ bitmap)
 */
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

// ==================== Preview ====================

// Preview Provider
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
