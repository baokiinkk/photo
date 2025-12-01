package com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.preview

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.ProcessedCellData
import kotlin.math.min

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
fun selectGestureModifier(
    index: Int,
    imageStates: MutableMap<Int, Pair<ImageTransformState, Boolean>>,
    onImageClick: ((Int, android.net.Uri) -> Unit)?,
    imageUri: android.net.Uri,
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
                onImageClick?.invoke(index, imageUri)
            }
        }
    }
}

@Composable
fun clearAreaModifier(
    cellData: ProcessedCellData,
    cornerRadiusPx: Float
): Modifier {
    return when {
        cellData.clearPathType != null && cellData.clearPathRatioBound != null -> {
            Modifier
                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                .drawWithContent {
                    drawContent()

                    val w = size.width
                    val h = size.height
                    val ratio = cellData.clearPathRatioBound

                    val left = ratio[0] * w
                    val top = ratio[1] * h
                    val right = ratio[2] * w
                    val bottom = ratio[3] * h
                    val pathWidth = right - left
                    val pathHeight = bottom - top

                    val (finalLeft, finalTop) = when (cellData.shrinkMethod) {
                        "3_8" -> {
                            val cx = w / 2f
                            val cy = h / 2f
                            (cx - pathWidth / 2f) to (cy - pathHeight / 2f)
                        }

                        "3_6" -> {
                            val x = if (ratio[0] > 0) {
                                w - pathWidth / 2f
                            } else {
                                -pathWidth / 2f
                            }
                            val y = h / 2f - pathHeight / 2f
                            x to y
                        }

                        else -> calculateCenteredPosition(
                            left = left,
                            top = top,
                            width = pathWidth,
                            height = pathHeight,
                            containerWidth = w,
                            containerHeight = h,
                            centerHorizontal = cellData.clearPathInCenterHorizontal,
                            centerVertical = cellData.clearPathInCenterVertical
                        )
                    }

                    val useRoundedCorner = cellData.cornerMethod == "3_13"
                    val useHexagonCorner = cellData.cornerMethod == "3_6"

                    val clearPath = if (useHexagonCorner) {
                        val hexSize = min(pathWidth, pathHeight)
                        val cx = finalLeft + pathWidth / 2f
                        val cy = finalTop + pathHeight / 2f
                        Path().createHexagonPath(
                            left = cx - hexSize / 2f,
                            top = cy - hexSize / 2f,
                            width = hexSize,
                            height = hexSize,
                            cornerRadiusPx = cornerRadiusPx
                        )
                    } else {
                        when (cellData.clearPathType) {
                            "CIRCLE" -> Path().createCirclePath(
                                finalLeft,
                                finalTop,
                                pathWidth,
                                pathHeight
                            )

                            "HEART" -> {
                                val heart = Path().createHeartPath(pathWidth, pathHeight)
                                Path().apply { addPath(heart, Offset(finalLeft, finalTop)) }
                            }

                            "RECT" -> {
                                if (useRoundedCorner && cornerRadiusPx > 0f) {
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
            val points = cellData.clearAreaPoints
            if (points.size >= 6 && points.size % 2 == 0) {
                Modifier
                    .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                    .drawWithContent {
                        drawContent()
                        val w = size.width
                        val h = size.height
                        val path = Path().apply {
                            val x0 = points[0] * w
                            val y0 = points[1] * h
                            moveTo(x0, y0)
                            for (i in 2 until points.size step 2) {
                                val x = points[i] * w
                                val y = points[i + 1] * h
                                lineTo(x, y)
                            }
                            close()
                        }
                        drawPath(
                            path = path,
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

fun extractTransforms(
    imageStates: MutableMap<Int, Pair<ImageTransformState, Boolean>>
): Map<Int, ImageTransformState> {
    return imageStates.map { (idx, pair) ->
        idx to pair.first
    }.toMap()
}

fun calculateCenteredPosition(
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
            val cx = containerWidth / 2f
            val cy = containerHeight / 2f
            (cx - width / 2f) to (cy - height / 2f)
        }

        centerHorizontal == true -> {
            val cx = containerWidth / 2f
            (cx - width / 2f) to top
        }

        centerVertical == true -> {
            val cy = containerHeight / 2f
            left to (cy - height / 2f)
        }

        else -> left to top
    }
}

