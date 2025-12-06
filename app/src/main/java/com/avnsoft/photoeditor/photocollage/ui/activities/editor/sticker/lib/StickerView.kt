package com.avnsoft.photoeditor.photocollage.ui.activities.editor.sticker.lib

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.SystemClock
import android.provider.MediaStore
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.widget.FrameLayout
import android.widget.RelativeLayout
import androidx.compose.ui.unit.max
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import com.avnsoft.photoeditor.photocollage.R
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.blur.SystemUtil
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.text_sticker.lib.TextSticker
import com.avnsoft.photoeditor.photocollage.ui.activities.freestyle.lib.FreeStyleSticker
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Arrays
import java.util.Collections
import java.util.Date
import java.util.Locale
import java.util.Random
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt


open class StickerView : FrameLayout {
    private var bitmapPoints = FloatArray(8)

    private val borderPaint: Paint

    private val borderPaintRed: Paint

    private var bounds = FloatArray(8)

    private var bringToFrontCurrentSticker = false

    private var circleRadius = 0

    var isConstrained: Boolean = false
        private set

    private var currentCenterPoint = PointF()

    private var currentIcon: BitmapStickerIcon? = null

    private var currentMode = 0

    private var currentMoveingX = 0f

    private var currentMoveingY = 0f

    var downMatrix: Matrix = Matrix()
        private set

    private var downX = 0f

    private var downY = 0f

    private var drawCirclePoint = false

    private var handlingSticker: Sticker? = null

    private var icons: MutableList<BitmapStickerIcon> = ArrayList<BitmapStickerIcon>(4)

    private var lastClickTime = 0L

    private var lastHandlingSticker: Sticker? = null

    private val linePaint: Paint
    private var borderColor = 0

    private var locked = false

    private var midPoint = PointF()

    var minClickDelayTime: Int = 200
        private set

    var moveMatrix: Matrix = Matrix()
        private set

    private var oldDistance = 0.0f

    private var oldRotation = 0.0f

    private var onMoving = false

    var onStickerOperationListener: OnStickerOperationListener? = null
        protected set

    private var paintCircle: Paint? = null

    private var point = FloatArray(2)

    private var showBorder = false

    private var showIcons = false

    var sizeMatrix: Matrix = Matrix()
        private set

    private var stickerRect = RectF()

    protected var stickers: MutableList<Sticker> = ArrayList<Sticker>()

    private var tmp = FloatArray(2)

    private var touchSlop = 0

    @JvmOverloads
    constructor(paramContext: Context, paramAttributeSet: AttributeSet? = null) : this(
        paramContext,
        paramAttributeSet,
        0
    )

