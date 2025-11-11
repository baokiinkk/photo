package com.amb.photo.ui.activities.editor.sticker

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.drawable.toDrawable
import com.amb.photo.ui.activities.editor.sticker.lib.DrawableSticker
import com.amb.photo.ui.activities.editor.sticker.lib.Sticker
import com.amb.photo.ui.activities.editor.sticker.lib.StickerAsset.loadBitmapFromAssets
import com.amb.photo.ui.activities.editor.sticker.lib.StickerView

@Composable
fun StickerViewCompose(
    modifier: Modifier,
    pathSticker: String,
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

            stickerView
        },
        update = { view ->
            if (pathSticker.isNotEmpty()){
                val drawable = loadBitmapFromAssets(
                    view.context,
                    pathSticker
                )
                view.addSticker(
                    DrawableSticker(drawable?.toDrawable(view.resources))
                )
            }
        }
    )
}