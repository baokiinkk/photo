package com.avnsoft.photoeditor.photocollage.ui.activities.collage.components

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.avnsoft.photoeditor.photocollage.data.model.collage.CollageTemplate
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.CollageViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

data class CollageScreenHandlers(
    val onToolClick: (CollageTool) -> Unit,
    val onImageEditAction: (ImageEditAction) -> Unit,
    val onImageSwap: (Int, Int) -> Unit,
    val onImageClick: (Int) -> Unit,
    val onAddPhoto: () -> Unit,
    val onReplaceImage: (Int) -> Unit,
    val onDeleteImage: (Int) -> Unit,
    val onCropImage: (Int, Uri) -> Unit
)

@Composable
fun rememberCollageScreenHandlers(
    context: Context,
    viewModel: CollageViewModel,
    currentUris: List<Uri>,
    selectedImageIndex: Int?,
    onShowDeleteDialog: (Int) -> Unit,
    onSetReplaceIndex: (Int) -> Unit,
    onSetSwapMode: (Boolean) -> Unit,
    onSetSelectedIndex: (Int?) -> Unit,
    onLaunchImagePicker: () -> Unit,
    onLaunchReplaceImage: () -> Unit,
    onLaunchCrop: (String) -> Unit
): CollageScreenHandlers {
    val scope = rememberCoroutineScope()

    return remember(viewModel, currentUris, selectedImageIndex) {
        CollageScreenHandlers(
            onToolClick = { tool ->
                when (tool) {
                    CollageTool.GRIDS -> Unit
                    CollageTool.RATIO -> Unit
                    CollageTool.BACKGROUND -> Unit
                    CollageTool.FRAME -> Unit
                    CollageTool.STICKER -> Unit
                    CollageTool.TEXT -> Unit
                    CollageTool.ADD_PHOTO -> {
                        if (currentUris.size < 10) {
                            onLaunchImagePicker()
                        }
                    }
                    else -> Unit
                }
            },
            onImageEditAction = { action ->
                val index = selectedImageIndex ?: return@CollageScreenHandlers
                when (action) {
                    ImageEditAction.REPLACE -> {
                        onSetReplaceIndex(index)
                        onLaunchReplaceImage()
                    }
                    ImageEditAction.SWAP -> {
                        onSetSwapMode(true)
                    }
                    ImageEditAction.CROP -> {
                        val selectedUri = currentUris.getOrNull(index)
                        selectedUri?.let { uri ->
                            scope.launch(Dispatchers.IO) {
                                val path = com.avnsoft.photoeditor.photocollage.ui.activities.editor.copyImageToAppStorage(
                                    context,
                                    uri
                                )
                                path?.let {
                                    scope.launch(Dispatchers.Main) {
                                        onLaunchCrop(it)
                                    }
                                }
                            }
                        }
                    }
                    ImageEditAction.ROTATE -> {
                        viewModel.rotateImage(context, index)
                    }
                    ImageEditAction.FLIP_HORIZONTAL -> {
                        viewModel.flipImageHorizontal(context, index)
                    }
                    ImageEditAction.FLIP_VERTICAL -> {
                        viewModel.flipImageVertical(context, index)
                    }
                    ImageEditAction.DELETE -> {
                        if (currentUris.size > 1) {
                            onShowDeleteDialog(index)
                        }
                    }
                }
            },
            onImageSwap = { firstIndex, secondIndex ->
                if (firstIndex < currentUris.size && secondIndex < currentUris.size) {
                    val newUris = currentUris.toMutableList().apply {
                        val temp = this[firstIndex]
                        this[firstIndex] = this[secondIndex]
                        this[secondIndex] = temp
                    }
                    viewModel.setImageUris(context, newUris)
                    onSetSwapMode(false)
                }
            },
            onImageClick = { index ->
                if (selectedImageIndex == index) {
                    onSetSelectedIndex(null)
                    onSetSwapMode(false)
                } else {
                    onSetSelectedIndex(index)
                }
            },
            onAddPhoto = {
                if (currentUris.size < 10) {
                    onLaunchImagePicker()
                }
            },
            onReplaceImage = { index ->
                onSetReplaceIndex(index)
                onLaunchReplaceImage()
            },
            onDeleteImage = { index ->
                if (currentUris.size > 1 && index < currentUris.size) {
                    viewModel.removeImageUri(index)
                    onSetSelectedIndex(null)
                    viewModel.triggerUnselectAllImages()
                    val newCount = (currentUris.size - 1).coerceAtLeast(1)
                    viewModel.load(newCount)
                }
            },
            onCropImage = { index, uri ->
                scope.launch(Dispatchers.IO) {
                    val path = com.avnsoft.photoeditor.photocollage.ui.activities.editor.copyImageToAppStorage(
                        context,
                        uri
                    )
                    if (path != null) {
                        scope.launch(Dispatchers.Main) {
                            onLaunchCrop(path)
                        }
                    }
                }
            }
        )
    }
}

