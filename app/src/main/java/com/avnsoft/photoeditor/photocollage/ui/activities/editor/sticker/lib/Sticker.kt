package com.avnsoft.photoeditor.photocollage.ui.activities.editor.sticker.lib

import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.PointF
import android.graphics.RectF
import android.graphics.drawable.Drawable
import androidx.annotation.IntRange
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.sqrt

abstract class Sticker {
    private val boundPoints = FloatArray(8)

    var isFlippedHorizontally: Boolean = false
        private set

    var isFlippedVertically: Boolean = false
        private set

    var isShow: Boolean = true

    private val mappedBounds = FloatArray(8)

    val matrix: Matrix = Matrix()

    private val matrixValues = FloatArray(9)

    private val trappedRect = RectF()

    private val unrotatedPoint = FloatArray(2)

    private val unrotatedWrapperCorner = FloatArray(8)

    @Retention(AnnotationRetention.SOURCE)
    annotation class Position {
        companion object {
            const val CENTER: Int = 1
            val TOP: Int = 1 shl 1
            val LEFT: Int = 1 shl 2
            val RIGHT: Int = 1 shl 3
            val BOTTOM: Int = 1 shl 4
        }
    }


    fun contains(paramFloat1: Float, paramFloat2: Float): Boolean {
        return contains(floatArrayOf(paramFloat1, paramFloat2))
    }

    fun contains(paramArrayOffloat: FloatArray): Boolean {
        val matrix = Matrix()
        matrix.setRotate(-this.currentAngle)
        getBoundPoints(this.boundPoints)
        getMappedPoints(this.mappedBounds, this.boundPoints)
        matrix.mapPoints(this.unrotatedWrapperCorner, this.mappedBounds)
        matrix.mapPoints(this.unrotatedPoint, paramArrayOffloat)
        StickerUtils.trapToRect(this.trappedRect, this.unrotatedWrapperCorner)
        return this.trappedRect.contains(this.unrotatedPoint[0], this.unrotatedPoint[1])
    }

    abstract fun draw(paramCanvas: Canvas)

    abstract val alpha: Int

    val bound: RectF
        get() {
            val rectF = RectF()
            getBound(rectF)
            return rectF
        }

    fun getBound(paramRectF: RectF) {
        paramRectF.set(0.0f, 0.0f, this.width.toFloat(), this.height.toFloat())
    }

    fun getBoundPoints(): FloatArray {
        val points = FloatArray(8)
        getBoundPoints(points)
        return points
    }

    fun getBoundPoints(points: FloatArray) {
        if (!isFlippedHorizontally) {
            if (!isFlippedVertically) {
                points[0] = 0f
                points[1] = 0f
                points[2] = this.width.toFloat()
                points[3] = 0f
                points[4] = 0f
                points[5] = this.height.toFloat()
                points[6] = this.width.toFloat()
                points[7] = this.height.toFloat()
            } else {
                points[0] = 0f
                points[1] = this.height.toFloat()
                points[2] = this.width.toFloat()
                points[3] = this.height.toFloat()
                points[4] = 0f
                points[5] = 0f
                points[6] = this.width.toFloat()
                points[7] = 0f
            }
        } else {
            if (!isFlippedVertically) {
                points[0] = this.width.toFloat()
                points[1] = 0f
                points[2] = 0f
                points[3] = 0f
                points[4] = this.width.toFloat()
                points[5] = this.height.toFloat()
                points[6] = 0f
                points[7] = this.height.toFloat()
            } else {
                points[0] = this.width.toFloat()
                points[1] = this.height.toFloat()
                points[2] = 0f
                points[3] = this.height.toFloat()
                points[4] = this.width.toFloat()
                points[5] = 0f
                points[6] = 0f
                points[7] = 0f
            }
        }
    }


    val centerPoint: PointF
        get() {
            val pointF = PointF()
            getCenterPoint(pointF)
            return pointF
        }

    fun getCenterPoint(paramPointF: PointF) {
        paramPointF.set(this.width * 1.0f / 2.0f, this.height * 1.0f / 2.0f)
    }

    val currentAngle: Float
        get() = getMatrixAngle(this.matrix)


    abstract val drawable: Drawable?

    abstract val height: Int

    val mappedBound: RectF
        get() {
            val rectF = RectF()
            getMappedBound(rectF, this.bound)
            return rectF
        }

    fun getMappedBound(paramRectF1: RectF, paramRectF2: RectF) {
        this.matrix.mapRect(paramRectF1, paramRectF2)
    }


    var mappedCenterPoint: PointF? = null
        get() {
            val pointF = this.centerPoint
            getMappedCenterPoint(pointF, FloatArray(2), FloatArray(2))
            return pointF
        }

    fun getMappedCenterPoint(
        paramPointF: PointF,
        paramArrayOffloat1: FloatArray,
        paramArrayOffloat2: FloatArray
    ) {
        getCenterPoint(paramPointF)
        paramArrayOffloat2[0] = paramPointF.x
        paramArrayOffloat2[1] = paramPointF.y
        getMappedPoints(paramArrayOffloat1, paramArrayOffloat2)
        paramPointF.set(paramArrayOffloat1[0], paramArrayOffloat1[1])
    }

    fun getMappedPoints(dst: FloatArray, src: FloatArray) {
        matrix.mapPoints(dst, src)
    }


    fun getMatrixAngle(paramMatrix: Matrix): Float {
        return Math.toDegrees(
            -atan2(
                getMatrixValue(paramMatrix, 1).toDouble(),
                getMatrixValue(paramMatrix, 0).toDouble()
            )
        ).toFloat()
    }


    fun getMatrixValue(paramMatrix: Matrix, @IntRange(from = 0L, to = 9L) paramInt: Int): Float {
        paramMatrix.getValues(this.matrixValues)
        return this.matrixValues[paramInt]
    }

    abstract val width: Int

    open fun release() {
    }

    abstract fun setAlpha(@IntRange(from = 0L, to = 255L) paramInt: Int): Sticker

    abstract fun setDrawable(paramDrawable: Drawable): Sticker?

    fun setFlippedHorizontally(paramBoolean: Boolean): Sticker {
        this.isFlippedHorizontally = paramBoolean
        return this
    }

    fun setFlippedVertically(paramBoolean: Boolean): Sticker {
        this.isFlippedVertically = paramBoolean
        return this
    }

    fun setMatrix(paramMatrix: Matrix?): Sticker {
        this.matrix.set(paramMatrix)
        return this
    }

    val mappedBoundPoints: FloatArray
        //new sticker
        get() {
            val fArr = FloatArray(8)
            getMappedPoints(fArr, getBoundPoints())
            return fArr
        }

    val currentScale: Float
        get() = getMatrixScale(this.matrix)

    val currentHeight: Float
        get() = getMatrixScale(this.matrix) * (this.height.toFloat())

    val currentWidth: Float
        get() = getMatrixScale(this.matrix) * (this.width.toFloat())

    fun getMatrixScale(matrix2: Matrix): Float {
        return sqrt(
            getMatrixValue(matrix2, 0).toDouble().pow(2.0) + getMatrixValue(
                matrix2,
                3
            ).toDouble().pow(2.0)
        ).toFloat()
    }
}