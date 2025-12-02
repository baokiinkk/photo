package com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.preview

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import com.avnsoft.photoeditor.photocollage.data.model.collage.FreePolygonShape
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.ProcessedCellData
import kotlin.math.min

fun ProcessedCellData.createShape(
    corner: Dp,
    cornerRadiusPx: Float,
    gapPx: Float
): Shape {
    val useRoundedCorner = cornerMethod == "3_13"
    val useHexagonCorner = cornerMethod == "3_6"

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

        normalizedPoints != null && normalizedPoints.isNotEmpty() -> {
            FreePolygonShape(
                points = normalizedPoints,
                shrinkMap = shrinkMap,
                shrinkSpacingPx = gapPx
            )
        }

        else -> RoundedCornerShape(corner)
    }

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
        override fun createOutline(
            size: Size,
            layoutDirection: LayoutDirection,
            density: Density
        ): Outline {
            val baseOutline = baseShape.createOutline(size, layoutDirection, density)

            val (left, top, right, bottom) = when (baseOutline) {
                is Outline.Generic -> {
                    val b = baseOutline.path.getBounds()
                    listOf(b.left, b.top, b.right, b.bottom)
                }

                is Outline.Rounded -> {
                    val r = baseOutline.roundRect
                    listOf(r.left, r.top, r.right, r.bottom)
                }

                is Outline.Rectangle -> {
                    val r = baseOutline.rect
                    listOf(r.left, r.top, r.right, r.bottom)
                }
            }

            val w = right - left
            val h = bottom - top
            val hexSize = min(w, h)
            val cx = left + w / 2f
            val cy = top + h / 2f

            val path = Path().createHexagonPath(
                left = cx - hexSize / 2f,
                top = cy - hexSize / 2f,
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
        override fun createOutline(
            size: Size,
            layoutDirection: LayoutDirection,
            density: Density
        ): Outline {
            val left = pathRatioBound[0] * size.width
            val top = pathRatioBound[1] * size.height
            val right = pathRatioBound[2] * size.width
            val bottom = pathRatioBound[3] * size.height
            val w = right - left
            val h = bottom - top

            val (finalLeft, finalTop) = if (shrinkMethod == "3_8" || shrinkMethod == "3_6") {
                val cx = size.width / 2f
                val cy = size.height / 2f
                (cx - w / 2f) to (cy - h / 2f)
            } else {
                calculateCenteredPosition(
                    left = left,
                    top = top,
                    width = w,
                    height = h,
                    containerWidth = size.width,
                    containerHeight = size.height,
                    centerHorizontal = pathInCenterHorizontal,
                    centerVertical = pathInCenterVertical
                )
            }

            val path = Path().createCirclePath(
                left = finalLeft,
                top = finalTop,
                width = w,
                height = h
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
        override fun createOutline(
            size: Size,
            layoutDirection: LayoutDirection,
            density: Density
        ): Outline {
            val left = pathRatioBound[0] * size.width
            val top = pathRatioBound[1] * size.height
            val right = pathRatioBound[2] * size.width
            val bottom = pathRatioBound[3] * size.height
            val w = right - left
            val h = bottom - top

            val (finalLeft, finalTop) = if (shrinkMethod == "3_8" || shrinkMethod == "3_6") {
                val cx = size.width / 2f
                val cy = size.height / 2f
                (cx - w / 2f) to (cy - h / 2f)
            } else {
                calculateCenteredPosition(
                    left = left,
                    top = top,
                    width = w,
                    height = h,
                    containerWidth = size.width,
                    containerHeight = size.height,
                    centerHorizontal = pathInCenterHorizontal,
                    centerVertical = pathInCenterVertical
                )
            }

            val heartPath = Path().createHeartPath(w, h)
            val translated = Path().apply {
                addPath(heartPath, Offset(finalLeft, finalTop))
            }
            return Outline.Generic(translated)
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
        override fun createOutline(
            size: Size,
            layoutDirection: LayoutDirection,
            density: Density
        ): Outline {
            val left = pathRatioBound[0] * size.width
            val top = pathRatioBound[1] * size.height
            val right = pathRatioBound[2] * size.width
            val bottom = pathRatioBound[3] * size.height
            val w = right - left
            val h = bottom - top

            val (finalLeft, finalTop) = if (shrinkMethod == "3_8" || shrinkMethod == "3_6") {
                val cx = size.width / 2f
                val cy = size.height / 2f
                (cx - w / 2f) to (cy - h / 2f)
            } else {
                calculateCenteredPosition(
                    left = left,
                    top = top,
                    width = w,
                    height = h,
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
                    width = w,
                    height = h,
                    cornerRadiusPx = cornerRadiusPx
                )
            } else {
                Path().createRectPath(
                    left = finalLeft,
                    top = finalTop,
                    width = w,
                    height = h
                )
            }

            return Outline.Generic(path)
        }
    }
}

