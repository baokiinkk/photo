package com.tanishranjan.cropkit

import android.graphics.Bitmap
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tanishranjan.cropkit.internal.CropStateChangeActions

/**
 * Returns a remembered state of [CropController].
 *
 * @param bitmap The bitmap to be cropped.
 * @param cropOptions The [CropOptions] to be used for cropping.
 * @param cropColors The [CropColors] to be used for styling.
 */
@Composable
fun rememberCropController(
    bitmap: Bitmap,
    cropOptions: CropOptions = CropDefaults.cropOptions(),
    cropColors: CropColors = CropDefaults.cropColors()
): CropController = remember(bitmap, cropOptions, cropColors) {
    CropController(bitmap, cropOptions, cropColors)
}

/**
 * Composable that displays an image with a crop rectangle to allow for cropping.
 *
 * @param modifier Modifier to be applied to the layout.
 * @param cropController The [CropController] for configuring and interacting with the ImageCropper.
 */
@Composable
fun ImageCropper(
    modifier: Modifier = Modifier,
    cropController: CropController
) {

    val state = cropController.state.collectAsStateWithLifecycle().value
    val cropOptions = cropController.cropOptions
    val cropColors = cropController.cropColors

    val overlay = animateColorAsState(
        targetValue = if (state.isDragging) {
            cropColors.overlayActive
        } else {
            cropColors.overlay
        },
        label = "overlay"
    )

    key(cropController) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            state.imageBitmap?.let { bmp ->
                Box(
                    Modifier
                        .aspectRatio(state.imageRect.width / state.imageRect.height)
                        .background(Color.Red)
                        .clipToBounds()
                    ,
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        bitmap = bmp,
                        contentDescription = null,
                        contentScale = ContentScale.None,
                        alignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                state.zoomScale?.let {
                                    if (scaleX != state.zoomScale) {
                                        scaleX = state.zoomScale * 1f
                                    }
                                    if (scaleY != state.zoomScale) {
                                        scaleY = state.zoomScale * 1f
                                    }
                                }
                                state.rotationZBitmap?.let {
                                    if (rotationZ != state.rotationZBitmap) {
                                        rotationZ = state.rotationZBitmap
                                    }
                                }
                                transformOrigin = TransformOrigin.Center
                                clip = true
                                shape = RectangleShape
                            }
                    )
                }
            }

            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                cropController.onStateChange(CropStateChangeActions.DragStart(offset))
                            },
                            onDragEnd = {
                                cropController.onStateChange(CropStateChangeActions.DragEnd)
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                cropController.onStateChange(
                                    CropStateChangeActions.DragBy(
                                        dragAmount
                                    )
                                )
                            }
                        )
                    }
                    .onSizeChanged { size ->
                        cropController.onStateChange(CropStateChangeActions.CanvasSizeChanged(size.toSize()))
                    }

            ) {

                state.imageBitmap?.let {

//                    clipPath(
//                        path = Path().apply {
//                            when (cropOptions.gridLinesType) {
//                                GridLinesType.CIRCLE, GridLinesType.GRID_AND_CIRCLE -> addOval(state.cropRect)
//                                else -> addRect(state.cropRect)
//                            }
//                        },
//                        clipOp = ClipOp.Difference // Chỉ vẽ ở vùng BÊN NGOÀI path
//                    ) {
//                        drawRect(
//                            color = overlay.value,
//                            size = this.size // Vẽ trên toàn bộ Canvas để che phủ đúng
//                        )
//                    }
//
//                    clipPath(path = Path().apply { addRect(state.cropRect) }) {
//                        // Bên trong khuôn cắt, ta áp dụng các phép biến đổi
//                        withTransform({
//                            // Di chuyển hệ tọa độ đến góc trên bên trái của ảnh
//                            translate(left = state.imageRect.left, top = state.imageRect.top)
//
//                            // Áp dụng scale, với tâm là tâm của ảnh
//                            state.zoomScale?.let {
//                                scale(
//                                    scaleX = it,
//                                    scaleY = it,
//                                    pivot = state.imageRect.center - state.imageRect.topLeft
//                                )
//                            }
//
//                            // Áp dụng xoay, với tâm là tâm của ảnh
//                            state.rotationZBitmap?.let {
//                                rotate(
//                                    degrees = it,
//                                    pivot = state.imageRect.center - state.imageRect.topLeft
//                                )
//                            }
//                        }) {
//                            // Sau khi đã biến đổi không gian vẽ, ta vẽ ảnh tại gốc tọa độ mới (0,0)
//                            drawImage(
//                                image = it,
//                                topLeft = Offset.Zero
//                            )
//                        }
//                    }


//                    clipPath(
//                        path = Path().apply {
//                            addRect(state.cropRect)
//                        }
//                    ) {
//                        // SỬA LỖI: Sử dụng 'withTransform' để áp dụng các phép biến đổi
//                        withTransform({
//                            // 1. Di chuyển toàn bộ canvas đến vị trí của ảnh
//                            translate(left = state.imageRect.left, top = state.imageRect.top)
//
//                            // 2. Áp dụng scale quanh tâm của ảnh
//                            state.zoomScale?.let {
//                                scale(
//                                    scaleX = it,
//                                    scaleY = it,
//                                    // Pivot là tâm của ảnh, nhưng trong hệ tọa độ đã di chuyển
//                                    pivot = state.imageRect.center - state.imageRect.topLeft
//                                )
//                            }
//
//                            // 3. Áp dụng rotation quanh tâm của ảnh
//                            state.rotationZBitmap?.let {
//                                rotate(
//                                    degrees = it,
//                                    // Pivot cũng là tâm của ảnh
//                                    pivot = state.imageRect.center - state.imageRect.topLeft
//                                )
//                            }
//                        }) {
//                            // 4. Vẽ hình ảnh tại gốc tọa độ MỚI (0,0),
//                            // vì canvas đã được di chuyển đến đúng vị trí
//                            drawImage(
//                                image = it,
//                                topLeft = state.imageRect.topLeft
//                            )
//                        }
//                    }
                    // Dark overlay outside crop area
                    clipPath(
                        path = Path().apply {
                            when (cropOptions.gridLinesType) {
                                GridLinesType.CIRCLE, GridLinesType.GRID_AND_CIRCLE -> addOval(state.cropRect)
                                else -> addRect(state.cropRect)
                            }
                        },
                        clipOp = ClipOp.Difference
                    ) {
                        drawRect(
                            color = overlay.value,
                            topLeft = state.imageRect.topLeft,
                            size = state.imageRect.size
                        )
                    }

                }

                val cropRect = state.cropRect

                // Crop Rectangle
                drawRect(
                    color = cropColors.cropRectangle,
                    topLeft = cropRect.topLeft,
                    size = cropRect.size,
                    style = Stroke(width = 2.dp.toPx())
                )

                // Gridlines
                if (cropOptions.gridLinesType in listOf(
                        GridLinesType.GRID,
                        GridLinesType.GRID_AND_CIRCLE
                    )
                ) {
                    val thirdWidth = cropRect.width / 3
                    val thirdHeight = cropRect.height / 3

                    // Vertical gridlines
                    for (i in 1..2) {
                        drawLine(
                            color = cropColors.gridlines,
                            start = Offset(cropRect.left + thirdWidth * i, cropRect.top),
                            end = Offset(cropRect.left + thirdWidth * i, cropRect.bottom),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    // Horizontal gridlines
                    for (i in 1..2) {
                        drawLine(
                            color = cropColors.gridlines,
                            start = Offset(cropRect.left, cropRect.top + thirdHeight * i),
                            end = Offset(cropRect.right, cropRect.top + thirdHeight * i),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                }

                if (cropOptions.gridLinesType in listOf(
                        GridLinesType.CIRCLE,
                        GridLinesType.GRID_AND_CIRCLE
                    )
                ) {
                    drawOval(
                        color = cropColors.gridlines,
                        topLeft = cropRect.topLeft,
                        size = cropRect.size,
                        style = Stroke(width = 1.dp.toPx())
                    )
                }

                if (cropOptions.gridLinesType == GridLinesType.CROSSHAIR) {
                    // Vertical crosshair
                    drawLine(
                        color = cropColors.gridlines,
                        start = Offset(cropRect.left + cropRect.width / 2, cropRect.top),
                        end = Offset(cropRect.left + cropRect.width / 2, cropRect.bottom),
                        strokeWidth = 1.dp.toPx()
                    )

                    // Horizontal crosshair
                    drawLine(
                        color = cropColors.gridlines,
                        start = Offset(cropRect.left, cropRect.top + cropRect.height / 2),
                        end = Offset(cropRect.right, cropRect.top + cropRect.height / 2),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // Draw edge handles only for free form cropping only
//                val handles = if (cropOptions.cropShape is CropShape.FreeForm) {
//                    state.handles.getAllHandles()
//                } else {
//                    state.handles.getCornerHandles()
//                }

                val handles = state.handles.getCornerHandles()
                val edgeHandles = state.handles.getAllHandles()
                handles.forEach { handle ->
                    drawOval(
                        color = cropColors.handle,
                        topLeft = handle.topLeft,
                        size = handle.size,
                        style = Fill
                    )
                }

                val barLength = 24.dp.toPx()
                val barWidth = 6.dp.toPx()

                drawLine(
                    Color.White,
                    Offset(
                        cropRect.center.x - barLength / 2,
                        cropRect.top
                    ),
                    Offset(
                        cropRect.center.x + barLength / 2,
                        cropRect.top
                    ),
                    strokeWidth = barWidth,
                    cap = StrokeCap.Round
                )

                drawLine(
                    Color.White,
                    Offset(
                        cropRect.center.x - barLength / 2,
                        cropRect.bottom
                    ),
                    Offset(
                        cropRect.center.x + barLength / 2,
                        cropRect.bottom
                    ),
                    strokeWidth = barWidth,
                    cap = StrokeCap.Round
                )
                drawLine(
                    Color.White,
                    Offset(
                        cropRect.left,
                        cropRect.center.y - barLength / 2
                    ),
                    Offset(
                        cropRect.left,
                        cropRect.center.y + barLength / 2
                    ),
                    strokeWidth = barWidth,
                    cap = StrokeCap.Round
                )
                drawLine(
                    Color.White,
                    Offset(
                        cropRect.right,
                        cropRect.center.y - barLength / 2
                    ),
                    Offset(
                        cropRect.right,
                        cropRect.center.y + barLength / 2
                    ),
                    strokeWidth = barWidth,
                    cap = StrokeCap.Round
                )
            }
        }
    }

}