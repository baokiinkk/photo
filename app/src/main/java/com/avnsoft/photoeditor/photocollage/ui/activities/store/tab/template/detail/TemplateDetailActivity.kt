package com.avnsoft.photoeditor.photocollage.ui.activities.store.tab.template.detail

import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.avnsoft.photoeditor.photocollage.R
import com.avnsoft.photoeditor.photocollage.data.model.template.TemplateModel
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.crop.ToolInput
import com.avnsoft.photoeditor.photocollage.ui.activities.imagepicker.GalleryImage
import com.avnsoft.photoeditor.photocollage.ui.activities.imagepicker.ImagePickerViewModel
import com.avnsoft.photoeditor.photocollage.ui.activities.imagepicker.components.BucketSheet
import com.avnsoft.photoeditor.photocollage.ui.activities.imagepicker.components.PickerHeaderBar
import com.avnsoft.photoeditor.photocollage.ui.activities.imagepicker.createImageUri
import com.avnsoft.photoeditor.photocollage.ui.activities.store.editor.EditorStoreActivity
import com.avnsoft.photoeditor.photocollage.ui.activities.store.editor.ToolTemplateInput
import com.avnsoft.photoeditor.photocollage.ui.theme.AppColor
import com.avnsoft.photoeditor.photocollage.ui.theme.AppStyle
import com.avnsoft.photoeditor.photocollage.ui.theme.BackgroundGray
import com.avnsoft.photoeditor.photocollage.ui.theme.BackgroundWhite
import com.avnsoft.photoeditor.photocollage.ui.theme.MainTheme
import com.avnsoft.photoeditor.photocollage.utils.FileUtil.toFile
import com.avnsoft.photoeditor.photocollage.utils.getInput
import com.basesource.base.ui.base.BaseActivity
import com.basesource.base.ui.base.IScreenData
import com.basesource.base.ui.image.LoadImage
import com.basesource.base.utils.ImageWidget
import com.basesource.base.utils.capturable
import com.basesource.base.utils.clickableWithAlphaEffect
import com.basesource.base.utils.launchActivity
import com.basesource.base.utils.rememberCaptureController
import com.basesource.base.utils.requestPermission
import com.basesource.base.utils.takePicture
import com.basesource.base.utils.toJson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File
import kotlin.math.roundToInt
import androidx.lifecycle.viewmodel.compose.viewModel as composeViewModel

data class TemplateDetailInput(
    val template: TemplateModel?,
    val type: ToolInput.TYPE = ToolInput.TYPE.NEW,
) : IScreenData

class TemplateDetailActivity : BaseActivity() {

    private val viewModel: TemplateDetailViewModel by viewModel()

    private val screenInput: TemplateDetailInput? by lazy {
        intent.getInput()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.initData(screenInput?.template)
        if (hasPermission()) {
            showContent()
        } else {
            val perm =
                if (Build.VERSION.SDK_INT >= 33) android.Manifest.permission.READ_MEDIA_IMAGES else android.Manifest.permission.READ_EXTERNAL_STORAGE
            requestPermission(perm) {
                if (it) {
                    showContent()
                } else {
                    finish()
                }
            }
        }
    }

    private fun hasPermission(): Boolean {
        val perm =
            if (Build.VERSION.SDK_INT >= 33) android.Manifest.permission.READ_MEDIA_IMAGES else android.Manifest.permission.READ_EXTERNAL_STORAGE
        return ContextCompat.checkSelfPermission(this, perm) == PackageManager.PERMISSION_GRANTED
    }

