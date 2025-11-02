package com.amb.photo.ui.activities.editor.blur

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.amb.photo.R
import com.basesource.base.utils.clickableWithAlphaEffect
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.wysaid.view.ImageGLSurfaceView
import kotlin.math.roundToInt

enum class BlurType(val label: String) {
    NONE("None"),
    CLASSIC("Classic"),
    CENTER("Center"),
    LINE("Line"),
    MOTION("Motion")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageBlurScreen(
    originalBitmap: Bitmap
) {
    val context = LocalContext.current
    val imageProcessor = remember { ImageBlurProcessor(context) }

    // Đảm bảo giải phóng RenderScript khi Composable bị dispose
    DisposableEffect(Unit) {
        onDispose {
            imageProcessor.destroy()
        }
    }


    var currentBitmap by remember { mutableStateOf(originalBitmap.asImageBitmap()) }
    var selectedBlurType by remember { mutableStateOf(BlurType.NONE) }
    var blurRadius by remember { mutableStateOf(15f) } // Bán kính mờ cho Classic/Center

    val coroutineScope = rememberCoroutineScope()

    // Hàm áp dụng hiệu ứng mờ
    val applyBlur: (BlurType) -> Unit = { type ->
        selectedBlurType = type
        coroutineScope.launch {
            val processedBitmap = withContext(Dispatchers.Default) {
                when (type) {
                    BlurType.NONE -> originalBitmap
                    BlurType.CLASSIC -> imageProcessor.applyClassicBlur(originalBitmap, blurRadius)
                    BlurType.CENTER -> imageProcessor.applyCenterBlur(originalBitmap, blurRadius, 0.3f)
                    // TODO: Triển khai Line và Motion Blur
                    BlurType.LINE -> originalBitmap // Placeholder
                    BlurType.MOTION -> originalBitmap // Placeholder
                }
            }
            processedBitmap?.let {
                currentBitmap = it.asImageBitmap()
            }
        }
    }

    // Áp dụng lại mờ khi blurRadius thay đổi
    LaunchedEffect(blurRadius, selectedBlurType) {
        if (selectedBlurType != BlurType.NONE) {
            applyBlur(selectedBlurType)
        }
    }

    Scaffold(
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.8f))
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Thanh trượt điều chỉnh độ mờ (Blur Radius)
                Slider(
                    value = blurRadius,
                    onValueChange = { blurRadius = it },
                    valueRange = 1f..25f,
                    steps = 24, // 25 - 1 = 24 bước
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = Color.White
                    )
                )
                Text(
                    text = "Blur Radius: ${blurRadius.roundToInt()}",
                    color = Color.White,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Các lựa chọn kiểu mờ
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    itemsIndexed(BlurType.values()) { _, type ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                                .clickableWithAlphaEffect() { applyBlur(type) }
                        ) {
                            // Hình ảnh đại diện cho kiểu mờ (ví dụ ảnh nhỏ đã mờ)
                            // Hiện tại dùng ảnh gốc làm placeholder
                            Image(
                                bitmap = originalBitmap.asImageBitmap(), // Thay bằng ảnh thumbnail nếu có
                                contentDescription = type.label,
                                modifier = Modifier
                                    .size(60.dp)
                                    .background(
                                        if (selectedBlurType == type) Color.Red else Color.Gray,
                                        shape = MaterialTheme.shapes.small
                                    )
                                    .padding(4.dp)
                                    .clickableWithAlphaEffect() { applyBlur(type) },
                                contentScale = ContentScale.Crop
                            )
                            Text(
                                text = type.label,
                                color = if (selectedBlurType == type) Color.Red else Color.White,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        // Vùng hiển thị ảnh chính
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Image(
                bitmap = currentBitmap,
                contentDescription = "Edited Image",
                modifier = Modifier.fillMaxWidth(0.8f), // Tỉ lệ ảnh trong màn hình
                contentScale = ContentScale.Fit
            )
//            // Icon âm lượng như trong ảnh ví dụ (nếu cần)
//            Icon(
//                // Thay bằng Icon của bạn
//                // painter = painterResource(id = R.drawable.ic_volume),
//                // contentDescription = "Volume",
//                // modifier = Modifier.align(Alignment.TopEnd).padding(16.dp),
//                // tint = Color.White
//            )
        }
    }
}


class ImageBlurProcessor(private val context: Context) {

    private var rs: RenderScript? = null

    init {
        try {
            rs = RenderScript.create(context)
        } catch (e: Exception) {
            Log.e("ImageBlurProcessor", "Failed to create RenderScript: ${e.message}")
            rs = null // Đảm bảo rs là null nếu tạo thất bại
        }
    }

