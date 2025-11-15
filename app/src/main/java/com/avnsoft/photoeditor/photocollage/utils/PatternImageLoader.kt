package com.avnsoft.photoeditor.photocollage.utils

import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import java.io.IOException

/**
 * Helper function to get image URI for pattern item
 * If URL fails, fallback to assets/pattern/Default/{fileName}
 */
fun getPatternImageUri(
    urlRoot: String,
    urlThumb: String
): String {
    // Try to build full URL first
    return if (urlThumb.startsWith("http://") || urlThumb.startsWith("https://")) {
        urlThumb
    } else {
        "$urlRoot$urlThumb"
    }
}

/**
 * Get fallback asset path for pattern image
 * Uses the fileName directly from the name field (e.g., "item_1.jpg" -> "pattern/Default/item_1.jpg")
 * If file doesn't exist, try with .webp extension
 */
fun getPatternAssetPath(fileName: String): String {
    // Use fileName directly: pattern/Default/{fileName}
    return "pattern/Default/$fileName"
}

/**
 * Load fallback image from assets as Painter
 * Tries fileName first, then tries with .webp extension if not found
 */
fun loadPatternAssetPainter(context: Context, fileName: String): Painter? {
    // Try with original fileName first
    val assetPath1 = getPatternAssetPath(fileName)
    val bitmap1 = try {
        context.assets.open(assetPath1).use { 
            BitmapFactory.decodeStream(it)
        }
    } catch (e: IOException) {
        null
    }
    
    if (bitmap1 != null) {
        return BitmapPainter(bitmap1.asImageBitmap())
    }
    
    // If not found, try with .webp extension
    val nameWithoutExt = fileName.substringBeforeLast(".")
    val assetPath2 = "pattern/Default/$nameWithoutExt.webp"
    return try {
        val bitmap2 = context.assets.open(assetPath2).use { 
            BitmapFactory.decodeStream(it)
        }
        bitmap2?.let { BitmapPainter(it.asImageBitmap()) }
    } catch (e: IOException) {
        null
    }
}