    @SuppressLint("ResourceType")
    constructor(paramContext: Context, paramAttributeSet: AttributeSet?, paramInt: Int) : super(
        paramContext,
        paramAttributeSet,
        paramInt
    ) {
        this.paintCircle = Paint()
        this.paintCircle!!.setAntiAlias(true)
        this.paintCircle!!.setDither(true)
        this.paintCircle!!.setColor(
            ContextCompat.getColor(
                getContext(),
                R.color.colorAccent
            )
        ) //TODO 2131099703 checkbox_themeable_attribute_color
        this.paintCircle!!.setStrokeWidth(SystemUtil.dpToPx(getContext(), 2).toFloat())
        this.paintCircle!!.setStyle(Paint.Style.STROKE)
        this.touchSlop = ViewConfiguration.get(paramContext).getScaledTouchSlop()

        linePaint = Paint()
        this.linePaint.setAntiAlias(true)
        this.linePaint.setStrokeWidth(SystemUtil.dpToPx(getContext(), 2).toFloat())


        this.borderPaint = Paint()
        this.borderPaint.setAntiAlias(true)
        this.borderPaint.setColor(ContextCompat.getColor(getContext(), R.color.white))
        this.borderPaintRed = Paint()
        this.borderPaintRed.setAntiAlias(true)
        this.borderPaintRed.setColor(ContextCompat.getColor(getContext(), R.color.sticker_icon_red))

        val typedArray: TypedArray
        try {
//            setBackgroundResource(R.drawable.bg_freestyle_view)

            typedArray =
                paramContext.obtainStyledAttributes(paramAttributeSet, R.styleable.StickerView)

            showIcons = typedArray.getBoolean(R.styleable.StickerView_showIcons, true)
            showBorder = typedArray.getBoolean(R.styleable.StickerView_showBorder, true)
            bringToFrontCurrentSticker =
                typedArray.getBoolean(R.styleable.StickerView_bringToFrontCurrentSticker, false)
            borderColor = typedArray.getColor(R.styleable.StickerView_borderColor, Color.WHITE)
            configDefaultIcons()
            typedArray.recycle()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        this.linePaint.setColor(borderColor)
    }

    constructor(context: Context, attributeSet: AttributeSet?, i: Int, i2: Int) : super(
        context,
        attributeSet,
        i,
        i2
    ) {
        this.stickers = ArrayList<Sticker>()
        this.icons = ArrayList<BitmapStickerIcon>(4)
        this.borderPaint = Paint()
        this.borderPaintRed = Paint()
        this.linePaint = Paint()
        this.stickerRect = RectF()
        this.sizeMatrix = Matrix()
        this.downMatrix = Matrix()
        this.moveMatrix = Matrix()
        this.bitmapPoints = FloatArray(8)
        this.bounds = FloatArray(8)
        this.point = FloatArray(2)
        this.currentCenterPoint = PointF()
        this.tmp = FloatArray(2)
        this.midPoint = PointF()
        this.drawCirclePoint = false
        this.onMoving = false
        this.oldDistance = 0.0f
        this.oldRotation = 0.0f
        this.currentMode = 0
        this.lastClickTime = 0
        this.minClickDelayTime = 200
    }


    fun configStickerIcons() {
        val bitmapStickerIcon = BitmapStickerIcon(
            ContextCompat.getDrawable(context, R.drawable.ic_close_sticker),
            0,
            BitmapStickerIcon.REMOVE
        )
        bitmapStickerIcon.setIconEvent(DeleteIconEvent())

        val bitmapStickerIcon3: BitmapStickerIcon = BitmapStickerIcon(
            ContextCompat.getDrawable(context, R.drawable.ic_sticker_flip),
            2,
            BitmapStickerIcon.FLIP
        )
        bitmapStickerIcon3.setIconEvent(FlipHorizontallyEvent())

//        val resetIcon = BitmapStickerIcon(
//            ContextCompat.getDrawable(context, R.drawable.ic_textrestore),
//            2,
//            BitmapStickerIcon.RESET
//        )
//        resetIcon.setIconEvent(ResetIconEvent())

        val bitmapStickerIcon2: BitmapStickerIcon = BitmapStickerIcon(
            ContextCompat.getDrawable(context, R.drawable.ic_sticker_scale),
            3,
            BitmapStickerIcon.ZOOM
        )
        bitmapStickerIcon2.setIconEvent(ZoomIconEvent())

        this.icons.clear()
        this.icons.add(bitmapStickerIcon)
        this.icons.add(bitmapStickerIcon2)
        this.icons.add(bitmapStickerIcon3)
//        this.icons.add(resetIcon)
    }

    fun configDefaultIcons() {
        val bitmapStickerIcon = BitmapStickerIcon(
            ContextCompat.getDrawable(context, R.drawable.ic_close_sticker),
            0,
            BitmapStickerIcon.REMOVE
        )
        bitmapStickerIcon.setIconEvent(DeleteIconEvent())

        val zoomIcon = BitmapStickerIcon(
            ContextCompat.getDrawable(context, R.drawable.ic_sticker_scale),
            3,
            BitmapStickerIcon.ZOOM
        )
        zoomIcon.setIconEvent(ZoomIconEvent())

        val editortext: BitmapStickerIcon = BitmapStickerIcon(
            ContextCompat.getDrawable(context, R.drawable.ic_text_editor),
            1,
            BitmapStickerIcon.EDIT
        )
        editortext.setIconEvent(EditTextIconEvent())

        val bitmapStickerIcon3: BitmapStickerIcon = BitmapStickerIcon(
            ContextCompat.getDrawable(context, R.drawable.ic_sticker_flip),
            2,
            BitmapStickerIcon.FLIP
        )
        bitmapStickerIcon3.setIconEvent(FlipHorizontallyEvent())

        icons.clear()
        icons.add(bitmapStickerIcon)
        icons.add(zoomIcon)
        icons.add(editortext)
        icons.add(bitmapStickerIcon3)
    }


    fun addSticker(paramSticker: Sticker): StickerView {
        return addSticker(paramSticker, Sticker.Position.CENTER)
    }

    fun addSticker(sticker: Sticker, position: Int): StickerView {
        if (ViewCompat.isLaidOut(this)) {
            addStickerImmediately(sticker, position)
            return this
        }
        post(Runnable { this@StickerView.addStickerImmediately(sticker, position) })
        return this
    }

    fun addStickerFromServer(
        sticker: FreeStyleSticker
    ): StickerView {
        if (ViewCompat.isLaidOut(this)) {
            addStickerImmediately(
                sticker,
                x = sticker.x,
                y = sticker.y,
                widthRatio = sticker.widthRatio,
                heightRatio = sticker.heightRatio,
                rotate = sticker.rotate
            )
            return this
        }
        post(Runnable {
            this@StickerView.addStickerImmediately(
                sticker,
                x = sticker.x,
                y = sticker.y,
                widthRatio = sticker.widthRatio,
                heightRatio = sticker.heightRatio,
                rotate = sticker.rotate
            )
        })
        return this
    }


    protected fun addStickerImmediately(paramSticker: Sticker, paramInt: Int) {
        setStickerPosition(paramSticker, paramInt)
        constrainSticker(paramSticker)
        paramSticker.matrix.postScale(1.0f, 1.0f, width.toFloat(), height.toFloat())
        this.handlingSticker = paramSticker
        this.stickers.add(paramSticker)
        if (this.onStickerOperationListener != null) this.onStickerOperationListener!!.onStickerAdded(
            paramSticker
        )
        invalidate()
    }

    protected fun addStickerImmediately(
        paramSticker: Sticker,
        x: Float,
        y: Float,
        widthRatio: Float,
        heightRatio: Float,
        rotate: Float
    ) {
        if (width <= 0 || height <= 0) return

        // 1. Quy đổi tỉ lệ sang pixel theo kích thước View
        val parentWidth = width.toFloat()
        val parentHeight = height.toFloat()

        // Tỉ lệ 0–1 -> px
        val targetWidth = (parentWidth * widthRatio).coerceAtLeast(0f)
        val targetHeight = (parentHeight * heightRatio).coerceAtLeast(0f)
        val left = parentWidth * x          // top-left X của placeholder
        val top = parentHeight * y          // top-left Y của placeholder

        // 2. Kích thước gốc của sticker
        val originalWidth = paramSticker.width.toFloat().takeIf { it > 0 } ?: targetWidth
        val originalHeight = paramSticker.height.toFloat().takeIf { it > 0 } ?: targetHeight

        // 3. Scale cần thiết để sticker khớp placeholder
        val scaleX = if (originalWidth > 0f) targetWidth / originalWidth else 1f
        val scaleY = if (originalHeight > 0f) targetHeight / originalHeight else 1f

        // 4. Tính pivot xoay giống logic TransformOrigin bên Compose
        val pivotX: Float
        val pivotY: Float
        if (rotate < 0f) {
            // Góc xoay âm: xoay giữ góc phải trên
            pivotX = left + targetWidth
            pivotY = top
        } else if (rotate > 0f) {
            // Góc xoay dương: xoay giữ góc trái dưới
            pivotX = left
            pivotY = top + targetHeight
        } else {
            // Không xoay: pivot giữa khung
            pivotX = left + targetWidth / 2f
            pivotY = top + targetHeight / 2f
        }

        // 5. Reset ma trận và apply scale + rotate quanh pivot
        val m = paramSticker.matrix
        m.reset()

        // Scale quanh pivot
        m.postScale(scaleX, scaleY, pivotX, pivotY)
        // Xoay quanh cùng pivot (đúng theo baseBannerItemModifier: rotate * -1 đã được apply ở JSON rồi)
        m.postRotate(rotate, pivotX, pivotY)

        // 6. Dời sticker để top-left sau transform trùng với (left, top)
        val mapped = android.graphics.RectF(0f, 0f, originalWidth, originalHeight)
        m.mapRect(mapped)
        val dx = left - mapped.left
        val dy = top - mapped.top
        m.postTranslate(dx, dy)

        // 7. Giữ sticker trong khung
        constrainSticker(paramSticker)

        handlingSticker = paramSticker
        stickers.add(paramSticker)
        onStickerOperationListener?.onStickerAdded(paramSticker)

        invalidate()
    }


    fun alignHorizontally() {
        this.moveMatrix.set(this.downMatrix)
        this.moveMatrix.postRotate(
            -this.currentSticker!!.currentAngle,
            this.midPoint.x,
            this.midPoint.y
        )
        this.handlingSticker!!.setMatrix(this.moveMatrix)
    }

    protected fun calculateDistance(
        paramFloat1: Float,
        paramFloat2: Float,
        paramFloat3: Float,
        paramFloat4: Float
    ): Float {
        val d1 = (paramFloat1 - paramFloat3).toDouble()
        val d2 = (paramFloat2 - paramFloat4).toDouble()
        return sqrt(d1 * d1 + d2 * d2).toFloat()
    }

    protected fun calculateDistance(paramMotionEvent: MotionEvent?): Float {
        return if (paramMotionEvent == null || paramMotionEvent.getPointerCount() < 2) 0.0f else calculateDistance(
            paramMotionEvent.getX(0),
            paramMotionEvent.getY(0),
            paramMotionEvent.getX(1),
            paramMotionEvent.getY(1)
        )
    }

    protected fun calculateMidPoint(): PointF {
        if (this.handlingSticker == null) {
            this.midPoint.set(0.0f, 0.0f)
            return this.midPoint
        }
        this.handlingSticker!!.getMappedCenterPoint(this.midPoint, this.point, this.tmp)
        return this.midPoint
    }

    protected fun calculateMidPoint(paramMotionEvent: MotionEvent?): PointF {
        if (paramMotionEvent == null || paramMotionEvent.getPointerCount() < 2) {
            this.midPoint.set(0.0f, 0.0f)
            return this.midPoint
        }
        val f1 = (paramMotionEvent.getX(0) + paramMotionEvent.getX(1)) / 2.0f
        val f2 = (paramMotionEvent.getY(0) + paramMotionEvent.getY(1)) / 2.0f
        this.midPoint.set(f1, f2)
        return this.midPoint
    }

    protected fun calculateRotation(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        val x = (x1 - x2).toDouble()
        val y = (y1 - y2).toDouble()
        val radians = atan2(y, x)
        return Math.toDegrees(radians).toFloat()
    }

    protected fun calculateRotation(paramMotionEvent: MotionEvent?): Float {
        return if (paramMotionEvent == null || paramMotionEvent.getPointerCount() < 2) 0.0f else calculateRotation(
            paramMotionEvent.getX(0),
            paramMotionEvent.getY(0),
            paramMotionEvent.getX(1),
            paramMotionEvent.getY(1)
        )
    }


    protected fun configIconMatrix(
        paramBitmapStickerIcon: BitmapStickerIcon,
        paramFloat1: Float,
        paramFloat2: Float,
        paramFloat3: Float
    ) {
        paramBitmapStickerIcon.x = paramFloat1
        paramBitmapStickerIcon.y = paramFloat2
        paramBitmapStickerIcon.matrix.reset()
        paramBitmapStickerIcon.matrix.postRotate(
            paramFloat3,
            (paramBitmapStickerIcon.width.toFloat() / 2),
            (paramBitmapStickerIcon.height.toFloat() / 2)
        )
        paramBitmapStickerIcon.matrix.postTranslate(
            paramFloat1 - (paramBitmapStickerIcon.width / 2),
            paramFloat2 - (paramBitmapStickerIcon.height / 2)
        )
    }

    //    protected fun constrainSticker(paramSticker: Sticker) {
//        val i = getWidth()
//        val j = getHeight()
//        paramSticker.getMappedCenterPoint(this.currentCenterPoint, this.point, this.tmp)
//        var f1 = this.currentCenterPoint.x
//        var f3 = 0.0f
//        if (f1 < 0.0f) {
//            f1 = -this.currentCenterPoint.x
//        } else {
//            f1 = 0.0f
//        }
//        var f4 = this.currentCenterPoint.x
//        var f2 = f1
//        if (f4 > i.toFloat()) f2 = i.toFloat() - this.currentCenterPoint.x
//        f1 = f3
//        if (this.currentCenterPoint.y < 0.0f) f1 = -this.currentCenterPoint.y
//        f3 = this.currentCenterPoint.y
//        f4 = j.toFloat()
//        if (f3 > f4) f1 = f4 - this.currentCenterPoint.y
//        paramSticker.matrix.postTranslate(f2, f1)
//    }
    protected fun constrainSticker(sticker: Sticker) {
        val viewWidth = this.width.toFloat()
        val viewHeight = this.height.toFloat()

        // 1. Lấy các điểm giới hạn gốc (chưa biến đổi) của sticker.
        sticker.getBoundPoints(this.bounds)

        // 2. Áp dụng ma trận hiện tại (xoay, di chuyển, phóng to) để lấy tọa độ 4 góc thực tế trên màn hình.
        // Kết quả được lưu vào mảng `this.bitmapPoints`.
        sticker.getMappedPoints(this.bitmapPoints, this.bounds)

        // 3. Tìm hình chữ nhật nhỏ nhất bao quanh 4 góc đã biến đổi.
        // this.bitmapPoints là mảng [x0, y0, x1, y1, x2, y2, x3, y3]
        var minX = this.bitmapPoints[0]
        var maxX = this.bitmapPoints[0]
        var minY = this.bitmapPoints[1]
        var maxY = this.bitmapPoints[1]

        for (i in 1 until 4) {
            minX = min(minX, this.bitmapPoints[i * 2])
            maxX = kotlin.math.max(maxX, this.bitmapPoints[i * 2])
            minY = min(minY, this.bitmapPoints[i * 2 + 1])
            maxY = kotlin.math.max(maxY, this.bitmapPoints[i * 2 + 1])
        }

        // 4. Tính toán khoảng cách cần dịch chuyển (delta) để đưa sticker trở lại vào trong.
        var dx = 0f
        var dy = 0f

        if (minX < 0) {
            dx = -minX // Nếu cạnh trái vượt ra ngoài, dịch sang phải.
        } else if (maxX > viewWidth) {
            dx = viewWidth - maxX // Nếu cạnh phải vượt ra ngoài, dịch sang trái.
        }

        if (minY < 0) {
            dy = -minY // Nếu cạnh trên vượt ra ngoài, dịch xuống dưới.
        } else if (maxY > viewHeight) {
            dy = viewHeight - maxY // Nếu cạnh dưới vượt ra ngoài, dịch lên trên.
        }

        // 5. Nếu cần dịch chuyển, áp dụng nó vào ma trận của sticker.
        if (dx != 0f || dy != 0f) {
            sticker.matrix.postTranslate(dx, dy)
            // Yêu cầu vẽ lại view ngay lập tức để thấy sticker "bật" lại vào trong.
            invalidate()
        }
    }


    @Throws(OutOfMemoryError::class)
    fun createBitmap(): Bitmap {
        this.handlingSticker = null
        val bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888)
        draw(Canvas(bitmap))
        return bitmap
    }

