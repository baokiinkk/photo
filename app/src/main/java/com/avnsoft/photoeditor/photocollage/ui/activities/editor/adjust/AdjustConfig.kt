package com.avnsoft.photoeditor.photocollage.ui.activities.editor.adjust

import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import android.util.Log
import android.view.PixelCopy
import android.view.View
import android.view.Window
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.roundToIntRect
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import com.basesource.base.ui.base.BaseActivity


fun getBrightness(brightness: Float): Float {
    val result = calcIntensity(
        intensity = brightness / 100f,
        minValue = -1f,
        maxValue = 1f,
        originValue = 0f
    )
    Log.d("BRIGHTNESS", "$result")
    return result
}

fun getContrast(contrast: Float): Float {
    val result = calcIntensity(
        intensity = contrast / 100f,
        minValue = 0.1f,
        maxValue = 5.0f,
        originValue = 1.0f
    )
    Log.d("CONTRAST", "$result")
    return result
}

fun getSaturation(
    saturation: Float
): Float {
    val result = calcIntensity(
        intensity = saturation / 100f,
        minValue = 0.0f,
        maxValue = 5.0f,
        originValue = 1.0f
    )
    Log.d("SATURATION", "$result")
    return result
}

fun getWarmth(
    warmth: Float
): Float {
    val result = calcIntensity(
        intensity = warmth / 100f,
        minValue = -1f,
        maxValue = 1f,
        originValue = 0.0f
    )
    Log.d("WARMTH", "$result")
    return result
}

fun getFade(
    fade: Float
): Float {
    val result = calcIntensity(
        intensity = fade / 100f,
        minValue = 0.0f,
        maxValue = 2.0f,
        originValue = 1.0f
    )
    Log.d("FADE", "$result")
    return result
}

fun getHighlight(
    highlight: Float
): Float {
    val result = calcIntensity(
        intensity = highlight / 100f,
        minValue = -100f,
        maxValue = 200f,
        originValue = 0.0f
    )
    Log.d("HIGHLIGHT", "$result")
    return result
}

fun getShadow(
    shadow: Float
): Float {
    val result = calcIntensity(
        intensity = shadow / 100f,
        minValue = -200f,
        maxValue = 100f,
        originValue = 0.0f
    )
    Log.d("SHADOW", "$result")
    return result
}

fun getHue(
    hue: Float
): Float {
    val result = calcIntensity(
        intensity = hue / 100f,
        minValue = 0.0f,
        maxValue = 6f,
        originValue = 0.0f
    )
    Log.d("HUE", "$result")
    return result
}

fun getVignette(
    vignette: Float
): Float {
    val result = calcIntensity(
        intensity = vignette / 100f,
        minValue = 0.0f,
        maxValue = 1f,
        originValue = 0.5f
    )
    Log.d("VIGNETTE", "$result")
    return result
}

fun getSharpen(
    sharpen: Float
): Float {
    val result = calcIntensity(
        intensity = sharpen / 100f,
        minValue = 0.0f,
        maxValue = 10f,
        originValue = 0.0f
    )
    Log.d("SHARPEN", "$result")
    return result
}

fun getGrain(
    grain: Float
): Float {
    val result = calcIntensity(
        intensity = grain / 100f,
        minValue = 0.0f,
        maxValue = 10f,
        originValue = 0.0f
    )
    Log.d("GRAIN", "$result")
    return result
}


// 1. Modifier để lấy vị trí và kích thước
fun Modifier.captureComposableBounds(onBoundsChange: (IntRect) -> Unit) =
    onGloballyPositioned { coordinates ->
        val boundsInWindow = coordinates.boundsInWindow().roundToIntRect()
        onBoundsChange(boundsInWindow)
    }
//
///**
// * Chụp một khu vực của View thành Bitmap bằng cách sử dụng Canvas.
// * Đây là phương pháp tương thích với mọi phiên bản Android (dù có thể không chụp được SurfaceView).
// *
// * @param view View chứa Compose.
// * @param rect Khu vực (bounds) của Composable muốn chụp.
// * @return Bitmap của khu vực đã chụp.
// */
//suspend fun captureViewToBitmapAllDevices(view: View, rect: IntRect): Bitmap =
//    withContext(Dispatchers.Main) { // Phải chạy trên Main Thread
//        val bitmap = createBitmap(rect.width, rect.height)
//        val canvas = Canvas(bitmap)
//
//        // Dịch chuyển Canvas để căn chỉnh (chỉ vẽ khu vực Composable)
//        canvas.translate(-rect.left.toFloat(), -rect.top.toFloat())
//
//        // Yêu cầu View vẽ chính nó và các Composable con lên Canvas
//        view.draw(canvas)
//
//        return@withContext bitmap
//    }
//
//fun convertToSoftwareBitmap(hardwareBitmap: Bitmap): Bitmap {
//    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && hardwareBitmap.config == Bitmap.Config.HARDWARE) {
//        // Tạo một bản sao mới với cấu hình ARGB_8888 (Software)
//        return hardwareBitmap.copy(Bitmap.Config.ARGB_8888, true)
//    }
//    return hardwareBitmap
//}
//

suspend fun captureViewToBitmapPixelCopy(view: View, window: Window, rect: IntRect): Bitmap =
    suspendCancellableCoroutine { continuation ->
        val bitmap = Bitmap.createBitmap(rect.width, rect.height, Bitmap.Config.ARGB_8888)
        val viewRect = android.graphics.Rect(rect.left, rect.top, rect.right, rect.bottom)

        PixelCopy.request(
            window,
            viewRect,
            bitmap,
            { copyResult ->
                if (copyResult == PixelCopy.SUCCESS) {
                    continuation.resume(bitmap)
                } else {
                    continuation.cancel(Exception("PixelCopy failed: $copyResult"))
                }
            },
            view.handler
        )
    }

// Hàm tương thích mọi thiết bị (API < 26)
suspend fun captureViewToBitmapAllDevices(view: View, rect: IntRect): Bitmap =
    withContext(Dispatchers.Main) {
        val bitmap = Bitmap.createBitmap(rect.width, rect.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.translate(-rect.left.toFloat(), -rect.top.toFloat())
        view.draw(canvas)
        return@withContext bitmap
    }

// Hàm điều phối chính
suspend fun captureComposableToBitmapFinal(view: View, rect: IntRect): Bitmap {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        // Lấy Window từ View Context
        val window =
            (view.context as? BaseActivity)?.window ?: throw IllegalStateException("Window not found")
        return captureViewToBitmapPixelCopy(view, window, rect)
    } else {
        return captureViewToBitmapAllDevices(view, rect)
    }
}