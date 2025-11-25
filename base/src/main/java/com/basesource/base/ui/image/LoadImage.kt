package com.basesource.base.ui.image

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import coil.request.ImageRequest


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
        onSuccess = {
            onSuccess?.invoke()
        }
    )
}

