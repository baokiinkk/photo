package com.amb.photo.ui.activities.editor.remove_object.lib

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.CornerPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.amb.photo.R
import kotlin.math.abs
import androidx.core.graphics.scale
import androidx.core.graphics.createBitmap
import com.amb.photo.ui.activities.editor.blur.getSoftwareBitmap


enum class Type {
    BRUSH, ERASE, LASSO_BRUSH, LASSO_ERASE, SELECT_OBJ
}

class DrawingView @JvmOverloads constructor(
    context: Context, attributeSet: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attributeSet, defStyleAttr) {

    private var type = Type.BRUSH

    lateinit var onFinishDraw: (Boolean) -> Unit

    lateinit var onDraw: () -> Unit

    private val paintColor = Color.parseColor("#95b00f26")

    private val alphaPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = 3.0f
        color = paintColor
        strokeWidth = 50.0f
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
        alpha = 178
    }

    private var isOnlyShowOriginalBitmap = false

    private val paintRectBound = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.white)
        style = Paint.Style.STROKE
        strokeWidth = 10.0f
        pathEffect = CornerPathEffect(8f)
        strokeCap = Paint.Cap.ROUND
    }

    private val pointPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeWidth = 3.0f
    }

    private val pointCirclePreview = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeWidth = 6.0f
    }

    private val paintRectObjAuto = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.white)
        style = Paint.Style.STROKE
        strokeWidth = 3.0f
        pathEffect = CornerPathEffect(8f)
        strokeCap = Paint.Cap.ROUND
    }

    private val pointPaintInside = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.white)
        style = Paint.Style.FILL
        strokeCap = Paint.Cap.ROUND
        strokeWidth = 3.0f
    }

    private val drawPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = 3.0f
        color = paintColor
        strokeWidth = 50.0f
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    }

    private val paintLasso = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#80FFFFFF")
        strokeWidth = 5f
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    }

    private val paintLassoFill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = paintColor
        style = Paint.Style.FILL
    }


    private var bitmapViewDrawMask: Bitmap? = null

    private val canvasPaint = Paint(Paint.DITHER_FLAG)

    private var canvasDrawLinePath: Canvas? = null

    private lateinit var originalBitmap: Bitmap

//    private var smallBitmap: Bitmap? = null

    private val drawPath = Path()

    // x coordinate of bottom image in view
    private var maxx = 0

    // y coordinate of bottom image in view
    private var maxy = 0

    // x coordinate of top image in view
    var minx = 0
        private set

    // y coordinate of top image in view
    var miny = 0
        private set

    private var touchX = 0f
    private var touchY = 0f

    var widthImg = 0
        private set

    var heightImg = 0
        private set

    private val rectPreviewLeft = RectF()

    private val rectPreviewRight = RectF()

    private val previewBitmapSize = 400

    private var isTouched = false

    private val canvasDrawMask = Canvas()

    private val canvasDrawPreview = Canvas()

    // bitmap background for view
    private lateinit var bitmapView: Bitmap

    private lateinit var originalBitmapScale: Bitmap

    constructor(
        context: Context,
        original: Bitmap,
        attributeSet: AttributeSet? = null,
        defStyleAttr: Int = 0
    ) : this(context, attributeSet, defStyleAttr) {
        this.originalBitmap = original

    }

