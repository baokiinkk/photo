package com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.preview

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import com.avnsoft.photoeditor.photocollage.data.model.collage.CollageTemplate
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.CollagePreviewDataProcessor
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.ProcessedCellData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.max
import kotlin.math.min

object ImageTransformCalculator {

    suspend fun calculateInitialTransforms(
        context: Context,
        processedCells: List<ProcessedCellData>
    ): Map<Int, ImageTransformState> {
        return withContext(Dispatchers.IO) {
            processedCells.mapIndexed { index, cell ->
                val size = getImageSizeFromUri(context, cell.imageUri)
                val scale = if (size != null) {
                    calculateFillScale(
                        boundWidth = cell.width,
                        boundHeight = cell.height,
                        imageWidth = size.width,
                        imageHeight = size.height
                    )
                } else {
                    1f
                }
                index to ImageTransformState(offset = Offset.Zero, scale = scale)
            }.toMap()
        }
    }

    suspend fun calculateInitialTransformsFromTemplate(
        context: Context,
        template: CollageTemplate,
        images: List<Uri>,
        canvasWidth: Float,
        canvasHeight: Float
    ): Map<Int, ImageTransformState> {
        val cells = CollagePreviewDataProcessor.processTemplate(
            template = template,
            images = images,
            canvasWidth = canvasWidth,
            canvasHeight = canvasHeight
        )
        return calculateInitialTransforms(context, cells)
    }

    private fun calculateFillScale(
        boundWidth: Float,
        boundHeight: Float,
        imageWidth: Float,
        imageHeight: Float
    ): Float {
        val widthRatio = boundWidth / imageWidth
        val heightRatio = boundHeight / imageHeight
        val fitScale = min(widthRatio, heightRatio)
        val fillScale = max(widthRatio, heightRatio)
        return if (fitScale > 0f) fillScale / fitScale else 1f
    }

    private suspend fun getImageSizeFromUri(context: Context, uri: Uri): Size? {
        return try {
            withContext(Dispatchers.IO) {
                val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                val inputStream = context.contentResolver.openInputStream(uri)
                inputStream?.use {
                    BitmapFactory.decodeStream(it, null, options)
                    if (options.outWidth > 0 && options.outHeight > 0) {
                        Size(options.outWidth.toFloat(), options.outHeight.toFloat())
                    } else {
                        null
                    }
                }
            }
        } catch (_: Exception) {
            null
        }
    }
}

