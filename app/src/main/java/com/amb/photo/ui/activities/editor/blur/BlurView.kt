package com.amb.photo.ui.activities.editor.blur

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.os.Build
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.view.MotionEventCompat
import androidx.core.view.ViewCompat
import com.amb.photo.ui.activities.editor.sticker.lib.Sticker
import org.wysaid.common.SharedContext
import org.wysaid.nativePort.CGEImageHandler
import java.text.MessageFormat
import java.util.Random
import java.util.Stack
import kotlin.math.atan2
import kotlin.math.sqrt

@Composable
fun BlurView(
    modifier: Modifier,
    blurView: BlurView,
    bitmap: Bitmap,
    intensity: Float,
    scaleType: ImageView.ScaleType = ImageView.ScaleType.FIT_CENTER
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            blurView.setImageBitmap(
                getBlurImageFromBitmap(bitmap, 3.0f)
            )
            blurView.scaleType = scaleType
            blurView
        },
        update = { view ->
            val bitmap = getBlurImageFromBitmap(bitmap, intensity / 10)
            view.setImageBitmap(bitmap)
        }
    )
}

fun BlurView.tabShape() {
    refreshDrawableState()
    setLayerType(View.LAYER_TYPE_SOFTWARE, null as Paint?) //1
    currentSplashMode = 0
    invalidate()
}

fun BlurView.tabBrush() {
    refreshDrawableState()
    setLayerType(View.LAYER_TYPE_SOFTWARE, null as Paint?) //1
    currentSplashMode = 1
    invalidate()
}


fun getBlurImageFromBitmap(bitmap: Bitmap?, f: Float, total: Float = 10.0f): Bitmap? {
    val create = SharedContext.create()
    create.makeCurrent()
    val cGEImageHandler = CGEImageHandler()
    cGEImageHandler.initWithBitmap(bitmap)
    cGEImageHandler.setFilterWithConfig(
        MessageFormat.format(
            "@blur lerp {0}",
            (f / total).toString() + ""
        )
    )
    cGEImageHandler.processFilters()
    val resultBitmap = cGEImageHandler.resultBitmap
    create.release()
    return resultBitmap
}

class BlurView : AppCompatImageView {
    private var bitmap: Bitmap? = null
    private var brushBitmapSize = 100

