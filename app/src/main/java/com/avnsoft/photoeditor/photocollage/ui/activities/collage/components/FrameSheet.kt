package com.avnsoft.photoeditor.photocollage.ui.activities.collage.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.avnsoft.photoeditor.photocollage.R
import com.avnsoft.photoeditor.photocollage.data.model.frame.FrameCategory
import com.avnsoft.photoeditor.photocollage.data.model.frame.FrameItem
import com.avnsoft.photoeditor.photocollage.data.repository.FrameRepository
import com.avnsoft.photoeditor.photocollage.ui.theme.AppColor.Companion.Gray500
import com.avnsoft.photoeditor.photocollage.ui.theme.AppColor.Companion.Gray900
import com.avnsoft.photoeditor.photocollage.ui.theme.AppStyle
import com.basesource.base.utils.clickableWithAlphaEffect
import org.koin.compose.koinInject

sealed class FrameSelection {
    data class Frame(val item: FrameItem, val category: FrameCategory, val urlRoot: String) : FrameSelection()
    object None : FrameSelection()
}

@Composable
fun FrameSheet(
    selectedFrameSelection: FrameSelection? = null,
    onFrameSelect: (FrameSelection) -> Unit,
    onClose: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val frameRepository: FrameRepository = koinInject()

    var categories by remember { mutableStateOf<List<FrameCategory>?>(emptyList()) }
    var urlRoot by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var selectedCategory by remember { mutableStateOf<FrameCategory?>(null) }

    LaunchedEffect(Unit) {
        isLoading = true
        error = null
        when (val result = frameRepository.getFrames()) {
            is com.basesource.base.result.Result.Success -> {
                categories = result.data.data
                urlRoot = result.data.urlRoot.orEmpty()
                selectedCategory = result.data.data?.firstOrNull()
                isLoading = false
            }
            is com.basesource.base.result.Result.Error -> {
                error = result.exception?.message
                isLoading = false
            }
            else -> {
                isLoading = false
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(Color.White, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.ic_cancel_frame),
                contentDescription = "",
                modifier = Modifier
                    .size(24.dp)
                    .clickableWithAlphaEffect {
                        onFrameSelect.invoke(FrameSelection.None)
                    })
            categories?.forEach { category ->
                Text(
                    text = category.categoryName.orEmpty(),
                    style = AppStyle.caption1().semibold().let {
                        if (selectedCategory == category) it.white() else it.gray500()
                    },
                    modifier = Modifier
                        .background(
                            if (selectedCategory == category) Color(0xFF9747FF) else Color(0xFFF3F4F6),
                            RoundedCornerShape(20.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .clickableWithAlphaEffect {
                            selectedCategory = category
                        }
                )
            }
        }

        // Frame grid
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.loading_frames),
                            style = AppStyle.body1().medium().gray500()
                        )
                    }
                }
                error != null -> {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.error, error.toString()),
                            style = AppStyle.body1().medium().gray500()
                        )
                    }
                }
                selectedCategory == null -> {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.no_frames_available),
                            style = AppStyle.body1().medium().gray500()
                        )
                    }
                }
                else -> {
                    val frames = selectedCategory?.content
                    if (frames?.isNotEmpty() == true) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(frames) { frame ->
                                FrameItemCard(
                                    frameItem = frame,
                                    frameCategory = selectedCategory!!,
                                    urlRoot = urlRoot,
                                    isSelected = selectedFrameSelection is FrameSelection.Frame &&
                                            selectedFrameSelection.item.name == frame.name,
                                    onFrameSelect = { item, category ->
                                        onFrameSelect(FrameSelection.Frame(item, category, urlRoot))
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
        // Bottom Action Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        )
        {
            IconButton(onClick = onClose) {
                Icon(
                    modifier = Modifier.size(28.dp),
                    painter = painterResource(R.drawable.ic_close),
                    contentDescription = stringResource(R.string.close),
                    tint = Gray500
                )
            }

            Text(
                text = stringResource(R.string.color),
                style = AppStyle.title2().semibold().gray900()
            )

            IconButton(onClick = onConfirm) {
                Icon(
                    modifier = Modifier.size(28.dp),
                    painter = painterResource(R.drawable.ic_confirm),
                    contentDescription = stringResource(R.string.confirm),
                    tint = Gray900
                )
            }
        }
    }
}

@Composable
private fun FrameItemCard(
    frameItem: FrameItem,
    frameCategory: FrameCategory,
    urlRoot: String,
    isSelected: Boolean,
    onFrameSelect: ((FrameItem, FrameCategory) -> Unit)? = null
) {
    val context = LocalContext.current
    val imageUri = remember(frameItem.urlThumb, urlRoot) {
        if (frameItem.urlThumb?.startsWith("http://") == true || frameItem.urlThumb?.startsWith("https://") == true) {
            frameItem.urlThumb
        } else {
            "$urlRoot${frameItem.urlThumb}"
        }
    }

    Column(
        modifier = Modifier
            .width(76.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(76.dp)
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(12.dp)
                )
                .then(
                    if (isSelected) {
                        Modifier.border(2.dp, Color(0xFF9747FF), RoundedCornerShape(12.dp))
                    } else {
                        Modifier
                    }
                )
                .clickableWithAlphaEffect {
                    onFrameSelect?.invoke(frameItem, frameCategory)
                },
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(imageUri)
                    .build(),
                contentDescription = frameItem.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
            )

            // Show PRO badge if needed
            if (frameCategory.isPro == true || frameItem.isPro == true) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .background(
                            color = Color(0xFF9747FF),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = stringResource(R.string.pro),
                        style = AppStyle.title1().medium().white(),
                    )
                }
            }
        }

        // Frame title below
        Text(
            text = frameItem.title.orEmpty(),
            style = AppStyle.body2().medium().gray900(),
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

