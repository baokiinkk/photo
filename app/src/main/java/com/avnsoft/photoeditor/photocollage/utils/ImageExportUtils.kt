package com.avnsoft.photoeditor.photocollage.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import androidx.compose.ui.geometry.Size
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.tools.BackgroundSelection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ImageExportUtils {

    suspend fun mergeImage(
        context: Context,
        backgroundSelection: BackgroundSelection?,
        foregroundBitmap: Bitmap?,
        canvasSize: Size?
    ): Bitmap? {
        if (foregroundBitmap == null) return null

        // Use canvasSize if available, otherwise fallback to foreground bitmap size
        // However, usually we want the output to match the "Box" size or a specific quality.
        // For now, let's use the foreground bitmap's size as the base if canvasSize is null,
        // but ideally we should respect the aspect ratio of the canvasSize.
        
        val width = canvasSize?.width?.toInt() ?: foregroundBitmap.width
        val height = canvasSize?.height?.toInt() ?: foregroundBitmap.height
        
        if (width <= 0 || height <= 0) return null

        val resultBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(resultBitmap)

        // 1. Draw Background
        drawBackground(context, canvas, backgroundSelection, width, height)

        // 2. Draw Foreground
        // We need to center the foreground bitmap in the canvas if their aspect ratios differ,
        // or just draw it to fill if that's the intention. 
        // Based on EditorActivity, the foreground bitmap seems to be scaled to fit/fill the box.
        // Let's assume we draw it to fill the canvas for now, or maintain aspect ratio?
        // In EditorActivity: 
        // if (uiState.isOriginal) -> ContentScale.None (Center)
        // else -> ContentScale.Crop (Fill)
        
        // For simplicity and common use case in this app (likely), let's draw it to fill the bounds 
        // or centered. Since we are exporting "the box", and the box has a specific size.
        
        val srcRect = Rect(0, 0, foregroundBitmap.width, foregroundBitmap.height)
        val dstRect = Rect(0, 0, width, height)
        
        // If we want to preserve aspect ratio like "Fit" or "Center", we need calculation.
        // But if the foregroundBitmap passed here is already the result of "scaleBitmapToBox",
        // then it might already be the correct size or aspect ratio.
        // Let's draw it to fill the destination for now.
        canvas.drawBitmap(foregroundBitmap, srcRect, dstRect, null)

        return resultBitmap
    }

    private suspend fun drawBackground(
        context: Context,
        canvas: Canvas,
        backgroundSelection: BackgroundSelection?,
        width: Int,
        height: Int
    ) {
        val paint = Paint().apply {
            isAntiAlias = true
        }

        when (backgroundSelection) {
            is BackgroundSelection.Solid -> {
                try {
                    val color = Color.parseColor(if (backgroundSelection.color.startsWith("#")) backgroundSelection.color else "#${backgroundSelection.color}")
                    canvas.drawColor(color)
                } catch (e: Exception) {
                    canvas.drawColor(Color.WHITE)
                }
            }

            is BackgroundSelection.Pattern -> {
                val url = backgroundSelection.item.urlThumb
                // Load image using Coil
                val bitmap = loadBitmapFromUrl(context, url)
                if (bitmap != null) {
                    // Draw pattern - usually CenterCrop
                    val srcRect = Rect(0, 0, bitmap.width, bitmap.height)
                    val dstRect = Rect(0, 0, width, height)
                    
                    // Calculate CenterCrop
                    val scale: Float
                    val dx: Float
                    val dy: Float

                    if (bitmap.width * height > width * bitmap.height) {
                        scale = height.toFloat() / bitmap.height.toFloat()
                        dx = (width - bitmap.width * scale) * 0.5f
                        dy = 0f
                    } else {
                        scale = width.toFloat() / bitmap.width.toFloat()
                        dx = 0f
                        dy = (height - bitmap.height * scale) * 0.5f
                    }

                    val matrix = android.graphics.Matrix()
                    matrix.setScale(scale, scale)
                    matrix.postTranslate(dx, dy)
                    
                    canvas.drawBitmap(bitmap, matrix, paint)
                } else {
                    canvas.drawColor(Color.WHITE)
                }
            }

            is BackgroundSelection.Gradient -> {
                 // Gradient drawing on Canvas is a bit more complex (LinearGradient shader).
                 // For MVP, let's try to parse colors.
                 val colors = backgroundSelection.item.colors.mapNotNull { 
                     try {
                         Color.parseColor(it)
                     } catch (e: Exception) {
                         null
                     }
                 }
                 
                 if (colors.isNotEmpty()) {
                     if (colors.size == 1) {
                         canvas.drawColor(colors[0])
                     } else {
                         // Vertical Gradient
                         val shader = android.graphics.LinearGradient(
                             0f, 0f, 0f, height.toFloat(),
                             colors.toIntArray(),
                             null,
                             android.graphics.Shader.TileMode.CLAMP
                         )
                         paint.shader = shader
                         canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
                     }
                 } else {
                     canvas.drawColor(Color.WHITE)
                 }
            }
            
            is BackgroundSelection.BackgroundTransparent -> {
                 // Draw transparent (nothing) or the resource if available
                 // For export, usually transparent means... transparent pixels?
                 // But we created bitmap with ARGB_8888 so it is transparent by default.
                 // If there is a resId, we might need to load it.
                 // For now, do nothing (transparent).
            }

            else -> {
                canvas.drawColor(Color.WHITE)
            }
        }
    }

    private suspend fun loadBitmapFromUrl(context: Context, url: String): Bitmap? {
        return withContext(Dispatchers.IO) {
            val loader = ImageLoader(context)
            val request = ImageRequest.Builder(context)
                .data(url)
                .allowHardware(false) // Important for Canvas drawing
                .build()
            
            val result = (loader.execute(request) as? SuccessResult)?.drawable
            (result as? android.graphics.drawable.BitmapDrawable)?.bitmap
        }
    }
}
