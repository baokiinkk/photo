package com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.preview

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Path
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

fun Path.createHeartPath(width: Float, height: Float): Path {
    val size = min(width, height)
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

fun Path.createCirclePath(
    left: Float,
    top: Float,
    width: Float,
    height: Float
): Path {
    val size = min(width, height)
    val cx = left + width / 2f
    val cy = top + height / 2f
    addOval(
        Rect(
            cx - size / 2f,
            cy - size / 2f,
            cx + size / 2f,
            cy + size / 2f
        )
    )
    return this
}

fun Path.createRectPath(
    left: Float,
    top: Float,
    width: Float,
    height: Float
): Path {
    addRect(Rect(left, top, left + width, top + height))
    return this
}

fun Path.createRoundedRectPath(
    left: Float,
    top: Float,
    width: Float,
    height: Float,
    cornerRadiusPx: Float
): Path {
    val radius = cornerRadiusPx.coerceAtMost(min(width, height) / 2f)
    addRoundRect(
        RoundRect(
            left,
            top,
            left + width,
            top + height,
            CornerRadius(radius, radius)
        )
    )
    return this
}

fun Path.createHexagonPath(
    left: Float,
    top: Float,
    width: Float,
    height: Float,
    cornerRadiusPx: Float
): Path {
    val size = min(width, height)
    val centerX = left + width / 2f
    val centerY = top + height / 2f
    val radius = size / 2f
    val vertexCount = 6
    val section = (2.0 * PI / vertexCount).toFloat()

    val points = mutableListOf<Offset>()
    for (i in 0 until vertexCount) {
        val angle = section * i
        val x = centerX + radius * cos(angle).toFloat()
        val y = centerY + radius * sin(angle).toFloat()
        points.add(Offset(x, y))
    }

    if (cornerRadiusPx > 0f && points.size >= 3) {
        moveTo(points[0].x, points[0].y)
        for (i in 1 until points.size) {
            val prev = points[(i - 1 + points.size) % points.size]
            val curr = points[i]
            val next = points[(i + 1) % points.size]

            val v1x = curr.x - prev.x
            val v1y = curr.y - prev.y
            val v2x = next.x - curr.x
            val v2y = next.y - curr.y

            val len1 = sqrt((v1x * v1x + v1y * v1y).toDouble()).toFloat()
            val len2 = sqrt((v2x * v2x + v2y * v2y).toDouble()).toFloat()

            val cornerR = cornerRadiusPx.coerceAtMost(min(len1, len2) / 2f)
            val t1 = cornerR / len1
            val t2 = cornerR / len2

            val startX = prev.x + v1x * (1f - t1)
            val startY = prev.y + v1y * (1f - t1)
            val endX = curr.x + v2x * t2
            val endY = curr.y + v2y * t2

            if (i == 1) {
                moveTo(startX, startY)
            } else {
                lineTo(startX, startY)
            }

            quadraticTo(curr.x, curr.y, endX, endY)
        }
        close()
    } else {
        moveTo(points[0].x, points[0].y)
        for (i in 1 until points.size) {
            lineTo(points[i].x, points[i].y)
        }
        close()
    }

    return this
}