//    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
//
//        val wView = MeasureSpec.getSize(widthMeasureSpec)
//        val hView = MeasureSpec.getSize(heightMeasureSpec)
//
//        calculateWidthAndHeightOfImage(wView, hView)
//
//        // Điều chỉnh kích thước dựa trên chế độ MeasureSpec
//        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
//        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
//
//        if (widthMode == MeasureSpec.AT_MOST) {
//            Log.i("TAG", "onMeasureăefawef:0 ")
//            widthImg = min(widthImg.toDouble(), wView.toDouble()).toInt()
//        }
//
//        if (heightMode == MeasureSpec.AT_MOST) {
//            Log.i("TAG", "onMeasureăefawef:1 ")
//            heightImg = min(heightImg.toDouble(), hView.toDouble()).toInt()
//        }
//
//        if (widthMode == MeasureSpec.EXACTLY) {
//            Log.i("TAG", "onMeasureăefawef:2 ${widthImg} / ${wView}")
//            widthImg = wView
//        }
//
//        if (heightMode == MeasureSpec.EXACTLY) {
//            Log.i("TAG", "onMeasureăefawef:3 ")
//            heightImg = hView
//        }
//
//        setMeasuredDimension(widthImg, heightImg)
//    }


    private val pathLasso = Path()


    fun setBitmapDraw(bitmap: Bitmap) {
        originalBitmap = bitmap
        val clearPaint = Paint()
        clearPaint.setXfermode(PorterDuffXfermode(PorterDuff.Mode.CLEAR))
        RectF(
            0f, 0f, width.toFloat(), height.toFloat()
        ).apply {
            canvasDrawLinePath?.drawRect(this, clearPaint)
            canvasDrawMask.drawRect(this, clearPaint)
            canvasDrawPreview.drawRect(this, clearPaint)
        }
        bitmapView = Bitmap.createScaledBitmap(originalBitmap, widthImg, heightImg, true)
        invalidate()
    }

    private fun calculateWidthAndHeightOfImage(w: Int, h: Int) {
        widthImg = originalBitmap.width
        heightImg = originalBitmap.height
        if ((widthImg.toFloat()) / (heightImg.toFloat()) > (w.toFloat()) / (h.toFloat())) {
            heightImg = (heightImg * w) / widthImg
            widthImg = w
            Log.i("TAG", "onSizeChangedưefwef: $w / $h / ${widthImg} / ${heightImg}")
        } else {
            widthImg = (widthImg * h) / heightImg
            heightImg = h
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)


        calculateWidthAndHeightOfImage(w, h)
        minx = (w - widthImg) / 2
        miny = (h - heightImg) / 2
        maxx = minx + widthImg
        maxy = miny + heightImg

        rectPreviewLeft.set(
            0f + paintRectBound.strokeWidth,
            0f + paintRectBound.strokeWidth,
            previewBitmapSize + paintRectBound.strokeWidth,
            previewBitmapSize + paintRectBound.strokeWidth
        )

        rectPreviewRight.set(
            width - previewBitmapSize.toFloat(),
            0f + paintRectBound.strokeWidth,
            width.toFloat(),
            previewBitmapSize + paintRectBound.strokeWidth
        )

        Log.i("TAG", "onSizeChangedưefwefqădqwe: $minx / $miny / ${maxx} / ${maxy}")



        bitmapView = Bitmap.createScaledBitmap(originalBitmap, widthImg, heightImg, true)

        originalBitmapScale = Bitmap.createScaledBitmap(originalBitmap, widthImg, heightImg, true)

        bitmapViewDrawMask = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)

        //smallBitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888)

        val createScaledBitmap = Bitmap.createScaledBitmap(
            getDrawableBitmap(), widthImg, heightImg, true
        )

        canvasDrawLinePath = Canvas(bitmapViewDrawMask!!)
        canvasDrawLinePath!!.drawBitmap(
            createScaledBitmap, minx.toFloat(), miny.toFloat(), null
        )
    }

    private var isDrawTouchSize = false

    fun setStrokeWidth(strokeWidth: Float, isDrawTouchSize: Boolean) {
        this.isDrawTouchSize = isDrawTouchSize
        drawPaint.strokeWidth = strokeWidth
        invalidate()
    }


    fun setType(type: Type) {
        Log.i("TAG", "setType: $type")
        this.type = type
        if (type == Type.BRUSH || type == Type.LASSO_BRUSH) {
            drawPaint.color = paintColor
            drawPaint.setXfermode(PorterDuffXfermode(PorterDuff.Mode.ADD))
            paintLassoFill.setXfermode(PorterDuffXfermode(PorterDuff.Mode.ADD))
        } else if (type == Type.ERASE || type == Type.LASSO_ERASE) {
            drawPaint.color = Color.BLACK
            drawPaint.setXfermode(PorterDuffXfermode(PorterDuff.Mode.CLEAR))
            paintLassoFill.setXfermode(PorterDuffXfermode(PorterDuff.Mode.CLEAR))
        }
        invalidate()
    }

    // create bitmap mask transparent
    private fun getDrawableBitmap(): Bitmap {
        val createBitmap = Bitmap.createBitmap(
            originalBitmap.width, originalBitmap.height, Bitmap.Config.ARGB_8888
        )
        createBitmap.eraseColor(Color.TRANSPARENT)
        return createBitmap
    }

    fun getMask(): Bitmap {
        val createBitmap = Bitmap.createBitmap(
            bitmapViewDrawMask!!, minx, miny, widthImg, heightImg
        )
        (0 until createBitmap.width).onEach { i ->
            (0 until createBitmap.height).onEach { i2 ->
                if (createBitmap.getPixel(i, i2) != Color.TRANSPARENT) {
                    createBitmap.setPixel(i, i2, Color.WHITE)
                } else {
                    createBitmap.setPixel(i, i2, Color.TRANSPARENT)
                }
            }
        }
        return Bitmap.createScaledBitmap(
            createBitmap, originalBitmap.width, originalBitmap.height, true
        )
    }

