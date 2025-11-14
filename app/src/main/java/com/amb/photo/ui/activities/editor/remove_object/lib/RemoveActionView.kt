package com.amb.photo.ui.activities.editor.remove_object.lib

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.setPadding
import com.amb.photo.R

class RemoveActionView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val bgColor = Color.parseColor("#6425F3")
    private val shadowColor = Color.parseColor("#6425F366") // 40% opacity

    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = shadowColor
        style = Paint.Style.FILL
        maskFilter = BlurMaskFilter(20f.dp(), BlurMaskFilter.Blur.NORMAL)
    }

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = bgColor
        style = Paint.Style.FILL
    }

    private val rect = RectF()
    private val shadowRect = RectF()

    private val iconView = ImageView(context)
    private val textView = TextView(context)

    init {
        orientation = HORIZONTAL
        setWillNotDraw(false)

        // Set padding nằm trong content, không ảnh hưởng nền
        setPadding(20.dpInt(), 14.dpInt(), 20.dpInt(), 14.dpInt())

        // Icon
        iconView.apply {
            layoutParams = LayoutParams(20.dpInt(), 20.dpInt())
            scaleType = ImageView.ScaleType.CENTER_INSIDE
            setImageResource(R.drawable.ic_remove_object_star)
        }

        // Text
        textView.apply {
            text = "Remove"
            setTextColor(Color.WHITE)
            textSize = 16f
            setPadding(12.dpInt(), 0, 0, 0)
        }

        addView(iconView)
        addView(textView)

        // Ripple
        background = context.getSelectableItemBackground()

        clipToPadding = false
        clipChildren = false
    }

    override fun onDraw(canvas: Canvas) {
        val radius = height / 2f

        // Shadow (offset y=2dp)
        shadowRect.set(
            0f,
            2f.dp(),
            width.toFloat(),
            height.toFloat()
        )
        canvas.drawRoundRect(shadowRect, radius, radius, shadowPaint)

        // Main background
        rect.set(
            0f,
            0f,
            width.toFloat(),
            height.toFloat()
        )
        canvas.drawRoundRect(rect, radius, radius, bgPaint)
    }

    fun setIcon(resId: Int) {
        iconView.setImageResource(resId)
    }

    fun setText(label: String) {
        textView.text = label
    }

    private fun Float.dp() = this * resources.displayMetrics.density
    private fun Int.dpInt() = (this * resources.displayMetrics.density).toInt()

    private fun Context.getSelectableItemBackground() =
        obtainStyledAttributes(intArrayOf(android.R.attr.selectableItemBackground))
            .let {
                val drawable = it.getDrawable(0)
                it.recycle()
                drawable
            }
}
