package com.avnsoft.photoeditor.photocollage.ui.activities.editor.crop

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.avnsoft.photoeditor.photocollage.R
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.crop.CropAspect.Companion.toAspectRatio
import com.avnsoft.photoeditor.photocollage.ui.theme.AppColor
import com.avnsoft.photoeditor.photocollage.ui.theme.fontFamily
import com.avnsoft.photoeditor.photocollage.utils.FileUtil.toBitmap
import com.avnsoft.photoeditor.photocollage.utils.getInput
import com.basesource.base.ui.base.BaseActivity
import com.basesource.base.ui.base.IScreenData
import com.basesource.base.utils.ImageWidget
import com.basesource.base.utils.clickableWithAlphaEffect
import com.tanishranjan.cropkit.CropColors
import com.tanishranjan.cropkit.CropDefaults
import com.tanishranjan.cropkit.CropShape
import com.tanishranjan.cropkit.GridLinesType
import com.tanishranjan.cropkit.ImageCropper
import com.tanishranjan.cropkit.rememberCropController
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream


data class ToolInput(
    val pathBitmap: String? = null,
    val type: TYPE = TYPE.NEW,
    val isBackgroundTransparent: Boolean = false
) : IScreenData {

    enum class TYPE {
        NEW,
        BACK_AND_RETURN
    }

    fun getBitmap(context: Context): Bitmap? {
//        val imageUri = pathBitmap?.toUri()
//        val bitmap = imageUri?.toBitmap(context)
        return pathBitmap.toBitmap(context)
    }
}

class CropActivity : BaseActivity() {

    private val screenInput: ToolInput? by lazy {
        intent.getInput()
    }

