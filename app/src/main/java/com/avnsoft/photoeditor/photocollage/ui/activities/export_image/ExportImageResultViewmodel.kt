package com.avnsoft.photoeditor.photocollage.ui.activities.export_image

import com.basesource.base.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class ExportImageResultViewmodel : BaseViewModel() {

    val uiState = MutableStateFlow(ExportImageResultUIState())

    fun initData(
        exportImageData: ExportImageData
    ) {
        uiState.update {
            it.copy(
                imageUrl = exportImageData.pathUriMark,
                pathBitmapOriginal = exportImageData.pathBitmapOriginal
            )
        }
    }

    fun removeWatermarkClick() {
        uiState.update {
            it.copy(
                imageUrl = uiState.value.pathBitmapOriginal,
                isMark = false
            )
        }
    }

    fun isMark(): Boolean {
        return uiState.value.isMark
    }
}

data class ExportImageResultUIState(
    val imageUrl: String? = null,
    val pathBitmapOriginal: String? = null,
    val isMark: Boolean = true
)