package com.avnsoft.photoeditor.photocollage.ui.activities.editor.draw

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.draw.lib.BrushDrawingView

sealed class DrawInput {

    data class DrawColor(
        val color: Color,
        val mode: Int = 1,
        val size: Float
    ) : DrawInput()

    data class DrawPattern(
        val color: Color,
        val mode: Int = 1,
        val size: Float
    ) : DrawInput()

}

@Composable
fun DrawComposeView(
    modifier: Modifier,
    drawInput: DrawInput
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            val view = BrushDrawingView(context)
            view.brushDrawingMode = true
            view
        },
        update = { view ->
            when (drawInput) {
                is DrawInput.DrawColor -> {
                    view.setDrawMode(drawInput.mode)
                    view.brushColor = drawInput.color.toArgb()
                    view.brushSize = drawInput.size + 10
                }

                is DrawInput.DrawPattern -> {

                }
            }
        }
    )
}

