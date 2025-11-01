package com.amb.photo.ui.activities.editor

import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.PaintingStyle.Companion.Stroke
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.amb.photo.R
import com.basesource.base.ui.base.BaseActivity
import com.basesource.base.utils.clickableWithAlphaEffect
import kotlin.math.roundToInt

// 🟣 Enum xác định chế độ crop
enum class CropAspect(val label: String, val ratio: Pair<Int, Int>?) {
    ORIGINAL("Original", null),
    FREE("Free", null),
    RATIO_1_1("1:1", 1 to 1),
    RATIO_4_5("4:5", 4 to 5),
    RATIO_5_4("5:4", 5 to 4)
}

// 🟣 CropState chứa toàn bộ trạng thái hiện tại
data class CropState(
    val cropRect: Rect = Rect.Zero,
    val aspect: CropAspect = CropAspect.RATIO_1_1,
    val activeCorner: String? = null,
    val isMoving: Boolean = false
)

class EditorActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Scaffold(
                containerColor = Color(0xFFF2F4F8)
            ) { inner ->
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = inner.calculateTopPadding())
                        .background(color = Color(0xFFF2F4F8))

                ) {
                    var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }

                    if (imageBitmap == null) {
                        PickImageFromGallery { picked -> imageBitmap = picked }
                    } else {
                        CropImageScreen(imageBitmap!!)
                    }
                    RulerSelector(
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun PickImageFromGallery(onImagePicked: (ImageBitmap) -> Unit) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val bitmap = if (Build.VERSION.SDK_INT < 28) {
                MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            } else {
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                ImageDecoder.decodeBitmap(source)
            }
            onImagePicked(bitmap.asImageBitmap())
        }
    }

    Button(onClick = { launcher.launch("image/*") }) {
        Text("Chọn ảnh từ thư viện")
    }
}