    override fun dispatchDraw(paramCanvas: Canvas) {
        super.dispatchDraw(paramCanvas)
        if (this.drawCirclePoint && this.onMoving) {
            paramCanvas.drawCircle(
                this.downX,
                this.downY,
                this.circleRadius.toFloat(),
                this.paintCircle!!
            )
            paramCanvas.drawLine(
                this.downX,
                this.downY,
                this.currentMoveingX,
                this.currentMoveingY,
                this.paintCircle!!
            )
        }
        drawStickers(paramCanvas)
    }

    protected fun drawStickers(paramCanvas: Canvas) {
        var i: Int
        i = 0
        while (i < this.stickers.size) {
            val sticker: Sticker? = this.stickers.get(i)
            if (sticker != null && sticker.isShow) sticker.draw(paramCanvas)
            i++
        }
        if (this.handlingSticker != null && !this.locked && (this.showBorder || this.showIcons)) {
            getStickerPoints(this.handlingSticker, this.bitmapPoints)
            val f5 = this.bitmapPoints[0]
            val f6 = this.bitmapPoints[1]
            val f7 = this.bitmapPoints[2]
            val f8 = this.bitmapPoints[3]
            val f4 = this.bitmapPoints[4]
            val f3 = this.bitmapPoints[5]
            val f2 = this.bitmapPoints[6]
            val f1 = this.bitmapPoints[7]
            if (this.showBorder) {
                paramCanvas.drawLine(f5, f6, f7, f8, this.linePaint)
                paramCanvas.drawLine(f5, f6, f4, f3, this.linePaint)
                paramCanvas.drawLine(f7, f8, f2, f1, this.linePaint)
                paramCanvas.drawLine(f2, f1, f4, f3, this.linePaint)
            }
            if (this.showIcons) {
                val f = calculateRotation(f2, f1, f4, f3)
                i = 0
                while (i < this.icons.size) {
                    val bitmapStickerIcon: BitmapStickerIcon = this.icons.get(i)
                    when (bitmapStickerIcon.position) {
                        3 -> {
                            configIconMatrix(bitmapStickerIcon, f2, f1, f)
                            bitmapStickerIcon.draw(paramCanvas, this.borderPaint)
                        }

                        2 -> {
                            configIconMatrix(bitmapStickerIcon, f4, f3, f)
                            bitmapStickerIcon.draw(paramCanvas, this.borderPaint)
                        }

                        1 -> {
                            configIconMatrix(bitmapStickerIcon, f7, f8, f)
                            bitmapStickerIcon.draw(paramCanvas, this.borderPaint)
                        }

                        0 -> {
                            configIconMatrix(bitmapStickerIcon, f5, f6, f)
                            bitmapStickerIcon.draw(paramCanvas, this.borderPaintRed)
                        }
                    }
                    i++
                }
            }
        }
        invalidate()
    }