    private val currentCenterPoint = PointF()
    private var currentMode = 0
    var currentSplashMode: Int = 0
    private var currentX = 0f
    private var currentY = 0f
    private val downMatrix = Matrix()
    private val lstPoints: Stack<LinePath?> = Stack<LinePath?>()
    private var mDrawPaint: Paint? = null
    private var mPath: Path? = null
    private val mPoints: Stack<LinePath?> = Stack<LinePath?>()
    private val mRedoPaths: Stack<LinePath?> = Stack<LinePath?>()
    private var mTouchX = 0f
    private var mTouchY = 0f
    private var midPoint = PointF()
    private val moveMatrix = Matrix()
    private var oldDistance = 0.0f
    private var oldRotation = 0.0f
    private var paintCircle: Paint? = null
    private val point = FloatArray(2)
    private var showTouchIcon = false
    var sticker: Sticker? = null
        private set
    private val tmp = FloatArray(2)


    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attributeSet: AttributeSet?) : super(context, attributeSet) {
        init()
    }

    constructor(context: Context, attributeSet: AttributeSet?, i: Int) : super(
        context,
        attributeSet,
        i
    ) {
        init()
    }

    override fun setImageBitmap(bitmap2: Bitmap?) {
        super.setImageBitmap(bitmap2)
        setBitmap(bitmap2)
    }

    fun setBitmap(bitmap2: Bitmap?) {
        this.bitmap = bitmap2
    }

    private fun init() {
        this.mDrawPaint = Paint()
        this.mDrawPaint!!.setAntiAlias(true)
        this.mDrawPaint!!.setDither(true)
        this.mDrawPaint!!.setStyle(Paint.Style.FILL)
        this.mDrawPaint!!.setStrokeJoin(Paint.Join.ROUND)
        this.mDrawPaint!!.setStrokeCap(Paint.Cap.ROUND)
        this.mDrawPaint!!.setStrokeWidth(this.brushBitmapSize.toFloat())
        this.mDrawPaint!!.setMaskFilter(BlurMaskFilter(20.0f, BlurMaskFilter.Blur.NORMAL))
        this.mDrawPaint!!.setXfermode(PorterDuffXfermode(PorterDuff.Mode.DST_OUT))
        this.mDrawPaint!!.setStyle(Paint.Style.STROKE)
        this.paintCircle = Paint()
        this.paintCircle!!.setAntiAlias(true)
        this.paintCircle!!.setDither(true)
        this.paintCircle!!.setColor(ContextCompat.getColor(getContext(), android.R.color.black))
        this.paintCircle!!.setStrokeWidth(SystemUtil.dpToPx(getContext(), 2).toFloat())
        this.paintCircle!!.setStyle(Paint.Style.STROKE)
        this.mPath = Path()
    }

    fun updateBrush() {
        this.mPath = Path()
        this.mDrawPaint!!.setAntiAlias(true)
        this.mDrawPaint!!.setDither(true)
        this.mDrawPaint!!.setStyle(Paint.Style.FILL)
        this.mDrawPaint!!.setStrokeJoin(Paint.Join.ROUND)
        this.mDrawPaint!!.setStrokeCap(Paint.Cap.ROUND)
        this.mDrawPaint!!.setStrokeWidth(this.brushBitmapSize.toFloat())
        this.mDrawPaint!!.setXfermode(PorterDuffXfermode(PorterDuff.Mode.DST_OUT))
        this.mDrawPaint!!.setStyle(Paint.Style.STROKE)
        this.showTouchIcon = false
        invalidate()
    }

    fun addSticker(sticker2: Sticker) {
        addSticker(sticker2, 1)
    }

    fun addSticker(sticker2: Sticker, i: Int) {
        if (ViewCompat.isLaidOut(this)) {
            addStickerImmediately(sticker2, i)
        } else {
            post(Runnable { this@BlurView.addStickerImmediately(sticker2, i) })
        }
    }


    fun addStickerImmediately(sticker2: Sticker, i: Int) {
        this.sticker = sticker2
        setStickerPosition(sticker2, i)
        invalidate()
    }


    fun setStickerPosition(sticker2: Sticker, i: Int) {
        val f: Float
        val width = getWidth().toFloat()
        val height = getHeight().toFloat()
        if (width > height) {
            f = ((height * 4.0f) / 5.0f) / (sticker2.height.toFloat())
        } else {
            f = ((width * 4.0f) / 5.0f) / (sticker2.width.toFloat())
        }
        this.midPoint.set(0.0f, 0.0f)
        this.downMatrix.reset()
        this.moveMatrix.set(this.downMatrix)
        this.moveMatrix.postScale(f, f)
        this.moveMatrix.postRotate(
            (Random().nextInt(20) - 10).toFloat(),
            this.midPoint.x,
            this.midPoint.y
        )
        val width2 = width - ((((sticker2.width.toFloat()) * f).toInt()).toFloat())
        val height2 = height - ((((sticker2.height.toFloat()) * f).toInt()).toFloat())
        this.moveMatrix.postTranslate(
            if ((i and 4) > 0) width2 / 4.0f else if ((i and 8) > 0) width2 * 0.75f else width2 / 2.0f,
            if ((i and 2) > 0) height2 / 4.0f else if ((i and 16) > 0) height2 * 0.75f else height2 / 2.0f
        )
        sticker2.setMatrix(this.moveMatrix)
    }


    @SuppressLint("CanvasSize")
    public override fun onDraw(canvas: Canvas) {
        if (this.bitmap != null && !this.bitmap!!.isRecycled()) {
            super.onDraw(canvas)
            if (this.currentSplashMode == 0) {
                drawStickers(canvas)
                return
            }
            val it: MutableIterator<*> = this.mPoints.iterator()
            while (it.hasNext()) {
                val linePath: LinePath = it.next() as LinePath
                canvas.drawPath(linePath.drawPath!!, linePath.drawPaint!!)
            }
            canvas.drawPath(this.mPath!!, this.mDrawPaint!!)
            if (this.showTouchIcon) {
                canvas.drawCircle(
                    this.currentX,
                    this.currentY,
                    (this.brushBitmapSize / 2).toFloat(),
                    this.paintCircle!!
                )
            }
        }
    }


    fun drawStickers(canvas: Canvas?) {
        if (this.sticker != null && this.sticker!!.isShow) {
            this.sticker!!.draw(canvas!!)
        }
        invalidate()
    }


    fun calculateDistance(f: Float, f2: Float, f3: Float, f4: Float): Float {
        val d = (f - f3).toDouble()
        val d2 = (f2 - f4).toDouble()
        return sqrt((d * d) + (d2 * d2)).toFloat()
    }


    fun calculateDistance(motionEvent: MotionEvent?): Float {
        if (motionEvent == null || motionEvent.getPointerCount() < 2) {
            return 0.0f
        }
        return calculateDistance(
            motionEvent.getX(0),
            motionEvent.getY(0),
            motionEvent.getX(1),
            motionEvent.getY(1)
        )
    }


    fun calculateRotation(motionEvent: MotionEvent?): Float {
        if (motionEvent == null || motionEvent.getPointerCount() < 2) {
            return 0.0f
        }
        return calculateRotation(
            motionEvent.getX(0),
            motionEvent.getY(0),
            motionEvent.getX(1),
            motionEvent.getY(1)
        )
    }


    fun calculateRotation(f: Float, f2: Float, f3: Float, f4: Float): Float {
        return Math.toDegrees(atan2((f2 - f4).toDouble(), (f - f3).toDouble())).toFloat()
    }


    fun calculateMidPoint(motionEvent: MotionEvent?): PointF {
        if (motionEvent == null || motionEvent.getPointerCount() < 2) {
            this.midPoint.set(0.0f, 0.0f)
            return this.midPoint
        }
        this.midPoint.set(
            (motionEvent.getX(0) + motionEvent.getX(1)) / 2.0f,
            (motionEvent.getY(0) + motionEvent.getY(1)) / 2.0f
        )
        return this.midPoint
    }


    fun calculateMidPoint(): PointF {
        if (this.sticker != null) {
            this.sticker!!.getMappedCenterPoint(this.midPoint, this.point, this.tmp)
        }
        return this.midPoint
    }


    fun isInStickerArea(sticker2: Sticker, f: Float, f2: Float): Boolean {
        if (sticker2 == null) return false

        this.tmp[0] = f
        this.tmp[1] = f2
        return sticker2.contains(this.tmp)
    }

    override fun onTouchEvent(motionEvent: MotionEvent): Boolean {
        val actionMasked = MotionEventCompat.getActionMasked(motionEvent)
        //todo motionEvent.getAction();

        val x = motionEvent.getX()
        val y = motionEvent.getY()
        this.currentX = x
        this.currentY = y
        when (actionMasked) {
            0 -> if (!onTouchDown(x, y)) {
                invalidate()
                return false
            }

            1 -> onTouchUp(motionEvent)
            2 -> {
                handleCurrentMode(x, y, motionEvent)
                invalidate()
            }

            5 -> {
                this.oldDistance = calculateDistance(motionEvent)
                this.oldRotation = calculateRotation(motionEvent)
                this.midPoint = calculateMidPoint(motionEvent)
                if (this.sticker != null && isInStickerArea(
                        this.sticker!!,
                        motionEvent.getX(1),
                        motionEvent.getY(1)
                    )
                ) {
                    this.currentMode = 2
                }
            }

            6 -> this.currentMode = 0
        }
        return true
    }


    @Synchronized
    fun handleCurrentMode(f: Float, f2: Float, motionEvent: MotionEvent) {
        if (this.currentSplashMode == 0) {
            val i = this.currentMode
            if (i != 4) {
                when (i) {
                    0 -> {}
                    1 -> if (this.sticker != null) {
                        this.moveMatrix.set(this.downMatrix)
                        this.moveMatrix.postTranslate(
                            motionEvent.getX() - this.mTouchX,
                            motionEvent.getY() - this.mTouchY
                        )
                        this.sticker!!.setMatrix(this.moveMatrix)
//                        break
                    }

                    2 -> if (this.sticker != null) {
                        val calculateDistance = calculateDistance(motionEvent)
                        val calculateRotation = calculateRotation(motionEvent)
                        this.moveMatrix.set(this.downMatrix)
                        this.moveMatrix.postScale(
                            calculateDistance / this.oldDistance,
                            calculateDistance / this.oldDistance,
                            this.midPoint.x,
                            this.midPoint.y
                        )
                        this.moveMatrix.postRotate(
                            calculateRotation - this.oldRotation,
                            this.midPoint.x,
                            this.midPoint.y
                        )
                        this.sticker!!.setMatrix(this.moveMatrix)
//                        break
                    }
                }
            }
        } else {
            this.mPath!!.quadTo(
                this.mTouchX,
                this.mTouchY,
                (this.mTouchX + f) / 2.0f,
                (this.mTouchY + f2) / 2.0f
            )
            this.mTouchX = f
            this.mTouchY = f2
        }
    }

    fun setBrushBitmapSize(i: Int) {
        this.brushBitmapSize = i
        this.mDrawPaint!!.setStrokeWidth(i.toFloat())
        this.showTouchIcon = true
        this.currentX = (getWidth() / 2).toFloat()
        this.currentY = (getHeight() / 2).toFloat()
        invalidate()
    }


    fun findHandlingSticker(): Sticker? {
        if (this.sticker == null) return null
        if (isInStickerArea(this.sticker!!, this.mTouchX, this.mTouchY)) {
            return this.sticker
        }
        return null
    }


    fun onTouchDown(f: Float, f2: Float): Boolean {
        this.currentMode = 1
        this.mTouchX = f
        this.mTouchY = f2
        this.currentX = f
        this.currentY = f2
        if (this.currentSplashMode == 0) {
            this.midPoint = calculateMidPoint()
            this.oldDistance =
                calculateDistance(this.midPoint.x, this.midPoint.y, this.mTouchX, this.mTouchY)
            this.oldRotation =
                calculateRotation(this.midPoint.x, this.midPoint.y, this.mTouchX, this.mTouchY)
            val findHandlingSticker = findHandlingSticker()
            if (findHandlingSticker != null) {
                this.downMatrix.set(this.sticker?.matrix)
            }
            if (findHandlingSticker == null) {
                return false
            }
        } else {
            this.showTouchIcon = true
            this.mRedoPaths.clear()
            this.mPath!!.reset()
            this.mPath!!.moveTo(f, f2)
        }
        invalidate()
        return true
    }


    fun onTouchUp(motionEvent: MotionEvent) {
        this.showTouchIcon = false
        if (this.currentSplashMode == 0) {
            this.currentMode = 0
        } else {
            val linePath: LinePath = LinePath(this.mPath, this.mDrawPaint)
            this.mPoints.push(linePath)
            this.lstPoints.push(linePath)
            this.mPath = Path()
        }
        invalidate()
    }


    fun undo(): Boolean {
        if (!this.lstPoints.empty()) {
            val pop: LinePath? = this.lstPoints.pop()
            this.mRedoPaths.push(pop)
            this.mPoints.remove(pop)
            invalidate()
        }
        return !this.lstPoints.empty()
    }


    fun redo(): Boolean {
        if (!this.mRedoPaths.empty()) {
            val pop: LinePath? = this.mRedoPaths.pop()
            this.mPoints.push(pop)
            this.lstPoints.push(pop)
            invalidate()
        }
        return !this.mRedoPaths.empty()
    }



    fun getBitmap(bitmap2: Bitmap): Bitmap {
        val bitmap1 = getSoftwareBitmap(this.bitmap!!)
        val bmp2 = getSoftwareBitmap(bitmap2)

        val width = getWidth()
        val height = getHeight()
        val createBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(createBitmap)
        canvas.drawBitmap(
            bitmap1,
            null,
            RectF(0f, 0f, width.toFloat(), height.toFloat()),
            null
        )

        if (this.currentSplashMode == 0) {
            drawStickers(canvas)
        } else {
            for (linePath in mPoints) {
                canvas.drawPath(linePath?.drawPath!!, linePath.drawPaint!!)
            }
        }

        val createBitmap2 = Bitmap.createBitmap(bmp2.width, bmp2.height, Bitmap.Config.ARGB_8888)
        val canvas2 = Canvas(createBitmap2)
        canvas2.drawBitmap(
            bmp2,
            null,
            RectF(0f, 0f, bmp2.width.toFloat(), bmp2.height.toFloat()),
            null
        )
        canvas2.drawBitmap(
            createBitmap,
            null,
            RectF(0f, 0f, bmp2.width.toFloat(), bmp2.height.toFloat()),
            null
        )
        return createBitmap2
    }

    companion object {
        const val DRAW: Int = 1
        const val SHAPE: Int = 0
    }
}


class LinePath(drawPath: Path?, drawPaints: Paint?) {
    val drawPaint: Paint?
    val drawPath: Path?

    init {
        this.drawPaint = Paint(drawPaints)
        this.drawPath = Path(drawPath)
    }
}


object SystemUtil {
    fun dpToPx(context: Context, i: Int): Int {
        return ((i.toFloat()) * context.getResources().getDisplayMetrics().density).toInt()
    }


    fun logs(tag: String?, log: String?) {
//        if (sh.mInstance != null && sh.mInstance.isDebugLog()
//                && tag !=null && log!=null) {
//        if (Constants.LOG_DEBUG && tag != null && log != null) {
//            Log.e(tag, log)
//        }
    }

    fun logs(log: String?) {
        val tag = "lg"
        logs(tag, log)
    }
}

fun getSoftwareBitmap(src: Bitmap): Bitmap {
    // Nếu bitmap đã là software, trả luôn
    if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            src.config != Bitmap.Config.HARDWARE
        } else {
            return src
        }
    ) return src

    val bmp = src.copy(Bitmap.Config.ARGB_8888, true) // copy sang software
    return bmp
}