package com.avnsoft.photoeditor.photocollage.ui.activities.editor.draw

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.draw.lib.BrushDrawingView
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.draw.lib.DrawAsset
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.draw.lib.DrawBitmapModel

interface DrawInput


data class DrawColor(
    val color: Color = Color.White,
    val mode: Int = 1,
    val size: Float = 20f
) : DrawInput

data class DrawPattern(
    val drawBitmapModel: DrawBitmapModel = DrawAsset.lstDrawBitmapModel().first(),
    val mode: Int = 3,
    val size: Float = 20f
) : DrawInput

data class DrawNeon(
    val color: Color = Color.White,
    val mode: Int = 2,
    val size: Float = 20f
) : DrawInput

data object Undo : DrawInput

data object Redo : DrawInput

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
                is DrawColor -> {
                    view.setDrawMode(drawInput.mode)
                    view.brushColor = drawInput.color.toArgb()
                    view.brushSize = drawInput.size + 10
                }

                is DrawPattern -> {
                    view.setDrawMode(drawInput.mode)
                    view.setCurrentMagicBrush(drawInput.drawBitmapModel)
                    view.brushSize = drawInput.size + 10
                }

                is DrawNeon -> {
                    view.setDrawMode(drawInput.mode)
                    view.brushColor = drawInput.color.toArgb()
                    view.brushSize = drawInput.size + 10
                }

                is Undo -> {
                    view.undo()
                }

                is Redo -> {
                    view.redo()
                }

                else -> {

                }
            }
        }
    )
}

