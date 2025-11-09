package com.amb.photo.ui.activities.editor.blur

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.drawable.Drawable
import androidx.annotation.IntRange
import com.amb.photo.BaseApplication
import com.amb.photo.ui.activities.editor.sticker.Sticker
import com.amb.photo.ui.activities.editor.sticker.StickerAsset


class SplashSticker(
    paramBitmapXor1Path: String?,
    paramBitmapOver2Path: String?
) : Sticker() {
    private var bitmapOver: Bitmap? = null
    private var bitmapXor: Bitmap? = null
    private var over: Paint?
    private var xor: Paint? = Paint()

    //fix bug out of memory bitmap
    private val bitmapOverPath: String?
    private val bitmapXorPath: String?

    init {
        this.xor!!.setDither(true)
        this.xor!!.setAntiAlias(true)
        this.xor!!.setXfermode(PorterDuffXfermode(PorterDuff.Mode.XOR))
        this.over = Paint()
        this.over!!.setDither(true)
        this.over!!.setAntiAlias(true)
        this.over!!.setXfermode(PorterDuffXfermode(PorterDuff.Mode.SRC_OVER))
        this.bitmapOverPath = paramBitmapOver2Path
        this.bitmapXorPath = paramBitmapXor1Path
    }

    fun getBitmapOver(): Bitmap? {
        if (bitmapOver == null) {
            bitmapOver = StickerAsset.loadBitmapFromAssets(
                BaseApplication.getInstanceApp(),
                bitmapOverPath!!
            )
        }
        return this.bitmapOver
    }

    fun getBitmapXor(): Bitmap? {
        if (bitmapXor == null) {
            bitmapXor =
                StickerAsset.loadBitmapFromAssets(BaseApplication.getInstanceApp(), bitmapXorPath!!)
        }

        return this.bitmapXor
    }

    public override fun draw(paramCanvas: Canvas) {
        paramCanvas.drawBitmap(getBitmapXor()!!, matrix, this.xor)
        paramCanvas.drawBitmap(getBitmapOver()!!, matrix, this.over)
    }

    override val alpha: Int
        get() = 1

    override val drawable: Drawable?
        get() = null

    override val height: Int
        get() =//bitmapOver
            getBitmapOver()!!.getHeight()

    override val width: Int
        get() =//bitmapXor
            getBitmapXor()!!.getWidth()

    override fun release() {
        super.release()
        this.xor = null
        this.over = null
        if (this.bitmapXor != null) this.bitmapXor!!.recycle()
        this.bitmapXor = null
        if (this.bitmapOver != null) this.bitmapOver!!.recycle()
        this.bitmapOver = null
    }

    override fun setAlpha(@IntRange(from = 0L, to = 255L) paramInt: Int): SplashSticker {
        return this
    }

    override fun setDrawable(paramDrawable: Drawable): SplashSticker {
        return this
    }
}