    fun editTextSticker() {
        this.onStickerOperationListener!!.onTextStickerEdit(this.handlingSticker!!)
    }

    protected fun findCurrentIconTouched(): BitmapStickerIcon? {
        for (bitmapStickerIcon in this.icons) {
            val f1: Float = bitmapStickerIcon.x - this.downX
            val f2: Float = bitmapStickerIcon.y - this.downY
            if ((f1 * f1 + f2 * f2) <= (bitmapStickerIcon.iconRadius + bitmapStickerIcon.iconRadius).pow(
                    2.0f
                )
            ) return bitmapStickerIcon
        }
        return null
    }

    protected fun findHandlingSticker(): Sticker? {
        for (i in this.stickers.indices.reversed()) {
            if (isInStickerArea(
                    this.stickers.get(i),
                    this.downX,
                    this.downY
                )
            ) return this.stickers.get(i)
        }
        return null
    }

    fun flip(paramSticker: Sticker?, paramInt: Int) {
        if (paramSticker != null) {
            paramSticker.getCenterPoint(this.midPoint)
            if ((paramInt and 0x1) > 0) {
                paramSticker.matrix.preScale(-1.0f, 1.0f, this.midPoint.x, this.midPoint.y)
                paramSticker.setFlippedHorizontally(!paramSticker.isFlippedHorizontally)
            }
            if ((paramInt and 0x2) > 0) {
                paramSticker.matrix.preScale(1.0f, -1.0f, this.midPoint.x, this.midPoint.y)
                paramSticker.setFlippedVertically(!paramSticker.isFlippedVertically)
            }
            if (this.onStickerOperationListener != null) this.onStickerOperationListener!!.onStickerFlipped(
                paramSticker
            )
            invalidate()
        }
    }

    fun flipCurrentSticker(paramInt: Int) {
        flip(this.handlingSticker, paramInt)
    }

    val currentSticker: Sticker?
        get() = this.handlingSticker

    fun getIcons(): MutableList<BitmapStickerIcon> {
        return this.icons
    }

    fun getLastHandlingSticker(): Sticker? {
        return this.lastHandlingSticker
    }

    fun getCurrentTextSticker(): TextSticker? {
        return currentSticker as? TextSticker
    }

    fun getCurrentDrawableSticker(): DrawableSticker? {
        return currentSticker as? DrawableSticker
    }

    val stickerCount: Int
        get() = this.stickers.size

    fun getStickerPoints(paramSticker: Sticker?, paramArrayOffloat: FloatArray) {
        if (paramSticker == null) {
            Arrays.fill(paramArrayOffloat, 0.0f)
            return
        }
        paramSticker.getBoundPoints(this.bounds)
        paramSticker.getMappedPoints(paramArrayOffloat, this.bounds)
    }

    fun getStickerPoints(paramSticker: Sticker?): FloatArray {
        val arrayOfFloat = FloatArray(8)
        getStickerPoints(paramSticker, arrayOfFloat)
        return arrayOfFloat
    }

    protected fun handleCurrentMode(paramMotionEvent: MotionEvent) {
        when (this.currentMode) {
            3 -> if (this.handlingSticker != null && this.currentIcon != null) {
                this.currentIcon!!.onActionMove(this, paramMotionEvent)
                return
            }

            2 -> if (this.handlingSticker != null) {
                val f1 = calculateDistance(paramMotionEvent)
                val f2 = calculateRotation(paramMotionEvent)
                this.moveMatrix.set(this.downMatrix)
                this.moveMatrix.postScale(
                    f1 / this.oldDistance,
                    f1 / this.oldDistance,
                    this.midPoint.x,
                    this.midPoint.y
                )
                this.moveMatrix.postRotate(f2 - this.oldRotation, this.midPoint.x, this.midPoint.y)
                this.handlingSticker!!.setMatrix(this.moveMatrix)
                return
            }

            1 -> {
                this.currentMoveingX = paramMotionEvent.getX()
                this.currentMoveingY = paramMotionEvent.getY()
                if (this.drawCirclePoint) this.onStickerOperationListener!!.onTouchDragForBeauty(
                    this.currentMoveingX,
                    this.currentMoveingY
                )
                if (this.handlingSticker != null) {
                    this.moveMatrix.set(this.downMatrix)
                    if (this.handlingSticker is BeautySticker) {
                        val beautySticker: BeautySticker = this.handlingSticker as BeautySticker
                        if (beautySticker.type === 10 || beautySticker.type === 11) {
                            this.moveMatrix.postTranslate(
                                0.0f,
                                paramMotionEvent.getY() - this.downY
                            )
                        } else {
                            this.moveMatrix.postTranslate(
                                paramMotionEvent.getX() - this.downX,
                                paramMotionEvent.getY() - this.downY
                            )
                        }
                    } else {
                        this.moveMatrix.postTranslate(
                            paramMotionEvent.getX() - this.downX,
                            paramMotionEvent.getY() - this.downY
                        )
                    }
                    this.handlingSticker!!.setMatrix(this.moveMatrix)
                    if (this.isConstrained) constrainSticker(this.handlingSticker!!)
                }
            }

            0, 4 -> {}
            else -> return
        }
    }

