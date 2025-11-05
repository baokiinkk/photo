package com.amb.photo.ui.activities.editor.crop

import android.graphics.Bitmap
import androidx.compose.ui.geometry.Rect
import com.amb.photo.R


// 🟣 CropState chứa toàn bộ trạng thái hiện tại
data class CropState(
    val cropRect: Rect = Rect.Zero,
    val aspect: CropAspect = CropAspect.RATIO_1_1,
    val activeCorner: String? = null,
    val isMoving: Boolean = false,
    val zoomScale: Float = 1f,
    val rotationAngle: Float = 0f,
    val id: String = CropAspect.RATIO_1_1.label,
    val rotateImage: Float = 0f,
    val bitmap: Bitmap? = null
)


data class IconAspect(
    val resId: Int,
    val width: Int,
    val height: Int
)

// 🟣 Enum xác định chế độ crop
enum class CropAspect(
    val label: String,
    var ratio: Pair<Int, Int>?,
    val iconAspect: IconAspect
) {
    ORIGINAL(
        "Original",
        null,
        IconAspect(
            resId = R.drawable.ic_original,
            width = 48,
            height = 48
        )
    ),
    FREE(
        "Free", null, IconAspect(
            resId = R.drawable.ic_free,
            width = 48,
            height = 48
        )
    ),
    RATIO_1_1(
        "1:1", 1 to 1, IconAspect(
            resId = R.drawable.ic_1_1,
            width = 48,
            height = 48
        )
    ),
    RATIO_4_5(
        "4:5", 4 to 5, IconAspect(
            resId = R.drawable.ic_4_5,
            width = 48,
            height = 60
        )
    ),
    RATIO_5_4(
        "5:4", 5 to 4, IconAspect(
            resId = R.drawable.ic_5_4,
            width = 60,
            height = 48
        )
    ),
    RATIO_3_4(
        "3:4", 3 to 4, IconAspect(
            resId = R.drawable.ic_3_4,
            width = 36,
            height = 48
        )
    ),
    RATIO_4_3(
        "4:3", 4 to 3, IconAspect(
            resId = R.drawable.ic_4_3,
            width = 48,
            height = 36
        )
    ),
    RATIO_2_3(
        "2:3", 2 to 3, IconAspect(
            resId = R.drawable.ic_2_3,
            width = 36,
            height = 54
        )
    ),
    RATIO_3_2(
        "3:2", 3 to 2, IconAspect(
            resId = R.drawable.ic_3_2,
            width = 48,
            height = 32
        )
    ),
    RATIO_9_16(
        "9:16", 9 to 16, IconAspect(
            resId = R.drawable.ic_9_16,
            width = 36,
            height = 64
        )
    ),
    RATIO_16_9(
        "16:9", 16 to 9, IconAspect(
            resId = R.drawable.ic_16_9,
            width = 64,
            height = 36
        )
    ),
    RATIO_1_2(
        "1:2", 1 to 2, IconAspect(
            resId = R.drawable.ic_1_2,
            width = 32,
            height = 64
        )
    );
    companion object{
        fun Pair<Int, Int>?.toAspectRatio(): Float {
            return this?.let { (w, h) ->
                if (h != 0) w.toFloat() / h.toFloat() else 1f
            } ?: 1f
        }
    }
}