@Composable
fun CropImageScreen(imageBitmap: ImageBitmap) {
    var cropState by remember { mutableStateOf(CropState()) }
    var imageBounds by remember { mutableStateOf(IntSize.Zero) }
    val overlayColor = Color(0f, 0f, 0f, 0.6f)
    val density = LocalDensity.current

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 35.dp)
                    .weight(1f)
                    .padding(bottom = 16.dp)
            ) {
                // 🟣 Ảnh hiển thị
                Image(
                    bitmap = imageBitmap,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .onGloballyPositioned {
                            imageBounds = it.size
                        }
                )

                if (imageBounds != IntSize.Zero) {
                    val canvasWidth = imageBounds.width.toFloat()
                    val canvasHeight = imageBounds.height.toFloat()

                    // 🟣 Canvas hiển thị đúng trên vùng ảnh
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(cropState.aspect) {
                                detectDragGestures(
                                    onDragStart = { offset ->
                                        if (cropState.aspect == CropAspect.ORIGINAL) return@detectDragGestures

                                        val rect = cropState.cropRect
                                        val cornerRadius = 80f
                                        var activeCorner: String? = null
                                        var isMoving = false

                                        if (cropState.aspect == CropAspect.FREE) {
                                            activeCorner = when {
                                                (offset - Offset(
                                                    rect.left,
                                                    rect.top
                                                )).getDistance() < cornerRadius -> "TL"

                                                (offset - Offset(
                                                    rect.right,
                                                    rect.top
                                                )).getDistance() < cornerRadius -> "TR"

                                                (offset - Offset(
                                                    rect.left,
                                                    rect.bottom
                                                )).getDistance() < cornerRadius -> "BL"

                                                (offset - Offset(
                                                    rect.right,
                                                    rect.bottom
                                                )).getDistance() < cornerRadius -> "BR"

                                                rect.contains(offset) -> {
                                                    isMoving = true; null
                                                }

                                                else -> null
                                            }
                                        } else if (rect.contains(offset)) {
                                            isMoving = true
                                        }

                                        cropState = cropState.copy(
                                            activeCorner = activeCorner,
                                            isMoving = isMoving
                                        )
                                    },
                                    onDragEnd = {
                                        cropState =
                                            cropState.copy(activeCorner = null, isMoving = false)
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consume()

                                        if (cropState.aspect == CropAspect.ORIGINAL) return@detectDragGestures

                                        val dx = dragAmount.x
                                        val dy = dragAmount.y
                                        val rect = cropState.cropRect
                                        val minSize = 100f
                                        var newRect = rect

                                        if (cropState.isMoving) {
                                            newRect = rect.translate(dragAmount)
                                            val shiftX = when {
                                                newRect.left < 0 -> -newRect.left
                                                newRect.right > canvasWidth -> canvasWidth - newRect.right
                                                else -> 0f
                                            }
                                            val shiftY = when {
                                                newRect.top < 0 -> -newRect.top
                                                newRect.bottom > canvasHeight -> canvasHeight - newRect.bottom
                                                else -> 0f
                                            }

                                            cropState = cropState.copy(
                                                cropRect = newRect.translate(Offset(shiftX, shiftY))
                                            )
                                            return@detectDragGestures
                                        }

                                        if (cropState.aspect == CropAspect.FREE) {
                                            newRect = when (cropState.activeCorner) {
                                                "TL" -> Rect(
                                                    (rect.left + dx).coerceIn(
                                                        0f,
                                                        rect.right - minSize
                                                    ),
                                                    (rect.top + dy).coerceIn(
                                                        0f,
                                                        rect.bottom - minSize
                                                    ),
                                                    rect.right, rect.bottom
                                                )

                                                "TR" -> Rect(
                                                    rect.left,
                                                    (rect.top + dy).coerceIn(
                                                        0f,
                                                        rect.bottom - minSize
                                                    ),
                                                    (rect.right + dx).coerceIn(
                                                        rect.left + minSize,
                                                        canvasWidth
                                                    ),
                                                    rect.bottom
                                                )

                                                "BL" -> Rect(
                                                    (rect.left + dx).coerceIn(
                                                        0f,
                                                        rect.right - minSize
                                                    ),
                                                    rect.top,
                                                    rect.right,
                                                    (rect.bottom + dy).coerceIn(
                                                        rect.top + minSize,
                                                        canvasHeight
                                                    )
                                                )

                                                "BR" -> Rect(
                                                    rect.left, rect.top,
                                                    (rect.right + dx).coerceIn(
                                                        rect.left + minSize,
                                                        canvasWidth
                                                    ),
                                                    (rect.bottom + dy).coerceIn(
                                                        rect.top + minSize,
                                                        canvasHeight
                                                    )
                                                )

                                                else -> rect
                                            }
                                            cropState = cropState.copy(cropRect = newRect)
                                        }
                                    }
                                )
                            }
                    ) {
                        val rect = cropState.cropRect

                        // 🟣 Khởi tạo cropRect ban đầu theo kích thước ảnh
                        if (rect == Rect.Zero) {
                            val padding = with(density) { 10.dp.toPx() }
                            val width = canvasWidth - 2 * padding
                            val height = width
                            val left = padding
                            val top = (canvasHeight - height) / 2f
                            cropState = cropState.copy(
                                cropRect = Rect(
                                    left,
                                    top,
                                    left + width,
                                    top + height
                                )
                            )
                        }

                        // vùng tối bên ngoài
                        val path = Path().apply {
                            addRect(Rect(0f, 0f, canvasWidth, canvasHeight))
                            addRect(cropState.cropRect)
                            fillType = PathFillType.EvenOdd
                        }
                        drawPath(path, overlayColor)

                        // khung trắng
                        drawRect(
                            Color.White,
                            Offset(cropState.cropRect.left, cropState.cropRect.top),
                            Size(cropState.cropRect.width, cropState.cropRect.height),
                            style = Stroke(width = 2.dp.toPx())
                        )

                        // 3x3 grid lines inside rect
                        val gridColor = Color.White.copy(alpha = 0.6f)
                        val stepX = rect.width / 3f
                        val stepY = rect.height / 3f
                        val gridStroke = 1.dp.toPx()
                        for (i in 1..2) {
                            // vertical
                            drawLine(
                                color = gridColor,
                                start = Offset(rect.left + stepX * i, rect.top),
                                end = Offset(rect.left + stepX * i, rect.bottom),
                                strokeWidth = gridStroke
                            )
                            // horizontal
                            drawLine(
                                color = gridColor,
                                start = Offset(rect.left, rect.top + stepY * i),
                                end = Offset(rect.right, rect.top + stepY * i),
                                strokeWidth = gridStroke
                            )
                        }


                        // 4 chấm góc
                        val handleRadius = 6.dp.toPx()
                        drawCircle(Color.White, handleRadius, Offset(rect.left, rect.top))
                        drawCircle(Color.White, handleRadius, Offset(rect.right, rect.top))
                        drawCircle(Color.White, handleRadius, Offset(rect.left, rect.bottom))
                        drawCircle(Color.White, handleRadius, Offset(rect.right, rect.bottom))

                        val barLength = 24.dp.toPx()
                        val barWidth = 6.dp.toPx()
                        drawLine(
                            Color.White,
                            Offset(
                                cropState.cropRect.center.x - barLength / 2,
                                cropState.cropRect.top
                            ),
                            Offset(
                                cropState.cropRect.center.x + barLength / 2,
                                cropState.cropRect.top
                            ),
                            strokeWidth = barWidth,
                            cap = StrokeCap.Round
                        )
                        drawLine(
                            Color.White,
                            Offset(
                                cropState.cropRect.center.x - barLength / 2,
                                cropState.cropRect.bottom
                            ),
                            Offset(
                                cropState.cropRect.center.x + barLength / 2,
                                cropState.cropRect.bottom
                            ),
                            strokeWidth = barWidth,
                            cap = StrokeCap.Round
                        )
                        drawLine(
                            Color.White,
                            Offset(
                                cropState.cropRect.left,
                                cropState.cropRect.center.y - barLength / 2
                            ),
                            Offset(
                                cropState.cropRect.left,
                                cropState.cropRect.center.y + barLength / 2
                            ),
                            strokeWidth = barWidth,
                            cap = StrokeCap.Round
                        )
                        drawLine(
                            Color.White,
                            Offset(
                                cropState.cropRect.right,
                                cropState.cropRect.center.y - barLength / 2
                            ),
                            Offset(
                                cropState.cropRect.right,
                                cropState.cropRect.center.y + barLength / 2
                            ),
                            strokeWidth = barWidth,
                            cap = StrokeCap.Round
                        )
                    }
                }
            }

            // 🟣 UI chọn tỉ lệ (đè lên hình)
            CropControlPanel(
                onCancel = { },
                onApply = { },
                onFormat = { aspect ->
                    if (imageBounds == IntSize.Zero) return@CropControlPanel

                    val canvasWidth = imageBounds.width.toFloat()
                    val canvasHeight = imageBounds.height.toFloat()
                    val padding = with(density) { 10.dp.toPx() }

                    val newRect = when (aspect) {
                        CropAspect.ORIGINAL -> {
                            // 🟣 Full khung ảnh
                            Rect(0f, 0f, canvasWidth, canvasHeight)
                        }

                        CropAspect.FREE -> {
                            // 🟣 Giữ nguyên khung hiện tại
                            cropState.cropRect
                        }

                        else -> {
                            // 🟣 Tính theo tỉ lệ cố định
                            val (rw, rh) = aspect.ratio ?: (1 to 1)
                            val width = canvasWidth - 2 * padding
                            val height = width * rh / rw
                            val left = padding
                            val top = (canvasHeight - height) / 2f
                            Rect(left, top, left + width, top + height)
                        }
                    }

                    cropState = cropState.copy(
                        aspect = aspect,
                        cropRect = newRect
                    )
                }
            )
        }
    }
}


