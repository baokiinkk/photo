package com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.preview

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.graphics.Shape
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.ProcessedCellData

@Composable
fun CollageContent(
    processedCells: List<ProcessedCellData>,
    gap: Dp,
    corner: Dp,
    borderWidthPx: Float,
    cornerRadiusPx: Float,
    density: Density,
    imageStates: MutableMap<Int, Pair<ImageTransformState, Boolean>>,
    onImageClick: ((Int, android.net.Uri) -> Unit)?,
    onImageTransformsChange: ((Map<Int, ImageTransformState>) -> Unit)?
) {
    val gapPx = with(density) { gap.toPx() }

    processedCells.forEachIndexed { index, cell ->
        val (transform, isSelected) = imageStates[index] ?: (ImageTransformState() to false)
        val shape = cell.createShape(corner, cornerRadiusPx, gapPx)

        CollageImageCell(
            cellData = cell,
            transformState = transform,
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