    protected fun isInStickerArea(
        paramSticker: Sticker,
        paramFloat1: Float,
        paramFloat2: Float
    ): Boolean {
        this.tmp[0] = paramFloat1
        this.tmp[1] = paramFloat2
        return paramSticker.contains(this.tmp)
    }

    val isNoneSticker: Boolean
        get() = (this.stickerCount == 0)

    override fun onInterceptTouchEvent(paramMotionEvent: MotionEvent): Boolean {
        if (this.locked) return super.onInterceptTouchEvent(paramMotionEvent)
        if (paramMotionEvent.getAction() != 0) return super.onInterceptTouchEvent(paramMotionEvent)
        this.downX = paramMotionEvent.getX()
        this.downY = paramMotionEvent.getY()
        return (findCurrentIconTouched() != null || findHandlingSticker() != null)
    }

    override fun onLayout(
        paramBoolean: Boolean,
        paramInt1: Int,
        paramInt2: Int,
        paramInt3: Int,
        paramInt4: Int
    ) {
        super.onLayout(paramBoolean, paramInt1, paramInt2, paramInt3, paramInt4)
        if (paramBoolean) {
            this.stickerRect.left = paramInt1.toFloat()
            this.stickerRect.top = paramInt2.toFloat()
            this.stickerRect.right = paramInt3.toFloat()
            this.stickerRect.bottom = paramInt4.toFloat()
        }
    }

    override fun onSizeChanged(paramInt1: Int, paramInt2: Int, paramInt3: Int, paramInt4: Int) {
        var paramInt1 = paramInt1
        super.onSizeChanged(paramInt1, paramInt2, paramInt3, paramInt4)
        paramInt1 = 0
        while (paramInt1 < this.stickers.size) {
            val sticker: Sticker? = this.stickers.get(paramInt1)
            if (sticker != null) transformSticker(sticker)
            paramInt1++
        }
    }

    protected fun onTouchDown(paramMotionEvent: MotionEvent): Boolean {
        this.currentMode = 1
        this.downX = paramMotionEvent.getX()
        this.downY = paramMotionEvent.getY()
        this.onMoving = true
        this.currentMoveingX = paramMotionEvent.getX()
        this.currentMoveingY = paramMotionEvent.getY()
        this.midPoint = calculateMidPoint()
        this.oldDistance =
            calculateDistance(this.midPoint.x, this.midPoint.y, this.downX, this.downY)
        this.oldRotation =
            calculateRotation(this.midPoint.x, this.midPoint.y, this.downX, this.downY)
        this.currentIcon = findCurrentIconTouched()
        if (this.currentIcon != null) {
            this.currentMode = 3
            this.currentIcon!!.onActionDown(this, paramMotionEvent)
        } else {
            this.handlingSticker = findHandlingSticker()
        }
        if (this.handlingSticker != null) {
            this.downMatrix.set(this.handlingSticker!!.matrix)
            if (this.bringToFrontCurrentSticker) {
                this.stickers.remove(this.handlingSticker)
                this.stickers.add(this.handlingSticker!!)
            }
            if (this.onStickerOperationListener != null) this.onStickerOperationListener!!.onStickerTouchedDown(
                this.handlingSticker!!
            )
        }
        if (this.drawCirclePoint) {
            this.onStickerOperationListener!!.onTouchDownForBeauty(
                this.currentMoveingX,
                this.currentMoveingY
            )
            invalidate()
            return true
        }
        if (this.currentIcon == null && this.handlingSticker == null) return false
        invalidate()
        return true
    }

    override fun onTouchEvent(paramMotionEvent: MotionEvent): Boolean {
        if (this.locked) return super.onTouchEvent(paramMotionEvent)
        when (paramMotionEvent.getAction()) {
            6 -> {
                if (this.currentMode == 2 && this.handlingSticker != null && this.onStickerOperationListener != null) this.onStickerOperationListener!!.onStickerZoomFinished(
                    this.handlingSticker!!
                )
                this.currentMode = 0
                return true
            }

            5 -> {
                this.oldDistance = calculateDistance(paramMotionEvent)
                this.oldRotation = calculateRotation(paramMotionEvent)
                this.midPoint = calculateMidPoint(paramMotionEvent)
                if (this.handlingSticker != null && isInStickerArea(
                        this.handlingSticker!!,
                        paramMotionEvent.getX(1),
                        paramMotionEvent.getY(1)
                    ) && findCurrentIconTouched() == null
                ) {
                    this.currentMode = 2
                    return true
                }
                return true
            }

            2 -> {
                handleCurrentMode(paramMotionEvent)
                invalidate()
                if (isConstrained && handlingSticker != null) {
                    constrainSticker(handlingSticker!!)
                }
                return true
            }

            1 -> {
                onTouchUp(paramMotionEvent)
                return true
            }

            0 -> {}
            else -> return true
        }
        if (!onTouchDown(paramMotionEvent)) {
            if (this.onStickerOperationListener == null) return false
            this.onStickerOperationListener!!.onStickerTouchOutside(this.handlingSticker)
            invalidate()
            return this.drawCirclePoint
        }
        return true
    }

    fun resetDefault() {
        this.onStickerOperationListener?.onStickerTouchOutside(this.handlingSticker)
        invalidate()
    }

    protected fun onTouchUp(paramMotionEvent: MotionEvent) {
        val l = SystemClock.uptimeMillis()
        this.onMoving = false
        if (this.drawCirclePoint) this.onStickerOperationListener!!.onTouchUpForBeauty(
            paramMotionEvent.getX(),
            paramMotionEvent.getY()
        )
        if (this.currentMode == 3 && this.currentIcon != null && this.handlingSticker != null) this.currentIcon!!.onActionUp(
            this,
            paramMotionEvent
        )
        if (this.currentMode == 1 && abs(paramMotionEvent.getX() - this.downX) < this.touchSlop && abs(
                paramMotionEvent.getY() - this.downY
            ) < this.touchSlop && this.handlingSticker != null
        ) {
            this.currentMode = 4
            if (this.onStickerOperationListener != null) this.onStickerOperationListener!!.onStickerClicked(
                this.handlingSticker!!
            )
            if (l - this.lastClickTime < this.minClickDelayTime && this.onStickerOperationListener != null) this.onStickerOperationListener!!.onStickerDoubleTapped(
                this.handlingSticker!!
            )
        }
        if (this.currentMode == 1 && this.handlingSticker != null && this.onStickerOperationListener != null) this.onStickerOperationListener!!.onStickerDragFinished(
            this.handlingSticker!!
        )
        this.currentMode = 0
        this.lastClickTime = l
    }

