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
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.amb.photo.R
import com.basesource.base.ui.base.BaseActivity
class EditorActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }

                if (imageBitmap == null) {
                    PickImageFromGallery { picked ->
                        imageBitmap = picked
                    }
                } else {
                    CropImageScreen(imageBitmap = imageBitmap!!)
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
fun CropImageScreen(
    imageBitmap: ImageBitmap,
    modifier: Modifier = Modifier
) {
    var cropRect by remember { mutableStateOf(Rect.Zero) }
    var ratio by remember { mutableStateOf(Pair(1, 1)) }
    var canvasSize by remember { mutableStateOf(Size.Zero) }
    val overlayColor = Color(0f, 0f, 0f, 0.6f)
    val density = LocalDensity.current

    var aspectRatioMode by remember { mutableStateOf("1:1") }

    var activeCorner by remember { mutableStateOf<String?>(null) }
    var isMoving by remember { mutableStateOf(false) }

    LaunchedEffect(canvasSize) {
        if (canvasSize != Size.Zero && cropRect == Rect.Zero) {
            val padding = with(density) { 40.dp.toPx() }
            val width = canvasSize.width - 2 * padding
            val height = width // 1:1
            val left = padding
            val top = (canvasSize.height - height) / 2f
            cropRect = Rect(left, top, left + width, top + height)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Image(
            bitmap = imageBitmap,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(aspectRatioMode) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            if (aspectRatioMode == "Original") {
                                activeCorner = null
                                isMoving = false
                                return@detectDragGestures
                            }

                            val cornerRadius = 80f // tăng vùng chạm
                            val rect = cropRect

                            if (aspectRatioMode == "Free") {
                                activeCorner = when {
                                    (offset - Offset(rect.left, rect.top)).getDistance() < cornerRadius -> "TL"
                                    (offset - Offset(rect.right, rect.top)).getDistance() < cornerRadius -> "TR"
                                    (offset - Offset(rect.left, rect.bottom)).getDistance() < cornerRadius -> "BL"
                                    (offset - Offset(rect.right, rect.bottom)).getDistance() < cornerRadius -> "BR"
                                    rect.contains(offset) -> {
                                        isMoving = true
                                        null
                                    }
                                    else -> null
                                }
                            } else {
                                // tỉ lệ cố định → chỉ cho di chuyển
                                if (rect.contains(offset)) {
                                    isMoving = true
                                }
                            }
                        },
                        onDragEnd = {
                            activeCorner = null
                            isMoving = false
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()

                            if (aspectRatioMode == "Original") return@detectDragGestures

                            val dx = dragAmount.x
                            val dy = dragAmount.y
                            val minSize = 100f
                            val maxWidth = canvasSize.width
                            val maxHeight = canvasSize.height
                            var newRect = cropRect

                            if (isMoving) {
                                // di chuyển toàn khung
                                newRect = cropRect.translate(dragAmount)

                                // giữ trong vùng ảnh
                                val shiftX = when {
                                    newRect.left < 0 -> -newRect.left
                                    newRect.right > maxWidth -> maxWidth - newRect.right
                                    else -> 0f
                                }
                                val shiftY = when {
                                    newRect.top < 0 -> -newRect.top
                                    newRect.bottom > maxHeight -> maxHeight - newRect.bottom
                                    else -> 0f
                                }

                                cropRect = newRect.translate(Offset(shiftX, shiftY))
                                return@detectDragGestures
                            }

                            // free mode → co giãn 4 góc
                            if (aspectRatioMode == "Free") {
                                newRect = when (activeCorner) {
                                    "TL" -> Rect(
                                        (cropRect.left + dx).coerceIn(0f, cropRect.right - minSize),
                                        (cropRect.top + dy).coerceIn(0f, cropRect.bottom - minSize),
                                        cropRect.right,
                                        cropRect.bottom
                                    )
                                    "TR" -> Rect(
                                        cropRect.left,
                                        (cropRect.top + dy).coerceIn(0f, cropRect.bottom - minSize),
                                        (cropRect.right + dx).coerceIn(cropRect.left + minSize, maxWidth),
                                        cropRect.bottom
                                    )
                                    "BL" -> Rect(
                                        (cropRect.left + dx).coerceIn(0f, cropRect.right - minSize),
                                        cropRect.top,
                                        cropRect.right,
                                        (cropRect.bottom + dy).coerceIn(cropRect.top + minSize, maxHeight)
                                    )
                                    "BR" -> Rect(
                                        cropRect.left,
                                        cropRect.top,
                                        (cropRect.right + dx).coerceIn(cropRect.left + minSize, maxWidth),
                                        (cropRect.bottom + dy).coerceIn(cropRect.top + minSize, maxHeight)
                                    )
                                    else -> cropRect
                                }
                                cropRect = newRect
                            }
                        }
                    )
                }
        ) {
            canvasSize = size

            // Làm tối ngoài khung
            val path = Path().apply {
                addRect(Rect(0f, 0f, size.width, size.height))
                addRect(cropRect)
                fillType = PathFillType.EvenOdd
            }
            drawPath(path, overlayColor)

            // Viền trắng
            val strokeWidth = 2.dp.toPx()
            drawRect(
                color = Color.White,
                topLeft = Offset(cropRect.left, cropRect.top),
                size = Size(cropRect.width, cropRect.height),
                style = Stroke(width = strokeWidth)
            )

            // Lưới 3x3
            val gridColor = Color.White.copy(alpha = 0.5f)
            val stepX = cropRect.width / 3
            val stepY = cropRect.height / 3
            for (i in 1..2) {
                drawLine(
                    gridColor,
                    Offset(cropRect.left + stepX * i, cropRect.top),
                    Offset(cropRect.left + stepX * i, cropRect.bottom),
                    strokeWidth = 1.dp.toPx()
                )
                drawLine(
                    gridColor,
                    Offset(cropRect.left, cropRect.top + stepY * i),
                    Offset(cropRect.right, cropRect.top + stepY * i),
                    strokeWidth = 1.dp.toPx()
                )
            }

            // 4 chấm tròn ở góc
            val handleRadius = 6.dp.toPx()
            drawCircle(Color.White, handleRadius, Offset(cropRect.left, cropRect.top))
            drawCircle(Color.White, handleRadius, Offset(cropRect.right, cropRect.top))
            drawCircle(Color.White, handleRadius, Offset(cropRect.left, cropRect.bottom))
            drawCircle(Color.White, handleRadius, Offset(cropRect.right, cropRect.bottom))

            // 4 thanh ngắn bo tròn
            val barLength = 24.dp.toPx()
            val barWidth = 6.dp.toPx()
            drawLine(
                Color.White,
                Offset(cropRect.center.x - barLength / 2, cropRect.top),
                Offset(cropRect.center.x + barLength / 2, cropRect.top),
                strokeWidth = barWidth,
                cap = StrokeCap.Round
            )
            drawLine(
                Color.White,
                Offset(cropRect.center.x - barLength / 2, cropRect.bottom),
                Offset(cropRect.center.x + barLength / 2, cropRect.bottom),
                strokeWidth = barWidth,
                cap = StrokeCap.Round
            )
            drawLine(
                Color.White,
                Offset(cropRect.left, cropRect.center.y - barLength / 2),
                Offset(cropRect.left, cropRect.center.y + barLength / 2),
                strokeWidth = barWidth,
                cap = StrokeCap.Round
            )
            drawLine(
                Color.White,
                Offset(cropRect.right, cropRect.center.y - barLength / 2),
                Offset(cropRect.right, cropRect.center.y + barLength / 2),
                strokeWidth = barWidth,
                cap = StrokeCap.Round
            )
        }

        // Panel chọn tỉ lệ
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .background(Color.White.copy(alpha = 0.9f))
                .padding(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf(
                    "Original" to (0 to 0),
                    "Free" to (0 to 0),
                    "1:1" to (1 to 1),
                    "4:5" to (4 to 5),
                    "5:4" to (5 to 4)
                ).forEach { (label, r) ->
                    Button(
                        onClick = {
                            aspectRatioMode = label
                            ratio = r
                            val padding = with(density) { 40.dp.toPx() }

                            when {
                                label == "Original" -> {
                                    cropRect = Rect(0f, 0f, canvasSize.width, canvasSize.height)
                                }

                                r.first != 0 && r.second != 0 -> {
                                    val width = canvasSize.width - 2 * padding
                                    val height = width * r.second / r.first
                                    val left = padding
                                    val top = (canvasSize.height - height) / 2f
                                    cropRect = Rect(left, top, left + width, top + height)
                                }

                                else -> {
                                    // Free giữ nguyên
                                }
                            }
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(label)
                    }
                }
            }
        }
    }
}


