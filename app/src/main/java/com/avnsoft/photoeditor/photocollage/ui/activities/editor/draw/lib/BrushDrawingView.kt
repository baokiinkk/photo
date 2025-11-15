package com.avnsoft.photoeditor.photocollage.ui.activities.editor.draw.lib

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.IntRange
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.blur.SystemUtil
import java.util.Random
import java.util.Stack
import kotlin.math.abs

interface BrushViewChangeListener {
    fun onStartDrawing()

    fun onStopDrawing()

    fun onViewAdd(brushDrawingView: BrushDrawingView?)

    fun onViewRemoved(brushDrawingView: BrushDrawingView?)
}

class Point {
    var linePath: LinePath? = null
    var vector2: Vector2? = null

    internal constructor(linePath2: LinePath?) {
        this.linePath = linePath2
    }

    internal constructor(vector22: Vector2?) {
        this.vector2 = vector22
    }
}

class Vector2 internal constructor(
    var x: Int,
    var y: Int,
    var x1: Int,
    var y1: Int,
    var drawableIndex: Int,
    var bitmap: Bitmap?
)

class LinePath(drawPath: Path?, drawPaints: Paint?) {
    val drawPaint: Paint?
    val drawPath: Path?

    init {
        this.drawPaint = Paint(drawPaints)
        this.drawPath = Path(drawPath)
    }
}

class BrushDrawingView : View {
    private var bitmapPaint: Paint? = null
    private var brushBitmapSize: Int
    private val currentBitmapPoint: MutableList<Point>
    private var currentMagicBrush: DrawBitmapModel? = null
    private val distance: Int
    private var drawMode = 0
    private val lstPoints: Stack<MutableList<Point>>
    private var mBrushDrawMode = false
    var eraserSize: Float
        private set
    private var mBrushSize: Float
    private var mBrushViewChangeListener: BrushViewChangeListener? = null
    private var mDrawCanvas: Canvas? = null
    private var mDrawPaint: Paint? = null
    private var mDrawPaintBlur: Paint? = null
    private var mLastTouchX = 0f
    private var mLastTouchY = 0f
    private var mOpacity: Int
    private var mPath: Path? = null
    private val mPoints: Stack<Point>
    private val mRedoPaths: Stack<MutableList<Point>>
    private var mTouchX = 0f
    private var mTouchY = 0f
    private val tempRect: Rect

    @JvmOverloads
    constructor(context: Context?, attributeSet: AttributeSet? = null as AttributeSet?) : super(
        context,
        attributeSet
    ) {
        this.mBrushSize = 25.0f
        this.eraserSize = 50.0f
        this.mOpacity = 255
        this.mPoints = Stack<Point>()
        this.lstPoints = Stack<MutableList<Point>>()
        this.mRedoPaths = Stack<MutableList<Point>>()
        this.brushBitmapSize = SystemUtil.dpToPx(getContext(), 25)
        this.distance = SystemUtil.dpToPx(getContext(), 3)
        this.currentBitmapPoint = ArrayList()
        this.tempRect = Rect()
        setupBrushDrawing()
    }

    constructor(context: Context?, attributeSet: AttributeSet?, i: Int) : super(
        context,
        attributeSet,
        i
    ) {
        this.mBrushSize = 25.0f
        this.eraserSize = 50.0f
        this.mOpacity = 255
        this.mPoints = Stack<Point>()
        this.lstPoints = Stack<MutableList<Point>>()
        this.mRedoPaths = Stack<MutableList<Point>>()
        this.brushBitmapSize = SystemUtil.dpToPx(getContext(), 25)
        this.distance = SystemUtil.dpToPx(getContext(), 3)
        this.currentBitmapPoint = ArrayList()
        this.tempRect = Rect()
        setupBrushDrawing()
    }

    fun setCurrentMagicBrush(drawBitmapModel: DrawBitmapModel) {
        this.currentMagicBrush = drawBitmapModel
    }