    private val viewmodel: CropViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewmodel.setBitmap(screenInput?.getBitmap(this@CropActivity))
        enableEdgeToEdge()
        setContent {
            Scaffold(
                containerColor = Color(0xFFF2F4F8)
            ) { inner ->
                Column(
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
                        CropImageScreen(
                            viewmodel,
                            onCancel = {
                                finish()
                            },
                            onApply = {
                                val intent = Intent()
                                intent.putExtra("pathBitmap", it)
                                setResult(RESULT_OK, intent)
                                finish()
                                Toast.makeText(this@CropActivity, getString(R.string.image_saved), Toast.LENGTH_SHORT)
                                    .show()
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
        Text(stringResource(R.string.select_image_from_gallery))
    }
}

@Composable
fun CropImageScreen(
    viewModel: CropViewModel,
    onCancel: () -> Unit,
    onApply: (String) -> Unit
) {
    val cropState by viewModel.uiState.collectAsStateWithLifecycle()

    var cropShape: CropShape by remember {
        mutableStateOf(
            CropShape.AspectRatio(
                CropAspect.RATIO_1_1.ratio.toAspectRatio(),
                false
            )
        )
    }
    var gridLinesType by remember { mutableStateOf(GridLinesType.GRID) }

    var zoomScale: Float? by remember { mutableStateOf(null) }

    var rotationZBitmap: Float? by remember { mutableStateOf(null) }

    val context = LocalContext.current

    val cropController = rememberCropController(
        bitmap = cropState.bitmap!!,
        cropOptions = CropDefaults.cropOptions(
            cropShape = cropShape,
            gridLinesType = gridLinesType,
            touchPadding = 24.dp,
            initialPadding = 0.dp,
            zoomScale = zoomScale,
            rotationZBitmap = rotationZBitmap,
        ),
        cropColors = CropColors(
            overlay = Color(1.0f, 1.0f, 1.0f, 0.6f),
            overlayActive = Color(1.0f, 1.0f, 1.0f, 0.6f),
            gridlines = Color.White,
            cropRectangle = Color.White,
            handle = Color.White
        )
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(0.dp))
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        ImageCropper(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .padding(horizontal = 16.dp),
            cropController = cropController
        )
        Spacer(modifier = Modifier.height(16.dp))
        // 🟣 UI chọn tỉ lệ (đè lên hình)
        CropControlPanel(
            idCropState = cropState.id,
            onCancel = onCancel,
            onApply = {
                saveImage(
                    context = context,
                    bitmap = cropController.crop(),
                    onImageSaved = onApply
                )
            },
            onFormat = { aspect ->
                cropShape = when (aspect) {
                    CropAspect.ORIGINAL -> {
                        aspect.ratio = cropState.bitmap!!.width to cropState.bitmap!!.height
                        CropShape.Original
                    }

                    CropAspect.FREE -> CropShape.FreeForm(
                        cropState.aspect.ratio.toAspectRatio()
                    )
//                        CropShape.AspectRatio(
//                        cropState.aspect.ratio.toAspectRatio(),
//                        true
//                    )

                    else -> CropShape.AspectRatio(aspect.ratio.toAspectRatio(), false)
                }

                cropController.setAspectRatio(cropShape)
                viewModel.onAspectFormatSelected(aspect)
            },
            onScaleAndRotationChange = { newScale, newAngle ->
                viewModel.updateScaleAndRotation(newScale, newAngle)
                zoomScale = newScale
                rotationZBitmap = newAngle
            },
            onRotateClick = {
                cropController.rotateClockwise {
                    viewModel.updateBitmap(it)
                }
            },
            onFlipHorizontal = {
                cropController.flipHorizontally {
                    viewModel.updateBitmap(it)
                }
            },
            onFlipVertical = {
                cropController.flipVertically {
                    viewModel.updateBitmap(it)
                }
            }
        )
    }
}


data class PositionModel(
    val icon: Int,
    val label: String
)

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
                        .wrapContentWidth()
//                        .width(64.dp)
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
        Spacer(modifier = Modifier.height(16.dp))
        FooterEditor(
            modifier = Modifier
                .fillMaxWidth(),
            onCancel = onCancel,
            onApply = onApply
        )
        Spacer(modifier = Modifier.height(16.dp))
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
                .background(backgroundColor),
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
fun ItemPosition(
    resId: Int,
    isSelected: Boolean,
    text: String,
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
        modifier = Modifier.clickableWithAlphaEffect(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .width(48.dp)
                .height(48.dp)
                .background(backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            ImageWidget(
                resId = resId,
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
    title: String = stringResource(R.string.crop),
    onCancel: () -> Unit,
    onApply: () -> Unit,
) {
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color(0xFFF2F4F7))
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = modifier,
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
                text = title,
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
}

fun saveImage(context: Context, bitmap: Bitmap, onImageSaved: (String) -> Unit) {
    val filename = "${System.currentTimeMillis()}.jpg"
    var fos: OutputStream? = null
    var imagePath: String? = null

//    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//        val resolver = context.contentResolver
//        val contentValues = ContentValues().apply {
//            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
//            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
//            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
//        }
//
//        val imageUri: Uri? = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
//        fos = imageUri?.let { uri ->
//            resolver.openOutputStream(uri)
//        }
//
//        // ✅ lấy path thực tế từ MediaStore
//        imageUri?.let { uri ->
//            imagePath = uri.toString() // hoặc bạn có thể query path vật lý nếu cần
//        }
//    } else {
//        val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
//        val imageFile = File(imagesDir, filename)
//        fos = FileOutputStream(imageFile)
//        imagePath = imageFile.absolutePath // ✅ trả về path thật
//    }

    val imagesDir = context.getExternalFilesDir("editor_image_success")
    if (imagesDir?.exists() == false) {
        imagesDir.mkdirs()
    }
    val imageFile = File(imagesDir, filename)
    fos = FileOutputStream(imageFile)
    imagePath = imageFile.absolutePath // ✅ trả về path thật

    fos?.use { stream ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
    }

    imagePath?.let { onImageSaved.invoke(it) }
}