    private fun showContent() {
        setContent {
            MainTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                val scope = rememberCoroutineScope()
                val context = LocalContext.current

                Scaffold(
                    containerColor = AppColor.White
                ) { inner ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(
                                top = inner.calculateTopPadding(), bottom = inner.calculateBottomPadding()
                            )
                    ) {
                        TemplateDetailHeader(
                            onClose = { finish() }, onApply = {
                            if (screenInput?.type == ToolInput.TYPE.BACK_AND_RETURN) {
                                // For BACK_AND_RETURN, still capture bitmap
                                scope.launch {
                                    try {
                                        val selectedImagesString = uiState.selectedImages.mapValues {
                                            it.value.toString()
                                        }
                                        val input = ToolTemplateInput(
                                            template = screenInput?.template, selectedImages = selectedImagesString
                                        )
                                        setResult(
                                            RESULT_OK, intent.putExtra("PATH", input.toJson())
                                        )
                                        finish()
                                    } catch (ex: Throwable) {
                                        Toast.makeText(
                                            context, "Error ${ex.message}", Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            } else {
                                // Convert Uri to String for serialization
                                val selectedImagesString = uiState.selectedImages.mapValues {
                                    it.value.toString()
                                }

                                launchActivity(
                                    toActivity = EditorStoreActivity::class.java, input = ToolTemplateInput(
                                        template = screenInput?.template, selectedImages = selectedImagesString
                                    )
                                ) {
                                    if (it.resultCode == RESULT_OK) {
                                        setResult(RESULT_OK)
                                        finish()
                                    }
                                }
                            }
                        }, modifier = Modifier.fillMaxWidth()
                        )

                        uiState.template?.let { template ->
                            TemplateDetailContent(
                                template = template,
                                selectedImages = uiState.selectedImages,
                                onImageSelected = { index, uri ->
                                    viewModel.selectImage(index, uri)
                                },
                                onImageUnselected = { index ->
                                    viewModel.unselectImage(index)
                                },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(BackgroundGray),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TemplateDetailHeader(
    onClose: () -> Unit, onApply: () -> Unit, modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(BackgroundWhite)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ImageWidget(
            resId = R.drawable.ic_close, modifier = Modifier
                .size(28.dp)
                .clickableWithAlphaEffect(onClick = onClose)
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = stringResource(R.string.apply),
            style = AppStyle.button().semibold().primary500(),
            textAlign = TextAlign.Center,
            modifier = Modifier.clickableWithAlphaEffect(onClick = onApply)
        )
    }
}

@Composable
fun TemplateDetailContent(
    template: TemplateModel,
    selectedImages: Map<Int, Uri>,
    onImageSelected: (Int, Uri) -> Unit,
    onImageUnselected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val imagePickerViewModel: ImagePickerViewModel = composeViewModel()
    var selectedCellIndex by remember { mutableStateOf<Int?>(0) }

    Column(modifier = modifier) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .then(
                    if (template.width != null && template.height != null) {
                        val ratio = template.width.toFloat() / template.height.toFloat()
                        Modifier.aspectRatio(ratio)
                    } else {
                        Modifier
                    }
                )
                .padding(20.dp)
                .clipToBounds(),
        ) {

            // Contents (cells) overlay - Layer 1
            template.cells?.forEachIndexed { index, cell ->
                val isSelected = selectedCellIndex == index
                val hasImage = selectedImages.containsKey(index)
                val baseModifier = Modifier.baseBannerItemModifier(
                    x = cell.x,
                    y = cell.y,
                    width = cell.width,
                    height = cell.height,
                    rotate = cell.rotate?.toFloat(),
                )
                Box(
                    modifier = baseModifier
                        .border(
                            width = if (isSelected) 3.dp else 2.dp,
                            color = if (isSelected) Color(0xFF6425F3) else Color.Transparent,
                        )
                        .background(Color.White.copy(alpha = 0.1f))
                        .clickableWithAlphaEffect {
                            selectedCellIndex = index
                        }) {
                    selectedImages[index]?.let { uri ->
                        LoadImage(
                            model = uri, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            // Banner background - Layer 0
            template.bannerUrl?.let { bannerUrl ->
                LoadImage(
                    model = bannerUrl, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.FillBounds
                )
            }

            template.layer?.forEachIndexed { index, layerItem ->
                val baseModifier = Modifier.baseBannerItemModifier(
                    x = layerItem.x,
                    y = layerItem.y,
                    width = layerItem.width,
                    height = layerItem.height,
                    rotate = layerItem.rotate?.toFloat(),
                )

                Box(
                    modifier = baseModifier
                ) {
                    layerItem.urlThumb?.let { urlThumb ->
                        LoadImage(
                            model = urlThumb, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }

        CustomImagePickerScreen(
            modifier = Modifier
                .fillMaxWidth()
                .background(BackgroundWhite),
            viewModel = imagePickerViewModel,
            selectedImages = selectedImages,
            selectedCellIndex = selectedCellIndex,
            onImageClick = { uri ->
                selectedCellIndex?.or(0)?.let { index ->
                    onImageSelected(index, uri)
                    val nextIndex = template.cells?.indices?.firstOrNull {
                        it > index && !selectedImages.containsKey(it)
                    }
                    selectedCellIndex = nextIndex?.or(0)
                }
            },
            onRemove = { uri ->
                selectedImages.entries.find { it.value == uri }?.key?.let { index ->
                    selectedCellIndex = index
                    onImageUnselected(index)
                }
            },
            onCancel = {
                // Do nothing, keep screen open
            })
    }
}

fun Modifier.baseBannerItemModifier(
    x: Float?, y: Float?, width: Float?, height: Float?, rotate: Float?
): Modifier {
    val xRatio = x ?: 0f
    val yRatio = y ?: 0f
    val wRatio = width ?: 0f
    val hRatio = height ?: 0f
    val rotation = rotate ?: 0f

    return this
        .layout { measurable, constraints ->
            val parentWidth = constraints.maxWidth
            val parentHeight = constraints.maxHeight

            val childWidthPx = (wRatio * parentWidth).roundToInt().coerceAtLeast(0)
            val childHeightPx = (hRatio * parentHeight).roundToInt().coerceAtLeast(0)
            val childX = (xRatio * parentWidth).roundToInt()
            val childY = (yRatio * parentHeight).roundToInt()

            val placeable = measurable.measure(
                Constraints.fixed(
                    width = childWidthPx, height = childHeightPx
                )
            )
            layout(parentWidth, parentHeight) {
                placeable.placeRelative(childX, childY)
            }
        }
        .graphicsLayer {
            val rotate = rotation * (-1)
            rotationZ = rotate
            transformOrigin = if (rotate < 0f) {
                TransformOrigin(1f, 0f)
            } else if (rotate > 0f) {
                TransformOrigin(0f, 1f)
            } else {
                TransformOrigin.Center
            }
        }
}

@Composable
fun CustomImagePickerScreen(
    modifier: Modifier,
    viewModel: ImagePickerViewModel,
    selectedImages: Map<Int, Uri>,
    selectedCellIndex: Int?,
    onImageClick: (Uri) -> Unit,
    onRemove: (Uri) -> Unit,
    onCancel: () -> Unit,
) {
    val buckets by viewModel.buckets.collectAsState()
    val currentBucket by viewModel.currentBucket.collectAsState()
    val images by viewModel.images.collectAsState()

    var showSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.loadInitial() }

    Column(modifier) {
        val context = LocalContext.current
        PickerHeaderBar(
            folderName = currentBucket?.name ?: "Gallery",
            canNext = false,
            showSheet = showSheet,
            onNext = { },
            onFolderClick = { showSheet = !showSheet })

        if (showSheet) {
            BucketSheet(
                buckets = buckets,
                currentBucketId = currentBucket?.id,
                modifier = Modifier
                    .height(284.dp)
                    .verticalScroll(rememberScrollState()),
                onSelect = {
                    viewModel.setCurrentBucket(it)
                    showSheet = false
                })
        } else {
            CustomGalleryGrid(
                images = images,
                selectedImages = selectedImages,
                selectedCellIndex = selectedCellIndex,
                modifier = Modifier
                    .height(284.dp)
                    .padding(horizontal = 16.dp),
                onImageClick = { galleryImage ->

                    if (selectedImages.containsValue(galleryImage.uri)) {
                        onRemove(galleryImage.uri)
                        return@CustomGalleryGrid
                    }
                    onImageClick(galleryImage.uri)
                },
                showCameraTile = true,
                maxSelect = viewModel.MAX_SELECT,
                onCameraClick = {
                    val activity = (context as? BaseActivity) ?: return@CustomGalleryGrid
                    val hasCamera = ContextCompat.checkSelfPermission(
                        activity, android.Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED
                    val launchCamera: () -> Unit = {
                        createImageUri(context)?.let { output ->
                            activity.takePicture(output) { success ->
                                if (success) {
                                    onImageClick(output)
                                }
                            }
                        }
                    }
                    if (hasCamera) {
                        launchCamera()
                    } else {
                        activity.requestPermission(android.Manifest.permission.CAMERA) { granted ->
                            if (granted) {
                                launchCamera()
                            }
                        }
                    }
                })
        }
    }
}

@Composable
fun CustomGalleryGrid(
    images: List<com.avnsoft.photoeditor.photocollage.ui.activities.imagepicker.GalleryImage>,
    selectedImages: Map<Int, Uri>,
    selectedCellIndex: Int?,
    onImageClick: (com.avnsoft.photoeditor.photocollage.ui.activities.imagepicker.GalleryImage) -> Unit,
    modifier: Modifier = Modifier,
    showCameraTile: Boolean = true,
    onCameraClick: () -> Unit = {},
    maxSelect: Int = 10
) {
    val canSelectMore = selectedImages.size < maxSelect
    LazyVerticalGrid(
        columns = GridCells.Fixed(3), modifier = modifier.heightIn(max = 999.dp)
    ) {
        if (showCameraTile) {
            item {
                Image(
                    modifier = Modifier
                        .padding(4.dp)
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .clickableWithAlphaEffect(
                            enabled = canSelectMore, onClick = {
                                if (canSelectMore) {
                                    onCameraClick.invoke()
                                }
                            }),
                    painter = painterResource(com.avnsoft.photoeditor.photocollage.R.drawable.ic_camera),
                    contentDescription = "",
                    colorFilter = if (!canSelectMore) androidx.compose.ui.graphics.ColorFilter.tint(
                        Color.Gray.copy(alpha = 0.5f)
                    ) else null
                )
            }
        }
        items(images) { img ->
            Box(
                modifier = Modifier
                    .padding(4.dp)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .clickableWithAlphaEffect() { onImageClick(img) }) {
                AsyncImage(
                    model = img.uri, contentDescription = img.displayName, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()
                )
                // Find cell index for this URI
                val cellIndex = selectedImages.entries.find { it.value == img.uri }?.key
                if (cellIndex != null) {
                    Box(
                        Modifier
                            .align(Alignment.BottomEnd)
                            .padding(6.dp)
                            .size(24.dp)
                            .background(Color(0xFF9747FF), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            (cellIndex + 1).toString(), style = com.avnsoft.photoeditor.photocollage.ui.theme.AppStyle.body2().bold().white()
                        )
                    }
                }
            }
        }
    }
}

