package com.amb.photo.ui.activities.editor

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.amb.photo.ui.theme.AppColor
import com.amb.photo.ui.theme.fontFamily
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

// Phạm vi giá trị của thước đo
private const val MIN_VALUE = -30
private const val MAX_VALUE = 30
private const val TOTAL_TICKS = MAX_VALUE - MIN_VALUE + 1 // 61 vạch

// Kích thước cố định của slot (vạch + khoảng trống)
private val TICK_WIDTH = 9.dp

@Composable
fun RulerSelector(
    modifier: Modifier = Modifier,
    onValueChange: (Int) -> Unit
) {
    val listState = rememberLazyListState()
    val density = LocalDensity.current

    // Độ rộng của 1 vạch chia (TICK_WIDTH) tính bằng Pixel
    val tickWidthPx = remember { with(density) { TICK_WIDTH.toPx() } }

    // Tính toán khoảng cách tâm màn hình (ViewPort Center)
    val viewportCenter = remember {
        derivedStateOf { listState.layoutInfo.viewportSize.width / 2 }
    }
    val contentPaddingDp = with(density) { viewportCenter.value.toDp() }


    // 🌟 LOGIC CẬP NHẬT GIÁ TRỊ LIÊN TỤC 🌟
    // Tính toán giá trị hiện tại dựa trên vị trí cuộn (Scroll Offset)
    val currentValue by remember {
        derivedStateOf {
            if (listState.layoutInfo.totalItemsCount == 0) {
                return@derivedStateOf 0
            }

            // Index của item đầu tiên hiển thị
            val firstIndex = listState.firstVisibleItemIndex
            // Scroll offset (px) của item đầu tiên hiển thị
            val firstOffset = listState.firstVisibleItemScrollOffset

            // Số lượng vạch chia bị che khuất (tính bằng TICKs)
            // Vì chúng ta dùng contentPadding = viewportCenter, vị trí cuộn
            // đã được căn chỉnh để start of the list = center of the screen
            val ticksScrolled = firstOffset / tickWidthPx

            // Index tương ứng với vị trí ở tâm (từ 0 đến 60)
            val centerIndexFloat = firstIndex + ticksScrolled

            // Làm tròn để hiển thị giá trị nguyên gần nhất
            val centerIndex = centerIndexFloat.roundToInt()

            // Ánh xạ Index về Giá trị: Index (0..60) -> Value (-30..30)
            (centerIndex.coerceIn(0, TOTAL_TICKS - 1) + MIN_VALUE)
        }
    }

    val maxProgressValue = 30f

    val progress = (kotlin.math.abs(currentValue) / maxProgressValue).coerceIn(0f, 1f)

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(300)
    )

    LaunchedEffect(currentValue) {
        onValueChange(currentValue)
    }
    // Đặt vị trí ban đầu là 0 (index 30)
    // Cần LaunchedEffect để đảm bảo viewportCenter đã có giá trị
    LaunchedEffect(viewportCenter.value) {
        if (viewportCenter.value > 0) {
            // Cuộn đến item index 30 (tương ứng với giá trị 0) với offset = 0
            listState.scrollToItem(30, 0)
        }
    }

    // ----------------------------------------------------------------------
    // 2. Giao diện (UI) - Chỉ dùng currentValue đã tính toán
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val isZero = currentValue == 0
        // Vùng hiển thị giá trị hiện tại
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(
                    if (isZero){
                        Color(0xFFF2F4F7)
                    } else {
                        Color(0xFFF2F0FE)
                    }
                )
        ) {

            // Vòng tròn progress
            CircularProgressIndicator(
                progress = { animatedProgress },
                color = if (isZero) Color(0xFFEAECF0) else Color(0xFF6425F3),
                strokeWidth = 2.dp,
                trackColor = if (isZero) Color(0xFFEAECF0) else Color(0xFFDAD3FD),
                strokeCap = ProgressIndicatorDefaults.CircularDeterminateStrokeCap,
            )

            // Số ở giữa
            Text(
                text = currentValue.toString(),
                style = TextStyle(
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    fontFamily = fontFamily,
                    fontWeight = FontWeight(500),
                    color = if (isZero) Color(0xFF1D2939) else Color(0xFF6425F3),
                    textAlign = TextAlign.Center,
                )
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            contentAlignment = Alignment.Center
        ) {

            // Thanh Active màu xanh ở giữa (Centre Mark)
            Spacer(
                modifier = Modifier
                    .width(1.5.dp)
                    .height(28.dp)
                    .background(Color(0xFF6425F3))
                    .zIndex(1f)
            )

            // LazyRow chứa các vạch chia
            LazyRow(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth(),
                // Đặt padding bằng một nửa viewport để căn chỉnh item 0 vào tâm
                contentPadding = PaddingValues(horizontal = contentPaddingDp),
                horizontalArrangement = Arrangement.spacedBy(TICK_WIDTH),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(TOTAL_TICKS) { index ->
                    // Index (0..60) -> Value (-30..30)
                    val value = index + MIN_VALUE
                    TickMark(value = value)
                }
            }
        }
    }
}

// Hàm Composable cho một vạch chia đơn lẻ
@Composable
fun TickMark(value: Int) {
    // Chiều cao của vạch
    val height = when {
        value % 10 == 0 -> 18.dp // Vạch chính (0, +-10, +-20, +-30)
        value % 5 == 0 -> 10.dp  // Vạch trung bình
        else -> 10.dp            // Vạch nhỏ
    }
    // Màu sắc
    val color = when {
        value % 10 == 0 -> AppColor.Gray400
        else -> {
            Color(0xFFD0D5DD)
        }
    }

    val width = when {
        value % 10 == 0 -> 2.dp
        else -> 1.dp
    }
    // Vạch
    Spacer(
        modifier = Modifier
            .width(1.5.dp) // Độ dày của vạch
            .height(height)
            .background(color)
    )
}

/**
 * Ánh xạ giá trị cuộn (-30 đến 30) sang tỷ lệ zoom và góc xoay.
 * Đã ĐẢO NGƯỢC chiều xoay.
 *
 * @param rulerValue Giá trị từ thước cuộn (-30 đến 30).
 * @return Pair<zoomScale, rotationAngle>
 */
fun mapRulerToScaleAndRotation(rulerValue: Int): Pair<Float, Float> {
    val maxRulerValue = 30.0f
    val maxRotation = 45.0f // Góc xoay tối đa (ví dụ: 45 độ)
    val minScale = 1.0f
    val maxScale = 3.0f
    val scaleRange = maxScale - minScale

    // 1. Tính toán Scale (giữ nguyên, chỉ dựa trên giá trị tuyệt đối)
    val absValue = abs(rulerValue).toFloat()
    val zoomScale = minScale + (absValue / maxRulerValue) * scaleRange

    // 2. Tính toán Rotation (NHÂN VỚI -1 ĐỂ ĐẢO NGƯỢC CHIỀU)
    // Ánh xạ tuyến tính rulerValue (-30..+30) -> angle (-45°..+45°)
    // Đảo ngược: rulerValue (+30) -> angle (-45°), rulerValue (-30) -> angle (+45°)
    val rotationAngle = (rulerValue / maxRulerValue) * maxRotation * -1.0f // ⭐️ THÊM * -1.0f

    return Pair(zoomScale, rotationAngle)
}