package com.amb.photo.ui.activities.editor.text_sticker

import android.content.Context
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Shader
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.annotation.ColorInt
import androidx.annotation.IntRange
import com.amb.photo.ui.activities.editor.blur.SystemUtil
import com.amb.photo.ui.activities.editor.sticker.lib.Sticker

class TextSticker(private val context: Context, paramAddTextProperties: AddTextProperties) :
    Sticker() {
    private val addTextProperties: AddTextProperties?
    private var backgroundAlpha = 0

    private var backgroundBorder = 0

    private var backgroundColor = 0

    private val backgroundDrawable: BitmapDrawable? = null

    override var drawable: Drawable? = null

    private var isShowBackground = false

    private var lineSpacingExtra = 0.0f

    private var lineSpacingMultiplier = 1.0f

    private val maxTextSizePixels: Float

    private val minTextSizePixels: Float

    private val paddingHeight = 0

    private var paddingWidth = 0

    private var staticLayout: StaticLayout? = null

    var text: String? = null
        private set

    private var textAlign: Layout.Alignment?

    private var textAlpha = 0

    private var textColor = 0

    override var height: Int = 0
        private set

    private val textPaint: TextPaint

    private var textShadow: AddTextProperties.TextShadow? = null

    override var width: Int = 0
        private set
    private val textRect: Rect
    private val minTextSize: Boolean

    init {
        this.addTextProperties = paramAddTextProperties
        this.textPaint = TextPaint(TextPaint.ANTI_ALIAS_FLAG)
        this.textRect = Rect(0, 0, this.width, this.height)
        this.lineSpacingMultiplier = 1.0f
        this.lineSpacingExtra = 0.0f
        minTextSize = false
        this.minTextSizePixels = convertSpToPx(2.0f)
        this.maxTextSizePixels = convertSpToPx(32.0f)
        this.textAlign = Layout.Alignment.ALIGN_CENTER
        this.textPaint.setTextSize(this.maxTextSizePixels)
        val textSticker =
            setTextSize(paramAddTextProperties.textSize)
                .setTextWidth(paramAddTextProperties.textWidth)
                .setTextHeight(paramAddTextProperties.textHeight)
                .setText(paramAddTextProperties.text)
                .setPaddingWidth(
                    SystemUtil.dpToPx(
                        context,
                        paramAddTextProperties.paddingWidth
                    )
                )
                .setBackgroundBorder(
                    SystemUtil.dpToPx(
                        context,
                        paramAddTextProperties.backgroundBorder
                    )
                )
                .setTextShadow(paramAddTextProperties.textShadow)
                .setTextColor(paramAddTextProperties.textColor)
                .setTextAlpha(paramAddTextProperties.textAlpha)
                .setBackgroundColor(paramAddTextProperties.backgroundColor)
                .setBackgroundAlpha(paramAddTextProperties.backgroundAlpha)
                .setShowBackground(paramAddTextProperties.isShowBackground)
                .setTextColor(paramAddTextProperties.textColor)
        val assetManager = context.getAssets()
        textSticker.setTypeface(
            Typeface.createFromAsset(
                assetManager,
                paramAddTextProperties.fontName
            )
        )
            .setTextAlign(paramAddTextProperties.textAlign)
            .setTextShare(paramAddTextProperties.textShader)
            .resizeText()
    }

    private fun convertSpToPx(paramFloat: Float): Float {
        return paramFloat * (this.context.getResources().getDisplayMetrics()).scaledDensity
    }

    public override fun draw(paramCanvas: Canvas) {
        val matrix: Matrix? = matrix
        paramCanvas.save()
        paramCanvas.concat(matrix)
        if (this.isShowBackground) {
            val paint = Paint()
            if (this.backgroundDrawable != null) {
                paint.setShader(
                    BitmapShader(
                        this.backgroundDrawable.getBitmap(),
                        Shader.TileMode.MIRROR,
                        Shader.TileMode.MIRROR
                    )
                )
                paint.setAlpha(this.backgroundAlpha)
            } else {
                paint.setARGB(
                    this.backgroundAlpha,
                    Color.red(this.backgroundColor),
                    Color.green(this.backgroundColor),
                    Color.blue(this.backgroundColor)
                )
            }
            paramCanvas.drawRoundRect(
                0.0f,
                0.0f,
                this.width.toFloat(),
                this.height.toFloat(),
                this.backgroundBorder.toFloat(),
                this.backgroundBorder.toFloat(),
                paint
            )
            paramCanvas.restore()
            paramCanvas.save()
            paramCanvas.concat(matrix)
        }
        paramCanvas.restore()
        paramCanvas.save()
        paramCanvas.concat(matrix)
        val i = this.paddingWidth
        val j = this.height / 2
        val k = this.staticLayout!!.getHeight() / 2
        paramCanvas.translate(i.toFloat(), (j - k).toFloat())
        this.staticLayout!!.draw(paramCanvas)
        paramCanvas.restore()
        paramCanvas.save()
        paramCanvas.concat(matrix)
        paramCanvas.restore()
    }

    fun getAddTextProperties(): AddTextProperties? {
        return this.addTextProperties
    }

    override val alpha: Int
        get() = this.textPaint.getAlpha()


    override fun release() {
        super.release()
        if (this.drawable != null) this.drawable = null
    }


    fun resizeText(): TextSticker {
        val text = this.text
        if (text != null) {
            if (text.length <= 0) return this

            if (this.textShadow != null) this.textPaint.setShadowLayer(
                this.textShadow!!.radius.toFloat(),
                this.textShadow!!.dx.toFloat(),
                this.textShadow!!.dy.toFloat(),
                this.textShadow!!.colorShadow
            )

            this.textPaint.setTextAlign(Paint.Align.LEFT)
            this.textPaint.setARGB(
                this.textAlpha,
                Color.red(this.textColor),
                Color.green(this.textColor),
                Color.blue(this.textColor)
            )
            var i = this.width - this.paddingWidth * 2
            if (i <= 0) i = 100
            this.staticLayout = StaticLayout(
                this.text,
                this.textPaint,
                i,
                this.textAlign,
                this.lineSpacingMultiplier,
                this.lineSpacingExtra,
                true
            )
            return this
        }
        return this
    }

    override fun setAlpha(@IntRange(from = 0L, to = 255L) paramInt: Int): TextSticker {
        this.textPaint.setAlpha(paramInt)
        return this
    }

    fun setBackgroundAlpha(paramInt: Int): TextSticker {
        this.backgroundAlpha = paramInt
        return this
    }

    fun setBackgroundBorder(paramInt: Int): TextSticker {
        this.backgroundBorder = paramInt
        return this
    }

    fun setBackgroundColor(paramInt: Int): TextSticker {
        this.backgroundColor = paramInt
        return this
    }


    override fun setDrawable(paramDrawable: Drawable): TextSticker {
        this.drawable = paramDrawable
        textRect.set(0, 0, this.width, this.height)
        return this
    }


    fun setPaddingWidth(paramInt: Int): TextSticker {
        this.paddingWidth = paramInt
        return this
    }


    fun setShowBackground(paramBoolean: Boolean): TextSticker {
        this.isShowBackground = paramBoolean
        return this
    }

    fun setText(paramString: String?): TextSticker {
        this.text = paramString
        return this
    }

    fun setTextAlign(paramInt: Int): TextSticker {
        when (paramInt) {
            4 -> {
                this.textAlign = Layout.Alignment.ALIGN_CENTER
                return this
            }

            3 -> {
                this.textAlign = Layout.Alignment.ALIGN_OPPOSITE
                return this
            }

            2 -> {
                this.textAlign = Layout.Alignment.ALIGN_NORMAL
                return this
            }

            else -> return this
        }
    }

    fun setTextAlpha(paramInt: Int): TextSticker {
        this.textAlpha = paramInt
        return this
    }

    fun setTextColor(@ColorInt paramInt: Int): TextSticker {
        this.textColor = paramInt
        return this
    }

    fun setTextHeight(paramInt: Int): TextSticker {
        this.height = paramInt
        return this
    }

    fun setTextShadow(paramTextShadow: AddTextProperties.TextShadow?): TextSticker {
        this.textShadow = paramTextShadow
        return this
    }

    fun setTextShare(paramShader: Shader?): TextSticker {
        this.textPaint.setShader(paramShader)
        return this
    }

    fun setTextSize(paramInt: Int): TextSticker {
        this.textPaint.setTextSize(convertSpToPx(paramInt.toFloat()))
        return this
    }

    fun setTextWidth(paramInt: Int): TextSticker {
        this.width = paramInt
        return this
    }

    fun setTypeface(paramTypeface: Typeface?): TextSticker {
        this.textPaint.setTypeface(paramTypeface)
        return this
    }

    companion object {
        private const val mEllipsis = "…"
    }
}