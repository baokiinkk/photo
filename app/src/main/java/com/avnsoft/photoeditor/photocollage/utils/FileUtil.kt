package com.avnsoft.photoeditor.photocollage.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.graphics.drawable.toBitmap
import androidx.core.net.toUri
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import coil.size.Size
import com.avnsoft.photoeditor.photocollage.R
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.toBitmap
import com.avnsoft.photoeditor.photocollage.ui.activities.export_image.Quality
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
    fun saveImageToStorageWithQuality(context: Context, quality: Quality, bitmap: Bitmap): Uri? {
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
            bitmap.compress(Bitmap.CompressFormat.PNG, quality.value, imageOutStream!!)
        } else {
            val parentFilePath: String = getRootFolder()
            val file = File(parentFilePath)
            if (!file.exists()) file.mkdirs()
            val image = File(file, filename)
            imageOutStream = FileOutputStream(image)
            bitmap.compress(Bitmap.CompressFormat.PNG, quality.value, imageOutStream)
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
//        folder.deleteRecursively()
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

    suspend fun Uri.toScaledBitmapForUpload(
        context: Context,
        size: Int = 1504
    ): Bitmap? = withContext(Dispatchers.IO) {
        val imageLoader = ImageLoader.Builder(context)
            .crossfade(true)
            // Tùy chọn: thiết lập cache nếu cần
            .build()
        // 1. Tạo ImageRequest với các thiết lập giới hạn
        val request = ImageRequest.Builder(context)
            .data(this@toScaledBitmapForUpload) // Uri/URL của ảnh
            .size(Size(size, size)) // 👈 GIỚI HẠN KÍCH THƯỚC ĐẦU RA
            .bitmapConfig(Bitmap.Config.ARGB_8888) // Đảm bảo chất lượng cao
            .allowHardware(false) // Tắt Hardware Bitmap để dễ dàng trích xuất và xử lý
            .diskCachePolicy(coil.request.CachePolicy.DISABLED) // Không cần lưu vào Disk Cache cho mục đích upload
            .build()

        // 2. Thực hiện request và chờ kết quả
        val result = imageLoader.execute(request)

        // 3. Trích xuất Bitmap từ kết quả
        return@withContext if (result is SuccessResult) {
            // Chuyển đổi Drawable thành Bitmap
            result.drawable.toBitmap()
        } else {
            null
        }
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

    fun getBitmapResize(bitmap: Bitmap, w: Int, h: Int): Bitmap {
        var maxWidth = 1080
        var maxHeight = 1920

        if (w != -1 && h != -1) {
            maxWidth = w
            maxHeight = h
        }

        val width = bitmap.getWidth()
        val height = bitmap.getHeight()
        if (width >= height) {
            val i3 = (height * maxWidth) / width
            if (i3 > maxHeight) {
                maxWidth = (maxWidth * maxHeight) / i3
            } else {
                maxHeight = i3
            }
        } else {
            val i4 = (width * maxHeight) / height
            if (i4 > maxWidth) {
                maxHeight = (maxHeight * maxWidth) / i4
            } else {
                maxWidth = i4
            }
        }
        return Bitmap.createScaledBitmap(bitmap, maxWidth, maxHeight, true)
    }

    fun decodeUriToDrawable(context: Context, uri: Uri?, w: Int, h: Int): BitmapDrawable? {
        try {
            var mBitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri)
            if (mBitmap == null) mBitmap =
                BitmapFactory.decodeResource(context.getResources(), R.drawable.thumbs)
            val bm: Bitmap =
                getBitmapResize(
                    mBitmap,
                    w,
                    h
                )
            return BitmapDrawable(context.getResources(), bm)
        } catch (ex: Exception) {
            ex.printStackTrace()
            System.gc()
        } catch (ex: OutOfMemoryError) {
            ex.printStackTrace()
            System.gc()
        }
        return null
    }
}

fun String.toFile(): File {
    return File(this)
}