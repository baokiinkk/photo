package com.avnsoft.photoeditor.photocollage.ui.activities.editor.remove_object.lib

import android.graphics.Bitmap
import android.graphics.RectF

data class ObjAuto(
    val nameObj: String,
    val bitmapMask: Bitmap,
    val rectBitmapMask: RectF,
    val bitmapMaskPreview: Bitmap,
    val isRemoved: Boolean = false,
)
