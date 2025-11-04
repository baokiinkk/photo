package com.amb.photo.ui.activities.collage

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalDensity
import coil.compose.AsyncImage
import com.amb.photo.data.model.collage.CollageTemplate
import com.amb.photo.data.model.collage.DiagonalShape

@Composable
fun CollagePreview(
    images: List<Uri>,
    template: CollageTemplate,
    gap: Dp = 6.dp,
    corner: Dp = 12.dp,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier) {
        val density = LocalDensity.current
        val w = constraints.maxWidth.toFloat()
        val h = constraints.maxHeight.toFloat()

        Box(Modifier.background(Color(0xFFF5F5F7)))

        template.cells.forEachIndexed { index, cell ->
            val img = images.getOrNull(index % images.size) ?: return@forEachIndexed
            val left = w * cell.x
            val top = h * cell.y
            val cw = w * cell.width
            val ch = h * cell.height

            val base = with(density) {
                Modifier
                    .offset(x = (left).toDp(), y = (top).toDp())
                    .size((cw).toDp(), (ch).toDp())
                    .padding(gap / 2)
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
                    modifier = Modifier
                        .matchParentSize()
                )
            }
        }
    }
}


