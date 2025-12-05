package com.avnsoft.photoeditor.photocollage.ui.activities.freestyle.lib

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.annotation.IntRange
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.sticker.lib.Sticker


class FreeStyleSticker(
    var id: Int,
    photo: Photo?,
    override var drawable: Drawable?,
    val x: Float = 0f,
    val y: Float = 0f,
    val widthRatio: Float = 0f,
    val heightRatio: Float = 0f,
    val rotate: Float=0f,
) : Sticker() {
    private val realBounds: Rect
    private val photo: Photo?

    init {
        this.realBounds = Rect(0, 0, this.width, this.height)
        this.photo = photo
    }

    override fun draw(paramCanvas: Canvas) {
        val matrix: Matrix? = matrix
        paramCanvas.save()
        paramCanvas.concat(matrix)

        val paint = Paint()
        paint.setColor(Color.RED)
        paramCanvas.drawRoundRect(
            0.0f,
            0.0f,
            drawable?.getIntrinsicWidth()?.toFloat() ?: 0f,
            drawable?.getIntrinsicHeight()?.toFloat() ?: 0f,
            40f,
            40f,
            paint
        )
        paramCanvas.restore()
        paramCanvas.save()
        paramCanvas.concat(matrix)


        this.drawable!!.setBounds(this.realBounds)
        this.drawable!!.draw(paramCanvas)
        paramCanvas.restore()
    }

    override val alpha: Int
        get() = this.drawable?.getAlpha() ?: 1


    override val height: Int
        get() = this.drawable?.getIntrinsicHeight() ?: 0

    override val width: Int
        get() = this.drawable?.getIntrinsicWidth() ?: 0

    override fun release() {
        super.release()
        if (this.drawable != null) this.drawable = null
    }

    fun setBorderSticker() {
        if (drawable != null && drawable is BitmapDrawable) {
            val bm = (drawable as BitmapDrawable).getBitmap()
            addWhiteBorder(bm, 20)
        }
    }

    private fun addWhiteBorder(bmp: Bitmap, borderSize: Int): Bitmap {
        val bmpWithBorder = Bitmap.createBitmap(
            bmp.getWidth() + borderSize * 2,
            bmp.getHeight() + borderSize * 2,
            bmp.getConfig()!!
        )
        val canvas = Canvas(bmpWithBorder)
        canvas.drawColor(Color.GREEN)
        canvas.drawBitmap(bmp, borderSize.toFloat(), borderSize.toFloat(), null)
        return bmpWithBorder
    }

    override fun setAlpha(@IntRange(from = 0L, to = 255L) paramInt: Int): FreeStyleSticker {
        this.drawable!!.setAlpha(paramInt)
        return this
    }

    override fun setDrawable(paramDrawable: Drawable): FreeStyleSticker {
        this.drawable = paramDrawable
        return this
    }

    fun getPhoto(): Photo? {
        return photo
    }
}