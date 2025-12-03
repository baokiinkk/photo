package com.avnsoft.photoeditor.photocollage.ui.activities.collage.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.avnsoft.photoeditor.photocollage.data.model.collage.CollageTemplate
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.CollageTemplates
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.CollageViewModel
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.preview.CollagePreview
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.crop.CropAspect.Companion.toAspectRatio
import com.avnsoft.photoeditor.photocollage.ui.theme.BackgroundWhite

@Composable
fun CollagePreviewContainer(
    modifier: Modifier = Modifier,
    viewModel: CollageViewModel,
    collageState: com.avnsoft.photoeditor.photocollage.data.model.collage.CollageState,
    currentUris: List<Uri>,
    selectedImageIndex: Int?,
    isSwapMode: Boolean,
    unselectAllImagesTrigger: Int,
    onImageClick: (Int) -> Unit,
    onImageSwap: (Int, Int) -> Unit,
    onOutsideClick: () -> Unit,
    onUnselectAll: () -> Unit
) {
    val context = LocalContext.current
    val templateToUse = remember(collageState.templateId, currentUris.size) {
        collageState.templateId ?: CollageTemplates.defaultFor(currentUris.size.coerceAtLeast(1))
    }

    val gapValue = remember(collageState.columnMargin) {
        (1 + collageState.columnMargin * 19).dp
    }
    val cornerValue = remember(collageState.cornerRadius) {
        (1 + collageState.cornerRadius * 19).dp
    }

    BoxWithConstraints(
        modifier = modifier
            .then(
                if (collageState.ratio != null) {
                    Modifier.aspectRatio(collageState.ratio.toAspectRatio())
                } else {
                    Modifier.aspectRatio(1f)
                }
            )
            .background(BackgroundWhite)
            .pointerInput(selectedImageIndex) {
                detectTapGestures {
                    if (selectedImageIndex != null) {
                        onUnselectAll()
                    }
                }
            }
    ) {
        val canvasWidth = constraints.maxWidth.toFloat()
        val canvasHeight = constraints.maxHeight.toFloat()

        LaunchedEffect(templateToUse.id, currentUris.size, canvasWidth, canvasHeight) {
            if (templateToUse.id.isNotEmpty() &&
                currentUris.isNotEmpty() &&
                canvasWidth > 0 &&
                canvasHeight > 0
            ) {
                viewModel.resetImageTransforms(
                    context = context,
                    template = templateToUse,
                    canvasWidth = canvasWidth,
                    canvasHeight = canvasHeight,
                    topMarginValue = collageState.topMargin
                )
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            CollagePreview(
                images = currentUris,
                template = templateToUse,
                gap = gapValue,
                corner = cornerValue,
                backgroundSelection = collageState.backgroundSelection,
                imageTransforms = collageState.imageTransforms,
                topMargin = collageState.topMargin,
                imageBitmaps = collageState.imageBitmaps,
                onImageClick = { index, _ ->
                    if (isSwapMode && selectedImageIndex != null && selectedImageIndex != index) {
                        onImageSwap(selectedImageIndex, index)
                    } else {
                        onImageClick(index)
                    }
                },
                onImageTransformsChange = { transforms ->
                    viewModel.updateImageTransforms(transforms)
                },
                unselectAllTrigger = unselectAllImagesTrigger,
                onOutsideClick = onOutsideClick
            )

            collageState.frameSelection
                ?.takeIf { it is FrameSelection.Frame }
                ?.let { frame ->
                    val data = frame as FrameSelection.Frame
                    val url = remember(data.item.urlThumb, data.urlRoot) {
                        if (data.item.urlThumb?.startsWith("http://") == true ||
                            data.item.urlThumb?.startsWith("https://") == true
                        ) {
                            data.item.urlThumb
                        } else {
                            "${data.urlRoot}${data.item.urlThumb}"
                        }
                    }

                    AsyncImage(
                        model = ImageRequest.Builder(context).data(url).build(),
                        contentDescription = null,
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier.fillMaxSize()
                    )
                }
        }
    }
}

