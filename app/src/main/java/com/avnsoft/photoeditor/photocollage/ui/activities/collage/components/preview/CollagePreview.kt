package com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.preview

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.avnsoft.photoeditor.photocollage.data.model.collage.CollageTemplate
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.tools.BackgroundLayer
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.tools.BackgroundSelection
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.CollagePreviewDataProcessor
import kotlinx.coroutines.Dispatchers
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
    topMargin: Float = 0f,
    imageBitmaps: Map<Int, Bitmap> = emptyMap(),
    onImageClick: ((Int, Uri) -> Unit)? = null,
    onImageTransformsChange: ((Map<Int, ImageTransformState>) -> Unit)? = null,
    unselectAllTrigger: Int = 0,
    onOutsideClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier) {
        val density = LocalDensity.current
        val context = LocalContext.current

        val canvasWidth = constraints.maxWidth.toFloat()
        val canvasHeight = constraints.maxHeight.toFloat()

        // topMargin: 0–1 -> padding: 0–20% canvas
        val marginFactor = 0.2f * topMargin
        val topMarginPx = marginFactor * canvasHeight
        val bottomMarginPx = marginFactor * canvasHeight
        val leftMarginPx = marginFactor * canvasWidth
        val rightMarginPx = marginFactor * canvasWidth

        val effectiveCanvasWidth = canvasWidth - leftMarginPx - rightMarginPx
        val effectiveCanvasHeight = canvasHeight - topMarginPx - bottomMarginPx

        BackgroundLayer(
            backgroundSelection = backgroundSelection,
            modifier = Modifier.fillMaxSize()
        )

        var processedCells by remember { mutableStateOf<List<com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.ProcessedCellData>>(emptyList()) }

        // states zoom + selected cho từng cell: index -> (transform, isSelected)
        val imageStates =
            remember(template.id) { mutableStateMapOf<Int, Pair<ImageTransformState, Boolean>>() }

        var isInitializing by remember(template.id, images) { mutableStateOf(true) }

        LaunchedEffect(
            template.id,
            images,
            imageBitmaps,
            effectiveCanvasWidth,
            effectiveCanvasHeight
        ) {
            if (template.id.isEmpty() ||
                images.isEmpty() ||
                effectiveCanvasWidth <= 0f ||
                effectiveCanvasHeight <= 0f
            ) {
                processedCells = emptyList()
                imageStates.clear()
                isInitializing = false
                return@LaunchedEffect
            }

            isInitializing = true

            // Tính processedCells
            val cells = withContext(Dispatchers.Default) {
                CollagePreviewDataProcessor.processTemplate(
                    template = template,
                    images = images,
                    imageBitmaps = imageBitmaps,
                    canvasWidth = effectiveCanvasWidth,
                    canvasHeight = effectiveCanvasHeight
                )
            }
            processedCells = cells

            val finalTransforms = imageTransforms.ifEmpty {
                val calculated = ImageTransformCalculator.calculateInitialTransforms(context, cells)
                if (calculated.isNotEmpty()) {
                    onImageTransformsChange?.invoke(calculated)
                }
                calculated
            }

            imageStates.clear()
            finalTransforms.forEach { (index, transform) ->
                imageStates[index] = transform to false
            }

            isInitializing = false
        }

        LaunchedEffect(topMargin) {
            if (template.id.isNotEmpty() &&
                images.isNotEmpty() &&
                effectiveCanvasWidth > 0f &&
                effectiveCanvasHeight > 0f
            ) {
                processedCells = withContext(Dispatchers.Default) {
                    CollagePreviewDataProcessor.processTemplate(
                        template = template,
                        images = images,
                        imageBitmaps = imageBitmaps,
                        canvasWidth = effectiveCanvasWidth,
                        canvasHeight = effectiveCanvasHeight
                    )
                }
            }
        }

        LaunchedEffect(unselectAllTrigger) {
            if (unselectAllTrigger > 0) {
                imageStates.keys.forEach { key ->
                    val (transform, _) = imageStates[key] ?: (ImageTransformState() to false)
                    imageStates[key] = transform to false
                }
            }
        }

        var lastSyncedTransforms by remember { mutableStateOf<Map<Int, ImageTransformState>?>(null) }
        LaunchedEffect(imageTransforms) {
            if (imageTransforms.isNotEmpty() && !isInitializing) {
                if (lastSyncedTransforms != imageTransforms) {
                    imageTransforms.forEach { (index, transform) ->
                        val isSelected = imageStates[index]?.second ?: false
                        imageStates[index] = transform to isSelected
                    }
                    lastSyncedTransforms = imageTransforms
                }
            }
        }

        Box(
            modifier = Modifier
                .offset(
                    x = with(density) { leftMarginPx.toDp() },
                    y = with(density) { topMarginPx.toDp() }
                )
                .size(
                    width = with(density) { effectiveCanvasWidth.toDp() },
                    height = with(density) { effectiveCanvasHeight.toDp() }
                )
                .pointerInput(processedCells.size) {
                    detectTapGestures { tapOffset ->
                        val x = tapOffset.x
                        val y = tapOffset.y
                        val insideCell = processedCells.any { cell ->
                            x >= cell.left &&
                                    x <= cell.left + cell.width &&
                                    y >= cell.top &&
                                    y <= cell.top + cell.height
                        }
                        if (!insideCell) onOutsideClick?.invoke()
                    }
                }
        ) {
            val isReady =
                !isInitializing && processedCells.isNotEmpty() && imageStates.isNotEmpty()

            if (!isReady) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(corner))
                        .graphicsLayer { alpha = 0.6f }
                )
            } else {
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
    }
}