    fun remove(paramSticker: Sticker): Boolean {
        if (this.stickers.contains(paramSticker)) {
            this.stickers.remove(paramSticker)
            if (this.onStickerOperationListener != null) this.onStickerOperationListener!!.onStickerDeleted(
                paramSticker
            )
            if (this.handlingSticker === paramSticker) this.handlingSticker = null
            invalidate()
            return true
        }
        Log.d("StickerView", "remove: the sticker is not in this StickerView")
        return false
    }

    fun removeAllStickers() {
        this.stickers.clear()
        if (this.handlingSticker != null) {
            this.handlingSticker!!.release()
            this.handlingSticker = null
        }
        invalidate()
    }

    fun removeCurrentSticker(): Boolean {
        return try {
            remove(this.handlingSticker!!)
        } catch (ex: Exception) {
            false
        }
    }

    fun resetTextRotation() {
        if (currentSticker == null) return
        if (this.handlingSticker == null) return
        val currentSticker: Sticker = this.currentSticker!!
        currentSticker.getCenterPoint(this.midPoint)
        val matrix: Matrix = this.handlingSticker!!.matrix
        matrix.preRotate(-currentSticker.currentAngle, this.midPoint.x, this.midPoint.y)
        currentSticker.setMatrix(matrix)
        invalidate()
    }


    fun replace(paramSticker: Sticker?): Boolean {
        return replace(paramSticker, true)
    }

    fun replace(paramSticker: Sticker?, paramBoolean: Boolean): Boolean {
        if (this.handlingSticker == null) this.handlingSticker = this.lastHandlingSticker
        if (this.handlingSticker != null && paramSticker != null) {
            val f1 = getWidth().toFloat()
            val f2 = getHeight().toFloat()
            if (paramBoolean) {
                paramSticker.setMatrix(this.handlingSticker!!.matrix)
                paramSticker.setFlippedVertically(this.handlingSticker!!.isFlippedVertically)
                paramSticker.setFlippedHorizontally(this.handlingSticker!!.isFlippedHorizontally)
            } else {
                this.handlingSticker!!.matrix.reset()
                var f3: Float = (f1 - this.handlingSticker!!.width) / 2.0f
                val f4: Float = (f2 - this.handlingSticker!!.height) / 2.0f
                paramSticker.matrix.postTranslate(f3, f4)
                if (f1 < f2) {
                    if (this.handlingSticker is TextSticker) {
                        f3 = f1 / this.handlingSticker!!.width
                    } else {
                        f3 = f1 / this.handlingSticker!!.drawable!!.getIntrinsicWidth()
                    }
                } else if (this.handlingSticker is TextSticker) {
                    f3 = f2 / this.handlingSticker!!.height
                } else {
                    f3 = f2 / this.handlingSticker!!.drawable!!.getIntrinsicHeight()
                }
                val matrix: Matrix = paramSticker.matrix
                f3 /= 2.0f
                matrix.postScale(f3, f3, f1 / 2.0f, f2 / 2.0f)
            }
            val i = this.stickers.indexOf(this.handlingSticker)
            this.stickers[i] = paramSticker
            this.handlingSticker = paramSticker
            invalidate()
            return true
        }
        return false
    }

    @Throws(Exception::class)
    fun save(): Uri? {
        val bitmap = createBitmap()
        return saveImageToStorage(getContext(), bitmap)
    }

    fun sendToLayer(paramInt1: Int, paramInt2: Int) {
        if (this.stickers.size >= paramInt1 && this.stickers.size >= paramInt2) {
            val sticker: Sticker = this.stickers[paramInt1]
            this.stickers.removeAt(paramInt1)
            this.stickers.add(paramInt2, sticker)
            invalidate()
        }
    }

    fun setCircleRadius(paramInt: Int) {
        this.circleRadius = paramInt
    }

    fun setConstrained(paramBoolean: Boolean): StickerView {
        this.isConstrained = paramBoolean
        postInvalidate()
        return this
    }

    fun setDrawCirclePoint(paramBoolean: Boolean) {
        this.drawCirclePoint = paramBoolean
        this.onMoving = false
    }

    fun setHandlingSticker(paramSticker: Sticker?) {
        this.lastHandlingSticker = this.handlingSticker
        this.handlingSticker = paramSticker
        invalidate()
    }

    fun setIcons(paramList: MutableList<BitmapStickerIcon>) {
        this.icons.clear()
        this.icons.addAll(paramList)
        invalidate()
    }

    fun setLocked(paramBoolean: Boolean): StickerView {
        this.locked = paramBoolean
        invalidate()
        return this
    }

    fun setMinClickDelayTime(paramInt: Int): StickerView {
        this.minClickDelayTime = paramInt
        return this
    }

    fun setOnStickerOperationListener(paramOnStickerOperationListener: OnStickerOperationListener?): StickerView {
        this.onStickerOperationListener = paramOnStickerOperationListener
        return this
    }


    fun setStickerPosition(sticker: Sticker, @Sticker.Position position: Int) {
        val width = getWidth().toFloat()
        val height = getHeight().toFloat()
        var offsetX: Float = width - sticker.width
        var offsetY: Float = height - sticker.height

        if ((position and Sticker.Position.TOP) > 0) {
            offsetY /= 4f
        } else if ((position and Sticker.Position.BOTTOM) > 0) {
            offsetY *= 3f / 4f
        } else {
            offsetY = offsetY / 2f
        }
        if ((position and Sticker.Position.LEFT) > 0) {
            offsetX /= 4f
        } else if ((position and Sticker.Position.RIGHT) > 0) {
            offsetX *= 3f / 4f
        } else {
            offsetX = offsetX / 2f
        }
        sticker.matrix.postTranslate(offsetX, offsetY)
    }

    fun setStickerPosition(sticker: Sticker, normalizedX: Float, normalizedY: Float) {
        // 1. Lấy kích thước thực của khung chứa
        val containerWidth = getWidth().toFloat()
        val containerHeight = getHeight().toFloat()

        // 2. Tính toán kích thước của Sticker
        val stickerWidth = sticker.width.toFloat()
        val stickerHeight = sticker.height.toFloat()

        // 3. Tính toán vị trí tuyệt đối (tọa độ góc trên bên trái của Sticker)
        // Công thức:
        // offsetX = (normalizedX * containerWidth) - (stickerWidth / 2)
        // offsetY = (normalizedY * containerHeight) - (stickerHeight / 2)

        // normalizedX và normalizedY thường biểu thị TÂM của sticker theo tỷ lệ.
        // Do đó, ta cần dịch chuyển lại để có tọa độ góc trên bên trái.

        val offsetX: Float = (normalizedX * containerWidth) - (stickerWidth / 2f)
        val offsetY: Float = (normalizedY * containerHeight) - (stickerHeight / 2f)

        // 4. Đặt lại ma trận biến đổi (Matrix)
        // Cần phải reset ma trận trước khi áp dụng tọa độ mới để tránh tích lũy dịch chuyển.
        sticker.matrix.reset()

        // 5. Áp dụng dịch chuyển
        sticker.matrix.postTranslate(offsetX, offsetY)

        // (Tùy chọn: Nếu cần các phép biến đổi khác như scale/rotate,
        // bạn sẽ thêm chúng vào đây TRƯỚC postTranslate nếu muốn chúng áp dụng cho tâm)
    }

