package com.avnsoft.photoeditor.photocollage.ui.activities.freestyle

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.viewinterop.AndroidView
import com.avnsoft.photoeditor.photocollage.ui.activities.freestyle.lib.FreeStyleStickerView

@Composable
fun FreeStyleStickerComposeView(
    view: FreeStyleStickerView,
    modifier: Modifier
) {
    val density = LocalDensity.current
    
    AndroidView(
        modifier = modifier
            .onGloballyPositioned { coordinates ->
                // Đảm bảo stickerView được constraint khi size thay đổi
                val size = coordinates.size
                view.post {
                    // Force update constraint bounds
                    view.setConstrained(true)
                    view.invalidate()
                }
            },
        factory = {
            Log.d("ssss","init nek")
            // Đảm bảo constraint được bật
            view.setConstrained(true)
            view
        },
        update = { stickerView ->
            // Update constraint khi view được update
            stickerView.setConstrained(true)
        }
    )
}