//    fun getMask(): Bitmap {
//        // Tạo bitmap từ phần mong muốn
//        val maskBitmap = Bitmap.createBitmap(
//            bitmapViewDrawMask!!, minx, miny, widthImg, heightImg
//        )
//
//        // Tạo bitmap kết quả với cùng kích thước nhưng chỉ cần kênh alpha
//        val resultBitmap = Bitmap.createBitmap(maskBitmap.width, maskBitmap.height, Bitmap.Config.ALPHA_8)
//
//        val canvas = Canvas(resultBitmap)
//        val paint = Paint().apply {
//            color = Color.WHITE // Chỉ vẽ màu trắng
//            xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN) // Giữ lại pixel không trong suốt
//        }
//
//        // Vẽ mask lên resultBitmap, chỉ giữ phần không trong suốt
//        canvas.drawBitmap(maskBitmap, 0f, 0f, paint)
//
//        // Scale bitmap về kích thước gốc
//        return Bitmap.createScaledBitmap(resultBitmap, originalBitmap.width, originalBitmap.height, true)
//    }

    private var listObjAutoSelected: List<ObjAuto>? = null

    private val paintObjAuto = Paint().apply {
        alpha = 90
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (isOnlyShowOriginalBitmap) {
            canvas.drawBitmap(originalBitmapScale, minx.toFloat(), miny.toFloat(), null)
            return
        }

        canvas.drawBitmap(getSoftwareBitmap(bitmapView), minx.toFloat(), miny.toFloat(), null)

        listObjAutoSelected?.onEach {
            canvas.drawBitmap(
                it.bitmapMask, minx.toFloat(), miny.toFloat(), paintObjAuto
            )
        }

        Log.i("TAG", "onDraw:000 asdfgadfg $type")
        if (type == Type.SELECT_OBJ) {
            Log.i("TAG", "onDraw:0 asdfgadfg")
            listObjAuto?.onEach {
                if (!it.isRemoved) {
                    canvas.drawRect(it.rectBitmapMask, paintRectObjAuto)
                }
            }

        } else if (type == Type.LASSO_BRUSH || type == Type.LASSO_ERASE) {
            Log.i("TAG", "onDraw:1 asdfgadfg")
            if (isTouched) {
                canvas.drawPath(pathLasso, paintLasso)
            }
        }


//        canvas.drawRect(minx.toFloat(), miny.toFloat(), maxx.toFloat(), maxy.toFloat(), alphaPaint)

        val createBitmap = createBitmap(width, height)
        canvasDrawMask.setBitmap(createBitmap)
        canvasDrawMask.drawBitmap(bitmapViewDrawMask!!, 0.0f, 0.0f, canvasPaint)
        canvasDrawMask.drawPath(drawPath, drawPaint)

        canvas.drawBitmap(createBitmap, 0.0f, 0.0f, canvasPaint)

        val createBitmap2 = createBitmap(width, height)
        canvasDrawPreview.setBitmap(createBitmap2)

        val newBitmap = getSoftwareBitmap(originalBitmap.scale(widthImg, heightImg))

        canvasDrawPreview.drawBitmap(
            newBitmap, minx.toFloat(), miny.toFloat(), canvasPaint
        )
        canvasDrawPreview.drawBitmap(createBitmap, 0.0f, 0.0f, this.alphaPaint)

        if (isDrawTouchSize) {
            canvas.drawCircle(
                width / 2f, heightImg / 2f, drawPaint.strokeWidth / 2.0f, pointCirclePreview
            )
            canvas.drawCircle(
                width / 2f, heightImg / 2f, drawPaint.strokeWidth / 2.0f, pointPaintInside
            )
        }

        if (type == Type.SELECT_OBJ) {
            return
        }

        if (isTouched) {
            if (type == Type.BRUSH || type == Type.ERASE) {
                canvas.drawCircle(
                    this.touchX, this.touchY, drawPaint.strokeWidth / 2.0f, pointPaint
                )
//            canvasDrawMask.drawCircle(
//                this.touchX, this.touchY, drawPaint.strokeWidth / 2.0f, pointPaint
//            )
                canvasDrawPreview.drawCircle(
                    this.touchX, this.touchY, drawPaint.strokeWidth / 2.0f, pointPaint
                )
                canvasDrawPreview.drawCircle(
                    this.touchX, this.touchY, drawPaint.strokeWidth / 2.0f, pointPaintInside
                )
            } else if (type == Type.LASSO_BRUSH || type == Type.LASSO_ERASE) {
                canvasDrawPreview.drawPath(pathLasso, paintLasso)
            }

//            val maxXLeft = max(touchX.toDouble(), smallBitmap!!.width / 2.0 + minx).toInt()
//            val maxXRight = maxx - smallBitmap!!.width / 2
//            val limitX = min(maxXLeft.toDouble(), maxXRight.toDouble()).toInt()
//            val xCoordDrawPreview = limitX - smallBitmap!!.width / 2
//
//            val maxYTop = max(touchY.toDouble(), smallBitmap!!.height / 2.0 + miny).toInt()
//            val maxYBottom = maxy - smallBitmap!!.width / 2
//            val limitY = min(maxYTop.toDouble(), maxYBottom.toDouble()).toInt()
//            val yCoordDrawPreview = limitY - smallBitmap!!.width / 2

//            smallBitmap = Bitmap.createBitmap(
//                createBitmap2,
//                xCoordDrawPreview,
//                yCoordDrawPreview,
//                smallBitmap!!.width,
//                smallBitmap!!.height
//            )

//            val createScaledBitmap =
//                Bitmap.createScaledBitmap(smallBitmap!!, previewBitmapSize, previewBitmapSize, true)

            if (rectPreviewLeft.contains(touchX, touchY)) {
//                canvas.drawBitmap(
//                    createScaledBitmap, rectPreviewRight.left, rectPreviewRight.top, null
//                )
                //canvas.drawRect(rectPreviewRight, paintRectBound)
                return
            }
//            canvas.drawBitmap(createScaledBitmap, rectPreviewLeft.left, rectPreviewLeft.top, null)
            //canvas.drawRect(rectPreviewLeft, paintRectBound)
        }
    }

    var eventClickObj: ((ObjAuto) -> Unit)? = null

    private var startX = 0f
    private var startY = 0f

    private var firstX = 0f
    private var firstY = 0f

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(motionEvent: MotionEvent): Boolean {
        touchX = motionEvent.x
        touchY = motionEvent.y

        when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> {
                isTouched = true
                when (type) {
                    Type.SELECT_OBJ -> {
                        run breaking@{
                            listObjAuto?.onEach {
                                if (!it.isRemoved && it.rectBitmapMask.contains(touchX, touchY)) {
                                    eventClickObj?.invoke(it)
                                    return@breaking
                                }
                            }
                        }
                    }

                    Type.LASSO_BRUSH, Type.LASSO_ERASE -> {
                        firstX = touchX
                        firstY = touchY
                        pathLasso.moveTo(touchX, touchY)
                        startX = touchX
                        startY = touchY
                    }

                    Type.BRUSH, Type.ERASE -> {
                        drawPath.moveTo(touchX, touchY)
                    }
                }

            }

            MotionEvent.ACTION_UP -> {
                isTouched = false

                if (type == Type.LASSO_BRUSH || type == Type.LASSO_ERASE) {
                    pathLasso.lineTo(firstX, firstY)
                    canvasDrawLinePath!!.drawPath(
                        pathLasso, paintLassoFill
                    )
                    pathLasso.reset()
                    Log.i("TAG", "onTouchEventetht: ${firstX} / $firstY")
                } else if (type == Type.BRUSH || type == Type.ERASE) {
                    drawPath.lineTo(touchX, touchY)
                    canvasDrawLinePath!!.drawPath(
                        drawPath, drawPaint
                    )
                    Log.i("TAG", "onTouchEventẻgerg: ")

                    drawPath.reset()
                }
                if (type != Type.SELECT_OBJ) {
                    onFinishDraw(type == Type.BRUSH || type == Type.LASSO_BRUSH)
                }
            }

            MotionEvent.ACTION_MOVE -> {
                if (type == Type.LASSO_BRUSH || type == Type.LASSO_ERASE) {
                    val dx = abs(touchX - startX)
                    val dy = abs(touchY - startY)
                    if (dx >= 4f || dy >= 4f) {
                        pathLasso.quadTo(
                            startX, startY, (startX + touchX) / 2, (startY + touchY) / 2
                        )
                        startX = touchX
                        startY = touchY
                    }
                } else if (type == Type.BRUSH || type == Type.ERASE) {
                    onDraw()
                    drawPath.lineTo(touchX, touchY)
                }
            }
        }
        postInvalidateOnAnimation()
        return true
    }

    fun setListObjSelected(list: List<ObjAuto>?) {
        listObjAutoSelected = list
        invalidate()
    }

    private var listObjAuto: List<ObjAuto>? = null

    fun setListObjAuto(list: List<ObjAuto>?) {
        list?.sortedByDescending {
            2 * (it.rectBitmapMask.width() + it.rectBitmapMask.height())
        }
        listObjAuto = list
        invalidate()
    }

    fun setShowOriginalBitmap(isShow: Boolean) {
        isOnlyShowOriginalBitmap = isShow
        invalidate()
    }
}