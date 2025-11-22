package com.avnsoft.photoeditor.photocollage.ui.activities.imagepicker

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.avnsoft.photoeditor.photocollage.ui.activities.imagepicker.components.BucketSheet
import com.avnsoft.photoeditor.photocollage.ui.activities.imagepicker.components.GalleryGrid
import com.avnsoft.photoeditor.photocollage.ui.activities.imagepicker.components.PickerHeaderBar
import com.avnsoft.photoeditor.photocollage.ui.activities.imagepicker.components.SelectedBar
import com.avnsoft.photoeditor.photocollage.ui.theme.Background1
import com.basesource.base.ui.base.BaseActivity
import com.basesource.base.utils.requestPermission
import com.basesource.base.utils.takePicture

@Composable
fun ImagePickerScreen(
    dataRequest: ImageRequest? = null,
    viewModel: ImagePickerViewModel = viewModel(),
    onDone: (List<Uri>) -> Unit,
    onCancel: () -> Unit,
) {
    val buckets by viewModel.buckets.collectAsState()
    val currentBucket by viewModel.currentBucket.collectAsState()
    val images by viewModel.images.collectAsState()
    val selected by viewModel.selected.collectAsState()

    var showSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.loadInitial() }

    Box(Modifier
        .fillMaxSize()
        .background(Color.White)) {
        Column(Modifier.fillMaxSize()) {
            val context = LocalContext.current
            PickerHeaderBar(
                folderName = currentBucket?.name ?: "Gallery",
                canNext = selected.isNotEmpty(),
                onBack = onCancel,
                showSheet = showSheet,
                onNext = { onDone(selected) },
                onFolderClick = { showSheet = !showSheet }
            )
            if (showSheet) {
                Box(
                    Modifier
                        .weight(1f)
                        .background(Background1)
                ) {
                    BucketSheet(
                        buckets = buckets,
                        currentBucketId = currentBucket?.id,
                        onSelect = {
                            viewModel.setCurrentBucket(it)
                            showSheet = false
                        }
                    )
                }
            } else {
                GalleryGrid(
                    images = images,
                    selected = selected,
                    modifier = Modifier
                        .weight(1f)
                        .background(Background1),
                    onImageClick = {
                        if (dataRequest?.type == TypeSelect.SINGLE) {
                            onDone.invoke(listOf(it.uri))
                        } else {
                            viewModel.toggleSelect(it)
                        }
                    },
                    showCameraTile = true,
                    maxSelect = viewModel.MAX_SELECT,
                    onCameraClick = {
                        val activity = (context as? BaseActivity) ?: return@GalleryGrid
                        val hasCamera = ContextCompat.checkSelfPermission(
                            activity,
                            Manifest.permission.CAMERA
                        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                        val launchCamera: () -> Unit = {
                            createImageUri(context)?.let { output ->
                                activity.takePicture(output) { success ->
                                    if (success) {
                                        viewModel.toggleSelect(
                                            GalleryImage(
                                                output, currentBucket?.id
                                                    ?: "", null, 0
                                            )
                                        )
                                    }
                                }
                            }
                        }
                        if (hasCamera) {
                            launchCamera()
                        } else {
                            activity.requestPermission(Manifest.permission.CAMERA) { granted ->
                                if (granted) {
                                    launchCamera()
                                }
                            }
                        }
                    }
                )
            }
            SelectedBar(
                selected = selected,
                onRemove = { uri -> viewModel.toggleSelect(GalleryImage(uri, currentBucket?.id ?: "", null, 0)) },
                onClearAll = { selected.toList().forEach { viewModel.toggleSelect(GalleryImage(it, currentBucket?.id ?: "", null, 0)) } }
            )
        }
    }
}

private fun createImageUri(context: Context): Uri? {
    val resolver = context.contentResolver
    val fileName = "IMG_${System.currentTimeMillis()}.jpg"
    val values = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        if (Build.VERSION.SDK_INT >= 29) {
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/AMBPhoto")
            put(MediaStore.Images.Media.IS_PENDING, 0)
        }
    }
    return resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
}
