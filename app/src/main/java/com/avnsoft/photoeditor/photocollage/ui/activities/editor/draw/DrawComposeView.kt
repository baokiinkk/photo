package com.avnsoft.photoeditor.photocollage.ui.activities.editor.draw

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.draw.lib.BrushDrawingView
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.draw.lib.BrushViewChangeListener
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.draw.lib.DrawAsset
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.draw.lib.DrawBitmapModel
import com.basesource.base.utils.toJson

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

data class Undo(val time: Long = System.currentTimeMillis()) : DrawInput

data class Redo(val time: Long = System.currentTimeMillis()) : DrawInput

@Composable
fun DrawComposeView(
    modifier: Modifier,
    drawInput: DrawInput,
    listener: BrushViewChangeListener
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            val view = BrushDrawingView(context)
            view.brushDrawingMode = true
            view.setBrushViewChangeListener(listener)
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
                    Log.d("aaaa", "undo")
                    val canUndo = view.undo()
                    listener.onUndo(canUndo)
                }

                is Redo -> {
                    Log.d("aaaa", "redo")
                    val canRedo = view.redo()
                    listener.onRedo(canRedo)
                }

                else -> {

                }
            }
        }
    )
}

