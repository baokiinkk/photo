package com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.preview

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import coil.compose.AsyncImage
import com.avnsoft.photoeditor.photocollage.R
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.ProcessedCellData
import com.avnsoft.photoeditor.photocollage.ui.theme.Primary500

@Composable
fun CollageImageCell(
    cellData: ProcessedCellData,
    transformState: ImageTransformState,
    isSelected: Boolean,
    shape: Shape,
    gap: Dp,
    borderWidthPx: Float,
    cornerRadiusPx: Float,
    corner: Dp,
    density: Density,
    imageStates: MutableMap<Int, Pair<ImageTransformState, Boolean>>,
    index: Int,
    onImageClick: ((Int, android.net.Uri) -> Unit)?,
    onImageTransformsChange: ((Map<Int, ImageTransformState>) -> Unit)?
) {
    val shouldShowBorder = isSelected || cellData.imageUri.toString().contains("true")

    val boxModifier = with(density) {
        Modifier
            .offset(x = cellData.left.toDp(), y = cellData.top.toDp())
            .size(cellData.width.toDp(), cellData.height.toDp())
            .clip(shape)
            .then(clearAreaModifier(cellData, cornerRadiusPx))
            .padding(gap / 2)
            .clip(RoundedCornerShape(corner))
    }

    Box(
        modifier = boxModifier
            .clipToBounds()
            .then(
                if (isSelected) {
                    transformGestureModifier(
                        index = index,
                        transformState = transformState,
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
            )
            .drawWithContent {
                drawContent()
                if (shouldShowBorder) {
                    val outline = shape.createOutline(
                        size = size,
                        layoutDirection = LayoutDirection.Ltr,
                        density = this
                    )
                    if (outline is androidx.compose.ui.graphics.Outline.Generic) {
                        val border = if (isSelected) 15f else borderWidthPx
                        drawPath(
                            path = outline.path,
                            color = Primary500,
                            style = Stroke(
                                width = border,
                                cap = StrokeCap.Round,
                                pathEffect = PathEffect.cornerPathEffect(
                                    cornerRadiusPx * 1.5f
                                )
                            )
                        )
                    }
                }
            }
    ) {
        val currentTransform = imageStates[index]?.first ?: ImageTransformState()

        val imageModifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                translationX = currentTransform.offset.x
                translationY = currentTransform.offset.y
                scaleX = currentTransform.scale
                scaleY = currentTransform.scale
                transformOrigin = TransformOrigin.Center
                clip = true
            }

        if (cellData.imageBitmap != null) {
            Image(
                bitmap = cellData.imageBitmap.asImageBitmap(),
                contentScale = ContentScale.FillBounds,
                contentDescription = null,
                modifier = imageModifier
            )
        } else {
            AsyncImage(
                model = cellData.imageUri,
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                modifier = imageModifier,
                error = painterResource(R.drawable.ic_empty_image)
            )
        }
    }
}

