package com.avnsoft.photoeditor.photocollage.utils

import android.graphics.Bitmap
import android.media.FaceDetector
import android.util.Log
import org.wysaid.myUtils.FileUtil
import java.io.BufferedOutputStream
import java.io.FileOutputStream
import java.io.IOException


object ImageUtil : FileUtil() {
    fun saveBitmap(bmp: Bitmap): String? {
        val path = getPath()
        val currentTime = System.currentTimeMillis()
        val filename = path + "/" + currentTime + ".jpg"
        return saveBitmap(bmp, filename)
    }

    fun saveBitmap(bmp: Bitmap, filename: String): String? {
        Log.i(LOG_TAG, "saving Bitmap : " + filename)

        try {
            val fileout = FileOutputStream(filename)
            val bufferOutStream = BufferedOutputStream(fileout)
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, bufferOutStream)
            bufferOutStream.flush()
            bufferOutStream.close()
        } catch (e: IOException) {
            Log.e(LOG_TAG, "Err when saving bitmap...")
            e.printStackTrace()
            return null
        }

        Log.i(LOG_TAG, "Bitmap " + filename + " saved!")
        return filename
    }

    @JvmOverloads
    fun findFaceByBitmap(bmp: Bitmap?, maxFaces: Int = 1): FaceRects? {
        if (bmp == null) {
            Log.e(LOG_TAG, "Invalid Bitmap for Face Detection!")
            return null
        }

        var newBitmap: Bitmap? = bmp

        //人脸检测API 仅支持 RGB_565 格式当图像. (for now)
        if (newBitmap!!.getConfig() != Bitmap.Config.RGB_565) {
            newBitmap = newBitmap.copy(Bitmap.Config.RGB_565, false)
        }

        val rects = FaceRects()
        rects.faces = arrayOfNulls<FaceDetector.Face>(maxFaces)

        try {
            val detector = FaceDetector(newBitmap!!.getWidth(), newBitmap.getHeight(), maxFaces)
            rects.numOfFaces = detector.findFaces(newBitmap, rects.faces)
        } catch (e: Exception) {
            Log.e(LOG_TAG, "findFaceByBitmap error: " + e.message)
            return null
        }


        if (newBitmap != bmp) {
            newBitmap.recycle()
        }
        return rects
    }

    class FaceRects {
        var numOfFaces: Int = 0 // 实际检测出的人脸数
        var faces: Array<FaceDetector.Face?>? = null
    }
}