package com.amb.photo.ui.activities.editor.sticker.lib

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.view.MotionEvent


class BitmapStickerIcon(drawable: Drawable?, paramInt: Int, paramString: String?) : DrawableSticker(drawable), StickerIconEvent {
    private var iconEvent: StickerIconEvent? = null

    private val iconExtraRadius = 10.0f

    val iconRadius: Float = 30.0f

    var position: Int = 0
        private set

    var tag: String?

    var x: Float = 0f

    var y: Float = 0f

    init {
        this.position = paramInt
        this.tag = paramString
    }

    fun draw(paramCanvas: Canvas, paramPaint: Paint) {
        paramCanvas.drawCircle(this.x, this.y, this.iconRadius, paramPaint)
        draw(paramCanvas)
    }

    override fun onActionDown(paramStickerView: StickerView?, paramMotionEvent: MotionEvent?) {
        if (this.iconEvent != null) this.iconEvent!!.onActionDown(paramStickerView, paramMotionEvent)
    }

    override fun onActionMove(paramStickerView: StickerView?, paramMotionEvent: MotionEvent?) {
        if (this.iconEvent != null) this.iconEvent!!.onActionMove(paramStickerView, paramMotionEvent)
    }

    override fun onActionUp(paramStickerView: StickerView?, paramMotionEvent: MotionEvent?) {
        if (this.iconEvent != null) this.iconEvent!!.onActionUp(paramStickerView, paramMotionEvent)
    }

    fun setIconEvent(paramStickerIconEvent: StickerIconEvent?) {
        this.iconEvent = paramStickerIconEvent
    }


    companion object {
        const val ALIGN_HORIZONTALLY: String = "ALIGN_HORIZONTALLY"

        const val EDIT: String = "EDIT"

        const val FLIP: String = "FLIP"

        const val REMOVE: String = "REMOVE"

        const val ROTATE: String = "ROTATE"

        const val ZOOM: String = "ZOOM"
        const val RESET: String = "RESET"
    }
}