    fun setDrawMode(i: Int) {
        this.drawMode = i
        if (this.drawMode == 2) {
            this.mDrawPaint!!.setColor(-1)
            this.mDrawPaintBlur!!.setColor(Color.parseColor(DrawAsset.lstColorForBrush().get(0)))
            refreshBrushDrawing()
            return
        }
        this.mDrawPaint!!.setColor(Color.parseColor(DrawAsset.lstColorForBrush().get(0)))
        refreshBrushDrawing()
    }

    private fun setupBrushDrawing() {
        setLayerType(LAYER_TYPE_HARDWARE, null)
        setLayerType(LAYER_TYPE_SOFTWARE, null)
        this.mDrawPaint = Paint()
        this.mPath = Path()
        this.mDrawPaint!!.setAntiAlias(true)
        this.mDrawPaint!!.setDither(true)
        this.mDrawPaint!!.setColor(Color.parseColor(DrawAsset.lstColorForBrush().get(0)))
        this.mDrawPaint!!.setStyle(Paint.Style.FILL)
        this.mDrawPaint!!.setStrokeJoin(Paint.Join.ROUND)
        this.mDrawPaint!!.setStrokeCap(Paint.Cap.ROUND)
        this.mDrawPaint!!.setStrokeWidth(this.mBrushSize)
        this.mDrawPaint!!.setAlpha(this.mOpacity)
        this.mDrawPaint!!.setXfermode(PorterDuffXfermode(PorterDuff.Mode.SRC_OVER))
        this.mDrawPaintBlur = Paint()
        this.mDrawPaintBlur!!.setAntiAlias(true)
        this.mDrawPaintBlur!!.setDither(true)
        this.mDrawPaintBlur!!.setStyle(Paint.Style.STROKE)
        this.mDrawPaintBlur!!.setStrokeJoin(Paint.Join.ROUND)
        this.mDrawPaintBlur!!.setMaskFilter(BlurMaskFilter(25.0f, BlurMaskFilter.Blur.OUTER))
        this.mDrawPaintBlur!!.setStrokeCap(Paint.Cap.ROUND)
        this.mDrawPaintBlur!!.setStrokeWidth(this.mBrushSize * 1.1f)
        this.mDrawPaintBlur!!.setColor(Color.parseColor(DrawAsset.lstColorForBrush().get(0)))
        this.mDrawPaintBlur!!.setAlpha(this.mOpacity)
        this.mDrawPaintBlur!!.setXfermode(PorterDuffXfermode(PorterDuff.Mode.SRC_OVER))
        this.bitmapPaint = Paint()
        this.bitmapPaint!!.setStyle(Paint.Style.FILL)
        this.bitmapPaint!!.setStrokeJoin(Paint.Join.ROUND)
        this.bitmapPaint!!.setStrokeCap(Paint.Cap.ROUND)
        this.bitmapPaint!!.setStrokeWidth(this.mBrushSize)
        this.bitmapPaint!!.setXfermode(PorterDuffXfermode(PorterDuff.Mode.SRC_OVER))
        setVisibility(GONE)
    }

