package com.avnsoft.photoeditor.photocollage.ui.activities.editor.draw.lib

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory


class DrawBitmapModel(
    val mainIcon: Int,
    val lstIconWhenDrawing: MutableList<Int>,
    private val keepExactPosition: Boolean,
    private val context: Context
) {
    var from: Int = 0
    var isLoadBitmap: Boolean = false
    private var lstBitmaps: MutableList<Bitmap>? = null
    private val mPositions: MutableList<Vector2?> = ArrayList(100)

    var to: Int = 0

    fun clearBitmap() {
        if (this.lstBitmaps != null && !this.lstBitmaps!!.isEmpty()) {
            this.lstBitmaps!!.clear()
        }
    }


    fun getmPositions(): MutableList<Vector2?> {
        return this.mPositions
    }

    fun getBitmapByIndex(i: Int): Bitmap? {
        if (this.lstBitmaps == null || this.lstBitmaps!!.isEmpty()) {
            init()
        }
        return this.lstBitmaps!!.get(i)
    }

    fun init() {
        if (this.lstBitmaps == null || this.lstBitmaps!!.isEmpty()) {
            this.lstBitmaps = ArrayList<Bitmap>()
            for (intValue in this.lstIconWhenDrawing) {
                this.lstBitmaps!!.add(
                    BitmapFactory.decodeResource(
                        this.context.getResources(),
                        intValue
                    )
                )
            }
        }
    }
}
