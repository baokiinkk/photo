package com.amb.photo.ui.activities.editor.sticker

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.InputStream

object StickerAsset {
    fun loadBitmapFromAssets(context: Context, str: String): Bitmap? {
        val inputStream: InputStream?
        try {
            inputStream = context.getAssets().open(str)
            val decodeStream = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            return decodeStream
        } catch (e: Exception) {
            return null
        }
    }
}