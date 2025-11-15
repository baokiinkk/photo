package com.avnsoft.photoeditor.photocollage.ui.activities.editor.sticker.lib

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.RectF
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException


object StickerUtils {
    fun notifySystemGallery(paramContext: Context, paramFile: File) {
        if (paramFile != null && paramFile.exists()) try {
            MediaStore.Images.Media.insertImage(
                paramContext.getContentResolver(),
                paramFile.getAbsolutePath(),
                paramFile.getName(),
                null
            )
            paramContext.sendBroadcast(
                Intent(
                    "android.intent.action.MEDIA_SCANNER_SCAN_FILE",
                    Uri.fromFile(paramFile)
                )
            )
            return
        } catch (fileNotFoundException: FileNotFoundException) {
            throw IllegalStateException("File couldn't be found")
        }
        throw IllegalArgumentException("bmp should not be null")
    }

    fun saveImageToGallery(paramFile: File, paramBitmap: Bitmap): File {
        if (paramBitmap != null) {
            try {
                val fileOutputStream = FileOutputStream(paramFile)
                paramBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
                fileOutputStream.flush()
                fileOutputStream.close()
            } catch (iOException: IOException) {
                iOException.printStackTrace()
            }
            val stringBuilder = StringBuilder()
            stringBuilder.append("saveImageToGallery: the path of bmp is ")
            stringBuilder.append(paramFile.getAbsolutePath())
            Log.e("StickerView", stringBuilder.toString())
            return paramFile
        }
        throw IllegalArgumentException("bmp should not be null")
    }


    fun trapToRect(paramRectF: RectF, paramArrayOffloat: FloatArray) {
        paramRectF.set(
            Float.Companion.POSITIVE_INFINITY,
            Float.Companion.POSITIVE_INFINITY,
            Float.Companion.NEGATIVE_INFINITY,
            Float.Companion.NEGATIVE_INFINITY
        )
        var i: Int
        i = 1
        while (i < paramArrayOffloat.size) {
            var f3: Float
            var f2 = Math.round(paramArrayOffloat[i - 1] * 10.0f) / 10.0f
            var f1 = Math.round(paramArrayOffloat[i] * 10.0f) / 10.0f
            if (f2 < paramRectF.left) {
                f3 = f2
            } else {
                f3 = paramRectF.left
            }
            paramRectF.left = f3
            if (f1 < paramRectF.top) {
                f3 = f1
            } else {
                f3 = paramRectF.top
            }
            paramRectF.top = f3
            if (f2 <= paramRectF.right) f2 = paramRectF.right
            paramRectF.right = f2
            if (f1 <= paramRectF.bottom) f1 = paramRectF.bottom
            paramRectF.bottom = f1
            i += 2
        }
        paramRectF.sort()
    }
}