package com.amb.photo.ui.activities.editor.sticker.lib

import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import androidx.annotation.IntRange

open class DrawableSticker(override var drawable: Drawable?) : Sticker() {
    private val realBounds: Rect

    init {
        this.realBounds = Rect(0, 0, this.width, this.height)
    }

    override fun draw(paramCanvas: Canvas) {
        paramCanvas.save()
        paramCanvas.concat(matrix)
        this.drawable?.bounds = this.realBounds
        this.drawable?.draw(paramCanvas)
        paramCanvas.restore()
    }

    override val alpha: Int
        get() = this.drawable?.alpha ?: 0


    override val height: Int
        get() = this.drawable?.intrinsicHeight ?: 0

    override val width: Int
        get() = this.drawable?.intrinsicWidth ?: 0

    override fun release() {
        super.release()
        if (this.drawable != null) this.drawable = null
    }


    override fun setAlpha(@IntRange(from = 0L, to = 255L) paramInt: Int): DrawableSticker {
        this.drawable?.alpha = paramInt
        return this
    }

    override fun setDrawable(paramDrawable: Drawable): DrawableSticker {
        this.drawable = paramDrawable
        return this
    }
}
