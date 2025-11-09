package com.amb.photo.ui.activities.collage.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil.compose.AsyncImage
import com.amb.photo.R
import com.amb.photo.data.model.collage.CellSpec
import com.amb.photo.data.model.collage.CollageTemplate
import com.amb.photo.data.model.collage.FreePolygonShape
import com.amb.photo.ui.theme.BackgroundWhite
import com.amb.photo.ui.theme.Primary500
import androidx.core.graphics.toColorInt

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

        template.cells.forEachIndexed { index, cell ->
            val img = images.getOrNull(index % images.size) ?: Uri.EMPTY

            require(cell.points.size == 8) { "Cell must have 8 points (x0,y0,x1,y1,x2,y2,x3,y3)" }

            // Tính bounding box từ points (absolute coordinates 0..1)
            val absPoints = cell.points.mapIndexed { i, v ->
                if (i % 2 == 0) v * w else v * h
            }
            val minX = absPoints.filterIndexed { i, _ -> i % 2 == 0 }.minOrNull() ?: 0f
            val maxX = absPoints.filterIndexed { i, _ -> i % 2 == 0 }.maxOrNull() ?: 0f
            val minY = absPoints.filterIndexed { i, _ -> i % 2 == 1 }.minOrNull() ?: 0f
            val maxY = absPoints.filterIndexed { i, _ -> i % 2 == 1 }.maxOrNull() ?: 0f

            val left = minX
            val top = minY
            val cw = maxX - minX
            val ch = maxY - minY

            // Normalize points to 0..1 relative to bounding box
            val normalizedPoints = cell.points.mapIndexed { i, v ->
                if (i % 2 == 0) {
                    // x coordinate: (v - minX/w) / (cw/w) = (v*w - minX) / cw
                    if (cw > 0f) (v * w - minX) / cw else 0f
                } else {
                    // y coordinate: (v - minY/h) / (ch/h) = (v*h - minY) / ch
                    if (ch > 0f) (v * h - minY) / ch else 0f
                }
            }

            val shape = FreePolygonShape(normalizedPoints)
            val borderWidthPx = with(density) { borderWidth.toPx() }
            val cornerRadiusPx = with(density) { corner.toPx() }

            // Box chứa image (có padding để tạo gap)
            val imageBox = with(density) {
                Modifier
                    .offset(x = left.toDp(), y = top.toDp())
                    .size(cw.toDp(), ch.toDp())
                    .padding(gap / 2)
                    .background(bgColor)
                    .clip(shape)
            }

            // Image box với border overlay
            Box(imageBox) {
                AsyncImage(
                    model = img,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(corner)),
                    error = painterResource(R.drawable.ic_empty_image)
                )

                // Vẽ border trực tiếp trên edge của shape (sau padding)
                if (img.toString().contains("true")) {
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
            "2-vertical", listOf(
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