    private fun refreshBrushDrawing() {
        this.mBrushDrawMode = true
        this.mPath = Path()
        this.mDrawPaint!!.setAntiAlias(true)
        this.mDrawPaint!!.setDither(true)
        this.mDrawPaint!!.setStyle(Paint.Style.STROKE)
        this.mDrawPaint!!.setStrokeJoin(Paint.Join.ROUND)
        this.mDrawPaint!!.setStrokeCap(Paint.Cap.ROUND)
        this.mDrawPaint!!.setStrokeWidth(this.mBrushSize)
        this.mDrawPaint!!.setAlpha(this.mOpacity)
        this.mDrawPaint!!.setXfermode(PorterDuffXfermode(PorterDuff.Mode.SRC_OVER))
        this.mDrawPaintBlur!!.setAntiAlias(true)
        this.mDrawPaintBlur!!.setDither(true)
        this.mDrawPaintBlur!!.setStyle(Paint.Style.STROKE)
        this.mDrawPaintBlur!!.setStrokeJoin(Paint.Join.ROUND)
        this.mDrawPaintBlur!!.setMaskFilter(BlurMaskFilter(30.0f, BlurMaskFilter.Blur.OUTER))
        this.mDrawPaintBlur!!.setStrokeCap(Paint.Cap.ROUND)
        this.mDrawPaintBlur!!.setStrokeWidth(this.mBrushSize * 1.1f)
        this.mDrawPaintBlur!!.setAlpha(this.mOpacity)
        this.mDrawPaintBlur!!.setXfermode(PorterDuffXfermode(PorterDuff.Mode.SRC_OVER))
        this.bitmapPaint!!.setStyle(Paint.Style.FILL)
        this.bitmapPaint!!.setStrokeJoin(Paint.Join.ROUND)
        this.bitmapPaint!!.setStrokeCap(Paint.Cap.ROUND)
        this.bitmapPaint!!.setStrokeWidth(this.mBrushSize)
        this.bitmapPaint!!.setXfermode(PorterDuffXfermode(PorterDuff.Mode.SRC_OVER))
    }


    fun brushEraser() {
        this.mBrushDrawMode = true
        this.drawMode = 4
        this.mDrawPaint!!.setStrokeWidth(this.eraserSize)
        this.mDrawPaint!!.setXfermode(PorterDuffXfermode(PorterDuff.Mode.CLEAR))
    }


    fun setOpacity(@IntRange(from = 0, to = 255) i: Int) {
        this.mOpacity = i
        this.brushDrawingMode = true
    }


    var brushDrawingMode: Boolean
        get() = this.mBrushDrawMode
        set(z) {
            this.mBrushDrawMode = z
            if (z) {
                setVisibility(VISIBLE)
                refreshBrushDrawing()
            }
        }


    fun setBrushEraserSize(f: Float) {
        this.eraserSize = f
        this.brushDrawingMode = true
    }


    fun setBrushEraserColor(@ColorInt i: Int) {
        this.mDrawPaint!!.setColor(i)
        this.brushDrawingMode = true
    }


    var brushSize: Float
        get() = this.mBrushSize
        set(f) {
            if (this.drawMode == 3) {
                this.brushBitmapSize = SystemUtil.dpToPx(getContext(), f.toInt())
                return
            }
            this.mBrushSize = f
            this.brushDrawingMode = true
        }


    var brushColor: Int
        get() = this.mDrawPaint!!.getColor()
        set(i) {
            if (this.drawMode == 1) {
                this.mDrawPaint!!.setColor(i)
            } else if (this.drawMode == 2) {
                this.mDrawPaintBlur!!.setColor(i)
            }
            this.brushDrawingMode = true
        }


    fun clearAll() {
        this.mRedoPaths.clear()
        this.mPoints.clear()
        this.lstPoints.clear()
        for (next in DrawAsset.drawBitmapModels) {
            if (next.isLoadBitmap) {
                next.clearBitmap()
                next.isLoadBitmap = false
            }
        }
        if (this.mDrawCanvas != null) {
            this.mDrawCanvas!!.drawColor(0, PorterDuff.Mode.CLEAR)
        }
        invalidate()
    }


    fun setBrushViewChangeListener(brushViewChangeListener: BrushViewChangeListener?) {
        this.mBrushViewChangeListener = brushViewChangeListener
    }


    public override fun onSizeChanged(i: Int, i2: Int, i3: Int, i4: Int) {
        super.onSizeChanged(i, i2, i3, i4)
        if (i > 0 && i2 > 0) {
            this.mDrawCanvas = Canvas(Bitmap.createBitmap(i, i2, Bitmap.Config.ARGB_8888))
        }
    }


