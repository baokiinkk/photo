package com.amb.photo.ui.activities.editor.crop

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.IntSize
import com.basesource.base.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class CropViewModel() : BaseViewModel() {

    private val _uiState = MutableStateFlow(CropState())
    val uiState: StateFlow<CropState> = _uiState

    fun updateCropState(newState: CropState) {
        _uiState.value = newState
    }

    fun updateScaleAndRotation(newScale: Float, newAngle: Float) {
        _uiState.update { it.copy(zoomScale = newScale, rotationAngle = newAngle) }
    }


    fun rotateImage() {
        _uiState.update { it.copy(rotationAngle = (it.rotationAngle + 90f) % 360) }
    }

    /**
     * Lật ngang.
     */
    fun flipHorizontal(currentScaleXFlip: Float) {
        // ViewModel không nên giữ state của scaleXFlip/scaleYFlip, nó là state của View/UI
        // Tuy nhiên, nếu nó ảnh hưởng đến logic của ViewModel, cần điều chỉnh.
        // Trong trường hợp này, ta sẽ đưa logic lật vào View, hoặc coi scaleXFlip/scaleYFlip là một phần của CropState.
        // Tạm thời, ta sẽ giữ logic lật trong View (CropImageScreen)
    }

    fun onDragStart(offset: Offset) {
        val cropState = _uiState.value

        val rect = cropState.cropRect
        val cornerRadius = 80f
        var activeCorner: String? = null
        var isMoving = false

        if (cropState.aspect == CropAspect.FREE) {
            activeCorner = when {
                (offset - Offset(
                    rect.left,
                    rect.top
                )).getDistance() < cornerRadius -> "TL"

                (offset - Offset(
                    rect.right,
                    rect.top
                )).getDistance() < cornerRadius -> "TR"

                (offset - Offset(
                    rect.left,
                    rect.bottom
                )).getDistance() < cornerRadius -> "BL"

                (offset - Offset(
                    rect.right,
                    rect.bottom
                )).getDistance() < cornerRadius -> "BR"

                rect.contains(offset) -> {
                    isMoving = true; null
                }

                else -> null
            }
        } else if (rect.contains(offset)) {
            isMoving = true
        }

        _uiState.update {
            it.copy(
                activeCorner = activeCorner,
                isMoving = isMoving
            )
        }
    }


    fun onDragEnd() {
        _uiState.update {
            it.copy(activeCorner = null, isMoving = false)
        }
    }

    fun onDrag(
        dragAmount: Offset,
        canvasWidth: Float,
        canvasHeight: Float
    ) {
        val cropState = uiState.value
        val dx = dragAmount.x
        val dy = dragAmount.y
        val rect = cropState.cropRect
        val minSize = 100f
        var newRect = rect

        if (cropState.isMoving) {
            newRect = rect.translate(dragAmount)
            val shiftX = when {
                newRect.left < 0 -> -newRect.left
                newRect.right > canvasWidth -> canvasWidth - newRect.right
                else -> 0f
            }
            val shiftY = when {
                newRect.top < 0 -> -newRect.top
                newRect.bottom > canvasHeight -> canvasHeight - newRect.bottom
                else -> 0f
            }
            _uiState.update {
                it.copy(
                    cropRect = newRect.translate(Offset(shiftX, shiftY))
                )
            }
            return
        }

        if (cropState.aspect == CropAspect.FREE) {
            newRect = when (cropState.activeCorner) {
                "TL" -> Rect(
                    (rect.left + dx).coerceIn(
                        0f,
                        rect.right - minSize
                    ),
                    (rect.top + dy).coerceIn(
                        0f,
                        rect.bottom - minSize
                    ),
                    rect.right, rect.bottom
                )

                "TR" -> Rect(
                    rect.left,
                    (rect.top + dy).coerceIn(
                        0f,
                        rect.bottom - minSize
                    ),
                    (rect.right + dx).coerceIn(
                        rect.left + minSize,
                        canvasWidth
                    ),
                    rect.bottom
                )

                "BL" -> Rect(
                    (rect.left + dx).coerceIn(
                        0f,
                        rect.right - minSize
                    ),
                    rect.top,
                    rect.right,
                    (rect.bottom + dy).coerceIn(
                        rect.top + minSize,
                        canvasHeight
                    )
                )

                "BR" -> Rect(
                    rect.left, rect.top,
                    (rect.right + dx).coerceIn(
                        rect.left + minSize,
                        canvasWidth
                    ),
                    (rect.bottom + dy).coerceIn(
                        rect.top + minSize,
                        canvasHeight
                    )
                )

                else -> rect
            }
            _uiState.update {
                it.copy(cropRect = newRect)
            }
        }
    }

    fun onAspectFormatSelected(imageBounds:IntSize,aspect:CropAspect,padding: Float) {
        if (imageBounds == IntSize.Zero) return
        val cropState = uiState.value
        val canvasWidth = imageBounds.width.toFloat()
        val canvasHeight = imageBounds.height.toFloat()

        val newRect = when (aspect) {
            CropAspect.ORIGINAL -> {
                // 🟣 Full khung ảnh
                Rect(0f, 0f, canvasWidth, canvasHeight)
            }

            CropAspect.FREE -> {
                // 🟣 Giữ nguyên khung hiện tại
                cropState.cropRect
            }

            else -> {
                // 🟣 Tính theo tỉ lệ cố định
                val (rw, rh) = aspect.ratio ?: (1 to 1)
                val width = canvasWidth - 2 * padding
                val height = width * rh / rw
                val left = padding
                val top = (canvasHeight - height) / 2f
                Rect(left, top, left + width, top + height)
            }
        }
        _uiState.update {
            it.copy(
                aspect = aspect,
                cropRect = newRect,
                id = aspect.label
            )
        }
    }
}