    fun setRandomCurrentSticker() {
        for (sticker in stickers) {
            setRandomStickerPosition(sticker)
        }
    }

    fun setRandomStickerPosition(sticker: Sticker) {
        val width = getWidth().toFloat()
        val height = getHeight().toFloat()

        val x = min(width, height)

        val offsetX: Float = x - sticker.width
        val offsetY: Float = x - sticker.height
        val R = Random()
        val dx = R.nextFloat() * offsetX
        val dy = R.nextFloat() * offsetY

        sticker.matrix.postTranslate(dx, dy)
    }

    fun showLastHandlingSticker() {
        if (this.lastHandlingSticker != null && !this.lastHandlingSticker!!.isShow) {
            this.lastHandlingSticker!!.isShow = true
            invalidate()
        }
    }

    fun setShowIcons(z: Boolean) {
        this.showIcons = z
    }

    fun setShowBorder(z: Boolean) {
        this.showBorder = z
    }

    fun setShowFocus(isForcus: Boolean) {
        this.showBorder = isForcus
        this.showIcons = isForcus
        invalidate()
    }

    fun swapLayers(paramInt1: Int, paramInt2: Int) {
        if (this.stickers.size >= paramInt1 && this.stickers.size >= paramInt2) {
            Collections.swap(this.stickers, paramInt1, paramInt2)
            invalidate()
        }
    }

    fun swapLayers() {
        val sticker: Sticker? = this.currentSticker
        if (sticker != null) {
            val index = stickers.indexOf(sticker)
            val size = stickers.size - 1
            swapLayers(index, size)
        }
    }

    protected fun transformSticker(paramSticker: Sticker?) {
        if (paramSticker == null) {
            Log.e(
                "StickerView",
                "transformSticker: the bitmapSticker is null or the bitmapSticker bitmap is null"
            )
            return
        }
        this.sizeMatrix.reset()
        val f2 = getWidth().toFloat()
        val f3 = getHeight().toFloat()
        var f1: Float = paramSticker.width.toFloat()
        val f4: Float = paramSticker.height.toFloat()
        val f5 = (f2 - f1) / 2.0f
        val f6 = (f3 - f4) / 2.0f
        this.sizeMatrix.postTranslate(f5, f6)
        if (f2 < f3) {
            f1 = f2 / f1
        } else {
            f1 = f3 / f4
        }
        val matrix = this.sizeMatrix
        f1 /= 2.0f
        matrix.postScale(f1, f1, f2 / 2.0f, f3 / 2.0f)
        paramSticker.matrix.reset()
        paramSticker.setMatrix(this.sizeMatrix)
        invalidate()
    }

    fun zoomAndRotateCurrentSticker(paramMotionEvent: MotionEvent) {
        zoomAndRotateSticker(this.handlingSticker, paramMotionEvent)
    }

    fun zoomAndRotateSticker(paramSticker: Sticker?, paramMotionEvent: MotionEvent) {
        if (paramSticker != null) {
            val f1 = calculateDistance(
                this.midPoint.x,
                this.midPoint.y,
                paramMotionEvent.getX(),
                paramMotionEvent.getY()
            )
            val f2 = calculateRotation(
                this.midPoint.x,
                this.midPoint.y,
                paramMotionEvent.getX(),
                paramMotionEvent.getY()
            )
            this.moveMatrix.set(this.downMatrix)
            this.moveMatrix.postScale(
                f1 / this.oldDistance,
                f1 / this.oldDistance,
                this.midPoint.x,
                this.midPoint.y
            )
            this.moveMatrix.postRotate(f2 - this.oldRotation, this.midPoint.x, this.midPoint.y)
            this.handlingSticker?.setMatrix(this.moveMatrix)
        }
    }


    /**
     * Đặt sticker hiện tại (handlingSticker) vào một vị trí ngang cụ thể trong StickerView,
     * đồng thời luôn căn giữa theo chiều dọc.
     *
     * @param position Vị trí ngang mong muốn (LEFT, CENTER, RIGHT).
     */
    fun setStickerHorizontalPosition(@Sticker.Position position: Int) {
        val currentSticker = currentSticker
        if (currentSticker == null || width == 0 || height == 0) {
            return
        }

        val viewWidth = width.toFloat()

        // Khai báo và chuyển đổi lề 10dp sang pixel
        val paddingPx = SystemUtil.dpToPx(context, 20).toFloat()

        // 1. Lấy tọa độ hiển thị của 4 góc (mapped points)
        getStickerPoints(currentSticker, this.bitmapPoints)

        // Tìm minX và maxX để xác định chiều rộng hiển thị hiện tại của sticker
        var minX = this.bitmapPoints[0]
        var maxX = this.bitmapPoints[0]

        for (i in 0..6 step 2) {
            minX = min(minX, this.bitmapPoints[i])
            maxX = maxOf(maxX, this.bitmapPoints[i])
        }

        val stickerDisplayWidth = maxX - minX

        // 2. Tính toán Tâm X mong muốn (Desired Center X)
        val desiredCenterX = when (position) {
            Sticker.Position.LEFT -> {
                // Mép trái của sticker (minX) phải cách lề trái (0) một khoảng 'paddingPx'
                // Tâm X = paddingPx + (stickerDisplayWidth / 2.0f)
                paddingPx + (stickerDisplayWidth / 2.0f)
            }

            Sticker.Position.CENTER -> {
                // Giữ nguyên: Tâm sticker trùng tâm ngang của View
                viewWidth / 2.0f
            }

            Sticker.Position.RIGHT -> {
                // Mép phải của sticker (maxX) phải cách lề phải (viewWidth) một khoảng 'paddingPx'
                // Tâm X = viewWidth - paddingPx - (stickerDisplayWidth / 2.0f)
                viewWidth - paddingPx - (stickerDisplayWidth / 2.0f)
            }

            else -> {
                // Trường hợp không hợp lệ, giữ nguyên vị trí X hiện tại
                minX + (stickerDisplayWidth / 2.0f)
            }
        }

        // 3. Lấy Tâm hiện tại và Tâm Y (giữ nguyên vị trí Y)
        // Tâm X hiện tại của bounding box
        val stickerCurrentCenterX = minX + (stickerDisplayWidth / 2.0f)

        // Lấy Tâm Y hiện tại (giữ nguyên vị trí Y)
        currentSticker.getCenterPoint(this.midPoint)
        val stickerCurrentCenterY = this.midPoint.y

        // 4. Tính toán lượng dịch chuyển (translate)
        val dx = desiredCenterX - stickerCurrentCenterX
        val dy = 0.0f // Giữ nguyên vị trí dọc (Y)

        // 5. Áp dụng dịch chuyển và cập nhật ma trận
        this.moveMatrix.set(currentSticker.matrix)
        this.moveMatrix.postTranslate(dx, dy)
        currentSticker.setMatrix(this.moveMatrix)

        // Thông báo sự thay đổi và vẽ lại
        this.onStickerOperationListener?.onStickerDragFinished(currentSticker)
        invalidate()
    }