    // Đảm bảo giải phóng tài nguyên RenderScript khi không cần nữa
    fun destroy() {
        rs?.destroy()
        rs = null
    }

    /**
     * Áp dụng hiệu ứng mờ Gaussian (Classic Blur).
     * @param bitmapToBlur Bitmap gốc.
     * @param radius Bán kính mờ (1-25f).
     * @return Bitmap đã được làm mờ.
     */
    fun applyClassicBlur(bitmapToBlur: Bitmap, radius: Float = 15f): Bitmap? {
        if (rs == null || bitmapToBlur.isRecycled) return null
        val outputBitmap = Bitmap.createBitmap(bitmapToBlur.width, bitmapToBlur.height, bitmapToBlur.config!!)

        try {
            val input = Allocation.createFromBitmap(rs, bitmapToBlur)
            val output = Allocation.createFromBitmap(rs, outputBitmap)
            val blur = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))

            blur.setRadius(radius.coerceIn(1f, 25f))
            blur.setInput(input)
            blur.forEach(output)
            output.copyTo(outputBitmap)

            input.destroy()
            output.destroy()
            blur.destroy()
        } catch (e: Exception) {
            Log.e("ImageBlurProcessor", "Error applying classic blur: ${e.message}")
            return null
        }
        return outputBitmap
    }

    /**
     * Áp dụng hiệu ứng làm mờ mọi thứ NGOẠI TRỪ vùng trung tâm (Center Blur).
     * Đây là sự kết hợp giữa mờ Gaussian và một mask tròn.
     * @param originalBitmap Bitmap gốc.
     * @param blurRadius Bán kính mờ cho vùng ngoài (1-25f).
     * @param centerRadius Tỷ lệ bán kính của vùng không mờ ở trung tâm (0.0f - 1.0f).
     * @return Bitmap đã được làm mờ vùng ngoài.
     */
    fun applyCenterBlur(originalBitmap: Bitmap, blurRadius: Float = 20f, centerRadius: Float = 0.3f): Bitmap? {
        if (rs == null || originalBitmap.isRecycled) return null

        val width = originalBitmap.width
        val height = originalBitmap.height

        // Tạo một bản sao có thể thay đổi
        val mutableBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true)

        // 1. Tạo bản mờ hoàn toàn của ảnh gốc
        val blurredBitmap = applyClassicBlur(mutableBitmap, blurRadius) ?: return null

        // 2. Tạo mask hình tròn (phần KHÔNG mờ)
        val maskBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ALPHA_8)
        val canvas = Canvas(maskBitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = ContextCompat.getColor(context, android.R.color.black) // Màu đen sẽ là vùng trong suốt (không ảnh hưởng)
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint) // Fill đen toàn bộ

        paint.color = ContextCompat.getColor(context, android.R.color.white)// Màu trắng sẽ là vùng giữ lại (không mờ)
        val radiusPx = kotlin.math.min(width, height) * centerRadius / 2
        canvas.drawCircle(width / 2f, height / 2f, radiusPx, paint)

        // 3. Sử dụng PorterDuffXfermode để kết hợp ảnh gốc và ảnh mờ
        val outputBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val resultCanvas = Canvas(outputBitmap)

        // Vẽ ảnh mờ lên Canvas
        resultCanvas.drawBitmap(blurredBitmap, 0f, 0f, null)

        // Đặt chế độ hòa trộn: SRC_ATOP để vẽ ảnh gốc lên trên chỉ ở vùng mask trắng
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP)
        resultCanvas.drawBitmap(originalBitmap, 0f, 0f, paint)
        paint.xfermode = null // Reset xfermode

        mutableBitmap.recycle()
        blurredBitmap.recycle()
        maskBitmap.recycle()

        return outputBitmap
    }

    // Thêm các hàm khác cho Line Blur, Motion Blur nếu cần
    // Line Blur: Cần một RenderScript kernel tùy chỉnh hoặc áp dụng mờ Gaussian nhiều lần theo hướng
    // Motion Blur: Phức tạp hơn, thường cần shader hoặc kernel tùy chỉnh.
}

//
//fun filterConfig(type: String, intensity: Float): String {
//    val power = (intensity / 100f) * 5f // chuyển 0–100 thành 0–5
//
//    return when (type) {
//        "Classic" -> "@blur gauss ${power}" // Gaussian blur
//        "Center" -> "@blur zoom ${power}"   // Zoom blur
//        "Line" -> "@blur line ${power}"     // Linear blur
//        "Motion" -> "@blur motion ${power}" // Motion blur
//        "Radial" -> "@blur radial ${power}" // Radial blur
//        else -> "@blur gauss ${power}"
//    }
//}
