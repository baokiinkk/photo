package com.avnsoft.photoeditor.photocollage.ui.activities.freestyle

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.avnsoft.photoeditor.photocollage.ui.activities.freestyle.lib.FreeStyleStickerView

@Composable
fun FreeStyleStickerComposeView(
    view: FreeStyleStickerView,
    modifier: Modifier
) {
    AndroidView(
        modifier = modifier,
        factory = {
            Log.d("ssss","init nek")
            view
        },
        update = {

        }
    )
}