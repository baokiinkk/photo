package com.basesource.base.ui.image

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.basesource.base.utils.Shimmer

@Composable
fun LoadImageUrl(
    modifier: Modifier = Modifier,
    model: Any?,
    size: Int,
    contentScale: ContentScale = ContentScale.None,
    colorFilter: ColorFilter? = null,
    onSuccess: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val request = ImageRequest.Builder(context)
        .data(model)
        .diskCachePolicy(CachePolicy.ENABLED) // lưu ảnh xuống disk
        .memoryCachePolicy(CachePolicy.ENABLED) // lưu ảnh vào RAM
        .networkCachePolicy(CachePolicy.ENABLED) // dùng cache khi có mạng
        .size(size)
        .build()
    AsyncImage(
        model = request,
        contentDescription = null,
        contentScale = contentScale,
        modifier = modifier,
        colorFilter = colorFilter,
        onSuccess = {
            onSuccess?.invoke()
        }
    )
}

@Composable
fun LoadImage(
    modifier: Modifier = Modifier,
    model: Any?,
    imageDefault: Int? = null,
    contentScale: ContentScale = ContentScale.None,
    error: Painter? = null,
    placeholder: Painter? = null,
    contentDescription: String? = null,
    colorFilter: ColorFilter? = null,
    onSuccess: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(true) }

    val request = ImageRequest.Builder(context)
        .data(model)
        .diskCachePolicy(CachePolicy.ENABLED) // lưu ảnh xuống disk
        .memoryCachePolicy(CachePolicy.ENABLED) // lưu ảnh vào RAM
        .networkCachePolicy(CachePolicy.ENABLED) // dùng cache khi có mạng
        .build()
    AsyncImage(
        model = request,
        contentDescription = contentDescription,
        error = if (imageDefault == null) error else painterResource(id = imageDefault),
        placeholder = if (imageDefault == null) placeholder else painterResource(id = imageDefault),
        contentScale = contentScale,
        modifier = modifier,
        colorFilter = colorFilter,
        onLoading = {
            isLoading = true
        },
        onError = {
            isLoading = false
        },
        onSuccess = {
            isLoading = false
            onSuccess?.invoke()
        }
    )
    if (isLoading) {
        Shimmer(modifier = modifier)
    }
}