    public override fun onDraw(canvas: Canvas) {
        val it: MutableIterator<*> = this.mPoints.iterator()
        while (it.hasNext()) {
            val point: Point = it.next() as Point
            if (point.vector2 != null) {
                this.tempRect.set(
                    point.vector2!!.x,
                    point.vector2!!.y,
                    point.vector2!!.x1,
                    point.vector2!!.y1
                )
                if (point.vector2?.bitmap != null) {
                    canvas.drawBitmap(
                        point.vector2?.bitmap!!,
                        null,
                        this.tempRect,
                        this.bitmapPaint
                    )
                }
            } else if (point.linePath?.drawPath != null && point.linePath?.drawPaint != null) {
                canvas.drawPath(point.linePath?.drawPath!!, point.linePath?.drawPaint!!)
            }
        }
        if (this.drawMode == 2) {
            canvas.drawPath(this.mPath!!, this.mDrawPaintBlur!!)
        }
        canvas.drawPath(this.mPath!!, this.mDrawPaint!!)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(motionEvent: MotionEvent): Boolean {
        if (!this.mBrushDrawMode) {
            return false
        }
        val x = motionEvent.getX().toInt()
        val y = motionEvent.getY().toInt()
        when (motionEvent.getAction()) {
            0 -> touchStart(x.toFloat(), y.toFloat())
            1 -> touchUp()
            2 -> touchMove(x, y)
        }
        invalidate()
        return true
    }

    //    public static class LinePath {
    //        private Paint mDrawPaint;
    //        private Path mDrawPath;
    //
    //        public LinePath(Path path, Paint paint) {
    //            this.mDrawPaint = new Paint(paint);
    //            this.mDrawPath = new Path(path);
    //        }
    //
    //        public Paint getDrawPaint() {
    //            return this.mDrawPaint;
    //        }
    //
    //        public Path getDrawPath() {
    //            return this.mDrawPath;
    //        }
    //    }
    fun undo(): Boolean {
        if (!this.lstPoints.empty()) {
            val pop: MutableList<Point> = this.lstPoints.pop()
            this.mRedoPaths.push(pop)
            this.mPoints.removeAll(pop)
            invalidate()
        }
        if (this.mBrushViewChangeListener != null) {
            this.mBrushViewChangeListener!!.onViewRemoved(this)
        }
        return !this.lstPoints.empty()
    }


    fun redo(): Boolean {
        if (!this.mRedoPaths.empty()) {
            val pop: MutableList<Point> = this.mRedoPaths.pop()
            for (push in pop) {
                this.mPoints.push(push)
            }
            this.lstPoints.push(pop as MutableList<Point>?)
            invalidate()
        }
        if (this.mBrushViewChangeListener != null) {
            this.mBrushViewChangeListener!!.onViewAdd(this)
        }
        return !this.mRedoPaths.empty()
    }

    private fun touchStart(f: Float, f2: Float) {
        this.mRedoPaths.clear()
        this.mPath!!.reset()
        this.mPath!!.moveTo(f, f2)
        this.mTouchX = f
        this.mTouchY = f2
        if (this.mBrushViewChangeListener != null) {
            this.mBrushViewChangeListener!!.onStartDrawing()
        }
        if (this.drawMode == 3) {
            this.currentBitmapPoint.clear()
        }
    }

    private fun touchMove(i: Int, i2: Int) {
        var nextInt: Int
        val f = i.toFloat()
        val abs = abs(f - this.mTouchX)
        val f2 = i2.toFloat()
        val abs2 = abs(f2 - this.mTouchY)
        if (abs < TOUCH_TOLERANCE && abs2 < TOUCH_TOLERANCE) {
            return
        }
        if (this.drawMode != 3) {
            this.mPath!!.quadTo(
                this.mTouchX,
                this.mTouchY,
                (this.mTouchX + f) / 2.0f,
                (this.mTouchY + f2) / 2.0f
            )
            this.mTouchX = f
            this.mTouchY = f2
        } else if (abs(f - this.mLastTouchX) > ((this.brushBitmapSize + this.distance).toFloat()) || abs(
                f2 - this.mLastTouchY
            ) > ((this.brushBitmapSize + this.distance).toFloat())
        ) {
            val random = Random()
            var i3 = -1
            val list: MutableList<Vector2?> = this.currentMagicBrush!!.getmPositions()
            if (list.size > 0) {
                i3 = list.get(list.size - 1)!!.drawableIndex
            }
            do {
                nextInt = random.nextInt(this.currentMagicBrush!!.lstIconWhenDrawing.size)
            } while (nextInt == i3)
            val vector2: Vector2 = Vector2(
                i,
                i2,
                i + this.brushBitmapSize,
                i2 + this.brushBitmapSize,
                nextInt,
                this.currentMagicBrush!!.getBitmapByIndex(nextInt)
            )
            list.add(vector2)
            val point: Point = Point(vector2)
            this.mPoints.push(point)
            this.currentBitmapPoint.add(point)
            this.mLastTouchX = f
            this.mLastTouchY = f2
        }
    }

    private fun touchUp() {
        if (this.drawMode != 3) {
            val arrayList: ArrayList<Point> = ArrayList<Point>()
            val point = Point(LinePath(this.mPath, this.mDrawPaint))
            this.mPoints.push(point)
            arrayList.add(point)
            if (this.drawMode == 2) {
                val point2: Point = Point(LinePath(this.mPath, this.mDrawPaintBlur))
                this.mPoints.push(point2)
                arrayList.add(point2)
            }
            this.lstPoints.push(arrayList)
        } else {
            this.lstPoints.push(ArrayList(this.currentBitmapPoint) as MutableList<Point>?)
            this.currentBitmapPoint.clear()
        }
        this.mPath = Path()
        if (this.mBrushViewChangeListener != null) {
            this.mBrushViewChangeListener!!.onStopDrawing()
            this.mBrushViewChangeListener!!.onViewAdd(this)
        }
        this.mLastTouchX = 0.0f
        this.mLastTouchY = 0.0f
    }

    //    public static final class Vector2 {
    //        public Bitmap bitmap;
    //        int drawableIndex;
    //
    //        public int x;
    //
    //        int x1;
    //
    //        public int y;
    //
    //        int y1;
    //
    //        Vector2(int i, int i2, int i3, int i4, int i5, Bitmap bitmap2) {
    //            this.x = i;
    //            this.y = i2;
    //            this.x1 = i3;
    //            this.y1 = i4;
    //            this.bitmap = bitmap2;
    //            this.drawableIndex = i5;
    //        }
    //    }
    //    class Point {
    //        LinePath linePath;
    //        Vector2 vector2;
    //
    //        Point(LinePath linePath2) {
    //            this.linePath = linePath2;
    //        }
    //
    //        Point(Vector2 vector22) {
    //            this.vector2 = vector22;
    //        }
    //    }
    fun getDrawBitmap(bitmap: Bitmap): Bitmap {
        val width = getWidth()
        val height = getHeight()
        val createBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(createBitmap)
        canvas.drawBitmap(
            bitmap,
            null,
            RectF(0.0f, 0.0f, width.toFloat(), height.toFloat()),
            null as Paint?
        )
        val it: MutableIterator<*> = this.mPoints.iterator()
        while (it.hasNext()) {
            val point: Point = it.next() as Point
            if (point.vector2 != null) {
                this.tempRect.set(
                    point.vector2!!.x,
                    point.vector2!!.y,
                    point.vector2!!.x1,
                    point.vector2!!.y1
                )
                canvas.drawBitmap(
                    point.vector2?.bitmap!!,
                    null as Rect?,
                    this.tempRect,
                    this.bitmapPaint
                )
            } else if (point.linePath != null) {
                canvas.drawPath(point.linePath?.drawPath!!, point.linePath?.drawPaint!!)
            }
        }
        return createBitmap
    }

    companion object {
        private const val TOUCH_TOLERANCE = 4.0f
    }
}