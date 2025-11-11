package com.amb.photo.ui.activities.editor.blur

import android.graphics.Bitmap
import com.amb.photo.R
import com.basesource.base.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class BlurViewModel : BaseViewModel() {


    val uiState = MutableStateFlow(BlurUIState(getShapes()))




    fun selectedItem(item: Shape) {
        uiState.value = uiState.value.copy(shapeId = item.id)
    }

    fun initBitmap(bitmap: Bitmap?) {
        uiState.update {
            it.copy(
                bitmap = bitmap,
                originBitmap = bitmap
            )
        }
    }

    fun updateBlur(blur: Float) {
        uiState.update {
            it.copy(blurBitmap = blur)
        }
    }

    fun updateBlurBrush(blur: Float) {
        uiState.update {
            it.copy(blurBrush = blur)
        }
    }

}

data class BlurUIState(
    val shapes: List<Shape> = emptyList(),
    val shapeId: String = "1",
    val bitmap: Bitmap? = null,
    val originBitmap: Bitmap? = null,
    val blurBitmap: Float = 30f,
    val blurBrush: Float = 0f
)

data class Shape(
    val id: String,
    val text: Int,
    val item: SplashSticker,
    val iconUrl: String
)