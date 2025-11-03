package com.amb.photo.ui.activities.editor.crop

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.amb.photo.R
import com.amb.photo.ui.activities.editor.RulerSelector
import com.amb.photo.ui.activities.editor.mapRulerToScaleAndRotation
import com.amb.photo.ui.activities.editor.toBitmap
import com.amb.photo.ui.theme.AppColor
import com.amb.photo.ui.theme.fontFamily
import com.amb.photo.utils.getInput
import com.basesource.base.ui.base.BaseActivity
import com.basesource.base.ui.base.IScreenData
import com.basesource.base.utils.ImageWidget
import com.basesource.base.utils.clickableWithAlphaEffect
import org.koin.androidx.viewmodel.ext.android.viewModel


data class CropInput(
    val pathBitmap: String? = null
) : IScreenData {

    fun getBitmap(context: Context): Bitmap? {
        val imageUri = pathBitmap?.toUri()
        val bitmap = imageUri?.toBitmap(context)
        return bitmap
    }
}

class CropActivity : BaseActivity() {

    private val screenInput: CropInput? by lazy {
        intent.getInput()
    }

    private val viewmodel: CropViewModel by viewModel()

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
                    screenInput?.getBitmap(this@CropActivity)?.let { bitmap ->
                        val width = bitmap.width
                        val height = bitmap.height
                        val aspectRatio = width.toFloat() / height.toFloat()
                        Log.d(
                            "BitmapInfo",
                            "Width: $width, Height: $height, AspectRatio: $aspectRatio"
                        )
                        CropImageScreen(
                            bitmap.asImageBitmap(),
                            viewmodel,
                            onCancel = {
                                finish()
                            },
                            onApply = {

                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PickImageFromGallery(onImagePicked: (Uri) -> Unit) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            onImagePicked.invoke(uri)
        }
    }

    Button(onClick = { launcher.launch("image/*") }) {
        Text("Chọn ảnh từ thư viện")
    }
}

// Trong CropActivity.kt (Cập nhật CropImageScreen)
@Composable
fun CropImageScreen(
    imageBitmap: ImageBitmap,
    viewModel: CropViewModel,
    onCancel: () -> Unit,
    onApply: () -> Unit
) {
//    var cropState by remember { mutableStateOf(CropState()) }
    val cropState by viewModel.uiState.collectAsStateWithLifecycle()

    var imageBounds by remember { mutableStateOf(IntSize.Zero) }
    val overlayColor = Color(0f, 0f, 0f, 0.6f)
    val density = LocalDensity.current
    var scaleXFlip by remember { mutableStateOf(1f) } // State UI cục bộ
    var scaleYFlip by remember { mutableStateOf(1f) } // State UI cục bộ
    val coroutineScope = rememberCoroutineScope() // Cần nếu logic cần Coroutine (ví dụ: onApply)

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 47.dp)
                    .height(400.dp)
//                    .aspectRatio(0.5f)
//                    .weight(1f)
                    .padding(bottom = 16.dp)
                    .clip(RoundedCornerShape(0.dp))
            ) {
                Image(
                    bitmap = imageBitmap,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    alignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .onGloballyPositioned {
                            imageBounds = it.size
                        }
                        .graphicsLayer {
                            scaleX = cropState.zoomScale * scaleXFlip // ⭐️ KẾT HỢP ZOOM VÀ LẬT
                            scaleY = cropState.zoomScale * scaleYFlip // ⭐️ KẾT HỢP ZOOM VÀ LẬT
                            rotationZ = cropState.rotationAngle // Áp dụng góc xoay
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
                                        viewModel.onDragStart(offset)
                                    },
                                    onDragEnd = {
                                        viewModel.onDragEnd()
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consume()

                                        viewModel.onDrag(
                                            dragAmount,
                                            canvasWidth,
                                            canvasHeight
                                        )
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
                            viewModel.updateCropState(
                                cropState.copy(
                                    cropRect = Rect(left, top, left + width, top + height)
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
                idCropState = cropState.id,
                onCancel = onCancel,
                onApply = onApply,
                onFormat = { aspect ->
                    viewModel.onAspectFormatSelected(
                        imageBounds = imageBounds,
                        aspect = aspect,
                        padding = with(density) { 10.dp.toPx() }
                    )
                },
                onScaleAndRotationChange = { newScale, newAngle ->
                    // ⭐️ GỌI VIEWMODEL ĐỂ THAY ĐỔI TRẠNG THÁI
                    viewModel.updateScaleAndRotation(newScale, newAngle)
                },
                onRotateClick = {
                    // ⭐️ GỌI VIEWMODEL ĐỂ THAY ĐỔI TRẠNG THÁI
                    viewModel.rotateImage()
                },
                onFlipHorizontal = {
                    // ⭐️ STATE LẬT NẰM Ở UI CỤC BỘ
                    scaleXFlip *= -1f
                },
                onFlipVertical = {
                    // ⭐️ STATE LẬT NẰM Ở UI CỤC BỘ
                    scaleYFlip *= -1f
                }
            )
        }
    }
}


@Composable
fun CropControlPanel(
    modifier: Modifier = Modifier,
    idCropState: String,
    onCancel: () -> Unit,
    onApply: () -> Unit,
    onFormat: (CropAspect) -> Unit,
    onScaleAndRotationChange: (Float, Float) -> Unit,
    onRotateClick: () -> Unit, // ⭐️ THÊM: Xoay 90 độ
    onFlipHorizontal: () -> Unit, // ⭐️ THÊM: Lật ngang
    onFlipVertical: () -> Unit // ⭐️ THÊM: Lật dọc
) {
    val selectedTab = remember { mutableStateOf("Format") }
    val positionList = listOf("Horizontal", "Vertical", "Rotate")
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
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

        RulerSelector(
            modifier = Modifier.background(Color.Green)
        ) { rulerValue ->
            val (newScale, newAngle) = mapRulerToScaleAndRotation(rulerValue)
            onScaleAndRotationChange(newScale, newAngle)
        }
        // Center slider or options depending on tab
        if (selectedTab.value == "Format") {
//            Spacer(modifier = Modifier.height(16.dp))
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(Color.Red),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                items(CropAspect.entries.toTypedArray()) { aspect ->
                    ItemFormat(
                        text = aspect.label,
                        iconAspect = aspect.iconAspect,
                        isSelected = idCropState == aspect.label,
                        onClick = {
                            onFormat.invoke(aspect)
                        }
                    )
                }
            }
        } else {
            // Position tab (horizontal / vertical / rotate)
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                items(positionList) { label ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickableWithAlphaEffect {
                            when (label) {
                                "Rotate" -> onRotateClick()
                                "Horizontal" -> onFlipHorizontal()
                                "Vertical" -> onFlipVertical()
                            }
                        }
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
        FooterEditor(
            onCancel = onCancel,
            onApply = onApply
        )
    }
}

@Composable
fun ItemFormat(
    text: String,
    iconAspect: IconAspect,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) {
        Color(0xFF6425F3)
    } else {
        Color(0xFFF2F4F7)
    }
    val tintColor = if (isSelected) {
        AppColor.Gray0
    } else {
        AppColor.Gray900
    }
    Column(
        modifier = Modifier
            .clickableWithAlphaEffect(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .width(iconAspect.width.dp)
                .height(iconAspect.height.dp)
                .background(backgroundColor)
             ,
            contentAlignment = Alignment.Center
        ) {
            ImageWidget(
                resId = iconAspect.resId,
                tintColor = tintColor
            )
        }

        Text(
            text = text,

            style = TextStyle(
                fontSize = 12.sp,
                lineHeight = 16.sp,
                fontFamily = fontFamily,
                fontWeight = FontWeight(500),
                color = AppColor.Gray800,
                textAlign = TextAlign.Center,
            ),
            modifier = Modifier
                .padding(top = 8.dp)
        )
    }
}

@Composable
fun FooterEditor(
    modifier: Modifier = Modifier,
    onCancel: () -> Unit,
    onApply: () -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ImageWidget(
            resId = R.drawable.ic_close,
            modifier = Modifier
                .clickableWithAlphaEffect(onClick = onCancel)
                .padding(start = 16.dp)
                .size(28.dp)
        )
        Text(
            text = stringResource(R.string.crop),
            style = TextStyle(
                fontSize = 16.sp,
                lineHeight = 24.sp,
                fontFamily = fontFamily,
                fontWeight = FontWeight(600),
                color = AppColor.Gray900,
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )
        ImageWidget(
            resId = R.drawable.ic_done,
            modifier = Modifier
                .clickableWithAlphaEffect(onClick = onApply)
                .padding(end = 16.dp)
                .size(28.dp)
        )
    }
}