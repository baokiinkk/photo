package com.avnsoft.photoeditor.photocollage.ui.activities.editor.crop

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.avnsoft.photoeditor.photocollage.R
import com.avnsoft.photoeditor.photocollage.ui.theme.AppColor
import com.avnsoft.photoeditor.photocollage.ui.theme.fontFamily
import com.avnsoft.photoeditor.photocollage.utils.getInput
import com.basesource.base.ui.base.BaseActivity
import com.basesource.base.utils.clickableWithAlphaEffect
import org.koin.androidx.viewmodel.ext.android.viewModel


//data class CropInput(
//    val pathBitmap: String? = null
//) : IScreenData {
//
//    fun getBitmap(context: Context): Bitmap? {
//        val imageUri = pathBitmap?.toUri()
//        val bitmap = imageUri?.toBitmap(context)
//        return bitmap
//    }
//}

class CropActivity_phase_1 : BaseActivity() {

    private val screenInput: ToolInput? by lazy {
        intent.getInput()
    }

    private val viewmodel: CropViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewmodel.setBitmap(screenInput?.getBitmap(this@CropActivity_phase_1))
        enableEdgeToEdge()
        setContent {
            Scaffold(
                containerColor = Color(0xFFF2F4F8)
            ) { inner ->
                Column (
//                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = inner.calculateTopPadding())
                        .background(color = Color(0xFFF2F4F8))

                ) {
//                    val width = bitmap.width
//                    val height = bitmap.height
//                    val aspectRatio = width.toFloat() / height.toFloat()
//                    Log.d(
//                        "BitmapInfo",
//                        "Width: $width, Height: $height, AspectRatio: $aspectRatio"
//                    )
                    val uistate by viewmodel.uiState.collectAsStateWithLifecycle()
                    uistate.bitmap?.let {
                        CropImageScreen1(
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

//@Composable
//fun PickImageFromGallery(onImagePicked: (Uri) -> Unit) {
//    val context = LocalContext.current
//    val launcher = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.GetContent()
//    ) { uri: Uri? ->
//        uri?.let {
//            onImagePicked.invoke(uri)
//        }
//    }
//
//    Button(onClick = { launcher.launch("image/*") }) {
//        Text("Chọn ảnh từ thư viện")
//    }
//}

@Composable
fun CropImageScreen1(
    viewModel: CropViewModel,
    onCancel: () -> Unit,
    onApply: () -> Unit
) {
//    var cropState by remember { mutableStateOf(CropState()) }
    val cropState by viewModel.uiState.collectAsStateWithLifecycle()
    val density = LocalDensity.current

    var imageBounds by remember { mutableStateOf(IntSize.Zero) }
    val overlayColor = Color(1.0f, 1.0f, 1.0f, 0.6f)
    var scaleXFlip by remember { mutableStateOf(1f) } // State UI cục bộ
    var scaleYFlip by remember { mutableStateOf(1f) } // State UI cục bộ
    val coroutineScope = rememberCoroutineScope() // Cần nếu logic cần Coroutine (ví dụ: onApply)

//    val flipHorizontal = cropState.rotateImage % 180f != 0f
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column {
//            val aspectRatio = if (flipHorizontal)
//                cropState.bitmap!!.height.toFloat() / cropState.bitmap!!.width.toFloat()
//            else
//                cropState.bitmap!!.width.toFloat() / cropState.bitmap!!.height.toFloat()
            val aspectRatio =
                cropState.bitmap!!.width.toFloat() / cropState.bitmap!!.height.toFloat()
            Box(
                modifier = Modifier
                    .fillMaxWidth()
//                    .padding(horizontal = 16.dp)
//                    .padding(horizontal = if (flipHorizontal)16.dp else 60.dp)
                    .weight(1f)
//                    .aspectRatio(aspectRatio,!flipHorizontal)
//                    .background(Color.Green)
                    .padding(bottom = 16.dp, top = 12.dp)
                    .clip(RoundedCornerShape(0.dp))
                    .graphicsLayer {
                        rotationZ = cropState.rotateImage
                        Log.d("aaa", "CropImageScreen:${cropState.rotateImage}")
                        if (cropState.rotateImage % 180f != 0f && imageBounds.width != 0 && imageBounds.height != 0) {
                            val scale = imageBounds.width.toFloat() / imageBounds.height.toFloat()
                            Log.d("aaa", "scale")
                            scaleX = 0.5f
                            scaleY = 0.7f
                        } else {
                            Log.d("aaa", "scale")
                            scaleX = aspectRatio
//                            scaleY = scale
                        }
                    }
                    .onGloballyPositioned {
                        Log.d("aaa", "it.size:${it.size}")
                        imageBounds = it.size
                        viewModel.resetCropRect()
                    }
            ) {

                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(0.dp))
                ) {
                    cropState.bitmap?.asImageBitmap()?.let {
                        Image(
                            bitmap = it,
                            contentDescription = null,
                            contentScale = ContentScale.FillBounds,
                            alignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    scaleX =
                                        cropState.zoomScale * scaleXFlip // ⭐️ KẾT HỢP ZOOM VÀ LẬT
                                    scaleY =
                                        cropState.zoomScale * scaleYFlip // ⭐️ KẾT HỢP ZOOM VÀ LẬT
                                    rotationZ = cropState.rotationAngle // Áp dụng góc xoay
                                    transformOrigin = TransformOrigin.Center
                                }
                        )
                    }


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
            }


            // 🟣 UI chọn tỉ lệ (đè lên hình)
            CropControlPanel2(
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

//
//data class PositionModel(
//    val icon: Int,
//    val label: String
//)

@Composable
fun CropControlPanel2(
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
    val format = stringResource(R.string.format)
    val selectedTab = remember { mutableStateOf(format) }
    val positionList = listOf(
        PositionModel(
            icon = R.drawable.ic_flip_horizontal,
            label = stringResource(R.string.horizontal)
        ),
        PositionModel(
            icon = R.drawable.ic_flip_vertical,
            label = stringResource(R.string.vertical)
        ),
        PositionModel(
            icon = R.drawable.ic_rotate_left,
            label = stringResource(R.string.rotate)
        )
    )
    val selectedPosition = remember { mutableStateOf("") }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(AppColor.White, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Tabs
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp, top = 16.dp)
        ) {
            listOf(format, stringResource(R.string.position)).forEach { tab ->
                val isSelected = selectedTab.value == tab
                Box(
                    modifier = Modifier
                        .width(64.dp)
                        .height(24.dp)
                        .clip(RoundedCornerShape(50))
                        .clickableWithAlphaEffect { selectedTab.value = tab }
                        .background(
                            color = if (isSelected) Color(0xFF6425F3) else Color(0xFFF2F4F7),
                            shape = RoundedCornerShape(size = 24.dp)
                        )
                        .padding(start = 12.dp, top = 4.dp, end = 12.dp, bottom = 4.dp)
                ) {

                    Text(
                        text = tab,
                        style = TextStyle(
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            fontFamily = fontFamily,
                            fontWeight = FontWeight(600),
                            color = if (isSelected) Color(0xFFFFFFFF) else Color(0xFF667085),
                        )
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
            }
        }

        RulerSelector(
            modifier = Modifier.padding(horizontal = 16.dp)
        ) { rulerValue ->
            val (newScale, newAngle) = mapRulerToScaleAndRotation(rulerValue)
            Log.d("dddd", "ddddd $newScale && $newAngle")
            onScaleAndRotationChange(newScale, newAngle)
        }
        // Center slider or options depending on tab
        if (selectedTab.value == format) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
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
                modifier = Modifier
                    .wrapContentWidth()
                    .height(100.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                items(positionList) { item ->
                    val rotate = stringResource(R.string.rotate)
                    val horizontal = stringResource(R.string.horizontal)
                    val vertical = stringResource(R.string.vertical)
                    ItemPosition(
                        resId = item.icon,
                        isSelected = selectedPosition.value == item.label,
                        text = item.label,
                        onClick = {
                            selectedPosition.value = item.label
                            when (item.label) {
                                rotate -> onRotateClick()
                                horizontal -> onFlipHorizontal()
                                vertical -> onFlipVertical()
                            }
                        }
                    )
//                    Column(
//                        horizontalAlignment = Alignment.CenterHorizontally,
//                        modifier = Modifier.clickableWithAlphaEffect {
//                            when (label) {
//                                "Rotate" -> onRotateClick()
//                                "Horizontal" -> onFlipHorizontal()
//                                "Vertical" -> onFlipVertical()
//                            }
//                        }
//                    ) {
//                        Box(
//                            modifier = Modifier
//                                .size(48.dp)
//                                .clip(RoundedCornerShape(12.dp))
//                                .background(Color(0xFFF5F5F5)),
//                            contentAlignment = Alignment.Center
//                        ) {
//                            // 👉 Thay bằng icon thật (flip, rotate)
//                            Icon(
//                                imageVector = Icons.Default.Face, // ví dụ
//                                contentDescription = label,
//                                tint = Color.Black
//                            )
//                        }
//                        Spacer(modifier = Modifier.height(4.dp))
//                        Text(text = label, fontSize = 12.sp, color = Color.Black)
//                    }
                }
            }
        }
        // Bottom row (Cancel / Confirm)
        FooterEditor(
            modifier = Modifier
                .fillMaxWidth(),
            onCancel = onCancel,
            onApply = onApply
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}

//@Composable
//fun ItemFormat(
//    text: String,
//    iconAspect: IconAspect,
//    isSelected: Boolean,
//    onClick: () -> Unit
//) {
//    val backgroundColor = if (isSelected) {
//        Color(0xFF6425F3)
//    } else {
//        Color(0xFFF2F4F7)
//    }
//    val tintColor = if (isSelected) {
//        AppColor.Gray0
//    } else {
//        AppColor.Gray900
//    }
//    Column(
//        modifier = Modifier
//            .clickableWithAlphaEffect(onClick = onClick),
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//
//        Box(
//            modifier = Modifier
//                .clip(RoundedCornerShape(12.dp))
//                .width(iconAspect.width.dp)
//                .height(iconAspect.height.dp)
//                .background(backgroundColor),
//            contentAlignment = Alignment.Center
//        ) {
//            ImageWidget(
//                resId = iconAspect.resId,
//                tintColor = tintColor
//            )
//        }
//
//        Text(
//            text = text,
//
//            style = TextStyle(
//                fontSize = 12.sp,
//                lineHeight = 16.sp,
//                fontFamily = fontFamily,
//                fontWeight = FontWeight(500),
//                color = AppColor.Gray800,
//                textAlign = TextAlign.Center,
//            ),
//            modifier = Modifier
//                .padding(top = 8.dp)
//        )
//    }
//}
//
//@Composable
//fun ItemPosition(
//    resId: Int,
//    isSelected: Boolean,
//    text: String,
//    onClick: () -> Unit
//) {
//    val backgroundColor = if (isSelected) {
//        Color(0xFF6425F3)
//    } else {
//        Color(0xFFF2F4F7)
//    }
//    val tintColor = if (isSelected) {
//        AppColor.Gray0
//    } else {
//        AppColor.Gray900
//    }
//    Column(
//        modifier = Modifier.clickableWithAlphaEffect(onClick = onClick)
//    ) {
//        Box(
//            modifier = Modifier
//                .clip(RoundedCornerShape(12.dp))
//                .width(48.dp)
//                .height(48.dp)
//                .background(backgroundColor),
//            contentAlignment = Alignment.Center
//        ) {
//            ImageWidget(
//                resId = resId,
//                tintColor = tintColor
//            )
//        }
//
//        Text(
//            text = text,
//
//            style = TextStyle(
//                fontSize = 12.sp,
//                lineHeight = 16.sp,
//                fontFamily = fontFamily,
//                fontWeight = FontWeight(500),
//                color = AppColor.Gray800,
//                textAlign = TextAlign.Center,
//            ),
//            modifier = Modifier
//                .padding(top = 8.dp)
//        )
//    }
//}
//
//@Composable
//fun FooterEditor(
//    modifier: Modifier = Modifier,
//    onCancel: () -> Unit,
//    onApply: () -> Unit,
//) {
//    Column {
//        Spacer(modifier = Modifier.height(16.dp))
//        Box(
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(1.dp)
//                .background(Color(0xFFF2F4F7))
//        )
//        Spacer(modifier = Modifier.height(12.dp))
//        Row(
//            modifier = modifier,
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            ImageWidget(
//                resId = R.drawable.ic_close,
//                modifier = Modifier
//                    .clickableWithAlphaEffect(onClick = onCancel)
//                    .padding(start = 16.dp)
//                    .size(28.dp)
//            )
//            Text(
//                text = stringResource(R.string.crop),
//                style = TextStyle(
//                    fontSize = 16.sp,
//                    lineHeight = 24.sp,
//                    fontFamily = fontFamily,
//                    fontWeight = FontWeight(600),
//                    color = AppColor.Gray900,
//                ),
//                textAlign = TextAlign.Center,
//                modifier = Modifier.weight(1f)
//            )
//            ImageWidget(
//                resId = R.drawable.ic_done,
//                modifier = Modifier
//                    .clickableWithAlphaEffect(onClick = onApply)
//                    .padding(end = 16.dp)
//                    .size(28.dp)
//            )
//        }
//    }
//}

//fun Bitmap.rotate(degrees: Float): Bitmap {
//    val matrix = android.graphics.Matrix().apply { postRotate(degrees) } // Sửa thành postRotate
//    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
//}