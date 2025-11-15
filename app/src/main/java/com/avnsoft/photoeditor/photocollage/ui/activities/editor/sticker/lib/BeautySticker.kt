package com.avnsoft.photoeditor.photocollage.ui.activities.editor.sticker.lib

import android.content.Context
import android.graphics.Canvas
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.Drawable
import androidx.annotation.IntRange
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.blur.SystemUtil


class BeautySticker(paramContext: Context, val type: Int, override var drawable: Drawable?) :
    Sticker() {
    private val drawableSizeBoobs: Int
    private val drawableSizeFace_Height: Int
    private val drawableSizeFace_Width: Int
    private val drawableSizeHip1_Height: Int
    private val drawableSizeHip1_Width: Int
    private val height_Width = 0
    var radius: Int = 0
    private val realBounds: Rect

    init {
        this.drawableSizeBoobs = SystemUtil.dpToPx(paramContext, 50)
        this.drawableSizeHip1_Width = SystemUtil.dpToPx(paramContext, 150)
        this.drawableSizeHip1_Height = SystemUtil.dpToPx(paramContext, 75)
        this.drawableSizeFace_Height = SystemUtil.dpToPx(paramContext, 50)
        this.drawableSizeFace_Width = SystemUtil.dpToPx(paramContext, 80)
        this.realBounds = Rect(0, 0, this.width, this.height)
    }


    override fun draw(paramCanvas: Canvas) {
        paramCanvas.save()
        paramCanvas.concat(matrix)
        this.drawable!!.setBounds(this.realBounds)
        this.drawable!!.draw(paramCanvas)
        paramCanvas.restore()
    }

    override val alpha: Int
        get() = this.drawable!!.getAlpha()



    override val height: Int
        get() = if (this.type == 1 || this.type == 0) this.drawableSizeBoobs else (if (this.type == 2) this.drawableSizeHip1_Height else (if (this.type == 4) this.drawableSizeFace_Height else (if (this.type == 10 || this.type == 11) this.drawable!!.getIntrinsicHeight() else 0)))

    val mappedCenterPoint2: PointF
        get() = this.mappedCenterPoint!!

    override val width: Int
        get() = if (this.type == 1 || this.type == 0) this.drawableSizeBoobs else (if (this.type == 2) this.drawableSizeHip1_Width else (if (this.type == 4) this.drawableSizeFace_Width else (if (this.type == 10 || this.type == 11) this.height_Width else 0)))

    override fun release() {
        super.release()
        if (this.drawable != null) this.drawable = null
    }

    override fun setAlpha(@IntRange(from = 0L, to = 255L) paramInt: Int): BeautySticker {
        this.drawable!!.setAlpha(paramInt)
        return this
    }

    override fun setDrawable(paramDrawable: Drawable): BeautySticker {
        return this
    }

    fun updateRadius() {
        val rectF: RectF = bound
        if (this.type == 0 || this.type == 1 || this.type == 4) {
            this.radius = (rectF.left + rectF.right).toInt()
        } else if (this.type == 2) {
            this.radius = (rectF.top + rectF.bottom).toInt()
        }
        this.mappedCenterPoint = mappedCenterPoint
    }
}
