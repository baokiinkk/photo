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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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

@Composable
fun PickImageFromGallery(
    onImagePicked: (ImageBitmap) -> Unit
) {
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
    var cropRect by remember { mutableStateOf(Rect(200f, 400f, 800f, 1000f)) }
    var ratio by remember { mutableStateOf(Pair(1, 1)) }
    val overlayColor = Color(0f, 0f, 0f, 0.6f)

    Box(modifier = modifier.fillMaxSize()) {
        // 1️⃣ Hiển thị ảnh nền
        Image(
            bitmap = imageBitmap,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // 2️⃣ Lớp phủ tối + khung crop
        Canvas(modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    cropRect = cropRect.translate(dragAmount)
                }
            }
        ) {
            val path = Path().apply {
                addRect(Rect(0f, 0f, size.width, size.height)) // toàn màn
                addRect(cropRect) // vùng crop
                fillType = PathFillType.EvenOdd // phần giao giữa 2 vùng -> "mask"
            }
            // Làm tối vùng ngoài
            drawPath(path, overlayColor)

            // Vẽ viền khung crop
            drawRect(
                color = Color.White,
                topLeft = Offset(cropRect.left, cropRect.top),
                size = Size(cropRect.width, cropRect.height),
                style = Stroke(width = 2.dp.toPx())
            )

            // Vẽ các line chia 3x3 trong khung crop
            val stepX = cropRect.width / 3
            val stepY = cropRect.height / 3
            for (i in 1..2) {
                drawLine(
                    Color.White.copy(alpha = 0.6f),
                    Offset(cropRect.left + stepX * i, cropRect.top),
                    Offset(cropRect.left + stepX * i, cropRect.bottom),
                    1.dp.toPx()
                )
                drawLine(
                    Color.White.copy(alpha = 0.6f),
                    Offset(cropRect.left, cropRect.top + stepY * i),
                    Offset(cropRect.right, cropRect.top + stepY * i),
                    1.dp.toPx()
                )
            }
        }

        // 3️⃣ Dãy chọn tỉ lệ
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .background(Color.White.copy(alpha = 0.9f))
                .padding(16.dp)
        ) {
            Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                listOf(
                    "Original" to (0 to 0),
                    "Free" to (0 to 0),
                    "1:1" to (1 to 1),
                    "4:5" to (4 to 5),
                    "5:4" to (5 to 4)
                ).forEach { (label, r) ->
                    Button(
                        onClick = {
                            ratio = r
                            // Cập nhật lại cropRect theo tỉ lệ
                            if (r.first != 0 && r.second != 0) {
                                val center = cropRect.center
                                val width = cropRect.width
                                val height = width * r.second / r.first
                                cropRect = Rect(
                                    center.x - width / 2,
                                    center.y - height / 2,
                                    center.x + width / 2,
                                    center.y + height / 2
                                )
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

@Composable
fun CropImageView(
    imageBitmap: ImageBitmap,
    aspectRatio: Pair<Int, Int>?,
    rotation: Float,
    modifier: Modifier = Modifier
) {
    // cropRect là vùng crop di chuyển được
    var cropRect by remember { mutableStateOf<Rect?>(null) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { dragOffset = Offset.Zero },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        cropRect = cropRect?.translate(dragAmount.x, dragAmount.y)
                    }
                )
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val viewWidth = size.width
            val viewHeight = size.height

            // vẽ ảnh nền
            withTransform({
                translate(size.width / 2, size.height / 2)
                rotate(rotation)
                translate(-imageBitmap.width / 2f, -imageBitmap.height / 2f)
            }) {
                drawImage(imageBitmap)
            }

            // Khởi tạo cropRect ban đầu
            if (cropRect == null) {
                val ratio = aspectRatio?.let {
                    if (it.first != 0 && it.second != 0)
                        it.first.toFloat() / it.second.toFloat()
                    else null
                }

                val width: Float
                val height: Float
                if (ratio != null) {
                    if (viewWidth / viewHeight > ratio) {
                        height = viewHeight * 0.8f
                        width = height * ratio
                    } else {
                        width = viewWidth * 0.8f
                        height = width / ratio
                    }
                } else {
                    width = viewWidth * 0.8f
                    height = viewHeight * 0.8f
                }

                cropRect = Rect(
                    (viewWidth - width) / 2f,
                    (viewHeight - height) / 2f,
                    (viewWidth + width) / 2f,
                    (viewHeight + height) / 2f
                )
            }

            val rect = cropRect ?: return@Canvas

            // overlay tối bên ngoài khung crop
            drawRect(
                color = Color.Black.copy(alpha = 0.6f),
                size = size,
                blendMode = BlendMode.SrcOver
            )

            // vùng crop sáng lại (clear overlay)
            drawRect(
                color = Color.Transparent,
                topLeft = Offset(rect.left, rect.top),
                size = Size(rect.width, rect.height),
                blendMode = BlendMode.Clear
            )

            // vẽ grid 3x3 trong khung crop
            val gridColor = Color.White.copy(alpha = 0.8f)
            val stepX = rect.width / 3
            val stepY = rect.height / 3
            for (i in 1..2) {
                drawLine(
                    gridColor,
                    Offset(rect.left + stepX * i, rect.top),
                    Offset(rect.left + stepX * i, rect.bottom),
                    strokeWidth = 1.5f
                )
                drawLine(
                    gridColor,
                    Offset(rect.left, rect.top + stepY * i),
                    Offset(rect.right, rect.top + stepY * i),
                    strokeWidth = 1.5f
                )
            }

            // viền ngoài khung crop
            drawRect(
                color = Color.White,
                topLeft = Offset(rect.left, rect.top),
                size = Size(rect.width, rect.height),
                style = Stroke(width = 3f)
            )
        }
    }
}

@Composable
fun CropControlPanel(
    onRatioSelected: (Pair<Int, Int>) -> Unit,
    onRotate: (Float) -> Unit,
    onApply: () -> Unit
) {
    var rotation by remember { mutableStateOf(0f) }
    val ratios = listOf(
        "Original" to (0 to 0),
        "Free" to (0 to 0),
        "1:1" to (1 to 1),
        "4:5" to (4 to 5),
        "5:4" to (5 to 4)
    )

    Column(
        Modifier
            .fillMaxWidth()
            .background(Color(0xFF121212))
            .padding(vertical = 12.dp)
    ) {
        Text(
            "Rotate",
            color = Color.White,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Slider(
            value = rotation,
            onValueChange = {
                rotation = it
                onRotate(it)
            },
            valueRange = -45f..45f,
            modifier = Modifier.padding(horizontal = 32.dp)
        )

        Text(
            "Aspect Ratio",
            color = Color.White,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 8.dp)
        )

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            items(ratios) { (label, pair) ->
                TextButton(onClick = { onRatioSelected(pair) }) {
                    Text(label, color = Color.White)
                }
            }
        }

        Button(
            onClick = onApply,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 8.dp)
        ) {
            Text("Crop")
        }
    }
}