    @Retention(AnnotationRetention.SOURCE)
    annotation class ActionMode {
        companion object {
            const val CLICK: Int = 4

            const val DRAG: Int = 1

            const val ICON: Int = 3

            const val NONE: Int = 0

            const val ZOOM_WITH_TWO_FINGER: Int = 2
        }
    }

    @Retention(AnnotationRetention.SOURCE)
    annotation class Flip

    interface OnStickerOperationListener {
        fun onStickerAdded(param1Sticker: Sticker)

        fun onStickerClicked(param1Sticker: Sticker)

        fun onStickerDeleted(param1Sticker: Sticker)

        fun onTextStickerEdit(param1Sticker: Sticker)

        fun onStickerDoubleTapped(param1Sticker: Sticker)

        fun onStickerDragFinished(param1Sticker: Sticker)

        fun onStickerFlipped(param1Sticker: Sticker)

        fun onStickerTouchOutside(param1Sticker: Sticker?)

        fun onStickerTouchedDown(param1Sticker: Sticker)

        fun onStickerZoomFinished(param1Sticker: Sticker)

        fun onTouchDownForBeauty(param1Float1: Float, param1Float2: Float)

        fun onTouchDragForBeauty(param1Float1: Float, param1Float2: Float)

        fun onTouchUpForBeauty(param1Float1: Float, param1Float2: Float)
    }

    companion object {
        private const val DEFAULT_MIN_CLICK_DELAY_TIME = 200
        const val FLIP_HORIZONTALLY: Int = 1
        const val FLIP_VERTICALLY: Int = 2
        private const val TAG = "StickerView"
    }
}

class DeleteIconEvent : StickerIconEvent {
    override fun onActionDown(paramStickerView: StickerView?, paramMotionEvent: MotionEvent?) {
    }

    override fun onActionMove(paramStickerView: StickerView?, paramMotionEvent: MotionEvent?) {
    }

    override fun onActionUp(
        paramStickerView: StickerView?,
        paramMotionEvent: MotionEvent?
    ) {
        paramStickerView?.removeCurrentSticker()
    }
}

interface StickerIconEvent {
    fun onActionDown(paramStickerView: StickerView?, paramMotionEvent: MotionEvent?)

    fun onActionMove(paramStickerView: StickerView?, paramMotionEvent: MotionEvent?)

    fun onActionUp(paramStickerView: StickerView?, paramMotionEvent: MotionEvent?)
}


@Throws(IOException::class)
fun saveImageToStorage(context: Context, bitmap: Bitmap): Uri? {
    val imageOutStream: OutputStream?
    val uri: Uri?
    val filename = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(Date()) + ".png"
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val values = ContentValues()
        val path = Environment.DIRECTORY_DCIM + "/" + FOLDER_SDK
        values.put(MediaStore.Images.Media.DISPLAY_NAME, filename)
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png")
        values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis())
        values.put(MediaStore.Images.Media.RELATIVE_PATH, path)
        val contentResolver = context.getContentResolver()

        uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        imageOutStream = contentResolver.openOutputStream(uri!!)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, imageOutStream!!)
    } else {
        val parentFilePath: String = getRootFolder()
        val file = File(parentFilePath)
        if (!file.exists()) file.mkdirs()
        val image = File(file, filename)
        imageOutStream = FileOutputStream(image)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, imageOutStream)
        uri = addImageToGallery(image.getPath(), context)
    }

    return uri
}

fun addImageToGallery(filePath: String?, context: Context): Uri? {
    val values = ContentValues()

    values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis())
    values.put(MediaStore.Images.Media.MIME_TYPE, "image/png")
    values.put(MediaStore.MediaColumns.DATA, filePath)

    return context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
}


const val FOLDER_SDK = "PhotoCollage"

fun getRootFolder(): String {
    return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
        .getAbsolutePath() + "/" + FOLDER_SDK
}

class FlipHorizontallyEvent : AbstractFlipEvent() {
    override val flipDirection: Int
        get() = 1
}

abstract class AbstractFlipEvent : StickerIconEvent {
    protected abstract val flipDirection: Int

    override fun onActionDown(paramStickerView: StickerView?, paramMotionEvent: MotionEvent?) {
    }

    override fun onActionMove(paramStickerView: StickerView?, paramMotionEvent: MotionEvent?) {
    }

    override fun onActionUp(paramStickerView: StickerView?, paramMotionEvent: MotionEvent?) {
        paramStickerView?.flipCurrentSticker(this.flipDirection)
    }
}

class EditTextIconEvent : StickerIconEvent {
    override fun onActionDown(paramStickerView: StickerView?, paramMotionEvent: MotionEvent?) {
    }

    override fun onActionMove(paramStickerView: StickerView?, paramMotionEvent: MotionEvent?) {
    }

    override fun onActionUp(paramStickerView: StickerView?, paramMotionEvent: MotionEvent?) {
        paramStickerView?.editTextSticker()
    }
}

class ZoomIconEvent : StickerIconEvent {
    override fun onActionDown(paramStickerView: StickerView?, paramMotionEvent: MotionEvent?) {
    }

    override fun onActionMove(paramStickerView: StickerView?, paramMotionEvent: MotionEvent?) {
        paramStickerView?.zoomAndRotateCurrentSticker(paramMotionEvent!!)
    }

    override fun onActionUp(paramStickerView: StickerView?, paramMotionEvent: MotionEvent?) {
        if (paramStickerView?.onStickerOperationListener != null) {
            paramStickerView.onStickerOperationListener?.onStickerZoomFinished(paramStickerView.currentSticker!!)
        }
    }
}


class ResetIconEvent : StickerIconEvent {
    public override fun onActionDown(stickerView: StickerView?, event: MotionEvent?) {
    }

    public override fun onActionMove(stickerView: StickerView?, event: MotionEvent?) {
    }

    public override fun onActionUp(stickerView: StickerView?, event: MotionEvent?) {
        stickerView?.resetTextRotation()
    }
}

//fun loadBitmapFromAssets(context: Context, str: String): Bitmap? {
//    val inputStream: InputStream?
//    try {
//        inputStream = context.assets.open(str)
//        val decodeStream = BitmapFactory.decodeStream(inputStream)
//        inputStream.close()
//        return decodeStream
//    } catch (e: java.lang.Exception) {
//        return null
//    }
//}