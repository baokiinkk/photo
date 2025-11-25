package com.avnsoft.photoeditor.photocollage.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.toBitmap
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FileUtil {
    const val FOLDER_SDK = "PhotoCollage"

    @Throws(IOException::class)
    fun saveImageToStorageWithQuality(context: Context, quality: Int, bitmap: Bitmap): Uri? {
        val imageOutStream: OutputStream?
        val uri: Uri?
        val filename = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(Date()) + ".png"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues()
            val path = Environment.DIRECTORY_DCIM + "/" + FOLDER_SDK
            values.put(MediaStore.Images.Media.DISPLAY_NAME, filename)
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis())
            values.put(MediaStore.Images.Media.RELATIVE_PATH, path)
            val contentResolver = context.getContentResolver()

            uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            imageOutStream = contentResolver.openOutputStream(uri!!)
            bitmap.compress(Bitmap.CompressFormat.PNG, quality, imageOutStream!!)
        } else {
            val parentFilePath: String = getRootFolder()
            val file = File(parentFilePath)
            if (!file.exists()) file.mkdirs()
            val image = File(file, filename)
            imageOutStream = FileOutputStream(image)
            bitmap.compress(Bitmap.CompressFormat.PNG, quality, imageOutStream)
            uri = addImageToGallery(image.path, context)
        }

        return uri
    }

    fun addImageToGallery(filePath: String?, context: Context): Uri? {
        val values = ContentValues()

        values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis())
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png")
        values.put(MediaStore.MediaColumns.DATA, filePath)

        return context.contentResolver
            .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
    }


    fun getRootFolder(): String {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
            .absolutePath + "/" + FOLDER_SDK
    }

    fun addDiagonalWatermark(originalBitmap: Bitmap, watermarkText: String, textSize: Int): Bitmap {
        // Tạo bitmap mới dựa trên ảnh gốc
        val watermarkedBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true)

        // Khởi tạo Canvas và Paint
        val canvas = Canvas(watermarkedBitmap)
        val paint = Paint()
        paint.setColor(Color.WHITE) // Màu chữ
        paint.setTextSize(textSize.toFloat()) // Kích thước chữ
        paint.setAntiAlias(true) // Làm mịn chữ
        paint.setAlpha(100) // Độ trong suốt (100 = khoảng 40%)

        // Xoay canvas 45 độ
        canvas.save() // Lưu trạng thái hiện tại của canvas
        canvas.rotate(
            -45f,
            (watermarkedBitmap.getWidth() / 2).toFloat(),
            (watermarkedBitmap.getHeight() / 2).toFloat()
        )

        // Tính toán khoảng cách giữa các watermark
        val textWidth = paint.measureText(watermarkText).toInt()
        val diagonalStep = textWidth + 50 // Khoảng cách giữa các watermark (50 là padding)

        // Lặp qua ảnh và vẽ watermark nhiều lần
        var x = -watermarkedBitmap.getHeight()
        while (x < watermarkedBitmap.getWidth()) {
            var y = 0
            while (y < watermarkedBitmap.getHeight() * 2) {
                canvas.drawText(watermarkText, x.toFloat(), y.toFloat(), paint)
                y += diagonalStep
            }
            x += diagonalStep
        }

        canvas.restore() // Khôi phục trạng thái ban đầu của canvas
        return watermarkedBitmap
    }

    fun getCacheFolder(context: Context): File {
        val folderTemp = context.cacheDir.absolutePath + "/" + FOLDER_SDK
        val folder = File(folderTemp)
        folder.deleteRecursively()
        if (!folder.exists()) {
            folder.mkdirs()
        }
        return folder
    }

    fun Bitmap.toFile(context: Context): String {
        val file = File.createTempFile("image_", ".jpg", getCacheFolder(context))
        file.outputStream().use { out ->
            this.compress(Bitmap.CompressFormat.JPEG, 100, out)
        }
        return file.absolutePath
    }

    fun String.scaleBitmapKeepRatio(maxWidth: Int, maxHeight: Int): Bitmap? {
        val bitmap = this.toBitmap() ?: return null
        return scaleBitmapKeepRatio(bitmap, maxWidth, maxHeight)
    }

    fun scaleBitmapKeepRatio(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val ratio = minOf(
            maxWidth.toFloat() / bitmap.width,
            maxHeight.toFloat() / bitmap.height
        )

        val width = (bitmap.width * ratio).toInt()
        val height = (bitmap.height * ratio).toInt()

        return Bitmap.createScaledBitmap(bitmap, width, height, true)
    }
}