package com.tanishranjan.cropkit.internal

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.core.graphics.scale
import com.tanishranjan.cropkit.CropShape
import com.tanishranjan.cropkit.GridLinesVisibility
import com.tanishranjan.cropkit.util.Extensions.coerceInOrderAgnostic
import com.tanishranjan.cropkit.util.Extensions.isInsideRect
import com.tanishranjan.cropkit.util.GestureUtils
import com.tanishranjan.cropkit.util.MathUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.abs

internal class CropStateManager(
    bitmap: Bitmap,
    val isMoveEnabled: Boolean = false,
    private val cropShape: CropShape,
    private val contentScale: ContentScale,
    private val gridLinesVisibility: GridLinesVisibility,
    private val handleRadius: Dp,
    private val touchPadding: Dp,
    val initialPadding: Dp,
    val zoomScale: Float? = null,
    val rotationZBitmap: Float? = null,
) {

    private val _state = MutableStateFlow(CropState(bitmap))
    val state = _state.asStateFlow()
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private var dragMode: DragMode = DragMode.None
    private val density get() = Resources.getSystem().displayMetrics.density
    private val handleRadiusPx: Float get() = handleRadius.value * density

    // Chuyển đổi Dp sang pixel một lần để tái sử dụng
    private val paddingPx = initialPadding.value * density // <-- THÊM DÒNG NÀY

    init {
        _state.update {
            it.copy(
                zoomScale = zoomScale,
                rotationZBitmap = rotationZBitmap
            )
        }
        reset(bitmap)
    }

    fun updateCanvasSize(canvasSize: Size) {
        setState(canvasSize, state.value.bitmap)
    }

    fun crop(): Bitmap {

        val state = state.value
        val bitmap = state.bitmap
        val imageRect = state.imageRect
        val cropRect = state.cropRect

        val scaleX = bitmap.width / imageRect.width
        val scaleY = bitmap.height / imageRect.height

        val cropX = ((cropRect.left - imageRect.left) * scaleX).toInt()
        val cropY = ((cropRect.top - imageRect.top) * scaleY).toInt()
        val cropWidth = (cropRect.width * scaleX).toInt()
        val cropHeight = (cropRect.height * scaleY).toInt()

        val x = cropX.coerceIn(0, bitmap.width)
        val y = cropY.coerceIn(0, bitmap.height)
        val width = cropWidth.coerceIn(0, bitmap.width - x)
        val height = cropHeight.coerceIn(0, bitmap.height - y)

        return Bitmap.createBitmap(
            bitmap,
            x, y,
            width, height
        )

    }

    fun onDragStart(offset: Offset) {

        val activeHandle = findActiveHandle(offset)

        dragMode = when {
            activeHandle != null -> DragMode.Handle(activeHandle)
            offset.isInsideRect(state.value.cropRect) -> DragMode.Move
            else -> DragMode.None
        }

        _state.update { cropState ->
            cropState.copy(
                isDragging = dragMode != DragMode.None,
                gridlinesActive = if (gridLinesVisibility == GridLinesVisibility.ON_TOUCH) {
                    dragMode != DragMode.None
                } else {
                    cropState.gridlinesActive
                }
            )
        }

    }

    fun onDragEnd() {
        _state.update { cropState ->
            cropState.copy(
                isDragging = false,
                gridlinesActive = if (gridLinesVisibility != GridLinesVisibility.ALWAYS) {
                    false
                } else cropState.gridlinesActive
            )
        }
        dragMode = DragMode.None
    }

    fun onDrag(dragAmount: Offset) {
        when (val dragMode = dragMode) {
            is DragMode.Handle -> dragHandles(dragMode.handle, dragAmount)

            DragMode.Move -> moveCropRect(dragAmount)

            DragMode.None -> {}
        }
    }

    fun rotateClockwise() = transformBitmap { matrix -> matrix.postRotate(90f) }

    fun rotateAntiClockwise() = transformBitmap { matrix -> matrix.postRotate(-90f) }

    fun flipHorizontally() = transformBitmap { matrix -> matrix.postScale(-1f, 1f) }

    fun flipVertically() = transformBitmap { matrix -> matrix.postScale(1f, -1f) }

    private fun transformBitmap(transformation: (Matrix) -> Unit) {
        val bitmap = state.value.bitmap
        val matrix = Matrix().apply(transformation)
        val newBitmap = Bitmap.createBitmap(
            bitmap, 0, 0,
            bitmap.width, bitmap.height,
            matrix, true
        )
        reset(newBitmap)
    }

    private fun moveCropRect(dragAmount: Offset) {

        val currentRect = state.value.cropRect
        val imageRect = state.value.imageRect

        val newLeft = (currentRect.left + dragAmount.x).coerceInOrderAgnostic(
            imageRect.left,
            imageRect.right - currentRect.width
        )
        val newTop = (currentRect.top + dragAmount.y).coerceInOrderAgnostic(
            imageRect.top,
            imageRect.bottom - currentRect.height
        )

        val newRect = Rect(
            left = newLeft,
            top = newTop,
            right = newLeft + currentRect.width,
            bottom = newTop + currentRect.height
        )

        _state.update {
            it.copy(
                cropRect = newRect,
                handles = GestureUtils.getNewHandleMeasures(newRect, handleRadiusPx)
            )
        }

    }

    private fun dragHandles(activeHandle: DragHandle, dragAmount: Offset) {
        if (!isMoveEnabled) return
        val adjustedDragAmount = if (cropShape is CropShape.FreeForm) {
            dragAmount
        } else {
            getDragAmountForShape(dragAmount, activeHandle)
        }

        GestureUtils.getNewRectMeasures(
            activeHandle = activeHandle,
            dragAmount = adjustedDragAmount,
            imageRect = state.value.imageRect,
            cropRect = state.value.cropRect,
            minCropSize = MIN_CROP_SIZE
        )?.let { newRect ->
            _state.update {
                it.copy(
                    cropRect = newRect,
                    handles = GestureUtils.getNewHandleMeasures(
                        newRect,
                        handleRadiusPx
                    )
                )
            }
        }
    }

    private fun getDragAmountForShape(dragAmount: Offset, handle: DragHandle): Offset {

        val aspectRatio = state.value.aspectRatio
        val dx = dragAmount.x
        val dy = dragAmount.y
        val xConstraint = abs(dragAmount.x)
        val xConstraintDeltaY = xConstraint / aspectRatio
        val yConstraint = abs(dragAmount.y)
        val yConstraintDeltaX = yConstraint * aspectRatio

        return when (handle) {
            DragHandle.TopLeft -> {
                val sign = if (dx < 0 && dy < 0) -1 else 1 // prioritize cropping in
                val xConstraintCropIn = minOf(xConstraint, xConstraintDeltaY)
                val yConstraintCropIn = minOf(yConstraint, yConstraintDeltaX)
                return if (xConstraintCropIn <= yConstraintCropIn) {
                    Offset(sign * xConstraint, sign * xConstraintDeltaY)
                } else {
                    Offset(sign * yConstraintDeltaX, sign * yConstraint)
                }
            }

            DragHandle.TopRight -> {
                val sign = if (dx > 0 && dy < 0) -1 else 1 // prioritize cropping in
                val xConstraintCropIn = minOf(xConstraint, xConstraintDeltaY)
                val yConstraintCropIn = minOf(yConstraint, yConstraintDeltaX)
                return if (xConstraintCropIn <= yConstraintCropIn) {
                    Offset(-sign * xConstraint, sign * xConstraintDeltaY)
                } else {
                    Offset(-sign * yConstraintDeltaX, sign * yConstraint)
                }

            }

            DragHandle.BottomLeft -> {
                val sign = if (dx < 0 && dy > 0) -1 else 1 // prioritize cropping in
                val xConstraintCropIn = minOf(xConstraint, xConstraintDeltaY)
                val yConstraintCropIn = minOf(yConstraint, yConstraintDeltaX)
                return if (xConstraintCropIn <= yConstraintCropIn) {
                    Offset(sign * xConstraint, -sign * xConstraintDeltaY)
                } else {
                    Offset(sign * yConstraintDeltaX, -sign * yConstraint)
                }
            }

            DragHandle.BottomRight -> {
                val sign = if (dx > 0 && dy > 0) -1 else 1 // prioritize cropping in
                val xConstraintCropIn = minOf(xConstraint, xConstraintDeltaY)
                val yConstraintCropIn = minOf(yConstraint, yConstraintDeltaX)
                return if (xConstraintCropIn <= yConstraintCropIn) {
                    Offset(-sign * xConstraint, -sign * xConstraintDeltaY)
                } else {
                    Offset(-sign * yConstraintDeltaX, -sign * yConstraint)
                }
            }

            else -> Offset.Zero
        }

    }

    private fun findActiveHandle(offset: Offset): DragHandle? {
        // TODO: Allow cropping with all handles in locked aspect ratios
        val handles = if (cropShape is CropShape.FreeForm) {
            state.value.handles.getAllNamedHandles()
        } else {
            state.value.handles.getCornerNamedHandles()
        }

        handles.forEach { (handle, handleType) ->
            val padding = touchPadding.value * density
            val paddedHandle = Rect(
                Offset(handle.left - padding, handle.top - padding),
                Size(handle.width + padding * 2, handle.height + padding * 2)
            )
            if (offset.isInsideRect(paddedHandle)) {
                return handleType
            }
        }

        return null

    }

    private fun reset(bitmap: Bitmap) {
        coroutineScope.launch {
            setState(
                state.value.canvasSize,
                bitmap
            )
        }
    }

    private fun setState(
        canvasSize: Size,
        bitmap: Bitmap
    ) {

        if (canvasSize == Size.Zero) {
            return
        }

        val imageWidth = bitmap.width.toFloat()
        val imageHeight = bitmap.height.toFloat()

        val scaledSize = MathUtils.calculateScaledSize(
            srcWidth = imageWidth,
            srcHeight = imageHeight,
            dstWidth = canvasSize.width,
            dstHeight = canvasSize.height,
            contentScale = contentScale
        )

        val newBitmap = bitmap.scale(scaledSize.width.toInt(), scaledSize.height.toInt())

        val offsetX = (canvasSize.width - scaledSize.width) / 2f
        val offsetY = (canvasSize.height - scaledSize.height) / 2f

        val aspectRatio = when (cropShape) {
            is CropShape.FreeForm -> null
            is CropShape.AspectRatio -> cropShape.ratio
            is CropShape.Original -> imageWidth / imageHeight
        }

        val cropSize: Size
        val cropOffset: Offset

        if (aspectRatio != null) {
//            val availableWidth = scaledSize.width
//            val availableHeight = scaledSize.height
            val availableWidth = scaledSize.width - (paddingPx * 2)
            val availableHeight = scaledSize.height // Giả sử chỉ có padding ngang

            var cropWidth = availableWidth
            var cropHeight = cropWidth / aspectRatio

            if (cropHeight > availableHeight) {
                cropHeight = availableHeight
                cropWidth = cropHeight * aspectRatio
            }

            cropSize = Size(cropWidth, cropHeight)
            cropOffset = Offset(
                offsetX + paddingPx + (availableWidth - cropWidth) / 2,
                offsetY + (availableHeight - cropHeight) / 2
//                offsetX + (availableWidth - cropWidth) / 2,
//                offsetY + (availableHeight - cropHeight) / 2
            )
        } else {
            // Free form
//            cropSize = Size(scaledSize.width, scaledSize.height)
//            cropOffset = Offset(offsetX, offsetY)
            cropSize = Size(scaledSize.width - (paddingPx * 2), scaledSize.height)
            cropOffset = Offset(offsetX + paddingPx, offsetY)
        }
        val cropRect = Rect(cropOffset, cropSize)

        _state.update {
            it.copy(
                canvasSize = canvasSize,
                bitmap = newBitmap,
                imageRect = Rect(
                    Offset(offsetX, offsetY),
                    Size(scaledSize.width, scaledSize.height)
                ),
                imageBitmap = newBitmap.asImageBitmap(),
                cropRect = cropRect,
                handles = GestureUtils.getNewHandleMeasures(cropRect, handleRadiusPx),
                gridlinesActive = gridLinesVisibility == GridLinesVisibility.ALWAYS,
                aspectRatio = when (cropShape) {
                    is CropShape.AspectRatio -> cropShape.ratio
                    CropShape.FreeForm -> 0f
                    CropShape.Original -> imageWidth / imageHeight
                },
            )
        }

    }

    fun setAspectRatio(cropShape: CropShape) {
        // Lấy các giá trị cần thiết từ state hiện tại
        val currentState = state.value
        val canvasSize = currentState.canvasSize
        val imageRect = currentState.imageRect // Sử dụng imageRect đã có
        val imageWidth = imageRect.width
        val imageHeight = imageRect.height
        val originalBitmapWidth = currentState.bitmap.width.toFloat()
        val originalBitmapHeight = currentState.bitmap.height.toFloat()

        if (canvasSize == Size.Zero) {
            return
        }

        // Tính toán aspectRatio mới
        val newAspectRatio = when (cropShape) {
            is CropShape.FreeForm -> 0f // hoặc null tùy vào logic của bạn
            is CropShape.AspectRatio -> cropShape.ratio
            is CropShape.Original -> originalBitmapWidth / originalBitmapHeight
        }

        val cropSize: Size
        val cropOffset: Offset

        // Tính toán cropRect mới dựa trên imageRect hiện tại và aspectRatio mới
        if (newAspectRatio != 0f) {
            val availableWidth = imageRect.width - (paddingPx * 2)
            val availableHeight = imageRect.height

            var cropWidth = availableWidth
            var cropHeight = cropWidth / newAspectRatio

            if (cropHeight > availableHeight) {
                cropHeight = availableHeight
                cropWidth = cropHeight * newAspectRatio
            }

            cropSize = Size(cropWidth, cropHeight)
            cropOffset = Offset(
                imageRect.left + paddingPx + (availableWidth - cropWidth) / 2,
                imageRect.top + (availableHeight - cropHeight) / 2
            )
        } else {
            // Free form
            cropSize = Size(imageRect.width - (paddingPx * 2), imageRect.height)
            cropOffset = Offset(imageRect.left + paddingPx, imageRect.top)
        }
        val newCropRect = Rect(cropOffset, cropSize)

        _state.update {
            it.copy(
                // Chỉ cập nhật các giá trị liên quan đến crop và aspect ratio
                cropRect = newCropRect,
                handles = GestureUtils.getNewHandleMeasures(newCropRect, handleRadiusPx),
                aspectRatio = newAspectRatio,
                // Không ghi đè lại bitmap hay imageBitmap
            )
        }
//
//        val bitmap = state.value.bitmap
//
//        val canvasSize = state.value.canvasSize
//        if (canvasSize == Size.Zero) {
//            return
//        }
//        val imageWidth = bitmap.width.toFloat()
//        val imageHeight = bitmap.height.toFloat()
//
//        val scaledSize = MathUtils.calculateScaledSize(
//            srcWidth = imageWidth,
//            srcHeight = imageHeight,
//            dstWidth = canvasSize.width,
//            dstHeight = canvasSize.height,
//            contentScale = contentScale
//        )
//        val offsetX = (canvasSize.width - scaledSize.width) / 2f
//        val offsetY = (canvasSize.height - scaledSize.height) / 2f
//
//        val aspectRatio = when (cropShape) {
//            is CropShape.FreeForm -> null
//            is CropShape.AspectRatio -> cropShape.ratio
//            is CropShape.Original -> imageWidth / imageHeight
//        }
//
//        val cropSize: Size
//        val cropOffset: Offset
//
//        if (aspectRatio != null) {
////            val availableWidth = scaledSize.width
////            val availableHeight = scaledSize.height
//            val availableWidth = scaledSize.width - (paddingPx * 2)
//            val availableHeight = scaledSize.height
//
//
//            var cropWidth = availableWidth
//            var cropHeight = cropWidth / aspectRatio
//
//            if (cropHeight > availableHeight) {
//                cropHeight = availableHeight
//                cropWidth = cropHeight * aspectRatio
//            }
//
//            cropSize = Size(cropWidth, cropHeight)
//            cropOffset = Offset(
//                offsetX + (availableWidth - cropWidth) / 2,
//                offsetY + (availableHeight - cropHeight) / 2
//            )
//        } else {
//            // Free form
////            cropSize = Size(scaledSize.width, scaledSize.height)
////            cropOffset = Offset(offsetX, offsetY)
//            cropSize = Size(scaledSize.width - (paddingPx * 2), scaledSize.height)
//            cropOffset = Offset(offsetX + paddingPx, offsetY)
//        }
//        val cropRect = Rect(cropOffset, cropSize)
//
//        _state.update {
//            it.copy(
//                bitmap = bitmap,
//                imageRect = Rect(
//                    Offset(offsetX, offsetY),
//                    Size(scaledSize.width, scaledSize.height)
//                ),
//                imageBitmap = bitmap.asImageBitmap(),
//                cropRect = cropRect,
//                handles = GestureUtils.getNewHandleMeasures(cropRect, handleRadiusPx),
//                gridlinesActive = gridLinesVisibility == GridLinesVisibility.ALWAYS,
//                aspectRatio = when (cropShape) {
//                    is CropShape.AspectRatio -> cropShape.ratio
//                    CropShape.FreeForm -> 0f
//                    CropShape.Original -> imageWidth / imageHeight
//                },
//            )
//        }
    }

    fun setZoomScale(
        scaleXBitmap: Float,
        scaleYBitmap: Float,
        rotationZBitmap: Float
    ) {
//        _state.update {
//            it.copy(
//                scaleXBitmap = scaleXBitmap,
//                scaleYBitmap = scaleYBitmap,
//                rotationZBitmap = rotationZBitmap
//            )
//        }
    }

    companion object {
        private const val MIN_CROP_SIZE = 250f
    }

}