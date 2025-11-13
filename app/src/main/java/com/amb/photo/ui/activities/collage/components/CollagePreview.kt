package com.amb.photo.ui.activities.collage.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import androidx.compose.ui.graphics.drawscope.Stroke
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
import androidx.core.graphics.toColorInt
import androidx.core.net.toUri
import coil.compose.AsyncImage
import com.amb.photo.R
import com.amb.photo.data.model.collage.CellSpec
import com.amb.photo.data.model.collage.CollageTemplate
import com.amb.photo.data.model.collage.FreePolygonShape
import com.amb.photo.ui.theme.BackgroundWhite
import com.amb.photo.ui.theme.Primary500

@Composable
fun CollagePreview(
    images: List<Uri>,
    template: CollageTemplate,
    gap: Dp = 6.dp,
    corner: Dp = 1.dp,
    borderWidth: Dp = 5.dp,
    backgroundColor: String? = null,
    modifier: Modifier = Modifier
) {
    // Parse background color from hex string
    val bgColor = remember(backgroundColor) {
        backgroundColor?.let {
            try {
                Color(it.toColorInt())
            } catch (e: Exception) {
                BackgroundWhite
            }
        } ?: BackgroundWhite
    }

    BoxWithConstraints(modifier = modifier.background(bgColor)) {
        val density = LocalDensity.current
        val w = constraints.maxWidth.toFloat()
        val h = constraints.maxHeight.toFloat()

        Box(
            Modifier
                .fillMaxSize()
                .background(bgColor)
        )

        // Xử lý template bằng CollagePreviewDataProcessor
        val processedCells = remember(template, images, w, h) {
            CollagePreviewDataProcessor.processTemplate(
                template = template,
                images = images,
                canvasWidth = w,
                canvasHeight = h
            )
        }

        processedCells.forEach { cellData ->
            val borderWidthPx = with(density) { borderWidth.toPx() }
            val cornerRadiusPx = with(density) { corner.toPx() }
            val gapPx = with(density) { gap.toPx() }

            // Tính toán path bounds nếu có pathRatioBound (lưu ratioBound để dùng trong Shape)
            val pathRatioBound = cellData.pathRatioBound
            val pathInCenterHorizontal = cellData.pathInCenterHorizontal
            val pathInCenterVertical = cellData.pathInCenterVertical

            // Helper function để tạo HEART path (dựa trên createHeartItem từ Java)
            // Java: createHeartItem(float top, float size) với top=0, size=512
            fun createHeartPath(width: Float, height: Float): Path {
                val path = Path()
                val size = kotlin.math.min(width, height)
                val top = 0f  // Trong Java code, top = 0

                // Tạo heart path dựa trên createHeartItem từ Java
                // path.moveTo(top, top + size / 4)
                path.moveTo(top, top + size / 4f)
                // path.quadTo(top, top, top + size / 4, top)
                path.quadraticTo(top, top, top + size / 4f, top)
                // path.quadTo(top + size / 2, top, top + size / 2, top + size / 4)
                path.quadraticTo(top + size / 2f, top, top + size / 2f, top + size / 4f)
                // path.quadTo(top + size / 2, top, top + size * 3 / 4, top)
                path.quadraticTo(top + size / 2f, top, top + size * 3f / 4f, top)
                // path.quadTo(top + size, top, top + size, top + size / 4)
                path.quadraticTo(top + size, top, top + size, top + size / 4f)
                // path.quadTo(top + size, top + size / 2, top + size * 3 / 4, top + size * 3 / 4)
                path.quadraticTo(top + size, top + size / 2f, top + size * 3f / 4f, top + size * 3f / 4f)
                // path.lineTo(top + size / 2, top + size)
                path.lineTo(top + size / 2f, top + size)
                // path.lineTo(top + size / 4, top + size * 3 / 4)
                path.lineTo(top + size / 4f, top + size * 3f / 4f)
                // path.quadTo(top, top + size / 2, top, top + size / 4)
                path.quadraticTo(top, top + size / 2f, top, top + size / 4f)
                path.close()

                return path
            }
            
            // Shape được vẽ trong box có size = actualWidth x actualHeight
            // Nếu có pathType thì dùng shape tương ứng, nếu không thì dùng FreePolygonShape với points
            val shape = when {
                cellData.pathType == "CIRCLE" && pathRatioBound != null && pathRatioBound.size >= 4 -> {
                    // Custom shape cho circle với pathRatioBound
                    object : Shape {
                        override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
                            val path = Path()
                            // Tính toán vị trí và kích thước circle từ pathRatioBound trong không gian size
                            val left = pathRatioBound[0] * size.width
                            val top = pathRatioBound[1] * size.height
                            val right = pathRatioBound[2] * size.width
                            val bottom = pathRatioBound[3] * size.height

                            val pathWidth = right - left
                            val pathHeight = bottom - top

                            // Căn giữa nếu cần
                            val centerX = size.width / 2f
                            val centerY = size.height / 2f

                            val finalLeft = if (pathInCenterHorizontal == true) {
                                centerX - pathWidth / 2f
                            } else {
                                left
                            }

                            val finalTop = if (pathInCenterVertical == true) {
                                centerY - pathHeight / 2f
                            } else {
                                top
                            }

                            // Tính toán circle size và position
                            val circleSize = kotlin.math.min(pathWidth, pathHeight)
                            val circleX = finalLeft + pathWidth / 2f
                            val circleY = finalTop + pathHeight / 2f

                            path.addOval(
                                Rect(
                                    circleX - circleSize / 2f,
                                    circleY - circleSize / 2f,
                                    circleX + circleSize / 2f,
                                    circleY + circleSize / 2f
                                )
                            )
                            return Outline.Generic(path)
                        }
                    }
                }

                cellData.pathType == "HEART" && pathRatioBound != null && pathRatioBound.size >= 4 -> {
                    // Custom shape cho heart với pathRatioBound
                    object : Shape {
                        override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
                            // Tính toán vùng path từ pathRatioBound
                            val left = pathRatioBound[0] * size.width
                            val top = pathRatioBound[1] * size.height
                            val right = pathRatioBound[2] * size.width
                            val bottom = pathRatioBound[3] * size.height

                            val pathWidth = right - left
                            val pathHeight = bottom - top

                            // Tính toán vị trí cuối cùng
                            val finalLeft: Float
                            val finalTop: Float
                            
                            if (pathInCenterHorizontal == true && pathInCenterVertical == true) {
                                // Căn giữa cả hai chiều: heart sẽ được đặt ở giữa bound
                                val centerX = size.width / 2f
                                val centerY = size.height / 2f
                                finalLeft = centerX - pathWidth / 2f
                                finalTop = centerY - pathHeight / 2f
                            } else if (pathInCenterHorizontal == true) {
                                // Chỉ căn giữa theo chiều ngang
                                val centerX = size.width / 2f
                                finalLeft = centerX - pathWidth / 2f
                                finalTop = top
                            } else if (pathInCenterVertical == true) {
                                // Chỉ căn giữa theo chiều dọc
                                val centerY = size.height / 2f
                                finalLeft = left
                                finalTop = centerY - pathHeight / 2f
                            } else {
                                // Không căn giữa
                                finalLeft = left
                                finalTop = top
                            }

                            val heartPath = createHeartPath(pathWidth, pathHeight)
                            // Translate path to final position
                            val translatedPath = Path()
                            translatedPath.addPath(heartPath, Offset(finalLeft, finalTop))
                            return Outline.Generic(translatedPath)
                        }
                    }
                }

                cellData.pathType == "CIRCLE" -> CircleShape
                cellData.normalizedPoints != null && cellData.normalizedPoints.isNotEmpty() ->
                    FreePolygonShape(cellData.normalizedPoints, cellData.shrinkMap)

                else -> RoundedCornerShape(corner) // Fallback
            }
            
            val imageBox = with(density) {
                Modifier
                    .offset(x = cellData.left.toDp(), y = cellData.top.toDp())
                    .size(cellData.width.toDp(), cellData.height.toDp())
                    .padding(gap / 2)
                    .clip(shape)
            }

            // Image box với border overlay
            Box(imageBox) {
                AsyncImage(
                    model = cellData.imageUri,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(corner))
                        .then(
                            // Vẽ clear area nếu có (khoét lỗ)
                            when {
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

                                        // Căn giữa nếu cần
                                        val centerX = actualWidth / 2f
                                        val centerY = actualHeight / 2f

                                        val finalLeft = if (cellData.clearPathInCenterHorizontal == true) {
                                            centerX - pathWidth / 2f
                                        } else {
                                            left
                                        }

                                        val finalTop = if (cellData.clearPathInCenterVertical == true) {
                                            centerY - pathHeight / 2f
                                        } else {
                                            top
                                        }

                                        val clearPath = when (cellData.clearPathType) {
                                            "CIRCLE" -> {
                                                val circleSize = kotlin.math.min(pathWidth, pathHeight)
                                                val circleX = finalLeft + pathWidth / 2f
                                                val circleY = finalTop + pathHeight / 2f
                                                Path().apply {
                                                    addOval(
                                                        Rect(
                                                            circleX - circleSize / 2f,
                                                            circleY - circleSize / 2f,
                                                            circleX + circleSize / 2f,
                                                            circleY + circleSize / 2f
                                                        )
                                                    )
                                                }
                                            }

                                            "HEART" -> {
                                                val heartPath = createHeartPath(pathWidth, pathHeight)
                                                val translatedPath = Path()
                                                translatedPath.addPath(heartPath, Offset(finalLeft, finalTop))
                                                translatedPath
                                            }

                                            "RECT" -> {
                                                Path().apply {
                                                    addRect(
                                                        Rect(
                                                            finalLeft,
                                                            finalTop,
                                                            finalLeft + pathWidth,
                                                            finalTop + pathHeight
                                                        )
                                                    )
                                                }
                                            }

                                            else -> null
                                        }

                                        clearPath?.let {
                                            drawPath(
                                                path = it,
                                                color = bgColor,
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
                                            // Vẽ clear area với background color để tạo hiệu ứng khoét lỗ
                                            val clearPath = Path().apply {
                                                val actualWidth = cellData.width - gapPx
                                                val actualHeight = cellData.height - gapPx
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
                                            // Vẽ clear area với background color để khoét lỗ
                                            drawPath(
                                                path = clearPath,
                                                color = bgColor,
                                                blendMode = BlendMode.SrcOver
                                            )
                                        }
                                    } else {
                                        Modifier
                                    }
                                }

                                else -> Modifier
                            }
                        ),
                    error = painterResource(R.drawable.ic_empty_image)
                )

                // Vẽ border trực tiếp trên edge của shape (sau padding)
                if (cellData.imageUri.toString().contains("true")) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(corner))
                            .drawBehind {
                                val outline = shape.createOutline(
                                    size = size,
                                    layoutDirection = androidx.compose.ui.unit.LayoutDirection.Ltr,
                                    density = this@drawBehind
                                )
                                if (outline is Outline.Generic) {
                                    drawPath(
                                        path = outline.path,
                                        color = Primary500,
                                        style = Stroke(
                                            width = borderWidthPx,
                                            cap = StrokeCap.Round,
                                            pathEffect = PathEffect.cornerPathEffect(cornerRadiusPx * 1.5f)
                                        )
                                    )
                                }
                            }
                    )
                }
            }
        }
    }

}

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
