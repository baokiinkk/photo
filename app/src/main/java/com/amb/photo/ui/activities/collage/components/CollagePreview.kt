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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.amb.photo.R
import com.amb.photo.data.model.collage.CellSpec
import com.amb.photo.data.model.collage.CollageTemplate
import com.amb.photo.data.model.collage.DiagonalShape
import com.amb.photo.ui.theme.BackgroundWhite

@Composable
fun CollagePreview(
    images: List<Uri>,
    template: CollageTemplate,
    gap: Dp = 6.dp,
    corner: Dp = 1.dp,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier.background(BackgroundWhite)) {
        val density = LocalDensity.current
        val w = constraints.maxWidth.toFloat()
        val h = constraints.maxHeight.toFloat()

        Box(
            Modifier
                .fillMaxSize()
                .background(BackgroundWhite)
        )

        template.cells.forEachIndexed { index, cell ->
            val img = images.getOrNull(index % images.size) ?: return@forEachIndexed
            val left = w * cell.x
            val top = h * cell.y
            val cw = w * cell.width
            val ch = h * cell.height

            val base = with(density) {
                Modifier
                    .offset(x = left.toDp(), y = top.toDp())
                    .size(cw.toDp(), ch.toDp())
                    .padding(gap / 2)
                    .background(BackgroundWhite)
            }

            val shape = when (cell.shape) {
                "diag_tlbr" -> DiagonalShape(true)
                "diag_bltr" -> DiagonalShape(false)
                else -> RoundedCornerShape(corner)
            }

            Box(base.clip(shape)) {
                AsyncImage(
                    model = img,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    error = painterResource(R.drawable.ic_empty_image)
                )
            }
        }
    }

}

// Preview Provider
class TemplatePreviewProvider : PreviewParameterProvider<CollageTemplate> {
    override val values = sequenceOf(
        CollageTemplate("1-full", listOf(CellSpec(0f, 0f, 1f, 1f))),
        CollageTemplate(
            "2-vertical", listOf(
                CellSpec(0f, 0f, 0.5f, 1f),
                CellSpec(0.5f, 0f, 0.5f, 1f)
            )
        ),
        CollageTemplate(
            "left-big-right-2", listOf(
                CellSpec(0f, 0f, 0.63f, 1f),
                CellSpec(0.66f, 0f, 0.34f, 0.48f),
                CellSpec(0.66f, 0.52f, 0.34f, 0.48f)
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
        1 -> listOf(Uri.EMPTY)
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
