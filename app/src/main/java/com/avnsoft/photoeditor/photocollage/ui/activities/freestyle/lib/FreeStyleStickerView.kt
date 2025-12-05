package com.avnsoft.photoeditor.photocollage.ui.activities.freestyle.lib

import android.content.Context
import android.util.AttributeSet
import androidx.core.view.ViewCompat
import com.avnsoft.photoeditor.photocollage.R
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.sticker.lib.StickerView

class FreeStyleStickerView : StickerView {
    constructor(paramContext: Context) : super(paramContext, null)

    constructor(paramContext: Context, paramAttributeSet: AttributeSet?) : super(
        paramContext,
        paramAttributeSet
    )

    constructor(paramContext: Context, paramAttributeSet: AttributeSet?, paramInt: Int) : super(
        paramContext,
        paramAttributeSet,
        paramInt
    )


    fun addSticker(
        sticker: MutableList<FreeStyleSticker>,
        position: Int,
        scale: Float
    ): StickerView? {
        if (ViewCompat.isLaidOut(this)) {
            addStickerImmediately(sticker, position, scale)
            return this
        }
        post({ addStickerImmediately(sticker, position, scale) })
        return this
    }

    protected fun addStickerImmediately(
        stickers: MutableList<FreeStyleSticker>,
        position: Int,
        scale: Float
    ) {
        for (paramSticker in stickers) {
            setRandomStickerPosition(paramSticker)
            paramSticker.matrix.postScale(scale, scale, width.toFloat(), height.toFloat())
            this.stickers.add(paramSticker)
            if (this.onStickerOperationListener != null) this.onStickerOperationListener!!.onStickerAdded(
                paramSticker
            )
        }

        invalidate()
    }
}