@Composable
fun CropControlPanel(
    modifier: Modifier = Modifier,
    onCancel: () -> Unit,
    onApply: () -> Unit,
    onFormat: (CropAspect) -> Unit
) {
    val selectedTab = remember { mutableStateOf("Format") }
//    val formatList = listOf("Original", "Free", "1:1", "4:5", "5:4")
    val positionList = listOf("Horizontal", "Vertical", "Rotate")

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Tabs
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            listOf("Format", "Position").forEach { tab ->
                val isSelected = selectedTab.value == tab
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(if (isSelected) Color(0xFF7C4DFF) else Color(0xFFF2F2F2))
                        .clickable { selectedTab.value = tab }
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = tab,
                        color = if (isSelected) Color.White else Color.Black,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }
        }

        // Center slider or options depending on tab
        if (selectedTab.value == "Format") {
            // Fake alignment slider (visual only)
            Slider(
                value = 0f,
                onValueChange = {},
                valueRange = -100f..100f,
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFF7C4DFF),
                    activeTrackColor = Color(0xFF7C4DFF)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(CropAspect.entries.toTypedArray()) { aspect ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFF5F5F5))
                            .padding(horizontal = 20.dp, vertical = 12.dp)
                            .clickableWithAlphaEffect {
                                onFormat.invoke(aspect)
                            }
                    ) {
                        Text(text = aspect.label, color = Color.Black)
                    }
                }
            }
        } else {
            // Position tab (horizontal / vertical / rotate)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                items(positionList) { label ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { /* handle flip/rotate */ }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF5F5F5)),
                            contentAlignment = Alignment.Center
                        ) {
                            // 👉 Thay bằng icon thật (flip, rotate)
                            Icon(
                                imageVector = Icons.Default.Face, // ví dụ
                                contentDescription = label,
                                tint = Color.Black
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = label, fontSize = 12.sp, color = Color.Black)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Bottom row (Cancel / Confirm)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onCancel) {
                Icon(Icons.Default.Close, contentDescription = "Cancel", tint = Color.Black)
            }
            Text(text = "Crop", fontSize = 16.sp, fontWeight = FontWeight.Medium)
            IconButton(onClick = onApply) {
                Icon(Icons.Default.Check, contentDescription = "Apply", tint = Color.Black)
            }
        }
    }
}

