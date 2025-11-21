package com.avnsoft.photoeditor.photocollage.ui.activities.editor.remove_object

import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import android.util.Log
import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.remove_object.lib.DrawingView
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.remove_object.lib.ObjAuto
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.remove_object.lib.Type

class RemoveObjectCustomView @JvmOverloads constructor(
    private val context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    var drawingView: DrawingView? = null


    fun registerView(
        mBitmap: Bitmap,
        onDrawView: () -> Unit,
        onFinishDrawView: (Boolean) -> Unit,
        eventClickObjView: ((ObjAuto) -> Unit)? = null
    ) {
        removeAllViews()
        drawingView = DrawingView(context, mBitmap).apply {
            onDraw = onDrawView
            onFinishDraw = onFinishDrawView
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
            )

            eventClickObj = eventClickObjView
        }
        addView(drawingView)
    }

    fun setType(type: Type) {
        drawingView?.setType(type)
    }

    fun setStrokeWidth(strokeWidth: Float, isDrawTouchSize: Boolean) {
        drawingView?.setStrokeWidth(strokeWidth, isDrawTouchSize)
    }

}