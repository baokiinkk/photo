package com.avnsoft.photoeditor.photocollage.ui.activities.editor.text_sticker

import android.content.Context
import android.graphics.Typeface
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.avnsoft.photoeditor.photocollage.R
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.sticker.lib.Sticker
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.sticker.lib.StickerView
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.text_sticker.lib.AddTextProperties
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.text_sticker.lib.FontItem
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.text_sticker.lib.TextSticker
import com.avnsoft.photoeditor.photocollage.ui.theme.AppColor
import com.avnsoft.photoeditor.photocollage.ui.theme.fontFamily


@Composable
fun TextStickerComposeView2(
    modifier: Modifier,
    stickerView: StickerView
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            stickerView
        },
        update = { stickerView ->

        }
    )
}

@Composable
fun TextStickerComposeView(
    modifier: Modifier,
    input: AddTextProperties? = null,
    onTextStickerEdit: (TextSticker) -> Unit,
    onStickerTouchOutside: (StickerView) -> Unit,
    onResultStickerView: (StickerView) -> Unit
) {
    if (input == null) return
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
            Log.d("aaaaa", "init view nek")

            stickerView.setOnStickerOperationListener(object :
                StickerView.OnStickerOperationListener {
                override fun onStickerAdded(param1Sticker: Sticker) {
                    stickerView.configDefaultIcons()
                }

                override fun onStickerClicked(param1Sticker: Sticker) {
                }

                override fun onStickerDeleted(param1Sticker: Sticker) {

                }

                override fun onTextStickerEdit(param1Sticker: Sticker) {
                    param1Sticker.isShow = false
                    stickerView.setHandlingSticker(null)
                    onTextStickerEdit.invoke(param1Sticker as TextSticker)
                }

                override fun onStickerDoubleTapped(param1Sticker: Sticker) {
                }

                override fun onStickerDragFinished(param1Sticker: Sticker) {
                }

                override fun onStickerFlipped(param1Sticker: Sticker) {
                }

                override fun onStickerTouchOutside(param1Sticker: Sticker?) {
                    param1Sticker?.isShow = true
                    onStickerTouchOutside.invoke(stickerView)
//                    stickerView.replace(
//                        TextSticker(
//                            context,
//                            input
//                        )
//                    )
                }

                override fun onStickerTouchedDown(param1Sticker: Sticker) {
                    stickerView.configDefaultIcons()
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
            onResultStickerView.invoke(stickerView)
            stickerView
        },
        update = { stickerView ->

            Log.d("aaaaa", "add view nek")
            stickerView.addSticker(
                TextSticker(
                    stickerView.context,
                    input,

                    ),
                Sticker.Position.CENTER
            )
        }
    )
}


@Composable
fun rememberFontFromAssets(context: Context, fontPath: String): FontFamily {
    return remember(fontPath) {
        val typeface = Typeface.createFromAsset(context.assets, fontPath)
        FontFamily(typeface)
    }
}

@Composable
fun CustomFontText(
    modifier: Modifier,
    isSelected: Boolean ,
    itemFont: FontItem
) {
    val context = LocalContext.current
    val customFont = rememberFontFromAssets(context, itemFont.fontPath)
    Box(
        modifier = modifier
            .background(AppColor.Gray100, RoundedCornerShape(16.dp))
            .border(
                width = if (isSelected) 1.dp else 0.dp,
                color = if (isSelected) AppColor.Primary500 else Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.grids),
                fontFamily = customFont,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = if (isSelected) AppColor.Primary500 else Color.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = itemFont.fontName,
                fontFamily = fontFamily,
                fontSize = 8.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 10.sp,
                color = if (isSelected) AppColor.Primary500 else AppColor.Gray500,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}

var textStickerStyle = TextStyle(
    fontSize = 14.sp,
    lineHeight = 20.sp,
    fontFamily = fontFamily,
)