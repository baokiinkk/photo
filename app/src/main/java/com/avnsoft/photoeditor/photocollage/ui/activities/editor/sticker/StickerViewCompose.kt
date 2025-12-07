package com.avnsoft.photoeditor.photocollage.ui.activities.editor.sticker

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.drawable.toDrawable
import androidx.core.net.toUri
import coil.imageLoader
import coil.request.ImageRequest
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.sticker.lib.DrawableSticker
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.sticker.lib.Sticker
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.sticker.lib.StickerView
import com.avnsoft.photoeditor.photocollage.utils.FileUtil.toBitmap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

sealed class StickerData {
    data class StickerFromAsset(
        val pathSticker: String,
        val timestamp: Long = System.currentTimeMillis()
    ) : StickerData()

    data class StickerFromGallery(
        val pathSticker: String,
        val timestamp: Long = System.currentTimeMillis()
    ) : StickerData()
}

@Composable
fun StickerViewCompose(
    modifier: Modifier,
    input: StickerData,
    onReturnView: ((StickerView) -> Unit)? = null,
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            val stickerView = StickerView(context, null)
            stickerView.setShowBorder(true)
            stickerView.setShowIcons(true)
            stickerView.configStickerIcons()
            stickerView.setLocked(false)
            stickerView.setConstrained(true)
            stickerView.setHandlingSticker(null)

            stickerView.setOnStickerOperationListener(object :
                StickerView.OnStickerOperationListener {
                override fun onStickerAdded(param1Sticker: Sticker) {
                    stickerView.configStickerIcons()
                }

                override fun onStickerClicked(param1Sticker: Sticker) {
                }

                override fun onStickerDeleted(param1Sticker: Sticker) {

                }

                override fun onTextStickerEdit(param1Sticker: Sticker) {
                }

                override fun onStickerDoubleTapped(param1Sticker: Sticker) {
                }

                override fun onStickerDragFinished(param1Sticker: Sticker) {
                }

                override fun onStickerFlipped(param1Sticker: Sticker) {
                }

                override fun onStickerTouchOutside(param1Sticker: Sticker?) {
                }

                override fun onStickerTouchedDown(param1Sticker: Sticker) {
                    stickerView.configStickerIcons()
                }

                override fun onStickerZoomFinished(param1Sticker: Sticker) {
                }

                override fun onTouchDownForBeauty(
                    param1Float1: Float,
                    param1Float2: Float
                ) {
                }

                override fun onTouchDragForBeauty(
                    param1Float1: Float,
                    param1Float2: Float
                ) {
                }

                override fun onTouchUpForBeauty(
                    param1Float1: Float,
                    param1Float2: Float
                ) {
                }
            })
            onReturnView?.invoke(stickerView)
            stickerView
        },
        update = { view ->
            when (input) {
                is StickerData.StickerFromAsset -> {
                    if (input.pathSticker.isNotEmpty()) {
                        CoroutineScope(Dispatchers.Main).launch {
                            val bitmap = loadBitmap(view.context, input.pathSticker)
//                            val drawable = loadBitmapFromAssets(
//                                view.context,
//                                input.pathSticker
//                            )
                            view.addSticker(
                                DrawableSticker(bitmap?.toDrawable(view.resources))
                            )
                        }
                    }
                }

                is StickerData.StickerFromGallery -> {
                    val bitmap = input.pathSticker.toUri().toBitmap(view.context)
                    view.addSticker(
                        DrawableSticker(bitmap?.toDrawable(view.resources))
                    )
                }
            }

        }
    )
}

suspend fun loadBitmap(context: Context, url: String): Bitmap? {
    val loader = context.imageLoader
    val request = ImageRequest.Builder(context)
        .data(url)
        .allowHardware(false)
        .build()

    val result = loader.execute(request)
    return (result.drawable as? BitmapDrawable)?.bitmap
}