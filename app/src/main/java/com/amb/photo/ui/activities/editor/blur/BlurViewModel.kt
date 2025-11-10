package com.amb.photo.ui.activities.editor.blur

import android.graphics.Bitmap
import com.amb.photo.R
import com.basesource.base.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class BlurViewModel : BaseViewModel() {

    fun getShapes(): List<Shape> {
        val mutableList = mutableListOf<Shape>()
        for (index in 1..27) {
            mutableList.add(
                Shape(
                    id = "$index",
                    text = getNameShapeByIndex(index),
                    item = SplashSticker(
                        paramBitmapXor1Path = "pip/pipstyle_$index/mask.webp",
                        paramBitmapOver2Path = "pip/pipstyle_$index/background.webp"
                    ),
                    iconUrl = "pip/pipstyle_$index/preview.webp"
                )
            )
        }
        return mutableList
    }


    val uiState = MutableStateFlow(BlurUIState(getShapes()))


    fun getNameShapeByIndex(index: Int): Int {
        return when (index) {
            1 -> R.string.selfie
            2 -> R.string.memory
            3 -> R.string.heart
            4 -> R.string.fancy
            else -> {
                R.string.selfie
            }
        }
    }

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
            it.copy(blur = blur)
        }
    }

}

data class BlurUIState(
    val shapes: List<Shape> = emptyList(),
    val shapeId: String = "1",
    val bitmap: Bitmap? = null,
    val originBitmap: Bitmap? = null,
    val blur: Float = 30f
)

data class Shape(
    val id: String,
    val text: Int,
    val item: SplashSticker,
    val iconUrl: String
)